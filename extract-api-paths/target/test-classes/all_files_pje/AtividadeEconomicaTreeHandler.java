/* $Id: AssuntoTreeHandler.java 862 2010-09-27 18:51:52Z danielsilva $ */

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
package br.jus.csjt.pje.view.action.component.tree;

import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;
import br.com.infox.cliente.component.tree.AbstractTreeHandlerCachedRoots;
import br.com.infox.component.tree.EntityNode;
import br.jus.pje.jt.entidades.AtividadeEconomica;

@Name(AtividadeEconomicaTreeHandler.NAME)
@BypassInterceptors
public class AtividadeEconomicaTreeHandler extends AbstractTreeHandlerCachedRoots<AtividadeEconomica> {

	public static final String NAME = "atividadeEconomicaTree";
	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		return "SELECT distinct a FROM AtividadeEconomica a left join fetch a.atividadeEconomicaList f WHERE a.atividadeEconomicaPai is null "
				+ " ORDER BY a.nomeAtividadeEconomica";
	}

	@Override
	protected String getQueryChildren() {
		return "FROM " + AtividadeEconomica.class.getSimpleName() + " a " + " WHERE a.atividadeEconomicaPai = :"
				+ EntityNode.PARENT_NODE;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void selectListener(NodeSelectedEvent ev) {
		HtmlTree tree = (HtmlTree) ev.getSource();
		treeId = tree.getId();
		// somente permite seleciona folhas
		if (tree.isLeaf()) {
			super.selectListener(ev);
		} else if (tree.isSelected()) {
			FacesMessages.instance().add(Severity.INFO,
					"Favor selecionar uma atividade mais específica dentro da atividade selecionada.");
		}
	}

	@Override
	protected List<AtividadeEconomica> getChildren(AtividadeEconomica entity){
		return entity.getAtividadeEconomicaList();
	}

	// @Override
	// protected AtividadeEconomica getEntityToIgnore() {
	// return ComponentUtil.getInstance(AtividadeEconomicaList.NAME);
	// }

}
