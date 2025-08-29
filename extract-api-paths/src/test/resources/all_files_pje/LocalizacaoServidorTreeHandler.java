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

import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.LocalizacaoUtil;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Localizacao;

@Name(LocalizacaoServidorTreeHandler.NAME)
@BypassInterceptors
public class LocalizacaoServidorTreeHandler extends AbstractTreeHandler<Localizacao> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "localizacaoServidorTreeHandler";
	
	/**
	 * @return instância do componente.
	 */
	public static LocalizacaoServidorTreeHandler instance() {
		return ComponentUtil.getComponent(LocalizacaoServidorTreeHandler.class);
	}
	
	@Override
	protected String getQueryRoots() {
		StringBuilder sb = new StringBuilder("SELECT l from Localizacao l");
		sb.append(" WHERE l.faixaInferior IS NOT NULL ");
		
		String idsLocalizacoes = "-1";
		if(CollectionUtilsPje.isNotEmpty(this.getRootNodeList()) ) {
			idsLocalizacoes = LocalizacaoUtil.converteLocalizacoesList(this.getRootNodeList());
		}else if(this.getRootNode() != null) {
			idsLocalizacoes = String.valueOf(this.getRootNode().getIdLocalizacao());
		}
		
		if(this.isShowRootNode()) {
			sb.append(" AND l.idLocalizacao IN (" + idsLocalizacoes + ")");
		}else {
			sb.append(" AND l.localizacaoPai.idLocalizacao IN (" + idsLocalizacoes + ")");
		}
		sb.append(" ORDER BY l.faixaInferior, l.localizacao");
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
		return "evtSelectLocalizacao";
	}

	@Override
	protected Localizacao getEntityToIgnore() {
		return ComponentUtil.getInstance("localizacaoHome");
	}

	@Override
	protected EntityNode<Localizacao> createNode() {
		return super.createNode();
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

	@Override
	public void clearTree() {
		super.clearTree();
	}
}