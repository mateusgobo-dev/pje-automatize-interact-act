/**
 * CdaDAO
 * 
 * Data: 20/08/2020
 */
package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.pje.nucleo.entidades.Cda;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe responsável pela persistência de uma CDA.
 * 
 * @author Adriano Pamplona
 */
@SuppressWarnings("unchecked")
@Name(CdaDAO.NAME)
public class CdaDAO extends BaseDAO<Cda> {

	public static final String NAME = "cdaDAO";

	@Override
	public Integer getId(Cda cda) {
		Long id = cda.getId();
		return (id != null ? id.intValue() : null);
	}

	/**
	 * @param numeroCda
	 * @return CDA pelo número.
	 */
	public List<Cda> consultar(String numeroCda) {
		List<Cda> resultado = new ArrayList<>();
		
		if (StringUtils.isNotBlank(numeroCda)) {
			numeroCda = StringUtil.removeNaoNumericos(numeroCda);
			Query query = getEntityManager().createQuery("from Cda where ativo = true and numero = :numero");
			query.setParameter("numero", numeroCda);
			
			resultado = query.getResultList();
		}
		return resultado;
	}
	
	/**
	 * @param numeroCda
	 * @return CDA pelo número.
	 */
	public Cda obter(ProcessoTrf processo, String numeroCda) {
		Cda resultado = null;
		
		if (processo != null && StringUtils.isNotBlank(numeroCda)) {
			Query query = getEntityManager().createQuery("from Cda where ativo = true and numero = :numero and processoTrf = :processo");
			query.setParameter("numero", numeroCda);
			query.setParameter("processo", processo);
			
			resultado = getSingleResult(query);
		}
		return resultado;
	}

	/**
	 * @param numeroCda
	 * @return True se o número informado existir.
	 */
	public Boolean isNumeroExiste(String numeroCda) {
		Boolean resultado = Boolean.FALSE;
		
		if (StringUtils.isNotBlank(numeroCda)) {
			Query query = getEntityManager().createQuery("from Cda where numero = :numero");
			query.setParameter("numero", numeroCda);
			
			List list = query.getResultList();
			resultado = ProjetoUtil.isNotVazio(list);
		}
		
		return resultado;
	}
}
