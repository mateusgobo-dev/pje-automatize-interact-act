package br.jus.cnj.pje.view;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.json.JSONException;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoComposicaoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.service.ComposicaoJulgamentoService;
import br.jus.cnj.pje.vo.PlacarSessaoVO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoComposicao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;

/**
 * Classe destinada a controlar o componente pje:placarSessao
 * @author Rodrigo Santos Menezes
 *
 */
@Name(PlacarSessaoAction.NAME)
@Scope(ScopeType.PAGE)
public class PlacarSessaoAction {

	public static final String NAME = "placarSessaoAction";
	
	private static final String[] colors = {
			 "#1C75AA", "#AA0D12", "#501193", "#63B8FF", "#CD6090",
			 "#B03060", "#09AA0F", "#B22222", "#CD853F", "#BC8F8F",
			 "#CD5C5C", "#21C3C7", "#0DDA41", "#B3EE3A", "#C9C667",
			 "#FFA500", "#8B5A00", "#FF7256", "#8B3626", "#94200F",
			 "#EDC11C", "#AB82FF", "#715C0D", "#FF9066", "#15B79D",
			 "#90B715", "#B72573", "#C72785", "#E35B98", "#16C159",
			 "#5D750D", "#8B864E"};	
	
	private Map<Integer, Map<Integer, String>> colorsMap;
	private Map<Integer, Set<Integer>> placar;
	private Map<Integer, Map<Integer,String>> mapaCoresOJ;
	private Map<Integer, String> nomesOrgaosJulgadores;

	private int ncolor = 0;
	private ProcessoTrf processoJudicial;
	private final Map<Sessao, List<SessaoProcessoDocumentoVoto>> votosSessao = new HashMap<>();
	private Map<Integer,Set<Integer>> idsOJsParticipantesProcessoSessao = new HashMap<Integer,Set<Integer>>();
	
	@Logger
	private Log logger;
	
	@In
	private SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;
	
	@In
	private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;
	
	@In
	private SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager;
	
	@In
	private ComposicaoJulgamentoService composicaoJulgamentoService;
	
	@Create
	public void init(){
		colorsMap = new HashMap<Integer, Map<Integer, String>>();
	}
	
	public String getSessaoPlacar(ProcessoTrf processo, Sessao sessao,boolean considerarSessao) throws JSONException{
		String retorno = null;
		if(considerarSessao){
			retorno = ComponentUtil.getPlacarSessaoManager().getSessaoPlacar(processo,sessao);
		}else{
			retorno = ComponentUtil.getPlacarSessaoManager().getSessaoPlacar(processo,null);
		}
		return retorno;
	}
	
    public Map<Integer, Set<Integer>> getPlacar(ProcessoTrf processo){
        return getPlacar(processo, true);
    }
    
    public Map<Integer, Set<Integer>> getPlacar(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		if (this.processoJudicial == null || (sessaoPautaProcessoTrf.getProcessoTrf() != null
				&& !this.processoJudicial.equals(sessaoPautaProcessoTrf.getProcessoTrf()))) {

			this.processoJudicial = sessaoPautaProcessoTrf.getProcessoTrf();
			placar = null;
		}

		if (placar == null) {
			placar = computarPlacarComPautaEhSessao(sessaoPautaProcessoTrf);
		}
		return placar;
    }
    
	public Map<Integer, Set<Integer>> getPlacar(ProcessoTrf processo, boolean considerarSessao) {
		if (this.processoJudicial == null || (processo != null && !this.processoJudicial.equals(processo))) {
			this.processoJudicial = processo;
			placar = null;
		}

		if (placar == null) {
			if (considerarSessao) {
				SessaoPautaProcessoTrf sppt = retornaPautaProcesso(processoJudicial);
				if (sppt != null && sppt.getSessao() != null && sppt.getDataExclusaoProcessoTrf() == null) {
					placar = computarPlacarComPautaEhSessao(sppt);
				}
			}
			else{
				placar = computarPlacarSemPautaOuSessao(processoJudicial);
			}
		}
		return placar;
	}
	
