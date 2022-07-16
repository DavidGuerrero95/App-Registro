package com.app.registro.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import com.app.registro.models.Registro;

public interface RegistroRepository extends MongoRepository<Registro, String>{

	@RestResource(path = "find-user")
	public Registro findByUsername(@Param("username") String username);
	
	@RestResource(path = "find-email")
	public Registro findByEmail(@Param("email") String email);
	
	@RestResource(path = "find-cellPhone")
	public Registro findByCellPhone(@Param("cellPhone") String cellPhone);
	
	@RestResource(path = "existe-user-email-cellPhone")
	public Boolean existsByUsernameOrEmailOrCellPhone(@Param("username") String username,
			@Param("username") String email, @Param("username") String cellPhone);
	
	@RestResource(path = "exists-user")
	public Boolean existsByUsername(@Param("username") String username);
	
	@RestResource(path = "exists-email")
	public Boolean existsByEmail(@Param("email") String email);
	
	@RestResource(path = "exists-cellPhone")
	public Boolean existsByCellPhone(@Param("cellPhone") String cellPhone);
	
	@RestResource(path = "delete-username")
	public Boolean deleteByUsername(@Param("username") String username);
}
