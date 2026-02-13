package br.com.infox.exceptions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.seam.annotations.intercept.Interceptors;


@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Interceptors(ExceptionInterceptor.class)
public @interface ExceptionHandler {

}
