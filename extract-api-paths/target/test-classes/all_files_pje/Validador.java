package br.jus.cnj.fluxo;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que valida os frames utilizados no fluxo
 * @author Pablo
 */
public class Validador {

	public List<String> erros = new ArrayList<String>();

	/**
	 * Adiciona um novo erro
	 * @param erro
	 * @return true se adicionou erro
	 */
	public boolean add(String erro) {
		return erros.add(erro);
	}
	
	/**
	 * Retorna os erros da validacao
	 * @return Lista de Erros
	 */
	public List<String> getErros() {
		return erros;
	}

	/**
	 * Verifica se na validacao existem erros
	 * @return True se a lista de erros nao for vazia
	 */
	public boolean getPossuiErros() {
		return erros != null && erros.size() > 0;
	}
	
	/**
	 * Verifica se o Objeto for null, se for nulo adiciona na lista de erro a msg
	 * @param object
	 * @param msg
	 * @return
	 */
	public boolean isNull(Object object, String msg) {
		boolean retorno = false;
		if (object == null) {
			add(msg);
			retorno = true;
		}
		return retorno;
	}
	
	public boolean isTrue(boolean valor, String mensagem) {
		boolean retorno = false;
		if (valor) {
			add(mensagem);
			retorno = true;
		}
		return retorno;
	}

}
