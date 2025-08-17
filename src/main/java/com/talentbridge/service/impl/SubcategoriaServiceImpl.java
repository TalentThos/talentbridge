package com.talentbridge.service.impl;

import com.talentbridge.model.Subcategoria;
import com.talentbridge.repository.SubcategoriaRepository;
import com.talentbridge.service.SubcategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubcategoriaServiceImpl implements SubcategoriaService {

    private final SubcategoriaRepository subcategoriaRepository;

    @Override
    public List<Subcategoria> listarPorCategoria(Long categoriaId) {
        return subcategoriaRepository.findByCategoriaId(categoriaId);
    }
}
