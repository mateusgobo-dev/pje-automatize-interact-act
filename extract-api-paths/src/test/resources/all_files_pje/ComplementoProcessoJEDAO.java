package br.jus.cnj.pje.business.dao;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.je.entidades.ComplementoProcessoJE;
import br.jus.pje.nucleo.entidades.VinculacaoDependenciaEleitoral;

@Name("complementoProcessoJEDAO")
public class ComplementoProcessoJEDAO extends BaseDAO<ComplementoProcessoJE> {
	
	@Override
	public Integer getId(ComplementoProcessoJE e) {
		return e.getId();
	}
	
	/**
	 * Metodo que verifica se existe um processo Paradigma para a vinculacao passada por parametro
	 * 
	 * @param vinculacao
	 * @return True se existir um processo paradigma, false se nao existir
	 */
	public boolean isParadigmaExistente(VinculacaoDependenciaEleitoral vinculacao) {
		StringBuilder sbQuery = new StringBuilder("SELECT COUNT(*) FROM ComplementoProcessoJE AS comp ");
		sbQuery.append(" WHERE comp.vinculacaoDependenciaEleitoral = :vinculo AND comp.paradigma = true");
		
		Query query =  entityManager.createQuery(sbQuery.toString());
		query.setParameter("vinculo", vinculacao);
 		
		Number count = (Number) query.getSingleResult();
		
		return count.intValue() > 0;
	}
	
	/**
	 * Metodo que recupera o primeiro complemento processual de uma cadeia.
	 * 
	 * @param vinculacao
	 * @return ComplementoJE
	 */
	public ComplementoProcessoJE recuperarComplementoParadigma(VinculacaoDependenciaEleitoral vinculacao) {
		StringBuilder sbQuery = new StringBuilder("SELECT comp FROM ComplementoProcessoJE AS comp ");
		sbQuery.append(" WHERE comp.vinculacaoDependenciaEleitoral = :vinculo");
		sbQuery.append(" ORDER BY comp.id ASC");
		
		Query query =  entityManager.createQuery(sbQuery.toString());
		query.setParameter("vinculo", vinculacao);
 		query.setMaxResults(1);
		
 		ComplementoProcessoJE retorno = null;
 		try{
			retorno = (ComplementoProcessoJE) query.getSingleResult();
 		} catch (NoResultException e){
 			logger.warn("Complemento paradgima nao encontrado.");
 		}
 		return retorno;
	}
}
