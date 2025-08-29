package br.com.infox.cliente.home;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.component.suggest.PessoaProcuradorMPSuggestBean;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.PautaJulgamentoList;
import br.com.infox.pje.list.ProcessoTrfMesaList;
import br.com.infox.pje.list.SessaoComposicaoOrdemSecretarioSessaoList;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.UrlUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.com.itx.util.LocalizacaoUtil;
import br.com.jt.pje.manager.SessaoManager;
import br.jus.cnj.pje.business.dao.SessaoPautaProcessoTrfDAO;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DiaSemanaManager;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorCargoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoComposicaoOrdemManager;
import br.jus.cnj.pje.nucleo.manager.SessaoJulgamentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.service.ComposicaoJulgamentoService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.SessaoJulgamentoService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.util.FormatadorUtils;
import br.jus.csjt.pje.business.pdf.HtmlParaPdf;
import br.jus.csjt.pje.business.pdf.PdfException;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.dto.SugestaoAdiamentoJulgamentoProcessoDTO;
import br.jus.pje.nucleo.entidades.CalendarioEvento;
import br.jus.pje.nucleo.entidades.ConsultaProcessoAdiadoVista;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrfSemFiltro;
import br.jus.pje.nucleo.entidades.DiaSemana;
import br.jus.pje.nucleo.entidades.DocumentoSessao;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.SalaHorario;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.AbrangenciaEnum;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.CadastroMensalEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.RelatorRevisorEnum;
import br.jus.pje.nucleo.enums.SimNaoFacultativoEnum;
import br.jus.pje.nucleo.enums.SituacaoSessaoEnum;
import br.jus.pje.nucleo.enums.StatusSessaoEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name(SessaoHome.NAME)
@BypassInterceptors
public class SessaoHome extends AbstractHome<Sessao> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "sessaoHome";
	private static final int DIAS_MES = 31;
	private Sala sala;
	private Boolean repetir = Boolean.FALSE;
	private DocumentoSessao documentoSessao;
	private List<Sessao> listaSessoesProcesso;
	private List<Sessao> listaSessoesProcurador;
	
	// usado no cadastro de sessão para definir qual a periodicidade da
	// repetição do cadastro diário
	private String flagRepete = "-";

	// usado no cadastro de sessão para definir qual a Classificação da
	// repetição do cadastro menssal
	private int flagClassificacao = 0;
	private Date dataInicial;
	private Date dataFinal;

	// usado no cadastro de sessão para determinar a quantidade de dias que o
	// cadastro vai pular para tentar cadastrar uma nova sessão
	private int numeroDias = 1;

	// usado no cadastro de sessão para determinar a quantidade de meses que o
	// cadastro vai pular para tentar cadastrar uma nova sessão
	private int numeroMeses = 1;

	// usado no cadastro de sessão para determinar a quantidade de anos que o
	// cadastro vai pular para tentar cadastrar uma nova sessão
	private int numeroAnos = 1;

	// usado no cadastro de sessão para determinar o dia em caso de prazo anual
	// por dia
	private int diaAnual;

	// usado no cadastro de sessão para determinar a quantidade de semanas que o
	// cadastro vai pular para tentar cadastrar uma nova sessão
	private int numeroSemanas = 1;

	// no cadastro da sessão define se será somente nos dias uteis ou, por
	// padrão, em uma periodicidade de dias informada no atributo numeroDias
	private boolean dias = true;

	// no cadastro da sessão define se será por oedem de classificação, por
	// padrão, em uma repetição nos dias selecionados
	private CadastroMensalEnum ordemClassificacao = CadastroMensalEnum.O;

	// no cadastro da sessão define no prazo anual devine os o mês que o
	// cadastro da sessão deverar tentar cadastrar a sessão
	private int mes;

	// fag par marcar ou desmarcar todos na composição da sessão
	private boolean flagMarcaDesmarcaTodos = false;

	private Date dataFechamentoPauta;

	// flag que controla exibição do modal de confirmação de adiamento de
	// processos no a,to do encerramento da sessão
	private boolean exibeMPConfirmacaoAdiaProcs = false;

	// flag que controla exibição do modal de processos julgados pendentes de
	// proclamação de julgamento
	private boolean exibeMPProcsPendentesProclamacao = false;

	private boolean exibeMPConfirmacaoGeracaoComposicaoInicial = false;
	private boolean exibeMPConfirmacaoAtualizacaoComposicaoProcessos = false;
	private boolean exibeMPConfirmacaoAtualizacaoComposicaoPrincipalSessao = false;
	private boolean exibeMPConfirmacaoOrgaosSemTitulares = false;
	private String orgaosSemTitulares = null;
	private boolean criandoComposicaoInicial = true;
	private boolean exibeMPConfirmacaoAtualizacaoPresidenteSessaoIniciada = false;
	
	private List<SugestaoAdiamentoJulgamentoProcessoDTO> sugestoesAdiamentoJulgamentoProcessoPorAusenciaJulgadoresPrincipais;
	
	// lista de processos julgados com proclamação de julgamento pendente
	private List<SessaoPautaProcessoTrf> spptProclamacaoPendenciaList;

	// variavel usada no suggest de processo em mesa
	private ProcessoTrf processoTrf;
	private Boolean exigePauta;

	private Set<OrgaoJulgador> listaOJ = new HashSet<OrgaoJulgador>(0);

	private Set<SalaHorario> salaHorarioSet = new HashSet<SalaHorario>();
	private Boolean marcouListTudo = Boolean.FALSE;
	private List<ProcessoTrf> listTRF = new ArrayList<ProcessoTrf>();
	private List<ProcessoTrf> listProcessosSemDocumentosSessao = new ArrayList<ProcessoTrf>();
	@SuppressWarnings("unused")
	private String procurador;
	private PessoaProcurador pessoaProcurador;
	private Date dataAtual;
	private OrgaoJulgador presidente = new OrgaoJulgador();
	private OrgaoJulgadorColegiado orgaoJulgadorColegiadoRevisor = new OrgaoJulgadorColegiado();
	private boolean habilitaCombo = true;
	private Collection<Integer> diasSelecionados = new TreeSet<Integer>();
	private Boolean[] checkDiasSelecionados = new Boolean[DIAS_MES];

	// lista de oj que estão vinculados ao mesmo OJC com os oj participantes da
	// composição marcados, (selecionados == True)
	private Collection<OrgaoJulgador> listaOjOjcComposicao = new ArrayList<OrgaoJulgador>();

	// lista de oj participantes da composição marcados
	private Collection<OrgaoJulgador> listaOjOjcCompSelecionados = new ArrayList<OrgaoJulgador>();

	private boolean carregaSuggest = false;
	
	private String modeloDocumento;

	private String variavelTitulo;
	
	private String presidenteSessao;
	private String composicaoSessao;
	Map<String, List<Pessoa>> destinatariosNaoIntimadosViaSistema;
	private Sessao ultimaSessaoProcesso;
	
	//armazena a data da sessao carregada previamente para exibiçao em tela
	private String dataSessaoString = null;
	
	private boolean exibeMPConfirmacaoInclusaoProcessoBlocoPauta;
	private boolean exibeMPConfirmacaoFecharPauta;

	List<ProcessoTrf> processosBlocosNaoPautados;
	
	private Map<ProcessoTrf, List<String>> processosComPendenciaMap = new HashMap<>();
	
	// Variáveis utilizadas para cache.
	private List<SessaoPautaProcessoTrf> listaSessaoPautaProcessoTrf = new ArrayList<>();
	private List<SessaoPautaProcessoTrf> sessaoPautaProcessosSessaoAtual = new ArrayList<>();
	private Map<Long, List<Sessao>> listSessaoDateCompleto = new LinkedHashMap<>();
	private Boolean habilitaBotaoAtaJulgamento;
	private Boolean habilitaBotaoFinalizar;
	
	@Logger
	private transient Log logger;
	
	/**
	 * Metodo que retorna os órgãos julgadores na última sessão do processo.
	 * 
	 * @param processoTrf
	 * @return String nomes separados por vírgula
	 */
	public String retornaUltimaComposicaoSessaoJulgamentoPorProcesso(ProcessoTrf processoTrf){
		String retorno = StringUtils.EMPTY;
		preencherProcessoValido(processoTrf);
		if(this.processoTrf != null) {
			preencherUltimaSessaoProcesso();
			if (ultimaSessaoProcesso != null){
				List<SessaoComposicaoOrdem> sessaoComposicaoOrdems = ultimaSessaoProcesso.getComposicoesPresentes();
				retorno = obterTextoUltimaComposicaoJulgamento(sessaoComposicaoOrdems);
			}
		}
		return retorno;
	}

	/**
	 * Metodo que formata a composicao em texto para a apresentação no documento.
	 * 
	 * @param sessaoComposicaoOrdems Ordem da Composicao da Sessao
	 * @return Texto para apresentacao da ultima composicao de julgamento. 
	 */
	private String obterTextoUltimaComposicaoJulgamento(List<SessaoComposicaoOrdem> sessaoComposicaoOrdems) {
		List<String> nomesMagistrados = new ArrayList<String>(sessaoComposicaoOrdems.size());
		for (SessaoComposicaoOrdem sessaoComposicaoOrdem : sessaoComposicaoOrdems) {
			if(sessaoComposicaoOrdem.getMagistradoPresenteSessao() != null){
				nomesMagistrados.add(sessaoComposicaoOrdem.getMagistradoPresenteSessao().toString());
			} else if (sessaoComposicaoOrdem.getMagistradoSubstitutoSessao() != null){
				nomesMagistrados.add(sessaoComposicaoOrdem.getMagistradoSubstitutoSessao().toString());
			}
		}
		Collections.sort(nomesMagistrados);
		return StringUtils.join(nomesMagistrados,", ");
	}

	/**
	 * Metodo que verifica se a ultima sessao esta preenchida, senao preenche.
	 */
	private void preencherUltimaSessaoProcesso() {
		if (ultimaSessaoProcesso == null){
			ultimaSessaoProcesso = getUltimaSessaoProcesso(this.processoTrf.getIdProcessoTrf());
		}
	}
	
	/**
	 * Metodo que preenche o processo valido.
	 * 
	 * @param processoTrf Processo
	 */
	private void preencherProcessoValido(ProcessoTrf processoTrf){
		if (this.processoTrf == null){
			setProcessoTrf(retornaProcessoTrfPorNumeroEPessoa(processoTrf));
		}
	}
	
	/**
	 * Metodo que retorna a proclamação do julgamento do processo na última sessão.
	 * @param processoTrf
	 * @return String
	 */
	public String retornaProclamacaoJulgamentoPorProcesso(ProcessoTrf processoTrf){
		String retorno = StringUtils.EMPTY;
		preencherProcessoValido(processoTrf);
		if(this.processoTrf != null) {
			List<SessaoPautaProcessoTrf> spptList = getSessaoPautaProcessoTrfManager().getSessoesJulgamentoPautados(this.processoTrf);
			for (SessaoPautaProcessoTrf sppt : spptList) {
				if (isProclamacaoProcessoValida(sppt)) {
					retorno = sppt.getProclamacaoDecisao();
				}
			}
		}
		return retorno;
	}

	/**
	 * Metodo que verifica se a proclamacao de julgamento esta valida.
	 * - Julgado
	 * - Decisao preenchida
	 * 
	 * @param sppt Processo pautado na sessao
	 * @return True se a proclamacao de julgamento for valida.
	 */
	private boolean isProclamacaoProcessoValida(SessaoPautaProcessoTrf sppt) {
		return sppt.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG)
				&& !Strings.isEmpty(sppt.getProclamacaoDecisao())
				&& sppt.getProcessoTrf().equals(this.processoTrf);
	}
	
	/**
	 * Metodo que retorna Procurador da última sessão por processo.
	 * 
	 * @param processoTrf processo
	 * @return String nome do procurador
	 */
	public String retornaProcuradorUltimaSessaoPorProcesso(ProcessoTrf processoTrf){
		String retorno = StringUtils.EMPTY;
		preencherProcessoValido(processoTrf);
		if(this.processoTrf != null) {
			Sessao ultimaSessaoProcesso = getUltimaSessaoProcesso(this.processoTrf.getIdProcessoTrf());
			if (isProcuradorPresenteNaSessao(ultimaSessaoProcesso)){
				retorno = ultimaSessaoProcesso.getPessoaProcurador().getNome();
			}
		}
		return retorno;
	}

	/**
	 * Veirifca se na sessão tem algum procurador
	 * @param ultimaSessaoProcesso - Sessão do processo
	 * @return true se o procurador estiver presente na sessão.
	 */
	private boolean isProcuradorPresenteNaSessao(Sessao ultimaSessaoProcesso) {
		return ultimaSessaoProcesso != null 
				&& ultimaSessaoProcesso.getPessoaProcurador() != null 
				&& StringUtils.isNotEmpty(ultimaSessaoProcesso.getPessoaProcurador().getNome());
	}
	
	/**
	 * Metodo que retorna dados da ata da sessão de julgamento por processo.
	 * 
	 * @param processoTrf
	 * @return String
	 */
	public String retornaDadosAtaSessaoJulgamentoPorProcesso(ProcessoTrf processoTrf){
		String retorno = StringUtils.EMPTY;
		try {
			preencherProcessoValido(processoTrf);
			if(this.processoTrf != null) {
				List<SessaoPautaProcessoTrf> spptList = getSessaoPautaProcessoTrfManager().getSessoesJulgamentoPautados(this.processoTrf);
				List<SessaoPautaProcessoTrf> processos = new ArrayList<SessaoPautaProcessoTrf>(1);
				for (SessaoPautaProcessoTrf sppt : spptList) {
					if (isProclamacaoProcessoValida(sppt)) {
						processos.add(sppt);
						retorno += buildHTMLSessao(processos);
						retorno += addDadosProcesso("Data Sessão","<b>"+DateUtil.dateHourToString(sppt.getSessao().getDataRealizacaoSessao())+"</b>");
						processos.clear();
					}
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return retorno;
	}

	/**
	 * Metodo que retorna o processo pelo número e pessoa logada.
	 * 
	 * @param processoTrf
	 * @return ProcessoTrf
	 */
	private ProcessoTrf retornaProcessoTrfPorNumeroEPessoa(ProcessoTrf processoTrf) {
		if (processoTrf.getIdProcessoTrf() == 0 && StringUtils.isNotEmpty(processoTrf.getNumeroProcesso())){
			processoTrf = ComponentUtil.getComponent(ProcessoTrfManager.class).recuperarProcesso(processoTrf.getNumeroProcesso(), Authenticator.getPessoaLogada());
		}
		return processoTrf;
	}
	
	public String getPresidenteSessao() {
		return presidenteSessao;
	}

	public void setPresidenteSessao(String presidenteSessao) {
		this.presidenteSessao = presidenteSessao;
	}

	public String getComposicaoSessao() {
		return composicaoSessao;
	}

	public void setComposicaoSessao(String composicaoSessao) {
		this.composicaoSessao = composicaoSessao;
	}

	public String getVariavelTitulo() {
		return variavelTitulo;
	}

	/**
	 * Grava a proclamação de uma SessaoPautaProcessoTrf na instancia de
	 * SessaoPautaProcessoTrfHome
	 */
	public void gravarProclamacao() {
		// atualiza a proclamação da SessaoPautaProcessoTrf

		// Verifica se o texto esta com algum valor
		if (Strings.isEmpty(SessaoPautaProcessoTrfHome.instance().getInstance().getProclamacaoDecisao())) {
			FacesMessages.instance().add(Severity.ERROR, "Antes de gravar, preencha a proclamação.");
			return;
		}

		// Verifica se ao menos um processo foi selecionado
		if (SessaoPautaProcessoTrfHome.instance().getInstance().getIdSessaoPautaProcessoTrf() == 0) {
			FacesMessages.instance().add(Severity.ERROR, "Escolha um processo antes de Gravar.");
			return;
		}
		getEntityManager().merge(SessaoPautaProcessoTrfHome.instance().getInstance());
		EntityUtil.flush();

		// remove da lista de pendencias
		spptProclamacaoPendenciaList.remove(SessaoPautaProcessoTrfHome.instance().getInstance());

		// limpa a instancia da proclamação
		SessaoPautaProcessoTrfHome.instance().setInstance(null);
	}

	/**
	 * Adiciona os gabinetes que irão compor a sessão.
	 * 
	 * @param OrgaoJulgador
	 *            row selecionada pelo usuario
	 */
	public void addRemoveComposicao(OrgaoJulgador ojRow) {
		if (listaOjOjcCompSelecionados.contains(ojRow)) {
			if (existePautaSessao()) {
				FacesMessages
				.instance()
				.add("Existe processo na Relação de Julgamento. Não é possível remover um órgão julgador da composição da sessão.");
			} else {
				// seta o selecionado da linha como false
				ojRow.setSelecionado(false);
				// retira o OJ na lista dos selecionados para composição.
				listaOjOjcCompSelecionados.remove(ojRow);
				if (ojRow.equals(getPresidente())) {
					setPresidente(null);
				}
			}
		} else {
			// seta o selecionado da linha como true
			ojRow.setSelecionado(true);
			// insere o OJ na lista dos selecionados para composição e seta ele
			// como selecionado.
			listaOjOjcCompSelecionados.add(ojRow);
		}
	}

	/**
	 * Metodo para marcar ou desmarcar todos
	 */
	public void marcaDesmarcaTodos() {
		if (existePautaSessao()) {
			FacesMessages
			.instance()
			.add("Existe processo na Relação de Julgamento. Não é possível remover um órgão julgador da composição da sessão.");
			return;
		}
		// limpa a lista dos selecionados para compor a sessão
		listaOjOjcCompSelecionados.clear();

		// limpa presidente da sessão
		this.presidente = null;

		// varre a lista de oj do mesmo colegiado
		for (OrgaoJulgador oj : listaOjOjcComposicao) {
			// seta como selecionado na lista caso esteja marcado todos
			oj.setSelecionado(flagMarcaDesmarcaTodos);

			// caso a opção seja incluir todos adiciona todos do colegiado a
			// composição da sessão
			if (flagMarcaDesmarcaTodos) {
				listaOjOjcCompSelecionados.add(oj);
			}
		}
	}

	/**
	 * Define quem será p presidente da sessão
	 * 
	 * @param ojRow
	 *            Linha selecionada da grid.
	 */
	public void definePresidente(OrgaoJulgador ojRow) {
		// guarda o oj de quem será o presidente
		setPresidente(ojRow);
		// veirifica se o oj selecionado já encontra se na lista de
		// participantes da composição
		// se não estiver adiciona o OJ selecionado na composição
		if (!listaOjOjcCompSelecionados.contains(ojRow)) {
			// seta o selecionado da linha como true
			ojRow.setSelecionado(true);
			// insere o OJ na lista dos selecionados para composição e seta ele
			// como selecionado.
			listaOjOjcCompSelecionados.add(ojRow);
		}
	}

	public void verificaDisponibilidadeRevisor(OrgaoJulgador ojRow) {
		boolean disponivel = true;
		// pega lista de OJ selecionados para a composição menos o ojRow
		Collection<OrgaoJulgador> listaOjSelecionadosMenosOJ = new ArrayList<OrgaoJulgador>(listaOjOjcCompSelecionados.size());
		listaOjSelecionadosMenosOJ.addAll(listaOjOjcCompSelecionados);
		if (listaOjSelecionadosMenosOJ.contains(ojRow)) {
			listaOjSelecionadosMenosOJ.remove(ojRow);
		}
		// varre a lista dos oj selecionados para verificar se o revisor setado
		// no row já é revisor de algum na lista.
		for (OrgaoJulgador oj : listaOjSelecionadosMenosOJ) {
			// verifica se o elemento da lista possue revisor
			if (oj.getOjRevisor() != null) {
				if (oj.getOjRevisor().equals(ojRow.getOjRevisor())) {
					disponivel = false;
				}
			}
		}
		// se o revisor não estiver disponivel limpa o valor do revisor da linha
		if (!disponivel) {
			ojRow.setOjRevisor(null);
			FacesMessages.instance().add("Revisor já selecionado para outro gabinete.");
		}
	}
	
	/**
	 * metodo responsavel por retornar todas os sessaoPautaProcessos da sessao atual, sem a aplicacao de filtros.
	 * @return List<SessaoPautaProcessoTrf>
	 */
	private List<SessaoPautaProcessoTrf> recuperaSessaoPautaProcessosSessaoAtual() {
		if (this.sessaoPautaProcessosSessaoAtual.isEmpty()) {
			try {
				this.sessaoPautaProcessosSessaoAtual = getSessaoPautaProcessoTrfManager()
						.recuperarTodosSessaoPautaProcessosTrf(getSessaoIdSessao());
				
			} catch (Exception e) {
				logger.error(e, "Erro no metodo: SessaoHome.recuperaSessaoPautaProcessosSessaoAtual ");
				ComponentUtil.getFacesMessages().add(Severity.ERROR, "Erro ao carregar o(s) processo(s).");
			}			
		}
		return this.sessaoPautaProcessosSessaoAtual;
	}

	public String iniciarSessao() {
		Sessao sessao = getInstance(); 
		if (sessao.getDataFechamentoPauta() == null) {
			FacesMessages.instance().add(Severity.WARN, 
				"A pauta de julgamento para esta sessão ainda está aberta. Feche a pauta antes de proceder com o início da sessão.");
			
			return null;
		}

		sessao.setIniciar(!sessao.getIniciar());
		
		if (sessao.getDataAberturaSessao() == null) {
			sessao.setDataAberturaSessao(new Date());
			List<SessaoPautaProcessoTrf> sessaoPautaProcessoTrfList = recuperaSessaoPautaProcessosSessaoAtual();
			
		    if(sessaoPautaProcessoTrfList != null){
		    	PessoaMagistrado presidente = sessao.getPresidenteSessao();
		    	
				for (SessaoPautaProcessoTrf sessaoPautaProcessoTrf: sessaoPautaProcessoTrfList){
					if (sessaoPautaProcessoTrf.getPresidente() == null) {
						sessaoPautaProcessoTrf.setPresidente(presidente);
						getEntityManager().merge(sessaoPautaProcessoTrf);
					}
					
					if(sessao.getContinua() != null && sessao.getContinua() &&
							sessaoPautaProcessoTrf.getSituacaoJulgamento() != null &&
							sessaoPautaProcessoTrf.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.AJ)) {
						Events.instance().raiseAsynchronousEvent(SessaoPautaProcessoTrfManager.EVENT_SITUACAO_JULGAMENTO, sessaoPautaProcessoTrf.getIdSessaoPautaProcessoTrf());
					}

					
					ComponentUtil.getComponent(ProcessoJudicialService.class).sinalizarFluxo(
						sessaoPautaProcessoTrf.getProcessoTrf(), Variaveis.PJE_FLUXO_COLEGIADO_INICIAR_SESSAO, Boolean.TRUE, false, false);
			    }
		    }
		}
		
		if (sessao.getSecretarioIniciou() == null) {
			PessoaServidor ps = EntityUtil.find(PessoaServidor.class, Authenticator.getUsuarioLogado().getIdUsuario());
			sessao.setSecretarioIniciou(ps);
		}
		
		super.update();
		getEntityManager().flush();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Sessão iniciada com sucesso.");
		return null;
	}

	public boolean verificaCheckTodos() {
		PautaJulgamentoList lista = ComponentUtil.getComponent(PautaJulgamentoList.class);
		if(lista.getHabilitaCombo() != null){
			return lista.getHabilitaCombo();
		}
		for (ConsultaProcessoTrfSemFiltro processoTrf : lista.getResultList()) {
			if (verificaInclusao(processoTrf.getProcessoTrf())) {
				lista.setHabilitaCombo(true);
				return true;
			}
		}
		lista.setHabilitaCombo(false);
		return false;
	}

	public void exibeModalAguardando() {
		// esconde o modal da confirmação
		exibeMPConfirmacaoAdiaProcs = false;
	}

	/**
	 * Verifica se o OJC da sessão esta configurado para que somente o
	 * presidente inclua processo em na relação de julgamento a partir da aba
	 * "Aptos para inclusão em pauta"
	 * 
	 * @return
	 */
	public boolean verificaInclusao(ProcessoTrf processoTrf) {
		if(Authenticator.isPapelPermissaoSecretarioSessao()){
			return true;
		}
		if (isSomentePresidenteIncluiPautaNaRelacao()) {
			// verifica se a sessão tem presidente
			SessaoComposicaoOrdem scoPresidente = pegarOjPresidente();
			if (null != scoPresidente) {
				// teste para verificar se é presidente
				if (Authenticator.getOrgaoJulgadorAtual() != null
						&& Authenticator.getOrgaoJulgadorAtual().equals(scoPresidente.getOrgaoJulgador())) {
					return true;
				}
			}
		} else if (processoTrf != null && BooleanUtils.isTrue(processoTrf.getExigeRevisor())) {
			if (RelatorRevisorEnum.REV.equals(getInstance().getOrgaoJulgadorColegiado().getRelatorRevisor())
					&& ProcessoTrfHome.instance().isRevisor(processoTrf)) {
				return true;
			} else if (RelatorRevisorEnum.REL.equals(getInstance().getOrgaoJulgadorColegiado().getRelatorRevisor())
					&& processoTrf.getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual())) {
				return true;
			}
			// caso o ojc não nescessite ser presidente
		} else if(processoTrf != null){
			return processoTrf.getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual());
		}
		return false;
	}

	/**
	 * Verifica se o ojc esta configurado para somente o presidente incluir em pauta na relação de julgamento.
	 * 
	 * @return True se somente o presidente pode incluir pauta.
	 */
	private Boolean isSomentePresidenteIncluiPautaNaRelacao() {
		return getInstance().getOrgaoJulgadorColegiado().getPresidenteRelacao();
	}

	/**
	 * Pega o OrgaoJulgador presidente da sessão.
	 * 
	 * @return OJ
	 */
	public SessaoComposicaoOrdem pegarOjPresidente() {
		return ComponentUtil.getComponent(SessaoComposicaoOrdemManager.class).obterOrgaoJulgadorPresidente(getInstance());
	}

	/**
	 * Verifica se a data atual é posterior a data de prazo maximo de inclusão
	 * em pauta da sessão na instancia de sessaoHome
	 * 
	 * @return retorna "false" se a data atual for posterior a data max de
	 *         inclusão e "true" caso contrario
	 */
	public boolean permiteInclusaoPauta() {
		if (null != getInstance().getDataMaxIncProcPauta()) {
			Date dtAtual = new Date();
			if (tratarData(dtAtual).after(tratarData(getInstance().getDataMaxIncProcPauta()))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Verifica se exite processo Aguardando Julgamento ou Em Julgamento
	 * 
	 * @return Retorna true se exitir na lista de processo algum aguardando
	 *         julgamento
	 */
	public boolean verificaAguardandoJulgamento() {
		List<SessaoPautaProcessoTrf> spptList = recuperaSessaoPautaProcessosSessaoAtual();
		// verifica se exite processo na lista
		if (null != spptList) {
			for (SessaoPautaProcessoTrf sppt : spptList) {
				if (!sppt.isJulgamentoFinalizado() && (sppt.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.AJ)
						|| sppt.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.EJ))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Verifica se exite processo julgado, que não tenha sido feita, a
	 * proclamação de julgamento. Guarada a lista dos processos julgados sem
	 * proclamação em "spptProclamacaoPendenciaList".
	 * 
	 * @return Retorna true se exitir na lista de processo algum pendente de
	 *         proclamação.
	 */
	public boolean verificaPendenteProclamacaoJulgamento(List<SessaoPautaProcessoTrf> spptList) {
		boolean ret = false;
		// instancia lista de sessão pauta processos julgados com proclamação de
		// julgamento pendente
		setSpptProclamacaoPendenciaList(new ArrayList<SessaoPautaProcessoTrf>());		
		// verifica se exite processo na lista
		if (null != spptList) {
			for (SessaoPautaProcessoTrf sppt : spptList) {
				// verifica se o processo está julgado e não possue proclamação
				// de julgamento
				if (!sppt.isJulgamentoFinalizado() && (sppt.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG)
						&& Strings.isEmpty(sppt.getProclamacaoDecisao()))) {
					// adiciona o processo julgado sem proclamação a lista dos
					// pendentes
					getSpptProclamacaoPendenciaList().add(sppt);
				}
			}
		}
		if (getSpptProclamacaoPendenciaList().size() > 0) {
			ret = true;
		}
		return ret;
	}

	/**
	 * Verifica se exite processo julgado, que não tenha sido feita, a
	 * proclamação de julgamento. Guarada a lista dos processos julgados sem
	 * proclamação em "spptProclamacaoPendenciaList".
	 * 
	 * @return Retorna true se exitir na lista de processo algum pendente de
	 *         proclamação.
	 */
	public boolean verificaPendenteProclamacaoJulgamento() {
		List<SessaoPautaProcessoTrf> spptList = recuperaSessaoPautaProcessosSessaoAtual();
		return verificaPendenteProclamacaoJulgamento(spptList);
	}
	
	/**
	 * Metodo chamado quando se clica no botão encerra sessão no painel do
	 * secretário da sessão
	 */
	public void encerrasessao() {
		// verifica se exite processo aguardando julgamento ou em julgamento
		if (verificaAguardandoJulgamento()) {
			SessaoPautaProcessoTrfHome.instance().setInstance(null);
			setExibeMPConfirmacaoAdiaProcs(true);
			return;
		}

		// verifica se exite processo julgado com pendencia de proclamação de julgamento
		if (verificaPendenteProclamacaoJulgamento()) {
			SessaoPautaProcessoTrfHome.instance().setInstance(null);
			setExibeMPProcsPendentesProclamacao(true);
			return;
		}
		
		// esconde os modais
		setExibeMPConfirmacaoAdiaProcs(false);
		setExibeMPProcsPendentesProclamacao(false);
		realizaSessao();
		tratarProcessosComPedidoDeVista();
	}

	/**
	 * No fluxo configurado pelo parametro VARIAVEL_FLUXO_PEDIDO_VISTA, cada processo marcado como pedido de vista ao encerrar a sessao sera instanciado no fluxo.
	 * O processo sera criado para o OrgaoJulgador que pediu vista.  
	 */
	private void tratarProcessosComPedidoDeVista(){
		Fluxo fluxo = getFluxoPedidoVista();
		if(fluxo != null){
			List<SessaoPautaProcessoTrf> processos = recuperaSessaoPautaProcessosSessaoAtual();
			
			for (SessaoPautaProcessoTrf sppt : processos) {
				if(sppt.getAdiadoVista() != null && sppt.getAdiadoVista().equals(AdiadoVistaEnum.PV)){
					Integer idLocalizacao = sppt.getOrgaoJulgadorPedidoVista().getLocalizacao().getIdLocalizacao();

					LocalizacaoManager localizacaoManager = (LocalizacaoManager) Component.getInstance(LocalizacaoManager.class);
					List<Localizacao> localizacaoFisicaList = localizacaoManager.getArvoreDescendente(idLocalizacao, true);
					String idsLocalizacoesFisicas = LocalizacaoUtil.converteLocalizacoesList(localizacaoFisicaList);
					List<Integer> idsLocalizacoes = CollectionUtilsPje.convertStringToIntegerList(idsLocalizacoesFisicas);
					
					boolean existeFluxoVistaAtivo = ComponentUtil.getComponent(FluxoManager.class).existeProcessoNoFluxoEmExecucao(
							sppt.getProcessoTrf().getIdProcessoTrf(), idsLocalizacoes, fluxo.getFluxo());
					if(!existeFluxoVistaAtivo && isProcessoPedidoVista(sppt) ) {
						try {
							inicializaFluxoPedidoVista(sppt, fluxo.getCodFluxo());
						} catch (PJeBusinessException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	/**
	 * Inicializa o Fluxo de pedido de vista para o Orgão Julgador que registrou o pedido
	 * @param julgamento - representação do processo na sessão
	 * @param codFluxo - coódigo do fluxo BPM
	 * @throws PJeBusinessException
	 */
	private void inicializaFluxoPedidoVista(SessaoPautaProcessoTrf julgamento, String codFluxo) throws PJeBusinessException{
		ProcessoTrf processoTrf = julgamento.getProcessoTrf();
		OrgaoJulgador orgaoJulgador = julgamento.getOrgaoJulgadorPedidoVista();
		OrgaoJulgadorCargo orgaoJulgadorCargo = ComponentUtil.getComponent(OrgaoJulgadorCargoManager.class).getOrgaoJulgadorCargoEmExercicio(orgaoJulgador);
		OrgaoJulgadorColegiado orgaoJulgadorColegiado = processoTrf.getOrgaoJulgadorColegiado();
		ComponentUtil.getComponent(ProcessoJudicialService.class).incluirNovoFluxo(processoTrf, codFluxo, orgaoJulgador.getIdOrgaoJulgador(), 
				orgaoJulgadorCargo.getIdOrgaoJulgadorCargo(), orgaoJulgadorColegiado.getIdOrgaoJulgadorColegiado(), true);
	}

	/**
	 * Verifica se para o processo foi feito o pedido de vista
	 * 
	 * @param sppt SessaoPautaProcessoTrf
	 * @return true se tiver marcado pedido de vista e o OrgaoJulgador solicitante estiver selecionado
	 */
	private boolean isProcessoPedidoVista(SessaoPautaProcessoTrf sppt) {
		return sppt.getAdiadoVista() != null && sppt.getAdiadoVista().equals(AdiadoVistaEnum.PV) && 
				sppt.getOrgaoJulgadorPedidoVista() != null;
	}	
	
	/**
	 * Recupera o fluxo de pedido de vista cadastrado no VARIAVEL_FLUXO_PEDIDO_VISTA
	 * 
	 * @return Fluxo de pedido de vista ou null caso nao encontrar o parametro ou o fluxo
	 */
	private Fluxo getFluxoPedidoVista(){
		return getSessaoManager().getFluxoPedidoVista();
    }

	/**
	 * Realiza a sessão adiando os processos que estão aguardando julgamento, e
	 * em julgamento.
	 */
	public void realizaSessaoAdiando() {
		SessaoPautaProcessoTrfHome.instance().adiaProcsAguardandoJulgamento();
		setExibeMPConfirmacaoAdiaProcs(false);
		encerrasessao();
	}

	public String realizaSessao() {
		getInstance().setIniciar(!getInstance().getIniciar());
		getInstance().setDataRealizacaoSessao(new Date());
		super.update();

		//PJEII-4765 - JE - Inserido para verificar os processos julgados que tiveram o relator vencedor diferente do relator inicial 
		disparaEventoMudarTarefa();
		
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Sessão encerrada com sucesso.");
		return null;
	}

	//PJEII-4765 - JE - encaminhar processo proxima tarefa
	private void disparaEventoMudarTarefa()
	{
		if (getInstance() != null && getInstance().getSessaoPautaProcessoTrfList() != null)
		{
			for (SessaoPautaProcessoTrf sppt : getInstance().getSessaoPautaProcessoTrfList()) 
			{
				if (sppt.getDataExclusaoProcessoTrf() == null && !sppt.isJulgamentoFinalizado() && sppt.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG))
				{
					Events.instance().raiseEvent(Eventos.EVENTO_ENCERRA_SESSAO, sppt.getProcessoTrf() );
				}
			}
		}
	}

	public CadastroMensalEnum[] getCadastroMensalEnumValues() {
		return CadastroMensalEnum.values();
	}

	public List<SelectItem> repeteItems() {
		List<SelectItem> temp = new ArrayList<SelectItem>(5);
		temp.add(new SelectItem(null, "Selecione"));
		temp.add(new SelectItem("D", "Por Dia"));
		temp.add(new SelectItem("S", "Semanalmente"));
		temp.add(new SelectItem("M", "Mensalmente"));
		temp.add(new SelectItem("A", "Anualmente"));
		return temp;
	}

	public List<SelectItem> classificacaoItems() {
		List<SelectItem> temp = new ArrayList<SelectItem>(6);
		temp.add(new SelectItem(null, "Selecione"));
		temp.add(new SelectItem(1, "Na Primeira"));
		temp.add(new SelectItem(2, "Na Segunda"));
		temp.add(new SelectItem(3, "Na Terceira"));
		temp.add(new SelectItem(4, "Na Quarta"));
		temp.add(new SelectItem(5, "Na Quinta"));
		return temp;
	}

	private void antecipaDocumentosProcessosAdiadosMelhor(List<Integer> spptIds){
		SessaoPautaProcessoTrfDAO sessaoPautaProcessoTrfDAO = getComponent("sessaoPautaProcessoTrfDAO");

		getEntityManager().flush();
		getEntityManager().clear();
		
		SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager = getComponent("sessaoProcessoDocumentoManager");	
		
		for(Integer spptId: spptIds){
			SessaoPautaProcessoTrf sppt = sessaoPautaProcessoTrfDAO.find(spptId);
			
			if(sppt.getDataExclusaoProcessoTrf() == null && sppt.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.NJ)){
				List<SessaoProcessoDocumento> listaDocumentosSessao= sessaoProcessoDocumentoManager.listaDocumentosAptosAntecipacao(sppt);
				for(SessaoProcessoDocumento spd : listaDocumentosSessao){
					if(spd.getProcessoDocumento() != null && !spd.getProcessoDocumento().getTipoProcessoDocumento().equals(ParametroUtil.instance().getTipoProcessoDocumentoNotasOrais()) && 
							!spd.getProcessoDocumento().getTipoProcessoDocumento().equals(ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento())) {
						spd.setSessao(null);
						getEntityManager().persist(spd);
					}
				}
				getEntityManager().flush();
			}
			getEntityManager().clear();
		}
	}
	
	public void registraSessao() {
		if (ComponentUtil.getComponent(SessaoManager.class).isEventosDeliberacaoSessaoConfigurados()) {
			getInstance().setDataRegistroEvento(new Date());
			// limpa a lista para evitar que após o registro da sessão o usuário não
			// possa alterar os dados de um processo preciamente marcado
			SessaoPautaProcessoTrfHome.instance().limparListaSCO();
			// colocando os processos que foram retirados de julgamento e que são do
			// tipo Pauta de Julgamento na tabela evento
			List<SessaoPautaProcessoTrf> listaEmPauta = recuperaSessaoPautaProcessosSessaoAtual();

			SessaoPautaProcessoTrfDAO sessaoPautaProcessoTrfDAO = getComponent("sessaoPautaProcessoTrfDAO");

			List<Integer> spptIds = new ArrayList<Integer>(listaEmPauta.size());
			for (SessaoPautaProcessoTrf sppt : listaEmPauta) {
				spptIds.add(sppt.getIdSessaoPautaProcessoTrf());
			}
			listaEmPauta.clear();

			List<SessaoPautaProcessoTrf> listaEmPautaDaInstance = getInstance().getSessaoPautaProcessoTrfList();
			List<Integer> spptIdsDaInstance = new ArrayList<Integer>(listaEmPautaDaInstance.size());
			for (SessaoPautaProcessoTrf sppt : listaEmPautaDaInstance) {
				spptIdsDaInstance.add(sppt.getIdSessaoPautaProcessoTrf());
			}
			listaEmPautaDaInstance.clear();

			for (Integer spptId : spptIds) {

				getEntityManager().flush();
				getEntityManager().clear();

				SessaoPautaProcessoTrf sppt = sessaoPautaProcessoTrfDAO.find(spptId);

				// PJEII-5767 lançamento de eventos (Julgado, Retirado de Pauta, adiado e pedido
				// de vista )

				String codigoMovimento = null;

				try {
					if (sppt.isJulgamentoFinalizado()
							&& ComponentUtil.getSessaoPautaProcessoTrfManager().verificarRegistroMovimento(sppt)) {
						continue;
					}
					if (sppt.getSituacaoJulgamento() == TipoSituacaoPautaEnum.JG) {
						sppt.setOrgaoJulgadorRelator(sppt.getProcessoTrf().getOrgaoJulgador());
						removerAptidao(sppt.getProcessoTrf());
						switch (sppt.getJulgamentoEnum()) {
						case M:
							codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_MERITO;
							break;

						case P:
							codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_LIMINAR;
							break;

						case O:
							codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_QUESTAO_ORDEM;
							break;

						default:
							codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_MERITO;
							break;
						}
					}

					if (sppt.getAdiadoVista() == AdiadoVistaEnum.AD) {
						codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_ADIADO;
					}

					if (sppt.getAdiadoVista() == AdiadoVistaEnum.PV) {
						codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_PEDIDO_VISTA;
					}

					if (sppt.getRetiradaJulgamento()) {
						codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_RETIRADO_PAUTA;
					}

					if (codigoMovimento == null) {
						codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_RETIRADO_PAUTA;
					}

					if (codigoMovimento != null) {
						MovimentoAutomaticoService.preencherMovimento().deCodigo(codigoMovimento)
								.associarAoProcesso(sppt.getProcessoTrf().getProcesso())
								.associarAoUsuario(Authenticator.getUsuarioLogado()).lancarMovimento();
					}

					// desvincula da sessão os documentos dos processos NÃO JULGADOS
					Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_PROCESSO_JULGADO_COLEGIADO,
							sppt.getIdSessaoPautaProcessoTrf());
					ComponentUtil.getProcessoTrfManager().gravarSugestaoSessao(sppt.getProcessoTrf(), null);
					// antecipaDocumentosProcessosAdiados();
				} catch (PJeBusinessException e) {
					FacesMessages.instance().add(Severity.ERROR,
							"No foi possvel verificar o registro anterior de movimentos para todos os processos: "
									+ e.getLocalizedMessage());
				}
			}
			antecipaDocumentosProcessosAdiadosMelhor(spptIdsDaInstance);

			super.update();
			EntityUtil.getEntityManager().flush();
		} else {
			FacesMessages.instance().add(Severity.ERROR,
					"Os movimentos da sessão não estão devidamente configurados. Por favor, contacte o suporte do tribunal.");
		}
	}

	/**
	 * Método responsável por remover a aptidão de pauta para o processo
	 * 
	 * @param processo
	 *            o processo que se deseja remover a aptidão de pauta
	 */
	private void removerAptidao(ProcessoTrf processo) {
		ProcessoJudicialManager processoJudicialManager = ComponentUtil.getComponent(ProcessoJudicialManager.class);
		try {
			processoJudicialManager.removerAptidaoParaJulgamento(processo.getIdProcessoTrf());
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, String.format("Não foi possível remover a aptidão do processo %s", processo.toString()));
			e.printStackTrace();
		}
	}

	public void atualizaProcuradorSessao(int idSessao, boolean redirecionar) {
		carregaPresidente();
		if (getPresidente() == null) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Presidente da sessão não informado.");
			return;
		} else if (!existeProcurador()) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Procurador não informado.");
			return;
		} else if (existeProcurador() && suggestVazia()) {
			if (redirecionar) {
				redirecionar(idSessao);
				return;
			} else {
				return;
			}
		}

		Sessao sessaoParaAtualizar = pegarSessaoPorId(idSessao);
		
		PessoaProcurador procuradorSelecionadoSuggest = (PessoaProcurador) getPessoaProcuradorMPSuggest().getSelected();
		if(procuradorSelecionadoSuggest == null) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Procurador informado não encontrado.");
			limparCampos();
			return;
		} else {
			sessaoParaAtualizar.setProcurador(null);
			sessaoParaAtualizar.setPessoaProcurador(procuradorSelecionadoSuggest);
		}
		
		getEntityManager().merge(sessaoParaAtualizar);
		getEntityManager().flush();
		limparCampos();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro gravado com sucesso.");

		if (redirecionar) {
			if (!sessaoPosueMinimoPresentes(sessaoParaAtualizar)) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, "Número de participantes menor que o mínimo");
				return;
			}
			redirecionar(idSessao);			
		}
		limparCampos();
	}

	private void redirecionar(int idSessao) {
		Redirect redirect = Redirect.instance();
		redirect.setViewId("/Painel/SecretarioSessao/sessaoAbertaContinuacaoPopUp.seam?idSessao=" + idSessao);
		redirect.setConversationPropagationEnabled(false);
		redirect.execute();
	}

	private void limparCampos() {
		pessoaProcurador = null;
		getPessoaProcuradorMPSuggest().setSelected(null);
		getPessoaProcuradorMPSuggest().setTyped(null);
	}

	private Sessao pegarSessaoPorId(int idSessao) {
		EntityManager entityManager = EntityUtil.getEntityManager();
		return entityManager.find(Sessao.class, idSessao);
	}

	/**
	 * Verifica se a sessão contem o minimo de presentes para poder ser iniciada
	 * baseada na configuração do OJC da sessão
	 * 
	 * @return
	 */
	private boolean sessaoPosueMinimoPresentes(Sessao sessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select sco from SessaoComposicaoOrdem sco ");
		sb.append("where sco.sessao = :sessao ");
		sb.append("and (sco.magistradoTitularPresenteSessao = true or sco.magistradoSubstitutoSessao is not null) ");
		Query query = EntityUtil.getEntityManager().createQuery(sb.toString());
		query.setParameter("sessao", sessao);
		if (query.getResultList().size() < sessao.getOrgaoJulgadorColegiado().getMinimoParticipante()) {
			return false;
		}
		return true;
	}

	public String getProcessosSemJulgamento() {
		return getSessaoJulgamentoManager().getProcessosSemJulgamento(this.getSessaoIdSessao());
	}

	public String getProcessosJulgados() {
		return getSessaoJulgamentoManager().getProcessosJulgados(this.getSessaoIdSessao());
	}

	public String getVista() {
		return getSessaoJulgamentoManager().getVista(this.getSessaoIdSessao());
	}
	
	public String getAdiado() {
		return getSessaoJulgamentoManager().getAdiado(this.getSessaoIdSessao());
	}
	
	public String getRetiradoJulgamento() {
		return getSessaoJulgamentoManager().getRetiradoJulgamento(this.getSessaoIdSessao());
	}

	private Sessao procuraSessao(Calendar dataInicial, OrgaoJulgador orgaoJulgador) {
		Calendar dataFim = Calendar.getInstance();
		dataFim.setTime(dataInicial.getTime());
		dataFim.add(Calendar.DAY_OF_MONTH, 120);
		StringBuilder sql = new StringBuilder();
		sql.append("select a from Sessao a where ").append("a.dataSessao between :data and :dataFim and ")
		.append("a.dataAberturaSessao = null and ")
		.append("a.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = :ojc and ")
		.append("a in (select sco.sessao from SessaoComposicaoOrdem ")
		.append("sco where sco.orgaoJulgador = :oj)");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("data", dataInicial.getTime());
		query.setParameter("dataFim", dataFim.getTime());
		query.setParameter("ojc", getInstance().getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado());
		query.setParameter("oj", orgaoJulgador);

		return EntityUtil.getSingleResult(query);
	}

	public void finalizaSessao() {
		Integer dias = SessaoHome.instance().getInstance().getOrgaoJulgadorColegiado().getDiaRetiradaAdiada();
		if (dias != null) {
			Calendar dataInicial = Calendar.getInstance();
			// setando a data da sessão
			dataInicial.setTime(SessaoHome.instance().getInstance().getDataSessao());
			dataInicial.add(Calendar.DAY_OF_MONTH, dias);

			List<SessaoPautaProcessoTrf> listaEmPauta = recuperaSessaoPautaProcessosSessaoAtual();
			for (SessaoPautaProcessoTrf sppt : listaEmPauta) {
				if ((sppt.getSituacaoJulgamento() == TipoSituacaoPautaEnum.AJ || (sppt.getAdiadoVista() == AdiadoVistaEnum.AD && !sppt
						.getRetiradaJulgamento())) && sppt.getDataExclusaoProcessoTrf() == null) {
					Sessao sessao = procuraSessao(dataInicial, sppt.getProcessoTrf().getOrgaoJulgador());
					if (sessao != null) {
						SessaoPautaProcessoTrf s = new SessaoPautaProcessoTrf();
						s.setTipoInclusao(TipoInclusaoEnum.AD);
						s.setAdiadoVista(null);
						s.setProcessoTrf(sppt.getProcessoTrf());
						s.setSessao(sessao);
						s.setDataInclusaoProcessoTrf(new Date());
						s.setUsuarioInclusao(Authenticator.getUsuarioLogado());
						s.setOrgaoJulgadorUsuarioInclusao(sppt.getProcessoTrf().getOrgaoJulgador());

						getEntityManager().persist(s);
						getEntityManager().flush();
					}
				}
			}
		}
		// fechando a sessão corrente
		getInstance().setDataFechamentoSessao(new Date());
		super.update();
	}

	public void refreshAgenda() {
		Contexts.removeFromAllContexts("agendaSessao");
		getEntityManager().clear();
	}

	/**
	 * Pega uma sessão e retorna a da data dela formatada.
	 * 
	 * @param sessao
	 *            Sessão esperada.
	 * @return Retorna data formatada.
	 */
	public String dataSessao(Sessao sessao) {
		SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
		return fm.format(sessao.getDataSessao());
	}

	/**
	 * metodo que retorna a data da sessao.
	 * verifica primeiro se a mesma ja nao se encontra armazenada, melhorando a performance.
	 * @return
	 */
	public String dataSessao() {
		if(dataSessaoString == null) {
			dataSessaoString = dataSessao(getInstance());
		}
		return dataSessaoString;
	}

	@SuppressWarnings("unchecked")
	public Boolean usuarioRevisorOJProcesso(ProcessoTrf row) {
		StringBuilder sqlPes = new StringBuilder();
		sqlPes.append("select o.sessao from SessaoComposicaoOrdem o ");
		sqlPes.append("where o.orgaoJulgadorRevisor.idOrgaoJulgador = :oj ");
		EntityManager em = getEntityManager();
		Query query = em.createQuery(sqlPes.toString());
		query.setParameter("oj", row.getOrgaoJulgador().getIdOrgaoJulgador());
		List<Sessao> list = query.getResultList();
		if (list.size() > 0) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	/**
	 * Método que verifica se a pauta exige pauta verificada na classe judicial.
	 * 
	 * @return true se a classe judicial exigir pauta
	 */
	public boolean exigePauta() {
		return (processoTrf != null && processoTrf.getIdProcessoTrf() != 0) ? processoTrf.getClasseJudicial()
				.getPauta() : false;
	}

	public String cadastrarSessaoPautaProcessoTrf() {
		String msg = null;
		if (processoTrf != null) {
			if ((BooleanUtils.isTrue(processoTrf.getExigeRevisor()) || 
					SimNaoFacultativoEnum.S.equals(processoTrf.getClasseJudicial().getExigeRevisor())) && 
						processoTrf.getOrgaoJulgadorRevisor() == null) {
				
				msg = String.format(" - %s: %s", processoTrf.getProcesso().getNumeroProcesso(), 
						Messages.instance().get("sessaoPautaProcessoTrf.erro.processoSemRevisor"));				
				
			} else if ((BooleanUtils.isTrue(processoTrf.getExigeRevisor()) && processoTrf.getRevisado()) || 
							!BooleanUtils.isTrue(processoTrf.getExigeRevisor())) {
				try {
					SessaoPautaProcessoTrf sessaoPPT = getSessaoPautaProcessoTrfManager().pautarProcessoIncluidoEmMesa(instance, processoTrf);
					SessaoHome.instance().setProcessoTrf(null);
					setCarregaSuggest(false);
					String dataHoraConfig = "dd/MM/yyyy HH:mm:ss";
					SimpleDateFormat formatador = new SimpleDateFormat(dataHoraConfig);
					if(sessaoPPT.getSessao().getDataFechamentoPauta() != null ) {
						ComponentUtil.getSessaoJulgamentoService().registrarMovimentoFechamento(sessaoPPT.getProcessoTrf(), sessaoPPT.getSessao(), formatador);
						getEntityManager().flush();
					}
					ComponentUtil.getComponent(ProcessoJudicialService.class).sinalizarFluxo(
							sessaoPPT.getProcessoTrf(), Variaveis.PJE_FLUXO_COLEGIADO_INCLUIDO_MESA, Boolean.TRUE, false, false);
					
				} catch (Exception e) {
					e.printStackTrace();
					if (e.toString().contains("ConstraintViolationException")) {
						msg = String.format(" - %s: %s", processoTrf.getProcesso().getNumeroProcesso(), 
								Messages.instance().get("Processo já cadastrado."));
					} else {
						msg = String.format(" - %s: %s", processoTrf.getProcesso().getNumeroProcesso(), 
								Messages.instance().get("Erro ao cadastrar o processo."));
					}
				}
			} else {
				msg = String.format(" - %s: %s", processoTrf.getProcesso().getNumeroProcesso(), 
						Messages.instance().get("sessaoPautaProcessoTrf.erro.processoNaoRevisado"));
			}
		}
		
		return msg;
	}

	public String cadastrarSessaoPautaProcessoTrfList() {
		if (listTRF.size() > 0) {
			String msgCadastroPauta = null;
			List<String> listMsg = new ArrayList<String>();
			for (ProcessoTrf ptrf : listTRF) {
				processoTrf = ptrf;
				if(Objects.nonNull(processoTrf) &&
						ComponentUtil.getComponent(SessaoPautaProcessoTrfHome.class).verificaProcessoRelacao(processoTrf,instance,true)){
                    msgCadastroPauta = String.format("O processo %s já está incluso na Relação de Julgamento.",processoTrf.getProcesso().getNumeroProcesso());
				}else{
					msgCadastroPauta = cadastrarSessaoPautaProcessoTrf();
				}
				if (msgCadastroPauta != null) {
					listMsg.add(msgCadastroPauta);
				}
				processoTrf = new ProcessoTrf();
			}
			FacesMessages.instance().clear();
			if (listMsg.size() > 0){
				FacesMessages.instance().add(Severity.ERROR, "Os seguintes processos não foram incluídos:");
				for (String string : listMsg){
					FacesMessages.instance().add(Severity.ERROR, string);
				}
			}else{
				FacesMessages.instance().add(Severity.INFO, "Processo(s) incluído(s) em pauta com sucesso.");
			}
		}
		listTRF.clear();
		return null;
	}

	/**
	 * [PJEII-9886]
	 * Método chamado para adicionar na Relação de Julgamento os processos
	 * selecionados, exceto aqueles que não possuem documentos de sessão
	 * 
	 * @return String
	 */
	public String adicionarSomenteProcessosComDocumentosSessao() {
		// remove da lista os processos que não tem documentos de sessao
		if (getListProcessosSemDocumentosSessao().size() > 0) {
			for (ProcessoTrf ptrf : getListProcessosSemDocumentosSessao()) {
				listTRF.remove(ptrf);
			}
		}

		return cadastrarSessaoPautaProcessoTrfList();
	}

	/**
	 * [PJEII-9886]
	 * Verifica quais processos selecionados não possuem documentos de
	 * sessão elaborados. Esta informação é exibida para o usuário na página
	 * para que o mesmo possa decidir se adiciona ou não na Relação de Julgamento
	 * 
	 */
	public void verificarProcessosComDocumentosSessao() {
		setListProcessosSemDocumentosSessao(new ArrayList<ProcessoTrf>());
		if (listTRF.size() > 0) {
			// adiciona na lista todos os processos selecionados
			getListProcessosSemDocumentosSessao().addAll(listTRF);

			// verifica quais processos possuem documentos de sessao
			List<ProcessoTrf> list = ComponentUtil.getComponent(SessaoProcessoDocumentoManager.class).listProcessosComDocumentosSessao(listTRF);

			// remove da lista os processos com documentos de sessao elaborados
			for (ProcessoTrf ptrf : list) {
				getListProcessosSemDocumentosSessao().remove(ptrf);
			}
		}
	}

	public void setSessaoIdSessao(Integer id) {
		setId(id);
	}

	public Integer getSessaoIdSessao() {
		return (Integer) getId();
	}

	public static SessaoHome instance() {
		return ComponentUtil.getComponent(SessaoHome.class);
	}

	public void setSala(Sala sala) {
		this.sala = sala;
	}

	public Sala getSala() {
		return sala;
	}

	@Override
	public void newInstance() {
		dataInicial = null;
		dataFinal = null;
		desmarcarSelecionados();
		setsalaHorarioSet(new HashSet<SalaHorario>());
		setRepetir(Boolean.FALSE);
		setSala(null);
		super.newInstance();
	}

	public void marcarSala(SalaHorario obj) {
		if (getSalaHorarioSet().contains(obj)) {
			getSalaHorarioSet().remove(obj);
		} else {
			getSalaHorarioSet().add(obj);
		}
	}

	/**
	 * Método que alimenta a combo da composição da sessão.
	 * 
	 * @param obj
	 * @return
	 */
	public List<OrgaoJulgador> comboRevisor(OrgaoJulgador ojRow) {
		List<OrgaoJulgador> listaOjRet = new ArrayList<OrgaoJulgador>(listaOjOjcCompSelecionados.size());
		listaOjRet.addAll(listaOjOjcCompSelecionados);
		listaOjRet.remove(ojRow);
		return listaOjRet;
	}

	/**
	 * Método que Habilita a combo de Composição da Sessão.
	 */
	public void habilitaCombo() {
		setHabilitaCombo(false);
	}

	public void setRepetir(Boolean repetir) {
		if(repetir.equals(Boolean.TRUE)){
			instance.setApelido(null);
			instance.setObservacao(null);
		}
		this.repetir = repetir;
	}

	public Boolean getRepetir() {
		return repetir;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	/**
	 * Trata a data para comparações sem diferenca de hora
	 * 
	 * @param data
	 *            Data esperada para formatar.
	 * @return Retorna data com hora zerada.
	 */
	public Date tratarData(Date data) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(data);
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * Verifica se a data escolhida para o fechamento da pauta entá entre a data
	 * atual e a data da sessão.
	 */
	public boolean verificaPossibilidadeFechamentoPauta() {
		boolean ret = true;
		// verifica se a data de fechamento da pauta esta após a data da sessão
		// ou se esta antes da data atual.
		if (tratarData(dataFechamentoPauta).after(tratarData(dataInicial))) {
			ret = false;
		}
		return ret;
	}

	/**
	 * Pega uma SalaHorario e uma data inicial, e devolve a data da sala horário
	 * se ela corresponder ao dia da dataInicial se não devolve a próxima data
	 * baseada no dia da semana da dataInicial.<br/>
	 * <b>ex: dataInicial cai em uma segunda devolve dataInicial. dataInicial
	 * não cai em uma segunda retorna a proxima segunda baseada na
	 * dataInicial.</b>
	 * 
	 * @return data Data retornada da SalaHorario ou da procima sala.
	 * 
	 * @param sh
	 *            SalaHorario esperada para verificação de qual será o dia da
	 *            semana. ex: Segunda, Terça, etc...
	 * 
	 * @param dataInicial
	 *            Data inicial esperada.
	 */
	public Date getProximaSessao(SalaHorario sh, Date dataInicial) {
		Calendar cDataInicial = Calendar.getInstance();
		cDataInicial.setTime(dataInicial);
		while (cDataInicial.get(Calendar.DAY_OF_WEEK) - 1 != DiaSemanaManager.diaSemanaInt(sh.getDiaSemana())) {
			cDataInicial.add(Calendar.DAY_OF_MONTH, 1);
		}
		return cDataInicial.getTime();
	}

	/**
	 * A partir de uma data base especifica retorna outra data baseada nos
	 * critérios de classificação mensal e de dia da semana. ex:
	 * 
	 * @param dataBase
	 *            Data base para o inicio da ação. <br/>
	 * <br/>
	 * @param sh
	 *            Sala horario esperada para verificar o dia de semana esperado. <br/>
	 *            <b> ex: 0 = Domingo, 1 = Segunda , etc... </b><br/>
	 * <br/>
	 * @param ordemClassificacao
	 *            Ordem de classificação que a data retornada deve possuir no
	 *            mês em questão.<br/>
	 *            <b> ex: 1 = Na Primeira, 2 = Na Segunda, etc... <br/>
	 *            </b><br/>
	 * <br/>
	 * @return Data encontrada retornada, retorna nulo caso não encontre uma
	 *         data valida.<br/>
	 *         <b> ex: Se a ordem de classificação foi 5, o dia da semana
	 *         selecionado foi uma quinta-feira e o mês não possue a 5ª
	 *         quinta-feira, o retorne é null. </b>
	 */
	public Date retornaDataComCriterios(Date dataBase, SalaHorario sh, int ordemClassificacao) {
		Date ret = null;
		// pega o primeiro dia do mês baseado numa data.
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dataBase);
		calendar.set(Calendar.DAY_OF_MONTH, 1);

		// verifica se a ordem de classificação é na primeira semana
		if (ordemClassificacao == 1) {
			ret = getProximaSessao(sh, calendar.getTime());
		}
		// verrifica se a ordem de classificação está entre a segunda e quinta
		// semana
		else {
			ret = getProximaSessao(sh, calendar.getTime());
			ret = adicionaDias(ret, 7 * (ordemClassificacao - 1));

			// verifica se a data virou o mês, se o mês virou devolve uma data
			// nula
			// represnetando que aquela data não exite naquele mês para os
			// critérios selecionados.
			Calendar cRet = Calendar.getInstance();
			cRet.setTime(ret);
			if (!verificaIgualdadeMesAno(calendar, cRet)) {
				ret = null;
			}
		}
		return ret;
	}

	/**
	 * Retorna Ultimo dia do mes referente ao dia da semana baseada numa
	 * dataBase e numa SalaHorario
	 * 
	 * @param dataBase
	 *            Data esperada para cauculo.
	 * @param sh
	 *            SalaHorario esperado.
	 * @return Ultimo dia do mês igual ao dia da semana pasado na SalaHorario.
	 */
	public Date retornaUltimoDiaDaSemanaMes(Date dataBase, SalaHorario sh) {
		// pega o primeiro dia do mês baseado numa data.
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dataBase);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

		while (calendar.get(Calendar.DAY_OF_WEEK) - 1 != DiaSemanaManager.diaSemanaInt(sh.getDiaSemana())) {
			calendar.add(Calendar.DAY_OF_MONTH, -1);
		}
		return calendar.getTime();
	}

	/**
	 * Pega a data do primeiro dia do proximo mês baseado em uma data qualquer.
	 * 
	 * @param dataBase
	 *            Data esperada para fazer o cauculo.
	 * @return Retorna o primeiro dia do proximo mês.
	 */
	public Date pegarPrimeiroDiaProxMes(Date dataBase) {
		Calendar ctemp = Calendar.getInstance();
		ctemp.setTime(dataBase);
		ctemp.set(Calendar.DAY_OF_MONTH, ctemp.getActualMaximum(Calendar.DAY_OF_MONTH));
		return adicionaDias(ctemp.getTime(), 1);
	}

	public Date pegarMesmoDiaMesProxAno(Date dataBase) {
		Calendar cDtInicio = Calendar.getInstance();
		cDtInicio.setTime(dataBase);
		Calendar ctemp = Calendar.getInstance();
		ctemp.setTime(dataBase);
		ctemp.set(Calendar.YEAR, ctemp.get(Calendar.YEAR) + 1);
		if (verificaIgualdadeMes(ctemp, cDtInicio)) {
			return ctemp.getTime();
		} else {
			return null;
		}
	}

	public Date pegarPrimeiroDiaMes(Date dataBase) {
		Calendar ctemp = Calendar.getInstance();
		ctemp.setTime(dataBase);
		ctemp.set(Calendar.DAY_OF_MONTH, ctemp.getActualMinimum(Calendar.DAY_OF_MONTH));
		return ctemp.getTime();
	}

	/**
	 * Pega a data do mesmo dia da data base no proximo mês.
	 * 
	 * @param dataBase
	 *            Data esperada para fazer o cauculo.
	 * @return Retorna o mesmo dia do proximo mês.
	 */
	public Date pegarMesmoDiaProxMes(Date dataBase) {
		Calendar ctemp = Calendar.getInstance();
		ctemp.setTime(dataBase);
		ctemp.set(Calendar.MONTH, ctemp.get(Calendar.MONTH) + 1);
		return ctemp.getTime();
	}

	public Date pegarMesmoDiaProxAno(Date dataBase) {
		Calendar ctemp = Calendar.getInstance();
		ctemp.setTime(dataBase);
		ctemp.set(Calendar.YEAR, ctemp.get(Calendar.YEAR) + 1);
		return ctemp.getTime();
	}

	/**
	 * Verifica se duas datas possuem mês e ano iguais.
	 * 
	 * @param c1
	 *            Primeira Data.
	 * @param c2
	 *            Segunda Data.
	 * @return Retorna true se as duas datas tiverem mês e ano iguais.
	 */
	private boolean verificaIgualdadeMesAno(Calendar c1, Calendar c2) {
		boolean ret = true;
		if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH) || c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {
			ret = false;
		}
		return ret;
	}

	private boolean verificaIgualdadeMes(Calendar c1, Calendar c2) {
		boolean ret = true;
		if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH)) {
			ret = false;
		}
		return ret;
	}

	@SuppressWarnings({ "deprecation"})
	@Override
	public String persist() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd"); 
		if (dataFinal != null && dateFormat.format(dataFinal).compareTo(dateFormat.format(dataInicial)) < 0) {
			FacesMessages.instance().add(Severity.ERROR, "A data final deve ser maior ou igual a data inicial.");
			return null;
		}
		if (Authenticator.getOrgaoJulgadorColegiadoAtual() == null) {
			FacesMessages.instance().add(Severity.ERROR, "Usuário precisa possuir um Órgão Julgador Colegiado.");
			return null;
		}
		
		/*
		 * variáveis para guardar o valor informado pelo usuário em tela.
		 * os valores serão retornados aos atributos em casos em que a
		 * persitência gere algum erro com mensagem para o usuário,
		 */
		Date dataIniInformadaEmTela = dataInicial;
		Date dataFinInformadaEmTelaOriginal = dataFinal;

		if (!repetir) {
			setDataFinal(null);
		}

		if (dataFinal == null) {
			dataFinal = new Date(dataInicial.getTime());
		}
		dataFinal = adicionaDias(dataFinal, 1);

		int qtdSessoesCadastradas = 0;

		// verifica se não existe repetição no tipo de cadastro ou se existe
		// repetição e se a mesma é diária. (caso atendido abaixo:)
		
		if(getInstance().getContinua() != null && getInstance().getContinua()){
			DiaSemana todosOsDias = EntityUtil.getEntityManager().getReference(DiaSemana.class, 8);
			List<SalaHorario> horariosSala = sala.getSalaHorarioList();
			SalaHorario horarioTodosDiasSala = null;
			for(SalaHorario sala : horariosSala){
				if(sala.getDiaSemana().equals(todosOsDias)){
					horarioTodosDiasSala = sala;
					break;
				}
			}
			if(horarioTodosDiasSala == null){
				FacesMessages.instance().clear();
				FacesMessages.instance().add(StatusMessage.Severity.INFO,
						"A sala "+ sala.toString() + " não está configurada para ser utilizada em todos os dias da semana.");
			}

			else if (verificarDisponibilidade(horarioTodosDiasSala)) {
				persistirSessao(horarioTodosDiasSala, dataInicial,getInstance().getDataFimSessao(),true);
				qtdSessoesCadastradas++;
			} else {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(StatusMessage.Severity.INFO,
						"A sala já esta reservada para o dia solicitado.");
			}
		}
		
		else if (!repetir || (repetir && "D".equals(flagRepete))) {
			// Se estiver selecionado "A cada dia util"
			// seta a dataInicial como o primeiro dia util apos a dataInicial
			// informada
			if (!dias) {
				while (!verificaDiaUtil(dataInicial)) {
					dataInicial = adicionaDias(dataInicial, 1);
				}
			}
			while (dataFinal.after(dataInicial)) {
				// percore a lista de horário selecionados pelo usuário
				SalaHorario ojcsh = null;
				Iterator<SalaHorario> salaHorarioIterator = salaHorarioSet.iterator();
				while(salaHorarioIterator.hasNext()) {
					ojcsh = salaHorarioIterator.next();
					// verifica se o dia corrente, "dataInicial", é o mesmo dia
					// de semana da opcão de horário selecionada pelo usuário
					if (DiaSemanaManager.diaSemanaInt(ojcsh.getDiaSemana()) == dataInicial.getDay() ||
							DiaSemanaManager.diaSemanaInt(ojcsh.getDiaSemana()) == 7) {
						// verifica se a sala ignora feriado
						if (sala.getIgnoraFeriado() || !isFeriado(dataInicial)) {
							// verifica disponibilidade do horario na data
							// selecionada
							if (verificarDisponibilidade(ojcsh)) {
								persistirSessao(ojcsh, dataInicial);
								qtdSessoesCadastradas++;
							} else {
								FacesMessages.instance().clear();
								FacesMessages.instance().add(StatusMessage.Severity.INFO,
										"A sala já esta reservada para o dia solicitado.");
							}
						} else {
							FacesMessages.instance().clear();
							FacesMessages.instance().add(StatusMessage.Severity.INFO, "O dia selecionado é feriado.");
						}
					} else {
						FacesMessages.instance().clear();
						FacesMessages.instance().add(StatusMessage.Severity.INFO,
								"O dia selecionado não é igual à data inicial.");
					}
				}
				if (dias) {
					dataInicial = adicionaDias(dataInicial, numeroDias);
				} else {
					do {
						dataInicial = adicionaDias(dataInicial, 1);
					} while (!verificaDiaUtil(dataInicial));
				}
			}
		}
		// verifica se a repetição existente é semanal
		else if (repetir && "S".equals(flagRepete)) {
			// vare a lista de horários selecionados pelo usuário
			Date inicioPeriodo = dataInicial;
			SalaHorario ojcsh;
			Iterator<SalaHorario> salaHorarioIterator = salaHorarioSet.iterator(); 
			while (salaHorarioIterator.hasNext()) {
				ojcsh = salaHorarioIterator.next();
				// flag para identeificar a primeira data do intervalo dos 7
				// dias.
				boolean isFrist7Dias = true;
				// percorre o prazo de dias selecionados pelo usuário
				while (dataFinal.after(dataInicial)) {
					// verifica se é a primeira vez dos dias e pega o primeiro
					// dia que deve-se iniciar o loop
					if (isFrist7Dias) {
						dataInicial = getProximaSessao(ojcsh, dataInicial);
						isFrist7Dias = false;
					}

					// verifica se o dia corrente, "dataInicial", é o mesmo dia
					// de semana da opcão de horário selecionada pelo usuário
					if (DiaSemanaManager.diaSemanaInt(ojcsh.getDiaSemana()) == dataInicial.getDay()) {
						// verifica se a sala ignora feriado
						if (sala.getIgnoraFeriado() || !isFeriado(dataInicial)) {
							// verifica disponibilidade do horario na data
							// selecionada
							if (verificarDisponibilidade(ojcsh)) {
								persistirSessao(ojcsh, dataInicial);
								qtdSessoesCadastradas++;
							}
						}
					}
					// pula para a proxima data a ser selecionada para tentar
					// efetuar o cadastro
					dataInicial = adicionaDias(dataInicial, 7 * numeroSemanas);
				}
				// reinicia o periodo para comerçar a cadastrar o proximo
				// horário
				dataInicial = inicioPeriodo;
			}
		}
		// verifica se a repetição selecionada foi mensal e por ordem de
		// classificação
		else if (repetir && "M".equals(flagRepete) && (ordemClassificacao.equals(CadastroMensalEnum.O))) {
			// vare a lista de horários selecionados pelo usuário
			SalaHorario ojcsh;
			Date dataTemp = dataInicial;
			Iterator<SalaHorario> salaHorarioIterator = salaHorarioSet.iterator();
			while (salaHorarioIterator.hasNext()) {
				ojcsh = salaHorarioIterator.next();
				Date oldDataTemp = dataTemp;
				while (dataFinal.after(dataTemp)) {
					// guarda o valor de data temp
					oldDataTemp = dataTemp;
					dataTemp = retornaDataComCriterios(dataTemp, ojcsh, flagClassificacao);
					// se o novo valor de data temp é nulo ou seja ,a data não
					// foi encontrada, procura no procimo mês a possivel data
					while (null == dataTemp) {
						oldDataTemp = pegarPrimeiroDiaProxMes(oldDataTemp);
						dataTemp = retornaDataComCriterios(oldDataTemp, ojcsh, flagClassificacao);
					}
					// verifica se a data encontrada está dentro do prazo
					// informado
					if (DateUtil.isBetweenDates(dataTemp, dataInicial, dataFinal)) {
						// verifica se encontrou data com a classificação
						// informada
						if (null != dataTemp) {
							// verifica se o dia corrente, "dataInicial", é o
							// mesmo dia de semana da opcão de horário
							// selecionada pelo usuário
							if (DiaSemanaManager.diaSemanaInt(ojcsh.getDiaSemana()) == dataTemp.getDay()) {
								// verifica se a sala ignora feriado
								if (sala.getIgnoraFeriado() || !isFeriado(dataTemp)) {
									// verifica disponibilidade do horario na
									// data selecionada
									if (verificarDisponibilidade(ojcsh)) {
										persistirSessao(ojcsh, dataTemp);
										qtdSessoesCadastradas++;
									}
								}
							}
						}
					}
					// salta a quantidade de meses informada no prazo a cada mês
					for (int i = 0; i < numeroMeses; i++) {
						// pega primeiro dia do proximo mês
						dataTemp = pegarPrimeiroDiaProxMes(dataTemp);
					}
				}
				// reinicia o periodo para comerçar a cadastrar o proximo
				// horário
				dataTemp = dataInicial;
			}
		} else if (repetir && "M".equals(flagRepete) && (ordemClassificacao.equals(CadastroMensalEnum.U))) {
			// vare a lista de horários selecionados pelo usuário
			SalaHorario ojcsh;
			Date dataTemp = dataInicial;
			Iterator<SalaHorario> salaHorarioIterator = salaHorarioSet.iterator();
			while(salaHorarioIterator.hasNext()) {
				ojcsh = salaHorarioIterator.next();
				while (dataFinal.after(dataTemp)) {
					// metodo que pega a dataTemp e retorna o ultimo dia do mes
					// que tem o mesmo dia da semana baseada na data temp e
					// ojcsh
					dataTemp = retornaUltimoDiaDaSemanaMes(dataTemp, ojcsh);
					// verifica se a data encontrada está dentro do prazo
					// informado
					if (DateUtil.isBetweenDates(dataTemp, dataInicial, dataFinal)) {
						// verifica se o dia corrente, "dataTemp", é o mesmo dia
						// de semana da opcão de horário selecionada pelo
						// usuário
						if (DiaSemanaManager.diaSemanaInt(ojcsh.getDiaSemana()) == dataTemp.getDay()) {
							// verifica se a sala ignora feriado
							if (sala.getIgnoraFeriado() || !isFeriado(dataTemp)) {
								// verifica disponibilidade do horario na data
								// selecionada
								if (verificarDisponibilidade(ojcsh)) {
									persistirSessao(ojcsh, dataTemp);
									qtdSessoesCadastradas++;
								}
							}
						}
					}
					// salta a quantidade de meses informada no prazo a cada mês
					for (int i = 0; i < numeroMeses; i++) {
						// pega primeiro dia do proximo mês
						dataTemp = pegarPrimeiroDiaProxMes(dataTemp);
					}
				}
				// seta a data temp com a data inicial apra reiniciar o periodo
				// de cadastro para o proximo horário selecionado
				dataTemp = dataInicial;
			}
		} else if (repetir && "M".equals(flagRepete) && (ordemClassificacao.equals(CadastroMensalEnum.R))) {
			// varre a lista dos dias selecionados pelo usuário
			for (int dia : diasSelecionados) {
				Date dataTemp = dataInicial;
				// seta o dia da tada como o dia corrente selecionado da lista
				dataTemp.setDate(dia);
				// vare a lista de horários selecionados pelo usuário
				SalaHorario ojcsh;
				Iterator<SalaHorario> salaHorarioIterator = salaHorarioSet.iterator();
				while(salaHorarioIterator.hasNext()) {
					ojcsh = salaHorarioIterator.next();
					while (dataFinal.after(dataTemp)) {
						// verifica se a data encontrada está dentro do prazo
						// informado
						if (DateUtil.isBetweenDates(dataTemp, dataInicial, dataFinal)) {
							// verifica se o dia corrente, "dataTemp", é o mesmo
							// dia de semana da opcão de horário selecionada
							// pelo usuário
							if (DiaSemanaManager.diaSemanaInt(ojcsh.getDiaSemana()) == dataTemp.getDay()) {
								// verifica se a sala ignora feriado
								if (sala.getIgnoraFeriado() || !isFeriado(dataTemp)) {
									// verifica disponibilidade do horario na
									// data selecionada
									if (verificarDisponibilidade(ojcsh)) {
										persistirSessao(ojcsh, dataTemp);
										qtdSessoesCadastradas++;
									}
								}
							}
						}
						// salta a quantidade de meses informada no prazo a cada
						// mês
						for (int i = 0; i < numeroMeses; i++) {
							// pega primeiro dia do proximo mês
							dataTemp = pegarMesmoDiaProxMes(dataTemp);
						}
					}
					// reinicia o periodo para o proximo dia selecionado
					dataTemp = dataInicial;
				}

			}
			// limpa a lista de dias selecionados
			diasSelecionados = new TreeSet<Integer>();
		} else if (repetir && "A".equals(flagRepete) && (ordemClassificacao.equals(CadastroMensalEnum.R))) {
			Date dataTemp = new Date();
			dataTemp.setYear(dataInicial.getYear());
			dataTemp.setMonth(mes);
			dataTemp.setDate(diaAnual);
			// vare a lista de horários selecionados pelo usuário
			SalaHorario ojcsh;
			Iterator<SalaHorario> salaHorarioIterator = salaHorarioSet.iterator(); 
			while(salaHorarioIterator.hasNext()) {
				ojcsh = salaHorarioIterator.next();
				while (dataFinal.after(dataTemp)) {
					// verifica se a data encontrada está dentro do prazo
					// informado
					if (DateUtil.isBetweenDates(dataTemp, dataInicial, dataFinal)) {
						// verifica se o dia corrente, "dataTemp", é o mesmo dia
						// de semana da opcão de horário selecionada pelo
						// usuário
						if (DiaSemanaManager.diaSemanaInt(ojcsh.getDiaSemana()) == dataTemp.getDay()) {
							// verifica se a sala ignora feriado
							if (sala.getIgnoraFeriado() || !isFeriado(dataTemp)) {
								// verifica disponibilidade do horario na data
								// selecionada
								if (verificarDisponibilidade(ojcsh)) {
									persistirSessao(ojcsh, dataTemp);
									qtdSessoesCadastradas++;
								}
							}
						}
					}
					// salta a quantidade de meses informada no prazo a cada mês
					for (int i = 0; i < numeroAnos; i++) {
						// pega primeiro dia do proximo mês
						dataTemp = pegarMesmoDiaProxAno(dataTemp);
					}
				}
				dataTemp.setYear(dataInicial.getYear());
				dataTemp.setMonth(mes);
				dataTemp.setDate(diaAnual);
			}
		}
		// verifica se a repetição selecionada foi anual e por ordem de
		// classificação
		else if (repetir && "A".equals(flagRepete) && (ordemClassificacao.equals(CadastroMensalEnum.O))) {
			// vare a lista de horários selecionados pelo usuário
			SalaHorario ojcsh;
			Iterator<SalaHorario> salaHorarioIterator = salaHorarioSet.iterator();
			while(salaHorarioIterator.hasNext()) {
				ojcsh = salaHorarioIterator.next();
				Date dataTemp = new Date();
				dataTemp.setYear(dataInicial.getYear());
				dataTemp.setMonth(mes);
				dataTemp.setDate(diaAnual);
				dataTemp = pegarPrimeiroDiaMes(dataTemp);
				Date oldDataTemp = dataTemp;
				while (dataFinal.after(dataTemp)) {
					// guarda o valor de data temp
					oldDataTemp = dataTemp;
					dataTemp = retornaDataComCriterios(dataTemp, ojcsh, flagClassificacao);
					// se o novo valor de data temp é nulo ou seja ,a data não
					// foi encontrada, procura no procimo mês a possivel data
					while (null == dataTemp) {
						oldDataTemp = pegarMesmoDiaMesProxAno(oldDataTemp);
						dataTemp = retornaDataComCriterios(oldDataTemp, ojcsh, flagClassificacao);
					}
					// verifica se a data encontrada está dentro do prazo
					// informado
					if (DateUtil.isBetweenDates(dataTemp, dataInicial, dataFinal)) {
						// verifica se encontrou data com a classificação
						// informada
						if (null != dataTemp) {
							// verifica se o dia corrente, "dataInicial", é o
							// mesmo dia de semana da opcão de horário
							// selecionada pelo usuário
							if (DiaSemanaManager.diaSemanaInt(ojcsh.getDiaSemana()) == dataTemp.getDay()) {
								// verifica se a sala ignora feriado
								if (sala.getIgnoraFeriado() || !isFeriado(dataTemp)) {
									// verifica disponibilidade do horario na
									// data selecionada
									if (verificarDisponibilidade(ojcsh)) {
										persistirSessao(ojcsh, dataTemp);
										qtdSessoesCadastradas++;
									}
								}
							}
						}
					}
					// salta a quantidade de meses informada no prazo a cada mês
					for (int i = 0; i < numeroAnos; i++) {
						dataTemp = pegarMesmoDiaMesProxAno(dataTemp);
					}
				}
			}
		} else if (repetir && "A".equals(flagRepete) && (ordemClassificacao.equals(CadastroMensalEnum.U))) {
			// vare a lista de horários selecionados pelo usuário
			SalaHorario ojcsh;
			Iterator<SalaHorario> salaHorarioIterator = salaHorarioSet.iterator();
			while(salaHorarioIterator.hasNext()) {
				ojcsh = salaHorarioIterator.next();
				Date dataTemp = new Date();
				dataTemp.setYear(dataInicial.getYear());
				dataTemp.setMonth(mes);
				dataTemp.setDate(diaAnual);
				dataTemp = retornaUltimoDiaDaSemanaMes(dataTemp, ojcsh);
				while (dataFinal.after(dataTemp)) {
					dataTemp = retornaUltimoDiaDaSemanaMes(dataTemp, ojcsh);
					// verifica se a data encontrada está dentro do prazo
					// informado
					if (DateUtil.isBetweenDates(dataTemp, dataInicial, dataFinal)) {
						// verifica se o dia corrente, "dataTemp", é o mesmo dia
						// de semana da opcão de horário selecionada pelo
						// usuário
						if (DiaSemanaManager.diaSemanaInt(ojcsh.getDiaSemana()) == dataTemp.getDay()) {
							// verifica se a sala ignora feriado
							if (sala.getIgnoraFeriado() || !isFeriado(dataTemp)) {
								// verifica disponibilidade do horario na data
								// selecionada
								if (verificarDisponibilidade(ojcsh)) {
									persistirSessao(ojcsh, dataTemp);
									qtdSessoesCadastradas++;
								}
							}
						}
					}
					// salta a quantidade de meses informada no prazo a cada mês
					for (int i = 0; i < numeroAnos; i++) {
						dataTemp = pegarMesmoDiaMesProxAno(dataTemp);
					}
				}
			}
		}

		if (qtdSessoesCadastradas > 0) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro cadastrado com sucesso.");
		}
		// verifica se o cadastro foi feito no modo de repetição para tratamento
		// das menssagens
		if (this.repetir) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, exibeMsgQtdSecCadastradas(qtdSessoesCadastradas));
		}
		
		/*  
		 *  se nenhuma sessão foi cadastrada mantém os valores em tela para
		 *  que o usuário possa 'lembrar' (junto com a mensagem de erro) o
		 *  que ele escolheu.
		 */
		if (qtdSessoesCadastradas > 0) {
			setDefalutValues();
			
		} else {
			setDataInicial(dataIniInformadaEmTela);
			setDataFinal(dataFinInformadaEmTelaOriginal);
			
		}
		return "persisted";
	}

	public boolean isFeriado(Date date) {
		Calendar data = Calendar.getInstance();
		data.setTime(date);

		StringBuffer ejbql = new StringBuffer();
		ejbql.append("select o from CalendarioEvento o ");
		ejbql.append("where o.dtDia = :dia ");
		ejbql.append("and o.dtMes = :mes ");
		ejbql.append("and (o.dtAno is null or o.dtAno = :ano) ");
		ejbql.append("and o.ativo = true ");
		
		Jurisdicao jurisdicao = null;
		if(Authenticator.getOrgaoJulgadorAtual() != null){
			jurisdicao = Authenticator.getOrgaoJulgadorAtual().getJurisdicao();			
		}else if(Authenticator.getOrgaoJulgadorColegiadoAtual() != null){
			jurisdicao = Authenticator.getOrgaoJulgadorColegiadoAtual().getJurisdicao();
		}
		
		if(jurisdicao != null){
			ejbql.append("and (o.inAbrangencia in ('N','O') or o.estado = :estado) ");
		}
		Query q = getEntityManager().createQuery(ejbql.toString());
		q.setParameter("dia", data.get(Calendar.DATE));
		q.setParameter("mes", data.get(Calendar.MONTH) + 1);
		q.setParameter("ano", data.get(Calendar.YEAR));
		
		if(jurisdicao != null){
			q.setParameter("estado", jurisdicao.getEstado());
		}
		CalendarioEvento evento = EntityUtil.getSingleResult(q);
		
		boolean retorno = evento != null;
		if(evento != null && evento.getInAbrangencia().equals(AbrangenciaEnum.O)){
			retorno = Authenticator.getIdOrgaoJulgadorAtual() != null && Authenticator.getIdOrgaoJulgadorAtual().equals(evento.getOrgaoJulgador().getIdOrgaoJulgador());
		}
		return retorno;
	}

	public Date adicionaDias(Date date, int numDias) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, numDias);
		return calendar.getTime();
	}

	/**
	 * Persiste a sessão propriamente dita, seta a composição altomatica, e
	 * incrementa o numero de sessões cadastradas.
	 * 
	 * @param ojcsh
	 *            Sessão que deverá ser cadastrada.
	 * @param dataInicial
	 *            Data que deverá ser cadastrada a sessão.
	 * @return Retorna o numero de sessões já cadastradas.
	 */
	private void persistirSessao(SalaHorario ojcsh, Date dataInicial){
		persistirSessao(ojcsh, dataInicial,null,false);
	}
	
	private void persistirSessao(SalaHorario ojcsh, Date dataInicial,Date dataFinal,Boolean continua) {
		Sessao sessao = new Sessao();
		OrgaoJulgadorColegiado ojc = Authenticator.getOrgaoJulgadorColegiadoAtual();
		sessao.setOrgaoJulgadorColegiado(ojc);
		sessao.setUsuarioExclusao(null);
		sessao.setOrgaoJulgadorColegiadoSalaHorario(ojcsh);
		sessao.setTipoSessao(getInstance().getTipoSessao());
		sessao.setApelido(getInstance().getApelido());
		sessao.setObservacao(getInstance().getObservacao());
		sessao.setDataSessao(dataInicial);
		sessao.setHorarioInicio(getInstance().getHorarioInicio());
		sessao.setUsarBlocos(getInstance().getUsarBlocos());
		if(dataFinal != null){
			sessao.setDataFimSessao(dataFinal);
		}

		// Verifica se o ojc não é nulo e se tem fechamento automatico.
		if (ojc != null && ojc.getFechamentoAutomatico()) {
			Date dataFechamento = adicionaDias(dataInicial, -ojc.getPrazoDisponibilizaJulgamento());
			sessao.setDataFechamentoPauta(dataFechamento);
			// seta a data do prazo maximo de inclusão em pauta
			sessao.setDataMaxIncProcPauta(adicionaDias(sessao.getDataFechamentoPauta(), -1));
			// Demais casos
		} else {
			sessao.setDataFechamentoPauta(null);
			sessao.setDataMaxIncProcPauta(null);
		}

		sessao.setContinua(continua);
		sessao.setDataExclusao(null);
		sessao.setDataAberturaSessao(null);
		sessao.setDataFechamentoSessao(null);
		sessao.setUsuarioInclusao(Authenticator.getUsuarioLogado());
		sessao.setIniciar(Boolean.FALSE);
		sessao.getDocumentoSessao().setSessao(sessao);

		setInstance(sessao);
		getEntityManager().persist(getInstance());
		EntityUtil.flush();
		if (Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
			//inserirComposicaoSessaoAutomatico(sessao);
		}
	}

	/**
	 * Verifica se o dia em questão é sabado ou domingo.
	 * 
	 * @param data
	 *            Data esperada para verificar.
	 * @return Em caso de sabado e domingo retorna "false" ou seja não é dia
	 *         util, demais casos retorna "true" ou seja, é dia util.
	 */
	@SuppressWarnings("deprecation")
	public boolean verificaDiaUtil(Date data) {
		if (data.getDay() == 6 || data.getDay() == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Seta os valores defaut usados no dastro da sessão.
	 */
	public void setDefalutValues() {
		this.numeroDias = 1;
		this.repetir = false;
		this.dias = true;
		this.flagRepete = null;
		this.dataInicial = null;
		this.dataFinal = null;	
		this.repetir = Boolean.FALSE;
		this.sala = null;
		this.numeroMeses = 1;
		this.numeroAnos = 1;
		this.numeroSemanas = 1;
		this.diaAnual = 0;
		this.mes = 0;
		this.flagMarcaDesmarcaTodos = false;
		this.ordemClassificacao = CadastroMensalEnum.O;
		
		/*
		 * A lista de SalarHorario para seleção vem do bean
		 * salaHorarioSessaoList. 
		 * 
		 * O retorno para popular o grid é uma lista de
		 * SalaHorario, o qual possui um atributo chamado 'selecioado', que é
		 * utilizado para exibir em tela quais os registros selecionados pelo
		 * usuário. 
		 * 
		 * Para limpar esta seleção não basta apenas restar o Set
		 * salaHorarioSet desta classe, é preciso percorrer a referência neste
		 * set e marcar os registros como selecionado=false;
		 */		
		if (this.salaHorarioSet != null) {
			for (SalaHorario salaHorario : this.salaHorarioSet) {
				salaHorario.setSelecionado(false);
			}
		}
		this.salaHorarioSet = new HashSet<SalaHorario>();
	}

	/**
	 * Retorna a mensagem de acordo com as sessões cadastradas de acordo com o
	 * numero de sessões.
	 * 
	 * @param qtdSessoesCadastradas
	 *            numero de sessões esperado.
	 * @return mensagem retornada de acordo com o numero de sessões cadastradas.
	 */
	public String exibeMsgQtdSecCadastradas(int qtdSessoesCadastradas) {
		if (qtdSessoesCadastradas > 0) {
			return "Foram cadastradas " + qtdSessoesCadastradas + " no periodo informado.";
		} else {
			return "Não houve ocorrência(s) para o período informado.";
		}
	}

	@Override
	public String update() {
		// verifica se foi atribuida uma data para o prazo maximo de inclusão em
		// pauta
		if (null != getInstance().getDataMaxIncProcPauta()) {
			// verifica se o OJC da sessão possue fechamento altomatico de pauta
			if (getInstance().getOrgaoJulgadorColegiado().getFechamentoAutomatico()) {
				// verifica se a data informada é posterior a data da de
				// fechamento da pauta
				if (tratarData(getInstance().getDataMaxIncProcPauta()).after(
						tratarData(adicionaDias(getInstance().getDataFechamentoPauta(), -1)))) {
					getEntityManager().refresh(getInstance());
					FacesMessages.instance().add(Severity.ERROR,
							"A data informada é maior que o Prazo de Disponibilização da Pauta de Julgamento.");
				} else {
					return super.update();
				}
			} else {
				// verifica se a data informada é posterior a data da de
				// fechamento da sessão
				if (tratarData(getInstance().getDataMaxIncProcPauta()).after(tratarData(getInstance().getDataSessao()))) {
					getEntityManager().refresh(getInstance());
					FacesMessages.instance().add(Severity.ERROR,
							"A data informada é maior que a data da Sessão de Julgamento.");
				} else {
					return super.update();
				}
			}
		} else {
			return super.update();
		}
		return null;
	}

	public Boolean verificarDisponibilidade(SalaHorario ojcsh) {
		StringBuffer ejbql = new StringBuffer();
		ejbql.append("select count(o) from Sessao o ");
		ejbql.append("where cast(o.dataSessao as date) = :dataSessao ");
		ejbql.append("and o.orgaoJulgadorColegiadoSalaHorario = :ojcsh ");
		ejbql.append("and o.dataExclusao is null ");

		Query query = getEntityManager().createQuery(ejbql.toString());
		query.setParameter("dataSessao", dataInicial);
		query.setParameter("ojcsh", ojcsh);
		try {
			Long retorno = (Long) query.getSingleResult();
			return !(retorno > 0);
		} catch (NoResultException no) {
			return Boolean.TRUE;
		}
	}

	/**
	 * Retorna uma lista de OJ com base em SessaoComposicaoOrdem de uma
	 * determinada sessão. A sessão usada como parametro é a do instance de
	 * SessaoHome.
	 * 
	 * @return Lista de OJ recuperada.
	 */
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaoJulgadorSessaoList() {
		StringBuffer ejbql = new StringBuffer();
		ejbql.append("select o.orgaoJulgador from SessaoComposicaoOrdem o ");
		ejbql.append("where o.sessao = :sessao");
		Query q = getEntityManager().createQuery(ejbql.toString());
		q.setParameter("sessao", getInstance());
		return q.getResultList();
	}

	private List<OrgaoJulgador> listTemp = new ArrayList<OrgaoJulgador>();

	/**
	 * Com base na sessao da intancia de SessaoHome traz a lista de
	 * OrgaoJulgadores da composição já com os que fazem parte da composição
	 * selecionados.
	 */
	private List<OrgaoJulgador> pegarComposicaoSelecionada() {
		List<OrgaoJulgador> listaOjCadastradosComposicao = getOrgaoJulgadorSessaoList();

		List<OrgaoJulgador> listaOJCOJ = pegarOjDeOjc();

		// pega a lista de oj já cadastrados na composição e seta tomo
		// selecionados na lista de oj recuperada da composição daquele ojc
		for (OrgaoJulgador oj : listaOjCadastradosComposicao) {
			for (OrgaoJulgador ojcoj : listaOJCOJ) {
				if (oj.equals(ojcoj)) {
					ojcoj.setSelecionado(true);
				}
			}
		}
		return listaOJCOJ;
	}

	/**
	 * Pega a lista de OJ de um determinado OJC da sessão corrente de sessaoHome
	 * baseados na relação OrgaoJulgadorColegiadoOrgaoJulgador.
	 * 
	 * @return Retorna uma lista de OJ do OJC que esta na instancia de
	 *         sessaoHome.
	 */
	@SuppressWarnings("unchecked")
	private List<OrgaoJulgador> pegarOjDeOjc() {
		StringBuffer sb = new StringBuffer();
		sb.append("select o.orgaoJulgador from OrgaoJulgadorColegiadoOrgaoJulgador o where o.orgaoJulgadorColegiado = :ojc ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("ojc", getInstance().getOrgaoJulgadorColegiado());
		List<OrgaoJulgador> listaOJCOJ = q.getResultList();
		return listaOJCOJ;
	}

	/**
	 * Carrega a lista de OJ da composição da sessão
	 */
	public void carregaComposicao() {
		int idTemp = getInstance().getIdSessao();
		Sessao sessaoTemp = getInstance();
		getEntityManager().clear();
		setId(idTemp);
		setInstance(sessaoTemp);

		// carrega a lista de OJ de um mesmo OJC setando os OJ pertencentes a
		// composição como selecionados.
		setListaOjOjcComposicao(pegarComposicaoSelecionada());

		// carrega a lista de OJ pertencentes a composição da sessao.
		setListaOjOjcCompSelecionados(getOrgaoJulgadorSessaoList());

		// carrega o pesidente da sessão
		carregaPresidente();

		// carrega todos os revisores dos gabinetes da composição da sessão
		carregaRevisores();
	}

	/**
	 * Para cada oj participante da sessão carrega seu revisor.
	 */
	private void carregaRevisores() {
		// seta os revisores da lista da composição da sessão
		for (OrgaoJulgador oj : listaOjOjcCompSelecionados) {
			oj.setOjRevisor(retornaRevisor(oj));
		}

		// seta os revisores da lista de OJ do OJC composição da sessão
		for (OrgaoJulgador oj : listaOjOjcComposicao) {
			oj.setOjRevisor(retornaRevisor(oj));
		}
	}

	/**
	 * 
	 * @param ojPai
	 * @return OrgaoJulgador
	 */
	private OrgaoJulgador retornaRevisor(OrgaoJulgador ojPai) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(SessaoComposicaoOrdem.class);
		criteria.add(Restrictions.eq("sessao", getInstance()));
		criteria.add(Restrictions.eq("orgaoJulgador", ojPai));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		SessaoComposicaoOrdem sessaoComposicaoOrdem = (SessaoComposicaoOrdem)criteria.uniqueResult();
		if (sessaoComposicaoOrdem != null) {
			return sessaoComposicaoOrdem.getOrgaoJulgadorRevisor();
		} else {
			return null;
		}
	}

	/**
	 * Seta sessaoHome.presidente com o presiidente da sessão carregado do banco.
	 */
	private void carregaPresidente() {
		Criteria criteria = HibernateUtil.getSession().createCriteria(SessaoComposicaoOrdem.class);
		criteria.add(Restrictions.eq("sessao", getInstance()));
		criteria.add(Restrictions.eq("presidente", true));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		SessaoComposicaoOrdem SessaoComposicaoOrdem = (SessaoComposicaoOrdem)criteria.uniqueResult();
		if (SessaoComposicaoOrdem != null) {
			setPresidente(SessaoComposicaoOrdem.getOrgaoJulgador());
		} else {
			setPresidente(null);
		}
	}

	@SuppressWarnings("unchecked")
	public void desmarcarSelecionados() {
		StringBuffer ejbql = new StringBuffer();
		ejbql.append("select o from OrgaoJulgadorColegiadoOrgaoJulgador o");
		Query q = getEntityManager().createQuery(ejbql.toString());
		List<OrgaoJulgadorColegiadoOrgaoJulgador> list = new ArrayList<OrgaoJulgadorColegiadoOrgaoJulgador>(0);
		list.addAll(q.getResultList());

		for (OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoOrgaoJulgador : list) {
			orgaoJulgadorColegiadoOrgaoJulgador.setSelecionado(Boolean.FALSE);
		}
	}

	public boolean desabilitaComboRevisor(OrgaoJulgador ojRow) {
		// seta o retorno como true para desabilitar a combo
		boolean ret = true;
		// verifica se a row selecionada esta na composição
		if (listaOjOjcCompSelecionados.contains(ojRow)) {
			// verifica se o usuario logado possue OJ e OJC
			if (null != Authenticator.getOrgaoJulgadorAtual() && null != Authenticator.getOrgaoJulgadorColegiadoAtual()) {
				// verifica se a row selecionada esta na composição, caso esteja
				// verifica se a row selecionada é igual ao oj logado
				if (ojRow.equals(Authenticator.getOrgaoJulgadorAtual())) {
					// retorna false para não desabilitar a combo
					ret = false;
				}
				// se o usuario so tiver OJC ou OJ ou nenhum dos dois
			} else {
				// retorna false para não desabilitar a combo
				ret = false;
			}
		}
		return ret;
	}

	private boolean existePautaSessao() {
		StringBuilder jbql = new StringBuilder();
		jbql.append("select count(o) from SessaoComposicaoOrdem o ");
		jbql.append("where o.sessao in ");
		jbql.append("(select s.sessao from SessaoPautaProcessoTrf s ");
		jbql.append("where s.dataExclusaoProcessoTrf is null and s.sessao.idSessao = :sessao)");

		EntityManager em = getEntityManager();
		Query query = em.createQuery(jbql.toString());
		query.setParameter("sessao", getInstance().getIdSessao());
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	/**
	 * Pega um ojcoj e insere uma composição ordem setando ele como não
	 * presidente e como presente na sessão.
	 * 
	 * @param ojcoj
	 * @param isPresidente
	 */
	private void insereComposicaoOrdem(OrgaoJulgador oj, Boolean isPresidente, OrgaoJulgador orgaoJulgadorRevisor) {
		SessaoComposicaoOrdem sco = new SessaoComposicaoOrdem();
		sco.setOrgaoJulgador(oj);
		sco.setOrgaoJulgadorRevisor(orgaoJulgadorRevisor);
		sco.setPresidente(isPresidente);
		sco.setSessao(getInstance());
		sco.setMagistradoTitularPresenteSessao(Boolean.TRUE);
		sco.setMagistradoPresenteSessao(recuperarMagistradoTitular(oj));
		getEntityManager().persist(sco);
		EntityUtil.flush();
		
		getInstance().getSessaoComposicaoOrdemList().add(sco);
	}
	
	/**
	 * Recupera o magistrado titular do órgao julgador.
	 * 
	 * @param orgaoJulgador Órgão julgador.
	 * @return O magistrado titular do órgao julgador.
	 */
	private PessoaMagistrado recuperarMagistradoTitular(OrgaoJulgador orgaoJulgador) {
		return ComponentUtil.getComponent(PessoaMagistradoManager.class).getMagistradoTitular(orgaoJulgador);
	}

	/**
	 * Verifica o numero minomo de oj participantes da sessão e remove toda a
	 * composição da sessão e regrava toda a composição da sessão com os OJ
	 * selecionados
	 */
	public void gravarComposicao() {
		if (listaOjOjcCompSelecionados.size() < getInstance().getOrgaoJulgadorColegiado().getMinimoParticipante()) {
			FacesMessages.instance().add(Severity.ERROR, "Número de participantes menor que o mínimo");
		} else {
			try {
				removerComposicaoOrdem();
				cadastrarListaSelecionados();
				FacesMessages.instance().add("Composição da sessão atualizada.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Remove toda a composição da sessão
	 */
	private void removerComposicaoOrdem() {
		for (OrgaoJulgador oj : getOrgaoJulgadorSessaoList()) {
			String ejbql = "delete from SessaoComposicaoOrdem where sessao = :sessao and orgaoJulgador = :oj";
			getEntityManager().createQuery(ejbql).setParameter("sessao", getInstance()).setParameter("oj", oj)
			.executeUpdate();
		}
	}

	/**
	 * Pega a lista de OJ selecionados e grava a composição daquela sessão
	 */
	public void cadastrarListaSelecionados() {
		for (OrgaoJulgador oj : listaOjOjcCompSelecionados) {
			// verifica se o oj a ser cadastrado é igual ao presidente e
			// cadastra como presidente caso ele seja.
			insereComposicaoOrdem(oj, oj.equals(presidente), oj.getOjRevisor());
		}
	}

	@Override
	public String inactive(Sessao instance) {
		instance.setUsuarioExclusao(Authenticator.getUsuarioLogado());
		instance.setDataExclusao(new Date());
		instance.setPessoaProcurador(null);
		instance.setProcurador(null);
		
		return super.update();
	}

	public Date ultimaSessao(int id) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(ConsultaProcessoAdiadoVista.class);
		criteria.add(Restrictions.eq("processoTrf.idProcessoTrf", id));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		ConsultaProcessoAdiadoVista consultaProcessoAdiadoVista = (ConsultaProcessoAdiadoVista) criteria.uniqueResult();
		if (consultaProcessoAdiadoVista != null && consultaProcessoAdiadoVista.getSessaoPautaProcessoTrf() != null) {
			Sessao sessao = consultaProcessoAdiadoVista.getSessaoPautaProcessoTrf().getSessao();
			return sessao != null ? sessao.getDataSessao() : null;
		}
		return null;
	}

	/**
	 * Recupera a data da última sessão de julgamento do processo.
	 * 
	 * @param idProcessoTrf Identificador do processo.
	 * @return A data da ultima sessão de julgamento do processo.
	 */
	public Date ultimaSessaoTrf(Integer idProcessoTrf) {
		Sessao sessao = getUltimaSessaoProcesso(idProcessoTrf);
		if (sessao != null){
			return sessao.getDataSessao();
		}
		return null;
	}
	
	/**
	 * Método responsável por recuperar a situação da última sessão de julgamento do processo.
	 * 
	 * @param idProcessoTrf Identificador do processo.
	 * @return A data da ultima sessão de julgamento do processo.
	 */
	public String getSituacaoUltimaSessao(Integer idProcessoTrf) {
		Sessao sessao = getUltimaSessaoProcesso(idProcessoTrf);
		if (sessao != null){
			return getStatus(sessao);
		}
		return StringUtils.EMPTY;
	}
	
	/**
	 * Método responsável por recuperar a última sessão de julgamento do processo.
	 * 
	 * @param idProcessoTrf Identificador do processo.
	 * @return A última sessão de julgamento do processo.
	 */
	private Sessao getUltimaSessaoProcesso(Integer idProcessoTrf) {
		if (idProcessoTrf != null){
			return getSessaoManager().getUltimaSessaoProcesso(idProcessoTrf);
		}
		return null;
	}

	public void addItem(ProcessoTrf row) {
		if (getListTRF().contains(row)) {
			getListTRF().remove(row);
		} else {
			getListTRF().add(row);
		}
	}

	public void marcaTudo() {
		listTRF = new ArrayList<ProcessoTrf>();
		if (!marcouListTudo) {
			ProcessoTrfMesaList lista = ComponentUtil.getComponent(ProcessoTrfMesaList.class);
			lista.setMaxResults(null);
			listTRF.addAll(lista.getResultList());
			marcouListTudo = Boolean.TRUE;
		} else {
			marcouListTudo = Boolean.FALSE;
		}
		refreshGrid("idProcessosMesaList");
	}

	public void setListTRF(List<ProcessoTrf> list) {
		this.listTRF = list;
	}

	public List<ProcessoTrf> getListTRF() {
		return listTRF;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setExigePauta(Boolean exigePauta) {
		this.exigePauta = exigePauta;
	}

	public Boolean getExigePauta() {
		return exigePauta;
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoItems() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorColegiado o");
		Query q = getEntityManager().createQuery(sb.toString());
		return q.getResultList();
	}

	// Verifica se a sessão tem mais que um elemento
	public Boolean qtdSessao() {
		String sql = "select o from Sessao o ";
		Query q = EntityUtil.getEntityManager().createQuery(sql);
		if (q.getResultList().size() == 1) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public String getHoraInicial(Sessao obj) {
		String output = obj.getHorarioInicio() != null ? obj.getHorarioInicio().toString() : "";
		return output;
	}

	public String getHoraFinal(Sessao obj) {
		String output = obj.getOrgaoJulgadorColegiadoSalaHorario().getHoraFinal().toString();
		return output;
	}

	public Integer countProcessoTrf(Sessao obj) {
		String sql = "select o from SessaoPautaProcessoTrf o " + "where o.sessao.idSessao = :idSessao "
				+ "and o.dataExclusaoProcessoTrf is null and " + "o.usuarioExclusao is null";
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sql);
		query.setParameter("idSessao", obj.getIdSessao());
		return query.getResultList().size();
	}

	public int countSessaoDate() {
		return getListSessaoDate(null).size();
	}

	@SuppressWarnings("unchecked")
	public List<Sessao> getListSessaoDate(String verificacao) {
		String sql = null;
		if (verificacao == null) {
			sql = "select o from Sessao o where o.dataExclusao is null and (o.dataSessao = #{agendaSessao.currentDate}  or (o.dataFimSessao is not null and #{agendaSessao.currentDate} "
					+ "between o.dataSessao and o.dataFimSessao)) "
					+ "and o.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual}";
		} else if (verificacao.equals("sessaoAberta")) {
			sql = "select o from Sessao o where o.dataExclusao is null and (o.dataSessao = #{agendaSessao.currentDate}  or (o.dataFimSessao is not null and #{agendaSessao.currentDate} "
					+ "between o.dataSessao and o.dataFimSessao)) "
					+ "and o.dataAberturaSessao is not null "
					+ "and o.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual}";
		}
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sql);

		return query.getResultList();
	}

	public Boolean sessaoUnicaAberta() {
		List<Sessao> sessao = getListSessaoDateCompleto();
		if (sessao != null && sessao.size() > 0) {
			return sessao.get(0).getDataAberturaSessao() != null;
		} else {
			return false;
		}
	}

	public int countSessaoDateCompleto() {
		return this.getListSessaoDateCompleto().size();
	}

	public Boolean existemVariasSessoes() {		
		return this.countSessaoDateCompleto() > 1;
	}

	public boolean verificaExitenciaSessaoAbertaVinculada(Usuario usuario) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(s) from Sessao s ");
		sb.append("where s.pessoaProcurador.idUsuario = :procurador ");
		sb.append("and s.dataAberturaSessao != null ");
		sb.append("and s.dataFechamentoSessao = null ");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("procurador", usuario.getIdUsuario());
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Sessao> getListSessaoDateCompleto() {
		List<Sessao> result = new ArrayList<>();
		Date dataSelecionada = (Date)org.jboss.seam.core.Expressions.instance().createValueExpression("#{agendaSessao.currentDate}").getValue();

		if (dataSelecionada != null) {
			if (!this.listSessaoDateCompleto.containsKey(dataSelecionada.getTime())) {
				StringBuilder sb = new StringBuilder("select s from Sessao s ")
						.append("where s.dataExclusao is null ")
						.append("and (s.dataSessao = #{agendaSessao.currentDate} ") // Sessão do dia
						.append("or (s.dataFimSessao is not null and #{agendaSessao.currentDate} between s.dataSessao and s.dataFimSessao)) "); // Sessão contínua

					if (Authenticator.getOrgaoJulgadorAtual() != null) {
						sb.append("and ( ")
							.append("s in (")
								.append("select o.sessao from SessaoComposicaoOrdem o where o.orgaoJulgador = #{authenticator.getOrgaoJulgadorAtual()}")
							.append(") or s.orgaoJulgadorColegiado in ( ")
								.append("select ojcoloj.orgaoJulgadorColegiado from OrgaoJulgadorColegiadoOrgaoJulgador ojcoloj ")
								.append("where ojcoloj.orgaoJulgador = #{authenticator.getOrgaoJulgadorAtual()} ")
								.append("and ojcoloj.dataInicial <= s.dataSessao and (ojcoloj.dataFinal >= s.dataSessao or ojcoloj.dataFinal is null)")
							.append("))");
					}

					if (Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
						sb.append("and s.orgaoJulgadorColegiado = #{authenticator.getOrgaoJulgadorColegiadoAtual()} ");
					} else {
						sb.append("and s.pessoaProcurador.idUsuario = #{authenticator.getIdUsuarioLogado()}");
					}

					Query query = EntityUtil.createQuery(sb, false, true, "SessaoHome.getListSessaoDateCompleto");
					this.listSessaoDateCompleto.put(dataSelecionada.getTime(), query.getResultList());
			}
			result = this.listSessaoDateCompleto.get(dataSelecionada.getTime());
		}

		return result;
	}

	public int getIdSessaoDateAberta() {
		List<Sessao> listSessaoDate = getListSessaoDate("sessaoAberta");
		if (listSessaoDate.size() >= 1) {
			return listSessaoDate.get(0).getIdSessao();
		}
		return -1;
	}

	public int getIdSessaoDate() {
		List<Sessao> listSessaoDate = getListSessaoDate(null);
		if (listSessaoDate.size() >= 1) {
			return listSessaoDate.get(0).getIdSessao();
		}
		return -1;
	}

	public int getIdSessaoDateCompleto() {
		List<Sessao> listSessaoDate = getListSessaoDateCompleto();
		if (listSessaoDate.size() >= 1) {
			return listSessaoDate.get(0).getIdSessao();
		}
		return -1;
	}

	
	public void confirmarInclusaoProcessosBlocoPauta(Sessao sessao) {
		processosBlocosNaoPautados = ComponentUtil.getProcessoBlocoManager().recuperaProcessosBlocosNaoPautados(sessao);
		if( processosBlocosNaoPautados != null && processosBlocosNaoPautados.size() > 0 ) {
			setExibeMPConfirmacaoInclusaoProcessoBlocoPauta(true);
			setExibeMPConfirmacaoFecharPauta(false);
		} else {
			setExibeMPConfirmacaoFecharPauta(true);
			setExibeMPConfirmacaoInclusaoProcessoBlocoPauta(false);
		}
	}
	
	public void fecharPauta(boolean automatico, Sessao sessao) {
		SessaoJulgamentoService sessaoJulgamentoService = ComponentUtil.getComponent(SessaoJulgamentoService.class);		
		try {
			sessaoJulgamentoService.fecharPauta(sessao, true);
			destinatariosNaoIntimadosViaSistema = sessaoJulgamentoService.getDestinatariosNaoIntimadosViaSistema();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}

		boolean fechar = true;
		if( processosBlocosNaoPautados != null && processosBlocosNaoPautados.size() > 0 ) {
	  		try{
				for (ProcessoTrf p : processosBlocosNaoPautados){
					ComponentUtil.getSessaoPautaProcessoTrfManager().pautarProcesso(sessao, p, TipoInclusaoEnum.BL);
				}
	  		} catch (Exception e) {
	  			ComponentUtil.getFacesMessages().add(Severity.ERROR, e.getMessage());
	  			fechar = false;
			}			
		}
		if(fechar) {
			try {
				sessaoJulgamentoService.fecharPauta(sessao, true);
				destinatariosNaoIntimadosViaSistema = sessaoJulgamentoService.getDestinatariosNaoIntimadosViaSistema();
			} catch (PJeBusinessException e) {
				e.printStackTrace();
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, "Erro ao fechar a pauta.");
				setExibeMPConfirmacaoFecharPauta(false);
				setExibeMPConfirmacaoInclusaoProcessoBlocoPauta(false);
				return;
			}
		}
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Pauta fechada com sucesso.");
		setExibeMPConfirmacaoFecharPauta(false);
		setExibeMPConfirmacaoInclusaoProcessoBlocoPauta(false);
	}

	private Integer idSessaoEmFoco;
	public Integer getIdSessaoEmFoco() {
		return idSessaoEmFoco;
	}
	
	public void setIdSessaoEmFoco(Integer value) {
		if (!Objects.equals(this.idSessaoEmFoco, value)) {
			if ((value!=null) && (value!=0)) {
				SessaoJulgamentoService sessaoJulgamentoService = getComponent("sessaoJulgamentoServiceCNJ");
				sessaoJulgamentoService.prepararSecaoEmFoco(value);
			}
			this.idSessaoEmFoco = value;
		}
	}
	
	public void gerarPDFDestinatariosNaoIntimadosViaSistema() {
		if (destinatariosNaoIntimadosViaSistema != null && !destinatariosNaoIntimadosViaSistema.isEmpty()) {
			StringBuilder conteudoHtml = new StringBuilder("<b>Os processos abaixo contém partes que não podem ser intimadas via sistema.</b>");
			conteudoHtml.append("<table>");
			for (Map.Entry<String, List<Pessoa>> entry : destinatariosNaoIntimadosViaSistema.entrySet()) {
				conteudoHtml.append("<tr><td><br /><b>"+entry.getKey()+"</b></td></tr>");
				for (Pessoa parte : entry.getValue()) {
					conteudoHtml.append("<tr><td style='padding-left: 10px;' >")
						.append(parte)
						.append("</td></tr>");
				}
			}
			conteudoHtml.append("</table>");

			byte[] conteudoPdf = null;
			try {
				conteudoPdf = HtmlParaPdf.converte(conteudoHtml.toString());
			} catch (PdfException e) {
				e.printStackTrace();
			}
			
			FacesContext facesContext = FacesContext.getCurrentInstance();			
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();  
			try {
				response.setHeader("Content-disposition", "attachment; filename=\"partesNaoIntimadasViaSistema.pdf\"");
		        response.setContentType("application/pdf");
		        ServletOutputStream outStream = response.getOutputStream();
		        try {
		            outStream.write(conteudoPdf);
		            outStream.flush();
		        } finally {
		            outStream.close();
		        }
		        facesContext.responseComplete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String formatDataSessao(Sessao obj) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(obj.getDataSessao());
	}

	public void setProcurador(String procurador) {
		this.procurador = procurador;
	}

	public String getProcurador() {
		getPessoaProcuradorMPSuggest().setDefaultValue(getInstance().getProcurador());
		return getInstance().getProcurador();
	}

	private PessoaProcuradorMPSuggestBean getPessoaProcuradorMPSuggest() {
		return ComponentUtil.getComponent(PessoaProcuradorMPSuggestBean.class);
	}

	public void setDataAtual(Date dataAtual) {
		this.dataAtual = dataAtual;
	}

	public Date getDataAtual() {
		this.dataAtual = new Date();
		return dataAtual;
	}

	public String getStatus(Sessao sessao) {
		if (sessao.getDataAberturaSessao() == null) {
			if (sessao.getDataFechamentoPauta() == null) {
				return StatusSessaoEnum.EASP.getLabel();
			}
			return StatusSessaoEnum.EACP.getLabel();
		}	
		if (sessao.getDataAberturaSessao() != null && sessao.getDataRealizacaoSessao() == null) {
			return StatusSessaoEnum.A.getLabel();
		}
		if (sessao.getDataRealizacaoSessao() != null && sessao.getDataRegistroEvento() == null) {
			return StatusSessaoEnum.R.getLabel();
		}
		if (sessao.getDataRegistroEvento() != null && sessao.getDataFechamentoSessao() == null) {
			return StatusSessaoEnum.RE.getLabel();
		}
		if (sessao.getDataFechamentoSessao() != null) {
			return StatusSessaoEnum.F.getLabel();
		}
		return StringUtils.EMPTY;
	}

	public StatusSessaoEnum[] getStatusSessaoEnumValues() {
		return StatusSessaoEnum.values();
	}
	
	public SituacaoSessaoEnum[] getSituacaoSessaoEnumValues() {
		return SituacaoSessaoEnum.values();
	}

	public void setsalaHorarioSet(Set<SalaHorario> salaHorarioSet) {
		this.salaHorarioSet = salaHorarioSet;

	}

	public Set<SalaHorario> getSalaHorarioSet() {
		return salaHorarioSet;

	}

	public void setOrgaoJulgadorColegiadoRevisor(OrgaoJulgadorColegiado orgaoJulgadorColegiadoRevisor) {
		this.orgaoJulgadorColegiadoRevisor = orgaoJulgadorColegiadoRevisor;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiadoRevisor() {
		return orgaoJulgadorColegiadoRevisor;
	}

	public void setListaOJ(Set<OrgaoJulgador> listaOJ) {
		this.listaOJ = listaOJ;
	}

	public Set<OrgaoJulgador> getListaOJ() {
		return listaOJ;
	}

	public void setHabilitaCombo(Boolean habilitaCombo) {
		this.habilitaCombo = habilitaCombo;
	}

	public Boolean getHabilitaCombo() {
		return habilitaCombo;
	}

	public void setListTemp(List<OrgaoJulgador> listTemp) {
		this.listTemp = listTemp;
	}

	public List<OrgaoJulgador> getListTemp() {
		return listTemp;
	}

	public void setPresidente(OrgaoJulgador presidente) {
		this.presidente = presidente;
	}

	public OrgaoJulgador getPresidente() {
		return presidente;
	}

	public void setDataFechamentoPauta(Date dataFechamentoPauta) {
		this.dataFechamentoPauta = dataFechamentoPauta;
	}

	public Date getDataFechamentoPauta() {
		return dataFechamentoPauta;
	}

	public void setFlagRepete(String flagRepete) {
		this.flagRepete = flagRepete;
	}

	public String getFlagRepete() {
		return flagRepete;
	}

	public void setNumeroDias(int numeroDias) {
		this.numeroDias = numeroDias;
	}

	public int getNumeroDias() {
		return numeroDias;
	}

	public void setDias(boolean dias) {
		this.dias = dias;
	}

	public boolean getDias() {
		return dias;
	}

	public void setNumeroSemanas(int numeroSemanas) {
		this.numeroSemanas = numeroSemanas;
	}

	public int getNumeroSemanas() {
		return numeroSemanas;
	}

	/**
	 * Método que retorna a data da sessão no formato DD/MM/AAAA
	 * 
	 * @return Ex.: 26/03/2011
	 */
	public String dataSessaoFormatada() {
		String dataFormatada = "";
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
		dataFormatada = formato.format(instance.getDataSessao());
		return dataFormatada;
	}

	/**
	 * Método que retorna se uma sessão está aberta
	 * 
	 * @return true se a sessão foi aberta
	 */
	public boolean getSessaoAberta() {
		if (getInstance().getDataAberturaSessao() != null) {
			return true;
		}
		return false;
	}

	public void setNumeroMeses(int numeroMeses) {
		this.numeroMeses = numeroMeses;
	}

	public int getNumeroMeses() {
		return numeroMeses;
	}

	public void setOrdemClassificacao(CadastroMensalEnum ordemClassificacao) {
		this.ordemClassificacao = ordemClassificacao;
	}

	public CadastroMensalEnum getOrdemClassificacao() {
		return ordemClassificacao;
	}

	public void setFlagClassificacao(int flagClassificacao) {
		this.flagClassificacao = flagClassificacao;
	}

	public int getFlagClassificacao() {
		return flagClassificacao;
	}

	public void marcarRepeticaoDias(Integer dia) {
		if (diasSelecionados.contains(dia)) {
			diasSelecionados.remove(dia);
		} else {
			diasSelecionados.add(dia);
		}
	}

	public void setDiasSelecionados(Collection<Integer> diasSelecionados) {
		this.diasSelecionados = diasSelecionados;
	}

	public Collection<Integer> getDiasSelecionados() {
		return diasSelecionados;
	}

	public void setCheckDiasSelecionados(Boolean[] checkDiasSelecionados) {
		this.checkDiasSelecionados = checkDiasSelecionados;
	}

	public Boolean[] getCheckDiasSelecionados() {
		return checkDiasSelecionados;
	}

	public void setNumeroAnos(int numeroAnos) {
		this.numeroAnos = numeroAnos;
	}

	public int getNumeroAnos() {
		return numeroAnos;
	}

	public void setMes(int mes) {
		this.mes = mes;
	}

	public int getMes() {
		return mes;
	}

	public void setDiaAnual(int diaAnual) {
		this.diaAnual = diaAnual;
	}

	public int getDiaAnual() {
		return diaAnual;
	}

	public void setListaOjOjcComposicao(Collection<OrgaoJulgador> listaOjOjcComposicao) {
		this.listaOjOjcComposicao = listaOjOjcComposicao;
	}

	public Collection<OrgaoJulgador> getListaOjOjcComposicao() {
		return listaOjOjcComposicao;
	}

	public void setListaOjOjcCompSelecionados(Collection<OrgaoJulgador> listaOjOjcCompSelecionados) {
		this.listaOjOjcCompSelecionados = listaOjOjcCompSelecionados;
	}

	public Collection<OrgaoJulgador> getListaOjOjcCompSelecionados() {
		return listaOjOjcCompSelecionados;
	}

	/**
	 * Método que verifica pautas com fechamento automático e com prazo de
	 * termino para exibição do prazo em dias.
	 * 
	 * @return mensagem com quantidade de dias restantes
	 */
	public String msgDiasFechamantoPauta() {
		OrgaoJulgadorColegiado oJColegiado = getInstance().getOrgaoJulgadorColegiado();
		Date dataFechamento = getInstance().getDataFechamentoPauta();
		Integer prazoTermino = oJColegiado.getPrazoTermino();
		if (oJColegiado.getFechamentoAutomatico() && prazoTermino != null && dataFechamento != null) {
			Long diasLong = DateUtil.diferencaDias(dataFechamento, new Date());
			if (diasLong <= prazoTermino) {
				return "Faltam " + diasLong + " dia(s) para o fechamento da Pauta de Julgamento.";
			}
		}
		return null;
	}

	public boolean getFlagMarcaDesmarcaTodos() {
		return flagMarcaDesmarcaTodos;
	}

	public void setFlagMarcaDesmarcaTodos(boolean flagMarcaDesmarcaTodos) {
		this.flagMarcaDesmarcaTodos = flagMarcaDesmarcaTodos;
	}

	public boolean getExibeMPConfirmacaoAdiaProcs() {
		return exibeMPConfirmacaoAdiaProcs;
	}

	public void setExibeMPConfirmacaoAdiaProcs(boolean exibeMPConfirmacaoAdiaProcs) {
		this.exibeMPConfirmacaoAdiaProcs = exibeMPConfirmacaoAdiaProcs;
	}

	public boolean getExibeMPProcsPendentesProclamacao() {
		return exibeMPProcsPendentesProclamacao;
	}

	public void setExibeMPProcsPendentesProclamacao(boolean exibeMPProcsPendentesProclamacao) {
		this.exibeMPProcsPendentesProclamacao = exibeMPProcsPendentesProclamacao;
	}

	public boolean getExibeMPConfirmacaoGeracaoComposicaoInicial() {
		return exibeMPConfirmacaoGeracaoComposicaoInicial;
	}

	public void setExibeMPConfirmacaoGeracaoComposicaoInicial(boolean exibeMPConfirmacaoGeracaoComposicaoInicial) {
		this.exibeMPConfirmacaoGeracaoComposicaoInicial = exibeMPConfirmacaoGeracaoComposicaoInicial;
	}

	public boolean getExibeMPConfirmacaoOrgaosSemTitulares() {
		return exibeMPConfirmacaoOrgaosSemTitulares;
	}

	public void setExibeMPConfirmacaoOrgaosSemTitulares(boolean exibeMPConfirmacaoOrgaosSemTitulares) {
		this.exibeMPConfirmacaoOrgaosSemTitulares = exibeMPConfirmacaoOrgaosSemTitulares;
	}
	
	public boolean getExibeMPConfirmacaoAtualizacaoComposicaoProcessos(){
		return exibeMPConfirmacaoAtualizacaoComposicaoProcessos; 
	}
	
	public void setExibeMPConfirmacaoAtualizacaoComposicaoProcessos(Boolean exibeMPConfirmacaoAtualizacaoComposicaoProcessos){
		this.exibeMPConfirmacaoAtualizacaoComposicaoProcessos = exibeMPConfirmacaoAtualizacaoComposicaoProcessos;
	}
	
	public boolean getExibeMPConfirmacaoAtualizacaoComposicaoPrincipalSessao() {
		return exibeMPConfirmacaoAtualizacaoComposicaoPrincipalSessao;
	}

	public void setExibeMPConfirmacaoAtualizacaoComposicaoPrincipalSessao(boolean exibeMPConfirmacaoAtualizacaoComposicaoPrincipalSessao) {
		this.exibeMPConfirmacaoAtualizacaoComposicaoPrincipalSessao = exibeMPConfirmacaoAtualizacaoComposicaoPrincipalSessao;
	}
	
	public List<SessaoPautaProcessoTrf> getSpptProclamacaoPendenciaList() {
		return spptProclamacaoPendenciaList;
	}

	public void setSpptProclamacaoPendenciaList(List<SessaoPautaProcessoTrf> spptProclamacaoPendenciaList) {
		this.spptProclamacaoPendenciaList = spptProclamacaoPendenciaList;
	}

	/**
	 * Método que carrega os valores dos campor procurador e pessoaProcurador de
	 * acordo com a sessão instanciada
	 */
	public void carregaProcurador() {
		if (getInstance().getPessoaProcurador() == null) {
			procurador = getInstance().getProcurador();
			getPessoaProcuradorMPSuggest().setDefaultValue(getInstance().getProcurador());
		} else {
			pessoaProcurador = getInstance().getPessoaProcurador();
			getPessoaProcuradorMPSuggest().setDefaultValue(getInstance().getPessoaProcurador().getNome());
		}
	}

	public PessoaProcurador getPessoaProcurador() {
		return pessoaProcurador;
	}

	public void setPessoaProcurador(PessoaProcurador pessoaProcurador) {
		this.pessoaProcurador = pessoaProcurador;
	}

	/**
	 * Método que realiza a busca da sessão aberta atribuída ao procurador MP
	 * logado e a instancia em sessaoHome.
	 * 
	 * @return boolean indicando se existe ou não sessão aberta para o
	 *         procurador MP logado
	 */
	@SuppressWarnings("unchecked")
	public Boolean instanceSessaoProcuradorMP() {

		if(listaSessoesProcurador == null){
			StringBuilder sb = new StringBuilder();
			sb.append("select o from Sessao o ");
			sb.append(" where (o.pessoaProcurador.idUsuario = :idProcuradorMP ");
			sb.append(" or o.continua = true ");
			sb.append(" or exists(select 1 from SessaoEnteExterno sess where sess.sessao.idSessao = o.idSessao ");
			sb.append(" and sess.pessoaAcompanhaSessao.idUsuario = :idProcuradorMP and sess.ativo = true))");
			sb.append(" and o.dataAberturaSessao is not null ");
			sb.append(" and o.dataRealizacaoSessao is null	");
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("idProcuradorMP", Authenticator.getUsuarioLogado().getIdUsuario());
			listaSessoesProcurador = q.getResultList();
		}
		if (listaSessoesProcurador.isEmpty()) {
			FacesMessages.instance().add(Severity.ERROR,
					"Painel indisponível. Não há nenhuma sessão aberta atribuída no momento.");
			return false;
		} else if(listaSessoesProcurador.size() == 1){
			setInstance(listaSessoesProcurador.get(0));
			setId(listaSessoesProcurador.get(0).getIdSessao());
			return true;
		}
		return listaSessoesProcurador.contains(getInstance());
	}

	public String pegarSituacaoSessaoStr(Sessao sessao) {
		if (sessao.getDataExclusao() != null) {
			return SituacaoSessaoEnum.INATIVA.toString();
		}
		return SituacaoSessaoEnum.ATIVA.toString();
	}

	public String getProcuradorMP() {
		if (getInstance().getPessoaProcurador() != null) {
			return getInstance().getPessoaProcurador().getNome();
		} else {
			return getInstance().getProcurador();
		}
	}

	private boolean existeProcurador() {
		return getInstance().getPessoaProcurador() != null || getPessoaProcuradorMPSuggest().getSelected() != null;
	}

	private boolean suggestVazia() {
		return getPessoaProcuradorMPSuggest().getSelected() == null
				&& Strings.isEmpty((String) getPessoaProcuradorMPSuggest().getTyped());
	}
	
	/**
	 * Metodo que retorna uma lista de processo com pendências na sessão de julgamento.
	 * @param sessaoPautaProcessoList
	 * @return new ArrayList<ProcessoTrf>()
	 */
	public List<ProcessoTrf> getProcessosComPendencia(){
		
		processosComPendenciaMap = new HashMap<ProcessoTrf, List<String>>(0);

		List<SessaoPautaProcessoTrf> sessaoPautaProcessoList = recuperaSessaoPautaProcessosSessaoAtual();
		
		verificaProclamacaoJulgamento(sessaoPautaProcessoList);
		
		verificaRegistrarMovimentacao();
		
		habilitaBotaoAtaJulgamento();
		
		habilitaBotaoFinalizar();
		
		return new ArrayList<ProcessoTrf>(processosComPendenciaMap.keySet());
	}

	/**
	 * Metodo que verifica a proclamação do julgamento dos processos na sessão de julgamento.
	 */
	public void verificaProclamacaoJulgamento(List<SessaoPautaProcessoTrf> lista) {
		if (verificaPendenteProclamacaoJulgamento(lista)){
			for (SessaoPautaProcessoTrf spp : getSpptProclamacaoPendenciaList()) {
				if (processosComPendenciaMap.get(spp.getProcessoTrf())==null){
					processosComPendenciaMap.put(spp.getProcessoTrf(), new ArrayList<String>(1));
				}
				processosComPendenciaMap.get(spp.getProcessoTrf()).add("Proclamação de Julgamento");
			}
		}
	}
	
	/**
	 * Metodo que verifica a proclamação do julgamento dos processos na sessão de julgamento.
	 */
	public void verificaProclamacaoJulgamento() {
		if (verificaPendenteProclamacaoJulgamento()){
			for (SessaoPautaProcessoTrf spp : getSpptProclamacaoPendenciaList()) {
				if (processosComPendenciaMap.get(spp.getProcessoTrf())==null){
					processosComPendenciaMap.put(spp.getProcessoTrf(), new ArrayList<String>(1));
				}
				processosComPendenciaMap.get(spp.getProcessoTrf()).add("Proclamação de Julgamento");
			}
		}
	}

	/**
	 * 
	 */
	public void verificaRegistrarMovimentacao() {
		List<ProcessoTrf> processosSemMovimentacaoJulgamento = this.getSessaoManager().recuperarProcessosSemMovimentacaoJulgamento(
			this.getSessaoIdSessao(), this.instance.getDataAberturaSessao());
		
		for (ProcessoTrf processoTrf : processosSemMovimentacaoJulgamento) {
			if (!this.processosComPendenciaMap.containsKey(processoTrf)) {
				this.processosComPendenciaMap.put(processoTrf, new ArrayList<>());
			}
			processosComPendenciaMap.get(processoTrf).add("Registrar Movimentação");
		}
	}

	public Map<ProcessoTrf, List<String>> getProcessosComPendenciaMap() {
		return processosComPendenciaMap;
	}

	public void setProcessosComPendenciaMap(Map<ProcessoTrf, List<String>> processosComPendenciaMap) {
		this.processosComPendenciaMap = processosComPendenciaMap;
	}

	/**
	 * @return the listProcessosSemDocumentosSessao
	 */
	public List<ProcessoTrf> getListProcessosSemDocumentosSessao() {
		return listProcessosSemDocumentosSessao;
	}

	/**
	 * @param listProcessosSemDocumentosSessao the listProcessosSemDocumentosSessao to set
	 */
	public void setListProcessosSemDocumentosSessao(List<ProcessoTrf> listProcessosSemDocumentosSessao) {
		this.listProcessosSemDocumentosSessao = listProcessosSemDocumentosSessao;
	}

	public boolean isCarregaSuggest() {
		return carregaSuggest;
	}

	public void setCarregaSuggest(boolean carregaSuggest) {
		this.carregaSuggest = carregaSuggest;
	}

	public void setModeloDocumento(String modelo) {
		documentoSessao.setModeloDocumentoSessao(ComponentUtil.getComponent(ModeloDocumentoManager.class).obtemConteudo(modelo));
	}

	public String getModeloDocumento() {
		return modeloDocumento;
	}
	
	public DocumentoSessao getDocumentoSessao() {
		return documentoSessao;
	}
	
	public void setDocumentoSessao(DocumentoSessao documentoSessao) {
		this.documentoSessao = documentoSessao;
	}
	
	/**
	 * Lista os processos da sessão.
	 * 
	 * @return Recupera os processos da sessão.
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoPautaProcessoTrf> getProcessosDaSessao(Integer idSessao) {
		StringBuilder sb = new StringBuilder("select o from SessaoPautaProcessoTrf o ");
		sb.append("where o.sessao.idSessao = :idSessao "
				+ " and o.situacaoJulgamento = 'JG' ");
		sb.append("order by o.numeroOrdem ");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("idSessao", idSessao);
		
		List<SessaoPautaProcessoTrf> processosSessao = query.getResultList();

		if (processosSessao.size() >= 1) {
			return processosSessao;
		} else {
			return null;
		}
	}
	
	public String addDadosProcesso(String label, String valor){
		StringBuilder sb = new StringBuilder();
		sb.append("<div style='padding: 0; width: 92%; clear: both;'>");
		sb.append("<div style=' height:20px;float:left;width: 120px;'><b>"+label+"</b></div>");
		sb.append("<div style='height:20px;float:left;'>"+" : "+valor+"</div>");
		sb.append("</div>");
		return sb.toString();
	}
		
	public void definirPresidenteSessao(){
		PessoaMagistrado presidente = getInstance().getPresidenteSessao();

		if (presidente != null) {
			setPresidenteSessao(presidente.getPessoa().getNome());
		}				
	}

	/**
	 * Obtém os processos da sessão.
	 * 
	 * @return String HTML contendo os processos da sessão.
	 * @throws PJeBusinessException
	 * @Deprecated User o método processoJulgados por motivos de coesão.
	 */
	public String processosPautaSessao() throws PJeBusinessException{
		Sessao sessao = getInstance();
		List<SessaoPautaProcessoTrf> processos = getSessaoJulgamentoManager().consultarJulgados(sessao);
		return buildHTMLSessao(processos);
	}

	/**
	 * Obtém os processos julgados da sessão.
	 * 
	 * @return String HTML contendo os processos julgados da sessão.
	 * @throws PJeBusinessException 
	 */
	public String processosJulgados() throws PJeBusinessException{
		String result = null;
		Sessao sessao = getInstance();
		if(sessao.getIdSessao() > 0){
			List<SessaoPautaProcessoTrf> processos = getSessaoJulgamentoManager().consultarJulgados(sessao);
			result = buildHTMLSessao(processos);
		}
		return result;
	}
	
	/**
	 * Obtém os processos adiados da sessão.
	 * 
	 * @return String HTML contendo os processos adiados da sessão.
	 * @throws PJeBusinessException 
	 */
	public String processosAdiados() throws PJeBusinessException{
		Sessao sessao = getInstance();
		List<SessaoPautaProcessoTrf> processos = getSessaoJulgamentoManager().consultarAdiados(sessao);
		return buildHTMLSessao(processos);
	}

	/**
	 * Obtém os processos retirados da sessão.
	 * 
	 * @return String HTML contendo os processos retirados da sessão.
	 * @throws PJeBusinessException 
	 */
	public String processosRetirados() throws PJeBusinessException{
		Sessao sessao = getInstance();
		List<SessaoPautaProcessoTrf> processos = getSessaoJulgamentoManager().consultarRetirados(sessao);
		return buildHTMLSessao(processos);
	}

	/**
	 * Obtém os processos com pedidos de vista da sessão.
	 * 
	 * @return String HTML contendo os processos com pedidos de vista da sessão.
	 * @throws PJeBusinessException 
	 */
	public String processosPedidosVista() throws PJeBusinessException{
		Sessao sessao = getInstance();
		List<SessaoPautaProcessoTrf> processos = getSessaoJulgamentoManager().consultarPedidosVista(sessao);
		return buildHTMLSessao(processos);
	}	

	/**
	 * Obtém processo da sessão.
	 * 
	 * @return String HTML contendo informações do processo da sessão.
	 * @throws PJeBusinessException 
	 */
	public String processoSessaoCabecalho(SessaoPautaProcessoTrf processoSessao) throws PJeBusinessException{
		StringBuilder sb = new StringBuilder();
		sb.append("<p style='height: 6px'>");
		if (processoSessao != null){
			ProcessoJudicialService processoJudicialService = ComponentUtil.getComponent(ProcessoJudicialService.class);
			
			//número da ordem da sessão
			sb.append(addDadosProcesso("Ordem",String.format("%03d", processoSessao.getNumeroOrdem())));
	
			if (processoSessao.getProcessoTrf() != null){
				//processo
				sb.append(addDadosProcesso("Processo","<b>"+processoSessao.getConsultaProcessoTrf().getNumeroProcesso()+"</b>"));
	
				//classe judicial
				sb.append(addDadosProcesso("Classe Judicial","<b>"+processoSessao.getConsultaProcessoTrf().getClasseJudicial()+"</b>"));
				
				sb.append(addDadosProcesso("Órgão julgador",processoSessao.getConsultaProcessoTrf().getOrgaoJulgador()));				
				//magistrado relator

				if (processoSessao.getProcessoTrf().getPessoaRelator() !=null && processoSessao.getProcessoTrf().getPessoaRelator().getPessoa() !=null){
					sb.append(addDadosProcesso("Relator",processoSessao.getProcessoTrf().getPessoaRelator().getPessoa().getNome()));
				}
				
				if (processoSessao.getProcessoTrf().getOrgaoJulgadorRevisor() !=null && processoSessao.getProcessoTrf().getOrgaoJulgadorRevisor().getOrgaoJulgador() !=null){
					sb.append(addDadosProcesso("Revisor",processoSessao.getProcessoTrf().getOrgaoJulgadorRevisor().getOrgaoJulgador()));
				}
				
				//requerente
				if (processoSessao.getConsultaProcessoTrf().getAutor() !=null){
					sb.append(addDadosProcesso("Requerente",processoJudicialService.nomeParaExibicao(processoSessao.getConsultaProcessoTrf().getAutor(), 
							processoSessao.getConsultaProcessoTrf().getQtAutor())));
				}
				
				//advogado ativo
				if (!processoSessao.getProcessoTrf().getListaAdvogados(ProcessoParteParticipacaoEnum.A).isEmpty()){
					sb.append(addDadosProcesso("Advogado",getNomeExibicaoAdvogadoPolo(processoSessao.getProcessoTrf(), ProcessoParteParticipacaoEnum.A)));
				}

				//requerido
				if (processoSessao.getConsultaProcessoTrf().getReu() !=null){
					sb.append(addDadosProcesso("Requerido",processoJudicialService.nomeParaExibicao(processoSessao.getConsultaProcessoTrf().getReu(), 
							processoSessao.getConsultaProcessoTrf().getQtReu())));
				}
				
				//advogado passivo
				if (!processoSessao.getProcessoTrf().getListaAdvogados(ProcessoParteParticipacaoEnum.P).isEmpty()){
					sb.append(addDadosProcesso("Advogado",getNomeExibicaoAdvogadoPolo(processoSessao.getProcessoTrf(), ProcessoParteParticipacaoEnum.P)));
				}
				
				//terceiros
				if (!processoSessao.getProcessoTrf().getListaPartePrincipalTerceiro().isEmpty()){
					sb.append(addDadosProcesso("Terceiros",getNomeExibicaoPolo(processoSessao.getProcessoTrf(), ProcessoParteParticipacaoEnum.T)));
				}
				
				//vencedor
				if (processoSessao.getOrgaoJulgadorVencedor() !=null){
					sb.append(addDadosProcesso("Vencedor",processoSessao.getOrgaoJulgadorVencedor().getOrgaoJulgador()));
				}

				//decisão
				if (processoSessao.getProclamacaoDecisao() !=null){
					sb.append("<div style='text-align: justify;clear:both;width:80%;'><b>Decisão: </b><i>"+processoSessao.getProclamacaoDecisao()+"</i></div>");
				}

				//presentes na sessão
				List<PessoaMagistrado> presentesSessaoList = recuperaPresentesSessao(processoSessao);
				FormatadorUtils formatadorUtils = new FormatadorUtils();
				
				if (!presentesSessaoList.isEmpty()){
					String presentes = formatadorUtils.lista(presentesSessaoList, ",", true, "nome");
					sb.append("<div style='text-align: justify;clear:both;width:80%;'><b>Presentes à sessão: </b><i>"+presentes+"</i></div>");
				}
			
				//sustentação oral
				String sustentacaoOral = processoSessao.getAdvogadoSustentacaoOral();			
				if (sustentacaoOral !=null){				
					sb.append("<div style='text-align: justify;clear:both;width:80%;'>Sustentou oralmente "+sustentacaoOral+"</div>");
				}				
			}
		}
		return sb.toString();
	}
	
	public String getNomeExibicaoPolo(ProcessoTrf processoJudicial, ProcessoParteParticipacaoEnum polo){
		List<ProcessoParte> parte = null;
		
		if(ProcessoParteParticipacaoEnum.A.equals(polo))
			parte = processoJudicial.getListaPartePrincipalAtivo();
		else if(ProcessoParteParticipacaoEnum.P.equals(polo))
			parte = processoJudicial.getListaPartePrincipalPassivo();
		else
			parte = processoJudicial.getListaPartePrincipal(polo);
		
		if (parte.size() == 0){
			return "Não definido";
		}
		else{
			StringBuilder sb = new StringBuilder();
			sb.append(parte.get(0).getNomeParte().toUpperCase());
			if (parte.size() > 1){
				sb.append(" e outros");
			}
			return sb.toString();
		}
	}
	
	public String getNomeExibicaoAdvogadoPolo(ProcessoTrf processoJudicial, ProcessoParteParticipacaoEnum polo) {
	    List<ProcessoParte> processosParte = getProcessosPartePoloAtivoPassivo(processoJudicial, polo);

	    if (processosParte == null || processosParte.isEmpty()) {
	        return "Não definido";
	    }

	    StringBuilder sb = new StringBuilder();
	    sb.append(processosParte.get(0).getNomeParte().toUpperCase());

	    if (processosParte.size() > 1) {
	        sb.append(" e outros");
	    }

	    return sb.toString();
	}

	/*
	 * Operação criada para atender refactoring a pedido da revisão de código.
	 */
	private List<ProcessoParte> getProcessosPartePoloAtivoPassivo(ProcessoTrf processoJudicial, ProcessoParteParticipacaoEnum polo) {
		if(ProcessoParteParticipacaoEnum.A.equals(polo)){
			return processoJudicial.getListaAdvogadosPoloAtivo();
		}else if(ProcessoParteParticipacaoEnum.P.equals(polo)){
			return processoJudicial.getListaAdvogadosPoloPassivo();
		}
		return null;
     }

	public List<PessoaMagistrado> recuperaPresentesSessao(SessaoPautaProcessoTrf julgamento){
		List<PessoaMagistrado> presentes = ComponentUtil.getComponent(SessaoJulgamentoService.class).getPresentes(julgamento, false);
		Collections.sort(presentes, new Comparator<PessoaMagistrado>() {
			@Override
			public int compare(PessoaMagistrado p1, PessoaMagistrado p2) {
				return p1.getNome().compareTo(p2.getNome());
			}
		});
		return presentes;
	}
	
	public String getModeloDocumentoSessao(int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.modeloDocumentoSessao from DocumentoSessao o where ");
		sb.append("o.sessao.idSessao = :idSessao ");
		Query q = getEntityManager()
				.createQuery(sb.toString())
				.setParameter("idSessao", idSessao);
		return EntityUtil.getSingleResult(q);
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public boolean habilitaBotaoAtaJulgamento() {
		if (this.habilitaBotaoAtaJulgamento == null) {
			if (this.instance.getDataRealizacaoSessao() != null) {
				List<ProcessoTrf> processosCertidaoJulgamentoNaoAssinado = this.getSessaoManager().recuperarProcessosCertidaoJulgamentoNaoAssinado(this.getSessaoIdSessao());

				for (ProcessoTrf processoTrf : processosCertidaoJulgamentoNaoAssinado) {
					if (!this.processosComPendenciaMap.containsKey(processoTrf)){
						this.processosComPendenciaMap.put(processoTrf, new ArrayList<>());
					}
					this.processosComPendenciaMap.get(processoTrf).add("Certidão de Julgamento");
				}
				this.habilitaBotaoAtaJulgamento = processosCertidaoJulgamentoNaoAssinado.isEmpty();
			} else {
				this.habilitaBotaoAtaJulgamento = Boolean.FALSE;
			}
		}
		return this.habilitaBotaoAtaJulgamento;
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public boolean habilitaBotaoFinalizar() {
		if (this.habilitaBotaoFinalizar == null) {
			List<ProcessoTrf> processosAcordaoNaoAssinado = this.getSessaoManager().recuperarProcessosAcordaoNaoAssinado(this.getSessaoIdSessao());
			
			for (ProcessoTrf processoTrf : processosAcordaoNaoAssinado) {
				if (!this.processosComPendenciaMap.containsKey(processoTrf)) {
					this.processosComPendenciaMap.put(processoTrf, new ArrayList<>());
				}
				this.processosComPendenciaMap.get(processoTrf).add("Assinatura do Acórdão");
			}
			
			this.habilitaBotaoFinalizar = processosAcordaoNaoAssinado.isEmpty();
		}
		return this.habilitaBotaoFinalizar;
	}
	
	/**
	 * Método que verifica se existe algum processo na lista que já foi julgado
	 * 
	 * @return <code>true</code> caso algum processo tenha sido julgado ou
	 *         <code>false</code> caso exista somente processos não julgados
	 */
	public boolean isPermiteEmitirCertidao() {
		return !recuperarProcessos().isEmpty();		
	}
	
	/**
	 * Método responsável por retornar os processos da sessão de acordo com o parametro pje:lista:situacaoJulgamento
	 * 
	 * @return {@link List} contendo os processos
	 */
	private List<SessaoPautaProcessoTrf> recuperarProcessos() {
		if (this.listaSessaoPautaProcessoTrf.isEmpty()) {
			try {
				String listaSituacaoJulgamento = ParametroUtil.instance().getListaSituacaoJulgamento();
				if (StringUtils.isNotEmpty(listaSituacaoJulgamento)) {
					this.listaSessaoPautaProcessoTrf = getSessaoPautaProcessoTrfManager().recuperarTodosSessaoPautaProcessosTrf(getSessaoIdSessao());
				} else {
					this.listaSessaoPautaProcessoTrf = getSessaoPautaProcessoTrfManager().recuperarTodosSessaoPautaProcessosTrfJulgados(getSessaoIdSessao());
				}
			} catch (Exception e) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, String.format("Não foi possível recuperar os processos julgados"));
			}
		}
		return this.listaSessaoPautaProcessoTrf;
	}

	/**
	 * Recupera as sessões de julgamento de um processo.
	 * 
	 * @return Lista de {@link Sessao}.
	 */
	@SuppressWarnings("unchecked")
	public List<Sessao> getListaSessoesProcesso() {
		if (getProcessoTrf() != null){
			listaSessoesProcesso = getSessaoManager().getSessoesProcesso(getProcessoTrf().getIdProcessoTrf());
		}
		return listaSessoesProcesso != null ? listaSessoesProcesso : Collections.EMPTY_LIST;
	}

	/**
	 * Retorna o cabeçalho da sessão para ser usado na Ata de julgamento.
	 * 
	 * @return Cabeçalho da sessão.
	 */
	public String processoSessaoCabecalho() {
		StringBuilder sb = new StringBuilder();
		definirPresidenteSessao();
		
		if (getPresidenteSessao() !=null){
			sb.append(addDadosProcesso("Presidente",getPresidenteSessao()));
		}
		
		if (getComposicaoSessao() !=null){
			sb.append(addDadosProcesso("Magistrados",getComposicaoSessao()));
		}

		if (getInstance().getProcurador() !=null){
			sb.append(addDadosProcesso("Procurador",getInstance().getProcurador()));
		}else if (getInstance().getPessoaProcurador() !=null){
				sb.append(addDadosProcesso("Procurador",getInstance().getPessoaProcurador().getNome()));
		}

		sb.append(addDadosProcesso("Secretário",getInstance().getSecretarioIniciou().getNome()));
		sb.append(addDadosProcesso("Data Sessão",dataSessaoFormatada()));
		sb.append(addDadosProcesso("Início sessão",getHoraInicial(getInstance())));
		sb.append(addDadosProcesso("Fim sessão",getHoraFinal(getInstance())));
		
		return sb.toString();
	}
	
	public void confirmarGeracaoComposicaoInicial() {
		exibeMPConfirmacaoGeracaoComposicaoInicial = true;
	}
	
	public void confirmarGeracaoSemTitulares() {
		exibeMPConfirmacaoOrgaosSemTitulares = true;
	}
	
	public void confirmarAtualizacaoComposicaoJulgamentoPrincipalSessao(){
		exibeMPConfirmacaoAtualizacaoComposicaoPrincipalSessao = true;
	}

	public void confirmarAtualizacaoComposicaoJulgamentoProcessos(){
		exibeMPConfirmacaoAtualizacaoComposicaoProcessos = true;
		this.sugestoesAdiamentoJulgamentoProcessoPorAusenciaJulgadoresPrincipais = getComposicaoJulgamentoService().obterSugestoesAdiamentoJulgamentoProcessoPorAusenciaJulgadoresPrincipais(getInstance());
	}

	public boolean verificarExistenciaOJsSemTitulares(){
		boolean retorno = Boolean.TRUE;

		List<OrgaoJulgadorColegiadoOrgaoJulgador> composicaoAtivaColegiado = ComponentUtil.getOrgaoJulgadorColegiadoManager()
				.obterComposicaoAtiva(this.instance.getOrgaoJulgadorColegiado());

		if (composicaoAtivaColegiado != null && !composicaoAtivaColegiado.isEmpty()) {

			List<Integer> composicaoAtivaColegiadoId = new ArrayList<>();

			for (OrgaoJulgadorColegiadoOrgaoJulgador p : composicaoAtivaColegiado) {
				composicaoAtivaColegiadoId.add(p.getOrgaoJulgador().getIdOrgaoJulgador());
			}

			OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent(OrgaoJulgadorManager.class);

			List<OrgaoJulgador> orgaosJulgadoresSemTitular = orgaoJulgadorManager
					.recuperarSemTitular(composicaoAtivaColegiadoId);

			if (!orgaosJulgadoresSemTitular.isEmpty()) {
				retorno = Boolean.FALSE;
				this.orgaosSemTitulares = String.format(
						"Não há titular para os seguintes órgãos julgadores: %s. Deseja gerar a composição sem esses órgãos?",
						StringUtil.listToString(orgaosJulgadoresSemTitular));
			}
		}

		if (exibeMPConfirmacaoGeracaoComposicaoInicial) {
			criandoComposicaoInicial = true;
			exibeMPConfirmacaoGeracaoComposicaoInicial = false;
		} else {
			criandoComposicaoInicial = false;
			exibeMPConfirmacaoAtualizacaoComposicaoPrincipalSessao = false;
		}

		return retorno;
	}

	/**
	 * Método responsável por criar a composição ininical dos participantes da sessão de julgamento.
	 */
	public void gerarComposicaoInicial() {
		try {
			getComposicaoJulgamentoService().gerarComposicaoJulgamentoInicial(this.instance);
			getEntityManager().refresh(this.instance);
			FacesMessages.instance().add(Severity.INFO, "Composição inicial gerada com sucesso.");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Não foi possível gerar a composição inicial da sessão de julgamento: " + e.getLocalizedMessage());
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Erro ao gerar a composição inicial da sessão de julgamento: " + e.getLocalizedMessage());
		} finally {
			ComponentUtil.getComponent(SessaoComposicaoOrdemSecretarioSessaoList.class).refresh();
			exibeMPConfirmacaoOrgaosSemTitulares = false;
		}
	}
	
	public void atualizarComposicaoJulgamentoPrincipalSessao(){
		try {
			getComposicaoJulgamentoService().atualizarComposicaoJulgamentoPrincipal(getInstance());
			getEntityManager().refresh(getInstance());
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Atualização de composição principal realizada com sucesso.");
			
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Atualização não realizada. "+e.getLocalizedMessage());
		} finally {
			exibeMPConfirmacaoOrgaosSemTitulares = false;
		}
		
	}
	
	public void atualizarComposicaoJulgamentoProcessos(){
		try {
			getComposicaoJulgamentoService().atualizarComposicaoJulgamentoProcessos(getInstance());
			getEntityManager().refresh(getInstance());
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Atualização de composição de processos realizada com sucesso.");
		} catch  (PJeBusinessException ex) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Erro ao atualizar composição dos processos. " + ex.getCode());
			ex.printStackTrace();
		}
		catch  (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Erro ao atualizar composição dos processos.");
			e.printStackTrace();
		} finally{
			exibeMPConfirmacaoAtualizacaoComposicaoProcessos = false;			
		}
	}
	
	public String obterNomeRelator(ProcessoTrf processo){
		return ComponentUtil.getComponent(ProcessoMagistradoManager.class).obterNomeRelator(processo);
	}
	
		
	/**
	 * Monta o HTML dos processos que serão exibidos no documento da Ata.
	 * 
	 * @param processosSessao
	 * @return String com HTML.
	 * @throws PJeBusinessException
	 */
	protected String buildHTMLSessao(List<SessaoPautaProcessoTrf> processosSessao) throws PJeBusinessException {
		StringBuilder sb = new StringBuilder();
		
		if (processosSessao != null){
			for (SessaoPautaProcessoTrf processo : processosSessao){
				sb.append(processoSessaoCabecalho(processo));
			}		
		}
		return sb.toString();
	}
	
	@Override
	protected Sessao loadInstance() {

		Sessao sessao = super.loadInstance();
		sessao.getSessaoComposicaoOrdemList().size();
		
		return sessao;
	}
	
	/**
	 * Monta o link padrao para o detalhemento do processo
	 * 
	 * @param idProcessoTrf Id do processoo
	 * @return link
	 */
	public String montarLinkDetalheProcesso(Integer idProcessoTrf) {
		return UrlUtil.montarLinkDetalheProcessoDefault(idProcessoTrf) + "&idSessao=" + this.getSessaoIdSessao();
	}
	
	public Map<String, List<Pessoa>> getDestinatariosNaoIntimadosViaSistema() {
		return destinatariosNaoIntimadosViaSistema;
	}

	public Sessao getUltimaSessaoProcesso() {
		return ultimaSessaoProcesso;
	}

	public void setUltimaSessaoProcesso(Sessao ultimaSessaoProcesso) {
		this.ultimaSessaoProcesso = ultimaSessaoProcesso;
	}
	
	public List<Sessao> getListaSessoesProcurador() {
		return listaSessoesProcurador;
	}

	public void setListaSessoesProcurador(List<Sessao> listaSessoesProcurador) {
		this.listaSessoesProcurador = listaSessoesProcurador;
	}
	
	public String getOrgaosSemTitulares() {
		return orgaosSemTitulares;
	}

	public void setOrgaosSemTitulares(String orgaosSemTitulares) {
		this.orgaosSemTitulares = orgaosSemTitulares;
	}
	
	public boolean isCriandoComposicaoInicial() {
		return criandoComposicaoInicial;
	}

	public void setCriandoComposicaoInicial(boolean criandoComposicaoInicial) {
		this.criandoComposicaoInicial = criandoComposicaoInicial;
	}

	public boolean votoAntecipadoElaborado(ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador){
		SessaoProcessoDocumentoVoto voto = ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class).recuperarVotoAntecipado(processoTrf, orgaoJulgador);
		if(voto != null && voto.getProcessoDocumento() != null 
				&& StringUtils.isNotBlank(voto.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento())){
			return true;
		}
		return false;
	}
	
	/**
	 * Método responsável por verificar se é permitido alterar a ordem da sessão
	 * 
	 * @return <code>Boolean</code>, <code>true</code> caso seja possível
	 *         alterar a ordenação da pauta
	 */
	public boolean podeAlterarOrdenacaoPautaSessao() {
		return getSessaoManager().podeAlterarOrdenacaoPautaSessao(getInstance());
	}
	
	/**
	 * Método responsável por verificar se a pauta da sessão está fechada
	 * 
	 * @return code>Boolean</code>, <code>true</code> caso a pauta da sessão
	 *         esteja fechada.
	 */
	public boolean isPautaFechada() {
		return getSessaoManager().isPautaFechada(getInstance());
	}
	
	
	public List<SugestaoAdiamentoJulgamentoProcessoDTO> getSugestoesAdiamentoJulgamentoProcessoPorAusenciaJulgadoresPrincipais() {
		return sugestoesAdiamentoJulgamentoProcessoPorAusenciaJulgadoresPrincipais;
	}

	public boolean isExibeMPConfirmacaoInclusaoProcessoBlocoPauta() {
		return exibeMPConfirmacaoInclusaoProcessoBlocoPauta;
	}

	public void setExibeMPConfirmacaoInclusaoProcessoBlocoPauta(boolean exibeMPConfirmacaoInclusaoProcessoBlocoPauta) {
		this.exibeMPConfirmacaoInclusaoProcessoBlocoPauta = exibeMPConfirmacaoInclusaoProcessoBlocoPauta;
	}
	
	public void atualizarPresidenteSessaoJulgamento(SessaoComposicaoOrdem sessaoComposicaoOrdem) {
		ComponentUtil.getComponent(SessaoComposicaoOrdemHome.class).atualizaPresidente(sessaoComposicaoOrdem);
		
		if (getInstance().getDataAberturaSessao() != null) {
			FacesMessages.instance().clear();
			this.exibeMPConfirmacaoAtualizacaoPresidenteSessaoIniciada = true;
		} else {
			confirmarAtualizacaoPresidenteSessaoJulgamento(false);
		}
	}
	
	/**
	 * Método responsável por atualizar o Presidente da sessão de julgamento para os processos pautados nela
	 * @param apenasPendentesJulgamento Informa se o sistema deve atualizar o Presidente apenas para os processos pendentes de julgamento
	 */
	public void confirmarAtualizacaoPresidenteSessaoJulgamento(boolean apenasPendentesJulgamento) {
		try {
			getSessaoPautaProcessoTrfManager().atualizarPresidenteSessaoJulgamento(getInstance(), apenasPendentesJulgamento);
			getEntityManager().refresh(getInstance());
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Atualização de Presidente realizada com sucesso.");
		} catch  (PJeBusinessException ex) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Erro ao atualizar o Presidente da sessão de julgamento. " + ex.getCode());
			ex.printStackTrace();
		}
		catch  (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Erro ao atualizar o Presidente da sessão de julgamento.");
			e.printStackTrace();
		} finally{
			exibeMPConfirmacaoAtualizacaoPresidenteSessaoIniciada = false;			
		}
	}
	
	public boolean isExibeMPConfirmacaoAtualizacaoPresidenteSessaoIniciada() {
		return exibeMPConfirmacaoAtualizacaoPresidenteSessaoIniciada;
	}

	public void setExibeMPConfirmacaoAtualizacaoPresidenteSessaoIniciada(
			boolean exibeMPConfirmacaoAtualizacaoPresidenteSessaoIniciada) {
		this.exibeMPConfirmacaoAtualizacaoPresidenteSessaoIniciada = exibeMPConfirmacaoAtualizacaoPresidenteSessaoIniciada;
	}
	
	private SessaoJulgamentoManager getSessaoJulgamentoManager() {
		return ComponentUtil.getComponent(SessaoJulgamentoManager.class);
	}
	
	private SessaoManager getSessaoManager() {
		return ComponentUtil.getComponent(SessaoManager.class);
	}
	
	private SessaoPautaProcessoTrfManager getSessaoPautaProcessoTrfManager() {
		return ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
	}
	
	private ComposicaoJulgamentoService getComposicaoJulgamentoService() {
		return ComponentUtil.getComponent(ComposicaoJulgamentoService.class);
	}

	public boolean isExibeMPConfirmacaoFecharPauta() {
		return exibeMPConfirmacaoFecharPauta;
	}

	public void setExibeMPConfirmacaoFecharPauta(boolean exibeMPConfirmacaoFecharPauta) {
		this.exibeMPConfirmacaoFecharPauta = exibeMPConfirmacaoFecharPauta;
	}

	public List<ProcessoTrf> getProcessosBlocosNaoPautados() {
		return processosBlocosNaoPautados;
	}

	public void setProcessosBlocosNaoPautados(List<ProcessoTrf> processosBlocosNaoPautados) {
		this.processosBlocosNaoPautados = processosBlocosNaoPautados;
	}
	
	public List<OrgaoJulgador> getListaOjByOjc() {
		OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent(OrgaoJulgadorManager.class);
		return orgaoJulgadorManager.obterAtivos(null, getInstance().getOrgaoJulgadorColegiado(), null);
	}

	public String getSessaoObservacao(Integer idSessao) {
		SessaoManager sessaoManager = ComponentUtil.getComponent(SessaoManager.class);
		return sessaoManager.getSessaoObservacao(idSessao);
	}

}