package br.jus.cnj.pje.business.dao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.jus.cnj.pje.util.CollectionUtilsPje;
import org.apache.commons.lang.BooleanUtils;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.utils.Constantes;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.ClasseJudicialDTO;
import br.jus.pje.nucleo.entidades.AgrupamentoClasseJudicial;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@Name(ClasseJudicialDAO.NAME)
public class ClasseJudicialDAO extends BaseDAO<ClasseJudicial> {

	private static final String ID_ORGAO_JULGADOR = "idOrgaoJulgador";
	private static final String ID_APLICACAO_CLASSE = "idAplicacaoClasse";
	private static final String ID_JURISDICAO = "idJurisdicao";
	private static final String PARAM_COD_CLASSE_PAI = "codClassePai";
	private static final String AND_EXISTS = "AND EXISTS (";
	public static final String NAME = "classeJudicialDAO";
	private static final String ID_CLASSE_JUDICIAL = "idClasseJudicial";
	private static final String PARAM_ID_CLASSE_JUDICIAL = "idClasseJudicial";
	private static final String PARAM_COD_AGRUPAMENTO = "codAgrupamento";
	
	@Override
	public Object getId(ClasseJudicial e) {
		return e.getIdClasseJudicial();
	}

	@SuppressWarnings("unchecked")
	public List<ClasseJudicial> getClassesDisponiveis(List<Competencia> competencias, Boolean inicial, Boolean recursal,
			Boolean incidental, Boolean segredo, Boolean complementar) {
		String baseQuery = "SELECT DISTINCT ca.classeAplicacao.classeJudicial FROM CompetenciaClasseAssunto AS ca "
				+ "	JOIN ca.classeAplicacao.classeJudicial classe " + "	WHERE ca.classeAplicacao.ativo = true "
				+ "	AND classe.ativo = true "
				+ "	AND (CURRENT_DATE >= ca.dataInicio AND (ca.dataFim IS NULL OR CURRENT_DATE <= ca.dataFim)) "
				+ "	AND ca.competencia IN (:competencias) ";
		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("competencias", competencias);

		// Ao passar null nas listas em que ocorre o "in (:list)" evita-se o erro
		// "unexpected end of subtree"
		if (Util.isEmpty(parametros.get("competencias"))) {
			parametros.put("competencias", null);
		}

		StringBuilder sb = new StringBuilder(baseQuery);
		if (inicial != null) {
			sb.append("	AND classe.inicial = :inicial ");
			parametros.put("inicial", inicial);
		}
		if (recursal != null) {
			sb.append("	AND classe.recursal = :recursal ");
			parametros.put("recursal", recursal);
		}
		if (incidental != null) {
			sb.append("	AND classe.incidental = :incidental ");
			parametros.put("incidental", incidental);
		}
		if (segredo != null) {
			sb.append("	AND classe.segredoJustica = :segredo ");
			parametros.put("segredo", segredo);
		}
		if (complementar != null) {
			sb.append("	AND classe.complementar = :complementar ");
			parametros.put("complementar", complementar);
		}
		Query q = entityManager.createQuery(sb.toString());
		loadParameters(q, parametros);
		return (List<ClasseJudicial>) q.getResultList();
	}

	/**
	 * Recupera a lista de classes judiciais que fazem parte do(s) agrupamento(s)
	 * dados.
	 * 
	 * @param agrupamentos a lista de agrupamentos cujas classes se pretende
	 *                     recuperar
	 * @return a lista de classes que fazem parte do agrupamento dado
	 */
	@SuppressWarnings("unchecked")
	public List<ClasseJudicial> getClassesAgrupadas(AgrupamentoClasseJudicial... agrupamentos) {
		String query = "SELECT ca.classe FROM AgrupamentoClasseJudicial AS a JOIN a.classeJudicialAgrupamentoList ca WHERE a IN (:agrupamentos)";
		Query q = entityManager.createQuery(query);
		q.setParameter("agrupamentos", Arrays.asList(agrupamentos));
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public ClasseJudicial findByCodigo(String codigo, boolean apenasClassesAtivas) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT c FROM ClasseJudicial c where c.codClasseJudicial = :codClasseJudicial ");

		if (apenasClassesAtivas) {
			query.append(" and c.ativo = true ");
		}

		Query q = entityManager.createQuery(query.toString());
		q.setParameter("codClasseJudicial", codigo);

		List<ClasseJudicial> classes = q.getResultList();
		ClasseJudicial classeJudicial = null;
		if (classes != null && !classes.isEmpty()) {
			classeJudicial = classes.get(0);
		}
		return classeJudicial;
	}

