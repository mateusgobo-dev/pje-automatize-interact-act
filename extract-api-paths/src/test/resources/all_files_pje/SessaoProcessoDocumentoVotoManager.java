package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.SessaoProcessoDocumentoVotoDAO;
import br.jus.cnj.pje.entidades.vo.OrdenarDocumentosVotoProcessoSessaoVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.service.ComposicaoJulgamentoService;
import br.jus.cnj.pje.vo.PlacarSessaoVO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.TipoVotoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;
import java.util.Objects;
import java.util.stream.Collectors;

@Name(SessaoProcessoDocumentoVotoManager.NAME)
public class SessaoProcessoDocumentoVotoManager extends BaseManager<SessaoProcessoDocumentoVoto>{

	public static final String NAME = "sessaoProcessoDocumentoVotoManager";
	
	@In
	private SessaoProcessoDocumentoVotoDAO sessaoProcessoDocumentoVotoDAO;

	@In
	private ComposicaoJulgamentoService composicaoJulgamentoService;
	
	@Override
	protected SessaoProcessoDocumentoVotoDAO getDAO() {
		return sessaoProcessoDocumentoVotoDAO;
	}

	/**
	 * Recupera o voto proferido pelo órgão julgador no processo e na sessão de julgamento especificada.
	 * 
	 * @param sessao a sessão de julgamento pertinente a esse voto
	 * @param processoJudicial o processo judicial em que o voto foi produzido
	 * @param orgaoJulgador o órgão julgador responsável
	 * @return o voto, ou null, se não houver voto que na sessão, processo e órgãos indicados
	 */
	public SessaoProcessoDocumentoVoto recuperarVoto(Sessao sessao, ProcessoTrf processoJudicial, OrgaoJulgador orgaoJulgador) {
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		addCriteria(s, 
				Criteria.equals("sessao", sessao),
				Criteria.equals("processoTrf", processoJudicial),
				Criteria.equals("orgaoJulgador", orgaoJulgador));
		s.addOrder("idSessaoProcessoDocumento", Order.DESC);
		s.setMax(1);
		List<SessaoProcessoDocumentoVoto> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}
	
	/**
	 * Recupera o voto proferido pelo órgão julgador no processo e na sessão de julgamento especificada.
	 * 
	 * @param sessao a sessão de julgamento pertinente a esse voto
	 * @param processoJudicial o processo judicial em que o voto foi produzido
	 * @param orgaoJulgador o órgão julgador responsável
	 * @return o voto, ou null, se não houver voto que na sessão, processo e órgãos indicados
	 */
	public List<SessaoProcessoDocumentoVoto> recuperarVotos(Sessao sessao) {
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		addCriteria(s, 
				Criteria.equals("sessao", sessao));
		s.addOrder("idSessaoProcessoDocumento", Order.DESC);
		List<SessaoProcessoDocumentoVoto> ret = list(s);
		return ret;
	}
	
	public SessaoProcessoDocumentoVoto recuperarVotoAntecipado(Sessao sessao, ProcessoTrf processoJudicial, OrgaoJulgador orgaoJulgador) {
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		addCriteria(s, 
				Criteria.or(Criteria.isNull("sessao"),Criteria.equals("sessao", sessao)),
				Criteria.equals("processoTrf", processoJudicial),
				Criteria.equals("orgaoJulgador", orgaoJulgador));
		s.setMax(1);
		s.addOrder("idSessaoProcessoDocumento", Order.DESC);
		List<SessaoProcessoDocumentoVoto> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}
	
	public SessaoProcessoDocumentoVoto recuperarVoto(Sessao sessao, ProcessoTrf processoJudicial, Integer idOrgaoJulgador) {
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		addCriteria(s, 
				Criteria.equals("sessao", sessao),
				Criteria.equals("processoTrf", processoJudicial),
				Criteria.equals("orgaoJulgador.idOrgaoJulgador", idOrgaoJulgador));
		s.setMax(1);
		s.addOrder("idSessaoProcessoDocumento", Order.DESC);
		List<SessaoProcessoDocumentoVoto> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}
	
	public Long getVotosCount(Sessao sessao, ProcessoTrf processoJudicial, List<Integer> idsOrgaoJulgador) {
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		addCriteria(s, 
				Criteria.equals("sessao", sessao),
				Criteria.equals("processoTrf", processoJudicial),
				Criteria.in("orgaoJulgador.idOrgaoJulgador", idsOrgaoJulgador.toArray()));
		s.setCount(true);
		Long ret = count(s);
		return ret;
	}	

