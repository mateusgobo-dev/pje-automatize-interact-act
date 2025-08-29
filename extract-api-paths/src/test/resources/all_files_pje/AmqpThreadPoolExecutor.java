package br.jus.cnj.pje.amqp;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmqpThreadPoolExecutor extends ThreadPoolExecutor {
	
	protected static final Logger logger = LoggerFactory.getLogger(AmqpThreadPoolExecutor.class);
	 
    public AmqpThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }
 
    public AmqpThreadPoolExecutor(int corePoolSize, int maxPoolSize, int keepAliveTime, TimeUnit unit,
    		BlockingQueue<Runnable> workQueue, ThreadFactory build) {
        super(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, build);
	}

	@Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);

		if (logger.isDebugEnabled()) {
			logger.debug("AmqpThreadPoolExecutor.beforeExecute: CurrentThread #" + Thread.currentThread().getId() + " '" + Thread.currentThread().getName() + "' will run Thread #" + t.getId() + " '" + t.getName() + "', ActiveThreadsCount: " + getActiveCount() +", TotalTasksCount: " + getTaskCount() +", CompletedTaskCount: " + getCompletedTaskCount());
		}
    }
 
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (t != null) {
	        logger.debug("AmqpThreadPoolExecutor.afterExecute: CurrentThread #" + Thread.currentThread().getId() + " '" + Thread.currentThread().getName() + "' execute and throws: " + t);
        }		
        super.afterExecute(r, t);
    }
 
}