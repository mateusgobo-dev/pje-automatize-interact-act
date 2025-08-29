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
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "AplicabilidadeView", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = AplicabilidadeView.TABLE_NAME)
public class AplicabilidadeView implements java.io.Serializable {

	public static final String TABLE_NAME = "vs_aplicabilidade";
	private static final long serialVersionUID = 1L;

	private Long idAplicabilidade;
	private int idOrgaoJustica;
	private String orgaoJustica;
	private int idAplicacaoClasse;
	private String aplicacaoClasse;
	private String codigoAplicacaoClasse;
	private Long idSujeitoAtivo;
	private String sujeitoAtivo;
	
	@Id
	@Column(name = "id_aplicabilidade", insertable = false, updatable = false)
	public Long getIdAplicabilidade() {
		return idAplicabilidade;
	}

	public void setIdAplicabilidade(Long idAplicabilidade) {
		this.idAplicabilidade = idAplicabilidade;
	}

	@Column(name = "id_orgao_justica", insertable = false, updatable = false)
	public int getIdOrgaoJustica() {
		return idOrgaoJustica;
	}

	public void setIdOrgaoJustica(int idOrgaoJustica) {
		this.idOrgaoJustica = idOrgaoJustica;
	}

	@Column(name = "ds_orgao_justica", insertable = false, updatable = false)
	public String getOrgaoJustica() {
		return orgaoJustica;
	}

	public void setOrgaoJustica(String orgaoJustica) {
		this.orgaoJustica = orgaoJustica;
	}

	@Column(name = "id_aplicacao_classe", insertable = false, updatable = false)
	public int getIdAplicacaoClasse() {
		return idAplicacaoClasse;
	}

	public void setIdAplicacaoClasse(int idAplicacaoClasse) {
		this.idAplicacaoClasse = idAplicacaoClasse;
	}

	@Column(name = "ds_aplicacao_classe", insertable = false, updatable = false)
	public String getAplicacaoClasse() {
		return aplicacaoClasse;
	}

	public void setAplicacaoClasse(String aplicacaoClasse) {
		this.aplicacaoClasse = aplicacaoClasse;
	}

	@Column(name = "cd_aplicacao_classe", insertable = false, updatable = false)
	public String getCodigoAplicacaoClasse() {
		return codigoAplicacaoClasse;
	}

	public void setCodigoAplicacaoClasse(String codigoAplicacaoClasse) {
		this.codigoAplicacaoClasse = codigoAplicacaoClasse;
	}

	@Column(name = "id_sujeito_ativo", insertable = false, updatable = false)
	public Long getIdSujeitoAtivo() {
		return idSujeitoAtivo;
	}

	public void setIdSujeitoAtivo(Long idSujeitoAtivo) {
		this.idSujeitoAtivo = idSujeitoAtivo;
	}

	@Column(name = "ds_sujeito_ativo", insertable = false, updatable = false)
	public String getSujeitoAtivo() {
		return sujeitoAtivo;
	}

	public void setSujeitoAtivo(String sujeitoAtivo) {
		this.sujeitoAtivo = sujeitoAtivo;
	}


	public AplicabilidadeView() {
	}
	
	
	@Override
	public String toString() {
		final String SEPARADOR = " - ";
		return getAplicacaoClasse() + " (" + getCodigoAplicacaoClasse()+ ")" + SEPARADOR +getOrgaoJustica() + SEPARADOR + getSujeitoAtivo();
	}

	@Transient
	public String getDescricaoAplicabilidade() {
		final String SEPARADOR = " - ";
		return getAplicacaoClasse() + " (" + getCodigoAplicacaoClasse()+ ")" + SEPARADOR +getOrgaoJustica() + SEPARADOR + getSujeitoAtivo();
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aplicacaoClasse == null) ? 0 : aplicacaoClasse.hashCode());
		result = prime * result + ((orgaoJustica == null) ? 0 : orgaoJustica.hashCode());
		result = prime * result + ((sujeitoAtivo == null) ? 0 : sujeitoAtivo.hashCode());
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
		AplicabilidadeView other = (AplicabilidadeView) obj;
		if (aplicacaoClasse == null) {
			if (other.aplicacaoClasse != null)
				return false;
		} else if (!aplicacaoClasse.equals(other.aplicacaoClasse))
			return false;
		if (orgaoJustica == null) {
			if (other.orgaoJustica != null)
				return false;
		} else if (!orgaoJustica.equals(other.orgaoJustica))
			return false;
		if (sujeitoAtivo == null) {
			if (other.sujeitoAtivo != null)
				return false;
		} else if (!sujeitoAtivo.equals(other.sujeitoAtivo))
			return false;
		return true;
	}

}
