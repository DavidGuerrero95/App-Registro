package com.app.registro.services;

import java.util.List;

import com.app.registro.models.Registro;
import com.app.registro.requests.Roles;
import com.app.registro.requests.Usuario;
import com.app.registro.requests.UsuarioPw;

public interface IRegistroService {

	public Usuario crearUsuarios(Registro registro);

	public UsuarioPw crearUsuariosPw(Registro registro);

	public List<String> edicionRoles(List<Roles> rolesEdicion);

	public void crearNuevoUsuario(Registro registro);

	public void eliminarUsuario(String username);

	public void editarCodigo(String username, String codigo, Long minutos);

	public void eliminarTodos();

}
