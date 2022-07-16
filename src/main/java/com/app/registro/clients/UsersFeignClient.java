package com.app.registro.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.registro.requests.Usuario;
import com.app.registro.requests.UsuarioPw;

@FeignClient(name = "app-usuarios")
public interface UsersFeignClient {

	@GetMapping("/users/registroExistencia/")
	public Boolean preguntarUsuarioExiste(@RequestParam(value = "username") String username,
			@RequestParam(value = "email") String email, @RequestParam(value = "cellPhone") String cellPhone);

	@GetMapping("/users/registroCedula/")
	public Boolean registroCedula(@RequestParam(value = "cedula") String cedula);

	@PostMapping("/users/crearRegistro/")
	public Boolean crearUsuarios(@RequestBody Usuario usuario, @RequestParam String password,
			@RequestParam List<String> roles);

	@GetMapping("/users/listar/")
	public List<Usuario> listarUsuarios();

	@GetMapping("/users/listarPw/")
	public List<UsuarioPw> listarUsuariosPw();

}
