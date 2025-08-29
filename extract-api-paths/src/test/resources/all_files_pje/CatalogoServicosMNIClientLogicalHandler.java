package br.jus.cnj.pje.intercomunicacao.v222.seguranca;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import br.jus.cnj.catalogoservicos.dto.TokenDTO;
import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ObjectFactory;
import br.jus.cnj.pje.intercomunicacao.seguranca.CatalogoServicos;
import br.jus.cnj.pje.intercomunicacao.seguranca.CatalogoServicosLogicalMessageHandler;

/**
 * Adiciona as credenciais do usuário (definidos no MNI) na mensagem
 * @author rodrigo
 *
 */
public class CatalogoServicosMNIClientLogicalHandler extends CatalogoServicosLogicalMessageHandler {

	private CatalogoServicos catalogoServicos = null;
	
	public CatalogoServicosMNIClientLogicalHandler(CatalogoServicos catalogoServicos) {
		this.catalogoServicos = catalogoServicos;
	}

	@Override
	public boolean doHandleMessage(LogicalMessageContext context) {
		boolean isRequest = (Boolean) context
				.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		if (isRequest) {
			try {
				LogicalMessage logicalMessage = context.getMessage();
			
				if (isMNI(context)) {
					JAXBContext jaxbContext = JAXBContext
							.newInstance(ObjectFactory.class);
					
					Object jaxbObject = logicalMessage.getPayload(jaxbContext);
					
					TokenDTO token = catalogoServicos.requisitarToken();
					
					String username = token.getUsuario();
					String idToken = token.getTokenId();
					if(jaxbObject instanceof ManifestacaoProcessual){
						((ManifestacaoProcessual) jaxbObject).setIdManifestante(username);
						((ManifestacaoProcessual) jaxbObject).setSenhaManifestante(idToken);
					}
					else{
						jaxbObject.getClass().getMethod("setIdConsultante", String.class).invoke(jaxbObject, username);
						jaxbObject.getClass().getMethod("setSenhaConsultante", String.class).invoke(jaxbObject, idToken);
					}
					
					logicalMessage.setPayload(jaxbObject, jaxbContext);
					
				}
			} catch (Exception e) {
				defaultErrorMessage(e);
			}

		}
		return true;
	}
	
	

}
