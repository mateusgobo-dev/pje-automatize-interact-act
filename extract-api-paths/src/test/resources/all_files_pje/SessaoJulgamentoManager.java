/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.dao.OrgaoJulgadorColegiadoOrgaoJulgadorDAO;
import br.jus.cnj.pje.business.dao.SessaoJulgamentoDAO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioPesquisa;
import br.jus.cnj.pje.webservice.json.InformacaoUsuarioSessao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.min.SessaoJulgamentoMin;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente destinado a permitir a realização de atividades negociais em sessões de julgamento.
 * 
 * @author cristof
 *
 */
@Name("sessaoJulgamentoManager")
public class SessaoJulgamentoManager extends BaseManager<Sessao> {
	
	@In
	private SessaoJulgamentoDAO sessaoJulgamentoDAO;
	
	@In
	private OrgaoJulgadorManager orgaoJulgadorManager;
	
	@Logger
	private Log logger;

	@Override
	protected SessaoJulgamentoDAO getDAO() {
		return sessaoJulgamentoDAO;
	}
	
	public List<Sessao> recuperaPendentesFechamentoAutomaticoPauta(Date dataReferencia){
		return sessaoJulgamentoDAO.findSessoesPautasPendentesFechamentoAutomatico(dataReferencia);
	}
	
	public List<Integer> recuperaIdsPendentesFechamentoAutomaticoPauta(Date dataReferencia){
		return sessaoJulgamentoDAO.findIdsSessoesPautasPendentesFechamentoAutomatico(dataReferencia);
	}
	
	/**
	 * Funciona da mesma forma que <code>getUltimaSessaoAberta(ProcessoTrf, TipoSituacaoPautaEnum)</code>
	 * com a diferença de que assume a situacaoPauta como sendo "aguardando julgamento"
	 * @see getUltimaSessaoAberta(ProcessoTrf, TipoSituacaoPautaEnum) 
	 */
	public Sessao getUltimaSessaoAberta(ProcessoTrf processo){
		return getUltimaSessaoAberta(processo, TipoSituacaoPautaEnum.AJ);
	}
	
	
	/**
	 * Funciona da mesma forma que <code>getUltimaSessaoAberta(ProcessoTrf, TipoSituacaoPautaEnum)</code>
	 * com a diferença de que assume a situacaoPauta como sendo nulo, objetivando
	 * não utilizar filtro de situacaoPauta.
	 * @see getUltimaSessaoAberta(ProcessoTrf, TipoSituacaoPautaEnum) 
	 */
	public Sessao getUltimaSessaoProcessoPautado(ProcessoTrf processo){
		return getUltimaSessaoAberta(processo, null);
	}
	
	
	/**
	 * Dado um processo, retorna a ultima sessão em que ele foi pautado, filtrando
	 * pela situacao desse processo na sessão.
	 * Caso o parametro <code>situacaoPauta</code> for nulo, então não filtra por situacao.
	 * @param processo processo cuja sessão de julgamento está sendo procurada
	 * @param situacaoPauta situacao de julgamento a ser filtrado (caso diferente de nulo)
	 * @return a sessão mais recente em que esse processo foi pautado  
	 */
	private Sessao getUltimaSessaoAberta(ProcessoTrf processo, TipoSituacaoPautaEnum situacaoPauta){
		Search s = new Search(SessaoPautaProcessoTrf.class);
		addCriteria(s, Criteria.equals("processoTrf", processo),
					   Criteria.isNull("dataExclusaoProcessoTrf"));	   
		
		if (situacaoPauta != null){
			addCriteria(s, Criteria.equals("situacaoJulgamento", situacaoPauta));
		}
		
		s.addOrder("o.dataInclusaoProcessoTrf", Order.DESC);
		s.setMax(1);
		s.setRetrieveField("sessao");
		List<Sessao> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}
	
    /**
	 * Método responsável por lista as sessões do processo que está pendente de
	 * julgamento
	 * 
	 * @param processoJudicial
	 *            {@link ProcessoTrf} a ser pesquisado nas sessões.
	 * 
	 * @return {@link List} de {@link Sessao} onde o processo está pendentes de
	 *         julgamento
	 */
	public List<Sessao> getSessoesPendentes(ProcessoTrf processo){
		
		TipoSituacaoPautaEnum[] situacoes = {TipoSituacaoPautaEnum.AJ,TipoSituacaoPautaEnum.EJ};
		
		Search s = new Search(SessaoPautaProcessoTrf.class);
		addCriteria(s, Criteria.equals("processoTrf", processo),
					   Criteria.isNull("dataExclusaoProcessoTrf"),
					   Criteria.in("situacaoJulgamento", situacoes));
		s.addOrder("o.sessao.dataSessao", Order.DESC);
		s.setRetrieveField("sessao");
		
		List<Sessao> ret = list(s);
		return ret;
	}
	
	public String getProcessosSemJulgamento(int idSessao) {
		return sessaoJulgamentoDAO.getProcessosSemJulgamento(idSessao);
	}
	
	public String getProcessosEmJulgamento(int idSessao) {
		return sessaoJulgamentoDAO.getProcessosEmJulgamento(idSessao);
	}

	public String getProcessosJulgados(int idSessao) {
		return sessaoJulgamentoDAO.getProcessosJulgados(idSessao);
	}

