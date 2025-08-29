package br.jus.cnj.pje.amqp.model.dto;

import java.util.List;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;

public class ValidaOABCloudEvent implements CloudEventPayload<ValidaOABCloudEvent, ValidaOABCloudEvent> {

	private static final long serialVersionUID = 1L;

	private List<String> documentos;

	public ValidaOABCloudEvent() {
		super();
	}

	public ValidaOABCloudEvent(List<String> documentos) {
		this.documentos = documentos;

	}

	public List<String> getDocumentos() {
		return documentos;
	}

	public void setIdProcessoParteExpediente(List<String> documentos) {
		this.documentos = documentos;
	}

	@Override
	public ValidaOABCloudEvent convertEntityToPayload(ValidaOABCloudEvent entity) {
		this.documentos = entity.getDocumentos();

		return this;
	}

	@Override
	public Long getId(ValidaOABCloudEvent entity) {
		if (entity != null && entity.getDocumentos() != null) {
			return Long.valueOf(entity.getDocumentos().get(0));
		} else {
			return null;
		}
	}
}