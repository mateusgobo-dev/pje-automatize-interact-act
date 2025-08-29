package br.jus.cnj.pje.permissao;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Verifica a visibilidade de um usuário com perfil de magistrado no sistema.
 */
class MagistradoVisibilidadeValidador implements VisibilidadeValidador {

	@Override
	public boolean isValido(Usuario usuario) {
		boolean primeiroGrau = ParametroUtil.instance().isPrimeiroGrau();
		OrgaoJulgador orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();

		return orgaoJulgador == null && (!primeiroGrau);
	}

	@Override
	public boolean isPossivelValidar(Usuario usuario) {
		return Pessoa.instanceOf(usuario, PessoaMagistrado.class)
				&& Authenticator.isMagistrado();
	}

}
