package br.com.infox.bpm.taskPage.remessacnj;

import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

public class ProcessoDocumentoRemessa extends ProcessoDocumento {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Boolean selecionado = true;
	private Boolean flagPrincipal = false;
	private ProcessoDocumento original;
	private TipoProcessoDocumento tipoDocumentoRemessa;

	public void setSelecionado(Boolean selecionado) {
		this.selecionado = selecionado;
	}

	public Boolean getSelecionado() {
		return selecionado;
	}

	public ProcessoDocumento getOriginal() {
		return original;
	}

	public void setOriginal(ProcessoDocumento original) {
		this.original = original;
	}

	public Boolean getFlagPrincipal() {
		return flagPrincipal;
	}

	public void setFlagPrincipal(Boolean flagPrincipal) {
		this.flagPrincipal = flagPrincipal;
	}

	public TipoProcessoDocumento getTipoDocumentoRemessa() {
		return tipoDocumentoRemessa;
	}

	public void setTipoDocumentoRemessa(
			TipoProcessoDocumento tipoDocumentoRemessa) {
		this.tipoDocumentoRemessa = tipoDocumentoRemessa;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumento> getEntityClass() {
		return ProcessoDocumentoRemessa.class;
	}
}
