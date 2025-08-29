package br.jus.cnj.pje.business.dao;

import javax.persistence.NoResultException;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.TempoAudienciaOrgaoJulgador;

@Name("tempoAudienciaOrgaoJulgadorDAO")
public class TempoAudienciaOrgaoJulgadorDAO extends BaseDAO<TempoAudienciaOrgaoJulgador>{

	@Override
	public Object getId(TempoAudienciaOrgaoJulgador e) {
		return e.getIdTempoAudienciaOrgaoJulgador();
	}
	
	public Integer recuperarAtivo(Integer idTipoAudiencia, Integer idOrgaoJulgador) {
		StringBuilder jpql = new StringBuilder("SELECT o.tempoAudiencia from TempoAudienciaOrgaoJulgador o ")
			.append("WHERE o.ativo = true AND o.tipoAudiencia.id = :idTipoAudiencia AND o.orgaoJulgador.id = :idOrgaoJulgador");
		
		try {
	        return getEntityManager().createQuery(jpql.toString(), Integer.class)
	        	.setParameter("idTipoAudiencia", idTipoAudiencia)
	        	.setParameter("idOrgaoJulgador", idOrgaoJulgador)
	            .getSingleResult();
	        
		} catch (NoResultException ex) {
			return null;
		}
	}

}
