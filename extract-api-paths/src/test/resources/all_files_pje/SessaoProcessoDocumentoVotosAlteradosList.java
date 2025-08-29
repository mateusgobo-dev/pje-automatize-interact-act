package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;

@Name(SessaoProcessoDocumentoVotosAlteradosList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoProcessoDocumentoVotosAlteradosList extends EntityList<SessaoProcessoDocumentoVoto> {

	public static final String NAME = "sessaoProcessoDocumentoVotosAlteradosList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select spdv from SessaoProcessoDocumentoVoto spdv "
			+ "where spdv.processoDocumento.ativo = true " + "and spdv.liberacao = true ";

	private static final String DEFAULT_ORDER = "spdv";

	private static final String R1 = "spdv.sessao.idSessao = #{sessaoProcessoDocumentoVotoHome.sessaoPauta.sessao.idSessao} ";
	private static final String R2 = "spdv.processoDocumento.processo.idProcesso = #{sessaoProcessoDocumentoVotoHome.sessaoPauta.processoTrf.processo.idProcesso} ";

	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf1", SearchCriteria.igual, R1);
		addSearchField("processoTrf2", SearchCriteria.igual, R2);
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

}
