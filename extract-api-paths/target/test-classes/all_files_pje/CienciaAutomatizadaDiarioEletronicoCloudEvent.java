package br.jus.cnj.pje.amqp.model.dto.jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.jus.cnj.pje.controleprazos.CodigosMateriaExpediente;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.enums.TipoPesquisaDJEEnum;

public class CienciaAutomatizadaDiarioEletronicoCloudEvent implements
		CloudEventPayload<CienciaAutomatizadaDiarioEletronicoCloudEvent, CienciaAutomatizadaDiarioEletronicoCloudEvent> {

	private static final long serialVersionUID = 1L;

	private TipoPesquisaDJEEnum tipoPesquisa;

	private boolean consultarMateriasPeloReciboDePublicacaoDJE;

	private List<Integer> idsExpedientes;

	private Calendar dtPesquisa;

	private List<CodigosMateriaExpediente> codigosMateriaExpedientes;

	private CloudEventBulkIdentificatioin bulkIdentification;

	public CienciaAutomatizadaDiarioEletronicoCloudEvent() {
		super();
	}

	public CienciaAutomatizadaDiarioEletronicoCloudEvent(List<Integer> idsExpedientes,
			List<CodigosMateriaExpediente> codigosMateriaExpedientes,
			boolean consultarMateriasPeloReciboDePublicacaoDJE, String uuidLote, Integer numJob) {
		this.tipoPesquisa = TipoPesquisaDJEEnum.MATERIA;
		this.consultarMateriasPeloReciboDePublicacaoDJE = consultarMateriasPeloReciboDePublicacaoDJE;
		this.idsExpedientes = idsExpedientes;
		this.codigosMateriaExpedientes = codigosMateriaExpedientes;

		this.bulkIdentification = new CloudEventBulkIdentificatioin(uuidLote, numJob, new Date());
	}

	public CienciaAutomatizadaDiarioEletronicoCloudEvent(List<Integer> idsExpedientes, Calendar dtPesquisa,
			String uuidLote, Integer numJob) {
		this.tipoPesquisa = TipoPesquisaDJEEnum.DATA;
		this.idsExpedientes = idsExpedientes;
		this.dtPesquisa = dtPesquisa;

		this.bulkIdentification = new CloudEventBulkIdentificatioin(uuidLote, numJob, new Date());
	}

	public CienciaAutomatizadaDiarioEletronicoCloudEvent(
			CienciaAutomatizadaDiarioEletronicoCloudEvent cienciaAutomatizadaDiarioEletronicoCloudEvent) {
		this.convertEntityToPayload(cienciaAutomatizadaDiarioEletronicoCloudEvent);
	}

	public TipoPesquisaDJEEnum getTipoPesquisa() {
		return tipoPesquisa;
	}

	public void setTipoPesquisa(TipoPesquisaDJEEnum tipoPesquisa) {
		this.tipoPesquisa = tipoPesquisa;
	}

	public boolean isConsultarMateriasPeloReciboDePublicacaoDJE() {
		return consultarMateriasPeloReciboDePublicacaoDJE;
	}

	public void setConsultarMateriasPeloReciboDePublicacaoDJE(boolean consultarMateriasPeloReciboDePublicacaoDJE) {
		this.consultarMateriasPeloReciboDePublicacaoDJE = consultarMateriasPeloReciboDePublicacaoDJE;
	}

	public Calendar getDtPesquisa() {
		return dtPesquisa;
	}

	public void setDtPesquisa(Calendar dtPesquisa) {
		this.dtPesquisa = dtPesquisa;
	}

	public List<Integer> getIdsExpedientes() {
		return idsExpedientes;
	}

	public void setIdsExpedientes(List<Integer> idsExpedientes) {
		this.idsExpedientes = idsExpedientes;
	}

	public List<CodigosMateriaExpediente> getCodigosMateriaExpedientes() {
		return codigosMateriaExpedientes;
	}

	public void setCodigosMateriaExpedientes(List<CodigosMateriaExpediente> codigosMateriaExpedientes) {
		this.codigosMateriaExpedientes = codigosMateriaExpedientes;
	}

	public CloudEventBulkIdentificatioin getBulkIdentification() {
		return bulkIdentification;
	}

	public void setBulkIdentification(CloudEventBulkIdentificatioin bulkIdentification) {
		this.bulkIdentification = bulkIdentification;
	}

	@Override
	public Long getId(CienciaAutomatizadaDiarioEletronicoCloudEvent entity) {
		if (entity != null && entity.getIdsExpedientes() != null) {
			return Long.valueOf(entity.getIdsExpedientes().get(0));
		} else {
			return null;
		}
	}

	@Override
	public CienciaAutomatizadaDiarioEletronicoCloudEvent convertEntityToPayload(
			CienciaAutomatizadaDiarioEletronicoCloudEvent entity) {
		this.tipoPesquisa = entity.getTipoPesquisa();
		this.consultarMateriasPeloReciboDePublicacaoDJE = entity.isConsultarMateriasPeloReciboDePublicacaoDJE();

		this.dtPesquisa = entity.getDtPesquisa();

		this.idsExpedientes = entity.getIdsExpedientes();
		this.codigosMateriaExpedientes = entity.getCodigosMateriaExpedientes();
		this.bulkIdentification = entity.getBulkIdentification();

		return this;
	}
}