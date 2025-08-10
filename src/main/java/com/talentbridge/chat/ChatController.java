package com.talentbridge.chat;

import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/{usuarioId}")
    public ResponseEntity<ChatMessage> iniciarChat(@PathVariable Long usuarioId,
                                                   @RequestParam String contenido,
                                                   Principal principal) {
        Usuario remitente = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        ChatMessage mensaje = new ChatMessage();
        mensaje.setSenderId(remitente.getId());
        mensaje.setReceiverId(usuarioId);
        mensaje.setContent(contenido);
        mensaje.setTimestamp(LocalDateTime.now());

        ChatMessage guardado = chatMessageRepository.save(mensaje);
        return ResponseEntity.ok(guardado);
    }
}
