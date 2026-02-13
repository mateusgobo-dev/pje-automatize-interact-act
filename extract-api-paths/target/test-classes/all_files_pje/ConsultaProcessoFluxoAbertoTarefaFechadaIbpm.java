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
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entidade para listagem de processos com erro no fluxo 
 *
 */
@Entity
@Table(name = "vs_proc_fluxo_aberto_tarefa_fechada_ibpm")
public class ConsultaProcessoFluxoAbertoTarefaFechadaIbpm implements Serializable{

	private static final long serialVersionUID = 6979436103696925235L;
	
	
	private Integer numeroSequencia;
	private Integer numeroDigitoVerificador;
	private Integer ano;
	private Integer numeroOrgaoJustica;
	private Integer numeroOrigem;
	private Long processInstance;
	private String nmFluxo;
	private Integer rootToken;
	private Integer superProcessToken;
	private boolean temPai;
	private Integer processRootTokenPai;
	private String nmFluxoPai;
	private boolean temFilho;
	private Integer filhoRootToken;
	private Date filhoAbertoStart;
	private Date filhoAbertoEnd;
	private Date inicioFluxo;
	private Date fimFluxo;
	private Long taskInstId;
	private String taskName;
	private Date taskFim;
	private Date taskInicio;
	private boolean taskIsOpen;
	
