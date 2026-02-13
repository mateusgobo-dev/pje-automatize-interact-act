package br.com.infox.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.component.tree.LocalizacaoEstruturaTreeHandler;
import br.jus.pje.nucleo.entidades.Localizacao;

@Name(LocalizacaoFisicaSearchTreeHandler.NAME)
@BypassInterceptors
public class LocalizacaoFisicaSearchTreeHandler extends LocalizacaoEstruturaTreeHandler {

	private static final long serialVersionUID = 1L;
	protected static final String NAME = "localizacaoFisicaSearchTreeHandler";

	@Override
	protected String getQueryRoots() {
		Localizacao localizacaoTribunal = ParametroUtil.instance().getLocalizacaoTribunal();
		StringBuilder sb = new StringBuilder("SELECT n FROM Localizacao n WHERE n.localizacaoPai IS NULL ");

		if (localizacaoTribunal != null) {
			sb.append(" AND n.idLocalizacao = " + localizacaoTribunal.getIdLocalizacao());
		}

		sb.append(" ORDER BY n.faixaInferior");
		return sb.toString();
	}

	@Override
	protected EntityNode<Localizacao> createNode() {
		LocalizacaoFisicaNodeSearch node = new LocalizacaoFisicaNodeSearch(getQueryChildrenList());
		return node;
	}
}