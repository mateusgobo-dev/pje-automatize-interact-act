package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import br.jus.cnj.pje.util.CollectionUtilsPje;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.webservice.json.InformacaoUsuarioSessao;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;
import br.jus.pje.nucleo.entidades.AssociacaoDimensaoPessoalEnum;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.DimensaoPessoal;
import br.jus.pje.nucleo.entidades.DimensaoPessoalPessoa;
import br.jus.pje.nucleo.entidades.DimensaoPessoalTipoPessoa;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.TipoVinculacaoUsuarioEnum;

@Name(CompetenciaDAO.NAME)
@Scope(ScopeType.EVENT)
public class CompetenciaDAO extends BaseDAO<Competencia> {

	public static final String NAME = "competenciaDAO";
	
	public static final String QUERY_PARAMETER_CLASSE_JUDICIAL = "classeJudicial";
	public static final String QUERY_PARAMETER_PROCESSO_TRF = "processoTrf";
	public static final String QUERY_PARAMETER_ORGAO_JULGADOR = "orgaoJulgador";
	public static final String QUERY_PARAMETER_APLICACAO_CLASSE = "aplicacaoClasse";

	public static final String GET_COMPETENCIA_BY_PROCESSO_TRF_QUERY = "select cca.competencia from CompetenciaClasseAssunto cca "
			+ "where cca.classeAplicacao = (select ca from ClasseAplicacao " + "ca where ca.classeJudicial = :"
			+ QUERY_PARAMETER_CLASSE_JUDICIAL + " " + "and ca.aplicacaoClasse = :" + QUERY_PARAMETER_APLICACAO_CLASSE
			+ ") " + "and cca.assuntoTrf = (select min(pa.assuntoTrf."
			+ "idAssuntoTrf) from ProcessoAssunto pa where pa.processoTrf " + "= :" + QUERY_PARAMETER_PROCESSO_TRF
			+ ")";

	public static final String COMPETENCIA_ITEMS_BY_ORGAO_JULGADOR_QUERY = "select o.competencia from OrgaoJulgadorCompetencia o where "
			+ "o.orgaoJulgador = :" + QUERY_PARAMETER_ORGAO_JULGADOR + " order by o.competencia.competencia";

	@Override
	public Integer getId(Competencia e){
		return e.getIdCompetencia();
	}

	/**
	 * Recupera competências pela jurisdição.
	 * @param jurisdicao Jurisdicão.
	 * @return List<Competencia> Lista de competências.
	 */
	public List<Competencia> getCompetenciasPorJurisdicao(Jurisdicao jurisdicao) {
		return getCompetenciasPorJurisdicao(jurisdicao, false);
	}

	public List<Competencia> getCompetenciasPorJurisdicao(Jurisdicao jurisdicao, boolean somenteIncidental) {
		Set<Competencia> competencias = new HashSet<Competencia>();
		if (jurisdicao != null) {
			competencias.addAll(recuperarCompetenciasPorOrgaoJulgador(jurisdicao, somenteIncidental));
			competencias.addAll(recuperarCompetenciasPorOrgaoJulgadorColegiado(jurisdicao));
		}
		return new ArrayList<Competencia>(competencias);
	}

	public List<Competencia> recuperarCompetenciasPorOrgaoJulgador(Jurisdicao jurisdicao) {
		return recuperarCompetenciasPorOrgaoJulgador(jurisdicao, false);
	}

