package br.com.infox.pje.dao;

import java.io.Serializable;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.BaseCalculoIrQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.BaseCalculoIr;

/**
 * Classe que realiza as consultas das NamedQueries e quaisquer outras consultas
 * da entidade BaseCalculoIr.
 * 
 * @author Silas
 * 
 */
@Name(BaseCalculoIrDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class BaseCalculoIrDAO extends GenericDAO implements Serializable, BaseCalculoIrQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "baseCalculoIrDAO";

	public BaseCalculoIr getBaseCalculoIrByValor(Double valor) {
		Query q = getEntityManager().createQuery(GET_BASE_CALCULO_IR_BY_VALOR_QUERY);
		q.setParameter(QUERY_PARAMETER_VALOR, valor);
		BaseCalculoIr result = EntityUtil.getSingleResult(q);
		return result;
	}

	public BaseCalculoIr getBaseCalculoIr() {
		Query q = getEntityManager().createQuery(GET_BASE_CALCULO_IR_QUERY);
		BaseCalculoIr result = EntityUtil.getSingleResult(q);
		return result;
	}	
	
}