	/**
	 * Recupera o voto proferido pelo órgão julgador no processo e na sessão de julgamento especificada.
	 *
	 * @param sessao a sessão de julgamento pertinente a esse voto
	 * @param processoJudicial o processo judicial em que o voto foi produzido
	 * @param orgaoJulgador o órgão julgador responsável
	 * @return o voto, ou null, se não houver voto que na sessão, processo e órgãos indicados
	 */
	public SessaoProcessoDocumentoVoto recuperarVotoAntecipado(ProcessoTrf processoJudicial, OrgaoJulgador orgaoJulgador) {
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		addCriteria(s, 
				Criteria.isNull("sessao"),
				Criteria.equals("processoTrf", processoJudicial),
				Criteria.equals("orgaoJulgador", orgaoJulgador));
		s.setMax(1);
		s.addOrder("idSessaoProcessoDocumento", Order.DESC);
		List<SessaoProcessoDocumentoVoto> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}
	
	/**
	 * Recupera o número de votos proferidos na sessão e processo informado, especificamente
	 * com o contexto dado {@link TipoVotoEnum#toString()} e que não se trate de um tipo de voto
	 * do relator.
	 * 
	 * @param sessao 
	 * @param processo
	 * @param contexto
	 * @return
	 */
	public long contagemVotos(Sessao sessao, ProcessoTrf processo, String contexto){
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		addCriteria(s,
				Criteria.equals("sessao", sessao),
				Criteria.equals("processoTrf", processo),
				Criteria.equals("tipoVoto.contexto", contexto),
				Criteria.equals("tipoVoto.relator", false));
		return count(s);
	}
	
	/**
	 * Recupera o número total de votos proferidos em uma dada sessão de julgamento
	 * que tenham o contexto do tipo de voto a recuperar e não tenham sido produzidos
	 * pelo órgão indicado.
	 * 
	 * @param sessao
	 * @param tipoVoto
	 * @param orgaoNaoContabilizado
	 * @return
	 */
	public long contagemVotos(Sessao sessao, TipoVotoEnum tipoVoto, OrgaoJulgador orgaoNaoContabilizado){
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		addCriteria(s,
				Criteria.equals("sessao", sessao),
				Criteria.equals("tipoVoto.contexto", tipoVoto.toString()),
				Criteria.not(Criteria.equals("orgaoJulgador", orgaoNaoContabilizado)),
				Criteria.isNull("processoDocumento.dataExclusao"));
		return count(s);
	}
	
	public long contagemVotos(Sessao sessao, ProcessoTrf processoJudicial, TipoVotoEnum tipoVoto, OrgaoJulgador...orgaosExcluidos){
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		addCriteria(s,
				Criteria.equals("processoTrf", processoJudicial),
				Criteria.equals("sessao", sessao),
				Criteria.equals("tipoVoto.contexto", tipoVoto.toString()));
		if(orgaosExcluidos != null && orgaosExcluidos.length > 0){
			addCriteria(s, Criteria.not(Criteria.in("orgaoJulgador", orgaosExcluidos)));
		}
		return count(s);
	}

	public SessaoProcessoDocumentoVoto update(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
		return sessaoProcessoDocumentoVotoDAO.persist(sessaoProcessoDocumentoVoto);
	}

	public SessaoProcessoDocumentoVoto persist(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
		return sessaoProcessoDocumentoVotoDAO.persist(sessaoProcessoDocumentoVoto);
	}

	public SessaoProcessoDocumentoVoto persistirSessaoEAgregados(Sessao sessao,
			SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto, ProcessoTrf processoTrf,
			UsuarioLocalizacao usuarioLocalizacao, OrgaoJulgador orgaoJulgador) {
		SessaoProcessoDocumentoVoto voto = null;
		try {
			voto = this.persist(sessaoProcessoDocumentoVoto);
		} catch (PJeDAOException e) {
			e.printStackTrace();
		}
		return voto;

	}

	public String getMagistradoAssinou(SessaoProcessoDocumentoVoto voto) {
		ProcessoDocumento pd = voto.getProcessoDocumento();
		if(pd == null || pd.getProcessoDocumentoBin() == null){
			return "Sem documento";
		}
		List<ProcessoDocumentoBinPessoaAssinatura> assinaturas = pd.getProcessoDocumentoBin().getSignatarios();
		if (!assinaturas.isEmpty()) {
			return assinaturas.get(0).getPessoa().getAssinatura();
		} else {
			return " - ";
		}
	}

