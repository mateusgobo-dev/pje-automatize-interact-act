/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;

@Name("orgaoJulgadorCargoDAO")
public class OrgaoJulgadorCargoDAO extends BaseDAO<OrgaoJulgadorCargo> {

	@Override
	public Integer getId(OrgaoJulgadorCargo ojc) {
		return ojc.getIdOrgaoJulgadorCargo();
	}

	public OrgaoJulgadorCargo getById(Integer id) {
		String hql = "select o from OrgaoJulgadorCargo o where o.idOrgaoJulgadorCargo = :id";
		Query query = EntityUtil.getEntityManager().createQuery(hql);
		query.setParameter("id", id);
		OrgaoJulgadorCargo retorno = (OrgaoJulgadorCargo)EntityUtil.getSingleResult(query);
		return retorno;
	}

	/**
	 * Recupera o menor valor do acumulador de distribuicao de uma lista da OrgaoJulgadorCargo
	 * @param cargosCompetentes
	 * @return Double menor valor do acumulador de distribuicao de uma lista da OrgaoJulgadorCargo
	 */
	public Double recuperaPisoDistribuicao(List<OrgaoJulgadorCargo> cargosCompetentes) {
		String query = "SELECT MIN(cargo.acumuladorDistribuicao) FROM OrgaoJulgadorCargo AS cargo " +
				"	WHERE cargo IN (:cargos) ";
		Query q = entityManager.createQuery(query);
		q.setParameter("cargos", cargosCompetentes);
		q.setMaxResults(1);
		return (Double) EntityUtil.getSingleResult(q);
	}

	/**
	 * Recupera, de uma lista de cargos judiciais candidatos, aqueles que têm um acumulador de 
	 * distribuição maior ou igual a um teto.
	 * 
	 * @param cargosCandidatos os cargos candidatos
	 * @param teto o valor a partir do qual o cargo será retornado na lista 
	 * @return os cargos que têm o acumulador com valor maior ou igual ao teto
	 * 
	 * @see <a href="http://www.cnj.jus.br/jira/secure/attachment/15980/distribuicao_1_3_0.pdf">R-012</a>
	 */
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorCargo> recuperaSuperamTeto(Double teto, List<OrgaoJulgadorCargo> cargosCandidatos) {
		String query = "SELECT cargo FROM OrgaoJulgadorCargo AS cargo " +
				"	WHERE cargo IN (:cargos) " +
				"		AND cargo.acumuladorDistribuicao >= :teto ";
		Query q = entityManager.createQuery(query);
		q.setParameter("teto", teto);
		q.setParameter("cargos", cargosCandidatos);
		return q.getResultList();
	}

	public void ajustarAcumuladores(OrgaoJulgadorCargo cargo, Double deltaAcumuladorProcesso, Double deltaAcumuladorDistribuicao) {
		StringBuilder jpql = new StringBuilder("UPDATE tb_orgao_julgador_cargo SET ")
				.append("nr_acumulador_processo = nr_acumulador_processo + ").append(deltaAcumuladorProcesso)
				.append(", nr_acumulador_distribuicao = nr_acumulador_distribuicao + ").append(deltaAcumuladorDistribuicao)
				.append(" WHERE id_orgao_julgador_cargo = ").append(cargo.getIdOrgaoJulgadorCargo());
		
		entityManager.createNativeQuery(jpql.toString()).executeUpdate();
	}
	
	/**
	 * Método responsável por recuperar o {@link OrgaoJulgadorCargo} (titular ou auxiliar) do {@OrgaoJulgador} especificado.
	 * 
	 * @param orgaoJulgador {@OrgaoJulgador}.
	 * @param isAuxiliar Variável que indica qual o tipo de {@link OrgaoJulgadorCargo} será pesquisado. 
	 * 		Caso verdadeiro. a pesquisa será feita pelo {@link OrgaoJulgadorCargo} auxiliar.
	 * 		Caso falso, a pesquisa será feita pelo {@link OrgaoJulgadorCargo} titular.  
	 * @return O {@link OrgaoJulgadorCargo} (titular ou auxiliar) do {@OrgaoJulgador} especificado.
	 */
	public OrgaoJulgadorCargo getOrgaoJulgadorCargo(OrgaoJulgador orgaoJulgador, boolean isAuxiliar) {		
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorCargo o ");
		sb.append("where o.orgaoJulgador = :orgaoJulgador ");
		sb.append("and o.auxiliar = :auxiliar ");
		sb.append("and o.ativo = true ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgador", orgaoJulgador);
		q.setParameter("auxiliar", isAuxiliar);
		return (OrgaoJulgadorCargo) EntityUtil.getSingleResult(q);
	}
	
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorCargo> obterCargosJudiciais(OrgaoJulgador orgaoJulgador, Boolean auxiliar, Boolean recebeDistribuicao){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorCargo o ");
		sb.append("where o.orgaoJulgador = :orgaoJulgador ");
		
		
		if (auxiliar != null){
			sb.append("and o.auxiliar = :auxiliar ");
		}
		
		if (recebeDistribuicao != null){
			sb.append("and o.recebeDistribuicao = :recebeDistribuicao ");  
		}
		
		sb.append("and o.ativo = true ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		
		q.setParameter("orgaoJulgador", orgaoJulgador);
		
		if (auxiliar != null){
			q.setParameter("auxiliar", auxiliar);
		}	
		
		if (recebeDistribuicao != null){
			q.setParameter("recebeDistribuicao", recebeDistribuicao);
		}
		
		return (List<OrgaoJulgadorCargo>)q.getResultList();
		
	}
}