	private SessaoPautaProcessoTrf retornaPautaProcesso(ProcessoTrf processoJudicial){
		return sessaoPautaProcessoTrfManager.recuperaUltimaPautaProcesso(processoJudicial);
	}

	private Map<Integer, Set<Integer>> computarPlacarComPautaEhSessao(SessaoPautaProcessoTrf julg) {
		Map<Integer, Set<Integer>> placar = new LinkedHashMap<>();
		
		if (julg == null) {
			return placar;
		}
		
		Sessao sessao = julg.getSessao();
		ProcessoTrf processoTrf = julg.getProcessoTrf();

		if (!this.votosSessao.containsKey(sessao)) {
			this.votosSessao.put(sessao, this.sessaoProcessoDocumentoVotoManager.getVotos(sessao, null, Boolean.TRUE));
			this.idsOJsParticipantesProcessoSessao = this.composicaoJulgamentoService.obterIDsOJsParticipantesProcessosSessao(sessao);
		}
		
		PlacarSessaoVO placarSessaoVO = this.sessaoProcessoDocumentoVotoManager.getPlacarCondutores(this.votosSessao.get(sessao), processoTrf);
		placar = placarSessaoVO.getMapaPlacar();
		
		if (this.mapaCoresOJ == null) {
			this.mapaCoresOJ = new HashMap<>();
		}

		mapaCoresOJ.put(processoTrf.getIdProcessoTrf(), placarSessaoVO.getMapaCor());
		
		Set<Integer> impedidos = this.sessaoProcessoDocumentoVotoManager.getImpedidos(this.votosSessao.get(sessao), processoTrf);
		Set<Integer> omissos = this.sessaoProcessoDocumentoVotoManager.getOmissos( this.votosSessao.get(sessao), sessao, processoTrf,
				this.idsOJsParticipantesProcessoSessao.get(julg.getIdSessaoPautaProcessoTrf()));
		
		omissos.removeAll(impedidos);

		placar.put(-1, omissos);
		placar.put(-2, impedidos);

		return placar;
	}	
	
	private Map<Integer, Set<Integer>> computarPlacarSemPautaOuSessao(ProcessoTrf processoJudicial){
		Map<Integer, Set<Integer>> placar = new LinkedHashMap<Integer, Set<Integer>>();
		PlacarSessaoVO p = sessaoProcessoDocumentoVotoManager.getPlacarCondutores(null, processoJudicial, true);
		placar.putAll(p.getMapaPlacar());
		if(this.mapaCoresOJ == null) {
			this.mapaCoresOJ = new HashMap<Integer, Map<Integer, String>>();
		}
		mapaCoresOJ.put(processoJudicial.getIdProcessoTrf(),p.getMapaCor());
		placar.put(-1, computarPlacarSemPautaEhSessaoOmissos(processoJudicial));
		placar.put(-2, computarPlacarSemPautaEhSessaoImpedidos(processoJudicial));
		placar.get(-1).removeAll(placar.get(-2));
		return placar;

	}	

	private Set<Integer> computarPlacarSemPautaEhSessaoOmissos(ProcessoTrf processoJudicial){
		return sessaoProcessoDocumentoVotoManager.getOmissos(null, processoJudicial, true);
	}
	
	private Set<Integer> computarPlacarSemPautaEhSessaoImpedidos(ProcessoTrf processoJudicial){
		return sessaoProcessoDocumentoVotoManager.getImpedidos(null, processoJudicial, true);
	}	
	
	
	public String getColor(Integer oj, Integer idProcessoJudicial){
		if(colorsMap.get(idProcessoJudicial) == null){
			Map<Integer, String> map = new HashMap<Integer, String>();
			if(mapaCoresOJ.get(idProcessoJudicial) != null && mapaCoresOJ.get(idProcessoJudicial).get(oj) != null){
				map.put(oj,mapaCoresOJ.get(idProcessoJudicial).get(oj));
			}
			else{
				map.put(oj, colors[0]);
			}
			colorsMap.put(idProcessoJudicial, map);
		}else if(colorsMap.get(idProcessoJudicial).get(oj) == null){
			if(mapaCoresOJ.get(idProcessoJudicial).get(oj) != null){
				colorsMap.get(idProcessoJudicial).put(oj, mapaCoresOJ.get(idProcessoJudicial).get(oj));
			}
			else{
				colorsMap.get(idProcessoJudicial).put(oj, colors[ncolor % colors.length]);
			}

		}
		ncolor++;
		return colorsMap.get(idProcessoJudicial).get(oj);
	}
	
