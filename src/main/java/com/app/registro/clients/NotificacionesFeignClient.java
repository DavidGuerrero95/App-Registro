package com.app.registro.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "app-notificaciones")
public interface NotificacionesFeignClient {

	@PostMapping("/notificaciones/registro/")
	public void enviarMensajeSuscripciones(@RequestParam("email") String email, @RequestParam("codigo") String codigo);
}