	/**
	 * Verifica se o voto do órgão informado em um dado processo judicial já foi liberado para
	 * os demais membros do colegiado.
	 * É possível indicar a sessão na qual se espera a liberação do voto.
	 * 
	 * @param processo o processo judicial
	 * @param orgao o órgão julgador que potencialmente teria liberado o voto
	 * @param sessao a sessão de julgamento na qual o julgamento pretensamente ocorrerá ou ocorreu
	 * @return true, se o voto do órgão já foi liberado
	 */
	public boolean votoLiberado(ProcessoTrf processo, OrgaoJulgador orgao, Sessao sessao) {
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		addCriteria(s,
				Criteria.equals("processoTrf", processo), 
				Criteria.equals("liberacao", true), 
				Criteria.equals("orgaoJulgador", orgao));
		if(sessao != null){
			addCriteria(s, Criteria.equals("sessao", sessao));
		}
		return count(s) > 0 ? true : false;
	}
	
	public PlacarSessaoVO getPlacarCondutores(Sessao sessao, ProcessoTrf processo, boolean somenteLiberados){
		List<SessaoProcessoDocumentoVoto> votos = getVotos(sessao, processo, somenteLiberados);
		return getPlacarCondutores(votos, processo);
	}
	
	public PlacarSessaoVO getPlacarCondutores(List<SessaoProcessoDocumentoVoto> todosVotos, ProcessoTrf processo){
		List<SessaoProcessoDocumentoVoto> votos = todosVotos.stream()
				.filter(v->processo.equals(v.getProcessoTrf()))
				.collect(Collectors.toList());
		return buildPlacar(votos);
	}

	private PlacarSessaoVO buildPlacar(List<SessaoProcessoDocumentoVoto> votosPlacar) {
		Map<Integer, Set<Integer>> ret = new HashMap<>();
		Map<Integer,String> cores = new HashMap<>();

		ProcessoTrf processo = null;
		Sessao sessao = null;
		
		for(SessaoProcessoDocumentoVoto voto: votosPlacar){
			if (processo == null)
				processo = voto.getProcessoTrf();
			if (processo != null && processo.getIdProcessoTrf() > 0 && voto.getProcessoTrf() != null && voto.getProcessoTrf().getIdProcessoTrf() > 0 && !processo.equals(voto.getProcessoTrf()))
 				throw new PJeRuntimeException("A lista de votos deve ser de um mesmo processo.");
 			
 			if (sessao == null)
 				sessao = voto.getSessao();
			if (sessao != null && sessao.getIdSessao() > 0 && voto.getSessao() != null && voto.getSessao().getIdSessao() > 0 && !Objects.equals(sessao, voto.getSessao()))
				throw new PJeRuntimeException("A lista de votos deve ser de uma mesma sessão.");
 			
			if(voto.getTipoVoto().getContexto().equals("C")){
				Integer idrelator;
				if(voto.getOjAcompanhado() != null) {
					idrelator = voto.getOjAcompanhado().getIdOrgaoJulgador();
				} else {
					idrelator = processo.getOrgaoJulgador().getIdOrgaoJulgador();
				}
				Set<Integer> acomp = ret.get(idrelator);
				if(acomp == null){
					acomp = new HashSet<Integer>();
				}
				acomp.add(voto.getOrgaoJulgador().getIdOrgaoJulgador());
				ret.put(idrelator, acomp);
			}else if(!voto.getImpedimentoSuspeicao()){
				if(voto.getTipoVoto() != null && !StringUtil.isNullOrEmpty(voto.getTipoVoto().getCor())){
					cores.put(voto.getOrgaoJulgador().getIdOrgaoJulgador() ,voto.getTipoVoto().getCor());
				}

				Integer idacompanhado = voto.getOjAcompanhado().getIdOrgaoJulgador();
				Set<Integer> acomp = ret.get(idacompanhado);
				if(acomp == null){
					acomp = new HashSet<Integer>();
				}
				acomp.add(voto.getOrgaoJulgador().getIdOrgaoJulgador());
				ret.put(idacompanhado, acomp);
			}
		}

		PlacarSessaoVO placar = new PlacarSessaoVO(sessao, processo);
		placar.setMapaPlacar(ret);
		placar.setMapaCor(cores);
		return placar;
	}

	/**
	 * @param somenteLiberados
	 * @param s
	 */
	private void verificaCriteriaSomenteLiberados(boolean somenteLiberados,
			Search s) {
		if (somenteLiberados) {
		      addCriteria(s, Criteria.equals("liberacao", true));
	    }
	}
	
	public Set<Integer> getImpedidos(Sessao sessao, ProcessoTrf processo, boolean somenteLiberados){
		List<SessaoProcessoDocumentoVoto> votos = getVotos(sessao, processo, somenteLiberados);
		return getImpedidos(votos, processo);
	}
	