	public String getVista(int idSessao) {
		return sessaoJulgamentoDAO.getVista(idSessao);
	}

	public String getAdiado(int idSessao) {
		return sessaoJulgamentoDAO.getAdiado(idSessao);
	}

	public String getRetiradoJulgamento(int idSessao) {
		return sessaoJulgamentoDAO.getRetiradoJulgamento(idSessao);
	}
		
	public OrgaoJulgadorColegiadoOrgaoJulgadorDAO getOrgaoJulgadorColegiadoOrgaoJulgadorDAO() {
		return ComponentUtil.getComponent(OrgaoJulgadorColegiadoOrgaoJulgadorDAO.NAME);
	}

	/**
	 * Consulta os processos julgados da sessão.
	 * 
	 * @param sessao Sessão.
	 * @return processos julgados.
	 */
	public List<SessaoPautaProcessoTrf> consultarJulgados(Sessao sessao) {
		List<SessaoPautaProcessoTrf> resultado = new ArrayList<SessaoPautaProcessoTrf>();

		if (sessao != null) {
			resultado = getDAO().consultarJulgados(sessao);
		}
		return resultado;
	}
	
	/**
	 * Consulta os processos retirados da sessão.
	 * 
	 * @param sessao Sessão.
	 * @return processos retirados.
	 */
	public List<SessaoPautaProcessoTrf> consultarRetirados(Sessao sessao) {
		List<SessaoPautaProcessoTrf> resultado = new ArrayList<SessaoPautaProcessoTrf>();

		if (sessao != null) {
			resultado = getDAO().consultarRetirados(sessao);
		}
		return resultado;
	}
	
	/**
	 * Consulta os processos com pedido de vista da sessão.
	 * 
	 * @param sessao Sessão.
	 * @return processos com pedido de vista.
	 */
	public List<SessaoPautaProcessoTrf> consultarPedidosVista(Sessao sessao) {
		List<SessaoPautaProcessoTrf> resultado = new ArrayList<SessaoPautaProcessoTrf>();

		if (sessao != null) {
			resultado = getDAO().consultarPedidosVista(sessao);
		}
		return resultado;
	}
	
	/**
	 * Consulta os processos adiados da sessão.
	 * 
	 * @param sessao Sessão.
	 * @return processos adiados.
	 */
	public List<SessaoPautaProcessoTrf> consultarAdiados(Sessao sessao) {
		List<SessaoPautaProcessoTrf> resultado = new ArrayList<SessaoPautaProcessoTrf>();

		if (sessao != null) {
			resultado = getDAO().consultarAdiados(sessao);
		}
		return resultado;
	}
	
	public List<Sessao> recuperaContinuasPorDataFim(Date data){
		Search s = new Search(Sessao.class);
		addCriteria(s, Criteria.equals("continua", true),
					   Criteria.equals("dataFimSessao",data));
 		return list(s);
	}
	
	public List<Sessao> recuperaContinuasPorDataInicio(Date data){
		Search s = new Search(Sessao.class);
		addCriteria(s, Criteria.equals("continua", true),
						Criteria.not(Criteria.isNull("dataFechamentoPauta")),
					   Criteria.equals("dataSessao",data));
 		return list(s);
	}
	
	/**
	 * Método responsável por recuperar a última sessão de julgamento em que o processo foi pautado.
	 * 
	 * @param processoTrf Dados do processo.
	 * return A última sessão de julgamento em que o processo foi pautado ou nulo se: <br>
	 * 		- O processo não foi pautado; <br>
	 * 		- O processo foi pautado e consta como não julgado <br> 
	 * 		- O processo foi pautado, consta como julgado mas não possui acórdão assinado.
	 */
	public Sessao getSessaoJulgamento(ProcessoTrf processoTrf) {
		Sessao ultimaSessaoProcessoPautado = getUltimaSessaoProcessoPautado(processoTrf);
		if (ultimaSessaoProcessoPautado != null) {
			SessaoPautaProcessoTrf sessaoPautaProcessoTrf = ComponentUtil.getSessaoPautaProcessoTrfManager()
						.getSessaoPautaProcessoTrf(processoTrf, ultimaSessaoProcessoPautado);
			
			if (TipoSituacaoPautaEnum.NJ.equals(sessaoPautaProcessoTrf.getSituacaoJulgamento()) || 
					(TipoSituacaoPautaEnum.JG.equals(sessaoPautaProcessoTrf.getSituacaoJulgamento()) && 
							ComponentUtil.getSessaoProcessoDocumentoManager().isAcordaoAssinado(processoTrf, ultimaSessaoProcessoPautado))) {
				
				ultimaSessaoProcessoPautado = null;
			}
		}
		return ultimaSessaoProcessoPautado;
	}	
	
    public List<SessaoJulgamentoMin> buscarSessoes(InformacaoUsuarioSessao usuarioSessao, CriterioPesquisa crit){
        if(usuarioSessao == null){
            return null;
        }
        return sessaoJulgamentoDAO.buscarSessoes(usuarioSessao.getIdOrgaoJulgadorColegiado(),crit);
    }

    public Long buscarQtdSessoes(InformacaoUsuarioSessao usuarioSessao, CriterioPesquisa crit){
        if(usuarioSessao == null){
            return null;
        }
        return sessaoJulgamentoDAO.buscarQtdSessoes(usuarioSessao.getIdOrgaoJulgadorColegiado(),crit);
    }	
}
