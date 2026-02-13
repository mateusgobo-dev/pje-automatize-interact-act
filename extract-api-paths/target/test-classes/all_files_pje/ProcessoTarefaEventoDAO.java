/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ProcessoTarefaEvento;

/**
 * Componente de acesso a dados da entidade {@link ProcessoTarefaEvento}.
 * 
 * @author cristof
 *
 */
@Name("processoTarefaEventoDAO")
public class ProcessoTarefaEventoDAO extends BaseDAO<ProcessoTarefaEvento> {

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Object getId(ProcessoTarefaEvento t) {
		return t.getIdProcessoTarefaEvento();
	}

}
