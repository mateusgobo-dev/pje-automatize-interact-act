package br.jus.cnj.pje.amqp;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import br.com.itx.util.ComponentUtil;
import org.jboss.seam.annotations.Destroy;

@Name(AmqpExecutor.NAME)
@Scope(ScopeType.APPLICATION)
@Startup()
@Install()
public class AmqpExecutor implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "amqpExecutor";
	
	protected static final Logger logger = LoggerFactory.getLogger(AmqpExecutor.class);

	private static final int CORE_POOL_SIZE = Math.min(6, Runtime.getRuntime().availableProcessors()+2);
	private static final int MAX_POOL_SIZE = (int)Math.min(32, Runtime.getRuntime().availableProcessors()*1.5+2);
	private static final int QUEUE_CAPACITY = 20000;
	
	private AmqpThreadPoolExecutor executor;
	
	@Create
	public void init() {
		/**
		 * - Número de threads ativas < CORE_POOL_SIZE => POOL dará preferência à execução imediata da nova thread.
		 * - Número de threads ativas > CORE_POOL_SIZE::
		 * -- Número de threads ativas < QUEUE_CAPACITY => POOL dará preferência a colocar a execução na fila
		 * -- Número de threads ativas > QUEUE_CAPACITY::
		 * --- Número de threads ativas < MAX_POOL_SIZE => POOL dará preferência à criação de nova thread 
		 * no POOL e execução imediata
		 * --- Número de threads ativas > MAX_POOL_SIZE => o POOL rejeitará a nova thread e lançará exceção que 
		 * será capturada, fará o processamento aguardar WAITING_TIME_UNTIL_NEXT_TRY ms e tentará re-enfileirar a thread
		 * Referência: https://stackoverflow.com/questions/28659779/submitcallablet-task-method-in-threadpoolexecutor
		 */
		this.executor = new AmqpThreadPoolExecutor(
				CORE_POOL_SIZE, 
				MAX_POOL_SIZE, 
				10, 
				TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(QUEUE_CAPACITY, true),
				new SeamLifecycleThreadFactory("AMQPEvent")
        );
		
		this.executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
	}
	
	@Destroy
	public void destroy() {
		if (this.executor!=null) {
			this.executor.shutdown();
		}
	}
	
	public static AmqpExecutor getInstance() {
		return ComponentUtil.getComponent(AmqpExecutor.NAME);
	}
	
	public ThreadPoolExecutor getExecutor() {
		return executor;
	}
}
