/**
 * 
 */
package br.jus.cnj.pje.nucleo;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.log.Log;

/**
 * @author cristof
 * 
 */
@Name("pjeResourceFactory")
@Scope(ScopeType.STATELESS)
public class PJeResourceFactory {

	@In(create = true)
	private Expressions expressions;

	@Logger
	private Log logger;

	@Factory
	public DataSource getNonBinaryRepository() {
		try {
			InitialContext ctx = new InitialContext();
			String nonBinaryDataSource = (String) expressions.createValueExpression("#{dataSourceName}").getValue();
			logger.debug("Obtendo datasource [" + nonBinaryDataSource + "] a partir do compponente [dataSourceName].");
			DataSource ret = (DataSource) ctx.lookup("java:/" + nonBinaryDataSource);
			return ret;
		} catch (NamingException e) {
			logger.error(e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		return null;
	}
}