	public List<ClasseJudicial> recuperarClassesJudiciais(int idJurisdicao, boolean inicial, boolean incidental) {
		return this.recuperarClassesJudiciais(idJurisdicao, inicial, incidental, 0);
	}

	public List<ClasseJudicial> recuperarClassesJudiciais(int idJurisdicao, boolean inicial, boolean incidental,
			int idCompetencia) {
		return recuperarClassesJudiciais(idJurisdicao, inicial, incidental, idCompetencia, 0);
	}

	public List<ClasseJudicial> recuperarClassesJudiciais(int idJurisdicao, boolean inicial, boolean incidental,int idCompetencia, int idClasseJudicial) {
		return recuperarClassesJudiciais(idJurisdicao, inicial, incidental, idCompetencia, idClasseJudicial, false);
	}

	/**
	 * Recupera classes judiciais conforme RN402 e PJEII-21909.
	 * 
	 * @param idJurisdicao  Id da jurisdição.
	 * @param inicial       Se deve recuperar classes iniciais.
	 * @param incidental    Se deve recuperar classes incidentais.
	 * @param idCompetencia Id da competencia.
	 * @return List<ClasseJudicial> lista de classes judiciais.
	 */
	@SuppressWarnings("unchecked")
	public List<ClasseJudicial> recuperarClassesJudiciais(int idJurisdicao, boolean inicial, boolean incidental,
			int idCompetencia, int idClasseJudicial, boolean somenteIncidental) {
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT o FROM ClasseJudicial o ");
		sql.append("WHERE o.ativo = true ");
		sql.append("AND o.fluxo IS NOT NULL ");

		if (inicial) {
			sql.append("AND o.inicial = true ");
		}

		if (incidental) {
			sql.append("AND o.incidental = true ");
		}

		if (Authenticator.isUsuarioExterno()) {
			sql.append("AND o.recursal = false ");
		}

		if (idClasseJudicial > 0) {
			sql.append("AND o.idClasseJudicial = :idClasseJudicial ");
		}
		sql.append(AND_EXISTS + montarSqlClassesPossuiCompetenciaJurisdicao(idCompetencia > 0, somenteIncidental) + ")");
		sql.append("ORDER BY o.classeJudicial ASC ");

		Query query = getEntityManager().createQuery(sql.toString());
		query.setParameter("idJurisdicao", idJurisdicao);
		if (idCompetencia > 0) {
			query.setParameter("idCompetencia", idCompetencia);
		}
		if (idClasseJudicial > 0) {
			query.setParameter(PARAM_ID_CLASSE_JUDICIAL, idClasseJudicial);
		}
		query.setParameter("idAplicacaoClasse", ParametroUtil.instance().getAplicacaoSistema().getIdAplicacaoClasse());
		query.setParameter("idOrgaoJustica", ParametroUtil.instance().getOrgaoJustica().getIdOrgaoJustica());

		if (Authenticator.isUsuarioInterno()) {
			query.setParameter("idOrgaoJulgador", Authenticator.getIdOrgaoJulgadorAtual());
			query.setParameter("idOrgaoJulgadorColegiado", Authenticator.getIdOrgaoJulgadorColegiadoAtual());
		}
		if (somenteIncidental) {
			String idOrgaoJulgadorCompetencia = ParametroUtil.getParametro("tjrj:permitir:idOrgaoJulgadorCompetencia");
			if (idOrgaoJulgadorCompetencia != null && !idOrgaoJulgadorCompetencia.isEmpty()) {
				query.setParameter("idOrgaoJulgadorCompetencia", CollectionUtilsPje.convertStringToIntegerList(idOrgaoJulgadorCompetencia));
			}
		}

		return query.getResultList();
	}

	/**
	 * Recupera classes judiciais retificação de autos.
	 * 
	 * @param idJurisdicao Id da jurisdição.
	 * @return List<ClasseJudicial> lista de classes judiciais.
	 */
	@SuppressWarnings("unchecked")
	public List<ClasseJudicial> recuperarClassesJudiciaisRetificacaoAutos(int idJurisdicao) {
		Query query = getQueryClassesJudiciaisRetificacaoAutos(idJurisdicao, null, false);
		return query.getResultList();
	}

