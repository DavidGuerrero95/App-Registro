package com.app.registro.requests;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "roles")
@Data
@NoArgsConstructor
public class Roles {

	@Id
	@JsonIgnore
	private String id;

	private String name;

	public Roles(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

}