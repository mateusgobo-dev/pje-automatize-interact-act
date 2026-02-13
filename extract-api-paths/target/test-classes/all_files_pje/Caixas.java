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
package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = Caixas.TABLE_NAME)
public class Caixas implements java.io.Serializable {

	public static final String TABLE_NAME = "vs_caixas";
	private static final long serialVersionUID = 1L;

	private Integer idTarefa;
	private Integer idCaixa;
	private String nomeCaixa;
	private Long idLocalizacao;
	private Boolean segredoJustica;
	private Long qtdEmCaixa;

	public Caixas() {
	}

	@Column(name = "id_tarefa", insertable = false, updatable = false)
	public Integer getIdTarefa() {
		return idTarefa;
	}

	public void setIdTarefa(Integer idTarefa) {
		this.idTarefa = idTarefa;
	}
	
	@Id
	@Column(name = "id_caixa", insertable = false, updatable = false)
	public Integer getIdCaixa() {
		return idCaixa;
	}
	
	public void setIdCaixa(Integer idCaixa) {
		this.idCaixa = idCaixa;
	}
	
	@Column(name = "nm_caixa", insertable = false, updatable = false)
	public String getNomeCaixa() {
		return nomeCaixa;
	}

	public void setNomeCaixa(String nomeCaixa) {
		this.nomeCaixa = nomeCaixa;
	}

	@Column(name = "id_localizacao", insertable = false, updatable = false)
	public Long getIdLocalizacao() {
		return idLocalizacao;
	}

	public void setIdLocalizacao(Long idLocalizacao) {
		this.idLocalizacao = idLocalizacao;
	}
	
	@Column(name = "in_segredo_justica", insertable = false, updatable = false)
	public Boolean getSegredoJustica() {
		return segredoJustica;
	}
	
	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	@Column(name = "qtd_em_caixa", insertable = false, updatable = false)
	public Long getQtdEmCaixa() {
		return qtdEmCaixa;
	}

	public void setQtdEmCaixa(Long qtdEmCaixa) {
		this.qtdEmCaixa = qtdEmCaixa;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idCaixa == null) ? 0 : idCaixa.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Caixas other = (Caixas) obj;
		if (idCaixa == null) {
			if (other.idCaixa != null)
				return false;
		} else if (!idCaixa.equals(other.idCaixa))
			return false;
		return true;
	}

}