package br.com.infox.component.tree;

import java.util.List;

import br.jus.pje.nucleo.entidades.Localizacao;

public class LocalizacaoFisicaNodeSearch extends EntityNode<Localizacao> {
	private static final long serialVersionUID = 1L;

	public LocalizacaoFisicaNodeSearch(String[] queryChildrenList) {
		super(queryChildrenList);
	}

	@Override
	protected List<Localizacao> getChildrenList(String query, Localizacao localizacao) {
		return super.getChildrenList(query, localizacao);
	}
}