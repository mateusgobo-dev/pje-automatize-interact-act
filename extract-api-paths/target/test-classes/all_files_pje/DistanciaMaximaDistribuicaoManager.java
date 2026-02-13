package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.DistanciaMaximaDistribuicaoDAO;
import br.jus.pje.nucleo.entidades.DistanciaMaximaDistribuicao;

@Name("distanciaMaximaDistribuicaoManager")
public class DistanciaMaximaDistribuicaoManager extends BaseManager<DistanciaMaximaDistribuicao> {
	
	@In
	private DistanciaMaximaDistribuicaoDAO distanciaMaximaDistribuicaoDAO;
	
	public DistanciaMaximaDistribuicaoManager() {
		if(distanciaMaximaDistribuicaoDAO == null){
			distanciaMaximaDistribuicaoDAO = new DistanciaMaximaDistribuicaoDAO();
		}
	}
	
	@Override
	protected BaseDAO<DistanciaMaximaDistribuicao> getDAO() {
		return distanciaMaximaDistribuicaoDAO;
	}
	
	public DistanciaMaximaDistribuicao recuperarDistancia(int qtdCargosCompetentes){
		return distanciaMaximaDistribuicaoDAO.recuperarDistancia(qtdCargosCompetentes);
	}
}
