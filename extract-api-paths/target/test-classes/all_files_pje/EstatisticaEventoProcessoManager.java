package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.bean.EstatisticaDistribuidosListBean;
import br.com.infox.pje.bean.EstatisticaProcTramitacaoSecaoListBean;
import br.com.infox.pje.bean.EstatisticaProcessosArquivadosSubTableBean;
import br.com.infox.pje.bean.EstatisticaProcessosJulgadosSubTableBean;
import br.com.infox.pje.bean.EstatisticaProcessosTramitacaoSubTableBean;
import br.com.infox.pje.bean.MapaProdutividadeTipoVaraBean;
import br.com.infox.pje.bean.MapaProdutividadeVaraBean;
import br.com.infox.pje.dao.EstatisticaEventoProcessoDAO;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe que acessa o DAO e contem a regra de negocios referente a entidade de
 * EstatisticaEventoProcesso
 * 
 * @author Daniel
 * 
 */
@Name(EstatisticaEventoProcessoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class EstatisticaEventoProcessoManager extends GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estatisticaEventoProcessoManager";

	@In
	private EstatisticaEventoProcessoDAO estatisticaEventoProcessoDAO;

	public List<String> listCompentenciaByOrgaoJulgador(String orgaoJulgador) {
		return estatisticaEventoProcessoDAO.listCompetenciaByOrgaoJulgador(orgaoJulgador);
	}

	public List<String> listGroupByCompentencia() {
		return estatisticaEventoProcessoDAO.listGroupByCompentencia();
	}

	public List<String> listGroupByEstado() {
		return estatisticaEventoProcessoDAO.listGroupByEstado();
	}

	public List<Object[]> secaoJudiciaria() {
		return estatisticaEventoProcessoDAO.secaoJudiciaria();
	}

	public List<Object[]> listProcessosRemaDisJulArqTramitacao(String dataInicioStr, String dataFimStr,
			String eventoProcessualDistribuicao, String eventoProcessualJulgamento, String eventoArquivamento,
			String eventoArquivamentoDefinitivoProcessual, String eventoBaixaDefinitivaProcessual,
			String eventoArquivamentoProvisorio, String dataRemanescente) {
		return estatisticaEventoProcessoDAO.listProcessosRemaDisJulArqTramitacao(dataInicioStr, dataFimStr,
				eventoProcessualDistribuicao, eventoProcessualJulgamento, eventoArquivamento,
				eventoArquivamentoDefinitivoProcessual, eventoBaixaDefinitivaProcessual, eventoArquivamentoProvisorio,
				dataRemanescente);
	}

	public List<Object[]> listProcessosRemaDisJulArqTramitacaoComSessao(String dataInicioStr, String dataFimStr,
			String eventoProcessualDistribuicao, String eventoProcessualJulgamento, String eventoArquivamento,
			String eventoArquivamentoDefinitivoProcessual, String eventoBaixaDefinitivaProcessual,
			String eventoArquivamentoProvisorio, String dataRemanescente, String sessao) {
		return estatisticaEventoProcessoDAO.listProcessosRemaDisJulArqTramitacaoComSessao(dataInicioStr, dataFimStr,
				eventoProcessualDistribuicao, eventoProcessualJulgamento, eventoArquivamento,
				eventoArquivamentoDefinitivoProcessual, eventoBaixaDefinitivaProcessual, eventoArquivamentoProvisorio,
				dataRemanescente, sessao);
	}

	public List<Object[]> listUltimoEventoProcessos(Date dataInicio, Date dataFim) {
		return estatisticaEventoProcessoDAO.listUltimoEventoProcessos(dataInicio, dataFim);
	}

	public List<MapaProdutividadeTipoVaraBean> listTipoVaraByEstado(String dataInicio, String dataFim, String codEstado) {
		List<Map<String, Object>> mapList = estatisticaEventoProcessoDAO.listTipoVaraByEstado(dataInicio, dataFim,
				codEstado);

		List<MapaProdutividadeTipoVaraBean> mapaProdutividadeTipoVaraBeanList = new ArrayList<MapaProdutividadeTipoVaraBean>();
		for (Map<String, Object> map : mapList) {
			MapaProdutividadeTipoVaraBean bean = new MapaProdutividadeTipoVaraBean();
			bean.setCompetencia(map.get("competencia").toString());
			bean.setMapaProdutividadeVaraBeanList(listVaraByTipoVara(dataInicio, dataFim, codEstado,
					bean.getCompetencia()));

			mapaProdutividadeTipoVaraBeanList.add(bean);
		}
		return mapaProdutividadeTipoVaraBeanList;
	}

	public List<MapaProdutividadeVaraBean> listVaraByTipoVara(String dataInicio, String dataFim, String codEstado,
			String competencia) {
		List<Map<String, Object>> mapList = estatisticaEventoProcessoDAO.listVaraByTipoVara(dataInicio, dataFim,
				codEstado, competencia);

		List<MapaProdutividadeVaraBean> mapaProdutividadeVaraBeanList = new ArrayList<MapaProdutividadeVaraBean>();
		for (Map<String, Object> map : mapList) {
			MapaProdutividadeVaraBean bean = new MapaProdutividadeVaraBean();
			bean.setVara(map.get("orgaoJulgador").toString());
			bean.setQtPorMes(getMapMesQtdProcesso(estatisticaEventoProcessoDAO.listQuantidadeProcessoByVara(dataInicio,
					dataFim, codEstado, competencia, bean.getVara())));
			String vara = StringUtil.removeNaoNumericos(map.get("orgaoJulgador").toString());
			bean.setVara(vara + "ª");
			mapaProdutividadeVaraBeanList.add(bean);
		}
		return mapaProdutividadeVaraBeanList;
	}

	private Map<Integer, Long> getMapMesQtdProcesso(List<Map<String, Object>> mapMesList) {
		Map<Integer, Long> mapMesQtd = new HashMap<Integer, Long>();
		for (Map<String, Object> mapMes : mapMesList) {
			mapMesQtd.put((Integer) mapMes.get("mes"), (Long) mapMes.get("numProcessos"));
		}
		return mapMesQtd;
	}

	/**
	 * Pega uma sessão e devolve a lista de varas junto com as quanntidades de
	 * processos.
	 * 
	 * @param secao
	 * @return Lista de Object[] daquela seção
	 */
	public List<Object[]> pegarListaVarasSecao(String secao, String dataInicio, String dataFim) {
		return estatisticaEventoProcessoDAO.listVarasSecaoProcessos(secao, dataInicio, dataFim);
	}

	/**
	 * Pega uma sessão e devolve a lista de varas junto com as quanntidades de
	 * processos em tramitação.
	 * 
	 * @param secao
	 * @return Lista de Object[] daquela seção
	 */
	public List<EstatisticaProcessosTramitacaoSubTableBean> buscaListaVarasSecaoTramitacao(String secao,
			String dataInicio, String dataFim) {
		List<Map<String, Object>> mapList = estatisticaEventoProcessoDAO.listVarasSecaoProcessosTramitacao(secao,
				dataInicio, dataFim);
		List<EstatisticaProcessosTramitacaoSubTableBean> lista = new ArrayList<EstatisticaProcessosTramitacaoSubTableBean>();
		for (Map<String, Object> map : mapList) {
			EstatisticaProcessosTramitacaoSubTableBean bean = new EstatisticaProcessosTramitacaoSubTableBean();
			bean.setVara(map.get("orgaoJulgador").toString());
			bean.setTotalVara(map.get("qtd").toString());
			bean.setJurisdicao(map.get("jurisdicao").toString());
			bean.setQtPorMes(getMapMesQtdProcesso(estatisticaEventoProcessoDAO.listQuantidadeProcessoTramitMesByVara(
					dataInicio, dataFim, secao, bean.getVara())));
			lista.add(bean);
		}
		return lista;
	}

	/**
	 * Pega uma sessão e devolve a lista de varas junto com as quanntidades de
	 * processos em tramitação.
	 * 
	 * @param secao
	 * @return Lista de Object[] daquela seção
	 */
	public List<EstatisticaProcessosArquivadosSubTableBean> buscaListaVarasSecaoArquivados(String secao,
			String dataInicio, String dataFim) {
		List<Map<String, Object>> mapList = estatisticaEventoProcessoDAO.listVarasSecaoProcessosArquivados(secao,
				dataInicio, dataFim);
		List<EstatisticaProcessosArquivadosSubTableBean> lista = new ArrayList<EstatisticaProcessosArquivadosSubTableBean>();
		for (Map<String, Object> map : mapList) {
			EstatisticaProcessosArquivadosSubTableBean bean = new EstatisticaProcessosArquivadosSubTableBean();
			bean.setVara(map.get("orgaoJulgador").toString());
			bean.setQtdVaras(Long.valueOf(map.get("qtd").toString()));
			bean.setCompetencias(getCompetenciasFormatadasByOrgaoJulgador(map.get("orgaoJulgador").toString()));
			bean.setQtPorMes(getMapMesQtdProcesso(estatisticaEventoProcessoDAO.listQuantidadeProcessoArqMesByVara(
					dataInicio, dataFim, secao, bean.getVara())));
			bean.setVara(getVara(map));
			lista.add(bean);
		}
		return lista;
	}

	public List<EstatisticaDistribuidosListBean> listVaraBySecao(String dataInicio, String dataFim, String codEstado) {
		List<Map<String, Object>> mapList = estatisticaEventoProcessoDAO
				.listVaraBySecao(dataInicio, dataFim, codEstado);
		List<EstatisticaDistribuidosListBean> estatisticaDistribuidosBeanList = new ArrayList<EstatisticaDistribuidosListBean>();

		for (Map<String, Object> map : mapList) {
			EstatisticaDistribuidosListBean bean = new EstatisticaDistribuidosListBean();
			bean.setCompetencias(getCompetenciasFormatadasByOrgaoJulgador(map.get("orgaoJulgador").toString()));
			bean.setQtPorMes(getMapMesQtdProcesso(estatisticaEventoProcessoDAO
					.listQuantidadeProcessoDistribuidosByVara(dataInicio, dataFim, codEstado, map.get("orgaoJulgador")
							.toString())));
			bean.setVara(getVara(map));

			estatisticaDistribuidosBeanList.add(bean);
		}
		return estatisticaDistribuidosBeanList;
	}

	/**
	 * Formata a vara para a exibição no relatório
	 * 
	 * @param orgaoJulgador
	 * @return
	 */
	private String getVara(Map<String, Object> map) {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.limparCharsNaoNumericos(map.get("orgaoJulgador").toString())).append("ª - ")
				.append((map.get("jurisdicao").toString()));
		return sb.toString();
	}

	private String getCompetenciasFormatadasByOrgaoJulgador(String orgaoJulgador) {
		List<String> listCompetencia = listCompentenciaByOrgaoJulgador(orgaoJulgador);
		if (!listCompetencia.isEmpty()) {
			StringBuilder sb = new StringBuilder("(");
			sb.append(listCompetencia.remove(0));
			for (String competencia : listCompetencia) {
				sb.append(" + ");
				sb.append(competencia);
			}
			sb.append(")");
			return sb.toString();
		}
		return null;
	}

	/**
	 * Pega uma sessão e devolve a lista de varas junto com as quanntidades de
	 * processos em tramitação.
	 * 
	 * @param secao
	 * @return Lista de Object[] daquela seção
	 */
	public List<EstatisticaProcessosJulgadosSubTableBean> buscaListaVarasSecaoJulgados(String secao, String dataInicio,
			String dataFim) {
		List<Map<String, Object>> mapList = estatisticaEventoProcessoDAO.listVarasSecaoProcessosJulgados(secao,
				dataInicio, dataFim);
		List<EstatisticaProcessosJulgadosSubTableBean> lista = new ArrayList<EstatisticaProcessosJulgadosSubTableBean>();
		for (Map<String, Object> map : mapList) {
			EstatisticaProcessosJulgadosSubTableBean bean = new EstatisticaProcessosJulgadosSubTableBean();
			bean.setJurisdicao(getCompetenciasFormatadasByOrgaoJulgador(map.get("orgaoJulgador").toString()));
			bean.setQtPorMes(getMapMesQtdProcesso(estatisticaEventoProcessoDAO.listQuantidadeProcessoJulMesByVara(
					dataInicio, dataFim, secao, map.get("orgaoJulgador").toString())));
			bean.setVara(getVara(map));
			lista.add(bean);
		}
		return lista;
	}

	public List<EstatisticaProcTramitacaoSecaoListBean> listDadosProcessosByEstadoOrgaoJulgador(Date data,
			String codEstado, String orgaoJulgador) {
		List<EstatisticaProcTramitacaoSecaoListBean> secaoListBeans = new ArrayList<EstatisticaProcTramitacaoSecaoListBean>();
		List<Object[]> list = estatisticaEventoProcessoDAO.listProcessoByEstadoOrgaoJulgador(data, codEstado,
				orgaoJulgador);
		for (Object[] o : list) {
			EstatisticaProcTramitacaoSecaoListBean bean = new EstatisticaProcTramitacaoSecaoListBean();
			bean.setProcesso(o[0].toString());
			bean.setClasseJudicial(o[1].toString());
			bean.setDataDistribuicao((Date) o[2]);
			bean.setRemessa(o[3] != null ? "S" : "N");
			bean.setTipoApelacao(o[4].toString().equals("false") ? "N" : "S");
			bean.setTotalJulgados((Long) o[5]);
			secaoListBeans.add(bean);
		}
		return secaoListBeans;
	}

	public long quantidadeProcessosJulgadosByEstadoOrgaoJulgador(Date data, String codEstado, String orgaoJulgador) {
		return estatisticaEventoProcessoDAO.quantidadeProcessosJulgadosByEstadoOrgaoJulgador(data, codEstado,
				orgaoJulgador);
	}

	/**
	 * Pega uma sessão e devolve a lista de varas junto com as quanntidades de
	 * processos para o tipo de evento distribuição.
	 * 
	 * @param secao
	 * @return Lista de Object[] daquela seção
	 */
	public List<String> pegarListaVarasSecaoEventoDistribuicao(String secao, String dataInicio, String dataFim) {
		return estatisticaEventoProcessoDAO.listVarasSecaoProcessosEventoDistribuicao(secao, dataInicio, dataFim);
	}

	public List<Object[]> pegarListaProcVarasSecaoEventoDistribuicao(String secao, String oj, String dataInicio,
			String dataFim) {
		return estatisticaEventoProcessoDAO.listProcVarasSecaoProcEventoDistribuicao(secao, oj, dataInicio, dataFim);
	}

}