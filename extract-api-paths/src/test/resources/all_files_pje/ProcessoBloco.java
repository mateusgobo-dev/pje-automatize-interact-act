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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_processo_bloco")
@SequenceGenerator(allocationSize = 1, name = "gen_processo_bloco", sequenceName = "sq_tb_processo_bloco")
public class ProcessoBloco implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcessoBloco;
	private TipoVoto votoRelator;
	private Date dataInclusao;
	private Date dataExclusao;
	private BlocoJulgamento bloco;
	private ProcessoTrf processoTrf;
	private Boolean ativo = Boolean.TRUE;
	private ProcessoDocumento certidaoJulgamento;

	public ProcessoBloco() { }

	@Id
	@GeneratedValue(generator = "gen_processo_bloco")
	@Column(name = "id_processo_bloco", nullable = false)
	public int getIdProcessoBloco() {
		return idProcessoBloco;
	}

	public void setIdProcessoBloco(int idProcessoBloco) {
		this.idProcessoBloco = idProcessoBloco;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_bloco_julgamento", nullable = false, updatable = false)
	@NotNull
	public BlocoJulgamento getBloco() {
		return bloco;
	}

	public void setBloco(BlocoJulgamento bloco) {
		this.bloco = bloco;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false, updatable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao_processo")
	public Date getDataInclusao() {
		return dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_exclusao_processo")
	public Date getDataExclusao() {
		return dataExclusao;
	}

	public void setDataExclusao(Date dataExclusao) {
		this.dataExclusao = dataExclusao;
	}
	
	@Column(name = "in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_voto_relator" )
	public TipoVoto getVotoRelator() {
		return votoRelator;
	}

	public void setVotoRelator(TipoVoto votoRelator) {
		this.votoRelator = votoRelator;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento_certidao" )
	public ProcessoDocumento getCertidaoJulgamento() {
		return certidaoJulgamento;
	}

	public void setCertidaoJulgamento(ProcessoDocumento certidaoJulgamento) {
		this.certidaoJulgamento = certidaoJulgamento;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoBloco)) {
			return false;
		}
		ProcessoBloco other = (ProcessoBloco) obj;
		if (getIdProcessoBloco() != other.getIdProcessoBloco()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoBloco();
		return result;
	}

	@Override
	public String toString() {
		if (this.getBloco() != null && this.getProcessoTrf() != null) {
			return this.getBloco() + " - " + this.processoTrf;
		}
		return super.toString();
	}
}
