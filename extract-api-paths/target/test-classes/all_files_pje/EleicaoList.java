package br.jus.je.pje.list;

import java.io.Serializable;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.je.entidades.Eleicao;

@Name("eleicaoList")
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class EleicaoList extends EntityList<Eleicao> implements Serializable {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5662301261689100590L;
	public static final String DEFAULT_EJBQL = "select o from Eleicao o";
	public static final String DEFAULT_ORDER = "o.ano";
	


	@Override
	protected void addSearchFields() {
		addSearchField("ano", SearchCriteria.igual);
		addSearchField("ativo", SearchCriteria.igual);
	}

	@Override
	protected String getDefaultEjbql()	{
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder()	{
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder()	{
		return null;
	}
	

}
