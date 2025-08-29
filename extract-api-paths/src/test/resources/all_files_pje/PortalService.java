package br.jus.cnj.pje.nucleo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import br.com.infox.pje.manager.LogNotificacaoManager;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.LogNotificacao;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.infox.cliente.util.HttpUtil;
import br.com.infox.cliente.util.JSONUtil;
import br.jus.cnj.intercomunicacao.v223.beans.Assinatura;
import br.jus.cnj.intercomunicacao.v223.beans.DataHora;
import br.jus.cnj.intercomunicacao.v223.beans.DocumentoProcessual;
import br.jus.cnj.intercomunicacao.v223.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v223.beans.NumeroUnico;
import br.jus.cnj.intercomunicacao.v223.beans.Parametro;
import br.jus.cnj.intercomunicacao.v223.beans.RespostaManifestacaoProcessual;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.v223.servico.IntercomunicacaoBase;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PjeRestClientException;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.client.RepositorioRestClient;
import br.jus.cnj.pje.webservice.client.keycloak.SsoTokenRestClient;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.dto.portal.DocumentoDTO;
import br.jus.pje.nucleo.dto.portal.MensagemDTO;
import br.jus.pje.nucleo.dto.portal.PeticaoIntegracaoDTO;
import br.jus.pje.nucleo.dto.portal.RespostaTribunalDTO;
import br.jus.pje.nucleo.dto.portal.TipoDocumentoDTO;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.util.DateUtil;

@Name(PortalService.NAME)
@Scope(ScopeType.EVENT)
public class PortalService {
	public static final String NAME = "portalService";
	
	@Logger
	private Log log;

	@In(create=true)
	private SsoTokenRestClient ssoTokenRestClient;

	private static final String NAO_POSSIVEL_GRAVAR_LOG_NOTIFICACAO="Não foi possível gravar log de notificação";

	@Observer(Eventos.EVENTO_PORTAL_PETICAO_RECEBIDA)
	public void onPeticaoRecebida(MensagemDTO mensagem, String numeroProcesso) {
		try {
			if(validarProcesso(mensagem, numeroProcesso)) {
				//Retorna o DTO da petição, conforme o protocolo recebido
				PeticaoIntegracaoDTO peticao = getPeticao(mensagem);
				//Carrega os documentos binários
				Map<String, byte[]> binarios = loadDocumentosBinarios(peticao);
				//Converte para ManifestacaoProcessual
				ManifestacaoProcessual manifestacao = peticaoToManifestacaoProcessual(peticao, binarios);
				//Entrega a manifestação processual e recebe a resposta
				RespostaManifestacaoProcessual res = entregarManifestacaoProcessual(manifestacao);

				if (!res.isSucesso()) {
					throw new PJeBusinessException(res.getMensagem());
				}

				//Converte a resposta da manifestação para o DTO de callback do portal
				RespostaTribunalDTO respostaTribunal = respostaManifestacaoToRespostaTribunal(res, mensagem.getProtocolo());
				//Transforma o conteúdo da resposta tribunal em uma string json
				String json = new ObjectMapper().writeValueAsString(respostaTribunal);
				//Chama a URL de callback da mensagem
				enviarCallback(mensagem.getCallback(), json);
				//Atualiza status do LogNotificacao
				registrarSucessoNoLogNotificacao(mensagem);
			}
		}
		catch(Exception e) {
			log.error(e);
			enviarErroCallback(mensagem, e.getLocalizedMessage());
			registrarErroNoLogNotificacao(mensagem, e);
		}
	}

	public void registrarErroNoLogNotificacao(MensagemDTO mensagem, Exception e) {
		registrarLogNotificacao(mensagem, Boolean.FALSE, e);
	}

	private void registrarSucessoNoLogNotificacao(MensagemDTO mensagem) {
		registrarLogNotificacao(mensagem, Boolean.TRUE, null);
	}

	private void registrarLogNotificacao(MensagemDTO mensagem, Boolean status, Exception e) {
		try {
			String notificacaoId = mensagem.getNotificacaoId();

			LogNotificacaoManager logNotificacaoManager = ComponentUtil.getComponent(LogNotificacaoManager.class);
			LogNotificacao logNotificacao = logNotificacaoManager.findLastByNotificacaoId(notificacaoId);

			if (logNotificacao == null) {
				String erro = String.format("Log não encontrado para o id: %s", notificacaoId);
				log.error(erro, notificacaoId);
				return;
			}

			logNotificacao.setSucesso(status);
			if (e != null) {
				logNotificacao.setMensagemErro(extrairRootCauseStackTraceException(e));
			}

			logNotificacaoManager.persistAndFlush(logNotificacao);
		} catch (PJeBusinessException pJeBusinessException) {
			log.error(NAO_POSSIVEL_GRAVAR_LOG_NOTIFICACAO, pJeBusinessException);
		}
	}

