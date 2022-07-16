package com.app.registro.models;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "registro")
@Data
@NoArgsConstructor
public class Registro {

	@Id
	@JsonIgnore
	private String id;

	@NotBlank(message = "Username cannot be null")
	@Size(max = 20)
	// @Indexed(unique = true)
	@Pattern(regexp = "[A-Za-z0-9_.-]+", message = "Solo se permite:'_' o '.' o '-'")
	private String username;

	@NotBlank(message = "Password cannot be null")
	@Pattern(regexp = "[^ ]*+", message = "Caracter: ' ' (Espacio en blanco) invalido")
	@Size(min = 6, max = 20, message = "About Me must be between 6 and 20 characters")
	private String password;

	@NotBlank(message = "Cell phone cannot be null")
	@Pattern(regexp = "[0-9]+", message = "Solo numeros")
	@Size(max = 50)
	// @Indexed(unique = true)
	private String cellPhone;

	@NotBlank(message = "Email cannot be null")
	@Size(max = 50)
	@Pattern(regexp = "[^ ]*+", message = "Caracter: ' ' (Espacio en blanco) invalido")
	@Email(message = "Email should be valid")
	// @Indexed(unique = true)
	private String email;

	private String codigo;
	private Long minutos;
	private List<String> roles;

}
