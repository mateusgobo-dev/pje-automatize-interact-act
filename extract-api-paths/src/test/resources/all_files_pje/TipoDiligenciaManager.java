/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.business.dao.TipoDiligenciaDAO;
import br.jus.pje.nucleo.entidades.TipoDiligencia;

/**
 * @author cristof
 * 
 */
@Name("tipoDiligenciaManager")
public class TipoDiligenciaManager extends BaseManager<TipoDiligencia>{

	@In
	private TipoDiligenciaDAO tipoDiligenciaDAO;

	@Override
	protected TipoDiligenciaDAO getDAO(){
		return tipoDiligenciaDAO;
	}

}
