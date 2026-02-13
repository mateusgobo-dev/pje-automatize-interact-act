package br.com.infox.editor.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.editor.Cabecalho;

@Name(CabecalhoList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class CabecalhoList extends EntityList<Cabecalho> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "cabecalhoList";
	
	private static final String DEFAULT_EJBQL = "select o from Cabecalho o where not exists (select o1 from AdvogadoLocalizacaoCabecalho o1 where o1.cabecalho.idCabecalho = o.cabecalho.idCabecalho)";
	private static final String DEFAULT_ORDER = "o.cabecalho";
	
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected void addSearchFields() {
		addSearchField("cabecalho", SearchCriteria.contendo);
		addSearchField("ativo", SearchCriteria.igual);
	}

	@SuppressWarnings("unchecked")
	@Factory(value="cabecalhoItems", scope = ScopeType.CONVERSATION)
	public List<Cabecalho> items() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Cabecalho o ");
		sb.append("where not exists (select o1 from AdvogadoLocalizacaoCabecalho o1 where o1.cabecalho.idCabecalho = o.cabecalho.idCabecalho) ");
		sb.append("and o.ativo = true ");
		return EntityUtil.createQuery(sb.toString()).getResultList();
	}
}
