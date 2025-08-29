package br.jus.cnj.pje.nucleo.service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.log.Log;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.pje.manager.SituacaoProcessoManager;
import br.com.itx.component.Util;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorService;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorServiceAbstract;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.EnderecoWsdlManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoManager;
import br.jus.cnj.pje.nucleo.manager.TarefaManager;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.Tarefa;

@Name(RemessaManifestacaoProcessualService.NAME)
public class RemessaManifestacaoProcessualService {
	
	/**
	 * SOAPHandler para capturar o envelope soap nas requisições as instancias
	 * superiores
	 * 
	 * @author linux
	 * 
	 */
	public class SOAPLoggingHandler implements SOAPHandler<SOAPMessageContext> {

		private String requisicao;

		@Override
		public void close(MessageContext arg0) {

		}

		@Override
		public boolean handleFault(SOAPMessageContext smc) {
			try {
			} catch (Exception e) {
				;
			}
			return false;

		}

		@Override
		public boolean handleMessage(SOAPMessageContext smc) {
			try {

				boolean isRequest = (Boolean) smc
						.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
				if (isRequest) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					smc.getMessage().writeTo(baos);
					requisicao = format(new String(baos.toByteArray(), "UTF-8"));
					baos.close();
				}
				return true;
			} catch (Exception e) {
				;
			}
			return false;
		}

		@Override
		public Set<QName> getHeaders() {
			// TODO Auto-generated method stub
			return null;
		}

