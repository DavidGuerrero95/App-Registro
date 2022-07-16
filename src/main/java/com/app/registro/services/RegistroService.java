package com.app.registro.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.registro.clients.NotificacionesFeignClient;
import com.app.registro.clients.UsersFeignClient;
import com.app.registro.models.Registro;
import com.app.registro.repository.RegistroRepository;
import com.app.registro.requests.Roles;
import com.app.registro.requests.Usuario;
import com.app.registro.requests.UsuarioPw;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RegistroService implements IRegistroService {

	@SuppressWarnings("rawtypes")
	@Autowired
	private CircuitBreakerFactory cbFactory;

	@Autowired
	RegistroRepository rRepository;

	@Autowired
	UsersFeignClient uClient;

	@Autowired
	NotificacionesFeignClient nClient;

	@Autowired
	PasswordEncoder encoder;

	@Override
	public Usuario crearUsuarios(Registro registro) {
		String cedula = String.valueOf((int) (1000000 * Math.random()));
		String cedula2 = String.valueOf((int) (1000000 * Math.random()));
		Usuario usuario = new Usuario();
		usuario.setUsername(registro.getUsername());
		usuario.setCellPhone(registro.getCellPhone());
		usuario.setEmail(registro.getEmail());
		if (cbFactory.create("registro").run(() -> uClient.registroCedula(cedula), e -> registroCedula2(e)))
			usuario.setCedula(cedula2);
		else
			usuario.setCedula(cedula);
		usuario.setName("");
		usuario.setLastName("");
		usuario.setBirthDate("21/11/2021");
		usuario.setGender(2);
		usuario.setPhone("");
		usuario.setEconomicActivity("");
		usuario.setEconomicData(new ArrayList<String>());
		usuario.setInterests(new ArrayList<String>());
		usuario.setLocation(new ArrayList<Double>(Arrays.asList(6.2678, -75.594037)));
		usuario.setHeadFamily(false);
		usuario.setStakeHolders("");
		return usuario;
	}

	@Override
	public UsuarioPw crearUsuariosPw(Registro registro) {
		List<Roles> roles = new ArrayList<Roles>();
		UsuarioPw usuarioPw = new UsuarioPw();
		usuarioPw.setPassword(registro.getPassword());
		registro.getRoles().forEach(r -> {
			Roles role = new Roles();
			switch (r) {
			case "admin":
				role = new Roles("1", "ROLE_ADMIN");
				roles.add(role);
				break;
			case "mod":
				role = new Roles("2", "ROLE_MODERATOR");
				roles.add(role);
				break;
			default:
				role = new Roles("4", "ROLE_USER");
				roles.add(role);
				break;
			}
		});
		usuarioPw.setRoles(roles);
		return usuarioPw;
	}

	private String codificar(String password) {
		return encoder.encode(password);
	}

	@Override
	public List<String> edicionRoles(List<Roles> rolesEdicion) {
		List<String> roles = new ArrayList<String>();
		rolesEdicion.forEach(r -> {
			switch (r.getId()) {
			case "1":
				roles.add("admin");
				break;
			case "2":
				roles.add("mod");
				break;
			case "4":
				roles.add("inter");
				break;
			default:
				roles.add("user");
				break;
			}
		});
		return roles;
	}

	private Boolean registroCedula2(Throwable e) {
		log.info(e.getMessage());
		return false;
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public void crearNuevoUsuario(Registro registro) {
		Long minutos = new Date().getTime();
		Registro rg = new Registro();
		if (rRepository.existsByUsername(registro.getUsername())) {
			rg = rRepository.findByUsername(registro.getUsername());
			rg.setEmail(registro.getEmail());
			rg.setCellPhone(registro.getCellPhone());
		} else if (rRepository.existsByEmail(registro.getEmail())) {
			rg = rRepository.findByEmail(registro.getEmail());
			rg.setUsername(registro.getUsername());
			rg.setCellPhone(registro.getCellPhone());
		} else if (rRepository.existsByCellPhone(registro.getCellPhone())) {
			rg = rRepository.findByCellPhone(registro.getCellPhone());
			rg.setEmail(registro.getEmail());
			rg.setUsername(registro.getUsername());
		} else {
			rg.setEmail(registro.getEmail());
			rg.setUsername(registro.getUsername());
			rg.setCellPhone(registro.getCellPhone());
		}
		rg.setCodigo(String.valueOf((int) (100000 * Math.random() + 99999)));
		rg.setPassword(codificar(registro.getPassword()));
		rg.setMinutos(minutos);
		if (rg.getRoles() == null) {
			rg.setRoles(new ArrayList<>(Arrays.asList("user")));
		}
		rRepository.save(rg);
		nClient.enviarMensajeSuscripciones(rg.getEmail(), rg.getCodigo());
	}

	@Override
	public void eliminarUsuario(String username) {
		rRepository.deleteByUsername(username);
	}

	@Override
	public void editarCodigo(String username, String codigo, Long minutos) {
		Registro registro = rRepository.findByUsername(username);
		registro.setCodigo(codigo);
		registro.setMinutos(minutos);
		rRepository.save(registro);
	}

	@Override
	public void eliminarTodos() {
		rRepository.deleteAll();
	}

}
