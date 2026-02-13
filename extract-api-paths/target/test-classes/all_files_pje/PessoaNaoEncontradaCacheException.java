package br.jus.cnj.pje.nucleo;

import br.com.itx.util.FacesUtil;

public class PessoaNaoEncontradaCacheException extends PJeException{
	private static final long serialVersionUID = 5767802508416168403L;

	public PessoaNaoEncontradaCacheException(Throwable e){
		super(e);
	}

	public PessoaNaoEncontradaCacheException(String codigo, Throwable t, Object... params){
		super(codigo, t, params);
	}

	public PessoaNaoEncontradaCacheException(String code){
		super(code);
	}
	
	/**
	 * Construtor que utiliza o resource bundle para converter a mensagem do arquivo entity_messages.properties.
	 * @param key define a chave a ser utilizada.
	 * @param params define os parâmetros que serão carregados na mensagem do bundle, se existentes.
	 */
	public PessoaNaoEncontradaCacheException(String key, Object... params){
		this(FacesUtil.getMessage(key, params));
	}

}
