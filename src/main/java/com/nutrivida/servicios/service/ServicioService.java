package com.nutrivida.servicios.service;

import com.nutrivida.servicios.dto.ServicioRequest;
import com.nutrivida.servicios.dto.ServicioResponse;
import com.nutrivida.servicios.exception.ConflictoException;
import com.nutrivida.servicios.exception.RecursoNoEncontradoException;
import com.nutrivida.servicios.exception.ReglaNegocioException;
import com.nutrivida.servicios.model.Categoria;
import com.nutrivida.servicios.model.Servicio;
import com.nutrivida.servicios.repository.CategoriaRepository;
import com.nutrivida.servicios.repository.DetallePedidoRepository;
import com.nutrivida.servicios.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Transactional(readOnly = true)
    public List<ServicioResponse> listar(Long idCategoria, String q) {
        String filtro = (q == null || q.isBlank()) ? null : q.trim();
        return servicioRepository.buscar(idCategoria, filtro).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ServicioResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public ServicioResponse crear(ServicioRequest req) {
        String codigo = normalizarCodigo(req.codigo());
        if (servicioRepository.existsByCodigo(codigo)) {
            throw new ConflictoException("Ya existe un servicio con el código " + codigo);
        }
        Servicio servicio = new Servicio();
        aplicar(servicio, req, codigo);
        return toResponse(servicioRepository.save(servicio));
    }

    @Transactional
    public ServicioResponse actualizar(Long id, ServicioRequest req) {
        Servicio servicio = buscar(id);
        String codigo = normalizarCodigo(req.codigo());
        if (servicioRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new ConflictoException("Ya existe otro servicio con el código " + codigo);
        }
        aplicar(servicio, req, codigo);
        return toResponse(servicioRepository.save(servicio));
    }

    @Transactional
    public void eliminar(Long id) {
        Servicio servicio = buscar(id);
        if (detallePedidoRepository.existsByServicioId(id)) {
            throw new ConflictoException(
                    "No se puede eliminar: el servicio ya forma parte de pedidos. Puedes dejar su stock en 0");
        }
        servicioRepository.delete(servicio);
    }

    private Servicio buscar(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado (id " + id + ")"));
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase();
    }

    private void aplicar(Servicio servicio, ServicioRequest req, String codigo) {
        Categoria categoria = categoriaRepository.findById(req.idCategoria())
                .orElseThrow(() -> new ReglaNegocioException("La categoría indicada no existe"));
        servicio.setCodigo(codigo);
        servicio.setNombre(req.nombre().trim());
        servicio.setDuracion(req.duracion().trim());
        servicio.setModalidad(req.modalidad());
        servicio.setPrecio(req.precio());
        servicio.setStock(req.stock());
        servicio.setDescripcion(req.descripcion().trim());
        servicio.setCategoria(categoria);
    }

    private ServicioResponse toResponse(Servicio s) {
        Categoria c = s.getCategoria();
        return new ServicioResponse(
                s.getId(), s.getCodigo(), s.getNombre(), s.getDuracion(), s.getModalidad(),
                s.getPrecio(), s.getStock(), s.getDescripcion(),
                c != null ? c.getId() : null,
                c != null ? c.getNombre() : null);
    }
}
