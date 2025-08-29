package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;

@Name(ProcessoTrfConexaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoTrfConexaoList extends EntityList<ProcessoTrfConexao> {

	public static final String NAME = "processoTrfConexaoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ProcessoTrfConexao o where "
			+ "o.tipoConexao in('DM','PR', 'DP') and o.ativo = 'true' ";
	private static final String DEFAULT_ORDER = "o.processoTrf";

	private static final String R1 = "o.processoTrf.idProcessoTrf = #{processoTrfHome.instance.idProcessoTrf}";

	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf", SearchCriteria.igual, R1);
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("numeroProcesso", "o.processoTrfConexo");
		map.put("tipoConexao", "o.tipoConexao");
		return map;
	}

}