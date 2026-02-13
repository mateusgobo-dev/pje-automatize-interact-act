package br.com.infox.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ClasseJudicialAgrupamento;

@Name(ClasseJudicialAgrupamentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ClasseJudicialAgrupamentoList extends EntityList<ClasseJudicialAgrupamento> {

	public static final String NAME = "classeJudicialAgrupamentoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ClasseJudicialAgrupamento o";
	private static final String DEFAULT_ORDER = "classe";

	/**
	 * Restricao por agrupamento (o.agrupamento)
	 */
	private static final String R1 = "o.agrupamento = #{agrupamentoClasseJudicialHome.instance}";

	@Override
	protected void addSearchFields() {
		addSearchField("agrupamento", SearchCriteria.igual, R1);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		// Map<String, String> map = new HashMap<String, String>();
		// map.put("classe", "classe.classeJudicial");
		// return map;
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}