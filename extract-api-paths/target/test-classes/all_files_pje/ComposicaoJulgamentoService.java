package br.jus.cnj.pje.nucleo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.manager.OrgaoJulgadorColegiadoOrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoComposicaoOrdemManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoComposicaoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SubstituicaoMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.SucessaoOJsColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoVisibilidadeManager;
import br.jus.pje.nucleo.dto.ParticipanteComposicaoJulgamentoProcessoDTO;
import br.jus.pje.nucleo.dto.SugestaoAdiamentoJulgamentoProcessoDTO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoComposicao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SubstituicaoMagistrado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.enums.ComposicaoJulgamentoEnum;
import br.jus.pje.nucleo.enums.MotivoAdiamentoJulgamentoProcessoEnum;
import br.jus.pje.nucleo.enums.TipoAtuacaoMagistradoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;

/**
 * Classe responsável pelos serviços de manutenção da composição dos
 * presentes/votantes de uma sessão de julgamento
 * 
 */
@Name("composicaoJulgamentoService")
public class ComposicaoJulgamentoService extends BaseService {

	/**
	 * Dada uma sessão o método irá retornar o presidente configurado
	 */
	
	private Boolean isCircular = false;
	private Map<String, UsuarioLocalizacaoMagistradoServidor> localizacaoMagistradoPrincipal = new LinkedHashMap<>();
	
	private Comparator<OrgaoJulgadorColegiadoOrgaoJulgador> orderByDate = new Comparator<OrgaoJulgadorColegiadoOrgaoJulgador>() {
		@Override
		public int compare(OrgaoJulgadorColegiadoOrgaoJulgador o1, OrgaoJulgadorColegiadoOrgaoJulgador o2) {
			return o1.getDataInicial().compareTo(o2.getDataInicial());
		}
	};
	
	public PessoaMagistrado obterPresidenteSessao(Sessao sessao) {
		PessoaMagistrado presidente = null;
		for (SessaoComposicaoOrdem componenteComposicao : sessao.getSessaoComposicaoOrdemList()) {
			if (componenteComposicao.getPresidente()) {
				presidente = componenteComposicao.getMagistradoPresenteSessao();
				break;
			}
		}

		return presidente;
	}

	/**
	 * Método responsável por obter o quórum dos magistrados
	 * participantes/votantes de um do processo pautados em sessão
	 * 
	 * @param processoPautado
	 *            {@link SessaoPautaProcessoTrf}: o processo pautado em sessão
	 * 
	 * @param incluirRelator
	 *            indica se no resultado terá somente relatores
	 * 
	 * @param cargoAuxiliar
	 *            indica se trará somente magistrados auxiliares/substitutos
	 * @return
	 */
	public List<PessoaMagistrado> obterMagistradosParticipantesJulgamentoProcesso(SessaoPautaProcessoTrf processoPautado, boolean incluirRelator, Boolean cargoAuxiliar) {
		return obterQuorumMagistradosPresentes(processoPautado.getSessao(), processoPautado, incluirRelator, cargoAuxiliar);
	}

	/**
	 * Método responsável por obter o quórum dos magistrados participantes de
	 * uma sessão de julgamento
	 * 
	 * @param sessao
	 *            {@link Sessao}: a sessão de julgamento para pesquisa.
	 * 
	 * @param incluirRelator
	 *            indica se no resultado terá somente relatores
	 * 
	 * @param cargoAuxiliar
	 *            indica se trará somente magistrados auxiliares/substitutos
	 * @return
	 */
	public List<PessoaMagistrado> obterMagistradosParticipantesJulgamentoProcesso(Sessao sessao, boolean incluirRelator, Boolean cargoAuxiliar) {
		return obterQuorumMagistradosPresentes(sessao, null, true, cargoAuxiliar);
	}

	/**
	 * Método responsável por obter o quórum dos magistrados participantes de
	 * uma sessão de julgamento. É possível obter os participantes de toda a
	 * sessão ou somente de um dos processo pautados.
	 * 
	 * @param sessao
	 *            {@link Sessao} informe quando quiser o quórum da sessão toda.
	 * 
	 * @param processoPautado
	 *            {@link SessaoPautaProcessoTrf} informe quando quiser obter o
	 *            quórum somente de um dos processos
	 * 
	 * @param incluirRelator
	 *            indica se no resultado terá somente relatores
	 * 
	 * @param cargoAuxiliar
	 *            indica se trará somente magistrados auxiliares/substitutos
	 * @return
	 */
	public List<PessoaMagistrado> obterQuorumMagistradosPresentes(Sessao sessao, SessaoPautaProcessoTrf processoPautado, boolean incluirRelator, Boolean cargoAuxiliar) {

		HashSet<PessoaMagistrado> magistradosPresentes = new HashSet<>();

		List<SessaoPautaProcessoTrf> processosPautados = new ArrayList<>();
		if (processoPautado != null) {
			processosPautados.add(processoPautado);
		} else {
			processosPautados.addAll(sessao.getSessaoPautaProcessoTrfList());
		}

		for (SessaoPautaProcessoTrf procPautado : processosPautados) {
			if(procPautado.getDataExclusaoProcessoTrf() == null) {
				for (SessaoPautaProcessoComposicao participante : procPautado.getSessaoPautaProcessoComposicaoList()) {
					if (participante.getPresente()) {
						if (cargoAuxiliar == null || participante.getCargoAtuacao().getAuxiliar().equals(cargoAuxiliar)) {
							Boolean isRelator = participante.getTipoAtuacaoMagistrado().equals(TipoAtuacaoMagistradoEnum.RELAT);
							if (incluirRelator || !isRelator) {
								magistradosPresentes.add(participante.getMagistradoPresente());
							}
						}
					}
				}
			}
		}

		return new ArrayList<>(magistradosPresentes);
	}

	/**
	 * Método responsável por retornar a composição dos participantes de uma
	 * sessão de julgamento para um processo, levando em consideração para isso
	 * o OJs principais (Relator e Revisor), os OJs do colegiado do processo e
	 * os magistrados que se vincularam ao processo.
	 * 
	 * @param processo
	 * 
	 * @throws Exception
	 */
	public List<ParticipanteComposicaoJulgamentoProcessoDTO> calcularParticipantesComposicaoJulgamentoProcesso(ProcessoTrf processo) throws PJeBusinessException {
		Integer ordemVotacao = null;
		List<OrgaoJulgador> ojsParticipantesComposicao = this.obterJulgadoresPrincipais(processo);
		this.adicionarDemaisOrgaosJulgadoresParticipantesComposicao(processo, ojsParticipantesComposicao);

		List<ProcessoMagistrado> magistradosVinculadosProcesso = ComponentUtil.getComponent(ProcessoMagistradoManager.class)
				.obterMagistradosRelacionados(processo, null, null, null, true);

		ArrayList<ParticipanteComposicaoJulgamentoProcessoDTO> julgadoresParticipantes = new ArrayList<>();
		for (OrgaoJulgador orgaoJulgador : ojsParticipantesComposicao) {
			if (getIsCircular()) {
				ordemVotacao = ojsParticipantesComposicao.indexOf(orgaoJulgador);
			}
			ParticipanteComposicaoJulgamentoProcessoDTO participante = this.obterParticipanteComposicaoJulgamento(
					processo, magistradosVinculadosProcesso, orgaoJulgador, ordemVotacao);

			if (participante.getMagistrado() != null) {
				julgadoresParticipantes.add(participante);
			}
		}

		return julgadoresParticipantes;
	}
	
