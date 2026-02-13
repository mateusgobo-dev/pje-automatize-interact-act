package br.com.infox.DAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.Pesquisa;

@Name(PesquisaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PesquisaList extends EntityList<Pesquisa>{

	public static final String NAME = "pesquisaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Pesquisa o ";

	private static final String DEFAULT_ORDER = "nome asc";

	@Override
	protected String getDefaultEjbql(){
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder(){
		return DEFAULT_ORDER;
	}

	@Override
	protected void addSearchFields(){
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("nome", "o.nome");
		map.put("descricao", "o.descricao");
		map.put("colunaOrdenacao", "o.colunaOrdenacao");
		map.put("operadorLogico", "o.operadorLogico");
		return map;
	}

	public List<Pesquisa> listSavedSearch(String entityList){
		getEntity().setEntityList(entityList);
		addSearchField("entityList", SearchCriteria.igual);
		return super.list(10);
	}

}