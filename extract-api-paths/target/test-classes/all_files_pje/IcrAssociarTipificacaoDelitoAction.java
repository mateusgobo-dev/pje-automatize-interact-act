package br.com.infox.cliente.home.icrrefactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.seam.international.StatusMessage.Severity;
import org.richfaces.component.UITree;

import br.jus.pje.nucleo.entidades.ConcursoCrime;
import br.jus.pje.nucleo.entidades.DispositivoNorma;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.NormaPenal;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.TipificacaoDelito;
import br.jus.pje.nucleo.enums.UsoDispositivoEnum;

public abstract class IcrAssociarTipificacaoDelitoAction<T extends InformacaoCriminalRelevante, J extends InformacaoCriminalRelevanteManager<T>>
		extends IcrAssociarTransitoEmJulgadoAction<T, J> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3226551832589783714L;

	private TipificacaoDelito tipificacaoDelito;

	// cópia de tipificacao
	private ProcessoParte processoParteCopia;
	private List<ProcessoParte> processoParteCopiaList;
	private T icrCopia;
	private List<T> icrCopiaList;

	// concurso de crime
	private ConcursoCrime concursoCrime;
	private Map<TipificacaoDelito, Boolean> tipificacoesNaoAgrupadas = new HashMap<TipificacaoDelito, Boolean>();

	// pesquisa de delitos na tree normaPenalTreee
	private String tipoItemPesquisaNormaPenal = IcrAssociarTipificacaoDelitoManager.TIPO_ITEM_PESQUISA_NORMA_PENAL_NORMA;
	private String criterioPesquisaNormaPenal;
	private DispositivoNorma dispositivoNormaSelecionado;

	private NormaPenal normaPenalModal;
	private DispositivoNorma dispositivoNormaModal;
	private boolean mostrarPainelPesquisaDelitos;
	private boolean vinculandoNormaExtensao;

	private List<DispositivoNorma> dispositivosFiltradosNaArvore;

	// tree
	private List<NormaPenal> normaPenalTreeRoots;
	private Map<NormaPenal, List<DispositivoNorma>> normaPenalTreeDispositivoNormaRoots = new HashMap<NormaPenal, List<DispositivoNorma>>();
	private Map<DispositivoNorma, List<DispositivoNorma>> normaPenalTreeDispositivoNormaChildren = new HashMap<DispositivoNorma, List<DispositivoNorma>>();

	@Override
	public void init() {
		super.init();
		if (exigeTipificacaoDelito()) {
			inicializaTipificacaoDelito();
		}
	}

	protected void inicializaTipificacaoDelito() {
		newTipificacaoDelito();
		newConcursoCrime();
		pesquisaNormaPenalTreeRoots();
		processoParteCopiaList = getManager()
				.recuperarReusComTipificacaoCadastradaList(
						getHome().getProcessoTrf());

		if (isManaged()) {
			/*
			 * caso a lista de icrs canditadas para cópia contiver somente um
			 * item, ou seja, a icr editada, remover da lista de processoParte a
			 * parte associada a icr editada
			 */
			if (getManager().recuperarIcrCopiaList(
					getInstance().getProcessoParte()).size() == 1) {
				processoParteCopiaList.remove(getInstance().getProcessoParte());
			}
		}
	}

	public String getTipoItemPesquisaNormaPenal() {
		return tipoItemPesquisaNormaPenal;
	}

	public void setTipoItemPesquisaNormaPenal(String tipoItemPesquisaNormaPenal) {
		this.tipoItemPesquisaNormaPenal = tipoItemPesquisaNormaPenal;
	}

	public String getCriterioPesquisaNormaPenal() {
		return criterioPesquisaNormaPenal;
	}

	public void setCriterioPesquisaNormaPenal(String criterioPesquisaNormaPenal) {
		this.criterioPesquisaNormaPenal = criterioPesquisaNormaPenal;
	}

	public DispositivoNorma getDispositivoNormaSelecionado() {
		return dispositivoNormaSelecionado;
	}

	public void setDispositivoNormaSelecionado(
			DispositivoNorma dispositivoNormaSelecionado) {
		this.dispositivoNormaSelecionado = dispositivoNormaSelecionado;
	}

	public NormaPenal getNormaPenalModal() {
		return normaPenalModal;
	}

	public void setNormaPenalModal(NormaPenal normaPenalModal) {
		this.normaPenalModal = normaPenalModal;
		this.dispositivoNormaModal = null;
	}

	public DispositivoNorma getDispositivoNormaModal() {
		return dispositivoNormaModal;
	}

	public void setDispositivoNormaModal(DispositivoNorma dispositivoNormaModal) {
		this.dispositivoNormaModal = dispositivoNormaModal;
		this.normaPenalModal = null;
	}

	public boolean getMostrarPainelPesquisaDelitos() {
		return mostrarPainelPesquisaDelitos;
	}

	public void setMostrarPainelPesquisaDelitos(
			boolean mostrarPainelPesquisaDelitos) {
		this.mostrarPainelPesquisaDelitos = mostrarPainelPesquisaDelitos;
	}

	public void combinarNormaExtensao() {
		vinculandoNormaExtensao = true;
		limparCriteriosPesquisaNormaPenal(true);
	}

	public void combinarDelito() {
		mostrarPainelPesquisaDelitos = true;
		vinculandoNormaExtensao = false;
	}

	public void setDispositivoNorma(DispositivoNorma dispositivoNorma) {
		try {
			if (!vinculandoNormaExtensao) {
				if (!tipificacaoDelito.getDelito().contains(dispositivoNorma)) {
					tipificacaoDelito.getDelito().add(dispositivoNorma);
				}
			} else {
				addNormaExtencao(dispositivoNorma);
			}
			setMostrarPainelPesquisaDelitos(true);
		} catch (IcrValidationException e) {
			addMessage(Severity.ERROR, e.getMessage(), e);

		}
	}

	public ProcessoParte getProcessoParteCopia() {
		return processoParteCopia;
	}

	public void setProcessoParteCopia(ProcessoParte processoParteCopia) {
		this.processoParteCopia = processoParteCopia;
	}

	public List<ProcessoParte> getProcessoParteCopiaList() {
		return processoParteCopiaList;
	}

	public void setProcessoParteCopiaList(
			List<ProcessoParte> processoParteCopiaList) {
		this.processoParteCopiaList = processoParteCopiaList;
	}

	public List<T> getIcrCopiaList() {
		return icrCopiaList;
	}

	public void setIcrCopiaList(List<T> icrCopiaList) {
		this.icrCopiaList = icrCopiaList;
	}

	public T getIcrCopia() {
		return icrCopia;
	}

	public void setIcrCopia(T icrCopia) {
		this.icrCopia = icrCopia;
	}

	public TipificacaoDelito getTipificacaoDelito() {
		return tipificacaoDelito;
	}

	public void setTipificacaoDelito(TipificacaoDelito tipificacaoDelito) {
		this.tipificacaoDelito = tipificacaoDelito;

	}

	public ConcursoCrime getConcursoCrime() {
		return concursoCrime;
	}

	public void setConcursoCrime(ConcursoCrime concursoCrime) {
		this.concursoCrime = concursoCrime;
	}

	public Map<TipificacaoDelito, Boolean> getTipificacoesNaoAgrupadas() {
		return tipificacoesNaoAgrupadas;

	}

	public void setTipificacoesNaoAgrupadas(
			Map<TipificacaoDelito, Boolean> tipificacoesNaoAgrupadas) {
		this.tipificacoesNaoAgrupadas = tipificacoesNaoAgrupadas;
	}

	/*************************************************** CÓPIA TIPIFICAÇÃO ***********************************/

	public void limparCriteriosCopiaTipificacao() {
		setProcessoParteCopia(null);
		setIcrCopia(null);
	}

	public void recuperarIcrCopiaList() {
		icrCopiaList = getManager().recuperarIcrCopiaList(
				getProcessoParteCopia());

		if (isManaged()) {
			// remover da lista de icrs candidatas para a cópia, a própria icr
			icrCopiaList.remove(getInstance());
		}
	}

	public void copiarUltimaTipificacao() {
		setProcessoParteCopia(getHome().getInstance().getProcessoParte());
		setIcrCopia(getManager().recuperarIcrUltimaTipificacao(
				getProcessoParteCopia()));
		if (getIcrCopia() == null) {
			addMessage(Severity.INFO,
					"tipificacaoDelito.nehum_registro_encontrado", null);
		}
	}

	public void copiarTodasTipificacoesDelito() {
		try {
			// validar todas antes da copia
			for (TipificacaoDelito tipificacaoDelito : getIcrCopia()
					.getTipificacoes()) {
				try {
					getManager().validarTipificacaoDelito(tipificacaoDelito,
							getInstance());
				} catch (IcrValidationException e) {
					addMessage(Severity.ERROR,
							"Operação Abortada, a Tipificação "
									+ tipificacaoDelito.getDelitoString()
									+ " Não pode ser copiada devido ao erro:",
							null);
					throw e;
				}
			}

			for (TipificacaoDelito tipificacaoDelito : getIcrCopia()
					.getTipificacoesNaoAgrupadas()) {
				copiarTipificacaoDelito(tipificacaoDelito, getInstance());
			}

			for (ConcursoCrime concursoCrime : getIcrCopia().getAllConcursos()) {
				copiarConcursoCrime(concursoCrime, getInstance());
			}
		} catch (IcrValidationException e) {
			addMessage(Severity.ERROR, e.getMessage(), e);
		}

		// addMessage(Severity.INFO, "Delito(s) copiado(s) com sucesso!", null);
	}

	public ConcursoCrime copiarConcursoCrime(ConcursoCrime concursoCrime,
			T owner) throws IcrValidationException {
		ConcursoCrime concursoCrimeNew = new ConcursoCrime();
		concursoCrimeNew.setTipoAgrupamento(concursoCrime.getTipoAgrupamento());

		for (TipificacaoDelito tipificacaoDelito : concursoCrime
				.getTipificacoes()) {
			try {
				getManager().validarTipificacaoDelito(tipificacaoDelito,
						getInstance());
			} catch (IcrValidationException e) {
				addMessage(Severity.ERROR, "Operação Abortada, a Tipificação "
						+ tipificacaoDelito.getDelitoString()
						+ " Não pode ser copiada devido ao erro:", null);
				throw e;
			}
		}

		for (TipificacaoDelito tipificacaoDelito : concursoCrime
				.getTipificacoes()) {
			TipificacaoDelito copia = copiarTipificacaoDelito(
					tipificacaoDelito, owner);
			concursoCrimeNew.getTipificacoes().add(copia);
			copia.getConcursos().add(concursoCrimeNew);
		}
		return concursoCrimeNew;
	}

	public TipificacaoDelito copiarTipificacaoDelito(
			TipificacaoDelito tipificacaoDelito, T owner)
			throws IcrValidationException {
		getManager().validarTipificacaoDelito(tipificacaoDelito, owner);
		TipificacaoDelito tipificacaoDelitoNew = new TipificacaoDelito();

		tipificacaoDelitoNew.getDelito().addAll(tipificacaoDelito.getDelito());
		tipificacaoDelitoNew.setDataDelito(tipificacaoDelito.getDataDelito());
		tipificacaoDelitoNew.setDataDesconhecida(tipificacaoDelito
				.getDataDesconhecida());
		tipificacaoDelitoNew.getCombinacoes().addAll(
				tipificacaoDelito.getCombinacoes());
		tipificacaoDelitoNew.setInformacaoCriminalRelevante(owner);
		tipificacaoDelitoNew
				.setNumeroReferencia(owner.getTipificacoes().size() + 1);
		tipificacaoDelitoNew.setObservacao(tipificacaoDelito.getObservacao());
		tipificacaoDelitoNew.setQuantidadeIncidencia(tipificacaoDelito
				.getQuantidadeIncidencia());
		tipificacaoDelitoNew.setTipoConsumacaoDelito(tipificacaoDelito
				.getTipoConsumacaoDelito());

		owner.getTipificacoes().add(tipificacaoDelitoNew);

		return tipificacaoDelitoNew;

	}

	// utilizado na toolbar de cópia
	public void copiarTipificacaoDelito(TipificacaoDelito tipificacaoDelito) {
		try {
			copiarTipificacaoDelito(tipificacaoDelito, getInstance());
		} catch (IcrValidationException e) {
			addMessage(Severity.ERROR, e.getMessage(), e);
		}
	}

	// utilizado na toolbar de cópia
	public void copiarConcursos(TipificacaoDelito tipificacaoDelito) {
		try {
			for (ConcursoCrime concurso : tipificacaoDelito.getConcursos()) {
				copiarConcursoCrime(concurso, getInstance());
			}
		} catch (IcrValidationException e) {
			addMessage(Severity.ERROR, e.getMessage(), e);
		}
	}

	/*************************************************** DEFINIR CONCURSO ************************************/

	private void newConcursoCrime() {
		concursoCrime = new ConcursoCrime();
	}

	public ConcursoCrime.TipoAgrupamento[] getTipoAgrupamentoItems() {
		return ConcursoCrime.TipoAgrupamento.values();
	}

	public void agrupar() {
		List<TipificacaoDelito> tipificacoesSelecionadas = getTipificacoesSelecionadas();
		if (!tipificacoesSelecionadas.isEmpty()) {
			for (TipificacaoDelito tipificacaoSelecionada : tipificacoesSelecionadas) {
				if (tipificacoesSelecionadas.size() == 1) {
					if (tipificacaoSelecionada.getQuantidadeIncidencia() == 1) {
						addMessage(
								Severity.ERROR,
								"tipificacaoDelito.concursoCrime.marcarcao_concurso_invalida",
								null);
						return;
					}
				}

				concursoCrime.getTipificacoes().add(tipificacaoSelecionada);
				tipificacaoSelecionada.getConcursos().add(concursoCrime);
			}

			// addMessage(Severity.INFO,
			// "Delito(s) Agrupado(s) com Sucesso!",null);
		} else {
			addMessage(Severity.ERROR, "Nenhum Delito Selecionado!", null);
		}
		newConcursoCrime();
	}

	public void desagrupar(ConcursoCrime concursoCrime) {
		for (TipificacaoDelito item : concursoCrime.getTipificacoes()) {
			item.getConcursos().remove(concursoCrime);
		}
		concursoCrime.getTipificacoes().clear();
		// addMessage(Severity.INFO, "Delito(s) Desagrupado(s) com Sucesso!",
		// null);
	}

	private List<TipificacaoDelito> getTipificacoesSelecionadas() {
		List<TipificacaoDelito> tipificacoesSelecionadas = new ArrayList<TipificacaoDelito>(
				0);
		for (TipificacaoDelito item : tipificacoesNaoAgrupadas.keySet()) {
			if (tipificacoesNaoAgrupadas.get(item)) {
				tipificacoesSelecionadas.add(item);
			}
		}
		tipificacoesNaoAgrupadas.clear();
		return tipificacoesSelecionadas;
	}

	public void preparaTipificacoesNaoAgrupadas() {
		if (!(getInstance().getTipificacoes() != null)
				&& !(getInstance().getTipificacoesNaoAgrupadas() != null)) {
			tipificacoesNaoAgrupadas.clear();
			for (TipificacaoDelito item : getInstance()
					.getTipificacoesNaoAgrupadas()) {
				tipificacoesNaoAgrupadas.put(item, false);
			}
		}
	}

	/******************************************************* CADASTRO ****************************************/

	public void newTipificacaoDelito() {
		tipificacaoDelito = new TipificacaoDelito();
		limparCriteriosPesquisaNormaPenal(false);
	}

	public void remove(TipificacaoDelito tipificacaoDelito) {
		getInstance().getTipificacoes().remove(tipificacaoDelito);
		// atualizar referencias
		int i = 1;
		for (TipificacaoDelito item : getInstance().getTipificacoes()) {
			item.setNumeroReferencia(i);
			i++;
		}
		
		// remover combinacoes
		tipificacaoDelito.getDelito().clear();
		tipificacaoDelito.getCombinacoes().clear();

		// remover concursos
		List<TipificacaoDelito> tipificacoesParaRemoverConcursos = new ArrayList<TipificacaoDelito>(
				0);
		for (ConcursoCrime concurso : tipificacaoDelito.getConcursos()) {
			for (TipificacaoDelito item : concurso.getTipificacoes()) { //
				// guardar as tipificacoes para remocao posterior (remover aqui
				// causa ConcurrentModificationException)
				tipificacoesParaRemoverConcursos.add(item);
			}
			concurso.getTipificacoes().clear();
		}
		for (TipificacaoDelito item : tipificacoesParaRemoverConcursos) {
			item.getConcursos().clear();
		}
		// addMessage(Severity.INFO, "TipificacaoDelito_deleted", null);
	}

	public TipificacaoDelito.TipoConsumacaoDelito[] getTipoConsumacaoDelitoItems() {
		return TipificacaoDelito.TipoConsumacaoDelito.values();
	}

	public void limparDelito() {
		tipificacaoDelito.getDelito().clear();
		tipificacaoDelito.getCombinacoes().clear();
		limparCriteriosPesquisaNormaPenal(false);
	}

	public List<TipificacaoDelito> getTipificacoesAssociadas() {
		return getInstance().getTipificacoes();
	}

	public boolean isSelecionavel(DispositivoNorma dispositivoNorma) {
		if (dispositivoNorma != null) {
			if (!vinculandoNormaExtensao) {
				if (dispositivoNorma.getUsoDispositivo() == UsoDispositivoEnum.TP) {
					if (getTipificacaoDelito().getDelito().isEmpty()) {
						return true;
					} else {
						DispositivoNorma delitoPrincipal =getTipificacaoDelito()
								.getDelito().get(0); 
						DispositivoNorma dispositivoPai = delitoPrincipal.getDispositivoNormaPai();
						NormaPenal normaPenal = delitoPrincipal.getNormaPenal();
						return dispositivoNorma.getNormaPenal() == normaPenal
								&& dispositivoNorma.getDispositivoNormaPai() == dispositivoPai
								&& dispositivoNorma.getPermitirAssociacaoMultipla();
					}
				}
			} else {
				return dispositivoNorma.getUsoDispositivo() == UsoDispositivoEnum.NE;
			}
		}
		return false;
	}

	public void addTipificacao(TipificacaoDelito tipificacaoDelito) {
		try {
			getManager().validarTipificacaoDelito(tipificacaoDelito,
					getInstance());
			if (!getInstance().getTipificacoes().contains(tipificacaoDelito)) {
				tipificacaoDelito.setNumeroReferencia(getInstance()
						.getTipificacoes().size() + 1);
				tipificacaoDelito.setInformacaoCriminalRelevante(getInstance());
				getInstance().getTipificacoes().add(tipificacaoDelito);
			}
			preparaTipificacoesNaoAgrupadas();
			newTipificacaoDelito();
			// addMessage(Severity.INFO, "Operação realizada com sucesso!",
			// null);

		} catch (IcrValidationException e) {
			addMessage(Severity.ERROR, e.getMessage(), e);
		}

	}

	private void addNormaExtencao(DispositivoNorma dispositivoNorma)
			throws IcrValidationException {
		getManager().validarAdicaoNormaExtencao(dispositivoNorma,
				tipificacaoDelito);
		tipificacaoDelito.getCombinacoes().add(dispositivoNorma);
		// addMessage(Severity.INFO, "tipificacaoDelito.item_inserido", null);
	}

	public boolean exigeTipificacaoDelito() {
		return getInstance().getTipo().exigeTipificacaoDelito();
	}

	@Override
	public boolean exibirBotaoIncluir() {
		if (exigeTipificacaoDelito()) {
			return !isManaged()
					&& !getHome().getTab().equals(
							InformacaoCriminalRelevanteHome.TAB_FORMULARIO_ID);
		} else {
			return super.exibirBotaoIncluir();
		}
	}

	@Override
	public boolean exibirBotaoProximoPasso() {
		if (exigeTipificacaoDelito()) {
			return !isManaged()
					&& getHome().getTab().equals(
							InformacaoCriminalRelevanteHome.TAB_FORMULARIO_ID);
		} else {
			return super.exibirBotaoProximoPasso();
		}
	}

	public void limparCriteriosPesquisaNormaPenal(boolean showModal) {
		dispositivosFiltradosNaArvore = null;
		setTipoItemPesquisaNormaPenal(IcrAssociarTipificacaoDelitoManager.TIPO_ITEM_PESQUISA_NORMA_PENAL_NORMA);
		setCriterioPesquisaNormaPenal(null);
		mostrarPainelPesquisaDelitos = showModal;
	}

	private void pesquisaNormaPenalTreeRoots() {
		normaPenalTreeRoots = getManager().recuperarNormaPenalTreeRoots();
	}

	public List<NormaPenal> getNormaPenalTreeRoots() {
		return normaPenalTreeRoots;
	}

	public void pesquisarNormaPenalTreeDispositivoNormaRoots(
			NormaPenal normaPenal) {
		normaPenalTreeDispositivoNormaRoots.put(normaPenal, getManager()
				.recuperarDispositivoNormaRoots(normaPenal));
	}

	public List<DispositivoNorma> getNormaPenalTreeDispositivoNormaRoots(
			NormaPenal normaPenal) {
		if (!normaPenalTreeDispositivoNormaRoots.containsKey(normaPenal)) {
			pesquisarNormaPenalTreeDispositivoNormaRoots(normaPenal);
		}
		return normaPenalTreeDispositivoNormaRoots.get(normaPenal);
	}

	public void pesquisarNormaPenalTreeDispositovNormaChildren(
			DispositivoNorma dispositivoNormaPai) {
		normaPenalTreeDispositivoNormaChildren.put(
				dispositivoNormaPai,
				getManager().recuperarDispositivoNormaChildren(
						dispositivoNormaPai));
	}

	public List<DispositivoNorma> getNormaPenalTreeDispositivoNormaChildren(
			DispositivoNorma dispositivoNormaPai) {
		if (!normaPenalTreeDispositivoNormaChildren
				.containsKey(dispositivoNormaPai)) {
			pesquisarNormaPenalTreeDispositovNormaChildren(dispositivoNormaPai);
		}
		return normaPenalTreeDispositivoNormaChildren.get(dispositivoNormaPai);
	}

	public void pesquisarDispositivos() {
		dispositivosFiltradosNaArvore = null;
	}

	public List<DispositivoNorma> getDispositivosFiltradosNaArvore() {
		if (dispositivosFiltradosNaArvore == null) {
			if (getCriterioPesquisaNormaPenal() != null
					&& !getCriterioPesquisaNormaPenal().trim().isEmpty()) {
				dispositivosFiltradosNaArvore = getManager()
						.pesquisarDispositivos(getTipoItemPesquisaNormaPenal(),
								getCriterioPesquisaNormaPenal());
			}
		}
		return dispositivosFiltradosNaArvore;
	}

	private boolean possuiParentesco(DispositivoNorma possivelPai,
			DispositivoNorma possivelFilho) {
		if (possivelPai.getDispositivoNormaList() != null) {
			if (possivelPai.getDispositivoNormaList().contains(possivelFilho)) {
				return true;
			} else {
				if (possivelFilho.getDispositivoNormaPai() != null) {
					return possuiParentesco(possivelPai,
							possivelFilho.getDispositivoNormaPai());
				}
			}
		}
		return false;
	}

	public Boolean adviseNodeSeleted(UITree tree) {
		Object node = tree.getRowData();
		if (getCriterioPesquisaNormaPenal() != null
				&& !getCriterioPesquisaNormaPenal().isEmpty()) {
			if (node instanceof NormaPenal) {
				NormaPenal normaPenal = (NormaPenal) node;
				Pattern pattern = Pattern.compile("[0-9]{0,9}+");
				Matcher matcher = pattern
						.matcher(getCriterioPesquisaNormaPenal());
				boolean isNumero = matcher.matches();
				if (getTipoItemPesquisaNormaPenal()
						.equals(IcrAssociarTipificacaoDelitoManager.TIPO_ITEM_PESQUISA_NORMA_PENAL_NORMA)
						&& isNumero) {
					if (normaPenal.getNrNorma().equals(
							Integer.parseInt(getCriterioPesquisaNormaPenal()))) {
						return true;
					}
				}
			}
			if (node instanceof DispositivoNorma) {
				DispositivoNorma dispositivoNorma = (DispositivoNorma) node;
				if (getDispositivoNormaSelecionado() != null) {
					return getDispositivoNormaSelecionado().equals(
							dispositivoNorma);
				}
			}
		}
		return false;
	}

	public Boolean adviseNodeOpened(UITree tree) {
		Object node = tree.getRowData();
		if (getCriterioPesquisaNormaPenal() != null
				&& !getCriterioPesquisaNormaPenal().isEmpty()) {
			List<DispositivoNorma> dispositivosFiltrados = getDispositivosFiltradosNaArvore();
			if (node instanceof NormaPenal) {
				NormaPenal normaPenal = (NormaPenal) node;
				if (dispositivosFiltrados != null) {
					for (DispositivoNorma dispositivoNorma : dispositivosFiltrados) {
						if (dispositivoNorma.getNormaPenal().getIdNormaPenal()
								.equals(normaPenal.getIdNormaPenal())) {
							return true;
						}
					}
				}
			}
			if (node instanceof DispositivoNorma) {
				if (dispositivosFiltrados != null) {
					DispositivoNorma dispositivoNorma = (DispositivoNorma) node;
					for (DispositivoNorma dispositivoNormaItem : dispositivosFiltrados) {
						if (possuiParentesco(dispositivoNorma,
								dispositivoNormaItem)) {
							return true;
						}
					}
				}
			}
		}
		return null;
	}

	public String getDescricaoHediondo(DispositivoNorma dispositivoNorma) {
		String returnValue = "Não";
		if (dispositivoNorma.getInHediondo()) {
			returnValue = "Sim (a partir de "
					+ new SimpleDateFormat("dd/MM/yyyy")
							.format(dispositivoNorma.getDtHediondo()) + ")";
		}
		return returnValue;
	}

	public String getDescricaoPenaMinima(DispositivoNorma dispositivoNorma) {
		String returnValue = "";
		if (dispositivoNorma.getNrPenaMinimaAnos() != null) {
			returnValue += dispositivoNorma.getNrPenaMinimaAnos() + " anos";
		}
		if (dispositivoNorma.getNrPenaMinimaMeses() != null) {
			if (!returnValue.isEmpty()) {
				returnValue += ", ";
			}
			returnValue += dispositivoNorma.getNrPenaMinimaMeses() + " meses";
		}
		if (dispositivoNorma.getNrPenaMinimaDias() != null) {
			if (!returnValue.isEmpty()) {
				returnValue += ", ";
			}
			returnValue += dispositivoNorma.getNrPenaMinimaDias() + " dias";
		}
		return returnValue;
	}

	public String getDescricaoPenaMaxima(DispositivoNorma dispositivoNorma) {
		String returnValue = "";
		if (dispositivoNorma.getNrPenaMaximaAnos() != null) {
			returnValue += dispositivoNorma.getNrPenaMaximaAnos() + " anos";
		}
		if (dispositivoNorma.getNrPenaMaximaMeses() != null) {
			if (!returnValue.isEmpty()) {
				returnValue += ", ";
			}
			returnValue += dispositivoNorma.getNrPenaMaximaMeses() + " meses";
		}
		if (dispositivoNorma.getNrPenaMaximaDias() != null) {
			if (!returnValue.isEmpty()) {
				returnValue += ", ";
			}
			returnValue += dispositivoNorma.getNrPenaMaximaDias() + " dias";
		}
		return returnValue;
	}

	@Override
	public void novo() {
		getIcrList().clear();
		super.novo();
	}
}