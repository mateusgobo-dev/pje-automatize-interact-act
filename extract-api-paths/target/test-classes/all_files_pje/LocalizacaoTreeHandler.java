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

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.component.tree.AbstractTreeHandlerCachedRoots;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.Localizacao;

/*
 * PJEII-5260: Bruno Sales - 2013-02-06. Alterações feitas pela JT. Classe alterada para herdar de AbstractTreeHandlerCachedRoots 
 * para otimizar a execução das consultas na montagem da árvore de localizações (estava demorando muito e estourava o concurrentRequestTimeout).  
 */

@Name("localizacaoTree")
@BypassInterceptors
public class LocalizacaoTreeHandler extends AbstractTreeHandlerCachedRoots<Localizacao> {

	private static final long serialVersionUID = 1L;

	private boolean pesquisarApenasModelosLocalizacaoEstruturada = false;
	
	@Override
	protected String getQueryRoots() {
		ParametroService parametroService = (ParametroService)Component.getInstance("parametroService");
		String idLocalizacaoTribunal = parametroService.valueOf("idLocalizacaoTribunal");
		
		StringBuilder sb = new StringBuilder("select n from Localizacao n ");
		sb.append("where localizacaoPai is null ");
		sb.append("and (estrutura = true");

		if (!pesquisarApenasModelosLocalizacaoEstruturada && idLocalizacaoTribunal != null) {
			sb.append(" or idLocalizacao = " + idLocalizacaoTribunal);
		}
		
		sb.append(")");
		sb.append("order by localizacao");

		return sb.toString();
	}

	@Override
	protected String getQueryChildren() {
		return "select n from Localizacao n where localizacaoPai = :" + EntityNode.PARENT_NODE;
	}
	
	@Override
	protected List<Localizacao> getChildren(Localizacao entity) {
		return entity.getLocalizacaoList();
	}

	@Override
	protected String getEventSelected() {
		return "evtSelectLocalizacao";
	}
	
	@Override
	protected Localizacao getEntityToIgnore() {
		return ComponentUtil.getInstance("localizacaoHome");
	}
	
	public boolean isPesquisarApenasModelosLocalizacaoEstruturada() {
		return pesquisarApenasModelosLocalizacaoEstruturada;
	}
	
	public void setPesquisarApenasModelosLocalizacaoEstruturada(boolean pesquisarApenasModelosLocalizacaoEstruturada) {
		this.pesquisarApenasModelosLocalizacaoEstruturada = pesquisarApenasModelosLocalizacaoEstruturada;
	}
}