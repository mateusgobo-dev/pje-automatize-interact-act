package br.jus.je.pje.entity.vo;

import java.math.BigInteger;

public class JurisdicaoVO {
	
	private int id;
	private String descricao;
	private Boolean admin;
	private BigInteger contador;
	private Boolean temCaixas;


	public JurisdicaoVO() { }
	
	public JurisdicaoVO(int id, String descricao, Boolean admin, BigInteger contador) {
		this(id, descricao, admin, contador, false);
	}

	public JurisdicaoVO(int id, String descricao, Boolean admin, BigInteger contador, Boolean temCaixas) {
		this.id = id;
		this.descricao = descricao;
		this.admin = admin;
		this.contador = contador;
		this.temCaixas = temCaixas;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public BigInteger getContador() {
		return contador;
	}

	public void setContador(BigInteger contador) {
		this.contador = contador;
	}

	public Boolean getTemCaixas() {
		return temCaixas;
	}

	public void setTemCaixas(Boolean temCaixas) {
		this.temCaixas = temCaixas;
	}
	
}
