package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.NotaSessaoJulgamento;

@Name(NotaSessaoJulgamentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class NotaSessaoJulgamentoList extends EntityList<NotaSessaoJulgamento> {

	public static final String NAME = "notaSessaoJulgamentoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from NotaSessaoJulgamento o";
	private static final String DEFAULT_ORDER = "dataCadastro desc";

	private static final String R1 = "o.sessao.idSessao = #{sessaoHome.instance.idSessao}";
	private static final String R2 = "o.processoTrf.idProcessoTrf = #{sessaoPautaProcessoTrfHome.listaSCO.isEmpty() ? null"
			+ " : sessaoPautaProcessoTrfHome.listaSCO.get(0).processoTrf.idProcessoTrf}";

	@Override
	protected void addSearchFields() {
		addSearchField("sessao", SearchCriteria.igual, R1);
		addSearchField("processoTrf.idProcessoTrf", SearchCriteria.igual, R2);
		addSearchField("orgaoJulgador", SearchCriteria.igual);
		addSearchField("processoTrf", SearchCriteria.igual);
		addSearchField("notaSessaoJulgamento", SearchCriteria.contendo);
		addSearchField("ativo", SearchCriteria.igual);
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