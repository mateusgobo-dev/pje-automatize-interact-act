/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.RespostaExpediente;

/**
 * @author cristof
 *
 */
@Name("respostaExpedienteDAO")
public class RespostaExpedienteDAO extends BaseDAO<RespostaExpediente> {

	@Override
	public Integer getId(RespostaExpediente e) {
		return e.getId();
	}
	
	/**
	 * Retorna a lista de objetos RespostaExpediente que possui, como documento,
	 * o id informado como parâmetro.
	 *
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-20734
	 * @param idProcessoDocumento
	 * @return List<RespostaExpediente>
	 */
	@SuppressWarnings("unchecked")
	public List<RespostaExpediente> findByDocumento(int idProcessoDocumento) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(RespostaExpediente.class);
		criteria.add(Restrictions.eq("processoDocumento.idProcessoDocumento", idProcessoDocumento));
		criteria.addOrder(Order.asc("id"));
		criteria.setCacheMode(CacheMode.IGNORE);
		return criteria.list();
	}

}
