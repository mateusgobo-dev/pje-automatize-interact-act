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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.LiberacaoConsultaPublicaEnum;

@Entity
@Table(name = "tb_processo_documento_trf")
public class ProcessoDocumentoTrfLocal implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoDocumentoTrfLocal,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoDocumentoTrf;
	private ProcessoDocumento processoDocumento;
	private Boolean decisaoTerminativa = Boolean.FALSE;
	private Boolean exibirDocMinuta = Boolean.FALSE;
	private String codInstanciaOriginal;
	private Boolean liberadoConsultaPublica = Boolean.FALSE;
	private LiberacaoConsultaPublicaEnum liberacaoConsultaPublicaEnum;

	@Column(name = "in_decisao_terminativa", nullable = false)
	@NotNull
	public Boolean getDecisaoTerminativa() {
		return decisaoTerminativa;
	}

	public void setDecisaoTerminativa(Boolean decisaoTerminativa) {
		this.decisaoTerminativa = decisaoTerminativa;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_documento_trf")
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setIdProcessoDocumentoTrf(int idProcessoDocumentoTrf) {
		this.idProcessoDocumentoTrf = idProcessoDocumentoTrf;
	}

	@Id
	@Column(name = "id_processo_documento_trf", unique = true, nullable = false)
	public int getIdProcessoDocumentoTrf() {
		return idProcessoDocumentoTrf;
	}

	@Column(name = "in_exibir_doc_minuta")
	@NotNull
	public Boolean getExibirDocMinuta() {
		return exibirDocMinuta;
	}

	public void setExibirDocMinuta(Boolean exibirDocMinuta) {
		this.exibirDocMinuta = exibirDocMinuta;
	}

	@Column(name = "cd_instancia_origem")
	public String getCodInstanciaOriginal() {
		return codInstanciaOriginal;
	}

	public void setCodInstanciaOriginal(String codInstanciaOriginal) {
		this.codInstanciaOriginal = codInstanciaOriginal;
	}

	@Column(name = "in_liberado_consulta_pub")
	public Boolean getLiberadoConsultaPublica() {
		return liberadoConsultaPublica;
	}

	public void setLiberadoConsultaPublica(Boolean liberadoConsultaPublica) {
		this.liberadoConsultaPublica = liberadoConsultaPublica;
	}

	@Column(name = "tp_liberacao_consulta_pub")
	@Enumerated(EnumType.STRING)
	public LiberacaoConsultaPublicaEnum getLiberacaoConsultaPublicaEnum() {
		return liberacaoConsultaPublicaEnum;
	}

	public void setLiberacaoConsultaPublicaEnum(LiberacaoConsultaPublicaEnum liberacaoConsultaPublicaEnum) {
		this.liberacaoConsultaPublicaEnum = liberacaoConsultaPublicaEnum;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoDocumentoTrfLocal)) {
			return false;
		}
		ProcessoDocumentoTrfLocal other = (ProcessoDocumentoTrfLocal) obj;
		if (getIdProcessoDocumentoTrf() != other.getIdProcessoDocumentoTrf()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoDocumentoTrf();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumentoTrfLocal> getEntityClass() {
		return ProcessoDocumentoTrfLocal.class;
	}
	
	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoDocumentoTrf());
	}
	
	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}