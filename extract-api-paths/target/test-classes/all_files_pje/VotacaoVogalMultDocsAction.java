/**
 * pje-web
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
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

import br.jus.cnj.pje.vo.PlacarSessaoVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoDocumentoDataInclusaoComparator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoLidoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoMultDocsVotoManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.view.fluxo.TramitacaoFluxoAction;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoLido;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.SessaoProcessoMultDocsVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

/**
 * Classe responsável por controlar as requisições da página xhtml/flx/votacaoVogalMultDocs.xhtml
 * 
 * @author carlos
 */
@Name("votacaoVogalMultDocsAction")
@Scope(ScopeType.CONVERSATION)
public class VotacaoVogalMultDocsAction extends TramitacaoFluxoAction implements Serializable {
	
	private static final long serialVersionUID = -6092748770177071655L;

	private static final Map<String, String> prms;
	
	private static final String[] colors = {
		"#FF0000", "#9400D3", "#63B8FF", "#2E8B57", "#1E90FF", 
		"#CD6090", "#B03060", "#006400", "#B22222", "#CD853F", 
		"#BC8F8F", "#CD5C5C","#00BFFF", "#7FFF00", "#B3EE3A", 
		"#EEE685", "#FFA500", "#8B5A00", "#FF7256", "#8B3626", 
		"#8B0000", "#EEC900", "#AB82FF", "#8B7355", "#FFDAB9", 
		"#8FBC8F", "#7CFC00", "#B03060", "#FF00FF", "#DA70D6", 
		"#00FF00", "#8B864E"};
	
	
	static {
		prms = new HashMap<String, String>();
		prms.put("identificadoresModelos", "pje:flx:votacaoVogal:modelos:ids");
		prms.put("permitirVotarSemCondutor", "pje:flx:votacaoVogal:flg:permitirVotoSemCondutor");
		prms.put("manipulaLiberacao", "pje:flx:votacaoVogal:exp:manipulaLiberacao");
		prms.put("papeisTransicaoAutomatica", "pje:flx:votacaoVogal:transicaoAutomatica:papeis");
	}
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In
	private ModeloDocumentoManager modeloDocumentoManager;
	
	@In
	private SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager;
	
	@In
	private SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;
	
	@In
	private SessaoProcessoMultDocsVotoManager sessaoProcessoMultDocsVotoManager;
	
	@In
	private Expressions expressions;
	
	@In
	private AssinaturaDocumentoService assinaturaDocumentoService;
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	
	@In
	private TramitacaoProcessualService tramitacaoProcessualService;
	
	@In
	private ProcessoDocumentoLidoManager processoDocumentoLidoManager;
	
	@In
	private ProcessoDocumentoManager processoDocumentoManager; 
	
	@In
	private OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager;
	
	@In
	private OrgaoJulgadorManager orgaoJulgadorManager;
	
	@In
	private TipoVotoManager tipoVotoManager;
	
	@In
    private TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService;
	
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
	private Boolean podeAssinar;
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

	@Override
	protected Map<String, String> getParametrosConfiguracao() {
		return prms;
	}

