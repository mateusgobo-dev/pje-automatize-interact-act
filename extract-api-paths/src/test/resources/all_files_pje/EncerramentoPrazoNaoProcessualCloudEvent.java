package br.jus.cnj.pje.amqp.model.dto.jobs;

import java.util.Date;
import java.util.List;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;

public class EncerramentoPrazoNaoProcessualCloudEvent implements
		CloudEventPayload<EncerramentoPrazoNaoProcessualCloudEvent, EncerramentoPrazoNaoProcessualCloudEvent> {

	private static final long serialVersionUID = 1L;

	private List<Integer> idsProcesso;
	private CloudEventBulkIdentificatioin bulkIdentification;

	public EncerramentoPrazoNaoProcessualCloudEvent() {
		super();
	}

	public EncerramentoPrazoNaoProcessualCloudEvent(List<Integer> idsProcessoParteExpediente, String uuidLote,
			Integer numJob) {
		this.idsProcesso = idsProcessoParteExpediente;
		this.bulkIdentification = new CloudEventBulkIdentificatioin(uuidLote, numJob, new Date());
	}

	public CloudEventBulkIdentificatioin getBulkIdentification() {
		return bulkIdentification;
	}

	public void setBulkIdentification(CloudEventBulkIdentificatioin bulkIdentification) {
		this.bulkIdentification = bulkIdentification;
	}

	public List<Integer> getIdsProcesso() {
		return idsProcesso;
	}

	public void setIdsProcesso(List<Integer> idsProcesso) {
		this.idsProcesso = idsProcesso;
	}

	@Override
	public EncerramentoPrazoNaoProcessualCloudEvent convertEntityToPayload(
			EncerramentoPrazoNaoProcessualCloudEvent entity) {
		this.idsProcesso = entity.getIdsProcesso();
		this.bulkIdentification = entity.getBulkIdentification();

		return this;
	}

	@Override
	public Long getId(EncerramentoPrazoNaoProcessualCloudEvent entity) {
		if (entity != null && entity.getIdsProcesso() != null) {
			return Long.valueOf(entity.getIdsProcesso().get(0));
		} else {
			return null;
		}
	}
}