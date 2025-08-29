package br.com.jt.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.jt.entidades.ComposicaoSessao;

@Name(ComposicaoSessaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ComposicaoSessaoList extends EntityList<ComposicaoSessao> {

	public static final String NAME = "composicaoSessaoList";
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = "select o from ComposicaoSessao o ";
	private static final String DEFAULT_ORDER = "orgaoJulgador";
	
	private static final String R1 = "o.sessao = #{pautaJulgamentoAction.sessao}";
	private static final String R2 = "o.sessao = #{secretarioSessaoJulgamentoAction.sessao}";
	
	protected void addSearchFields() {
		addSearchField("sessao", SearchCriteria.igual, R1);
		addSearchField("sessao.idSessao", SearchCriteria.igual, R2);
	}

	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("orgaoJulgador", "orgaoJulgador.orgaoJulgador");
		return map;
	}

	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}