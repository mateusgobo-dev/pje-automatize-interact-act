package br.com.infox.pje.list;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.SearchCriteria;

@Name(VotoAcompanhadoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class VotoAcompanhadoList extends SessaoProcessoDocumentoVotosAcompanharList {
	public static final String NAME = "votoAcompanhadoList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "spdv.idSessaoProcessoDocumento";
	private static final String R1 = "spdv.sessao = #{votarEmLoteAction.sessao == null ? sessaoJulgamentoAction.sessao : votarEmLoteAction.sessao}";
	private static final String R2 = "spdv.processoTrf.idProcessoTrf = #{sessaoJulgamentoAction.processoTrf.idProcessoTrf}";
	private static final String R3 = "spdv.ojAcompanhado != #{sessaoJulgamentoAction.processoTrf.orgaoJulgador} ";

	@Override
	protected void addSearchFields() {
		addSearchField("sessao", SearchCriteria.igual, R1);
		addSearchField("processoDocumento.processo", SearchCriteria.igual, R2);
		addSearchField("ojAcompanhado", SearchCriteria.igual, R3);
		addSearchField("ojUsuarioLogado", SearchCriteria.diferente, R4);
		addSearchField("orgaoJulgador", SearchCriteria.diferente, R5);
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}