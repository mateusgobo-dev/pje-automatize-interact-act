package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.DAO.SearchField;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;

@Name(SessaoProcessoDocumentoVotoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoProcessoDocumentoVotoList extends EntityList<SessaoProcessoDocumentoVoto> {

	public static final String NAME = "sessaoProcessoDocumentoVotoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select spdv from SessaoProcessoDocumentoVoto spdv";

	private static final String DEFAULT_ORDER = "idSessaoProcessoDocumento";

	private static final String R1 = "spdv.sessao = #{sessaoJulgamentoAction.sessao}";

	private static final String R2 = "spdv.orgaoJulgador != #{authenticator.getOrgaoJulgadorAtual()}";

	private static final String R3 = "spdv.processoTrf.idProcessoTrf = #{sessaoJulgamentoAction.processoTrf.idProcessoTrf}";

	private static final String R4 = "spdv.orgaoJulgador != #{sessaoJulgamentoAction.processoTrf.orgaoJulgador}";

	@Override
	public void addSearchFields() {
		addSearchField("sessao", SearchCriteria.igual, R1);
		addSearchField("ojAcompanhado", SearchCriteria.diferente, R2);
		addSearchField("processoDocumento.processo", SearchCriteria.igual, R3);
		addSearchField("processoDocumento.processo1", SearchCriteria.igual, R4);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
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
	public Map<String, SearchField> getSearchFieldMap() {
		return super.getSearchFieldMap();
	}

	@Override
	public void setSearchFieldMap(Map<String, SearchField> searchFieldMap) {
		super.setSearchFieldMap(searchFieldMap);
	}

}