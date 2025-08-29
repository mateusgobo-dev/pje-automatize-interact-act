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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.jt.entidades.HabilitacaoAutos;

@Entity
@Table(name = "tb_proc_doc_ptcao_nao_lida")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_doc_pticao_nao_lida", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_doc_pticao_nao_lida"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoDocumentoPeticaoNaoLida implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoDocumentoPeticaoNaoLida,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoDocumentoPeticaoNaoLida;
	private ProcessoDocumento processoDocumento;
	private Boolean retificado;
	private Boolean retirado;
	
	private HabilitacaoAutos habilitacaoAutos;

	public ProcessoDocumentoPeticaoNaoLida() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_doc_pticao_nao_lida")
	@Column(name = "id_proc_doc_peticao_nao_lida", unique = true, nullable = false)
	public int getIdProcessoDocumentoPeticaoNaoLida() {
		return this.idProcessoDocumentoPeticaoNaoLida;
	}

	public void setIdProcessoDocumentoPeticaoNaoLida(int idProcessoDocumentoPeticaoNaoLida) {
		this.idProcessoDocumentoPeticaoNaoLida = idProcessoDocumentoPeticaoNaoLida;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento", nullable = false)
	@NotNull
	public ProcessoDocumento getProcessoDocumento() {
		return this.processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@Column(name = "in_retificado", nullable = false)
	@NotNull
	public Boolean getRetificado() {
		return retificado;
	}

	public void setRetificado(Boolean retificado) {
		this.retificado = retificado;
	}

	@Column(name = "in_retirado", nullable = false)
	@NotNull
	public Boolean getRetirado() {
		return retirado;
	}

	public void setRetirado(Boolean retirado) {
		this.retirado = retirado;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoDocumentoPeticaoNaoLida)) {
			return false;
		}
		ProcessoDocumentoPeticaoNaoLida other = (ProcessoDocumentoPeticaoNaoLida) obj;
		if (getIdProcessoDocumentoPeticaoNaoLida() != other.getIdProcessoDocumentoPeticaoNaoLida()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoDocumentoPeticaoNaoLida();
		return result;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_habilitacao_autos", nullable = true)
	public HabilitacaoAutos getHabilitacaoAutos(){
		return habilitacaoAutos;
	}

	public void setHabilitacaoAutos(HabilitacaoAutos habilitacaoAutos){
		this.habilitacaoAutos = habilitacaoAutos;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumentoPeticaoNaoLida> getEntityClass() {
		return ProcessoDocumentoPeticaoNaoLida.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoDocumentoPeticaoNaoLida());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
