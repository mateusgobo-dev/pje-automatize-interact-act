package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.PessoaFisica;

@Name("pessoaFisicaSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PessoaFisicaSuggestBean extends AbstractSuggestBean<PessoaFisica> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaFisica o ");
		sb.append("where o.inTipoPessoa = 'F' and lower(TO_ASCII(o.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by 1");
		return sb.toString();
	}

	@Override
	public String getDefaultValue() {
		return getInstance() != null ? getInstance().toString() : "";
	}

}
