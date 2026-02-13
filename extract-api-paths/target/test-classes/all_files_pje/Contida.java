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
 * Anotação indicatida de que o campo em questão é entidade de indexação
 * que contém a presente entidade e que, por isso, deve ser atualizado 
 * em razão da indexação desta entidade.
 *  
 * @author cristof
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Contida {

}
