package com.app.registro.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.registro.clients.AuthFeigClient;
import com.app.registro.clients.NotificacionesFeignClient;
import com.app.registro.clients.UsersFeignClient;
import com.app.registro.models.Registro;
import com.app.registro.models.Roles;
import com.app.registro.models.Usuario;
import com.app.registro.models.UsuarioPw;
import com.app.registro.repository.RegistroRepository;
import com.app.registro.services.IRegistroService;

@RestController
public class RegistroController {

	private final Logger logger = LoggerFactory.getLogger(RegistroController.class);

	@SuppressWarnings("rawtypes")
	@Autowired
	private CircuitBreakerFactory cbFactory;

	@Autowired
	UsersFeignClient uClient;

	@Autowired
	AuthFeigClient aClient;

	@Autowired
	IRegistroService rService;

	@Autowired
	RegistroRepository rRepository;

	@Autowired
	NotificacionesFeignClient nClient;

	@GetMapping("/registro/listar/")
	public List<Registro> listar() {
		return rRepository.findAll();
	}

	@PostMapping("/registro/crearNuevo/")
	@ResponseStatus(code = HttpStatus.CREATED)
	public ResponseEntity<?> crearRegistroCodigo(@RequestBody @Validated Registro registro) {
		Long minutos = new Date().getTime();
		Registro rg = new Registro();
		if (!(Boolean) cbFactory.create("registro")
				.run(() -> uClient.preguntarUsuarioExiste(registro.getUsername(), registro.getEmail(),
						registro.getCellPhone()),
						e -> preguntarUsuarioExiste2(registro.getUsername(), registro.getEmail(),
								registro.getCellPhone(), e))) {
			
			if(rRepository.existsByUsername(registro.getUsername())) rg = rRepository.findByUsername(registro.getUsername());
			else if(rRepository.existsByEmail(registro.getEmail())) rg = rRepository.findByEmail(registro.getEmail());
			else if(rRepository.existsByCellPhone(registro.getCellPhone())) rg = rRepository.findByCellPhone(registro.getCellPhone());
			else {
				rg.setEmail(registro.getEmail());
				rg.setUsername(registro.getUsername());
				rg.setCellPhone(registro.getCellPhone());
			}
			rg.setCodigo(String.valueOf((int) (100000 * Math.random() + 99999)));
			rg.setPassword(rService.codificar(registro.getPassword()));
			rg.setMinutos(minutos);
			if (rg.getRoles() == null) {
				rg.setRoles(new ArrayList<>(Arrays.asList("user")));
			}
			rRepository.save(rg);
			nClient.enviarMensajeSuscripciones(rg.getEmail(), rg.getCodigo());
			return ResponseEntity.ok("Codigo de verificación enviado a su correo");
		}
		return ResponseEntity.badRequest().body("Usuario Ya existe");
	}

