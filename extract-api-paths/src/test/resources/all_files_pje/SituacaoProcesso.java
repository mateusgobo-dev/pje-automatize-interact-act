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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;

import br.jus.pje.nucleo.entidades.filters.SituacaoProcessoFilter;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;

@Entity
@Table(name = SituacaoProcesso.TABLE_NAME)
@FilterDefs(value = {
	@FilterDef(name = SituacaoProcessoFilter.FILTER_LOCALIZACAO_SERVIDOR, parameters = {
			@ParamDef(type = SituacaoProcessoFilter.TYPE_INT, name = SituacaoProcessoFilter.FILTER_PARAM_IDS_LOCALIZACOES_FISICAS_FILHAS),
			@ParamDef(type = SituacaoProcessoFilter.TYPE_INT, name = SituacaoProcessoFilter.FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO),
			@ParamDef(type = SituacaoProcessoFilter.TYPE_BOOLEAN, name = SituacaoProcessoFilter.FILTER_PARAM_SERVIDOR_EXCLUSIVO_COLEGIADO),
		}),
		
	@FilterDef(name = SituacaoProcessoFilter.FILTER_PAPEL_LOCALIZACAO_FLUXO, 
		parameters = {
			@ParamDef(type = SituacaoProcessoFilter.TYPE_INT, name = SituacaoProcessoFilter.FILTER_PARAM_ID_LOCALIZACAO_MODELO),
			@ParamDef(type = SituacaoProcessoFilter.TYPE_INT, name = SituacaoProcessoFilter.FILTER_PARAM_ID_PAPEL) 
		}),
	@FilterDef(name = SituacaoProcessoFilter.FILTER_ORGAO_JULGADOR_COLEGIADO, 
	parameters = { 
		@ParamDef(type = SituacaoProcessoFilter.TYPE_INT, name = SituacaoProcessoFilter.FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO) 
	}),
	@FilterDef(name = SituacaoProcessoFilter.FILTER_ORGAO_JULGADOR_CARGO, 
		parameters = { 
			@ParamDef(type = SituacaoProcessoFilter.TYPE_INT, name = SituacaoProcessoFilter.FILTER_PARAM_ID_USUARIO_LOCALIZACAO),
			@ParamDef(type = SituacaoProcessoFilter.TYPE_DATE, name = SituacaoProcessoFilter.FILTER_PARAM_DATA_ATUAL)
		}),
	@FilterDef(name = SituacaoProcessoFilter.FILTER_CARGO, 
		parameters = {
			@ParamDef(type = SituacaoProcessoFilter.TYPE_INT, name = SituacaoProcessoFilter.FILTER_PARAM_ID_USUARIO_LOCALIZACAO),
			@ParamDef(type = SituacaoProcessoFilter.TYPE_DATE, name = SituacaoProcessoFilter.FILTER_PARAM_DATA_ATUAL) 
		}),
	@FilterDef(name = SituacaoProcessoFilter.FILTER_SEGREDO_JUSTICA, 
		parameters = { 
			@ParamDef(type = SituacaoProcessoFilter.TYPE_INT, name = SituacaoProcessoFilter.FILTER_PARAM_ID_USUARIO), 
			@ParamDef(type = SituacaoProcessoFilter.TYPE_BOOLEAN, name = SituacaoProcessoFilter.FILTER_PARAM_VISUALIZA_SIGILOSO),
			@ParamDef(type = SituacaoProcessoFilter.TYPE_INT, name = SituacaoProcessoFilter.FILTER_PARAM_IDS_LOCALIZACOES_FISICAS_FILHAS),
			@ParamDef(type = SituacaoProcessoFilter.TYPE_INT, name = SituacaoProcessoFilter.FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO),
			@ParamDef(type = SituacaoProcessoFilter.TYPE_BOOLEAN, name = SituacaoProcessoFilter.FILTER_PARAM_SERVIDOR_EXCLUSIVO_COLEGIADO),
		}),
	@FilterDef(name = SituacaoProcessoFilter.FILTER_COMPETENCIA,
		parameters = {
			@ParamDef(type = SituacaoProcessoFilter.TYPE_INT, name = "idCompetencia")
		}),
	@FilterDef(name = SituacaoProcessoFilter.FILTER_NUMERO_PROCESSO,
		parameters = {
			@ParamDef(type = SituacaoProcessoFilter.TYPE_STRING, name = "numeroProcesso")
		})
	})
