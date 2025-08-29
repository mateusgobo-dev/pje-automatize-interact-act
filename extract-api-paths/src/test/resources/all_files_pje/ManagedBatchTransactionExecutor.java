package br.jus.cnj.pje.util;

import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.transaction.UserTransaction;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;

/**
 * Executa operações em batch dentro de uma transação de forma gerenciada.
 * @param <K> Id ou elemento de entrada que será posto numa coleção que será 
 * processada.
 * @param <E> Elemento que será recuperado com base no Id (parâmetro K) ou 
 * elemento de entrada.
 * 
 * @author mwborges@trf3.jus.br 
 */
public class ManagedBatchTransactionExecutor<K,E> implements Runnable {
	
	private static final LogProvider log = Logging.getLogProvider(ManagedBatchTransactionExecutor.class);

	private Supplier<Collection<K>> collectionSupplier;
	
	private BiFunction<ManagedBatchTransactionExecutor<K,E>,Exception, Boolean> handleElementException = (m,e)-> {
					logError(e);
					return true;
				};
	private Function<K, E> transformer;
	private Consumer<E> consumer;
	private Consumer<ManagedBatchTransactionExecutor<K,E>> afterClearSession;
	private Consumer<ManagedBatchTransactionExecutor<K,E>> afterFinish;
	private Consumer<ManagedBatchTransactionExecutor<K,E>> afterCommit;
	private BiConsumer<ManagedBatchTransactionExecutor<K,E>,Set<K>> onCommitRequired;
	private Consumer<ManagedBatchTransactionExecutor<K,E>> beforeCommit;
	private Consumer<ManagedBatchTransactionExecutor<K,E>> afterRollback;
	private Consumer<ManagedBatchTransactionExecutor<K,E>> beforeFlush;
	private Consumer<ManagedBatchTransactionExecutor<K,E>> afterFlush;
	private Predicate<K> acceptKey = (k) -> true;
	private Predicate<E> acceptElement = (o) -> true;
	private Predicate<ManagedBatchTransactionExecutor<K,E>> shouldStop = (o) -> Thread.interrupted() || Lifecycle.isDestroying() || !Lifecycle.isApplicationInitialized();
	private Collection<K> collection;
	private volatile transient UserTransaction tx;
	private boolean shouldKeepInitialTransactionState = true;
	
	private final Map<K,Integer> keysPending = new HashMap<>();
	private final Set<K> keysToCommit = new HashSet<>();

	private boolean shouldSetupFlushOnCommit;	
	private boolean neverClearFlag;
	
	private static final int INITIAL_TRANSACTION_STATUS = -666;
	private volatile transient int initialTransactionStatus = INITIAL_TRANSACTION_STATUS;
	private volatile transient boolean processingKeysPending;
	private volatile transient int idxKeyPending;
	
	private volatile transient Instant startInstant, endInstant, lastFlush, lastCommit;
	
	private volatile transient int numTotal, idxCurrent, numChanges;
	private volatile transient int commitCounter, rollbackCounter, otherTransactionStatusCounter;
	
	private volatile transient K currentKey;
	private volatile transient E currentElement;
	
	private volatile transient boolean running, finishing, finished;
	
	private Integer maxTimeInSecsBeforeFlush;
	private Integer maxElementsBeforeFlush;
	
	private int maxTimeInSecsBeforeCommit = 120;
	private int maxElementsBeforeCommit = 300;
	
	private String operationName;
	private String keyName = "id";
	private String elemName = "elemento";
	private boolean avoidFlush = true;
	private boolean shouldManageJbpmSession = true;

	public ManagedBatchTransactionExecutor(String name) {
		this.operationName = name;
	}
	
