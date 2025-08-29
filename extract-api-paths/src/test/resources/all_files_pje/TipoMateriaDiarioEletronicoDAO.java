/**
 * 
 */
package br.jus.csjt.pje.persistence.dao;

import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.jt.entidades.TipoMateriaDiarioEletronico;

@Name("tipoMateriaDiarioEletronicoDAO")
public class TipoMateriaDiarioEletronicoDAO extends BaseDAO<TipoMateriaDiarioEletronico>{

	@Override
	public Integer getId(TipoMateriaDiarioEletronico e){
		return e.getIdTipoMateria();
	}

}
