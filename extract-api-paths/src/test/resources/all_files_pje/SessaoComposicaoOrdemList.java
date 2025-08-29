package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;

/**
 * SessaoComposicaoOrdem
 */

@Name(SessaoComposicaoOrdemList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoComposicaoOrdemList extends EntityList<SessaoComposicaoOrdem> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "sessaoComposicaoOrdemList";

	private static final String DEFAULT_EJBQL = "select o from SessaoComposicaoOrdem o "
			+ "where o.sessao = #{sessaoHome.instance}";
	private static final String DEFAULT_ORDER = "idSessaoComposicaoOrdem";

	@Override
	protected void addSearchFields() {
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("idGabinete", "o.orgaoJulgador");
		map.put("idGabineteRevisor", "o.orgaoJulgadorRevisor");
		return map;
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