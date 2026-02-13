/*
 * MNIMediatorService.java
 *
 * Data: 28/07/2020
 */
package br.jus.cnj.pje.intercomunicacao.service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;
import br.jus.cnj.pje.nucleo.manager.EnderecoWsdlManager;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;

/**
 * Classe mediator responsável pelo encapsulamento da intercomunicacão entre
 * sistemas sem expor a dependência com a biblioteca cnj-interop.
 * 
 * @author Adriano Pamplona
 */
public abstract class MNIMediatorServiceAbstract implements MNIMediatorService {
	public static final String NAME = "MNIMediatorService";

	private EnderecoWsdl enderecoWsdl;

	/**
	 * Retorna a instância de MNIMediatorService.
	 * 
	 * @param wsdl Endereço WSDL com o endpoint do MNI.
	 * @return MNIMediatorService
	 */
	public static MNIMediatorService instance(EnderecoWsdl wsdl) {
		Map<String, MNIMediatorServiceAbstract> mapa = new HashMap<>();
		mapa.put("http://www.cnj.jus.br/servico-intercomunicacao-2.2.2/",
				ComponentUtil.getComponent(MNIMediatorService222.class));
		mapa.put("http://www.cnj.jus.br/servico-intercomunicacao-2.2.3.1/",
				ComponentUtil.getComponent(MNIMediatorService223.class));

		MNIMediatorServiceAbstract instance = mapa.get(MNIMediatorServiceAbstract.getTargetNamespace(wsdl));
		instance.setEnderecoWsdl(wsdl);

		return instance;
	}

	/**
	 * Retorna a instância de MNIMediatorService.
	 * 
	 * @param wsdlIntercomunicacao Endpoint do MNI.
	 * @return MNIMediatorService
	 */
	public static MNIMediatorService instance(String wsdlIntercomunicacao) {
		EnderecoWsdlManager manager = EnderecoWsdlManager.instance();
		return instance(manager.obterPeloWsdlIntercomunicacao(wsdlIntercomunicacao));
	}

	/**
	 * Retorna o targetname do endereço WSDL.
	 * 
	 * @param enderecoWsdl EnderecoWsdl
	 * @return Targetname do endereço WSDL.
	 */
	protected static String getTargetNamespace(EnderecoWsdl enderecoWsdl) {
		String resultado = null;

		String wsdl = enderecoWsdl.getWsdlIntercomunicacao();
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(wsdl).openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			f.setNamespaceAware(true);
			Document doc = f.newDocumentBuilder().parse(connection.getInputStream());
			connection.disconnect();
			
			NodeList nodes = doc.getChildNodes();
			Node node = nodes.item(0);
			NamedNodeMap attrs = node.getAttributes();
			resultado = attrs.getNamedItem("targetNamespace").getNodeValue();
		} catch (Exception e) {
			String erro = String.format("Não foi possível obter o targetNamespace do endereço WSDL '%s'.", wsdl);
			throw new IntercomunicacaoException(erro, e);
		}

		return resultado;
	}

	/**
	 * @return enderecoWsdl.
	 */
	protected EnderecoWsdl getEnderecoWsdl() {
		return enderecoWsdl;
	}

	/**
	 * @param enderecoWsdl Atribui enderecoWsdl.
	 */
	protected void setEnderecoWsdl(EnderecoWsdl enderecoWsdl) {
		this.enderecoWsdl = enderecoWsdl;
	}

}
