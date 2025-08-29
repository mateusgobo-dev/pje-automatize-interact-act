/**
 * IntercomunicacaoTest.java
 * 
 * Data: 28/10/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.servico;

import java.net.URL;
import java.util.List;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaAvisosPendentes;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultarTeorComunicacao;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaAvisosPendentes;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultarTeorComunicacao;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.servico.Intercomunicacao;
import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;
import br.jus.cnj.pje.intercomunicacao.util.LogMessageSOAPHandler;

/**
 * Classe de teste da interface do MNI Soap (Intercomunicacao).
 * 
 * @author adriano.pamplona
 */
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
@SuppressWarnings("all")
public class IntercomunicacaoSoapTest extends IntercomunicacaoTest {
	protected static final String URL_WSDL = "http://localhost:8080/pje/intercomunicacao?wsdl";
	private static Intercomunicacao endpoint = null;
	private static final Boolean HABILITAR_MTOM = Boolean.TRUE;
	private static final Boolean HABILITAR_EXIBIR_SOAP = Boolean.FALSE;
	
	@Test
	public void test1EntregarManifestacaoProcessual() {
		super.test1EntregarManifestacaoProcessual();
	}
	
	@Test
	public void test6ConsultarProcessoEspecifico() {
		super.test6ConsultarProcessoEspecifico();
	}
	
	@Override
	protected RespostaManifestacaoProcessual mniEntregarManifestacaoProcessual(ManifestacaoProcessual manifestacao) {
		return obterIntercomunicacao().entregarManifestacaoProcessual(manifestacao);
	}
	
	@Override
	protected RespostaConsultaProcesso mniConsultarProcesso(RequisicaoConsultaProcesso parametro) {
		return obterIntercomunicacao().consultarProcesso(parametro);
	}
	
	@Override
	protected RespostaConsultaAvisosPendentes mniConsultarAvisosPendentes(RequisicaoConsultaAvisosPendentes parametro) {
		return obterIntercomunicacao().consultarAvisosPendentes(parametro);
	}
	
	@Override
	protected RespostaConsultarTeorComunicacao mniConsultarTeorComunicacao(RequisicaoConsultarTeorComunicacao parametro) {
		return obterIntercomunicacao().consultarTeorComunicacao(parametro);
	}
	
	/**
	 * @return Serviço do MNI Intercomunicacao.
	 * @throws RuntimeException
	 */
	@SuppressWarnings("rawtypes")
	protected static Intercomunicacao obterIntercomunicacao() {
		String wsdl = null;
		
		if (endpoint == null) {
			try {
				URL url = new URL(URL_WSDL);
				WebService annotation = IntercomunicacaoSoapImpl.class.getAnnotation(WebService.class);
				QName qname = new QName(
						annotation.targetNamespace(),
						"IntercomunicacaoService");
	
				Service service = Service.create(url, qname);
	
				endpoint = service.getPort(Intercomunicacao.class);
				BindingProvider bp = (BindingProvider) endpoint;
				SOAPBinding binding = (SOAPBinding) bp.getBinding();
				binding.setMTOMEnabled(HABILITAR_MTOM);
				if (HABILITAR_EXIBIR_SOAP) {
					List<Handler> handlers = binding.getHandlerChain();
					handlers.add(new LogMessageSOAPHandler());
					binding.setHandlerChain(handlers);
				}
				bp.getRequestContext().put("javax.xml.ws.client.connectionTimeout", "60000");
				bp.getRequestContext().put("javax.xml.ws.client.receiveTimeout", "600000");
				bp.getRequestContext().put(
						BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
						url.toString().replace("?wsdl", ""));
			} catch (Exception e) {
				String mensagem = e.getMessage();
				mensagem = String.format(
						"Erro ao criar instância do serviço do MNI, erro: %s",
						mensagem);
				throw new IntercomunicacaoException(mensagem);
			}
		}
		return endpoint;
	}
}