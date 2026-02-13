package br.jus.cnj.pje.amqp.model.dto.jobs;

import java.util.Date;
import java.util.List;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;

public class ProsseguimentoSemPrazoCloudEvent
		implements CloudEventPayload<ProsseguimentoSemPrazoCloudEvent, ProsseguimentoSemPrazoCloudEvent> {

	private static final long serialVersionUID = 1L;

	private List<Integer> idsProcessoParteExpediente;
	private CloudEventBulkIdentificatioin bulkIdentification;

	public ProsseguimentoSemPrazoCloudEvent() {
		super();
	}

	public ProsseguimentoSemPrazoCloudEvent(List<Integer> idsProcessoParteExpediente, String uuidLote, Integer numJob) {
		this.idsProcessoParteExpediente = idsProcessoParteExpediente;
		this.bulkIdentification = new CloudEventBulkIdentificatioin(uuidLote, numJob, new Date());
	}

	public CloudEventBulkIdentificatioin getBulkIdentification() {
		return bulkIdentification;
	}

	public void setBulkIdentification(CloudEventBulkIdentificatioin bulkIdentification) {
		this.bulkIdentification = bulkIdentification;
	}

	public List<Integer> getIdsProcessoParteExpediente() {
		return idsProcessoParteExpediente;
	}

	public void setIdsProcessoParteExpediente(List<Integer> idsProcessoParteExpediente) {
		this.idsProcessoParteExpediente = idsProcessoParteExpediente;
	}

	@Override
	public ProsseguimentoSemPrazoCloudEvent convertEntityToPayload(ProsseguimentoSemPrazoCloudEvent entity) {
		this.idsProcessoParteExpediente = entity.getIdsProcessoParteExpediente();
		this.bulkIdentification = entity.getBulkIdentification();

		return this;
	}

	@Override
	public Long getId(ProsseguimentoSemPrazoCloudEvent entity) {
		if (entity != null && entity.getIdsProcessoParteExpediente() != null) {
			return Long.valueOf(entity.getIdsProcessoParteExpediente().get(0));
		} else {
			return null;
		}
	}
}