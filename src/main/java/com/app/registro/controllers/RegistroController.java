package com.app.registro.controllers;

import java.io.IOException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.app.registro.clients.UsersFeignClient;
import com.app.registro.models.Registro;
import com.app.registro.repository.RegistroRepository;
import com.app.registro.requests.Usuario;
import com.app.registro.services.IRegistroService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class RegistroController {

	@SuppressWarnings("rawtypes")
	@Autowired
	private CircuitBreakerFactory cbFactory;

	@Autowired
	RegistroRepository rRepository;

	@Autowired
	UsersFeignClient uClient;

	@Autowired
	IRegistroService rService;

//  ****************************	REGISTRO 	***********************************  //

	// CREAR REGISTRO
	@PostMapping("/registro/nuevo/")
	@ResponseStatus(code = HttpStatus.CREATED)
	public String crearRegistroCodigo(@RequestBody @Validated Registro registro) {
		if (!(Boolean) cbFactory.create("registro")
				.run(() -> uClient.preguntarUsuarioExiste(registro.getUsername(), registro.getEmail(),
						registro.getCellPhone()),
						e -> preguntarUsuarioExiste2(registro.getUsername(), registro.getEmail(),
								registro.getCellPhone(), e))) {
			rService.crearNuevoUsuario(registro);
			return "Codigo de verificación enviado a su correo";
		}
		throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
	}

	// CONFIRMAR REGISTRO
	@PostMapping("/registro/confirmar/{username}")
	@ResponseStatus(code = HttpStatus.CREATED)
	public String crearUsuario(@PathVariable("username") String username, @RequestParam("codigo") String codigo)
			throws IOException {
		Registro registro = rRepository.findByUsername(username);
		Long minutos = new Date().getTime();
		long diferencia = (Math.abs(registro.getMinutos() - minutos)) / 1000;
		long limit = (600 * 1000) / 1000L;
		if (diferencia <= limit) {
			if (registro.getCodigo().equals(codigo)) {
				Usuario usuario = rService.crearUsuarios(registro);
				registro.setCodigo("0");
				rRepository.save(registro);
				if (cbFactory.create("registro").run(
						() -> uClient.crearUsuarios(usuario, registro.getPassword(), registro.getRoles()),
						e -> crearUsuarios2(e))) {
					rRepository.delete(registro);
					return "Usuario Creado Exitosamente";
				}
			}
			return "Error en la creacion";
		}
		rRepository.delete(registro);
		throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Codigo expirado, intente otra vez");
	}

//  ****************************	FUNCIONES 	***********************************  //

	// ARREGLAR REGISTRO
	@PostMapping("/registro/arreglar/")
	public String arreglarRegistro() {

		return "Error";
	}

//  ****************************	FUNCIONES TOLERANCIA A FALLOS	***********************************  //

	private Boolean preguntarUsuarioExiste2(String username, String email, String cellPhone, Throwable e) {
		log.info(e.getMessage());
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Servicio usuarios no disponible");
	}

	private Boolean crearUsuarios2(Throwable e) {
		log.info(e.getMessage());
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Servicio usuarios no disponible");
	}

}