@Filters(value = {
		@Filter(name = SituacaoProcessoFilter.FILTER_PAPEL_LOCALIZACAO_FLUXO, condition = SituacaoProcessoFilter.CONDITION_PAPEL_LOCALIZACAO_FLUXO),
		@Filter(name = SituacaoProcessoFilter.FILTER_CARGO, condition = SituacaoProcessoFilter.CONDITION_CARGO),
		@Filter(name = SituacaoProcessoFilter.FILTER_ORGAO_JULGADOR_CARGO, condition = SituacaoProcessoFilter.CONDITION_ORGAO_JULGADOR_CARGO),
		@Filter(name = SituacaoProcessoFilter.FILTER_ORGAO_JULGADOR_COLEGIADO, condition = SituacaoProcessoFilter.CONDITION_ORGAO_COLEGIADO),
		@Filter(name = SituacaoProcessoFilter.FILTER_LOCALIZACAO_SERVIDOR, condition = SituacaoProcessoFilter.CONDITION_LOCALIZACAO_SERVIDOR),
		@Filter(name = SituacaoProcessoFilter.FILTER_SEGREDO_JUSTICA, condition = SituacaoProcessoFilter.CONDITION_SEGREDO_JUSTICA),
		@Filter(name = SituacaoProcessoFilter.FILTER_COMPETENCIA, condition = SituacaoProcessoFilter.CONDITION_COMPETENCIA),
		@Filter(name = SituacaoProcessoFilter.FILTER_NUMERO_PROCESSO, condition = SituacaoProcessoFilter.CONDITION_NUMERO_PROCESSO)
})

