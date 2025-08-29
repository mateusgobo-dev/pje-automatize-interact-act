package br.jus.cnj.pje.amqp.model.dto;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;

/**
 * Classe que representa os dados do órgão julgador colegiado que são enviados para o
 * RabbitMQ.
 * 
 * @author Adriano Pamplona
 */
public class OrgaoJulgadorColegiadoDomainEventMessage
		implements CloudEventPayload<OrgaoJulgadorColegiadoDomainEventMessage, OrgaoJulgadorColegiado> {

	private String descricao;
	private String estado;

	/**
	 * Construtor.
	 * 
	 * @param orgaoJulgador
	 */
	public OrgaoJulgadorColegiadoDomainEventMessage(OrgaoJulgadorColegiado orgaoJulgador) {
		super();
		if (orgaoJulgador != null) {
			setDescricao(orgaoJulgador.getOrgaoJulgadorColegiado());
			setEstado(orgaoJulgador.getJurisdicao().getEstado().getEstado());
		}
	}

	@Override
	public OrgaoJulgadorColegiadoDomainEventMessage convertEntityToPayload(OrgaoJulgadorColegiado entity) {
		return new OrgaoJulgadorColegiadoDomainEventMessage(entity);
	}

	@Override
	public Long getId(OrgaoJulgadorColegiado entity) {
		return (entity != null ? Long.valueOf(entity.getIdOrgaoJulgadorColegiado()) : null);
	}

	/**
	 * @return the descricao
	 */
	public String getDescricao() {
		return descricao;
	}

	/**
	 * @param descricao the descricao to set
	 */
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	/**
	 * @return the estado
	 */
	public String getEstado() {
		return estado;
	}

	/**
	 * @param estado the estado to set
	 */
	public void setEstado(String estado) {
		this.estado = estado;
	}

}
