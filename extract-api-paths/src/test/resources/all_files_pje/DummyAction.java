/**
 * ActionDummy.java
 *
 * Data: 6 de dez de 2016
 */
package br.com.infox.pje.action;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Action dummy usada nos componentes onde o atributo 'action' é facultativo.
 * 
 * @author Adriano Pamplona
 */
@Name("dummyAction")
@BypassInterceptors
public class DummyAction {

	/**
	 * Método dummy.
	 */
	public void dummy() {
	}
}
