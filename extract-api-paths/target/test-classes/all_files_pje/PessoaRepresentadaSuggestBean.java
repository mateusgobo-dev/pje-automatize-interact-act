package br.com.infox.cliente.component.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.Pessoa;

@Name(PessoaRepresentadaSuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PessoaRepresentadaSuggestBean extends AbstractSuggestBean<Pessoa> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "pessoaRepresentadaSuggest";

	@Override
	public String getEjbql() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("select o from Pessoa o ");
		stringBuilder.append("where lower(o.nome) like lower(concat('%',TO_ASCII(:");
		stringBuilder.append(INPUT_PARAMETER);
		stringBuilder.append("), '%')) ");
		Pessoa pessoaRepresentante = PessoaRepresentanteSuggestBean.instance().getInstance();
		if (pessoaRepresentante != null) {
			stringBuilder.append("and o.idUsuario != " + pessoaRepresentante.getIdUsuario() + " ");
		}
		stringBuilder.append("order by o.nome");
		return stringBuilder.toString();
	}

	public static PessoaRepresentadaSuggestBean instance() {
		return (PessoaRepresentadaSuggestBean) Component.getInstance(PessoaRepresentadaSuggestBean.NAME);
	}
}
