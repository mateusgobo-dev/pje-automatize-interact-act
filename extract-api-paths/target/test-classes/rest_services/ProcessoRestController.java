package br.jus.cnj.pje.webservice.mobile.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.infox.cliente.home.SessaoProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.SituacaoProcessoManager;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ListProcessoCompletoBetaDAO;
import br.jus.cnj.pje.editor.lool.LibreOfficeManager;
import br.jus.cnj.pje.extensao.AssinadorA1;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.auxiliar.ResultadoAssinatura;
import br.jus.cnj.pje.list.ResultadoSentencaParteList;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ListProcessoCompletoBetaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoManager;
import br.jus.cnj.pje.nucleo.service.FluxoService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.view.ListProcessoCompletoBetaAction;
import br.jus.cnj.pje.view.fluxo.ElaborarAcordaoAction;
import br.jus.cnj.pje.vo.AcordaoCompilacao;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CabecalhoProcesso;
import br.jus.csjt.pje.business.pdf.GeradorPdfUnificado;
import br.jus.pje.nucleo.dto.AutoProcessualDTO;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioMobile;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.nucleo.util.Utf8ParaIso88591Util;

@Name(ProcessoRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("pje-legacy/api/v1/mobile/processos")
public class ProcessoRestController extends AbstractRestController {
	
	public static final String NAME = "processoRestController";
	private static final String CODE = "code";
	private static final String STATUS = "status";
	private static final String ERROR = "error";
	public static final String MESSAGES = "messages";
	
	@Logger
	private Log log;
	
	@In(create = true, required = false)
	private AssinadorA1 assinadorA1;
	
	@GET
	@Path("/tarefas-assinatura/{idLocalizacao}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarProcessos(@PathParam("idLocalizacao") Integer idLocalizacao) throws JSONException {
		
		JSONObject jsonOut = new JSONObject();
		
		try {
			autenticarUsuarioToken(idLocalizacao);
			
			ProcessoJudicialManager processoJudicialManager = ProcessoJudicialManager.instance();
			
			List<CabecalhoProcesso> processosMetadados = processoJudicialManager.recuperarMetadadosProcessoParaAssinaturaMobile(getUsuarioSesssao());
			
			jsonOut.put("status", "ok");
			jsonOut.put("code", 200);
			jsonOut.put("data", processosMetadados);
		} catch (Exception e) {
			e.printStackTrace();
			jsonOut.put("status", "error");
			jsonOut.put("code", 500);
			jsonOut.put("messages", new String[]{e.getMessage()});
		} finally {
			Identity.instance().logout();
		}
		
		return Response.status(200).entity(jsonOut.toString()).build();
		
	}
	
	@GET
	@Path("/documentos/{taskId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarDocumentos(@PathParam("taskId") Long taskId) throws JSONException {
		
		JSONObject jsonOut = new JSONObject();
		
		try {
			autenticarUsuarioToken();
			
			Contexts.getSessionContext().set(ResultadoSentencaParteList.NAME, new ResultadoSentencaParteList());
			Contexts.getSessionContext().set(SessaoProcessoDocumentoHome.NAME, new SessaoProcessoDocumentoHome());
			
			TaskInstanceHome.instance().setTaskId(taskId);
			
			TramitacaoProcessualService tramitacaoProcessualService = ComponentUtil.getComponent("tramitacaoProcessualService");
			
			JSONObject data = new JSONObject();
			
			JSONArray documentosEdicao = new JSONArray();
			
			ProcessoTrf processoTrf = TaskInstanceUtil.instance().getProcesso(TaskInstanceUtil.instance().getProcessInstance().getId());
			
			if ( tramitacaoProcessualService.contemVariavel("frame:WEB-INF_xhtml_flx_elaborarAcordao") ) {
				
				ElaborarAcordaoAction action = ComponentUtil.getComponent(ElaborarAcordaoAction.NAME);
				
				ProcessoDocumento pd = action.getAcordao();
				JSONObject jsonDoc = new JSONObject();
				jsonDoc.put("id", pd.getIdProcessoDocumento());
				jsonDoc.put("tipo", pd.getTipoProcessoDocumento().getTipoProcessoDocumento());
				jsonDoc.put("conteudo", pd.getProcessoDocumentoBin().getModeloDocumento());
				jsonDoc.put("podeEditar", false);
				documentosEdicao.put(jsonDoc);
			} else {
				
				ProcessoHome.instance().setId(processoTrf.getIdProcessoTrf());
				ProcessoDocumento pd = ProcessoHome.instance().getProtocolarDocumentoBean().getDocumentoPrincipal();
				JSONObject jsonDoc = new JSONObject();
				jsonDoc.put("id", pd.getIdProcessoDocumento());
				jsonDoc.put("tipo", pd.getTipoProcessoDocumento().getTipoProcessoDocumento());
				
				if ( pd.getProcessoDocumentoBin().isBinario() && pd.getProcessoDocumentoBin().getNomeDocumentoWopi()!=null ) {
					jsonDoc.put("html", false);
					LibreOfficeManager loolManager = new LibreOfficeManager(pd.getProcessoDocumentoBin().getNomeDocumentoWopi());
					jsonDoc.put("url", loolManager.getUrlLool());
					jsonDoc.put("conteudo", Base64.getEncoder().encodeToString(IOUtils.toByteArray(loolManager.getPDFContent())));
				} else {
					jsonDoc.put("conteudo", pd.getProcessoDocumentoBin().getModeloDocumento());
					jsonDoc.put("html", true);
				}
				
				jsonDoc.put("podeEditar", true);
				documentosEdicao.put(jsonDoc);
				
			}
			
			JSONArray documentosVisualizacao = new JSONArray();
			
			ListProcessoCompletoBetaManager listProcessoCompletoBetaManager = ComponentUtil.getComponent(ListProcessoCompletoBetaManager.NAME);
			List<AutoProcessualDTO> autos = listProcessoCompletoBetaManager.recuperarAutos(processoTrf.getIdProcessoTrf(), true, false, null);
			ListProcessoCompletoBetaAction action = new ListProcessoCompletoBetaAction();
			for (AutoProcessualDTO auto: autos) {
				ProcessoDocumento pd = auto.getDocumento();
				JSONObject jsonDoc = new JSONObject();
				jsonDoc.put("id", pd.getIdProcessoDocumento());
				jsonDoc.put("descricao", action.processarNomeDocumento(pd));
				jsonDoc.put("data", pd.getDataJuntada().getTime());
				documentosVisualizacao.put(jsonDoc);
			}
			
			data.put("documentos-edicao", documentosEdicao);
			data.put("documentos-visualizacao", documentosVisualizacao);
			
			jsonOut.put("status", "ok");
			jsonOut.put("code", 200);
			jsonOut.put("data", data);
		} catch (Exception e) {
			e.printStackTrace();
			jsonOut.put("status", "error");
			jsonOut.put("code", 500);
			jsonOut.put("messages", new String[]{e.getMessage()});
		} finally {
			Identity.instance().logout();
		}
		
		return Response.status(200).entity(jsonOut.toString()).build();
		
	}
	
	private byte[] getConteudoBinario(ProcessoDocumento processoDocumento) throws FileNotFoundException, IOException {
		ComponentUtil.getComponent(ListProcessoCompletoBetaManager.class).recuperarConteudoBinario(processoDocumento.getProcessoDocumentoBin());
		return IOUtils.toByteArray( new FileInputStream(processoDocumento.getProcessoDocumentoBin().getFile()));
	}

	@GET
	@Path("/documentos/gerar-pdf/{id}")
	@Produces("application/pdf")
	public Response gerarPDf(@PathParam("id") Integer id) {
		
		
		try {
			autenticarUsuarioToken();
			
			DocumentoJudicialService documentoJudicialService = ComponentUtil.getDocumentoJudicialService();
			
			ProcessoDocumento pd = documentoJudicialService.getDocumento(id);
			
			File f = File.createTempFile(pd.getTipoProcessoDocumento().getTipoProcessoDocumento(), "pdf");
			FileOutputStream fos = new FileOutputStream(f);
			String resourcePath = getResourcePath();
			GeradorPdfUnificado geradorPdf = new GeradorPdfUnificado();
			geradorPdf.setResurcePath(resourcePath);
			geradorPdf.setGerarIndiceDosDocumentos(true);
			geradorPdf.gerarPdfUnificado(pd.getProcessoTrf(), Arrays.asList(pd), fos);
			fos.close();
			
			return Response.ok().entity(f).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		} finally {
			Identity.instance().logout();
		}
		
		
		
	}
	
	@PUT
	@Path("/documentos/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response salvarDocumento(@PathParam("id") Integer id, String inputJson) throws JSONException {
		
		JSONObject jsonOut = new JSONObject();
		
		
		
		Util.beginTransaction();
		
		try {
			autenticarUsuarioToken();
			
			JSONObject jsonIn = new JSONObject(inputJson);
			String conteudo = jsonIn.getString("conteudo");
			
			DocumentoJudicialService documentoJudicialService = ComponentUtil.getDocumentoJudicialService();
			
			ProcessoDocumento pd = documentoJudicialService.getDocumento(id);
			
			if ( pd.getProcessoDocumentoBin().isBinario() && pd.getProcessoDocumentoBin().getNomeDocumentoWopi()!=null ) {
				LibreOfficeManager loolManager = new LibreOfficeManager(pd.getProcessoDocumentoBin().getNomeDocumentoWopi());
				InputStream pdf = loolManager.getPDFContent();
				int size = pdf.available();
				File arquivoBinario = loolManager.salvarPDFTemp(pdf);
				pd.getProcessoDocumentoBin().setFile(arquivoBinario);
				pd.getProcessoDocumentoBin().setSize(size);
				jsonOut.put("pdf", Base64.getEncoder().encodeToString(IOUtils.toByteArray(new FileInputStream(arquivoBinario))));
			} else {
				pd.getProcessoDocumentoBin().setModeloDocumento(Utf8ParaIso88591Util.converter(conteudo));
			}
			
			
			documentoJudicialService.persist(pd, true);
			documentoJudicialService.flush();
			
			Util.commitTransction();
			
			jsonOut.put("status", "ok");
			jsonOut.put("code", 200);
			
		} catch (Exception e) {
			Util.rollbackTransaction();
			e.printStackTrace();
			jsonOut.put("status", "error");
			jsonOut.put("code", 500);
			jsonOut.put("messages", new String[]{e.getMessage()});
		} finally {
			Identity.instance().logout();
		}
		
		return Response.status(200).entity(jsonOut.toString()).build();
		
	}
	
	@POST
	@Path("/documentos/assinar/{taskId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response assinarDocumento(@PathParam("taskId") Long taskId) throws JSONException {
		
		JSONObject jsonOut = new JSONObject();
		
		Util.beginTransaction();
		
		try {
			autenticarUsuarioToken(this.getIdLocalizacaoUsuario(taskId));
			
			Contexts.getSessionContext().set(ResultadoSentencaParteList.NAME, new ResultadoSentencaParteList());
			
			TaskInstanceHome.instance().setTaskId(taskId);
			
			TramitacaoProcessualService tramitacaoProcessualService = ComponentUtil.getComponent("tramitacaoProcessualService");
			
			if ( tramitacaoProcessualService.contemVariavel("frame:WEB-INF_xhtml_flx_elaborarAcordao") ) {
				
				Contexts.getSessionContext().set(SessaoProcessoDocumentoHome.NAME, new SessaoProcessoDocumentoHome());
				
				ElaborarAcordaoAction action = ComponentUtil.getComponent(ElaborarAcordaoAction.NAME);
				
				AcordaoCompilacao acordaoCompilacao = action.getAcordaoCompilacao();
				
				List<ProcessoDocumento> documentosAssinatura = acordaoCompilacao.getProcessoDocumentosParaAssinatura();
				
				for (ProcessoDocumento documento: documentosAssinatura) {
					ArquivoAssinadoHash arquivoAssinadoHash = assinarDocumento(documento, taskId);
					action.getArquivosAssinados().add(arquivoAssinadoHash);
				}
				
				ComponentUtil.getDocumentoJudicialService().gravarAssinaturaDeProcessoDocumento(action.getArquivosAssinados(), acordaoCompilacao.getProcessoDocumentosParaAssinatura());
				
				action.concluirJulgamentoExterno();
				
			} else {
				DocumentoJudicialService documentoJudicialService = ComponentUtil.getDocumentoJudicialService();
				
				
				ProcessoTrf processoTrf = TaskInstanceUtil.instance().getProcesso(TaskInstanceUtil.instance().getProcessInstance().getId());
				ProcessoHome.instance().setId(processoTrf.getIdProcessoTrf());
				ProcessoDocumento pd = ProcessoHome.instance().getProtocolarDocumentoBean().getDocumentoPrincipal();
				
				ArquivoAssinadoHash arquivoAssinadoHash = assinarDocumento(pd, taskId);
				
				LibreOfficeManager libreOfficeManager = null;
				if ( pd.getProcessoDocumentoBin().isBinario() && pd.getProcessoDocumentoBin().getNomeDocumentoWopi()!=null ) {
					libreOfficeManager = new LibreOfficeManager(pd.getProcessoDocumentoBin().getNomeDocumentoWopi());
					pd.getProcessoDocumentoBin().setModeloDocumento( libreOfficeManager.getHtmlContent() );
					pd.getProcessoDocumentoBin().setNomeDocumentoWopi(null);
					ComponentUtil.getComponent(ListProcessoCompletoBetaDAO.class).recuperarConteudoBinario(pd.getProcessoDocumentoBin());
					
				}
				
				pd.setIdJbpmTask(taskId);
				if(taskId != null && taskId > 0) {
					pd.setExclusivoAtividadeEspecifica(Boolean.TRUE);
				}

				documentoJudicialService.gravarAssinatura(
						pd.getIdProcessoDocumento()+"", arquivoAssinadoHash.getCodIni(), arquivoAssinadoHash.getHash(), 
						arquivoAssinadoHash.getAssinatura(), arquivoAssinadoHash.getCadeiaCertificado(),
						Authenticator.getPessoaLogada());
				documentoJudicialService.finalizaDocumento(pd, pd.getProcessoTrf(), null, true, false);
				documentoJudicialService.flush();
				Boolean finalizado = ComponentUtil.getComponent(FluxoService.class).finalizarTarefa(taskId, true, null);
				if (!finalizado) {
					throw new Exception("A tarefa n�o foi transitada");
				} else if (libreOfficeManager!=null) {
					libreOfficeManager.apagarDocumento();
				}
				
			}
			
			Util.commitTransction();
			
			jsonOut.put("status", "ok");
			jsonOut.put("code", 200);
		} catch (Exception e) {
			Util.rollbackTransaction();
			processarExcecao(jsonOut, e);
		} finally {
			Identity.instance().logout();
		}
		
		return Response.status(200).entity(jsonOut.toString()).build();
	}
	
	private void processarExcecao(JSONObject jsonOut, Exception e) {
		log.error(e);
		jsonOut.put(STATUS, ERROR);
		jsonOut.put(CODE, 500);
		jsonOut.put(MESSAGES, new String[]{e.getMessage() != null ? e.getMessage() : e.getLocalizedMessage()});
	}
	
	private ArquivoAssinadoHash assinarDocumento(ProcessoDocumento processoDocumento, Long idTarefa) throws PJeBusinessException, IOException, PontoExtensaoException {
		DocumentoJudicialService documentoJudicialService = ComponentUtil.getDocumentoJudicialService();
		documentoJudicialService.validaPendenciaMovimentacaoParaAssinatura(processoDocumento, idTarefa);
		String hash = null;
		if ( processoDocumento.getProcessoDocumentoBin().isBinario() ) {
			hash = Crypto.encodeMD5( getConteudoBinario(processoDocumento) );
		} else {
			hash = Crypto.encodeMD5(processoDocumento.getProcessoDocumentoBin().getModeloDocumento());
		}
		ResultadoAssinatura res = assinadorA1.assinarHash(hash);
		
		
		ArquivoAssinadoHash arquivoAssinadoHash = new ArquivoAssinadoHash();
		arquivoAssinadoHash.setAssinatura(res.getAssinatura());
		arquivoAssinadoHash.setCadeiaCertificado(res.getCadeiaCertificado());
		arquivoAssinadoHash.setId( processoDocumento.getIdProcessoDocumento()+"" );

		return arquivoAssinadoHash;
	}
	

	private Integer getIdLocalizacaoUsuario(Long taskId) throws Exception {
		if (taskId == null) {
			return null;
		}
		SituacaoProcessoManager situacaoManager = ComponentUtil.getComponent(SituacaoProcessoManager.class);
		Long idLocalizacao = situacaoManager.getByIdTaskInstance(taskId).getIdLocalizacao();

		UsuarioMobile usuarioMobile = validarJwt();
		UsuarioLocalizacaoManager usuLocManager = ComponentUtil.getComponent(UsuarioLocalizacaoManager.class);
		List<UsuarioLocalizacao> localizacoesAtuais = usuLocManager.getLocalizacoesAtuais(usuarioMobile.getUsuario()).stream().filter(l -> l.getLocalizacaoFisica().getIdLocalizacao() == idLocalizacao).collect(Collectors.toList());
		Papel papelMagistrado = ParametroUtil.instance().getPapelMagistrado();
		boolean isMagistrado = localizacoesAtuais.parallelStream().anyMatch(l -> l.getPapel().equals(papelMagistrado));
		
		Predicate<UsuarioLocalizacao> condicao = isMagistrado ? l -> l.getPapel().equals(papelMagistrado) : l -> !l.getPapel().equals(papelMagistrado);
		return localizacoesAtuais.stream()
				.filter(condicao)
				.map(UsuarioLocalizacao::getIdUsuarioLocalizacao)
				.findAny()
				.orElse(null);
	}
	
}
