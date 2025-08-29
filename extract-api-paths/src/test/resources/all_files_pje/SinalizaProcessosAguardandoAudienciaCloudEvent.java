package br.jus.cnj.pje.amqp.model.dto.jobs;

import java.util.Date;
import java.util.List;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;

public class SinalizaProcessosAguardandoAudienciaCloudEvent implements
		CloudEventPayload<SinalizaProcessosAguardandoAudienciaCloudEvent, SinalizaProcessosAguardandoAudienciaCloudEvent> {
	private static final long serialVersionUID = 1L;

	private List<Integer> idsAudiencia;
	private CloudEventBulkIdentificatioin bulkIdentification;

	public SinalizaProcessosAguardandoAudienciaCloudEvent() {
		super();
	}

	public SinalizaProcessosAguardandoAudienciaCloudEvent(List<Integer> idsAudiencia, String uuidLote, Integer numJob) {
		this.idsAudiencia = idsAudiencia;
		this.bulkIdentification = new CloudEventBulkIdentificatioin(uuidLote, numJob, new Date());
	}

	public SinalizaProcessosAguardandoAudienciaCloudEvent(
			SinalizaProcessosAguardandoAudienciaCloudEvent sinalizaProcessosAguardandoAudienciaCloudEvent) {
		this.convertEntityToPayload(sinalizaProcessosAguardandoAudienciaCloudEvent);
	}

	public List<Integer> getIdsAudiencia() {
		return idsAudiencia;
	}

	public void setIdsAudiencia(List<Integer> idsAudiencia) {
		this.idsAudiencia = idsAudiencia;
	}

	public CloudEventBulkIdentificatioin getBulkIdentification() {
		return bulkIdentification;
	}

	public void setBulkIdentification(CloudEventBulkIdentificatioin bulkIdentification) {
		this.bulkIdentification = bulkIdentification;
	}

	@Override
	public SinalizaProcessosAguardandoAudienciaCloudEvent convertEntityToPayload(
			SinalizaProcessosAguardandoAudienciaCloudEvent entity) {
		this.idsAudiencia = entity.getIdsAudiencia();
		this.bulkIdentification = entity.getBulkIdentification();

		return this;
	}

	@Override
	public Long getId(SinalizaProcessosAguardandoAudienciaCloudEvent entity) {
		if (entity != null && entity.getIdsAudiencia() != null) {
			return Long.valueOf(entity.getIdsAudiencia().get(0));
		} else {
			return null;
		}
	}
}