	private String extrairRootCauseStackTraceException(Exception e) {
		StringBuilder rootCauseStackTrace = new StringBuilder();
		String[] stackTraceList = ExceptionUtils.getRootCauseStackTrace(e);

		for (String traceLine : stackTraceList) {
			rootCauseStackTrace.append(traceLine).append("\n");
		}
		return rootCauseStackTrace.toString();
	}

	private boolean validarProcesso(MensagemDTO mensagem, String numeroProcesso) throws PJeBusinessException {
		boolean processoValido = Boolean.TRUE;
		ProcessoJudicialManager processoJudicialManager = (ProcessoJudicialManager) Component.getInstance(ProcessoJudicialManager.class); 
		List<ProcessoTrf> ptf = processoJudicialManager.findByNU(numeroProcesso);
		if(ptf == null || ptf.isEmpty()) {
			processoValido = Boolean.FALSE;
			String msg = "[RespostaWebhookSuprimida] Processo " + numeroProcesso + " não encontrado";
			log.error(msg);
			registrarErroNoLogNotificacao(mensagem, new PJeBusinessException(msg));
		} else if (ptf.get(0).getInBloqueioMigracao()) {
			processoValido = Boolean.FALSE;
			String msg = "[RespostaWebhookSuprimida] Processo " + numeroProcesso + " bloqueado (migrado).";
			log.error(msg);
			registrarErroNoLogNotificacao(mensagem, new PJeBusinessException(msg));
		}

		return processoValido;
	}
	
	private String getToken() throws PjeRestClientException {
		return ssoTokenRestClient.getTokenSso();
	}
	
	public void enviarErroCallback(MensagemDTO mensagem, String erro) {
		try {
			RespostaTribunalDTO respostaTribunal = new RespostaTribunalDTO();
			respostaTribunal.setSucesso(Boolean.FALSE);
			respostaTribunal.setProtocoloPortal(mensagem.getProtocolo());
			respostaTribunal.setDataHora(new Date());
			respostaTribunal.getErros().add(erro);

			String json = new ObjectMapper().writeValueAsString(respostaTribunal);
			enviarCallback(mensagem.getCallback(), json);
		}
		catch(Exception e) {
			log.error("Erro ao enviar callback:" + e.getLocalizedMessage());
		}
	}
	
	private void enviarCallback(String url, String conteudo) throws PjeRestClientException {
		HttpUtil.postJSONContent(url, getToken(), conteudo);
	}
	
	private RespostaManifestacaoProcessual entregarManifestacaoProcessual(ManifestacaoProcessual manifestacao) {
		IntercomunicacaoBase intercomunicacao = (IntercomunicacaoBase) Component.getInstance(IntercomunicacaoBase.class); 
		return intercomunicacao.entregarManifestacaoProcessual(manifestacao);
	}
	
	private PeticaoIntegracaoDTO getPeticao(MensagemDTO mensagem) throws PjeRestClientException {
		JSONObject peticaoJson = HttpUtil.getJSONContent(mensagem.getUrl(), getToken());

		if (peticaoJson == null) {
			throw new PjeRestClientException("Falha ao recuperar protocolo de peticao: " + mensagem.getUrl());
		}
		return gerarPeticao(peticaoJson);
	}
	
	private ManifestacaoProcessual peticaoToManifestacaoProcessual(PeticaoIntegracaoDTO peticao, Map<String, byte[]> documentosBinarios) {
		ManifestacaoProcessual manifestacao = criarManifestacaoProcessual(peticao);
		manifestacao.getDocumento().addAll(criarDocumentosProcessuais(peticao, documentosBinarios));
		return manifestacao;
	}

	private RespostaTribunalDTO respostaManifestacaoToRespostaTribunal(RespostaManifestacaoProcessual respostaManifestacao, String protocoloPortal) {
		RespostaTribunalDTO dto = new RespostaTribunalDTO();
		dto.setProtocoloPortal(protocoloPortal);
		dto.setSucesso(respostaManifestacao.isSetSucesso());
		if (respostaManifestacao.getDataOperacao() != null) {
			dto.setDataHora(DateUtil.stringToDate(respostaManifestacao.getDataOperacao().getValue(), "yyyy/MM/dd HH:mm:ss"));
		}
		dto.setProtocolo(respostaManifestacao.getProtocoloRecebimento());
		if(!respostaManifestacao.isSucesso()) {
			dto.getErros().add(respostaManifestacao.getMensagem());
		}
		return dto;
	}
	
