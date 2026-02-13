/**
 * 
 */
package br.jus.cnj.pje.business.dao.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.seam.annotations.intercept.Interceptors;

/**
 * @author cristof
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Interceptors({PJeDAOInterceptor.class})
@Inherited
public @interface PJeDAOIntercepted {

}
