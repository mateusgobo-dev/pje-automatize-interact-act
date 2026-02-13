package br.jus.cnj.pje.business.dao;

import java.util.Collections;
import java.util.List;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoPrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ProcessoPrioridadeProcessoDAO.NAME)
public class ProcessoPrioridadeProcessoDAO extends BaseDAO<ProcessoPrioridadeProcesso> {

	public static final String NAME = "processoPrioridadeProcessoDAO";

	@Override
	public Object getId(ProcessoPrioridadeProcesso e) {
		return e.getIdProcessoPrioridadeProcesso();
	}

	public List<ProcessoPrioridadeProcesso> getByProcessoTrf(Integer idProcessoTrf){
		List<ProcessoPrioridadeProcesso> retorno = Collections.emptyList();
		if (idProcessoTrf != null) {
			retorno = EntityUtil.getEntityManager()
					.createQuery("SELECT o FROM ProcessoPrioridadeProcesso o WHERE o.processoTrf.idProcessoTrf = :idProcessoTrf", ProcessoPrioridadeProcesso.class)
					.setParameter("idProcessoTrf", idProcessoTrf)
					.getResultList();
		}
		return retorno;
	}
	
	public void removeTodosProcessoPrioridadeProcessoPorProcesso(ProcessoTrf processoTrf) {
		// operações em lote (bulk operations) como a desse método não vão disparar listeners de entidades.
		// por essa razão, precisou-se remover um a um
		if (processoTrf != null) {
			getByProcessoTrf(processoTrf.getIdProcessoTrf()).forEach(p -> EntityUtil.getEntityManager().remove(p));
			EntityUtil.flush();
		}
	}

	public void removeProcessoPrioridadeProcessoPorPrioridade(ProcessoTrf processoTrf, PrioridadeProcesso prioridadeProcesso) {
		// operações em lote (bulk operations) como a desse método não vão disparar listeners de entidades.
		// por essa razão, precisou-se remover um a um
		if (processoTrf != null && prioridadeProcesso != null) {
			getByProcessoTrf(processoTrf.getIdProcessoTrf()).stream()
				.filter(p -> p.getPrioridadeProcesso().equals(prioridadeProcesso))
				.forEach(p -> EntityUtil.getEntityManager().remove(p));
			EntityUtil.flush();
		}
	}
}
