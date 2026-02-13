package br.com.jt.pje.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.jt.pje.query.OrgaoJulgadorColegiadoOrgaoJulgadorQuery;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;

@Name(OrgaoJulgadorColegiadoOrgaoJulgadorDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class OrgaoJulgadorColegiadoOrgaoJulgadorDAO extends BaseDAO<OrgaoJulgadorColegiadoOrgaoJulgador> implements OrgaoJulgadorColegiadoOrgaoJulgadorQuery, Serializable{

	private static final long serialVersionUID = 9187966712676466874L;
	
	public static final String NAME = "orgaoJulgadorColegiadoOrgaoJulgadorDAO";


	@Override
	public Integer getId(OrgaoJulgadorColegiadoOrgaoJulgador e) {
		return e.getIdOrgaoJulgadorColegiadoOrgaoJulgador();
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaoJulgadorSemComposicaoSessaoByColegiadoSessaoItems(SessaoJT sessao){
		Query q = getEntityManager().createQuery(ORGAO_JULGADOR_SEM_COMPOSICAO_BY_COLEGIADO_SESSAO_QUERY);
		q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
		q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO, sessao.getOrgaoJulgadorColegiado());
		List<OrgaoJulgador> resultList = q.getResultList();
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiadoOrgaoJulgador> recuperarOrgaosJulgadoresDoColegiadoNaoUtilizadosComoRevisor(OrgaoJulgadorColegiado orgaoJulgadorColegiado, OrgaoJulgador orgaoJulgadorRelator) {

		StringBuilder jpql = new StringBuilder();
		
		jpql.append(" select ojco from OrgaoJulgadorColegiadoOrgaoJulgador ojco ")
			.append("  where ojco.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ")
			.append("    and ojco.dataInicial <= :dataAtual and ojco.orgaoJulgador.ativo = true ")
			.append("    and ( ojco.dataFinal is null or ojco.dataFinal >= :dataAtual ) ")
			.append("    and not exists ( ")
			.append("		             select ojco2 from OrgaoJulgadorColegiadoOrgaoJulgador ojco2 ")
			.append("				      where ojco2.orgaoJulgadorColegiado = ojco.orgaoJulgadorColegiado ")
			.append("						and ojco2.orgaoJulgadorRevisor = ojco ")
			.append(" 					) ")
			;
		
		if (orgaoJulgadorRelator != null) {
			jpql.append(" and ojco.orgaoJulgador != :orgaoJulgador" );
		}
		
		jpql.append("  order by ojco.orgaoJulgador.orgaoJulgador ");
				
		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("dataAtual", new Date());
		query.setParameter("orgaoJulgadorColegiado", orgaoJulgadorColegiado);
		
		if (orgaoJulgadorRelator != null) {
			query.setParameter("orgaoJulgador", orgaoJulgadorRelator);
		}
		
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public OrgaoJulgadorColegiadoOrgaoJulgador recuperarPorOrgaoJulgadorColegiadoEhOrgaoJulgador(OrgaoJulgadorColegiado orgaoJulgadorColegiado, OrgaoJulgador orgaoJulgador) {

		StringBuilder jpql = new StringBuilder();
		
		jpql.append(" select ojco from OrgaoJulgadorColegiadoOrgaoJulgador ojco ")
			.append("  where ojco.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ")			
			.append(" 	 and ojco.orgaoJulgador = :orgaoJulgador" )
			.append("  order by ojco.dataInicial desc ");
				
		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("orgaoJulgadorColegiado", orgaoJulgadorColegiado);
		query.setParameter("orgaoJulgador", orgaoJulgador);
		query.setMaxResults(1);
		
		List<OrgaoJulgadorColegiadoOrgaoJulgador> resultado = query.getResultList();
		
		if (!resultado.isEmpty()) {
			return resultado.get(0);
		} else {
			return null;
		}	
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiadoOrgaoJulgador> recuperarAtivosPor(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {

		StringBuilder jpql = new StringBuilder();
		
		jpql.append(" select ojco from OrgaoJulgadorColegiadoOrgaoJulgador ojco ")
			.append("  where ojco.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ")
			.append("    and ojco.dataInicial <= :dataAtual ")
			.append("    and ( ojco.dataFinal is null or ojco.dataFinal >= :dataAtual ) ")			
			.append("  order by ojco.orgaoJulgador.orgaoJulgador ");
				
		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("dataAtual", new Date());
		query.setParameter("orgaoJulgadorColegiado", orgaoJulgadorColegiado);
		
		return query.getResultList();		
	}
	
	/**
	 * Método responsável por recuperar a lista de Órgãos Colegiados ativos aos quais um Órgão Julgador pertença e que
	 * estejam vigentes.
	 * 
	 * @param	orgaoJulgador
	 * @return	retorna uma lista com os Órgãos Julgadores Colegiados Ativos, conforme o Órgão Julgador.
	 */
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiado> recuperaOrgaosColegiadosAtivosPorOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		
		StringBuilder jpql = new StringBuilder();
		
		jpql.append(" SELECT o.orgaoJulgadorColegiado ")
			.append("	FROM OrgaoJulgadorColegiadoOrgaoJulgador o ")
			.append("  WHERE o.orgaoJulgador = :orgaoJulgador ")
			.append("    AND o.orgaoJulgadorColegiado.ativo IS TRUE ")
			.append("    AND o.dataInicial <= :dataAtual ")
			.append("    AND ( o.dataFinal IS NULL OR o.dataFinal >= :dataAtual ) ")			
			.append("  ORDER BY o.orgaoJulgadorColegiado.orgaoJulgadorColegiado ");
				
		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("dataAtual", new Date());
		query.setParameter("orgaoJulgador", orgaoJulgador);
		
		return query.getResultList();
		
	}
	
	public Long countOrgaoJulgadorPorOrgaoJulgadorColegiado(OrgaoJulgadorColegiado ojc){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(o) FROM OrgaoJulgadorColegiadoOrgaoJulgador o ")
			.append("WHERE o.orgaoJulgadorColegiado = :ojc ")
			.append("    AND o.orgaoJulgador.ativo IS TRUE ")
			.append("    AND o.dataInicial <= :dataAtual ")
			.append("    AND ( o.dataFinal IS NULL OR o.dataFinal >= :dataAtual ) ");
		
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("dataAtual", new Date());
		query.setParameter("ojc", ojc);
		
		Long retorno = (Long) query.getSingleResult();
		
		if(retorno == null){
			retorno =  0L;
		}
		
		return retorno;
	}
	
	public void removeRevisoresOJsColegiado(OrgaoJulgadorColegiado ojc) {
		StringBuilder hql = new StringBuilder("UPDATE OrgaoJulgadorColegiadoOrgaoJulgador ");
		hql.append(" SET orgaoJulgadorRevisor = NULL ");
		hql.append(" WHERE orgaoJulgadorColegiado = :ojc ");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("ojc", ojc);
		
		if(query.executeUpdate() > 0){
			this.flush();
		}
	}
}