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
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_proc_audiencia_pessoa")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_audiencia_pessoa", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_audiencia_pessoa"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoAudienciaPessoa implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoAudienciaPessoa,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoAudienciaPessoa;
	private ProcessoAudiencia processoAudiencia;
	private Pessoa pessoaRepresentante;
	private Pessoa pessoa;
	private Boolean parteOuvida;
	private Boolean testemunha;

	public ProcessoAudienciaPessoa() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_audiencia_pessoa")
	@Column(name = "id_processo_audiencia_pessoa", unique = true, nullable = false)
	public int getIdProcessoAudienciaPessoa() {
		return idProcessoAudienciaPessoa;
	}

	public void setIdProcessoAudienciaPessoa(int idProcessoAudienciaPessoa) {
		this.idProcessoAudienciaPessoa = idProcessoAudienciaPessoa;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_audiencia", nullable = false)
	@NotNull
	public ProcessoAudiencia getProcessoAudiencia() {
		return this.processoAudiencia;
	}

	public void setProcessoAudiencia(ProcessoAudiencia processoAudiencia) {
		this.processoAudiencia = processoAudiencia;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa_representante", nullable = false, updatable = false)
	@NotNull
	public Pessoa getPessoaRepresentante() {
		return this.pessoaRepresentante;
	}

	public void setPessoaRepresentante(Pessoa pessoaRepresentante) {
		this.pessoaRepresentante = pessoaRepresentante;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaRepresentante(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaRepresentante(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaRepresentante(pessoa.getPessoa());
		} else {
			setPessoaRepresentante((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa", nullable = false, updatable = false)
	@NotNull
	public Pessoa getPessoa() {
		return this.pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	@Column(name = "in_parte_ouvida")
	public Boolean getParteOuvida() {
		return this.parteOuvida;
	}

	public void setParteOuvida(Boolean parteOuvida) {
		this.parteOuvida = parteOuvida;
	}

	@Column(name = "in_testemunha")
	public Boolean getTestemunha() {
		return this.testemunha;
	}

	public void setTestemunha(Boolean testemunha) {
		this.testemunha = testemunha;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoAudienciaPessoa)) {
			return false;
		}
		ProcessoAudienciaPessoa other = (ProcessoAudienciaPessoa) obj;
		if (getIdProcessoAudienciaPessoa() != other.getIdProcessoAudienciaPessoa()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoAudienciaPessoa();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoAudienciaPessoa> getEntityClass() {
		return ProcessoAudienciaPessoa.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoAudienciaPessoa());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
