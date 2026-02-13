package br.jus.cnj.pje.business.dao.migrador;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.OrgaoProcedimentoOriginario;

@Name(OrgaoProcedimentoOriginarioLegacyDAO.NAME)
public class OrgaoProcedimentoOriginarioLegacyDAO extends BaseDAO<OrgaoProcedimentoOriginario>{

	public static final String NAME = "orgaoProcedimentoOriginarioLegacyDAO";

	@Override
	public Object getId(OrgaoProcedimentoOriginario e) {
		return e.getId();
	}
	
	@SuppressWarnings("unchecked")
	public List<OrgaoProcedimentoOriginario> recuperarPendentesMigracao(){
		StringBuilder sb = new StringBuilder("SELECT o FROM OrgaoProcedimentoOriginario o ");
		sb.append(" WHERE o.codigoNacional IS NULL ");
		
		Query q = this.getEntityManager().createQuery(sb.toString());
		
		List<OrgaoProcedimentoOriginario> ret = q.getResultList();
		
		return CollectionUtilsPje.isEmpty(ret) ? new ArrayList<OrgaoProcedimentoOriginario>(0) : ret;		
	}
}
