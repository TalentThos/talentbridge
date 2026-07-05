package com.talentbridge.service.impl;

import com.talentbridge.service.CorreoService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class CorreoServiceImpl implements CorreoService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String remitente;

    public void enviarCorreo(String destinatario, String asunto, String contenidoHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setFrom(remitente);
            helper.setText(contenidoHtml, true); // true indica que el contenido es HTML
            mailSender.send(message);
            log.info("Correo enviado a {} desde {}", destinatario, remitente);
        } catch (MessagingException e) {
            log.error("Error al enviar mail", e);
            throw new RuntimeException("Error al enviar correo: " + e.getMessage(), e);
        } catch (MailException e) {
            log.error("Error SMTP al enviar mail a {} desde {}", destinatario, remitente, e);
            throw new RuntimeException("Error al enviar correo: " + e.getMessage(), e);
        }
    }
}

