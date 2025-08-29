package br.jus.cnj.pje.nucleo.view;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.codehaus.jettison.json.JSONArray;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.entidades.vo.ConsultaDocumentoIndexadoVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ControleVersaoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.OrgaoJulgadorService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.servicos.EditorEstiloService;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.je.pje.business.dto.TipoVotoDTO;
import br.jus.pje.jt.entidades.ControleVersaoDocumento;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;


/**
 * Classe abstrata com a implementação de referencia para os casos onde o editor
 * irá gerar um processo documento.
 */

public abstract class CkEditorGeraDocumentoAbstractAction implements ICkEditorGeraDocumentoController {

	private ProtocolarDocumentoBean protocolarDocumentoBean;

	private TipoProcessoDocumento tipoProcessoDocumento;

	@In
	private TramitacaoProcessualService tramitacaoProcessualService;

	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;

	@In
	private DocumentoJudicialService documentoJudicialService;

	@Logger
	private Log logger;

	@In
	private FacesMessages facesMessages;

	@In(create = true)
	private ModeloDocumentoManager modeloDocumentoManager;

	@In(create = true)
	private EditorEstiloService editorEstiloService;

	@In(create = true)
	private OrgaoJulgadorService orgaoJulgadorService;
	
	@In(create = true)
	private ProcessoDocumentoBinManager processoDocumentoBinManager;
	
	@In(create = true)
	private PessoaFisicaManager pessoaFisicaManager; 
	
	@In(create = true)
	private ControleVersaoDocumentoManager controleVersaoDocumentoManager;
	
	@In(create = true)
	private UsuarioService usuarioService;
	
	@In(required=false)
	private TaskInstance taskInstance;
	
	private ProcessInstance processInstance;

	private List<ModeloDocumento> listaModelosDocumentoPorTipoDocumento = new ArrayList<ModeloDocumento>();
	
	private int limit;
	private int offset;
	
	private String abaSelecionada;

	private int idTipoVotoSelecionado = 0;
	
	private List<Localizacao> localizacoes;
	
	private Map<String, List<ModeloDocumento>> mapaModelosDocumentos = new HashMap<String, List<ModeloDocumento>>();
    
    public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
    
    public int getIdTipoVotoSelecionado() {
		return idTipoVotoSelecionado;
	}

	public void setIdTipoVotoSelecionado(int idTipoVotoSelecionado) {
		this.idTipoVotoSelecionado = idTipoVotoSelecionado;
	}
	
	public String getEstilosFormatacao() {
		return editorEstiloService.recuperarEstilosJSON();
	}
	
	public boolean isTipoProcessoDocumentoDefinido(){
		boolean result = Boolean.TRUE;

		if(getTipoProcessoDocumento() == null){
			result = Boolean.FALSE;
		}

		return result;
	}

