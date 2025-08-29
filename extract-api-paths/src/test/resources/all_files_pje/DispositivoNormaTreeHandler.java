package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.DispositivoNorma;
import br.jus.pje.nucleo.entidades.NormaPenal;

@Name("dispositivoNormaTree")
@BypassInterceptors
public class DispositivoNormaTreeHandler extends
		AbstractTreeHandler<DispositivoNorma> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() { // PEGA NÍVEL INICIAL DA HIERARQUIA ...
		Integer idNormaPenal = ((NormaPenal) ComponentUtil
				.getInstance("normaPenalHome")).getIdNormaPenal();
		return "select n from DispositivoNorma n "
				+ "where dispositivoNormaPai is null "
				+ "and n.normaPenal.idNormaPenal =  " + idNormaPenal
				+ " order by n.numeroOrdem";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from DispositivoNorma n where dispositivoNormaPai = :"
				+ EntityNode.PARENT_NODE + " order by n.numeroOrdem ";
	}

	@Override
	protected DispositivoNorma getEntityToIgnore() {
		return ComponentUtil.getInstance("dispositivoNormaHome");
	}

	@Override
	public String getSelectedView(DispositivoNorma selected) {
		String selecionado = "";

		if (selected == null || selected.toString() == null) {
			return selecionado;
		} else {		
			String texto = selected.getDsSimbolo() == null ? "" : selected.getDsSimbolo() + " - ";
			texto += selected.getDsIdentificador();

			if (texto.length() > 25) {
				selecionado = texto.substring(0, 25) + "...";
			} else {
				//texto = selecionado;	
				selecionado = texto;
			}
			return selecionado;
		}
	}
}
