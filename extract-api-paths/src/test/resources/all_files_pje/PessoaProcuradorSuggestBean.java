

package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.PessoaProcurador;

@Name("pessoaProcuradorSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PessoaProcuradorSuggestBean extends AbstractSuggestBean<PessoaProcurador> {

	private static final long serialVersionUID = 1L;
	
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaProcurador o ");
		sb.append("where lower(TO_ASCII(o.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append( INPUT_PARAMETER + "), '%'))");
		return sb.toString();
	}

}
