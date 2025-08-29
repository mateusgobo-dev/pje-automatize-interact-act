package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ProcessoHistoricoClasseDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.ProcessoHistoricoClasse;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ProcessoHistoricoClasseManager.NAME)
public class ProcessoHistoricoClasseManager extends BaseManager<ProcessoHistoricoClasse>{

	public static final String NAME = "processoHistoricoClasseManager";

	@In
	private ProcessoHistoricoClasseDAO processoHistoricoClasseDAO;

	@Override
	protected BaseDAO<ProcessoHistoricoClasse> getDAO() {
		return processoHistoricoClasseDAO;
	}
	
	private Date verificaDataInicio(ProcessoTrf processo) {
		return processoHistoricoClasseDAO.verificaDataInicio(processo);
	}
	
	@Override
	public ProcessoHistoricoClasse persist(ProcessoHistoricoClasse e)
			throws PJeBusinessException {
		return super.persist(e);
	}
	
	@Override
	public void persistAndFlush(ProcessoHistoricoClasse t)
			throws PJeBusinessException {
		t.setDataInicio(verificaDataInicio(t.getProcessoTrf()));
		t.setDataFim(new Date());
		super.persistAndFlush(t);
	}
	
}
