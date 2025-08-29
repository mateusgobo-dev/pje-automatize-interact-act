package br.jus.cnj.pje.business.dao;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.DistanciaMaximaDistribuicao;

@Name("distanciaMaximaDistribuicaoDAO")
public class DistanciaMaximaDistribuicaoDAO extends BaseDAO<DistanciaMaximaDistribuicao>{

	@Override
	public Object getId(DistanciaMaximaDistribuicao dmd) {
		return dmd.getIdDistancia();
	}
	
	public DistanciaMaximaDistribuicao recuperarDistancia(int numeroCargosCompetentes){
		String query = "select dmd from DistanciaMaximaDistribuicao as dmd WHERE :numeroCargosCompetentes BETWEEN dmd.intervaloInicial AND dmd.intervaloFinal";
		Query q = entityManager.createQuery(query);
		q.setParameter("numeroCargosCompetentes", numeroCargosCompetentes);
		return  (DistanciaMaximaDistribuicao) q.getSingleResult();
	}

}
