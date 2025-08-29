package br.com.infox.cliente.home;

import org.jboss.seam.Component;

import br.com.infox.ibpm.home.EventoHome;
import br.com.infox.ibpm.home.TipoProcessoDocumentoHome;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoDetalhe;

public abstract class AbstractTipoProcessoDocumentoDetalheHome<T> extends AbstractHome<TipoProcessoDocumentoDetalhe> {

	private static final long serialVersionUID = 1L;

	public void setTipoProcessoDocumentoDetalheIdTipoProcessoDocumentoDetalhe(Integer id) {
		setId(id);
	}

	public Integer getTipoProcessoDocumentoDetalheIdTipoProcessoDocumentoDetalhe() {
		return (Integer) getId();
	}

	@Override
	protected TipoProcessoDocumentoDetalhe createInstance() {
		TipoProcessoDocumentoDetalhe tipoProcessoDocumentoDetalhe = new TipoProcessoDocumentoDetalhe();

		TipoProcessoDocumentoHome tipoProcessoDocumentoHome = (TipoProcessoDocumentoHome) Component.getInstance(
				"tipoProcessoDocumentoHome", false);
		if (tipoProcessoDocumentoHome != null) {
			tipoProcessoDocumentoDetalhe.setTipoProcessoDocumento(tipoProcessoDocumentoHome.getDefinedInstance());
		}

		return tipoProcessoDocumentoDetalhe;
	}

	@Override
	public String remove() {
		AplicacaoClasseHome aplicacaoClasse = (AplicacaoClasseHome) Component.getInstance("aplicacaoClasseHome", false);
		if (aplicacaoClasse != null) {
			aplicacaoClasse.getInstance().getAplicacaoClasseEventoList().remove(instance);
		}
		EventoHome evento = (EventoHome) Component.getInstance("eventoHome", false);
		if (evento != null) {
			evento.getInstance().getAplicacaoClasseEventoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(TipoProcessoDocumentoDetalhe obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("tipoProcessoDocumentoDetalheGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = null;
		action = super.persist();

		if (action != null) {
			newInstance();
		}
		return action;
	}
}