	public ManagedBatchTransactionExecutor<K,E> supplier(Supplier<Collection<K>> collectionSupplier) {
		this.collectionSupplier = collectionSupplier;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> supplier(Collection<K> collectionSupplier) {
		this.collectionSupplier = collectionSupplier==null ? null : ()->collectionSupplier;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> supplier(K[] collectionSupplier) {
		this.collectionSupplier = collectionSupplier==null ? null : ()->Arrays.asList(collectionSupplier);
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> nameKey(String name) {
		this.keyName = name;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> nameElem(String name) {
		this.elemName = name;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> transform(Function<K,E> transformer) {
		this.transformer = transformer;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> consume(Consumer<E> consumer) {
		this.consumer = consumer;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> handleElementException(BiFunction<ManagedBatchTransactionExecutor<K,E>,Exception,Boolean> handleException) {
		this.handleElementException = handleException;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> handleElementException(BiConsumer<ManagedBatchTransactionExecutor<K,E>,Exception> handleException) {
		this.handleElementException = (mbt,ex)->{
			try {
				if (handleException==null) {
					logError(ex);
					return true;
				}
				handleException.accept(mbt, ex);
			} catch (Exception e) {
				logError(e);
				return true;
			}
			return false;
		};
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> acceptKey(Predicate<K> acceptor) {
		this.acceptKey = acceptor;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> acceptElement(Predicate<E> acceptor) {
		this.acceptElement = acceptor;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> shouldInterrupt(Predicate<ManagedBatchTransactionExecutor<K,E>> interruptor) {
		this.shouldStop = interruptor;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> operationName(String name) {
		this.operationName = name;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> shouldSetupFlushOnCommit() {
		this.shouldSetupFlushOnCommit = true;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> doesntMatterFinalTransactionState() {
		this.shouldKeepInitialTransactionState = false;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> maxTimeInSecsBeforeFlush(int value) {
		this.maxTimeInSecsBeforeFlush = value;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> afterClearSession(Consumer<ManagedBatchTransactionExecutor<K,E>> consumer) {
		this.afterClearSession = consumer;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> afterFinish(Consumer<ManagedBatchTransactionExecutor<K,E>> consumer) {
		this.afterFinish = consumer;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> afterClearSession(Runnable runnable) {
		this.afterClearSession = (runnable==null) ? null : new Consumer<ManagedBatchTransactionExecutor<K, E>>() {
			public void accept(ManagedBatchTransactionExecutor<K, E> man) {
				runnable.run();
			}
		};
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> afterFinish(Runnable runnable) {
		this.afterFinish = (runnable==null) ? null : new Consumer<ManagedBatchTransactionExecutor<K, E>>() {
			public void accept(ManagedBatchTransactionExecutor<K, E> man) {
				runnable.run();
			}
		};
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> onCommitRequired(BiConsumer<ManagedBatchTransactionExecutor<K,E>,Set<K>> consumer) {
		this.onCommitRequired = consumer;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> beforeCommit(Consumer<ManagedBatchTransactionExecutor<K,E>> consumer) {
		this.beforeCommit = consumer;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> afterCommit(Consumer<ManagedBatchTransactionExecutor<K,E>> consumer) {
		this.afterCommit = consumer;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> beforeFlush(Consumer<ManagedBatchTransactionExecutor<K,E>> consumer) {
		this.beforeFlush = consumer;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> afterFlush(Consumer<ManagedBatchTransactionExecutor<K,E>> consumer) {
		this.afterFlush = consumer;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> afterRollback(Consumer<ManagedBatchTransactionExecutor<K,E>> consumer) {
		this.afterRollback = consumer;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> maxElementsBeforeFlush(int value) {
		//this.maxElementsBeforeCommit = value;
		//return this;
		return maxElementsBeforeCommit(value); //TODO investigar por que está ocorrendo problema quando o valor do flush é diferente do padrão.
	}
	
	public ManagedBatchTransactionExecutor<K,E> maxTimeInSecsBeforeCommit(int value) {
		this.maxTimeInSecsBeforeCommit = value;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> maxElementsBeforeCommit(int value) {
		this.maxElementsBeforeCommit = value;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> neverClearSession() {
		this.neverClearFlag = true;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> dontManageJbpmSession() {
		this.shouldManageJbpmSession = false;
		return this;
	}
	
	public ManagedBatchTransactionExecutor<K,E> allowNonManagedFlush() {
		this.avoidFlush = false;
		return this;
	}
	
	public Instant getStartInstant() {
		return startInstant;
	}
	
	public Instant getEndInstant() {
		return endInstant;
	}
	
	public K getCurrentKey() {
		return currentKey;
	}
	
	public E getCurrentElement() {
		return currentElement;
	}
	
	public int getCurrentIndex() {
		return idxCurrent;
	}
	
	public int getTotal() {
		return numTotal;
	}
	
	protected void startExec() {
		startInstant = Instant.now();

		tx = Transaction.instance();
		tryBeginTrans(0);
	
		setupAvoidFlush();
		
		lastFlush = startInstant;
		lastCommit = startInstant;
	}
	
	protected void setupFinalTransactionState() {
		if (shouldKeepInitialTransactionState) {
			try {
				int status = tx.getStatus();
				if (status==initialTransactionStatus)
					return;
				
				switch (initialTransactionStatus) {
					case Status.STATUS_UNKNOWN:
					case Status.STATUS_ACTIVE:
						tryBeginTrans(0);
						break;
					case Status.STATUS_MARKED_ROLLBACK:
						tryBeginTrans(0);
						tx.setRollbackOnly();
						break;
					default:
						if (tx.isActive()) 
							tx.commit();
				}
			} catch (Exception ex) {
				logError(ex);
			}
		}
	}
	
	protected void endExec() {
		finishing = true;
		try {
			setupAllowFlush();

			try {
				ensureTransaction();
			} catch (Exception ex) {
				logError("endExec", ex);
			}

			endInstant = Instant.now();

			if (afterFinish!=null) {
				afterFinish.accept(this);
				try {
					tx.commit();
				} catch (Exception ex) {
					logError(ex);
				}
			}
		} finally {			
			finished = true;
			setupFinalTransactionState();
			finishing = false;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected E transform(K k) {
		E o;
		if (transformer!=null)
			o = transformer.apply(k);
		else
			o = (E)k;
		return o;
	}
	
	protected void change(E o) {
		consumer.accept(o);
		numChanges++;
	}
	
	public int getMaxTimeInSecsBeforeFlush() {
		return maxTimeInSecsBeforeFlush==null ? maxTimeInSecsBeforeCommit : maxTimeInSecsBeforeFlush;
	}
	
	public int getMaxElementsBeforeFlush() {
		return maxElementsBeforeFlush==null ? maxElementsBeforeCommit : maxElementsBeforeFlush;
	}
	
	private boolean isFlushNeeded() {
		return (Duration.between(lastFlush, Instant.now()).getSeconds()>getMaxTimeInSecsBeforeFlush()) 
				|| ((idxCurrent % getMaxElementsBeforeFlush())==0);
	}

	private boolean isCommitNeeded() {
		return (Duration.between(lastCommit, Instant.now()).getSeconds()>maxTimeInSecsBeforeCommit) 
				|| ((idxCurrent % maxElementsBeforeCommit)==0);
	}
	
	protected void doRollback() throws Exception {
		logError("A operação foi instruída a realizar o rollback.");
		tx.rollback(); //TODO Quando o rollback é realizado, deveria informar quais foram os elementos que foram desfeitos.
	}
	
	protected EntityManager getEntityManager() {
		EntityManager em = EntityUtil.getEntityManager();
		if (shouldSetupFlushOnCommit) {
			boolean flushOnCommit = FlushModeType.COMMIT.equals(em.getFlushMode());
			if (!flushOnCommit) {
				em.setFlushMode(FlushModeType.COMMIT);
			}
		}
		return em;
	}
	
	private void beginTrans() throws NotSupportedException, SystemException {
//		UserTransaction ut = Transaction.instance();
//		if (ut==tx) {
//			logInfo("setupTrans: found same transaction instance.");
//		}		
		if (tx==null) {
			tx = Transaction.instance();
		}
		tx.begin();
		
		tx.registerSynchronization(new Synchronization() {
			@Override
			public void beforeCompletion() {
				if (!running || finishing || finished)
					return;
				try {
					log.debug("Transaction.beforeCompletion: Status=" + tx.getStatus());
					try {
						if (beforeCommit!=null) {
							beforeCommit.accept(ManagedBatchTransactionExecutor.this);
						}
					} catch (Exception ex) {
						logError(ex);
						tx.setRollbackOnly();
						throw ex;
					}
				} catch (SystemException ex) {
					logError(ex);
				}
			}

			@Override
			public void afterCompletion(int status) { //TODO Usar isso para sincronizar o ManagedBatchTransaction para o caso de algo alterar manualmente a transação
				if (!running || finishing || finished)
					return;
				log.debug("Transaction.afterCompletion: Status=" + status);
				try {
				
					switch (status) {
						case Status.STATUS_COMMITTED:
						case Status.STATUS_COMMITTING:
							commitCounter++;
							if (afterCommit!=null)
								afterCommit.accept(ManagedBatchTransactionExecutor.this);
							break;

						case Status.STATUS_ROLLEDBACK:
						case Status.STATUS_ROLLING_BACK:
							rollbackCounter++;
							if (afterRollback!=null)
								afterRollback.accept(ManagedBatchTransactionExecutor.this);
							break;
							
						default:
							otherTransactionStatusCounter++;
					}
				} catch (Exception ex) {
					logError(ex);
				}
			}
			
		});
	}
	
	private int waitForTransStatusChange(int curStatus, int timeout) {
		long startTime = System.currentTimeMillis();
		int status = -666;
		try {
			do {
				Thread.yield();
				status = tx.getStatus();
				if (status!=curStatus) 
					return status;
				long elapsed = (System.currentTimeMillis()-startTime);
				long waitTime = timeout-elapsed;
				if (waitTime>0)
					Thread.sleep(Math.min(waitTime, 20));
			} while ((System.currentTimeMillis()-startTime)<timeout);
		} catch (InterruptedException | SystemException ex) {
			logError(ex);
		}
		return status;
	}
	
	private boolean tryBeginTrans(int tryCounter) {
		if (tryCounter>4)
			throw new IllegalStateException("Failed to setup transaction!");
		
		try {
			for (int idxState=0; idxState<4; idxState++) {
				
				int status = tx.getStatus();
				if (initialTransactionStatus==INITIAL_TRANSACTION_STATUS) {
					initialTransactionStatus = status;
				}
				log.debug("tryBeginTrans: Transaction.Status=" + status);
				
				switch (status) {
					case Status.STATUS_ACTIVE:
						log.debug("Transaction is already active");
						return false;
					case Status.STATUS_MARKED_ROLLBACK:
						tx.rollback();
						break;
					case Status.STATUS_ROLLING_BACK:
					case Status.STATUS_COMMITTING:
						waitForTransStatusChange(status,1000);
						beginTrans();
						return true;
					case Status.STATUS_ROLLEDBACK:
					case Status.STATUS_COMMITTED:
						waitForTransStatusChange(status,100);
						beginTrans();
						return true;
					case Status.STATUS_PREPARING:
						waitForTransStatusChange(status,200);
						beginTrans();
						return true;
					case Status.STATUS_PREPARED:
						waitForTransStatusChange(status,2000);
						beginTrans();
						return true;
					case Status.STATUS_NO_TRANSACTION:
						beginTrans();
						return true;
					case Status.STATUS_UNKNOWN:
						log.debug("Transaction status is unkown");
						switch (idxState) {
							case 0:
								beginTrans();
								return true;
							case 1:
								tx.commit();
								break;
							case 2:
								tx.rollback();
								break;
							case 3:
								tx = Transaction.instance();
								return tryBeginTrans(tryCounter+1);
						}
						break;
				}
			}
			
		} catch (IllegalStateException | SecurityException | HeuristicRollbackException |HeuristicMixedException | RollbackException | NotSupportedException | SystemException ex) {
			try {				
				logError("tryBeginTrans: Transaction.Status=" + tx.getStatus(), ex);

				return tryBeginTrans(tryCounter+1);
			} catch (Exception exx) {
				log.error("tryBeginTrans: Cannot start a transaction again!!!", exx);
				tx = Transaction.instance();
				return tryBeginTrans(tryCounter+1);
			}
		}
		return false;
	}

	protected boolean ensureTransaction() throws Exception {
		boolean reset;
		
		reset = !tx.isActive();
		
		if (reset) {
			tryBeginTrans(0);
			EntityManager em = getEntityManager();
			em.joinTransaction(); //TODO Conferir o caso onde o EntityManager já está participando de uma transação, que não a nossa. Isso, em tese, nunca deveria ocorrer...
			//clearSession();
			keysToCommit.clear();
		}
		
		return !reset;
	}
	
	private void setupAvoidFlush() {
		if (avoidFlush){
			Context context = this.getContext();
			if (context!=null) {
				context.set("pje:ignoreFlush", Boolean.TRUE);
			} else {
				logError("setupAvoidFlush: Failed to acquire any valid Seam Context.");
			}
		}
	}

	private void setupAllowFlush() {
		if (avoidFlush) {
			Context context = this.getContext();
			if (context!=null)
				context.remove("pje:ignoreFlush");
		}
	}

	private Context getContext() {
		Context context = Contexts.getEventContext();
		if (context!=null)
			return context;
		return null;
	}

	protected void doCommit(boolean forceCommit) throws Exception {
		boolean holdSession = true;

		EntityManager em = getEntityManager();
		
		try {
			if (tx.isMarkedRollback()) {
				doRollback();

			} else if (tx.isActive()) {
				boolean commitNeeded = forceCommit || isCommitNeeded();

				if (commitNeeded || isFlushNeeded()) {
					boolean flushOnCommit = FlushModeType.COMMIT.equals(em.getFlushMode());

					if (!commitNeeded || !flushOnCommit)
						flushSession();

					if (commitNeeded) {
						if (onCommitRequired!=null) {
							onCommitRequired.accept(this, Collections.unmodifiableSet(keysToCommit));
						}
						logInfo("Será realizado o commit.");
						setupAllowFlush();
						try {
							flushJbpm();
							if (!tx.isActive())
								throw new IllegalStateException("Uma transação é requerida!");
							tx.commit();
							holdSession = false;
						} finally {
							setupAvoidFlush();
						}
						lastCommit = Instant.now();
						if (flushOnCommit)
							lastFlush = lastCommit;

						keysToCommit.forEach(keysPending::remove);
						keysToCommit.clear();
					}				
				}
			}
			
		} catch (Exception ex) {
			logError(ex);
			if (tx.isMarkedRollback()) 
				doRollback();
			
		} finally {
			if (!holdSession) {//TODO talvez o clear devesse estar junto de flushSession...
				assert em.isOpen() : "Em nenhuma situação o entitymanager deveria estar fechado!";
				ensureTransaction(); //Jbpm exige uma transação...
				clearSession();
			}		
		}
	}
	
	protected void flushSession() {
		EntityManager em = getEntityManager();
		setupAllowFlush();
		try {
			if (beforeFlush!=null)
				beforeFlush.accept(this);
			em.flush();
			if (afterFlush!=null)
				afterFlush.accept(this);
		} finally {
			setupAvoidFlush();
		}
		lastFlush = Instant.now();
	}
	
	protected JbpmContext getJbpmContext() {
		if ( !Contexts.isEventContextActive() )
			return null;
		if (tx==null)
			return null;
		
		JbpmContext jbpmContext;
		try {
			jbpmContext = tx.isActiveOrMarkedRollback() ? (JbpmContext) Component.getInstance( Component.getComponentName(ManagedJbpmContext.class), ScopeType.EVENT, false, false) : null;
		} catch (Exception ex) {
			jbpmContext = null;
		}
		
		if (JbpmConfiguration.hasInstance("jbpm.cfg.xml")) {
			JbpmConfiguration jbpmConfig = JbpmConfiguration.getInstance();
			if (!jbpmConfig.isClosed() && jbpmConfig.getJobExecutor().isStarted()) {
				jbpmContext = jbpmConfig.getCurrentJbpmContext();
			}				
		}
		
		return jbpmContext;
	}
	
	protected void flushJbpm() throws SystemException {
		if (shouldManageJbpmSession) {
			JbpmContext jbpmContext = getJbpmContext();
			if (jbpmContext!=null) {
				if (tx.isActive()) {
					JbpmUtil.saveFlushAndClear(jbpmContext, false);
				}
			}
		}
	}
	
	protected void clearSession() {
		if (neverClearFlag) //TODO se a transação for fechada (por commit ou rollback, nosso, ou não), pode ser, sim, necessário limpar o Entitymanager. Ver esse caso.
			return;
		
		EntityManager em = getEntityManager();
		em.clear();
		
		if (shouldManageJbpmSession) {
			try {
				tryBeginTrans(0);
			} catch (Exception ex) {
				logError(ex);
			}
			JbpmContext jbpmContext = getJbpmContext();
			if (jbpmContext!=null) {
				JbpmUtil.clearWithoutClose(jbpmContext);
			}
		}
		
		if (afterClearSession!=null)
			afterClearSession.accept(this);
	}
	
	protected void checkShouldStop() throws InterruptException, IllegalStateException, SystemException {
		if (shouldStop.test(this)) { //TODO mwborges Talvez em caso de InterruptedException ou Thread.isInterrupted() deveríamos também abortar a execução.
			if ((tx!=null) && tx.isActive())
				tx.setRollbackOnly();
			throw new InterruptException();
		}
	}
	
	public void run() {		
		if (running)
			throw new RuntimeException("Operação em lote já está em execução.");
		running = true;
		
		collection = null;
		currentElement = null;
		currentKey = null;
		numTotal = -1;
		idxCurrent = 0;
		numChanges = 0;
		
		try {			
			startExec();
			
			ensureTransaction();
			checkShouldStop();
			
			collection = collectionSupplier.get();
			checkShouldStop();
			
			numTotal = collection==null ? 0 : collection.size();
			logInfo("Os elementos da operação foram obtidos: " + numTotal);
			
			if (collection==null) 
				return;

			checkShouldStop();
			
			for (K k: collection) { //TODO Talvez seja melhor substituir isso por um iterator, pois assim poderá melhorarar o gerenciamento da transação. Por exemplo, no caso onde um erro de transação ocorre ao se tentar recuperar o elemento da coleção.
				idxCurrent++;
				currentKey = k;
				currentElement = null;

				checkShouldStop();

				if (k==null)
					continue;
				
				keysToCommit.add(k);
				keysPending.put(k, idxCurrent);

				try {						
					ensureTransaction();
					if (!acceptKey.test(k)) {
						keysToCommit.remove(k);
						keysPending.remove(k);
						continue;
					}

					ensureTransaction();
					E o = transform(k);

					if (!ensureTransaction()) {
						keysToCommit.remove(k);
						continue;
					}
					if (!acceptElement.test(currentElement = o)) {
						keysToCommit.remove(k);
						keysPending.remove(k);
						continue;
					}

					if (!ensureTransaction()) {
						keysToCommit.remove(k);
						continue;				
					}
					checkShouldStop();

					change(o);

					doCommit(false);
				} catch (InterruptedException ex) {
					logError(ex);
					throw ex;
				} catch (Exception ex) {
					keysToCommit.remove(k);
					if (!handleElementException.apply(this, ex))//Se retornar true, ele tentará novamente.
						keysPending.remove(k);
					if (ex instanceof InterruptException)
						throw ex;
				}
			}

			checkShouldStop();
			try {
				doCommit(true);
			} catch (InterruptedException ex) {
				logError(ex);
				throw ex;
			} catch (Exception ex) {
				logError(ex);
				if (ex instanceof InterruptException)
					throw ex;
			}

			checkShouldStop();
			if (!keysPending.isEmpty()) {
				processingKeysPending = true;

				final int bkpCurrIndex = idxCurrent;
				final K bkpCurrentKey = currentKey;
				final E bkpCurrentElem = currentElement;					

				logInfo("Iremos processar individualmente " + keysPending.size() + " elementos que restaram pendentes por decorrência de erro de processamento.");
				for (Map.Entry<K,Integer> entry: keysPending.entrySet()) {
					final K k = entry.getKey();
					idxCurrent = entry.getValue();
					currentKey = k;
					currentElement = null;
					idxKeyPending++;
					try {
						checkShouldStop();
						ensureTransaction();
						if (!acceptKey.test(k))
							continue;

						ensureTransaction();
						E o = transform(k);

						if (!ensureTransaction())
							continue;
						if (!acceptElement.test(currentElement = o))
							continue;

						if (!ensureTransaction())
							continue;						
						checkShouldStop();
						change(o);

						doCommit(true);
					} catch (InterruptException ex) {
						break;
					} catch (InterruptedException ex) {
						logError(ex);
						throw ex;
					} catch (Exception ex) {
						if (handleElementException.apply(this, ex))
							logInfo("O elemento " + currentKey + " não será processado novamente.");
					}
				}
				idxCurrent = bkpCurrIndex;
				currentKey = bkpCurrentKey;
				currentElement = bkpCurrentElem;

				keysPending.clear();
			}						
			
			ensureTransaction();//Jbpm exige um transação aberta para poder retornar seu entitymanager
			clearSession();
			
		} catch (Exception ex) {
			try {
				if ((tx!=null) && tx.isActiveOrMarkedRollback()) //TODO verificar se o fluxo do gerenciamento da transação está realmente correto.
					tx.rollback();
			} catch (IllegalStateException | SecurityException | SystemException e) {
				logError("Erro ao realizar rollback.", e);
			}
			if (ex instanceof InterruptException) {
				logInfo("Processamento interrompido");
			} else {
				logError("Erro ao executar operação em lote.", ex);			
				throw new PJeRuntimeException(String.format("Erro ao executar opera\u00e7\u00e3o %s em lote.", operationName), ex);
			}
		} finally {		 
			try {
				endExec();
				logInfo("Fim da operação");
			} finally {
				running = false;			
			}
		}
	}
	
	private void logError(Object msg, Throwable ex) {
		if ((ex==null) && (msg instanceof Throwable)) {
			ex = (Throwable) msg;
		}
		msg += "\nStatus da Operação: " + this;

		if (ex!=null)
			log.error(msg, ex);
		else
			log.error(msg);
	}
	
	private void logInfo(Object msg) {
		msg += "\nStatus da Operação: " + this;
		
		log.info(msg);
	}
	
	private void logError(Object msg) {
		logError(msg, null);
	}
	
	@Override
	public String toString() {		
		StringBuilder sb = new StringBuilder(1000);
		
		Instant untilLeast = (endInstant==null) ? Instant.now() : endInstant;
		Duration elapsed = Duration.between(startInstant, untilLeast);
		
		sb.append(operationName)
				.append(" gastou ").append(elapsed)
				.append(" (").append(idxCurrent).append(" de ").append(numTotal).append(")");
				
		if (isRunning()) {
			sb.append(" atualmente processando ").append(keyName).append("=").append(currentKey);
			if (!Objects.equals(currentKey, currentElement))
				sb.append(" que corresponde a ").append(elemName).append("=").append(currentElement);
		} else {
			sb.append(" encerrada");
		}
		
		if (processingKeysPending) 
			return sb.append(". Execução individual ").append(idxKeyPending).append(" de ").append(keysPending.size()).toString();
		
		long elapsedSecs = elapsed.getSeconds();
		
		if (numChanges>0)
			sb.append(" com ").append(numChanges).append(" possíveis alterações ");
				
		if (elapsedSecs>0) {
			NumberFormat numberFormat = NumberFormat.getNumberInstance();
			numberFormat.setMaximumFractionDigits(1);
			numberFormat.setMinimumFractionDigits(1);
			double elementsPerSec = numChanges/(double)elapsedSecs;
			
			sb.append(" (").append(numberFormat.format(elementsPerSec)).append(" ").append(keyName).append("/s)");
			if (endInstant==null) {
				long estimatedTotalTime = (long)Math.floor(numTotal/elementsPerSec);
				long eat = estimatedTotalTime-elapsedSecs;
				sb.append(" (Tempo restante estimado: ").append(Duration.ofSeconds(eat)).append(")");
			}
		}
		
		return sb.toString();
	}

	public boolean isRunning() {
		return running;
	}
	
	public int getRollbackCounter() {
		return rollbackCounter;
	}

	public int getCommitCounter() {
		return commitCounter;
	}
	
	public static class InterruptException extends RuntimeException {
		public InterruptException() {
			super();
		}
		public InterruptException(Throwable ex) {
			super(ex);
		}
		public InterruptException(String msg, Throwable ex) {
			super(msg, ex);
		}
	}
}
