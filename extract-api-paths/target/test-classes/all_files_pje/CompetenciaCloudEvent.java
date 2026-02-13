package br.jus.cnj.pje.amqp.model.dto;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.entidades.Competencia;

/**
 * Classe CompetenciaCloudEvent.
 * 
 * @author Adriano Pamplona
 */
public class CompetenciaCloudEvent implements CloudEventPayload<CompetenciaCloudEvent, Competencia> {
	
	private String descricao;
	
	/**
	 * Construtor.
	 *
	 * @param assunto
	 */
	public CompetenciaCloudEvent(Competencia competencia) {
		this.setDescricao(competencia.getCompetencia());
	}

	@Override
	public CompetenciaCloudEvent convertEntityToPayload(Competencia entity) {
		return new CompetenciaCloudEvent(entity);
	}

	@Override
	public Long getId(Competencia entity) {
		return (entity != null ? Long.valueOf(entity.getIdCompetencia()) :  null);
	}

	/**
	 * @return Retorna competencia.
	 */
	public String getDescricao() {
		return descricao;
	}

	/**
	 * @param competencia Atribui competencia.
	 */
	public void setDescricao(String competencia) {
		this.descricao = competencia;
	}
	
}
