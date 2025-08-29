package br.jus.cnj.pje.permissao;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Validador de visibilidade para o usuário com perfil de administrador.
 */
class AdministradorVisibilidadeValidador implements VisibilidadeValidador {

	public static final String ROLE_ADMIN = "admin";
	public static final String ROLE_ADMINISTRADOR = "administrador";

	/**
	 * O Administrador possui visibilidade a qualquer funcionalidade.
	 * Se ele está sendo executado é porque o usuário já possui visibilidade.
	 */
	@Override
	public boolean isValido(Usuario usuario) {
		return true;
	}

	/**
	 * Valida se o usuário possui permissão de administrador no sistema.
	 */
	@Override
	public boolean isPossivelValidar(Usuario usuario) {
		return Authenticator.hasRole(ROLE_ADMIN, ROLE_ADMINISTRADOR, Papeis.ADMINISTRADOR);
	}
}
