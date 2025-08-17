package com.talentbridge.api;

import com.talentbridge.model.Subcategoria;
import com.talentbridge.service.SubcategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaRestController {

    private final SubcategoriaService subcategoriaService;

    @GetMapping("/{categoriaId}/subcategorias")
    public List<Subcategoria> listarSubcategorias(@PathVariable Long categoriaId) {
        return subcategoriaService.listarPorCategoria(categoriaId);
    }
}
