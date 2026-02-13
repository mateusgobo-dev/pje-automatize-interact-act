package br.jus.cnj.pje.intercomunicacao.v222.seguranca;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import br.jus.cnj.intercomunicacao.v222.beans.ObjectFactory;
import br.jus.cnj.pje.intercomunicacao.seguranca.CatalogoServicos;
import br.jus.cnj.pje.intercomunicacao.seguranca.CatalogoServicosLogicalMessageHandler;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIUtil;

/**
 * Recupera as credenciais do usuário passadas como parâmetro (de acordo com definição no MNI) e autentica o usuário
 * @author rodrigo
 *
 */
public class CatalogoServicosMNIServerLogicalHandler extends CatalogoServicosLogicalMessageHandler{

	@Override
	protected boolean doHandleMessage(LogicalMessageContext context) {
		boolean isRequest = (Boolean) context
				.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		if (!isRequest) {
			try {
				LogicalMessage logicalMessage = context.getMessage();
			

				if (isMNI(context)) {
					Object jaxbObject = logicalMessage.getPayload(JAXBContext
							.newInstance(ObjectFactory.class));
					String username = MNIUtil.obterLogin(jaxbObject);
					String idToken = MNIUtil.obterSenha(jaxbObject);
					
					CatalogoServicos catalogoServicos = new CatalogoServicos(idToken);
					if(catalogoServicos.autenticar(username)){
						return true;
					}
					else{
						generateSOAPErrMessage("Acesso Negado");
					}
				}
			} catch (SOAPFaultException e) {
				throw e;
			} catch (Exception e) {
				defaultErrorMessage(e);
			} 

		}
		return true;
	}
	
	

}
