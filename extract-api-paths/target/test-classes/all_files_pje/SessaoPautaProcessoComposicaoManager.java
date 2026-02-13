package br.jus.cnj.pje.nucleo.manager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.manager.OrgaoJulgadorColegiadoOrgaoJulgadorManager;
import br.jus.cnj.pje.business.dao.SessaoPautaProcessoComposicaoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.service.ComposicaoJulgamentoService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.dto.ParticipanteComposicaoJulgamentoProcessoDTO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoComposicao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.enums.ComposicaoJulgamentoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(SessaoPautaProcessoComposicaoManager.NAME)
public class SessaoPautaProcessoComposicaoManager extends BaseManager<SessaoPautaProcessoComposicao> {
		
	public static final String NAME = "sessaoPautaProcessoComposicaoManager";
	
	@Logger
	private Log logger;
	
	@Override
	protected SessaoPautaProcessoComposicaoDAO getDAO() {
		SessaoPautaProcessoComposicaoDAO sessaoPautaProcessoComposicaoDAO = ComponentUtil.getComponent(SessaoPautaProcessoComposicaoDAO.class);
		return sessaoPautaProcessoComposicaoDAO;
	}

	/**
	 * Cria a composicao do julgamento da sessao, define os votantes e revisor.
	 * @param sessaoPautaProcessoTrf
	 * @throws Exception
	 */
	public void criarComposicaoJulgamento(SessaoPautaProcessoTrf processoPautado) throws Exception {
		ProcessoTrf processo = processoPautado.getProcessoTrf();
		ComposicaoJulgamentoService composicaoJulgamentoService = ComponentUtil.getComponent(ComposicaoJulgamentoService.class);
		List<ParticipanteComposicaoJulgamentoProcessoDTO> participantesComposicaoJulgamentoProcesso = composicaoJulgamentoService.calcularParticipantesComposicaoJulgamentoProcesso(processo);
		
		for (ParticipanteComposicaoJulgamentoProcessoDTO participanteComposicaoJulgamentoProcesso : participantesComposicaoJulgamentoProcesso) {
			criarItemComposicaoJulgamentoProcesso(processoPautado, participanteComposicaoJulgamentoProcesso);
		}
	}
	
	public void criarItemComposicaoJulgamentoProcesso(SessaoPautaProcessoTrf processoPautado,
			ParticipanteComposicaoJulgamentoProcessoDTO participanteComposicaoJulgamentoProcesso) {
		SessaoPautaProcessoComposicao composicao = new SessaoPautaProcessoComposicao();
		composicao.setSessaoPautaProcessoTrf(processoPautado);
		composicao.setImpedidoSuspeicao(false);
		composicao.setOrgaoJulgador(participanteComposicaoJulgamentoProcesso.getOrgaoJulgador());
		composicao.setCargoAtuacao(participanteComposicaoJulgamentoProcesso.getCargoVinculacao());
		composicao.setTipoAtuacaoMagistrado(participanteComposicaoJulgamentoProcesso.getTipoAtuacaoMagistrado());
		composicao.setMagistradoPresente(participanteComposicaoJulgamentoProcesso.getMagistrado());
		composicao.setPresente(true);
		composicao.setOrdemVotacao(participanteComposicaoJulgamentoProcesso.getOrdemDeVotacao());
		processoPautado.getSessaoPautaProcessoComposicaoList().add(composicao);
	}
	
	
	public Set<OrgaoJulgador> recuperarOrgaosJulgadoresDaComposicaoJulgamento(ProcessoTrf processoTrf) throws Exception {
		
		if (ComposicaoJulgamentoEnum.R.equals(processoTrf.getComposicaoJulgamento())) {
			return recuperarOrgaosJulgadoresDaComposicaoJulgamentoReduzida(processoTrf);
		}
		else {
			OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent(OrgaoJulgadorManager.class);
			return new HashSet<OrgaoJulgador>(orgaoJulgadorManager.orgaosPorColegiado(processoTrf.getOrgaoJulgadorColegiado()));
		}
	}
	
