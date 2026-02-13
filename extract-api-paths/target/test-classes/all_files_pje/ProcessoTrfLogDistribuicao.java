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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.jus.pje.nucleo.entidades.log.Ignore;
import br.jus.pje.nucleo.enums.TipoDistribuicaoEnum;

@Entity
@Table(name = ProcessoTrfLogDistribuicao.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_processo_trf_log")
@Ignore
public class ProcessoTrfLogDistribuicao extends ProcessoTrfLog implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_processo_trf_log_dist";
	private static final long serialVersionUID = 1L;

	private TipoDistribuicaoEnum inTipoDistribuicao;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgadorCargo orgaoJulgadorCargo;

	public ProcessoTrfLogDistribuicao() {
	}

	@Column(name = "in_tipo_distribuicao", length = 2)
	@Enumerated(EnumType.STRING)
	public TipoDistribuicaoEnum getInTipoDistribuicao() {
		return this.inTipoDistribuicao;
	}

	public void setInTipoDistribuicao(TipoDistribuicaoEnum inTipoDistribuicao) {
		this.inTipoDistribuicao = inTipoDistribuicao;
	}	
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_orgao_julgador_colegiado")
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_orgao_julgador_cargo")
	public OrgaoJulgadorCargo getOrgaoJulgadorCargo() {
		return orgaoJulgadorCargo;
	}

	public void setOrgaoJulgadorCargo(OrgaoJulgadorCargo orgaoJulgadorCargo) {
		this.orgaoJulgadorCargo = orgaoJulgadorCargo;
	}

	@Transient
	@Override
	public Class<? extends ProcessoTrfLog> getEntityClass() {
		return ProcessoTrfLogDistribuicao.class;
	}
}