	public Set<Integer> getImpedidos(List<SessaoProcessoDocumentoVoto> todosVotos, ProcessoTrf processo){
		return todosVotos.stream()
				.filter(v->processo.equals(v.getProcessoTrf()) && v.getImpedimentoSuspeicao())
				.map(v->v.getOrgaoJulgador().getIdOrgaoJulgador())
				.collect(Collectors.toSet());
	}
	
	public List<SessaoProcessoDocumentoVoto> getImpedidos(Sessao sessao, ProcessoTrf processo){
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		addCriteria(s, 
				Criteria.equals("sessao", sessao),
				Criteria.equals("processoTrf", processo),
				Criteria.equals("impedimentoSuspeicao", true));		
		return list(s);
	}	

	public Set<Integer> getOmissos(SessaoPautaProcessoTrf processoPautado, boolean somenteLiberados){
		
		Set<Integer> idsOJsParticipantesJulgamentoProcesso = obterIdsOJsParticipantesJulgamento(processoPautado);
		Set<Integer> idsOJsProferiramVoto = obterIDsOrgaosJulgadoresProferiramVoto(processoPautado.getSessao(), processoPautado.getProcessoTrf(), somenteLiberados);
		
		idsOJsParticipantesJulgamentoProcesso.removeAll(idsOJsProferiramVoto);
		
		return idsOJsParticipantesJulgamentoProcesso;
	}
	
	public Set<Integer> getOmissos(Sessao sessao, ProcessoTrf processo, boolean somenteLiberados){
		
		Set<Integer> idsOJsParticipantesJulgamentoProcesso = obterIdsOJsParticipantesJulgamento(sessao, processo);
		Set<Integer> idsOJsProferiramVoto = obterIDsOrgaosJulgadoresProferiramVoto(sessao, processo, somenteLiberados);
		
		idsOJsParticipantesJulgamentoProcesso.removeAll(idsOJsProferiramVoto);
		
		return idsOJsParticipantesJulgamentoProcesso;
	}
	
	public Set<Integer> getOmissos(List<SessaoProcessoDocumentoVoto> votos, Sessao sessao,ProcessoTrf processo){
		
		Set<Integer> idsOJsParticipantesJulgamentoProcesso = obterIdsOJsParticipantesJulgamento(sessao, processo);
		return getOmissos(votos, sessao,processo,idsOJsParticipantesJulgamentoProcesso);
	}
	
	
	public Set<Integer> getOmissos(List<SessaoProcessoDocumentoVoto> votos, Sessao sessao,ProcessoTrf processo,Set<Integer> idsOJsParticipantesJulgamentoProcesso){
		Set<Integer> copy = new HashSet<>(); 
		Set<Integer> idsOJsProferiramVoto = obterIDsOrgaosJulgadoresProferiramVoto(votos, processo);
		if(idsOJsParticipantesJulgamentoProcesso != null) {
			copy = new HashSet<Integer>(idsOJsParticipantesJulgamentoProcesso);
			if(idsOJsProferiramVoto != null) {
				copy.removeAll(idsOJsProferiramVoto);
			}
		}
		return copy;
	}
	
	public List<SessaoProcessoDocumentoVoto> getVotos(Sessao sessao, ProcessoTrf processo, boolean somenteLiberados) {
		return sessaoProcessoDocumentoVotoDAO.votosProferidosSessao(sessao, processo, somenteLiberados);
	}
	
	private Set<Integer> obterIDsOrgaosJulgadoresProferiramVoto(Sessao sessao, ProcessoTrf processo, boolean somenteLiberados) {		
				
		List<SessaoProcessoDocumentoVoto> votosProferidos = getVotos(sessao, processo, somenteLiberados);
		return obterIDsOrgaosJulgadoresProferiramVoto(votosProferidos,processo);
	}
	
	private Set<Integer> obterIDsOrgaosJulgadoresProferiramVoto(List<SessaoProcessoDocumentoVoto> votosProferidos, ProcessoTrf processo) {		
		Set<Integer> idsOJsProferiramVoto = new HashSet<>();

		votosProferidos.stream()
				.filter(v->processo.equals(v.getProcessoTrf()))
				.map(v->v.getOrgaoJulgador().getIdOrgaoJulgador())
				.forEach(o->idsOJsProferiramVoto.add(o));

		return idsOJsProferiramVoto;
	}
		
	private Set<Integer> obterIdsOJsParticipantesJulgamento(Sessao sessao, ProcessoTrf processo) {
		ComposicaoJulgamentoService composicaoJulgamentoService = ComponentUtil.getComponent(ComposicaoJulgamentoService.class);
		return composicaoJulgamentoService.obterIDsOrgaosJulgadoresParticipantesComposicaoJulgamento(sessao, processo);
	}
	
