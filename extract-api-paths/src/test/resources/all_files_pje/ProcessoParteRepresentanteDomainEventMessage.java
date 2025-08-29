package br.jus.cnj.pje.amqp.model.dto;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;

/**
 * Classe que representa os dados da parte que são enviados para o RabbitMQ.
 * 
 * @author Adriano Pamplona
 */
public class ProcessoParteRepresentanteDomainEventMessage implements CloudEventPayload<ProcessoParteRepresentanteDomainEventMessage, ProcessoParteRepresentante>{

	private String tipo;
	private PessoaDomainEventMessage pessoa;

	/**
	 * Construtor.
	 *
	 * @param processo
	 */
	public ProcessoParteRepresentanteDomainEventMessage(ProcessoParteRepresentante representante) {
		super();
		if (representante != null) {
			setTipo(representante.getTipoRepresentante().getTipoParte());
			setPessoa(new PessoaDomainEventMessage(representante.getRepresentante()));
		}
	}

	@Override
	public ProcessoParteRepresentanteDomainEventMessage convertEntityToPayload(ProcessoParteRepresentante entity) {
		return new ProcessoParteRepresentanteDomainEventMessage(entity);
	}

	@Override
	public Long getId(ProcessoParteRepresentante entity) {
		return (entity != null ? Long.valueOf(entity.getIdProcessoParteRepresentante()) :  null);
	}

	/**
	 * @return the tipo
	 */
	public String getTipo() {
		return tipo;
	}

	/**
	 * @param tipo the tipo to set
	 */
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	/**
	 * @return the pessoa
	 */
	public PessoaDomainEventMessage getPessoa() {
		return pessoa;
	}

	/**
	 * @param pessoa the pessoa to set
	 */
	public void setPessoa(PessoaDomainEventMessage pessoa) {
		this.pessoa = pessoa;
	}

}