		public String format(String xml) {

			try {
				final InputSource src = new InputSource(new StringReader(xml));
				final Node document = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().parse(src).getDocumentElement();
				final Boolean keepDeclaration = Boolean.valueOf(xml
						.startsWith("<?xml"));

				final DOMImplementationRegistry registry = DOMImplementationRegistry
						.newInstance();
				final DOMImplementationLS impl = (DOMImplementationLS) registry
						.getDOMImplementation("LS");
				final LSSerializer writer = impl.createLSSerializer();

				writer.getDomConfig().setParameter("format-pretty-print",
						Boolean.TRUE);
				writer.getDomConfig().setParameter("xml-declaration",
						keepDeclaration);

				return writer.writeToString(document);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public String getRequisicao() {
			return requisicao;
		}

	}

	private static final String COMPLEMENTO_DESTINO = "destino";
	private static final String COMPLEMENTO_MOTIVO_DA_REMESSA = "motivo_da_remessa";

	public static final String NAME = "remessaManifestacaoProcessualService";
	public static final String RESPOSTA_MANIFESTACAO = "resposta";
	public static final String ENVELOPE_REQUISICAO = "requisicao";
	public static final String MANIFESTACAO_PROCESSUAL = "manifestacaoProcessual";
	public static final String DATA_ENVIO = "dataEnvio";
	public static final String STACK_TRACE = "stackTrace";
	public static final String WSDL = "wsdl";
	public static final String ORGAO_DESTINO = "orgaoDestino";

	@Logger
	private Log log;

	@In
	private SituacaoProcessoManager situacaoProcessoManager;

	@In
	private TramitacaoProcessualService tramitacaoProcessualService;

	@In
	private ParametroService parametroService;

	@In
	private TarefaManager tarefaManager;

	@In
	private ProcessoManager processoManager;

	/**
	 * Remeter todas as manifestações processuais que estão localizadas na
	 * tarefa Parametros.REMESSA_MANIFESTACAO_PROCESSUAL_NOME_TAREFA Dependendo
	 * da resposta da instancia superior o fluxo: 1. Em caso de sucesso, navega
	 * para a próxima transição visível. 2. Em caso de sucessivos erros
	 * (Parametros.REMESSA_MANIFESTACAO_PROCESSUAL_MAXIMO_TENTATIVAS), navega
	 * para REMESSA_MANIFESTACAO_PROCESSUAL_TRANSICAO_RECUPERACAO_ERROS
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void remeterManifestacoesPendentes() throws PJeBusinessException {

		String[] nomesTarefa = parametroService.valueOf(
				Parametros.REMESSA_MANIFESTACAO_PROCESSUAL_NOME_TAREFA).split(
				",");

		for (String nomeTarefa : nomesTarefa) {
			List<Tarefa> tarefas = tarefaManager.findByName(nomeTarefa.trim());

			for (Tarefa tarefa : tarefas) {
				List<Integer> idProcessoList = situacaoProcessoManager
						.listProcessosByTarefa(tarefa);
				for (Integer idProcesso : idProcessoList) {

					SituacaoProcesso situacaoProcesso = situacaoProcessoManager
							.getByIdProcesso(idProcesso);
					try {

						// iniciar a tarefa
						JbpmUtil.resumeTask(situacaoProcesso
								.getIdTaskInstance());

						Map<String, Object> dadosResposta = (Map<String, Object>) tramitacaoProcessualService
								.recuperaVariavel(Variaveis.VARIAVEL_REMESSA_RESPOSTA_MANIFESTACAO_PROCESSUAL);

						ManifestacaoProcessualRequisicaoDTO manifestacaoProcessual = (ManifestacaoProcessualRequisicaoDTO) dadosResposta
								.get(MANIFESTACAO_PROCESSUAL);

						ManifestacaoProcessualRespostaDTO resposta = remeterManifestacaoProcessual(
								(String) dadosResposta.get(WSDL),
								(String) dadosResposta.get(ORGAO_DESTINO),
								situacaoProcesso.getProcessoTrf(),
								manifestacaoProcessual);

						String leavingTransition = null;
						if (resposta != null && resposta.getSucesso()) {
							// movimentar para a primeira transicao
							// disponivel
							leavingTransition = org.jboss.seam.bpm.TaskInstance
									.instance().getTask().getTaskNode()
									.getLeavingTransitions().get(0).getName();
						}

						if (leavingTransition != null) {
							Util.beginTransaction();
							BusinessProcess businessProcess = BusinessProcess
									.instance();
							if (JbpmUtil.canTransitTo(TaskInstance.instance(),
									leavingTransition)) {
								businessProcess.endTask(leavingTransition);
								businessProcess.clearDirty();
								businessProcess.setProcessId(null);
							} else {
								log.warn("Transição automática falhou. A transição "
										+ leavingTransition
										+ " não está presente na tarefa "
										+ TaskInstance.instance().getName());
							}
							Util.commitTransction();
						}

					} catch (Exception e) {
						log.error(
								"falha ao remeter manifestação processual (processo (id:"
										+ idProcesso + ")", e);
						BusinessProcess businessProcess = BusinessProcess
								.instance();
						if (businessProcess.hasActiveProcess()) {
							if (Util.beginTransaction()) {
								JbpmUtil.clearAndClose(ManagedJbpmContext
										.instance());
							}
						}
					}
				}
			}

		}
	}

	/**
	 * Remeter manifestação processual ao STF
	 * 
	 * @param manifestacaoProcessual
	 * @return
	 * @throws Exception
	 */
	public ManifestacaoProcessualRespostaDTO remeterManifestacaoProcessualSTF(ProcessoTrf processoOrigem, 
			ManifestacaoProcessualRequisicaoDTO manifestacaoProcessual) {
		String wsdlSTF = parametroService.valueOf(Parametros.WSDL_REMESSA_STF);

		return remeterManifestacaoProcessual(wsdlSTF, "STF", processoOrigem,
				manifestacaoProcessual);
	}

	/**
	 * Remeter manifestação processual
	 * 
	 * @param wsdl
	 * @param manifestacaoProcessual
	 * @return
	 * @throws Exception
	 */
	public ManifestacaoProcessualRespostaDTO remeterManifestacaoProcessual(
			String wsdl, String orgaoDestino, ProcessoTrf processoOrigem,
			ManifestacaoProcessualRequisicaoDTO manifestacaoProcessual) {

		String requisicao = null;
		ManifestacaoProcessualRespostaDTO resposta = null;
		Throwable erro = null;

		try {
			MNIMediatorService mediator = MNIMediatorServiceAbstract.instance(wsdl);
			resposta = mediator.entregarManifestacaoProcessual(manifestacaoProcessual);
			
			if(resposta != null && resposta.getSucesso()){
				lancarMovimentoRemessa(processoOrigem, orgaoDestino);
			}
		} catch (Exception e) {
			erro = e;
		} finally {
			preencherDadosResposta(wsdl, orgaoDestino, requisicao,
					manifestacaoProcessual, resposta, erro);
		}

		return resposta;

	}

	private void lancarMovimentoRemessa(ProcessoTrf processoTrf, String destino) {
		MovimentoAutomaticoService.preencherMovimento()
				.deCodigo(CodigoMovimentoNacional.CODIGO_MOVIMENTO_COMUNICACAO_REMESSA)
				.associarAoProcesso(processoTrf)
				.comComplementoDeNome(COMPLEMENTO_MOTIVO_DA_REMESSA)
				.preencherComTexto("em grau de recurso")
				.comComplementoDeNome(COMPLEMENTO_DESTINO)
				.preencherComTexto(destino).lancarMovimento();
	}

	/**
	 * Monta um debug da resposta da instancia superior e grava na variável
	 * Variaveis.VARIAVEL_REMESSA_RESPOSTA_MANIFESTACAO_PROCESSUAL do fluxo
	 * corrente (se houver)
	 * 
	 * @param requisicao
	 * @param manifestacaoProcessual
	 * @param resposta
	 * @param erro
	 */
	@SuppressWarnings("unchecked")
	private void preencherDadosResposta(String wsdl, String orgaoDestino,
			String requisicao, ManifestacaoProcessualRequisicaoDTO manifestacaoProcessual,
			ManifestacaoProcessualRespostaDTO resposta, Throwable erro) {
		BusinessProcess businessProcess = BusinessProcess.instance();

		if (businessProcess.hasActiveProcess()) {
			Map<String, Object> dadosResposta = (Map<String, Object>) tramitacaoProcessualService
					.recuperaVariavel(Variaveis.VARIAVEL_REMESSA_RESPOSTA_MANIFESTACAO_PROCESSUAL);

			dadosResposta.put(DATA_ENVIO, new Date());
			dadosResposta.put(WSDL, wsdl);
			dadosResposta.put(MANIFESTACAO_PROCESSUAL, manifestacaoProcessual);
			dadosResposta.put(ENVELOPE_REQUISICAO, requisicao);
			dadosResposta.put(RESPOSTA_MANIFESTACAO, resposta);
			dadosResposta.put(ORGAO_DESTINO, orgaoDestino);

			if (erro != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream printStream = new PrintStream(baos);
				erro.printStackTrace(printStream);
				try {
					dadosResposta.put(STACK_TRACE,
							new String(baos.toByteArray(), "UTF-8"));
					baos.close();

				} catch (Exception e1) {
					;
				}
			} else {
				dadosResposta.remove(STACK_TRACE);
			}

			tramitacaoProcessualService
					.gravaVariavel(
							Variaveis.VARIAVEL_REMESSA_RESPOSTA_MANIFESTACAO_PROCESSUAL,
							dadosResposta);

		} else {
			if (log.isDebugEnabled()) {
				log.debug("Resposta da Manifestação Processual");
				log.debug("Envio sucedido? : " + resposta != null ? resposta
						.getSucesso() : "false");
				log.debug("Mensagem : " + resposta != null ? resposta
						.getMensagem() : "");
				log.debug("Protocolo : " + resposta != null ? resposta
						.getNumeroProcesso() : "");
				log.debug("Requisição: " + requisicao);
				log.debug("Erro: ", erro);
			}
		}

	}
}