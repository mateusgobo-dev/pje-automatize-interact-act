package br.jus.cnj.pje.util.checkup.spi;

import java.util.List;

/**
 * Representa um validador do PJe.
 */
public interface CheckupWorker {

	/**
	 * @return Título do checkup(resumo). Ex.: Verificador da carga de movimentos processuais.
	 */
	String getTitle();
	/**
	 * @return Descrição do checkup(razão de existir). Ex.: Verifica as tabelas de movimentos e complementos à procura de inconsistências na configuração que possam levar a erros no sistema.
	 */
	String getDescription();
	/**
	 * @return Identificador único que será usado para permitir ao usuário inativar manualmente esse checkup
	 */
	String getID();
	/**
	 * @return Se o checkup deve ser rodado para a instancia do PJe atual. Alguns validadores, por ex., podem só fazer sentido para
	 *  a JT, nesse caso devem retornar false para instâncias do PJE que não forem da JT.
	 */
	Boolean shouldRun();
	/**
	 * Método que rodará a validação em um contexto assíncrono.
	 */
	List<CheckupError> work();
	
}