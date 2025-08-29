package br.com.infox.cliente.exception;

/**
 * 
 * Esta exceção é utilizada quando alguém tentar inativar um alerta, não respeitando a regra de negócio.
 * Só poderá ser desativado alerta em que não há processo cadastrado (relação alerta e processoAlerta no estado ativo). 
 * 
 * @link http://www.cnj.jus.br/jira/browse/PJEII-18551
 * @author orlando.resende
 *
 */
public class AlertaInativacaoInvalidaException extends Exception{
	private static final long serialVersionUID = 794108342592436124L;

	public AlertaInativacaoInvalidaException() {
		super("Não é permitido desativar este alerta, pois há processos de seu ou de outros órgãos cadastrados.");
	}
}