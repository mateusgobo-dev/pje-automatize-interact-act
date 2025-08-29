/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

/**
 * @author cristof
 * 
 */
@Name("entityManagerUtil")
public class EntityManagerUtil {

	@In(create = true, required = true)
	private EntityManager entityManager;

	public void flush() {
		entityManager.flush();
	}

}
