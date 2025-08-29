package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.PesoPrevencaoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.PesoPrevencao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(PesoPrevencaoManager.NAME)
public class PesoPrevencaoManager extends BaseManager<PesoPrevencao>{
	
	public static final String NAME = "pesoPrevencaoManager";
	
	@In(create = true)
	private PesoPrevencaoDAO pesoPrevencaoDAO;

	@Override
	protected BaseDAO<PesoPrevencao> getDAO() {
		return this.pesoPrevencaoDAO;
	}
	
	public Double buscarPesoPrevencaoIncidental(ProcessoTrf processoTrf) throws PJeBusinessException {
		return this.pesoPrevencaoDAO.buscarPesoPrevencaoIncidental(processoTrf);
	}
}
