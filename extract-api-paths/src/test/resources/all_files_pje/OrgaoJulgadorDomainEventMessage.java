package br.jus.cnj.pje.amqp.model.dto;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

/**
 * Classe que representa os dados do órgão julgador que são enviados para o
 * RabbitMQ.
 * 
 * @author Adriano Pamplona
 */
public class OrgaoJulgadorDomainEventMessage
		implements CloudEventPayload<OrgaoJulgadorDomainEventMessage, OrgaoJulgador> {

	private String codigo;
	private String descricao;
	private String estado;

	/**
	 * Construtor.
	 * 
	 * @param orgaoJulgador
	 */
	public OrgaoJulgadorDomainEventMessage(OrgaoJulgador orgaoJulgador) {
		super();
		if (orgaoJulgador != null) {
			setCodigo(orgaoJulgador.getSigla());
			setDescricao(orgaoJulgador.getOrgaoJulgador());
			setEstado(orgaoJulgador.getJurisdicao().getEstado().getEstado());
		}
	}

	@Override
	public OrgaoJulgadorDomainEventMessage convertEntityToPayload(OrgaoJulgador entity) {
		return new OrgaoJulgadorDomainEventMessage(entity);
	}

	@Override
	public Long getId(OrgaoJulgador entity) {
		return (entity != null ? Long.valueOf(entity.getIdOrgaoJulgador()) : null);
	}

	/**
	 * @return the codigo
	 */
	public String getCodigo() {
		return codigo;
	}

	/**
	 * @param codigo the codigo to set
	 */
	public void setCodigo(String codigo) {
		this.codigo = codigo;
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
