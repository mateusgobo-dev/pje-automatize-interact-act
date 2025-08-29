package br.jus.cnj.pje.amqp.model.dto.jobs;

import java.util.Date;
import java.util.List;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;

public class CienciaAutomaticaCloudEvent
		implements CloudEventPayload<CienciaAutomaticaCloudEvent, CienciaAutomaticaCloudEvent> {

	private static final long serialVersionUID = 1L;

	private List<Integer> idsProcessoParteExpediente;
	private Date dtCienciaAutomatica;
	private Integer prazoPresuncaoCorreios;
	private CloudEventBulkIdentificatioin bulkIdentification;

	public CienciaAutomaticaCloudEvent() {
		super();
	}

	public CienciaAutomaticaCloudEvent(Date dtCienciaAutomatica, List<Integer> idsProcessoParteExpediente,
			Integer prazoPresuncaoCorreios, String uuidLote, Integer numJob) {
		this.idsProcessoParteExpediente = idsProcessoParteExpediente;
		this.dtCienciaAutomatica = dtCienciaAutomatica;
		this.prazoPresuncaoCorreios = prazoPresuncaoCorreios;
		this.bulkIdentification = new CloudEventBulkIdentificatioin(uuidLote, numJob, new Date());
	}

	public CienciaAutomaticaCloudEvent(CienciaAutomaticaCloudEvent cienciaAutomaticaCloudEvent) {
		this.convertEntityToPayload(cienciaAutomaticaCloudEvent);
	}

	public List<Integer> getIdsProcessoParteExpediente() {
		return idsProcessoParteExpediente;
	}

	public void setIdsProcessoParteExpediente(List<Integer> idsProcessoParteExpediente) {
		this.idsProcessoParteExpediente = idsProcessoParteExpediente;
	}

	public Date getDtCienciaAutomatica() {
		return dtCienciaAutomatica;
	}

	public void setDtCienciaAutomatica(Date dtCienciaAutomatica) {
		this.dtCienciaAutomatica = dtCienciaAutomatica;
	}

	public Integer getPrazoPresuncaoCorreios() {
		return prazoPresuncaoCorreios;
	}

	public void setPrazoPresuncaoCorreios(Integer prazoPresuncaoCorreios) {
		this.prazoPresuncaoCorreios = prazoPresuncaoCorreios;
	}

	public CloudEventBulkIdentificatioin getBulkIdentification() {
		return bulkIdentification;
	}

	public void setBulkIdentification(CloudEventBulkIdentificatioin bulkIdentification) {
		this.bulkIdentification = bulkIdentification;
	}

	@Override
	public CienciaAutomaticaCloudEvent convertEntityToPayload(CienciaAutomaticaCloudEvent entity) {
		this.idsProcessoParteExpediente = entity.getIdsProcessoParteExpediente();
		this.dtCienciaAutomatica = entity.getDtCienciaAutomatica();
		this.prazoPresuncaoCorreios = entity.getPrazoPresuncaoCorreios();
		this.bulkIdentification = entity.getBulkIdentification();

		return this;
	}

	@Override
	public Long getId(CienciaAutomaticaCloudEvent entity) {
		if (entity != null && entity.getIdsProcessoParteExpediente() != null) {
			return Long.valueOf(entity.getIdsProcessoParteExpediente().get(0));
		} else {
			return null;
		}
	}
}