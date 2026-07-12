package com.talentbridge.controller;

import com.talentbridge.dto.AdminCrearServicioDTO;
import com.talentbridge.dto.AdminCrearUsuarioDTO;
import com.talentbridge.model.Categoria;
import com.talentbridge.model.Servicio;
import com.talentbridge.model.Subcategoria;
import com.talentbridge.model.Usuario;
import com.talentbridge.repository.CategoriaRepository;
import com.talentbridge.repository.ServicioRepository;
import com.talentbridge.repository.SubcategoriaRepository;
import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.tipos.EstadoRegistro;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private static final String ADMIN_SESSION = "TALENTBRIDGE_ADMIN";
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "rbak2654";

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final ServicioRepository servicioRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/admin/login")
    public String login() {
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String doLogin(@RequestParam String usuario,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {
        if (ADMIN_USER.equals(usuario) && ADMIN_PASS.equals(password)) {
            session.setAttribute(ADMIN_SESSION, Boolean.TRUE);
            return "redirect:/admin";
        }
        model.addAttribute("error", "Credenciales invalidas.");
        return "admin/login";
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(ADMIN_SESSION);
        return "redirect:/admin/login";
    }

    @GetMapping("/admin")
    public String panel(HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/admin/login";
        cargarModelo(model);
        model.addAttribute("usuarioForm", new AdminCrearUsuarioDTO());
        model.addAttribute("servicioForm", new AdminCrearServicioDTO());
        return "admin/panel";
    }

    @PostMapping("/admin/usuarios")
    public String crearUsuario(@ModelAttribute AdminCrearUsuarioDTO dto,
                               HttpSession session,
                               Model model) {
        if (!esAdmin(session)) return "redirect:/admin/login";
        try {
            Usuario usuario = usuarioRepository.findByEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT))
                    .orElseGet(Usuario::new);
            usuario.setNombre(dto.getNombre().trim());
            usuario.setEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT));
            usuario.setPais("Chile");
            usuario.setCiudad(dto.getCiudad().trim());
            usuario.setNumeroMovil(normalizarTexto(dto.getNumeroMovil()));
            usuario.setRol("OFERENTE");
            usuario.setPassword(passwordEncoder.encode(
                    dto.getPassword() == null || dto.getPassword().isBlank() ? "talentbridge123" : dto.getPassword()));
            usuario.setActivo(true);
            usuario.setVerificado(true);
            usuario.setEstadoRegistro(EstadoRegistro.CORREO_VALIDADO);
            usuario.setAceptaPoliticaPrivacidad(true);
            usuario.setAceptaCondicionesUso(true);
            usuario.setFechaAceptacionPoliticas(LocalDateTime.now());
            usuario.setVersionPoliticas("2026-07-05");
            usuarioRepository.save(usuario);
            model.addAttribute("mensaje", "Usuario creado o actualizado.");
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        cargarModelo(model);
        model.addAttribute("usuarioForm", new AdminCrearUsuarioDTO());
        model.addAttribute("servicioForm", new AdminCrearServicioDTO());
        return "admin/panel";
    }

    @PostMapping("/admin/servicios")
    public String crearServicio(@ModelAttribute AdminCrearServicioDTO dto,
                                HttpSession session,
                                Model model) {
        if (!esAdmin(session)) return "redirect:/admin/login";
        try {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada."));
            Subcategoria subcategoria = subcategoriaRepository.findById(dto.getSubcategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Subcategoria no encontrada."));

            Servicio servicio = new Servicio();
            servicio.setUsuario(usuario);
            servicio.setCategoria(categoria);
            servicio.setSubcategoria(subcategoria);
            servicio.setTitulo(dto.getTitulo().trim());
            servicio.setDescripcion(dto.getDescripcion().trim());
            servicio.setNumeroMovil(normalizarTexto(dto.getNumeroMovil()));
            servicio.setTipoValorizacion(dto.getTipoValorizacion());
            servicio.setValorReferencial(dto.getValorReferencial());
            servicio.setEstadoPublicacion("PUBLICADO");
            servicioRepository.save(servicio);
            model.addAttribute("mensaje", "Servicio publicado.");
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        cargarModelo(model);
        model.addAttribute("usuarioForm", new AdminCrearUsuarioDTO());
        model.addAttribute("servicioForm", new AdminCrearServicioDTO());
        return "admin/panel";
    }

    private void cargarModelo(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("servicios", servicioRepository.findAll());
    }

    private boolean esAdmin(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(ADMIN_SESSION));
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
