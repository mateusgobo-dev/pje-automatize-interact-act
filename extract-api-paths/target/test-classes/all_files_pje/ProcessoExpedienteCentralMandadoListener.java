package br.jus.cnj.pje.entidades.listeners;

import java.util.Date;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;

public class ProcessoExpedienteCentralMandadoListener {

	public void prePersist(ProcessoExpedienteCentralMandado processoExpedienteCentralMandado) {
		processoExpedienteCentralMandado.setDtRecebido(new Date());
	}

	public void preUpdate(ProcessoExpedienteCentralMandado processoExpedienteCentralMandado) {
		Integer idCentralMandado = null;
		String query = "SELECT o.idProcessoExpedienteCentralMandado FROM ProcessoExpedienteCentralMandado o WHERE o.idProcessoExpedienteCentralMandado = :id ";
		idCentralMandado = (Integer)EntityUtil.getEntityManager()
				.createQuery(query)
				.setParameter("id", processoExpedienteCentralMandado.getIdProcessoExpedienteCentralMandado())
				.getSingleResult();
		
		if (processoExpedienteCentralMandado.getCentralMandado() != null && 
				processoExpedienteCentralMandado.getCentralMandado().getIdCentralMandado() != idCentralMandado) {
			
			processoExpedienteCentralMandado.setDtRecebido(new Date());
		}
		
	}
}
