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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_proc_trf_pess_mgistrado")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_trf_pess_magistrado", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_trf_pess_magistrado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoTrfPessoaMagistrado implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoTrfPessoaMagistrado,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoTrfPessoaMagistrado;
	private ProcessoTrf processoTrf;
	private PessoaMagistrado pessoaMagistrado;

	public ProcessoTrfPessoaMagistrado() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_trf_pess_magistrado")
	@Column(name = "id_proc_trf_pessoa_magistrado", unique = true, nullable = false)
	public int getIdProcessoTrfPessoaMagistrado() {
		return this.idProcessoTrfPessoaMagistrado;
	}

	public void setIdProcessoTrfPessoaMagistrado(int idProcessoTrfPessoaMagistrado) {
		this.idProcessoTrfPessoaMagistrado = idProcessoTrfPessoaMagistrado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_magistrado")
	public PessoaMagistrado getPessoaMagistrado() {
		return this.pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoTrfPessoaMagistrado)) {
			return false;
		}
		ProcessoTrfPessoaMagistrado other = (ProcessoTrfPessoaMagistrado) obj;
		if (getIdProcessoTrfPessoaMagistrado() != other.getIdProcessoTrfPessoaMagistrado()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoTrfPessoaMagistrado();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoTrfPessoaMagistrado> getEntityClass() {
		return ProcessoTrfPessoaMagistrado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoTrfPessoaMagistrado());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
