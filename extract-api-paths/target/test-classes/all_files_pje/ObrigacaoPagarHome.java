package br.jus.csjt.pje.view.action;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.itx.component.AbstractHome;
import br.jus.csjt.pje.business.service.ObrigacaoPagarService;
import br.jus.csjt.pje.commons.exception.BusinessException;
import br.jus.pje.jt.entidades.Credor;
import br.jus.pje.jt.entidades.Devedor;
import br.jus.pje.jt.entidades.GrupoEdicao;
import br.jus.pje.jt.entidades.ObrigacaoPagar;
import br.jus.pje.jt.entidades.ParticipanteObrigacao;
import br.jus.pje.jt.entidades.ProcessoJT;
import br.jus.pje.jt.entidades.Rubrica;
import br.jus.pje.jt.entidades.TipoRubrica;
import br.jus.pje.jt.enums.ParticipacaoObrigacaoEnum;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoPericia;

/**
 * Classe para cadastro de obrigacao de pagar
 * 
 * @author Ricardo / Athos
 * @since versao 1.2.0
 * @see PessoaFisica, [PJE-583]
 * @category PJE-JT
 */
@Name(ObrigacaoPagarHome.NAME)
public class ObrigacaoPagarHome extends AbstractHome<ProcessoJT> {

	public static final String NAME = "obrigacaoPagarHome";
	@SuppressWarnings("unused")
	private static final LogProvider log = Logging.getLogProvider(ObrigacaoPagarHome.class);
	private static final long serialVersionUID = 1L;

	private static DecimalFormat decimalFormat = new DecimalFormat("###,##0.00");

	private List<ParticipanteObrigacao> participanteObrigacaoList = new ArrayList<ParticipanteObrigacao>();

	private List<ParticipanteObrigacaoVO> participanteObrigacaoVoList = new ArrayList<ParticipanteObrigacaoVO>();

	private static final ParticipacaoObrigacaoEnum participacaoObrigacaoDevedor = ParticipacaoObrigacaoEnum.D;

	private ObrigacaoPagarService obrigacaoPagarService = new ObrigacaoPagarService();

	private List<Rubrica> rubricaHonorarioList = new ArrayList<Rubrica>();
	private Rubrica rubricaHonorarioTmp;
	private List<Rubrica> rubricaMultaList = new ArrayList<Rubrica>();
	private Rubrica rubricaMultaTmp;
	private List<Rubrica> rubricaOutrosList = new ArrayList<Rubrica>();
	private Rubrica rubricaOutrosTmp;
	private Rubrica valorPrincipal;
	private Rubrica fgtsDepositado;
	private Rubrica juros;
	private Rubrica custas;
	private Rubrica editais;
	private Rubrica inssReclamante;
	private Rubrica inssReclamado;
	private Rubrica emolumentos;
	private Rubrica impostoRenda;
	private Date dataAtualizacao;

	private BigDecimal totalHonorario = new BigDecimal(0);
	private BigDecimal totalMulta = new BigDecimal(0);
	private BigDecimal totalOutros = new BigDecimal(0);

	private List<GrupoEdicao> grupoEdicaoList = new ArrayList<GrupoEdicao>();
	private GrupoEdicao grupoEdicaoTmp;

	private int grupoEdicaoRemoverIndex;

	private List<TipoRubrica> tipoRubricaList;

	@Override
	protected ProcessoJT loadInstance() {
		ProcessoJT processoJt = super.loadInstance();

		carregaTipoRubricaList();
		carregaDadosProcessoJt(processoJt);

		return processoJt;
	}

	public void cargaProcessoJt(ProcessoJT processoJt) {
		if (processoJt == null || processoJt.getProcessoTrf() == null) {
			return;
		}

		setId(processoJt.getIdProcessoJt());
		setInstance(processoJt);
		carregaDadosProcessoJt(processoJt);
	}

