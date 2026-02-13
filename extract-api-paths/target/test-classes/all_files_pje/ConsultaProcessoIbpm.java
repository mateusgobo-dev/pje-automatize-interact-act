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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "vs_processo_localizacao_ibpm")
public class ConsultaProcessoIbpm implements Serializable{

	private static final long serialVersionUID = 1L;

	private long idProcessoLocalizacao;
	private int idProcesso;
	private String numeroProcesso;
	private Integer idOrgaoJulgador;
	private String orgaoJulgador;
	private Integer idOrgaoJulgadorColegiado;
	private String orgaoJulgadorColegiado;
	private String nomePapel;
	private String localizacao;
	private Long idJbpmTask;
	private TarefaJbpm tarefaJbpm;
	private String nomeTask;
	private Long idProcessDefinition;
	private String nomeProcessDefinition;

	@Id
	@Column(name = "id_processo_localizacao", insertable = false, updatable = false)
	public long getIdProcessoLocalizacao(){
		return idProcessoLocalizacao;
	}

	public void setIdProcessoLocalizacao(long idProcessoLocalizacao){
		this.idProcessoLocalizacao = idProcessoLocalizacao;
	}

	@Column(name = "id_processo", insertable = false, updatable = false)
	public int getIdProcesso(){
		return idProcesso;
	}

	public void setIdProcesso(int idProcesso){
		this.idProcesso = idProcesso;
	}

	@Column(name = "nr_processo", insertable = false, updatable = false)
	public String getNumeroProcesso(){
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso){
		this.numeroProcesso = numeroProcesso;
	}

	@Column(name = "id_orgao_julgador", insertable = false, updatable = false)
	public Integer getIdOrgaoJulgador(){
		return idOrgaoJulgador;
	}

	public void setIdOrgaoJulgador(Integer idOrgaoJulgador){
		this.idOrgaoJulgador = idOrgaoJulgador;
	}

	@Column(name = "ds_orgao_julgador", insertable = false, updatable = false)
	public String getOrgaoJulgador(){
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(String orgaoJulgador){
		this.orgaoJulgador = orgaoJulgador;
	}

	@Column(name = "id_orgao_julgador_colegiado", insertable = false, updatable = false)
	public Integer getIdOrgaoJulgadorColegiado(){
		return idOrgaoJulgadorColegiado;
	}

	public void setIdOrgaoJulgadorColegiado(Integer idOrgaoJulgadorColegiado){
		this.idOrgaoJulgadorColegiado = idOrgaoJulgadorColegiado;
	}

	@Column(name = "ds_orgao_julgador_colegiado", insertable = false, updatable = false)
	public String getOrgaoJulgadorColegiado(){
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(String orgaoJulgadorColegiado){
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@Column(name = "nm_papel", insertable = false, updatable = false)
	public String getNomePapel(){
		return nomePapel;
	}

	public void setNomePapel(String nomePapel){
		this.nomePapel = nomePapel;
	}

	@Column(name = "ds_localizacao", insertable = false, updatable = false)
	public String getLocalizacao(){
		return localizacao;
	}

	public void setLocalizacao(String localizacao){
		this.localizacao = localizacao;
	}

	@Column(name = "id_task_jbpm", insertable = false, updatable = false)
	public Long getIdJbpmTask(){
		return idJbpmTask;
	}

	public void setIdJbpmTask(Long idJbpmTask){
		this.idJbpmTask = idJbpmTask;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_task_jbpm", insertable = false, updatable = false, referencedColumnName = "id_jbpm_task")
	public TarefaJbpm getTarefaJbpm(){
		return tarefaJbpm;
	}

	public void setTarefaJbpm(TarefaJbpm tarefaJbpm){
		this.tarefaJbpm = tarefaJbpm;
	}

	@Column(name = "nm_task", insertable = false, updatable = false)
	public String getNomeTask(){
		return nomeTask;
	}

	public void setNomeTask(String nomeTask){
		this.nomeTask = nomeTask;
	}

	@Column(name = "id_processinstance_jbpm", insertable = false, updatable = false)
	public Long getIdProcessDefinition(){
		return idProcessDefinition;
	}

	public void setIdProcessDefinition(Long idProcessDefinition){
		this.idProcessDefinition = idProcessDefinition;
	}

	@Column(name = "nm_process_definition", insertable = false, updatable = false)
	public String getNomeProcessDefinition(){
		return nomeProcessDefinition;
	}

	public void setNomeProcessDefinition(String nomeProcessDefinition){
		this.nomeProcessDefinition = nomeProcessDefinition;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (!(obj instanceof ConsultaProcessoIbpm)){
			return false;
		}
		ConsultaProcessoIbpm other = (ConsultaProcessoIbpm) obj;
		if (getIdProcessoLocalizacao() != other.getIdProcessoLocalizacao()){
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(idProcessoLocalizacao);
	}
}
