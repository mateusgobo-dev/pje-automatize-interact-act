/**
 * ConsultarCNPJTest.java
 * 
 * Data: 21/05/2019
 */
package br.jus.cnj.pje.webservice.client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;
import br.jus.cnj.pje.webservice.client.consultacnpj.ArrayOfCNPJPerfil3;
import br.jus.cnj.pje.webservice.client.consultacnpj.ConsultarCNPJ;
import br.jus.cnj.pje.webservice.client.consultacnpj.ConsultarCNPJSoap;

/**
 * Classe de teste responsável pelo teste do webservice da Receita Federal.
 * 
 * @author Adriano Pamplona
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConsultarCNPJTest {

	private static final String URL_WSDL = "https://www.cnj.jus.br/proxyReceitaFederalCNJ/wsdl/proxyReceitaCNPJ_p.wsdl";
	private static final String CPF_CONSULENTE = "82749655153";
	private static ConsultarCNPJSoap consultarCNPJSoap;

	static {
		HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> hostname.equals("localhost"));
	}

	@Test
	public void testConsultarCNPJ() {
		ConsultarCNPJSoap endpoint = getConsultarCNPJSoap();
		ArrayOfCNPJPerfil3 resultado = endpoint.consultarCNPJP3("03472246000154", CPF_CONSULENTE);
		Assert.assertNotNull(resultado);
	}

	/**
	 * @return ConsultarCNPJSoap
	 */
	private ConsultarCNPJSoap getConsultarCNPJSoap() {
		if (consultarCNPJSoap == null) {
			try {
				URL url = new URL(URL_WSDL);
				QName qname = new QName("nsProxyRFBCNJ", "ConsultarCNPJ");
				ConsultarCNPJ service = new ConsultarCNPJ(url, qname);
				consultarCNPJSoap = service.getConsultarCNPJSoap();
			} catch (MalformedURLException e) {
				String mensagem = e.getMessage();
				mensagem = String.format("Erro ao criar instância do serviço da Receita Federal, erro: %s", mensagem);
				throw new IntercomunicacaoException(mensagem);
			}
		}

		return consultarCNPJSoap;
	}
}