	public void carregaProcessoJt() {

		if (getIdObrigacaoPagar() == null) {
			ProcessoTrfHome ptHome = (ProcessoTrfHome) Component.getInstance("processoTrfHome");
			if (ptHome != null && ptHome.getInstance() != null) {
				ProcessoJT processoJt = getEntityManager().find(ProcessoJT.class,
						ptHome.getInstance().getIdProcessoTrf());
				setId(processoJt.getIdProcessoJt());
			}
		}
	}

	private void carregaDadosProcessoJt(ProcessoJT processoJt) {
		for (ProcessoParte autor : processoJt.getProcessoTrf().getListaAutor()) {

			participanteObrigacaoVoList.add(new ParticipanteObrigacaoVO(autor, ParticipacaoObrigacaoEnum.C));
		}

		for (ProcessoParte reu : processoJt.getProcessoTrf().getListaReu()) {

			participanteObrigacaoVoList.add(new ParticipanteObrigacaoVO(reu, ParticipacaoObrigacaoEnum.D));
		}

		for (ProcessoParte advAutor : processoJt.getProcessoTrf().getListaAdvogadosPoloAtivo()) {

			participanteObrigacaoVoList.add(new ParticipanteObrigacaoVO(advAutor, ParticipacaoObrigacaoEnum.C));
		}

		for (ProcessoParte advReu : processoJt.getProcessoTrf().getListaAdvogadosPoloPassivo()) {

			participanteObrigacaoVoList.add(new ParticipanteObrigacaoVO(advReu, ParticipacaoObrigacaoEnum.C));
		}

		for (ProcessoPericia processoPericia : processoJt.getProcessoTrf().getProcessoPericiaList()) {

			participanteObrigacaoVoList.add(new ParticipanteObrigacaoVO(processoPericia, ParticipacaoObrigacaoEnum.C));
		}

		for (ProcessoParte leiloeiro : processoJt.getProcessoTrf().getListaLeiloeiro()) {

			participanteObrigacaoVoList.add(new ParticipanteObrigacaoVO(leiloeiro, ParticipacaoObrigacaoEnum.C));
		}

		limpaParticipantes();
		limpaRubricas();
		recarregaObrigacoes(processoJt);
	}

	private void carregaTipoRubricaList() {
		this.tipoRubricaList = obrigacaoPagarService.carregaTipoRubrica();
	}

	public void incluiParticipante() {
		for (ParticipanteObrigacaoVO participanteObrigacaoVO : participanteObrigacaoVoList) {
			if (participanteObrigacaoVO.getSelecionado() && !participanteObrigacaoVO.getIncluido()) {
				ParticipanteObrigacao participanteObrigacao;

				if (participanteObrigacaoVO.getParticipacao() == ParticipacaoObrigacaoEnum.D) {
					participanteObrigacao = new Devedor();
					((Devedor) participanteObrigacao).setBeneficioOrdem(participanteObrigacaoVO
							.getBeneficioOrdemNumerico());
				} else {
					participanteObrigacao = new Credor();
				}

				participanteObrigacao.setParticipacaoObrigacao(participanteObrigacaoVO.getParticipacao());
				participanteObrigacao.setProcessoParte(participanteObrigacaoVO.getProcessoParte());
				participanteObrigacao.setProcessoPericia(participanteObrigacaoVO.getProcessoPericia());

				participanteObrigacaoVO.setIncluido(true);
				participanteObrigacaoVO.setSelecionado(false);
				participanteObrigacaoVO.setBeneficioOrdem("1");

				participanteObrigacaoList.add(participanteObrigacao);
			}
		}
	}

	public void removeParticipante(int index) {
		ParticipanteObrigacao participanteObrigacao = participanteObrigacaoList.get(index);
		for (ParticipanteObrigacaoVO participanteObrigacaoVO : participanteObrigacaoVoList) {
			if (participanteObrigacaoVO.isParte()) {
				if (participanteObrigacaoVO.getProcessoParte().equals(participanteObrigacao.getProcessoParte())) {
					participanteObrigacaoVO.reset();
					break;
				}
			} else if (participanteObrigacaoVO.isPerito()) {
				if (participanteObrigacaoVO.getProcessoPericia().equals(participanteObrigacao.getProcessoPericia())) {
					participanteObrigacaoVO.reset();
					break;
				}
			}
		}

		participanteObrigacaoList.remove(index);
	}