	private Set<Integer> obterIdsOJsParticipantesJulgamento(SessaoPautaProcessoTrf processoPautado) {
		return composicaoJulgamentoService.obterIDsOrgaosJulgadoresParticipantesComposicaoJulgamento(processoPautado);
	}
	
	/**
	 * Metodo responsável por recuperar o voto pelo tipo de voto selecionado
	 * 
	 * @param sessaoPautaProcessoTrf Processo e sessão para consulta
	 * @param tipoVoto tipo de voto a ser recuperado
	 * @param somenteLiberados define se apenas votos liberados virão na resposta
	 * @return Set<Integer> lista com os ids dos registro encontrados'
	 * */
	public Set<Integer> getVotosPorTipo(SessaoPautaProcessoTrf sessaoPautaProcessoTrf, TipoVoto tipoVoto, boolean somenteLiberados) {
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		s.setRetrieveField("orgaoJulgador.idOrgaoJulgador");
		s.setDistinct(true);
		addCriteria(s, 
				Criteria.equals("sessao", sessaoPautaProcessoTrf.getSessao()),
				Criteria.equals("processoTrf", sessaoPautaProcessoTrf.getProcessoTrf()),
				Criteria.equals("tipoVoto", tipoVoto));
		verificaCriteriaSomenteLiberados(somenteLiberados, s);
		List<Integer> idsSessaoProcessoDocumentoVoto = list(s);		
		Set<Integer> idsSessaoProcessoDocumentoVotoOrdenado = new HashSet<Integer>();
		idsSessaoProcessoDocumentoVotoOrdenado.addAll(idsSessaoProcessoDocumentoVoto);
		return idsSessaoProcessoDocumentoVotoOrdenado;		
	}
	
	/**
	 * Metodo responsável por recuperar o voto pelo tipo de voto selecionado
	 * 
	 * @param Sessao Sessão para consulta
	 * @param ProcessoTrf processo
	 * @param tipoVoto tipo de voto a ser recuperado
	 * @param somenteLiberados define se apenas votos liberados virão na resposta
	 * @return Set<Integer> lista com os ids dos registro encontrados'
	 * */
	public Set<Integer> getVotosPorTipo(Sessao sessao, ProcessoTrf processoTrf, TipoVoto tipoVoto, boolean somenteLiberados) {
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		s.setRetrieveField("orgaoJulgador.idOrgaoJulgador");
		s.setDistinct(true);
		addCriteria(s,Criteria.equals("processoTrf", processoTrf),
				Criteria.equals("tipoVoto", tipoVoto));
		verificaCriteriaSessaoNula(sessao, s);
		verificaCriteriaSomenteLiberados(somenteLiberados, s);
		List<Integer> idsSessaoProcessoDocumentoVoto = list(s);		
		Set<Integer> idsSessaoProcessoDocumentoVotoOrdenado = new HashSet<Integer>();
		idsSessaoProcessoDocumentoVotoOrdenado.addAll(idsSessaoProcessoDocumentoVoto);
		return idsSessaoProcessoDocumentoVotoOrdenado;		
	}
	
	/**
	 * Metodo responsável por recuperar o voto pelo contexto D
	 * 
	 * @param Sessao Sessão para consulta
	 * @param ProcessoTrf processo
	 * @param tipoVoto tipo de voto a ser recuperado
	 * @param somenteLiberados define se apenas votos liberados virão na resposta
	 * @return Set<Integer> lista com os ids dos registro encontrados'
	 * */
	public Set<Integer> getVotosPorTipoContextoDivergencia(Sessao sessao, ProcessoTrf processoTrf, boolean somenteLiberados) {
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		s.setRetrieveField("orgaoJulgador.idOrgaoJulgador");
		s.setDistinct(true);
		addCriteria(s, 
				Criteria.equals("processoTrf", processoTrf),
				Criteria.equals("tipoVoto.contexto", "D"));
		verificaCriteriaSessaoNula(sessao, s);
		verificaCriteriaSomenteLiberados(somenteLiberados, s);
		List<Integer> idsSessaoProcessoDocumentoVoto = list(s);		
		Set<Integer> idsSessaoProcessoDocumentoVotoOrdenado = new HashSet<Integer>();
		idsSessaoProcessoDocumentoVotoOrdenado.addAll(idsSessaoProcessoDocumentoVoto);
		return idsSessaoProcessoDocumentoVotoOrdenado;		
	}
	
