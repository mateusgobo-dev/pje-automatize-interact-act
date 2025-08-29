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
package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.richfaces.event.NodeSelectedEvent;

import br.com.infox.component.tree.EntityNode;
import br.com.infox.ibpm.component.tree.LocalizacaoEstruturaSearchTreeHandler;
import br.com.infox.ibpm.component.tree.LocalizacaoEstruturaTreeHandler;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Localizacao;

/**
 * Classe destinada para a treeview de localização do segundo grau
 */

@Name("localizacaoEstruturaServidorSegundoGrauTree")
@BypassInterceptors
public class LocalizacaoEstruturaServidorSegundoGrauTreeHandler extends LocalizacaoEstruturaSearchTreeHandler {

	private static final long serialVersionUID = -3108287866771999436L;

	/**
	 * @return instância do componente.
	 */
	public static LocalizacaoEstruturaServidorSegundoGrauTreeHandler instance() {
		return ComponentUtil.getComponent(LocalizacaoEstruturaServidorSegundoGrauTreeHandler.class);
	}
	
	public void selectListener(NodeSelectedEvent evento) {
		super.selectListener(evento);

		LocalizacaoEstruturaTreeHandler locEstTree = ComponentUtil.getComponent(LocalizacaoEstruturaTreeHandler.class);
		Localizacao loc = locEstTree.getEstrutura(evento);

		LocalizacaoNaoEstruturadaServidorTreeHandler locNaoEstSerTree = ComponentUtil.getComponent(LocalizacaoNaoEstruturadaServidorTreeHandler.class);
		if (loc != null) {
			locNaoEstSerTree.setIdLocalizacao(loc.getIdLocalizacao());
			locNaoEstSerTree.clearTree();
		}
	}
	 
	@Override
	protected String getQueryRoots() {
		Localizacao localizacaoFisica = getLocalizacaoFisica();
		return getLocalizacaoManager().getQueryRootsLocalizacao(
				getUsrLocalizacaoHome().getInstance(), (localizacaoFisica != null ? localizacaoFisica.getIdLocalizacao() : null), getIdLocalizacaoPai());
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
		LocalizacaoNodeServidor node = new LocalizacaoNodeServidor(getQueryChildrenList());
		String queryFilhoPrimeiroNivel = getQueryFilhoPrimeiroNivel();
		if (queryFilhoPrimeiroNivel != null) {
			node.setQueryFilhoNoRaiz(queryFilhoPrimeiroNivel);
		}
		return node;
	}

	/**
	 * Obtem a lista dos ids que deverão ser filtrados para o filho
	 * 
	 * @return sql para retornar a lista de localizações
	 */
	private String getQueryFilhoPrimeiroNivel() {
		StringBuilder sb = new StringBuilder();
		sb.append("select l from Localizacao l where l.localizacaoPai =:");
		sb.append(EntityNode.PARENT_NODE);
		return sb.toString();
	}
}