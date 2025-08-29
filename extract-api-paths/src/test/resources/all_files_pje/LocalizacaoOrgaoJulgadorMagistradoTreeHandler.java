package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.OrgaoJulgadorHome;
import br.com.infox.ibpm.component.tree.LocalizacaoTreeHandler;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@Name("localizacaoOrgaoJulgadorMagistradoTree")
@BypassInterceptors
public class LocalizacaoOrgaoJulgadorMagistradoTreeHandler extends LocalizacaoTreeHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		OrgaoJulgador orgaoJulgador = OrgaoJulgadorHome.instance().getInstance();
		int idLocalizacao = 0;
		Localizacao localizacao = orgaoJulgador.getLocalizacao();
		if (localizacao != null && localizacao.getEstruturaFilho() != null) {
			idLocalizacao = localizacao.getEstruturaFilho().getIdLocalizacao();
		}
		return "select n from Localizacao n " + "where localizacaoPai.idLocalizacao = " + idLocalizacao
				+ "order by localizacao";
	}

}
