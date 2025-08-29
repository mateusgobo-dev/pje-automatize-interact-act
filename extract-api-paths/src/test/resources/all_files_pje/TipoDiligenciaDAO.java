/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.TipoDiligencia;

/**
 * @author cristof
 * 
 */
@Name("tipoDiligenciaDAO")
public class TipoDiligenciaDAO extends BaseDAO<TipoDiligencia>{

	@Override
	public Integer getId(TipoDiligencia e){
		return e.getIdTipoDiligencia();
	}

}