	private ManifestacaoProcessual criarManifestacaoProcessual(PeticaoIntegracaoDTO peticao) {
		ManifestacaoProcessual manifestacao = new ManifestacaoProcessual();
		NumeroUnico numeroProcesso = new NumeroUnico();
		DataHora dataEnvio = new DataHora();
		dataEnvio.setValue(DateUtil.dateToString(peticao.getDataEnvio()));
		numeroProcesso.setValue(peticao.getNumeroProcesso());
		manifestacao.setNumeroProcesso(numeroProcesso);
		manifestacao.setDataEnvio(dataEnvio);
		return manifestacao;
	}

	
	private List<DocumentoProcessual> criarDocumentosProcessuais(PeticaoIntegracaoDTO peticao, Map<String, byte[]> documentosBinarios) {
	    List<DocumentoDTO> documentosOrdenados = peticao.getDocumentos().stream()
	            .sorted(Comparator.comparing(DocumentoDTO::getOrdem))
	            .collect(Collectors.toList());

	    DocumentoProcessual documentoPrincipal = convert(documentosOrdenados.get(0), documentosBinarios, peticao.getCpfPeticionante());
	    
	    if(documentosOrdenados.size() > 1) {
	    	List<DocumentoProcessual> documentosVinculados = convertDocumentosVinculados(peticao, documentosBinarios, documentosOrdenados.subList(1, documentosOrdenados.size()));
	    	documentoPrincipal.getDocumentoVinculado().addAll(documentosVinculados);
	    }

	    return CollectionUtilsPje.toList(documentoPrincipal);
	}

	private List<DocumentoProcessual> convertDocumentosVinculados(PeticaoIntegracaoDTO peticao, Map<String, byte[]> documentosBinarios, List<DocumentoDTO> documentos) {
	    return documentos.stream()
	            .map(documento -> convert(documento, documentosBinarios, peticao.getCpfPeticionante()))
	            .collect(Collectors.toList());
	}

	private DocumentoProcessual convert(DocumentoDTO documento, Map<String, byte[]> documentosBinarios, String cpfPeticionante) {
	    DocumentoProcessual documentoProcessual = new DocumentoProcessual();
	    documentoProcessual.setIdDocumento(documento.getId().toString());
	    documentoProcessual.setIdDocumentoVinculado(
	            documento.getIdDocumentoPai() == null ? null : documento.getIdDocumentoPai().toString()
	    );
	    documentoProcessual.setTipoDocumento(documento.getTipoDocumento().getCodigo());
	    documentoProcessual.setDataHora(criarDataHora(documento.getDataHora()));
	    documentoProcessual.setDescricao(documento.getDescricao());
	    documentoProcessual.setNivelSigilo(documento.getNivelSigilo());
	    documentoProcessual.setMimetype(documento.getMimeType());
	    documentoProcessual.setHash(documento.getHash());
	    documentoProcessual.setConteudo(getDataHandler(documento, documentosBinarios));
	    documentoProcessual.getAssinatura().addAll(criarAssinaturas(documento));
	    documentoProcessual.getOutroParametro().addAll(criarParametros(documento, cpfPeticionante));

	    return documentoProcessual;
	}

	private DataHora criarDataHora(Date data) {
	    DataHora dataHora = new DataHora();
	    dataHora.setValue(DateUtil.dateToString(data, "yyyyMMddHHmmss"));
	    return dataHora;
	}

	private List<Parametro> criarParametros(DocumentoDTO documento, String cpfPeticionante) {
	    return CollectionUtilsPje.toList(
	            criarParametro(MNIParametro.PARAM_NUMERO_ORDEM, String.valueOf(documento.getOrdem())),
	            criarParametro(MNIParametro.PARAM_DATA_INCLUSAO, String.valueOf(documento.getDataHora().getTime())),
	            criarParametro(MNIParametro.PARAM_DATA_JUNTADA, String.valueOf(documento.getDataHora().getTime())),
	            criarParametro(MNIParametro.PARAM_DOCUMENTO_IDENTIFICACAO_USUARIO_JUNTADA_ARQUIVO, cpfPeticionante)
	    );
	}

	private Parametro criarParametro(String nome, String valor) {
	    Parametro parametro = new Parametro();
	    parametro.setNome(nome);
	    parametro.setValor(valor);
	    return parametro;
	}

	
	
	private List<Assinatura> criarAssinaturas(DocumentoDTO documento) {
		List<Assinatura> assinaturas = new ArrayList<>();
		Assinatura assinatura = new Assinatura();
		assinatura.setCodificacaoCertificado("PkiPath");
		assinatura.setAssinatura(documento.getAssinatura());
		assinatura.setCadeiaCertificado(documento.getCadeiaCertificado());
		DataHora dataAssinatura = new DataHora();
		dataAssinatura.setValue(DateUtil.dateToString(documento.getDataHora(), "yyyyMMddHHmmss"));
		assinatura.setDataAssinatura(dataAssinatura);
		assinaturas.add(assinatura);
		return assinaturas;
	}
	
