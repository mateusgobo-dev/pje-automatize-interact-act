package br.jus.cnj.pje.business.dao.migrador;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.TipoOrigem;

@Name(TipoOrigemLegacyDAO.NAME)
public class TipoOrigemLegacyDAO extends BaseDAO<TipoOrigem>{

	public static final String NAME = "tipoOrigemLegacyDAO";

	@Override
	public Object getId(TipoOrigem e) {
		return e.getId();
	}
	
	@SuppressWarnings("unchecked")
	public List<TipoOrigem> recuperarPendentesMigracao(){
		StringBuilder sb = new StringBuilder("SELECT t FROM TipoOrigem t ");
		sb.append(" WHERE t.codigoNacional IS NULL ");
		
		Query q = this.getEntityManager().createQuery(sb.toString());
		
		List<TipoOrigem> ret = q.getResultList();
		
		return CollectionUtilsPje.isEmpty(ret) ? new ArrayList<TipoOrigem>(0) : ret;		
	}
}
