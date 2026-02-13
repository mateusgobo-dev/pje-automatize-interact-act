/* $Id: CargoList.java 13417 2010-10-28 13:49:03Z allan $ */

package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ProcessosPorTarefaSimplesList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessosPorTarefaSimplesList extends EntityList<ProcessoTrf> {

	public static final String NAME = "processosProTarefaSimplesList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select new map(o.nomeFluxo as nomeFluxo, o.nomeTarefa as nomeTarefa, "
			+ "count(o.idProcessInstance) as quantidade) from SituacaoProcesso o ";

	private static final String DEFAULT_ORDER = "o.nomeFluxo, o.nomeTarefa";

	@Override
	public String getGroupBy() {
		return DEFAULT_ORDER;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getCountEjbql() {
		return "select count(distinct o.nomeTarefa) from SituacaoProcesso o";
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected void addSearchFields() {
	}

}
