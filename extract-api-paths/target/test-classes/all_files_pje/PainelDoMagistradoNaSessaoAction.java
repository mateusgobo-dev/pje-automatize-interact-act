package br.jus.cnj.pje.view;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.home.SessaoProcessoDocumentoVotoHome;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.manager.SessaoJulgamentoManager;
import br.jus.cnj.pje.nucleo.view.CkEditorGeraDocumentoAbstractAction;
import br.jus.cnj.pje.permissao.VisibilidadeValidador;
import br.jus.cnj.pje.permissao.VisibilidadeValidadorEnum;
import br.jus.cnj.pje.util.SituacaoDocumentoSessao;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.business.pdf.GeradorPdfUnificado;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.je.pje.business.dto.RespostaDTO;
import br.jus.je.pje.business.dto.RespostaTiposVotoDTO;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrfSemFiltro;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.NotaSessaoJulgamento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoLido;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.SessaoProcessoMultDocsVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Translate;
import java.net.URI;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;


@Name(PainelDoMagistradoNaSessaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PainelDoMagistradoNaSessaoAction extends CkEditorGeraDocumentoAbstractAction implements ArquivoAssinadoUploader {
	
	private static final String RELATORIO = "RELATORIO";
	private static final String EMENTA = "EMENTA";
	private static final String VOTO = "VOTO";
	private static final String NAO_VOTANTE = "Não votante";
	private static final String SEU_VOTO_NAO_FOI_PROFERIDO = "Seu voto não foi proferido";
	private static final String TIPO_VOTO_ACOMPANHO_RELATOR = "Acompanho o Relator";

	public static final String  NAME  = "painelDoMagistradoNaSessaoAction";
	
	private int idSessao = 0;
	private String quantidadeProcessos = null;
	private String quantidadeProcessosJulgados = null;
	private String quantidadeProcessosEmJulgamento = null;
	private String quantidadeProcessosComPedidoVista = null;
	private String quantidadeProcessosAdiados = null;
	private String quantidadeProcessosRetiradosJulgamento = null;
	private Sessao sessao =  null;
	private List<SessaoPautaProcessoTrf> listaPautaProcessoTrf;
	private SessaoPautaProcessoTrf sessaoPautaProcessoTrfSelecionado = null;
	private OrgaoJulgador orgaoRelator;
	private SessaoProcessoDocumentoVoto voto;
	private Map<String, String> icones = new HashMap<String, String>();
	private boolean isExibeEditor;
	private boolean isRedigirVotoVogal;
	private List<SessaoProcessoDocumento> elementosJulgamento;
	private Map<String,SessaoProcessoDocumento> elementosJulgamentoFiltrados;	
	private Map<String, TipoProcessoDocumento> tiposProcessoDocumento;
	private TipoSituacaoPautaEnum tipoSituacaoPautaListagem = null;
	
	// Campos de filtragem //
	
	private Integer numeroOrdem;
	private String numeroProcesso;
	private String campoAssunto;
	private String campoClasse;
	private PrioridadeProcesso prioridade;
	private List<PrioridadeProcesso> prioridades;
	private Date dataInicialDistribuicao;
	private Date dataFinalDistribuicao;
	private String nomeParte;
	private String codigoIMF;
	private String codigoOAB;
	private OrgaoJulgador orgaoFiltro;
	private TipoVoto tipoVotoFiltro;
	private boolean mostrarVoto = false;
	private boolean mostrarAnotacoes = false;
	
	// Fim dos campos de filtragem //
	
	@In
	private FacesContext facesContext;
	
	@Logger
	private Log logger;
	private ArquivoAssinadoHash arquivoAssinado;
	
	public void load(){
		if (isDocumentoPersistido()) {
			int idProcessoTrf = ProcessoJbpmUtil.getProcessoTrf()==null ? 0 : ProcessoJbpmUtil.getProcessoTrf().getIdProcessoTrf();
			if (idProcessoTrf==0)
				idProcessoTrf=getSessaoPautaProcessoTrfSelecionado().getProcessoTrf().getIdProcessoTrf();
			setProtocolarDocumentoBean(new ProtocolarDocumentoBean(idProcessoTrf, ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.RECUPERA_DOCUMENTO_FLUXO, NAME));
		}
	}
	
	@Create
	public void init(){
		logger.info("Inicializando controller do Painel do Magistrado na Sessão...");
		
		carregarIdSessao();
		
		carregarSessaoJulgamento();
		
		carregarQuantitativosResultadoSessao();
		
		carregarListagemProcessosPorSituacaoPauta((this.tipoSituacaoPautaListagem != null)?this.tipoSituacaoPautaListagem.toString():"");
		
		carregarIconesContextoVoto();
		
		carregarTiposDocumentos();
						
		logger.info("Painel do Magistrado na Sessão carregado.");

	}
	
	public void refreshPainelDoMagistrado(){
		carregarQuantitativosResultadoSessao();
		carregarListagemProcessosPorSituacaoPauta("");
		limparCamposFiltro();
	}

	private void carregarTiposDocumentos() {
		tiposProcessoDocumento = new HashMap<String, TipoProcessoDocumento>();
		tiposProcessoDocumento.put(RELATORIO, ParametroUtil.instance().getTipoProcessoDocumentoRelatorio());
		tiposProcessoDocumento.put(EMENTA, ParametroUtil.instance().getTipoProcessoDocumentoEmenta());
		tiposProcessoDocumento.put(VOTO	, ParametroUtil.instance().getTipoProcessoDocumentoVoto());
		tiposProcessoDocumento.putAll(ComponentUtil.getTipoProcessoDocumentoManager().getMapTipoProcessoDocumento(ParametroUtil.instance().getIdsTipoDocumentoVoto()));
	}
	
	/**
	 * Carrega um HashMap contendo como chave o codigo do contexto do tipo de voto e como valor o icone bootstrap (font-awesome) correspondente
	 */
	private void carregarIconesContextoVoto() {
		icones.put("C", "fa-thumbs-o-up pull-left mt-5");
		icones.put("P", "fa-hand-o-up pull-left mt-5");
		icones.put("D", "fa-thumbs-o-down pull-left mt-5");
		icones.put("S", "fa-ban pull-left mt-5");
		icones.put("I", "fa-hand-paper-o pull-left mt-5");
		icones.put("N", "fa-exclamation-triangle pull-left mt-5");
	}

	public void carregarListagemProcessosTodasSituacoes(){
		setListaPautaProcessoTrf(ComponentUtil.getSessaoPautaProcessoTrfManager().getProcessoSessao(getSessao(),TipoSituacaoPautaEnum.values()));
	}
	
	public void carregarListagemProcessosPorSituacaoPauta(String situacao){
		
		if(StringUtil.isEmpty(situacao)){
			setTipoSituacaoPautaListagem(null);
			carregarListagemProcessosTodasSituacoes();
			return;
		}

		setTipoSituacaoPautaListagem(TipoSituacaoPautaEnum.getEnum(situacao));		
		setListaPautaProcessoTrf(ComponentUtil.getSessaoPautaProcessoTrfManager().getProcessoSessao(getSessao(),getTipoSituacaoPautaListagem()));
	}

	public void carregarQuantitativosResultadoSessao() {
		SessaoJulgamentoManager sessaoJulgamentoManager = ComponentUtil.getSessaoJulgamentoManager();
		Integer idSessao = getIdSessao();
		
		quantidadeProcessos = sessaoJulgamentoManager.getProcessosSemJulgamento(idSessao);
		quantidadeProcessosEmJulgamento = sessaoJulgamentoManager.getProcessosEmJulgamento(idSessao);
		quantidadeProcessosJulgados = sessaoJulgamentoManager.getProcessosJulgados(idSessao);
		quantidadeProcessosComPedidoVista = sessaoJulgamentoManager.getVista(idSessao);
		quantidadeProcessosAdiados = sessaoJulgamentoManager.getAdiado(idSessao);
		quantidadeProcessosRetiradosJulgamento = sessaoJulgamentoManager.getRetiradoJulgamento(idSessao);
	}

	private void carregarIdSessao() {
		try {
			HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String sIdSessao = request.getParameter("idsess_");

			if (sIdSessao==null) 
				for (org.apache.http.NameValuePair param: URLEncodedUtils.parse(URI.create(request.getHeader("Referer")), request.getCharacterEncoding()))
					if ("idsess_".equals(param.getName()))
						sIdSessao = param.getValue();
			
			idSessao = Integer.parseInt(sIdSessao);
			
		} catch (NullPointerException | NumberFormatException ex) {
			throw new PJeRuntimeException("Não foi possível carregar o id da sessão!", ex);
		}
		setIdSessao(idSessao);
	}

	private void carregarSessaoJulgamento() {
		try {
			this.sessao = ComponentUtil.getSessaoJulgamentoManager().findById(getIdSessao());
		} catch (PJeBusinessException e) {
			logger.error("Sessão de julgamento não encontrada pelo id especificado: {0}: {1}", getIdSessao(), e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Retorna o voto do relator para a sessao pauta processo.
	 * 
	 * @param processoPautadoSessao
	 * @return Retorna o voto do relator
	 */
	public SessaoProcessoDocumentoVoto getVotoRelator(SessaoPautaProcessoTrf processoPautadoSessao){
		return ComponentUtil.getVotoManager().getVotoRelator(processoPautadoSessao);
	}
	
	/**
	 * Formata os nomes das partes do processo para exibição na listagem de processos no formato "Nome do Autor X Nome do Reu"
	 * 
	 * @param processoPautadoSessao
	 * @return String com o nome das partes formatado
	 */
	
	public String getNomeDasPartesDoProcessoFormatado(SessaoPautaProcessoTrf processoPautadoSessao){
		
		ConsultaProcessoTrfSemFiltro consultaProcesso = processoPautadoSessao.getConsultaProcessoTrf();
		String nomeAutor = ComponentUtil.getProcessoJudicialService().nomeParaExibicao(consultaProcesso.getAutor(), consultaProcesso.getQtAutor()); 
		String nomeReu = ComponentUtil.getProcessoJudicialService().nomeParaExibicao(consultaProcesso.getReu(), consultaProcesso.getQtReu());
		
		return  (nomeAutor +" X "+nomeReu);
	}
	
	/**
	 * Define o objeto SessaoPautaProcessoTrf que foi selecionado pelo magistrado para votação e demais ações de julgamento
	 * 
	 * @param sessaoPautaProcessoTrfId
	 */
	public void carregarSessaoPautaProcessoTrfSelecionado(int sessaoPautaProcessoTrfId){
		try {
			SessaoPautaProcessoTrf sessaoPautaProcessoTrf = ComponentUtil.getSessaoPautaProcessoTrfManager().findById(sessaoPautaProcessoTrfId); 			
			setSessaoPautaProcessoTrfSelecionado(sessaoPautaProcessoTrf);
					
			setExibeEditor(Boolean.FALSE);
			setRedigirVotoVogal(Boolean.FALSE);
			
		} catch (PJeBusinessException e) {
			logger.error("Objeto SessaoPautaProcessoTrf não encontrado para o id especificado: {0}: {1}", sessaoPautaProcessoTrfId, e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	public void carregarDocumentosParaEditarVoto(int idSessaoPautaProcessoTrf){
		setIdSessaoPautaProcessoTrfSelecionado(idSessaoPautaProcessoTrf);
		
		carregarElementosJulgamento();
		
		if(isOrgaoAtualRelator()){
			setRedigirVotoVogal(Boolean.FALSE);
			filtrarDocumentosRelator();
		}else{
			setRedigirVotoVogal(Boolean.TRUE);
			filtrarDocumentosVogal();
		}
		
		definirPrimeiroElementoComoChaveMapElementosFiltrados();
		
		apresentaEscondeEditor();
		
	}

	private void apresentaEscondeEditor() {
		if(isElementosJulgamentoFiltradosCarregados()){
			setExibeEditor(Boolean.TRUE);
		}else{
			setExibeEditor(Boolean.FALSE);
		}
	}

	private boolean isElementosJulgamentoFiltradosCarregados() {
		return getElementosJulgamentoFiltrados() != null && !getElementosJulgamentoFiltrados().isEmpty();
	}
	
	private void filtrarDocumentosRelator(){
		setElementosJulgamentoFiltrados(ComponentUtil.getSessaoProcessoDocumentoManager().filtrarDocumentosRelator(elementosJulgamento, getOrgaoRelator(), tiposProcessoDocumento));
	}
	
	private void filtrarDocumentosVogal(){
		setElementosJulgamentoFiltrados(ComponentUtil.getSessaoProcessoDocumentoManager().filtrarDocumentosVogal(elementosJulgamento, getOrgaoAtual(), tiposProcessoDocumento));
		this.elementosJulgamentoFiltrados.put(VOTO, voto);
	}
	
	public void filtrarDocumentosAgrupadosPeloContextoDosVotosEOrgaoJulgadorAcompanhado(String contexto, int idOrgaoJulgadorAcompanhado){
		try {
			
			if(StringUtil.isEmpty(contexto)){
				return;
			}
			
			setRedigirVotoVogal(Boolean.FALSE);
			
			carregarElementosJulgamento();
			setElementosJulgamentoFiltrados(ComponentUtil.getSessaoProcessoDocumentoManager().filtrarDocumentosAgrupadosPeloContextoDosVotosEOrgaoJulgadorAcompanhado(elementosJulgamento, contexto, idOrgaoJulgadorAcompanhado));
			
			definirPrimeiroElementoComoChaveMapElementosFiltrados();
			
			apresentaEscondeEditor();
			
		} catch (PJeBusinessException e) {
			logger.error("Objeto OrgaoJulgador do relator do processo selecionado não encontrado para o id especificado: {0}",e.getLocalizedMessage());
		}
	}

	private void definirPrimeiroElementoComoChaveMapElementosFiltrados() {
		if(getElementosJulgamentoFiltrados().isEmpty()){ 
			return; 
		}
		
		Object[] chaves = getElementosJulgamentoFiltrados().keySet().toArray();
		setChaveDocumentoFiltradoEmEdicao((String)chaves[0]);
	}
	
	public List<NotaSessaoJulgamento> getAnotacoes() {
		return  ComponentUtil.getNotaSessaoJulgamentoManager().recuperaNotas(this.getSessaoPautaProcessoTrfSelecionado().getSessao(),this.getSessaoPautaProcessoTrfSelecionado().getProcessoTrf());
	}
	
	public void gravarAnotacao(String anotacao){
		try {
			SessaoPautaProcessoTrf sessaoPautaProcessoTrf = getSessaoPautaProcessoTrfSelecionado();
			NotaSessaoJulgamento nota = new NotaSessaoJulgamento();
			nota.setAtivo(true);
			nota.setDataCadastro(new Date());
			nota.setNotaSessaoJulgamento(anotacao);
			nota.setOrgaoJulgador(getOrgaoAtual());
			nota.setProcessoTrf(sessaoPautaProcessoTrf.getProcessoTrf());
			nota.setSessao(sessaoPautaProcessoTrf.getSessao());
			nota.setUsuarioCadastro(Authenticator.getUsuarioLogado());
			ComponentUtil.getNotaSessaoJulgamentoManager().persistAndFlush(nota);
			ComponentUtil.getFacesMessages().add(Severity.INFO, "Anotação incluída com sucesso.");
		} catch (PJeBusinessException e) {
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Não foi possível gravar a anotação.");
			e.printStackTrace();
		}
	}
	
	public void votar(Integer tipoVotoId){
		try {
			
			TipoVoto tipoVoto = ComponentUtil.getTipoVotoManager().findById(tipoVotoId);
			
			if(isOrgaoAtualRelator()){
				throw new PJeBusinessException("Não é possível abrir divergência de relatório próprio");
			}
			
			if(tipoVoto == null){
				throw new PJeBusinessException("Não é possível votar sem indicação do tipo de voto");
			}
			
			if(tipoVoto.getContexto().equals("C")){
				votar(getOrgaoRelator(), tipoVoto);
			}else{
				votar(getOrgaoAtual(), tipoVoto);
			}
			
		} catch (PJeBusinessException e) {
			logger.error("Erro ao proferir o tipo de voto de id {0} do Órgão Julgador {1} : {2}",tipoVotoId,getOrgaoAtual().getOrgaoJulgador(), e.getLocalizedMessage());
			e.printStackTrace();
		}
		
	}
	
	private void votar(OrgaoJulgador acompanhado, TipoVoto tipoVoto) {
		SessaoProcessoDocumentoVoto voto = getVoto();
		voto.setCheckAcompanhaRelator(acompanhado.equals(orgaoRelator));
		voto.setDtVoto(new Date());
		voto.setTipoVoto(tipoVoto);
		voto.setOjAcompanhado(acompanhado);
		voto.setImpedimentoSuspeicao(false);
		voto.setLiberacao(true);
		
		try {
			ComponentUtil.getSessaoProcessoDocumentoManager().persistAndFlush(voto);
			analisarTramitacaoFluxoVotoDerrubado(acompanhado,voto);
		} catch (PJeBusinessException e) {
			logger.error("Erro ao registrar voto do orgão julgador {0} para a sessão de id {1} : {2}",acompanhado.toString(), getIdSessao(), e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	private void acompanharRelator(OrgaoJulgador relator) throws PJeBusinessException{
		if(relator == getOrgaoAtual()){
			throw new PJeBusinessException("Não é possível realizar acompanhamento de si próprio");
		}
		
		votar(relator, ComponentUtil.getTipoVotoManager().recuperaAcompanhaRelator());
	}
	
	public void acompanharVoto(){
		try {
			SessaoProcessoDocumento sessaoProcessoDocumento = getElementosJulgamentoFiltrados().get(getChaveDocumentoFiltradoEmEdicao());
			if(sessaoProcessoDocumento != null && sessaoProcessoDocumento instanceof SessaoProcessoDocumentoVoto){
				
				SessaoProcessoDocumentoVoto votoAcompanhado = (SessaoProcessoDocumentoVoto)sessaoProcessoDocumento;
				
				if(votoAcompanhado.getOrgaoJulgador().equals(getOrgaoRelator())){
					acompanharRelator(getOrgaoRelator());
				}else{
					votar(votoAcompanhado.getOrgaoJulgador(), votoAcompanhado.getTipoVoto());
				}
	        }
		} catch (PJeBusinessException e) {
			logger.error("Erro ao acompanhar o voto! \n {0}", e.getLocalizedMessage());
		}
	}
	
	private void analisarTramitacaoFluxoVotoDerrubado(OrgaoJulgador acompanhado,SessaoProcessoDocumentoVoto voto){
		try {
			ComponentUtil.getDerrubadaVotoManager().analisarTramitacaoFluxoVotoDerrubado(getVoto());
		} catch (PJeBusinessException e) {
			logger.error("Erro ao registrar derrubada de voto do orgão julgador {0} para a sessão de id {1} : {2}",acompanhado.toString(), getIdSessao(), e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	private boolean isTipoVotoProferidoPeloOrgaoAtualParaOProcessoSelecionado(Integer tipoVotoId){
		boolean resultado = Boolean.FALSE;

		try {
			TipoVoto tipoVoto = ComponentUtil.getTipoVotoManager().findById(tipoVotoId);
			
			SessaoProcessoDocumentoVoto votoOrgaoAtual = getVoto();
			
			if(votoOrgaoAtual != null && votoOrgaoAtual.getTipoVoto() != null && votoOrgaoAtual.getTipoVoto().getTipoVoto().equalsIgnoreCase(tipoVoto.getTipoVoto())){
				resultado = Boolean.TRUE;
			}
			
			if(tipoVoto.getTipoVoto().equalsIgnoreCase(TIPO_VOTO_ACOMPANHO_RELATOR) && votoOrgaoAtual.getOjAcompanhado() != getOrgaoAtual() && votoOrgaoAtual.getCheckAcompanhaRelator()){
				resultado = Boolean.TRUE;
			}
			
		} catch (PJeBusinessException e) {
			logger.error("Erro ao verificar o tipo de voto proferido pelo OJ logado! \n {0}", e.getLocalizedMessage());
		}
		
		return resultado;
	}
	
	public String getClassBotaoVoto(Integer tipoVotoId){
		String retorno = null;
		
		if(isTipoVotoProferidoPeloOrgaoAtualParaOProcessoSelecionado(tipoVotoId)){
			retorno = "btn btn-primary btn-block";
		}else{
			retorno = "btn btn-default btn-block";
		}
		
		return retorno;
	}
	
	public List<TipoVoto> recuperaTiposVotosVogais(){
		return ComponentUtil.getTipoVotoManager().tiposVotosVogais();
	}
	
	public List<TipoVoto> recuperaTiposVotosRelator(){
		return ComponentUtil.getTipoVotoManager().tiposVotosRelator();
	}
	
	/**
	 * Recupera o ícone de menu de acordo com o contexto
	 * @param contexto
	 * @return icone do contexto
	 */
	public String recuperaIconeContexto(String contexto){
		String iconeSelecionado = "fa-exclamation";
		if(icones.containsKey(contexto)){
			iconeSelecionado = icones.get(contexto).toString(); 
		}
		
		return iconeSelecionado;
	}
	
	public String getLabelDeclararImpedimentoSuspeicao() {
		return ParametroJtUtil.instance().cnj() ? 
				Messages.instance().get("sessao.impedidoSuspeito") : Messages.instance().get("sessao.declaradoImpedimentoSuspeicao");
	}
	
	public boolean isApresentarVotosEAcoesVogais(){
		boolean resultado = 
				!sessaoPautaProcessoTrfSelecionado.isJulgamentoFinalizado() && isOrgaoAtualParticipaVotacaoProcessoSelecionado() 
				&& !isOrgaoAtualRelator()
				&& (getSessao().getContinua() || getSessao().getIniciar() || ParametroUtil.instance().isSessaoHabilitarAcoesEmVotacaoAntecipada())
				&& (getSessao().getDataExclusao()==null)
				&& (getSessao().getDataRealizacaoSessao()==null);
		return resultado;
	}
	
	public boolean isApresentarVotosEAcoesRelator(){
		boolean resultado = 
				!sessaoPautaProcessoTrfSelecionado.isJulgamentoFinalizado() && isOrgaoAtualParticipaVotacaoProcessoSelecionado() && isOrgaoAtualRelator()
				&& (getSessao().getContinua() || getSessao().getIniciar() || ParametroUtil.instance().isSessaoHabilitarAcoesEmVotacaoAntecipada())
				&& (getSessao().getDataExclusao()==null)
				&& (getSessao().getDataRealizacaoSessao()==null);
		return resultado;
	}

	private void carregarElementosJulgamento(){
		List<SessaoProcessoDocumento> docsOutros = ComponentUtil.getSessaoProcessoDocumentoManager().recuperaElementosJulgamento(getProcessoTrfSelecionado(), getSessao(), getOrgaoAtual(), true, true);
		setElementosJulgamento(docsOutros);
	}
	
	public String getLabelSessao(){
		return (getSessao().getContinua())? getLabelSessaoContinua() : getLabelSessaoNaoContinua();
	}
	
	private String getLabelSessaoNaoContinua(){
		DateFormat dateFormat = new SimpleDateFormat("EEEE dd/MM/yyyy", new Locale("pt", "BR"));
		DateFormat dateFormatHoras = new SimpleDateFormat("HH:mm");
		
		String dataCompleta = (getSessao().getDataSessao() != null)? dateFormat.format(getSessao().getDataSessao()) : "";
		String horas = (getSessao().getMomentoInicio() != null)? dateFormatHoras.format(getSessao().getMomentoInicio()) : "";
		
		return String.format("%s - %s %s", getApelidoOuTipoSessao(), dataCompleta, horas);
	}
	
	private String getLabelSessaoContinua(){
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		
		String dataInicio = (getSessao().getDataSessao() != null)? dateFormat.format(getSessao().getDataSessao()) : "";
		String dataFim = (getSessao().getDataFimSessao() != null)? dateFormat.format(getSessao().getDataFimSessao()) : "";
		
		return String.format("%s - De %s a %s", getApelidoOuTipoSessao() , dataInicio, dataFim);
	}
	
	private String getApelidoOuTipoSessao(){
		return (!StringUtil.isEmpty(getSessao().getApelido()))? getSessao().getApelido() : "Sessão de julgamento " + getSessao().getTipoSessao().getTipoSessao();
	}
	
	//Ações dos vogais
	
	public void enviarPautaPresencial(){
		try {
			ComponentUtil.getSessaoPautaProcessoTrfManager().retirarDePauta(sessaoPautaProcessoTrfSelecionado, getOrgaoAtual());
			getProcessoTrfSelecionado().setPautaVirtual(Boolean.FALSE);
			ComponentUtil.getSessaoPautaProcessoTrfManager().persistAndFlush(sessaoPautaProcessoTrfSelecionado);
		} catch (Exception e) {
			logger.error("Erro ao retirar de julgamento o processo {0}: {1}",getProcessoTrfSelecionado().getNumeroProcesso(), e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	public void pedidoDeVista(){
		try {
			ComponentUtil.getSessaoPautaProcessoTrfManager().registrarPedidoVista(sessaoPautaProcessoTrfSelecionado, getOrgaoAtual(), Authenticator.getOrgaoJulgadorCargoAtual());
		} catch (Exception e) {
			logger.error("Erro ao registrar pedido de vista para o processo {0}: {1}",getProcessoTrfSelecionado().getNumeroProcesso(), e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	public boolean isOrgaoAtualRealizouPedidoVistaProcessoSelecionado(){
		return (this.sessaoPautaProcessoTrfSelecionado.getOrgaoJulgadorPedidoVista().getIdOrgaoJulgador() == getOrgaoAtual().getIdOrgaoJulgador());
	}
	
	public String recuperarOrgaoJulgadorRegistrouPedidoVista(){
		return this.sessaoPautaProcessoTrfSelecionado.getOrgaoJulgadorPedidoVista().getOrgaoJulgador();
	}

	private void recarregarSessaoPautaProcessoTrfSelecionado() throws PJeBusinessException {
		if (sessaoPautaProcessoTrfSelecionado!=null)
			sessaoPautaProcessoTrfSelecionado = ComponentUtil.getSessaoPautaProcessoTrfManager().findById(sessaoPautaProcessoTrfSelecionado.getIdSessaoPautaProcessoTrf());
	}
	
	public void retirarPedidoVista(){
		try {
			ComponentUtil.getSessaoPautaProcessoTrfManager().retiraPedidoVista(sessaoPautaProcessoTrfSelecionado);
		} catch (Exception e) {
			logger.error("Erro ao retirar o pedido de vista para o processo {0}: {1}",getProcessoTrfSelecionado().getNumeroProcesso(), e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	public void inverterImpedimentoOuSuspeicao(){
		try {
			SessaoProcessoDocumentoVoto voto = getVoto();
			if(voto != null) {
				if(voto.getImpedimentoSuspeicao()) {
					ComponentUtil.getSessaoProcessoDocumentoManager().remove(voto);
					ComponentUtil.getSessaoProcessoDocumentoManager().flush();
					setupVoto(null);
				} else {
					if(ComponentUtil.getSessaoPautaProcessoComposicaoManager().verificarImpedimentoSuspeicao(sessaoPautaProcessoTrfSelecionado, getOrgaoAtual())) {
						ComponentUtil.getSessaoPautaProcessoComposicaoManager().atualizarImpedimento(sessaoPautaProcessoTrfSelecionado, getOrgaoAtual(), false);
					} else {
						voto.setImpedimentoSuspeicao(true);
						voto.setOjAcompanhado(getOrgaoAtual());
						voto.setTipoVoto(ComponentUtil.getTipoVotoManager().recuperaImpedido());
						ComponentUtil.getSessaoProcessoDocumentoManager().persistAndFlush(voto);
					}
				}
			} 
		} catch (PJeBusinessException e) {
			logger.error("Erro ao declarar impedimento ou suspeicao para o processo {0}: {1}",getProcessoTrfSelecionado().getNumeroProcesso(), e.getLocalizedMessage());
		}
	}
	
	public void removerVoto(){
		try {
			SessaoProcessoDocumentoVoto voto = getVoto();
			
			if(voto != null && voto.getIdSessaoProcessoDocumento() > 0){

				ProcessoDocumento processoDocumento = voto.getProcessoDocumento();
				
				ajustarAcompanhamentoDeVotoParaProprioVotante(voto);

				ComponentUtil.getDerrubadaVotoManager().analisarTramitacaoFluxoVotoDerrubado(voto);
				
				if(processoDocumento != null){
					br.com.itx.component.Util.beginTransaction();
					try {
						//http://www.pje.jus.br/wiki/index.php/Regras_de_neg%C3%B3cio#RN504
						if (processoDocumento.getDataJuntada()!=null) {
							ComponentUtil.getProcessoDocumentoManager().excluirDocumento(processoDocumento, Authenticator.getUsuarioLogado(), "Magistrado removeu o Voto pelo Painel do Magistrado.");
						} else {
							ComponentUtil.getProcessoDocumentoManager().remove(processoDocumento);
						}
						removerProcessoDocumentoLido(processoDocumento);
						ComponentUtil.getDocumentoJudicialService().removerDadosVinculados(processoDocumento.getIdProcessoDocumento());
					} finally {
						br.com.itx.component.Util.commitAndOpenJoinTransaction(); //Há um problema de transação distribuída que ocorre aqui em removerVoto... o único jeito que consegui fazer esse método funcionar foi fazendo o commit manualmente nesse ponto. O erro que ocorre é do PostgreSQL: "cannot PREPARE a transaction that has operated on temporary table"
					}
				} else {				
					ComponentUtil.getSessaoProcessoDocumentoVotoManager().remove(voto);
				}
				ComponentUtil.getSessaoProcessoDocumentoVotoManager().flush();
				
				setupVoto(null);
			}
		} catch (PJeBusinessException e) {
			logger.error("Erro ao remover voto para o processo {0}: {1}", getProcessoTrfSelecionado().getNumeroProcesso(),e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	private void ajustarAcompanhamentoDeVotoParaProprioVotante(SessaoProcessoDocumentoVoto voto) {
		List<SessaoProcessoDocumentoVoto> votosAcompanhantes = ComponentUtil.getSessaoProcessoDocumentoVotoManager().getVotosAcompanhantes(voto, getOrgaoAtual());
		for(SessaoProcessoDocumentoVoto votoAcompanhante : votosAcompanhantes){
			votoAcompanhante.setOjAcompanhado(votoAcompanhante.getOrgaoJulgador());
			ComponentUtil.getSessaoProcessoDocumentoVotoManager().persist(votoAcompanhante);
		}
	}
	
	/**
 	 * Método responsável por remover a referência do documento em
 	 * {@link ProcessoDocumentoLido} quando o usuário clicar para remover o
 	 * voto.
 	 * 
 	 * @param processoDocumento
 	 *            documento que se deseja excluir
 	 * @throws PJeBusinessException
 	 *             exceção lançada caso não seja possível remover o voto
 	 */
 	private void removerProcessoDocumentoLido(ProcessoDocumento processoDocumento) throws PJeBusinessException {
 		if (processoDocumento != null) {
 			List<ProcessoDocumentoLido> processosDocumentosLidos = ComponentUtil.getProcessoDocumentoLidoManager().listProcessosDocumentosLidos(Arrays.asList(processoDocumento));
 			for (ProcessoDocumentoLido processoDocumentoLido : processosDocumentosLidos) {
 				ComponentUtil.getProcessoDocumentoLidoManager().remove(processoDocumentoLido);
 			}
 		}
 	}
	
	public void redigirVoto(int idSessaoPautaProcessoTrf){
		setIdSessaoPautaProcessoTrfSelecionado(idSessaoPautaProcessoTrf);

		ProcessoDocumento documentoVoto	= getVoto().getProcessoDocumento();
		if(documentoVoto == null){
			br.com.itx.component.Util.beginTransaction();
			try {
				ProcessoTrf processoTrf = getSessaoPautaProcessoTrfSelecionado().getProcessoTrf();
				TipoProcessoDocumento tipoDocumentoVoto = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
				documentoVoto = ComponentUtil.getDocumentoJudicialService().getNovoDocumento(" ");
				documentoVoto.setProcessoDocumento(tipoDocumentoVoto.getTipoProcessoDocumento());
				documentoVoto.setTipoProcessoDocumento(tipoDocumentoVoto);
				documentoVoto.setProcessoTrf(processoTrf);
				documentoVoto.setProcesso(processoTrf.getProcesso());
				ComponentUtil.getDocumentoJudicialService().persist(documentoVoto, true);
				ComponentUtil.getDocumentoJudicialService().flush();
				getVoto().setProcessoDocumento(documentoVoto);					
				ComponentUtil.getSessaoProcessoDocumentoVotoManager().persistAndFlush(getVoto());
				br.com.itx.component.Util.commitAndOpenJoinTransaction(); 

			} catch (PJeBusinessException e) {
				e.printStackTrace();
				br.com.itx.component.Util.rollbackAndOpenJoinTransaction();
			}		
		}
		carregarDocumentosParaEditarVoto(idSessaoPautaProcessoTrf);
	}
	
	//Ações dos Relatores
	
	public void retirarParaReexame(){
		try {
			recarregarSessaoPautaProcessoTrfSelecionado();
			ComponentUtil.getSessaoPautaProcessoTrfManager().retirarParaReexame(sessaoPautaProcessoTrfSelecionado);
			executaAcaoAptidaoProcessoRetiradoPauta(getProcessoTrfSelecionado());
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}

	private ProcessoTrf getProcessoTrfSelecionado() {
		try {
			recarregarSessaoPautaProcessoTrfSelecionado();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return sessaoPautaProcessoTrfSelecionado.getProcessoTrf();
	}
	
	private void executaAcaoAptidaoProcessoRetiradoPauta(ProcessoTrf processoTrf){
		try{
			if(Authenticator.getOrgaoJulgadorAtual().equals(processoTrf.getOrgaoJulgador())){
				ComponentUtil.getProcessoJudicialManager().aptidaoParaJulgamento(processoTrf.getIdProcessoTrf(), false, null);
			} else {
				ComponentUtil.getProcessoJudicialManager().aptidaoParaJulgamento(processoTrf.getIdProcessoTrf(), false, null);
				ComponentUtil.getProcessoJudicialManager().aptidaoParaJulgamento(processoTrf.getIdProcessoTrf(), true);
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	//******** Início do tratamento de filtragem *********//
	
	public void filtrar(){
		List<Criteria> criterias = new ArrayList<Criteria>();
		addCriterias("processoTrf.", criterias);
		if(criterias != null && criterias.size() > 0){
			setListaPautaProcessoTrf(ComponentUtil.getSessaoPautaProcessoTrfManager().filtrar(criterias));
		}else{
			carregarListagemProcessosTodasSituacoes();
		}
	}
	
	public boolean filtroProcessoOjClasseOuParte(String texto){
		boolean encontrouResultados = Boolean.FALSE;
		
		List<Criteria> criterias = new ArrayList<Criteria>();
		
		addCriteriaSessao(criterias);
		
		carregarFiltroClasseAssuntoParteOrgaoJulgadorENumeroProcesso("processoTrf.", texto, criterias);
		
		if(criterias != null && criterias.size() > 0){
			setListaPautaProcessoTrf(ComponentUtil.getSessaoPautaProcessoTrfManager().filtrar(criterias));

			if(getListaPautaProcessoTrf() != null && !getListaPautaProcessoTrf().isEmpty()){
				encontrouResultados = Boolean.TRUE;
			}
			
		}
		
		if(!encontrouResultados){
			carregarListagemProcessosTodasSituacoes();
		}
		
		limparCamposFiltro();
		
		return encontrouResultados;
	}
	
	/**
	 * Efetua a limpeza dos campos de filtragem
	 */
	public void limparCamposFiltro(){
		this.campoClasse = null;
		this.campoAssunto = null;
		this.numeroProcesso = null;
		this.prioridade = null;
		this.orgaoFiltro = null;
		this.tipoVotoFiltro = null;
		this.nomeParte = null;
		this.codigoIMF = null;
		this.codigoOAB = null;	
		this.dataInicialDistribuicao = null;
		this.dataFinalDistribuicao = null;
	}
	
	/**
	 * Acrescenta eventuais critérios de pesquisa de página a uma dada pesquisa.
	 * 
	 * @param prefix o prefixo JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriterias(String prefix, List<Criteria> criterias){
		addCriteriaSessao(criterias);
		addCriteriaAssunto(prefix, criterias);
		addCriteriaClasse(prefix, criterias);
		addCriteriaProcesso(prefix, criterias);
		addCriteriaDataDistribuicao(prefix, criterias);
		addCriteriaPrioridade(prefix, criterias);
		addCriteriaOrgao(prefix, criterias);
		addCriteriaTipoVoto(prefix, criterias);
		addCriteriaNomeParte(prefix, criterias);
		addCriteriaIMF(prefix, criterias);
		addCriteriaOAB(prefix, criterias);
	}
	
	private void carregarFiltroClasseAssuntoParteOrgaoJulgadorENumeroProcesso(String prefix, String texto, List<Criteria> criterias){
		
		if(texto != null && !StringUtil.fullTrim(texto).isEmpty()){

			String textoSemNumeroProcesso = StringUtil.fullTrim(texto).replaceAll("[\\-\\.\\d]", "");
			String numeroProcessoSemTexto = StringUtil.fullTrim(texto).replaceAll("\\D", "");

			if(textoSemNumeroProcesso != null && !StringUtil.fullTrim(textoSemNumeroProcesso).isEmpty()){
				this.campoClasse = textoSemNumeroProcesso;
				this.campoAssunto = textoSemNumeroProcesso;
				this.nomeParte = textoSemNumeroProcesso;
				
				criterias.add(
						Criteria.or(
								Criteria.contains(prefix + "processoAssuntoList.assuntoTrf.assuntoTrf", this.campoAssunto),
								Criteria.contains(prefix + "classeJudicial.classeJudicial", this.campoClasse),
								Criteria.contains(prefix + "processoParteList.pessoa.nome", this.nomeParte),
								Criteria.contains(prefix + "orgaoJulgador.orgaoJulgador", textoSemNumeroProcesso)
						)
				);
			}
			
			if(numeroProcessoSemTexto != null && !StringUtil.fullTrim(numeroProcessoSemTexto).isEmpty()){
				this.numeroProcesso = numeroProcessoSemTexto;
				addCriteriaProcesso(prefix, criterias);
			}
		}
		
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados a sessão.
	 * 
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaSessao(List<Criteria> criterias) {
		if(criterias != null){
			criterias.add(Criteria.equals("sessao", getSessao()));
			criterias.add(Criteria.isNull("dataExclusaoProcessoTrf"));
		}
	}

	/**
	 * Acrescenta à pesquisa os filtros relacionados ao número do processo.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaProcesso(String prefix, List<Criteria> criterias){
		if(numeroProcesso != null && !StringUtil.fullTrim(numeroProcesso).isEmpty()){
			String numeroApenasDigitos = numeroProcesso.replaceAll("\\D", "");
			criterias.add(Criteria.contains(prefix+"processo.numeroProcesso", new Translate(".-", ""), numeroApenasDigitos));
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados à prioridade.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaPrioridade(String prefix, List<Criteria> criterias){
		if(prioridade != null && prioridade.getIdPrioridadeProcesso() > 0){
			criterias.add(Criteria.equals(prefix + "prioridadeProcessoList.idPrioridadeProcesso", prioridade.getIdPrioridadeProcesso()));
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados à data de distribuição.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaDataDistribuicao(String prefix, List<Criteria> criterias){
		if(dataInicialDistribuicao != null){
			criterias.add(Criteria.greaterOrEquals(prefix + "dataDistribuicao", dataInicialDistribuicao));
		}
		if(dataFinalDistribuicao != null){
			criterias.add(Criteria.lessOrEquals(prefix + "dataDistribuicao", dataFinalDistribuicao));
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados ao assunto.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaAssunto(String prefix, List<Criteria> criterias){
		if(campoAssunto != null && !StringUtil.fullTrim(campoAssunto).isEmpty()){
			criterias.add(Criteria.or(
					Criteria.equals(prefix + "processoAssuntoList.assuntoTrf.codAssuntoTrf", campoAssunto),
					Criteria.contains(prefix + "processoAssuntoList.assuntoTrf.assuntoTrf", campoAssunto)));
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados ao órgão julgador.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaOrgao(String prefix, List<Criteria> criterias){
		if(orgaoFiltro != null){
			criterias.add(Criteria.equals(prefix + "orgaoJulgador", orgaoFiltro));
		}
	}

	/**
	 * Acrescenta à pesquisa os filtros relacionados ao tipo de voto do relator.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaTipoVoto(String prefix, List<Criteria> criterias){
		if(tipoVotoFiltro  != null){
			criterias.add(Criteria.exists(" select 1 from SessaoProcessoDocumentoVoto spdv where spdv.sessao = o.sessao and processoTrf = o.processoTrf and orgaoJulgador = o.processoTrf.orgaoJulgador and spdv.tipoVoto.idTipoVoto = "+ tipoVotoFiltro.getIdTipoVoto()));
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados à classe judicial.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaClasse(String prefix, List<Criteria> criterias){
		if(campoClasse != null && !StringUtil.fullTrim(campoClasse).isEmpty()){
			criterias.add(Criteria.or(
					Criteria.equals(prefix + "classeJudicial.codClasseJudicial", campoClasse),
					Criteria.contains(prefix + "classeJudicial.classeJudicial", campoClasse),
					Criteria.equals(prefix + "classeJudicial.classeJudicialSigla", campoClasse)));
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados ao nome da parte.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaNomeParte(String prefix, List<Criteria> criterias){
		if(nomeParte == null || StringUtil.fullTrim(nomeParte).isEmpty()){
			return;
		}
		String nome = StringUtil.fullTrim(nomeParte);
		if(nome.split(" ").length > 1){
			nome = nome.replaceAll(" ", "%");
			Criteria nomeDocumento = Criteria.contains(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.nome", nome);
			nomeDocumento.setRequired(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList", false);
			Criteria n3 = Criteria.and(
					Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.ativo", true),
					Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.usadoFalsamente", false),
					nomeDocumento);
			Criteria n1 = Criteria.contains(prefix + "processoParteList.pessoa.nome", nome);
			Criteria n2 = Criteria.contains(prefix + "processoParteList.pessoa.pessoaNomeAlternativoList.pessoaNomeAlternativo", nome);
			n2.setRequired(prefix + "processoParteList.pessoa.pessoaNomeAlternativoList", false);
			Criteria n4 = Criteria.equals(prefix + "processoParteList.inSituacao", ProcessoParteSituacaoEnum.A);
			Criteria orC = Criteria.or(n3, n2, n1);
			Criteria andC = Criteria.and(n4, orC);
			criterias.add(andC);
		}else{
			ComponentUtil.getFacesMessages().add(Severity.WARN, "É necessário informar ao menos dois nomes para realizar a consulta por nome.");
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados ao CPF ou CNPJ.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaIMF(String prefix, List<Criteria> criterias){
		InscricaoMFUtil.InscricaoMF inscricaoMF = InscricaoMFUtil.criarInscricaoMF(codigoIMF, "CPF");
		if (inscricaoMF == null || inscricaoMF.inscricao.isEmpty()){
			return;
		}
		Criteria documento = Criteria.and(
				Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo", inscricaoMF.tipo),
				Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.ativo", true),
				Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.usadoFalsamente", false),
				inscricaoMF.tipo.equals("CPF") ? 
					Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.numeroDocumento", inscricaoMF.inscricao) :
					Criteria.startsWith(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.numeroDocumento", inscricaoMF.inscricao)
		);
		criterias.add(documento);
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados ao número da OAB.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaOAB(String prefix, List<Criteria> criterias){
		if(codigoOAB == null || codigoOAB.isEmpty()){
			return;
		}
		String oab = StringUtil.fullTrim(codigoOAB).replaceAll(" ", "%");
		if(!oab.isEmpty()){
			Criteria documento = Criteria.and(
					Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo", "OAB"),
					Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.ativo", true),
					Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.usadoFalsamente", false),
					Criteria.contains(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.numeroDocumento", oab));
			criterias.add(documento);
		}
	}

	//******** Fim do tratamento de filtragem *********//
	
	//Metodos exigidos pelo CKEditor
	
	@Override
	public String obterVersoesDocumentoJSONProximas() {
		try {
			SessaoProcessoDocumento sessaoProcessoDocumentoEmEdicao = elementosJulgamentoFiltrados.get(getChaveDocumentoFiltradoEmEdicao());
			sessaoProcessoDocumentoEmEdicao = ComponentUtil.getSessaoProcessoDocumentoManager().findById(sessaoProcessoDocumentoEmEdicao.getIdSessaoProcessoDocumento());
			String resposta = ComponentUtil.getControleVersaoDocumentoManager().obterVersoesDocumentoJSONPaginada(sessaoProcessoDocumentoEmEdicao.getProcessoDocumento().getProcessoDocumentoBin(), getLimit(), getOffset());
			paginarProximasControleVersaoDocumento();
			return resposta;
		} catch (PJeBusinessException e) {
			logger.error(e.getMessage());
		}
		
		return "";
	}
	
	@Override
	public String verificarPluginTipoVoto() {
		JSONObject retorno = new JSONObject();
		try {
			retorno.put("sucesso", Boolean.TRUE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return retorno.toString();
	}
	
	@Override
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		ProcessoDocumento pdSelecionado = null;
		if(this.elementosJulgamentoFiltrados != null){
			SessaoProcessoDocumento sessaoProcessoDocumentoEmEdicao = elementosJulgamentoFiltrados.get(getChaveDocumentoFiltradoEmEdicao());
			if(sessaoProcessoDocumentoEmEdicao != null ){
				pdSelecionado = sessaoProcessoDocumentoEmEdicao.getProcessoDocumento();
			}
		}
        return (pdSelecionado!= null)?pdSelecionado.getTipoProcessoDocumento(): super.getTipoProcessoDocumento();
	}
	
	@Override
	public String getModelosPorTipoDocumentoSelecionado() {
		JSONArray arrayJSON = new JSONArray();
		try {
			TipoProcessoDocumento tipo = getTipoProcessoDocumento();
			if(tipo != null) {
				List<ModeloDocumento> modelosDocumento = ComponentUtil.getDocumentoJudicialService().getModelosDisponiveisPorTipoDocumento(tipo);
	
	            for (ModeloDocumento modeloDocumento : modelosDocumento) {
	                arrayJSON.put(modeloDocumento.getTituloModeloDocumento());
	            }
	            
	        }else{
	        	arrayJSON.put("");
	        }

		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		
		return arrayJSON.toString();
	}

	@Override
	public String recuperarModeloDocumento(String modeloDocumento) {
		ModeloDocumento modelo = null;
		try {
			List<ModeloDocumento> modelosPorTipoTituloOuDescricao = ComponentUtil.getModeloDocumentoManager().getModelosPorTipoTituloOuDescricao(getTipoProcessoDocumento(), modeloDocumento, ( Integer[] )null);
			modelo = modelosPorTipoTituloOuDescricao.get(0);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return modelo.getModeloDocumento();
	}

	private boolean isDocumentoVotoAssinado() {
		ProcessoDocumento documento = getVoto().getProcessoDocumento();
		return (documento!=null) && ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado( documento );
	}
	
	@Override
	public boolean isDocumentoAssinado() {
		return isDocumentoVotoAssinado();
	}

	@Override
	public void removerAssinatura() {
		if (!isUsuarioAtualMagistrado())
			throw new PJeRuntimeException("Somente o magistrado pode remover a assinatura do documento.");
		
		ProcessoDocumento documento = getVoto().getProcessoDocumento();
		if ((documento==null) || !ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado( documento ))
			throw new PJeRuntimeException("Documento não está assinado!");
		
		ComponentUtil.getAssinaturaDocumentoService().removeAssinatura(documento);
		ComponentUtil.getTaskInstanceHome().updateTransitions();
}

	@Override
	public void descartarDocumento() throws PJeBusinessException {
		elementosJulgamentoFiltrados.get(getChaveDocumentoFiltradoEmEdicao()).getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento("");
	}

	@Override
	public String obterConteudoDocumentoAtual() {
		SessaoProcessoDocumento sessaoProcessoDocumentoEmEdicao = elementosJulgamentoFiltrados.get(getChaveDocumentoFiltradoEmEdicao());
		String modeloDocumento = "";
		
		try {
			modeloDocumento = ComponentUtil.getControleVersaoDocumentoManager().obterConteudoDocumentoJSON(sessaoProcessoDocumentoEmEdicao.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return modeloDocumento;
	}

	@Override
	public String obterTiposVoto() {
		SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto = (SessaoProcessoDocumentoVoto)elementosJulgamentoFiltrados.get(getChaveDocumentoFiltradoEmEdicao());
		
		RespostaDTO respostaDTO = new RespostaDTO();
		
		try {
			respostaDTO.setSucesso(Boolean.TRUE);
			RespostaTiposVotoDTO respostaTiposVotoDTO = new RespostaTiposVotoDTO();
			respostaTiposVotoDTO.setPodeAlterar(Boolean.TRUE);
			if(sessaoProcessoDocumentoVoto.getTipoVoto() != null) {
				TipoVoto tipoVoto = sessaoProcessoDocumentoVoto.getTipoVoto();
				respostaTiposVotoDTO.setSelecao(criarTipoVotoDTO(tipoVoto));
			}
			
			if(isOrgaoAtualRelator()){
				respostaTiposVotoDTO.setTipos(criarListaTiposVoto(recuperaTiposVotosRelator()));
			}else{
				respostaTiposVotoDTO.setTipos(criarListaTiposVoto(recuperaTiposVotosVogais()));
			}

			respostaDTO.setResposta(respostaTiposVotoDTO);
		} catch (Exception e) {
			e.printStackTrace();
			respostaDTO.setSucesso(Boolean.FALSE);
			respostaDTO.setMensagem(e.getLocalizedMessage());
		}
		
		String strRetornoTiposVotoJSON = new Gson().toJson(respostaDTO, RespostaDTO.class);

		return strRetornoTiposVotoJSON;
	}
	
	public boolean isDocumentoVotoVazio(){
		ProcessoDocumento pd = getVoto().getProcessoDocumento(); 
		if (pd==null)
			return true;
		if(pd.getProcessoDocumentoBin() == null) 
			return true;
		String conteudo = pd.getProcessoDocumentoBin().getModeloDocumento(); 
		if (conteudo==null)
			return true;
		conteudo = StringUtil.removeHtmlTags(conteudo).trim();
		return conteudo.isEmpty();
	}
	
	@Override
	public boolean podeAssinar() {
		return isUsuarioAtualMagistrado() && isDocumentoPersistido() && !isDocumentoVotoVazio();
	}
	
	@Override
	public String getNomeTipoDocumentoPrincipal() {
		SessaoProcessoDocumento sessaoProcessoDocumento = null;
		
		if(isRedigirVotoVogal()){
			sessaoProcessoDocumento = getVoto();
		}else{
			sessaoProcessoDocumento = elementosJulgamentoFiltrados.get(getChaveDocumentoFiltradoEmEdicao());
		}
		
		if(sessaoProcessoDocumento != null 
				&& sessaoProcessoDocumento.getProcessoDocumento() != null
				&& sessaoProcessoDocumento.getProcessoDocumento().getTipoProcessoDocumento() != null){
			
				TipoProcessoDocumento tipo;
				try {
					tipo = ComponentUtil.getTipoProcessoDocumentoManager().findById(sessaoProcessoDocumento.getProcessoDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento());
					return tipo.getTipoProcessoDocumento();
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				}
		}
		return "";
	}
	
	private SessaoProcessoDocumento getSessaoProcessoDocumentoSelecionado() {
		SessaoProcessoDocumento sessaoProcessoDocumento = elementosJulgamentoFiltrados.get(getAbaSelecionada());
		return sessaoProcessoDocumento;
	}
	
	@Override
	public boolean isDocumentoPersistido() {
		Boolean resultado = Boolean.FALSE;
		SessaoProcessoDocumento sessaoProcessoDocumento = getSessaoProcessoDocumentoSelecionado();
		
		if(sessaoProcessoDocumento != null && sessaoProcessoDocumento.getProcessoDocumento() != null && sessaoProcessoDocumento.getProcessoDocumento().getIdProcessoDocumento() > 0){
			resultado = Boolean.TRUE;
		}
		
		return resultado;
		
	}
	
	@Override
	public String getTiposDocumentosDisponiveis() {
		JSONArray retorno = new JSONArray();
		
		try {
			
			List<TipoProcessoDocumento> tiposDocumentosDisponiveis = getTiposDocumentosParaOrgaoJulgadorAtualEDocumentoEmEdicao();
			if(tiposDocumentosDisponiveis != null){
				for (TipoProcessoDocumento tipoProcessoDocumento : tiposDocumentosDisponiveis) {
					retorno.put(tipoProcessoDocumento.getTipoProcessoDocumento());			
				}
			}
		} catch (Exception e) {
			ComponentUtil.getFacesMessages().add(Severity.ERROR,"Houve um erro de banco de dados ao tentar obter os tipos de documentos disponíveis.");
		}
		
		return retorno.toString();
	}

	private List<TipoProcessoDocumento> getTiposDocumentosParaOrgaoJulgadorAtualEDocumentoEmEdicao() throws Exception {
		Integer[] idsTiposDocumentos = new Integer[0];
		
		if(getChaveDocumentoFiltradoEmEdicao() == EMENTA){
			idsTiposDocumentos = Util.converterStringIdsToIntegerArray(String.valueOf(ParametroUtil.instance().getTipoProcessoDocumentoEmenta().getIdTipoProcessoDocumento()));
		}
		
		if(getChaveDocumentoFiltradoEmEdicao() == RELATORIO){
			idsTiposDocumentos = Util.converterStringIdsToIntegerArray(String.valueOf(ParametroUtil.instance().getTipoProcessoDocumentoRelatorio().getIdTipoProcessoDocumento()));
		}
		
		if(getChaveDocumentoFiltradoEmEdicao() == VOTO){
			
			if(isOrgaoAtualRelator()){
				idsTiposDocumentos = ParametroUtil.instance().getIdsTipoDocumentoVoto();
			}else{
				idsTiposDocumentos = ParametroUtil.instance().getIdsTipoDocumentoVotoVogalPainelMagistrado();
			}
			
		}
		
		List<TipoProcessoDocumento> tiposDocumentosDisponiveis = null;
	
		if (idsTiposDocumentos.length > 0) {
			tiposDocumentosDisponiveis = ComponentUtil.getDocumentoJudicialService().getTiposDisponiveis(idsTiposDocumentos);
		} else {
			tiposDocumentosDisponiveis = ComponentUtil.getDocumentoJudicialService().getTiposDocumentosAntigos(false, true);
		}
		return tiposDocumentosDisponiveis;
	}
	
	private boolean isAbaVotoSelecionada() {
		return VOTO.equals(getAbaSelecionada());
	}
	
	private static String getConteudoDocumento(SessaoProcessoDocumento sessaoProcessoDocumento) {
		if (sessaoProcessoDocumento==null)
			return null;
		if (sessaoProcessoDocumento.getProcessoDocumento()==null)
			return null;
		if (sessaoProcessoDocumento.getProcessoDocumento().getProcessoDocumentoBin()==null)
			return null;
		return sessaoProcessoDocumento.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
	}

	@Override
	public void salvar(String conteudo) {
		AjaxDataUtil ajaxDataUtil = ComponentUtil.getComponent(AjaxDataUtil.NAME, ScopeType.EVENT);
		try {
			if (StringUtils.isEmpty(conteudo))
				throw new PJeBusinessException("O conteúdo do documento não pode estar vazio.");
			if (isAbaVotoSelecionada() && isDocumentoVotoAssinado())
				throw new PJeBusinessException("O voto já foi assinado. Não é possível alterá-lo!");

			SessaoProcessoDocumento sessaoProcessoDocumento = 
					isRedigirVotoVogal() ? getVoto() : getSessaoProcessoDocumentoSelecionado();
			
			EntityUtil.getEntityManager().clear();
						
			sessaoProcessoDocumento = ComponentUtil.getSessaoProcessoDocumentoManager().findById(sessaoProcessoDocumento.getIdSessaoProcessoDocumento());
			
			if (conteudo.equals(getConteudoDocumento(sessaoProcessoDocumento))) {
				ajaxDataUtil.sucesso();			
				return;
			}
			
			sessaoProcessoDocumento.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(conteudo);
			
			ComponentUtil.getDocumentoJudicialService().persist(sessaoProcessoDocumento.getProcessoDocumento(), true);
			ComponentUtil.getDocumentoJudicialService().flush();
			
			if(sessaoProcessoDocumento instanceof SessaoProcessoDocumentoVoto){
				SessaoProcessoDocumentoVoto voto = (SessaoProcessoDocumentoVoto)sessaoProcessoDocumento;
	            voto.setDtVoto(new Date());
	            if(getIdTipoVotoSelecionado() > 0){
	            	voto.setTipoVoto(ComponentUtil.getTipoVotoManager().findById(getIdTipoVotoSelecionado()));
	            }
	            
	            salvarVoto(voto);
	        }else{	        	
	        	if(!isRedigirVotoVogal())
	        		elementosJulgamentoFiltrados.put(getChaveDocumentoFiltradoEmEdicao(), sessaoProcessoDocumento);	        	
	        }
			
			ComponentUtil.getControleVersaoDocumentoManager().salvarVersaoDocumento(sessaoProcessoDocumento.getProcessoDocumento());
			ComponentUtil.getControleVersaoDocumentoManager().flush();			

			ajaxDataUtil.sucesso();			
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof PJeBusinessException)
				ComponentUtil.getFacesMessages().add(Severity.ERROR, e.getLocalizedMessage());
			ajaxDataUtil.erro();
		}

	}
	
	public void cancelarEdicao(){
		try {
			recarregarSessaoPautaProcessoTrfSelecionado();
			setExibeEditor(Boolean.FALSE);
			setRedigirVotoVogal(Boolean.FALSE);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	private void salvarVoto(SessaoProcessoDocumentoVoto voto){
		try {
			
			recarregarSessaoPautaProcessoTrfSelecionado();
			
			if (!isOrgaoAtualParticipaVotacaoProcessoSelecionado()) {
				throw new PJeBusinessException("O órgão julgador atual não participa da votação do processo em questão para esta sessão!");
			}

			replicarDoc(voto, voto.getProcessoDocumento());
			ComponentUtil.getDerrubadaVotoManager().analisarTramitacaoFluxoVotoDerrubado(voto);
			
			ComponentUtil.getSessaoProcessoDocumentoManager().persist(voto);
			ComponentUtil.getSessaoProcessoDocumentoManager().flush();
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar gravar o voto: {0}.", e.getLocalizedMessage());
			ComponentUtil.getFacesMessages().add(Severity.ERROR, e.getLocalizedMessage());
		}
		
	}
	
	public String recuperarTextoVotoDetalhesProcessoSelecionado(){
		if(!isOrgaoAtualParticipaVotacaoProcessoSelecionado()){
			return NAO_VOTANTE;
		}
		
		if(getVoto() != null && getVoto().getTipoVoto() != null && !StringUtil.isEmpty(getVoto().getTipoVoto().getTipoVoto())){
			return getVoto().getTipoVoto().getTipoVoto();
		}
		
		return SEU_VOTO_NAO_FOI_PROFERIDO;
	}

	public boolean isOrgaoAtualParticipaVotacaoProcessoSelecionado() {
		return getSessaoPautaProcessoTrfSelecionado().getParticipaVotacao(getOrgaoAtual());
	}
	
	/**
	 * Metodo copiado da VotacaoVogalMultDocsAction
	 * @param voto
	 * @return Proximo Numero Ordem Doc
	 */
	private Integer getProximoNumeroOrdemDoc(SessaoProcessoDocumentoVoto voto) {
		return ComponentUtil.getSessaoProcessoMultDocsVotoManager().recuperarProximoNumeroOrdemDoc(voto);
	}
	
	/**
	 * Metodo responsável por gravar documento na tabela onde são armazenados os documentos para multdocs
	 * @param voto
	 * @param documento
	 */
	private void gravarEmMultDocs(SessaoProcessoDocumentoVoto voto, ProcessoDocumento documento){
		SessaoProcessoMultDocsVoto sessaoProcessoDocumentoMultDocs = new SessaoProcessoMultDocsVoto();
		sessaoProcessoDocumentoMultDocs.setProcessoDocumento(documento);
		sessaoProcessoDocumentoMultDocs.setSessaoProcessoDocumentoVoto(voto);
		sessaoProcessoDocumentoMultDocs.setOrdemDocumento(getProximoNumeroOrdemDoc(voto));
		try {
			ComponentUtil.getSessaoProcessoMultDocsVotoManager().persist(sessaoProcessoDocumentoMultDocs);
			ComponentUtil.getSessaoProcessoMultDocsVotoManager().flush();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Houve um erro gravar o documento: {0}", e.getLocalizedMessage());
		}
	}
	
	/**
	 * Metodo responsável por gravar uma copia do documento na tabela onde são armazenados os documentos para multdocs
	 * @param voto
	 * @param documento
	 */
	private void replicarDoc(SessaoProcessoDocumentoVoto voto,ProcessoDocumento documento) {
		if(!verificarDocEmSessaoProcMultDocs(documento)){
			gravarEmMultDocs(voto, documento);
		}
	}
	
	/**
	 * Metodo responsável por verificar se o documento já existe na tabela de multdocs
	 * @param voto
	 * @param documento
	 */
	private boolean verificarDocEmSessaoProcMultDocs(ProcessoDocumento proc) {
		SessaoProcessoMultDocsVoto docSessaMult = ComponentUtil.getSessaoProcessoMultDocsVotoManager().recuperarSessaoProcessoDoc(proc);
		return docSessaMult != null;
	}
	
	/**
	 * Método pra definir um comportamento diferente na visão para documento enviado.
	 * @param sessaoProcessoDocumento
	 * @return 	1 - Se o orgão julgador selecionado for diferente do orgão julgador da sessão
	 * 			2 - Se o orgão julgador selecionado for igual ao orgão julador da sessão E o documento estiver assinado.
	 * 			3 - Se o orgão julgador selecionado for igual ao orgão julador da sessão E o documento NÃO estiver assinado. 
	 */
	public SituacaoDocumentoSessao validaApresentacaoSessaoDocumento(SessaoProcessoDocumento sessaoProcessoDocumento){
		SituacaoDocumentoSessao retorno = null;
		
		try {
			sessaoProcessoDocumento = ComponentUtil.getSessaoProcessoDocumentoManager().findById(sessaoProcessoDocumento.getIdSessaoProcessoDocumento());
			String nomeOrgaoJulgadorLogado = getOrgaoAtual().getOrgaoJulgador();
			Boolean isExisteDocumento = (sessaoProcessoDocumento.getProcessoDocumento() != null);
			
			if(isExisteDocumento){
				
				Boolean isDocumentoNaoAssinado = sessaoProcessoDocumento.getProcessoDocumento().getProcessoDocumentoBin().getSignatarios().isEmpty();
				Boolean isRelatorDocumento = sessaoProcessoDocumento.getOrgaoJulgador().getOrgaoJulgador().equals(nomeOrgaoJulgadorLogado);
				
				if(isRelatorDocumento){
					if(isDocumentoNaoAssinado){
						retorno = SituacaoDocumentoSessao.RELATOR_DOCUMENTO_NAO_ASSINADO;
					}else{
						retorno = SituacaoDocumentoSessao.RELATOR_DOCUMENTO_ASSINADO;
					}
				}else{
					retorno = SituacaoDocumentoSessao.NAO_RELATOR;
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		
		return retorno;
	}

	public void downloadDocumentos(String chave) {
		ProcessoDocumento pd = elementosJulgamentoFiltrados.get(chave).getProcessoDocumento();
		String filename = chave.substring(0, chave.indexOf("_") + 1) + sessaoPautaProcessoTrfSelecionado.getProcessoTrf().getNumeroProcesso() + ".pdf";

		exportarArquivoPDF(filename, pd);
	}

	private void exportarArquivoPDF(String filename, ProcessoDocumento pd) {
		List<ProcessoDocumento> lista = new ArrayList<ProcessoDocumento>(1);
		lista.add(pd);

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""+ filename + "\"");
		
		OutputStream out = null;
		try {
			GeradorPdfUnificado geradorPdf = new GeradorPdfUnificado();
			geradorPdf.setResurcePath(new br.com.itx.component.Util().getUrlProject());
			out = response.getOutputStream();
			geradorPdf.gerarPdfSimples(lista, out);
			out.flush();

			facesContext.responseComplete();
		} catch (Exception ex) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao exportar o arquivo: " + filename + ". Mensagem interna: " + ex.getLocalizedMessage());
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//Getters and Setters
	
	public int getIdSessao() {
		return idSessao;
	}

	public void setIdSessao(int idSessao) {
		this.idSessao = idSessao;
	}

	public String getQuantidadeProcessos() {
		return quantidadeProcessos;
	}

	public String getQuantidadeProcessosEmJulgamento() {
		return quantidadeProcessosEmJulgamento;
	}

	public String getQuantidadeProcessosJulgados() {
		return quantidadeProcessosJulgados;
	}

	public String getQuantidadeProcessosComPedidoVista() {
		return quantidadeProcessosComPedidoVista;
	}

	public String getQuantidadeProcessosAdiados() {
		return quantidadeProcessosAdiados;
	}

	public String getQuantidadeProcessosRetiradosJulgamento() {
		return quantidadeProcessosRetiradosJulgamento;
	}
	
	public Sessao getSessao() {
		return this.sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}

	public List<SessaoPautaProcessoTrf> getListaPautaProcessoTrf() {
		return listaPautaProcessoTrf;
	}

	public void setListaPautaProcessoTrf(List<SessaoPautaProcessoTrf> listaPautaProcessoTrf) {
		this.listaPautaProcessoTrf = listaPautaProcessoTrf;
	}

	public SessaoPautaProcessoTrf getSessaoPautaProcessoTrfSelecionado() {
		return sessaoPautaProcessoTrfSelecionado;
	}
	
	private void setupVoto(SessaoProcessoDocumentoVoto voto) {
		if(voto == null){
			voto = new SessaoProcessoDocumentoVoto();
			voto.setSessao(sessao);
			voto.setCheckAcompanhaRelator(isOrgaoAtualRelator());
			voto.setDestaqueSessao(false);
			voto.setImpedimentoSuspeicao(false);
			ProcessoTrf processoTrf = sessaoPautaProcessoTrfSelecionado==null ? null : sessaoPautaProcessoTrfSelecionado.getProcessoTrf();
			voto.setProcessoTrf(processoTrf);
			voto.setOrgaoJulgador(getOrgaoAtual());
			voto.setLiberacao(true);
		}
		this.voto = voto;
		
		if (voto.getProcessoDocumento()==null) {
			Optional<SessaoProcessoMultDocsVoto> optMultDoc = voto.getSessaoProcessoMultDocsVoto().stream().max((a, b) -> {
				return a.getOrdemDocumento()-b.getOrdemDocumento();
			});
			if (optMultDoc.isPresent()) {
				voto.setProcessoDocumento(optMultDoc.get().getProcessoDocumento());
				ComponentUtil.getSessaoProcessoDocumentoVotoManager().persist(voto);
				try {
					ComponentUtil.getSessaoProcessoDocumentoVotoManager().flush();
				} catch (PJeBusinessException ex) {
					ex.printStackTrace();
				}
			}				
		}
	}

	private void setupSessaoPautaProcessoTrfSelecionado(SessaoPautaProcessoTrf sessaoPautaProcessoTrfSelecionado) {
		this.sessaoPautaProcessoTrfSelecionado = sessaoPautaProcessoTrfSelecionado;
		
		ProcessoTrf processoTrf = sessaoPautaProcessoTrfSelecionado==null ? null : sessaoPautaProcessoTrfSelecionado.getProcessoTrf();
		ProcessoTrfHome.instance().setInstance(processoTrf);

		try {
			OrgaoJulgador orgaoJulgador = processoTrf==null ? null : processoTrf.getOrgaoJulgador();
			OrgaoJulgador relator = ComponentUtil.getOrgaoJulgadorManager().findById(orgaoJulgador==null ? null : orgaoJulgador.getIdOrgaoJulgador());
			setOrgaoRelator(relator);
		} catch (PJeBusinessException e) {
			logger.error("Objeto OrgaoJulgador do relator do processo selecionado não encontrado para o id especificado: {0}",e.getLocalizedMessage());
		} 

		SessaoProcessoDocumentoVoto voto = ComponentUtil.getSessaoProcessoDocumentoVotoDAO().recuperarVoto(getSessao(), processoTrf, getOrgaoAtual());
		if (voto==null) {
			Date ultimoJulgamento = ComponentUtil.getSessaoPautaProcessoTrfDAO().obterUltimaDataSessaoJulgamentoProcesso(processoTrf);
			if (ultimoJulgamento==null)
				ultimoJulgamento = new Date(0);
			voto = ComponentUtil.getSessaoProcessoDocumentoVotoDAO().recuperarVoto(ultimoJulgamento, processoTrf, getOrgaoAtual());
		}
		setupVoto(voto);
	}
	
	public void setSessaoPautaProcessoTrfSelecionado(SessaoPautaProcessoTrf sessaoPautaProcessoTrfSelecionado) {
		//if (!Objects.equals(this.sessaoPautaProcessoTrfSelecionado, sessaoPautaProcessoTrfSelecionado)) {
			setupSessaoPautaProcessoTrfSelecionado(sessaoPautaProcessoTrfSelecionado);
		//}
	}

	public OrgaoJulgador getOrgaoAtual() {
		return Authenticator.getOrgaoJulgadorAtual();
	}

	public OrgaoJulgador getOrgaoRelator() {
		return orgaoRelator;
	}

	public void setOrgaoRelator(OrgaoJulgador orgaoRelator) {
		this.orgaoRelator = orgaoRelator;
	}
	
	public SessaoProcessoDocumentoVoto getVoto() {
		return voto;
	}
	
	public boolean isOrgaoAtualRelator() {
		return getOrgaoAtual() != null ? getOrgaoAtual().equals(orgaoRelator) : false;
	}

	public boolean isExibeEditor() {
		return isExibeEditor;
	}

	public void setExibeEditor(boolean isExibeEditor) {
		this.isExibeEditor = isExibeEditor;
	}

	public List<SessaoProcessoDocumento> getElementosJulgamento() {
		return elementosJulgamento;
	}

	public void setElementosJulgamento(List<SessaoProcessoDocumento> elementosJulgamento) {
		this.elementosJulgamento = elementosJulgamento;
	}

	public Map<String,SessaoProcessoDocumento> getElementosJulgamentoFiltrados() {
		return elementosJulgamentoFiltrados;
	}

	public void setElementosJulgamentoFiltrados(Map<String,SessaoProcessoDocumento> elementosJulgamentoFiltrados) {
		this.elementosJulgamentoFiltrados = elementosJulgamentoFiltrados;		
	}

	public TipoSituacaoPautaEnum getTipoSituacaoPautaListagem() {
		return tipoSituacaoPautaListagem;
	}

	public void setTipoSituacaoPautaListagem(TipoSituacaoPautaEnum tipoSituacaoPautaListagem) {
		this.tipoSituacaoPautaListagem = tipoSituacaoPautaListagem;
	}

	public String getChaveDocumentoFiltradoEmEdicao() {
		return getAbaSelecionada();
	}

	public void setChaveDocumentoFiltradoEmEdicao(String chaveDocumentoFiltradoEmEdicao) {
		setAbaSelecionada(chaveDocumentoFiltradoEmEdicao);
	}

	public boolean isRedigirVotoVogal() {
		return isRedigirVotoVogal;
	}

	public void setRedigirVotoVogal(boolean isRedigirVoto) {
		this.isRedigirVotoVogal = isRedigirVoto;
	}

	public Integer getNumeroOrdem() {
		return numeroOrdem;
	}

	public void setNumeroOrdem(Integer numeroOrdem) {
		this.numeroOrdem = numeroOrdem;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getCampoAssunto() {
		return campoAssunto;
	}

	public void setCampoAssunto(String campoAssunto) {
		this.campoAssunto = campoAssunto;
	}

	public String getCampoClasse() {
		return campoClasse;
	}

	public void setCampoClasse(String campoClasse) {
		this.campoClasse = campoClasse;
	}

	public PrioridadeProcesso getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(PrioridadeProcesso prioridade) {
		this.prioridade = prioridade;
	}

	public List<PrioridadeProcesso> getPrioridades() {
		if(prioridades == null){
			try {
				prioridades = ComponentUtil.getPrioridadeProcessoManager().listActive();
			} catch (PJeBusinessException e) {
				ComponentUtil.getFacesMessages().add(Severity.ERROR, "Houve um erro ao tentar recuperar as listas de prioridades: {0}.", e.getLocalizedMessage());
				return Collections.emptyList();
			}
		}
		return prioridades;
	}

	public void setPrioridades(List<PrioridadeProcesso> prioridades) {
		this.prioridades = prioridades;
	}

	public Date getDataInicialDistribuicao() {
		return dataInicialDistribuicao;
	}

	public void setDataInicialDistribuicao(Date dataInicialDistribuicao) {
		this.dataInicialDistribuicao = dataInicialDistribuicao;
	}

	public Date getDataFinalDistribuicao() {
		return dataFinalDistribuicao;
	}

	public void setDataFinalDistribuicao(Date dataFinalDistribuicao) {
		this.dataFinalDistribuicao = dataFinalDistribuicao;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getCodigoIMF() {
		return codigoIMF;
	}

	public void setCodigoIMF(String codigoIMF) {
		this.codigoIMF = codigoIMF;
	}

	public String getCodigoOAB() {
		return codigoOAB;
	}

	public void setCodigoOAB(String codigoOAB) {
		this.codigoOAB = codigoOAB;
	}

	public OrgaoJulgador getOrgaoFiltro() {
		return orgaoFiltro;
	}

	public void setOrgaoFiltro(OrgaoJulgador orgaoFiltro) {
		this.orgaoFiltro = orgaoFiltro;
	}

	public TipoVoto getTipoVotoFiltro() {
		return tipoVotoFiltro;
	}

	public void setTipoVotoFiltro(TipoVoto tipoVotoFiltro) {
		this.tipoVotoFiltro = tipoVotoFiltro;
	}

	public boolean isMostrarVoto() {
		return mostrarVoto;
	}

	public void setMostrarVoto(boolean mostrarVoto) {
		this.mostrarVoto = mostrarVoto;
	}

	public boolean isMostrarAnotacoes() {
		return mostrarAnotacoes;
	}

	public void setMostrarAnotacoes(boolean mostrarAnotacoes) {
		this.mostrarAnotacoes = mostrarAnotacoes;
	}
	
	/**
	 * Verifica se o usuário atual é um dos magistrados da turma/seção para o 
	 * processo em foco.
	 * @return <code>true</code> caso o usuário atual seja um magistrado apto a 
	 * votar no processo selecionado.
	 */
	public boolean isUsuarioAtualMagistrado() {
		Usuario usuarioAtual = Authenticator.getUsuarioLogado();				
		VisibilidadeValidador validadorMagistrado = VisibilidadeValidadorEnum.MAGISTRADO.getValidador();
		return validadorMagistrado.isPossivelValidar(usuarioAtual) || Boolean.getBoolean("modoTesteEhMagistrado");
	}

	public boolean isUploadArquivoAssinadoRealizado(){
		return (arquivoAssinado!=null) 
				&& !StringUtils.isEmpty(arquivoAssinado.getAssinatura())
				&& !StringUtils.isEmpty(arquivoAssinado.getCadeiaCertificado())
				&& !StringUtils.isEmpty(arquivoAssinado.getId());
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		arquivoAssinado = arquivoAssinadoHash;
		logger.info("doUploadArquivoAssinado: " + arquivoAssinadoHash);
	}
	
	@Override
	public String getActionName() {
		return NAME;
	}
	
	public boolean isPluginAssinaturaHabilitado() {
		return isUsuarioAtualMagistrado() && !isDocumentoVotoAssinado();
	}

	@Override
	public String getDownloadLinks(){
		ProcessoDocumento documento = getVoto().getProcessoDocumento();
		if (documento==null)
			return null;
		return ComponentUtil.getDocumentoJudicialService().getDownloadLinks(Arrays.asList(documento));
	}
	
	public void concluirAssinatura(){
		logger.info("Concluiu assinaturas");
		return;
	}
	
	public void refreshDetalhesProcesso() {
		logger.info("refreshDetalhesProcesso");
		return;
	}
	
	public boolean assinarDocumento(){
		if (isDocumentoVotoAssinado())
			throw new PJeRuntimeException("O voto já está assinado!");
		assinarVoto();
		return isDocumentoVotoAssinado();
	}

	private void assinarVoto(){
		if(arquivoAssinado == null)
			throw new PJeRuntimeException("Os dados da assinatura não foram recebidos. Verifique se o PJeOffice está funcionando adequadamente.");
		
		SessaoProcessoDocumentoVotoHome sessaoProcessoDocumentoVotoHome = ComponentUtil.getSessaoProcessoDocumentoVotoHome();
		sessaoProcessoDocumentoVotoHome.setInstance(getVoto());
		sessaoProcessoDocumentoVotoHome.setId(getVoto().getIdSessaoProcessoDocumento());
		
		ProcessoDocumentoBin processoDocumentoBin = sessaoProcessoDocumentoVotoHome.getInstance().getProcessoDocumento().getProcessoDocumentoBin();
		processoDocumentoBin.setCertChain(arquivoAssinado.getCadeiaCertificado());
		processoDocumentoBin.setSignature(arquivoAssinado.getAssinatura());
		
		ProcessoDocumentoBinHome.instance().setInstance(processoDocumentoBin);
		
		if(sessaoProcessoDocumentoVotoHome.isManaged()){
			sessaoProcessoDocumentoVotoHome.updateVotoComAssinatura();
		}else{
			sessaoProcessoDocumentoVotoHome.persistVotoComAssinatura();
		}
	}

	public int getIdSessaoPautaProcessoTrfSelecionado() {
		if (sessaoPautaProcessoTrfSelecionado==null) 
			return 0;
		return sessaoPautaProcessoTrfSelecionado.getIdSessaoPautaProcessoTrf();
	}
	
	public void setIdSessaoPautaProcessoTrfSelecionado(int idSessaoPautaProcessoTrf) {
		//if (getIdSessaoPautaProcessoTrfSelecionado()!=idSessaoPautaProcessoTrf) {
			carregarSessaoPautaProcessoTrfSelecionado(idSessaoPautaProcessoTrf);
		//}
	}

	public boolean isOrgaoAtualImpedidoSuspeito() {
		boolean retorno = false;
		if(voto != null && voto.getImpedimentoSuspeicao()) {
			retorno = true;
		} else {
			if(ComponentUtil.getSessaoPautaProcessoComposicaoManager().verificarImpedimentoSuspeicao(sessaoPautaProcessoTrfSelecionado, getOrgaoAtual())) {
				retorno = true;
			}
		}
		return retorno;
	}
}
