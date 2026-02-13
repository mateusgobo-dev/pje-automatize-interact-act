/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import javax.persistence.Query;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.pje.nucleo.entidades.PessoaDomicilioEletronico;

/**
 * @author cristof
 * 
 */
@Name("pessoaDomicilioEletronicoDAO")
public class PessoaDomicilioEletronicoDAO extends BaseDAO<PessoaDomicilioEletronico> {

	@Logger
	private Log logger;

	@Override
	public Object getId(PessoaDomicilioEletronico e) {
		return e.getId();
	}

	/**
	 * ATENÇÃO: ESTE MÉTODO NÃO DEVE SER UTILIZADO DIRETAMENTE! Utilizar
	 * {@link DomicilioEletronicoService#getPessoa(String)}, já que o
	 * método do Service leva em consideração se o cache local está ativo ou não.
	 * Recupera do banco de dados uma pessoa por número de documento.
	 * 
	 * @param numeroDocumento número do documento da pessoa.
	 * @return entidade que representa a pessoa com esse documento no banco de
	 *         dados.
	 */
	public PessoaDomicilioEletronico findByNumeroDocumento(String numeroDocumento) {
		String jpql = "select p from PessoaDomicilioEletronico p where p.numeroDocumento = :numeroDocumento";
		Query query = getEntityManager().createQuery(jpql);
		query.setParameter("numeroDocumento", numeroDocumento);
		return EntityUtil.getSingleResult(query);
	}
}
