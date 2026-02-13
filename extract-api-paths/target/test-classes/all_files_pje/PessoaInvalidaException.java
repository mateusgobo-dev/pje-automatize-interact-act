package br.jus.cnj.pje.nucleo;

import br.com.itx.util.FacesUtil;

public class PessoaInvalidaException extends PJeException {
	private static final long serialVersionUID = 8536312883410065053L;

	public PessoaInvalidaException(Throwable e){
		super(e);
	}

	public PessoaInvalidaException(String codigo, Throwable t, Object... params){
		super(codigo, t, params);
	}

	public PessoaInvalidaException(String code){
		super(code);
	}
	
	/**
	 * Construtor que utiliza o resource bundle para converter a mensagem do arquivo entity_messages.properties.
	 * @param key define a chave a ser utilizada.
	 * @param params define os parâmetros que serão carregados na mensagem do bundle, se existentes.
	 */
	public PessoaInvalidaException(String key, Object... params){
		this(FacesUtil.getMessage(key, params));
	}

}