	@Id
	@Column(name = "nr_sequencia")
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}
	
	@Column(name = "nr_origem_processo")
	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	@Column(name = "nr_ano")
	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	@Column(name = "nr_digito_verificador")
	public Integer getNumeroDigitoVerificador() {
		return numeroDigitoVerificador;
	}

	public void setNumeroDigitoVerificador(Integer numeroDigitoVerificador) {
		this.numeroDigitoVerificador = numeroDigitoVerificador;
	}
	
	@Column(name = "nr_identificacao_orgao_justica")
	public Integer getNumeroOrgaoJustica() {
		return numeroOrgaoJustica;
	}

	public void setNumeroOrgaoJustica(Integer numeroOrgaoJustica) {
		this.numeroOrgaoJustica = numeroOrgaoJustica;
	}

	@Column(name = "process_instance_id", insertable = false, updatable = false)
	public Long getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(Long processInstance) {
		this.processInstance = processInstance;
	}

	@Column(name = "nome_fluxo", insertable = false, updatable = false)
	public String getNmFluxo() {
		return nmFluxo;
	}

	public void setNmFluxo(String nmFluxo) {
		this.nmFluxo = nmFluxo;
	}

	@Column(name = "roottoken_", insertable = false, updatable = false)
	public Integer getRootToken() {
		return rootToken;
	}

	public void setRootToken(Integer rootToken) {
		this.rootToken = rootToken;
	}

	@Column(name = "superprocesstoken_", insertable = false, updatable = false)
	public Integer getSuperProcessToken() {
		return superProcessToken;
	}

	public void setSuperProcessToken(Integer superProcessToken) {
		this.superProcessToken = superProcessToken;
	}

	@Column(name = "tem_pai", insertable = false, updatable = false)
	public boolean isTemPai() {
		return temPai;
	}

	public void setTemPai(boolean temPai) {
		this.temPai = temPai;
	}

	@Column(name = "process_root_token_pai", insertable = false, updatable = false)
	public Integer getProcessRootTokenPai() {
		return processRootTokenPai;
	}

	public void setProcessRootTokenPai(Integer processRootTokenPai) {
		this.processRootTokenPai = processRootTokenPai;
	}

	@Column(name = "nome_fluxo_pai", insertable = false, updatable = false)
	public String getNmFluxoPai() {
		return nmFluxoPai;
	}

	public void setNmFluxoPai(String nmFluxoPai) {
		this.nmFluxoPai = nmFluxoPai;
	}

	@Column(name = "tem_filho", insertable = false, updatable = false)
	public boolean isTemFilho() {
		return temFilho;
	}

	public void setTemFilho(boolean temFilho) {
		this.temFilho = temFilho;
	}

	@Column(name = "filho_roottoken", insertable = false, updatable = false)
	public Integer getFilhoRootToken() {
		return filhoRootToken;
	}

	public void setFilhoRootToken(Integer filhoRootToken) {
		this.filhoRootToken = filhoRootToken;
	}

	@Column(name = "filho_aberto_start", insertable = false, updatable = false)
	public Date getFilhoAbertoStart() {
		return filhoAbertoStart;
	}

	public void setFilhoAbertoStart(Date filhoAbertoStart) {
		this.filhoAbertoStart = filhoAbertoStart;
	}

	@Column(name = "filho_aberto_end", insertable = false, updatable = false)
	public Date getFilhoAbertoEnd() {
		return filhoAbertoEnd;
	}

	public void setFilhoAbertoEnd(Date filhoAbertoEnd) {
		this.filhoAbertoEnd = filhoAbertoEnd;
	}

	@Column(name = "inicio_fluxo", insertable = false, updatable = false)
	public Date getInicioFluxo() {
		return inicioFluxo;
	}

	public void setInicioFluxo(Date inicioFluxo) {
		this.inicioFluxo = inicioFluxo;
	}

	@Column(name = "fim_fluxo", insertable = false, updatable = false)
	public Date getFimFluxo() {
		return fimFluxo;
	}

	public void setFimFluxo(Date fimFluxo) {
		this.fimFluxo = fimFluxo;
	}

	@Column(name = "task_instace_id", insertable = false, updatable = false)
	public Long getTaskInstId() {
		return taskInstId;
	}

	public void setTaskInstId(Long taskInstId) {
		this.taskInstId = taskInstId;
	}

	@Column(name = "name_", insertable = false, updatable = false)
	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	@Column(name = "end_", insertable = false, updatable = false)
	public Date getTaskFim() {
		return taskFim;
	}

	public void setTaskFim(Date taskFim) {
		this.taskFim = taskFim;
	}

	@Column(name = "start_", insertable = false, updatable = false)
	public Date getTaskInicio() {
		return taskInicio;
	}

	public void setTaskInicio(Date taskInicio) {
		this.taskInicio = taskInicio;
	}

	@Column(name = "isopen_", insertable = false, updatable = false)
	public boolean isTaskIsOpen() {
		return taskIsOpen;
	}

	public void setTaskIsOpen(boolean taskIsOpen) {
		this.taskIsOpen = taskIsOpen;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ano == null) ? 0 : ano.hashCode());
		result = prime * result
				+ ((filhoAbertoEnd == null) ? 0 : filhoAbertoEnd.hashCode());
		result = prime
				* result
				+ ((filhoAbertoStart == null) ? 0 : filhoAbertoStart.hashCode());
		result = prime * result
				+ ((filhoRootToken == null) ? 0 : filhoRootToken.hashCode());
		result = prime * result
				+ ((fimFluxo == null) ? 0 : fimFluxo.hashCode());
		result = prime * result
				+ ((inicioFluxo == null) ? 0 : inicioFluxo.hashCode());
		result = prime * result + ((nmFluxo == null) ? 0 : nmFluxo.hashCode());
		result = prime * result
				+ ((nmFluxoPai == null) ? 0 : nmFluxoPai.hashCode());
		result = prime
				* result
				+ ((numeroDigitoVerificador == null) ? 0
						: numeroDigitoVerificador.hashCode());
		result = prime
				* result
				+ ((numeroOrgaoJustica == null) ? 0 : numeroOrgaoJustica
						.hashCode());
		result = prime * result
				+ ((numeroOrigem == null) ? 0 : numeroOrigem.hashCode());
		result = prime * result
				+ ((numeroSequencia == null) ? 0 : numeroSequencia.hashCode());
		result = prime * result
				+ ((processInstance == null) ? 0 : processInstance.hashCode());
		result = prime
				* result
				+ ((processRootTokenPai == null) ? 0 : processRootTokenPai
						.hashCode());
		result = prime * result
				+ ((rootToken == null) ? 0 : rootToken.hashCode());
		result = prime
				* result
				+ ((superProcessToken == null) ? 0 : superProcessToken
						.hashCode());
		result = prime * result + ((taskFim == null) ? 0 : taskFim.hashCode());
		result = prime * result
				+ ((taskInicio == null) ? 0 : taskInicio.hashCode());
		result = prime * result
				+ ((taskInstId == null) ? 0 : taskInstId.hashCode());
		result = prime * result + (taskIsOpen ? 1231 : 1237);
		result = prime * result
				+ ((taskName == null) ? 0 : taskName.hashCode());
		result = prime * result + (temFilho ? 1231 : 1237);
		result = prime * result + (temPai ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConsultaProcessoFluxoAbertoTarefaFechadaIbpm other = (ConsultaProcessoFluxoAbertoTarefaFechadaIbpm) obj;
		if (ano == null) {
			if (other.ano != null)
				return false;
		} else if (!ano.equals(other.ano))
			return false;
		if (filhoAbertoEnd == null) {
			if (other.filhoAbertoEnd != null)
				return false;
		} else if (!filhoAbertoEnd.equals(other.filhoAbertoEnd))
			return false;
		if (filhoAbertoStart == null) {
			if (other.filhoAbertoStart != null)
				return false;
		} else if (!filhoAbertoStart.equals(other.filhoAbertoStart))
			return false;
		if (filhoRootToken == null) {
			if (other.filhoRootToken != null)
				return false;
		} else if (!filhoRootToken.equals(other.filhoRootToken))
			return false;
		if (fimFluxo == null) {
			if (other.fimFluxo != null)
				return false;
		} else if (!fimFluxo.equals(other.fimFluxo))
			return false;
		if (inicioFluxo == null) {
			if (other.inicioFluxo != null)
				return false;
		} else if (!inicioFluxo.equals(other.inicioFluxo))
			return false;
		if (nmFluxo == null) {
			if (other.nmFluxo != null)
				return false;
		} else if (!nmFluxo.equals(other.nmFluxo))
			return false;
		if (nmFluxoPai == null) {
			if (other.nmFluxoPai != null)
				return false;
		} else if (!nmFluxoPai.equals(other.nmFluxoPai))
			return false;
		if (numeroDigitoVerificador == null) {
			if (other.numeroDigitoVerificador != null)
				return false;
		} else if (!numeroDigitoVerificador
				.equals(other.numeroDigitoVerificador))
			return false;
		if (numeroOrgaoJustica == null) {
			if (other.numeroOrgaoJustica != null)
				return false;
		} else if (!numeroOrgaoJustica.equals(other.numeroOrgaoJustica))
			return false;
		if (numeroOrigem == null) {
			if (other.numeroOrigem != null)
				return false;
		} else if (!numeroOrigem.equals(other.numeroOrigem))
			return false;
		if (numeroSequencia == null) {
			if (other.numeroSequencia != null)
				return false;
		} else if (!numeroSequencia.equals(other.numeroSequencia))
			return false;
		if (processInstance == null) {
			if (other.processInstance != null)
				return false;
		} else if (!processInstance.equals(other.processInstance))
			return false;
		if (processRootTokenPai == null) {
			if (other.processRootTokenPai != null)
				return false;
		} else if (!processRootTokenPai.equals(other.processRootTokenPai))
			return false;
		if (rootToken == null) {
			if (other.rootToken != null)
				return false;
		} else if (!rootToken.equals(other.rootToken))
			return false;
		if (superProcessToken == null) {
			if (other.superProcessToken != null)
				return false;
		} else if (!superProcessToken.equals(other.superProcessToken))
			return false;
		if (taskFim == null) {
			if (other.taskFim != null)
				return false;
		} else if (!taskFim.equals(other.taskFim))
			return false;
		if (taskInicio == null) {
			if (other.taskInicio != null)
				return false;
		} else if (!taskInicio.equals(other.taskInicio))
			return false;
		if (taskInstId == null) {
			if (other.taskInstId != null)
				return false;
		} else if (!taskInstId.equals(other.taskInstId))
			return false;
		if (taskIsOpen != other.taskIsOpen)
			return false;
		if (taskName == null) {
			if (other.taskName != null)
				return false;
		} else if (!taskName.equals(other.taskName))
			return false;
		if (temFilho != other.temFilho)
			return false;
		if (temPai != other.temPai)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Fluxo: "+getNmFluxo()+" Inicio do fluxo:"+getInicioFluxo()
				+" Fim do fluxo:"+getFimFluxo()+" Tarefa:"+getTaskName()
				+" Inicio da tarefa:"+getTaskInicio()+" Fim da tarefa:"+getTaskFim();
	}

}