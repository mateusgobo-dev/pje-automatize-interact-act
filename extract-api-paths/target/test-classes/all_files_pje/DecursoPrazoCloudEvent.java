package br.jus.cnj.pje.amqp.model.dto.jobs;

import java.util.Date;
import java.util.List;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;

public class DecursoPrazoCloudEvent implements CloudEventPayload<DecursoPrazoCloudEvent, DecursoPrazoCloudEvent> {

	private static final long serialVersionUID = 1L;

	private List<Integer> idsProcessoParteExpediente;
	private Date dtDecurso;
	private CloudEventBulkIdentificatioin bulkIdentification;

	public DecursoPrazoCloudEvent() {
		super();
	}

	public DecursoPrazoCloudEvent(Date dtDecurso, List<Integer> idsProcessoParteExpediente, String uuidLote,
			Integer numJob) {
		this.idsProcessoParteExpediente = idsProcessoParteExpediente;
		this.dtDecurso = dtDecurso;
		this.bulkIdentification = new CloudEventBulkIdentificatioin(uuidLote, numJob, new Date());
	}

	public DecursoPrazoCloudEvent(DecursoPrazoCloudEvent decursoPrazoCloudEvent) {
		this.convertEntityToPayload(decursoPrazoCloudEvent);
	}

	public List<Integer> getIdsProcessoParteExpediente() {
		return idsProcessoParteExpediente;
	}

	public void setIdsProcessoParteExpediente(List<Integer> idsProcessoParteExpediente) {
		this.idsProcessoParteExpediente = idsProcessoParteExpediente;
	}

	public Date getDtDecurso() {
		return dtDecurso;
	}

	public void setDtDecurso(Date dtDecurso) {
		this.dtDecurso = dtDecurso;
	}

	public CloudEventBulkIdentificatioin getBulkIdentification() {
		return bulkIdentification;
	}

	public void setBulkIdentification(CloudEventBulkIdentificatioin bulkIdentification) {
		this.bulkIdentification = bulkIdentification;
	}

	@Override
	public Long getId(DecursoPrazoCloudEvent entity) {
		if (entity != null && entity.getIdsProcessoParteExpediente() != null) {
			return Long.valueOf(entity.getIdsProcessoParteExpediente().get(0));
		} else {
			return null;
		}
	}

	@Override
	public DecursoPrazoCloudEvent convertEntityToPayload(DecursoPrazoCloudEvent entity) {
		this.idsProcessoParteExpediente = entity.getIdsProcessoParteExpediente();
		this.dtDecurso = entity.getDtDecurso();
		this.bulkIdentification = entity.getBulkIdentification();

		return this;
	}
}