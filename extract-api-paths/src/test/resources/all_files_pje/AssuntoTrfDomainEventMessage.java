package br.jus.cnj.pje.amqp.model.dto;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.entidades.AssuntoTrf;

/**
 * Classe que representa os dados do assunto que são enviados para o
 * RabbitMQ.
 * 
 * @author Adriano Pamplona
 */
public class AssuntoTrfDomainEventMessage implements CloudEventPayload<AssuntoTrfDomainEventMessage, AssuntoTrf> {
	private String codigo;
	private String descricao;
	
	/**
	 * Construtor.
	 *
	 * @param assunto
	 */
	public AssuntoTrfDomainEventMessage(AssuntoTrf assunto) {
		if (assunto != null) {
			this.setCodigo(assunto.getCodAssuntoTrf());
			this.setDescricao(assunto.getAssuntoTrf());
		}
	}

	@Override
	public AssuntoTrfDomainEventMessage convertEntityToPayload(AssuntoTrf entity) {
		return new AssuntoTrfDomainEventMessage(entity);
	}

	@Override
	public Long getId(AssuntoTrf entity) {
		return (entity != null ? Long.valueOf(entity.getIdAssuntoTrf()) :  null);
	}

	/**
	 * @return Retorna codAssuntoTrf.
	 */
	public String getCodigo() {
		return codigo;
	}

	/**
	 * @param codAssuntoTrf Atribui codAssuntoTrf.
	 */
	public void setCodigo(String codAssuntoTrf) {
		this.codigo = codAssuntoTrf;
	}

	/**
	 * @return Retorna assuntoTrf.
	 */
	public String getDescricao() {
		return descricao;
	}

	/**
	 * @param assuntoTrf Atribui assuntoTrf.
	 */
	public void setDescricao(String assuntoTrf) {
		this.descricao = assuntoTrf;
	}
	
}
