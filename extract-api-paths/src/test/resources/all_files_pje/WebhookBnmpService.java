package br.jus.pdpj.notificacao.service;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.MimetypeUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.EventoDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.PecaDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.SigiloDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.TipoDTO;
import br.jus.cnj.pje.webservice.client.bnmp.PecaMinBnmpRestClient;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pdpj.commons.models.dtos.webhooks.WebhookWrapperMessage;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.util.PJEHolder;
import br.jus.pje.nucleo.util.StringUtil;

@Name("webhookBnmpService")
@Path("pdpjwebhook")
public class WebhookBnmpService extends WebhookWrapperService{


	private static final String VAR_PJE_FLUXO_BNMP_MAGISTRADO = "pje:fluxo:bnmp:magistrado";

	private static final String VAR_PJE_FLUXO_BNMP_TIPO_PECA = "pje:fluxo:bnmp:tipoPeca";
	
	private static final String VAR_PJE_FLUXO_BNMP_NUMERO_PECA = "pje:fluxo:bnmp:numeroPeca";

	private static final String VAR_PJE_FLUXO_BNMP_ID_PECA = "pje:fluxo:bnmp:idPeca";

	private static final String VAR_PJE_FLUXO_BNMP_ID_TIPO_PROCESSO_DOCUMENTO = "pje:fluxo:bnmp:idTipoProcessoDocumento";

	private static final String VAR_PJE_FLUXO_BNMP_ID_PROCESSO_DOCUMENTO = "pje:fluxo:bnmp:idProcessoDocumento";

	private static final String VAR_PJE_FLUXO_BNMP_MODELO_EVENTO = "pje:fluxo:bnmp:modeloEvento";

	private static final int CONST_STATUS_PENDENTE_ASSINATURA = 4;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;

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
	
	private Logger log = Logger.getLogger(WebhookBnmpService.class);


	@SuppressWarnings("resource")
	@POST
	@Path("/bnmp/documento")
	public Response pecaExpedida(WebhookWrapperMessage message) {
		Response response = null;

		try {
			response = validarRequisicao(message);
			if (response.getStatus() == 200) {
				PJEHolder.setWebhookAction(Boolean.TRUE);
				Util.beginTransaction();
				
				DocumentoBNMPMinDTO docBNMP = obterDocumentoBNMP(message);
								
				if(docBNMP.getStatusPeca() != null && docBNMP.getStatusPeca().getId() == CONST_STATUS_PENDENTE_ASSINATURA) {
					throw new PJeBusinessException("Peça pendente de assinatura!");		
				}
				
				ProcessoTrf processo = obterProcesso(message.getNumeroUnicoProcesso());
				
				if(processo.getInBloqueiaPeticao() || Boolean.TRUE.equals(processo.getInOutraInstancia())) {
					throw new PJeBusinessException("Processo "+ processo.getNumeroProcesso() +" não permite peticionamento. ");		
				}

				TipoProcessoDocumento tipoProcessoDocumento = obterTipoProcessoDocumento(docBNMP);
				
				ProcessoDocumento processoDocumento = criaProcessoDocumentoElancaMovimento(docBNMP, tipoProcessoDocumento, processo);
				if(tipoProcessoDocumento.getFluxo() != null) {
					dispararFluxoPecaExpedida(processo, tipoProcessoDocumento.getFluxo().getCodFluxo(), processoDocumento, docBNMP); 
				}
				registraSucessoNotificacao();
			
				Util.commitTransction();
				response = Response.ok().build();
			}
		} catch (PJeBusinessException ex) {
			realizaTratamentoErro(ex, ex.getLocalizedMessage());
			response = Response.status(Status.BAD_REQUEST).entity(ex.getLocalizedMessage()).build();	
		}  catch (Exception e) {
			realizaTratamentoErro(e, e.getMessage());
			response = Response.serverError().entity(e.getMessage()).build();
		}
		return response;
	}

	private TipoProcessoDocumento obterTipoProcessoDocumento(DocumentoBNMPMinDTO docBNMP) throws PJeBusinessException {
		TipoProcessoDocumento tipoProcessoDocumento = getTipoProcessoDocumento(docBNMP.getTipo(), docBNMP.getParametroTipo());
		
		if(tipoProcessoDocumento == null) {
			throw new PJeBusinessException("Tipo de documento "+ docBNMP.getTipo().getDescricao() +" não mapeado. Verifique as configurações do parâmetro: "+docBNMP.getParametroTipo());
		}
		return tipoProcessoDocumento;
	}

