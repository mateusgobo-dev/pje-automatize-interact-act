package br.jus.cnj.pje.intercomunicacao.v222.servico;

import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.persistence.FlushModeType;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.SystemException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.Events;
import org.jboss.seam.security.Identity;
import org.jboss.seam.transaction.Transaction;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.ReflectionsUtil;
import br.jus.cnj.certificado.CertificadoICP;
import br.jus.cnj.certificado.CertificadoICPBrUtil;
import br.jus.cnj.intercomunicacao.v222.beans.AvisoComunicacaoPendente;
import br.jus.cnj.intercomunicacao.v222.beans.ComunicacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ConfirmacaoRecebimento;
import br.jus.cnj.intercomunicacao.v222.beans.DataHora;
import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.NumeroUnico;
import br.jus.cnj.intercomunicacao.v222.beans.ProcessoJudicial;
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
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.intercomunicacao.v222.servico.IntercomunicacaoObserver.ServiceAdapterEnum;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

public abstract class IntercomunicacaoAbstract implements Intercomunicacao {

	public static final String BEFORE_EVENT_NAME = "br.jus.cnj.pje.intercomunicacao.v222.servico.BeforeProcess";

	public static final String PREVENT_DEFAULT_EVENT_NAME = "br.jus.cnj.pje.intercomunicacao.v222.servico.PreventDefault";

	private Logger log = Logger.getLogger(IntercomunicacaoAbstract.class);

	private boolean preventDefault = false;
	
	public RespostaConsultaAvisosPendentes consultarAvisosPendentes(RequisicaoConsultaAvisosPendentes parametro) {

		RespostaConsultaAvisosPendentes resposta = null;

		try {
			inicializarChamadaSeam();
			autenticar(parametro);

			IntercomunicacaoService service = obterIntercomunicacaoService();

			List<ProcessoParteExpediente> expedientes = service.consultarAvisosPendentes(parametro);
			List<AvisoComunicacaoPendente> avisos = service.converterParaAvisoComunicacaoPendente(expedientes);
			
			Integer totalExpedientes = ProjetoUtil.getTamanho(expedientes);
			Integer totalAvisos = ProjetoUtil.getTamanho(avisos);
			Integer totalFalhas = (totalExpedientes - totalAvisos);
			List<String> idsExpedientesNaoConvertidos = MNIUtil.obterIDExpedientesNaoConvertidos(expedientes, avisos);
				
			String mensagem = "Avisos de comunicação processual consultados com sucesso! Consultados %s de %s (%s avisos com falha %s).";
			resposta = montarRespostaAvisosPendentes(
					null,
					String.format(mensagem, totalAvisos, totalExpedientes, totalFalhas, idsExpedientesNaoConvertidos.toString()),
					avisos);
			
			if(totalFalhas > 0){
				resposta.setSucesso(false);
			}
		} catch (Exception ex) {
			resposta = montarRespostaAvisosPendentes(ex,
					null, null);
		} finally {
			finalizarChamadaSeam();
		}

		return resposta;
	}

