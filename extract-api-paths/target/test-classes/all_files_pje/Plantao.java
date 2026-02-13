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

import java.sql.Time;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;


@Entity
@Table(name = "tb_plantao")
@org.hibernate.annotations.GenericGenerator(name = "gen_plantao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_plantao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Plantao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Plantao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idPlantao;
	private Pessoa pessoa;
	private Localizacao localizacao;
	private Date dtPlantao;
	private Time horaInicial;
	private Time horaFinal;

	public Plantao() {
	}

	@Id
	@GeneratedValue(generator = "gen_plantao")
	@Column(name = "id_plantao", unique = true, nullable = false)
	public int getIdPlantao() {
		return idPlantao;
	}

	public void setIdPlantao(int idPlantao) {
		this.idPlantao = idPlantao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa", nullable = false)
	@NotNull
	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao", nullable = false)
	@NotNull
	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_plantao", nullable = false)
	@NotNull
	public Date getDtPlantao() {
		return dtPlantao;
	}

	public void setDtPlantao(Date dtPlantao) {
		this.dtPlantao = dtPlantao;
	}

	@Column(name = "dt_hora_inicial")
	public Time getHoraInicial() {
		return horaInicial;
	}

	public void setHoraInicial(Time horaInicial) {
		this.horaInicial = horaInicial;
	}

	@Column(name = "dt_hora_final")
	public Time getHoraFinal() {
		return horaFinal;
	}

	public void setHoraFinal(Time horaFinal) {
		this.horaFinal = horaFinal;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Plantao)) {
			return false;
		}
		Plantao other = (Plantao) obj;
		if (getIdPlantao() != other.getIdPlantao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPlantao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Plantao> getEntityClass() {
		return Plantao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPlantao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
