package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;

@Name("advogadoSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class AdvogadoSuggestBean extends AbstractSuggestBean<PessoaAdvogado> {	
    private static final long serialVersionUID = 4376637547740125114L;

    @Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaAdvogado o ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND LOWER(o.nome) LIKE LOWER(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) ");
		sb.append(" ORDER BY o.nome");
		return sb.toString();
	}

}