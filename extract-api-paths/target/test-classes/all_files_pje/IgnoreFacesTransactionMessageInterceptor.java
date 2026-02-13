package br.jus.cnj.pje.interceptor;

import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.core.Events;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.transaction.FacesTransactionEvents;

/**
 * Responsável por desabilitar do framework SEAM a mensagem de falha na transação.
 * <br><br>
 * Os métodos anotados com {@link IgnoreFacesTransactionMessageError} serão
 * tratados para não apresentar a mensagem de falha na transação.
 * 
 * @see {@link FacesTransactionEvents},
 *      {@link FacesTransactionEventsInterceptor},
 *      {@link IgnoreFacesTransactionMessageEvent}
 */
@Interceptor(stateless = true)
public class IgnoreFacesTransactionMessageInterceptor {

	/**
	 * Caso o método esteja anotado com
	 * {@link IgnoreFacesTransactionMessageError}, dispara o evento para remover
	 * a mensagem de falha na transação colocada pelo framework seam.
	 * 
	 * @param ctx
	 * 
	 * @return retorno do método executado.
	 * 
	 * @throws Exception
	 */
	@AroundInvoke
	public Object aroundInvoke(InvocationContext ctx) throws Exception {
		if (ctx.getMethod().isAnnotationPresent(IgnoreFacesTransactionMessageError.class)) {
			Events.instance().raiseEvent(IgnoreFacesTransactionMessageEvent.IGNORE_MESSAGE);
		}

		return ctx.proceed();
	}
}
