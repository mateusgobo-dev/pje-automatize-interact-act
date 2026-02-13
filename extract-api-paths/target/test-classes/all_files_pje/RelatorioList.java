package br.jus.jt.estatistica.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.jt.entidades.estatistica.Relatorio;

@Name(RelatorioList.NAME)
@BypassInterceptors
@Scope(ScopeType.EVENT)
public class RelatorioList extends EntityList<Relatorio> {

	public static final String NAME = "relatorioList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Relatorio o where o.valido = true";
	private static final String DEFAULT_ORDER = "o.orgaoJulgador.idOrgaoJulgador, o.quadro.ordem";
	private static final String R1 = "o.orgaoJulgador = #{estatisticaEGestaoAction.orgaoJulgador}";
	private static final String R2 = "o.periodo = #{estatisticaEGestaoAction.periodo}";
	private static final String R3 = "o.quadro = #{estatisticaEGestaoAction.quadro}";

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
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

	@Override
	protected void addSearchFields() { 
		addSearchField("orgaoJulgador", SearchCriteria.igual, R1);
		addSearchField("periodo", SearchCriteria.igual, R2);
		addSearchField("quadro", SearchCriteria.igual, R3);
	}

}