	@PostMapping("/registro/registro/confirmar/{username}")
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
						e -> crearUsuarios2(username, e))) {
					return "Usuario Creado Exitosamente";
				}
			}
			return "Error en la creacion";
		}
		rRepository.delete(registro);
		return "Codigo expirado, intente otra vez";
	}

	@DeleteMapping("/registro/eliminar/")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public Boolean eliminarUsuario(@RequestBody UsuarioPw usuario) throws IOException {
		try {
			rRepository.delete(rRepository.findByUsername(usuario.getUsername()));
			return true;
		} catch (Exception e) {
			throw new IOException("Error: " + e.getMessage());
		}
	}

	@PutMapping("/registro/editar/")
	public Boolean editarUsuarioRegistro(@RequestParam String username, @RequestParam String uEdit,
			@RequestParam String cEdit, @RequestParam String eEdit, @RequestParam String newPassword,
			@RequestParam List<Roles> rolesEdicion) throws IOException {
		try {
			Registro registro = rRepository.findByUsername(username);
			if (!uEdit.isEmpty() && cEdit.isEmpty() && eEdit.isEmpty() && newPassword.isEmpty()
					&& rolesEdicion.isEmpty())
				registro.setUsername(uEdit);
			else if (uEdit.isEmpty() && !cEdit.isEmpty() && eEdit.isEmpty() && newPassword.isEmpty()
					&& rolesEdicion.isEmpty())
				registro.setCellPhone(cEdit);
			else if (uEdit.isEmpty() && cEdit.isEmpty() && !eEdit.isEmpty() && newPassword.isEmpty()
					&& rolesEdicion.isEmpty())
				registro.setEmail(eEdit);
			else if (uEdit.isEmpty() && cEdit.isEmpty() && eEdit.isEmpty() && !newPassword.isEmpty()
					&& rolesEdicion.isEmpty())
				registro.setPassword(newPassword);
			else if (uEdit.isEmpty() && cEdit.isEmpty() && eEdit.isEmpty() && newPassword.isEmpty()
					&& !rolesEdicion.isEmpty()) {
				List<String> roles = rService.edicionRoles(rolesEdicion);
				registro.setRoles(roles);
			}
			rRepository.save(registro);
			return true;
		} catch (Exception e) {
			throw new IOException("Error: " + e.getMessage());
		}
	}

	@PutMapping("/registro/editar/codigo/")
	public void editarUsuario(@RequestParam String username, @RequestParam String codigo, @RequestParam Long minutos)
			throws IOException {
		try {
			Registro registro = rRepository.findByUsername(username);
			registro.setCodigo(codigo);
			registro.setMinutos(minutos);
			rRepository.save(registro);
		} catch (Exception e) {
			throw new IOException("Error: " + e.getMessage());
		}

	}

	private Boolean preguntarUsuarioExiste2(String username, String email, String cellPhone, Throwable e) {
		logger.info(e.getMessage());
		if (rRepository.existsByUsernameOrEmailOrCellPhone(username, email, cellPhone))
			return true;
		return false;
	}

	private Boolean crearUsuarios2(String username, Throwable e) {
		try {
			logger.info(e.getMessage());
			Registro registro = rRepository.findByUsername(username);
			UsuarioPw usuarioPw = rService.crearUsuariosPw(registro);
			usuarioPw.setAttempts(0);
			usuarioPw.setUsername(username);
			usuarioPw.setEnabled(true);
			aClient.crearUsuario(usuarioPw);
			logger.info("Creado correctaemente");
			return true;
		} catch (Exception e2) {
			logger.info(e2.getMessage());
			return false;
		}
	}

	@PostMapping("/registro/arreglar/")
	public String arreglarRegistro() {
		List<Usuario> lUser = cbFactory.create("registro").run(() -> uClient.listarUsuarios(),
				e -> errorArreglarUser(e));
		if (lUser != null) {
			List<Registro> lReg = listar();
			if (lUser.size() != lReg.size()) {
				List<UsuarioPw> lPw = uClient.listarUsuariosPw();
				List<String> uUser = new ArrayList<String>();
				lUser.forEach(l -> uUser.add(l.getUsername()));
				List<String> uReg = new ArrayList<String>();
				lReg.forEach(l -> uReg.add(l.getUsername()));
				uUser.forEach(u -> {
					if (!uReg.contains(u)) {
						Registro registro = new Registro();
						registro.setUsername(u);
						registro.setCellPhone(lUser.get(uUser.indexOf(u)).getCellPhone());
						registro.setEmail(lUser.get(uUser.indexOf(u)).getEmail());
						registro.setCodigo("0");
						registro.setMinutos(0L);
						registro.setPassword(lPw.get(uUser.indexOf(u)).getPassword());
						registro.setRoles(rService.edicionRoles(lPw.get(uUser.indexOf(u)).getRoles()));
						rRepository.save(registro);
					}
				});
				return "ok";
			}
			return "sin errores";
		}
		return "Error";
	}
	
	@GetMapping("/registro/ver/")
	public List<Registro> verRegistro() {
		return rRepository.findAll();
	}
	
	@DeleteMapping("/registro/borrar/")
	@ResponseStatus(code = HttpStatus.OK)
	public void borrarLista() {
		rRepository.deleteAll();
	}

	private List<Usuario> errorArreglarUser(Throwable e) {
		logger.info("Error: " + e.getMessage());
		return null;
	}

}
