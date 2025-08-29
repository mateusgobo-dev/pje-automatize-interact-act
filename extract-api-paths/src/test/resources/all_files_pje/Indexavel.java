package br.jus.pje.nucleo.anotacoes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Indexavel {
	/**
	 * Indica o nome deste campo.
	 * 
	 * @return o nome de indexação do campo
	 */
	String value();
	
	/**
	 * Indica se o campo, na indexação, deve ou não ser tokenizado.
	 * 
	 * @return false, por padrão, ou true, caso seja necessário tokenizar o campo
	 */
	boolean tokenized() default false;
}
