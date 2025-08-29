package br.jus.cnj.pje.intercomunicacao.seguranca;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.log4j.Logger;

/**
 * Métodos auxiliares para os manipuladores de mensagens jax-ws que utilizam o catalogo de serviços
 * @author rodrigo
 *
 */
public class CatalogoServicosMessageHandler {

	private Logger logger = null;
	
	/**
	 * adiciona entrada soapFault 
	 * @param reason
	 */
	protected void generateSOAPErrMessage(String reason) {
		try {
			SOAPFactory fac = SOAPFactory.newInstance();
			SOAPFault sf = fac.createFault(reason, new QName(
					"http://www.w3.org/2003/05/soap-envelope", "Receiver"));
			throw new SOAPFaultException(sf);
		} catch (SOAPException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected Logger getLogger(){
		if(logger == null){
			logger = Logger.getLogger(this.getClass());
		}
			
		return logger; 
	}
	
	/**
	 * Verifica se o serviço a se consumir é a implementação do Modelo Nacional de Interoperabilidade (MNI)
	 * @param context
	 * @return
	 */
	protected boolean isMNI(MessageContext context){
		return ((QName) context
				.get(MessageContext.WSDL_INTERFACE)).getLocalPart()
				.contains("intercomunicacao");

	}
	
	/**
	 * Mensagem padrão para erros não tratados
	 * @param e
	 */
	protected void defaultErrorMessage(Exception e){
		getLogger().warn("Não foi possivel recuperar as informacões do token",e);
		generateSOAPErrMessage("Erro interno");
	}
}
