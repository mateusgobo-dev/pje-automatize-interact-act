package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;

@Name("pessoaServidorSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PessoaServidorSuggestBean extends AbstractSuggestBean<UsuarioLogin> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from UsuarioLogin o ");
		sb.append("where lower(o.nome) like lower(concat ('%', :");
		sb.append(INPUT_PARAMETER);
		sb.append(", '%')) ");
		sb.append("and o.idUsuario in (select serv.idUsuario from PessoaServidor serv) ");
		sb.append("order by o.nome");
		return sb.toString();
	}

}