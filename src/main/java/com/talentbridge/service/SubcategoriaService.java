package com.talentbridge.service;

import com.talentbridge.model.Subcategoria;

import java.util.List;

public interface SubcategoriaService {
    List<Subcategoria> listarPorCategoria(Long categoriaId);
}
