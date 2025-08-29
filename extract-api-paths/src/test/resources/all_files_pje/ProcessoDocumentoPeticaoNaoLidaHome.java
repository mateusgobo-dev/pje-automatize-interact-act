package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;

import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;

@Name("processoDocumentoPeticaoNaoLidaHome")
@BypassInterceptors
public class ProcessoDocumentoPeticaoNaoLidaHome extends
		AbstractProcessoDocumentoPeticaoNaoLidaHome<ProcessoDocumentoPeticaoNaoLida> {

	private static final long serialVersionUID = 1L;

	public static ProcessoDocumentoPeticaoNaoLidaHome instance() {
		return ComponentUtil.getComponent("processoDocumentoPeticaoNaoLidaHome");
	}
	
	public void alterarRetificado(ProcessoDocumentoPeticaoNaoLida obj) {
		alterarRetificado(obj,"peticaoAvulsaGrid");
	}

	public void alterarRetificado(ProcessoDocumentoPeticaoNaoLida obj, String gridId) {
		obj.setRetificado(Boolean.TRUE);
		update();
		FacesMessages.instance().clear();
		refreshGrid(gridId);

		GridQuery gq = getComponent(gridId);
		if (gq.getResultCount() == 0) {
			ProcessoTrfHome.instance().alterarRetirado();
		}
	}
}