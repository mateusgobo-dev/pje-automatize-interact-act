package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;

@Name(SessaoComposicaoOrdemSecretarioSessaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class SessaoComposicaoOrdemSecretarioSessaoList extends EntityList<SessaoComposicaoOrdem> {

	public static final String NAME = "sessaoComposicaoOrdemSecretarioSessaoList";
	private static final long serialVersionUID = 1L;
	private static String DEFAULT_EJBQL = "select o from SessaoComposicaoOrdem o where o.sessao = #{sessaoHome.instance}  ";
	private static final String DEFAULT_ORDER = "o.orgaoJulgador.orgaoJulgador";

	@Override
	protected void addSearchFields() {
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
		return null;
	}
}