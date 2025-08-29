/**
 * pje-comum
 * Copyright (C) 2009-2014 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.anotacoes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação indicativa de que um determinada entidade deve ser indexada.
 * 
 * @author cristof
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target(value={ElementType.TYPE})
public @interface IndexedEntity {
	String value() default "";
	String id() default "";
	String[] owners() default {};
	Mapping[] mappings();
}