	private DataHandler getDataHandler(DocumentoDTO documento, Map<String, byte[]> documentosBinarios) {
		DataHandler dataHandler = null;
		String hash = documento.getHash();
		Set<String> keys = documentosBinarios.keySet();
		for (String key : keys) {
			if(key.equals(hash)) {
				byte[] value = documentosBinarios.get(key);
				ByteArrayDataSource dataSource = new ByteArrayDataSource(value,  "application/octet-stream");
				dataHandler = new DataHandler(dataSource);
				break;
			}
		}
		return dataHandler;
	}
	
	private Map<String, byte[]> loadDocumentosBinarios(PeticaoIntegracaoDTO peticao) {
		Map<String, byte[]> documentosBinarios = new HashMap<>();
		List<DocumentoDTO> documentos = peticao.getDocumentos();
		for (DocumentoDTO documento : documentos) {
			String hash = documento.getHash();
			RepositorioRestClient repositorio = (RepositorioRestClient) Component.getInstance(RepositorioRestClient.class);
			byte[] file = repositorio.getFile("portal", hash);
			documentosBinarios.put(hash, file);
		}
		return documentosBinarios;
	}

	private PeticaoIntegracaoDTO gerarPeticao(JSONObject peticaoJson) {
		String dataEnvioStr = JSONUtil.getJsonString(peticaoJson, "dataEnvio");
		Date dataEnvio = dataEnvioStr == null ? null : DateUtil.stringToDate(dataEnvioStr, "dd/MM/yyyy HH:mm:ss");
		PeticaoIntegracaoDTO peticao = new PeticaoIntegracaoDTO();
		peticao.setProtocolo(JSONUtil.getJsonString(peticaoJson, "protocolo"));
		peticao.setNumeroProcesso(JSONUtil.getJsonString(peticaoJson, "numeroProcesso"));
		peticao.setIdAvisoExpediente(JSONUtil.getJsonLong(peticaoJson, "idAvisoExpediente"));
		peticao.setDataEnvio(dataEnvio);
		peticao.setCpfPeticionante(JSONUtil.getJsonString(peticaoJson, "cpfPeticionante"));
		peticao.setNomePeticionante(JSONUtil.getJsonString(peticaoJson, "nomePeticionante"));
		peticao.setDocumentos(gerarDocumentos(peticaoJson.getJSONArray("documentos")));
		return peticao;
	}

	private List<DocumentoDTO> gerarDocumentos(JSONArray documentosJson) {
		List<DocumentoDTO> documentos = new ArrayList<>();
		for (int i = 0; i < documentosJson.length(); i++) {
			JSONObject documentoJson = documentosJson.getJSONObject(i);
			DocumentoDTO documentoDTO = gerarDocumento(documentoJson);
			documentos.add(documentoDTO);
		}
		return documentos;
	}

	private DocumentoDTO gerarDocumento(JSONObject documentoJson) {
		DocumentoDTO documento = new DocumentoDTO();
		TipoDocumentoDTO tipo = new TipoDocumentoDTO();
		JSONObject tipoDocumentoJson = JSONUtil.getJsonObject(documentoJson, "tipoDocumento");
		if(tipoDocumentoJson != null) {
			tipo.setCodigo(JSONUtil.getJsonString(tipoDocumentoJson, "codigo"));
			tipo.setDescricao(JSONUtil.getJsonString(tipoDocumentoJson, "descricao"));
		}
		String dataHoraStr = JSONUtil.getJsonString(documentoJson, "dataHora");
		Date dataHora = dataHoraStr == null ? null : DateUtil.stringToDate(dataHoraStr, "dd/MM/yyyy HH:mm:ss");
		documento.setTipoDocumento(tipo);
		documento.setId(JSONUtil.getJsonLong(documentoJson, "id"));
		documento.setIdDocumentoPai(JSONUtil.getJsonLong(documentoJson, "idDocumentoPai"));
		documento.setDataHora(dataHora);
		documento.setDescricao(JSONUtil.getJsonString(documentoJson, "descricao"));
		documento.setMimeType(JSONUtil.getJsonString(documentoJson, "mimeType"));
		documento.setPrincipal(JSONUtil.getJsonBoolean(documentoJson, "principal"));
		documento.setNivelSigilo(JSONUtil.getJsonInteger(documentoJson, "nivelSigilo"));
		documento.setOrdem(JSONUtil.getJsonInteger(documentoJson, "ordem"));
		documento.setHash(JSONUtil.getJsonString(documentoJson, "hash"));
		documento.setAssinatura(JSONUtil.getJsonString(documentoJson, "assinatura"));
		documento.setCadeiaCertificado(JSONUtil.getJsonString(documentoJson, "cadeiaCertificado"));
		return documento;
	}
}
