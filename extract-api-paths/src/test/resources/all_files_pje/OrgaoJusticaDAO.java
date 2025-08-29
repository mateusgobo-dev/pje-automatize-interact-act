/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.lancadormovimento.OrgaoJustica;

@Name(OrgaoJusticaDAO.NAME)
public class OrgaoJusticaDAO extends BaseDAO<OrgaoJustica> {

	public static final String NAME = "orgaoJusticaDAO";

	@Override
	public Long getId(OrgaoJustica oj) {
		return oj.getIdOrgaoJustica();
	}
	
	public OrgaoJustica findByOrgaoJustica(String orgaoJustica) {
		StringBuilder jpql = new StringBuilder("from OrgaoJustica oj ");
		jpql.append("where oj.nome = :orgaoJustica ");
		jpql.append("AND oj.ativo = true ");
		
		Query query = getEntityManager().createQuery(jpql.toString())
			.setParameter("orgaoJustica", orgaoJustica);
	
		return (OrgaoJustica) EntityUtil.getSingleResult(query);
	}

}