	/**
	 * Inicializa os Documentos.
	 */
	private void inicializarDocumentos(){
		
		colorsMap = new HashMap<Integer, String>();
		nomeOrgao = new HashMap<Integer, String>();
		
		List<SessaoProcessoDocumento> aux = sessaoProcessoDocumentoManager.recuperaElementosJulgamento(
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
		if(voto == null){
			voto = new SessaoProcessoDocumentoVoto();
			voto.setCheckAcompanhaRelator(false);
			voto.setDestaqueSessao(false);
			voto.setImpedimentoSuspeicao(false);
			voto.setLiberacao(false);
			voto.setProcessoTrf(getProcessoJudicial());
			voto.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		}
	
		if(voto.getProcessoDocumento() != null){
			redigir = true;
		}
		
		if(getProcessoDocumento() == null){
			setProcessoDocumento(getDocVazio());
		}
		
		if(voto.getProcessoDocumento() != null){
			podeAssinar = documentoJudicialService.podeAssinar(voto.getProcessoDocumento().getTipoProcessoDocumento(), Authenticator.getPapelAtual());
		}
		setOrgaoAtual(Authenticator.getOrgaoJulgadorAtual());
		getPossiveisAcompanhados();
		carregarPlacar();
	}
	
	/**
	 * Metodo que excluir os votos escritos ao acionar o botão de Votar sem documento
	 * após a confirmação.
	 * @author rafaelmatos
	 * @since 24/07/2015
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-19559
	 */
	public void excluirDocsGravarVotoSemDoc(){
		try {
			for (ProcessoDocumento doc : listDocsVoto) {
				SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto = recuperarVoto(doc);
				sessaoProcessoDocumentoVotoManager.remove(sessaoProcessoDocumentoVoto);
				SessaoProcessoMultDocsVoto sessaoProcessoMultDocsVoto = sessaoProcessoMultDocsVotoManager.recuperarSessaoProcessoDoc(doc);
				voto.getSessaoProcessoMultDocsVoto().remove(sessaoProcessoMultDocsVoto);
				sessaoProcessoMultDocsVotoManager.remove(sessaoProcessoMultDocsVoto);
				documentoJudicialService.remove(doc);
			}
			documentoJudicialService.flush();
			removerVotoEscrito();
			facesMessages.add(Severity.INFO, "Voto(s) excluído(s) com sucesso.");
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar remover voto escrito: {0}.", e.getLocalizedMessage());
		}
		
	}

	public void modificarModelo(Integer idModelo){
		ModeloDocumento m;
		try {
			m = modeloDocumentoManager.findById(idModelo);
			voto.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(modeloDocumentoManager.obtemConteudo(m));
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar atualizar o modelo: {0}.", e.getLocalizedMessage());
		}
	}
	
	public void redigirVoto(){
		redigir = true;
		if(getProcessoDocumento() == null){
			setProcessoDocumento(getDocVazio());
		}
		facesMessages.add(Severity.INFO, "Iniciada a edição de voto");
	}

	/**
	 * Retorna o documento vazio com algumas informações já setadas.
	 * 
	 * @return
	 */
	private ProcessoDocumento getDocVazio() {
		ProcessoDocumento doc = documentoJudicialService.getDocumento();
		doc.setProcesso(processoJudicial.getProcesso());
		doc.setProcessoTrf(processoJudicial);
		return doc;
	}
	
	public void removerVotoEscrito() throws PJeBusinessException{
		redigir = false;
		voto.setProcessoDocumento(null);
		listDocsVoto = null;
		facesMessages.add(Severity.INFO, "Minuta de voto removida com sucesso");
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
			sessaoProcessoMultDocsVotoManager.remove(doc);
			processarDocumentoLido(doc.getProcessoDocumento());
			documentoJudicialService.remove(processoDocumento);
			documentoJudicialService.flush();
			sessaoProcessoDocumentoVotoManager.refresh(voto);
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
		SessaoProcessoMultDocsVoto docSessaMult = sessaoProcessoMultDocsVotoManager.recuperarSessaoProcessoDoc(doc);
		try {
			sessaoProcessoMultDocsVotoManager.remove(docSessaMult);
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
	
	public void registrarImpedimento(){
		voto.setImpedimentoSuspeicao(true);
		if (voto.getOjAcompanhado() == null){
			voto.setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
		}
		ProcessoDocumento doc = voto.getProcessoDocumento();
		if(verificarDocumentoParaImpedimento(doc)){
			voto.setProcessoDocumento(null);
		}
		try {
			sessaoProcessoDocumentoManager.persistAndFlush(voto);
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar gravar o voto: {0}.", e.getLocalizedMessage());
		}
	}
	
	/**
	 * Verifica as características do documento para setar corretamento
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
			voto.setImpedimentoSuspeicao(false);
			sessaoProcessoDocumentoManager.persistAndFlush(voto);
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar gravar o voto: {0}.", e.getLocalizedMessage());
		}
	}
	
	public boolean podeAlterarVoto(){
		ProcessoDocumento processoDocumento = this.voto.getProcessoDocumento();
		Boolean retorno = true;
		if (processoDocumento != null) {
			ProcessoDocumentoBin processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
			if(processoDocumentoBin != null && !StringUtils.isEmpty(processoDocumentoBin.getSignature())){
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
			this.assinaturaDocumentoService.removeAssinatura(processoDocumento);
		}
	}
	
	public void gravarVoto(){
		try {
			if(getProcessoDocumento().getIdProcessoDocumento() == 0 || !verificarDocEmSessaoProcMultDocs(getProcessoDocumento())){
				tratarAcompanhamento();
				gravarDocumento(getProcessoDocumento());
				voto.setDtVoto(new Date());
				voto.setProcessoDocumento(getProcessoDocumento());
				sessaoProcessoDocumentoManager.persist(voto);
				sessaoProcessoDocumentoManager.flush();
				replicarDoc(voto, getProcessoDocumento());
				ncolor = 0;
				if (this.listDocsVoto == null){
					this.listDocsVoto = new ArrayList<ProcessoDocumento>();
				}
				this.listDocsVoto.add(this.processoDocumento);
				facesMessages.add(Severity.INFO, "Documento incluído com sucesso.");
			}else{
				alterarDocVoto();
			}
			carregarPlacar();
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar gravar o voto: {0}.", e.getLocalizedMessage());
		}
	}

	/**
	 * Reseta os valores para exibição do texto do voto e das combos
	 * Tipo Documento do Voto e Modelo de documento.
	 */
	private void resetarComponentes() {
		setProcessoDocumento(getDocVazio());
		this.modeloDocumento = null;
		facesMessages.clear();
	}
	
	public void novoDocVoto(){
		resetarComponentes();
	}
	
	public void gravarVotoSemDoc(){
		try {
			tratarAcompanhamento();
			voto.setDtVoto(new Date());
			sessaoProcessoDocumentoManager.persist(voto);
			sessaoProcessoDocumentoManager.flush();
			ncolor = 0;
			carregarPlacar();
			facesMessages.clear();
			facesMessages.add(Severity.INFO, "Voto registrado com sucesso.");
			taskInstance.getProcessInstance().getContextInstance().setVariable(Variaveis.VARIAVEL_ID_VOT_COLEGIADO, voto.getIdSessaoProcessoDocumento());
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	private boolean verificarDocEmSessaoProcMultDocs(ProcessoDocumento proc) {
		SessaoProcessoMultDocsVoto docSessaMult = sessaoProcessoMultDocsVotoManager.recuperarSessaoProcessoDoc(proc);
		return docSessaMult != null;
	}

	private void gravarProcessoDocumento(SessaoProcessoDocumentoVoto voto) throws PJeBusinessException {
		sessaoProcessoDocumentoManager.persist(voto);
		sessaoProcessoDocumentoManager.flush();
	}
	
	private void alterarDocVoto() {
		try {
			processoDocumentoManager.persist(getProcessoDocumento());
			processoDocumentoManager.flush();
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Erro ao atualizar o Documento: {0}", e.getMessage());
			e.printStackTrace();
		}
		facesMessages.add(Severity.INFO, "Documento alterado com sucesso.");
	}

	private void tratarAcompanhamento() throws PJeBusinessException{
		
		if (voto.getTipoVoto()==null){
			facesMessages.add(Severity.ERROR, "Selecione o tipo de voto");
			throw new PJeBusinessException("Selecione o tipo de voto");
		}
		if(voto.getTipoVoto().getContexto().equals("C")){
			voto.setOjAcompanhado(processoJudicial.getOrgaoJulgador());
		}else if(voto.getTipoVoto().getContexto().equals("P")){
			voto.setOjAcompanhado(processoJudicial.getOrgaoJulgador());
		}else if(voto.getTipoVoto().getContexto().equals("D") && voto.getOjAcompanhado() == null){
			if ("Acompanho a divergência".equals(voto.getTipoVoto().getTipoVoto())){
				selecionaOrgaoJulgadorDivergente();
			}else{
				voto.setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
			}
		}else if(voto.getTipoVoto().getContexto().equals("D") && voto.getOjAcompanhado() != null){
			if ("Diverge do Relator".equals(voto.getTipoVoto().getTipoVoto()) 
					|| "Diverge em Parte com o Relator".equals(voto.getTipoVoto().getTipoVoto())
					|| "Divirjo do relator".equals(voto.getTipoVoto().getTipoVoto())){
				voto.setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
			}else{
				selecionaOrgaoJulgadorDivergente();
			}
		}
		voto.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		voto.setSessao(sessao);
	}

	/**
	 * Metodo que seleciona órgão julgador acompanhado na situação de Acompanhar divergência e 
	 * atribui ao voto corrente.
	 * @author rafaelmatos
	 * @since 24/07/2015
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-19559
	 * @throws PJeBusinessException
	 */
	private void selecionaOrgaoJulgadorDivergente() throws PJeBusinessException {
		OrgaoJulgador ojAcompanhado = Authenticator.getOrgaoJulgadorAtual();
		for (Integer idOjAcompanhado : getListaTipoVotoDivergentes()) {
			ojAcompanhado = orgaoJulgadorManager.findById(idOjAcompanhado);
		}
		voto.setOjAcompanhado(ojAcompanhado);
	}

	private void gravarDocumento(ProcessoDocumento processoDocumento) {
		try {
			this.processoDocumento = documentoJudicialService.persist(processoDocumento, true);
			documentoJudicialService.flush();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Erro ao gravar o documento do voto: {0}", e.getMessage());
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
			facesMessages.add(Severity.INFO, "Erro ao salvar o Documento na Sessão.");
		}
	}

	private void gravarEmMultDocs(SessaoProcessoDocumentoVoto voto, ProcessoDocumento documento){
		SessaoProcessoMultDocsVoto sessaoProcessoDocumentoMultDocs = new SessaoProcessoMultDocsVoto();
		sessaoProcessoDocumentoMultDocs.setProcessoDocumento(documento);
		sessaoProcessoDocumentoMultDocs.setSessaoProcessoDocumentoVoto(voto);
		sessaoProcessoDocumentoMultDocs.setOrdemDocumento(getProximoNumeroOrdemDoc(voto));
		try {
			sessaoProcessoMultDocsVotoManager.persist(sessaoProcessoDocumentoMultDocs);
			sessaoProcessoMultDocsVotoManager.flush();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Houve um erro gravar o documento: {0}", e.getLocalizedMessage());
		}
	}

	private Integer getProximoNumeroOrdemDoc(SessaoProcessoDocumentoVoto voto) {
		return sessaoProcessoMultDocsVotoManager.recuperarProximoNumeroOrdemDoc(voto);
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
			documentoJudicialService.finalizaDocumento(getProcessoDocumento(), processoJudicial, taskInstance.getId(), false, true, false, Authenticator.getPessoaLogada(), false);
			documentoJudicialService.flush();
			resetarComponentes();
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao finalizar o documento: {0}", e.getLocalizedMessage());
		}
		facesMessages.add(Severity.INFO, "Assinatura bem sucedida.");
	}
	
	public String getDownloadLinks(){
		if(voto.getProcessoDocumento() != null){
			return documentoJudicialService.getDownloadLinks(Arrays.asList(voto.getProcessoDocumento()));
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
	
	public List<OrgaoJulgador> getPossiveisAcompanhados() {
		
		try {
			possiveisAcompanhados.clear();
			int i = 0;
			if (voto.getTipoVoto()!=null && "D".equals(voto.getTipoVoto().getContexto())){
				for (Integer idOjAcompanhado : getListaTipoVotoDivergentes()) {
					possiveisAcompanhados.add(i, orgaoJulgadorManager.findById(idOjAcompanhado));
					i++;
				}
			}else{
				possiveisAcompanhados.add(i, processoJudicial.getOrgaoJulgador());
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
					manipulaLiberacao_ = (Boolean) expressions.createValueExpression(manipulaLiberacao).getValue();
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
	
	public Boolean getPodeAssinar() {
		if (podeAssinar != null && podeAssinar){
 			podeAssinar = !tipoProcessoDocumentoPapelService.verificarExigibilidadeNaoAssina(
 					Authenticator.getPapelAtual(), voto.getProcessoDocumento().getTipoProcessoDocumento());
		}
		return podeAssinar;
	}
	
	public List<SessaoProcessoMultDocsVoto> getListSessaoProcDocVoto() {
		return listSessaoProcDocVoto;
	}

	/**
	 * Método que carrega os Documentos da Sessao do Voto
	 */
	private void carregarDocsVoto() throws PJeBusinessException {
		List<ProcessoDocumento> listDocs = new ArrayList<ProcessoDocumento>();
		List<SessaoProcessoMultDocsVoto> listMultDocsVoto = new ArrayList<SessaoProcessoMultDocsVoto>();
		if(voto.getIdSessaoProcessoDocumento() != 0){
			List<SessaoProcessoMultDocsVoto> multDocsVoto = sessaoProcessoMultDocsVotoManager.recuperarDocsVoto(voto);
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
			listDocsVoto = new ArrayList<ProcessoDocumento>();
			facesMessages.add(Severity.INFO, "Documento removido com sucesso.");
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Erro ao excluir voto.");
			e.printStackTrace();
		}
	}

	/**
	 * Caso tenha processoDocumento não é necessário excluir o documento pois,
	 * já sera apagado na ProcessoDocumentoLido
	 * @param procDoc ProcessoDocumento
	 */
	private void isProcessoNaoLido(ProcessoDocumento procDoc) throws PJeBusinessException {
		if(!processarDocumentoLido(procDoc)){
			processoDocumentoManager.inactivate(procDoc);
			processoDocumentoManager.flush();
			sessaoProcessoDocumentoManager.refresh(voto);
		}
	}

	/**
	 *Ao remover o documento da SessaoProcessoDocumentoVoto é necessário
	 *inserir o ultimo(de acordo com a data inclusão) doc da SessaoProcessoMultDocs
	 */
	private void verificarUltimoDoc() {
		ProcessoDocumento ultimoDoc = recuperarUltimoDoc(voto);
		if(ultimoDoc != null){
			atualizarDocSessaoVoto(voto, ultimoDoc);
		}
	}

	/**
	 * Verifica se o existe o registro na processoDocumento,
	 * O Fato ocorre quando o documento é assinado.
	 * @param procDoc
	 */
	private boolean processarDocumentoLido(ProcessoDocumento procDoc) {
		ProcessoDocumentoLido procDocLido;
		procDocLido = processoDocumentoLidoManager.recuperarDocumentoLido(procDoc);
		if(procDocLido!=null){
			removerProcessoDocumentoLido(procDocLido);
			return true;
		}
		return false;
	}

	private void removerProcessoDocumentoLido(ProcessoDocumentoLido procDocLido) {
		if(procDocLido!= null && procDocLido.getIdProcessoDocumentoLido() != 0){
			try {
				processoDocumentoLidoManager.remove(procDocLido);
				processoDocumentoLidoManager.flush();
			} catch (PJeBusinessException e) {
				e.printStackTrace();
				facesMessages.add(Severity.ERROR, "Erro ao tentar excluir processoDocumentoLido: {0}", e.getMessage());
			}
		}
	}

	/**
	 * Documento somente existente da sessaoProcessoMultDocsVoto
	 * @param procDoc ProcessoDocumento
	 */
	private void removerDocMultDocs(ProcessoDocumento procDoc) {
		SessaoProcessoMultDocsVoto doc = sessaoProcessoMultDocsVotoManager.recuperarSessaoProcessoDoc(procDoc);
		removerDocMult(doc);
			
	}
	
	private ProcessoDocumento recuperarUltimoDoc(SessaoProcessoDocumentoVoto voto) {
		SessaoProcessoMultDocsVoto sessaoProcessoMultDocsVoto = sessaoProcessoMultDocsVotoManager.recuperarUltimoDoc(voto);
		return sessaoProcessoMultDocsVoto != null ? sessaoProcessoMultDocsVoto.getProcessoDocumento() : null;
	}
	
	public boolean isManaged() {
		return this.processoDocumento != null && this.processoDocumento.getIdProcessoDocumento() != 0;
	}
	
	public boolean isExibeBotaoAssinatura(){
		if(processarParamExibeAssinador() && getProcessoDocumento().getIdProcessoDocumento() != 0 && !isAssinado(getProcessoDocumento())){
			return !tipoProcessoDocumentoPapelService.verificarExigibilidadeNaoAssina(
 					Authenticator.getPapelAtual(), 
					voto.getProcessoDocumento().getTipoProcessoDocumento());
		}
		return false;
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
		return assinaturaDocumentoService.isProcessoDocumentoAssinado(proc);
	}
	
	private boolean processarParamExibeAssinador() {
		Boolean booleanExibeAssinador =  (Boolean) tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.VARIAVEL_FLUXO_PERMITE_ASSINATURA_VOTO);
		if(booleanExibeAssinador != null && booleanExibeAssinador.equals(Boolean.FALSE)){
			return false;
		}
		return true;
	}
	
	public void assinarDocumento(){
		try {
			documentoJudicialService.finalizaDocumento(getProcessoDocumento(), processoJudicial, taskInstance.getId(), false, true, false, Authenticator.getPessoaLogada(), false);
			documentoJudicialService.flush();
			resetarComponentes();
		}catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao assinar o documento: {0}", e.getLocalizedMessage());
		}
	}
	
	public String formatarSelecionado(ProcessoDocumento doc){
		if(doc.getIdProcessoDocumento() == getProcessoDocumento().getIdProcessoDocumento()){
			return "background-color: #ff9797;";
		}
		return StringUtils.EMPTY;
	}
	
	private SessaoProcessoDocumentoVoto recuperarVoto(ProcessoDocumento procDoc) {
		return sessaoProcessoDocumentoVotoManager.recuperarVoto(procDoc);
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
		this.processoDocumento = processoDocumento;
	}

	public List<ProcessoDocumento> getListDocsVoto() {
		if(ProjetoUtil.isVazio(listDocsVoto)){
			try {
				carregarDocsVoto();
			} catch (PJeBusinessException e) {
				e.printStackTrace();
				facesMessages.add(Severity.ERROR, "Erro ao listar os documentos: {0}", e.getMessage());
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
	 * Método responsável por retornar um array de inteiros. Esse array será composto pelos identificadores 
	 * cadastrados no parâmetro de sistema "pje:flx:votacaoVogal:tiposVoto:ids". Caso haja identificadores cadastrados 
	 * na variável de tarefa "tiposDisponiveisIds", os identificadores retornados serão aqueles que estão cadastrados
	 * tanto no parâmetro de sistema quando na variável de tarefa.
	 * 
	 * @return Um array de inteiros.
	 */
	private Integer[] getIds(){
		Integer[] idsFromParametroSistema = ParametroUtil.instance().getIdsTipoDocumentoVoto();
		Integer[] idsFromVariavelTarefa = Util.converterStringIdsToIntegerArray(
				(String)tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.VARIAVEL_IDS_TIPOS_DOCUMENTOS_FLUXO));
		
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
					tipoProcessoDocumentoManager.findDisponiveis(Authenticator.getPapelAtual(), getIds());
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
	 * Retorna uma lista de modelos de documento que estão relacionados ao tipo de documento selecionado.
	 * 
	 * @return Uma lista de modelos de documento que estão relacionados ao tipo de documento selecionado.
	 */
	public List<ModeloDocumento> getModelosDocumento() {
		this.modelosDocumento.clear();
		
		if(this.processoDocumento.getTipoProcessoDocumento() != null){
			try {
				this.modelosDocumento.addAll(
						documentoJudicialService.getModelosLocais(getProcessoDocumento().getTipoProcessoDocumento()));
				
			} catch (PJeBusinessException ex) {
				facesMessages.add(Severity.ERROR, ex.getMessage());
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
			PlacarSessaoVO p = sessaoProcessoDocumentoVotoManager.getPlacarCondutores(sessao, getProcessoJudicial(), false);
			placar = p.getMapaPlacar();
			impedidos = sessaoProcessoDocumentoVotoManager.getImpedidos(sessao, getProcessoJudicial(), false);
			if (sessao!=null){
				omissos = sessaoProcessoDocumentoVotoManager.getOmissos(sessao, getProcessoJudicial(), false);
			}else{
				omissos = new HashSet<Integer>();
			}
			listaTipoVotoDivergentes = sessaoProcessoDocumentoVotoManager.getVotosPorTipoContextoDivergencia(sessao, getProcessoJudicial(), false);
			listaTipoVotoNaoConhece = sessaoProcessoDocumentoVotoManager.getVotosPorTipo(sessao, getProcessoJudicial(), tipoVotoManager.recuperaNaoConhece(), false);
			placar.put(-1, omissos);
			placar.put(-2, impedidos);
			placar.put(-3, listaTipoVotoDivergentes);
			placar.put(-4, listaTipoVotoNaoConhece);
			if (sessao==null){
				atualizaPlacarSemSessao();
			}
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Erro ao carregar placar: {0}", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Metodo responsavel por atualizar placar quando não houver sessão
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
	 * Metodo responsável por retorna uma lista apenas com os órgãos julgadores 
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
	 * Metodo responsável por retorna uma lista apenas com os órgãos julgadores 
	 * que votaram com o tipo de voto não conhece.
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
					nomeOrgao.put(idOrgaoJulgador, orgaoJulgadorManager.findById(idOrgaoJulgador).getOrgaoJulgador());
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

}
