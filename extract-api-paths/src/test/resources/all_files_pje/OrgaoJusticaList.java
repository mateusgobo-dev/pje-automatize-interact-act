package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.lancadormovimento.OrgaoJustica;

@Name(OrgaoJusticaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class OrgaoJusticaList extends EntityList<OrgaoJustica>{

	public static final String NAME = "orgaoJusticaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from OrgaoJustica o";
	private static final String DEFAULT_ORDER = "nome";

	/**
	 * Restricao por seleção de um orgao de justica (o.nome)
	 */
	private static final String R1 = "lower(o.nome) like lower(" +
		"'%' || #{orgaoJusticaList.entity.nome} || '%')";
	private static final String R_ATIVO = "o.ativo = #{true}";

	protected void addSearchFields(){
		addSearchField("nome", SearchCriteria.contendo, R1);
		addSearchField("ativo", SearchCriteria.igual);
	}

	protected Map<String, String> getCustomColumnsOrder(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("nome", "nome");
		return map;
	}

	protected String getDefaultEjbql(){
		return DEFAULT_EJBQL;
	}

	protected String getDefaultOrder(){
		return DEFAULT_ORDER;
	}

	public List<OrgaoJustica> listAtivos(){
		addSearchField("ativo", SearchCriteria.contendo, R_ATIVO);
		return super.list();
	}

}