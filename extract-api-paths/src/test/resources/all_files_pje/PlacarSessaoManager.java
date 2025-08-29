package br.jus.cnj.pje.nucleo.manager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;

@Name(PlacarSessaoManager.NAME)
public class PlacarSessaoManager {
	
	public static final String NAME = "placarSessaoManager";
	
	private static final String NAO_PROFERIDOS = "Não Proferidos";
	private static final String IMPEDIDOS_SUSPEICAO = "I";
	private LinkedHashMap<String, List<OrgaoJulgador>> agrupadorVotos = new LinkedHashMap<String, List<OrgaoJulgador>>();
	private Sessao sessao;
	private int quantidadeOrgaosPlacar;
	
	@Logger
	private Log logger;
	
	private enum Contextos{
		
		C("Concorda","C"),
		P("Concorda Parcialmente","P"),
		D("Discorda","D"),
		S("Suspeição","S"),
		I("Impedido","I"),
		N("Não conhece","N");
		
		private String descricao;
		private String contexto;
		
		private Contextos(String descricao, String contexto){
			this.descricao = descricao;
			this.contexto = contexto;
		}
		
		public String getDescricao(){
			return this.descricao;
		}
		
		public String getContexto(){
			return this.contexto;
		}
	}	

	private enum Cores{
		
		VERDE("#65b446"),
		AZUL("#0275d8"),
		VERMELHO("#d9534f"),
		AMARELO("#d4d84f"),
		LARANJA("#f0ad4e"),
		VERMELHO_2("#9f2723"),
		CINZA("#999");
		
		private String cor;
		
		private Cores(String cor) {
			this.cor = cor;
		}
		
		public String getCor(){
			return this.cor;
		}
} 
	
	private enum ContextoCoresVotoEnum {
		
		CONCORDA_VERDE(Contextos.C,Cores.VERDE),
		CONCORDA_PARCIALMENTE_AZUL(Contextos.P,Cores.AZUL),
		DISCORDA_VERMELHO(Contextos.D,Cores.VERMELHO),
		SUSPEICAO_AMARELO(Contextos.S,Cores.AMARELO),
		IMPEDIDO_LARANJA(Contextos.I,Cores.LARANJA),
		NAO_CONHECE_VERMELHO_2(Contextos.N,Cores.VERMELHO_2),
		NAO_PROFERIDO_CINZA(null,Cores.CINZA);
		
		private Contextos contexto;
		private Cores cor;
		
		private ContextoCoresVotoEnum(Contextos contexto, Cores cor) {
			this.contexto = contexto;
			this.cor = cor;
		}
		
		public String getDescricaoContexto(){
			if(this.contexto != null){
				return this.contexto.getDescricao();
			}
			return NAO_PROFERIDOS;
		}
		
		public String getCor(){
			return this.cor.getCor();
		}
		
		public String getContexto(){
			if(this.contexto != null){
				return this.contexto.contexto;
			}
			return "";
		}
		
		public static ContextoCoresVotoEnum valorPorContexto(String contexto){
			
			ContextoCoresVotoEnum retorno = null;
			
			if(contexto.equalsIgnoreCase(NAO_PROFERIDOS)){
				retorno = NAO_PROFERIDO_CINZA;
			}else{
				Contextos contextoEnum = Contextos.valueOf(contexto);
				
				switch (contextoEnum) {
				case C:
					retorno = CONCORDA_VERDE;
					break;
				case P:
					retorno = CONCORDA_PARCIALMENTE_AZUL;
					break;
				case D:
					retorno = DISCORDA_VERMELHO;
					break;
				case S:
					retorno = SUSPEICAO_AMARELO;
					break;
				case I:
					retorno = IMPEDIDO_LARANJA;
					break;
				case N:
					retorno = NAO_CONHECE_VERMELHO_2;
					break;
				}
			}
			
			return retorno;
			
		} 
	}
	
