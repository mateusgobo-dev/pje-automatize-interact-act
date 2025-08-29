package br.com.infox.pje.webservice.consultaoutrasessao;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the
 * br.com.infox.pje.webservice.consultaoutrasessao package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	private final static QName _ObterProcessosResponse_QNAME = new QName("http://webservices.pje.infox.com.br/",
			"obterProcessosResponse");
	private final static QName _ObterProcessos_QNAME = new QName("http://webservices.pje.infox.com.br/",
			"obterProcessos");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package:
	 * br.com.infox.pje.webservice.consultaoutrasessao
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link BeanConsultaProcesso }
	 * 
	 */
	public BeanConsultaProcesso createBeanConsultaProcesso() {
		return new BeanConsultaProcesso();
	}

	/**
	 * Create an instance of {@link BeanRespostaConsultaProcesso }
	 * 
	 */
	public BeanRespostaConsultaProcesso createBeanRespostaConsultaProcesso() {
		return new BeanRespostaConsultaProcesso();
	}

	/**
	 * Create an instance of {@link ObterProcessosResponse }
	 * 
	 */
	public ObterProcessosResponse createObterProcessosResponse() {
		return new ObterProcessosResponse();
	}

	/**
	 * Create an instance of {@link BeanDadosProcesso }
	 * 
	 */
	public BeanDadosProcesso createBeanDadosProcesso() {
		return new BeanDadosProcesso();
	}

	/**
	 * Create an instance of {@link ObterProcessos }
	 * 
	 */
	public ObterProcessos createObterProcessos() {
		return new ObterProcessos();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link ObterProcessosResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webservices.pje.infox.com.br/", name = "obterProcessosResponse")
	public JAXBElement<ObterProcessosResponse> createObterProcessosResponse(ObterProcessosResponse value) {
		return new JAXBElement<ObterProcessosResponse>(_ObterProcessosResponse_QNAME, ObterProcessosResponse.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link ObterProcessos }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webservices.pje.infox.com.br/", name = "obterProcessos")
	public JAXBElement<ObterProcessos> createObterProcessos(ObterProcessos value) {
		return new JAXBElement<ObterProcessos>(_ObterProcessos_QNAME, ObterProcessos.class, null, value);
	}

}