	public RespostaConsultarTeorComunicacao consultarTeorComunicacao(RequisicaoConsultarTeorComunicacao parametro) {

		RespostaConsultarTeorComunicacao resposta = null;

		try {
			inicializarChamadaSeam();
			autenticar(parametro);
			String mensagem = "Comunicações processuais consultadas com sucesso!";

			IntercomunicacaoService service = obterIntercomunicacaoService();

			Transaction.instance().begin();
			List<ComunicacaoProcessual> comunicacoes = service.consultarTeorComunicacao(parametro);
			if(comunicacoes.isEmpty()){
				mensagem = "Nenhuma comunicação processual localizada.";
			}
			resposta = montarRespostaTeorComunicacao(null, mensagem, comunicacoes);
			Transaction.instance().commit();
		} catch (Exception ex) {
			if(ex instanceof NegocioException){
				resposta = montarRespostaTeorComunicacao(null, ((NegocioException) ex).getMensagem(), null);
			}else{
				resposta = montarRespostaTeorComunicacao(ex, null, null);
			}
			try {
				Transaction.instance().rollback();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (SystemException e) {
				e.printStackTrace();
			}
		} finally {
			finalizarChamadaSeam();
		}
		return resposta;
	}

	public RespostaConsultaProcesso consultarProcesso(RequisicaoConsultaProcesso parametro) {

		RespostaConsultaProcesso resposta = null;

		try {
			inicializarChamadaSeam();
			autenticar(parametro);

			IntercomunicacaoService service = obterIntercomunicacaoService();

			ProcessoJudicial tipoProcessoJudicial = service.consultarProcesso(parametro);
			resposta = montarRespostaConsultaProcessual(null, "Processo consultado com sucesso", tipoProcessoJudicial);

		} catch (Exception ex) {
			resposta = montarRespostaConsultaProcessual(ex,
					null, null);
			log.error("Não foi possível consultar o processo pela interoperabilidade. Erro:"+resposta.getMensagem());
		} finally {
			finalizarChamadaSeam();
		}

		return resposta;
	}

	public RespostaManifestacaoProcessual entregarManifestacaoProcessual(ManifestacaoProcessual parametro) {

		RespostaManifestacaoProcessual resposta = null;

		try {
			inicializarChamadaSeam();
			autenticar(parametro);

			if (log.isDebugEnabled()) {
				log.debug("Inicio entregaManifestacaoProcessual");
			}
			
			long inicio = System.currentTimeMillis();
			setarTimeoutRemessa();
			Util.beginTransaction();
			FlushModeType modoPadrao = EntityUtil.getEntityManager().getFlushMode();
			EntityUtil.getEntityManager().setFlushMode(FlushModeType.COMMIT);
			EntityUtil.getEntityManager().joinTransaction();
			Events.instance().raiseEvent(BEFORE_EVENT_NAME, parametro);
			IntercomunicacaoService service = obterIntercomunicacaoService();

			if (!preventDefault) {
				String protocolo = service.entregarManifestacaoProcessual(parametro);
				protocolo = (protocolo == null ? parametro.getNumeroProcesso().getValue() : protocolo);
				protocolo = StringUtil.removeNaoNumericos(protocolo);
				protocolo = StringUtils.left(protocolo, 20); //remove do protocolo somente o número do processo
				byte[] recibo = service.obterReciboEntregaManifestacaoProcessual(novoNumeroProcesso(protocolo));

				resposta = montarRespostaManifestacaoProcessual(null, "Manifestação processual recebida com sucesso", protocolo, recibo);

				Util.commitTransction();
				
				EntityUtil.getEntityManager().setFlushMode(modoPadrao == null ? FlushModeType.AUTO : modoPadrao);
			}
			
			long tempoProcessamento = System.currentTimeMillis() - inicio;

			if (log.isDebugEnabled()) {
				log.debug("Fim entregaManifestacaoProcessual em " + tempoProcessamento);
			}

		} catch (Exception ex) {
			String numeroProcesso = "indefinido"; 
			if(parametro.getNumeroProcesso() != null && parametro.getNumeroProcesso().isSetValue()){
				numeroProcesso = parametro.getNumeroProcesso().getValue();
			}else if(parametro.getDadosBasicos() != null && parametro.getDadosBasicos().getNumero() != null && parametro.getDadosBasicos().getNumero().isSetValue()){
				numeroProcesso = parametro.getDadosBasicos().getNumero().getValue();
			}
			String msg = String.format("A entrega do processo %s falhou", numeroProcesso);
			log.error(msg);
			log.error(ex.getMessage(), ex);
			resposta = montarRespostaManifestacaoProcessual(ex, null, null, null);
			Util.rollbackTransaction();
		} finally {
			finalizarChamadaSeam();
		}
		return resposta;
	}

	/**
	 * Operação destinada a permitir uma verificação rápida quanto à existência
	 * de modificações havidas em um processo judicial.
	 * 
	 * 
	 * @param parameters
	 * @return returns
	 *         br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaAlteracao
	 */
	public RespostaConsultaAlteracao consultarAlteracao(RequisicaoConsultaAlteracao parameters) {
		return null;
	}
	

	/**
	 * Operação destinada exclusivamente a tribunais em sua intercomunicação que tem por objetivo permitir 
	 * que um tribunal que tenha sido objeto de uma operação de entrega de manifestação processual (4) 
	 * confirme junto ao tribunal que enviou a manifestação que a recebeu integralmente.
	 * 
	 * @param parameters
	 * @return returns
	 *         br.jus.cnj.intercomunicacao.v222.beans.RespostaConfirmacaoRecebimento
	 */
	public RespostaConfirmacaoRecebimento confirmarRecebimento(ConfirmacaoRecebimento parameters) {
		return null;
	}

	// determina o cancelamento de eventos posteriores
	@Observer({ PREVENT_DEFAULT_EVENT_NAME })
	public void preventDefault() {
		preventDefault = true;
	}

	/**
	 * Monta a resposta da consulta de avisos pendentes.
	 * 
	 * @param sucesso
	 * @param mensagem
	 * @param avisos
	 * @return resposta da consulta de avisos pendentes.
	 */
	protected RespostaConsultaAvisosPendentes montarRespostaAvisosPendentes(Exception excecao, String mensagem, List<AvisoComunicacaoPendente> avisos) {
		RespostaConsultaAvisosPendentes resposta = new RespostaConsultaAvisosPendentes();
		
		tratarResposta(resposta, excecao, mensagem);
		if (excecao == null && avisos != null) {
			resposta.getAviso().addAll(avisos);
		}

		return resposta;
	}

	/**
	 * @param sucesso
	 * @param mensagem
	 * @param tipoProcessoJudicial
	 * @return resposta da consulta processual.
	 */
	protected RespostaConsultaProcesso montarRespostaConsultaProcessual(Exception excecao, String mensagem, ProcessoJudicial tipoProcessoJudicial) {
		RespostaConsultaProcesso resposta = new RespostaConsultaProcesso();
		
		tratarResposta(resposta, excecao, mensagem);
		if (excecao == null) {
			resposta.setProcesso(tipoProcessoJudicial);
		}
		return resposta;
	}

	/**
	 * Monta a resposta da manifestação processual.
	 * 
	 * @param sucesso
	 * @param mensagem
	 * @param protocolo
	 * @param pdfRecibo
	 * @return resposta da manifestação processual.
	 */
	protected RespostaManifestacaoProcessual montarRespostaManifestacaoProcessual(Exception excecao, String mensagem, String protocolo, byte[] pdfRecibo) {
		RespostaManifestacaoProcessual resposta = new RespostaManifestacaoProcessual();
		
		tratarResposta(resposta, excecao, mensagem);
		if (excecao == null) {
			resposta.setProtocoloRecebimento(protocolo);
			DataHora dataHora = new DataHora();
			dataHora.setValue(DateUtil.dateToString(new Date(), "yyyy/MM/dd H:m:s"));
			resposta.setDataOperacao(dataHora);
			
			if (pdfRecibo != null) {
				DataHandler dh = ProjetoUtil.converterParaDataHandler(pdfRecibo);
				resposta.setRecibo(dh);
			}
		}
		return resposta;
	}

	/**
	 * Monta a resposta da consulta de teor específico de comunicação.
	 * 
	 * @param excecao
	 * @param mensagem
	 * @param comunicacoes
	 * @return resposta da consulta de teor específico de comunicação.
	 */
	protected RespostaConsultarTeorComunicacao montarRespostaTeorComunicacao(Exception excecao, String mensagem, List<ComunicacaoProcessual> comunicacoes) {
		RespostaConsultarTeorComunicacao resposta = new RespostaConsultarTeorComunicacao();
		
		tratarResposta(resposta, excecao, mensagem);
		if (excecao == null) {
			if (comunicacoes != null) {
				resposta.getComunicacao().addAll(comunicacoes);
			}
		}
		return resposta;
	}

	/**
	 * @param ex
	 * @return mensagem de erro.
	 */
	protected String gerarMensagemDeErro(Throwable ex) {
		StringBuilder mensagem = new StringBuilder();
		if(ex instanceof NegocioException){
			mensagem.append(((NegocioException) ex).getMensagem());
		}else if(ex.getCause() != null && ex.getCause() instanceof NegocioException){
			mensagem.append(((NegocioException) ex.getCause()).getMensagem());
		}else if(ex.getCause() != null && ex.getCause() instanceof LoginException){
			mensagem.append(((LoginException) ex.getCause()).getMessage());
		}else if(ex instanceof PJeBusinessException){
			mensagem.append(((PJeBusinessException) ex).getLocalizedMessage());
		}
		while (ex != null && mensagem.length() == 0) {
			if (ex.getMessage() != null) {
				mensagem.append(ex.getMessage());
			} else if (ex.getLocalizedMessage() != null) {
				mensagem.append(ex.getLocalizedMessage());
			} else {
				ex = ex.getCause();
			}
		}
		if (mensagem.length() == 0) {
			mensagem.append("Ocorreu um erro no sistema, contacte o administrador!");
		}
		return mensagem.toString();
	}

	/**
	 * Inicializa uma chamada aos objetos mantidos no contexto do seam.
	 */
	protected void inicializarChamadaSeam() {
		Lifecycle.beginCall();	
	}

	/**
	 * Finaliza a chamada a objetos mantidos pelo contexto do seam.
	 */
	protected void finalizarChamadaSeam() {
		try {
			Identity.instance().logout();
			Lifecycle.endCall();
		} catch (Exception e) {
			log.warn(e);
			// Abstrai exceções do tipo 'GenericJDBCException', neste momento 
			// não é preciso fazer controle de exceções, pois a execução do 
			// serviço chegou ao fim.
		}
	}

	/**
	 * Retorna o serviço de intercomunicação.
	 * 
	 * @return IntercomunicacaoService
	 */
	protected IntercomunicacaoService obterIntercomunicacaoService() {
		return (IntercomunicacaoService) Component.getInstance(IntercomunicacaoService.NAME);
	}

	/**
	 * Efetua a autenticação do usuário da requisição. A autenticação pode ser
	 * via certificado digital, login/senha ou pode ser ignorada. 
	 * TODO (adriano.pamplona): mover recurso para Handler assim que colocar para
	 * funcionar o catálogo de serviço.
	 * 
	 * @param requisicao
	 *            Objeto da requisição (RequisicaoConsultaAvisosPendentes,
	 *            TipoConsultarTeorComunicacao, RequisicaoConsultaProcesso,
	 *            ManifestacaoProcessual, RequisicaoConsultaAlteracao)
	 * @throws LoginException
	 */
	protected void autenticar(Object requisicao) throws LoginException {

		try {
			if (isAutenticacaoPeloCertificadoDigital()) {
				autenticarPeloCertificadoDigital();
			} else if (isAutenticacaoPeloConsultanteEhSenha(requisicao)) {
				autenticarPeloConsultanteEhSenha(requisicao);
			} else {
				throw new LoginException("Falha no login. Acesso não Autorizado!");
			}
			Identity identity = Identity.instance();
			if(identity != null){
				identity.addRole(Papeis.CONSULTA_MNI);
			}
			
			atribuirLogin(requisicao);
		} catch (Exception e) {
			throw new LoginException("Erro ao realizar login via MNI. "+ e.getLocalizedMessage());
		}
	}

	/**
	 * Verifica se o idManifestante necessita de um adaptador
	 * 
	 * @param requisicao
	 * @return true caso o idManifestante esteje na lista enumerada ServiceAdapterEnum
	 */
	protected boolean requiresObserver(Object requisicao) {
		String demandante = ReflectionsUtil.getStringValue(requisicao, "idManifestante");
		
		if (ServiceAdapterEnum.constains(demandante)) {
			return true;
		}
		return false;
	}

	/**
	 * Retorna true se houver certificado digital registrado no request.
	 * 
	 * @return true se houver certificado digital no request.
	 */
	protected boolean isAutenticacaoPeloCertificadoDigital() {
		X509Certificate[] certificados = getX509CertificateArray();

		return ArrayUtils.isNotEmpty(certificados);
	}

	/**
	 * Retorna true se o login/senha estiverem preenchidos.
	 * 
	 * @param requisicao
	 *            Objeto da requisição (RequisicaoConsultaAvisosPendentes,
	 *            TipoConsultarTeorComunicacao, RequisicaoConsultaProcesso,
	 *            ManifestacaoProcessual, RequisicaoConsultaAlteracao)
	 * @return true se a autenticação for pelo login/senha.
	 */
	protected boolean isAutenticacaoPeloConsultanteEhSenha(Object requisicao) {
		String login = MNIUtil.obterLogin(requisicao);
		String senha = MNIUtil.obterSenha(requisicao);
		
		return !isAutenticacaoPeloCertificadoDigital() && ProjetoUtil.isNaoVazio(login, senha);
	}

	/**
	 * Executa a autenticação via certificado digital.
	 * 
	 * @throws LoginException
	 */
	protected void autenticarPeloCertificadoDigital() throws LoginException {
		X509Certificate[] certificados = getX509CertificateArray();
		X509Certificate certificado = certificados[0];

		Authenticator authenticator = ComponentUtil.getComponent(Authenticator.class);
		CertificadoICP icp = CertificadoICPBrUtil.getInstance(certificado);
		authenticator.authenticateWS(icp);
	}

	/**
	 * Executa a autenticação via login/senha.
	 * 
	 * @param requisicao
	 *            Objeto da requisição (RequisicaoConsultaAvisosPendentes,
	 *            TipoConsultarTeorComunicacao, RequisicaoConsultaProcesso,
	 *            ManifestacaoProcessual, RequisicaoConsultaAlteracao)
	 * @throws LoginException
	 */
	protected void autenticarPeloConsultanteEhSenha(Object requisicao)
			throws LoginException {

		String login = MNIUtil.obterLogin(requisicao);
		String senha = MNIUtil.obterSenha(requisicao);
		UsuarioLocalizacao localizacao = MNIUtil.obterLocalizacao(requisicao);
		String idOrgaoRepresentacao = MNIUtil.obterIdOrgaoRepresentacao(requisicao);
		
		Authenticator authenticator = (Authenticator) Component.getInstance(Authenticator.class, true);

		authenticator.authenticateWS(login, senha, localizacao, idOrgaoRepresentacao);
	}

	/**
	 * Retorna o array de X509Certificate registrado na requisição.
	 * 
	 * @return array de X509Certificate
	 */
	protected X509Certificate[] getX509CertificateArray() {
		return (X509Certificate[]) getRequest().getAttribute("javax.servlet.request.X509Certificate");
	}

	/**
	 * Atribui o login do usuário logado no objeto da requisição. Essa informação será usada 
	 * posteriormente nos serviços.
	 * 
	 * @param requisicao
	 */
	protected void atribuirLogin(Object requisicao) {
		Usuario usuarioLogado = Authenticator.getUsuarioLogado();
		
		if (usuarioLogado != null) {
			String login = usuarioLogado.getLogin();
			MNIUtil.atribuirLogin(requisicao, login);
		}
	}

	/**
	 * @param numeroProcesso
	 * @return instância do NumeroUnidco do processo.
	 */
	protected NumeroUnico novoNumeroProcesso(String numeroProcesso) {
		NumeroUnico nu = new NumeroUnico();
		nu.setValue(numeroProcesso);
		return nu;
	}

	/**
	 * Preenche os atributos da resposta da execução do serviço, somente os 
	 * atributos comuns são preenchidos.
	 * 
	 * @param resposta Objeto de retorno da invocação do serviço.
	 * @param excecao Exceção
	 * @param mensagem Mensagem que será atribuída ao retorno.
	 */
	protected void tratarResposta(Object resposta, Exception excecao, String mensagem) {
		
		if (excecao == null) {
			ReflectionsUtil.setValue(resposta, "sucesso", Boolean.TRUE);
			ReflectionsUtil.setValue(resposta, "mensagem", mensagem);
		} else {
			ReflectionsUtil.setValue(resposta, "sucesso", Boolean.FALSE);
			ReflectionsUtil.setValue(resposta, "mensagem", gerarMensagemDeErro(excecao));
		}
	}

	/**
	 * Conforme parâmetro seta o tempo na transação da remessa.
	 * @throws SystemException
	 */
	protected void setarTimeoutRemessa() throws SystemException {
		ParametroService parametroService = (ParametroService) Component.getInstance("parametroService");
		String parametroTimeout = parametroService.valueOf(Parametros.TIMEOUT_REMESSA);
	    int timeout = 0;
		if(!StringUtil.isEmpty(parametroTimeout)){
			try {
				timeout = Integer.parseInt(parametroTimeout);
				if(timeout != 0){
					Transaction.instance().setTransactionTimeout(timeout);
				}
			} catch( Exception e ) {
				String mensagem = MessageFormat.format("O parametro {0} está com formato inválido: {1}",Parametros.TIMEOUT_REMESSA,parametroTimeout);
				log.error(mensagem);
			}
		}
		
	}
	
	protected abstract HttpServletRequest getRequest();
}
