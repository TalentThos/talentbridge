package com.talentbridge.service.impl;

import com.talentbridge.model.Categoria;
import com.talentbridge.repository.CategoriaRepository;
import com.talentbridge.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }
}

