package br.jus.cnj.pje.util;

import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.bpm.BusinessProcessInterceptor;
import org.jboss.seam.core.BijectionInterceptor;
import org.jboss.seam.core.ConversationInterceptor;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.transaction.RollbackInterceptor;

/**
 * Interceptor implementado para gerenciar transações em andamento no contexto do Jbpm.
 * 
 */
@Interceptor(stateless = false,
		around = {RollbackInterceptor.class, BusinessProcessInterceptor.class,
				ConversationInterceptor.class, BijectionInterceptor.class})
public class CustomJbpmTransactionalInterceptor extends AbstractInterceptor
{

	private static final long serialVersionUID = -4364203056333738988L;

	transient private Map<AnnotatedElement, TransactionMetadata> transactionMetadata = new HashMap<AnnotatedElement, TransactionMetadata>();

	private class TransactionMetadata
	{

		private boolean annotationPresent;

		public TransactionMetadata(AnnotatedElement element)
		{
			annotationPresent = element.isAnnotationPresent(CustomJbpmTransactional.class);
		}

		public boolean isAnnotationPresent(){
			return annotationPresent;
		}

	}

	private TransactionMetadata lookupTransactionMetadata(AnnotatedElement element){
		if (transactionMetadata == null){
			transactionMetadata = new HashMap<AnnotatedElement, TransactionMetadata>();
		}

		TransactionMetadata metadata = transactionMetadata.get(element);

		if (metadata == null){
			metadata = loadMetadata(element);
		}

		return metadata;
	}

	private synchronized TransactionMetadata loadMetadata(AnnotatedElement element){
		if (!transactionMetadata.containsKey(element)){
			TransactionMetadata metadata = new TransactionMetadata(element);
			transactionMetadata.put(element, metadata);
			return metadata;
		}

		return transactionMetadata.get(element);
	}

	@AroundInvoke
	public Object aroundInvoke(final InvocationContext invocation) throws Exception
	{
		if (!gerenciarTransacao(invocation)) {
			return invocation.proceed();
		}
		return 
				//Utilizando o worker criado para evitar duplicação de código.
			new CustomTransactionalWork<Object>(true) {
				@Override
				protected Object work() throws Exception {
					return invocation.proceed();
				}
			}.workInTransaction();

		
	}

	public boolean gerenciarTransacao(final InvocationContext invocation){
		TransactionMetadata metadata = lookupTransactionMetadata(invocation.getMethod());
		// Se método estiver anotado com TransactionalJbpmAware -> return true
		if (metadata.isAnnotationPresent())
		{
			return true;
		}
		else{
			return false;
		}
	}

	public boolean isInterceptorEnabled()
	{
		return true;
	}

}
