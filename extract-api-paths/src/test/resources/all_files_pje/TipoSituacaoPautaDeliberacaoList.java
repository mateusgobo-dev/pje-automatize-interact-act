package br.com.jt.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.jt.entidades.TipoSituacaoPauta;

@Name(TipoSituacaoPautaDeliberacaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class TipoSituacaoPautaDeliberacaoList extends EntityList<TipoSituacaoPauta> {

	public static final String NAME = "tipoSituacaoPautaDeliberacaoList";
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = "select o from TipoSituacaoPauta o where " +
												"o.classificacao = 'D'";
	private static final String DEFAULT_ORDER = "tipoSituacaoPauta";
	
	@Override
	protected void addSearchFields() {
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}