package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;

@Name(OrgaoJulgadorColegiadoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class OrgaoJulgadorColegiadoList extends EntityList<OrgaoJulgadorColegiado> {

	public static final String NAME = "orgaoJulgadorColegiadoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.orgaoJulgadorColegiado";

	/**
	 * Restricao por seleção de um orgaoJulgadorColegiado
	 * (o.orgaoJulgadorColegiadoPai)
	 */
	private static final String R1 = "o.caminhoCompleto like concat("
			+ "#{orgaoJulgadorColegiadoList.entity.orgaoJulgadorColegiadoPai.orgaoJulgadorColegiadoCompleto}, '%')";

	@Override
	protected void addSearchFields() {
		addSearchField("ativo", SearchCriteria.igual);
		addSearchField("orgaoJulgadorColegiado", SearchCriteria.contendo);
		addSearchField("orgaoJulgadorColegiadoPai", SearchCriteria.contendo, R1);
		addSearchField("novoOrgaoJulgadorColegiado", SearchCriteria.igual);
		addSearchField("instancia", SearchCriteria.igual);
		addSearchField("aplicacaoClasse", SearchCriteria.igual);
		addSearchField("dtCriacao", SearchCriteria.igual);
		addSearchField("atoCriacao", SearchCriteria.contendo);
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorColegiado o");

		if(!Authenticator.isServidorExclusivoColegiado()) {
			String idsLocalizacoesFisicas = Authenticator.getIdsLocalizacoesFilhasAtuais();
			sb.append(" JOIN o.localizacao loc ");
			sb.append(" WHERE loc.idLocalizacao IN ("+idsLocalizacoesFisicas+")");
		}else {
			sb.append(" WHERE o.idOrgaoJulgadorColegiado = "+Authenticator.getIdOrgaoJulgadorColegiadoAtual());
		}

		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("o.orgaoJulgadorColegiadoPai", "o.orgaoJulgadorColegiadoPai.orgaoJulgadorColegiado");
		return map;
	}

}