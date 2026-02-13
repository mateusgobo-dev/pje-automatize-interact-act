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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_processo_parte_advogado")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_parte_advogado", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_parte_advogado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoParteAdvogado implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoParteAdvogado,Integer> {

	private static final long serialVersionUID = 1L;
	private int idProcessoParteAdvogado;
	private ProcessoParte processoParte;
	private PessoaAdvogado pessoaAdvogado;

	@Id
	@GeneratedValue(generator = "gen_processo_parte_advogado")
	@Column(name = "id_processo_parte_advogado", unique = true, nullable = false)
	public int getIdProcessoParteAdvogado() {
		return this.idProcessoParteAdvogado;
	}

	public void setIdProcessoParteAdvogado(int idProcessoParteAdvogado) {
		this.idProcessoParteAdvogado = idProcessoParteAdvogado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte")
	public ProcessoParte getProcessoParte() {
		return processoParte;
	}

	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_advogado")
	public PessoaAdvogado getPessoaAdvogado() {
		return pessoaAdvogado;
	}

	public void setPessoaAdvogado(PessoaAdvogado pessoaAdvogado) {
		this.pessoaAdvogado = pessoaAdvogado;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoParteAdvogado)) {
			return false;
		}
		ProcessoParteAdvogado other = (ProcessoParteAdvogado) obj;
		if (getIdProcessoParteAdvogado() != other.getIdProcessoParteAdvogado()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoParteAdvogado();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoParteAdvogado> getEntityClass() {
		return ProcessoParteAdvogado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoParteAdvogado());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