	/**
	 * Dado um processo é retornada uma {@link List} de {@link OrgaoJulgador}
	 * contendo o OJ Relator e, quando houver, o OJ Revisor.
	 */
	private List<OrgaoJulgador> obterJulgadoresPrincipais(ProcessoTrf processo) {
		List<OrgaoJulgador> julgadoresPrincipais = new ArrayList<>();
		
		OrgaoJulgador ojRelator = processo.getOrgaoJulgador();
		OrgaoJulgador ojRevisor = processo.getOrgaoJulgadorRevisor();

		julgadoresPrincipais.add(ojRelator);
		if (ojRevisor != null) {
			julgadoresPrincipais.add(ojRevisor);
		}

		return julgadoresPrincipais;
	}

	/**
	 * Adiciona na lista informada no parâmetro
	 * <code>ojsParticipantesComposicao</code> os demais participantes da
	 * votação do <code>processo</code>, levando em conta se a composição é
	 * reduzida ou integral.
	 */
	private void adicionarDemaisOrgaosJulgadoresParticipantesComposicao(ProcessoTrf processo,
			List<OrgaoJulgador> ojsParticipantesComposicao) throws PJeBusinessException {

		if (ComposicaoJulgamentoEnum.R.equals(processo.getComposicaoJulgamento())) {
			this.adicionarDemaisParticipantesComposicaoReduzida(ojsParticipantesComposicao, processo.getOrgaoJulgadorColegiado());
		} else {
			this.adicionarDemaisParticipantesComposicaoIntegral(ojsParticipantesComposicao, processo.getOrgaoJulgadorColegiado(), processo.getOrgaoJulgador());
		}
	}

	/**
	 * Dada uma lista previamente populada com os órgãos julgadores
	 * participantes principais (relator e possível revisor) da composição de um
	 * processo, adiciona os demais participantes necessários a completar a
	 * composição reduzida.
	 * 
	 * @param ojsParticipantesComposicao
	 *            lista previamente populada com relator e possivel revisor.
	 * 
	 * @param orgaoJulgadorColegiado
	 * 
	 * @throws Exception
	 */
	private void adicionarDemaisParticipantesComposicaoReduzida(List<OrgaoJulgador> ojsParticipantesComposicao,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado) throws PJeBusinessException {

		OrgaoJulgador orgaoJulgadorReferencia = ojsParticipantesComposicao.get(ojsParticipantesComposicao.size() - 1);
		OrgaoJulgadorColegiadoOrgaoJulgador proximoComponenteColegiado = this.obterProximoOrgaoJulgadorComposicaoDadoUmOrgaoJulgadorReferencia(
				orgaoJulgadorReferencia, orgaoJulgadorColegiado);

		List<OrgaoJulgador> ojsTestados = new ArrayList<>(ojsParticipantesComposicao);

		while (ojsParticipantesComposicao.size() < orgaoJulgadorColegiado.getQuantidadeJulgadoresComposicaoReduzida()) {
			if (proximoComponenteColegiado == null || ojsTestados.contains(proximoComponenteColegiado.getOrgaoJulgador())) {
				throw new PJeBusinessException("Impossível determinar composição reduzida para esse processo. Verifique a configuração do Colegiado");
			}

			if (!this.existeImpedimentoOrgaoJulgadorPorSucessaoEmColegiado(
					proximoComponenteColegiado.getOrgaoJulgador(), ojsParticipantesComposicao, orgaoJulgadorColegiado)) {

				ojsParticipantesComposicao.add(proximoComponenteColegiado.getOrgaoJulgador());
			}

			ojsTestados.add(proximoComponenteColegiado.getOrgaoJulgador());
			proximoComponenteColegiado = this.obterProximoOrgaoJulgadorComposicaoDadoUmOrgaoJulgadorReferencia(
					proximoComponenteColegiado.getOrgaoJulgador(), orgaoJulgadorColegiado);
		}
	}
	
	/***
	 * Dado um órgão julgador de referência <code>orgaoJulgadorReferencia</code>
	 * e a composição atualmente ativa <code>composicaoAtivaColegiado</code> de
	 * um colegiado, retorna o próximo componente para cálculo de composição
	 * reduzida.
	 * 
	 * @param orgaoJulgadorReferencia
	 * @param orgaoJulgadorColegiado
	 * @return
	 * @throws PJeBusinessException
	 */
	private OrgaoJulgadorColegiadoOrgaoJulgador obterProximoOrgaoJulgadorComposicaoDadoUmOrgaoJulgadorReferencia(
			OrgaoJulgador orgaoJulgadorReferencia, OrgaoJulgadorColegiado orgaoJulgadorColegiado) throws PJeBusinessException {

		return ComponentUtil .getComponent(OrgaoJulgadorColegiadoOrgaoJulgadorManager.class).obterProximoOrgaoJulgadorComposicaoDadoUmOrgaoJulgadorReferencia(
				orgaoJulgadorReferencia, orgaoJulgadorColegiado);
	}
	
	/**
	 * Dado um órgão julgador <code>orgaoJulgador</code>, verifica se o mesmo
	 * possui impedimento implícito por sucessão em colegiado (dança de
	 * cadeiras) em relação a algum dos demais orgãos julgadores
	 * <code>orgaosJulgadores</code> da composição. RN: se um órgão julgador X é
	 * sucedido no colegiado por um órgão julgador Y, os mesmos não podem
	 * participar da composição de um mesmo processo.
	 * 
	 * @param orgaoJulgador
	 *            órgão julgador a ser analisado
	 * 
	 * @param orgaosJulgadores
	 *            lista de orgãos julgadores a ser verificada pela regra de
	 *            sucessão.
	 * 
	 * @return true caso exista impedimento por sucessão entre o órgão julgador
	 *         em questão e pelo menos um órgão julgador da lista.
	 */
	private boolean existeImpedimentoOrgaoJulgadorPorSucessaoEmColegiado(OrgaoJulgador orgaoJulgador, List<OrgaoJulgador> orgaosJulgadores, OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		Boolean existeImpedimentoPorSucessao = false;
		SucessaoOJsColegiadoManager sucessaoOJsColegiadoManager = ComponentUtil.getComponent(SucessaoOJsColegiadoManager.class);
		for (OrgaoJulgador possivelOorgaoJulgadorSucessor : orgaosJulgadores) {
			if (sucessaoOJsColegiadoManager.existeRelacaoSucessaoRecursiva(orgaoJulgador, possivelOorgaoJulgadorSucessor, orgaoJulgadorColegiado)) {
				existeImpedimentoPorSucessao = true;
				break;
			}
		}
		return existeImpedimentoPorSucessao;
	}