	public void incluiHonorario() {
		rubricaHonorarioList.add(rubricaHonorarioTmp);
		totalHonorario = totalHonorario.add(rubricaHonorarioTmp.getValor());
		rubricaHonorarioTmp = new Rubrica();
	}

	public void removeHonorario(int index) {
		totalHonorario = totalHonorario.subtract(rubricaHonorarioList.get(index).getValor());
		rubricaHonorarioList.remove(index);
	}

	public void incluiMulta() {
		rubricaMultaList.add(rubricaMultaTmp);
		totalMulta = totalMulta.add(rubricaMultaTmp.getValor());
		rubricaMultaTmp = new Rubrica();
	}

	public void removeMulta(int index) {
		totalMulta = totalMulta.subtract(rubricaMultaList.get(index).getValor());
		rubricaMultaList.remove(index);
	}

	public void incluiOutros() {
		rubricaOutrosList.add(rubricaOutrosTmp);
		totalOutros = totalOutros.add(rubricaOutrosTmp.getValor());
		rubricaOutrosTmp = new Rubrica(getTipoRubrica(TipoRubrica.COD_OUTROS));
	}

	public void removeOutros(int index) {
		totalOutros = totalOutros.subtract(rubricaOutrosList.get(index).getValor());
		rubricaOutrosList.remove(index);
	}

	public void editaGrupoEdicao(int index) {
		limpaParticipantes();
		limpaRubricas();
		grupoEdicaoTmp = grupoEdicaoList.get(index);
		List<Rubrica> merged = new ArrayList<Rubrica>();
		obrigacaoPagarService.obterParticipantesERubricasDoGrupoEdicao(grupoEdicaoTmp, participanteObrigacaoList,
				merged);
		unmergeListasRubricas(merged);
	}

