package com.nutrivida.servicios.service;

import com.nutrivida.servicios.dto.DetallePedidoResponse;
import com.nutrivida.servicios.dto.PedidoItemRequest;
import com.nutrivida.servicios.dto.PedidoRequest;
import com.nutrivida.servicios.dto.PedidoResponse;
import com.nutrivida.servicios.exception.RecursoNoEncontradoException;
import com.nutrivida.servicios.exception.ReglaNegocioException;
import com.nutrivida.servicios.model.DetallePedido;
import com.nutrivida.servicios.model.EstadoPedido;
import com.nutrivida.servicios.model.Pedido;
import com.nutrivida.servicios.model.Servicio;
import com.nutrivida.servicios.model.Usuario;
import com.nutrivida.servicios.repository.PedidoRepository;
import com.nutrivida.servicios.repository.ServicioRepository;
import com.nutrivida.servicios.repository.UsuarioRepository;
import com.nutrivida.servicios.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;

    /** Crea el pedido del usuario autenticado: valida stock, descuenta y calcula el total en el servidor. */
    @Transactional
    public PedidoResponse crear(Long idUsuario, PedidoRequest req) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // Une items repetidos y ordena por id (TreeMap) para bloquear siempre en el mismo orden
        Map<Long, Integer> cantidades = new TreeMap<>();
        for (PedidoItemRequest item : req.items()) {
            cantidades.merge(item.idServicio(), item.cantidad(), Integer::sum);
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.CONFIRMADO);

        long total = 0;
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Long idServicio = entrada.getKey();
            int cantidad = entrada.getValue();

            Servicio servicio = servicioRepository.findByIdForUpdate(idServicio)
                    .orElseThrow(() -> new ReglaNegocioException("El servicio " + idServicio + " no existe"));
            if (servicio.getStock() < cantidad) {
                throw new ReglaNegocioException("Stock insuficiente para \"" + servicio.getNombre()
                        + "\" (disponible: " + servicio.getStock() + ")");
            }
            servicio.setStock(servicio.getStock() - cantidad);

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setServicio(servicio);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(servicio.getPrecio()); // precio congelado al momento de la compra
            pedido.getDetalles().add(detalle);

            total += (long) servicio.getPrecio() * cantidad;
        }
        if (total > Integer.MAX_VALUE) {
            throw new ReglaNegocioException("El total del pedido es demasiado alto");
        }
        pedido.setTotal((int) total);
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> misPedidos(Long idUsuario) {
        return pedidoRepository.findByUsuarioIdOrderByFechaDesc(idUsuario).stream()
                .map(this::toResponse).toList();
    }

    /** Un cliente solo ve sus pedidos; para los de otros recibe 404 (no se revela que existen). */
    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long id, UsuarioAutenticado actual) {
        Pedido pedido = buscar(id);
        validarAcceso(pedido, actual);
        return toResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos(EstadoPedido estado) {
        List<Pedido> pedidos = (estado == null)
                ? pedidoRepository.findAllByOrderByFechaDesc()
                : pedidoRepository.findByEstadoOrderByFechaDesc(estado);
        return pedidos.stream().map(this::toResponse).toList();
    }

    /** Uso de administrador y vendedor. Transiciones validas: PENDIENTE->CONFIRMADO->COMPLETADO, o CANCELADO. */
    @Transactional
    public PedidoResponse cambiarEstado(Long id, EstadoPedido nuevo) {
        Pedido pedido = buscar(id);
        validarTransicion(pedido.getEstado(), nuevo);
        aplicarEstado(pedido, nuevo);
        return toResponse(pedidoRepository.save(pedido));
    }

    /** El dueño del pedido (o el personal) puede cancelarlo mientras no este completado. */
    @Transactional
    public PedidoResponse cancelar(Long id, UsuarioAutenticado actual) {
        Pedido pedido = buscar(id);
        validarAcceso(pedido, actual);
        validarTransicion(pedido.getEstado(), EstadoPedido.CANCELADO);
        aplicarEstado(pedido, EstadoPedido.CANCELADO);
        return toResponse(pedidoRepository.save(pedido));
    }

    // ---------- Auxiliares ----------

    private Pedido buscar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado (id " + id + ")"));
    }

    private void validarAcceso(Pedido pedido, UsuarioAutenticado actual) {
        boolean personal = "ADMINISTRADOR".equals(actual.rol()) || "VENDEDOR".equals(actual.rol());
        if (!personal && !pedido.getUsuario().getId().equals(actual.id())) {
            throw new RecursoNoEncontradoException("Pedido no encontrado (id " + pedido.getId() + ")");
        }
    }

    private void validarTransicion(EstadoPedido actual, EstadoPedido nuevo) {
        boolean valida = switch (actual) {
            case PENDIENTE -> nuevo == EstadoPedido.CONFIRMADO || nuevo == EstadoPedido.CANCELADO;
            case CONFIRMADO -> nuevo == EstadoPedido.COMPLETADO || nuevo == EstadoPedido.CANCELADO;
            case COMPLETADO, CANCELADO -> false;
        };
        if (!valida) {
            throw new ReglaNegocioException("No se puede cambiar un pedido de " + actual + " a " + nuevo);
        }
    }

    private void aplicarEstado(Pedido pedido, EstadoPedido nuevo) {
        if (nuevo == EstadoPedido.CANCELADO) {
            for (DetallePedido d : pedido.getDetalles()) { // devuelve el stock
                Servicio servicio = servicioRepository.findByIdForUpdate(d.getServicio().getId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));
                servicio.setStock(servicio.getStock() + d.getCantidad());
            }
        }
        pedido.setEstado(nuevo);
    }

    private PedidoResponse toResponse(Pedido p) {
        List<DetallePedidoResponse> detalles = p.getDetalles().stream()
                .map(d -> new DetallePedidoResponse(
                        d.getServicio().getId(), d.getServicio().getCodigo(), d.getServicio().getNombre(),
                        d.getCantidad(), d.getPrecioUnitario(), d.getCantidad() * d.getPrecioUnitario()))
                .toList();
        Usuario u = p.getUsuario();
        return new PedidoResponse(p.getId(), u.getId(), u.getNombre() + " " + u.getApellido(),
                p.getFecha(), p.getEstado(), p.getTotal(), detalles);
    }
}
