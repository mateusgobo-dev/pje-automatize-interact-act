package br.jus.cnj.pje.amqp.model.dto.jobs;

import java.io.Serializable;
import java.util.Date;

public class CloudEventBulkIdentificatioin implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uuidLote;
	private int numJob;
	private Date dataJob;

	public CloudEventBulkIdentificatioin(String uuidLote, int numJob) {
		super();
		this.uuidLote = uuidLote;
		this.numJob = numJob;
	}

	public CloudEventBulkIdentificatioin(String uuidLote, int numJob, Date dataJob) {
		this.uuidLote = uuidLote;
		this.numJob = numJob;
		this.dataJob = dataJob;
	}

	public CloudEventBulkIdentificatioin() {
		super();
	}

	public String getUuidLote() {
		return uuidLote;
	}

	public void setUuidLote(String uuidLote) {
		this.uuidLote = uuidLote;
	}

	public int getNumJob() {
		return numJob;
	}

	public void setNumJob(int numJob) {
		this.numJob = numJob;
	}

	public Date getDataJob() {
		return dataJob;
	}

	public void setDataJob(Date dataJob) {
		this.dataJob = dataJob;
	}
}