	/**
	 * Recupera se existem classes judiciais retificação de autos baseados nos
	 * parâmetros idJurisdicao e idClasseJudicial.
	 * 
	 * @param idJurisdicao Id da jurisdição.
	 * @return Boolean existemClassesJudiciaisRetificacaoAutos
	 */
	public boolean isExistemClassesJudiciaisRetificacaoAutos(int idJurisdicao, Integer idClasseJudicial) {
		Query query = getQueryClassesJudiciaisRetificacaoAutos(idJurisdicao, idClasseJudicial, true);
		return EntityUtil.getSingleResultCount(query) > 0;
	}

	private Query getQueryClassesJudiciaisRetificacaoAutos(int idJurisdicao, Integer idClasseJudicial,
			boolean selectCount) {
		StringBuilder sql = new StringBuilder();
		if (selectCount) {
			sql.append("SELECT count(o.idClasseJudicial) ");
		} else {
			sql.append("SELECT o ");
		}
		sql.append("FROM ClasseJudicial o ");
		sql.append("WHERE o.ativo = true AND (o.incidental = true OR o.inicial = true OR o.recursal = true) ");
		sql.append(AND_EXISTS + montarSqlClassesPossuiCompetenciaJurisdicao(false, false) + ")");

		if (idClasseJudicial != null) {
			sql.append(" AND o.idClasseJudicial = :idClasseJudicial ");
		}

		if (!selectCount) {
			sql.append("ORDER BY o.classeJudicial ASC ");
		}

		Query query = getEntityManager().createQuery(sql.toString());
		query.setParameter("idJurisdicao", idJurisdicao);
		query.setParameter("idAplicacaoClasse", ParametroUtil.instance().getAplicacaoSistema().getIdAplicacaoClasse());
		query.setParameter("idOrgaoJustica", ParametroUtil.instance().getOrgaoJustica().getIdOrgaoJustica());

		if (Authenticator.isUsuarioInterno()) {
			query.setParameter("idOrgaoJulgador", Authenticator.getIdOrgaoJulgadorAtual());
			query.setParameter("idOrgaoJulgadorColegiado", Authenticator.getIdOrgaoJulgadorColegiadoAtual());
		}
		if (idClasseJudicial != null) {
			query.setParameter(PARAM_ID_CLASSE_JUDICIAL, idClasseJudicial);
		}

		return query;
	}

	/**
	 * Recupera classes judiciais para cadastro de um novo processo em primeiro
	 * grau.
	 * 
	 * @param jusPostulandi        Se deve recuperar classes que permite jus
	 *                             postulandi.
	 * @param idJurisdicao         Id da jurisdição.
	 * @param classeJudicialFiltro Parametro de filtro da classe.
	 * @return List<ClasseJudicial> lista de classes judiciais.
	 */
	@SuppressWarnings("unchecked")
	public List<ClasseJudicial> recuperarClassesJudiciaisNovoProcessoPrimeiroGrau(Integer idAreaDireito,
			Boolean jusPostulandi, int idJurisdicao, String classeJudicialFiltro) {

		StringBuilder sql = new StringBuilder(
				"SELECT DISTINCT(o) FROM CompetenciaAreaDireito a JOIN a.classeJudicial o ")
				.append("WHERE a.idJurisdicao = :idJurisdicao ");

		if (idAreaDireito != null) {
			sql.append("AND a.idAreaDireito = :idAreaDireito ");
		}

		if (Authenticator.isUsuarioExterno()) {
			sql.append("AND (o.incidental = false OR o.inicial = true) AND o.recursal = false ");
		} else {
			sql.append("AND (o.incidental = false OR o.recursal = true OR o.inicial = true) ");
		}

		if (BooleanUtils.isTrue(jusPostulandi)) {
			sql.append("AND o.jusPostulandi = true ");
		}

		if (classeJudicialFiltro != null) {
			sql.append(
					"AND (lower(TO_ASCII(o.classeJudicial)) like '%' || lower(TO_ASCII(:classeJudicialFiltro)) || '%' OR o.codClasseJudicial = :classeJudicialFiltro) ");
		}

		sql.append("ORDER BY o.classeJudicial ");

		Query query = getEntityManager().createQuery(sql.toString());
		query.setParameter("idJurisdicao", idJurisdicao);

		if (idAreaDireito != null) {
			query.setParameter("idAreaDireito", idAreaDireito);
		}

		if (classeJudicialFiltro != null) {
			query.setParameter("classeJudicialFiltro", classeJudicialFiltro);
		}

		return query.getResultList();
	}
	
