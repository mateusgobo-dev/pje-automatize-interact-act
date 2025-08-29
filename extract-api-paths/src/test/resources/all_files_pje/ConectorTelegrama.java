/**
 * 
 */
package br.jus.cnj.pje.extensao;

import br.jus.cnj.pje.extensao.auxiliar.AvisoRecebimentoECT;
import br.jus.cnj.pje.extensao.auxiliar.DestinatarioECT;
import br.jus.cnj.pje.extensao.auxiliar.RemetenteECT;

/**
 * Interface destinada a permitir a criação de um ponto de extensão para o PJe, especificamente
 * para viabilizar o envio de comunicações por Telegrama via aplicativo dos correios SPE Escritório..
 * 
 * A classe que implementar essa interface deverá ser um componente Seam cujo nome deve ser "conectorTelegrama".
 * 
 *  Esta interface baseia-se na interface correspondente do ConectorECT
 * 
 * @author luteixei
 *
 */
public interface ConectorTelegrama {
	
	/**
	 * Encaminha uma comunicaï¿½ï¿½o para entrega via telegrama pelo SPE Escritï¿½rio.
	 * 
	 * @param idProcesso o ID do processo no qual foi produzido o ato
	 * de comunicação, se existente.
	 * @param idProcessoParteExpediente 
	 * @param numeroProcesso o número do processo no qual foi produzido o ato
	 * de comunicação, se existente.
	 * @param remetente o {@link RemetenteECT} da comunicação
	 * @param destinatario o {@link DestinatarioECT} da comunicação
	 * @param comunicacao o conteúdo da comunicação a ser enviada
	 * @param auxiliar array de strings com informações auxiliares
	 * @return identificador da correspondência para futuro acompanhamento
	 * @throws PontoExtensaoException caso haja algum erro durante a chamada
	 */
	public String enviaTelegrama(int idProcesso, int idProcessoParteExpediente, String nuemroProcesso, RemetenteECT remetente, DestinatarioECT destinatario, byte[] comunicacao, String[] auxiliar) throws PontoExtensaoException;

	/**
	 * Encaminha uma comunicaï¿½ï¿½o para entrega via correios.
	 * 
	 * @param idProcesso o ID do processo no qual foi produzido o ato
	 * de comunicação, se existente.
	 * @param idProcessoParteExpediente 
	 * @param numeroProcesso o número do processo no qual foi produzido o ato
	 * de comunicação, se existente.
	 * @param remetente o {@link RemetenteECT} da comunicação
	 * @param destinatario o {@link DestinatarioECT} da comunicação
	 * @param comunicacao o conteúdo da comunicação a ser enviada
	 * @param auxiliar array de strings com informações auxiliares
	 * @return identificador da correspondência para futuro acompanhamento
	 * @throws PontoExtensaoException caso haja algum erro durante a chamada
	 */
	public String enviaTelegrama(int idProcesso, int idProcessoParteExpediente, String numeroProcesso, RemetenteECT remetente, DestinatarioECT destinatario, String comunicacao, String[] auxiliar) throws PontoExtensaoException;

	/**
	 * Verifica a entrega de uma determinada comunicação.
	 * 
	 * @param codigoVerificacao código fornecido quando da solicitação de comunicação
	 * @return o {@link AvisoRecebimentoECT}, quando tiver havido a entrega ou concluídas as tentativas para tanto ou
	 * null, se ainda não há resultado.
	 * @throws PontoExtensaoException caso haja algum erro durante a chamada
	 */
	public AvisoRecebimentoECT verificaEntrega(String codigoVerificacao) throws PontoExtensaoException;
	
	/**
	 * Permite cancelar o envio de uma correspondencia
	 * 
	 * @param codigoVerificacao
	 * @return
	 * @throws PontoExtensaoException
	 */
	public String cancelaTelegrama(String codigoVerificacao) throws PontoExtensaoException;
	
}
