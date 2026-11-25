package com.nutrivida.servicios.service;

import com.nutrivida.servicios.dto.CategoriaRequest;
import com.nutrivida.servicios.dto.CategoriaResponse;
import com.nutrivida.servicios.exception.ConflictoException;
import com.nutrivida.servicios.exception.RecursoNoEncontradoException;
import com.nutrivida.servicios.model.Categoria;
import com.nutrivida.servicios.repository.CategoriaRepository;
import com.nutrivida.servicios.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest req) {
        String nombre = req.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe una categoría llamada \"" + nombre + "\"");
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest req) {
        Categoria categoria = buscar(id);
        String nombre = req.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException("Ya existe otra categoría llamada \"" + nombre + "\"");
        }
        categoria.setNombre(nombre);
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscar(id);
        if (servicioRepository.existsByCategoriaId(id)) {
            throw new ConflictoException("No se puede eliminar: la categoría tiene servicios asociados");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada (id " + id + ")"));
    }

    private CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre());
    }
}
