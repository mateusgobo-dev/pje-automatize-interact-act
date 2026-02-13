package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;

@Name("usuarioLoginSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class UsuarioLoginSuggestBean extends AbstractSuggestBean<UsuarioLogin> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from UsuarioLogin o ");
		sb.append("where lower(o.nome) like lower(concat ('%', :");
		sb.append(INPUT_PARAMETER);
		sb.append(", '%')) ");
		sb.append("and(o.idUsuario in (select adv.idUsuario from PessoaAdvogado adv) ");
		sb.append("or o.idUsuario in (select asadv.idUsuario from PessoaAssistenteAdvogado asadv) ");
		sb.append("or o.idUsuario in (select asproc.idUsuario from PessoaAssistenteProcuradoria asproc) ");
		sb.append("or o.idUsuario in (select mag.idUsuario from PessoaMagistrado mag) ");
		sb.append("or o.idUsuario in (select ofj.idUsuario from PessoaOficialJustica ofj) ");
		sb.append("or o.idUsuario in (select per.idUsuario from PessoaPerito per) ");
		sb.append("or o.idUsuario in (select proc.idUsuario from PessoaProcurador proc) ");
		sb.append("or o.idUsuario in (select serv.idUsuario from PessoaServidor serv)) ");
		sb.append("order by o.nome");
		return sb.toString();
	}

}