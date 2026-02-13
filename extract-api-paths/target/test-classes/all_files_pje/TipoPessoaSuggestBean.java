package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.TipoPessoa;

@SuppressWarnings("serial")
@Name("tipoPessoaSuggest")
@Scope(ScopeType.SESSION)
@BypassInterceptors
public class TipoPessoaSuggestBean extends AbstractSuggestBean<TipoPessoa> {
	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from TipoPessoa o ");
		sb.append("where lower(o.tipoPessoa) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by 1");
		return sb.toString();
	}

	@Override
	public String getDefaultValue() {
		return getInstance() != null ? getInstance().toString() : "";
	}
}