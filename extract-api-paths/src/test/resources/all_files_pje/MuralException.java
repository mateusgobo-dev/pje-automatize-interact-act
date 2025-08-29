package br.jus.cnj.pje.nucleo;

import br.com.itx.util.FacesUtil;


public class MuralException extends PJeException{

	private static final long serialVersionUID = 3076115122403704735L;

	public MuralException(Throwable e){
		super(e);
	}

	public MuralException(String codigo, Throwable t, Object... params){
		super(codigo, t, params);
	}

	public MuralException(String code){
		super(code);
	}
	
	/**
	 * Construtor que utiliza o resource bundle para converter a mensagem do arquivo entity_messages.properties.
	 * @param key define a chave a ser utilizada.
	 * @param params define os parâmetros que serão carregados na mensagem do bundle, se existentes.
	 */
	public MuralException(String key, Object... params){
		this(FacesUtil.getMessage(key, params));
	}

}