	public Set<OrgaoJulgador> recuperarOrgaosJulgadoresDaComposicaoJulgamentoReduzida(ProcessoTrf processo) throws Exception {

		Set<OrgaoJulgador> orgaosParticipantes = new HashSet<OrgaoJulgador>();
		orgaosParticipantes.add(processo.getOrgaoJulgador()); // Relator
		
		Integer quantJulgCompReduzida = processo.getOrgaoJulgadorColegiado().getQuantidadeJulgadoresComposicaoReduzida();
		
		if (quantJulgCompReduzida == orgaosParticipantes.size()) {
			return orgaosParticipantes;
		}
					
		OrgaoJulgador orgaoJulgadorReferencia = processo.getOrgaoJulgador();
		OrgaoJulgador orgaoJulgadorRevisorProcesso = processo.getOrgaoJulgadorRevisor();

		if(orgaoJulgadorRevisorProcesso != null) {
			orgaoJulgadorReferencia = orgaoJulgadorRevisorProcesso;
			orgaosParticipantes.add(orgaoJulgadorReferencia);
		}
		OrgaoJulgadorColegiadoOrgaoJulgadorManager ojcojManager = ComponentUtil.getComponent(OrgaoJulgadorColegiadoOrgaoJulgadorManager.class);

		int qtdOrgaosParticipantes =  orgaosParticipantes.size();
		int numTentativasRecursivas = 0;
		while (qtdOrgaosParticipantes < quantJulgCompReduzida) {
			OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorComposicao = 
					ojcojManager.obterProximoOrgaoJulgadorComposicaoDadoUmOrgaoJulgadorReferencia(orgaoJulgadorReferencia, processo.getOrgaoJulgadorColegiado());
			if(orgaoJulgadorComposicao != null) {
				orgaoJulgadorReferencia = orgaoJulgadorComposicao.getOrgaoJulgador();
				if(!orgaosParticipantes.contains(orgaoJulgadorReferencia)) {
					orgaosParticipantes.add(orgaoJulgadorReferencia);
					qtdOrgaosParticipantes++;
				}else {
					if(numTentativasRecursivas > 3) {
						lancaExpecptionSemRevisor(orgaoJulgadorComposicao);
						break;
					}
					numTentativasRecursivas++;
				}
			}else {
				lancaExpecptionSemRevisor(orgaoJulgadorComposicao);
				break;
			}
		}			
	
		return orgaosParticipantes;
	}
	
	public OrgaoJulgadorColegiadoOrgaoJulgador recuperarPorOrgaojulgadorColegiadoEhOrgaoJulgador(OrgaoJulgadorColegiado orgaoJulgadorColegiado, OrgaoJulgador orgaoJulgador) throws Exception {
		OrgaoJulgadorColegiadoOrgaoJulgador ojcOrgaoJulgador = OrgaoJulgadorColegiadoOrgaoJulgadorManager.instance()
				.recuperarPorOrgaoJulgadorColegiadoEhOrgaoJulgador(orgaoJulgadorColegiado, orgaoJulgador);
		
		if (ojcOrgaoJulgador == null) {
			throw new Exception(MessageFormat.format("Não foi possível recuperar o cadastro do Orgão Julgador(id:{0}, nome:{1}) no Colegiado(id:{2}, nome:{3})!"
					, orgaoJulgador.getIdOrgaoJulgador()
					, orgaoJulgador.getOrgaoJulgador()
					, orgaoJulgadorColegiado.getIdOrgaoJulgadorColegiado()
					, orgaoJulgadorColegiado.getOrgaoJulgadorColegiado()
			));
		}
		else {		
			return ojcOrgaoJulgador;
		}
	}

	private void lancaExpecptionSemRevisor(	OrgaoJulgadorColegiadoOrgaoJulgador ojcOrgaoJulgadorAtual)	throws PJeBusinessException {
		throw new PJeBusinessException(MessageFormat.format("Não foi possível recuperar o cadastro do revisor do Orgão Julgador(id:{0}, nome:{1}) no Colegiado(id:{2}, nome:{3})!"
				, ojcOrgaoJulgadorAtual.getOrgaoJulgador().getIdOrgaoJulgador()
				, ojcOrgaoJulgadorAtual.getOrgaoJulgador().getOrgaoJulgador()
				, ojcOrgaoJulgadorAtual.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado()
				, ojcOrgaoJulgadorAtual.getOrgaoJulgadorColegiado().getOrgaoJulgadorColegiado()
		));
	}

		
	public void salvarComposicaoComDefinicaoDoUsuario(SessaoPautaProcessoComposicao sppc) throws Exception {
		if (sppc.getOrdemVotacao() != null) {
			for (SessaoPautaProcessoComposicao sppcTemp : sppc.getSessaoPautaProcessoTrf()
					.getSessaoPautaProcessoComposicaoList()) {
				if (sppcTemp != sppc && sppcTemp.getOrdemVotacao() != null
						&& sppcTemp.getOrdemVotacao().equals(sppc.getOrdemVotacao())) {

					throw new Exception(
							"A ordem de votação \"" + sppc.getOrdemVotacao() + "\" já está sendo utilizada.");

				}
			}
		}
		sppc.setDefinidoPorUsuario(true);
		merge(sppc);
		flush();
	}

	/**
	 * Altera a presenca do magistrado na sessao para todas as composicoes dos processos da sessao
	 * 
	 * @param sessaoComposicaoOrdem
	 */
	public void atualizarPresencaDoMagistradoNasComposicoesDosProcessosDaSessao(SessaoComposicaoOrdem sessaoComposicaoOrdem) {
		getDAO().atualizarPresencaDoMagistradoNasComposicoesDosProcessosDaSessao(sessaoComposicaoOrdem);
	}
	
