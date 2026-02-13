/*
x * MNIMediatorService222.java
 *
 * Data: 28/07/2020
 */
package br.jus.cnj.pje.intercomunicacao.service;

import java.net.URL;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.servico.Intercomunicacao;
import br.jus.cnj.intercomunicacao.v222.servico.IntercomunicacaoService;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;
import br.jus.cnj.pje.intercomunicacao.v222.converter.ConsultarProcessoRequisicaoDTOConverter;
import br.jus.cnj.pje.intercomunicacao.v222.converter.ManifestacaoProcessualRequisicaoDTOConverter;
import br.jus.cnj.pje.intercomunicacao.v222.converter.RespostaConsultaProcessoConverter;
import br.jus.cnj.pje.intercomunicacao.v222.converter.RespostaManifestacaoProcessualConverter;
import br.jus.cnj.pje.intercomunicacao.v222.servico.IntercomunicacaoSoapImpl;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;

/**
 * Classe de serviço responsável pela comunicação entre sistemas via MNI versão 2.2.2.
 * 
 * @author Adriano Pamplona
 */
@Name(MNIMediatorService222.NAME)
public class MNIMediatorService222 extends MNIMediatorServiceAbstract {
	public static final String NAME = "mniMediatorService222";

	public static MNIMediatorService223 instance(EnderecoWsdl enderecoWsdl) {
		MNIMediatorService223 instance = ComponentUtil.getComponent(MNIMediatorService223.class);
		instance.setEnderecoWsdl(enderecoWsdl);
		
		return instance;
	}

	@Override
	public ConsultarProcessoRespostaDTO consultarProcesso(ConsultarProcessoRequisicaoDTO requisicao) throws IntercomunicacaoException {
		ConsultarProcessoRequisicaoDTOConverter consultarProcessoRequisicaoDTOConverter = ConsultarProcessoRequisicaoDTOConverter.instance();
		RequisicaoConsultaProcesso requisicaoMNI = consultarProcessoRequisicaoDTOConverter.converter(requisicao, getEnderecoWsdl());
		
		Intercomunicacao intercomunicacao = obterIntercomunicacao();
		RespostaConsultaProcesso respostaMNI = intercomunicacao.consultarProcesso(requisicaoMNI);
		
		RespostaConsultaProcessoConverter respostaConsultaProcessoConverter = RespostaConsultaProcessoConverter.instance();
		return respostaConsultaProcessoConverter.converter(respostaMNI);
	}
	
	@Override
	public ManifestacaoProcessualRespostaDTO entregarManifestacaoProcessual(
			ManifestacaoProcessualRequisicaoDTO requisicao) throws IntercomunicacaoException {
		ManifestacaoProcessualRequisicaoDTOConverter manifestacaoProcessualRequisicaoDTOConverter = ManifestacaoProcessualRequisicaoDTOConverter.instance();
		
		ManifestacaoProcessual requisicaoMNI = manifestacaoProcessualRequisicaoDTOConverter.converter(requisicao, getEnderecoWsdl());
		
		Intercomunicacao intercomunicacao = obterIntercomunicacao();
		RespostaManifestacaoProcessual respostaMNI = intercomunicacao.entregarManifestacaoProcessual(requisicaoMNI);
		
		RespostaManifestacaoProcessualConverter respostaManifestacaoProcessualConverter = RespostaManifestacaoProcessualConverter.instance();
		return respostaManifestacaoProcessualConverter.converter(respostaMNI);
	}
	
	@Override
	public ConsultarProcessoRespostaDTO login() throws IntercomunicacaoException {
		return login(getEnderecoWsdl().getLogin(), getEnderecoWsdl().getSenha());
	}

	@Override
	public ConsultarProcessoRespostaDTO login(String login, String senha) throws IntercomunicacaoException {
		RequisicaoConsultaProcesso requisicaoMNI = new RequisicaoConsultaProcesso();
		requisicaoMNI.setIdConsultante(getEnderecoWsdl().getLogin());
		requisicaoMNI.setSenhaConsultante(getEnderecoWsdl().getSenha());
		requisicaoMNI.setIncluirCabecalho(Boolean.FALSE);
		requisicaoMNI.setIncluirDocumentos(Boolean.FALSE);
		requisicaoMNI.setMovimentos(Boolean.FALSE);
		
		Intercomunicacao intercomunicacao = obterIntercomunicacao();
		RespostaConsultaProcesso respostaMNI = intercomunicacao.consultarProcesso(requisicaoMNI);
		
		if (!StringUtils.contains(respostaMNI.getMensagem(), "Erro ao realizar login via MNI.")) {
			respostaMNI.setSucesso(Boolean.TRUE);
			respostaMNI.setMensagem(null);
		}
		
		RespostaConsultaProcessoConverter respostaConsultaProcessoConverter = RespostaConsultaProcessoConverter.instance();
		return respostaConsultaProcessoConverter.converter(respostaMNI);
	}

	/**
	 * Faz lookup no serviço Intercomunicacao.
	 * 
	 * @param EnderecoWsdl
	 * @param anonimo
	 * @return Serviço Intercomunicacao
	 */
	protected Intercomunicacao obterIntercomunicacao() {
		Intercomunicacao resultado = null;
		WebService annotation = IntercomunicacaoSoapImpl.class.getAnnotation(WebService.class);
		String url = getEnderecoWsdl().getWsdlIntercomunicacao();
		String namespace = annotation.targetNamespace();
		String port = ObjectUtils.firstNonNull(getEnderecoWsdl().getDsServiceName(), annotation.serviceName());
		
		
		try {
			IntercomunicacaoService service = new IntercomunicacaoService(new URL(url), new QName(namespace, port));
			resultado =  service.getPort(Intercomunicacao.class);
			BindingProvider bp = (BindingProvider) resultado;
			bp.getRequestContext().put("com.sun.xml.internal.ws.request.timeout", 0);
			bp.getRequestContext().put("javax.xml.ws.client.connectionTimeout", 0);
			bp.getRequestContext().put("javax.xml.ws.client.receiveTimeout", 0);	
		} catch (Exception e) {
			String mensagem = "Não foi possível fazer lookup no endpoint ['url: %s', 'namespace: %s', 'port: %s'], "
					+ "verifique se a url do endereço está correta. Erro: %s";
			throw new WebServiceException(String.format(mensagem, url, namespace, port, e.getMessage()));
		}
		
		return resultado;
	}
}
