package com.app.registro.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.registro.clients.UsersFeignClient;
import com.app.registro.models.Registro;
import com.app.registro.models.Roles;
import com.app.registro.models.Usuario;
import com.app.registro.models.UsuarioPw;

@Service
public class RegistroService implements IRegistroService {

	private final Logger logger = LoggerFactory.getLogger(RegistroService.class);

	@SuppressWarnings("rawtypes")
	@Autowired
	private CircuitBreakerFactory cbFactory;

	@Autowired
	UsersFeignClient uClient;

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

	@Override
	public String codificar(String password) {
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
		logger.info(e.getMessage());
		return false;
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
