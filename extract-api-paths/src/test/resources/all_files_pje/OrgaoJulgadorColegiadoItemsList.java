package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;

@Name(OrgaoJulgadorColegiadoItemsList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class OrgaoJulgadorColegiadoItemsList extends EntityList<OrgaoJulgadorColegiado> {

	public static final String NAME = "orgaoJulgadorColegiadoItemsList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.orgaoJulgadorColegiado";

	@Override
	protected String getDefaultEjbql() {
		OrgaoJulgadorColegiado ojc = Authenticator.getOrgaoJulgadorColegiadoAtual();

		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorColegiado o ");
		sb.append("where o.ativo = true ");

		if (Authenticator.isPermissaoCadastroTodosPapeis()) {
			return sb.toString();
		} else {
			if (ojc != null) {
				sb.append("and o.idOrgaoJulgadorColegiado = ");
				sb.append(ojc.getIdOrgaoJulgadorColegiado());
			} else {
				sb.append("and 1 = 1 ");
			}
		}

		return sb.toString();
	}

	@Override
	protected void addSearchFields() {
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}
