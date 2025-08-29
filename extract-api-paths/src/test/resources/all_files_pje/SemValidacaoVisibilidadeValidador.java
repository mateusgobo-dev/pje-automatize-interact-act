package br.jus.cnj.pje.permissao;

import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Implementa o padrão NullObject para evitar NullPointerException quando nenhum
 * validador for encontrado
 */
class SemValidacaoVisibilidadeValidador implements VisibilidadeValidador {

	@Override
	public boolean isValido(Usuario usuario) {
		return true;
	}

	@Override
	public boolean isPossivelValidar(Usuario usuario) {
		return true;
	}

}
