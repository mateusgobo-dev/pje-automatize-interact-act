/**
 * pje-web
 * Copyright (C) 2009-2013 Conselho Nacional de Justia
 *
 * A propriedade intelectual deste programa, como cdigo-fonte
 * e como sua derivao compilada, pertence  Unio Federal,
 * dependendo o uso parcial ou total de autorizao expressa do
 * Conselho Nacional de Justia.
 *
 **/
package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoDocumentoDataInclusaoComparator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.view.CkEditorGeraDocumentoAbstractAction;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.cnj.pje.vo.PlacarSessaoVO;
import br.jus.je.pje.business.dto.RespostaDTO;
import br.jus.je.pje.business.dto.RespostaTiposVotoDTO;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoLido;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.SessaoProcessoMultDocsVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;

/**
 * Classe responsvel por controlar as requisies da pgina xhtml/flx/votacaoVogalMultDocs.xhtml
 * 
 * @author carlos
 */
@Name(VotacaoVogalMultDocsCkEditorAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class VotacaoVogalMultDocsCkEditorAction extends CkEditorGeraDocumentoAbstractAction implements Serializable, ArquivoAssinadoUploader {
	
	private static final long serialVersionUID = -6092748770177071655L;

	public static final String NAME = "votacaoVogalMultDocsCkEditorAction";
	
	private static final String[] colors = {
		"#FF0000", "#9400D3", "#63B8FF", "#2E8B57", "#1E90FF", 
		"#CD6090", "#B03060", "#006400", "#B22222", "#CD853F", 
		"#BC8F8F", "#CD5C5C","#00BFFF", "#7FFF00", "#B3EE3A", 
		"#EEE685", "#FFA500", "#8B5A00", "#FF7256", "#8B3626", 
		"#8B0000", "#EEC900", "#AB82FF", "#8B7355", "#FFDAB9", 
		"#8FBC8F", "#7CFC00", "#B03060", "#FF00FF", "#DA70D6", 
		"#00FF00", "#8B864E"};
	
	@Logger
	protected transient Log logger;
	
	@In(required=false)
	protected TaskInstance taskInstance;
	
	private int ncolor = 0;
	
	private String papeisTransicaoAutomatica;
	
	private List<TipoProcessoDocumento> tiposProcessoDocumento = new ArrayList<TipoProcessoDocumento>(0);
	
	private List<ModeloDocumento> modelosDocumento = new ArrayList<ModeloDocumento>(0);
	
	private ModeloDocumento modeloDocumento;
	
	private Boolean transitarAutomaticamente;
	
	private Map<Integer, Set<Integer>> placar;
	
	private List<SessaoProcessoDocumento> elementosJulgamento;
	
	private List<SessaoProcessoDocumentoVoto> demaisVotos;
	
	private List<OrgaoJulgador> possiveisAcompanhados = new ArrayList<OrgaoJulgador>();
	
	private Sessao sessao;
	
	private SessaoProcessoDocumentoVoto votoRelator;
	
	private SessaoProcessoDocumentoVoto voto;
	
	private boolean redigir = false;
	
	private Boolean permitirVotarSemCondutor = false;
	
	private String manipulaLiberacao = null;
	
	private Boolean manipulaLiberacao_ = null;
		
	private List<SessaoProcessoMultDocsVoto> listSessaoProcDocVoto;
	
	private List<ProcessoDocumento> listDocsVoto;
	
	private ProcessoDocumento processoDocumento;
	
	private Set<Integer> impedidos;
	
	private Set<Integer> omissos;
	
	private Map<Integer, String> nomeOrgao;
	
	private Map<Integer, String> colorsMap;
	
	private Set<Integer> listaTipoVotoDivergentes;
	
	private Set<Integer> listaTipoVotoNaoConhece;
	
	private OrgaoJulgador orgaoAtual;
	
	protected ProcessoTrf processoJudicial;
	
	protected String transicaoPadrao;
	
	private boolean uploadArquivoAssinadoRealizado;
	
	private boolean liberaVoto = false;

	private boolean destacarVoto = false;
	
	private Sessao sessaoSugerida;
	
	/**
	 * Inicializa o componente, especialmente o processo judicial a ser gerenciado e a transio padro
	 * de sada, se definida em fluxo.
	 * Caso seja necessria a sobrecarga, recomenda-se que se inclua, na classe derivada, chamada a 
	 * super.init().
	 * 
	 */
	@Create
	public void init(){
		setProtocolarDocumentoBean(new ProtocolarDocumentoBean(ComponentUtil.getTaskInstanceUtil().getProcesso(ComponentUtil.getTaskInstanceUtil().getProcessInstance().getId()).getIdProcessoTrf(), ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.RECUPERA_DOCUMENTO_FLUXO, NAME));
		processoJudicial = ComponentUtil.getTramitacaoProcessualService().recuperaProcesso();
		sessaoSugerida = processoJudicial.getSessaoSugerida();
		carregarVariaveisParametrizadas();
		carregarTransicaoPadrao();
	}
	
	public void carregarTransicaoPadrao() {
		if (transicaoPadrao == null) {
			transicaoPadrao = (String) ComponentUtil.getTaskInstanceUtil().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		}
	}
	
	private void carregarVariaveisParametrizadas(){
			permitirVotarSemCondutor = (Boolean) recuperarVariavel("pje:flx:votacaoVogal:flg:permitirVotoSemCondutor");
			manipulaLiberacao = (String) recuperarVariavel("pje:flx:votacaoVogal:exp:manipulaLiberacao");
			papeisTransicaoAutomatica = (String) recuperarVariavel("pje:flx:votacaoVogal:transicaoAutomatica:papeis");
	}
	
	private Object recuperarVariavel(String variavelFluxo){
		Object variavelTarefa = ComponentUtil.getTramitacaoProcessualService().recuperaVariavelTarefa(variavelFluxo);
		
		if(variavelTarefa == null){
			variavelTarefa = ComponentUtil.getTramitacaoProcessualService().recuperaVariavel(variavelFluxo);
		}
		
		return variavelTarefa;		
	}

	/**
	 * Inicializa os Documentos.
	 */
	private void inicializarDocumentos(){
		
		colorsMap = new HashMap<Integer, String>();
		nomeOrgao = new HashMap<Integer, String>();
		
		List<SessaoProcessoDocumento> aux = ComponentUtil.getSessaoProcessoDocumentoManager().recuperaElementosJulgamento(
				processoJudicial, null, Authenticator.getOrgaoJulgadorAtual());
		
		elementosJulgamento = new ArrayList<SessaoProcessoDocumento>(aux.size());
		demaisVotos = new ArrayList<SessaoProcessoDocumentoVoto>();
		for(SessaoProcessoDocumento spd: aux){
			if(SessaoProcessoDocumentoVoto.class.isAssignableFrom(spd.getClass())){
				SessaoProcessoDocumentoVoto v = (SessaoProcessoDocumentoVoto) spd;
				if(v.getOrgaoJulgador().equals(v.getProcessoTrf().getOrgaoJulgador())){
					votoRelator = v;
				}else if(v.getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual())){
					voto = v;
					setLiberaVoto(v.getLiberacao());
					setDestacarVoto(v.getDestaqueSessao());

					if(voto.getProcessoDocumento() != null){
						redigir = true;
						getProtocolarDocumentoBean().setDocumentoPrincipal(voto.getProcessoDocumento());
					}
				}else{
					demaisVotos.add(v);
				}
			}else{
				elementosJulgamento.add(spd);
			}
			if(sessao == null && spd.getSessao() != null){
				sessao = spd.getSessao();
			}
		}
		if(votoRelator == null && processoJudicial.getOrgaoJulgadorRevisor() != null && processoJudicial.getOrgaoJulgadorRevisor().equals(Authenticator.getOrgaoJulgadorAtual()) ) {
			votoRelator = ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVotoAntecipado(processoJudicial, processoJudicial.getOrgaoJulgador());
		}
		
		initVoto();
		
		if(getProcessoDocumento() == null){
			this.processoDocumento = getDocVazio();
		}
		
		modeloDocumento = null;
		tiposProcessoDocumento = null;
		
		if(getProtocolarDocumentoBean() == null){
			setProtocolarDocumentoBean(new ProtocolarDocumentoBean(voto.getProcessoTrf().getIdProcessoTrf(),
					ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.RECUPERA_DOCUMENTO_FLUXO,NAME));
		}
		
		setOrgaoAtual(Authenticator.getOrgaoJulgadorAtual());
		getPossiveisAcompanhados();
		carregarPlacar();

	}

	private void initVoto() {
		if(voto == null){
			voto = new SessaoProcessoDocumentoVoto();
			voto.setCheckAcompanhaRelator(false);
			voto.setDestaqueSessao(false);
			voto.setImpedimentoSuspeicao(false);
			voto.setLiberacao(false);
			voto.setProcessoTrf(getProcessoJudicial());
			voto.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
			if(sessao != null) {
				voto.setSessao(sessao);
			}
		}
	}

	/**
	 * Metodo que excluir os votos escritos ao acionar o boto de Votar sem documento
	 * aps a confirmao.
	 * @author rafaelmatos
	 * @since 24/07/2015
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-19559
	 */
	public void excluirDocsGravarVotoSemDoc(){
        try {
            excluirDocs();
            removerVotoEscrito();
            this.processoDocumento = null;
            this.voto = null;
            inicializarDocumentos();
        } catch (PJeBusinessException e) {
            logger.error("Houve um erro ao tentar remover voto escrito: {0}.", e.getLocalizedMessage());
        }
    }

	private void excluirDocs() throws PJeBusinessException {
		if(listDocsVoto != null) {
		    for (ProcessoDocumento doc : listDocsVoto) {
		        SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto = recuperarVoto(doc);
		        if (sessaoProcessoDocumentoVoto != null) {
		            ComponentUtil.getSessaoProcessoDocumentoVotoManager().refresh(sessaoProcessoDocumentoVoto);
		            ComponentUtil.getSessaoProcessoDocumentoVotoManager().remove(sessaoProcessoDocumentoVoto);
		            ComponentUtil.getSessaoProcessoDocumentoVotoManager().flush();
		            ComponentUtil.getDocumentoJudicialService().remove(doc);
		            ComponentUtil.getDocumentoJudicialService().flush(); 
		        }
		    }
		}
		ComponentUtil.getFacesMessages().add(Severity.INFO, "Voto(s) excluído(s) com sucesso.");
	}

	public void modificarModelo(Integer idModelo){
		ModeloDocumento m;
		try {
			m = ComponentUtil.getModeloDocumentoManager().findById(idModelo);
			voto.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(ComponentUtil.getModeloDocumentoManager().obtemConteudo(m));
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar atualizar o modelo: {0}.", e.getLocalizedMessage());
		}
	}
	
	public void redigirVoto(){
		redigir = true;
		if(getProcessoDocumento() == null){
			this.processoDocumento = getDocVazio();
		}
		
		modeloDocumento = null;
		tiposProcessoDocumento = null;
	}

	/**
	 * Retorna o documento vazio com algumas informaes j setadas.
	 * 
	 * @return
	 */
	private ProcessoDocumento getDocVazio() {
		ProcessoDocumento doc = ComponentUtil.getDocumentoJudicialService().getDocumento();
		doc.setProcesso(processoJudicial.getProcesso());
		doc.setProcessoTrf(processoJudicial);
		doc.setTipoProcessoDocumento(new TipoProcessoDocumento());
		return doc;
	}
	
	public void removerVotoEscrito() throws PJeBusinessException{
		redigir = false;
		voto.setProcessoDocumento(null);
		listDocsVoto = null;
		ComponentUtil.getFacesMessages().add(Severity.INFO, "Minuta de voto removida com sucesso");
	}
	
	/**
	 * Remove o documento na sessaoProcessoMultDocsVoto e na ProcessoDocumento.
	 * 
	 * @param doc
	 */
	private void removerDocMult(SessaoProcessoMultDocsVoto doc) {
		try {
			voto.getSessaoProcessoMultDocsVoto().remove(doc);
			ProcessoDocumento processoDocumento = doc.getProcessoDocumento();
			ComponentUtil.getSessaoProcessoMultDocsVotoManager().remover(processoDocumento.getIdProcessoDocumento());
			ComponentUtil.getDocumentoJudicialService().remove(processoDocumento);
			processarDocumentoLido(doc.getProcessoDocumento());
			ComponentUtil.getSessaoProcessoDocumentoVotoManager().refresh(voto);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Remove somente na sessaoProcessoMultDocsVoto.
	 * 
	 * @param doc
	 */
	private void removerSomenteDocMult(ProcessoDocumento doc){
		SessaoProcessoMultDocsVoto docSessaMult = ComponentUtil.getSessaoProcessoMultDocsVotoManager().recuperarSessaoProcessoDoc(doc);
		try {
			voto.getSessaoProcessoMultDocsVoto().remove(docSessaMult);
			ComponentUtil.getSessaoProcessoMultDocsVotoManager().remover(doc.getIdProcessoDocumento());
			ComponentUtil.getSessaoProcessoDocumentoVotoManager().refresh(voto);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Processa os modelos de acordo com o tipo selecionado
	 */
	public void processarModelo(){
		if(getModeloDocumento()!=null){
			getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(getModeloDocumento().getModeloDocumento());
		}
	}
	
	public void registrarImpedimento() throws PJeBusinessException{
		excluirDocs();
		this.listDocsVoto = null;
		if (voto.getProcessoDocumento()==null) {
			ComponentUtil.getSessaoProcessoDocumentoManager().remove(voto);
	        ComponentUtil.getSessaoProcessoDocumentoManager().flush();
		}
		this.voto = null;
		initVoto();
		redigir = false;
        voto.setImpedimentoSuspeicao(true);
        if (voto.getOjAcompanhado() == null){
            voto.setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
        }
        ProcessoDocumento doc = voto.getProcessoDocumento();
        if(verificarDocumentoParaImpedimento(doc)){
            voto.setProcessoDocumento(null);
        }
        try {
            ComponentUtil.getSessaoProcessoDocumentoManager().persistAndFlush(voto);
        } catch (PJeBusinessException e) {
            logger.error("Houve um erro ao tentar gravar o voto: {0}.", e.getLocalizedMessage());
        }
    }
	
	/**
	 * Verifica as caractersticas do documento para setar corretamento
	 * o impedimento do documento.
	 * @param doc
	 * @return
	 */
	private boolean verificarDocumentoParaImpedimento(ProcessoDocumento doc){
		if(doc != null && doc.getProcessoDocumentoBin() != null && 
			(doc.getProcessoDocumentoBin().getModeloDocumento() == null || 
				doc.getProcessoDocumentoBin().getModeloDocumento().isEmpty())){
			
			return true;
		}
		return false;
	}
	
	public void removerImpedimento(){
        try {
            ComponentUtil.getSessaoProcessoDocumentoManager().remove(voto);
            ComponentUtil.getSessaoProcessoDocumentoManager().flush();
            this.voto = null;
            this.processoDocumento = null;
            inicializarDocumentos();
            setIdTipoVotoSelecionado(-1);
        } catch (PJeBusinessException e) {
            logger.error("Houve um erro ao tentar gravar o voto: {0}.", e.getLocalizedMessage());
        }
    }

	public boolean podeAlterarVoto(){
		ProcessoDocumento processoDocumento = this.voto.getProcessoDocumento();
		Boolean retorno = true;
		if (processoDocumento != null && processoDocumento.getIdProcessoDocumento() > 0) {
			ProcessoDocumentoBin processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
			if(processoDocumentoBin != null 
				&& processoDocumentoBin.getIdProcessoDocumentoBin() > 0 
				&& !StringUtils.isEmpty(processoDocumentoBin.getSignature())){
				retorno = false;
			}
		}
		return retorno;
	}
	
	public boolean isPodeRemoverAssinatura(){
		if(isAssinado(getProcessoDocumento()) && getProcessoDocumento().getIdProcessoDocumento() != 0){
			return true;
		}
		return false;
	}
	
	public void removerAssinaturaVoto(){
		ProcessoDocumento processoDocumento = getProcessoDocumento();
		if(processoDocumento != null){
			ComponentUtil.getAssinaturaDocumentoService().removeAssinatura(processoDocumento);
		}
	}
	
	public boolean isDocumentoVazio(){
		boolean retorno = true;
		if(getProcessoDocumento() != null && getProcessoDocumento().getProcessoDocumentoBin() != null) {
			String conteudo = getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento(); 
			if( conteudo != null && !conteudo.trim().equals("")) {
				retorno = false;
			}
		}
		return retorno; 
	}
	
	@Override
	public boolean isDocumentoPersistido(){
		return (getProcessoDocumento() != null && getProcessoDocumento().getIdProcessoDocumento() > 0); 
	}
	
	public void atualizarLiberacaoVoto() {		
		if (verificarVoto()) {
			voto.setLiberacao(isLiberaVoto());
			gravarVoto();
			ComponentUtil.getFacesMessages().clear();
			if (isLiberaVoto()){
				ComponentUtil.getFacesMessages().addFromResourceBundle(Severity.INFO, "Voto liberado com sucesso.");
			}else{
				ComponentUtil.getFacesMessages().addFromResourceBundle(Severity.WARN, "Liberação removida com sucesso.");
			}
		} else {
			setLiberaVoto(false);
		}
	}
	
	public void atualizarDestaqueVoto() {
		if (verificarVoto()) {
			voto.setDestaqueSessao(isDestacarVoto());
			gravarVoto();
			ComponentUtil.getFacesMessages().clear();
			if (isDestacarVoto()){
				ComponentUtil.getFacesMessages().addFromResourceBundle(Severity.INFO, "Voto destacado com sucesso.");
			}else{
				ComponentUtil.getFacesMessages().addFromResourceBundle(Severity.WARN, "Destaque removido com sucesso.");
			}
		} else {
			setDestacarVoto(false);
		}
	}
	
	public boolean verificarVoto() {
		boolean retorno = true;
		
		if (voto.getTipoVoto() == null){
			ComponentUtil.getFacesMessages().addFromResourceBundle(Severity.ERROR, "Selecione o tipo de voto");
			retorno = false;
		}
		
		if (!isDocumentoPersistido()) {
			ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Não é possível liberar ou destacar um documento vazio.");
			retorno = false;
		}
		
		return retorno;
	}
	
	public void gravarVoto(){
		try {
			if(getProcessoDocumento().getIdProcessoDocumento() == 0 || !verificarDocEmSessaoProcMultDocs(getProcessoDocumento())){
				tratarAcompanhamento();
				gravarDocumento(getProcessoDocumento());
				voto.setDtVoto(new Date());
				voto.setProcessoDocumento(getProcessoDocumento());
				ComponentUtil.getSessaoProcessoDocumentoManager().persist(voto);
				ComponentUtil.getSessaoProcessoDocumentoManager().flush();
				replicarDoc(voto, getProcessoDocumento());
				ncolor = 0;
				if (this.listDocsVoto == null){
					this.listDocsVoto = new ArrayList<ProcessoDocumento>();
				}
				this.listDocsVoto.add(this.processoDocumento);
			}else{
				alterarDocVoto();
			}
			carregarPlacar();
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar gravar o voto: {0}.", e.getLocalizedMessage());
		}
	}

	/**
	 * Reseta os valores para exibio do texto do voto e das combos
	 * Tipo Documento do Voto e Modelo de documento.
	 */
	private void resetarComponentes() {
		this.processoDocumento = getDocVazio();
		this.modeloDocumento = null;
		this.redigir = false;
		ComponentUtil.getFacesMessages().clear();
	}
	
	public void novoDocVoto(){
		resetarComponentes();
		redigir = true;
	}
	
	public void gravarVotoSemDoc(){
		try {
			ComponentUtil.getFacesMessages().clear();
			this.processoDocumento = null;
			tratarAcompanhamento();
			voto.setDtVoto(new Date());
			voto.setProcessoDocumento(this.processoDocumento);
			
			ComponentUtil.getSessaoProcessoDocumentoManager().persist(voto);
			ComponentUtil.getSessaoProcessoDocumentoManager().flush();
			ncolor = 0;
			carregarPlacar();
			taskInstance.getProcessInstance().getContextInstance().setVariable(Variaveis.VARIAVEL_ID_VOT_COLEGIADO, voto.getIdSessaoProcessoDocumento());
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	private boolean verificarDocEmSessaoProcMultDocs(ProcessoDocumento proc) {
		SessaoProcessoMultDocsVoto docSessaMult = ComponentUtil.getSessaoProcessoMultDocsVotoManager().recuperarSessaoProcessoDoc(proc);
		return docSessaMult != null;
	}

	private void gravarProcessoDocumento(SessaoProcessoDocumentoVoto voto) throws PJeBusinessException {
		ComponentUtil.getSessaoProcessoDocumentoManager().persist(voto);
		ComponentUtil.getSessaoProcessoDocumentoManager().flush();
	}
	
	private void alterarDocVoto() {
		try {
			ComponentUtil.getProcessoDocumentoManager().persist(getProcessoDocumento());
			ComponentUtil.getProcessoDocumentoManager().flush();
			
			ComponentUtil.getControleVersaoDocumentoManager().salvarVersaoDocumento(getProcessoDocumento());
		} catch (PJeBusinessException e) {
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Erro ao atualizar o Documento: {0}", e.getMessage());
			e.printStackTrace();
		}
	}

	private void tratarAcompanhamento() throws PJeBusinessException{
		if (voto.getTipoVoto()==null){
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Selecione o tipo de voto");
			throw new PJeBusinessException("Selecione o tipo de voto");
		} else if(isPodeSelecionarOrgaoJulgadorDivergente()) {
			selecionaOrgaoJulgadorDivergente();
		} else {
			voto.setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
		}
		
		voto.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		voto.setSessao(sessao);
	}
	
	/**
	 * Verifica de acordo com o tipo de voto selecionado
	 * se permite alteração de órgão julgador acompanhado
	 * @return isPodeSelecionar
	 */
	private boolean isPodeSelecionarOrgaoJulgadorDivergente() {
		boolean isPodeSelecionar = false;
		
		if(voto.getTipoVoto().getContexto().equals("D")) {
			if(voto.getOjAcompanhado() == null) {
				isPodeSelecionar = "Acompanho a divergência".equals(voto.getTipoVoto().getTipoVoto());
				
			} else if (!"Diverge do Relator".equals(voto.getTipoVoto().getTipoVoto()) 
					&& !"Diverge em Parte com o Relator".equals(voto.getTipoVoto().getTipoVoto())
					&& !"Divirjo do relator".equals(voto.getTipoVoto().getTipoVoto())) {
				isPodeSelecionar = true;
			}
		}
		
		return isPodeSelecionar;
	}

	/**
	 * Metodo que seleciona rgo julgador acompanhado na situao de Acompanhar divergncia e 
	 * atribui ao voto corrente.
	 * @author rafaelmatos
	 * @since 24/07/2015
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-19559
	 * @throws PJeBusinessException
	 */
	private void selecionaOrgaoJulgadorDivergente() throws PJeBusinessException {
		OrgaoJulgador ojAcompanhado = Authenticator.getOrgaoJulgadorAtual();
		for (Integer idOjAcompanhado : getListaTipoVotoDivergentes()) {
			ojAcompanhado = ComponentUtil.getOrgaoJulgadorManager().findById(idOjAcompanhado);
		}
		voto.setOjAcompanhado(ojAcompanhado);
	}

	private void gravarDocumento(ProcessoDocumento processoDocumento) {
		try {
			this.processoDocumento = ComponentUtil.getDocumentoJudicialService().persist(processoDocumento, true);
			ComponentUtil.getDocumentoJudicialService().flush();
			
			ComponentUtil.getControleVersaoDocumentoManager().salvarVersaoDocumento(processoDocumento);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Erro ao gravar o documento do voto: {0}", e.getMessage());
		}
	}

	/**
	 * Registra o Documento na SessaoProcessoDocumentoVoto
	 * @param voto
	 */
	private void atualizarDocSessaoVoto(SessaoProcessoDocumentoVoto voto, ProcessoDocumento proc) {
		voto.setProcessoDocumento(proc);
		try {
			gravarProcessoDocumento(voto);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			ComponentUtil.getFacesMessages().add(Severity.INFO, "Erro ao salvar o Documento na Sessão.");
		}
	}

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

	private Integer getProximoNumeroOrdemDoc(SessaoProcessoDocumentoVoto voto) {
		return ComponentUtil.getSessaoProcessoMultDocsVotoManager().recuperarProximoNumeroOrdemDoc(voto);
	}

	private void replicarDoc(SessaoProcessoDocumentoVoto voto,ProcessoDocumento documento) {
		if(!verificarDocEmSessaoProcMultDocs(documento)){
			gravarEmMultDocs(voto, documento);
		}
	}

	public void finalizarSemDocumento(){
		gravarVotoSemDoc();
		if(transicaoPadrao != null && !transicaoPadrao.isEmpty()){
			TaskInstanceHome tih = TaskInstanceHome.instance();
			tih.end(transicaoPadrao);
		}
	}
	
	public void transitar(){
		if(transicaoPadrao != null && !transicaoPadrao.isEmpty()){
			ProcessoHome.instance().setIdProcessoDocumento(processoJudicial.getIdProcessoTrf());
			TaskInstanceHome tih = TaskInstanceHome.instance();
			tih.end(transicaoPadrao);
		}
	}
	
	/**
	 * Finalizar o documento para devida assinatura sem o transitar na tarefa.
	 */
	public void finalizarAssinaturaSemTransitar(){
		try {
			ComponentUtil.getDocumentoJudicialService().finalizaDocumento(getProcessoDocumento(), processoJudicial, taskInstance.getId(), false, true, false, Authenticator.getPessoaLogada(), false);
			ComponentUtil.getDocumentoJudicialService().flush();
			resetarComponentes();
		} catch (PJeBusinessException e) {
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Houve um erro ao finalizar o documento: {0}", e.getLocalizedMessage());
		}
		ComponentUtil.getFacesMessages().add(Severity.INFO, "Assinatura bem sucedida.");
	}
	
	public String getDownloadLinks(){
		if(voto.getProcessoDocumento() != null){
			return ComponentUtil.getDocumentoJudicialService().getDownloadLinks(Arrays.asList(voto.getProcessoDocumento()));
		}
		return StringUtils.EMPTY;
	}
	
	public List<SessaoProcessoDocumento> getElementosJulgamento() {
		if(elementosJulgamento == null){
			inicializarDocumentos();
		}
		return elementosJulgamento;
	}
	
	public List<SessaoProcessoDocumentoVoto> getDemaisVotos() {
		if(demaisVotos == null){
			inicializarDocumentos();
		}
		return demaisVotos;
	}
	
	public SessaoProcessoDocumentoVoto getVoto() {
		if(voto == null){
			inicializarDocumentos();
		}
		return voto;
	}
	
	public void setVoto(SessaoProcessoDocumentoVoto voto) {
		this.voto = voto;
	}

	public SessaoProcessoDocumentoVoto getVotoRelator() {
		return votoRelator;
	}
	
	public boolean isRedigir() {
		return redigir;
	}
	
	public Boolean getPermitirVotarSemCondutor() {
		return permitirVotarSemCondutor;
	}

	public Map<Integer, Set<Integer>> getPlacar() {
		if (placar==null){
			carregarPlacar();
		}
		return placar;
	}
	
	public static String[] getColors() {
		return colors;
	}
	
	public String getColor(Integer oj){
		if(colorsMap.get(oj) == null){
			colorsMap.put(oj, colors[colorsMap.size()]);
		}
		return colorsMap.get(oj);
	}
	
	public String nextColor(){
		int cor = ncolor % colors.length;
		ncolor++;
		return colors[cor];
	}
	
	public boolean isLiberaVoto() {
		return liberaVoto;
	}

	public void setLiberaVoto(boolean liberaVoto) {
		this.liberaVoto = liberaVoto;
	}

	public boolean isDestacarVoto() {
		return destacarVoto;
	}

	public void setDestacarVoto(boolean destacarVoto) {
		this.destacarVoto = destacarVoto;
	}
	
	public List<OrgaoJulgador> getPossiveisAcompanhados() {
		
		try {
			possiveisAcompanhados.clear();
			
			if (voto.getTipoVoto()!=null && "D".equals(voto.getTipoVoto().getContexto())){
				for (Integer idOjAcompanhado : getListaTipoVotoDivergentes()) {
					possiveisAcompanhados.add(ComponentUtil.getOrgaoJulgadorManager().findById(idOjAcompanhado));
				}
			}else{
				possiveisAcompanhados.add(processoJudicial.getOrgaoJulgador());
			}
			
			if(!possiveisAcompanhados.contains(voto.getOjAcompanhado())) {
				possiveisAcompanhados.add(voto.getOjAcompanhado());
			}
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar recuperar órgão julgador acompanhado: {0}.", e.getLocalizedMessage());
		}
		
		return possiveisAcompanhados;
	}
	
	public Boolean getManipulaLiberacao() {
		if(manipulaLiberacao_ == null){
			if(manipulaLiberacao != null && !manipulaLiberacao.isEmpty()){
				try{
					if(!manipulaLiberacao.startsWith("#{")){
						manipulaLiberacao = "#{" + manipulaLiberacao + "}";
					}
					manipulaLiberacao_ = (Boolean) ComponentUtil.getExpressions().createValueExpression(manipulaLiberacao).getValue();
				}catch(Throwable t){
					manipulaLiberacao_ = true;
				}
			}else{
				manipulaLiberacao_ = true;
			}
		}
		return manipulaLiberacao_;
	}
	
	public Boolean getTransitarAutomaticamente() {
		if(transitarAutomaticamente == null){
			if(papeisTransicaoAutomatica == null || papeisTransicaoAutomatica.isEmpty()){
				transitarAutomaticamente = false;
			}else{
				String[] papeis = papeisTransicaoAutomatica.split(",");
				Identity identity = Identity.instance();
				for(String p: papeis){
					if(identity.hasRole(p)){
						transitarAutomaticamente = true;
						break;
					}
				}
			}
		}
		return transitarAutomaticamente;
	}
	
	public List<SessaoProcessoMultDocsVoto> getListSessaoProcDocVoto() {
		return listSessaoProcDocVoto;
	}

	/**
	 * Mtodo que carrega os Documentos da Sessao do Voto
	 */
	private void carregarDocsVoto() throws PJeBusinessException {
		List<ProcessoDocumento> listDocs = new ArrayList<ProcessoDocumento>();
		List<SessaoProcessoMultDocsVoto> listMultDocsVoto = new ArrayList<SessaoProcessoMultDocsVoto>();
		if(voto.getIdSessaoProcessoDocumento() != 0){
			List<SessaoProcessoMultDocsVoto> multDocsVoto = ComponentUtil.getSessaoProcessoMultDocsVotoManager().recuperarDocsVoto(voto);
			if(ProjetoUtil.isNotVazioSize(multDocsVoto)){
				listMultDocsVoto.addAll(multDocsVoto);
			}
		}
		if(ProjetoUtil.isNotVazioSize(listMultDocsVoto)){
			for(SessaoProcessoMultDocsVoto docsVoto : listMultDocsVoto){
				listDocs.add(docsVoto.getProcessoDocumento());
			}
			setListDocsVoto(listDocs);
		}
	}
	
	/**
	 * Remove o Documento do voto.
	 * @param procDoc
	 */
	public void removerDocumento(ProcessoDocumento procDoc){
		try {
			listDocsVoto = new ArrayList<ProcessoDocumento>();
			SessaoProcessoDocumentoVoto procDocVoto;
			procDocVoto = recuperarVoto(procDoc);
			if(procDocVoto != null){
				removerSomenteDocMult(procDoc);
				isProcessoNaoLido(procDoc);
				verificarUltimoDoc();
			}else{
				removerDocMultDocs(procDoc);
			}
			resetarComponentes();
			ComponentUtil.getFacesMessages().add(Severity.INFO, "Documento removido com sucesso.");
		} catch (PJeBusinessException e) {
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Erro ao excluir voto.");
			e.printStackTrace();
		}
	}

	/**
	 * Caso tenha processoDocumento no  necessrio excluir o documento pois,
	 * j sera apagado na ProcessoDocumentoLido
	 * @param procDoc ProcessoDocumento
	 */
	private void isProcessoNaoLido(ProcessoDocumento procDoc) throws PJeBusinessException {
		if(!processarDocumentoLido(procDoc)){
			ComponentUtil.getProcessoDocumentoManager().inactivate(procDoc);
			ComponentUtil.getProcessoDocumentoManager().flush();
			ComponentUtil.getSessaoProcessoDocumentoManager().refresh(voto);
		}
	}

	/**
	 *Ao remover o documento da SessaoProcessoDocumentoVoto  necessrio
	 *inserir o ultimo(de acordo com a data incluso) doc da SessaoProcessoMultDocs
	 */
	private void verificarUltimoDoc() {
		ProcessoDocumento ultimoDoc = recuperarUltimoDoc(voto);
		if(ultimoDoc != null){
			atualizarDocSessaoVoto(voto, ultimoDoc);
		}
	}

	/**
	 * Verifica se o existe o registro na processoDocumento,
	 * O Fato ocorre quando o documento  assinado.
	 * @param procDoc
	 */
	private boolean processarDocumentoLido(ProcessoDocumento procDoc) {
		ProcessoDocumentoLido procDocLido;
		procDocLido = ComponentUtil.getProcessoDocumentoLidoManager().recuperarDocumentoLido(procDoc);
		if(procDocLido!=null){
			removerProcessoDocumentoLido(procDocLido);
			return true;
		}
		return false;
	}

	private void removerProcessoDocumentoLido(ProcessoDocumentoLido procDocLido) {
		if(procDocLido!= null && procDocLido.getIdProcessoDocumentoLido() != 0){
			try {
				ComponentUtil.getProcessoDocumentoLidoManager().remove(procDocLido);
				ComponentUtil.getProcessoDocumentoLidoManager().flush();
			} catch (PJeBusinessException e) {
				e.printStackTrace();
				ComponentUtil.getFacesMessages().add(Severity.ERROR, "Erro ao tentar excluir processoDocumentoLido: {0}", e.getMessage());
			}
		}
	}

	/**
	 * Documento somente existente da sessaoProcessoMultDocsVoto
	 * @param procDoc ProcessoDocumento
	 */
	private void removerDocMultDocs(ProcessoDocumento procDoc) {
		SessaoProcessoMultDocsVoto doc = ComponentUtil.getSessaoProcessoMultDocsVotoManager().recuperarSessaoProcessoDoc(procDoc);
		removerDocMult(doc);
			
	}
	
	private ProcessoDocumento recuperarUltimoDoc(SessaoProcessoDocumentoVoto voto) {
		SessaoProcessoMultDocsVoto sessaoProcessoMultDocsVoto = ComponentUtil.getSessaoProcessoMultDocsVotoManager().recuperarUltimoDoc(voto);
		return sessaoProcessoMultDocsVoto != null ? sessaoProcessoMultDocsVoto.getProcessoDocumento() : null;
	}
	
	public boolean isManaged() {
		return this.processoDocumento != null && this.processoDocumento.getIdProcessoDocumento() != 0;
	}
	
	public boolean isExibeRemoverVotoEscrito(){
		if(listDocsVoto != null && listDocsVoto.size() > 0 || !isRedigir()){
			return false;
		}
		return true;
	}
	
	public boolean isSomenteLeitura(){
		if(isAssinado(getProcessoDocumento())){
			return true;
		}
		return false;
	}
	
	public boolean isAssinado(ProcessoDocumento proc){
		return ComponentUtil.getAssinaturaDocumentoService().isProcessoDocumentoAssinado(proc);
	}
	
	public void assinarDocumento(){
		try {
			ComponentUtil.getDocumentoJudicialService().finalizaDocumento(getProcessoDocumento(), processoJudicial, taskInstance.getId(), false, true, false, Authenticator.getPessoaLogada(), false);
			ComponentUtil.getDocumentoJudicialService().flush();
			resetarComponentes();
		}catch (Exception e) {
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Houve um erro ao assinar o documento: {0}", e.getLocalizedMessage());
		}
	}
	
	public String formatarSelecionado(ProcessoDocumento doc){
		if(doc.getIdProcessoDocumento() == getProcessoDocumento().getIdProcessoDocumento()){
			return "info";
		}
		return StringUtils.EMPTY;
	}
	
	private SessaoProcessoDocumentoVoto recuperarVoto(ProcessoDocumento procDoc) {
		return ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVoto(procDoc);
	}

	public void editarDocumento(ProcessoDocumento doc){
		setProcessoDocumento(doc);
	}

	public void setListSessaoProcDocVoto(List<SessaoProcessoMultDocsVoto> list) {
		this.listSessaoProcDocVoto = list;
	}

	public ProcessoDocumento getProcessoDocumento() {
		if (processoDocumento==null && ProjetoUtil.isNotVazio(getListDocsVoto())){
			processoDocumento = listDocsVoto.get(0); 
		}
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.redigir = true;
		this.processoDocumento = processoDocumento;
		getProtocolarDocumentoBean().setDocumentoPrincipal(processoDocumento);
		voto.setProcessoDocumento(processoDocumento);
	}

	public List<ProcessoDocumento> getListDocsVoto() {
		if(ProjetoUtil.isVazio(listDocsVoto)){
			try {
				carregarDocsVoto();
			} catch (PJeBusinessException e) {
				e.printStackTrace();
				ComponentUtil.getFacesMessages().add(Severity.ERROR, "Erro ao listar os documentos: {0}", e.getMessage());
			}
		}	
		if(ProjetoUtil.isNotVazio(listDocsVoto)){
			Collections.sort(listDocsVoto, new ProcessoDocumentoDataInclusaoComparator());
		}
		return listDocsVoto;
	}

	public void setListDocsVoto(List<ProcessoDocumento> listDocsVoto) {
		this.listDocsVoto = listDocsVoto;
	}
	
	/**
	 * Mtodo responsvel por retornar um array de inteiros. Esse array ser composto pelos identificadores 
	 * cadastrados no parmetro de sistema "pje:flx:votacaoVogal:tiposVoto:ids". Caso haja identificadores cadastrados 
	 * na varivel de tarefa "tiposDisponiveisIds", os identificadores retornados sero aqueles que esto cadastrados
	 * tanto no parmetro de sistema quando na varivel de tarefa.
	 * 
	 * @return Um array de inteiros.
	 */
	private Integer[] getIds(){
		Integer[] idsFromParametroSistema = ParametroUtil.instance().getIdsTipoDocumentoVoto();
		Integer[] idsFromVariavelTarefa = Util.converterStringIdsToIntegerArray(
				(String)ComponentUtil.getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.VARIAVEL_IDS_TIPOS_DOCUMENTOS_FLUXO));
		
		List<Integer> resultIds = new ArrayList<Integer>(0);
		if (idsFromVariavelTarefa.length > 0) {
			List<Integer> list01 = Arrays.asList(idsFromParametroSistema);
			List<Integer> list02 = Arrays.asList(idsFromVariavelTarefa);
			
			for(Integer id : list01) {
				if (list02.contains(id)) {
					resultIds.add(id);
				}
			}
		} else {
			resultIds = Arrays.asList(idsFromParametroSistema);
		}
		return resultIds.toArray(new Integer[resultIds.size()]);
	}
	
	// GETTERs SETTERs

	/**
	 * Retorna uma lista de tipos de documento.
	 * 
	 * @return Uma lista de tipos de documento.
	 */
	public List<TipoProcessoDocumento> getTiposProcessoDocumento() {
		if (tiposProcessoDocumento.isEmpty()) {
			List<TipoProcessoDocumento> listaTipoProcessoDocumentoTemp = 
					ComponentUtil.getTipoProcessoDocumentoManager().findDisponiveis(Authenticator.getPapelAtual(), getIds());
			if (!getProcessoDocumento().getProcessoTrf().getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual()) ){
				for (TipoProcessoDocumento tipoProcessoDocumento : listaTipoProcessoDocumentoTemp) {
					if (!tipoProcessoDocumento.getTipoProcessoDocumento().equals("Voto Relator")){
						tiposProcessoDocumento.add(tipoProcessoDocumento);
					}
				}
			}else{
				tiposProcessoDocumento.addAll(listaTipoProcessoDocumentoTemp);
			}
		}
		return tiposProcessoDocumento;
	}
	
	/**
	 * Retorna uma lista de modelos de documento que esto relacionados ao tipo de documento selecionado.
	 * 
	 * @return Uma lista de modelos de documento que esto relacionados ao tipo de documento selecionado.
	 */
	public List<ModeloDocumento> getModelosDocumento() {
		this.modelosDocumento.clear();
		
		if(this.processoDocumento.getTipoProcessoDocumento() != null){
			try {
				this.modelosDocumento.addAll(ComponentUtil.getDocumentoJudicialService().getModelosLocais(getProcessoDocumento().getTipoProcessoDocumento()));
				
			} catch (PJeBusinessException ex) {
				ComponentUtil.getFacesMessages().add(Severity.ERROR, ex.getMessage());
			}
		}
		return this.modelosDocumento;
	}

	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}
	
	private void carregarPlacar(){
		try {
			PlacarSessaoVO p = ComponentUtil.getSessaoProcessoDocumentoVotoManager().getPlacarCondutores(sessao, getProcessoJudicial(), false);
			placar = p.getMapaPlacar();
			impedidos = ComponentUtil.getSessaoProcessoDocumentoVotoManager().getImpedidos(sessao, getProcessoJudicial(), false);
			if (sessao!=null){
				omissos = ComponentUtil.getSessaoProcessoDocumentoVotoManager().getOmissos(sessao, getProcessoJudicial(), false);
			}else{
				omissos = new HashSet<Integer>();
			}
			listaTipoVotoDivergentes = ComponentUtil.getSessaoProcessoDocumentoVotoManager().getVotosPorTipoContextoDivergencia(sessao, getProcessoJudicial(), false);
			listaTipoVotoNaoConhece = ComponentUtil.getSessaoProcessoDocumentoVotoManager().getVotosPorTipo(sessao, getProcessoJudicial(), ComponentUtil.getTipoVotoManager().recuperaNaoConhece(), false);
			placar.put(-1, omissos);
			placar.put(-2, impedidos);
			placar.put(-3, listaTipoVotoDivergentes);
			placar.put(-4, listaTipoVotoNaoConhece);
			if (sessao==null){
				atualizaPlacarSemSessao();
			}
		} catch (Exception e) {
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Erro ao carregar placar: {0}", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Metodo responsavel por atualizar placar quando no houver sesso
	 */
	private void atualizaPlacarSemSessao() {
		for(Entry<Integer, Set<Integer>> placar : this.placar.entrySet()) {
		    if (placar.getKey() > 0 && CollectionUtils.isNotEmpty(placar.getValue())){
		    	Iterator<Integer> idsOrgaoJulgadoresNoPlacar = placar.getValue().iterator();
		    	while (idsOrgaoJulgadoresNoPlacar.hasNext()){
		    		Integer idOrgaoJulgador = idsOrgaoJulgadoresNoPlacar.next();
		    		if (listaTipoVotoNaoConhece.contains(idOrgaoJulgador)){
						idsOrgaoJulgadoresNoPlacar.remove();
					}
					if (listaTipoVotoDivergentes.contains(idOrgaoJulgador)){
						idsOrgaoJulgadoresNoPlacar.remove();
					}
					if (impedidos.contains(idOrgaoJulgador)){
						idsOrgaoJulgadoresNoPlacar.remove();
					}
		    	}
		    }
		}
	}
	
	/**
	 * Metodo responsvel por retorna uma lista apenas com os rgos julgadores 
	 * que votaram com o tipo de voto divergente.
	 * 
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-17870
	 * @return Map<Integer, Set<Integer>> 
	 */
	public Set<Integer> getListaTipoVotoDivergentes() {
		if (listaTipoVotoDivergentes==null){
			carregarPlacar();
		}
		if (listaTipoVotoDivergentes==null){
			listaTipoVotoDivergentes = new HashSet<Integer>();
		}
		return listaTipoVotoDivergentes;
	}
	
	/**
	 * Metodo responsvel por retorna uma lista apenas com os rgos julgadores 
	 * que votaram com o tipo de voto no conhece.
	 * 
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-17870
	 * @return Map<Integer, Set<Integer>> 
	 */
	public Set<Integer> getListaTipoVotoNaoConhece() {
		if (listaTipoVotoNaoConhece==null){
			carregarPlacar();
		}
		return listaTipoVotoNaoConhece;
	}
	
	public String getNomeOrgao(Integer idOrgaoJulgador){
		if (idOrgaoJulgador>0){
			if(nomeOrgao.get(idOrgaoJulgador) == null){
				try {
					nomeOrgao.put(idOrgaoJulgador, ComponentUtil.getOrgaoJulgadorManager().findById(idOrgaoJulgador).getOrgaoJulgador());
				} catch (PJeBusinessException e) {
					logger.error("Erro ao tentar recuperar o nome do órgão.");
				}
			}
			return nomeOrgao.get(idOrgaoJulgador);
		}
		return "";
	}

	public Set<Integer> getImpedidos() {
		return impedidos;
	}

	public void setImpedidos(Set<Integer> impedidos) {
		this.impedidos = impedidos;
	}

	public Set<Integer> getOmissos() {
		return omissos;
	}

	public void setOmissos(Set<Integer> omissos) {
		this.omissos = omissos;
	}

	public OrgaoJulgador getOrgaoAtual() {
		return orgaoAtual;
	}

	public void setOrgaoAtual(OrgaoJulgador orgaoAtual) {
		this.orgaoAtual = orgaoAtual;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		uploadArquivoAssinadoRealizado = Boolean.TRUE;
	}
	
	public boolean isUploadArquivoAssinadoRealizado(){
		return uploadArquivoAssinadoRealizado;
	}

	@Override
	public String getActionName() {
		return NAME;
	}

	@Override
	public String recuperarModeloDocumento(String modeloDocumento) {
		selecionarModeloProcessoDocumento(modeloDocumento);
		getProcessoDocumento().setTipoProcessoDocumento(getTipoProcessoDocumento());
		return getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().getModeloDocumento(); 
	}
	
	@Override
	public boolean isDocumentoAssinado() throws PJeBusinessException {
		boolean retorno = false;
		ProcessoDocumento processoDocumento = this.voto.getProcessoDocumento();
		if(processoDocumento != null){
			retorno = ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(processoDocumento);
		}
		return retorno;
	}

	@Override
	public void removerAssinatura() {
		this.removerAssinaturaVoto();
	}

	@Override
	public void descartarDocumento() throws PJeBusinessException {
		Object variable = ComponentUtil.getTramitacaoProcessualService().recuperaVariavel(Variaveis.MINUTA_EM_ELABORACAO);

		if (variable != null && variable instanceof Integer) {
			getProtocolarDocumentoBean().setDocumentoPrincipal(getProcessoDocumento());
			getProtocolarDocumentoBean().acaoRemoverTodos();
		}
 		Contexts.getBusinessProcessContext().flush();
	}

	@Override
	public String obterConteudoDocumentoAtual() {
		String conteudo = getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
		
		String conteudoJson = "";
		try {
			conteudoJson = ComponentUtil.getControleVersaoDocumentoManager().obterConteudoDocumentoJSON(conteudo);
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		
		return conteudoJson;
	}

	@Override
	public void salvar(String conteudo) {
		getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(conteudo);
		getProtocolarDocumentoBean().setDocumentoPrincipal(getProcessoDocumento());
		gravarVoto();
	}
	
	@Override
	public String obterTiposVoto() {
		RespostaDTO respostaDTO = new RespostaDTO();
		
		try {
			respostaDTO.setSucesso(Boolean.TRUE);
			
			RespostaTiposVotoDTO respostaTiposVotoDTO = new RespostaTiposVotoDTO();			
			respostaTiposVotoDTO.setPodeAlterar(true);
			
			if(voto.getTipoVoto() != null) {
				TipoVoto tipoVoto = voto.getTipoVoto();
				respostaTiposVotoDTO.setSelecao(criarTipoVotoDTO(tipoVoto));
			}
			
			respostaTiposVotoDTO.setTipos(criarListaTiposVoto(ComponentUtil.getTipoVotoManager().tiposVotosVogais()));

			respostaDTO.setResposta(respostaTiposVotoDTO);
		} catch (Exception e) {
			e.printStackTrace();
			respostaDTO.setSucesso(Boolean.FALSE);
			respostaDTO.setMensagem(e.getLocalizedMessage());
		}
		
		String strRetornoTiposVotoJSON = new Gson().toJson(respostaDTO, RespostaDTO.class);

		return strRetornoTiposVotoJSON;
	}

	/**
	 * Recupera o processo judicial tratado por este componente de controle.
	 * 
	 * @return o processo judicial
	 * @see #init()
	 */
	public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}

	@Override
	public void selecionarTipoVoto(String idTipoVoto) {
		int id = Integer.parseInt(idTipoVoto);
		setIdTipoVotoSelecionado(id);
		
		try {
			voto.setTipoVoto(ComponentUtil.getTipoVotoManager().findById(id));
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao selecionar o tipo de voto: {0}.", e.getLocalizedMessage());
			ComponentUtil.getFacesMessages().add(Severity.ERROR, e.getLocalizedMessage());
		}
	}
	
	public void load(){
		inicializarDocumentos();
	}

	@Override
	public boolean podeAssinar() {
		Boolean retorno = false;
		
		if (isDocumentoPassivelDeAssinatura()){
			retorno = ComponentUtil.getDocumentoJudicialService().podeAssinar(voto.getProcessoDocumento().getTipoProcessoDocumento(), Authenticator.getPapelAtual());
		}
		
		return retorno;
	}
	
	/**
	 * Verifica se o tipo de documento foi definido,
	 * se o documento nao esta vazio,
	 * se o documento foi salvo,
	 * e se o documento nao foi assinado
	 * @return
	 */
	private boolean isDocumentoPassivelDeAssinatura() {
		return isTipoProcessoDocumentoDefinido() 
				&& isDocumentoPersistido() 
				&& !isDocumentoVazio() 
				&& (voto.getProcessoDocumento()!=null && !ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(voto.getProcessoDocumento()));
	}
	
	@Override
	public String verificarPluginTipoVoto() {
		JSONObject retorno = new JSONObject();

		try {
			retorno.put("sucesso", Boolean.TRUE);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retorno.toString();
	}
	
	@Override
	public String getNomeTipoDocumentoPrincipal() {
		String retorno = "";
		
		if (voto != null && voto.getIdSessaoProcessoDocumento() > 0 && voto.getProcessoDocumento() != null) {
			retorno = voto.getProcessoDocumento().getTipoProcessoDocumento().getTipoProcessoDocumento();
		}
		
		return retorno;
	}
	
	public Sessao getSessaoSugerida() {
		return sessaoSugerida;
	}

	public void setSessaoSugerida(Sessao sessaoSugerida) {
		this.sessaoSugerida = sessaoSugerida;
	}

	public String getDataHoraSessao(Sessao sessaoSugerida){
		String retorno = "";
		if(sessaoSugerida != null) {
			retorno = ComponentUtil.getSessaoManager().getDataHoraSessao(sessaoSugerida);
		}
		return retorno;
	}
	
	public List<Sessao> getSessoesJulgamento(){
		List<Sessao> retorno = new ArrayList<Sessao>();
		if(ComponentUtil.getTramitacaoProcessualService().recuperaProcesso() != null ) {
			retorno = ComponentUtil.getSessaoManager().getSessoesJulgamentoFuturas(ComponentUtil.getTramitacaoProcessualService().recuperaProcesso().getOrgaoJulgadorColegiado());
		}
		return retorno;
	}
	
	public boolean renderizaComboSessaoSugerida(){
		boolean retorno = false;
		if(ComponentUtil.getTramitacaoProcessualService().recuperaProcesso() != null ) {
			retorno = ComponentUtil.getProcessoTrfManager().permiteIndicarPauta(ComponentUtil.getTramitacaoProcessualService().recuperaProcesso());
		}
		retorno = true;
		return retorno;
	}

}
