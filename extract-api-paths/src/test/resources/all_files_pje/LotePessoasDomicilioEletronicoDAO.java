/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.LotePessoasDomicilioEletronico;

/**
 * @author cristof
 * 
 */
@Name("lotePessoasDomicilioEletronicoDAO")
public class LotePessoasDomicilioEletronicoDAO extends BaseDAO<LotePessoasDomicilioEletronico> {

	@Override
	public Object getId(LotePessoasDomicilioEletronico e) {
		return e.getId();
	}

	@SuppressWarnings("unchecked")
	public List<String> findAllNomesLotesProcessados() {
		String jpql = "select l.nomeArquivo from LotePessoasDomicilioEletronico l";
		Query query = getEntityManager().createQuery(jpql);
		return (List<String>) query.getResultList();
	}
	
	public LotePessoasDomicilioEletronico findLote(String nomeArquivo) {
		String jpql = "select l from LotePessoasDomicilioEletronico l where l.nomeArquivo = :nomeArquivo";
		Query query = getEntityManager().createQuery(jpql);
		query.setParameter("nomeArquivo", nomeArquivo);
		query.setMaxResults(1);
		return EntityUtil.getSingleResult(query);
	}

	public boolean isLoteJaProcessado(String nomeArquivo) {
		String jpql = "select count(1) from LotePessoasDomicilioEletronico where nomeArquivo = :nomeArquivo";
		Query query = getEntityManager().createQuery(jpql);
		query.setParameter("nomeArquivo", nomeArquivo);
		Long value =  EntityUtil.getSingleResult(query);
		return  value > 0;
	}

}
