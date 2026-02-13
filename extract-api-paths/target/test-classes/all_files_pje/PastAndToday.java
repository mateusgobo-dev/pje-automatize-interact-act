package br.jus.csjt.pje.commons.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.validator.ValidatorClass;

/**
 * Verifica se um Date, Calendar ou String esta no passado ou hoje.
 * 
 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
 * 
 * @category PJE-JT
 * @since 1.4.2
 * @created 29/09/2011
 * 
 */
@Documented
@ValidatorClass(PastAndTodayValidator.class)
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface PastAndToday {
	String message() default "{validator.pastAndToday}";
}
