package br.jus.cnj.fluxo.interfaces;

import br.jus.cnj.fluxo.Validador;

/**
 * Interfaces para TaskVariavel, metodos que validam e movimentam as tarefas
 * @author Pablo
 */
public interface TaskVariavelAction {

	/**
	 * Valida a transicaoSelecionada
	 * @param transicaoSelecionada transicao
	 * @param validador validador
	 */
	public void validar(String transicaoSelecionada, Validador validador);
	
	/**
	 * Movimenta para a transicaoselecionada
	 * @param transicaoSelecionada
	 * @throws Exception
	 */
	public void movimentar(String transicaoSelecionada) throws Exception;
	
}
