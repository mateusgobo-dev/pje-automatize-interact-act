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
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_visita")
@org.hibernate.annotations.GenericGenerator(name = "gen_visita", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_visita"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Visita implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Visita,Integer> {

	private static final long serialVersionUID = 1L;

	private int idVisita;
	private Diligencia diligencia;
	private PessoaOficialJustica pessoaOficialJustica;
	private String dsVisita;
	private Date dtVisita;

	private List<ProcessoParteExpedienteVisita> processoParteExpedienteVisitaList = new ArrayList<ProcessoParteExpedienteVisita>(
			0);

	public Visita() {
	}

	@Id
	@GeneratedValue(generator = "gen_visita")
	@Column(name = "id_visita", unique = true, nullable = false)
	public int getIdVisita() {
		return this.idVisita;
	}

	public void setIdVisita(int idVisita) {
		this.idVisita = idVisita;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_diligencia")
	public Diligencia getDiligencia() {
		return diligencia;
	}

	public void setDiligencia(Diligencia diligencia) {
		this.diligencia = diligencia;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_oficial_justica", nullable = false)
	@NotNull
	public PessoaOficialJustica getPessoaOficialJustica() {
		return pessoaOficialJustica;
	}

	public void setPessoaOficialJustica(PessoaOficialJustica pessoaOficialJustica) {
		this.pessoaOficialJustica = pessoaOficialJustica;
	}

	@Column(name = "ds_visita", length = 600)
	@Length(max = 600)
	public String getDsVisita() {
		return this.dsVisita;
	}

	public void setDsVisita(String dsVisita) {
		this.dsVisita = dsVisita;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_visita")
	public Date getDtVisita() {
		return dtVisita;
	}

	public void setDtVisita(Date dtVisita) {
		this.dtVisita = dtVisita;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "visita")
	public List<ProcessoParteExpedienteVisita> getProcessoParteExpedienteVisitaList() {
		return this.processoParteExpedienteVisitaList;
	}

	public void setProcessoParteExpedienteVisitaList(
			List<ProcessoParteExpedienteVisita> processoParteExpedienteVisitaList) {
		this.processoParteExpedienteVisitaList = processoParteExpedienteVisitaList;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Visita)) {
			return false;
		}
		Visita other = (Visita) obj;
		if (getIdVisita() != other.getIdVisita()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdVisita();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Visita> getEntityClass() {
		return Visita.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdVisita());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
