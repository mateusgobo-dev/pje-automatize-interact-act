package br.com.infox.pje.action;

import static br.com.itx.util.EntityUtil.find;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;

import br.com.infox.DAO.SearchField;
import br.com.infox.cliente.home.SessaoProcessoDocumentoVotoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.SessaoJulgamentoBean;
import br.com.infox.pje.list.SessaoProcessoTrfList;
import br.com.infox.pje.service.SessaoJulgamentoService;
import br.com.itx.exception.AplicationException;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.SituacaoVotoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;

/**
 * Classe action para a página /PJE2/Painel/painel_usuario/
 * Painel_Usuario_Magistrado_2_Grau/sessaoJulgamentoPopUp.xhtml
 * 
 * @author daniel
 * 
 */
@Name(SessaoJulgamentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class SessaoJulgamentoAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3578383628426437504L;

	public static final String NAME = "sessaoJulgamentoAction";

	@RequestParameter
	private Integer idSessao;
	
	private String currentTab;
	private Sessao sessao;

	@In
	private SessaoJulgamentoService sessaoJulgamentoService;

	@In
	private AbaVotoRelatorAction abaVotoRelatorAction;

	@In
	private AbaVotarAction abaVotarAction;

	@In
	private AbaDemaisVotosAction abaDemaisVotosAction;

	@In
	private AbaRelatorioAction abaRelatorioAction;

	@In
	private AbaEmentaAction abaEmentaAction;
	
	@In
	private ProcessoJudicialService processoJudicialService;

	private List<SessaoJulgamentoBean> sessaoJulgamentoBeanList;
	private SessaoProcessoTrfList sessaoProcessoTrfList = new SessaoProcessoTrfList();
	private ProcessoTrf processoTrf;

	private boolean showInteiroTeorProcesso;
	private boolean pesquisaAvancada;
	private boolean checkAll;
	private int sizeAll;

	private OrgaoJulgador orgaoJulgadorUsuarioInclusao;
	private String numeroProcesso;
	private OrgaoJulgador orgaoJulgadorSessao;
	private List<OrgaoJulgador> orgaoJulgadorSessaoList;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private String nomeRelator;
	private SituacaoVotoEnum situacaoVoto;
	private boolean cpf;
	private String cpfOuCnpj;
	private TipoPessoa tipoPessoa;
	private String nomeParte;
	private TipoProcessoDocumento tipoProcessoDocumentoVoto;
	private OrgaoJulgador orgaoJulgadorUsuarioLogado;
	private OrgaoJulgador orgaoJulgadorPedidoVista;
	private Boolean destaqueDiscussao;
	private Boolean sustentacaoOral;
	private Boolean preferencia;

	public static String[] SIGLAS_LEGENDAS = { "PR", "SO", "RE", "TD" };
	private static String[][] LEGENDAS_ARRAY = {
			{ "/img/aguardando.jpg", "Aguardando julgamento", String.valueOf(TipoSituacaoPautaEnum.AJ) },
			{ "/img/julgamento.jpg", "Em julgamento", String.valueOf(TipoSituacaoPautaEnum.EJ) },
			{ "/img/martelo.jpg", "Julgado", String.valueOf(TipoSituacaoPautaEnum.JG) },
			{ "/img/preferencia.jpg", "Preferência", SIGLAS_LEGENDAS[0] },
			{ "/img/vista.jpg", "Pedido de vista", String.valueOf(AdiadoVistaEnum.PV) },
			{ "/img/sustentacaoOral.jpg", "Pedido de sustentação oral", SIGLAS_LEGENDAS[1] },
			{ "/img/adiado.jpg", "Adiado para próxima sessão", String.valueOf(AdiadoVistaEnum.AD) },
			{ "/img/retirado.jpg", "Retirado de julgamento", SIGLAS_LEGENDAS[2] },
			{ "/img/lote.gif", "Todos", SIGLAS_LEGENDAS[3] } };

	private TipoVoto tipoVoto;
	private List<TipoVoto> tipoVotoList;
	private boolean showVotarLoteButton;
	
	@Create
	public void init(){
		if(idSessao != null && sessao == null){
			setIdSessao(idSessao);
		}
	}

	/**
	 * Sessão corrente, em execução.
	 */
	public void setIdSessao(Integer idSessao) {
		if (idSessao != null && sessao == null) {
			sessao = find(Sessao.class, idSessao);
			if (sessao == null) {
				throw new AplicationException("Sessão não encontrada: " + idSessao);
			}
			this.idSessao = idSessao;
		}
	}

	public Integer getIdSessao() {
		return idSessao;
	}

	public Sessao getSessao() {
		return this.sessao;
	}

	/*
	 * Inicio - Resultado sessão dia
	 */
	public long totalProcessoBySessao() {
		return sessaoJulgamentoService.totalIncluidos(sessao);
	}

	public long totalProcessoJulgadoBySessao() {
		return sessaoJulgamentoService.totalJulgados(sessao);
	}

	public long totalProcessoPedidoVistaBySessao() {
		return sessaoJulgamentoService.totalComVista(sessao);
	}

	public long totalProcessoAdiadoBySessao() {
		return sessaoJulgamentoService.totalAdiados(sessao);
	}

	public long totalProcessoRetiradoJulBySessao() {
		return sessaoJulgamentoService.totalRetirados(sessao);
	}

	/*
	 * Fim - Resultado sessão dia
	 */

	/*
	 * Inicio dos métodos que executam ações e invocam serviços
	 */

	/**
	 * Filtra o processo emm julgamento e automaticamente já o seleciona para
	 * exibir o inteiro teor do processo.
	 */
	public void filtrarProcessoEmJulgamento() {
		pesquisarProcessos(String.valueOf(TipoSituacaoPautaEnum.EJ));
		List<SessaoJulgamentoBean> julgamentoBeanList = sessaoJulgamentoBeanList();
		if (julgamentoBeanList != null && julgamentoBeanList.size() > 0) {
			onSelectProcessoTrf(julgamentoBeanList.get(0).getSessaoPautaProcessoTrf().getProcessoTrf());
			showInteiroTeorProcesso = true;
		}
	}

	/**
	 * Realiza o filtro de pesquisa dos processos.
	 */
	public void pesquisarProcessos(String sigla) {
		sessaoProcessoTrfList.setSearchFieldMap(new HashMap<String, SearchField>());
		sessaoProcessoTrfList.addSearchFields(situacaoVoto, getTipoProcessoDocumentoVoto());
		showInteiroTeorProcesso = false;
		if (sigla != null && !"".equals(sigla) && !sigla.equals(SessaoJulgamentoAction.SIGLAS_LEGENDAS[3])) {
			sessaoProcessoTrfList.addSearchFields(sigla);
		}
		sessaoJulgamentoBeanList = null;
	}

	/**
	 * Array para popular a tabela de legendas.
	 * 
	 * @return caminho do ícone, label e sigla
	 */
	public String[][] getLegendasItems() {
		return LEGENDAS_ARRAY;
	}

	/**
	 * Modifica o tipo da pesquisa, limpando o formulário que não será
	 * utilizado. O que evita filtros sem o conhecimento do usuario, caso sejam
	 * populados campos na pesquisa avançada e o usuario mude para a simples e
	 * execute a pesquisa, ou vice-versa.
	 */
	public void mudarTipoPesquisa() {
		if (pesquisaAvancada) {
			pesquisaAvancada = false;
			numeroProcesso = null;
			orgaoJulgadorSessao = null;
			classeJudicial = null;
			assuntoTrf = null;
			nomeRelator = null;
			situacaoVoto = null;
			cpfOuCnpj = null;
			tipoPessoa = null;
			nomeParte = null;
			orgaoJulgadorUsuarioInclusao = null;
		} else {
			pesquisaAvancada = true;
			orgaoJulgadorSessao = null;
		}
	}

	/**
	 * Verifica se o orgaoJulgador informado é igual ao do usuarioLogado.
	 * 
	 * @param orgaoJulgador
	 *            que se deseja verificar
	 * @return true se for igual ao do usuario logado
	 */
	public boolean isOrgaoJulgadorLogado(OrgaoJulgador orgaoJulgador) {
		boolean ret = true;
		if (orgaoJulgador != null
				&& orgaoJulgador.getIdOrgaoJulgador() == getOrgaoJulgadorUsuarioLogado().getIdOrgaoJulgador()) {
			ret = false;
		}
		return ret;
	}

	/**
	 * Seta no action da abas que serão executadas qual é o processo Trf
	 * selecionado pelo usuário.
	 */
	public void onSelectProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
		showInteiroTeorProcesso = true;

		abaRelatorioAction.newInstance();
		abaRelatorioAction.setProcessoTrf(processoTrf);
		abaRelatorioAction.setSessao(sessao);

		abaVotoRelatorAction.newInstance();
		abaVotoRelatorAction.setProcessoTrf(processoTrf);
		abaVotoRelatorAction.setSessao(sessao);
		abaVotoRelatorAction.setSessaoProcessoDocumentoVoto(null);

		abaVotarAction.newInstance();
		abaVotarAction.setProcessoTrf(processoTrf);
		abaVotarAction.setSessao(sessao);
		abaVotarAction.setSessaoProcessoDocumentoVoto(null);

		abaDemaisVotosAction.newInstance();
		abaDemaisVotosAction.setVisualizarProcesso(false);
		abaDemaisVotosAction.setProcessoTrf(processoTrf);
		abaDemaisVotosAction.setSessao(sessao);

		abaEmentaAction.newInstance();
		abaEmentaAction.setProcessoTrf(processoTrf);
		abaEmentaAction.setSessao(sessao);
	}

	/**
	 * Marcar ou desmarca todos os proecessos da sessão em execução.
	 */
	public void checkAllProcessos() {
		showInteiroTeorProcesso = false;
		sizeAll = 0;
		for (SessaoJulgamentoBean sjb : sessaoJulgamentoBeanList) {
			if (sjb.isRendered()) {
				sjb.setCheck(checkAll);
				if (checkAll) {
					sizeAll++;
				}
			}
		}
	}

	/**
	 * Obtem a lista de resultados dos processos da sessão em execução.
	 * 
	 * @param maxResults
	 *            max de resultados desejado.
	 * @return lista de sessaoJulgamentoBean
	 */
	public List<SessaoJulgamentoBean> sessaoJulgamentoBeanList() {
		if (sessaoJulgamentoBeanList == null) {
			sessaoJulgamentoBeanList = new ArrayList<SessaoJulgamentoBean>();
			for (SessaoPautaProcessoTrf sppTrf : sessaoProcessoTrfList.list()) {
				SessaoJulgamentoBean sjb = null;
				if(Authenticator.isProcurador()){
					sjb = new SessaoJulgamentoBean(sppTrf, false, false);
				}else{
					sjb = new SessaoJulgamentoBean(sppTrf, false, isOrgaoJulgadorLogado(sppTrf
							.getProcessoTrf().getOrgaoJulgador()));
				}
				// verifica se exite voto com destaque para discução na sessão
				sjb.setDestacadoSessao(SessaoProcessoDocumentoVotoHome.instance().existeVotoDestacadoDiscussao(
						sjb.getSessaoPautaProcessoTrf()));
				sessaoJulgamentoBeanList.add(sjb);
			}
		}
		return sessaoJulgamentoBeanList;
	}

	/**
	 * Obtem a lista de ids concatenados por virgula dos processos selecionados
	 * na Grid.
	 * 
	 * @return lista de ids dos processos concatenados por virgula.
	 */
	public String getIdProcessoTrfList() {
		StringBuilder sb = new StringBuilder();
		for (SessaoJulgamentoBean sjb : sessaoJulgamentoBeanList) {
			if (sjb.getCheck()) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				ProcessoTrf processoTrf = sjb.getSessaoPautaProcessoTrf().getProcessoTrf();
				sb.append(processoTrf.getIdProcessoTrf());
			}
		}
		return sb.toString();
	}

	/**
	 * Verifica se a sessão corrente está encerrada.
	 * 
	 * @return true se estiver encerrada.
	 */
	public boolean isSessaoEncerrada() {
		return sessao.getDataRealizacaoSessao() != null && sessao.getDataRegistroEvento() == null;
	}

	/**
	 * Verifica se já foram marcados mais de dois processos para habilitar a
	 * região de votar em lote.
	 */
	public void checkVotarLote() {
		int count = 0;
		sizeAll = 0;
		for (SessaoJulgamentoBean sjb : sessaoJulgamentoBeanList) {
			if (count == 2) {
				checkAll = true;
				break;
			} else if (sjb.getCheck()) {
				count++;
				sizeAll++;
			}
		}
		if (count < 2) {
			checkAll = false;
		}
	}

	public String getTipoVotoProcessoTrf(ProcessoTrf processoTrf) {
		TipoVotoManager tvm = new TipoVotoManager();
		return tvm.getLetraTipoVoto(sessaoJulgamentoService.listTipoVotoDocumentoSessaoRelator(sessao,
				processoTrf.getIdProcessoTrf(), getTipoProcessoDocumentoVoto(), getOrgaoJulgadorUsuarioLogado()));
	}

	/*
	 * Inicio dos métodos que executam ações e invocam serviços
	 */

	public Usuario getUsuarioRelator() {
		return processoJudicialService.recuperaRelator(processoTrf);
	}

	public String getDataSessaoFormatted() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(sessao.getDataSessao());
	}

	/*
	 * Inicio filtros - Pesquisa de processos (get/set)
	 */

	public void setOrgaoJulgadorUsuarioInclusao(OrgaoJulgador orgaoJulgadorUsuarioInclusao) {
		this.orgaoJulgadorUsuarioInclusao = orgaoJulgadorUsuarioInclusao;
	}

	public OrgaoJulgador getOrgaoJulgadorUsuarioInclusao() {
		return orgaoJulgadorUsuarioInclusao;
	}

	public void setPesquisaAvancada(boolean pesquisaAvancada) {
		this.pesquisaAvancada = pesquisaAvancada;
	}

	public boolean isPesquisaAvancada() {
		return pesquisaAvancada;
	}

	public int getSizeAll() {
		return sizeAll;
	}

	public void setSizeAll(int sizeAll) {
		this.sizeAll = sizeAll;
	}

	public void setShowInteiroTeorProcesso(boolean showInteiroTeorProcesso) {
		this.showInteiroTeorProcesso = showInteiroTeorProcesso;
	}

	public boolean getShowInteiroTeorProcesso() {
		return showInteiroTeorProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public AbaVotarAction getAbaVotarAction() {
		return abaVotarAction;
	}

	public void setAbaVotarAction(AbaVotarAction abaVotarAction) {
		this.abaVotarAction = abaVotarAction;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setOrgaoJulgadorSessaoList(List<OrgaoJulgador> orgaoJulgadorSessao) {
		this.orgaoJulgadorSessaoList = orgaoJulgadorSessao;
	}

	public void setOrgaoJulgadorSessao(OrgaoJulgador orgaoJulgadorSessao) {
		this.orgaoJulgadorSessao = orgaoJulgadorSessao;
	}

	public OrgaoJulgador getOrgaoJulgadorSessao() {
		return this.orgaoJulgadorSessao;
	}

	public SituacaoVotoEnum[] getSituacaoVotoEnumList() {
		return SituacaoVotoEnum.values();
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public AbaEmentaAction getAbaEmentaAction() {
		return abaEmentaAction;
	}

	public void setAbaEmentaAction(AbaEmentaAction abaEmentaAction) {
		this.abaEmentaAction = abaEmentaAction;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setNomeRelator(String nomeRelator) {
		this.nomeRelator = nomeRelator;
	}

	public String getNomeRelator() {
		return nomeRelator;
	}

	public void setSituacaoVoto(SituacaoVotoEnum situacaoVoto) {
		this.situacaoVoto = situacaoVoto;
	}

	public SituacaoVotoEnum getSituacaoVoto() {
		return situacaoVoto;
	}

	public void setCpf(boolean cpf) {
		this.cpf = cpf;
	}

	public boolean isCpf() {
		return cpf;
	}

	public void setCpfOuCnpj(String cpfOuCnpj) {
		this.cpfOuCnpj = cpfOuCnpj;
	}

	public String getCpfOuCnpj() {
		return cpfOuCnpj;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public TipoProcessoDocumento getTipoProcessoDocumentoVoto() {
		if (tipoProcessoDocumentoVoto == null) {
			tipoProcessoDocumentoVoto = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
		}
		return tipoProcessoDocumentoVoto;
	}

	public OrgaoJulgador getOrgaoJulgadorUsuarioLogado() {
		if (orgaoJulgadorUsuarioLogado == null) {
			orgaoJulgadorUsuarioLogado = Authenticator.getOrgaoJulgadorAtual();
		}
		return orgaoJulgadorUsuarioLogado;
	}

	public List<OrgaoJulgador> getOrgaoJulgadorSessaoList() {
		if (orgaoJulgadorSessaoList == null) {
			orgaoJulgadorSessaoList = sessaoJulgamentoService.listOrgaoJulgadorComposicaoSessao(sessao);
		}
		return orgaoJulgadorSessaoList;
	}

	/*
	 * Fim filtros - Pesquisa de Processos (get/set)
	 */

	public void setSessaoProcessoTrfList(SessaoProcessoTrfList sessaoProcessoTrfList) {
		this.sessaoProcessoTrfList = sessaoProcessoTrfList;
	}

	public SessaoProcessoTrfList getSessaoProcessoTrfList() {
		return sessaoProcessoTrfList;
	}

	public void setSessaoJulgamentoBeanList(List<SessaoJulgamentoBean> sessaoJulgamentoBeanList) {
		this.sessaoJulgamentoBeanList = sessaoJulgamentoBeanList;
	}

	/*
	 * Inicio votar em lote
	 */

	public void setCheckAll(boolean checkAll) {
		this.checkAll = checkAll;
	}

	public boolean isCheckAll() {
		return checkAll;
	}

	public void setTipoVoto(TipoVoto tipoVoto) {
		this.tipoVoto = tipoVoto;
	}

	public TipoVoto getTipoVoto() {
		return tipoVoto;
	}

	public void setTipoVotoList(List<TipoVoto> tipoVotoList) {
		this.tipoVotoList = tipoVotoList;
	}

	public List<TipoVoto> getTipoVotoList() {
		if (tipoVotoList == null) {
			tipoVotoList = sessaoJulgamentoService.listTipoVotoAtivoSemRelator();
		}
		return tipoVotoList;
	}

	public void setShowVotarLoteButton(boolean showVotarLoteButton) {
		if (tipoVoto == null) {
			this.showVotarLoteButton = false;
		} else {
			this.showVotarLoteButton = showVotarLoteButton;
		}
	}

	public boolean isShowVotarLoteButton() {
		return showVotarLoteButton;
	}

	public AbaVotoRelatorAction getAbaVotoRelatorAction() {
		return abaVotoRelatorAction;
	}

	public void setAbaVotoRelatorAction(AbaVotoRelatorAction abaVotoRelatorAction) {
		this.abaVotoRelatorAction = abaVotoRelatorAction;
	}

	public AbaRelatorioAction getAbaRelatorioAction() {
		return abaRelatorioAction;
	}

	public void setAbaRelatorioAction(AbaRelatorioAction abaRelatorioAction) {
		this.abaRelatorioAction = abaRelatorioAction;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setCurrentTab(String currentTab) {
		this.currentTab = currentTab;
	}

	public String getCurrentTab() {
		if (this.currentTab == null) {
			currentTab = "abaRelatorio";
		}
		return currentTab;
	}

	public void setOrgaoJulgadorPedidoVista(OrgaoJulgador orgaoJulgadorPedidoVista) {
		this.orgaoJulgadorPedidoVista = orgaoJulgadorPedidoVista;
	}

	public OrgaoJulgador getOrgaoJulgadorPedidoVista() {
		return orgaoJulgadorPedidoVista;
	}

	public void setAbaDemaisVotosAction(AbaDemaisVotosAction abaDemaisVotosAction) {
		this.abaDemaisVotosAction = abaDemaisVotosAction;
	}

	public AbaDemaisVotosAction getAbaDemaisVotosAction() {
		return abaDemaisVotosAction;
	}

	public void setSustentacaoOral(Boolean sustentacaoOral) {
		this.sustentacaoOral = sustentacaoOral;
	}

	public Boolean getSustentacaoOral() {
		return sustentacaoOral;
	}

	public void setPreferencia(Boolean preferencia) {
		this.preferencia = preferencia;
	}

	public Boolean getPreferencia() {
		return preferencia;
	}

	public void setDestaqueDiscussao(Boolean destaqueDiscussao) {
		this.destaqueDiscussao = destaqueDiscussao;
	}

	public Boolean getDestaqueDiscussao() {
		return destaqueDiscussao;
	}

	/*
	 * Fim votar em lote
	 */

}