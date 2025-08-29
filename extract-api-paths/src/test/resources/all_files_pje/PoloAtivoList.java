package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.ProcessoParte;

@Name(PoloAtivoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PoloAtivoList extends EntityList<ProcessoParte> {

	public static final String NAME = "poloAtivoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.pessoa.nome";

	private static final String R1 = "o.processoTrf.idProcessoTrf = #{processoTrfHome.instance.idProcessoTrf}";

	@Override
	protected void addSearchFields() {
		addSearchField("pessoa", SearchCriteria.igual, R1);
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoParte o where o.inParticipacao = 'A' ");
		if (!Authenticator.getPapelAtual().equals(ParametroUtil.instance().getPapelMagistrado())) {
			sb.append("and o.parteSigilosa = false ");
		}
		return sb.toString();
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