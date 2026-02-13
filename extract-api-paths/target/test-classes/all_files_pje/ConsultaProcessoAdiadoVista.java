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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.jus.pje.nucleo.enums.AdiadoVistaEnum;

@Entity
@Table(name = ConsultaProcessoAdiadoVista.TABLE_NAME)
public class ConsultaProcessoAdiadoVista implements java.io.Serializable {

	public static final String TABLE_NAME = "vs_consulta_proc_adiado_vista";
	private static final long serialVersionUID = 1L;

	private int idProcessoTrf;
	private SessaoPautaProcessoTrf sessaoPautaProcessoTrf;
	private ProcessoTrf processoTrf;
	private Date dataInclusao;
	private AdiadoVistaEnum adiadoVista;
	private Boolean retiradoJulgamento; 
	private Boolean check;
	private Date dataUltimaSessao;

	public ConsultaProcessoAdiadoVista() {
	}

	@Id
	@Column(name = "id_processo_trf", insertable = false, updatable = false)
	public int getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(int idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sessao_pauta_processo_trf", insertable = false, updatable = false)
	public SessaoPautaProcessoTrf getSessaoPautaProcessoTrf() {
		return sessaoPautaProcessoTrf;
	}

	public void setSessaoPautaProcessoTrf(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		this.sessaoPautaProcessoTrf = sessaoPautaProcessoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", insertable = false, updatable = false)
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao_processo", insertable = false, updatable = false)
	public Date getDataInclusao() {
		return dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}

	@Column(name = "in_adiado_vista", length = 2, insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.AdiadoVistaType")
	public AdiadoVistaEnum getAdiadoVista() {
		return this.adiadoVista;
	}

	public void setAdiadoVista(AdiadoVistaEnum adiadoVista) {
		this.adiadoVista = adiadoVista;
	}

	@Column(name = "in_retirado_julgamento", length = 2, insertable = false, updatable = false)
	public Boolean getRetiradoJulgamento() {
		return retiradoJulgamento;
	}

	public void setRetiradoJulgamento(Boolean retiradoJulgamento) {
		this.retiradoJulgamento = retiradoJulgamento;
	}

	@Transient
	public Boolean getCheck() {
		return check;
	}

	public void setCheck(Boolean check) {
		this.check = check;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ConsultaProcessoAdiadoVista)) {
			return false;
		}
		ConsultaProcessoAdiadoVista other = (ConsultaProcessoAdiadoVista) obj;
		if (getIdProcessoTrf() != other.getIdProcessoTrf()) {
			return false;
		}
		return true;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ultima_sessao", insertable = false, updatable = false)
	public Date getDataUltimaSessao() {
		return dataUltimaSessao;
	}

	public void setDataUltimaSessao(Date dataUltimaSessao) {
		this.dataUltimaSessao = dataUltimaSessao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoTrf();
		return result;
	}
}