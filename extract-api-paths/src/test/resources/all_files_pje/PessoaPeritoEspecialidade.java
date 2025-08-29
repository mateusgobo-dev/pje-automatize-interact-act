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

@Entity
@Table(name = "tb_pess_perito_especialida")
@org.hibernate.annotations.GenericGenerator(name = "gen_pess_prto_especialidade", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pess_prto_especialidade"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaPeritoEspecialidade implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaPeritoEspecialidade,Integer> {

	private static final long serialVersionUID = 1L;

	private int idPessoaPeritoEspecialidade;
	private Especialidade especialidade;
	private PessoaPerito pessoaPerito;
	private List<PessoaPeritoDisponibilidade> pessoaPeritoDisponibilidadeList = new ArrayList<PessoaPeritoDisponibilidade>(
			0);
	private List<PessoaPeritoIndisponibilidade> pessoaPeritoIndisponibilidadeList = new ArrayList<PessoaPeritoIndisponibilidade>(
			0);

	public PessoaPeritoEspecialidade() {

	}

	@Id
	@GeneratedValue(generator = "gen_pess_prto_especialidade")
	@Column(name = "id_pessoa_perito_especialidade", unique = true, nullable = false)
	public int getIdPessoaPeritoEspecialidade() {
		return idPessoaPeritoEspecialidade;
	}

	public void setIdPessoaPeritoEspecialidade(int idPessoaPeritoEspecialidade) {
		this.idPessoaPeritoEspecialidade = idPessoaPeritoEspecialidade;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_especialidade")
	public Especialidade getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(Especialidade especialidade) {
		this.especialidade = especialidade;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_perito")
	public PessoaPerito getPessoaPerito() {
		return pessoaPerito;
	}

	public void setPessoaPerito(PessoaPerito pessoaPerito) {
		this.pessoaPerito = pessoaPerito;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "pessoaPeritoEspecialidade")
	public List<PessoaPeritoDisponibilidade> getPessoaPeritoDisponibilidadeList() {
		return pessoaPeritoDisponibilidadeList;
	}

	public void setPessoaPeritoIndisponibilidadeList(
			List<PessoaPeritoIndisponibilidade> pessoaPeritoIndisponibilidadeList) {
		this.pessoaPeritoIndisponibilidadeList = pessoaPeritoIndisponibilidadeList;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "pessoaPeritoEspecialidade")
	public List<PessoaPeritoIndisponibilidade> getPessoaPeritoIndisponibilidadeList() {
		return pessoaPeritoIndisponibilidadeList;
	}

	public void setPessoaPeritoDisponibilidadeList(List<PessoaPeritoDisponibilidade> pessoaPeritoDisponibilidadeList) {
		this.pessoaPeritoDisponibilidadeList = pessoaPeritoDisponibilidadeList;
	}

	@Override
	public String toString() {
		if(especialidade == null){
			return pessoaPerito.getNome();
		}
		else{
			if (especialidade.getEspecialidadePai() == null) {
				return especialidade.getEspecialidade();
			} else {
				return especialidade.getEspecialidadePai() + "-" + especialidade.getEspecialidade();
			}
			
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PessoaPeritoEspecialidade)) {
			return false;
		}
		PessoaPeritoEspecialidade other = (PessoaPeritoEspecialidade) obj;
		if (getIdPessoaPeritoEspecialidade() != other.getIdPessoaPeritoEspecialidade()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPessoaPeritoEspecialidade();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaPeritoEspecialidade> getEntityClass() {
		return PessoaPeritoEspecialidade.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPessoaPeritoEspecialidade());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
