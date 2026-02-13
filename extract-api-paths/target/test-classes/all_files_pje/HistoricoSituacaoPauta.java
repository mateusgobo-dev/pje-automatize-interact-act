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
package br.jus.pje.jt.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.Usuario;


@Entity
@Table(name = HistoricoSituacaoPauta.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_hist_situacao_pauta", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_hist_situacao_pauta"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HistoricoSituacaoPauta implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<HistoricoSituacaoPauta,Integer> {

	public static final String TABLE_NAME = "tb_hist_situacao_pauta";
	private static final long serialVersionUID = 1L;

	private int idHistoricoSituacaoPauta;
	private PautaSessao pautaSessao;
	private TipoSituacaoPauta tipoSituacaoPauta;
	private Date dataSituacaoPauta;
	private Usuario usuarioSituacaoPauta;
	
	public HistoricoSituacaoPauta() {
	}

	@Id
	@GeneratedValue(generator = "gen_hist_situacao_pauta")
	@Column(name = "id_hist_situacao_pauta", unique = true, nullable = false)
	public int getIdHistoricoSituacaoPauta() {
		return this.idHistoricoSituacaoPauta;
	}

	public void setIdHistoricoSituacaoPauta(int idHistoricoSituacaoPauta) {
		this.idHistoricoSituacaoPauta = idHistoricoSituacaoPauta;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pauta_sessao", nullable = false)
	@NotNull
	public PautaSessao getPautaSessao() {
		return pautaSessao;
	}

	public void setPautaSessao(PautaSessao pautaSessao) {
		this.pautaSessao = pautaSessao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_situacao_pauta", nullable = false)
	@NotNull
	public TipoSituacaoPauta getTipoSituacaoPauta() {
		return tipoSituacaoPauta;
	}

	public void setTipoSituacaoPauta(TipoSituacaoPauta tipoSituacaoPauta) {
		this.tipoSituacaoPauta = tipoSituacaoPauta;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_situacao_pauta", nullable = false)
	@NotNull
	public Date getDataSituacaoPauta() {
		return dataSituacaoPauta;
	}

	public void setDataSituacaoPauta(Date dataSituacaoPauta) {
		this.dataSituacaoPauta = dataSituacaoPauta;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_situacao_pauta", nullable = false)
	@NotNull
	public Usuario getUsuarioSituacaoPauta() {
		return usuarioSituacaoPauta;
	}

	public void setUsuarioSituacaoPauta(Usuario usuarioSituacaoPauta) {
		this.usuarioSituacaoPauta = usuarioSituacaoPauta;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HistoricoSituacaoPauta> getEntityClass() {
		return HistoricoSituacaoPauta.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdHistoricoSituacaoPauta());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
