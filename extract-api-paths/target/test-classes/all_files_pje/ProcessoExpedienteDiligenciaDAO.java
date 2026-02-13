/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ProcessoExpedienteDiligencia;

/**
 * @author cristof
 * 
 */
@Name("processoExpedienteDiligenciaDAO")
public class ProcessoExpedienteDiligenciaDAO extends BaseDAO<ProcessoExpedienteDiligencia>{

	@Override
	public Integer getId(ProcessoExpedienteDiligencia e){
		return e.getIdProcessoExpedienteDiligencia();
	}

}
