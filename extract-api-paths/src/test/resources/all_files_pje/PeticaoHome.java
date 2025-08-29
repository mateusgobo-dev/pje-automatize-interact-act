package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.jus.pje.nucleo.entidades.Peticao;
import br.jus.pje.nucleo.entidades.TipoModeloDocumento;

@Name("peticaoHome")
@BypassInterceptors
public class PeticaoHome extends AbstractPeticaoHome<Peticao> {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(PeticaoHome.class);

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return ret;
	}

	@Override
	public String update() {
		String ret = null;
		try {
			ret = super.update();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return ret;
	}

	@Override
	public String remove(Peticao obj) {
		setInstance(obj);
		obj.setAtivo(Boolean.FALSE);
		super.update();
		newInstance();
		Contexts.removeFromAllContexts("peticaoGrid");
		return "updated";
	}

	public void addPeticaoTipoModeloDocumento(TipoModeloDocumento obj, String gridId) {
		if (getInstance() != null) {
			getInstance().getTipoModeloDocumentoList().add(obj);
			refreshGrid(gridId);
		}
	}

	public void removePeticaoTipoModeloDocumento(TipoModeloDocumento obj, String gridId) {
		if (getInstance() != null) {
			getInstance().getTipoModeloDocumentoList().remove(obj);
			refreshGrid(gridId);
		}
	}

}