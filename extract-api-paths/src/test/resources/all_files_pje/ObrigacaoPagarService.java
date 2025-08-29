package br.jus.csjt.pje.business.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.AbstractHome;
import br.jus.csjt.pje.commons.exception.BusinessException;
import br.jus.pje.jt.entidades.Credor;
import br.jus.pje.jt.entidades.CredorRubrica;
import br.jus.pje.jt.entidades.Devedor;
import br.jus.pje.jt.entidades.GrupoEdicao;
import br.jus.pje.jt.entidades.ObrigacaoAtomica;
import br.jus.pje.jt.entidades.ObrigacaoPagar;
import br.jus.pje.jt.entidades.ParticipanteObrigacao;
import br.jus.pje.jt.entidades.ProcessoJT;
import br.jus.pje.jt.entidades.Rubrica;
import br.jus.pje.jt.entidades.TipoRubrica;
import br.jus.pje.jt.enums.OrdemCreditoEnum;
import br.jus.pje.jt.enums.ParticipacaoObrigacaoEnum;
import br.jus.pje.jt.enums.TipoCredorEnum;

/**
 * Classe responsavel pelos servicos de validacao e persistencia das obrigacoes
 * de pagar.
 * 
 * @author Rafael Carvalho | Tiago Zanon
 * 
 * @category PJE-JT
 * @since 1.4.2
 * @created 29/09/2011
 * 
 */
