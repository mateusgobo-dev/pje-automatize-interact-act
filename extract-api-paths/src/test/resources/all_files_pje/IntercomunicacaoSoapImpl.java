package br.jus.cnj.pje.intercomunicacao.v222.servico;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.MTOM;

import br.jus.cnj.intercomunicacao.v222.beans.ConfirmacaoRecebimento;
import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaAlteracao;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaAvisosPendentes;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultarTeorComunicacao;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConfirmacaoRecebimento;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaAlteracao;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaAvisosPendentes;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultarTeorComunicacao;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.servico.Intercomunicacao;

@WebService(serviceName = "IntercomunicacaoService", targetNamespace = "http://www.cnj.jus.br/servico-intercomunicacao-2.2.2/", endpointInterface = "br.jus.cnj.intercomunicacao.v222.servico.Intercomunicacao")
@MTOM(enabled = true, threshold = 4096)
@org.apache.cxf.interceptor.InInterceptors(interceptors = { "br.jus.cnj.pje.util.PJESoapActionInInterceptor" })
public class IntercomunicacaoSoapImpl extends IntercomunicacaoAbstract {

	@Resource
	private WebServiceContext wsContext;

	/**
	 * @see Intercomunicacao#consultarAvisosPendentes(RequisicaoConsultaAvisosPendentes)
	 */
	@WebMethod
	public RespostaConsultaAvisosPendentes consultarAvisosPendentes(RequisicaoConsultaAvisosPendentes parameters) {
		return super.consultarAvisosPendentes(parameters);
	}

	/**
	 * @see Intercomunicacao#consultarTeorComunicacao(RequisicaoConsultarTeorComunicacao)
	 */
	@WebMethod
	public RespostaConsultarTeorComunicacao consultarTeorComunicacao(RequisicaoConsultarTeorComunicacao parameters) {
		return super.consultarTeorComunicacao(parameters);
	}

	/**
	 * @see Intercomunicacao#consultarProcesso(RequisicaoConsultaProcesso)
	 */
	@WebMethod
	public RespostaConsultaProcesso consultarProcesso(RequisicaoConsultaProcesso parameters) {
		return super.consultarProcesso(parameters);
	}

	/**
	 * @see Intercomunicacao#entregarManifestacaoProcessual(ManifestacaoProcessual)
	 */
	@WebMethod
	public RespostaManifestacaoProcessual entregarManifestacaoProcessual(ManifestacaoProcessual parameters) {
		return super.entregarManifestacaoProcessual(parameters);
	}

	/**
	 * @see Intercomunicacao#consultarAlteracao(RequisicaoConsultaAlteracao)
	 */
	@WebMethod
	public RespostaConsultaAlteracao consultarAlteracao(RequisicaoConsultaAlteracao parameters) {
		return super.consultarAlteracao(parameters);
	}

	/**
	 * @see Intercomunicacao#confirmarRecebimento(ConfirmacaoRecebimento)
	 */
	@WebMethod
	public RespostaConfirmacaoRecebimento confirmarRecebimento(ConfirmacaoRecebimento parameters) {
		return super.confirmarRecebimento(parameters);
	}

	@Override
	protected HttpServletRequest getRequest() {
		MessageContext messageContext = wsContext.getMessageContext();
		return (HttpServletRequest) messageContext.get(MessageContext.SERVLET_REQUEST);
	}
}
