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
package br.jus.pje.nucleo.entidades.lancadormovimento;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Classe que representa orgao da justiça no qual um determinado elemento seja
 * aplicavel.
 */
@Entity
@javax.persistence.Cacheable(true)
@Table(name = OrgaoJustica.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_orgao_justica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_orgao_justica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OrgaoJustica implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<OrgaoJustica,Long> {

	public static final String TABLE_NAME = "tb_orgao_justica";
	private static final long serialVersionUID = 1L;

	private Long idOrgaoJustica;
	private String nome;
	private Boolean ativo;

	public OrgaoJustica() {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_orgao_justica")
	@Column(name = "id_orgao_justica", unique = true, nullable = false)
	public Long getIdOrgaoJustica() {
		return idOrgaoJustica;
	}

	public void setIdOrgaoJustica(Long idOrgaoJustica) {
		this.idOrgaoJustica = idOrgaoJustica;
	}

	@Column(name = "ds_orgao_justica")
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idOrgaoJustica == null) ? 0 : idOrgaoJustica.hashCode());
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
		OrgaoJustica other = (OrgaoJustica) obj;
		if (idOrgaoJustica == null) {
			if (other.getIdOrgaoJustica() != null)
				return false;
		} else if (!idOrgaoJustica.equals(other.getIdOrgaoJustica()))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return nome;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends OrgaoJustica> getEntityClass() {
		return OrgaoJustica.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdOrgaoJustica();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
