package com.talentbridge.service;

public interface CorreoService {
    void enviarCorreo(String destinatario, String asunto, String contenidoHtml);
}