	public boolean isClasseJudicialIncidentalValida(Jurisdicao jurisdicao, OrgaoJulgador orgaoJulgador,
			ClasseJudicial classeJudicial) {
		StringBuilder jpql = new StringBuilder();
		jpql.append("select count(o) ");
		jpql.append(getJpqlCorrelacaoClasseJudicialIncidental());
		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("isUsuarioExterno", Authenticator.isUsuarioExterno());
		query.setParameter(ID_JURISDICAO, jurisdicao.getIdJurisdicao());
		query.setParameter(ID_ORGAO_JULGADOR, orgaoJulgador.getIdOrgaoJulgador());
		query.setParameter(ID_APLICACAO_CLASSE, ParametroUtil.instance().getAplicacaoSistema().getIdAplicacaoClasse());
		query.setParameter(PARAM_ID_CLASSE_JUDICIAL, classeJudicial.getIdClasseJudicial());
		try {
			Long qtdRegistros = (Long) query.getSingleResult();
			return qtdRegistros > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	private String getJpqlCorrelacaoClasseJudicialIncidental() {
		return  " from ClasseJudicial o " 
				+ "where o.ativo = true "
				+ "and o.fluxo is not null "
				+ "and o.classeJudicial.idClasseJudicial = :idClasseJudicial "
				+ "and (((o.incidental = true or o.recursal = true) and (true = :isUsuarioExterno)) "
				+ "or ((o.incidental = true and o.recursal = false) and true = :isUsuarioExterno)) "
				+ "and o in (select distinct(cj) from ClasseJudicial cj "
				+ "inner join cj.classeAplicacaoList cat "
				+ "inner join cat.competenciaClasseAssuntoList cca "
				+ "inner join cca.competencia comp "
				+ "inner join comp.orgaoJulgadorCompetenciaList ojComp "
				+ "inner join ojComp.orgaoJulgador oj "
				+ "where oj.jurisdicao.idJurisdicao = :idJurisdicao "
				+ "and cca.competencia.ativo = true "
				+ "and oj.ativo = true "
				+ "and oj.idOrgaoJulgador = :idOrgaoJulgador "
				+ "and oj.aplicacaoClasse = cat.aplicacaoClasse "
				+ "and ojComp.competencia = cca.competencia "
				+ "and current_date >= ojComp.dataInicio "
				+ "and (ojComp.dataFim >= current_date or ojComp.dataFim is null) "
				+ "and cat.aplicacaoClasse.idAplicacaoClasse = :idAplicacaoClasse)";
	}

	/**
	 * Monta sql para retornar todas as classes que possuem compentência na
	 * jurisdição.
	 * 
	 * @return String query que retona as competencias de uma classe judicial.
	 */
	private String montarSqlClassesPossuiCompetenciaJurisdicao(boolean identificaCompetencia, boolean somenteIncidental) {
		boolean primeiroGrau = ParametroUtil.instance().isPrimeiroGrau();

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT 1 ");
		sql.append("FROM Competencia comp ");
		sql.append("INNER JOIN comp.competenciaClasseAssuntoList cca ");
		sql.append("INNER JOIN cca.classeAplicacao clap ");

		if (primeiroGrau) {
			sql.append("INNER JOIN comp.orgaoJulgadorCompetenciaList ojcp ");
			sql.append("INNER JOIN ojcp.orgaoJulgador oj ");
		} else {
			sql.append("LEFT JOIN comp.orgaoJulgadorCompetenciaList ojcp ");
			sql.append("LEFT JOIN ojcp.orgaoJulgador oj ");
			sql.append("LEFT JOIN comp.orgaoJulgadorColegiadoCompetenciaList ojccp ");
			sql.append("LEFT JOIN ojccp.orgaoJulgadorColegiado ojc ");
		}

		sql.append(
				"WHERE clap.aplicacaoClasse.idAplicacaoClasse = :idAplicacaoClasse AND clap.aplicacaoClasse.ativo = true ");
		sql.append("AND clap.orgaoJustica.idOrgaoJustica = :idOrgaoJustica AND clap.orgaoJustica.ativo = true ");
		sql.append("AND oj.jurisdicao.idJurisdicao = :idJurisdicao ");
		if (identificaCompetencia) {
			sql.append("AND comp.idCompetencia = :idCompetencia ");
		}
		sql.append("AND current_date >= cca.dataInicio ");
		sql.append("AND (cca.dataFim >= current_date OR cca.dataFim IS NULL) ");
		sql.append("AND comp.ativo = true ");
		sql.append("AND clap.classeJudicial.idClasseJudicial = o.idClasseJudicial ");
		sql.append("AND (((ojcp.dataFim >= current_date OR ojcp.dataFim IS NULL) ");
		sql.append("AND oj.ativo = true) ");

		if (!primeiroGrau) {
			sql.append("OR ");
			sql.append("((ojccp.dataFim >= current_date OR ojccp.dataFim IS NULL) ");
			sql.append("AND ojc.ativo = true) ");
		}
		if (somenteIncidental) {
			String idOrgaoJulgadorCompetencia = ParametroUtil.getParametro("tjrj:permitir:idOrgaoJulgadorCompetencia");
			if (idOrgaoJulgadorCompetencia != null && !idOrgaoJulgadorCompetencia.isEmpty()) {
				sql.append("OR oj.idOrgaoJulgador in (:idOrgaoJulgadorCompetencia) ");
			}
		}
		sql.append(") ");

		if (Authenticator.isUsuarioExterno()) {
			sql.append("AND comp.usuariosExternosPodemProtocolar = true ");
		} else if (Authenticator.isUsuarioInterno()) {
			sql.append("AND ( ");
			sql.append("comp.restricaoProtocoloServidorInteno = 'TD' ");
			sql.append(" OR (" + montarSqlApenasOrgaoJulgadorPodemProtocolar() + ") ");
			sql.append(" OR (" + montarSqlApenasOrgaoJulgadorOuColegiadoPodemProtocolar() + ")");
			sql.append(") ");
		}

		return sql.toString();
	}

	/**
	 * Script sql que verifica se a competência permite que apenas os órgãos
	 * julgadores podem protocolar.
	 * 
	 * @return String query sql
	 */
	private String montarSqlApenasOrgaoJulgadorPodemProtocolar() {
		StringBuilder sql = new StringBuilder();
		sql.append("comp.restricaoProtocoloServidorInteno = 'AOJ' ");
		sql.append(AND_EXISTS + montarSqlExisteOrgaoJulgadorCompetencia() + ") ");
		return sql.toString();
	}

	/**
	 * Script sql que verifica se a competência permite que apenas os órgãos
	 * julgadores ou órgão julgadores colegiados podem protocolar.
	 * 
	 * @return String query sql
	 */
	private String montarSqlApenasOrgaoJulgadorOuColegiadoPodemProtocolar() {
		StringBuilder sql = new StringBuilder();
		sql.append("comp.restricaoProtocoloServidorInteno = 'AOJC' ");
		sql.append("AND ( ");
		sql.append("EXISTS (" + montarSqlExisteOrgaoJulgadorColegiadoCompetencia() + ")");
		sql.append("  OR EXISTS (" + montarSqlExisteOrgaoJulgadorCompetencia() + ") ");
		sql.append(") ");
		return sql.toString();
	}

	/**
	 * Script sql que verifica se o órgão julgador atual está associado a
	 * competência.
	 * 
	 * @return String query sql
	 */
	private String montarSqlExisteOrgaoJulgadorCompetencia() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT 1 ");
		sql.append("FROM OrgaoJulgadorCompetencia ojcp ");
		sql.append("WHERE ojcp.competencia.idCompetencia = comp.idCompetencia ");
		sql.append("AND ojcp.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ");
		return sql.toString();
	}

	/**
	 * Script sql que verifica se o órgão julgador colegiado atual está associado a
	 * competência.
	 * 
	 * @return String query sql
	 */
	private String montarSqlExisteOrgaoJulgadorColegiadoCompetencia() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT 1 ");
		sql.append("FROM OrgaoJulgadorColegiadoCompetencia ojccp ");
		sql.append("WHERE ojccp.competencia.idCompetencia = comp.idCompetencia ");
		sql.append("AND ojccp.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = :idOrgaoJulgadorColegiado ");
		return sql.toString();
	}

