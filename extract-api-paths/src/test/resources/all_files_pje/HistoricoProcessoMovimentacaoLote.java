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

import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = HistoricoProcessoMovimentacaoLote.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_hist_proc_moviment_lote", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "SQ_HIST_PROC_MOVIMENT_LOTE"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HistoricoProcessoMovimentacaoLote implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<HistoricoProcessoMovimentacaoLote,Long> {

	public static final String TABLE_NAME = "tb_hist_proc_moviment_lote";
	private static final long serialVersionUID = 1L;

	
	private Long idHistoricoProcessoMovimentacaoLote;
	private HistoricoMovimentacaoLote historicoMovimentacaoLote;
	private Integer idProcesso;
	private String nomeFluxo;
	private Long idTransicao;
	
	private String nomeTarefaOrigem;
	private Long idProcessInstanceOrigem;
	private Long idTaskInstanceOrigem;
	private Long idTaskOrigem;

	
	@Id
	@GeneratedValue(generator = "gen_hist_proc_moviment_lote")
	@Column(name = "id_hist_proc_moviment_lote", unique = true)
	public Long getIdHistoricoProcessoMovimentacaoLote() {
		return idHistoricoProcessoMovimentacaoLote;
	}
	
	public void setIdHistoricoProcessoMovimentacaoLote(Long idHistoricoProcessoMovimentacaoLote){
		this.idHistoricoProcessoMovimentacaoLote = idHistoricoProcessoMovimentacaoLote;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_hist_movimentacao_lote", nullable = true)
	public HistoricoMovimentacaoLote getHistoricoMovimentacaoLote(){
		return historicoMovimentacaoLote;
	}

	public void setHistoricoMovimentacaoLote(HistoricoMovimentacaoLote historicoMovimentacaoLote){
		this.historicoMovimentacaoLote = historicoMovimentacaoLote;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("{0}:{1}:{2}", nomeFluxo, nomeTarefaOrigem, idProcesso);
	}

	
	@Column(name = "id_processo_trf")
	public Integer getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso = idProcesso;
	}
	
	
	@Column(name = "nm_fluxo")
	public String getNomeFluxo() {
		return nomeFluxo;
	}

	public void setNomeFluxo(String nomeFluxo) {
		this.nomeFluxo = nomeFluxo;
	}
	
	@Column(name = "id_transicao")
	public Long getIdTransicao(){
		return idTransicao;
	}

	
	public void setIdTransicao(Long idTransicao){
		this.idTransicao = idTransicao;
	}

	@Column(name = "nm_tarefa_origem")
	public String getNomeTarefaOrigem() {
		return nomeTarefaOrigem;
	}

	public void setNomeTarefaOrigem(String nomeTarefaOrigem) {
		this.nomeTarefaOrigem = nomeTarefaOrigem;
	}

	@Column(name = "id_process_instance_origem")
	public Long getIdProcessInstanceOrigem() {
		return idProcessInstanceOrigem;
	}

	public void setIdProcessInstanceOrigem(Long idProcessInstanceOrigem) {
		this.idProcessInstanceOrigem = idProcessInstanceOrigem;
	}

	@Column(name = "id_task_instance_origem")
	public Long getIdTaskInstanceOrigem() {
		return idTaskInstanceOrigem;
	}

	public void setIdTaskInstanceOrigem(Long idTaskInstanceOrigem) {
		this.idTaskInstanceOrigem = idTaskInstanceOrigem;
	}

	@Column(name = "id_task_origem")
	public Long getIdTaskOrigem() {
		return idTaskOrigem;
	}

	public void setIdTaskOrigem(Long idTaskOrigem) {
		this.idTaskOrigem = idTaskOrigem;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HistoricoProcessoMovimentacaoLote> getEntityClass() {
		return HistoricoProcessoMovimentacaoLote.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdHistoricoProcessoMovimentacaoLote();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