	/**
	 * Dada uma lista previamente populada com os órgãos julgadores participantes principais (relator e possível revisor) 
	 * da composição de um processo, adiciona demais participantes necessários a completar a composição integral.
	 * 
	 * @param ojsParticipantesComposicao lista previamente populada com relator e possivel revisor.
	 * @param orgaoJulgadorColegiado
	 * @throws Exception
	 */
	private void adicionarDemaisParticipantesComposicaoIntegral(List<OrgaoJulgador> ojsParticipantesComposicao,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado, OrgaoJulgador ojRelator) throws PJeBusinessException {

		List<OrgaoJulgador> orgaosJulgadoresAtivosColegiado = this.getOrgaosJulgadores(orgaoJulgadorColegiado, ojRelator);

		for (OrgaoJulgador orgaoJulgadorAtivo : orgaosJulgadoresAtivosColegiado) {
			if (!ojsParticipantesComposicao.contains(orgaoJulgadorAtivo)) {
				ojsParticipantesComposicao.add(orgaoJulgadorAtivo);
			}
		}
	}

	private List<OrgaoJulgador> getOrgaosJulgadores(OrgaoJulgadorColegiado orgaoJulgadorColegiado,
			OrgaoJulgador ojRelator) throws PJeBusinessException {

		List<OrgaoJulgadorColegiadoOrgaoJulgador> orgaosJulgadoresAtivosColegiadoOrdenado = 
				this.getListaOrgaoJulgadorColegiadoOrgaoJulgador(ojRelator, orgaoJulgadorColegiado);

		List<OrgaoJulgador> orgaosJulgadores = new ArrayList<>(orgaosJulgadoresAtivosColegiadoOrdenado.size());
		for (OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoOrgaoJulgador : orgaosJulgadoresAtivosColegiadoOrdenado) {
			orgaosJulgadores.add(orgaoJulgadorColegiadoOrgaoJulgador.getOrgaoJulgador());
		}
		return orgaosJulgadores;
	}

	private List<OrgaoJulgadorColegiadoOrgaoJulgador> getListaOrgaoJulgadorColegiadoOrgaoJulgador(
			OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado) {

		OrgaoJulgador ojProcesso = orgaoJulgador;
		List<OrgaoJulgadorColegiadoOrgaoJulgador> composicaoColegiadoAtivaOrdenada = new ArrayList<>();

		do {
			OrgaoJulgadorColegiadoOrgaoJulgador ojcoj;
			try {
				Optional<OrgaoJulgadorColegiadoOrgaoJulgador> optional = orgaoJulgador.getOrgaoJulgadorColegiadoOrgaoJulgadorList().stream()
						.filter(p -> p.getOrgaoJulgadorColegiado().equals(orgaoJulgadorColegiado)).sorted(this.orderByDate).findFirst();
				
				if (optional.isPresent()) {
					ojcoj = optional.get();
				} else {
					throw new Exception();
				}
				
				composicaoColegiadoAtivaOrdenada.add(ojcoj);
				OrgaoJulgadorColegiadoOrgaoJulgador ojulgadorRevisor = ojcoj.getOrgaoJulgadorRevisor();
				if (ojulgadorRevisor == null) {
					composicaoColegiadoAtivaOrdenada = null;
					break;
				}
				orgaoJulgador = ojcoj.getOrgaoJulgadorRevisor().getOrgaoJulgador();
			} catch (Exception e) {
				composicaoColegiadoAtivaOrdenada = null;
				break;
			}

		} while (!ojProcesso.equals(orgaoJulgador));

		List<OrgaoJulgadorColegiadoOrgaoJulgador> composicaoColegiadoAtiva = ComponentUtil.getComponent(OrgaoJulgadorColegiadoManager.class)
				.obterComposicaoAtiva(orgaoJulgadorColegiado);

		setIsCircular(false);
		if (Objects.nonNull(composicaoColegiadoAtivaOrdenada) && (composicaoColegiadoAtivaOrdenada.size() == composicaoColegiadoAtiva.size())) {
			setIsCircular(true);
			return composicaoColegiadoAtivaOrdenada;
		}

		return composicaoColegiadoAtiva;
	}
	
	private ParticipanteComposicaoJulgamentoProcessoDTO obterParticipanteComposicaoJulgamento(ProcessoTrf processo,
			List<ProcessoMagistrado> magistradosVinculadosProcesso, OrgaoJulgador orgaoJulgador, Integer ordemVotacao) throws PJeBusinessException {

		ParticipanteComposicaoJulgamentoProcessoDTO participante = new ParticipanteComposicaoJulgamentoProcessoDTO();
		participante.setProcesso(processo);
		participante.setOrgaoJulgador(orgaoJulgador);
		participante.setTipoAtuacaoMagistrado(obterTipoAtuacaoMagistrado(processo, orgaoJulgador));
		this.atribuirInformacoesPessoaisMagistrado(participante, magistradosVinculadosProcesso);
		participante.setOrdemDeVotacao(ordemVotacao);

		return participante;
	}
	
