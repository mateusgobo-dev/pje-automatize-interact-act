package br.com.infox.pje.list;

/***
 * @author felix
 */

import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.HistoricoMotivoAcessoTerceiro;

@Name(AcessoProcessoTerceirosMotivoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AcessoProcessoTerceirosMotivoList extends EntityList<HistoricoMotivoAcessoTerceiro>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "acessoProcessoTerceirosMotivoList";

	private static final String DEFAULT_EJBQL = "select o from HistoricoMotivoAcessoTerceiro o ";
	private static final String DEFAULT_ORDER = "dtMotivoAcesso";

	private static final String R1 = "o.processoTrf.idProcessoTrf = "
									+ "#{processoTrfHome.instance.idProcessoTrf eq 0 ? "
									+ "listProcessoCompletoBetaAction.processoSelecionado.idProcessoTrf : "
									+ "processoTrfHome.instance.idProcessoTrf}";

	@Override
	protected void addSearchFields(){
		addSearchField("processoTrf", SearchCriteria.igual, R1);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder(){
		return null;
	}

	@Override
	protected String getDefaultEjbql(){
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder(){
		return DEFAULT_ORDER;
	}

}
