/**
 * 
 */
package br.jus.cnj.pje.business.dao;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.OrgaoJulgadorColegiadoDTO;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * Componente de acesso a dados da entidade {@link OrgaoJulgadorColegiadoCargo}.
 */
@Name("orgaoJulgadorColegiadoDAO")
public class OrgaoJulgadorColegiadoDAO extends BaseDAO<OrgaoJulgadorColegiado> {
	
	@Override
	public Integer getId(OrgaoJulgadorColegiado e) {
		return e.getIdOrgaoJulgadorColegiado();
	}
	
	/**
	 * Importado de jt.OrgaoJulgadorColegiado.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoItems(){
		String query = "SELECT o FROM OrgaoJulgadorColegiado AS o " +
				" WHERE o.ativo = true " +
				" ORDER BY CASE WHEN o.orgaoJulgadorColegiado >= 'A' THEN upper(to_ascii(o.orgaoJulgadorColegiado)) ELSE fn_to_number(o.orgaoJulgadorColegiado) END, upper(to_ascii(o.orgaoJulgadorColegiado))"; 
				
		Query q = getEntityManager().createQuery(query);
		return q.getResultList();
	}
	
	/**
	 * Recupera a lista de órgãos colegiados que têm entre seus gabinetes o órgão julgador dado
	 * e que tenha a competência informada no momento da chamaeda.
	 * 
	 * @param competencia a competência que restringirá a busca
	 * @param orgaoJulgador o gabinete que deve estar vinculado ao colegiado
	 * @return a lista de colegiados a que pertente o gabinete e que têm a competência informada.
	 */
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiado> getColegiadosCompetentes(Competencia competencia, OrgaoJulgador orgaoJulgador){
		String query = "SELECT ojc FROM OrgaoJulgadorColegiadoCompetencia AS occ " +
				"	JOIN occ.orgaoJulgadorColegiado ojc " +
				"	JOIN ojc.orgaoJulgadorColegiadoOrgaoJulgadorList gabs " +
				"	WHERE gabs.orgaoJulgador = :orgaoJulgador " +
				"		AND gabs.orgaoJulgador.ativo = true " +
				"		AND (gabs.dataInicial IS NULL OR gabs.dataInicial <= :dataAtual) " +
				"		AND (gabs.dataFinal IS NULL OR gabs.dataFinal >= :dataAtual) " +
				"		AND ojc.ativo = true " +
				"		AND occ.competencia = :competencia " +
				"		AND (occ.dataInicio IS NULL OR occ.dataInicio <= :dataAtual) " +
				"		AND (occ.dataFim IS NULL OR occ.dataFim >= :dataAtual) ";
		Query q = entityManager.createQuery(query);
		q.setParameter("dataAtual", new Date());
		q.setParameter("orgaoJulgador", orgaoJulgador);
		q.setParameter("competencia", competencia);
		return q.getResultList();
	}
	
	/**
	 * Recupera uma lista de {@link OrgaoJulgadorColegiado} ativos, de competência ativa e vinculados à uma Jurisdicao.
	 * 
	 * @param jurisdicao A jurisdição que restringirá a busca.
	 * @return Lista de {@link OrgaoJulgadorColegiado} ativos, de competência ativa e vinculados à uma Jurisdicao.
	 */
	public List<OrgaoJulgadorColegiado> getColegiadosByJurisdicao(Jurisdicao jurisdicao) {
		return this.getColegiadosAtivos(jurisdicao, null);
	}

	public List<OrgaoJulgadorColegiado> getColegiadosByCompetencia(Competencia competencia) {
		return this.getColegiadosAtivos(null, competencia);
	}
	
	public List<OrgaoJulgadorColegiado> getColegiadosByLocalizacao(Localizacao localizacao) {
		return this.getColegiadosAtivos(null, null, localizacao, true);
	}

	public List<OrgaoJulgadorColegiado> getColegiadosByLocalizacaoExata(Localizacao localizacao) {
		return this.getColegiadosAtivos(null, null, localizacao, false);
	}

	public List<OrgaoJulgadorColegiado> getColegiadosAtivos(Jurisdicao jurisdicao, Competencia competencia) {
		return this.getColegiadosAtivos(jurisdicao, competencia, null, true);
	}
	
