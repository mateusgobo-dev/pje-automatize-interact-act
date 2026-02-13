package br.jus.cnj.pje.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Anotação que habilita o {@link IgnoreFacesTransactionMessageInterceptor} a
 * ignorar a mensagem de falha de transação ao executar o método anotado.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreFacesTransactionMessageError {

}
