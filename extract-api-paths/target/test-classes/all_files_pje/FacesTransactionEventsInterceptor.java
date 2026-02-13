package br.jus.cnj.pje.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.seam.annotations.intercept.Interceptors;
import org.jboss.seam.transaction.FacesTransactionEvents;

/**
 * Habilita o interceptor que desabilita do framework SEAM a mensagem de falha na transação.
 * 
 * @see {@link FacesTransactionEvents},
 *      {@link IgnoreFacesTransactionMessageInterceptor}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Interceptors(IgnoreFacesTransactionMessageInterceptor.class)
public @interface FacesTransactionEventsInterceptor {

}
