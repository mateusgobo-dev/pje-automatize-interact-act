package br.jus.cnj.pje.util.checkup.spi;

/**
 * Representa um erro que ocorreu ao executar um validador (CheckupWorker)
 * @see CheckupWorker
 */
public interface CheckupError {

	/**
	 * @return Identificador do erro. Útil para o usuário inativar falsos-positivos.
	 */
	String getID();
	/**
	 * @return Mensagem descrevendo o erro encontrado.
	 */
	String getErrorMessage();
	/**
	 * @return Mensagem com passos para corrigir erro.
	 */
	String getFixMessage();
	//TODO future
	//QuickFixWorker getQuickFixWorker();
	
}
