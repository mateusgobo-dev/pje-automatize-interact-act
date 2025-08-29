package br.jus.pdpj.notificacao.service;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.util.HttpUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.manager.LogNotificacaoManager;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.entidades.vo.ResultadoComplexoVO;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.intercomunicacao.v223.servico.IntercomunicacaoBase;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.AlertaManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoAlertaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.PortalService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pdpj.commons.models.dtos.mensagens.DocumentoDTO;
import br.jus.pdpj.commons.models.dtos.webhooks.WebhookWrapperMessage;
import br.jus.pdpj.commons.models.vo.TextoProcessado;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.dto.portal.MensagemDTO;
import br.jus.pje.nucleo.entidades.Alerta;
import br.jus.pje.nucleo.entidades.LogNotificacao;
import br.jus.pje.nucleo.entidades.ProcessoAlerta;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;
import br.jus.pje.nucleo.util.PJEHolder;
import br.jus.pje.nucleo.util.StringUtil;

@Name("webhookWrapperService")
@Path("pdpjwebhook")
public class WebhookWrapperService {

	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private ProcessoAlertaManager processoAlertaManager;
	
	@In
	private AlertaManager alertaManager;
	
	@In
	private ParametroService parametroService;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In
	private ProcessoDocumentoBinManager processoDocumentoBinManager;
	
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	
	@In(create=true)
	private IntercomunicacaoBase intercomunicacaoBase;

	@In(create=true)
	private PortalService portalService;

	@Context
	HttpServletRequest httpRequest;
	
	private Logger log = Logger.getLogger(WebhookWrapperService.class);
	
	private LogNotificacao logNotificacao;
	
	private static final String NAO_POSSIVEL_GRAVAR_LOG_NOTIFICACAO="Não foi possível gravar log de notificação";

