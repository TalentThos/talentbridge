package com.talentbridge.service.impl;

import com.talentbridge.model.CodigoVerificacion;
import com.talentbridge.service.VerificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificacionServiceImpl implements VerificacionService {

    public void enviarCodigoVerificacion(String email) {
        /*String codigo = generarCodigo();
        CodigoVerificacion verificacion = new CodigoVerificacion();
        verificacion.setEmail(email);
        verificacion.setCodigo(codigo);
        verificacion.setCreadoEn(LocalDateTime.now());
        verificacion.setExpiraEn(LocalDateTime.now().plusMinutes(10));
        verificacion.setUsado(false);

        codigoRepo.save(verificacion);

        // Enviar por correo
        emailService.enviar(email, "Tu código de verificación", "Tu código es: " + codigo);
        // Aquí deberías implementar la lógica para generar un código único y enviarlo por correo electrónico.
         */
    }


}
