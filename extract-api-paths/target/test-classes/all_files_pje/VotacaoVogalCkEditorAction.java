/**
 * pje-web
 * Copyright (C) 2009-2013 Conselho Nacional de Justica
 *
 * A propriedade intelectual deste programa, como codigo-fonte
 * e como sua derivacao compilada, pertence  Uniao Federal,
 * dependendo o uso parcial ou total de autorizacao expressa do
 * Conselho Nacional de Justica.
 *
 **/
package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

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

import br.com.infox.cliente.home.SessaoPautaProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.view.CkEditorGeraDocumentoAbstractAction;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.je.pje.business.dto.RespostaDTO;
import br.jus.je.pje.business.dto.RespostaTiposVotoDTO;
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
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Componente de controle do frame de fluxo
 * WEB-INF/xhtml/flx/votacaoVogalCkEditor.xhtml
 * 
 * @author cristof
 * @since 1.6.0
 */
@Name(VotacaoVogalCkEditorAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class VotacaoVogalCkEditorAction extends CkEditorGeraDocumentoAbstractAction implements Serializable, ArquivoAssinadoUploader {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6092748770177071655L;

	public static final String NAME = "votacaoVogalCkEditorAction";

	private static final String[] colors = { "#1C75AA", "#AA0D12", "#501193", "#63B8FF", "#CD6090", "#B03060",
			"#09AA0F", "#B22222", "#CD853F", "#BC8F8F", "#CD5C5C", "#21C3C7", "#0DDA41", "#B3EE3A", "#C9C667",
			"#FFA500", "#8B5A00", "#FF7256", "#8B3626", "#94200F", "#EDC11C", "#AB82FF", "#715C0D", "#FF9066",
			"#15B79D", "#90B715", "#B72573", "#C72785", "#E35B98", "#16C159", "#5D750D", "#8B864E" };

	private int ncolor = 0;

	private String identificadoresModelos;

	private String papeisTransicaoAutomatica;

	private List<ModeloDocumento> modelos;

	private Map<Integer, Map<Integer, String>> colorsMap;

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

	private Boolean permitirVotarSemCondutor = false;

	private String manipulaLiberacao = null;

	private Boolean manipulaLiberacao_ = null;

	private Boolean podeAssinar;

	private Map<OrgaoJulgador, List<OrgaoJulgador>> detalhesPlacar = new HashMap<OrgaoJulgador, List<OrgaoJulgador>>();

	private Integer idSessaoPauta;

	private SessaoPautaProcessoTrf sessaoPautaProcessoTrf;

	private ArquivoAssinadoHash arquivoAssinado;

	@Logger
	private Log logger;

	private String transicaoPadrao;

	private ProcessoTrf processoJudicial;

	private boolean uploadArquivoAssinadoRealizado;
	
	@In(required=false)
	protected TaskInstance taskInstance;

	@Create
	public void init() {
		setProtocolarDocumentoBean(new ProtocolarDocumentoBean(ComponentUtil.getTaskInstanceUtil().getProcesso(ComponentUtil.getTaskInstanceUtil().getProcessInstance().getId()).getIdProcessoTrf(), ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.RECUPERA_DOCUMENTO_FLUXO, NAME));
		processoJudicial = getProcessoJudicial();
		carregarTransicaoPadrao();
		carregarVariaveisParametrizadas();
		inicializarDocumentos();
	}

	public void load() {
		inicializarDocumentos();
	}

	public void carregarTransicaoPadrao() {
		if (transicaoPadrao == null) {
			transicaoPadrao = (String) ComponentUtil.getTaskInstanceUtil().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		}
	}

	private void carregarVariaveisParametrizadas() {
		identificadoresModelos = (String) recuperarVariavel("pje:flx:votacaoVogal:modelos:ids");
		permitirVotarSemCondutor = (Boolean) recuperarVariavel("pje:flx:votacaoVogal:flg:permitirVotoSemCondutor");
		manipulaLiberacao = (String) recuperarVariavel("pje:flx:votacaoVogal:exp:manipulaLiberacao");
		papeisTransicaoAutomatica = (String) recuperarVariavel("pje:flx:votacaoVogal:transicaoAutomatica:papeis");
		idSessaoPauta = (Integer) recuperarVariavel("pje:flx:votacaoVogal:sessaoPauta:id");
	}

	private Object recuperarVariavel(String variavelFluxo) {
		Object variavelTarefa = ComponentUtil.getTramitacaoProcessualService().recuperaVariavelTarefa(variavelFluxo);

		if (variavelTarefa == null) {
			variavelTarefa = ComponentUtil.getTramitacaoProcessualService().recuperaVariavel(variavelFluxo);
		}

		return variavelTarefa;
	}

	public List<ModeloDocumento> getModelos() {
		if (modelos == null) {
			carregarModelos();
		}
		return modelos;
	}

	private void carregarModelos() {
		if (identificadoresModelos != null) {
			List<Integer> ids = new ArrayList<Integer>();
			String[] strIds = identificadoresModelos.split(",");
			for (String s : strIds) {
				ids.add(Integer.parseInt(s));
			}
			modelos = ComponentUtil.getModeloDocumentoManager().findByIds(ids.toArray(new Integer[] {}));
		}
	}

	private void inicializarDocumentos() {
		colorsMap = new HashMap<Integer, Map<Integer, String>>();

		sessao = null;
		if (idSessaoPauta != null) {
			try {
				sessaoPautaProcessoTrf = ComponentUtil.getSessaoPautaProcessoTrfManager().findById(idSessaoPauta);
				if (sessaoPautaProcessoTrf != null) {
					sessao = sessaoPautaProcessoTrf.getSessao();
				}
			} catch (PJeBusinessException e) {
				sessao = null;
			}
		}
		List<SessaoProcessoDocumento> aux = ComponentUtil.getSessaoProcessoDocumentoManager()
				.recuperaElementosJulgamento(getProcessoJudicial(), sessao, Authenticator.getOrgaoJulgadorAtual());
		elementosJulgamento = new ArrayList<SessaoProcessoDocumento>(aux.size());
		demaisVotos = new ArrayList<SessaoProcessoDocumentoVoto>();
		for (SessaoProcessoDocumento spd : aux) {
			if (SessaoProcessoDocumentoVoto.class.isAssignableFrom(spd.getClass())) {
				SessaoProcessoDocumentoVoto v = (SessaoProcessoDocumentoVoto) spd;
				if (v.getOrgaoJulgador().equals(v.getProcessoTrf().getOrgaoJulgador())) {
					votoRelator = v;
				} else if (v.getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual())) {
					voto = v;
					if (voto.getProcessoDocumento() != null) {
						redigir = true;
						getProtocolarDocumentoBean().setDocumentoPrincipal(voto.getProcessoDocumento());
					}
				} else {
					demaisVotos.add(v);
				}
			} else {
				if (sessaoPautaProcessoTrf != null && isEmentaPerdida(spd)) {
					continue;
				}
				elementosJulgamento.add(spd);
			}
			if (sessao == null && spd.getSessao() != null) {
				sessao = spd.getSessao();
			}
		}
		if (voto == null) {
			voto = new SessaoProcessoDocumentoVoto();
			voto.setCheckAcompanhaRelator(false);
			voto.setDestaqueSessao(false);
			voto.setImpedimentoSuspeicao(false);
			voto.setLiberacao(false);
			voto.setProcessoTrf(getProcessoJudicial());
			voto.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		}

		carregaListaPossiveisAcompanhados();

		if (voto.getProcessoDocumento() != null) {
			podeAssinar = ComponentUtil.getDocumentoJudicialService().podeAssinar(voto.getProcessoDocumento().getTipoProcessoDocumento(), Authenticator.getPapelAtual());
		}

		if (getProtocolarDocumentoBean() == null) {
			setProtocolarDocumentoBean(new ProtocolarDocumentoBean(voto.getProcessoTrf().getIdProcessoTrf(), ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.RECUPERA_DOCUMENTO_FLUXO, NAME));
		}
	}

	public Map<Integer, Set<Integer>> getPlacar(Integer idJulgamento) {
		return SessaoPautaProcessoTrfHome.instance().getPlacar(idJulgamento);
	}

	private boolean isEmentaPerdida(SessaoProcessoDocumento spd) {
		if (spd == null) {
			return false;
		}

		if (sessaoPautaProcessoTrf.getOrgaoJulgadorRelator() == null || sessaoPautaProcessoTrf.getOrgaoJulgadorRelator().equals(sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor())) {
			return false;
		}

		ProcessoDocumento doc = spd.getProcessoDocumento();

		if (doc != null && doc.getTipoProcessoDocumento().equals(ParametroUtil.instance().getTipoProcessoDocumentoEmenta())) {
			if (spd.getOrgaoJulgador().equals(sessaoPautaProcessoTrf.getOrgaoJulgadorRelator())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Trata possivel mensagem de voto desatualizado em relacao ao voto do
	 * relator
	 * 
	 * @return possivel mensagem contendo informacao de data caso o voto esteja
	 *         desatualizado em relacao ao voto do relator
	 */
	public String verificaValidadeVotoRelator() {
		return ComponentUtil.getDerrubadaVotoManager().verificaValidadeVotoRelator(getVotoRelator(), getVoto());
	}

	public List<String> getVotantes() {
		List<String> ret = new ArrayList<String>();
		for (OrgaoJulgador oj : placar.keySet()) {
			ret.add(oj.getSigla());
		}
		return ret;
	}

	public List<Integer> getNumeroVotos() {
		List<Integer> ret = new ArrayList<Integer>();
		for (OrgaoJulgador oj : placar.keySet()) {
			ret.add(placar.get(oj));
		}
		return ret;
	}

	public void redigirVoto() {
		redigir = true;
		if (voto.getProcessoDocumento() == null) {
			ProcessoDocumento doc = ComponentUtil.getDocumentoJudicialService().getDocumento();
			TipoProcessoDocumento tipo = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
			doc.setProcessoDocumento(tipo.getTipoProcessoDocumento());
			doc.setTipoProcessoDocumento(tipo);
			doc.setProcesso(getProcessoJudicial().getProcesso());
			doc.setProcessoTrf(getProcessoJudicial());
			doc.getProcessoDocumentoBin().setModeloDocumento(" ");
			voto.setProcessoDocumento(doc);

			try {
				ComponentUtil.getDocumentoJudicialService().persist(doc, true);
				ComponentUtil.getDocumentoJudicialService().flush();
				ComponentUtil.getFacesMessages().add(Severity.INFO, "Iniciada a edição de voto");
			} catch (PJeBusinessException e) {
				ComponentUtil.getFacesMessages().add(Severity.ERROR,
						"Erro ao tentar inicializar documento de voto: {0}.", e.getLocalizedMessage());
			}
		}
	}

	public void removerVotoEscrito() {
		redigir = false;
		ProcessoDocumento doc = voto.getProcessoDocumento();
		if (doc != null && doc.getDataJuntada() == null) {
			TipoVoto tipoVoto = voto.getTipoVoto();
			removerVoto();
			voto.setTipoVoto(tipoVoto);
		}
	}

	public void registrarImpedimento() {
		removerVotoEscrito();
        voto.setImpedimentoSuspeicao(true);
        voto.setTipoVoto(ComponentUtil.getTipoVotoManager().recuperaImpedido());
        redigir = false;
        if (voto.getOjAcompanhado() == null){
            voto.setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
        }
        ProcessoDocumento doc = voto.getProcessoDocumento();
		if (doc != null) {
			if (doc.getProcessoDocumentoBin() != null && (doc.getProcessoDocumentoBin().getModeloDocumento() == null || doc.getProcessoDocumentoBin().getModeloDocumento().isEmpty())) {
				voto.setProcessoDocumento(null);
			}
		}
		try {
			tratarAcompanhamento();
			ComponentUtil.getSessaoProcessoDocumentoManager().persistAndFlush(voto);
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar gravar o voto: {0}.", e.getLocalizedMessage());
		}
	}

	public void recarregarPlacar() {
		SessaoPautaProcessoTrfHome.instance().getPlacares()
				.remove(sessaoPautaProcessoTrf.getIdSessaoPautaProcessoTrf());
	}

	public void removerImpedimento() {
		removerVoto();
	}

	private void removerVoto() {
		try {
			if(voto.getProcessoDocumento() != null) {
				ProcessoDocumento pd = voto.getProcessoDocumento();
				ProcessoDocumentoBin pdb = pd.getProcessoDocumentoBin();
				voto.setProcessoDocumento(null);
				ComponentUtil.getControleVersaoDocumentoManager().deletarTodasVersoesIdDocumento(pdb.getIdProcessoDocumentoBin());
				ProcessoDocumentoManager.instance().remove(pd);
				ProcessoDocumentoBinManager.instance().remove(pdb);
			}
			SessaoProcessoDocumentoVotoManager.instance().remove(voto);
			SessaoProcessoDocumentoVotoManager.instance().flush();
            voto = null;
            inicializarDocumentos();
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar gravar o voto: {0}.", e.getLocalizedMessage());
		}
	}

	public boolean podeAlterarVoto() {
		ProcessoDocumento processoDocumento = this.voto.getProcessoDocumento();
		Boolean retorno = true;
		if (processoDocumento != null) {
			ProcessoDocumentoBin processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
			if (processoDocumentoBin != null && !StringUtils.isEmpty(processoDocumentoBin.getSignature())) {
				retorno = false;
			}
		}
		return retorno;
	}

	public void removerAssinaturaVoto() {
		ProcessoDocumento processoDocumento = this.voto.getProcessoDocumento();
		if (processoDocumento != null) {
			ComponentUtil.getAssinaturaDocumentoService().removeAssinatura(processoDocumento);
		}
	}

	/**
	 * Realiza a gravacao do voto.
	 * 
	 * @return <code>true</code> caso a gravacao tenha sido bem sucedida ou
	 *         <code>false</code> caso algum erro tenha ocorrido.
	 */
	public boolean gravarVoto() {
		try {
			if (!isOrgaoVotanteNaSessao(voto.getOrgaoJulgador(), sessao)) {
				ComponentUtil.getFacesMessages().addFromResourceBundle(Severity.ERROR, "votacaoVogalCkEditor.votoNaoRegistrado.orgaoJulgadorNaoVotante");
				return false;
			}

			if (voto.getTipoVoto() == null) {
				ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Selecione o tipo de voto");
				return false;
			}

			ProcessoDocumento doc = voto.getProcessoDocumento();
			if (doc != null && doc.getProcessoDocumentoBin() != null && doc.getProcessoDocumentoBin().getModeloDocumento() != null && !doc.getProcessoDocumentoBin().getModeloDocumento().isEmpty()) {
				doc.setExclusivoAtividadeEspecifica(Boolean.TRUE);
				getProtocolarDocumentoBean().setDocumentoPrincipal(doc);
				ComponentUtil.getDocumentoJudicialService().persist(doc, true);
				ComponentUtil.getControleVersaoDocumentoManager().salvarVersaoDocumento(doc);
			}

			tratarAcompanhamento();
			voto.setDtVoto(new Date());

			if (voto.getSessao() == null) {
				vinculaSessaoVoto(voto);
			}
			
			ComponentUtil.getSessaoProcessoDocumentoManager().persist(voto);
			ComponentUtil.getSessaoProcessoDocumentoManager().flush();
			ncolor = 0;
			if (doc != null) {
				podeAssinar = ComponentUtil.getDocumentoJudicialService().podeAssinar(voto.getProcessoDocumento().getTipoProcessoDocumento(), Authenticator.getPapelAtual());
			}
			ComponentUtil.getTaskInstanceUtil().getProcessInstance().getContextInstance().setVariable(Variaveis.VARIAVEL_ID_VOT_COLEGIADO, voto.getIdSessaoProcessoDocumento());

			ComponentUtil.getDerrubadaVotoManager().analisarTramitacaoFluxoVotoDerrubado(voto);
			ComponentUtil.getFacesMessages().add(Severity.INFO, "Voto registrado com sucesso.");

			return true;

		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar gravar o voto: {0}.", e.getLocalizedMessage());
			ComponentUtil.getFacesMessages().add(Severity.ERROR, e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * Mtodo responsvel por setar o id da sesso no voto proferido quando
	 * processo j pautado em uma sesso de julgamento.
	 * 
	 * @param voto
	 *            voto inserido apos processo estar pautado em uma sesso.
	 */
	private void vinculaSessaoVoto(SessaoProcessoDocumentoVoto voto) {
		ProcessoTrf processoTrf = voto.getProcessoTrf();
		SessaoPautaProcessoTrf sessaoPautaProcessoPautado = ComponentUtil.getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoPautado(processoTrf);
		if (sessaoPautaProcessoPautado != null) {
			voto.setSessao(sessaoPautaProcessoPautado.getSessao());
		}
	}

	private void tratarAcompanhamento() {
		voto.setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
		
		if (voto.getTipoVoto() != null && voto.getTipoVoto().getContexto().equals("C")) {
			voto.setOjAcompanhado(getProcessoJudicial().getOrgaoJulgador());
		}
		
		Sessao sessaoJudicial = sessaoPautaProcessoTrf != null ? sessaoPautaProcessoTrf.getSessao() : sessao;
		if (sessaoJudicial != null && sessaoJudicial.getDataRealizacaoSessao() == null && sessaoJudicial.getDataRegistroEvento() == null) {
			voto.setSessao(sessaoPautaProcessoTrf != null ? sessaoPautaProcessoTrf.getSessao() : sessao);
		} else {
			voto.setSessao(null);
		}
	}

	public void finalizarSemDocumento() {
		if (gravarVoto()) {
			if (transicaoPadrao != null && !transicaoPadrao.isEmpty()) {
				TaskInstanceHome tih = TaskInstanceHome.instance();
				tih.end(transicaoPadrao);
			}
		}
	}

	public void assinarDocumento() {
		try {
			ProcessoDocumento processoDocumento = this.voto.getProcessoDocumento();
			if(ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(processoDocumento)) {
				ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Documento está assinado");
			} else { 
				// Utilizado com o PJeOffice
				if (arquivoAssinado != null && this.voto.getProcessoDocumento().getProcessoDocumentoBin() != null) {
					this.voto.getProcessoDocumento().getProcessoDocumentoBin().setCertChain(arquivoAssinado.getCadeiaCertificado());
					this.voto.getProcessoDocumento().getProcessoDocumentoBin().setSignature(arquivoAssinado.getAssinatura());
				}
				ComponentUtil.getDocumentoJudicialService().finalizaDocumento(voto.getProcessoDocumento(), getProcessoJudicial(), taskInstance.getId(), false, true, false, Authenticator.getPessoaLogada(), false);
				ComponentUtil.getDocumentoJudicialService().flush();
				if (transicaoPadrao != null && !transicaoPadrao.isEmpty()) {
					ProcessoHome.instance().setIdProcessoDocumento(voto.getProcessoDocumento().getIdProcessoDocumento());
					TaskInstanceHome tih = TaskInstanceHome.instance();
					tih.end(transicaoPadrao);
				}
				ComponentUtil.getFacesMessages().add(Severity.INFO, "Assinatura bem sucedida.");
			}
		} catch (PJeBusinessException e) {
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Houve um erro ao finalizar o documento: {0}", e.getLocalizedMessage());
		}
	}

	public String getDownloadLinks() {
		if (voto.getProcessoDocumento() != null) {
			return ComponentUtil.getDocumentoJudicialService().getDownloadLinks(Arrays.asList(voto.getProcessoDocumento()));
		} else {
			return "";
		}
	}

	public List<SessaoProcessoDocumento> getElementosJulgamento() {
		if (elementosJulgamento == null) {
			inicializarDocumentos();
		}
		return elementosJulgamento;
	}

	public List<SessaoProcessoDocumentoVoto> getDemaisVotos() {
		if (demaisVotos == null) {
			inicializarDocumentos();
		}
		return demaisVotos;
	}

	public SessaoProcessoDocumentoVoto getVoto() {
		if (voto == null) {
			inicializarDocumentos();
		}
		return voto;
	}
	
	public String getLabelVotoRelatorSessao(){
		return (getVotoRelator().getSessao().getContinua())? getLabelVotoRelatorSessaoContinua() : getLabelVotoRelatorSessaoNaoContinua();
	}
	
	private String getLabelVotoRelatorSessaoNaoContinua(){
		DateFormat dateFormat = new SimpleDateFormat("EEEE dd/MM/yyyy");
		DateFormat dateFormatHoras = new SimpleDateFormat("HH:mm");
		
		String dataCompleta = (getVotoRelator().getSessao().getDataSessao() != null)? dateFormat.format(getVotoRelator().getSessao().getDataSessao()) : "";
		String horas = (getVotoRelator().getSessao().getMomentoInicio() != null)? dateFormatHoras.format(getVotoRelator().getSessao().getMomentoInicio()) : "";
		
		return String.format("%s - %s %s", getApelidoOuTipoVotoRelatorSessao(), dataCompleta, horas);
	}
	
	private String getLabelVotoRelatorSessaoContinua(){
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		
		String dataInicio = (getVotoRelator().getSessao().getDataSessao() != null)? dateFormat.format(getVotoRelator().getSessao().getDataSessao()) : "";
		String dataFim = (getVotoRelator().getSessao().getDataFimSessao() != null)? dateFormat.format(getVotoRelator().getSessao().getDataFimSessao()) : "";
		
		return String.format("%s - De %s a %s", getApelidoOuTipoVotoRelatorSessao() , dataInicio, dataFim);
	}
	
	private String getApelidoOuTipoVotoRelatorSessao(){
		return (!StringUtil.isEmpty(getVotoRelator().getSessao().getApelido()))? getVotoRelator().getSessao().getApelido() : "Sessão de julgamento " + getVotoRelator().getSessao().getTipoSessao().getTipoSessao();
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
		if (manipulaLiberacao_ == null) {
			if (manipulaLiberacao != null && !manipulaLiberacao.isEmpty()) {
				try {
					if (!manipulaLiberacao.startsWith("#{")) {
						manipulaLiberacao = "#{" + manipulaLiberacao + "}";
					}
					manipulaLiberacao_ = (Boolean) ComponentUtil.getExpressions().createValueExpression(manipulaLiberacao).getValue();
				} catch (Throwable t) {
					manipulaLiberacao_ = true;
				}
			} else {
				manipulaLiberacao_ = true;
			}
		}
		return manipulaLiberacao_;
	}

	public Boolean getTransitarAutomaticamente() {
		if (transitarAutomaticamente == null) {
			if (papeisTransicaoAutomatica == null || papeisTransicaoAutomatica.isEmpty()) {
				transitarAutomaticamente = false;
			} else {
				String[] papeis = papeisTransicaoAutomatica.split(",");
				Identity identity = Identity.instance();
				for (String p : papeis) {
					if (identity.hasRole(p)) {
						transitarAutomaticamente = true;
						break;
					}
				}
			}

		}
		return transitarAutomaticamente;
	}

	@Override
	public boolean podeAssinar() {
		if (podeAssinar != null && podeAssinar) {
			podeAssinar = !ComponentUtil.getTipoProcessoDocumentoPapelService().verificarExigibilidadeNaoAssina(Authenticator.getPapelAtual(), voto.getProcessoDocumento().getTipoProcessoDocumento());
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
	 * Faz a validacao se o orgao Julgador que esta realizando o voto esta
	 * marcado como votante na composicao da sessao de julgamento.
	 * 
	 * Caso ainda no exista sessao definida entao deixa o usuario registrar o
	 * voto.
	 * 
	 * @see VotacaoVogalAction#recuperarProcessoPautadoSessao(ProcessoTrf,
	 *      Sessao)
	 * @see SessaoPautaProcessoTrf#getParticipaVotacao(OrgaoJulgador)
	 * 
	 * @param voto
	 *            {@link SessaoProcessoDocumentoVoto} voto que est sendo
	 *            realizado
	 * 
	 * @return <code>true</code> nos casos em que o processo no esta pautado em
	 *         uma sessao de julgamento ou quando esta pautado em uma sessao o
	 *         orgao Julgador esta marcado como votante na sessao.
	 * 
	 *         <code>false</code> quando o processo esta pautado em uma sessao
	 *         de julgamento e o orgao julgador nao faz parte da composio ou o
	 *         orgao faz parte da sessao e o mesmo nao esta marcado como votante
	 *         na sessao
	 * 
	 */
	private boolean isOrgaoVotanteNaSessao(OrgaoJulgador orgaoJulgador, Sessao sessaoJulgamento) {

		boolean retorno = true;

		if (orgaoJulgador == null) {
			retorno = false;

		} else if (sessaoJulgamento != null) {
			SessaoPautaProcessoTrf processoPautado = recuperarProcessoPautadoSessao(voto.getProcessoTrf(), sessaoJulgamento);

			if (processoPautado != null) {
				retorno = processoPautado.getParticipaVotacao(orgaoJulgador);
			}

		}

		return retorno;
	}

	/**
	 * Recupera da sessao de julgamento a pauta que contem o processo.
	 * 
	 * @param processo
	 *            {@link ProcessoTrf} a ser pesquisado na sessao de julgamento
	 * 
	 * @param sessaoJulgamento
	 *            {@link Sessao} sessao de julgamento que contem a lista de de
	 *            processos pautados
	 * 
	 * @return a composicao de pauta processo {@link SessaoPautaProcessoTrf} que
	 *         representa o processo pautado na sessao de julgamento
	 */
	private SessaoPautaProcessoTrf recuperarProcessoPautadoSessao(ProcessoTrf processo, Sessao sessaoJulgamento) {

		SessaoPautaProcessoTrf processoPautadoRetorno = null;

		if (sessaoJulgamento != null && processo != null) {
			List<SessaoPautaProcessoTrf> processosPautados = sessaoJulgamento.getSessaoPautaProcessoTrfList();

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
	
	public boolean isUploadArquivoAssinadoRealizado() {
		return uploadArquivoAssinadoRealizado;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.arquivoAssinado = arquivoAssinadoHash;
		uploadArquivoAssinadoRealizado = Boolean.TRUE;
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

	public String getColor(Integer oj, Integer idJulgamento) {
		if (colorsMap.get(idJulgamento) == null) {
			Map<Integer, String> map = new HashMap<Integer, String>();
			map.put(oj, colors[0]);
			colorsMap.put(idJulgamento, map);
		} else if (colorsMap.get(idJulgamento).get(oj) == null) {
			colorsMap.get(idJulgamento).put(oj, colors[ncolor % colors.length]);
		}
		ncolor++;
		return colorsMap.get(idJulgamento).get(oj);
	}

	public String recuperarLabelVotoRelator() {
		String retorno = "Voto do relator - ";

		if (sessaoPautaProcessoTrf != null && sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor() != null) {
			retorno = "Voto vencedor - ";
		}

		return retorno;
	}

	/**
	 * Recupera o modelo do documento selecionado
	 * 
	 * @return String modelo do documento
	 */
	@Override
	public String recuperarModeloDocumento(String modeloDocumento) {
		selecionarModeloProcessoDocumento(modeloDocumento);
		return getModeloDocumento();
	}

	/**
	 * Obtem o modelo do documento
	 * 
	 * @return String
	 */
	public String getModeloDocumento() {
		if (getProtocolarDocumentoBean().getDocumentoPrincipal() != null && getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin() != null) {
			return getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().getModeloDocumento();
		}
		return null;
	}

	@Override
	public boolean isDocumentoAssinado() throws PJeBusinessException {
		boolean retorno = false;
		ProcessoDocumento processoDocumento = this.voto.getProcessoDocumento();
		if (processoDocumento != null) {
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
		Object variable = voto.getProcessoDocumento().getIdProcessoDocumento();
		if (variable != null && variable instanceof Integer) {
			getProtocolarDocumentoBean().setDocumentoPrincipal(voto.getProcessoDocumento());
			getProtocolarDocumentoBean().acaoRemoverTodos();
		}
		Contexts.getBusinessProcessContext().flush();
	}

	@Override
	public String obterConteudoDocumentoAtual() {
		String conteudo = this.getModeloDocumento();
		String conteudoJson = "";
		try {
			conteudoJson = ComponentUtil.getControleVersaoDocumentoManager().obterConteudoDocumentoJSON(conteudo);
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		
		return conteudoJson;
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
	public String obterTiposVoto() {
		RespostaDTO respostaDTO = new RespostaDTO();

		try {
			respostaDTO.setSucesso(Boolean.TRUE);

			RespostaTiposVotoDTO respostaTiposVotoDTO = new RespostaTiposVotoDTO();

			respostaTiposVotoDTO.setPodeAlterar(podeAlterarVoto());

			if (getVoto() != null) {
				TipoVoto tipoVoto = getVoto().getTipoVoto();

				if (tipoVoto != null) {
					respostaTiposVotoDTO.setSelecao(criarTipoVotoDTO(tipoVoto));
				}
			}

			if (isOrgaoJulgadorAtual()) {
				respostaTiposVotoDTO.setTipos(criarListaTiposVoto(ComponentUtil.getTipoVotoManager().listTipoVotoAtivoComRelator()));
			} else {
				respostaTiposVotoDTO.setTipos(criarListaTiposVoto(ComponentUtil.getTipoVotoManager().tiposVotosVogais()));
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

	/**
	 * Salva o conteudo do editor.
	 * 
	 * @param conteudo
	 */
	@Override
	public void salvar(String conteudo) {
		ProcessoDocumento doc = voto.getProcessoDocumento();
		doc.setTipoProcessoDocumento(this.getTipoProcessoDocumento());
		doc.setProcessoDocumento(this.getTipoProcessoDocumento().getTipoProcessoDocumento());

		if (doc != null && doc.getProcessoDocumentoBin() != null && doc.getProcessoDocumentoBin().getModeloDocumento() != null && !doc.getProcessoDocumentoBin().getModeloDocumento().isEmpty()) {
			voto.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(conteudo);
		}

		this.gravarVoto();
	}

	/**
	 * Verifica se o orgao Julgador e o atual
	 * 
	 * @return boolean
	 */
	public boolean isOrgaoJulgadorAtual() {
		OrgaoJulgador orgaoJulgador = getProcessoJudicial().getOrgaoJulgador();
		OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();

		return (orgaoJulgadorAtual == orgaoJulgador);
	}

	/**
	 * Verifica e obtem o processo judicial
	 * 
	 * @return ProcessoTrf
	 */
	public ProcessoTrf getProcessoJudicial() {
		if (processoJudicial == null) {
			processoJudicial = ComponentUtil.getTramitacaoProcessualService().recuperaProcesso();
		}
		return processoJudicial;
	}

	private void carregaListaPossiveisAcompanhados() {
		try {
			possiveisAcompanhados.clear();
			
			Set<Integer> listaVtosDivergentes = ComponentUtil.getSessaoProcessoDocumentoVotoManager().getVotosPorTipoContextoDivergencia(sessao, getProcessoJudicial(), true);
			
			for (Integer idOjAcompanhado : listaVtosDivergentes) {
				possiveisAcompanhados.add(ComponentUtil.getOrgaoJulgadorManager().findById(idOjAcompanhado));
			}
			
			if (Authenticator.getOrgaoJulgadorAtual() != null && !possiveisAcompanhados.contains(Authenticator.getOrgaoJulgadorAtual())) {
				possiveisAcompanhados.add(Authenticator.getOrgaoJulgadorAtual());
			}
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar recuperar órgão julgador acompanhado: {0}.", e.getLocalizedMessage());
		}
	}
}
