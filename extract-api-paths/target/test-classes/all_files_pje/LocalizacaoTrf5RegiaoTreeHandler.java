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

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Localizacao;

@Name("localizacaoTrf5RegiaoTree")
@BypassInterceptors
public class LocalizacaoTrf5RegiaoTreeHandler extends AbstractTreeHandler<Localizacao> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		String papel = Authenticator.getPapelAtual().getIdentificador();
		StringBuilder sb = new StringBuilder("select n from Localizacao n where");
		if (papel.equalsIgnoreCase("admin") || papel.equalsIgnoreCase("administrador")) {
			sb.append(" n.idLocalizacao = ");
			sb.append(ParametroUtil.instance().getLocalizacaoTribunal().getIdLocalizacao());
		} else {
			sb.append(" n.localizacaoPai.idLocalizacao = ");
			sb.append(ParametroUtil.instance().getLocalizacaoTribunal().getIdLocalizacao());
		}
		sb.append(" and n.idLocalizacao not in (select o.localizacao.idLocalizacao from Procuradoria o)");
		sb.append(" and n.estrutura = false and n.ativo = true order by n.localizacao");
		return sb.toString();
	}

	@Override
	protected String getQueryChildren() {
		StringBuilder sb = new StringBuilder("select n from Localizacao n where localizacaoPai = :");
		sb.append(EntityNode.PARENT_NODE);
		sb.append(" order by n.faixaInferior ");
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
}