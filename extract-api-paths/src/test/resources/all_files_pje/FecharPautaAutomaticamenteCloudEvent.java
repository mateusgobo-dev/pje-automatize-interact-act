package br.jus.cnj.pje.amqp.model.dto.jobs;

import java.util.Date;
import java.util.List;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;

public class FecharPautaAutomaticamenteCloudEvent
		implements CloudEventPayload<FecharPautaAutomaticamenteCloudEvent, FecharPautaAutomaticamenteCloudEvent> {

	private static final long serialVersionUID = 1L;

	private List<Integer> idsSessoes;
	private CloudEventBulkIdentificatioin bulkIdentification;

	public FecharPautaAutomaticamenteCloudEvent() {
		super();
	}

	public FecharPautaAutomaticamenteCloudEvent(List<Integer> idsSessoes, String uuidLote, Integer numJob) {
		this.idsSessoes = idsSessoes;
		this.bulkIdentification = new CloudEventBulkIdentificatioin(uuidLote, numJob, new Date());
	}

	public CloudEventBulkIdentificatioin getBulkIdentification() {
		return bulkIdentification;
	}

	public void setBulkIdentification(CloudEventBulkIdentificatioin bulkIdentification) {
		this.bulkIdentification = bulkIdentification;
	}

	public List<Integer> getIdsSessoes() {
		return idsSessoes;
	}

	public void setIdsSessoes(List<Integer> idsSessoes) {
		this.idsSessoes = idsSessoes;
	}

	@Override
	public FecharPautaAutomaticamenteCloudEvent convertEntityToPayload(FecharPautaAutomaticamenteCloudEvent entity) {
		this.idsSessoes = entity.getIdsSessoes();
		this.bulkIdentification = entity.getBulkIdentification();

		return this;
	}

	@Override
	public Long getId(FecharPautaAutomaticamenteCloudEvent entity) {
		if (entity != null && entity.getIdsSessoes() != null) {
			return Long.valueOf(entity.getIdsSessoes().get(0));
		} else {
			return null;
		}
	}
}