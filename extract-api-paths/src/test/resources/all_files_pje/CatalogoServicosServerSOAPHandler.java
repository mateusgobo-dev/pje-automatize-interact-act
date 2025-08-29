package br.jus.cnj.pje.intercomunicacao.seguranca;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

/**
 * Recupera as credenciais do usuario passadas via SOAPHeader e autentica o usuario
 * @author rodrigo
 *
 */
public class CatalogoServicosServerSOAPHandler extends CatalogoServicosSOAPMessageHandler
	{
	@Override
	protected boolean doHandleMessage(SOAPMessageContext context) {

		boolean isRequest = (Boolean) context
				.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		if (!isRequest) {
			SOAPMessage soapMessage = context.getMessage();

			try {
								
				if(isMNI(context)){
					return true;
				}

				SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart()
						.getEnvelope();
				SOAPHeader soapHeader = soapEnvelope.getHeader();
				
				if (soapHeader == null) {
					soapHeader = soapEnvelope.addHeader();
				}

				else if (soapHeader.getElementsByTagName("SOAP-SEC:username").getLength() > 0) {
				
					String username = soapHeader
							.getElementsByTagName("SOAP-SEC:username").item(0)
							.getChildNodes().item(0).getNodeValue();
					String tokenId = soapHeader.getElementsByTagName("SOAP-SEC:tokenId")
							.item(0).getChildNodes().item(0).getNodeValue();

					CatalogoServicos catalogoServicos = new CatalogoServicos(
							tokenId);
					if (catalogoServicos.autenticar(username)) {
						return true;
					}
				}
				
				generateSOAPErrMessage("Acesso negado");

			}

			catch (SOAPFaultException e) {
				throw e;
			} catch (Exception e) {
				defaultErrorMessage(e);
			} 
		}

		return true;
	}
}