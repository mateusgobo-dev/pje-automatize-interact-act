/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.itx.jsf;

import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.core.Expressions;

import br.com.itx.util.CreateCachedFactories;

public final class JsfFunctions {

	private JsfFunctions() {
	}

	public static Object get(Object value, Object defaultValue) {
		return value == null ? defaultValue : value;
	}

	public static Integer splitLength(String obj, String token) {
		if (obj == null) {
			return 0;
		}
		return obj.split(token).length;
	}

	@BypassInterceptors
	public static Object cached(String expression, Context context) {
		// Procurando a váriavel no contexto, 
		// se encontrar então o getter já executou,
		// senão roda EL para executar o método e cachear o resultado no contexto
		Object fromContext = context.get(CreateCachedFactories.CACHED_PREFIX + expression);
		
		return fromContext != null ? 
					fromContext : 
					Expressions
						.instance()
						.createValueExpression(
								"#{" + CreateCachedFactories.CACHED_PREFIX + expression
								+ "}")
						.getValue();
	}
	
	/**
	 * @return true se existir mensagem de erro.
	 */
	public static Boolean isExisteMensagemError() {
		Boolean resultado = Boolean.FALSE;
		
		FacesContext fc = FacesContext.getCurrentInstance();
		Iterator<FacesMessage> messages = fc.getMessages();
		while (messages.hasNext() && !resultado) {
			FacesMessage message = messages.next();
			
			Severity severityError = FacesMessage.SEVERITY_ERROR;
			Severity severity = message.getSeverity();
			resultado = (severity.getOrdinal() == severityError.getOrdinal());
		}
		return resultado;
	}
}