	public boolean isDocumentoPersistido() {
		if (protocolarDocumentoBean.getDocumentoPrincipal() != null
				&& protocolarDocumentoBean.getDocumentoPrincipal().getIdProcessoDocumento() > 0) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public void selecionarTipoProcessoDocumento(String tipoDocumentoString) {
		TipoProcessoDocumento tpd = tipoProcessoDocumentoManager.findByDescricaoTipoDocumento(tipoDocumentoString);
		this.setTipoProcessoDocumento(tpd);
	}

	public String getTiposDocumentosDisponiveis() {
		JSONArray retorno = new JSONArray();
		
		List<TipoProcessoDocumento> tiposDocumentosDisponiveis = documentoJudicialService.getTiposDocumentoMinuta();
		for (TipoProcessoDocumento tpd : tiposDocumentosDisponiveis) {
			retorno.put(tpd.getTipoProcessoDocumento());			
		}
		
		return retorno.toString();
	}

	public Integer[] obterIdsModeloDocumentoFluxo() {
		Integer[] idsModelos = null;
		String idsModeloDocumentoFluxo = (String) tramitacaoProcessualService
				.recuperaVariavelTarefa(Variaveis.VARIAVEL_IDS_MODELOS_DOCUMENTOS_FLUXO);
		if (StringUtils.isNotBlank(idsModeloDocumentoFluxo)) {
			idsModelos = Util.converterStringIdsToIntegerArray(idsModeloDocumentoFluxo);
		}
		return idsModelos;
	}

	public String getModelosPorTipoDocumentoSelecionado() {
		JSONArray arrayJSON = new JSONArray();
		
		this.carregarMapaModelosDocumentos();

		Integer[] idsModelosFluxo = obterIdsModeloDocumentoFluxo();
		for (ModeloDocumento modeloDocumento : listaModelosDocumentoPorTipoDocumento) {
			if(idsModelosFluxo == null || Arrays.asList(idsModelosFluxo).contains(modeloDocumento.getIdModeloDocumento())) {
				arrayJSON.put(modeloDocumento.getTituloModeloDocumento());
			}
		}

		return arrayJSON.toString();
	}
	
	private void carregarMapaModelosDocumentos() {
		TipoProcessoDocumento tipoProcessoDocumento = this.getTipoProcessoDocumento();
		String key = tipoProcessoDocumento.getIdTipoProcessoDocumento().toString() + Authenticator.getIdLocalizacaoFisicaAtual();
		if(mapaModelosDocumentos.containsKey(key)) {
			listaModelosDocumentoPorTipoDocumento = mapaModelosDocumentos.get(key);
		} else {
			try {
				listaModelosDocumentoPorTipoDocumento = this.modeloDocumentoManager.getModelos(tipoProcessoDocumento, getLocalizacoes(), "");
				mapaModelosDocumentos.put(key, this.listaModelosDocumentoPorTipoDocumento);
			} catch (Exception e) {
				this.facesMessages.clear();
				this.facesMessages.add(Severity.ERROR, "Erro durante a pesquisa de modelos de documento!");
				logger.error(e.getMessage());
			}
		}
	}
	
	/**
	 * Carrega o modelo de documento selecionado na suggestion box de pesquisa no editor de texto
	 * @param modelo
	 */
	public void setModeloDocumentoSelecionado(ModeloDocumento modelo){
		getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().setModeloDocumento(modeloDocumentoManager.obtemConteudo(modelo));
	}
	
	/**
	 * Método que define o modelo de documento de acordo com sua descrição ou titulo.
	 * @param modeloDocumentoString
	 */
	public void selecionarModeloProcessoDocumento(String modeloDocumentoString){
		if(modeloDocumentoString == null || modeloDocumentoString.equals("")){
			this.setModeloDocumentoSelecionado(null);
			return;
		}
		this.carregarMapaModelosDocumentos();
		Integer[] idsModelosFluxo = obterIdsModeloDocumentoFluxo();
		for (ModeloDocumento modeloDocumento : listaModelosDocumentoPorTipoDocumento) {
			if(modeloDocumento.getTituloModeloDocumento().equals(modeloDocumentoString)
					&& (idsModelosFluxo == null || Arrays.asList(idsModelosFluxo).contains(modeloDocumento.getIdModeloDocumento()))) {
				this.setModeloDocumentoSelecionado(modeloDocumento);
				break;
			}
		}
	}
	
	public String getNomeTipoDocumentoPrincipal(){
		if(getProtocolarDocumentoBean() != null 
				&& getProtocolarDocumentoBean().getDocumentoPrincipal() != null
				&& getProtocolarDocumentoBean().getDocumentoPrincipal().getTipoProcessoDocumento() != null){
			return getProtocolarDocumentoBean().getDocumentoPrincipal().getTipoProcessoDocumento().getTipoProcessoDocumento();
		}
		return "";
	}
	
	public String consultarDocumentosIndexados(String filtros){
		JSONObject retorno = new JSONObject();
		try {
			JSONObject filtrosJSON = new JSONObject(filtros);
			ConsultaDocumentoIndexadoVO consultaDocumentoIndexadoVO = new ConsultaDocumentoIndexadoVO(filtrosJSON);
			consultaDocumentoIndexadoVO.setLocalizacaoUsuario(Authenticator.getLocalizacaoAtual().getCaminho());
			retorno.put("resposta", documentoJudicialService.consultarDocumentosIndexados(consultaDocumentoIndexadoVO, false));
			retorno.put("sucesso", Boolean.TRUE);
		} catch (PJeBusinessException e) {
			logger.error(e.getMessage());
			try {
				retorno.put("sucesso", Boolean.FALSE);
				retorno.put("mensagem", e.getLocalizedMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} catch (JSONException e) {
			logger.error(e.getMessage());
			try {
				retorno.put("sucesso", Boolean.FALSE);
				retorno.put("mensagem", e.getLocalizedMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
			try {
				retorno.put("sucesso", Boolean.FALSE);
				retorno.put("mensagem", "Falha ao transformar datas : " + e.getLocalizedMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return retorno.toString();
	}
	
	public String consultarOrgaosJulgadores(String filtro){
		JSONObject retorno = new JSONObject();
		try {
			JSONArray localizacoesJSON = new JSONArray();
			if(filtro != null && !filtro.trim().isEmpty()){
				List<OrgaoJulgador> orgaosJulgadores = orgaoJulgadorService.obterOrgaosJulgadoresPorDescricao(filtro);
				for (OrgaoJulgador local : orgaosJulgadores) {
					localizacoesJSON.put(local.getOrgaoJulgador());
				}
			}
			retorno.put("sucesso", Boolean.TRUE);
			retorno.put("resposta", localizacoesJSON);
		} catch (PJeBusinessException | JSONException e) {
			logger.error(e.getMessage());
			try {
				retorno.put("sucesso", Boolean.FALSE);
				retorno.put("mensagem", e.getLocalizedMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return retorno.toString();
	}
	
	public String consultarTipoDocumento(String filtro){
		JSONObject retorno = new JSONObject();
		JSONArray tiposDocumentoJSON = new JSONArray();
		try {
			if(filtro != null && !filtro.trim().isEmpty()){
				List<TipoProcessoDocumento> tiposDocumento = documentoJudicialService.consultarTipoDocumento(filtro);
				for (TipoProcessoDocumento tpd : tiposDocumento) {
					tiposDocumentoJSON.put(tpd.getTipoProcessoDocumento());
				}
			}
			retorno.put("sucesso", Boolean.TRUE);
			retorno.put("resposta", tiposDocumentoJSON);
		} catch (Exception e) {
			logger.error(e.getMessage());
			try {
				retorno.put("sucesso", Boolean.FALSE);
				retorno.put("mensagem", e.getLocalizedMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return retorno.toString();
	}
	
	public String consultarPessoaAutorDocumento(String filtro){
		JSONObject retorno = new JSONObject();
		JSONArray pessoasFisicasJSON = new JSONArray();
		if(filtro != null && !filtro.trim().isEmpty()){
			PessoaFisica pessoaFisica = pessoaFisicaManager.getPessoaFisicaByNome(filtro);
			if (pessoaFisica != null) {
				pessoasFisicasJSON.put(pessoaFisica.getNome());
			}
		}
		try {
			retorno.put("sucesso", Boolean.TRUE);
			retorno.put("resposta", pessoasFisicasJSON);
		} catch (JSONException e) {
			e.printStackTrace();
			logger.error(e.getLocalizedMessage());
		}
		return retorno.toString();
	}
	
	/**
	 * Operação que extrai html de um documento binário.
	 * 
	 * @param arquivo
	 * 
	 * @return String referente ao conteúdo html.
	 * @throws PJeBusinessException 
	 */
	private String extrairHtmlDoPdf(byte[] arquivo){
		String retorno = "";
		ToXMLContentHandler handler = new ToXMLContentHandler();
		AutoDetectParser parser = new AutoDetectParser();
	    Metadata metadata = new Metadata();
    	InputStream stream;
		try {
			stream = new ByteArrayInputStream(arquivo);
			parser.parse(stream, handler, metadata);
			retorno = handler.toString();
		} catch (Exception e) {
			logger.error(e);
		}
		return retorno;
	}
	
	public String consultarConteudoDocumento(Integer idDocumento){
		JSONObject retorno = new JSONObject();
		try {
			ProcessoDocumentoBin documento = processoDocumentoBinManager.findById(idDocumento);
			String sDoc = "";
			if (documento!=null) {
				if (documento.getBinario() && "application/pdf".equals(documento.getExtensao())) {
					byte[] bin = processoDocumentoBinManager.getBinaryData(documento);
					sDoc = extrairHtmlDoPdf(bin);
				} else {
					sDoc = documento.getModeloDocumento();
				}
			}
			retorno.put("resposta", sDoc);
			retorno.put("sucesso", Boolean.TRUE);
		} catch (PJeBusinessException | JSONException e) {
			try {
				retorno.put("sucesso", Boolean.FALSE);
				retorno.put("mensagem", "Falha ao recuperar documento : " + e.getLocalizedMessage());
			} catch (JSONException e1) {
				logger.error(e,"Falha ao recuperar documento : " + e.getLocalizedMessage());
				e1.printStackTrace();
			}
		}
		return retorno.toString();
	}
	
	public Boolean isConsultaDocumentosIndexadosHabilitada() {
		return this.documentoJudicialService.isConsultaDocumentosIndexadosHabilitada();
	}
	
	public String obterVersoesDocumentoJSON() {
		try {
			return controleVersaoDocumentoManager.obterVersoesDocumentoJSON(getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		return "";
	}

	public void aplicarVersaoDocumento(int versaoDocumento) {
		try {
			ControleVersaoDocumento controleVersaoDocumento = controleVersaoDocumentoManager.obterVersaoDocumento(versaoDocumento, getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin());
			getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().setModeloDocumento(controleVersaoDocumento.getConteudo());
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR,"Erro ao aplicar a versão documento");
		}

	}
	
	public void inicializarPaginacao() {
		setLimit(10);
		setOffset(0);
	}
	
	/**
	 * Faz a paginação
	 */
	protected void paginarProximasControleVersaoDocumento() {
		setOffset(getOffset() + getLimit());
	}
	
	public String obterVersoesDocumentoJSONProximas() {
		try {
			String resposta = controleVersaoDocumentoManager.obterVersoesDocumentoJSONPaginada(getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin(), getLimit(), getOffset());
			paginarProximasControleVersaoDocumento();
			return resposta;
		} catch (PJeBusinessException e) {
			logger.error(e.getMessage());
		}
		
		return "";
	}
	
	public TipoVotoDTO criarTipoVotoDTO(TipoVoto tipoVoto) {
		TipoVotoDTO tipoVotoDTO = new TipoVotoDTO();
		
		tipoVotoDTO.setIdTipoVoto(tipoVoto.getIdTipoVoto());
		tipoVotoDTO.setTipoVoto(tipoVoto.getTipoVoto());
		tipoVotoDTO.setTextoCertidao(tipoVoto.getTextoCertidao());
		tipoVotoDTO.setRelator(tipoVoto.getRelator());
		tipoVotoDTO.setContexto(tipoVoto.getContexto());
		tipoVotoDTO.setAtivo(tipoVoto.getAtivo());
		tipoVotoDTO.setCor(tipoVoto.getCor());
		
		return tipoVotoDTO;
	}
	
	public List<TipoVotoDTO> criarListaTiposVoto(List<TipoVoto> listTipoVoto) {
		List<TipoVotoDTO> listTipoVotoDTO = new ArrayList<TipoVotoDTO>();
		
		for(TipoVoto tv : listTipoVoto) {
			listTipoVotoDTO.add(criarTipoVotoDTO(tv));
		}
		
		return listTipoVotoDTO;
	}
	
	public void selecionarTipoVoto(String idTipoVoto) {
		setIdTipoVotoSelecionado(Integer.parseInt(idTipoVoto));
	}
	
	public void limparVotoSelecionado() {
		if(getIdTipoVotoSelecionado() != 0) {
			setIdTipoVotoSelecionado(0);
		}
	}
	
	public String verificarPluginTipoVoto() {
		JSONObject retorno = new JSONObject();
		
		try {
			if(getProtocolarDocumentoBean() != null && getProtocolarDocumentoBean().getDocumentoPrincipal() != null && getProtocolarDocumentoBean().getDocumentoPrincipal().getTipoProcessoDocumento() != null){
					retorno.put("sucesso", getProtocolarDocumentoBean().getDocumentoPrincipal().getTipoProcessoDocumento().equals(ParametroUtil.instance().getTipoProcessoDocumentoVoto()));
			} else {
				retorno.put("sucesso", Boolean.FALSE);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return retorno.toString();
	}
	
	public String getAbasWidget() {
		return null;
	}
	
	public List<ModeloDocumento> getListaModelosDocumentoPorTipoDocumento() {
		return listaModelosDocumentoPorTipoDocumento;
	}

	public void setListaModelosDocumentoPorTipoDocumento(List<ModeloDocumento> listaModelosDocumentoPorTipoDocumento) {
		this.listaModelosDocumentoPorTipoDocumento = listaModelosDocumentoPorTipoDocumento;
	}

	public TipoProcessoDocumento getTipoProcessoDocumento() {
		
		if(isDocumentoPersistido() 
				&& protocolarDocumentoBean.getDocumentoPrincipal().getIdProcessoDocumento() > 0
				&& this.tipoProcessoDocumento == null){
			try {
				this.tipoProcessoDocumento =  tipoProcessoDocumentoManager.findById(protocolarDocumentoBean.getDocumentoPrincipal().getTipoProcessoDocumento().getIdTipoProcessoDocumento());
			} catch (PJeBusinessException e) {
				logger.error(Severity.WARN,"Não foi possível recuperar o tipo de documento para o documento persistido em questão!");
				facesMessages.add(Severity.ERROR,"Não foi possível recuperar o tipo de documento para o documento persistido em questão!");
				logger.error(e.getMessage());
			}
		}
		
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}

	public void setProtocolarDocumentoBean(ProtocolarDocumentoBean protocolarDocumentoBean) {
		this.protocolarDocumentoBean = protocolarDocumentoBean;
	}
	
	public String getAbaSelecionada() {
		return abaSelecionada;
	}

	public void setAbaSelecionada(String abaSelecionada) {
		this.abaSelecionada = abaSelecionada;
	}

	public String getDownloadLinks(){
		return getProtocolarDocumentoBean().getDownloadLinks();
	}
	
	@Override
	public boolean isFormularioPreenchido() {
		return true;
	}
	
	public TaskInstance getTaskInstance() {
		return taskInstance;
	}

	public void setTaskInstance(TaskInstance taskInstance) {
		this.taskInstance = taskInstance;
	}

	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}
	
	@Override
	public void selecionarAnexosConcluido() {
	}

	public List<Localizacao> getLocalizacoes() throws PJeBusinessException {
		if(localizacoes == null || localizacoes.isEmpty()) {
			localizacoes = this.modeloDocumentoManager.getLocalizacoes();
		}
		return localizacoes;
	}
}