public class SituacaoProcesso implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_processo_tarefa";
	private static final long serialVersionUID = 1L;

	private Long id;
	private String pooledActor;
	private String nomeFluxo;
	private String nomeTarefa;
	private String nomeCaixa;
	private Integer idTarefa;
	private Integer idCaixa;
	private Integer idLote;
	private Integer idCargo;
	private Integer idProcesso;
	private Long idProcessInstance;
	private Long idTaskInstance;
	private Long idTask;
	private String actorId;
	private Long idLocalizacao;
	private Long idOrgaoJulgadoColegiado;
	private Boolean segredoJustica;
	private ProcessoTrfApreciadoEnum segredoApreciado;
	private Integer idOrgaoJulgadorCargo;
	private Date dataChegadaTarefa;
	private Long idPreviousTask;
	private ProcessoTrf processoTrf;
	private Integer prioridade;
	private Tarefa tarefa;
	private ConsultaProcessoTrfSemFiltro cabecalhoProcesso;

	@Id
	@Column(name = "id_processo_tarefa", insertable = false, updatable = false)
	public Long getIdSituacaoProcesso() {
		return id;
	}

	public void setIdSituacaoProcesso(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return MessageFormat.format("{0}:{1}:{2}:{3}", nomeFluxo, nomeTarefa, nomeCaixa, idProcesso);
	}

	@Transient
	public String getPooledActor() {
		return pooledActor;
	}

	public void setPooledActor(String pooledActor) {
		this.pooledActor = pooledActor;
	}

	@Column(name = "nm_fluxo", insertable = false, updatable = false)
	public String getNomeFluxo() {
		return nomeFluxo;
	}

	public void setNomeFluxo(String nomeFluxo) {
		this.nomeFluxo = nomeFluxo;
	}

	@Column(name = "nm_tarefa", insertable = false, updatable = false)
	public String getNomeTarefa() {
		return nomeTarefa;
	}

	public void setNomeTarefa(String nomeTarefa) {
		this.nomeTarefa = nomeTarefa;
	}

	@Column(name = "nm_caixa", insertable = false, updatable = false)
	public String getNomeCaixa() {
		return nomeCaixa;
	}

	public void setNomeCaixa(String nomeCaixa) {
		this.nomeCaixa = nomeCaixa;
	}

	@Column(name = "id_processo_trf", insertable = false, updatable = false)
	public Integer getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso = idProcesso;
	}

	@Column(name = "id_process_instance", insertable = false, updatable = false)
	public Long getIdProcessInstance() {
		return idProcessInstance;
	}

	public void setIdProcessInstance(Long idProcessInstance) {
		this.idProcessInstance = idProcessInstance;
	}

	@Column(name = "id_task_instance", insertable = false, updatable = false)
	public Long getIdTaskInstance() {
		return idTaskInstance;
	}

	public void setIdTaskInstance(Long idTaskInstance) {
		this.idTaskInstance = idTaskInstance;
	}

	@Column(name = "id_task", insertable = false, updatable = false)
	public Long getIdTask() {
		return idTask;
	}

	public void setIdTask(Long idTask) {
		this.idTask = idTask;
	}

	@Column(name = "nm_actorid", insertable = false, updatable = false)
	public String getActorId() {
		return actorId;
	}

	public void setActorId(String actorId) {
		this.actorId = actorId;
	}

	public void setIdCaixa(Integer idCaixa) {
		this.idCaixa = idCaixa;
	}

	@Column(name = "id_caixa", insertable = false, updatable = false)
	public Integer getIdCaixa() {
		return idCaixa;
	}

	@Column(name = "id_localizacao", insertable = false, updatable = false)
	public Long getIdLocalizacao() {
		return idLocalizacao;
	}

	public void setIdLocalizacao(Long idLocalizacao) {
		this.idLocalizacao = idLocalizacao;
	}
	
	@Column(name = "id_orgao_julgador_colegiado", insertable = false, updatable = false)
	public Long getIdOrgaoJulgadoColegiado() {
		return idOrgaoJulgadoColegiado;
	}

	public void setIdOrgaoJulgadoColegiado(Long idOrgaoJulgadoColegiado) {
		this.idOrgaoJulgadoColegiado = idOrgaoJulgadoColegiado;
	}

	@Column(name = "id_lote", insertable = false, updatable = false)
	public Integer getIdLote() {
		return idLote;
	}

	public void setIdLote(Integer idLote) {
		this.idLote = idLote;
	}

	@Column(name = "id_cargo", insertable = false, updatable = false)
	public Integer getIdCargo() {
		return idCargo;
	}

	public void setIdCargo(Integer idCargo) {
		this.idCargo = idCargo;
	}

	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	@Column(name = "in_segredo_justica", insertable = false, updatable = false)
	public Boolean getSegredoJustica() {
		return segredoJustica;
	}

	public void setSegredoApreciado(ProcessoTrfApreciadoEnum segredoApreciado) {
		this.segredoApreciado = segredoApreciado;
	}

	@Column(name = "in_apreciado_segredo", insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	public ProcessoTrfApreciadoEnum getSegredoApreciado() {
		return segredoApreciado;
	}

	@Column(name = "id_tarefa", insertable = false, updatable = false)
	public Integer getIdTarefa() {
		return idTarefa;
	}

	public void setIdTarefa(Integer idTarefa) {
		this.idTarefa = idTarefa;
	}


	@Column(name="id_orgao_julgador_cargo", insertable=false,updatable=false) 
	public Integer getIdOrgaoJulgadorCargo() { 
		return idOrgaoJulgadorCargo; 
	}
	 
	public void setIdOrgaoJulgadorCargo(Integer idOrgaoJulgadorCargo) {
		this.idOrgaoJulgadorCargo = idOrgaoJulgadorCargo; 
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_create_task")
	public Date getDataChegadaTarefa() {
		return dataChegadaTarefa;
	}

	public void setDataChegadaTarefa(Date dataChegadaTarefa) {
		this.dataChegadaTarefa = dataChegadaTarefa;
	}

	@Column(name = "id_previous_task", insertable = false, updatable = false)
	public Long getIdPreviousTask() {
		return idPreviousTask;
	}

	public void setIdPreviousTask(Long idPreviousTask) {
		this.idPreviousTask = idPreviousTask;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", insertable = false, updatable = false)
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}
	
	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", insertable = false, updatable = false)
	public ConsultaProcessoTrfSemFiltro getCabecalhoProcesso() {
		return cabecalhoProcesso;
	}
	
	public void setCabecalhoProcesso(ConsultaProcessoTrfSemFiltro cabecalhoProcesso) {
		this.cabecalhoProcesso = cabecalhoProcesso;
	}

	@Column(name="vl_prioridade")
	public Integer getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(Integer prioridade) {
		this.prioridade = prioridade;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getIdSituacaoProcesso() == null) {
			return false;
		}
		if (!(obj instanceof SituacaoProcesso)) {
			return false;
		}
		SituacaoProcesso other = (SituacaoProcesso) obj;
		if (!id.equals(other.getIdSituacaoProcesso())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIdSituacaoProcesso() == null) ? 0 : getIdSituacaoProcesso().hashCode());
		return result;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tarefa", insertable = false, updatable = false)
	public Tarefa getTarefa() {
		return tarefa;
	}

	public void setTarefa(Tarefa tarefa) {
		this.tarefa = tarefa;
	}
}