	/**
	 * Recupera competências pelo orgão julgador.
	 * @param jurisdicao Jurisdicão.
	 * @return List<Competencia> Lista de competências.
	 */
	public List<Competencia> recuperarCompetenciasPorOrgaoJulgador(Jurisdicao jurisdicao, boolean somenteIncidental) {
		Map<String, Object> params = new HashMap<String,Object>(0);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT c FROM Competencia AS c ");
		sql.append("INNER JOIN c.orgaoJulgadorCompetenciaList AS ojc ");
		sql.append("INNER JOIN ojc.orgaoJulgador.orgaoJulgadorCargoList AS cargo ");
		sql.append("WHERE ojc.orgaoJulgador.ativo = true ");
		sql.append("AND ojc.orgaoJulgador.jurisdicao = :jurisdicao ");
		sql.append("AND c.ativo = true ");
		sql.append("AND ojc.orgaoJulgador.aplicacaoClasse = :aplicacao ");
		sql.append("AND cargo.recebeDistribuicao = true ");
		sql.append("AND cargo.valorPeso > 0.0 ");
		sql.append("AND CURRENT_DATE >= ojc.dataInicio ");
		sql.append("AND ((ojc.dataFim IS NULL OR CURRENT_DATE <= ojc.dataFim) ");
		if (somenteIncidental) {
			String idOrgaoJulgadorCompetencia = ParametroUtil.getParametro("tjrj:permitir:idOrgaoJulgadorCompetencia");
			if (idOrgaoJulgadorCompetencia != null && !idOrgaoJulgadorCompetencia.isEmpty()) {
				sql.append("OR ojc.orgaoJulgador.idOrgaoJulgador in (:idOrgaoJulgadorCompetencia) ");
				if(!params.containsKey("idOrgaoJulgadorCompetencia")) {
					params.put("idOrgaoJulgadorCompetencia", CollectionUtilsPje.convertStringToIntegerList(idOrgaoJulgadorCompetencia));
				}
			}
		}
		sql.append(") ");


		boolean isUsuarioInterno = Authenticator.isUsuarioInterno();
		TipoVinculacaoUsuarioEnum tipoUsuarioInterno = Authenticator.getTipoUsuarioInternoAtual();
		Integer idOrgaoJulgadorUsuario = Authenticator.getIdOrgaoJulgadorAtual();
		Integer idOrgaoJulgadorColegiadoUsuario = Authenticator.getIdOrgaoJulgadorColegiadoAtual();		
		
		if(!params.containsKey("jurisdicao")) {
			params.put("jurisdicao", jurisdicao);
		}
		if(!params.containsKey("aplicacao")) {
			params.put("aplicacao", jurisdicao.getAplicacao());
		}
		
		sql.append(montarHqlRestricoesCompetencia("c", isUsuarioInterno, tipoUsuarioInterno, idOrgaoJulgadorUsuario, idOrgaoJulgadorColegiadoUsuario, params));
		
		return executarQuery(sql, params);
	}
	
	/**
	 * Recupera competências pelo orgão julgador colegiado.
	 * @param jurisdicao Jurisdicão.
	 * @return List<Competencia> Lista de competências.
	 */
	public List<Competencia> recuperarCompetenciasPorOrgaoJulgadorColegiado(Jurisdicao jurisdicao) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT c FROM Competencia AS c ");
		sql.append("INNER JOIN c.orgaoJulgadorColegiadoCompetenciaList AS ojc ");
		sql.append("INNER JOIN ojc.orgaoJulgadorColegiado AS oj ");
		sql.append("WHERE c.ativo = true ");
		sql.append("AND oj.ativo = true ");
		sql.append("AND oj.jurisdicao = :jurisdicao ");
		sql.append("AND oj.aplicacaoClasse = :aplicacao ");
		sql.append("AND CURRENT_DATE >= ojc.dataInicio ");
		sql.append("AND (ojc.dataFim IS NULL OR CURRENT_DATE <= ojc.dataFim) ");

		Map<String, Object> params = new HashMap<String,Object>(0);
		
		boolean isUsuarioInterno = Authenticator.isUsuarioInterno();
		TipoVinculacaoUsuarioEnum tipoUsuarioInterno = Authenticator.getTipoUsuarioInternoAtual();
		Integer idOrgaoJulgadorUsuario = Authenticator.getIdOrgaoJulgadorAtual();
		Integer idOrgaoJulgadorColegiadoUsuario = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
		
		if(!params.containsKey("jurisdicao")) {
			params.put("jurisdicao", jurisdicao);
		}
		if(!params.containsKey("aplicacao")) {
			params.put("aplicacao", jurisdicao.getAplicacao());
		}

		sql.append(montarHqlRestricoesCompetencia("c", isUsuarioInterno, tipoUsuarioInterno, idOrgaoJulgadorUsuario, idOrgaoJulgadorColegiadoUsuario, params));
		
