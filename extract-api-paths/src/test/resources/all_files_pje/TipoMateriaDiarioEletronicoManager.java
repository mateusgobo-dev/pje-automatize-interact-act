/**
 * 
 */
package br.jus.csjt.pje.business.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.csjt.pje.persistence.dao.TipoMateriaDiarioEletronicoDAO;
import br.jus.pje.jt.entidades.TipoMateriaDiarioEletronico;


@Name("tipoMateriaDiarioEletronicoManager")
public class TipoMateriaDiarioEletronicoManager extends BaseManager<TipoMateriaDiarioEletronico>{

	@In(create = true, required = true)
	private TipoMateriaDiarioEletronicoDAO tipoMateriaDiarioEletronicoDAO;

	@Override
	protected TipoMateriaDiarioEletronicoDAO getDAO(){
		return this.tipoMateriaDiarioEletronicoDAO;
	}

}
