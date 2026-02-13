package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Usuario;

@Name("usuariosLoteSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class UsuariosLoteSuggestBean extends AbstractSuggestBean<Usuario> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct o.usuario.nome from Lote o join o.usuario.usuarioLocalizacaoList ul ");
		sb.append("where lower(TO_ASCII(o.usuario.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%'))");
		if (!Authenticator.getPapelAtual().toString().equals("admin")
				&& !Authenticator.getPapelAtual().toString().equals("Administrador")) {
			sb.append("and ul.localizacaoFisica.localizacao like '");
			sb.append(Authenticator.getLocalizacaoAtual().getLocalizacao());
			sb.append("' ");
		}
		sb.append("order by 1");
		return sb.toString();
	}

}