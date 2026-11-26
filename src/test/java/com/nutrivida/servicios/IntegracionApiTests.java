package com.nutrivida.servicios;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integracion: cadena completa HTTP -> seguridad -> servicios -> base de datos MySQL
 * (base "nutrivida_test", creada por Flyway). Cada prueba se revierte al terminar (@Transactional).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IntegracionApiTests {

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private FilterChainProxy filtrosSeguridad;
    @Value("${app.jwt.secret}")
    private String secretoJwt;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto)
                .addFilters((Filter) filtrosSeguridad)
                .build();
    }

    // ---------- utilidades ----------

    private ResultActions llamar(MockHttpServletRequestBuilder peticion, String token, String json) throws Exception {
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        if (json != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mvc.perform(peticion);
    }

    private String cuerpo(ResultActions r) throws Exception {
        return r.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String extraer(String json, String patron) {
        Matcher m = Pattern.compile(patron).matcher(json);
        assertTrue(m.find(), "No se encontró " + patron + " en: " + json);
        return m.group(1);
    }

    private String login(String correo, String clave) throws Exception {
        String json = "{\"correo\":\"" + correo + "\",\"contrasena\":\"" + clave + "\"}";
        ResultActions r = llamar(post("/api/auth/login"), null, json).andExpect(status().isOk());
        return extraer(cuerpo(r), "\"accessToken\":\"([^\"]+)\"");
    }

    private String admin() throws Exception { return login("admin@nutrivida.cl", "Admin123"); }
    private String cliente() throws Exception { return login("cliente@nutrivida.cl", "Cliente123"); }
    private String maria() throws Exception { return login("maria@nutrivida.cl", "Cliente456"); }

    /** Crea un servicio de prueba como admin y devuelve su id. */
    private String crearServicio(String tokenAdmin, int stock) throws Exception {
        String codigo = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        String json = "{\"codigo\":\"" + codigo + "\",\"nombre\":\"Servicio prueba\",\"duracion\":\"30 min\","
                + "\"modalidad\":\"ONLINE_VIDEO\",\"precio\":10000,\"stock\":" + stock
                + ",\"descripcion\":\"Creado por las pruebas\",\"idCategoria\":1}";
        ResultActions r = llamar(post("/api/servicios"), tokenAdmin, json).andExpect(status().isCreated());
        return extraer(cuerpo(r), "\"id\":(\\d+)");
    }

    private String pedidoJson(String idServicio, int cantidad) {
        return "{\"items\":[{\"idServicio\":" + idServicio + ",\"cantidad\":" + cantidad + "}]}";
    }

    // ---------- autenticacion ----------

    @Test
    void loginCorrectoDevuelveTokens() throws Exception {
        String json = "{\"correo\":\"admin@nutrivida.cl\",\"contrasena\":\"Admin123\"}";
        String cuerpo = cuerpo(llamar(post("/api/auth/login"), null, json).andExpect(status().isOk()));
        assertTrue(cuerpo.contains("accessToken") && cuerpo.contains("refreshToken"));
        assertFalse(cuerpo.contains("contrasena"), "La respuesta no debe incluir la contraseña");
    }

    @Test
    void loginIncorrectoDa401() throws Exception {
        String json = "{\"correo\":\"admin@nutrivida.cl\",\"contrasena\":\"mala\"}";
        llamar(post("/api/auth/login"), null, json).andExpect(status().isUnauthorized());
    }

    @Test
    void tresIntentosFallidosBloqueanLaCuenta() throws Exception {
        String mala = "{\"correo\":\"maria@nutrivida.cl\",\"contrasena\":\"mala\"}";
        llamar(post("/api/auth/login"), null, mala).andExpect(status().isUnauthorized());
        llamar(post("/api/auth/login"), null, mala).andExpect(status().isUnauthorized());
        llamar(post("/api/auth/login"), null, mala).andExpect(status().isLocked());
        String buena = "{\"correo\":\"maria@nutrivida.cl\",\"contrasena\":\"Cliente456\"}";
        llamar(post("/api/auth/login"), null, buena).andExpect(status().isLocked());
    }

    @Test
    void refreshRotaElTokenYElAnteriorYaNoSirve() throws Exception {
        String json = "{\"correo\":\"cliente@nutrivida.cl\",\"contrasena\":\"Cliente123\"}";
        String refresh = extraer(cuerpo(llamar(post("/api/auth/login"), null, json)), "\"refreshToken\":\"([^\"]+)\"");
        String cuerpoRefresh = "{\"refreshToken\":\"" + refresh + "\"}";
        llamar(post("/api/auth/refresh"), null, cuerpoRefresh).andExpect(status().isOk());
        llamar(post("/api/auth/refresh"), null, cuerpoRefresh).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevocaElRefreshToken() throws Exception {
        String json = "{\"correo\":\"cliente@nutrivida.cl\",\"contrasena\":\"Cliente123\"}";
        String refresh = extraer(cuerpo(llamar(post("/api/auth/login"), null, json)), "\"refreshToken\":\"([^\"]+)\"");
        String cuerpoRefresh = "{\"refreshToken\":\"" + refresh + "\"}";
        llamar(post("/api/auth/logout"), null, cuerpoRefresh).andExpect(status().isNoContent());
        llamar(post("/api/auth/refresh"), null, cuerpoRefresh).andExpect(status().isUnauthorized());
    }

    @Test
    void registroPublicoSiempreCreaCliente() throws Exception {
        String json = "{\"nombre\":\"Nuevo\",\"apellido\":\"Usuario\",\"correo\":\"nuevo@nutrivida.cl\","
                + "\"contrasena\":\"Clave12345\",\"rol\":\"ADMINISTRADOR\"}";
        String cuerpo = cuerpo(llamar(post("/api/auth/registro"), null, json).andExpect(status().isCreated()));
        assertTrue(cuerpo.contains("\"rol\":\"CLIENTE\""), "El registro debe crear un CLIENTE: " + cuerpo);
    }

    // ---------- tokens y roles ----------

    @Test
    void sinTokenDa401EnRutasProtegidas() throws Exception {
        llamar(get("/api/usuarios"), null, null).andExpect(status().isUnauthorized());
        llamar(get("/api/pedidos/mis-pedidos"), null, null).andExpect(status().isUnauthorized());
        llamar(post("/api/pedidos"), null, pedidoJson("1", 1)).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenManipuladoDa401() throws Exception {
        String[] partes = admin().split("\\.");
        char original = partes[1].charAt(10);
        char distinto = (original == 'A') ? 'B' : 'A';
        String payloadAlterado = partes[1].substring(0, 10) + distinto + partes[1].substring(11);
        String falso = partes[0] + "." + payloadAlterado + "." + partes[2];
        llamar(get("/api/usuarios"), falso, null).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpiradoDa401() throws Exception {
        SecretKey clave = Keys.hmacShaKeyFor(secretoJwt.getBytes(StandardCharsets.UTF_8));
        String expirado = Jwts.builder()
                .subject("1")
                .issuedAt(Date.from(Instant.now().minusSeconds(7200)))
                .expiration(Date.from(Instant.now().minusSeconds(3600)))
                .signWith(clave)
                .compact();
        llamar(get("/api/usuarios"), expirado, null).andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoAccedeAUsuariosPeroAdminSi() throws Exception {
        llamar(get("/api/usuarios"), cliente(), null).andExpect(status().isForbidden());
        llamar(get("/api/usuarios"), admin(), null).andExpect(status().isOk());
    }

    @Test
    void perfilPropioEsAccesibleParaCualquierUsuario() throws Exception {
        String cuerpo = cuerpo(llamar(get("/api/usuarios/me"), cliente(), null).andExpect(status().isOk()));
        assertTrue(cuerpo.contains("cliente@nutrivida.cl"));
    }

    @Test
    void catalogoEsPublicoPeroEscribirRequiereRol() throws Exception {
        llamar(get("/api/servicios"), null, null).andExpect(status().isOk());
        llamar(post("/api/servicios"), cliente(), "{}").andExpect(status().isForbidden());
    }

    // ---------- CRUD ----------

    @Test
    void crudDeCategorias() throws Exception {
        String token = admin();
        String nombre = "Cat-" + UUID.randomUUID().toString().substring(0, 8);
        String json = "{\"nombre\":\"" + nombre + "\"}";

        String id = extraer(cuerpo(llamar(post("/api/categorias"), token, json).andExpect(status().isCreated())),
                "\"id\":(\\d+)");
        llamar(post("/api/categorias"), token, json).andExpect(status().isConflict());
        llamar(put("/api/categorias/" + id), token, "{\"nombre\":\"" + nombre + "-b\"}").andExpect(status().isOk());
        llamar(get("/api/categorias/" + id), null, null).andExpect(status().isOk());
        llamar(delete("/api/categorias/" + id), token, null).andExpect(status().isNoContent());
        llamar(get("/api/categorias/" + id), null, null).andExpect(status().isNotFound());
    }

    @Test
    void crudDeServiciosConValidaciones() throws Exception {
        String token = admin();
        String id = crearServicio(token, 5);
        llamar(get("/api/servicios/" + id), null, null).andExpect(status().isOk());

        String invalido = "{\"codigo\":\"ABC\",\"nombre\":\"X\",\"duracion\":\"1\",\"modalidad\":\"ONLINE_VIDEO\","
                + "\"precio\":-5,\"stock\":1,\"descripcion\":\"d\",\"idCategoria\":1}";
        llamar(post("/api/servicios"), token, invalido).andExpect(status().isBadRequest());

        llamar(delete("/api/servicios/" + id), token, null).andExpect(status().isNoContent());
    }

    // ---------- pedidos e historial ----------

    @Test
    void pedidoDescuentaStockYApareceEnElHistorial() throws Exception {
        String idServicio = crearServicio(admin(), 5);
        String tokenCliente = cliente();

        String pedido = cuerpo(llamar(post("/api/pedidos"), tokenCliente, pedidoJson(idServicio, 2))
                .andExpect(status().isCreated()));
        String idPedido = extraer(pedido, "\"id\":(\\d+)");
        assertTrue(pedido.contains("\"total\":20000"), "El total debe calcularse en el servidor: " + pedido);

        String historial = cuerpo(llamar(get("/api/pedidos/mis-pedidos"), tokenCliente, null).andExpect(status().isOk()));
        assertTrue(historial.contains("\"id\":" + idPedido), "El pedido debe aparecer en el historial");

        String servicio = cuerpo(llamar(get("/api/servicios/" + idServicio), null, null));
        assertTrue(servicio.contains("\"stock\":3"), "El stock debe bajar de 5 a 3: " + servicio);
    }

    @Test
    void pedidoConStockInsuficienteDa400() throws Exception {
        String idServicio = crearServicio(admin(), 1);
        llamar(post("/api/pedidos"), cliente(), pedidoJson(idServicio, 2)).andExpect(status().isBadRequest());
    }

    @Test
    void unClienteNoPuedeVerElPedidoDeOtro() throws Exception {
        String idServicio = crearServicio(admin(), 5);
        String pedido = cuerpo(llamar(post("/api/pedidos"), cliente(), pedidoJson(idServicio, 1))
                .andExpect(status().isCreated()));
        String idPedido = extraer(pedido, "\"id\":(\\d+)");

        llamar(get("/api/pedidos/" + idPedido), maria(), null).andExpect(status().isNotFound());
        llamar(get("/api/pedidos/" + idPedido), cliente(), null).andExpect(status().isOk());
        llamar(get("/api/pedidos/" + idPedido), admin(), null).andExpect(status().isOk());

        String historialMaria = cuerpo(llamar(get("/api/pedidos/mis-pedidos"), maria(), null));
        assertFalse(historialMaria.contains("\"id\":" + idPedido + ","), "El historial de María no debe incluirlo");
    }

    @Test
    void cancelarUnPedidoDevuelveElStock() throws Exception {
        String idServicio = crearServicio(admin(), 5);
        String tokenCliente = cliente();
        String pedido = cuerpo(llamar(post("/api/pedidos"), tokenCliente, pedidoJson(idServicio, 2))
                .andExpect(status().isCreated()));
        String idPedido = extraer(pedido, "\"id\":(\\d+)");

        llamar(patch("/api/pedidos/" + idPedido + "/cancelar"), tokenCliente, null).andExpect(status().isOk());
        String servicio = cuerpo(llamar(get("/api/servicios/" + idServicio), null, null));
        assertTrue(servicio.contains("\"stock\":5"), "El stock debe volver a 5: " + servicio);
        llamar(patch("/api/pedidos/" + idPedido + "/cancelar"), tokenCliente, null).andExpect(status().isBadRequest());
    }

    @Test
    void soloElPersonalListaPedidosYCambiaEstados() throws Exception {
        llamar(get("/api/pedidos"), cliente(), null).andExpect(status().isForbidden());
        llamar(get("/api/pedidos"), admin(), null).andExpect(status().isOk());

        String idServicio = crearServicio(admin(), 5);
        String pedido = cuerpo(llamar(post("/api/pedidos"), cliente(), pedidoJson(idServicio, 1))
                .andExpect(status().isCreated()));
        String idPedido = extraer(pedido, "\"id\":(\\d+)");

        String estado = "{\"estado\":\"COMPLETADO\"}";
        llamar(patch("/api/pedidos/" + idPedido + "/estado"), cliente(), estado).andExpect(status().isForbidden());
        llamar(patch("/api/pedidos/" + idPedido + "/estado"), admin(), estado).andExpect(status().isOk());
        llamar(patch("/api/pedidos/" + idPedido + "/estado"), admin(), "{\"estado\":\"PENDIENTE\"}")
                .andExpect(status().isBadRequest());
    }

    // ---------- reportes ----------

    @Test
    void reportesSoloParaPersonalYConDatosReales() throws Exception {
        llamar(get("/api/reportes/resumen"), cliente(), null).andExpect(status().isForbidden());

        String cuerpo = cuerpo(llamar(get("/api/reportes/resumen?periodo=anio"), admin(), null)
                .andExpect(status().isOk()));
        assertTrue(cuerpo.contains("totalPedidos") && cuerpo.contains("masSolicitados"));
        long total = Long.parseLong(extraer(cuerpo, "\"totalPedidos\":(\\d+)"));
        assertTrue(total > 0, "Con los datos de prueba debe haber pedidos en el último año");

        llamar(get("/api/reportes/resumen?periodo=siglo"), admin(), null).andExpect(status().isBadRequest());
    }

    @Test
    void elReporteSeActualizaAlRegistrarUnPedido() throws Exception {
        String token = admin();
        long antes = Long.parseLong(extraer(
                cuerpo(llamar(get("/api/reportes/resumen?periodo=semana"), token, null)), "\"totalPedidos\":(\\d+)"));
        String idServicio = crearServicio(token, 5);
        llamar(post("/api/pedidos"), cliente(), pedidoJson(idServicio, 1)).andExpect(status().isCreated());
        long despues = Long.parseLong(extraer(
                cuerpo(llamar(get("/api/reportes/resumen?periodo=semana"), token, null)), "\"totalPedidos\":(\\d+)"));
        assertEquals(antes + 1, despues);
    }
}
