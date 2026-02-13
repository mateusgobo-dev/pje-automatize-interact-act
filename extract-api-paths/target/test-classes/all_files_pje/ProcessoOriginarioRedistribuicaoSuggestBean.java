package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("processoOriginarioRedistribuicaoSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ProcessoOriginarioRedistribuicaoSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		HibernateUtil.disableAllFilters();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o");
		sb.append(" where o.orgaoJulgador.idOrgaoJulgador != "
				+ ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getIdOrgaoJulgador());
		sb.append(" and lower(TO_ASCII(o.processo.numeroProcesso)) like lower(concat('%',TO_ASCII(:" + INPUT_PARAMETER
				+ "), '%'))");
		sb.append("	order by o.processo.numeroProcesso");
		return sb.toString();
	}
}