    /**
     * Atualiza o magistrado 'presente' das composicoes dos processos da sessão passada por parâmetro.
     *
     * @param sessaoComposicaoOrdem SessaoComposicaoOrdem
     */
	public void atualizarMagistradoPresenteNasComposicoesDosProcessosDaSessao(SessaoComposicaoOrdem sessaoComposicaoOrdem) {
		getDAO().atualizarMagistradoPresenteNasComposicoesDosProcessosDaSessao(sessaoComposicaoOrdem);
	}
	
    /**
     * Atualiza o magistrado 'substituto' das composicoes dos processos da sessão passada por parâmetro.
     *
     * @param sessaoComposicaoOrdem SessaoComposicaoOrdem
     */
	public void atualizarMagistradoSubstitutoNasComposicoesDosProcessosDaSessao(SessaoComposicaoOrdem sessaoComposicaoOrdem) {
		getDAO().atualizarMagistradoSubstitutoNasComposicoesDosProcessosDaSessao(sessaoComposicaoOrdem);
	}
	
	/**
	 * @author rafaelmatos
	 * @see Metodo responsável por retorna uma lista com os objetos SessaoPautaProcessoComposicao
	 * a partir de um processo.
	 * @since 23/07/2015
	 * @param ProcessoTrf processoTrf
	 * @return lista com dados da SessaoPautaProcessoComposicao
	 * @throws Exception 
	 */
	public Set<SessaoPautaProcessoComposicao> recuperarSessaoPautaProcessoComposicaoPorProcesso(
			ProcessoTrf processoTrf, Sessao sessao) throws Exception {
		SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
		return new HashSet<SessaoPautaProcessoComposicao>(sessaoPautaProcessoTrfManager.getSessaoPautaProcessoTrf(processoTrf, sessao).getSessaoPautaProcessoComposicaoList());
	}
	
	/**
	 * Metodo que retorna lista dos Órgãos Julgadores Impedido/Suspeicao por Sessao Pauta Processo
	 * @param sessaoPautaProcessoTrf
	 */
	public Set<Integer> recuperarOrgaosJulgadoresDaComposicaoJulgamentoImpedidoSuspeicaoPorSessaoPautaProcessoTrf(
			SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		return getDAO().recuperarOrgaosJulgadoresDaComposicaoJulgamentoImpedidoSuspeicaoPorSessaoPautaProcessoTrf(sessaoPautaProcessoTrf);
	}	
	
	/**
	 * Retorna todas as participações em composição processual que um dado órgão julgador <code>orgaoJulgador</code>
	 * possui nos processos pautados em uma dada sessão <code>sessao</code> 
	 * @param sessao
	 * @param orgaoJulgador
	 * @return
	 */
	public List<SessaoPautaProcessoComposicao> obterParticipacoesComposicaoProcessual(Sessao sessao, OrgaoJulgador orgaoJulgador){
		return getDAO().obterParticipacoesComposicaoProcessual(sessao, orgaoJulgador);
	}

	public List<SessaoPautaProcessoComposicao> findBySessaoPautaProcessoTrf(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		return getDAO().findBySessaoPautaProcessoTrf(sessaoPautaProcessoTrf, false);
	}
	
	public List<SessaoPautaProcessoComposicao> findBySessaoPautaProcessoTrf(SessaoPautaProcessoTrf sessaoPautaProcessoTrf, Boolean somentePresentes) {
		return getDAO().findBySessaoPautaProcessoTrf(sessaoPautaProcessoTrf, somentePresentes);
	}
	
	
	public void insereComposicaoProcesso(SessaoPautaProcessoComposicao composicao){
		getDAO().persist(composicao);
	}
	
	public SessaoPautaProcessoComposicao updateComposicaoProcesso(SessaoPautaProcessoComposicao composicao){
		return getDAO().merge(composicao);
	}
	
	public void removeComposicaoProcesso(SessaoPautaProcessoComposicao composicao){
		getDAO().remove(composicao);
	}
	
	/**
	 * Metodo que atualiza os votos impedidos/suspeicao no painel do secretario da sessao realizado pelo
	 * magistrado na votacao vogal
	 * @param idsOrgaoJulgadoresImpedidos
	 * @param sessaoPautaProcessoTrf
	 */
	public void atualizaVotosImpedidos(List<Integer> idsOrgaoJulgadoresImpedidos, SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		getDAO().atualizaVotosImpedidos(idsOrgaoJulgadoresImpedidos,sessaoPautaProcessoTrf);
	}
    
