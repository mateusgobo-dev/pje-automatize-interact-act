package br.jus.cnj.pje.amqp.model.dto;

import java.util.Date;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.entidades.ProcessoEvento;

public class ProcessoEventoCloudEvent implements CloudEventPayload<ProcessoEventoCloudEvent, ProcessoEvento>{
	
	private static final long serialVersionUID = 1L;

	private Integer idProcessoEvento;
	private Integer idProcesso;
	private Integer idProcessoDocumento;
	private Integer idUsuario;
	private Date dataAtualizacao;
	private String descricaoEvento;
	private Long idJbpmTask;
	private Long idProcessInstance;
	private Integer idTarefa;
	private String nomeUsuario;
	private String cpfUsuario;
	private String cnpjUsuario;
	private boolean processado = false;
	private boolean verificadoProcessado = false;
	private Integer idProcessoEventoExcludente;
	private Boolean visibilidadeExterna;
	private String observacao;
	private String textoFinalInterno;
	private String textoFinalExterno;
	private String textoParametrizado;
	private String numeroProcesso;
	
	private Boolean ativo = Boolean.TRUE;
	
	
	public ProcessoEventoCloudEvent(ProcessoEvento processoEvento) {
		this.idProcessoEvento = processoEvento.getIdProcessoEvento();
		if(processoEvento.getProcesso() != null){
			this.idProcesso = processoEvento.getProcesso().getIdProcesso();
		}
		if(processoEvento.getProcessoDocumento() != null){
			this.idProcessoDocumento = processoEvento.getProcessoDocumento().getIdProcessoDocumento();
		}
		if(processoEvento.getUsuario() != null){
			this.idUsuario = processoEvento.getUsuario().getIdUsuario();
		}
		this.dataAtualizacao = processoEvento.getDataAtualizacao();
		this.descricaoEvento = processoEvento.getDescricaoEvento();
		this.idJbpmTask = processoEvento.getIdJbpmTask();
		this.idProcessInstance = processoEvento.getIdProcessInstance();
		if(processoEvento.getTarefa() != null){
			this.idTarefa = processoEvento.getTarefa().getIdTarefa();
		}
		this.nomeUsuario = processoEvento.getNomeUsuario();
		this.cpfUsuario = processoEvento.getCpfUsuario();
		this.processado = processoEvento.isProcessado();
		this.verificadoProcessado = processoEvento.isVerificadoProcessado();
		if(processoEvento.getProcessoEventoExcludente() != null){
			this.idProcessoEventoExcludente = processoEvento.getProcessoEventoExcludente().getIdProcessoEvento();
		}
		this.visibilidadeExterna = processoEvento.getVisibilidadeExterna();
		this.observacao = processoEvento.getObservacao();
		this.textoFinalInterno = processoEvento.getTextoFinalInterno();
		this.textoFinalExterno = processoEvento.getTextoFinalExterno();
		this.textoParametrizado = processoEvento.getTextoParametrizado();
		this.numeroProcesso = processoEvento.getProcesso().getNumeroProcesso();
	}
	
	public ProcessoEventoCloudEvent() {
		super();
	}

	public int getIdProcessoEvento() {
		return idProcessoEvento;
	}

	public void setIdProcessoEvento(int idProcessoEvento) {
		this.idProcessoEvento = idProcessoEvento;
	}

