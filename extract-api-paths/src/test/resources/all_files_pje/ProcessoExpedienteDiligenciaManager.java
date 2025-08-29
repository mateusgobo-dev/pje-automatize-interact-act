/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.business.dao.ProcessoExpedienteDiligenciaDAO;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteDiligencia;

/**
 * @author cristof
 * 
 */
@Name("processoExpedienteDiligenciaManager")
public class ProcessoExpedienteDiligenciaManager extends BaseManager<ProcessoExpedienteDiligencia>{

	@In
	private ProcessoExpedienteDiligenciaDAO processoExpedienteDiligenciaDAO;

	@Override
	protected ProcessoExpedienteDiligenciaDAO getDAO(){
		return processoExpedienteDiligenciaDAO;
	}

}
