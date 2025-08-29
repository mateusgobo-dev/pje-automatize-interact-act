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
package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


@Name(value=ProcessoOrgaoJulgadorSuggestBean.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class ProcessoOrgaoJulgadorSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	public static final String NAME = "processoOrgaoJulgadorSuggest";
	private static final long serialVersionUID = 1L;

	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.processo from ProcessoTrf o ");
		sb.append("where lower(o.processo.numeroProcesso) like lower(concat ('%', :");
		sb.append(INPUT_PARAMETER);
		sb.append(", '%')) ");
		sb.append("and o.processo.numeroProcesso != null ");		
		sb.append("and o.orgaoJulgador = #{estatisticaJusticaFederalPermissaoSegredoJusticaAction.instance().getOrgaoJulgador()} ");	
		sb.append("order by o");
		return sb.toString();
	}
	
	
	public static ProcessoOrgaoJulgadorSuggestBean instance() {
		return ComponentUtil.getComponent(NAME);
	}
}