	/**
	 * @param sessao
	 * @param s
	 */
	private void verificaCriteriaSessaoNula(Sessao sessao, Search s) {
		if (sessao!=null){
			addCriteria(s, Criteria.equals("sessao", sessao));
		}else{
			addCriteria(s, Criteria.isNull("sessao"));
		}
	}
	
	public List<SessaoProcessoDocumentoVoto> getVotosAcompanhantes(SessaoProcessoDocumentoVoto voto,OrgaoJulgador oj){
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		addCriteria(s, Criteria.equals("sessao", voto.getSessao()));
		addCriteria(s, Criteria.equals("processoTrf", voto.getProcessoTrf()));
		addCriteria(s, Criteria.equals("ojAcompanhado", oj));
		return list(s);
	}

	public HashMap<String, Long> recuperarPlacar(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		HashMap<String,Long> placar = new HashMap<String, Long>();
		placar.put("procedente", contagemVotos(sessaoPautaProcessoTrf.getSessao(), sessaoPautaProcessoTrf.getProcessoTrf(), "C"));
		placar.put("parcialmente", contagemVotos(sessaoPautaProcessoTrf.getSessao(), sessaoPautaProcessoTrf.getProcessoTrf(), "P"));
		placar.put("contra", contagemVotos(sessaoPautaProcessoTrf.getSessao(), sessaoPautaProcessoTrf.getProcessoTrf(), "D"));
		return placar;
	}

	public void atualizarSessaoProcessoDocumentosVotos(ProcessoTrf processoTrf, Sessao sessao) {
		List<SessaoProcessoDocumentoVoto> sessaoProcessoDocumentosVotos = getDAO().recuperarSessaoProcessoDocumentosVotosSemSessaoDefinida(processoTrf);
		for (SessaoProcessoDocumentoVoto spdv : sessaoProcessoDocumentosVotos) {
			spdv.setSessao(sessao);
		}
	}

	/**
	 * Recupera o voto de acordo com o argumento informado.
	 * 
	 * @param processoDocumento {@link ProcessoDocumento}
	 * @return Voto do processo.
	 */
	public SessaoProcessoDocumentoVoto recuperarVoto(ProcessoDocumento processoDocumento) {
		return sessaoProcessoDocumentoVotoDAO.recuperarVoto(processoDocumento);
	}
	
	/**
	 * Recupera o voto de acordo com o argumento informado.
	 * 
	 * @param processoDocumento {@link ProcessoDocumento}
	 * @return Voto do processo.
	 */
	public SessaoProcessoDocumentoVoto recuperarVoto(ProcessoDocumento processoDocumento, boolean apenasVoto) {
		return sessaoProcessoDocumentoVotoDAO.recuperarVoto(processoDocumento,apenasVoto);
	}
	
	/**
	 * Recupera lista com votos de acordo com o argumento informado.
	 * @author rafaelmatos
	 * @param sessao {@link Sessao}
	 * @param processoTrf {@link ProcessoTrf}
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-20513
	 * @since 09/06/2015
	 * @return lista com votos.
	 */
	public List<OrdenarDocumentosVotoProcessoSessaoVO> recuperarSessaoVotosComDocumentosPorSessaoEhProcessoVO(Sessao sessao, ProcessoTrf processoTrf) {
		return sessaoProcessoDocumentoVotoDAO.recuperarSessaoVotosComDocumentosPorSessaoEhProcessoVO(sessao,processoTrf);
	}
	
	/**
	 * Metodo que atualiza a ordem do voto.
	 * @author rafaelmatos
	 * @param id da tabela SessaoProcessoMultDocsVoto
	 * @param ordem nova ordem do voto
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-20513
	 * @since 09/06/2015
	 */
	public void atualizaOrdemVoto(Integer id, Integer ordem){
		sessaoProcessoDocumentoVotoDAO.atualizaOrdemVoto(id,ordem);
	}
	
	/**
	 * Recupera o texto da proclamação de julgamento antecipado do
	 * voto do relator, se este for o órgão julgador vencedor.
	 * @param sessaoPautaProcessoTrf 
	 * @param orgaoJulgadorVencedor 
	 * @return String texto da proclamação de julgamento antecipada.
	 */
	public String obterTxtProclamacaoAntecipadaOrgaoVencedor(SessaoPautaProcessoTrf sessaoPautaProcessoTrf, OrgaoJulgador orgaoJulgadorVencedor) {
		String texto = StringUtils.EMPTY;
		
		if(isRelatorProcessoEhOrgaoVencedor(sessaoPautaProcessoTrf, orgaoJulgadorVencedor)) {
			
			SessaoProcessoDocumentoVoto voto = recuperarVotoRelator(sessaoPautaProcessoTrf);
			if(voto != null) {
				texto = StringUtil.removeHtmlTags(voto.getTextoProclamacaoJulgamento());
			}
		}
		
		return texto;
	}
	