	private DocumentoBNMPMinDTO obterDocumentoBNMP(WebhookWrapperMessage message) {
		DocumentoBNMPMinDTO dto = new DocumentoBNMPMinDTO();
		
		dto.setNomeModeloEvento(message.getModeloEvento().getNome());

		if (dto.getNomeModeloEvento().equalsIgnoreCase("EventoCriado")){
			EventoDTO evento = getObjectMapper()
					.convertValue(message.getPayload().getConteudo(), EventoDTO.class);
			
			dto.setTipo(evento.getTipoEvento());
			dto.setParametroTipo(Parametros.PDPJ_INTEGRACAO_BNMP_DE_PARA_TIPO_DOCUMENTO_EVENTO);
			dto.setNumero(evento.getNumeroEventoFormatado());
			dto.setUrlPdf(evento.getUrlPdf());
			dto.setIdPeca(evento.getId());

		}else{
			PecaDTO pecaExpedida = getObjectMapper()
					.convertValue(message.getPayload().getConteudo(), PecaDTO.class);
			
			dto.setTipo(pecaExpedida.getTipoPeca());
			dto.setParametroTipo(Parametros.PDPJ_INTEGRACAO_BNMP_DE_PARA_TIPO_DOCUMENTO_PECA);
			dto.setSigilo(pecaExpedida.getSigilo());
			dto.setNumero(pecaExpedida.getNumeroPecaFormatado());
			dto.setUrlPdf(pecaExpedida.getUrlPdf());
			dto.setIdPeca(pecaExpedida.getId());
			dto.setMagistrado(pecaExpedida.getAssinaturaMagistrado());
			dto.setStatusPeca(pecaExpedida.getStatus());

		}			
		return dto;
	}

	private void dispararFluxoPecaExpedida(ProcessoTrf processo, String codigoFluxo, ProcessoDocumento processoDocumento, DocumentoBNMPMinDTO  docBNMP)
			throws PJeBusinessException {
		Map<String, Object> variaveis = new HashMap<>();
		
		variaveis.put(VAR_PJE_FLUXO_BNMP_ID_PROCESSO_DOCUMENTO, processoDocumento.getIdProcessoDocumento());
		variaveis.put(VAR_PJE_FLUXO_BNMP_ID_TIPO_PROCESSO_DOCUMENTO, processoDocumento.getTipoProcessoDocumento().getIdTipoProcessoDocumento());

		variaveis.put(VAR_PJE_FLUXO_BNMP_ID_PECA, docBNMP.getIdPeca());
		variaveis.put(VAR_PJE_FLUXO_BNMP_TIPO_PECA, docBNMP.getTipo());
		variaveis.put(VAR_PJE_FLUXO_BNMP_NUMERO_PECA, docBNMP.getNumero());
		
		variaveis.put(VAR_PJE_FLUXO_BNMP_MODELO_EVENTO, docBNMP.getNomeModeloEvento());
		
		if (docBNMP.getMagistrado() != null) {
			variaveis.put(VAR_PJE_FLUXO_BNMP_MAGISTRADO, docBNMP.getMagistrado().getUsuario());
		}
		
		ComponentUtil.getProcessoJudicialService().incluirNovoFluxo(processo, codigoFluxo, variaveis);
	}

	private void realizaTratamentoErro(Exception e, String msgErro) {
		Util.rollbackTransaction();
		registraFalhaNotificacao(msgErro);
		log.error(e);
	}

	private ProcessoTrf obterProcesso(String numeroUnicoProcesso) throws PJeBusinessException {
		List<ProcessoTrf> ptf = processoJudicialManager.findByNU(numeroUnicoProcesso);
		
		if(ptf == null || ptf.size() != 1) {
			throw new PJeBusinessException("Processo "+ numeroUnicoProcesso +" não encontrado. ");		
		}
		return ptf.get(0);
	}	

