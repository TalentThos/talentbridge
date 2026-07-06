package com.talentbridge.service.impl;

import com.talentbridge.service.CorreoService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import jakarta.mail.internet.MimeMessage;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CorreoServiceImpl implements CorreoService {

    private final JavaMailSender mailSender;
    private final RestClient restClient = RestClient.create();

    @Value("${spring.mail.from}")
    private String remitente;

    @Value("${spring.mail.from-name}")
    private String remitenteNombre;

    @Value("${spring.mail.provider}")
    private String proveedorCorreo;

    @Value("${brevo.api-key}")
    private String brevoApiKey;

    @Value("${brevo.api-url}")
    private String brevoApiUrl;

    public void enviarCorreo(String destinatario, String asunto, String contenidoHtml) {
        if ("brevo-api".equalsIgnoreCase(proveedorCorreo)) {
            enviarCorreoPorBrevoApi(destinatario, asunto, contenidoHtml);
            return;
        }

        enviarCorreoPorSmtp(destinatario, asunto, contenidoHtml);
    }

    private void enviarCorreoPorSmtp(String destinatario, String asunto, String contenidoHtml) {
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

    private void enviarCorreoPorBrevoApi(String destinatario, String asunto, String contenidoHtml) {
        if (!StringUtils.hasText(brevoApiKey)) {
            throw new RuntimeException("BREVO_API_KEY no está configurada.");
        }

        Map<String, Object> payload = Map.of(
                "sender", Map.of(
                        "name", remitenteNombre,
                        "email", remitente
                ),
                "to", List.of(Map.of("email", destinatario)),
                "subject", asunto,
                "htmlContent", contenidoHtml
        );

        try {
            ResponseEntity<String> response = restClient.post()
                    .uri(brevoApiUrl)
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey)
                    .header("content-type", "application/json")
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class);
            log.info("Correo aceptado por API Brevo a {} desde {} status={} response={}",
                    destinatario, remitente, response.getStatusCode(), response.getBody());
        } catch (RestClientException e) {
            log.error("Error API Brevo al enviar mail a {} desde {}", destinatario, remitente, e);
            throw new RuntimeException("Error al enviar correo por API Brevo: " + e.getMessage(), e);
        }
    }
}

