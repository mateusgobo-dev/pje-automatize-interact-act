package br.jus.cnj.pje.nucleo.view;

/**
 * Interface para a implementação essencial de um controller que proverá os
 * callbacks básicos para o uso da taglib pje:ckEditor
 * 
 * Para os casos em que o editor irá gerar um processo documento, favor usar a
 * interface ICkEditorGeraDocumentoController
 * 
 * Caso queira uma implementação de referencia para os casos onde se gera
 * documento, extenda da classe abstrata CkEditorGeraDocumentoAbstractAction
 * 
 * @author eduardo.pereira@tse.jus.br
 *
 */

public interface ICkEditorController {
	
	/**
	 * Salva o conteudo do editor.
	 * 
	 * @param conteudo
	 */
	public void salvar(String conteudo);

	/**
	 * Recupera os estilos de formatação para o novo editor de textos baseado no
	 * ckEditor
	 *
	 * @return String contendo o objeto JSON
	 */
	public String getEstilosFormatacao();
	
	/**
	 * Verifica se o formulário externo ao editor teve seus atributos obrigatórios preenchidos.
	 * 
	 * @return boolean
	 */
	public boolean isFormularioPreenchido();

}