	private ProcessoDocumento criaProcessoDocumentoElancaMovimento(DocumentoBNMPMinDTO docBNMP, TipoProcessoDocumento tipoProcessoDocumento, ProcessoTrf processo) throws PJeException {
		Integer idDoc = documentoJudicialService.gerarMinuta(processo.getIdProcessoTrf(), null, null, tipoProcessoDocumento.getIdTipoProcessoDocumento(), null);
		
		ProcessoDocumento processoDocumento = processoDocumentoManager.findById(idDoc);	
		processoDocumento.setDocumentoSigiloso(isSigiloso(docBNMP.getSigilo(), tipoProcessoDocumento));
		processoDocumento.setNumeroDocumento(docBNMP.getNumero());
		
		ProcessoDocumentoBin processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
		PecaMinBnmpRestClient pecaMinBnmpRestClient = ComponentUtil.getComponent(PecaMinBnmpRestClient.class);
		
		File pecaPdf = pecaMinBnmpRestClient.downloadPeca(docBNMP.getUrlPdf());
		
		processoDocumentoBin.setFile(pecaPdf);
		processoDocumentoBin.setBinario(true);
		processoDocumentoBin.setExtensao(MimetypeUtil.getMimetypePdf());
		processoDocumentoBin.setModeloDocumento(null);
		processoDocumentoBin.setMd5Documento(documentoJudicialService.obterMD5(processoDocumentoBin));
		
		// Inserindo o binário no storage			
		processoDocumentoManager.persist(processoDocumento);
		processoDocumento.setProcessoDocumentoBin(processoDocumentoBin);

		processoDocumentoBinManager.persistAndFlush(processoDocumentoBin);
		
		documentoJudicialService.juntarDocumento(processoDocumento.getIdProcessoDocumento(), null);
		
		lancaMovimento(processo, processoDocumento);	

		return processoDocumento;
	}
	

	private void lancaMovimento(ProcessoTrf processo, ProcessoDocumento processoDocumento) {
		MovimentoAutomaticoService.preencherMovimento().
		deCodigo(CodigoMovimentoNacional.COD_MOVIMENTO_JUNTADA_DOCUMENTO).
		comProximoComplementoVazio().
		doTipoLivre().
		preencherComTexto(processoDocumento.getTipoProcessoDocumento().getTipoProcessoDocumento().toLowerCase()).
		associarAoProcesso(processo).
		associarAoDocumento(processoDocumento).
		lancarMovimento();
	}

	private boolean isSigiloso(SigiloDTO sigilo, TipoProcessoDocumento tipoProcessoDocumento) {
		String listaTiposSigilosos = ParametroUtil.getParametro(Parametros.PDPJ_INTEGRACAO_BNMP_TIPOS_DOCUMENTOS_SIGILOSOS_POR_PADRAO);
		
		return (sigilo != null && sigilo.getId() > 1) || (listaTiposSigilosos != null && br.com.infox.cliente.Util.listaContem(listaTiposSigilosos, tipoProcessoDocumento.getCodigoDocumento()));
	}	
    
	private TipoProcessoDocumento getTipoProcessoDocumento(TipoDTO tipo, String parametro) {
		if(tipo == null) {
			return null;
		}		
		String cdTipoDocumento = getMapaTipoDocumento(parametro).get(tipo.getId());		
		
		if(StringUtil.isNotEmpty(cdTipoDocumento)) {
			try {
				return ComponentUtil.getTipoProcessoDocumentoManager().findByCodigoDocumento(cdTipoDocumento, true);
			} catch (PJeBusinessException e) {
				log.error("Erro ao carregar tipo de documento do BNMP. Erro: "+e.getMessage());
			}
		}
		return null;
	}
	
	private Map<Integer, String> getMapaTipoDocumento(String parametro) {
		String deParaTipoDoc = ParametroUtil.getParametro(parametro);
		
	    if (StringUtil.isEmpty(deParaTipoDoc)) {
	        return Collections.emptyMap(); 
	    }
	    
		final Map<Integer, String> mapTipoDocumento = new HashMap<>();
		if(deParaTipoDoc != null) {
			List<String> tiposDocList = Arrays.asList(deParaTipoDoc.split(","));
	
			for(String linha : tiposDocList) {
				String[] tiposDoc = linha.split("=");
				try {
					Integer idBnmp	= Integer.parseInt(tiposDoc[0]);
					mapTipoDocumento.put(idBnmp, tiposDoc[1]);
				}catch (Exception e) {
					log.error("Erro ao popular mapa de documentos. ",e);
				}
			}
		}
		return mapTipoDocumento;
	}
	
}
