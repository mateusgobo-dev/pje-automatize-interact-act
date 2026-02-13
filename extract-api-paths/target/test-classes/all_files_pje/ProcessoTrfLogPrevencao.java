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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.jus.pje.nucleo.enums.TipoSolicitacaoPrevencaoEnum;

@Entity
@Table(name = ProcessoTrfLogPrevencao.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_processo_trf_log")
public class ProcessoTrfLogPrevencao extends ProcessoTrfLog implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_processo_trf_log_prev";
	private static final long serialVersionUID = 1L;

	List<ProcessoTrfPreventoLog> possiveisPreventos = new ArrayList<ProcessoTrfPreventoLog>(0);
	TipoSolicitacaoPrevencaoEnum inTipoSolicitacaoPrevencao;

	public ProcessoTrfLogPrevencao() {
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrfLogPrevencao")
	public List<ProcessoTrfPreventoLog> getPossiveisPreventos() {
		return this.possiveisPreventos;
	}

	public void setPossiveisPreventos(List<ProcessoTrfPreventoLog> possiveisPreventos) {
		this.possiveisPreventos = possiveisPreventos;
	}

	@Column(name = "in_tipo_prevencao", length = 1)
	@Enumerated(EnumType.STRING)
	public TipoSolicitacaoPrevencaoEnum getInTipoSolicitacaoPrevencao() {
		return this.inTipoSolicitacaoPrevencao;
	}

	public void setInTipoSolicitacaoPrevencao(TipoSolicitacaoPrevencaoEnum inTipoSolicitacaoPrevencao) {
		this.inTipoSolicitacaoPrevencao = inTipoSolicitacaoPrevencao;
	}

	@Transient
	@Override
	public Class<? extends ProcessoTrfLog> getEntityClass() {
		return ProcessoTrfLogPrevencao.class;
	}
}
