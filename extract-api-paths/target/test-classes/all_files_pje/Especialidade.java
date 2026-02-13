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
@Table(name = Especialidade.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_especialidade", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_especialidade"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Especialidade implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Especialidade,Integer> {

	public static final String TABLE_NAME = "tb_especialidade";
	private static final long serialVersionUID = 1L;

	private int idEspecialidade;
	private String codEspecialidade;
	private String especialidade;
	private Especialidade especialidadePai;
	private Boolean ativo;
	private List<Especialidade> especialidadeList = new ArrayList<Especialidade>(0);
	private List<PessoaPeritoEspecialidade> pessoaPeritoEspecialidadeList = new ArrayList<PessoaPeritoEspecialidade>(0);

	public Especialidade() {
	}

	@Id
	@GeneratedValue(generator = "gen_especialidade")
	@Column(name = "id_especialidade", unique = true, nullable = false)
	public int getIdEspecialidade() {
		return this.idEspecialidade;
	}

	public void setIdEspecialidade(int idEspecialidade) {
		this.idEspecialidade = idEspecialidade;
	}

	@Column(name = "cd_especialidade", length = 30, unique = true)
	@Length(max = 30)
	public String getCodEspecialidade() {
		return this.codEspecialidade;
	}

	public void setCodEspecialidade(String codEspecialidade) {
		this.codEspecialidade = codEspecialidade;
	}

	@Column(name = "ds_especialidade", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getEspecialidade() {
		return this.especialidade;
	}

	public void setEspecialidade(String especialidade) {
		this.especialidade = especialidade;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_especialidade_pai")
	public Especialidade getEspecialidadePai() {
		return this.especialidadePai;
	}

	public void setEspecialidadePai(Especialidade especialidadePai) {
		this.especialidadePai = especialidadePai;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "especialidadePai")
	public List<Especialidade> getEspecialidadeList() {
		return this.especialidadeList;
	}

	public void setEspecialidadeList(List<Especialidade> especialidadeList) {
		this.especialidadeList = especialidadeList;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "especialidade")
	public List<PessoaPeritoEspecialidade> getPessoaPeritoEspecialidadeList() {
		return this.pessoaPeritoEspecialidadeList;
	}

	public void setPessoaPeritoEspecialidadeList(List<PessoaPeritoEspecialidade> pessoaPeritoEspecialidadeList) {
		this.pessoaPeritoEspecialidadeList = pessoaPeritoEspecialidadeList;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return especialidade;
	}

	@Transient
	public Especialidade getEspecialidadeRaiz() {
		if (this.especialidadePai != null) {
			return getEspecialidadePai();
		}
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Especialidade)) {
			return false;
		}
		Especialidade other = (Especialidade) obj;
		if (getIdEspecialidade() != other.getIdEspecialidade()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdEspecialidade();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Especialidade> getEntityClass() {
		return Especialidade.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEspecialidade());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
