package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.Jurisdicao;

@Name("jurisdicaoSuggest")
@BypassInterceptors
public class JurisdicaoSuggestBean extends AbstractSuggestBean<Jurisdicao> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Jurisdicao o where ");
		sb.append("lower(TO_ASCII(o.jurisdicao)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) and ativo = true order by o.jurisdicao");
		return sb.toString();
	}

}
