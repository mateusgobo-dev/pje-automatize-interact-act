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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = HistoricoMovimentacaoLote.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_hist_movimentacao_lote", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_hist_movimentacao_lote"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HistoricoMovimentacaoLote implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<HistoricoMovimentacaoLote,Long> {

	public static final String TABLE_NAME = "tb_hist_movimentacao_lote";
	private static final long serialVersionUID = 1L;

	
	private Long idHistoricoMovimentacaoLote;
	private String tipoAtividadeLote;
	private Date dataMovimentacao;
	private Integer idUsuario;

	@Id
	@GeneratedValue(generator = "gen_hist_movimentacao_lote")
	@Column(name = "id_hist_movimentacao_lote", unique = true)
	public Long getIdHistoricoMovimentacaoLote() {
		return idHistoricoMovimentacaoLote;
	}

	public void setIdHistoricoMovimentacaoLote(Long idHistoricoMovimentacaoLote) {
		this.idHistoricoMovimentacaoLote = idHistoricoMovimentacaoLote;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_movimentacao")
	public Date getDataMovimentacao() {
		return dataMovimentacao;
	}

	public void setDataMovimentacao(Date dataMovimentacao) {
		this.dataMovimentacao = dataMovimentacao;
	}

	@Column(name = "nm_tipo_atividade_lote")
	public String getTipoAtividadeLote(){
		return tipoAtividadeLote;
	}

	public void setTipoAtividadeLote(String tipoAtividadeLote){
		this.tipoAtividadeLote = tipoAtividadeLote;
	}

	@Column(name = "id_usuario")
	public Integer getIdUsuario(){
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario){
		this.idUsuario = idUsuario;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HistoricoMovimentacaoLote> getEntityClass() {
		return HistoricoMovimentacaoLote.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdHistoricoMovimentacaoLote();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
