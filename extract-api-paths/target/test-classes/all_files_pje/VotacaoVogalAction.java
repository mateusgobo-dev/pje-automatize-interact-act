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
package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.home.SessaoPautaProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.jt.pje.manager.DerrubadaVotoManager;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

/**
 * Componente de controle do frame de fluxo WEB-INF/xhtml/flx/votacaoVogal.xhtml
 * 
 * @author cristof
 * @since 1.6.0
 */
@Name(VotacaoVogalAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class VotacaoVogalAction extends TramitacaoFluxoAction implements Serializable, ArquivoAssinadoUploader{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6092748770177071655L;

	private static final Map<String, String> prms;
	
	public static final String NAME = "votacaoVogalAction";
	
	private static final String[] colors = {
			 "#1C75AA", "#AA0D12", "#501193", "#63B8FF", "#CD6090",
			 "#B03060", "#09AA0F", "#B22222", "#CD853F", "#BC8F8F",
			 "#CD5C5C", "#21C3C7", "#0DDA41", "#B3EE3A", "#C9C667",
			 "#FFA500", "#8B5A00", "#FF7256", "#8B3626", "#94200F",
			 "#EDC11C", "#AB82FF", "#715C0D", "#FF9066", "#15B79D",
			 "#90B715", "#B72573", "#C72785", "#E35B98", "#16C159",
			 "#5D750D", "#8B864E"};	
	
	static {
		prms = new HashMap<String, String>();
		prms.put("identificadoresModelos", "pje:flx:votacaoVogal:modelos:ids");
		prms.put("permitirVotarSemCondutor", "pje:flx:votacaoVogal:flg:permitirVotoSemCondutor");
		prms.put("manipulaLiberacao", "pje:flx:votacaoVogal:exp:manipulaLiberacao");
		prms.put("papeisTransicaoAutomatica", "pje:flx:votacaoVogal:transicaoAutomatica:papeis");
		prms.put("idSessaoPauta", "pje:flx:votacaoVogal:sessaoPauta:id");
	}
	
	private int ncolor = 0;
	
	private String identificadoresModelos;
	
	private String papeisTransicaoAutomatica;
	
	private List<ModeloDocumento> modelos;
	
	private Map<Integer, Map<Integer, String>> colorsMap;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In
	private ModeloDocumentoManager modeloDocumentoManager;
	
	@In
	private SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager;
	
	@In
	private Expressions expressions;
	
	@In
	private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;
	
	@In
	private AssinaturaDocumentoService assinaturaDocumentoService;
	
	@In
	private DerrubadaVotoManager derrubadaVotoManager;
	
	@In
    private TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService;
	
	@In
	private TipoVotoManager tipoVotoManager;
	
	@In
 	private SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;
 	
 	@In
	private OrgaoJulgadorManager orgaoJulgadorManager;
 	
	@In(create = true, required = true)
	private transient TaskInstanceUtil taskInstanceUtil;

	
	private Boolean transitarAutomaticamente;
	
	private Map<OrgaoJulgador, Integer> placar = new HashMap<OrgaoJulgador, Integer>();
	
	private List<SessaoProcessoDocumento> elementosJulgamento;
	
	private List<SessaoProcessoDocumentoVoto> demaisVotos;
	
	private List<OrgaoJulgador> possiveisAcompanhados = new ArrayList<OrgaoJulgador>();
	
	private Sessao sessao;
	
	private SessaoProcessoDocumentoVoto votoRelator;
	
	private SessaoProcessoDocumentoVoto voto;
	
	private ModeloDocumento modelo;
	
	private boolean redigir = false;

	private boolean votarSemDocumento = false;

	private boolean votoSalvo = false;
	
	private Boolean permitirVotarSemCondutor = false;
	
	private String manipulaLiberacao = null;
	
	private Boolean manipulaLiberacao_ = null;
	
	private Boolean podeAssinar;
	
	private Map<OrgaoJulgador, List<OrgaoJulgador>> detalhesPlacar = new HashMap<OrgaoJulgador, List<OrgaoJulgador>>();
	
	private Integer idSessaoPauta;
	
	private SessaoPautaProcessoTrf sessaoPautaProcessoTrf;
	
	private ArquivoAssinadoHash arquivoAssinado;
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.view.fluxo.TramitacaoFluxoAction#init()
	 */
	@Override
	public void init(){
		super.init();
		sessaoPautaProcessoTrf = carregarSessaoPautaProcessoTrf(processoJudicial);
		colorsMap = new HashMap<Integer, Map<Integer, String>>();
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.view.fluxo.TramitacaoFluxoAction#getParametrosConfiguracao()
	 */
	@Override
	protected Map<String, String> getParametrosConfiguracao() {
		return prms;
	}
	
	public List<ModeloDocumento> getModelos() {
		if(modelos == null){
			carregarModelos();
		}
		return modelos;
	}
	
	private void carregarModelos(){
		if(identificadoresModelos != null){
			List<Integer> ids = new ArrayList<Integer>();
			String[] strIds = identificadoresModelos.split(",");
			for(String s: strIds){
				ids.add(Integer.parseInt(s));
			}
			modelos = modeloDocumentoManager.findByIds(ids.toArray(new Integer[]{}));
		}
	}
	
	private void inicializarDocumentos(){
		sessao = null;
		if(idSessaoPauta != null){
			try {
				sessaoPautaProcessoTrf = sessaoPautaProcessoTrfManager.findById(idSessaoPauta);
				if(sessaoPautaProcessoTrf != null){
					sessao = sessaoPautaProcessoTrf.getSessao(); 
				}
			} catch (PJeBusinessException e) {
				sessao = null;
			}
		}
		List<SessaoProcessoDocumento> aux = sessaoProcessoDocumentoManager.recuperaElementosJulgamento(processoJudicial, sessao, Authenticator.getOrgaoJulgadorAtual());
		elementosJulgamento = new ArrayList<SessaoProcessoDocumento>(aux.size());
		demaisVotos = new ArrayList<SessaoProcessoDocumentoVoto>();
		for(SessaoProcessoDocumento spd: aux){
			if(SessaoProcessoDocumentoVoto.class.isAssignableFrom(spd.getClass())){
				SessaoProcessoDocumentoVoto v = (SessaoProcessoDocumentoVoto) spd;
				if(v.getOrgaoJulgador().equals(v.getProcessoTrf().getOrgaoJulgador())){
					votoRelator = v;
				}else if(v.getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual())){
					voto = v;
					votarSemDocumento = true;
					votoSalvo = true;
					if(voto.getProcessoDocumento() != null){
						redigir = true;
						votarSemDocumento = false;
					}
				}else{
					demaisVotos.add(v);
				}
			}else{
				if(sessaoPautaProcessoTrf != null && isEmentaPerdida(spd) ){
					continue;
				}
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
		if(Authenticator.getOrgaoJulgadorAtual() != null && !possiveisAcompanhados.contains(Authenticator.getOrgaoJulgadorAtual())){
			carregaListaPossiveisAcompanhados();
		}
		if(voto.getProcessoDocumento() != null){
			podeAssinar = documentoJudicialService.podeAssinar(voto.getProcessoDocumento().getTipoProcessoDocumento(), Authenticator.getPapelAtual());
		}
	}
	
	private SessaoPautaProcessoTrf carregarSessaoPautaProcessoTrf(ProcessoTrf processoTrf){
		return sessaoPautaProcessoTrfManager.recuperaUltimaPautaProcesso(processoTrf);
	}
	
	public Map<Integer, Set<Integer>> getPlacar(Integer idJulgamento){
		return SessaoPautaProcessoTrfHome.instance().getPlacar(idJulgamento);
	}
	
	private boolean isEmentaPerdida(SessaoProcessoDocumento spd) {
		if(spd == null ){
			return false;
		}
		
		if(sessaoPautaProcessoTrf.getOrgaoJulgadorRelator() == null || 
				sessaoPautaProcessoTrf.getOrgaoJulgadorRelator().equals(sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor())){
			return false;
		}
		
		ProcessoDocumento doc = spd.getProcessoDocumento();
		
		if(doc != null && doc.getTipoProcessoDocumento().equals(ParametroUtil.instance().getTipoProcessoDocumentoEmenta())){
			if(spd.getOrgaoJulgador().equals(sessaoPautaProcessoTrf.getOrgaoJulgadorRelator())){
				return true;
			}
		}
		return false;
	}

	
	/**
	 * Trata possível mensagem de voto desatualizado em relação ao voto do relator
	 * 
	 * @return possivel mensagem contendo informação de data caso o voto esteja
	 * desatualizado em relação ao voto do relator 
	 */
	public String verificaValidadeVotoRelator() {
		return derrubadaVotoManager.verificaValidadeVotoRelator(getVotoRelator(), getVoto());
	}
	
	public List<String> getVotantes(){
		List<String> ret = new ArrayList<String>();
		for(OrgaoJulgador oj: placar.keySet()){
			ret.add(oj.getSigla());
		}
		return ret;
	}
	
	public List<Integer> getNumeroVotos(){
		List<Integer> ret = new ArrayList<Integer>();
		for(OrgaoJulgador oj: placar.keySet()){
			ret.add(placar.get(oj));
		}
		return ret;
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
		if(voto.getProcessoDocumento() == null){
			ProcessoDocumento doc = documentoJudicialService.getDocumento();
			TipoProcessoDocumento tipo = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
			doc.setProcessoDocumento(tipo.getTipoProcessoDocumento());
			doc.setTipoProcessoDocumento(tipo);
			doc.setProcesso(processoJudicial.getProcesso());
			doc.setProcessoTrf(processoJudicial);
			doc.getProcessoDocumentoBin().setModeloDocumento(" ");
			voto.setProcessoDocumento(doc);
			
			try {
				documentoJudicialService.persist(doc, true);
				facesMessages.add(Severity.INFO, "Iniciada a edição de voto");
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Erro ao tentar inicializar documento de voto: {0}.", e.getLocalizedMessage());
			}
		}
	}
	
	public void removerVoto(){
		
		if (voto!= null){
			voto.setTipoVoto(null);
			if (redigir) {
				ProcessoDocumento doc = voto.getProcessoDocumento();
				ProcessoDocumentoBin docBin = doc.getProcessoDocumentoBin();
				if(doc.getDataJuntada() == null){
					try {
						voto.setProcessoDocumento(null);
						ProcessoDocumentoManager.instance().remove(doc);
						ProcessoDocumentoBinManager.instance().remove(docBin);
					} catch (PJeBusinessException e) {
						facesMessages.add(Severity.ERROR, "Erro ao tentar remover o voto: {0}.", e.getLocalizedMessage());
					}
				}
			}
			try {
				sessaoProcessoDocumentoVotoManager.remove(voto);
				sessaoProcessoDocumentoVotoManager.flush();
				facesMessages.add(Severity.INFO, "Voto removido com sucesso");
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Erro ao tentar remover o voto: {0}.", e.getLocalizedMessage());
			}
		}
		votarSemDocumento = false;
		redigir = false;
		voto = new SessaoProcessoDocumentoVoto();
		voto.setCheckAcompanhaRelator(false);
		voto.setDestaqueSessao(false);
		voto.setImpedimentoSuspeicao(false);
		voto.setLiberacao(false);
		voto.setProcessoTrf(getProcessoJudicial());
		voto.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		voto.setDtVoto(null);
		podeAssinar = false;
		
	}
	
	
	public void votarSemDocumento(){
		votarSemDocumento = true;
	}
	
	public boolean isVotarSemDocumento(){
		return votarSemDocumento;
	}
	
	public void registrarImpedimento(){
		voto.setImpedimentoSuspeicao(true);
		voto.setTipoVoto(tipoVotoManager.recuperaImpedido());
		
		if (voto.getOjAcompanhado() == null){
			voto.setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
		}
		ProcessoDocumento doc = voto.getProcessoDocumento();
		if(doc != null){
			if(doc.getProcessoDocumentoBin() != null 
				&& (doc.getProcessoDocumentoBin().getModeloDocumento() == null || doc.getProcessoDocumentoBin().getModeloDocumento().isEmpty())){
					voto.setProcessoDocumento(null);
			}
		}
		try {
			tratarAcompanhamento();
			sessaoProcessoDocumentoManager.persistAndFlush(voto);
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar gravar o voto: {0}.", e.getLocalizedMessage());
		}
	}
	
	public boolean isVotoSalvo(){
		return votoSalvo;
	}
	
	public void recarregarPlacar(){
		SessaoPautaProcessoTrfHome.instance().getPlacares().remove(sessaoPautaProcessoTrf.getIdSessaoPautaProcessoTrf());
	}
	
	public void removerImpedimento(){
		try {
			voto.setImpedimentoSuspeicao(false);
			voto.setTipoVoto(null);
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
			if(processoDocumentoBin != null && CollectionUtilsPje.isNotEmpty(processoDocumentoBin.getSignatarios())){
				retorno = false;
			}
		}
		return retorno;
	}
	
	public void removerAssinaturaVoto(){
		ProcessoDocumento processoDocumento = this.voto.getProcessoDocumento();
		if(processoDocumento != null){
			this.assinaturaDocumentoService.removeAssinatura(processoDocumento);
		}
	}
	
	/**
	 * Realiza a gravação do voto.
	 * 
	 * @return <code>true</code> caso a gravação tenha sido bem sucedida ou
	 *         <code>false</code> caso algum erro tenha ocorrido.
	 */
	public boolean gravarVoto(){
		try {
			
			if (!isOrgaoVotanteNaSessao()) {
				facesMessages.addFromResourceBundle(Severity.ERROR, "votacaoVogal.votoNaoRegistrado.orgaoJulgadorNaoVotante");
				return false;
			}
			
			if (voto.getTipoVoto()==null){
				facesMessages.add(Severity.ERROR, "Selecione o tipo de voto");
				return false;
			}
			
			ProcessoDocumento doc = voto.getProcessoDocumento();
			if(doc != null 
					&& doc.getProcessoDocumentoBin() != null 
					&& doc.getProcessoDocumentoBin().getModeloDocumento() != null 
					&& !doc.getProcessoDocumentoBin().getModeloDocumento().isEmpty()){
				doc.setExclusivoAtividadeEspecifica(Boolean.TRUE);
				documentoJudicialService.persist(doc, true);
			}
			tratarAcompanhamento();
			voto.setDtVoto(new Date());
			vinculaSessaoVoto(voto);
			
			sessaoProcessoDocumentoVotoManager.persist(voto);
			sessaoProcessoDocumentoVotoManager.flush();
			votoSalvo = true;
			ncolor = 0;
			if(doc != null){
				podeAssinar = documentoJudicialService.podeAssinar(voto.getProcessoDocumento().getTipoProcessoDocumento(), Authenticator.getPapelAtual());
			}
			taskInstance.getProcessInstance().getContextInstance().setVariable(Variaveis.VARIAVEL_ID_VOT_COLEGIADO, voto.getIdSessaoProcessoDocumento());
			
			derrubadaVotoManager.analisarTramitacaoFluxoVotoDerrubado(voto);			
			facesMessages.add(Severity.INFO, "Voto registrado com sucesso.");	
			return true;
			
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar gravar o voto: {0}.", e.getLocalizedMessage());
			facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
			return false;
		}
	}
	
	/**
	 * Método responsável por setar o id da sessão no voto proferido 
	 * quando processo já pautado em uma sessão de julgamento.
	 * 
	 * @param voto voto inserido apos processo estar pautado em uma sessão.
	 */
	private void vinculaSessaoVoto(SessaoProcessoDocumentoVoto voto){
		
		ProcessoTrf processoTrf = voto.getProcessoTrf();
		SessaoPautaProcessoTrf sessaoPautaProcessoPautado = 
				sessaoPautaProcessoTrfManager.getSessaoPautaProcessoPautado(processoTrf);
		if (sessaoPautaProcessoPautado != null) {
			voto.setSessao(sessaoPautaProcessoPautado.getSessao());
		}
	}
	
	private void tratarAcompanhamento(){
		if(voto.getTipoVoto() == null){
			voto.setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
		}else if(voto.getTipoVoto().getContexto().equals("C")){
			voto.setOjAcompanhado(processoJudicial.getOrgaoJulgador());
		}else if(voto.getTipoVoto().getContexto().equals("P")){
			voto.setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
		}else if(voto.getTipoVoto().getContexto().equals("D") && voto.getOjAcompanhado() == null){
			voto.setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
		}else if(voto.getTipoVoto().getContexto().equals("I")){
			voto.setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
		}
		voto.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		Sessao sessaoJudicial  = sessaoPautaProcessoTrf != null ? sessaoPautaProcessoTrf.getSessao() : sessao;
		if(sessaoJudicial !=  null && sessaoJudicial.getDataRealizacaoSessao() == null && sessaoJudicial.getDataRegistroEvento() == null){
			voto.setSessao(sessaoPautaProcessoTrf != null ? sessaoPautaProcessoTrf.getSessao() : sessao);
		}else{
			voto.setSessao(null);
		}
	}
	
	public void finalizarSemDocumento(){
		if (gravarVoto()) {
			if(transicaoPadrao != null && !transicaoPadrao.isEmpty()){
				TaskInstanceHome tih = TaskInstanceHome.instance();
				tih.end(transicaoPadrao);
			}			
		}		
	}
	
	public void finalizarAssinatura(){
		try {
			if (gravarVoto()) {
				// Utilizado com o PJeOffice
				if(arquivoAssinado != null && this.voto.getProcessoDocumento().getProcessoDocumentoBin() != null){
					this.voto.getProcessoDocumento().getProcessoDocumentoBin().setCertChain(arquivoAssinado.getCadeiaCertificado());
					this.voto.getProcessoDocumento().getProcessoDocumentoBin().setSignature(arquivoAssinado.getAssinatura());
				}
				documentoJudicialService.finalizaDocumento(voto.getProcessoDocumento(), processoJudicial, taskInstance.getId(), false, true, false, Authenticator.getPessoaLogada(), false);
				documentoJudicialService.flush();
				if(transicaoPadrao != null && !transicaoPadrao.isEmpty()){
					ProcessoHome.instance().setIdProcessoDocumento(voto.getProcessoDocumento().getIdProcessoDocumento());
					TaskInstanceHome tih = TaskInstanceHome.instance();
					tih.end(transicaoPadrao);
				}
				facesMessages.add(Severity.INFO, "Assinatura bem sucedida.");
			}			
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao finalizar o documento: {0}", e.getLocalizedMessage());
		}		
	}
	
	public String getDownloadLinks(){
		if(voto.getProcessoDocumento() != null){
			return documentoJudicialService.getDownloadLinks(Arrays.asList(voto.getProcessoDocumento()));
		}else{
			return "";
		}
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
	
	public ModeloDocumento getModelo() {
		return modelo;
	}
	
	public void setModelo(ModeloDocumento modelo) {
		this.modelo = modelo;
	}
	
	public Boolean getPermitirVotarSemCondutor() {
		return permitirVotarSemCondutor;
	}

	public Map<OrgaoJulgador, Integer> getPlacar() {
		return placar;
	}
	
	public List<OrgaoJulgador> getPossiveisAcompanhados() {
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
		if( voto.getProcessoDocumento() != null && voto.getProcessoDocumento().getProcessoDocumentoBin() != null ){
			if (podeAssinar != null && podeAssinar && podeAlterarVoto()){
	 			podeAssinar = !tipoProcessoDocumentoPapelService.verificarExigibilidadeNaoAssina(
	 					Authenticator.getPapelAtual(), 
	 					voto.getProcessoDocumento().getTipoProcessoDocumento());
			}
		}
		return podeAssinar;
	}
	
	public Map<OrgaoJulgador, List<OrgaoJulgador>> getDetalhesPlacar() {
		return detalhesPlacar;
	}

	public SessaoPautaProcessoTrf getSessaoPautaProcessoTrf() {
		return sessaoPautaProcessoTrf;
	}

	public void setSessaoPautaProcessoTrf(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		this.sessaoPautaProcessoTrf = sessaoPautaProcessoTrf;
	}
	
	/**
	 * Faz a validação se o Órgão Julgador que está realizando o voto está
	 * marcado como votante na composição da sessão de julgamento.
	 * 
	 * Caso ainda não exista sessão definida então deixa o usuário registrar o
	 * voto.
	 * 
	 * @see VotacaoVogalAction#recuperarProcessoPautadoSessao(ProcessoTrf, Sessao)
	 * @see SessaoPautaProcessoTrf#getParticipaVotacao(OrgaoJulgador)
	 * 
	 * @param voto {@link SessaoProcessoDocumentoVoto} voto que está sendo realizado
	 * 
	 * @return <code>true</code> nos casos em que o processo não está pautado em
	 *         uma sessão de julgamento ou quando está pautado em uma sessão é o
	 *         Órgão Julgador está marcado como votante na sessão.
	 * 
	 *         <code>false</code> quando o processo está pautado em uma sessão
	 *         de julgamento e o órgão julgador não faz parte da composição ou o
	 *         órgão faz parte da sessão e o mesmo não está marcado como votante
	 *         na sessão
	 * 
	 */
	public boolean isOrgaoVotanteNaSessao() {
		
		boolean retorno = true;
		
		if (voto.getOrgaoJulgador() == null) {
			retorno = false;
			
		} else if (sessao != null) {
			SessaoPautaProcessoTrf processoPautado = recuperarProcessoPautadoSessao(
					voto.getProcessoTrf(), sessao);
						
			if (processoPautado != null) {
				retorno = processoPautado.getParticipaVotacao(voto.getOrgaoJulgador());	
			}
			
		}
		
		return retorno;
	}
	
	/**
	 * Recupera da sessão de julgamento a pauta que contém o processo.
	 * 
	 * @param processo
	 *            {@link ProcessoTrf} a ser pesquisado na sessão de julgamento
	 * 
	 * @param sessaoJulgamento
	 *            {@link Sessao} sessão de julgamento que contém a lista de de
	 *            processos pautados
	 * 
	 * @return a composição de pauta processo {@link SessaoPautaProcessoTrf} que
	 *         representa o processo pautado na sessão de julgamento
	 */
	private SessaoPautaProcessoTrf recuperarProcessoPautadoSessao(
			ProcessoTrf processo, Sessao sessaoJulgamento) {
		
		SessaoPautaProcessoTrf processoPautadoRetorno = null;
		
		if (sessaoJulgamento != null && processo != null) {
			List<SessaoPautaProcessoTrf> processosPautados = sessaoJulgamento
					.getSessaoPautaProcessoTrfList();
			
			if (processosPautados != null) {
				for (SessaoPautaProcessoTrf processoPautado : processosPautados) {
					if (processoPautado.getDataExclusaoProcessoTrf() == null && processoPautado.getProcessoTrf().equals(processo)) {
						processoPautadoRetorno = processoPautado;
					}
				}			
			}
		}
		
		return processoPautadoRetorno;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		this.arquivoAssinado = arquivoAssinadoHash;
	}

	@Override
	public String getActionName() {
		return NAME;
	}
	
	public ArquivoAssinadoHash getArquivoAssinado() {
		return arquivoAssinado;
	}
	
	public void setArquivoAssinado(ArquivoAssinadoHash arquivoAssinado) {
		this.arquivoAssinado = arquivoAssinado;
	}
	
	public String getColor(Integer oj, Integer idJulgamento){
		if(colorsMap.get(idJulgamento) == null){
			Map<Integer, String> map = new HashMap<Integer, String>();
			map.put(oj, colors[0]);
			colorsMap.put(idJulgamento, map);
		}else if(colorsMap.get(idJulgamento).get(oj) == null){
			colorsMap.get(idJulgamento).put(oj, colors[ncolor % colors.length]);
		}
		ncolor++;
		return colorsMap.get(idJulgamento).get(oj);
	}
	
	public String recuperarLabelVotoRelator(){
		String retorno = "Voto do relator: ";
		
		if(sessaoPautaProcessoTrf != null && sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor() != null){
			retorno = "Voto vencedor: ";
		}
		
		return retorno;
	}
	
	private void carregaListaPossiveisAcompanhados() {
 		try {
 			possiveisAcompanhados.clear();
 			Set<Integer> listaVotosDivergentes = sessaoProcessoDocumentoVotoManager.getVotosPorTipoContextoDivergencia(sessao, getProcessoJudicial(), true);
 			int i = 0;
			for (Integer idOjAcompanhado : listaVotosDivergentes) {
				possiveisAcompanhados.add(i, orgaoJulgadorManager.findById(idOjAcompanhado));
				i++;
			}
			possiveisAcompanhados.add(i++, Authenticator.getOrgaoJulgadorAtual());
 		} catch (PJeBusinessException e) {
 			logger.error("Houve um erro ao tentar recuperar órgão julgador acompanhado: {0}.", e.getLocalizedMessage());
 		}
	}

	public void votarComRelatorLiberarDemaisJulgadores() {
		votarSemDocumento = true;
		voto.setLiberacao(true);
		voto.setTipoVoto(tipoVotoManager.recuperaAcompanhaRelator());
		gravarVoto();
	}
	public boolean isOcultarBotaoAssinar(){
 		boolean retorno = false;
 		if (this.voto.getProcessoDocumento() != null){
 			retorno = tipoProcessoDocumentoPapelService.verificarExigibilidadeNaoAssina(
 					Authenticator.getPapelAtual(), 
 					this.voto.getProcessoDocumento().getTipoProcessoDocumento());
 		}
 		return retorno;
 	}
	
	public Boolean isOcultaBotaoAssinarVoto(){
 		Boolean retorno = (Boolean) taskInstanceUtil.getVariable(Parametros.PJE_FLUXO_OCULTAR_BOTAO_ASSINAR_VOTO);
 		if (retorno != null && retorno){
 			retorno = !isOcultarBotaoAssinar();
 		} else {
 			retorno = Boolean.FALSE;
 		}
 		return retorno;
	}

	
}
