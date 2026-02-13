package br.jus.cnj.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@Name(OrgaoJulgadorList.NAME)
@BypassInterceptors
@Scope(ScopeType.EVENT)
public class OrgaoJulgadorList extends EntityList<OrgaoJulgador> {

	public static final String NAME = "orgaoJulgadorList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.orgaoJulgadorOrdemAlfabetica";

	@Override
	protected void addSearchFields() {
		addSearchField("sigla", SearchCriteria.igual);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgador o");

		Localizacao localizacaoFisicaAtual = Authenticator.getLocalizacaoFisicaAtual();
		if(Authenticator.getOrgaoJulgadorAtual() == null && localizacaoFisicaAtual != null && localizacaoFisicaAtual.getFaixaInferior() != null) {
			sb.append(" JOIN o.localizacao loc ");
			sb.append(" WHERE (loc.faixaInferior IS NULL OR loc.faixaInferior >= "+localizacaoFisicaAtual.getFaixaInferior() + ") ");
			sb.append(" AND (loc.faixaSuperior IS NULL OR loc.faixaSuperior <= "+localizacaoFisicaAtual.getFaixaSuperior() + ") ");
		}else {
			sb.append(" WHERE o.idOrgaoJulgador = "+Authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador());
		}
		sb.append(" AND o.ativo = true ");

		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}
