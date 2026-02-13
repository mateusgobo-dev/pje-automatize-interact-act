package br.jus.cnj.pje.amqp.model.dto;

import java.util.Date;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;

/**
 * Classe que representa os dados do expediente que são enviados para o RabbitMQ.
 * 
 * @author Adriano Pamplona
 */
public class ProcessoParteExpedienteDomainEventMessage
		implements CloudEventPayload<ProcessoParteExpedienteDomainEventMessage, ProcessoParteExpediente> {

	private Long idProcessoParteExpediente;
	private ProcessoTrfDomainEventMessage processo;
	private PessoaDomainEventMessage pessoaDestinatario;
	private PessoaDomainEventMessage pessoaCiencia;
	private Boolean cienciaSistema;
	private Date dataCriacao;
	private Date dataPrazoFinal;
	private String tipoPrazo;
	
	/**
	 * Construtor.
	 * 
	 */
	public ProcessoParteExpedienteDomainEventMessage() {
		super();
	}
	
	/**
	 * Construtor.
	 * 
	 * @param classe
	 */
	public ProcessoParteExpedienteDomainEventMessage(ProcessoParteExpediente ppe) {
		if (ppe != null) {
			this.setProcesso(new ProcessoTrfDomainEventMessage(ppe.getProcessoJudicial()));
			this.setPessoaDestinatario(new PessoaDomainEventMessage(ppe.getPessoaParte()));
			this.setPessoaCiencia(new PessoaDomainEventMessage(ppe.getPessoaCiencia()));
			this.setCienciaSistema(ppe.getCienciaSistema());
			this.setDataCriacao(ppe.getDataDisponibilizacao());
			this.setDataPrazoFinal(ppe.getDtPrazoLegal());
			this.setTipoPrazo(ppe.getTipoPrazo().getLabel());
		}
	}

	@Override
	public ProcessoParteExpedienteDomainEventMessage convertEntityToPayload(ProcessoParteExpediente entity) {
		return new ProcessoParteExpedienteDomainEventMessage(entity);
	}

	@Override
	public Long getId(ProcessoParteExpediente entity) {
		return (entity != null ? Long.valueOf(entity.getIdProcessoParteExpediente()) : null);		
	}

	/**
	 * @return the idProcessoParteExpediente
	 */
	public Long getIdProcessoParteExpediente() {
		return idProcessoParteExpediente;
	}

	/**
	 * @param idProcessoParteExpediente the idProcessoParteExpediente to set
	 */
	public void setIdProcessoParteExpediente(Long idProcessoParteExpediente) {
		this.idProcessoParteExpediente = idProcessoParteExpediente;
	}
	
	/**
	 * @return the processo
	 */
	public ProcessoTrfDomainEventMessage getProcesso() {
		return processo;
	}

	/**
	 * @param processo the processo to set
	 */
	public void setProcesso(ProcessoTrfDomainEventMessage processo) {
		this.processo = processo;
	}

	/**
	 * @return the pessoaDestinatario
	 */
	public PessoaDomainEventMessage getPessoaDestinatario() {
		return pessoaDestinatario;
	}

	/**
	 * @param pessoaDestinatario the pessoaDestinatario to set
	 */
	public void setPessoaDestinatario(PessoaDomainEventMessage pessoaDestinatario) {
		this.pessoaDestinatario = pessoaDestinatario;
	}

	/**
	 * @return the pessoaCiencia
	 */
	public PessoaDomainEventMessage getPessoaCiencia() {
		return pessoaCiencia;
	}

	/**
	 * @param pessoaCiencia the pessoaCiencia to set
	 */
	public void setPessoaCiencia(PessoaDomainEventMessage pessoaCiencia) {
		this.pessoaCiencia = pessoaCiencia;
	}

	/**
	 * @return the cienciaSistema
	 */
	public Boolean getCienciaSistema() {
		return cienciaSistema;
	}

	/**
	 * @param cienciaSistema the cienciaSistema to set
	 */
	public void setCienciaSistema(Boolean cienciaSistema) {
		this.cienciaSistema = cienciaSistema;
	}

	/**
	 * @return the dataCriacao
	 */
	public Date getDataCriacao() {
		return dataCriacao;
	}

	/**
	 * @param dataCriacao the dataCriacao to set
	 */
	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	/**
	 * @return the dataPrazoFinal
	 */
	public Date getDataPrazoFinal() {
		return dataPrazoFinal;
	}

	/**
	 * @param dataPrazoFinal the dataPrazoFinal to set
	 */
	public void setDataPrazoFinal(Date dataPrazoFinal) {
		this.dataPrazoFinal = dataPrazoFinal;
	}

	/**
	 * @return the tipoPrazo
	 */
	public String getTipoPrazo() {
		return tipoPrazo;
	}

	/**
	 * @param tipoPrazo the tipoPrazo to set
	 */
	public void setTipoPrazo(String tipoPrazo) {
		this.tipoPrazo = tipoPrazo;
	}
}
