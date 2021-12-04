package com.app.registro.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.app.registro.models.UsuarioPw;


@FeignClient(name = "app-autenticacion")
public interface AuthFeigClient {

	@PostMapping("/autenticacion/crear")
	public Boolean crearUsuario(@RequestBody UsuarioPw usuarioPw);
}