	/**
	 * Verifica se o relator do processo é o mesmo do órgão julgador vencedor.
	 * @param sessaoPautaProcessoTrf Objeto que possui o relator do processo que será verificado.
	 * @param orgaoJulgadorVencedor órgão julgador vencedor.
	 * @return true se o relator do processo for o mesmo do órgão julgador vencedor, caso contrário, retorna false.
	 */
	public boolean isRelatorProcessoEhOrgaoVencedor(SessaoPautaProcessoTrf sessaoPautaProcessoTrf, OrgaoJulgador orgaoJulgadorVencedor) {
		return isSessaoPautaProcessoValida(sessaoPautaProcessoTrf) 
				&& isOrgaoJulgadorVencedor(orgaoJulgadorVencedor, sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador());
	}
	
	/**
	 * Recupera o texto da proclamação de julgamento do voto do relator.
	 * @param sessaoPautaProcessoTrf 
	 * @param orgaoJulgadorVencedor 
	 * @return String contendo o texto da proclamação de julgamento.
	 */
	public String recuperarTextoProclamacaoJulgamentoAntecipada(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		String texto = StringUtils.EMPTY;
		if(isSessaoPautaProcessoValida(sessaoPautaProcessoTrf) ) {
			SessaoProcessoDocumentoVoto voto = recuperarVotoRelator(sessaoPautaProcessoTrf);
			if(voto != null) {
				texto = StringUtil.removeHtmlTags(voto.getTextoProclamacaoJulgamento());
			}
		}
		return texto;
	}
	
	public void atualizarTextoDaProclamacaoDoVotoRelator(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		if(isSessaoPautaProcessoValida(sessaoPautaProcessoTrf) ) {
			SessaoProcessoDocumentoVoto voto = recuperarVotoRelator(sessaoPautaProcessoTrf);
			if(voto != null) {
				voto.setTextoProclamacaoJulgamento(sessaoPautaProcessoTrf.getProclamacaoDecisao());
				this.mergeAndFlush(voto);
			}
		}
	}
	