	@Transactional
	public void atualizarGrupoEdicao() {
		try {
			ajustaDataAtualizacaoRubricas();
			obrigacaoPagarService.atualizar(instance, grupoEdicaoTmp, participanteObrigacaoList, mergeListasRubricas());
			grupoEdicaoTmp = null;
			limpaParticipantes();
			limpaRubricas();
			recarregaObrigacoes(this.instance);

			FacesMessages.instance().clearGlobalMessages();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "cadastroObrigacaoPagar.atualizaComSucesso");
		} catch (BusinessException e) {
			// Não faz nada, já que a exceção encarrega-se de mostrar o modal
			// com a mensagem de erro. Este bloco 'catch' apenas garante que o
			// fluxo de chamadas fora deste método não será afetado.
		}
	}

	public void cancelaAtualizacaoGrupoEdicao() {
		grupoEdicaoTmp = null;
		limpaParticipantes();
		limpaRubricas();
	}

	@Transactional
	public void removeGrupoEdicao() {
		obrigacaoPagarService.remover(grupoEdicaoList.get(grupoEdicaoRemoverIndex));
		grupoEdicaoList.remove(grupoEdicaoRemoverIndex);

		FacesMessages.instance().clearGlobalMessages();
		FacesMessages.instance().addFromResourceBundle(Severity.INFO, "cadastroObrigacaoPagar.removeComSucesso");
	}

	@Transactional
	public void gravaObrigacaoPagar() {
		try {
			ajustaDataAtualizacaoRubricas();
			obrigacaoPagarService.gravar(instance, participanteObrigacaoList, mergeListasRubricas());
			limpaParticipantes();
			limpaRubricas();
			recarregaObrigacoes(this.instance);

			FacesMessages.instance().clearGlobalMessages();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "cadastroObrigacaoPagar.gravaComSucesso");
		} catch (BusinessException e) {
			// Não faz nada, já que a exceção encarrega-se de mostrar o modal
			// com a mensagem de erro. Este bloco 'catch' apenas garante que o
			// fluxo de chamadas fora deste método não será afetado.
		}
	}

	public void limpaParticipantes() {
		for (ParticipanteObrigacaoVO participanteObrigacaoVO : participanteObrigacaoVoList) {
			participanteObrigacaoVO.reset();
		}

		participanteObrigacaoList.clear();
	}

	public void limpaRubricas() {
		// Listas de Rubricas
		rubricaHonorarioList.clear();
		rubricaMultaList.clear();
		rubricaOutrosList.clear();

		// Verbas Principais
		valorPrincipal = new Rubrica();
		valorPrincipal.setDataCalculo(null);
		valorPrincipal.setValor(null);
		valorPrincipal.setTipoRubrica(getTipoRubrica(TipoRubrica.COD_VALOR_PRINCIPAL));

		fgtsDepositado = new Rubrica();
		fgtsDepositado.setDataCalculo(null);
		fgtsDepositado.setValor(null);
		fgtsDepositado.setTipoRubrica(getTipoRubrica(TipoRubrica.COD_FGTS_CTA_VINCULADA));

		juros = new Rubrica();
		juros.setDataCalculo(null);
		juros.setValor(null);
		juros.setTipoRubrica(getTipoRubrica(TipoRubrica.COD_JUROS));

		custas = new Rubrica();
		custas.setDataCalculo(null);
		custas.setValor(null);
		custas.setTipoRubrica(getTipoRubrica(TipoRubrica.COD_CUSTAS));

		editais = new Rubrica();
		editais.setDataCalculo(null);
		editais.setValor(null);
		editais.setTipoRubrica(getTipoRubrica(TipoRubrica.COD_EDITAIS));

		inssReclamante = new Rubrica();
		inssReclamante.setDataCalculo(null);
		inssReclamante.setValor(null);
		inssReclamante.setTipoRubrica(getTipoRubrica(TipoRubrica.COD_INSS_RECLAMANTE));

		inssReclamado = new Rubrica();
		inssReclamado.setDataCalculo(null);
		inssReclamado.setValor(null);
		inssReclamado.setTipoRubrica(getTipoRubrica(TipoRubrica.COD_INSS_RECLAMADO));

		emolumentos = new Rubrica();
		emolumentos.setDataCalculo(null);
		emolumentos.setValor(null);
		emolumentos.setTipoRubrica(getTipoRubrica(TipoRubrica.COD_EMOLUMENTOS));

		impostoRenda = new Rubrica();
		impostoRenda.setDataCalculo(null);
		impostoRenda.setValor(null);
		impostoRenda.setTipoRubrica(getTipoRubrica(TipoRubrica.COD_IMPOSTO_RENDA));

		dataAtualizacao = null;

		// Objetos temporarios
		rubricaHonorarioTmp = new Rubrica();
		rubricaMultaTmp = new Rubrica();
		rubricaOutrosTmp = new Rubrica(getTipoRubrica(TipoRubrica.COD_OUTROS));

		// Totais
		totalHonorario = totalHonorario.multiply(BigDecimal.ZERO);
		totalMulta = totalMulta.multiply(BigDecimal.ZERO);
		totalOutros = totalOutros.multiply(BigDecimal.ZERO);
	}

	public void recarregaObrigacoes(ProcessoJT processoJt) {
		grupoEdicaoList = obrigacaoPagarService.obterGrupoEdicao(processoJt);
	}

	private TipoRubrica getTipoRubrica(String codigo) {
		TipoRubrica retorno = null;

		for (TipoRubrica tipoRubrica : tipoRubricaList) {
			if (tipoRubrica.getCodigo().equals(codigo)) {
				retorno = tipoRubrica;
				break;
			}
		}

		return retorno;
	}

	private void ajustaDataAtualizacaoRubricas() {
		valorPrincipal.setDataCalculo(dataAtualizacao);
		fgtsDepositado.setDataCalculo(dataAtualizacao);
		juros.setDataCalculo(dataAtualizacao);
		custas.setDataCalculo(dataAtualizacao);
		editais.setDataCalculo(dataAtualizacao);
		inssReclamante.setDataCalculo(dataAtualizacao);
		inssReclamado.setDataCalculo(dataAtualizacao);
		emolumentos.setDataCalculo(dataAtualizacao);
		impostoRenda.setDataCalculo(dataAtualizacao);

		for (Rubrica multa : rubricaMultaList) {
			multa.setDataCalculo(dataAtualizacao);
		}

		for (Rubrica outro : rubricaOutrosList) {
			outro.setDataCalculo(dataAtualizacao);
		}
	}
	
	/**
	 * [PJEIII-3713] Método responsável por recuperar a data de cálculo da rubrica.
	 */
	private void recuperarDataAtualizacao() {
		if (valorPrincipal != null) {
			this.dataAtualizacao = valorPrincipal.getDataCalculo();
		} else if (fgtsDepositado != null) {
			this.dataAtualizacao = fgtsDepositado.getDataCalculo();
		} else if (juros != null) {
			this.dataAtualizacao = juros.getDataCalculo();
		} else if (custas != null) {
			this.dataAtualizacao = custas.getDataCalculo();
		} else if (editais != null) {
			this.dataAtualizacao = editais.getDataCalculo();
		} else if (inssReclamante != null) {
			this.dataAtualizacao = inssReclamante.getDataCalculo();
		} else if (inssReclamado != null) {
			this.dataAtualizacao = inssReclamado.getDataCalculo();
		} else if (emolumentos != null) {
			this.dataAtualizacao = emolumentos.getDataCalculo();
		} else if (impostoRenda != null) {
			this.dataAtualizacao = impostoRenda.getDataCalculo();
		}
	}

	private List<Rubrica> mergeListasRubricas() {
		List<Rubrica> merge = new ArrayList<Rubrica>();

		adicionaRubrica(merge, valorPrincipal);
		adicionaRubrica(merge, fgtsDepositado);
		adicionaRubrica(merge, juros);
		adicionaRubrica(merge, custas);
		adicionaRubrica(merge, editais);
		adicionaRubrica(merge, inssReclamante);
		adicionaRubrica(merge, inssReclamado);
		adicionaRubrica(merge, emolumentos);
		adicionaRubrica(merge, impostoRenda);

		merge.addAll(rubricaHonorarioList);
		merge.addAll(rubricaMultaList);
		merge.addAll(rubricaOutrosList);

		return merge;
	}

	/**
	 * @author Ricardo Scholz
	 * 
	 *         Adiciona uma rubrica a uma lista de rubricas apenas se a rubrica
	 *         a ser adicionada for válida (não nula e com valor não nulo).
	 * 
	 * @param lista
	 *            lista onde a rubrica será adicionada.
	 * @param rubrica
	 *            rubrica a ser adicionada, caso seja válida.
	 * 
	 * @return <code>true</code> caso a rubrica seja adicionada à lista.
	 */
	private boolean adicionaRubrica(List<Rubrica> lista, Rubrica rubrica) {
		if (rubrica != null && rubrica.getValor() != null) {
			lista.add(rubrica);
			return true;
		}
		return false;
	}

	private void unmergeListasRubricas(List<Rubrica> merged) {

		for (Rubrica rubrica : merged) {
			if (rubrica.getTipoRubrica().getCategoriaRubrica().getDescricao().equalsIgnoreCase("principal")) {
				if (rubrica.getTipoRubrica().getCodigo().equalsIgnoreCase(TipoRubrica.COD_VALOR_PRINCIPAL)) {
					valorPrincipal = rubrica;
				} else if (rubrica.getTipoRubrica().getCodigo().equalsIgnoreCase(TipoRubrica.COD_FGTS_CTA_VINCULADA)) {
					fgtsDepositado = rubrica;
				} else if (rubrica.getTipoRubrica().getCodigo().equalsIgnoreCase(TipoRubrica.COD_JUROS)) {
					juros = rubrica;
				} else if (rubrica.getTipoRubrica().getCodigo().equalsIgnoreCase(TipoRubrica.COD_CUSTAS)) {
					custas = rubrica;
				} else if (rubrica.getTipoRubrica().getCodigo().equalsIgnoreCase(TipoRubrica.COD_EDITAIS)) {
					editais = rubrica;
				} else if (rubrica.getTipoRubrica().getCodigo().equalsIgnoreCase(TipoRubrica.COD_INSS_RECLAMANTE)) {
					inssReclamante = rubrica;
				} else if (rubrica.getTipoRubrica().getCodigo().equalsIgnoreCase(TipoRubrica.COD_INSS_RECLAMADO)) {
					inssReclamado = rubrica;
				} else if (rubrica.getTipoRubrica().getCodigo().equalsIgnoreCase(TipoRubrica.COD_EMOLUMENTOS)) {
					emolumentos = rubrica;
				} else if (rubrica.getTipoRubrica().getCodigo().equalsIgnoreCase(TipoRubrica.COD_IMPOSTO_RENDA)) {
					impostoRenda = rubrica;
				}
			} else if (rubrica.getTipoRubrica().getCategoriaRubrica().getDescricao().equalsIgnoreCase("honorários")) {
				totalHonorario = totalHonorario.add(rubrica.getValor());
				rubricaHonorarioList.add(rubrica);
			} else if (rubrica.getTipoRubrica().getCategoriaRubrica().getDescricao().equalsIgnoreCase("multas")) {
				totalMulta = totalMulta.add(rubrica.getValor());
				rubricaMultaList.add(rubrica);
			} else if (rubrica.getTipoRubrica().getCategoriaRubrica().getDescricao().equalsIgnoreCase("outros")) {
				totalOutros = totalOutros.add(rubrica.getValor());
				rubricaOutrosList.add(rubrica);
			}
		}
	}

	/**
	 * Homologar todas as as obrigações de pagar do processo ativas. Lança
	 * movimento para o processo de sentença de homologação de cálculo.
	 * [PJE-982][PJE-907]
	 * 
	 * @author athos reiser
	 * 
	 * @category PJE-JT
	 * @since 1.4.3
	 * @created 08/12/2011
	 * 
	 */
	public void homologaObrigacaoPagar() {
		obrigacaoPagarService.homologaObrigacaoPagar(getInstance());
		FacesMessages.instance().clearGlobalMessages();
		FacesMessages.instance().addFromResourceBundle(Severity.INFO, "cadastroObrigacaoPagar.homologadoComSucesso");
	}

	/**
	 * Verifica se existe alguma obrigação de pagar para o processo JT que
	 * esteja ativa e não homologada. [PJE-982][PJE-907]
	 * 
	 * @author athos reiser
	 * 
	 * @category PJE-JT
	 * @since 1.4.3
	 * @created 08/12/2011
	 * 
	 * @param processoJT
	 *            processo com as obrigações de pagar a verificar.
	 * 
	 * @return true caso exista uma obrigação de pagar ativa e não homologada,
	 *         false caso todas estejam homologadas ou inativas.
	 * 
	 */
	public Boolean verificaObrigacaoPagarHomologado(ProcessoJT processoJT) {
		if (processoJT == null || processoJT.getObrigacaoPagarList() == null
				|| processoJT.getObrigacaoPagarList().size() < 1) {
			return false;
		}
		
		carregaProcessoJt();
		
		for (ObrigacaoPagar obrigacaoPagar : processoJT.getObrigacaoPagarList()) {
			if (obrigacaoPagar.getAtivo() && !obrigacaoPagar.getHomologado()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Verifica se existe alguma obrigação de pagar para o processo JT 
	 * que esteja ativa e homologada. [PJE-982][PJE-907]
	 * 
	 * @author athos reiser
	 *
	 * @category PJE-JT
	 * @since 1.4.3
	 * @created 08/12/2011
	 * 
	 * @param processoJT processo com as obrigações de pagar a verificar.
	 * 
	 * @return true caso exista uma obrigação de pagar ativa e homologada,
	 * false caso todas estejam não homologadas ou inativas.
	 * 
	 */
	public Boolean verificaExisteObrigacaoPagarHomologada(ProcessoJT processoJT) {
		if (processoJT == null || processoJT.getObrigacaoPagarList() == null 
				|| processoJT.getObrigacaoPagarList().size() < 1) {
			return false;
		}
		
		for (ObrigacaoPagar obrigacaoPagar : getInstance().getObrigacaoPagarList()) {
			if (obrigacaoPagar.getAtivo() && obrigacaoPagar.getHomologado()) {
				return true;
			}
		}
		
		return false;
	}
	
	@Factory(value = "participacaoObrigacaoFactory")
	public ParticipacaoObrigacaoEnum[] getParticipacaoObrigacao() {
		return ParticipacaoObrigacaoEnum.values();
	}

	@Factory(value = "especieHonorarioFactory", scope = ScopeType.APPLICATION)
	public List<TipoRubrica> getEspecieHonorario() {
		return obrigacaoPagarService.carregaTipoRubrica("honorários");
	}

	@Factory(value = "especieMultaFactory", scope = ScopeType.APPLICATION)
	public List<TipoRubrica> getEspecieMulta() {
		return obrigacaoPagarService.carregaTipoRubrica("multas");
	}

	public void setIdObrigacaoPagar(Integer id) {
		setId(id);
	}

	public Integer getIdObrigacaoPagar() {
		return (Integer) getId();
	}

	public List<ParticipanteObrigacao> getParticipanteObrigacaoList() {
		return participanteObrigacaoList;
	}

	public void setParticipanteObrigacaoList(List<ParticipanteObrigacao> participanteObrigacaoList) {
		this.participanteObrigacaoList = participanteObrigacaoList;
	}

	public List<ParticipanteObrigacaoVO> getParticipanteObrigacaoVoList() {
		return participanteObrigacaoVoList;
	}

	public void setParticipanteObrigacaoVoList(List<ParticipanteObrigacaoVO> participanteObrigacaoVoList) {
		this.participanteObrigacaoVoList = participanteObrigacaoVoList;
	}

	public List<Rubrica> getRubricaHonorarioList() {
		return rubricaHonorarioList;
	}

	public void setRubricaHonorarioList(List<Rubrica> rubricaHonorarioList) {
		this.rubricaHonorarioList = rubricaHonorarioList;
	}

	public Rubrica getRubricaHonorarioTmp() {
		return rubricaHonorarioTmp;
	}

	public void setRubricaHonorarioTmp(Rubrica rubricaHonorarioTmp) {
		this.rubricaHonorarioTmp = rubricaHonorarioTmp;
	}

	public List<Rubrica> getRubricaMultaList() {
		return rubricaMultaList;
	}

	public void setRubricaMultaList(List<Rubrica> rubricaMultaList) {
		this.rubricaMultaList = rubricaMultaList;
	}

	public Rubrica getRubricaMultaTmp() {
		return rubricaMultaTmp;
	}

	public void setRubricaMultaTmp(Rubrica rubricaMultaTmp) {
		this.rubricaMultaTmp = rubricaMultaTmp;
	}

	public List<Rubrica> getRubricaOutrosList() {
		return rubricaOutrosList;
	}

	public void setRubricaOutrosList(List<Rubrica> rubricaOutrosList) {
		this.rubricaOutrosList = rubricaOutrosList;
	}

	public Rubrica getRubricaOutrosTmp() {
		return rubricaOutrosTmp;
	}

	public void setRubricaOutrosTmp(Rubrica rubricaOutrosTmp) {
		this.rubricaOutrosTmp = rubricaOutrosTmp;
	}

	public Rubrica getValorPrincipal() {
		return valorPrincipal;
	}

	public void setValorPrincipal(Rubrica valorPrincipal) {
		this.valorPrincipal = valorPrincipal;
	}

	public Rubrica getFgtsDepositado() {
		return fgtsDepositado;
	}

	public void setFgtsDepositado(Rubrica fgtsDepositado) {
		this.fgtsDepositado = fgtsDepositado;
	}

	public Rubrica getJuros() {
		return juros;
	}

	public void setJuros(Rubrica juros) {
		this.juros = juros;
	}

	public Rubrica getCustas() {
		return custas;
	}

	public void setCustas(Rubrica custas) {
		this.custas = custas;
	}

	public Rubrica getEditais() {
		return editais;
	}

	public void setEditais(Rubrica editais) {
		this.editais = editais;
	}

	public Rubrica getInssReclamante() {
		return inssReclamante;
	}

	public void setInssReclamante(Rubrica inssReclamante) {
		this.inssReclamante = inssReclamante;
	}

	public Rubrica getInssReclamado() {
		return inssReclamado;
	}

	public void setInssReclamado(Rubrica inssReclamado) {
		this.inssReclamado = inssReclamado;
	}

	public Rubrica getEmolumentos() {
		return emolumentos;
	}

	public void setEmolumentos(Rubrica emolumentos) {
		this.emolumentos = emolumentos;
	}

	public Rubrica getImpostoRenda() {
		return impostoRenda;
	}

	public void setImpostoRenda(Rubrica impostoRenda) {
		this.impostoRenda = impostoRenda;
	}

	public Date getDataAtualizacao() {
		recuperarDataAtualizacao();
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	public ParticipacaoObrigacaoEnum getParticipacaoObrigacaoDevedor() {
		return participacaoObrigacaoDevedor;
	}

	public BigDecimal getTotalHonorario() {
		return totalHonorario;
	}

	public void setTotalHonorario(BigDecimal totalHonorario) {
		this.totalHonorario = totalHonorario;
	}

	public String getTotalHonorarioString() {
		return getTotalHonorario() != null ? decimalFormat.format(getTotalHonorario().doubleValue()) : "";
	}

	public BigDecimal getTotalMulta() {
		return totalMulta;
	}

	public void setTotalMulta(BigDecimal totalMulta) {
		this.totalMulta = totalMulta;
	}

	public String getTotalMultaString() {
		return getTotalMulta() != null ? decimalFormat.format(getTotalMulta().doubleValue()) : "";
	}

	public BigDecimal getTotalOutros() {
		return totalOutros;
	}

	public void setTotalOutros(BigDecimal totalOutros) {
		this.totalOutros = totalOutros;
	}

	public String getTotalOutrosString() {
		return getTotalOutros() != null ? decimalFormat.format(getTotalOutros().doubleValue()) : "";
	}

	public List<GrupoEdicao> getGrupoEdicaoList() {
		return grupoEdicaoList;
	}

	public void setGrupoEdicaoList(List<GrupoEdicao> grupoEdicaoList) {
		this.grupoEdicaoList = grupoEdicaoList;
	}

	public GrupoEdicao getGrupoEdicaoTmp() {
		return grupoEdicaoTmp;
	}

	public void setGrupoEdicaoTmp(GrupoEdicao grupoEdicaoTmp) {
		this.grupoEdicaoTmp = grupoEdicaoTmp;
	}

	public int getGrupoEdicaoRemoverIndex() {
		return grupoEdicaoRemoverIndex;
	}

	public void setGrupoEdicaoRemoverIndex(int grupoEdicaoRemoverIndex) {
		this.grupoEdicaoRemoverIndex = grupoEdicaoRemoverIndex;
	}
}