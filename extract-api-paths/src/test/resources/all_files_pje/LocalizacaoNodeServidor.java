package br.com.infox.cliente.component.tree;

import java.util.List;
import javax.persistence.Query;
import br.com.infox.component.tree.EntityNode;
import br.jus.pje.nucleo.entidades.Localizacao;

public class LocalizacaoNodeServidor extends EntityNode<Localizacao>{

	private static final long serialVersionUID = 1L;
	private boolean showEstrutura;

	/**
	 * Filtro que será usado apenas com os filhos do nó raiz
	 */
	private String queryFilhosNoRaiz;

	public LocalizacaoNodeServidor(String queryChildren){
		super(queryChildren);
	}

	public LocalizacaoNodeServidor(LocalizacaoNodeServidor localizacaoNodeSearch,
			Localizacao n, String[] queryChildren){
		super(localizacaoNodeSearch, n, queryChildren);
	}

	public LocalizacaoNodeServidor(String[] queryChildrenList){
		super(queryChildrenList);
	}

	@Override
	public List<EntityNode<Localizacao>> getRoots(Query queryRoots){
		List<EntityNode<Localizacao>> roots = super.getRoots(queryRoots);
		for (EntityNode<Localizacao> entityNode : roots){
			LocalizacaoNodeServidor localizacaoNode = (LocalizacaoNodeServidor) entityNode;
			localizacaoNode.setShowEstrutura(showEstrutura);
		}
		return roots;
	}

	@Override
	protected EntityNode<Localizacao> createChildNode(Localizacao n){
		LocalizacaoNodeServidor node = new LocalizacaoNodeServidor(this, n, getQueryChildren());
		repasarDadosParaFilho(node);
		return node;
	}

	@Override
	protected EntityNode<Localizacao> createRootNode(Localizacao n){
		LocalizacaoNodeServidor node = new LocalizacaoNodeServidor(null, n, getQueryChildren());
		repasarDadosParaFilho(node);
		return node;
	}

	private void repasarDadosParaFilho(LocalizacaoNodeServidor node){
		node.setShowEstrutura(getShowEstrutura());
		node.setQueryFilhoNoRaiz(queryFilhosNoRaiz);
	}

	public void setShowEstrutura(boolean showEstrutura){
		this.showEstrutura = showEstrutura;
	}

	public boolean getShowEstrutura(){
		return showEstrutura;
	}

	public void setQueryFilhoNoRaiz(String queryFilhosNoRaiz){
		this.queryFilhosNoRaiz = queryFilhosNoRaiz;
	}

}