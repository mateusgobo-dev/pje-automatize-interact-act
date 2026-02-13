package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.EspecialidadeDAO;
import br.jus.pje.nucleo.entidades.Especialidade;

@Name("especialidadeManager")
public class EspecialidadeManager extends BaseManager<Especialidade> {

	@In
	private EspecialidadeDAO especialidadeDAO;
	
	@Override
	protected BaseDAO<Especialidade> getDAO() {
		return especialidadeDAO;
	}

	public List<Especialidade> recuperarAtivas() {
		return especialidadeDAO.recuperarAtivas();
	}
	
}
