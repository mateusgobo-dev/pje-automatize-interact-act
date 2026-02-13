package br.com.infox.ibpm.bean;

import br.com.infox.ibpm.home.FluxoHome;
import br.com.itx.component.FileHome;

public class ImportarFluxoFileBean extends FileHome {

	private static final long serialVersionUID = 1L;

	@Override
	public void clear() {
		super.clear();
		FluxoHome fh = FluxoHome.instance();
		fh.setRenderedAgrupamento(false);
		fh.setRenderedLocalizacao(false);
		fh.setRenderedPapel(false);
		fh.setValidado(false);
	}

}