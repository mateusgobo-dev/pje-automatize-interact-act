/**
 * 
 */
package br.jus.pje.nucleo.anotacoes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cristof
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Mapping {
	String beanPath();
	String mappedPath();
	String when() default "";
	String extractor() default "";
	String typeMapping() default "";
}