		return executarQuery(sql, params);
	}
	
	public List<Competencia> getCompetenciasDisponiveis(Jurisdicao jurisdicao, OrgaoJulgador orgaoJulgador, 
			OrgaoJulgadorColegiado orgaoJulgadorColegiado, boolean somenteIncidental, 
			boolean isUsuarioInterno, TipoVinculacaoUsuarioEnum tipoUsuarioInterno,
			Integer idOrgaoJulgadorUsuario, Integer idOrgaoJulgadorColegiadoUsuario){

		Map<String, Object> params = new HashMap<String,Object>(0);
		
		AplicacaoClasse aplicacao = null;
		if(jurisdicao != null && jurisdicao.getAplicacao() != null) {
			aplicacao = jurisdicao.getAplicacao();
		}

		StringBuilder hql = new StringBuilder();
		hql.append("SELECT DISTINCT c FROM Competencia AS c ");
		hql.append("INNER JOIN c.orgaoJulgadorCompetenciaList AS ojcomp ");
		hql.append("INNER JOIN ojcomp.orgaoJulgador AS oj ");
		hql.append("INNER JOIN oj.orgaoJulgadorCargoList AS cargo ");
		if(orgaoJulgadorColegiado != null) {
			hql.append("INNER JOIN c.orgaoJulgadorColegiadoCompetenciaList AS ojcolcomp ");
			hql.append("INNER JOIN ojcolcomp.orgaoJulgadorColegiado AS ojc ");
		}
		if(somenteIncidental) {
			hql.append("INNER JOIN c.competenciaClasseAssuntoList AS compClassAssunto ");
			hql.append("INNER JOIN compClassAssunto.classeAplicacao AS classeAplicacao ");
			hql.append("INNER JOIN classeAplicacao.classeJudicial AS classeJudicial ");
		}
		hql.append("WHERE ojcomp.orgaoJulgador.ativo = true ");
		hql.append("AND c.ativo = true ");
		if(aplicacao != null) {
			hql.append("AND ojcomp.orgaoJulgador.aplicacaoClasse = :aplicacao ");

			if(!params.containsKey("aplicacao")) {
				params.put("aplicacao", aplicacao);
			}
		}
		hql.append("AND cargo.recebeDistribuicao = true ");
		hql.append("AND cargo.valorPeso > 0.0 ");
		hql.append("AND CURRENT_DATE >= ojcomp.dataInicio ");
		hql.append("AND ((ojcomp.dataFim IS NULL OR CURRENT_DATE <= ojcomp.dataFim) ");
		if (somenteIncidental) {
			String idOrgaoJulgadorCompetencia = ParametroUtil.getParametro("tjrj:permitir:idOrgaoJulgadorCompetencia");
			if (idOrgaoJulgadorCompetencia != null && !idOrgaoJulgadorCompetencia.isEmpty()) {
				hql.append("OR oj.idOrgaoJulgador in (:idOrgaoJulgadorCompetencia) ");
				if(!params.containsKey("idOrgaoJulgadorCompetencia")) {
					params.put("idOrgaoJulgadorCompetencia", CollectionUtilsPje.convertStringToIntegerList(idOrgaoJulgadorCompetencia));
				}
			}
		}
		hql.append(") ");

		if(orgaoJulgador != null) {
			hql.append("AND oj.ativo = true ");
			hql.append("AND CURRENT_DATE >= ojcomp.dataInicio ");
			//hql.append("AND (ojcomp.dataFim IS NULL OR CURRENT_DATE <= ojcomp.dataFim) ");
			hql.append("AND oj.idOrgaoJulgador = :idOrgaoJulgador ");

			if(!params.containsKey("idOrgaoJulgador")) {
				params.put("idOrgaoJulgador", orgaoJulgador.getIdOrgaoJulgador());
			}
		}
		if(jurisdicao != null) {
			hql.append("AND ojcomp.orgaoJulgador.jurisdicao = :jurisdicao ");			
			if(!params.containsKey("jurisdicao")) {
				params.put("jurisdicao", jurisdicao);
			}
		}
		
		if(orgaoJulgadorColegiado != null) {
			hql.append("AND ojc.ativo = true ");
			hql.append("AND CURRENT_DATE >= ojcolcomp.dataInicio ");
			hql.append("AND (ojcolcomp.dataFim IS NULL OR CURRENT_DATE <= ojcolcomp.dataFim) ");
			hql.append("AND ojc.idOrgaoJulgadorColegiado = :idOrgaoJulgadorColegiado ");

			if(!params.containsKey("idOrgaoJulgadorColegiado")) {
				params.put("idOrgaoJulgadorColegiado", orgaoJulgadorColegiado.getIdOrgaoJulgadorColegiado());
			}
			
			if(jurisdicao != null) {
				hql.append("AND ojc.jurisdicao = :jurisdicao ");
				if(!params.containsKey("jurisdicao")) {
					params.put("jurisdicao", jurisdicao);
				}
			}
			hql.append("AND ojc.aplicacaoClasse = :aplicacao ");
			if(!params.containsKey("aplicacao")) {
				params.put("aplicacao", aplicacao);
			}
		}
		
		if(somenteIncidental) {
			hql.append("AND classeJudicial.ativo = true ");
			hql.append("AND classeJudicial.incidental = true ");
		}
		
		hql.append(montarHqlRestricoesCompetencia("c", isUsuarioInterno, tipoUsuarioInterno, idOrgaoJulgadorUsuario, idOrgaoJulgadorColegiadoUsuario, params));
		
		return executarQuery(hql, params);
	}

	/***
	 * Executa a query hql.
	 * 
	 * @param hql
	 * @param params
	 * @return List<Competencia> Lista de competências.
	 */
	@SuppressWarnings("unchecked")
	private List<Competencia> executarQuery(StringBuilder hql, Map<String, Object> params) {
		Query query = entityManager.createQuery(hql.toString());
		
		for(String key: params.keySet()){
			query.setParameter(key, params.get(key));
		}
		
		return query.getResultList();
	}
	
	/**
	 * Recupera as competências básicas.
	 * @param processoJudicial Processo judicial.
	 * @param jurisdicao Jurisdição.
	 * @return List<Competencia> Lista de competências.
	 */
	@SuppressWarnings("unchecked")
	public List<Competencia> getCompetenciasBasicas(ProcessoTrf processoJudicial, Jurisdicao jurisdicao, List<AssuntoTrf> assuntoTrfList) {
		List<Competencia> competenciasPorJurisdicao = getCompetenciasPorJurisdicao(jurisdicao, processoJudicial.getIsIncidente());
		
		if (isListasVazias(assuntoTrfList, competenciasPorJurisdicao)){
			return Collections.EMPTY_LIST;
		}
				
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT o.competencia ");
		sql.append("FROM CompetenciaClasseAssunto o ");
		sql.append("JOIN o.competencia.orgaoJulgadorCompetenciaList ojc ");
		sql.append("WHERE o.classeAplicacao.classeJudicial = :classeJudicial ");
		sql.append("AND o.assuntoTrf IN (:assuntos) ");
		sql.append("AND ojc.orgaoJulgador.ativo = true ");
		sql.append("AND CURRENT_DATE >= o.dataInicio AND (o.dataFim IS NULL OR CURRENT_DATE <= o.dataFim) ");
		sql.append("AND ((CURRENT_DATE >= ojc.dataInicio AND (ojc.dataFim IS NULL OR CURRENT_DATE <= ojc.dataFim)) ");
		if (processoJudicial.getIsIncidente()) {
			String idOrgaoJulgadorCompetencia = ParametroUtil.getParametro("tjrj:permitir:idOrgaoJulgadorCompetencia");
			if (idOrgaoJulgadorCompetencia != null && !idOrgaoJulgadorCompetencia.isEmpty()) {
				sql.append("OR ojc.orgaoJulgador.idOrgaoJulgador in (:idOrgaoJulgadorCompetencia) ");
			}
		}
		sql.append(") ");
		sql.append("AND ojc.orgaoJulgador.jurisdicao = :jurisdicao ");
		sql.append("AND o.classeAplicacao.aplicacaoClasse = :aplicacaoClasse ");
		sql.append("AND o.competencia.ativo = true ");
		sql.append("AND o.competencia IN (:competencias) ");
				
		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("classeJudicial", processoJudicial.getClasseJudicial());
		query.setParameter("assuntos", assuntoTrfList);
		query.setParameter("jurisdicao", jurisdicao);
		query.setParameter("aplicacaoClasse", ParametroUtil.instance().getAplicacaoSistema());
		query.setParameter("competencias", competenciasPorJurisdicao);
		if  (processoJudicial.getIsIncidente()) {
			String idOrgaoJulgadorCompetencia = ParametroUtil.getParametro("tjrj:permitir:idOrgaoJulgadorCompetencia");
			if (idOrgaoJulgadorCompetencia != null && !idOrgaoJulgadorCompetencia.isEmpty()) {
				query.setParameter("idOrgaoJulgadorCompetencia", CollectionUtilsPje.convertStringToIntegerList(idOrgaoJulgadorCompetencia));
			}
		}
		
		return query.getResultList();
	}

	/**
	 * Verifica se as listas estão nulas ou vazias.
	 * @param assuntoTrfList Lista de assuntos do processo.
	 * @param competenciasPorJurisdicao Lista de compentências por jurisdiçaõ.
	 * @return verdadeiro se as listas não estiverem nulas ou vazias.
	 */
	private boolean isListasVazias(List<AssuntoTrf> assuntoTrfList, List<Competencia> competenciasPorJurisdicao) {
		return (competenciasPorJurisdicao == null || competenciasPorJurisdicao.size() == 0 
				|| assuntoTrfList == null || assuntoTrfList.size() == 0);
	}
	
	/**
	 * Monta script sql que verifica as retrições das competencias que podem ser utilizadas na protocolação de um processo.
	 * @param prefixo Referencias das classes/tabelas para acessar os campos.
	 * @return query sql
	 */
	private String montarHqlRestricoesCompetencia(String prefixo, 
			boolean isUsuarioInterno, TipoVinculacaoUsuarioEnum tipoUsuarioInterno, 
			Integer idOrgaoJulgadorUsuario, Integer idOrgaoJulgadorColegiadoUsuario, Map<String, Object> params) {
		
		StringBuilder hql = new StringBuilder();
		
		if (isUsuarioInterno) {
			hql.append("AND ( ");
			hql.append(prefixo + ".restricaoProtocoloServidorInteno = 'TD' ");
			if(TipoVinculacaoUsuarioEnum.EGA.equals(tipoUsuarioInterno)) {
				hql.append(" OR (" + montarHqlApenasOrgaoJulgadorPodeProtocolar(prefixo) + ") ");
				
				if(!params.containsKey("idOrgaoJulgador")) {
					params.put("idOrgaoJulgador", idOrgaoJulgadorUsuario);
				}
			}
			if(idOrgaoJulgadorColegiadoUsuario != null && (TipoVinculacaoUsuarioEnum.EGA.equals(tipoUsuarioInterno)) || TipoVinculacaoUsuarioEnum.COL.equals(tipoUsuarioInterno)) {
				hql.append(" OR (" + montarHqlApenasOrgaoJulgadorColegiadoPodeProtocolar(prefixo) + ")");
				if(!params.containsKey("idOrgaoJulgadorColegiado")) {
					params.put("idOrgaoJulgadorColegiado", idOrgaoJulgadorColegiadoUsuario);
				}
			}
			hql.append(") ");
		} else {
			hql.append("AND " + prefixo + ".usuariosExternosPodemProtocolar = true ");
		}
		
		return hql.toString();
	}

	/**
	 * Script sql que verifica se a competência permite que 
	 * apenas os órgãos julgadores podem protocolar.
	 * @param prefixo Referencias das classes/tabelas para acessar os campos. 
	 * @return query sql
	 */
	private String montarHqlApenasOrgaoJulgadorPodeProtocolar(String prefixo) {
		StringBuilder hql = new StringBuilder();
		hql.append(prefixo + ".restricaoProtocoloServidorInteno = 'AOJ' ");
		hql.append("AND EXISTS (");
		hql.append("	SELECT 1 ");
		hql.append("	FROM OrgaoJulgadorCompetencia ojcp ");
		hql.append("	WHERE ojcp.competencia.idCompetencia = " + prefixo + ".idCompetencia ");
		hql.append("	AND ojcp.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ");
		hql.append(") ");
		return hql.toString();
	}
	
	/**
	 * Script hql que verifica se a competência permite que apenas os 
	 * órgãos julgadores ou órgão julgadores colegiados podem protocolar. 
	 * @param prefixo Referencias das classes/tabelas para acessar os campos.
	 * @return query sql
	 */
	private String montarHqlApenasOrgaoJulgadorColegiadoPodeProtocolar(String prefixo) {
		StringBuilder hql = new StringBuilder();
		hql.append(prefixo + ".restricaoProtocoloServidorInteno = 'AOJC' ");
		hql.append("AND EXISTS ( ");
		hql.append("	SELECT 1 ");
		hql.append("	FROM OrgaoJulgadorColegiadoCompetencia ojccp ");
		hql.append("	WHERE ojccp.competencia.idCompetencia = " + prefixo + ".idCompetencia ");
		hql.append("	AND ojccp.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = :idOrgaoJulgadorColegiado ");
		hql.append(") ");
		return hql.toString();
	}
	
	@SuppressWarnings("unchecked")
	public List<Competencia> getCompetenciasDimensaoPessoal(ProcessoTrf processoJudicial,
			List<Competencia> competencias,
			AssociacaoDimensaoPessoalEnum tipoAssociacao){
		
		List<Pessoa> autores = processoJudicial.getPessoaPoloAtivoList();
		List<Pessoa> reus = processoJudicial.getPessoaPoloPassivoList();
		List<TipoPessoa> tiposAutores = processoJudicial.getTipoPessoaPoloAtivoList();
		List<TipoPessoa> tiposReus = processoJudicial.getTipoPessoaPoloPassivoList();
		
		StringBuilder sql = new StringBuilder();
		sql.append(String.format("SELECT DISTINCT %s ", tipoAssociacao == AssociacaoDimensaoPessoalEnum.A ? "dp" : "c"));
		sql.append("FROM Competencia c " 
		+ "JOIN c.competenciaClasseAssuntoList cca "
		+ "JOIN c.orgaoJulgadorCompetenciaList ojc "
		+ "JOIN c.dimensaoPessoalList dp "
		+ "LEFT JOIN dp.pessoasAfetadasList dpp "
		+ "LEFT JOIN dp.tiposDePessoasAfetadosList dpt "
		+ "WHERE ojc.orgaoJulgador.jurisdicao = :jurisdicao " 
		+ "AND cca.classeAplicacao.classeJudicial = :classeJudicial "
		+ "AND cca.classeAplicacao.aplicacaoClasse = :aplicacaoClasse " 
		+ "AND cca.assuntoTrf IN (:assuntos) "
		+ "AND dp.ativo = true "
		+ "AND cca.competencia.ativo = true "
		+ "AND ojc.orgaoJulgador.ativo = true "
		+ "AND c IN (:competencias) "
		+ "AND ((now() >= ojc.dataInicio AND ojc.dataFim = null) or (now() between ojc.dataInicio and ojc.dataFim)) "
		+ "AND ojc.orgaoJulgador.ativo = true "
		+ "AND ("
		+ "		("
		+ "			(dpp.pessoa IN (:autores) AND dpp.tipoAssociacao = :tipoAssociacao AND (dpp.polo = 'A' OR dpp.polo = 'T'))"
		+ "			OR"
		+ "			(dpt.tipoPessoa IN (:tiposAutores) AND dpt.tipoAssociacao = :tipoAssociacao AND (dpt.polo='A' OR dpt.polo = 'T'))"
		+ "		)"
		+ "		OR"
		+ "		("
		+ "			(dpp.pessoa IN (:reus) AND dpp.tipoAssociacao = :tipoAssociacao AND (dpp.polo = 'P' OR dpp.polo = 'T'))"
		+ "			OR"
		+ "			(dpt.tipoPessoa IN (:tiposReus) AND dpt.tipoAssociacao = :tipoAssociacao AND (dpt.polo='P' OR dpt.polo = 'T'))"
		+ "		)"
		+ "     )");

		Query q = entityManager.createQuery(sql.toString());
		q.setParameter("jurisdicao", processoJudicial.getJurisdicao());
		q.setParameter("classeJudicial", processoJudicial.getClasseJudicial());
		q.setParameter("aplicacaoClasse", ParametroUtil.instance().getAplicacaoSistema());
		q.setParameter("assuntos", Util.isEmpty(processoJudicial.getAssuntoTrfList())?null:processoJudicial.getAssuntoTrfList());
		q.setParameter("autores", Util.isEmpty(autores)?null:autores);
		q.setParameter("reus", Util.isEmpty(reus)?null:reus);
		q.setParameter("tiposAutores", Util.isEmpty(tiposAutores)?null:tiposAutores);
		q.setParameter("tiposReus", Util.isEmpty(tiposReus)?null:tiposReus);
		q.setParameter("tipoAssociacao", tipoAssociacao);
		q.setParameter("competencias", Util.isEmpty(competencias)?null:competencias);
		
		if(tipoAssociacao == AssociacaoDimensaoPessoalEnum.A) {
			List<DimensaoPessoal> dimensoes = q.getResultList();
			if(dimensoes.size() == 0) {
				return competencias;
			}
			Set<Competencia> competenciasAptas = new HashSet<Competencia>();
			Set<Competencia> competenciasInaptas = new HashSet<Competencia>();
			int total = 0;
			int parcial = 0;
			for (DimensaoPessoal dimensaoPessoal : dimensoes) {
				for (DimensaoPessoalPessoa dpp : dimensaoPessoal.getPessoasAfetadasList()) {
					if(dpp.getTipoAssociacao() == AssociacaoDimensaoPessoalEnum.E) {
						continue;
					}
					total++;
					if(autores.contains(dpp.getPessoa()) && 
						(dpp.getPolo() == ProcessoParteParticipacaoEnum.A || dpp.getPolo() == ProcessoParteParticipacaoEnum.T)) {
						parcial++;
					}
					if(reus.contains(dpp.getPessoa()) && 
						(dpp.getPolo() == ProcessoParteParticipacaoEnum.P || dpp.getPolo() == ProcessoParteParticipacaoEnum.T)) {
						parcial++;
					}
				}
				if(total != parcial) {
					competenciasInaptas.addAll(dimensaoPessoal.getCompetencias());
					continue;
				}
				total = 0;
				parcial = 0;
				for (DimensaoPessoalTipoPessoa dpt : dimensaoPessoal.getTiposDePessoasAfetadosList()) {
					if(dpt.getTipoAssociacao() == AssociacaoDimensaoPessoalEnum.E) {
						continue;
					}
					total++;
					if(tiposAutores.contains(dpt.getTipoPessoa()) && 
						(dpt.getPolo() == ProcessoParteParticipacaoEnum.A || dpt.getPolo() == ProcessoParteParticipacaoEnum.T)) {
						parcial++;
					}
					if(tiposReus.contains(dpt.getTipoPessoa()) && 
							(dpt.getPolo() == ProcessoParteParticipacaoEnum.P || dpt.getPolo() == ProcessoParteParticipacaoEnum.T)) {
							parcial++;
					}
				}
				if(total == parcial) {
					competenciasAptas.addAll(dimensaoPessoal.getCompetencias());
				} else {
					competenciasInaptas.addAll(dimensaoPessoal.getCompetencias());
				}
				
			}
			if(competenciasAptas.size() > 0) {
				competencias.retainAll(competenciasAptas);
			} else {
				competencias.removeAll(competenciasInaptas);
			}
			return competencias;
		} else {
			return q.getResultList();
		}
	}
	
	/**
	 * Obtem a competencia atraves do processo.
	 * 
	 * @param procTrf
	 *            a se obter a competencia
	 * @return Competencia relacionada ao processo informado.
	 */
	@SuppressWarnings("unchecked")
	public Competencia getCompetenciaByProcessoTrf(ProcessoTrf procTrf) {
		Query q = entityManager.createQuery(GET_COMPETENCIA_BY_PROCESSO_TRF_QUERY);
		q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, procTrf);
		q.setParameter(QUERY_PARAMETER_CLASSE_JUDICIAL, procTrf.getClasseJudicial());
		q.setParameter(QUERY_PARAMETER_APLICACAO_CLASSE, ParametroUtil.instance().getAplicacaoSistema());
		List<Competencia> competencias = (List<Competencia>) q.getResultList();
		Competencia result = competencias.size() > 0 ? competencias.get(0) : null;
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Competencia> competenciaItemsByOrgaoJulgador(OrgaoJulgador oj) {
		Query q = getEntityManager().createQuery(COMPETENCIA_ITEMS_BY_ORGAO_JULGADOR_QUERY);
		q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, oj);
		List<Competencia> resultList = (List<Competencia>) q.getResultList();
		return resultList;
	}
	
	@SuppressWarnings("unchecked")
	public List<Competencia> getCompetencia(Integer idJurisdicao, Integer idClasseJudicial,
			List<AssuntoTrf> assuntos, Integer idCompetencia){
			String q = "SELECT DISTINCT cmp FROM Competencia AS cmp "
					+ "INNER JOIN cmp.competenciaClasseAssuntoList AS cca "
					+ "INNER JOIN cca.classeAplicacao AS cap "
					+ "WHERE cmp.ativo = true "
					+ "AND cap.classeJudicial.ativo = true "
					+ "AND cap.classeJudicial.idClasseJudicial = :idClasseJudicial "
					+ "AND cca.assuntoTrf IN (:assuntos) "
					+ "AND cca.dataInicio <= :agora "
					+ "AND (cca.dataFim >= :agora OR cca.dataFim IS NULL) "
					+ "AND ("
					+ "EXISTS "
					+ "(SELECT 1 FROM Competencia AS cmp2 "
					+ "	INNER JOIN cmp2.orgaoJulgadorCompetenciaList AS ojcp "
					+ "	INNER JOIN ojcp.orgaoJulgador AS oj "
					+ "	WHERE oj.ativo = true "
					+ "	AND cmp2.idCompetencia = cmp.idCompetencia"
					+ "	AND oj.jurisdicao.idJurisdicao = :idJurisdicao "
					+ "	AND ojcp.dataInicio <= :agora "
					+ "	AND (ojcp.dataFim >= :agora OR ojcp.dataFim IS NULL)) "
					+ "OR "
					+ "EXISTS "
					+ "(SELECT 1 FROM Competencia AS cmp3 "
					+ "	INNER JOIN cmp3.orgaoJulgadorColegiadoCompetenciaList AS ojcc "
					+ "	INNER JOIN ojcc.orgaoJulgadorColegiado AS ojc "
					+ "	WHERE cmp3.idCompetencia = cmp.idCompetencia"
					+ "	AND ojc.ativo = true "
					+ "	AND ojc.jurisdicao.idJurisdicao = :idJurisdicao "
					+ "	AND ojcc.dataInicio <= :agora "
					+ "	AND (ojcc.dataFim >= :agora OR ojcc.dataFim IS NULL))) ";
			
			if (idCompetencia != null && idCompetencia > 0) {
				q += " AND cmp.idCompetencia = :idCompetencia";
			}
			Query query = EntityUtil.getEntityManager().createQuery(q);
			query.setParameter("idClasseJudicial", idClasseJudicial);
			query.setParameter("idJurisdicao", idJurisdicao);
			query.setParameter("assuntos", Util.isEmpty(assuntos)? null : assuntos);
			query.setParameter("agora", new Date());
			if (idCompetencia != null && idCompetencia > 0) {
				query.setParameter("idCompetencia", idCompetencia);
			}

			return query.getResultList();
		}
		
    @SuppressWarnings({ "unchecked" })
	public List<String> obterNomeCompetenciasPorTaskInstance(
        InformacaoUsuarioSessao informacaoUsuario, String taskInstance) {
        Map<String, Object> parametros = new HashMap<String, Object>();

        StringBuilder sql = new StringBuilder();

        sql.append(" select distinct comp.ds_competencia from tb_processo_tarefa proctar ");
        sql.append(" inner join tb_cabecalho_processo cabpro on cabpro.id_processo_trf = proctar.id_processo_trf ");
        sql.append(" inner join tb_processo_trf proctrf on proctrf.id_processo_trf = proctar.id_processo_trf ");
        sql.append(" inner join tb_competencia comp on proctrf.id_competencia = comp.id_competencia and comp.in_ativo = :ativo ");
        sql.append(" where proctar.nm_tarefa = :taskInstance ");
        sql.append(" and exists (  ");
        sql.append("  select 1 from tb_proc_localizacao_ibpm tl ");
        sql.append("  where tl.id_processo = proctar.id_processo_trf ");
        sql.append("  and tl.id_task_jbpm = proctar.id_task ");
        sql.append("  and tl.id_localizacao = :idLocalizacaoModelo ");
        sql.append("  and tl.id_papel = :idPapel ");
        sql.append(" ) ");

        List<Integer> idsLocalizacoesFisicas = informacaoUsuario.getIdsLocalizacoesFisicasFilhas();
        Integer idOrgaoJulgadorColegiado = informacaoUsuario.getIdOrgaoJulgadorColegiado();
        boolean isServidorExclusivoOJC = informacaoUsuario.isServidorExclusivoOJC();
        Integer idOrgaoJulgadorCargo = informacaoUsuario.getIdOrgaoJulgadorCargo();
        Boolean visualizaSigiloso = informacaoUsuario.getVisualizaSigiloso();

        if (!isServidorExclusivoOJC && idsLocalizacoesFisicas != null) {
            sql.append("and proctar.id_localizacao IN (:idsLocalizacoesFisicas) ");
            parametros.put("idsLocalizacoesFisicas", idsLocalizacoesFisicas);
        }

        if (idOrgaoJulgadorColegiado != null) {
            sql.append("and proctar.id_orgao_julgador_colegiado = :idOrgaoJulgadorColegiado ");
            parametros.put("idOrgaoJulgadorColegiado", idOrgaoJulgadorColegiado);
        }

        if (idOrgaoJulgadorCargo != null) {
            sql.append("and proctar.id_orgao_julgador_cargo = :idOrgaoJulgadorCargo ");
            parametros.put("idOrgaoJulgadorCargo", idOrgaoJulgadorCargo);
        }

        if (visualizaSigiloso != null && !visualizaSigiloso) {
            sql.append("and (cabpro.in_segredo_justica = false or exists ");
            sql.append("(select 1 from tb_proc_visibilida_segredo vis ");
            sql.append("where vis.id_pessoa = :idUsuario and vis.id_processo_trf = proctar.id_processo_trf)) ");
            parametros.put("idUsuario", informacaoUsuario.getIdUsuario());
        }
        
        sql.append(" order by comp.ds_competencia ");

        parametros.put("taskInstance", taskInstance);
        parametros.put("ativo", true);
        parametros.put("idPapel", informacaoUsuario.getIdPapel());
        parametros.put("idLocalizacaoModelo", informacaoUsuario.getIdLocalizacaoModelo());

        Query query = getEntityManager().createNativeQuery(sql.toString());

        setQueryParameters(query, parametros);

        return query.getResultList();
    }
	
	public boolean isClasseAtendimentoPlantao(Competencia competencia,ClasseJudicial classeJudicial) {
		StringBuilder sql = new StringBuilder();
        sql.append("select exists (select * from tb_competencia_cl_atend_plantao where id_classe_judicial= :idClasseJudicial and id_competencia = :idCompetencia)");
        Map<String, Object> parametros = new HashMap<String, Object>();
        
        parametros.put("idClasseJudicial", classeJudicial.getIdClasseJudicial());
        parametros.put("idCompetencia", competencia.getIdCompetencia());
        
		Query query = getEntityManager().createNativeQuery(sql.toString());
		setQueryParameters(query, parametros);

        return EntityUtil.getSingleResult(query);
	}

}
