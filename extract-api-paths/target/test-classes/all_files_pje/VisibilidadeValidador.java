package br.jus.cnj.pje.permissao;

import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Inteface que adiciona o comportamento de validação da visibilidade do usuário à uma classe.
 *
 */
public interface VisibilidadeValidador {

	/**
	 * Verifica se o usuário possui visibilidade.
	 */
	boolean isValido(Usuario usuario);

	/**
	 * Verifica se o validador pode executar a validação para o usuário.
	 */
	boolean isPossivelValidar(Usuario usuario);

}
