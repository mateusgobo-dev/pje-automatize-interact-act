package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.CentralMandadoLocalizacao;

@Name(CentralMandadoLocalizacaoHome.NAME)
@BypassInterceptors
public class CentralMandadoLocalizacaoHome extends AbstractCentralMandadoLocalizacaoHome<CentralMandadoLocalizacao> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "centralMandadoLocalizacaoHome";

	public static CentralMandadoLocalizacaoHome instance() {
		return ComponentUtil.getComponent(CentralMandadoLocalizacaoHome.NAME);
	}

	@Override
	public String persist() {
		String persist = super.persist();
		refreshGrid("centralMandadoLocalizacaoGrid");
		return persist;
	}

	@Override
	public String update() {
		String update = super.update();
		refreshGrid("centralMandadoLocalizacaoGrid");
		return update;
	}

	@Override
	public String remove(CentralMandadoLocalizacao obj) {
		String update = super.remove(obj);
		refreshGrid("centralMandadoLocalizacaoGrid");
		return update;
	}
}