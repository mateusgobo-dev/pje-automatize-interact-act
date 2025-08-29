package br.jus.cnj.pje.util;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Especifica que um método irá tratar a transação do Seam e do Jbpm.
 * 
 * Para usá-lo é necessário que o componente tenha a anotação @CustomJbpmTransactionalClass
 * 
 */
@Target({METHOD})
@Retention(RUNTIME)
@Documented
@Inherited
public @interface CustomJbpmTransactional
{
}
