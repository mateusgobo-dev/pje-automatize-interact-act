package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.query.ProcessoAudienciaQuery;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.TipoAudiencia;
import br.jus.pje.nucleo.enums.StatusAudienciaEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Search;

/**
 * Classe com as consultas a entidade de Competencia.
 */
@Name(ProcessoAudienciaDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ProcessoAudienciaDAO extends BaseDAO<ProcessoAudiencia> implements Serializable, ProcessoAudienciaQuery {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "processoAudienciaDAO";

	public long totalAcordosHomologadosJuiz(String dataInicial, String dataFinal, OrgaoJulgador oj, String juiz) {
		Query q = getEntityManager().createQuery(TOTAL_ACORDOS_HOMOLOGADOS_QUERY_JUIZ);
		q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicial);
		q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFinal);
		q.setParameter(QUERY_PARAMETER_JUIZ, juiz);
		q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, oj);

		return EntityUtil.getSingleResultCount(q);
	}

	public double valorAcordosHomologadosJuiz(String dataInicial, String dataFinal, OrgaoJulgador oj, String juiz) {
		Query q = getEntityManager().createQuery(VALOR_ACORDOS_HOMOLOGADOS_QUERY_JUIZ);
		q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicial);
		q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFinal);
		q.setParameter(QUERY_PARAMETER_JUIZ, juiz);
		q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, oj);

		return EntityUtil.getSingleResultCount(q).doubleValue();
	}

	public long totalAcordosHomologados(String dataInicial, String dataFinal, OrgaoJulgador oj) {
		Query q = getEntityManager().createQuery(TOTAL_ACORDOS_HOMOLOGADOS_QUERY);
		q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicial);
		q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFinal);
		q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, oj);

		return EntityUtil.getSingleResultCount(q);
	}

	public double valorAcordosHomologados(String dataInicial, String dataFinal, OrgaoJulgador oj) {
		Query q = getEntityManager().createQuery(VALOR_ACORDOS_HOMOLOGADOS_QUERY);
		q.setParameter(QUERY_PARAMETER_DATA_INCIO, dataInicial);
		q.setParameter(QUERY_PARAMETER_DATA_FIM, dataFinal);
		q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, oj);

		return EntityUtil.getSingleResultCount(q).doubleValue();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoAudiencia> getAudienciasMarcadas(Sala sala, Date dataInicio){
		Query query = getEntityManager().createQuery("from ProcessoAudiencia p where "
				+ "p.salaAudiencia = :salaAudiencia and "
				+ "p.statusAudiencia = 'M' and "
				+ "p.dtInicio >= :dataInicioAudiencia "
				+ "order by p.dtInicio asc").setParameter("salaAudiencia", sala).setParameter("dataInicioAudiencia", dataInicio);
	
		return query.getResultList();
	}
	
	/**
	 * Retorna uma lista de {@link ProcessoAudiencia} com status de {@link StatusAudienciaEnum#M} e que tenham como data de início a data parâmetro.
	 * Se houver horário na data parâmetro, o mesmo será removido na busca.
	 * 
	 * @param dataAudiencia a data de início da audiência
	 * @return lista de audiências que tenham data de início igual à data parâmetro
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoAudiencia> findByData(Date dataAudiencia) {
		Query query = getEntityManager()
				.createQuery("from ProcessoAudiencia p where " + "date_trunc('day', p.dtInicio) = :dataAudiencia "
						+ "and p.statusAudiencia = 'M' " + "order by p.dtInicio asc")
				.setParameter("dataAudiencia", DateUtil.getDataSemHora(dataAudiencia));

		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Integer> findIdsByData(Date dataAudiencia) {
		Query query = getEntityManager()
				.createQuery("Select p.idProcessoAudiencia from ProcessoAudiencia p where "
						+ "date_trunc('day', p.dtInicio) = :dataAudiencia " + "and p.statusAudiencia = 'M' "
						+ "order by p.dtInicio asc")
				.setParameter("dataAudiencia", DateUtil.getDataSemHora(dataAudiencia));

		return query.getResultList();
	}

	/**
	 * Método responsável por recuperar uma lista de {@link ProcessoAudiencia} 
	 * com o status de designada e de acordo com os parãmetros informados.
	 * 
	 * @param orgaoJulgador Órgão julgador.
	 * @param dataInicio Data de início 
	 * @param tipoAudiencia Tipo de audiência
	 * @param sala Sala
	 * 
	 * @return Lista de {@link ProcessoAudiencia} 
	 * com o status de designada e de acordo com os parãmetros informados.
	 */
	@SuppressWarnings("unchecked")
	/**
	 * Método responsável por recuperar as audiências agendadas para um determinado Orgão Julgador em uma data especifica.
	 * 
	 * @param orgaoJulgador
	 * @param dataInicio Data de início da pesquisa
	 * @param sala Sala onde as audiências foram marcadas
	 * @return Lista de audiências designadas no período
	 */
	public List<ProcessoAudiencia> procurarSalasComAudienciaMarcadaPorDia(OrgaoJulgador orgaoJulgador, Date dataInicio, TipoAudiencia tipoAudiencia, Sala sala) {

		StringBuilder jpql = new StringBuilder("from ProcessoAudiencia pa ");
		jpql.append("where pa.processoTrf.orgaoJulgador = :orgaoJulgador ");
		jpql.append("and pa.dtInicio between :dataInicio and :dataFim ");
		jpql.append("and pa.statusAudiencia = :statusAudiencia ");
		jpql.append("and pa.tipoAudiencia = :tipoAudiencia ");
		
		if (sala != null) {
			jpql.append("and pa.salaAudiencia = :sala");
		}
		
		Query query = getEntityManager().createQuery(jpql.toString())
				.setParameter("orgaoJulgador", orgaoJulgador)
				.setParameter("dataInicio", DateUtil.getBeginningOfDay(dataInicio))
				.setParameter("dataFim", DateUtil.getEndOfDay(dataInicio))
				.setParameter("statusAudiencia", StatusAudienciaEnum.M)
				.setParameter("tipoAudiencia", tipoAudiencia);
		
		if (sala != null) {
			query.setParameter("sala", sala);
		}
		
		return query.getResultList();
	}
	
	/**
	 * Método responsável por recuperar as audiências agendadas para um determinado Orgão Julgador em um intervalo de datas.
	 * 
	 * @param orgaoJulgador
	 * @param dataInicio Data de início da pesquisa
	 * @param dataFim Data de início da pesquisa
	 * @return Lista de audiências designadas no período
	 */
	public List<ProcessoAudiencia> procurarSalasComAudienciaMarcadaPorDia(OrgaoJulgador orgaoJulgador, Date dataInicio, Date dataFim, Sala sala) {
		return procurarSalasComAudienciaMarcada(orgaoJulgador, dataInicio, dataFim, null, sala);
	}
	
	/**
	 * Método responsável por recuperar as audiências agendadas para um determinado Orgão Julgador em uma data especifica.
	 * 
	 * @param orgaoJulgador
	 * @param dataInicio Data de início da pesquisa
	 * @param sala Sala onde as audiências foram marcadas
	 * @return Lista de audiências designadas no período
	 */
	public List<ProcessoAudiencia> procurarSalasComAudienciaMarcadaPorDia(OrgaoJulgador orgaoJulgador, Date dataInicio, TipoAudiencia tipoAudiencia) {
		return procurarSalasComAudienciaMarcada(orgaoJulgador, dataInicio, null, tipoAudiencia, null);
	}

	/**
	 * 
	 * Método responsável por recuperar as audiências agendadas para uma sala e data especifica.
	 * @param orgaoJulgador
	 * @param dataInicio Data de início da pesquisa
	 * @param tipoAudiencia 
	 * @param sala Sala onde as audiências foram marcadas
	 * @return Lista de audiências designadas no período
	 */
	public List<ProcessoAudiencia> procurarSalaEspecificaComAudienciaMarcadaPorDia(OrgaoJulgador orgaoJulgador, Date dataInicio, TipoAudiencia tipoAudiencia,
			Sala sala) {
		return procurarSalasComAudienciaMarcada(orgaoJulgador, dataInicio, null, tipoAudiencia, sala);
	}

	/**
	 * Método responsável por recuperar as audiências agendadas para um determinado Orgão Julgador em uma data especifica.
	 * 
	 * @param orgaoJulgador
	 * @param dataInicio Data de início da pesquisa
	 * @param sala Sala onde as audiências foram marcadas
	 * @return Lista de audiências designadas no período
	 */
	@SuppressWarnings("unchecked")
	private List<ProcessoAudiencia> procurarSalasComAudienciaMarcada(OrgaoJulgador orgaoJulgador, Date dataInicio, Date dataFim, TipoAudiencia tipoAudiencia, Sala sala) {
		StringBuilder sql = new StringBuilder();
		sql.append(" FROM ProcessoAudiencia pa");
		sql.append(" WHERE 1=1 ");
		if(Objects.isNull(dataFim))	{
			dataFim = DateUtil.getEndOfDay(dataInicio);			
		}	
		sql.append(" AND pa.dtInicio BETWEEN :dataInicio AND :dataFim");
		sql.append(" AND pa.statusAudiencia = :statusAudiencia ");
		
		if(orgaoJulgador != null){
			sql.append(" AND pa.salaAudiencia.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador");
		}
		
		if(tipoAudiencia != null){
			sql.append(" AND pa.tipoAudiencia = :tipoAudiencia");
		}
		
		if(sala != null){
			sql.append(" AND pa.salaAudiencia = :sala"); 
		}
		// Recuperar as Salas com audiências marcadas na data especificada e a quantidade de audiências por sala
		Query query = getEntityManager().createQuery(sql.toString());

		query.setParameter("dataInicio", dataInicio);
		query.setParameter("dataFim", dataFim);
		query.setParameter("statusAudiencia", StatusAudienciaEnum.M);
		
		if(orgaoJulgador != null){
			query.setParameter("idOrgaoJulgador", orgaoJulgador.getIdOrgaoJulgador());
		}
		
		if(tipoAudiencia != null){
			query.setParameter("tipoAudiencia", tipoAudiencia);
		}

		if(sala != null){
			query.setParameter("sala",sala);
		}

		return query.getResultList();
	}
	
	/**
	 * Método responsável por recuperar as audiências designadas no período do bloqueio de pauta
	 * @param dataInicio Data de início da pesquisa
	 * @param dataFim Data de fim da pesquisa 
	 * @param salaAudiencia Sala onde as audiências foram marcadas
	 * @return Lista de audiências designadas no período
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoAudiencia> procurarAudienciasDesignadasPorPeriodo(Date dataInicio, Date dataFim, Sala salaAudiencia) { 
		Query query = getEntityManager()
				.createQuery(
						" from ProcessoAudiencia pa"
						+ " where pa.salaAudiencia.orgaoJulgador = :orgaoJulgador"
						+ " and pa.salaAudiencia = :salaAudiencia"
						+ " and (pa.dtInicio between :dataInicio and :dataFim or pa.dtFim between :dataInicio and :dataFim)"
						+ " and pa.statusAudiencia = 'M'"
						+ " and pa.inAtivo = true"
						+ " order by pa.dtInicio")
						.setParameter("orgaoJulgador", salaAudiencia.getOrgaoJulgador())
						.setParameter("salaAudiencia", salaAudiencia)
						.setParameter("dataInicio", dataInicio)
						.setParameter("dataFim", dataFim);
		
		return query.getResultList();
	}
	
	/**
	 * /** Método responsável por listar as audiências abertas por processo a
	 * partir de uma determinada data
	 * 
	 * @param processo
	 *            {@link ProcessoTrf} a ser pesquisado.
	 * 
	 * @param aPartirDe
	 *            {@link Date} com a data a partir para pesquisa.
	 * 
	 * @return {@link List} de {@link ProcessoAudiencia} com as audiências
	 *         abertas/agendadas para o processo
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoAudiencia> procurarAudienciasAbertasPorProcesso(ProcessoTrf processo, Date aPartirDe) {
		// Recuperar as Audiências designadas no período especificado para o órgão julgador
		StringBuilder sql = new StringBuilder();
		sql.append(
				" from ProcessoAudiencia pa"
				+ " where pa.processoTrf = :processo"
				+ " and pa.statusAudiencia = 'M'"
				+ " and pa.inAtivo = true");

		if (aPartirDe != null) {
			sql.append(" and pa.dtFim >= :dataPesquisa");
		}
		
		Query query = getEntityManager().createQuery(sql.toString()).setParameter("processo", processo);
		
		if (aPartirDe != null) {
			query.setParameter("dataPesquisa", aPartirDe);
		}
		
		return query.getResultList();
	}
	
	public String createQueryString(Search search) {
		Map<String, Object> params = new HashMap<String, Object>();
		return this.createQueryString(search, params);
	}
	
	public void loadCriterias(StringBuilder sb, Search search, Map<String, Object> params) {
		super.loadCriterias(sb, search, params);
	}
	
	/**
	 * Método responsável por recuperar as audiências que foram marcadas para uma pessoa em um determinado período.
	 * 
	 * @param pessoa Pessoa.
	 * @param dataInicio Data de início da marcação
	 * @param dataFim Data fim da marcação
	 * @return Audiências que foram marcadas para uma pessoa em um determinado período.
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoAudiencia> recuperarAudienciasMarcadas(Pessoa pessoa, Date dataInicio, Date dataFim) {
		StringBuilder jpql = new StringBuilder("from ProcessoAudiencia pa ");
		jpql.append("join fetch pa.processoTrf ptf join fetch ptf.processoParteList pp ");
		jpql.append("where pa.statusAudiencia = :statusDesignada ");
		jpql.append("and pp.pessoa.idPessoa = :idPessoa ");
		jpql.append("and ((pa.dtInicio > :dataInicio and pa.dtInicio < :dataFim) or (pa.dtFim > :dataInicio and pa.dtFim < :dataFim))");
		
		Query query = getEntityManager().createQuery(jpql.toString())
			.setParameter("statusDesignada", StatusAudienciaEnum.M)
			.setParameter("idPessoa", pessoa.getIdPessoa())
			.setParameter("dataInicio", dataInicio)
			.setParameter("dataFim", dataFim);
	
		return query.getResultList();
	}

	@Override
	public Object getId(ProcessoAudiencia e) {
		return e.getIdProcessoAudiencia();
	}

}
