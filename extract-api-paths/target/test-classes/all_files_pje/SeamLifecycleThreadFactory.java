package br.jus.cnj.pje.amqp;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;

/**
 *
 * @author marcio
 */
public class SeamLifecycleThreadFactory implements ThreadFactory {

	private static final AtomicInteger poolNumber = new AtomicInteger(0);
	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(0);
	private final String namePrefix;

	SeamLifecycleThreadFactory(String name) {
		String sName = "slctf-" + name + "-" + poolNumber.incrementAndGet();
		namePrefix = sName + "-";
		SecurityManager s = System.getSecurityManager();
		ThreadGroup tgp = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		group = new ThreadGroup(tgp, sName);
	}

	@Override
	public Thread newThread(Runnable target) {
		Thread t = new Thread(group, setupTarget(target), namePrefix + threadNumber.incrementAndGet(), 0);
		if (!t.isDaemon()) {
			t.setDaemon(true);
		}
		if (t.getPriority() != Thread.NORM_PRIORITY) {
			t.setPriority(Thread.NORM_PRIORITY);
		}
		return t;
	}

	/**
	 * Nas versões mais recentes, há:
	 * javax.enterprise.concurrent.ManagedThreadFactory
	 * javax.enterprise.concurrent.ContextService. Vide:
	 * https://docs.wildfly.org/13/Developer_Guide.html
	 */
	private Runnable setupTarget(Runnable target) {
		return new Runnable() {
			@Override
			public void run() {
				boolean isAppInitilized = Lifecycle.isApplicationInitialized() && Contexts.isApplicationContextActive();
				if (!isAppInitilized) {
					Lifecycle.beginCall();
				}
				try {
					target.run();
				} catch (Exception ex) {
					ex.printStackTrace();
					throw new RuntimeException(ex);
				} finally {
					if (!isAppInitilized) {
						Lifecycle.endCall();
					}
				}
			}
		};
	}
	
	public ThreadGroup getThreadGroup() {
		return group;
	}
	
	
}//end of class SeamLifecycleThreadFactory
