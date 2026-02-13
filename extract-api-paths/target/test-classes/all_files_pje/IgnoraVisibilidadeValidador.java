package br.jus.cnj.pje.permissao;

import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Regra implementada para o caso de que deve-se ignorar a validação de
 * visibilidade de forma genérica, onde o usuário não tem o perfil de
 * administrador.
 */
public class IgnoraVisibilidadeValidador implements VisibilidadeValidador {

	/**
	 * Implementação de regra de origem {@link ControleFiltros#iniciarFiltro()} para ignorar visibilidade.
	 */
	@Override
	public boolean isValido(Usuario usuario) {
		boolean primeiroGrau = ParametroUtil.instance().isPrimeiroGrau();
		OrgaoJulgador orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
		OrgaoJulgadorColegiado orgaoJulgadorColegiado = Authenticator.getOrgaoJulgadorColegiadoAtual();

		return (!primeiroGrau) && orgaoJulgador == null && orgaoJulgadorColegiado == null;
	}

	@Override
	public boolean isPossivelValidar(Usuario usuario) {
		return !Authenticator.hasRole(AdministradorVisibilidadeValidador.ROLE_ADMIN,
				AdministradorVisibilidadeValidador.ROLE_ADMINISTRADOR, Papeis.ADMINISTRADOR);
	}

}