	/**
	 * Retorna uma string com o JSON necessário para preenchimento do placar (gráfico) para um determinado processo em julgamento
	 * 
	 * Formato de exemplo do JSON de retorno que será consumido por cada gráfico:
	 * 
	 * {
	 * 	id: 1 //sessaoPautaProcessoTrfId
	 *  dados:[
	 *   {value: 20 , color:  'green', label: {text:'Magistrado X'  ,tooltipLegenda:'Magistrado X\nMagistrado Alfa'    , qtd: 2 ,voto:"Voto X"        ,contexto:'C',isRelator:false}},
	 *   {value: 20 , color:  'red'  , label: {text:'Magistrado Y'  ,tooltipLegenda:'Magistrado Y\nMagistrado Z'       , qtd: 2 ,voto:"Voto Y"        ,contexto:'P',isRelator:false}},
	 *   {value: 20 , color:  'gray' , label: {text:'Não Proferidos',tooltipLegenda:'Magistrado Teta\nMagistrado Beta' , qtd: 2 ,voto:"Não Proferido" ,contexto:'',isRelator:false}},
	 *   ...
	 *  ]
	 * }
	 * 
	 * @param ProcessoTrf, Sessao
	 * 
	 * @return String contento o JSON de dados para preenchimento dos graficos de barra (preview) e pizza (na seleção de um processo na listagem)
	 * @throws JSONException 
	 */
	public String getSessaoPlacar(ProcessoTrf processo, Sessao sessao) throws JSONException{
		
		if(processo == null){
			return null;
		}
		
		if(sessao != null){
			setSessao(sessao);
		}
		
		SessaoPautaProcessoTrf sessaoPautaProcessoTrf = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperaUltimaPautaProcesso(processo);
		
		JSONObject retorno = new JSONObject();
		HashMap<String, List<OrgaoJulgador>> agrupadorVotos = agruparOrgaosJulgadoresPorVotoProferido(processo);
		
		retorno.put("id", processo.getIdProcessoTrf());
		retorno.put("dados", carregarSegmentosGraficoVotos(processo, agrupadorVotos, sessaoPautaProcessoTrf));
		
		return retorno.toString();
		
	}
	
	private JSONArray carregarSegmentosGraficoVotos(ProcessoTrf processoTrf, HashMap<String, List<OrgaoJulgador>> agrupadorVotos, SessaoPautaProcessoTrf sessaoPautaProcessoTrf) throws JSONException {
		JSONArray dados = new JSONArray();
		
		for(String chave : agrupadorVotos.keySet()){
			
			List<OrgaoJulgador> orgaosJulgadoresAgrupados = agrupadorVotos.get(chave);
			String orgaosJulgadoresJoined = "";
			
			if(orgaosJulgadoresAgrupados != null){
				
				for (OrgaoJulgador orgaoJulgador : orgaosJulgadoresAgrupados) {
					orgaosJulgadoresJoined += ComponentUtil.getPlacarSessaoAction().recuperarLabelParticipanteJulgamento(orgaoJulgador.getIdOrgaoJulgador(), processoTrf) + "\n";
				}
				
				float porcentagem = (float)((orgaosJulgadoresAgrupados.size() * 100)/quantidadeOrgaosPlacar);
				ContextoCoresVotoEnum contextoCoresVotoEnum = ContextoCoresVotoEnum.valorPorContexto(chave);

				String textoLegenda = (!chave.equalsIgnoreCase(IMPEDIDOS_SUSPEICAO) && !chave.equalsIgnoreCase(NAO_PROFERIDOS))? (ComponentUtil.getPlacarSessaoAction().recuperarLabelParticipanteJulgamento(orgaosJulgadoresAgrupados.get(0).getIdOrgaoJulgador(), processoTrf)) : contextoCoresVotoEnum.getDescricaoContexto();
				boolean isRelator = processoTrf.getOrgaoJulgador().getOrgaoJulgador().equalsIgnoreCase(orgaosJulgadoresAgrupados.get(0).getOrgaoJulgador());
				boolean isImpedidoNaoProferido = (chave.equalsIgnoreCase(NAO_PROFERIDOS) || chave.equalsIgnoreCase(IMPEDIDOS_SUSPEICAO));

				
				JSONObject segmentoGrafico = new JSONObject();
				segmentoGrafico.put("value", new DecimalFormat("0.00").format(porcentagem).replaceAll(",","."));
				segmentoGrafico.put("color", contextoCoresVotoEnum.getCor());

				segmentoGrafico.put("label", 
					new JSONObject().put("text", textoLegenda)
									.put("tooltipLegenda", orgaosJulgadoresJoined)
									.put("qtd",String.valueOf(orgaosJulgadoresAgrupados.size()) )
									.put("voto", contextoCoresVotoEnum.getDescricaoContexto())	
									.put("contextoVoto", contextoCoresVotoEnum.getContexto())
									.put("idOjAcompanhado", orgaosJulgadoresAgrupados.get(0).getIdOrgaoJulgador())
									.put("isRelator", isRelator)
									.put("isImpedidoNaoProferido", isImpedidoNaoProferido )
									.put("idSessao", (getSessao() != null) ? getSessao().getIdSessao() : "" )
									.put("idSessaoPautaProcessoTrf", (sessaoPautaProcessoTrf != null)? sessaoPautaProcessoTrf.getIdSessaoPautaProcessoTrf() : "")
									
					);
				dados.put(segmentoGrafico);
			}
		}
		return dados;
	}
	
