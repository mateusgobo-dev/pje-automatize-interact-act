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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = GrupoOficialJustica.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_grupo_oficial_justica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_grupo_oficial_justica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class GrupoOficialJustica implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<GrupoOficialJustica,Integer> {

	public static final String TABLE_NAME = "tb_grupo_oficial_justica";
	private static final long serialVersionUID = 1L;

	private int idGrupoOficialJustica;
	private String grupoOficialJustica;
	private CentralMandado centralMandado;
	private Boolean ativo = Boolean.TRUE;

	private GrupoOficialJustica grupoOficialInstancia;

	private List<PessoaGrupoOficialJustica> pessoaGrupoOficialJusticaList = new ArrayList<>(0);

	public GrupoOficialJustica() {
	}

	@Id
	@GeneratedValue(generator = "gen_grupo_oficial_justica")
	@Column(name = "id_grupo_oficial_justica", unique = true, nullable = false)
	public int getIdGrupoOficialJustica() {
		return this.idGrupoOficialJustica;
	}

	public void setIdGrupoOficialJustica(int idGrupoOficialJustica) {
		this.idGrupoOficialJustica = idGrupoOficialJustica;
	}

	@Column(name = "ds_grupo_oficial_justica", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getGrupoOficialJustica() {
		return this.grupoOficialJustica;
	}

	public void setGrupoOficialJustica(String grupoOficialJustica) {
		this.grupoOficialJustica = grupoOficialJustica;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_central_mandado")
	public CentralMandado getCentralMandado() {
		return centralMandado;
	}

	public void setCentralMandado(CentralMandado centralMandado) {
		this.centralMandado = centralMandado;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "grupoOficialJustica")
	public List<PessoaGrupoOficialJustica> getPessoaGrupoOficialJusticaList() {
		return pessoaGrupoOficialJusticaList;
	}

	public void setPessoaGrupoOficialJusticaList(List<PessoaGrupoOficialJustica> pessoaGrupoOficialJusticaList) {
		this.pessoaGrupoOficialJusticaList = pessoaGrupoOficialJusticaList;
	}

	@Transient
	public GrupoOficialJustica getGrupoOficialInstancia() {
		return grupoOficialInstancia;
	}

	public void setGrupoOficialInstancia(GrupoOficialJustica grupoOficialInstancia) {
		this.grupoOficialInstancia = grupoOficialInstancia;
	}

	@Override
	public String toString() {
		return grupoOficialJustica;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof GrupoOficialJustica)) {
			return false;
		}
		GrupoOficialJustica other = (GrupoOficialJustica) obj;
		if (getIdGrupoOficialJustica() != other.getIdGrupoOficialJustica()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdGrupoOficialJustica();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends GrupoOficialJustica> getEntityClass() {
		return GrupoOficialJustica.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdGrupoOficialJustica());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
