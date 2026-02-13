package br.jus.cnj.pje.webservice.criminal.dto;

import java.io.Serializable;

public class PjeUser implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String username;
	private String password;
	private String name;
	private String email;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean enabled;
	
	public PjeUser() {
		super();
	}

	public PjeUser(String username, String password, boolean enabled, String name, String email) {
		super();
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.name = name;
		this.email = email;
	}

	public String getPassword() {
		return this.password;
	}

	public String getUsername() {
		return this.username;
	}

	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	public boolean isAccountNonLocked() {
		return this.accountNonLocked;
	}

	public boolean isCredentialsNonExpired() {
		return false;
	}

	public boolean isEnabled() {
		return this.enabled;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
}