	public Integer getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso = idProcesso;
	}

	public Integer getIdProcessoDocumento() {
		return idProcessoDocumento;
	}

	public void setIdProcessoDocumento(Integer idProcessoDocumento) {
		this.idProcessoDocumento = idProcessoDocumento;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	public String getDescricaoEvento() {
		return descricaoEvento;
	}

	public void setDescricaoEvento(String descricaoEvento) {
		this.descricaoEvento = descricaoEvento;
	}

	public Long getIdJbpmTask() {
		return idJbpmTask;
	}

	public void setIdJbpmTask(Long idJbpmTask) {
		this.idJbpmTask = idJbpmTask;
	}

	public Long getIdProcessInstance() {
		return idProcessInstance;
	}

	public void setIdProcessInstance(Long idProcessInstance) {
		this.idProcessInstance = idProcessInstance;
	}

	public Integer getIdTarefa() {
		return idTarefa;
	}

	public void setIdTarefa(Integer idTarefa) {
		this.idTarefa = idTarefa;
	}

	public String getNomeUsuario() {
		return nomeUsuario;
	}

	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}

	public String getCpfUsuario() {
		return cpfUsuario;
	}

	public void setCpfUsuario(String cpfUsuario) {
		this.cpfUsuario = cpfUsuario;
	}

	public String getCnpjUsuario() {
		return cnpjUsuario;
	}

	public void setCnpjUsuario(String cnpjUsuario) {
		this.cnpjUsuario = cnpjUsuario;
	}

	public boolean isProcessado() {
		return processado;
	}

	public void setProcessado(boolean processado) {
		this.processado = processado;
	}

	public boolean isVerificadoProcessado() {
		return verificadoProcessado;
	}

	public void setVerificadoProcessado(boolean verificadoProcessado) {
		this.verificadoProcessado = verificadoProcessado;
	}

	public Integer getIdProcessoEventoExcludente() {
		return idProcessoEventoExcludente;
	}

	public void setIdProcessoEventoExcludente(Integer idProcessoEventoExcludente) {
		this.idProcessoEventoExcludente = idProcessoEventoExcludente;
	}

	public Boolean getVisibilidadeExterna() {
		return visibilidadeExterna;
	}

	public void setVisibilidadeExterna(Boolean visibilidadeExterna) {
		this.visibilidadeExterna = visibilidadeExterna;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public String getTextoFinalInterno() {
		return textoFinalInterno;
	}

	public void setTextoFinalInterno(String textoFinalInterno) {
		this.textoFinalInterno = textoFinalInterno;
	}

	public String getTextoFinalExterno() {
		return textoFinalExterno;
	}

	public void setTextoFinalExterno(String textoFinalExterno) {
		this.textoFinalExterno = textoFinalExterno;
	}

	public String getTextoParametrizado() {
		return textoParametrizado;
	}

	public void setTextoParametrizado(String textoParametrizado) {
		this.textoParametrizado = textoParametrizado;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	public String getNumeroProcesso() {
		return numeroProcesso;
	}
	
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	@Override
	public ProcessoEventoCloudEvent convertEntityToPayload(ProcessoEvento processoEvento) {
		this.idProcessoEvento = processoEvento.getIdProcessoEvento();
		if(processoEvento.getProcesso() != null){
			this.idProcesso = processoEvento.getProcesso().getIdProcesso();
		}
		if(processoEvento.getProcessoDocumento() != null){
			this.idProcessoDocumento = processoEvento.getProcessoDocumento().getIdProcessoDocumento();
		}
		if(processoEvento.getUsuario() != null){
			this.idUsuario = processoEvento.getUsuario().getIdUsuario();
		}
		this.dataAtualizacao = processoEvento.getDataAtualizacao();
		this.descricaoEvento = processoEvento.getDescricaoEvento();
		this.idJbpmTask = processoEvento.getIdJbpmTask();
		this.idProcessInstance = processoEvento.getIdProcessInstance();
		if(processoEvento.getTarefa() != null){
			this.idTarefa = processoEvento.getTarefa().getIdTarefa();
		}
		this.nomeUsuario = processoEvento.getNomeUsuario();
		this.cpfUsuario = processoEvento.getCpfUsuario();
		this.processado = processoEvento.isProcessado();
		this.verificadoProcessado = processoEvento.isVerificadoProcessado();
		if(processoEvento.getProcessoEventoExcludente() != null){
			this.idProcessoEventoExcludente = processoEvento.getProcessoEventoExcludente().getIdProcessoEvento();
		}
		this.visibilidadeExterna = processoEvento.getVisibilidadeExterna();
		this.observacao = processoEvento.getObservacao();
		this.textoFinalInterno = processoEvento.getTextoFinalInterno();
		this.textoFinalExterno = processoEvento.getTextoFinalExterno();
		this.textoParametrizado = processoEvento.getTextoParametrizado();
		this.numeroProcesso = processoEvento.getProcesso().getNumeroProcesso();
		
		return this;
	}

	@Override
	public Long getId(ProcessoEvento entity) {
		return (entity != null ? Long.valueOf(entity.getIdProcessoEvento()) : null);
	}
	
}
