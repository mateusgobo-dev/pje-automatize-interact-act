package br.com.infox.ibpm.component.tree;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.ibpm.home.LocalizacaoHome;
import br.com.infox.ibpm.home.UsuarioHome;
import br.jus.pje.nucleo.entidades.Localizacao;

public class LocalizacaoNode extends LocalizacaoNodeSearch {

	private static final long serialVersionUID = 1L;

	public LocalizacaoNode(String[] queryChildrenList){
		super(queryChildrenList);
	}

	public LocalizacaoNode(LocalizacaoNode localizacaoNode, Localizacao n, String[] queryChildren){
		super(localizacaoNode, n, queryChildren);
	}

	@Override
	public boolean canSelect(){
		return LocalizacaoHome.instance().checkPermissaoLocalizacao(this);
	}

	@Override
	protected EntityNode<Localizacao> createChildNode(Localizacao n){
		LocalizacaoNode node = new LocalizacaoNode(this, n, getQueryChildren());
		node.setShowEstrutura(getShowEstrutura());
		node.setExibirModeloLocalizacao(isExibirModeloLocalizacao());
		return node;
	}

	@Override
	protected EntityNode<Localizacao> createRootNode(Localizacao n){
		LocalizacaoNode node = new LocalizacaoNode(null, n, getQueryChildren());
		node.setShowEstrutura(getShowEstrutura());
		node.setExibirModeloLocalizacao(isExibirModeloLocalizacao());
		return node;
	}

	@Override
	public List<EntityNode<Localizacao>> getRoots(Query queryRoots){
		Localizacao estrutura = UsuarioHome.getUsuarioLocalizacaoAtual().getLocalizacaoFisica();
		
		if (estrutura != null){
			List<EntityNode<Localizacao>> nodes = new ArrayList<EntityNode<Localizacao>>();
			LocalizacaoNode no = new LocalizacaoNode(null, estrutura, getQueryChildren());
			no.setShowEstrutura(getShowEstrutura());
			no.setExibirModeloLocalizacao(isExibirModeloLocalizacao());
			nodes.add(no);
			return nodes;
		} else {
			return super.getRoots(queryRoots);
		}
	}
}