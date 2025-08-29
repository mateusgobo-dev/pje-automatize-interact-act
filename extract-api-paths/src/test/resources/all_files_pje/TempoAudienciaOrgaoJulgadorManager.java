package br.jus.cnj.pje.nucleo.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.TempoAudienciaOrgaoJulgadorDAO;
import br.jus.pje.nucleo.entidades.TempoAudienciaOrgaoJulgador;

@Name("tempoAudienciaOrgaoJulgadorManager")
public class TempoAudienciaOrgaoJulgadorManager extends BaseManager<TempoAudienciaOrgaoJulgador>{

	@In
	private TempoAudienciaOrgaoJulgadorDAO tempoAudienciaOrgaoJulgadorDAO;
	
	@Override
	protected BaseDAO<TempoAudienciaOrgaoJulgador> getDAO() {
		return tempoAudienciaOrgaoJulgadorDAO;
	}
	
	public Integer recuperarAtivo(Integer idTipoAudiencia, List<Integer> idsOrgaoJulgador) {
		Set<Integer> ids = new HashSet<Integer>(idsOrgaoJulgador);
		
		if (ids.size() == 1) {
			return tempoAudienciaOrgaoJulgadorDAO.recuperarAtivo(idTipoAudiencia, ids.iterator().next());
		}
		return null;
	}

}
