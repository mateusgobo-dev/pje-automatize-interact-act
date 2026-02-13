package br.com.infox.cliente.component.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.Pessoa;

@Name(PessoaRepresentanteSuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PessoaRepresentanteSuggestBean extends AbstractSuggestBean<Pessoa> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "pessoaRepresentanteSuggest";

	@Override
	public String getEjbql() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("select o from Pessoa o ");
		stringBuilder.append("where lower(TO_ASCII(o.nome)) like lower(concat('%',TO_ASCII(:");
		stringBuilder.append(INPUT_PARAMETER);
		stringBuilder.append("), '%')) ");
		Pessoa pessoaRepresentada = PessoaRepresentadaSuggestBean.instance().getInstance();
		if (pessoaRepresentada != null) {
			stringBuilder.append("and o.idUsuario != " + pessoaRepresentada.getIdUsuario() + " ");
		}
		stringBuilder.append("order by o.nome");
		return stringBuilder.toString();
	}

	public static PessoaRepresentanteSuggestBean instance() {
		return (PessoaRepresentanteSuggestBean) Component.getInstance(PessoaRepresentanteSuggestBean.NAME);
	}
}
