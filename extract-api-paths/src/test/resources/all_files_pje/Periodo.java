/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.jt.entidades.estatistica;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Embeddable
public class Periodo implements Serializable {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = -993423814768201647L;

	private Integer mes;
	 
	private Integer ano;

	public Periodo() {
		super();
	}

	public Periodo(Integer mes, Integer ano) {
		super();
		this.mes = mes;
		this.ano = ano;
	}

	@Column(name = "nr_mes", nullable = false)
	@NotNull
	public Integer getMes() {
		return mes;
	}

	public void setMes(Integer mes) {
		this.mes = mes;
	}

	@Column(name = "nr_ano", nullable = false)
	@NotNull
	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}
	
	@Transient
	public void setPeriodo(Integer mes, Integer ano) {
		this.mes = mes;
		this.ano = ano;
	}

	@Override
	public String toString() {
		return this.mes.toString()+"-"+this.ano.toString();
	}	
	
	 
}
 