	/**
	 * Recupera uma lista de SessaoPautaProcessoComposicao ordenadode acordo com
	 * o parâmetro de ordenação
	 * 
	 * @param sessaoPautaProcessoTrf
	 * @return
	 */
	public List<SessaoPautaProcessoComposicao> getListaSessaoPautaProcessoComposicaoOrdenado(
			SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		List<SessaoPautaProcessoComposicao> sessaoPautaProcessoComposicaoList = getDAO().findBySessaoPautaProcessoTrf(sessaoPautaProcessoTrf, true);
		
		String ordenador = ParametroUtil.getParametro(Parametros.ATRIBUTO_ORDENADOR_SESSAO_PAUTA_PROC_COMPOSICAO);
		if (StringUtil.isSet(ordenador)) {
			try {
				sessaoPautaProcessoComposicaoList = CollectionUtilsPje.sortCollection(sessaoPautaProcessoComposicaoList,
						true, ordenador);

			} catch (Exception e) {

				logger.error("Parâmetro "+ ordenador+" é  inválido para ordenação.");

			}
		}
		return sessaoPautaProcessoComposicaoList;
	}
	
	private String obterTextoComposicaoJulgamento(List<SessaoPautaProcessoComposicao> composicao) {
		List<String> nomesMagistrados = new ArrayList<String>(composicao.size());
		for (SessaoPautaProcessoComposicao orgaoComposicao : composicao) {
			if(!orgaoComposicao.getImpedidoSuspeicao()) {
				nomesMagistrados.add(orgaoComposicao.getMagistradoPresente().toString());
			}
		}
		Collections.sort(nomesMagistrados);
		return StringUtils.join(nomesMagistrados,", ");
	}
	
	public String retornaComposicaoSessaoJulgamentoPorProcesso(ProcessoTrf processoTrf, Sessao sessao, boolean comRelator){
		String retorno = StringUtils.EMPTY;
		ProcessoTrf processo = ComponentUtil.getProcessoTrfManager().recuperarProcesso(processoTrf);
		if( processo != null && processo.getIdProcessoTrf() > 0) {
			Sessao sessaoAtual = ComponentUtil.getSessaoManager().recuperarSessao(sessao);
			if( sessaoAtual != null && sessaoAtual.getIdSessao() > 0) {
				SessaoPautaProcessoTrf sessaoPauta = ComponentUtil.getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoTrf(processo, sessaoAtual);
				List<SessaoPautaProcessoComposicao> composicao = ComponentUtil.getSessaoPautaProcessoComposicaoManager().findBySessaoPautaProcessoTrf(sessaoPauta, true); 
				if(!composicao.isEmpty()) {
					if(!comRelator) {
						List<SessaoPautaProcessoComposicao> composicaoCaminhar = new ArrayList<SessaoPautaProcessoComposicao>();
						composicaoCaminhar.addAll(composicao);
						for( SessaoPautaProcessoComposicao componente : composicaoCaminhar) {
							if( componente.getOrgaoJulgador() == processo.getOrgaoJulgador() ) {
								composicao.remove(componente);
								break;
							}
						}
					} 
					retorno = obterTextoComposicaoJulgamento(composicao);
				}
			}
		}
		return retorno;
	}
	
	public List<OrgaoJulgador> recuperarOrgaosImpedidos(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		Search s = new Search(SessaoPautaProcessoComposicao.class);
		addCriteria(s,
				Criteria.equals("sessaoPautaProcessoTrf", sessaoPautaProcessoTrf),
				Criteria.equals("impedidoSuspeicao", true));
		s.setRetrieveField("orgaoJulgador");
		return list(s);
	}
	
	public boolean verificarImpedimentoSuspeicao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf, OrgaoJulgador orgao) {
		Search s = new Search(SessaoPautaProcessoComposicao.class);
		addCriteria(s,
				Criteria.equals("sessaoPautaProcessoTrf", sessaoPautaProcessoTrf),
				Criteria.equals("orgaoJulgador", orgao),
				Criteria.equals("impedidoSuspeicao", true));
		return list(s).isEmpty() ? false : true;
	}
	
	public void atualizarImpedimento(SessaoPautaProcessoTrf sessaoPautaProcessoTrf, OrgaoJulgador orgao, boolean impedimento) {
		Search s = new Search(SessaoPautaProcessoComposicao.class);
		addCriteria(s,
				Criteria.equals("sessaoPautaProcessoTrf", sessaoPautaProcessoTrf),
				Criteria.equals("orgaoJulgador", orgao));
		List <SessaoPautaProcessoComposicao> lista = list(s);
		if(lista != null && !lista.isEmpty()) {
			SessaoPautaProcessoComposicao composicao =  lista.get(0);
			composicao.setImpedidoSuspeicao(impedimento);
			mergeAndFlush(composicao);
		}
	}
}