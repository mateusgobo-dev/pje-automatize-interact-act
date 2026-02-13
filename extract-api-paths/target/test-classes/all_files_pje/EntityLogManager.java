package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.EntityLogDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.log.EntityLog;

@Name(EntityLogManager.NAME)
public class EntityLogManager extends BaseManager<EntityLog>{
	
	public static final String NAME = "entityLogManager";
	
	@In
	private EntityLogDAO entityLogDAO;

	@Override
	protected EntityLogDAO getDAO() {
		return entityLogDAO;
	}

	/**
	 * metodo responsavel por retornar todos os entityLogs onde a pessoa passada em parametro foi a pessoa que alterou a entidade.
	 * @param _pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<EntityLog> recuperarEntityLogs(Pessoa _pessoa) throws Exception {
		return entityLogDAO.recuperarEntityLogs(_pessoa);
	}

	public EntityLog recuperaEntityLog(Integer idEntityLog) {
		return entityLogDAO.find(idEntityLog);
	}
}