	@SuppressWarnings("unchecked")
	public List<ClasseJudicialDTO> findAllClasseJudicialDTO() {
		StringBuilder sb = new StringBuilder("");
		sb.append(
				"SELECT new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.ClasseJudicialDTO(cj.idClasseJudicial, cj.classeJudicialCompleto, cj.classeJudicialSigla)");
		sb.append("FROM ClasseJudicial cj ");
		sb.append("WHERE cj.ativo=true ");

		Query q = this.entityManager.createQuery(sb.toString());

		return q.getResultList();
	}

	/**
	 * @param classeJudicial
	 * @return True se a classe for de Execução Fiscal.
	 */
	public boolean isClasseExecucaoFiscal(ClasseJudicial classeJudicial) {
		return isClasseDoAgrupamento(classeJudicial, Constantes.COD_AGRUPAMENTO_EXECUCAO_FISCAL);
	}

	/**
	 * Retorna true se a classe for do agrupamento passado por parâmetro.
	 * 
	 * @param classeJudicial ClasseJudicial.
	 * @param agrupamento    Código do agrupamento de classes. (Ex:
	 *                       Constantes.COD_AGRUPAMENTO_EXECUCAO_FISCAL)
	 * @return Boleano.
	 */
	public boolean isClasseDoAgrupamento(ClasseJudicial classeJudicial, String codAgrupamento) {
		if (classeJudicial != null && classeJudicial.getIdClasseJudicial() != 0) {
			String hql = " select count(o.idAgrupamentoClasses) from ClasseJudicialAgrupamento o"
					+ " where o.agrupamento.ativo = true and o.classe.id = :idClasseJudicial"
					+ " and o.agrupamento.codAgrupamento = :codAgrupamento";

			Query qry = getEntityManager().createQuery(hql)
						.setParameter(PARAM_ID_CLASSE_JUDICIAL, classeJudicial.getIdClasseJudicial())
						.setParameter(PARAM_COD_AGRUPAMENTO, codAgrupamento);
			try {
				Long count = (Long) qry.getSingleResult();
				return count.compareTo(0L) > 0;
			} catch (NoResultException ex) {
				return false;
			}
		}
		return false;
	}


