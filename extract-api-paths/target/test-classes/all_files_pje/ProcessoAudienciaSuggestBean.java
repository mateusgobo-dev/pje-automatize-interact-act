package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;

@Name("processoAudienciaSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ProcessoAudienciaSuggestBean extends AbstractSuggestBean<String> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.processoTrf.processo.numeroProcesso from ProcessoAudiencia o ");
		sb.append("where lower(TO_ASCII(o.processoTrf.processo.numeroProcesso)) like ");
		sb.append("lower(concat('%', TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) ");
		sb.append("and o.inStatus = true ");
		sb.append("order by o.processoTrf.processo.numeroProcesso");
		return sb.toString();
	}

}