	/**
	 * Agrupa os Órgãos Julgadores de acordo com o tipo de voto proferido para o processo.
	 * 
	 * @param processoTrf
	 * @param orgaosJulgadores
	 * @return HashMap<String, List<String>> com os Órgãos Julgadores agrupados pelo voto proferido, onde a chave do HashMap é o tipo de voto proferido
	 */
	private LinkedHashMap<String, List<OrgaoJulgador>> agruparOrgaosJulgadoresPorVotoProferido(ProcessoTrf processoTrf) {
		List<OrgaoJulgador> orgaosJulgadoresOmissos = new ArrayList<OrgaoJulgador>();
		if (sessao!=null)
			orgaosJulgadoresOmissos.addAll(sessao.getOrgaosJulgadoresPresentes());
		quantidadeOrgaosPlacar = 0;
		List<SessaoProcessoDocumentoVoto> votosPlacar = ComponentUtil.getSessaoProcessoDocumentoVotoDAO().votosProferidosSessao(sessao, processoTrf, true);
		
		LinkedHashMap<String, List<OrgaoJulgador>> agrupadorVotosLocal = new LinkedHashMap<String, List<OrgaoJulgador>>();
		for(SessaoProcessoDocumentoVoto v: votosPlacar){
			quantidadeOrgaosPlacar++;
			orgaosJulgadoresOmissos.remove(v.getOrgaoJulgador());
			if( v.getImpedimentoSuspeicao()) {
				adicionaVoto(v, IMPEDIDOS_SUSPEICAO, agrupadorVotos);
			} else {
				if( v.getOrgaoJulgador() == v.getOjAcompanhado() || v.getOjAcompanhado() == null) { // Esse é o dono do voto
					adicionaVoto(v, v.getTipoVoto().getContexto(), agrupadorVotos);
				} else {
					adicionaVoto(v, v.getTipoVoto().getContexto(), agrupadorVotosLocal);
				}
			}
		}
		
		if(!agrupadorVotosLocal.isEmpty()) {
			for(String valor : agrupadorVotosLocal.keySet()){
				List<OrgaoJulgador> acomp = agrupadorVotos.get(valor);
				if(acomp == null){
					acomp = new ArrayList<OrgaoJulgador>();
				}
				acomp.addAll(agrupadorVotosLocal.get(valor));
				agrupadorVotos.put(valor,acomp);
	        }
		}
		List<SessaoProcessoDocumentoVoto> votosImpedidos = ComponentUtil.getSessaoProcessoDocumentoVotoManager().getImpedidos(sessao, processoTrf);
		for(SessaoProcessoDocumentoVoto v: votosImpedidos){
			quantidadeOrgaosPlacar++;
			orgaosJulgadoresOmissos.remove(v.getOrgaoJulgador());
			adicionaVoto(v, IMPEDIDOS_SUSPEICAO, agrupadorVotos);
		}
		if(processoTrf != null && sessao != null) {
			SessaoPautaProcessoTrf pauta = ComponentUtil.getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoTrf(processoTrf, sessao);
			if( pauta != null ) {
				List <OrgaoJulgador> orgaosImpedidos = ComponentUtil.getSessaoPautaProcessoComposicaoManager().recuperarOrgaosImpedidos(pauta);
				if(orgaosImpedidos != null && !orgaosImpedidos.isEmpty()) {
					adicionaVotoImpedido(agrupadorVotos, orgaosImpedidos);
					orgaosJulgadoresOmissos.removeAll(orgaosImpedidos);
				}
			}
		}
		
		if(orgaosJulgadoresOmissos != null && orgaosJulgadoresOmissos.size() > 0) {
			quantidadeOrgaosPlacar += orgaosJulgadoresOmissos.size();
			agrupadorVotos.put(NAO_PROFERIDOS, orgaosJulgadoresOmissos);
		}
		return agrupadorVotos;
	}
	
	private void adicionaVotoImpedido(LinkedHashMap<String, List<OrgaoJulgador>> agrupadorVotosAdicionar, List<OrgaoJulgador> orgaos) {
		if (!agrupadorVotosAdicionar.containsKey(IMPEDIDOS_SUSPEICAO)) {
			agrupadorVotosAdicionar.put(IMPEDIDOS_SUSPEICAO, orgaos);
		} else {
			agrupadorVotosAdicionar.get(IMPEDIDOS_SUSPEICAO).addAll(orgaos);
		}
	}
			
	private void adicionaVoto(SessaoProcessoDocumentoVoto voto, String chaveTipoVoto, LinkedHashMap<String, List<OrgaoJulgador>> agrupadorVotosAdicionar) {
		if (!agrupadorVotosAdicionar.containsKey(chaveTipoVoto)) {
			List<OrgaoJulgador> listaOrgaosJulgadoresAgrupadosParaEsteTipoVoto = new ArrayList<OrgaoJulgador>();
			listaOrgaosJulgadoresAgrupadosParaEsteTipoVoto.add(voto.getOrgaoJulgador());
			agrupadorVotosAdicionar.put(chaveTipoVoto, listaOrgaosJulgadoresAgrupadosParaEsteTipoVoto);
		} else {
			agrupadorVotosAdicionar.get(chaveTipoVoto).add(voto.getOrgaoJulgador());
		}
	}
		
	public Sessao getSessao() {
		return sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}

}
