package com.app.registro.services;

import java.util.List;

import com.app.registro.models.Registro;
import com.app.registro.models.Roles;
import com.app.registro.models.Usuario;
import com.app.registro.models.UsuarioPw;

public interface IRegistroService {

	public Usuario crearUsuarios(Registro registro);

	public UsuarioPw crearUsuariosPw(Registro registro);

	public String codificar(String password);

	public List<String> edicionRoles(List<Roles> rolesEdicion);

}