	/**
	 * Verifica se a sessão, o processo e o órgão julgador não estão nulos.
	 * @param sessaoPautaProcessoTrf - Objeto SessaoPautaProcessoTrf que contém os atributos a serem validados.
	 * @return true se os atributos não estiverem nulos.
	 */
	public boolean isSessaoPautaProcessoValida(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.NAME);
		return sessaoPautaProcessoTrfManager.isSessaoPautaProcessoValida(sessaoPautaProcessoTrf);
	}

	/**
	 * Verifica se o órgão julgador vencedor da sessão é igual ao órgão julgador do processo pautado na sessão.
	 * @param orgaoJulgadorVencedor - Órgão julgador vencedor da sessão.
	 * @param orgaoJulgadorProcessoSessao - Órgão julgador do processo pautado na sessão.
	 * @return true se o orgão julgador vencedor for igual ao órgão julgador do processo.
	 */
	public boolean isOrgaoJulgadorVencedor(OrgaoJulgador orgaoJulgadorVencedor, OrgaoJulgador orgaoJulgadorProcessoSessao) {
		return orgaoJulgadorVencedor != null && orgaoJulgadorVencedor.equals(orgaoJulgadorProcessoSessao);
	}
	
	/**
	 * Recupera a sessão voto por sessão e processos.
	 * @param sessao - sessão
	 * @param listProcessoTrf lista de processos
	 * @return List<SessaoProcessoDocumentoVoto>
	 */
	public List<SessaoProcessoDocumentoVoto> recuperarVotosPorSessaoEhProcessos(Sessao sessao, List<ProcessoTrf> listProcessoTrf) {
		return getDAO().recuperarSessaoVotosPorSessaoEhProcessos(sessao, listProcessoTrf);
	}
	
	/**
	 * Recupera o voto do relator.
	 * @param sessaoPautaProcessoTrf
	 * @return SessaoProcessoDocumentoVoto voto do relator.
	 */
	public SessaoProcessoDocumentoVoto recuperarVotoRelator(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		return getDAO().recuperarVotoDoRelator(sessaoPautaProcessoTrf);
	}
	
	public List<SessaoProcessoDocumentoVoto> votosConcluidos(SessaoPautaProcessoTrf julgamento){
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		addCriteria(s, Criteria.equals("sessao", julgamento.getSessao()));
		addCriteria(s, Criteria.equals("processoTrf", julgamento.getProcessoTrf()));
		addCriteria(s, Criteria.equals("confirmado", Boolean.TRUE));
		return list(s);
	}
	
	public OrgaoJulgador contagemMaioriaVotacao(Sessao sessao, ProcessoTrf processo) {
		OrgaoJulgador ojVencedor = getDAO().contagemMaioriaVotacao(sessao, processo);		
	
		return ojVencedor;
		
	}
	
	/**
	 * Ao passar um id de um ProcessoDocumento ira apagar todos os SessaoProcessoDocumentoVoto que tenham este
	 * ProcessoDocumento vinculado.
	 * 
	 * @param idProcessoDocumento id do ProcessoDocumento vinculado.
	 */
	public void remover(Integer idProcessoDocumento) {
		getDAO().remover(idProcessoDocumento);
	}

	public void removerVoto(SessaoPautaProcessoTrf sessaoPauta, OrgaoJulgador orgaoJulgador) throws Exception {
		if(sessaoPauta != null){
			if (orgaoJulgador != null) {
				SessaoProcessoDocumentoVoto spdv = ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVoto(sessaoPauta.getSessao(), sessaoPauta.getProcessoTrf(), orgaoJulgador);
				if(spdv != null){
					if(spdv.getProcessoDocumento() != null){
						ProcessoDocumento pd = spdv.getProcessoDocumento();
						if(pd.getDataJuntada() != null){
							spdv.setProcessoDocumento(null);
						}
					}
					List<SessaoProcessoDocumentoVoto> votosAcompanhantes = ComponentUtil.getSessaoProcessoDocumentoVotoManager().getVotosAcompanhantes(spdv, spdv.getOrgaoJulgador());
					for(SessaoProcessoDocumentoVoto vot : votosAcompanhantes){
						vot.setOjAcompanhado(vot.getOrgaoJulgador());
						ComponentUtil.getSessaoProcessoDocumentoVotoManager().persist(vot);
					}
					ComponentUtil.getSessaoProcessoDocumentoVotoManager().remove(spdv);
					ComponentUtil.getSessaoProcessoDocumentoVotoManager().flush();		
					OrgaoJulgador ojMaioria = ComponentUtil.getSessaoProcessoDocumentoVotoManager().contagemMaioriaVotacao(sessaoPauta.getSessao(), sessaoPauta.getProcessoTrf());
					sessaoPauta.setOrgaoJulgadorVencedor(ojMaioria != null ? ojMaioria : sessaoPauta.getProcessoTrf().getOrgaoJulgador());
	 				ComponentUtil.getSessaoPautaProcessoTrfManager().alterar(sessaoPauta);					
	 			}
			}
		}
	}
	
	public SessaoProcessoDocumentoVoto recuperarVotoExistenteNovo(Sessao sessao, ProcessoTrf processo, OrgaoJulgador orgao) {
		SessaoProcessoDocumentoVoto retorno = this.recuperarVoto(sessao, processo, orgao);
		if(retorno == null) {
			retorno = new SessaoProcessoDocumentoVoto();
		}
		return retorno;
	}
	
	public void registrarVotacao(SessaoPautaProcessoTrf processoPautado, OrgaoJulgador votante, OrgaoJulgador acompanhado, TipoVoto tipoVoto, boolean isRelator ) throws PJeBusinessException{
		SessaoProcessoDocumentoVoto processovoto = recuperarVotoExistenteNovo(processoPautado.getSessao(), processoPautado.getProcessoTrf(), votante);
		processovoto.setProcessoTrf(processoPautado.getProcessoTrf());
		processovoto.setSessao(processoPautado.getSessao());
		processovoto.setOrgaoJulgador(votante);
		processovoto.setTipoVoto(tipoVoto);
		processovoto.setDtVoto(new Date());
		processovoto.setLiberacao(true);
		processovoto.setOjAcompanhado(acompanhado);
		processovoto.setImpedimentoSuspeicao(false);
		this.persistAndFlush(processovoto);
		if(isRelator) {
			ComponentUtil.getDerrubadaVotoManager().analisarTramitacaoFluxoVotoDerrubado(processovoto);
		}
	}
	
	public boolean existeDivergente(SessaoPautaProcessoTrf sessaoPauta) {
		return !(recuperarOrgaosDivergentes(sessaoPauta).isEmpty());
	}
	
	public List<OrgaoJulgador> recuperarOrgaosDivergentes(SessaoPautaProcessoTrf sessaoPauta){
		return getDAO().recuperarOrgaosDivergentes(sessaoPauta);
	}
	
	public static SessaoProcessoDocumentoVotoManager instance() {
    	return ComponentUtil.getComponent(NAME);
    }
}