	private void atribuirInformacoesPessoaisMagistrado(ParticipanteComposicaoJulgamentoProcessoDTO participante,
			List<ProcessoMagistrado> magistradosVinculadosProcesso) throws PJeBusinessException {

		PessoaMagistrado magistrado = null;
		OrgaoJulgadorCargo cargoVinculacao = null;
		OrgaoJulgadorCargo cargoPrincipalDistribuicao = null;

		String key = String.format("%d;%d",
				participante.getProcesso().getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado(),
				participante.getOrgaoJulgador().getIdOrgaoJulgador());

		if (!this.localizacaoMagistradoPrincipal.containsKey(key)) {
			this.localizacaoMagistradoPrincipal.put(key, ComponentUtil.getComponent(UsuarioLocalizacaoMagistradoServidorManager.class)
					.obterLocalizacaoMagistradoPrincipal(participante.getOrgaoJulgador(), participante.getProcesso().getOrgaoJulgadorColegiado()));
		}
		UsuarioLocalizacaoMagistradoServidor lotacaoMagistradoTitular = this.localizacaoMagistradoPrincipal.get(key);

		if (lotacaoMagistradoTitular != null) {
			if (lotacaoMagistradoTitular.getOrgaoJulgadorCargoVinculacao() != null) {
				cargoVinculacao = lotacaoMagistradoTitular.getOrgaoJulgadorCargoVinculacao();
			} else {
				cargoVinculacao = lotacaoMagistradoTitular.getOrgaoJulgadorCargo();
			}

			cargoPrincipalDistribuicao = lotacaoMagistradoTitular.getOrgaoJulgadorCargo();
			magistrado = ComponentUtil.getComponent(PessoaMagistradoManager.class)
					.findById(lotacaoMagistradoTitular.getUsuarioLocalizacao().getUsuario().getIdUsuario());
		}

		for (ProcessoMagistrado procMag : magistradosVinculadosProcesso) {
			if (procMag.getOrgaoJulgador().equals(participante.getOrgaoJulgador())) {
				magistrado = procMag.getMagistrado();
				cargoVinculacao = procMag.getOrgaoJulgadorCargo();
			}
		}

		participante.setMagistrado(magistrado);
		participante.setCargoVinculacao(cargoVinculacao);
		participante.setCargoPrincipalDistribuicao(cargoPrincipalDistribuicao);
	}

	/**
	 * Método responsável por gerar a composição inicial dos participantes de
	 * uma sessão de julgamento. Cria a composição da sessão e também dos
	 * votantes de cada um dos processos
	 * 
	 */
	public void gerarComposicaoJulgamentoInicial(Sessao sessao) throws Exception {
		this.atualizarComposicaoJulgamentoPrincipal(sessao);
		this.atualizarComposicaoJulgamentoProcessos(sessao);
	}

	/**
	 * Método responsável por atualizar os participantes/votantes de uma sessão
	 * de julgamento de acordo com o colegiado atual, sugerindo magistrados
	 * substitutos quando houver período de substituição cadastrado.
	 * 
	 * @param sessao
	 * @throws PJeBusinessException
	 */
	public void atualizarComposicaoJulgamentoPrincipal(Sessao sessao) throws PJeBusinessException {
		List<OrgaoJulgadorColegiadoOrgaoJulgador> composicaoAtivaColegiado = ComponentUtil.getComponent(OrgaoJulgadorColegiadoManager.class)
				.obterComposicaoAtiva(sessao.getOrgaoJulgadorColegiado());

		Map<OrgaoJulgadorColegiadoOrgaoJulgador, PessoaMagistrado> mapTitulares = ComponentUtil.getComponent(OrgaoJulgadorColegiadoOrgaoJulgadorManager.class)
				.obterMapeamentoTitularesComposicaoColegiado(composicaoAtivaColegiado, false);
		
		Map<OrgaoJulgadorColegiadoOrgaoJulgador, PessoaMagistrado> mapSubstitutos = this.obterMapeamentoMagistradosSubstitutosComposicaoColegiado(mapTitulares, sessao.getDataSessao());
		
		SessaoComposicaoOrdemManager sessaoComposicaoOrdemManager = ComponentUtil.getComponent(SessaoComposicaoOrdemManager.class);
		sessaoComposicaoOrdemManager.removerComposicaoSessao(sessao);
		sessaoComposicaoOrdemManager.criarComposicaoSessao(sessao, mapTitulares, mapSubstitutos);
	}

	/**
	 * Identifica o magistrado substituto vigente em um determinado OJ se a
	 * substituição estiver cadastrada.
	 * 
	 * @see #obterPossivelMagistradoSubstituto(OrgaoJulgador,
	 *      OrgaoJulgadorColegiado, Date)
	 * @see SubstituicaoMagistradoManager#obterSubstituicaoMagistradoVigente(OrgaoJulgador,
	 *      OrgaoJulgadorColegiado, UsuarioLocalizacaoMagistradoServidor)
	 * 
	 * @throws PJeBusinessException
	 */
	private Map<OrgaoJulgadorColegiadoOrgaoJulgador, PessoaMagistrado> obterMapeamentoMagistradosSubstitutosComposicaoColegiado(
			Map<OrgaoJulgadorColegiadoOrgaoJulgador, PessoaMagistrado> mapTitularesComposicaoColegiado, Date dataSessao) throws PJeBusinessException {

		Map<OrgaoJulgadorColegiadoOrgaoJulgador, PessoaMagistrado> mapMagistradosSubstitutosComposicaoColegiado = new HashMap<>();

		for (OrgaoJulgadorColegiadoOrgaoJulgador componenteColegiado : mapTitularesComposicaoColegiado.keySet()) {
			PessoaMagistrado magistradoSubstituto = this.obterPossivelMagistradoSubstituto(componenteColegiado.getOrgaoJulgador(), 
					componenteColegiado.getOrgaoJulgadorColegiado(), dataSessao);

			mapMagistradosSubstitutosComposicaoColegiado.put(componenteColegiado, magistradoSubstituto);
		}

		return mapMagistradosSubstitutosComposicaoColegiado;
	}

	/**
	 * Dado um OJ e OJc é verificado se existe uma substituição cadastrada
	 * vigente da data da sessão, retornando o magistrado substituto quando
	 * houver.
	 * 
	 * @see SubstituicaoMagistradoManager#obterSubstituicaoVigentePorMagistradoAfastado(OrgaoJulgador,
	 *      OrgaoJulgadorColegiado, Date)
	 */
	private PessoaMagistrado obterPossivelMagistradoSubstituto(OrgaoJulgador orgaoJulgador,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado, Date dataReferencia) throws PJeBusinessException {

		PessoaMagistrado magistradoSubstituto = null;
		SubstituicaoMagistrado substituicaoVigente = ComponentUtil.getComponent(SubstituicaoMagistradoManager.class)
				.obterSubstituicaoVigentePorMagistradoAfastado(orgaoJulgador, orgaoJulgadorColegiado, dataReferencia);

		if (substituicaoVigente != null) {
			magistradoSubstituto = substituicaoVigente.getMagistradoSubstituto();
		}

		return magistradoSubstituto;
	}

	/**
	 * Atualiza os participantes/votantes dos processos pautados em uma sessão
	 * de julgamento, levando em consideração a composição atual da sessão de
	 * julgamento.
	 * @param sessao Sessão de julgamento na qual os processos estão pautados
	 */
	public void atualizarComposicaoJulgamentoProcessos(Sessao sessao) throws Exception {
		this.atualizarOrgaosJulgadoresComposicaoJulgamentoProcessos(sessao);
		this.atualizarMagistradosAtuantesComposicaoJulgamentoProcessos(sessao);
	}
	