	/**
	 * Busca os OJCs ativos de acordo com a parametrização, se for passado apenas localizacao, a pesquisa ignora a verificação de ter pelo menos 1 competência ativa e 1 OJ ativo
	 * 
	 * @param jurisdicao
	 * @param competencia
	 * @param localizacao
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiado> getColegiadosAtivos(Jurisdicao jurisdicao, Competencia competencia, Localizacao localizacao, boolean isBuscarFilhos) {
		List<OrgaoJulgadorColegiado> resultado = new ArrayList<OrgaoJulgadorColegiado>(0);
		if (jurisdicao != null || competencia != null || localizacao != null) {
			StringBuilder query = new StringBuilder();
			if(jurisdicao == null && competencia == null) {
				query.append("SELECT distinct ojc ")
					.append("FROM OrgaoJulgadorColegiado ojc ");
				
			}else {
				query.append("SELECT distinct ojc ")
					.append("FROM OrgaoJulgadorColegiadoCompetencia AS occ ")
					.append("JOIN occ.orgaoJulgadorColegiado ojc ")
					.append("JOIN ojc.orgaoJulgadorColegiadoOrgaoJulgadorList gabs ");
				
			}
			if(localizacao != null) {
				query.append(" JOIN ojc.localizacao loc ");
			}
			query.append(" WHERE ojc.ativo = true ");
			if(jurisdicao != null || competencia != null) {
				query.append(" AND gabs.orgaoJulgador.ativo = true ")
				.append("AND gabs.dataInicial <= :dataAtualInicio AND (gabs.dataFinal IS NULL OR gabs.dataFinal >= :dataAtualFim) ")
				.append("AND occ.dataInicio <= :dataAtualInicio AND (occ.dataFim IS NULL OR occ.dataFim >= :dataAtualFim) ");
			}
			
			if (jurisdicao != null) {
				query.append("AND ojc.jurisdicao = :jurisdicao ");
			}
			if (competencia != null) {
				query.append("AND occ.competencia = :competencia ");
			}
			if (localizacao != null) {
				query.append(" AND (loc.localizacao.idLocalizacao = :idLocalizacao ");
				if(localizacao.getFaixaInferior() != null && isBuscarFilhos) {
					query.append(" OR ( ");
					query.append(" loc.faixaInferior IS NOT NULL ");
					query.append(" AND loc.faixaInferior >= :faixaInferiorLocalizacao ");
					query.append(" AND loc.faixaSuperior IS NOT NULL ");
					query.append(" AND loc.faixaSuperior <= :faixaSuperiorLocalizacao ");
					query.append(" ) ");
				}
				query.append(" ) ");
			}
			query.append(" ORDER BY ojc.orgaoJulgadorColegiado ");

			Query q = entityManager.createQuery(query.toString());

			if (jurisdicao != null) {
				q.setParameter("jurisdicao", jurisdicao);
			}
			if (competencia != null) {
				q.setParameter("competencia", competencia);
			}
			if(jurisdicao != null || competencia != null) {
				q.setParameter("dataAtualInicio", DateUtil.getBeginningOfDay(new Date()));
				q.setParameter("dataAtualFim", DateUtil.getEndOfDay(new Date()));				
			}
			if (localizacao != null) {
				q.setParameter("idLocalizacao", localizacao.getIdLocalizacao());
				if(localizacao.getFaixaInferior() != null && isBuscarFilhos) {
					q.setParameter("faixaInferiorLocalizacao", localizacao.getFaixaInferior());
					q.setParameter("faixaSuperiorLocalizacao", localizacao.getFaixaSuperior());
				}
			}
			resultado = q.getResultList();
		}
		return resultado;
	}
	
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiadoDTO> findAllDTO(){
		StringBuilder query = new StringBuilder();
		query.append("SELECT new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.OrgaoJulgadorColegiadoDTO(ojc.idOrgaoJulgadorColegiado, ojc.orgaoJulgadorColegiado) ");
		query.append("FROM OrgaoJulgadorColegiado ojc ");
		query.append("WHERE ojc.ativo = true ");
		query.append("ORDER BY ojc.orgaoJulgadorColegiado ");
		Query q = entityManager.createQuery(query.toString());
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiado> getColegiadosByClasseAssunto(ClasseJudicial classeJudicial, List<AssuntoTrf> assuntoTrfList){
		List<OrgaoJulgadorColegiado> resultado = new ArrayList<OrgaoJulgadorColegiado>(0);

		if (classeJudicial != null || (assuntoTrfList != null && !assuntoTrfList.isEmpty())) {
			StringBuilder sql = new StringBuilder("SELECT DISTINCT ojc ")
				.append("FROM OrgaoJulgadorColegiado ojc  ")					
				.append("JOIN ojc.orgaoJulgadorColegiadoOrgaoJulgadorList gabs ")
				.append("JOIN ojc.orgaoJulgadorColegiadoCompetenciaList ojcl ")
				.append("JOIN ojcl.competencia.competenciaClasseAssuntoList cca ")
				.append("WHERE ojc.ativo = true AND gabs.orgaoJulgador.ativo = true ")
				.append("AND gabs.dataInicial <= :dataAtualInicio AND (gabs.dataFinal is null OR gabs.dataFinal >= :dataAtualFim) ")
				.append("AND ojcl.dataInicio <= :dataAtualInicio AND (ojcl.dataFim is null OR ojcl.dataFim >= :dataAtualFim) ");

			if (classeJudicial != null) {
				sql.append("AND cca.classeAplicacao.classeJudicial = :classeJudicial ");
			}
			if (assuntoTrfList != null && !assuntoTrfList.isEmpty()) {
				sql.append("AND cca.assuntoTrf in (:assuntos) ");
			}
			sql.append("ORDER BY ojc.orgaoJulgadorColegiado ");
			
			Query query = entityManager.createQuery(sql.toString());
			
			if (classeJudicial != null) {
				query.setParameter("classeJudicial", classeJudicial);
			}
			if (assuntoTrfList != null && !assuntoTrfList.isEmpty()) {
				query.setParameter("assuntos", assuntoTrfList);
			}
			query.setParameter("dataAtualInicio", DateUtil.getBeginningOfDay(new Date()));
			query.setParameter("dataAtualFim", DateUtil.getEndOfDay(new Date()));

			resultado = query.getResultList();
		}
		return resultado;
	}
	
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiadoOrgaoJulgador> obterComposicaoAtiva(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		StringBuilder jpql = new StringBuilder();

		jpql.append("select ojco from OrgaoJulgadorColegiadoOrgaoJulgador ojco join fetch ojco.orgaoJulgadorColegiado ojc join fetch ojco.orgaoJulgador oj ")
			.append("where ojc = :orgaoJulgadorColegiado and oj.ativo = true and ojco.dataInicial <= :dataAtual and ( ojco.dataFinal is null or ojco.dataFinal >= :dataAtual ) ")
			.append("order by ojco.ordem, ojco.orgaoJulgador.orgaoJulgador ");

		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("dataAtual", new Date());
		query.setParameter("orgaoJulgadorColegiado", orgaoJulgadorColegiado);

		return query.getResultList();
	}
}
