package br.com.infox.cliente.util;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RUNTIME)
@Documented
/**
 * Anotação para restringir a validações de parâmetros no checkup,
 * baseado em uma EL que deverá retornar um boolean.
 */
public @interface ValidarParametro {

	/**
	 * EL que deverá retornar um boolean. Ex.: somenteQuando="#{!justicaTrabalho}"
	 */
	String somenteQuando();

}
