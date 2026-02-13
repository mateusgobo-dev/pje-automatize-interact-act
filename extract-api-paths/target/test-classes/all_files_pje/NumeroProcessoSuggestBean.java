package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("numeroProcessoSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class NumeroProcessoSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o where o.processoStatus = 'D'");
		sb.append("and lower(TO_ASCII(o.processo.numeroProcesso)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by o.processo.numeroProcesso");
		return sb.toString();
	}

}
