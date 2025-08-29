package br.jus.cnj.pje.amqp.model.dto;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.entidades.Jurisdicao;

/**
 * Classe JurisdicaoCloudEvent.
 * 
 * @author Adriano Pamplona
 */
public class JurisdicaoDomainEventMessage implements CloudEventPayload<JurisdicaoDomainEventMessage, Jurisdicao> {
	private String descricao;
	private String estado;
	
	/**
	 * Construtor.
	 *
	 * @param jurisdicao
	 */
	public JurisdicaoDomainEventMessage(Jurisdicao jurisdicao) {
		if (jurisdicao != null) {
			this.setDescricao(jurisdicao.getJurisdicao());
			this.setEstado(jurisdicao.getEstado().getEstado());
		}
	}

	@Override
	public JurisdicaoDomainEventMessage convertEntityToPayload(Jurisdicao entity) {
		return new JurisdicaoDomainEventMessage(entity);
	}

	@Override
	public Long getId(Jurisdicao entity) {
		return (entity != null ? Long.valueOf(entity.getIdJurisdicao()) :  null);
	}

	/**
	 * @return Retorna jurisdicao.
	 */
	public String getDescricao() {
		return descricao;
	}

	/**
	 * @param jurisdicao Atribui jurisdicao.
	 */
	public void setDescricao(String jurisdicao) {
		this.descricao = jurisdicao;
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
