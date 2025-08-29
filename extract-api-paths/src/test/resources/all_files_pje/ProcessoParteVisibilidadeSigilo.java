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
@Table(name = "tb_proc_parte_visib_sigilo")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_parte_vsblde_sigilo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_parte_vsblde_sigilo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoParteVisibilidadeSigilo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoParteVisibilidadeSigilo,Integer> {

	private static final long serialVersionUID = 1L;
	private int idProcessoParteVisibilidadeSigilo;
	private ProcessoParte processoParte;
	private Pessoa pessoa;

	@Id
	@GeneratedValue(generator = "gen_proc_parte_vsblde_sigilo")
	@Column(name = "id_proc_parte_vsblidade_sigilo", nullable = false)
	public int getIdProcessoParteVisibilidadeSigilo() {
		return idProcessoParteVisibilidadeSigilo;
	}

	public void setIdProcessoParteVisibilidadeSigilo(int idProcessoParteVisibilidadeSigilo) {
		this.idProcessoParteVisibilidadeSigilo = idProcessoParteVisibilidadeSigilo;
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
	@JoinColumn(name = "id_pessoa")
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoParteVisibilidadeSigilo)) {
			return false;
		}
		ProcessoParteVisibilidadeSigilo other = (ProcessoParteVisibilidadeSigilo) obj;
		if (getIdProcessoParteVisibilidadeSigilo() != other.getIdProcessoParteVisibilidadeSigilo()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoParteVisibilidadeSigilo();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoParteVisibilidadeSigilo> getEntityClass() {
		return ProcessoParteVisibilidadeSigilo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoParteVisibilidadeSigilo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