	public boolean isClasseCriminal(ClasseJudicial classeJudicial) {
		return isClasseDoAgrupamento(classeJudicial, Constantes.COD_AGRUPAMENTO_CRIMINAL);
	}

	public boolean isClasseInfracional(ClasseJudicial classeJudicial) {
		return isClasseFilha(Constantes.COD_CLASSE_PAI_INFRACIONAL, classeJudicial);
	}

	private boolean isClasseFilha(String codClassePai, ClasseJudicial classeJudicial) {
		if (classeJudicial != null && classeJudicial.getIdClasseJudicial() != 0) {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT EXISTS ( ");
			sql.append(" SELECT cl.id_classe_judicial FROM client.tb_classe_judicial cl ");
			sql.append(" JOIN client.tb_classe_judicial clpai ON ");
			sql.append(
					" (clpai.nr_faixa_inferior < cl.nr_faixa_inferior AND clpai.nr_faixa_superior > cl.nr_faixa_superior AND clpai.in_ativo = true) ");
			sql.append(" WHERE cl.in_ativo = true ");
			sql.append(" AND cl.in_possui_filhos = false ");
			sql.append(" AND clpai.cd_classe_judicial = :codClassePai ");
			sql.append(" AND cl.id_classe_judicial = :idClasseJudicial ");
			sql.append(")");
			Query query = EntityUtil.createNativeQuery(getEntityManager(),sql.toString(),"tb_classe_judicial");	
			query.setParameter(PARAM_ID_CLASSE_JUDICIAL, classeJudicial.getIdClasseJudicial());
			query.setParameter(PARAM_COD_CLASSE_PAI, codClassePai);
			return EntityUtil.getSingleResult(query);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public List<ClasseJudicial> findByCompetencia(int idCompetencia) {
		StringBuilder sql = new StringBuilder();
		sql.append(" select distinct cca.classeAplicacao.classeJudicial ");
		sql.append(" from CompetenciaClasseAssunto cca ");
		sql.append(" where cca.competencia.idCompetencia = :idCompetencia ");
		sql.append(" and (cca.dataFim is null or cca.dataFim >= current_date) ");

		Query query = getEntityManager().createQuery(sql.toString());
		query.setParameter("idCompetencia", idCompetencia);

		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<ClasseJudicial> findByCodigos(List<String> codigos, boolean apenasClassesAtivas) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT c FROM ClasseJudicial c where c.codClasseJudicial in (:codigos) ");
		if (apenasClassesAtivas) {
			query.append(" and c.ativo = true ");
		}
		Query q = entityManager.createQuery(query.toString());
		q.setParameter("codigos", codigos);
		return q.getResultList();
	}

}