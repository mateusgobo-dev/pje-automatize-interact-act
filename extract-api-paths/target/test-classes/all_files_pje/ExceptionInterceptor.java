package br.com.infox.exceptions;

import javax.interceptor.AroundInvoke;

import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.transaction.TransactionInterceptor;


@Interceptor(around = { TransactionInterceptor.class })
public class ExceptionInterceptor { //extends AbstractInterceptor{
	
	private static final long serialVersionUID = 1L;

	private static final LogProvider log = Logging.getLogProvider(ExceptionInterceptor.class);

	private transient boolean recorrente;

	
	/**
	 * Método interceptador que envolve a chamada ao componente
	 */
	@AroundInvoke
	public Object aroundInvoke(javax.interceptor.InvocationContext invocation) throws Exception {

		if (recorrente) {
			if (log.isTraceEnabled()) {
//				log.trace("Chamada recorrente: " + getComponent().getName());
			}
			return invocation.proceed();
		} else {
			recorrente = true;
			Object result = null;
			try {
				result = invocation.proceed();
				return result;
			} catch (NegocioException ne) {
				trataExcecaoNegocio(ne);
				return result;
			} finally {
				recorrente = false;
			}
		}
	}

	/**
	 * Indica ao Seam se o intercepator está abilitado.
	 * Veja mais em {@link #isInterceptorEnabled()} 
	 */
	public boolean isInterceptorEnabled() {
		return true;
	}

	/**
	 * Cria mensagem do tipo ERROR a ser exibida para o usuário
	 */
	protected void trataExcecaoNegocio(NegocioException e) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR,e.getMensagem());
	}

}