	@POST
	@Path("/portal/peticao")
	public Response peticionar(WebhookWrapperMessage message) {
		Response response = null;
		MensagemDTO mensagem = null;
		try {
			response = validarRequisicao(message);

			mensagem = getObjectMapper().convertValue(message.getPayload().getConteudo(), MensagemDTO.class);
			if (mensagem == null) {
				throw new PJeBusinessException("Não foi possível recuperar o payload da mensagem.");
			}
			mensagem.setNotificacaoId(message.getNotificacaoId());

			if (HttpUtil.isStatus2xx(response.getStatus())) {
				PJEHolder.setWebhookAction(Boolean.TRUE);
				Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_PORTAL_PETICAO_RECEBIDA, mensagem, message.getNumeroUnicoProcesso());
			} else {
				String erro = "[PortalDeServicos] Erro ao validar peticão protocolada: Status " + response.getStatus() + " Mensagem: " + response.getEntity();
				throw new PJeBusinessException(erro);
			}
		} catch (Exception e) {
			log.error(e);
			portalService.enviarErroCallback(mensagem, e.getLocalizedMessage());
			portalService.registrarErroNoLogNotificacao(mensagem, e);
			response = Response.serverError().entity(e.getLocalizedMessage()).build();
		}
		return response;
	}

	@POST
	@Path("/documento")
	public Response incluirDocumento(WebhookWrapperMessage documento) {
		Response response = null;
		
		try {
			response = validarRequisicao(documento);
			if (response.getStatus() == 200) {
				PJEHolder.setWebhookAction(Boolean.TRUE);
				Util.beginTransaction();
				
				List<ProcessoTrf> ptf = processoJudicialManager.findByNU(documento.getNumeroUnicoProcesso());
				
				if(ptf == null || ptf.isEmpty()) {
					Util.rollbackTransaction();

					return Response.status(Status.NOT_FOUND).entity("Processo "+documento.getNumeroUnicoProcesso() +" não encontrado.").build();		
				}
				
				DocumentoDTO vo = getObjectMapper().convertValue(documento.getPayload().getConteudo(), DocumentoDTO.class);
				ProcessoDocumento pd = trataProcessoDocumento(vo,ptf.get(0));
				trataProcessoDocumentoBin(pd.getProcessoDocumentoBin(), vo);
				ResultadoComplexoVO res = DocumentoJudicialService.instance().juntarDocumento(pd.getIdProcessoDocumento(), null);
				
				if(!res.getResultado()) {
					Util.rollbackTransaction();
					return Response.serverError().entity(res.getMensagem()).build();
				}
				
				MovimentoAutomaticoService.preencherMovimento().
					deCodigo(CodigoMovimentoNacional.COD_MOVIMENTO_JUNTADA_DOCUMENTO).
					comProximoComplementoVazio().
					doTipoLivre().
					preencherComTexto(pd.getTipoProcessoDocumento().getTipoProcessoDocumento().toLowerCase()).
					associarAoProcesso(ptf.get(0)).
					associarAoDocumento(pd).
					lancarMovimento();
				Util.commitTransction();
				response = Response.ok().build();
			}
		} catch (Exception e) {
			Util.rollbackTransaction();
			log.error(e);
			response = Response.serverError().entity(e.getLocalizedMessage()).build();
		}
		
		return response;
	}
	
	@POST
	@Path("/alerta")
	public Response incluirAlerta(WebhookWrapperMessage alerta) {
		Response response = null;
		
		try {
			response = validarRequisicao(alerta);
			if (response.getStatus() == 200) {
				PJEHolder.setWebhookAction(Boolean.TRUE);
				Util.beginTransaction();
				List<ProcessoTrf> ptf = processoJudicialManager.findByNU(alerta.getNumeroUnicoProcesso());
				
				if(ptf == null || ptf.isEmpty()) {
					return Response.status(Status.NOT_FOUND).entity("Processo "+alerta.getNumeroUnicoProcesso() +" não encontrado.").build();		
				}
				
				TextoProcessado vo = getObjectMapper().convertValue(alerta.getPayload().getConteudo(), TextoProcessado.class);
				
				for(ProcessoTrf p : ptf){
					
					Alerta a = new Alerta();
					a.setAlerta(vo.getTextoFinal());
					a.setAtivo(true);
					a.setDataAlerta(new Date());
					a.setInCriticidade(CriticidadeAlertaEnum.I);
					a.setOrgaoJulgador(p.getOrgaoJulgador());
					a.setOrgaoJulgadorColegiado(p.getOrgaoJulgadorColegiado());
					alertaManager.persist(a);
					
					ProcessoAlerta pa = new ProcessoAlerta();
					pa.setAlerta(a);
					pa.setAtivo(true);
					pa.setProcessoTrf(p);
					
					processoAlertaManager.persist(pa);
				}
				
				processoAlertaManager.flush();
				Util.commitTransction();
				response = Response.ok().build();
			}
		} catch (Exception e) {
			Util.rollbackTransaction();
			log.error(e);
			response = Response.serverError().entity(e.getLocalizedMessage()).build();
		}
		
		return response;
	}

	/**
	 * Registra ciência no expediente enviado pelo payload. Formato do payload: <br/>
	 * <code>
	 *   {
	 *     <dd>idProcessoParteExpediente: Integer</dd>
	 *   }
	 * </code>
	 * @param message Objeto enviado pelo serviço de notificações.
	 * @return Código HTTP (200 = OK) 
	 */
	@POST
	@Path("/expediente/ciencia")
	public Response atribuirCienciaExpediente(WebhookWrapperMessage message) {
		Response response = null;

		try {
			response = validarRequisicao(message);
			if (response.getStatus() == 200) {
				PJEHolder.setWebhookAction(Boolean.TRUE);
				Util.beginTransaction();
				AtoComunicacaoService.instance().registrarCienciaAutomatica(message.getModeloEvento(),
						message.getPayload());
				Util.commitTransction();
				response = Response.ok().build();

			}
		} catch (Exception e) {
			Util.rollbackTransaction();
			log.error(e);
			response = Response.serverError().entity(e.getLocalizedMessage()).build();
		}

		return response;
	}
	
	@POST
	@Path("/expediente/ciencia/tacita")
	public Response atribuirCienciaTacitaExpediente(WebhookWrapperMessage message) {
		Response response = null;

		try {
			response = validarRequisicao(message);
			if (response.getStatus() == 200) {
				PJEHolder.setWebhookAction(Boolean.TRUE);
				Util.beginTransaction();
				AtoComunicacaoService.instance().registrarCienciaTacita(message.getModeloEvento(),
						message.getPayload());
				Util.commitTransction();
				 response = Response.ok().build();
			}
		} catch (Exception e) {
			Util.rollbackTransaction();
			log.error(e);
			response = Response.serverError().entity(e.getLocalizedMessage()).build();
		}
		
		return response;
	}
	
	protected boolean validaAssinatura(WebhookWrapperMessage mensagem, String hashAssinaturaRequest,String chave) throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException {
		try {
			String messageString = getObjectMapper().writeValueAsString(mensagem);
		
			SecretKeySpec keySpec = new SecretKeySpec(chave.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(keySpec);
			byte[] rawHmac = mac.doFinal(messageString.getBytes(StandardCharsets.UTF_8));
			String hashAssinaturaProcessado = Hex.encodeHexString(rawHmac);

			return hashAssinaturaProcessado.equals(hashAssinaturaRequest);
		} catch (IllegalStateException e) {
			log.error(e);
		}
		return false;

	}
	
	private void trataProcessoDocumentoBin(ProcessoDocumentoBin bin, DocumentoDTO vo) throws PJeBusinessException, IOException, MimeTypeException {
		bin.setBinario(true);
		bin.setExtensao(vo.getDocumento().getMimetype());
		byte[] conteudo = SigningUtilities.base64Decode(vo.getDocumento().getConteudoBase64());

		String uri = "/mySecureDirectory";
		File arquivoBinario = File.createTempFile(Long.toString(bin.getIdProcessoDocumentoBin()), 
				".tmp", new File(uri));

		FileUtils.writeByteArrayToFile(arquivoBinario, conteudo);

		bin.setFile(arquivoBinario);
		documentoJudicialService.updateMD5(bin);
		bin.setModeloDocumento(null);
		bin.setSize(conteudo.length);
		bin.setNomeArquivo(vo.getDescricao() + "." + MimeTypes.getDefaultMimeTypes().forName(vo.getDocumento().getMimetype()).getExtension());
		processoDocumentoBinManager.persist(bin);
	}
	
	private ProcessoDocumento trataProcessoDocumento(DocumentoDTO vo, ProcessoTrf ptf) throws PJeBusinessException {
		TipoProcessoDocumento tpd = tipoProcessoDocumentoManager.findByCodigoDocumento(vo.getTipoCodigoNacional(), true);
		
		Integer idDoc = documentoJudicialService.gerarMinuta(ptf.getIdProcessoTrf(), null, null, tpd.getIdTipoProcessoDocumento(), null);
		ProcessoDocumento pd = processoDocumentoManager.findById(idDoc);
		pd.setProcessoDocumento(vo.getDescricao());
		pd.setDocumentoSigiloso(vo.getSigiloNivel() != null && vo.getSigiloNivel() > 0);
		
		return pd;
	}
	
	protected ObjectMapper getObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
		mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		
		return mapper;
	}
	
	/**
	 * Valida requisição de entrada do serviço rest.
	 * 
	 * @param message WebhookWrapperMessage
	 * @return Response(200) se estiver OK ou Response(<>200 se houver algum problema)
	 * @throws JsonProcessingException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 */
	protected Response validarRequisicao(WebhookWrapperMessage message) throws JsonProcessingException, InvalidKeyException, NoSuchAlgorithmException {

		logarRequisao(message);
		Response response = Response.ok().build();
		
		String assinaturaRequest = httpRequest.getHeader("x-pdpj-webhook-signature");
		String secretTokenPdpjNotificacao = parametroService.valueOf(Parametros.PDPJ_NOTIFICACOES_SECRET_TOKEN);

		if(StringUtil.isEmpty(secretTokenPdpjNotificacao)) {
			log.error("WebhookWrapperService:validarRequisição: " + "Chave de assinatura para o serviço de notificações não cadastrada.");
			return Response.status(Status.FORBIDDEN).entity("Chave de assinatura para o serviço de notificações não cadastrada.").build();
		}
		
		if(assinaturaRequest == null || !validaAssinatura(message, assinaturaRequest, secretTokenPdpjNotificacao)) {
			log.error("WebhookWrapperService:validarRequisição: " + "Assinatura do payload inválida.");
			return Response.status(Status.FORBIDDEN).entity("Assinatura do payload inválida.").build();
		}
		
		if (!isGrauSistemaIgualGrauRequisicao(message)) {
			log.warn("WebhookWrapperService:validarRequisição: " + "O grau da instância da requisição não é a mesma da instalação.");
			return Response.status(Status.BAD_REQUEST).entity("O grau da instância da requisição não é a mesma da instalação.").build();
		}

		if(!isNumeracaoProcessualPertecenteAoTribunalLocal(message)) {
			return Response.status(Status.BAD_REQUEST).entity("O processo não pertece ao tribunal.").build();
		}

		LogNotificacaoManager logNotificacaoManager = ComponentUtil.getComponent(LogNotificacaoManager.class);
		
		if(logNotificacaoManager.existeNotificaoProcessadaComSucesso(message.getNotificacaoId())) {
			response = Response.status(Status.BAD_REQUEST).entity("A Notificao já foi processada.").build();
		}
		
		return response;
	}

	protected void logarRequisao(WebhookWrapperMessage message) {
		LogNotificacaoManager logWebHookManager = ComponentUtil.getComponent(LogNotificacaoManager.class);
		
		try {
			LogNotificacao logNot = LogNotificacao.createNew();
			
			logNot.setNrProcesso(message.getNumeroUnicoProcesso());
			logNot.setPayload(StringUtil.normalize(new Gson().toJson(message)));
			logNot.setData(new Date());
			logNot.setIdNotificacao(message.getNotificacaoId());
			logNot.setIpRequisicao(httpRequest.getRemoteAddr());

			logWebHookManager.persistAndFlush(logNot);
			
			this.logNotificacao = logNot;			
		} catch (PJeBusinessException e) {
			log.error(NAO_POSSIVEL_GRAVAR_LOG_NOTIFICACAO,e);
		}
	}
	
	
	protected void registraSucessoNotificacao() {	
		LogNotificacaoManager logWebHookManager = ComponentUtil.getComponent(LogNotificacaoManager.class);

		if(logNotificacao == null) {
			return;
		}
		try {
			logNotificacao.setSucesso(true);
			logWebHookManager.persistAndFlush(logNotificacao);
		} catch (PJeBusinessException e) {
			log.error(NAO_POSSIVEL_GRAVAR_LOG_NOTIFICACAO,e);
		}		
	}
	
	protected void registraFalhaNotificacao(String erro) {	
		LogNotificacaoManager logWebHookManager = ComponentUtil.getComponent(LogNotificacaoManager.class);

		if(logNotificacao == null) {
			return;
		}
		try {
			logNotificacao = logWebHookManager.findById(logNotificacao.getId());
			
			logNotificacao.setSucesso(false);
			logNotificacao.setMensagemErro(erro);
			logWebHookManager.persistAndFlush(logNotificacao);
		} catch (PJeBusinessException e) {
			log.error(NAO_POSSIVEL_GRAVAR_LOG_NOTIFICACAO,e);
		}		
	}

	/**
	 * @param message WebhookWrapperMessage
	 * @return True se o grau da origem da requisição for o mesmo da instalação atual.
	 */
	protected boolean isGrauSistemaIgualGrauRequisicao(WebhookWrapperMessage message) {
		
		if(message.getOrigemAcao() == null || Optional.ofNullable(message.getOrigemAcao().getIdentificadorGrauJurisdicao()).orElse(0) == 0) {
			return true;
		}
		String grauRequisicao = Objects.toString(message.getOrigemAcao().getIdentificadorGrauJurisdicao(), null) ;
		String grauPJe = ParametroUtil.instance().getInstancia();
		return StringUtils.equals(grauRequisicao, grauPJe);
	}

	protected boolean isNumeracaoProcessualPertecenteAoTribunalLocal(WebhookWrapperMessage message) {
		String numeroProcesso = message.getNumeroUnicoProcesso();
		if (!NumeroProcessoUtil.numeroProcessoValido(numeroProcesso)) {
			return false;
		}
		String numeroOrgaoJusticaInstalacao = ParametroUtil.instance().getNumeroOrgaoJustica();
		String numeroOrgaoJusticaProcesso = NumeroProcessoUtil.obterNumeroOrgaoJustica(numeroProcesso);
		return numeroOrgaoJusticaInstalacao.equals(numeroOrgaoJusticaProcesso);
	}
}

class PPE implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer idProcessoParteExpediente;

	/**
	 * @return the idProcessoParteExpediente
	 */
	public Integer getIdProcessoParteExpediente() {
		return idProcessoParteExpediente;
	}

	/**
	 * @param idProcessoParteExpediente the idProcessoParteExpediente to set
	 */
	public void setIdProcessoParteExpediente(Integer idProcessoParteExpediente) {
		this.idProcessoParteExpediente = idProcessoParteExpediente;
	}
}