package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.EstatisticaEventoProcessoQuery;
import br.com.itx.util.EntityUtil;

/**
 * Classe com as consultas a entidade de Competencia.
 * 
 * @author Daniel
 * 
 */
@Name(EstatisticaEventoProcessoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class EstatisticaEventoProcessoDAO extends GenericDAO implements Serializable, EstatisticaEventoProcessoQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estatisticaEventoProcessoDAO";

	/**
	 * Obtem as competencia agrupadas atraves do descricao do orgaoJulgador.
	 * 
	 * @param procTrf
	 *            a se obter a competencia
	 * @return Competencia relacionada ao processo informado.
	 */
	@SuppressWarnings("unchecked")
	public List<String> listCompetenciaByOrgaoJulgador(String orgaoJulgador) {
		Query q = getEntityManager().createQuery(LIST_COMPETENCIA_EST_BY_ORGAO_JULGADOR_QUERY);
		q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
		List<String> resultList = q.getResultList();
		return resultList;
	}

	/**
	 * Lista todos os estados (agrupando pelo próprio) contido na tabela de
	 * estatistica de processos.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> listGroupByEstado() {
		Query q = getEntityManager().createQuery(LIST_GROUP_BY_ESTADO_QUERY);
		List<String> resultList = q.getResultList();
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> secaoJudiciaria() {
		Query q = getEntityManager().createQuery(SECAO_JUDICIARIA_QUERY);
		List<Object[]> resultList = null;
		resultList = q.getResultList();
		return resultList;
	}

	/**
	 * Lista todos as competencias da tabela tb_est_evento_processo.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> listGroupByCompentencia() {
		Query q = getEntityManager().createQuery(LIST_GROUP_BY_COMPETENCIA_QUERY);
		List<String> resultList = q.getResultList();
		return resultList;
	}

	/**
	 * Obtem a lista para a exibição dos processos remanescentes distribuidos,
	 * julgados, arquivados e em tramiação. Exibindo quantificadores para os
	 * mesmos através de um intervalo de datas informado nos parametros.
	 * 
	 * @param dataInicio
	 *            Inicial
	 * @param dataFim
	 *            Final
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> listProcessosRemaDisJulArqTramitacao(String dataInicioStr, String dataFimStr,
			String eventoDistribuido, String eventoJulgamento, String eventoArquivamento,
			String eventoArquivamentoDefinitivoProcessual, String eventoBaixaDefinitivaProcessual,
			String eventoArquivamentoProvisorio, String dataRemanescente) {
		List<Object[]> resultList = null;
		if (dataInicioStr != null || dataFimStr != null) {
			Query q = getEntityManager().createQuery(LIST_PROCESSOS_REMA_DIS_JUL_ARQ_TRAMITACAO_QUERY);
			q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicioStr);
			q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFimStr);
			q.setParameter(QUERY_PARAMETER_EVENTO_DISTRIBUICAO, eventoDistribuido);
			q.setParameter(QUERY_PARAMETER_EVENTO_JULGAMENTO, eventoJulgamento);
			q.setParameter(QUERY_PARAMETER_EVENTO_ARQUIVAMENTO, eventoArquivamento);
			q.setParameter(QUERY_PARAMETER_EVENTO1, eventoArquivamentoDefinitivoProcessual);
			q.setParameter(QUERY_PARAMETER_EVENTO2, eventoBaixaDefinitivaProcessual);
			q.setParameter(QUERY_PARAMETER_EVENTO3, eventoArquivamentoProvisorio);
			q.setParameter(QUERY_PARAMETER_DATA_REMANESCENTE_STR, dataRemanescente);

			resultList = q.getResultList();
		}
		return resultList;
	}

	/**
	 * Obtem a lista para a exibição dos processos remanescentes distribuidos,
	 * julgados, arquivados e em tramiação. Exibindo quantificadores para os
	 * mesmos através de um intervalo de datas informado nos parametros(quando
	 * passado uma Sessao Judiciária na combo).
	 * 
	 * @param dataInicio
	 *            Inicial
	 * @param dataFim
	 *            Final
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> listProcessosRemaDisJulArqTramitacaoComSessao(String dataInicioStr, String dataFimStr,
			String eventoDistribuido, String eventoJulgamento, String eventoArquivamento,
			String eventoArquivamentoDefinitivoProcessual, String eventoBaixaDefinitivaProcessual,
			String eventoArquivamentoProvisorio, String dataRemanescente, String sessao) {
		List<Object[]> resultList = null;
		if (dataInicioStr != null || dataFimStr != null) {
			Query q = getEntityManager().createQuery(LIST_PROCESSOS_REMA_DIS_JUL_ARQ_TRAMITACAO_COM_SESSAO_QUERY);
			q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicioStr);
			q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFimStr);
			q.setParameter(QUERY_PARAMETER_EVENTO_DISTRIBUICAO, eventoDistribuido);
			q.setParameter(QUERY_PARAMETER_EVENTO_JULGAMENTO, eventoJulgamento);
			q.setParameter(QUERY_PARAMETER_EVENTO_ARQUIVAMENTO, eventoArquivamento);
			q.setParameter(QUERY_PARAMETER_EVENTO1, eventoArquivamentoDefinitivoProcessual);
			q.setParameter(QUERY_PARAMETER_EVENTO2, eventoBaixaDefinitivaProcessual);
			q.setParameter(QUERY_PARAMETER_EVENTO3, eventoArquivamentoProvisorio);
			q.setParameter(QUERY_PARAMETER_DATA_REMANESCENTE_STR, dataRemanescente);
			q.setParameter(QUERY_PARAMETER_COD_ESTADO, sessao);

			resultList = q.getResultList();
		}
		return resultList;
	}

	/**
	 * Pega uma sessão e devolve a lista de varas junto com as quanntidades de
	 * processos.
	 * 
	 * @param secao
	 * @return Lista de VaraProcessosArquivadosRankingSessaoBean daquela seção
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> listVarasSecaoProcessos(String secao, String dataInicio, String dataFim) {
		List<Object[]> resultList = null;
		Query q = getEntityManager().createQuery(LIST_VARAS_SECAO_PROCESSOS_QUERY);
		q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicio);
		q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);
		q.setParameter(QUERY_PARAMETER_COD_ESTADO, secao);
		q.setParameter(QUERY_PARAMETER_EVENTO1, ParametroUtil.instance().getEventoArquivamentoProvisorio()
				.getCodEvento());
		q.setParameter(QUERY_PARAMETER_EVENTO2, ParametroUtil.instance().getEventoArquivamentoDefinitivoProcessual()
				.getCodEvento());
		q.setParameter(QUERY_PARAMETER_EVENTO3, ParametroUtil.instance().getEventoArquivamento().getCodEvento());

		resultList = q.getResultList();
		return resultList;
	}

	/**
	 * Pega uma sessão e devolve a lista de varas junto com as quanntidades de
	 * processos para o tipo de evento distribuição.
	 * 
	 * @param secao
	 * @return Lista de VaraProcessosArquivadosRankingSessaoBean daquela seção
	 */
	@SuppressWarnings("unchecked")
	public List<String> listVarasSecaoProcessosEventoDistribuicao(String secao, String dataInicio, String dataFim) {
		List<String> resultList = null;
		Query q = getEntityManager().createQuery(LIST_VARAS_SECAO_PROCESSOS_DISTRIBUIDOS_QUERY);
		q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicio);
		q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);
		q.setParameter(QUERY_PARAMETER_COD_ESTADO, secao);
		q.setParameter(QUERY_PARAMETER_EVENTO1, ParametroUtil.instance().getEventoProcessualDistribuicao()
				.getCodEvento());

		resultList = q.getResultList();
		return resultList;
	}

	/**
	 * Pega uma sessão e devolve a lista de processos das vara para o tipo de
	 * evento distribuição.
	 * 
	 * @param secao
	 * @return Lista de VaraProcessosArquivadosRankingSessaoBean daquela seção
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> listProcVarasSecaoProcEventoDistribuicao(String secao, String oj, String dataInicio,
			String dataFim) {
		List<Object[]> resultList = null;
		Query q = getEntityManager().createQuery(LIST_PROC_VARAS_SECAO_PROCESSOS_DISTRIBUIDOS_QUERY);
		q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicio);
		q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);
		q.setParameter(QUERY_PARAMETER_COD_ESTADO, secao);
		q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, oj);
		q.setParameter(QUERY_PARAMETER_EVENTO1, ParametroUtil.instance().getEventoProcessualDistribuicao()
				.getCodEvento());
		resultList = q.getResultList();
		return resultList;
	}

	/**
	 * Lista o último evento lançado para cada um dos processos contidos na
	 * tabela de estatística
	 * 
	 * @return 0 - Número Processo, 1 - idEstatisticaProcesso
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> listUltimoEventoProcessos(Date dataInicio, Date dataFim) {
		List<Object[]> resultList = null;
		if (dataInicio != null || dataFim != null) {
			Query q = getEntityManager().createQuery(LIST_ULTIMO_EVENTO_PROCESSOS_QUERY);
			q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicio);
			q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);

			resultList = q.getResultList();
		}
		return resultList;
	}

	/**
	 * Lista o MapaProdutividadeTipoVaraBean para cada tipo de vara na seção
	 * 
	 * @param dataInicio
	 * @param dataFim
	 * @return lista de MapaProdutividadeTipoVaraBean
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> listTipoVaraByEstado(String dataInicio, String dataFim, String codEstado) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (dataInicio != null && dataFim != null) {
			Query q = getEntityManager().createQuery(LIST_TIPO_VARA_BY_ESTADO_QUERY);
			q.setParameter(QUERY_PARAMETER_COD_ESTADO, codEstado);
			q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicio);
			q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);

			resultList = q.getResultList();
		}
		return resultList;
	}

	/**
	 * Lista o MapaProdutividadeVaraBean para cada vara com o mesmo tipo de vara
	 * 
	 * @param dataInicio
	 * @param dataFim
	 * @return lista de MapaProdutividadeTipoVaraBean
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> listVaraByTipoVara(String dataInicio, String dataFim, String codEstado,
			String competencia) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (dataInicio != null && dataFim != null) {
			Query q = getEntityManager().createQuery(LIST_VARA_BY_TIPO_VARA_QUERY);
			q.setParameter(QUERY_PARAMETER_COD_ESTADO, codEstado);
			q.setParameter(QUERY_PARAMETER_COMPETENCIA, competencia);
			q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicio);
			q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);

			resultList = q.getResultList();
		}
		return resultList;
	}

	/**
	 * Lista a quantidade de processo por mes de cada vara
	 * 
	 * @param dataInicio
	 * @param dataFim
	 * @return lista de Map com o mes e a quantidade de processos
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> listQuantidadeProcessoByVara(String dataInicio, String dataFim, String codEstado,
			String competencia, String orgaoJulgador) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (dataInicio != null && dataFim != null) {
			Query q = getEntityManager().createQuery(LIST_QUANTIDADE_PROCESSOS_MENSAL_BY_VARA_QUERY);
			q.setParameter(QUERY_PARAMETER_EVENTO1, ParametroUtil.instance().getEventoJulgamentoProcessual()
					.getCodEvento());
			q.setParameter(QUERY_PARAMETER_COD_ESTADO, codEstado);
			q.setParameter(QUERY_PARAMETER_COMPETENCIA, competencia);
			q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
			q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicio);
			q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);
			resultList = q.getResultList();
		}
		return resultList;
	}

	/**
	 * Pega uma sessão e devolve a lista de varas junto com a quantidade de
	 * processos em tramitação.
	 * 
	 * @param secao
	 * @return Lista de VaraProcessosArquivadosRankingSessaoBean daquela seção
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> listVarasSecaoProcessosTramitacao(String secao, String dataInicio, String dataFim) {
		List<Map<String, Object>> resultList = null;
		Query q = getEntityManager().createQuery(LIST_VARAS_SECAO_PROC_TRAMIT_QUERY);
		q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);
		q.setParameter(QUERY_PARAMETER_COD_ESTADO, secao);
		q.setParameter(QUERY_PARAMETER_EVENTO1, ParametroUtil.instance().getEventoBaixaDefinitivaProcessual()
				.getCodEvento());
		q.setParameter(QUERY_PARAMETER_EVENTO2, ParametroUtil.instance().getEventoArquivamentoDefinitivoProcessual()
				.getCodEvento());

		resultList = q.getResultList();
		return resultList;
	}

	/**
	 * Lista a quantidade de processo por mes de cada vara
	 * 
	 * @param dataInicio
	 * @param dataFim
	 * @return lista de Map com o mes e a quantidade de processos
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> listQuantidadeProcessoDistribuidosByVara(String dataInicio, String dataFim,
			String codEstado, String orgaoJulgador) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (dataInicio != null && dataFim != null) {
			Query q = getEntityManager().createQuery(LIST_QUANTIDADE_PROCESSOS_DISTRIBUIDOS_MENSAL_BY_VARA_QUERY);
			q.setParameter(QUERY_PARAMETER_COD_ESTADO, codEstado);
			q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
			q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicio);
			q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);
			q.setParameter(QUERY_PARAMETER_EVENTO_DISTRIBUICAO, ParametroUtil.instance()
					.getEventoProcessualDistribuicao().getCodEvento());

			resultList = q.getResultList();
		}
		return resultList;
	}

	/**
	 * Lista a quantidade de processos em tramitacao por mes de cada vara
	 * 
	 * @param dataInicio
	 * @param dataFim
	 * @return lista de Map com o mes e a quantidade de processos
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> listQuantidadeProcessoTramitMesByVara(String dataInicio, String dataFim,
			String codEstado, String orgaoJulgador) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (dataInicio != null && dataFim != null) {
			Query q = getEntityManager().createQuery(LIST_QUANTIDADE_PROCESSOS_TRAMIT_MENSAL_BY_VARA_QUERY);
			q.setParameter(QUERY_PARAMETER_COD_ESTADO, codEstado);
			q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
			q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);
			q.setParameter(QUERY_PARAMETER_EVENTO1, ParametroUtil.instance().getEventoBaixaDefinitivaProcessual()
					.getCodEvento());
			q.setParameter(QUERY_PARAMETER_EVENTO2, ParametroUtil.instance()
					.getEventoArquivamentoDefinitivoProcessual().getCodEvento());
			resultList = q.getResultList();
		}
		return resultList;
	}

	/**
	 * Pega uma sessão e devolve a lista de varas junto com a quantidade de
	 * processos em tramitação.
	 * 
	 * @param secao
	 * @return Lista de VaraProcessosArquivadosRankingSessaoBean daquela seção
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> listVarasSecaoProcessosArquivados(String secao, String dataInicio, String dataFim) {
		List<Map<String, Object>> resultList = null;
		Query q = getEntityManager().createQuery(LIST_VARAS_SECAO_PROC_ARQ_QUERY);
		q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicio);
		q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);
		q.setParameter(QUERY_PARAMETER_COD_ESTADO, secao);
		q.setParameter(QUERY_PARAMETER_EVENTO1, ParametroUtil.instance().getEventoArquivamento().getCodEvento());
		q.setParameter(QUERY_PARAMETER_EVENTO2, ParametroUtil.instance().getEventoArquivamentoProvisorio()
				.getCodEvento());
		q.setParameter(QUERY_PARAMETER_EVENTO3, ParametroUtil.instance().getEventoArquivamentoDefinitivoProcessual()
				.getCodEvento());

		resultList = q.getResultList();
		return resultList;
	}

	/**
	 * Lista a quantidade de processos em tramitacao por mes de cada vara
	 * 
	 * @param dataInicio
	 * @param dataFim
	 * @return lista de Map com o mes e a quantidade de processos
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> listQuantidadeProcessoArqMesByVara(String dataInicio, String dataFim,
			String codEstado, String orgaoJulgador) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (dataInicio != null && dataFim != null) {
			Query q = getEntityManager().createQuery(LIST_QUANTIDADE_PROCESSOS_ARQ_MENSAL_BY_VARA_QUERY);
			q.setParameter(QUERY_PARAMETER_COD_ESTADO, codEstado);
			q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
			q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicio);
			q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);
			q.setParameter(QUERY_PARAMETER_EVENTO1, ParametroUtil.instance().getEventoArquivamento().getCodEvento());
			q.setParameter(QUERY_PARAMETER_EVENTO2, ParametroUtil.instance().getEventoArquivamentoProvisorio()
					.getCodEvento());
			q.setParameter(QUERY_PARAMETER_EVENTO3, ParametroUtil.instance()
					.getEventoArquivamentoDefinitivoProcessual().getCodEvento());
			resultList = q.getResultList();
		}
		return resultList;
	}

	/**
	 * Lista o EstatisticaProcessosDistribuidosBean para cada vara com o mesmo
	 * tipo de vara
	 * 
	 * @param dataInicio
	 * @param dataFim
	 * @return lista de EstatisticaDistribuidosListBean
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> listVaraBySecao(String dataInicio, String dataFim, String codEstado) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (dataInicio != null && dataFim != null) {
			Query q = getEntityManager().createQuery(LIST_VARA_BY_SECAO_QUERY);
			q.setParameter(QUERY_PARAMETER_COD_ESTADO, codEstado);
			q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicio);
			q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);
			q.setParameter(QUERY_PARAMETER_EVENTO_DISTRIBUICAO, ParametroUtil.instance()
					.getEventoProcessualDistribuicao().getCodEvento());

			resultList = q.getResultList();
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> listQuantidadeProcessoJulMesByVara(String dataInicio, String dataFim,
			String codEstado, String orgaoJulgador) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (dataInicio != null && dataFim != null) {
			Query q = getEntityManager().createQuery(LIST_QUANTIDADE_PROCESSOS_JUL_MENSAL_BY_VARA_QUERY);
			q.setParameter(QUERY_PARAMETER_COD_ESTADO, codEstado);
			q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, codEstado);
			q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicio);
			q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);
			q.setParameter(QUERY_PARAMETER_EVENTO1, ParametroUtil.instance().getEventoJulgamentoProcessual()
					.getCodEvento());

			resultList = q.getResultList();
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> listVarasSecaoProcessosJulgados(String secao, String dataInicio, String dataFim) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (dataInicio != null && dataFim != null) {
			Query q = getEntityManager().createQuery(LIST_VARAS_SECAO_PROC_JUL_QUERY);
			q.setParameter(QUERY_PARAMETER_COD_ESTADO, secao);
			q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicio);
			q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFim);
			q.setParameter(QUERY_PARAMETER_EVENTO1, ParametroUtil.instance().getEventoJulgamentoProcessual()
					.getCodEvento());

			resultList = q.getResultList();
		}
		return resultList;
	}

	/**
	 * retorna alguns dados do processo a partir de um estado e orgaoJulgador
	 * num determinado periodo
	 * 
	 * @param dataInicio
	 * @param dataFim
	 * @param estado
	 * @param orgaoJulgador
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> listProcessoByEstadoOrgaoJulgador(Date data, String estado, String orgaoJulgador) {
		List<Object[]> resultList = null;
		if (data != null || estado != null || orgaoJulgador != null) {
			Query q = getEntityManager().createQuery(LIST_PROCESSOS_BY_ESTADO_ORGAO_JULGADOR_QUERY);
			q.setParameter(QUERY_PARAMETER_EVENTO1, ParametroUtil.instance().getEventoRemetidoTrfProcessual()
					.getCodEvento());
			q.setParameter(QUERY_PARAMETER_EVENTO2, ParametroUtil.instance().getEventoBaixaDefinitivaProcessual()
					.getCodEvento());
			q.setParameter(QUERY_PARAMETER_EVENTO3, ParametroUtil.instance()
					.getEventoArquivamentoDefinitivoProcessual().getCodEvento());
			q.setParameter(QUERY_PARAMETER_DATA_INCIO, data);
			q.setParameter(QUERY_PARAMETER_COD_ESTADO, estado);
			q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
			q.setParameter(QUERY_PARAMETER_EVENTO_JULGADO, ParametroUtil.instance().getEventoJulgamentoProcessual()
					.getCodEvento());

			resultList = q.getResultList();
		}
		return resultList;
	}

	public long quantidadeProcessosJulgadosByEstadoOrgaoJulgador(Date data, String estado, String orgaoJulgador) {
		Long count = 0L;
		if (data != null || estado != null || orgaoJulgador != null) {
			Query q = getEntityManager().createQuery(COUNT_PROCESSOS_BY_ESTADO_ORGAO_JULGADOR_QUERY);
			q.setParameter(QUERY_PARAMETER_EVENTO_JULGADO, ParametroUtil.instance().getEventoJulgamentoProcessual()
					.getCodEvento());
			q.setParameter(QUERY_PARAMETER_DATA_INCIO, data);
			q.setParameter(QUERY_PARAMETER_COD_ESTADO, estado);
			q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
			q.setParameter(QUERY_PARAMETER_EVENTO1, ParametroUtil.instance().getEventoBaixaDefinitivaProcessual()
					.getCodEvento());
			q.setParameter(QUERY_PARAMETER_EVENTO2, ParametroUtil.instance()
					.getEventoArquivamentoDefinitivoProcessual().getCodEvento());

			count = EntityUtil.getSingleResult(q);
		}
		return count;
	}
}