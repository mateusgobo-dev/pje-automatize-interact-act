package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.Pessoa;

@SuppressWarnings("serial")
@Name("pessoaSuggest")
@Scope(ScopeType.SESSION)
@BypassInterceptors
public class PessoaSuggestBean extends AbstractSuggestBean<Pessoa> {
	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Pessoa o ");
		sb.append("where lower(TO_ASCII(o.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by 1");
		return sb.toString();
	}

	@Override
	public String getDefaultValue() {
		return getInstance() != null ? getInstance().toString() : "";
	}
}