	public String recuperarLabelParticipanteJulgamento(Integer idOj, ProcessoTrf processoJudicial) {
		return ParametroUtil.instance().mostrarNomeMagistradoLabelPlacar() ? 
			recuperarNomeMagistradoRepresentante(idOj, processoJudicial) : recuperarNomeOJ(idOj);
	}
	
	public String recuperarNomeOJ(Integer idOj){
		String retorno = "";
		if (nomesOrgaosJulgadores == null){
			nomesOrgaosJulgadores = new HashMap<Integer, String>();
		}
		if (nomesOrgaosJulgadores.get(idOj)!=null){
			retorno = nomesOrgaosJulgadores.get(idOj);
		}else{
			retorno = buscaOrgaoJulgadorPorId(idOj);
		}
		return retorno;
	}

	/** 
	 * Metodo que realiza a pesquisa do nome do orgao julgador
	 * @param idOj
	 * @param retorno
	 * @return String com o nome do orgao julgador
	 */
	private String buscaOrgaoJulgadorPorId(Integer idOj) {
		String retorno = "";
		try {
			OrgaoJulgador oj;
			OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent(OrgaoJulgadorManager.class);
			oj = orgaoJulgadorManager.findById(idOj);
			if(oj != null){
				nomesOrgaosJulgadores.put(idOj, oj.getOrgaoJulgador());
				retorno = oj.getOrgaoJulgador();
			}
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR,"Ocorreu um erro ao tentar recuperar o órgão julgador do processo");
			e.printStackTrace();
		}
		return retorno;
	}

	public String recuperarNomeMagistradoRepresentante(Integer idOJ, ProcessoTrf processoJudicial){
		SessaoPautaProcessoTrf sppt = retornaPautaProcesso(processoJudicial);
		String nomeMagistradoRepresentante = "";
		
		if(sppt != null && 
				sppt.getSessao() != null && 
				sppt.getDataExclusaoProcessoTrf() == null){
			nomeMagistradoRepresentante = recuperarNomeMagistradoRepresentante(sppt, idOJ);
		} else {
			ProcessoMagistradoManager processoMagistradoManager = ComponentUtil.getComponent(ProcessoMagistradoManager.class);
			nomeMagistradoRepresentante = processoMagistradoManager.recuperarNomeMagistradoRepresentante(idOJ, processoJudicial);
		}
		
		return nomeMagistradoRepresentante;
	}

	private String recuperarNomeMagistradoRepresentante(SessaoPautaProcessoTrf sppt, Integer idOJ) {
		String nomeMagistradoRepresentante = "";
		
		for (SessaoPautaProcessoComposicao componente : sppt.getSessaoPautaProcessoComposicaoList()){
			if (componente.getOrgaoJulgador().getIdOrgaoJulgador() == idOJ){
				if(componente.getMagistradoPresente()!= null) {
					nomeMagistradoRepresentante = componente.getMagistradoPresente().getNome();
				} else {
					nomeMagistradoRepresentante = recuperarNomeOJ(idOJ);
				}
				break;
			}
			
		}

		return nomeMagistradoRepresentante;
	}

	public Map<Integer,Map<Integer, String>> getMapaCoresOJ() {
		return mapaCoresOJ;
	}

	public void setMapaCoresOJ(Map<Integer,Map<Integer, String>> mapaCoresOJ) {
		this.mapaCoresOJ = mapaCoresOJ;
	}
	
	public void recalcularPlacar(){
		if(processoJudicial!=null){
			placar = computarPlacarSemPautaOuSessao(processoJudicial);
		}
	}

}
