/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.VinculacaoDependenciaEleitoral;

/**
 * Componente de acesso a dados da entidade {@link VinculacaoDependenciaEleitoral}.
 * 
 * @author eduardo.pereira
 *
 */
@Name("vinculacaoDependenciaEleitoralDAO")
public class VinculacaoDependenciaEleitoralDAO extends BaseDAO<VinculacaoDependenciaEleitoral> {
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(VinculacaoDependenciaEleitoral e) {
		return e.getId();
	}
	
	/**
	 * Recupera uma vinculação única de dependência eleitoral para um dado cargo, eleição e município.
	 * 
	 * @param eleicao a eleição vinculada
	 * @param municipio o município de origem
	 * @return a vinculação eleitoral, se existente para esse cargo, ou null, caso não exista uma vinculação em tais condições
	 */
	public VinculacaoDependenciaEleitoral recuperaVinculacaoDependenciaEleitoral(Eleicao eleicao, Municipio municipio, ProcessoTrf processo){
		return recuperaVinculacaoDependenciaEleitoral(eleicao, null, municipio, processo);
	}
	public VinculacaoDependenciaEleitoral recuperaVinculacaoDependenciaEleitoralPorEleicaoMunicipio(Eleicao eleicao, Municipio municipio){
		return recuperaVinculacaoDependenciaEleitoralPorEleicao(eleicao, null, municipio);
	}

	/**
	 * Recupera uma vinculação única de dependência eleitoral para um dado cargo, eleição e estado.
	 * 
	 * @param eleicao a eleição vinculada
	 * @param estado o estado de origem
	 * @return a vinculação eleitoral, se existente para esse cargo, ou null, caso não exista uma vinculação em tais condições
	 */
 
	public VinculacaoDependenciaEleitoral recuperaVinculacaoDependenciaEleitoral(Eleicao eleicao, Estado estado, ProcessoTrf processo){
		return recuperaVinculacaoDependenciaEleitoral(eleicao, estado, null, processo);
	}
	
	public VinculacaoDependenciaEleitoral recuperaVinculacaoDependenciaEleitoralPorEleicaoEstado(Eleicao eleicao, Estado estado){
		return recuperaVinculacaoDependenciaEleitoralPorEleicao(eleicao, estado, null);
 
	}

	/**
	 * Recupera uma vinculação única de dependência eleitoral para um dado cargo, eleição e, alternativamente, um estado ou município.
	 * 
	 * @param eleicao a eleição vinculada
	 * @param municipio o município de origem, ou null, caso se pretenda recuperar de estado
	 * @param estado o estado de origem, ou null, caso se pretenda recuperar de município
	 * @return a vinculação eleitoral, se existente para esse cargo, ou null, caso não exista uma vinculação em tais condições
	 */
	private VinculacaoDependenciaEleitoral recuperaVinculacaoDependenciaEleitoral(Eleicao eleicao, Estado estado, Municipio municipio, ProcessoTrf processo ){
		StringBuilder query = new StringBuilder("SELECT v FROM VinculacaoDependenciaEleitoral AS v  ");
		
		query.append(" INNER JOIN v.complementosProcessoJE AS comp ");
		query.append(" INNER JOIN comp.processoTrf AS p ");
		query.append(" WHERE v.eleicao = :eleicao ");
		query.append(" and p = :processoTrf");
		
		Query q = null;
		if(estado == null){
			query.append("	AND v.estado IS NULL " +
					"	AND v.municipio = :municipio");
			q = entityManager.createQuery(query.toString());
			q.setParameter("municipio", municipio);
		}else{
			query.append("	AND v.municipio IS NULL " +
					"	AND v.estado = :estado");
			q = entityManager.createQuery(query.toString());
			q.setParameter("estado", estado);
		}
		q.setParameter("eleicao", eleicao);
		q.setParameter("processoTrf", processo);
		try{
			return (VinculacaoDependenciaEleitoral) q.getSingleResult();
		}catch(NoResultException e){
			return null;
		}
	}
	
	private VinculacaoDependenciaEleitoral recuperaVinculacaoDependenciaEleitoralPorEleicao(Eleicao eleicao, Estado estado, Municipio municipio){
		StringBuilder query = new StringBuilder("SELECT v FROM VinculacaoDependenciaEleitoral AS v " +
				"	WHERE v.eleicao = :eleicao ");
		Query q = null;
		if(estado == null){
			query.append("	AND v.estado IS NULL " +
					"	AND v.municipio = :municipio");
			q = entityManager.createQuery(query.toString());
			q.setParameter("municipio", municipio);
		}else{
			query.append("	AND v.municipio IS NULL " +
					"	AND v.estado = :estado");
			q = entityManager.createQuery(query.toString());
			q.setParameter("estado", estado);
		}
		q.setParameter("eleicao", eleicao);
		try{
			return (VinculacaoDependenciaEleitoral) q.getSingleResult();
		}catch(NoResultException e){
			return null;
		}
	}
	
	
	/**
	 * Recupera os processos que estão vinculados a um determinado Vinculo de Dependencia Eleitoral (processos vinculados a uma cadeia especifica).
	 * @param vinculacaoDependenciaEleitoral
	 * @return List<ProcessoTrf> processos pertencentes a uma cadeia.
	 * @throws PJeBusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> recuperarProcessosAssociadosVinculacaoDependencia(VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral) {
		if (vinculacaoDependenciaEleitoral == null)
		{
			return null;
		}
		StringBuilder query = new StringBuilder("SELECT p FROM VinculacaoDependenciaEleitoral AS vinc ");
		query.append(" INNER JOIN vinc.complementosProcessoJE AS comp ");
		query.append(" INNER JOIN comp.processoTrf AS p ");
		query.append(" WHERE vinc = :vinculo");
		query.append(" order by comp.processoTrf asc ");
		
		Query q =  entityManager.createQuery(query.toString());
		q.setParameter("vinculo", vinculacaoDependenciaEleitoral);
 		
		try{
			return q.getResultList();
		}catch(NoResultException e){
			return null;
		}
	}
}
