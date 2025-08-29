package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoDetalhe;

@Name("tipoProcessoDocumentoDetalheHome")
@BypassInterceptors
public class TipoProcessoDocumentoDetalheHome extends
		AbstractTipoProcessoDocumentoDetalheHome<TipoProcessoDocumentoDetalhe> {

	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		String ret = null;
		try {
			getInstance().setTipoProcessoDocumento(TipoProcessoDocumentoTrfHome.instance().getInstance());
			ret = super.persist();
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
		}
		refreshGrid("tipoProcessoDocumentoDetalheGrid");
		return ret;
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

	@Override
	public String remove(TipoProcessoDocumentoDetalhe obj) {
		setInstance(obj);
		refreshGrid("tipoProcessoDocumentoDetalheGrid");
		return super.remove(obj);
	}
}