	/**
	 * Dada uma sessão de julgamento, será percorrida a lista de processos
	 * pautados e para cada um será feita uma atualização dos participantes/votantes 
	 * do processo de acordo com a composição atual do colegiado.
	 * 
	 * @param sessao Sessão de julgamento na qual os processos estão pautados
	 */
	private void atualizarOrgaosJulgadoresComposicaoJulgamentoProcessos(Sessao sessao) throws PJeBusinessException {
		List<SessaoPautaProcessoTrf> processosPautados = sessao.getSessaoPautaProcessoTrfList();
		for (SessaoPautaProcessoTrf processoPautado : processosPautados) {
			if (processoPautado.getDataExclusaoProcessoTrf() == null && !processoPautado.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG)) {
				atualizarOrgaosJulgadoresComposicaoJulgamentoProcesso(processoPautado);
			}
		}
		ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).flush();
	}
	
	/**
	 * Em um processo pautado será atualizada a composição dos vogais participantes, 
	 * removendo ou incluindo novos de acordo com a nova composição do colegiado.
	 */
	private void atualizarOrgaosJulgadoresComposicaoJulgamentoProcesso(SessaoPautaProcessoTrf processoPautado) throws PJeBusinessException {
		List<ParticipanteComposicaoJulgamentoProcessoDTO> vogaisAtuantes = this.calcularVogaisAtuantes(processoPautado.getProcessoTrf(), processoPautado.getSessao());
		Boolean ocorreuRemocaoVogal = removerVogaisNaoMaisAtuantes(processoPautado, vogaisAtuantes);
		Boolean ocorreuInclusaoVogal = incluirPossiveisNovosVogais(processoPautado, vogaisAtuantes);

		if (ocorreuRemocaoVogal || ocorreuInclusaoVogal) {
			ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).merge(processoPautado);
		}
	}
	
	/**
	 * Dado um processo e uma sessão, retorna os possíveis vogais para atuação no processo, 
	 * de acordo com a posição (status) de composição atual do colegiado. Esse método é útil para 
	 * atualizar/adaptar a composição de vogais de um processo já pautado, para refletir possíveis modificações da
	 * composição padrão do colegiado nesse processo já pautado.
	 * 
	 * @param processo
	 * @param sessao
	 * @return lista de possíveis participantes vogais.
	 * @throws PJeBusinessException
	 */
	private List<ParticipanteComposicaoJulgamentoProcessoDTO> calcularVogaisAtuantes(ProcessoTrf processo, Sessao sessao) throws PJeBusinessException {
		List<ParticipanteComposicaoJulgamentoProcessoDTO> novosPossiveisParticipantesCompJulgProcesso = this.calcularParticipantesComposicaoJulgamentoProcesso(processo);
		this.filtrarMantendoSomenteVogais(novosPossiveisParticipantesCompJulgProcesso);
		return novosPossiveisParticipantesCompJulgProcesso;
	}

	/**
	 * Filtra a lista informada no parâmetro mantendo apenas os magistrados
	 * vogais na lista.
	 */
	private void filtrarMantendoSomenteVogais(List<ParticipanteComposicaoJulgamentoProcessoDTO> participantesCompJulgProcesso) {
		participantesCompJulgProcesso.removeIf(new Predicate<ParticipanteComposicaoJulgamentoProcessoDTO>() {
			@Override
			public boolean test(ParticipanteComposicaoJulgamentoProcessoDTO participanteCompJulgProcesso) {
				return !participanteCompJulgProcesso.getTipoAtuacaoMagistrado().equals(TipoAtuacaoMagistradoEnum.VOGAL);
			}
		});
	}
	
	/**
	 * Baseando-se na composição
	 * 
	 * @param processoPautado
	 * @param novosPossiveisVogais
	 * @return
	 */
	private Boolean removerVogaisNaoMaisAtuantes(SessaoPautaProcessoTrf processoPautado, final List<ParticipanteComposicaoJulgamentoProcessoDTO> novosPossiveisVogais) {
		Boolean composicaoProcessualModificada = false;
		composicaoProcessualModificada = processoPautado.getSessaoPautaProcessoComposicaoList().removeIf(new Predicate<SessaoPautaProcessoComposicao>() {
			@Override
			public boolean test(SessaoPautaProcessoComposicao participanteCompProcessual) {
				return isVogalAusenteNaNovaComposicao(participanteCompProcessual, novosPossiveisVogais);
			}
		});
		return composicaoProcessualModificada;
	}
	
	private Boolean isVogalAusenteNaNovaComposicao(SessaoPautaProcessoComposicao participanteCompProcessual, List<ParticipanteComposicaoJulgamentoProcessoDTO> novosPossiveisVogais) {
		Boolean vogalAusente = false;
		if (participanteCompProcessual.getTipoAtuacaoMagistrado().equals(TipoAtuacaoMagistradoEnum.VOGAL)) {
			vogalAusente = true;
			for (ParticipanteComposicaoJulgamentoProcessoDTO novoPossivelVogal : novosPossiveisVogais) {
				if (novoPossivelVogal.getOrgaoJulgador().equals(participanteCompProcessual.getOrgaoJulgador())) {
					vogalAusente = false;
					break;
				}
			}
		}
		return vogalAusente;
	}
	
	/**
	 * Realiza a inclusão na base de dados de novos vogais na composição do
	 * processo pautado;
	 */
	private Boolean incluirPossiveisNovosVogais(SessaoPautaProcessoTrf processoPautado, final List<ParticipanteComposicaoJulgamentoProcessoDTO> novosPossiveisVogais) {
		Integer qtdComponentesComposicaoAntesAnalise = processoPautado.getSessaoPautaProcessoComposicaoList().size();
		for (ParticipanteComposicaoJulgamentoProcessoDTO novoPossivelVogal : novosPossiveisVogais) {
			Boolean adicionarNovoVogal = true;
			for (SessaoPautaProcessoComposicao participanteCompProcessual : processoPautado.getSessaoPautaProcessoComposicaoList()) {
				if (novoPossivelVogal.getOrgaoJulgador().equals(participanteCompProcessual.getOrgaoJulgador())) {
					adicionarNovoVogal = false;
					break;
				}
			}
			if (adicionarNovoVogal) {
				SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager = ComponentUtil.getComponent(SessaoPautaProcessoComposicaoManager.class);
				sessaoPautaProcessoComposicaoManager.criarItemComposicaoJulgamentoProcesso(processoPautado, novoPossivelVogal);
			}
		}

		return qtdComponentesComposicaoAntesAnalise < processoPautado.getSessaoPautaProcessoComposicaoList().size();
	}

	/**
	 * Dada uma sessão o método percorre a lista de participantes/votantes de
	 * cada um dos processo pautados verificando se os julgadore principais
	 * (relator e revisor) estão presentes, caso não estejam eles são
	 * identificados e adicionados na lista de retorno.
	 * 
	 */
	public List<SugestaoAdiamentoJulgamentoProcessoDTO> obterSugestoesAdiamentoJulgamentoProcessoPorAusenciaJulgadoresPrincipais(Sessao sessao) {
		List<SugestaoAdiamentoJulgamentoProcessoDTO> sugestoesAdiamento = new ArrayList<>();
		SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager = ComponentUtil.getComponent(SessaoPautaProcessoComposicaoManager.class);
		for (SessaoComposicaoOrdem componenteSessao : sessao.getSessaoComposicaoOrdemList()) {
			if (!componenteSessao.getMagistradoTitularPresenteSessao()) {
				List<SessaoPautaProcessoComposicao> participacoesOrgaoJulgadorComposicalProcessual = sessaoPautaProcessoComposicaoManager.obterParticipacoesComposicaoProcessual(sessao, componenteSessao.getOrgaoJulgador());
				for (SessaoPautaProcessoComposicao participanteComposicaoProcessual : participacoesOrgaoJulgadorComposicalProcessual) {
					if (isProcessoAdiavelPorAusenciaJulgadoresPrincipais(participanteComposicaoProcessual, componenteSessao)) {
						SugestaoAdiamentoJulgamentoProcessoDTO sugestaoAdiamento = criarSugestaoAdiamentoJulgamentoProcesso(participanteComposicaoProcessual);
						sugestoesAdiamento.add(sugestaoAdiamento);
					}
				}
			}
		}

		return sugestoesAdiamento;
	}

	public Set<Integer> obterIDsOrgaosJulgadoresParticipantesComposicaoJulgamento(SessaoPautaProcessoTrf processoPautado) {
		return obterIDsOJsParticipantesProcessoJaPautado(processoPautado);
	}
	
	/**
	 * Dada uma sessão e um processo o método retorna os IDs dos OJs
	 * participantes/votantes do processo na sessão. Caso o processo ainda não
	 * tenha sido pautado os participantes serão calculados levando-se em
	 * consideração o OJ colegiado e os OJs vinculados.
	 * 
	 * @see #obterIDsOJsParticipantesProcessoNaoPautado(ProcessoTrf)
	 * @see #obterIDsOJsParticipantesProcessoJaPautado(SessaoPautaProcessoTrf)
	 * 
	 */
	public Set<Integer> obterIDsOrgaosJulgadoresParticipantesComposicaoJulgamento(Sessao sessao, ProcessoTrf processo) {
		Set<Integer> idsOJsParticipantes;

		SessaoPautaProcessoTrf processoPautado = null;

		if (sessao != null && processo != null) {
			SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
			processoPautado = sessaoPautaProcessoTrfManager.getSessaoPautaProcessoTrf(processo, sessao);
		}

		if (processoPautado != null) {
			idsOJsParticipantes = obterIDsOJsParticipantesProcessoJaPautado(processoPautado);
		} else {
			idsOJsParticipantes = obterIDsOJsParticipantesProcessoNaoPautado(processo);
		}

		return idsOJsParticipantes;
	}

	/**
	 * verifica se o usuário logado faz parte da lista de participantes votantes
	 * de um processo pautado.
	 */
	public Boolean isUsuarioLogadoParticipanteComposicao(SessaoPautaProcessoTrf processoPautado) {
		UsuarioLocalizacaoMagistradoServidor lotacaoServidor = Authenticator.getUsuarioLocalizacaoMagistradoServidorAtual();
		Boolean isParticipante = false;
		for (SessaoPautaProcessoComposicao votante : processoPautado.getComposicoesVotantes()) {
			if (votante.getOrgaoJulgador().equals(lotacaoServidor.getOrgaoJulgador())) {
				UsuarioLocalizacaoVisibilidadeManager usuarioLocalizacaoVisibilidadeManager = ComponentUtil.getComponent(UsuarioLocalizacaoVisibilidadeManager.class);
				isParticipante = (votante.getCargoAtuacao() == null || usuarioLocalizacaoVisibilidadeManager.possuiVisibilidadeAtiva(lotacaoServidor, votante.getCargoAtuacao()));
				break;
			}
		}
		return isParticipante;
	}

	/**
	 * Recupera os IDs dos OJs participantes/votantes de um processo já pautado
	 * em sessão.
	 */
	private Set<Integer> obterIDsOJsParticipantesProcessoJaPautado(SessaoPautaProcessoTrf processoPautado) {
		Set<Integer> idsOJsParticipantes = new LinkedHashSet<>();
		
		String hql = "select spc from SessaoPautaProcessoComposicao spc "+
					"where spc.sessaoPautaProcessoTrf = :sppt "
					+ "order by spc.ordemVotacao";
		javax.persistence.Query q = EntityUtil.getEntityManager().createQuery(hql);
		q.setParameter("sppt", processoPautado);
		
		@SuppressWarnings("unchecked")
		List<SessaoPautaProcessoComposicao> participantes = q.getResultList();

		for (SessaoPautaProcessoComposicao participante : participantes) {
			if (participante.getPresente()) {
				idsOJsParticipantes.add(participante.getOrgaoJulgador().getIdOrgaoJulgador());
			}
		}

		return idsOJsParticipantes;
	}
	
	public Map<Integer,Set<Integer>> obterIDsOJsParticipantesProcessosSessao(Sessao sessao) {
		Map<Integer,Set<Integer>> idsOJsParticipantes = new HashMap<>();
		
		String hql = "select spc from SessaoPautaProcessoComposicao spc join fetch spc.sessaoPautaProcessoTrf sptf join fetch sptf.sessao sess "+
					"where sess = :sessao ";
		javax.persistence.Query q = EntityUtil.getEntityManager().createQuery(hql);
		q.setParameter("sessao", sessao);
		
		@SuppressWarnings("unchecked")
		List<SessaoPautaProcessoComposicao> participantes = q.getResultList();

		for (SessaoPautaProcessoComposicao participante : participantes) {
			if (participante.getPresente()) {
				if(idsOJsParticipantes.containsKey(participante.getSessaoPautaProcessoTrf().getIdSessaoPautaProcessoTrf())) {
					Set<Integer> idOjs = idsOJsParticipantes.get(participante.getSessaoPautaProcessoTrf().getIdSessaoPautaProcessoTrf());
					if(idOjs == null) {
						idOjs = new HashSet<>();
					}
					idOjs.add(participante.getOrgaoJulgador().getIdOrgaoJulgador());
					idsOJsParticipantes.put(participante.getSessaoPautaProcessoTrf().getIdSessaoPautaProcessoTrf(), idOjs);
					
				}
				else {
					Set<Integer> idOjs = new HashSet<>();
					idOjs.add(participante.getOrgaoJulgador().getIdOrgaoJulgador());
					idsOJsParticipantes.put(participante.getSessaoPautaProcessoTrf().getIdSessaoPautaProcessoTrf(), idOjs);
				}
				
			}
		}
		

		return idsOJsParticipantes;
	}

	/**
	 * Recupera os IDs dos OJs participantes/votantes de um processo que ainda
	 * não foi pautado em sessão, levando em consideração a composição atual do
	 * colegiado e os magistrados vinculados ao processo.
	 * 
	 * @see #calcularParticipantesComposicaoJulgamentoProcesso(ProcessoTrf)
	 * 
	 */
	private Set<Integer> obterIDsOJsParticipantesProcessoNaoPautado(ProcessoTrf processo) {
		Set<Integer> idsOJsParticipantes = new HashSet<>();

		try {
			List<ParticipanteComposicaoJulgamentoProcessoDTO> participantes = calcularParticipantesComposicaoJulgamentoProcesso(processo);
			for (ParticipanteComposicaoJulgamentoProcessoDTO participante : participantes) {
				idsOJsParticipantes.add(participante.getOrgaoJulgador().getIdOrgaoJulgador());
			}

		} catch (Exception e) {
			OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent(OrgaoJulgadorManager.class);
			List<OrgaoJulgador> orgaosJulgadoresAtivosColegiado = orgaoJulgadorManager.orgaosPorColegiado(processo.getOrgaoJulgadorColegiado());
			for (OrgaoJulgador ojAtivo : orgaosJulgadoresAtivosColegiado) {
				idsOJsParticipantes.add(ojAtivo.getIdOrgaoJulgador());
			}
		}

		return idsOJsParticipantes;
	}

	/**
	 * Cria o objeto {@link SugestaoAdiamentoJulgamentoProcessoDTO} para
	 * sugestão de adiamentos, levando em consideração o tipo de atuação do
	 * participante na composição do julgamento
	 * 
	 * @see MotivoAdiamentoJulgamentoProcessoEnum
	 * 
	 * @param participanteComposicaoProcessual
	 * @return
	 */
	private SugestaoAdiamentoJulgamentoProcessoDTO criarSugestaoAdiamentoJulgamentoProcesso(SessaoPautaProcessoComposicao participanteComposicaoProcessual) {

		SugestaoAdiamentoJulgamentoProcessoDTO sugestaoAdiamento = new SugestaoAdiamentoJulgamentoProcessoDTO();
		sugestaoAdiamento.setNumeroProcesso(participanteComposicaoProcessual.getSessaoPautaProcessoTrf().getProcessoTrf().toString());

		MotivoAdiamentoJulgamentoProcessoEnum motivoAdiamento = TipoAtuacaoMagistradoEnum.RELAT.equals(participanteComposicaoProcessual.getTipoAtuacaoMagistrado()) ? MotivoAdiamentoJulgamentoProcessoEnum.RELAT_AUSENTE : MotivoAdiamentoJulgamentoProcessoEnum.REVIS_AUSENTE;

		sugestaoAdiamento.setMotivoAdiamento(motivoAdiamento);
		sugestaoAdiamento.setNomeMagistrado(participanteComposicaoProcessual.getMagistradoPresente().toString());

		return sugestaoAdiamento;
	}

	/**
	 * Método que verifica se o processo deve ser adiado por ausência dos
	 * participais principais.
	 */
	private Boolean isProcessoAdiavelPorAusenciaJulgadoresPrincipais(SessaoPautaProcessoComposicao participanteComposicaoProcessual, SessaoComposicaoOrdem componenteSessao) {
		return (participanteComposicaoProcessual.getSessaoPautaProcessoTrf().getAdiadoVista() == null) && (!participanteComposicaoProcessual.getSessaoPautaProcessoTrf().getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG)) && (!componenteSessao.getMagistradoTitularPresenteSessao()) && (!participanteComposicaoProcessual.getTipoAtuacaoMagistrado().equals(TipoAtuacaoMagistradoEnum.VOGAL)) && (participanteComposicaoProcessual.getMagistradoPresente() != null && participanteComposicaoProcessual.getMagistradoPresente().equals(componenteSessao.getMagistradoPresenteSessao()));
	}

	private void atualizarMagistradosAtuantesComposicaoJulgamentoProcessos(Sessao sessao) throws Exception {
		SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager = ComponentUtil.getComponent(SessaoPautaProcessoComposicaoManager.class);

		for (SessaoComposicaoOrdem componenteSessao : sessao.getSessaoComposicaoOrdemList()) {
			List<SessaoPautaProcessoComposicao> participacoesOrgaoJulgadorComposicalProcessual = sessaoPautaProcessoComposicaoManager
					.obterParticipacoesComposicaoProcessual(sessao, componenteSessao.getOrgaoJulgador());

			for (SessaoPautaProcessoComposicao participanteComposicaoProcessual : participacoesOrgaoJulgadorComposicalProcessual) {
				if (!participanteComposicaoProcessual.getSessaoPautaProcessoTrf().getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG)) {
					this.tratarInfomacoesPresenciaisParticipante(participanteComposicaoProcessual, componenteSessao);
					tratarAdiamentoJulgadoresPrincipaisAusentes(participanteComposicaoProcessual, componenteSessao);
				}
			}
		}
	}

	private void tratarInfomacoesPresenciaisParticipante(SessaoPautaProcessoComposicao participanteComposicaoProcessual, SessaoComposicaoOrdem componenteSessao) throws PJeBusinessException {
		Boolean ocorreuAtualizacaoMagistradoParticipante = this.atualizarMagistradoParticipante(participanteComposicaoProcessual, componenteSessao);
		Boolean ocorreuAtualizacaoStatusPresencaParticipante = atualizarStatusPresencaParticipante(participanteComposicaoProcessual, componenteSessao);

		if (ocorreuAtualizacaoMagistradoParticipante || ocorreuAtualizacaoStatusPresencaParticipante) {
			SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager = ComponentUtil.getComponent(SessaoPautaProcessoComposicaoManager.class);
			participanteComposicaoProcessual = sessaoPautaProcessoComposicaoManager.merge(participanteComposicaoProcessual);
			sessaoPautaProcessoComposicaoManager.flush();
		}
	}
	
	/**
	 * Dado um componente de uma composição específica de um processo
	 * <code>participanteComposicaoProcessual</code>, atualiza o magistrado
	 * participante em função da informação referente ao respectivo componente
	 * na sessão <code>componenteSessao</code> Utilizado para alterar um
	 * magistrado vogal participante em função de informação de susbtituto
	 * presencial na sessão.
	 * 
	 * @param participanteComposicaoProcessual
	 *            componente de uma composição de um processo
	 * 
	 * @param componenteSessao
	 *            respectivo componente na sessão.
	 * 
	 * @return true caso efetuar atualização do magistrado participante, false
	 *         caso contrário.
	 * @throws PJeBusinessException 
	 */
	private Boolean atualizarMagistradoParticipante(SessaoPautaProcessoComposicao participanteComposicaoProcessual,
			SessaoComposicaoOrdem componenteSessao) throws PJeBusinessException {
		
		Boolean ocorreuAtualizacao = false;

		if (participanteComposicaoProcessual.getTipoAtuacaoMagistrado().equals(TipoAtuacaoMagistradoEnum.VOGAL)) {
			PessoaMagistrado novoMagistradoComposicaoProcesso = componenteSessao.getMagistradoSubstitutoSessao() != null ? 
					componenteSessao.getMagistradoSubstitutoSessao() : componenteSessao.getMagistradoPresenteSessao();

			if (!novoMagistradoComposicaoProcesso.equals(participanteComposicaoProcessual.getMagistradoPresente())) {
				if (!this.isMagistradoParticipanteComposicaoProcesso(novoMagistradoComposicaoProcesso, participanteComposicaoProcessual.getSessaoPautaProcessoTrf())) {
					participanteComposicaoProcessual.setMagistradoPresente(novoMagistradoComposicaoProcesso);

					UsuarioLocalizacaoMagistradoServidor lotacaoMagistrado = ComponentUtil.getComponent(UsuarioLocalizacaoMagistradoServidorManager.class)
							.obterLocalizacaoAtivaPriorizandoColegiado(novoMagistradoComposicaoProcesso.getIdUsuario(), participanteComposicaoProcessual.getOrgaoJulgador(),
									participanteComposicaoProcessual.getSessaoPautaProcessoTrf().getSessao().getOrgaoJulgadorColegiado());

					if (lotacaoMagistrado == null) {
						String nomeOJ = participanteComposicaoProcessual.getOrgaoJulgador().getOrgaoJulgador();
						String nomeMagistrado = novoMagistradoComposicaoProcesso.getNome();
						throw new PJeBusinessException(
								"Não foi possível encontrar cargo para atuação do(a) magistrado(a) " + nomeMagistrado
										+ " no órgão julgador " + nomeOJ + ".");
					}

					participanteComposicaoProcessual.setCargoAtuacao(lotacaoMagistrado.getOrgaoJulgadorCargo());
					ocorreuAtualizacao = true;
				}
			}
		}

		return ocorreuAtualizacao;
	}

	/**
	 * Verifica se o magistrado informado é participante a votação de um processo.
	 */
	private boolean isMagistradoParticipanteComposicaoProcesso(PessoaMagistrado novoMagistradoComposicaoProcesso,
			SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		
		Boolean isMagistradoParticipante = false;
		for (SessaoPautaProcessoComposicao participanteComposicaoProcessual : sessaoPautaProcessoTrf.getSessaoPautaProcessoComposicaoList()) {
			if (participanteComposicaoProcessual.getMagistradoPresente() != null
					&& participanteComposicaoProcessual.getMagistradoPresente().equals(novoMagistradoComposicaoProcesso)
					&& participanteComposicaoProcessual.getPresente()) {

				isMagistradoParticipante = true;
				break;
			}
		}
		return isMagistradoParticipante;
	}
	
	/**
	 * Atualiza a informação de presente na sessão da composição da sessão
	 * (SessaoComposicaoOrdem) na composição por processo
	 * (SessaoPautaProcessoComposicao), retornando se houve atualização ou não.
	 */
	private Boolean atualizarStatusPresencaParticipante(SessaoPautaProcessoComposicao participanteComposicaoProcessual,
			SessaoComposicaoOrdem componenteSessao) {

		Boolean ocorreuAtualizacao = false;

		Boolean isMagistradoVogal = participanteComposicaoProcessual.getTipoAtuacaoMagistrado().equals(TipoAtuacaoMagistradoEnum.VOGAL);
		Boolean novoStatusMagistradoPresente = componenteSessao.getMagistradoTitularPresenteSessao()
				|| (isMagistradoVogal && componenteSessao.getMagistradoSubstitutoSessao() != null);

		if (!novoStatusMagistradoPresente.equals(participanteComposicaoProcessual.getPresente())) {
			ocorreuAtualizacao = true;
			participanteComposicaoProcessual.setPresente(novoStatusMagistradoPresente);
		}

		return ocorreuAtualizacao;
	}

	private void tratarAdiamentoJulgadoresPrincipaisAusentes(SessaoPautaProcessoComposicao participanteComposicaoProcessual, SessaoComposicaoOrdem componenteSessao) throws Exception {
		if (isProcessoAdiavelPorAusenciaJulgadoresPrincipais(participanteComposicaoProcessual, componenteSessao)) {
			SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
			sessaoPautaProcessoTrfManager.adiar(participanteComposicaoProcessual.getSessaoPautaProcessoTrf());
		}
	}

	/**
	 * Dado um {@link ProcessoTrf} e um {@link OrgaoJulgador} será identificado
	 * o tipo de atuação ({@link TipoAtuacaoMagistradoEnum}) daquele OJ no
	 * processo.
	 */
	public TipoAtuacaoMagistradoEnum obterTipoAtuacaoMagistrado(ProcessoTrf processo, OrgaoJulgador orgaoJulgador) {
		TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado;
		if (orgaoJulgador.equals(processo.getOrgaoJulgador())) {
			tipoAtuacaoMagistrado = TipoAtuacaoMagistradoEnum.RELAT;
		} else if (orgaoJulgador.equals(processo.getOrgaoJulgadorRevisor())) {
			tipoAtuacaoMagistrado = TipoAtuacaoMagistradoEnum.REVIS;
		} else {
			tipoAtuacaoMagistrado = TipoAtuacaoMagistradoEnum.VOGAL;
		}

		return tipoAtuacaoMagistrado;
	}

	public Boolean getIsCircular() {
		return isCircular;
	}

	public void setIsCircular(Boolean isCircular) {
		this.isCircular = isCircular;
	}
	
	/**
	 * Método responsável por atualizar o magistrado presidente do processo pautado na sessão de julgamento
	 * @param processoPautado Processo pautado na sessão de julgamento
	 * @param presidente Presidente da sessão de julgamento
	 */
	public void atualizarMagistradoPresidenteJulgamentoProcesso(SessaoPautaProcessoTrf processoPautado, PessoaMagistrado presidente) throws PJeBusinessException {
		SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
		processoPautado.setPresidente(presidente);
		processoPautado = sessaoPautaProcessoTrfManager.merge(processoPautado);
		sessaoPautaProcessoTrfManager.flush();		
	}
}