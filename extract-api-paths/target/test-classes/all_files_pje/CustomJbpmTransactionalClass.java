package br.jus.cnj.pje.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jboss.seam.annotations.intercept.Interceptors;

/**
 * Especifica que uma classe irá tratar a transação do Seam e do Jbpm.
 * 
 * Para usá-lo é necessário que o método tenha a anotação @CustomJbpmTransactional
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Interceptors(CustomJbpmTransactionalInterceptor.class)
public @interface CustomJbpmTransactionalClass{
}