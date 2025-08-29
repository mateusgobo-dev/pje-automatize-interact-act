package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.cliente.home.UsuarioLocalizacaoMagistradoServidorHome;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;

@Name(OrgaoJulgadorItemsList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class OrgaoJulgadorItemsList extends EntityList<OrgaoJulgador> {

	public static final String NAME = "orgaoJulgadorItemsList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "1";

	@Override
	protected String getDefaultEjbql() {
		OrgaoJulgadorColegiado ojc = Authenticator.getOrgaoJulgadorColegiadoAtual();
		OrgaoJulgador oj = Authenticator.getOrgaoJulgadorAtual();
		OrgaoJulgadorColegiado orgaoColegiado = UsuarioLocalizacaoMagistradoServidorHome.instance().getInstance()
				.getOrgaoJulgadorColegiado();

		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(ojcoj.orgaoJulgador) from OrgaoJulgadorColegiado ojc ");
		sb.append("inner join ojc.orgaoJulgadorColegiadoOrgaoJulgadorList ojcoj ");
		sb.append("where ojcoj.orgaoJulgador.ativo = true ");

		if (Authenticator.isPermissaoCadastroTodosPapeis()) {
			if (orgaoColegiado != null) {
				sb.append("and ojc.idOrgaoJulgadorColegiado = ");
				sb.append(orgaoColegiado.getIdOrgaoJulgadorColegiado());
			} else {
				sb.append("and 1 != 1 ");
			}
		} else {
			if (ojc != null) {
				sb.append("and ojc.idOrgaoJulgadorColegiado = ");
				sb.append(ojc.getIdOrgaoJulgadorColegiado());
			}
			if (oj != null) {
				sb.append(" and ojcoj.orgaoJulgador.idOrgaoJulgador = ");
				sb.append(oj.getIdOrgaoJulgador());
			}
			if (ojc == null && oj == null) {
				sb.append("and 1 != 1 ");
			}
		}

		return sb.toString();
	}

	@Override
	public List<OrgaoJulgador> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
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