@Name(ObrigacaoPagarService.COMPONENT_NAME)
@Scope(ScopeType.EVENT)
public class ObrigacaoPagarService extends AbstractHome<ObrigacaoPagar> implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String COMPONENT_NAME = "obrigacaoPagarService";

	EntityManager em = getEntityManager();

	/**
	 * Faz validacao de participantes em relacao as rubricas associadas a eles.
	 * 
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 29/09/2011
	 * 
	 * @param participantes
	 * @param rubricas
	 * @throws BusinessException
	 */
	public void validarRubricasParaParticipantes(List<ParticipanteObrigacao> participantes, List<Rubrica> rubricas)
			throws BusinessException {

		validarRubricas(rubricas);

		verificaSePossuiDevedor(participantes);

		verificaRubricasQueExigeCredor(rubricas, participantes);

		for (ParticipanteObrigacao participante : participantes) {
			if (!verificaRubricasRequeridasPorParticipante(rubricas, participante)) {
				Credor credor = (Credor) participante;

				// [PJE-905] Tiago Zanon - 16/11/2011
				if (credor.isPerito() || credor.getTipoCredor().equals(TipoCredorEnum.G)
						|| credor.getTipoCredor().equals(TipoCredorEnum.L)) { // se
																				// for
																				// perito,
																				// advogado
																				// ou
																				// leiloeiro
					throw new BusinessException("obrigacaoPagarService.credorPeritoExigeHonorarios");
				}
				// PJE-FIM

				throw new BusinessException("obrigacaoPagarService.rubricaNaoInformadaParaCredor", credor.getNome());
			}
		}

	}

	/**
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @param rubricas
	 * @throws BusinessException
	 */
	private void validarRubricas(List<Rubrica> rubricas) throws BusinessException {
		if (rubricas.isEmpty()) {
			throw new BusinessException("obrigacaoPagarService.semRubricas");
		}

		for (Rubrica rubrica : rubricas) {

			if (rubrica.getValor() == null) {
				throw new BusinessException("obrigacaoPagarService.rubricaComValorNulo");
			}

			// [PJE-903] Tiago Zanon - 16/11/2011
			if (rubrica.getDataCalculo() == null) {
				throw new BusinessException("obrigacaoPagarService.rubricaSemData", rubrica.getTipoRubrica()
						.getDescricao());
			}
			// PJE-FIM
		}
	}

	/**
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 29/09/2011
	 * 
	 * @param rubricas
	 * @param participantes
	 */
	private void verificaRubricasQueExigeCredor(List<Rubrica> rubricas, List<ParticipanteObrigacao> participantes)
			throws BusinessException {

		boolean possuiCredor = verificaSePossuiCredor(participantes);

		if (possuiCredor) {
			Credor credor;
			boolean achouTipoCredor = false;

			for (Rubrica rubrica : rubricas) {

				if (rubrica.getTipoRubrica().getExigeCredor()) {
					achouTipoCredor = false;

					// verificar se o TipoCredor exigido pela rubrica existe
					for (ParticipanteObrigacao participanteObrigacao : participantes) {

						if (participanteObrigacao instanceof Credor) {
							credor = (Credor) participanteObrigacao;

							achouTipoCredor = false;

							for (CredorRubrica credorRubrica : rubrica.getTipoRubrica().getCredorRubricaList()) {

								if (credorRubrica.getTipoCredor().equals(credor.getTipoCredor())) {
									achouTipoCredor = true;
									break;
								}
							}
						}

						if (achouTipoCredor) {
							break;
						}
					}

					if (!achouTipoCredor) {
						throw new BusinessException("obrigacaoPagarService.rubricaExigeCredor", rubrica
								.getTipoRubrica().getDescricao(), rubrica.getTipoRubrica().getTipoCredorFormatado());
					}
				}
			}
		} else {

			for (Rubrica rubrica : rubricas) {
				if (rubrica.getTipoRubrica().getExigeCredor()) {
					throw new BusinessException("obrigacaoPagarService.rubricaExigeCredor", rubrica.getTipoRubrica()
							.getDescricao(), rubrica.getTipoRubrica().getTipoCredorFormatado());
				}
			}
		}
	}

	/**
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 29/09/2011
	 * 
	 * @param participantes
	 * @return true se tiver mais de um credor.
	 */
	private boolean verificaSePossuiCredor(List<ParticipanteObrigacao> participantes) {
		for (ParticipanteObrigacao participante : participantes) {
			if (participante instanceof Credor) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 29/09/2011
	 * 
	 * @param rubricas
	 * @param participante
	 * @return false se nao existir rubricas para o participante.
	 */
	private boolean verificaRubricasRequeridasPorParticipante(List<Rubrica> rubricas, ParticipanteObrigacao participante) {
		if (participante instanceof Devedor)
			return true;

		TipoCredorEnum tipoCredor = ((Credor) participante).getTipoCredor();

		for (Rubrica rubrica : rubricas) {

			TipoRubrica tipoRubrica = rubrica.getTipoRubrica();

			for (CredorRubrica credorRubrica : tipoRubrica.getCredorRubricaList()) {
				if (credorRubrica.getTipoCredor().equals(tipoCredor)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 29/09/2011
	 * 
	 * @param participantes
	 * @throws BusinessException
	 */
	private Boolean verificaSePossuiDevedor(List<ParticipanteObrigacao> participantes) throws BusinessException {
		for (ParticipanteObrigacao participante : participantes) {
			if (participante instanceof Devedor) {
				return true;
			}
		}
		throw new BusinessException("obrigacaoPagarService.devedorNotNull");
	}

	/**
	 * Retorna uma lista de tipo rubrica da categoria recebida como parametro.
	 * 
	 * @author Athos / Ricardo
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 04/10/2011
	 * 
	 * @return lista de rubricas da categoria recebida como parametro.
	 */
	@SuppressWarnings("unchecked")
	public List<TipoRubrica> carregaTipoRubrica(String descricaoCategoriaRubrica) {
		String hql = "FROM " + TipoRubrica.class.getSimpleName() + " WHERE lower(categoriaRubrica.descricao) = lower('"
				+ (descricaoCategoriaRubrica == null ? "" : descricaoCategoriaRubrica) + "')";

		return em.createQuery(hql).getResultList();
	}

	/**
	 * Retorna uma lista de tipo rubrica com todos cadastrados no banco.
	 * 
	 * @author rodrigo
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 04/10/2011
	 */
	@SuppressWarnings("unchecked")
	public List<TipoRubrica> carregaTipoRubrica() {
		String hql = "FROM " + TipoRubrica.class.getSimpleName();

		return em.createQuery(hql).getResultList();
	}

	/**
	 * Cria um grupo de edicao para a entrada de formulario de participantes e
	 * rubricas.
	 * 
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @since 1.4.02
	 * @category PJE-JT
	 * @created 06/10/2011
	 * 
	 * @param participantes
	 * @param rubricas
	 * @return um grupo de edicao
	 */
	public GrupoEdicao criarGrupoEdicao(ProcessoJT processoJt, List<ParticipanteObrigacao> participantes,
			List<Rubrica> rubricas) {

		GrupoEdicao grupo = new GrupoEdicao();

		List<ObrigacaoPagar> obrigacoes = new ArrayList<ObrigacaoPagar>();
		List<Credor> credores = getCredores(participantes, rubricas);
		List<Devedor> devedores = getDevedores(participantes);

		definirDataCalculoRubricas(rubricas);

		for (TipoCredorEnum tipoCredor : TipoCredorEnum.values()) {
			ObrigacaoPagar obrigacao = new ObrigacaoPagar();

			for (Credor credor : credores) {
				if (credor.getTipoCredor().equals(tipoCredor)) {
					obrigacao.getObrigacaoAtomicaList().addAll(criarObrigacaoAtomicaList(obrigacao, credor, devedores));
				}
			}
			obrigacao.setRubricaList(getRubricasPorTipoDeCredor(obrigacao, rubricas, tipoCredor, credores));

			if ((obrigacao.getRubricaList().size() > 0) && (obrigacao.getObrigacaoAtomicaList().size() > 0)) {

				obrigacoes.add(obrigacao);
				obrigacao.setGrupoEdicao(grupo);
				obrigacao.setProcessoJT(processoJt);

			}
		}

		grupo.setObrigacaoPagarList(obrigacoes);
		return grupo;
	}

	/**
	 * Caso a rubrica esteja sem uma data de cálculo, colocar a data atual
	 * 
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @since 1.4.02
	 * @category PJE-JT
	 * 
	 * @param rubricas
	 */
	private void definirDataCalculoRubricas(List<Rubrica> rubricas) {
		for (Rubrica rubrica : rubricas) {
			if (rubrica.getDataCalculo() == null) {
				rubrica.setDataCalculo(new Date());
			}
		}
	}

	/**
	 * Retorna uma lista de rubricas que fazem parte do mesmo tipo de credor.
	 * 
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @since 1.4.02
	 * @category PJE-JT
	 * 
	 * @param rubricas
	 * @param tipoCredor
	 * @param b
	 * @return lista de rubricas para o tipo de credor.
	 */
	private List<Rubrica> getRubricasPorTipoDeCredor(ObrigacaoPagar obrigacaoPagar, List<Rubrica> rubricas,
			TipoCredorEnum tipoCredor, List<Credor> credores) {

		List<Rubrica> itens = new ArrayList<Rubrica>();

		for (Rubrica rubrica : rubricas) {

			for (CredorRubrica cr : rubrica.getTipoRubrica().getCredorRubricaList()) {

				if (cr.getTipoCredor().equals(tipoCredor)) {
					if ((!tipoCredor.equals(TipoCredorEnum.U)) || (cr.getOrdemCredito().equals(OrdemCreditoEnum.P))) {
						itens.add(rubrica);
						rubrica.setObrigacaoPagar(obrigacaoPagar);
						break;
					} else {
						// é união e é secundário; é necessário verificar se há
						// um outro credor primário disponível
						if (!existeTipoCredorNaoUniaoPrimarioParaRubrica(rubrica.getTipoRubrica(), credores)) {
							itens.add(rubrica);
							rubrica.setObrigacaoPagar(obrigacaoPagar);
							break;
						}
					}
				}
			}
		}

		return itens;
	}

	/**
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 05/10/2011
	 * 
	 * @param tipoRubrica
	 * @param credores
	 * @return true se existir na lista ao menos um credor que seja credor
	 *         primário para o tipo de rubrica informado
	 */
	private boolean existeTipoCredorNaoUniaoPrimarioParaRubrica(TipoRubrica tipoRubrica, List<Credor> credores) {
		for (CredorRubrica credorRubrica : tipoRubrica.getCredorRubricaList()) {
			if ((credorRubrica.getOrdemCredito().equals(OrdemCreditoEnum.P))
					&& (!credorRubrica.getTipoCredor().equals(TipoCredorEnum.U))) {
				for (Credor credor : credores) {
					if (credor.getTipoCredor().equals(credorRubrica.getTipoCredor())) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Cria uma lista de obrigacoes atomicas para um credor especifico.
	 * 
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @since 1.4.02
	 * @category PJE-JT
	 * 
	 * @param credor
	 * @param devedores
	 * @return uma lista de obrigacoes atomicas.
	 */
	private List<ObrigacaoAtomica> criarObrigacaoAtomicaList(ObrigacaoPagar obrigacaoPagar, Credor credor,
			List<Devedor> devedores) {
		List<ObrigacaoAtomica> obrigacoes = new ArrayList<ObrigacaoAtomica>();

		for (Devedor devedor : devedores) {

			Credor credorClone = credor.clone();
			Devedor devedorClone = devedor.clone();

			ObrigacaoAtomica obrigacaoAtomica = new ObrigacaoAtomica();
			obrigacaoAtomica.setCredor(credorClone);
			obrigacaoAtomica.setDevedor(devedorClone);
			obrigacaoAtomica.setObrigacaoPagar(obrigacaoPagar);

			credorClone.setObrigacaoAtomica(obrigacaoAtomica);
			devedorClone.setObrigacaoAtomica(obrigacaoAtomica);

			obrigacoes.add(obrigacaoAtomica);
		}
		return obrigacoes;
	}

	/**
	 * Retorna uma lista de devedores.
	 * 
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @since 1.4.02
	 * @category PJE-JT
	 * 
	 * @param participantes
	 * @return devedores.
	 */
	private List<Devedor> getDevedores(List<ParticipanteObrigacao> participantes) {
		List<Devedor> devedores = new ArrayList<Devedor>();
		for (ParticipanteObrigacao participante : participantes) {
			if (participante instanceof Devedor) {
				devedores.add((Devedor) participante);
			}
		}
		return devedores;
	}

	/**
	 * Cria uma lista de credores e se necessario coloca o credor uniao como
	 * participante, dependendo da rubrica.
	 * 
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @since 1.4.02
	 * @category PJE-JT
	 * 
	 * @param participantes
	 * @param rubricas
	 * @returnCredores.
	 */
	private List<Credor> getCredores(List<ParticipanteObrigacao> participantes, List<Rubrica> rubricas) {

		List<Credor> credores = new ArrayList<Credor>();
		Credor participanteUniao = new Credor();
		participanteUniao.setParticipacaoObrigacao(ParticipacaoObrigacaoEnum.C);
		participanteUniao.setTipoCredor(TipoCredorEnum.U);

		if (!verificaSePossuiCredor(participantes)) {
			credores.add(participanteUniao);
			return credores;
		}

		for (Rubrica rubrica : rubricas) {
			boolean encontrouUniao = false;
			for (CredorRubrica cr : rubrica.getTipoRubrica().getCredorRubricaList()) {
				if (cr.getOrdemCredito().equals(OrdemCreditoEnum.P) && cr.getTipoCredor().equals(TipoCredorEnum.U)) {
					credores.add(participanteUniao);
					encontrouUniao = true;
					break;
				}
			}

			if (encontrouUniao) {
				break;
			}
		}

		for (ParticipanteObrigacao participante : participantes) {
			if (participante instanceof Credor) {
				credores.add((Credor) participante);
			}
		}

		return credores;
	}

	/**
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 05/10/2011
	 * 
	 * @param processoJT
	 * @return lista de grupo de edições e de obrigações de pagar referentes ao
	 *         processo informado
	 */
	public List<GrupoEdicao> obterGrupoEdicao(ProcessoJT processoJT) {
		String hql = "select distinct p.grupoEdicao from ObrigacaoPagar p "
				+ "where p.processoJT.processoTrf.processo.idProcesso = :processo " + "and p.ativo = true";
		Query query = getEntityManager().createQuery(hql);
		query.setParameter("processo", processoJT.getProcessoTrf().getProcesso().getIdProcesso());

		@SuppressWarnings(value = "unchecked")
		ArrayList<GrupoEdicao> gruposEdicao = (ArrayList<GrupoEdicao>) query.getResultList();
		// gruposEdicao = ordenaGrupoEdicaoList(gruposEdicao);
		// refresh

		for (GrupoEdicao ge : gruposEdicao) {
			em.refresh(ge);
		}

		return gruposEdicao;
	}

	/**
	 * Ordena a lista de grupos de edição de acordo com a data da primeira
	 * rubrica encontrada.
	 * 
	 * @param gruposEdicao
	 * @return
	 */
	private ArrayList<GrupoEdicao> ordenaGrupoEdicaoList(ArrayList<GrupoEdicao> gruposEdicao) {

		// TODO: Terminar o método e testar

		ArrayList<GrupoEdicao> gruposEdicaoOrdenados = new ArrayList<GrupoEdicao>();
		int contador = 0;

		while (contador < gruposEdicao.size()) {

			GrupoEdicao maisAntigo = null;
			Date dataMaisAntiga = null;

			for (GrupoEdicao grupoEdicao : gruposEdicao) {

				if (!gruposEdicaoOrdenados.contains(grupoEdicao)) {

					List<ObrigacaoPagar> obrigacaoPagarList = grupoEdicao.getObrigacaoPagarList();

					for (ObrigacaoPagar obrigacaoPagar : obrigacaoPagarList) {
						Rubrica rubrica = obrigacaoPagar.getRubricaList().get(0);

						if (maisAntigo != null) {

							if (rubrica.getDataCalculo().before(dataMaisAntiga)) {

								maisAntigo = grupoEdicao;
								dataMaisAntiga = rubrica.getDataCalculo();
							}

						} else {
							maisAntigo = grupoEdicao;
							dataMaisAntiga = rubrica.getDataCalculo();
						}
					}
				}
			}

			gruposEdicaoOrdenados.add(maisAntigo);
			contador++;
		}

		return gruposEdicaoOrdenados;
	}

	/**
	 * Persiste no banco as obrigações de pagar geradas a partir das rubricas e
	 * participantes informados
	 * 
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 05/10/2011
	 * 
	 * @param processoJt
	 * @param participantes
	 * @param rubricas
	 */
	public void gravar(ProcessoJT processoJt, List<ParticipanteObrigacao> participantes, List<Rubrica> rubricas)
			throws BusinessException {
		validarRubricasParaParticipantes(participantes, rubricas);
		GrupoEdicao grupoEdicao = criarGrupoEdicao(processoJt, participantes, rubricas);

		for (ObrigacaoPagar obrigacaoPagar : grupoEdicao.getObrigacaoPagarList()) {

			obrigacaoPagar.setGrupoEdicao(grupoEdicao);

			for (Rubrica rubrica : obrigacaoPagar.getRubricaList()) {
				rubrica.setObrigacaoPagar(obrigacaoPagar);
			}
		}

		em.joinTransaction();
		em.persist(grupoEdicao);
		em.flush();
	}

	/**
	 * Remove (inativa) as obrigações de pagar de um grupo de edição
	 * 
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 05/10/2011
	 * 
	 * @param grupoEdicao
	 */
	public void remover(GrupoEdicao grupoEdicao) {
		for (ObrigacaoPagar obrigacaoPagar : grupoEdicao.getObrigacaoPagarList()) {
			obrigacaoPagar.setAtivo(Boolean.FALSE);
		}

		em.joinTransaction();
		em.persist(grupoEdicao);
		em.flush();
	}

	/**
	 * Atualiza as obrigações de pagar de um grupo de edição (inativa o
	 * existente e gera um novo grupo).
	 * 
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 05/10/2011
	 * 
	 * @param grupoEdicao
	 * @param participantes
	 * @param rubricas
	 */
	public void atualizar(ProcessoJT processoJt, GrupoEdicao grupoEdicao, List<ParticipanteObrigacao> participantes,
			List<Rubrica> rubricas) {
		// passo 1: persistir o novo grupo de edição
		gravar(processoJt, participantes, rubricas);

		// passo 2: não dando erro no passo 1, remover o grupo anterior
		remover(grupoEdicao);
	}

	/**
	 * Obtém a lista de participantes e a lista de rubricas de um grupo de
	 * edição
	 * 
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 05/10/2011
	 * 
	 * @param grupoEdicao
	 * @param participantes
	 * @param rubricas
	 */
	public void obterParticipantesERubricasDoGrupoEdicao(GrupoEdicao grupoEdicao,
			List<ParticipanteObrigacao> participantes, List<Rubrica> rubricas) {
		for (ObrigacaoPagar obrigacaoPagar : grupoEdicao.getObrigacaoPagarList()) {
			for (ObrigacaoAtomica obrigacaoAtomica : obrigacaoPagar.getObrigacaoAtomicaList()) {
				if (obrigacaoAtomica.getCredor().getTipoCredor() != TipoCredorEnum.U) {
					if (!participantes.contains(obrigacaoAtomica.getCredor())) {
						participantes.add(obrigacaoAtomica.getCredor().clone());
					}
				}
				
				if (!participantes.contains(obrigacaoAtomica.getDevedor())) {
					participantes.add(obrigacaoAtomica.getDevedor().clone());
				}
			
			}

			for (Rubrica rubrica : obrigacaoPagar.getRubricaList()) {
				if (!rubricas.contains(rubrica)) {
					rubricas.add(rubrica.clone());
				}
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
	 * @param processoJT
	 *            a ter suas obrigações de pagar homologadas
	 */
	public void homologaObrigacaoPagar(ProcessoJT processoJT) {
		if (processoJT.getObrigacaoPagarList() == null || processoJT.getObrigacaoPagarList().size() < 1) {
			return;
		}

		// Homologar as obrigacoes a pagar
		for (ObrigacaoPagar obrigacaoPagar : processoJT.getObrigacaoPagarList()) {
			if (obrigacaoPagar.getAtivo() && !obrigacaoPagar.getHomologado()) {
				obrigacaoPagar.setHomologado(true);
				obrigacaoPagar.getGrupoEdicao().setHomologado(true);
			}
		}

		joinTransaction();
		getEntityManager().flush();

		lancarMovimentoHomologaObrigacaoPagar(processoJT);
	}

	/**
	 * Lança movimento para o processo de sentença de homologação de cálculo.
	 * [PJE-982][PJE-907]
	 * 
	 * @author athos reiser
	 * 
	 * @category PJE-JT
	 * @since 1.4.3
	 * @created 08/12/2011
	 * 
	 * @param processoJT
	 *            a ter o lançamento de movimento de liquidação de cálculo.
	 */
	public void lancarMovimentoHomologaObrigacaoPagar(ProcessoJT processoJT) {

		String codMovimentoLiquidacaoHomologacao = ParametroUtil.getFromContext("codMovimentoLiquidacaoHomologacao",
				true);

		// Código = 50047 - Descrição = Homologada a liquidação
		// **************************************************************************************
		MovimentoAutomaticoService.preencherMovimento().deCodigo(codMovimentoLiquidacaoHomologacao)
		  						  .associarAoProcesso(processoJT.getProcessoTrf().getProcesso())
		  						  .lancarMovimento();
	}
}