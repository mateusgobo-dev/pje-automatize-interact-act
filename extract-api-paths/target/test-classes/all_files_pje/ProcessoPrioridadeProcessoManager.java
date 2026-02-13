package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcessoPrioridadeProcessoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoPrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ProcessoPrioridadeProcessoManager.NAME)
public class ProcessoPrioridadeProcessoManager extends BaseManager<ProcessoPrioridadeProcesso> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "processoPrioridadeProcessoManager";

	@Override
	protected ProcessoPrioridadeProcessoDAO getDAO() {
		return ComponentUtil.getComponent(ProcessoPrioridadeProcessoDAO.class);
	}

	public void removeTodosProcessoPrioridadeProcessoPorProcesso(ProcessoTrf processoTrf) {
		getDAO().removeTodosProcessoPrioridadeProcessoPorProcesso(processoTrf);
	}

	public void removeProcessoPrioridadeProcessoPorPrioridade(ProcessoTrf processoTrf, PrioridadeProcesso prioridadeProcesso) {
		getDAO().removeProcessoPrioridadeProcessoPorPrioridade(processoTrf, prioridadeProcesso);
	}

	public void insereNovaProcessoPrioridadeProcessoPorPrioridade(ProcessoTrf processoTrf, PrioridadeProcesso prioridadeProcesso) throws PJeBusinessException {
		ProcessoPrioridadeProcesso ppp = new ProcessoPrioridadeProcesso();
		ppp.setProcessoTrf(processoTrf);
		ppp.setPrioridadeProcesso(prioridadeProcesso);
		persistAndFlush(ppp);
	}
}