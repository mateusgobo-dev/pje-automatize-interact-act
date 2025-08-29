package br.jus.cnj.pje.business.dao.interceptor;

import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.annotations.intercept.InterceptorType;
import org.jboss.seam.intercept.InvocationContext;
import br.jus.cnj.pje.business.dao.PJeDAOExceptionFactory;

@Interceptor(stateless = true, type = InterceptorType.ANY)
public class PJeDAOInterceptor{

	@AroundInvoke
	public Object aroundInvoke(InvocationContext icx) throws Exception{

		Object o = null;
		try{

			o = icx.proceed();

		} catch (Exception e){
			throw PJeDAOExceptionFactory.getDaoException(e);
		}
		return o;
	}

}
