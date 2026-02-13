package br.jus.cnj.pje.intercomunicacao.seguranca;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import br.jus.cnj.catalogoservicos.dto.TokenDTO;

/**
 * Adiciona as informações de credenciais do usuário na SOAPHeader
 * @author rodrigo
 *
 */
public class CatalogoServicosClientSOAPHandler extends CatalogoServicosSOAPMessageHandler {

	private CatalogoServicos catalogoServicos;
	
		
	public CatalogoServicosClientSOAPHandler(CatalogoServicos catalogoServicos) {
		this.catalogoServicos = catalogoServicos;
	}
	

	@Override
	protected boolean doHandleMessage(SOAPMessageContext context) {
	
		boolean isRequest = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		if(isRequest){
			SOAPMessage soapMessage = context.getMessage();
			
			try{
				if(!isMNI(context)){
					SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
					SOAPHeader soapHeader = soapEnvelope.getHeader();
					
					if(soapHeader == null){
						soapHeader = soapEnvelope.addHeader();
					}
					
					TokenDTO token = catalogoServicos.requisitarToken();
					
					SOAPHeaderElement usernameHeader = soapHeader.addHeaderElement(soapEnvelope.createName("username", "SOAP-SEC", "http://www.cnj.jus.br/catalogoServicos/security"));
					usernameHeader.addTextNode(token.getUsuario());
					SOAPHeaderElement passwordHeader = soapHeader.addHeaderElement(soapEnvelope.createName("tokenId", "SOAP-SEC", "http://www.cnj.jus.br/catalogoServicos/security"));
					passwordHeader.addTextNode(token.getTokenId());
						
					soapMessage.saveChanges();
				}
				
				
				return true;
			}
			catch(SOAPException e){
				defaultErrorMessage(e);
			}
		}
		
		
		
		return true;
	}

}
