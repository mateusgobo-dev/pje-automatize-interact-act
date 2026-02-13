package br.jus.cnj.pje.business.dao.migrador;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.TipoProcedimentoOrigem;

@Name(TipoProcedimentoOrigemLegacyDAO.NAME)
public class TipoProcedimentoOrigemLegacyDAO extends BaseDAO<TipoProcedimentoOrigem>{

	public static final String NAME = "tipoProcedimentoOrigemLegacyDAO";

	@Override
	public Object getId(TipoProcedimentoOrigem e) {
		return e.getId();
	}
	
	@SuppressWarnings("unchecked")
	public List<TipoProcedimentoOrigem> recuperarPendentesMigracao(){
		StringBuilder sb = new StringBuilder("SELECT t FROM TipoProcedimentoOrigem t ");
		sb.append(" WHERE t.codigoNacional IS NULL ");
		
		Query q = this.getEntityManager().createQuery(sb.toString());
		
		List<TipoProcedimentoOrigem> ret = q.getResultList();
		
		return CollectionUtilsPje.isEmpty(ret) ? new ArrayList<TipoProcedimentoOrigem>(0) : ret;		
	}
}
