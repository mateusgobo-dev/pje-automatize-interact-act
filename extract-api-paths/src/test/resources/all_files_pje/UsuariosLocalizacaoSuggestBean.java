package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Usuario;

@Name("usuariosLocalizacaoSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class UsuariosLocalizacaoSuggestBean extends AbstractSuggestBean<Usuario> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Usuario o inner join o.usuarioLocalizacaoList ul ");
		sb.append("where ul.localizacaoFisica.localizacao like '");
		sb.append(Authenticator.getLocalizacaoAtual().getLocalizacao());
		sb.append("' and ");
		sb.append("lower(TO_ASCII(o.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by 1");
		return sb.toString();
	}

}
