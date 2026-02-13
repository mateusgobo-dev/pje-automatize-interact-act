package br.com.infox.pje.list;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.SearchCriteria;

@Name(VotoAcompanhadoSecretarioList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class VotoAcompanhadoSecretarioList extends SessaoProcessoDocumentoVotosAcompanharList {

	private static final long serialVersionUID = -1800463711835876628L;

	public static final String NAME = "votoAcompanhadoSecretarioList";

	private static final String DEFAULT_ORDER = "spdv.idSessaoProcessoDocumento";
	private static final String R1 = "spdv.sessao = #{winVotoAction.sessaoPautaProcessoComposicao.sessaoPautaProcessoTrf.sessao}";
	private static final String R2 = "spdv.processoTrf.idProcessoTrf = #{winVotoAction.sessaoPautaProcessoComposicao.sessaoPautaProcessoTrf.processoTrf.idProcessoTrf}";
	private static final String R3 = "spdv.ojAcompanhado != #{winVotoAction.sessaoPautaProcessoComposicao.sessaoPautaProcessoTrf.processoTrf.orgaoJulgador} ";
	private static final String R4 = "spdv.ojAcompanhado != #{winVotoAction.sessaoPautaProcessoComposicao.orgaoJulgador} ";
	private static final String R5 = "spdv.orgaoJulgador != #{winVotoAction.sessaoPautaProcessoComposicao.orgaoJulgador} ";

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