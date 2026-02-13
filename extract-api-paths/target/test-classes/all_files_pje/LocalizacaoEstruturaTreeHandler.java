/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Localizacao;

@Name("localizacaoEstruturaTree")
@BypassInterceptors
public class LocalizacaoEstruturaTreeHandler extends AbstractTreeHandler<Localizacao> {

	private static final long serialVersionUID = 1L;
	private boolean exibirModeloLocalizacao = true;
	
	/**
	 * @return instância do componente.
	 */
	public static LocalizacaoEstruturaTreeHandler instance() {
		return ComponentUtil.getComponent(LocalizacaoEstruturaTreeHandler.class);
	}
	
	@Override
	protected String getQueryRoots() {
		StringBuilder sb = new StringBuilder("select l from Localizacao l where ");
		sb.append(" l.idLocalizacao = " + this.getIdLocalizacaoAtual());
		sb.append(" order by l.faixaInferior");
		return sb.toString();
	}

	@Override
	protected String getQueryChildren() {
		StringBuilder sb = new StringBuilder("select n from Localizacao n where localizacaoPai = :");
		sb.append(EntityNode.PARENT_NODE);
		sb.append(" order by n.faixaInferior");
		return sb.toString();
	}

	@Override
	protected String getEventSelected() {
		return null;
	}

	@Override
	protected Localizacao getEntityToIgnore() {
		return ComponentUtil.getInstance("localizacaoHome");
	}

	@Override
	protected EntityNode<Localizacao> createNode() {
		LocalizacaoNode node = new LocalizacaoNode(getQueryChildrenList());
		node.setExibirModeloLocalizacao(isExibirModeloLocalizacao());
		return node;
	}

	@Override
	public void selectListener(NodeSelectedEvent ev) {
		EntityNode<Localizacao> en = getEntityNode(ev);
		
		if (en != null) {
			setSelected(en.getEntity());
			
			if (exibirModeloLocalizacao) {
				Events.instance().raiseEvent("evtSelectLocalizacaoEstrutura", getSelected(), getEstrutura(en));
			} else {
				Events.instance().raiseEvent("evtSelectLocalizacao", getSelected());
			}
		}
	}
	
	public Localizacao getEstrutura(NodeSelectedEvent ev) {
		EntityNode<Localizacao> en = getEntityNode(ev);
		return en.getEntity().getEstruturaFilho();
	}
	
	@SuppressWarnings("unchecked")
	protected EntityNode<Localizacao> getEntityNode(NodeSelectedEvent ev) {
		HtmlTree tree = (HtmlTree) ev.getSource();
		EntityNode<Localizacao> en = null;
		if (tree != null) {
			treeId = tree.getId();
			en = (EntityNode<Localizacao>) tree.getData();
		}
		return en;
	}

	protected Localizacao getEstrutura(EntityNode<Localizacao> en) {
		EntityNode<Localizacao> parent = en.getParent();
		while (parent != null) {
			if (parent.getEntity().getEstruturaFilho() != null) {
				return parent.getEntity();
			}
			parent = parent.getParent();
		}
		return null;
	}

	public Integer getIdLocalizacaoAtual() {
		return Authenticator.getIdLocalizacaoAtual();
	}

	@Override
	public void clearTree() {
		super.clearTree();
	}
	
	public boolean isExibirModeloLocalizacao() {
		return exibirModeloLocalizacao;
	}
	
	public void setExibirModeloLocalizacao(boolean exibirModeloLocalizacao) {
		this.exibirModeloLocalizacao = exibirModeloLocalizacao;
	}
}