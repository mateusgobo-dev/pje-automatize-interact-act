package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.Competencia;

@Name("competenciaSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class CompetenciaSuggestBean extends AbstractSuggestBean<Competencia> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Competencia o ");
		sb.append("where lower(o.competencia) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%'))	order by o.competencia");
		return sb.toString();
	}

}
