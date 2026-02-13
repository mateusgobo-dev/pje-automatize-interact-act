/**
 * pje-business Copyright (C) 2011 Conselho Nacional de Justiça (União Federal - República Federativa do Brasil)
 * 
 * Este programa é software livre: você pode redistribuí-lo ou modificá-lo nos termos da Licença GNU Affero General Public como publicada pela Free
 * Software Foundation, quer em sua versão 3, quer em versão pos- terior.
 * 
 * Este programa é distribuído na esperança de que ele será útil, mas SEM QUALQUER GARANTIA; especialmente a de que ele tem algum VALOR COMERCIAL ou
 * APTIDÃO PARA UM OBJETIVO ESPECÍFICO. Leia a licença GNU Affero General Public para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da licença GNU Affero General Public com este programa. Se não, acesse em <http://www.gnu.org/licenses/>.
 * 
 */
package br.jus.cnj.pje.nucleo;

import br.com.itx.util.FacesUtil;

/**
 * Exceção que deve ser lançada quando for identificado um erro de NEGÓCIO do sistema. Esta exceção NÃO FORÇA UM ROLLBACK na transação corrente.
 * 
 * @author Rodrigo Alves Reis / CNJ
 * @author Daniel Castro Machado Miranda / CNJ
 */
public class PJeBusinessException extends PJeException{

	private static final long serialVersionUID = 775052658983474374L;

	public PJeBusinessException(Throwable e){
		super(e);
	}

	public PJeBusinessException(String codigo, Throwable t, Object... params){
		super(codigo, t, params);
	}

	public PJeBusinessException(String code){
		super(code);
	}
	
	/**
	 * Construtor que utiliza o resource bundle para converter a mensagem do arquivo entity_messages.properties.
	 * @param key define a chave a ser utilizada.
	 * @param params define os parâmetros que serão carregados na mensagem do bundle, se existentes.
	 */
	public PJeBusinessException(String key, Object... params){
		this(FacesUtil.getMessage(key, params));
	}

}
