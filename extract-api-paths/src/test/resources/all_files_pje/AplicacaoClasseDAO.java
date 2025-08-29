/**
 * 
 */
package br.jus.cnj.pje.business.dao;


import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;

/**
 * @author cristof
 *
 */
@Name("aplicacaoClasseDAO")
public class AplicacaoClasseDAO extends BaseDAO<AplicacaoClasse> {

	@Override
	public Integer getId(AplicacaoClasse ac) {
		return ac.getIdAplicacaoClasse();
	}

	public AplicacaoClasse findByCodigo(String codigoAplicacao) {
		StringBuilder jpql = new StringBuilder("from AplicacaoClasse ac ");
		jpql.append("where ac.codigoAplicacaoClasse = :codigoAplicacao ");
		jpql.append("AND ac.ativo = true ");
		
		Query query = getEntityManager().createQuery(jpql.toString())
			.setParameter("codigoAplicacao", codigoAplicacao);
	
		return (AplicacaoClasse) EntityUtil.getSingleResult(query);
	}
}
