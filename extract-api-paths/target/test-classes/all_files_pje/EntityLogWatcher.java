package br.com.infox.ibpm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.Events;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.enums.TipoOperacaoLogEnum;

@Name(EntityLogWatcher.NAME)
@Scope(ScopeType.APPLICATION)
@Startup(depends = {ParametroUtil.NAME, CarregarParametrosAplicacao.NAME})
public class EntityLogWatcher {

	public static final String NAME = "logChecker";

	private static final Object SYNC_LOG_CHECKER = "Sync" + NAME + "_" + System.currentTimeMillis();
	private static ThreadLogChecker thread = new ThreadLogChecker();
	private static volatile transient int holdingThreadCounter;
	public static final String ULTIMAS_ENTIDADES_MODIFICADAS = "br.com.infox.ibpm.util.ultimasEntidadesModificadas";

	@Create
	public void init() {
		synchronized (SYNC_LOG_CHECKER) {
			if ((holdingThreadCounter++==0)) {
				assert Thread.State.NEW.equals(thread.getState());
				thread.start();
			}
		}
	}

	@Destroy
	public void finish() {
		synchronized (SYNC_LOG_CHECKER) {
			if (--holdingThreadCounter==0) {
				if (thread != null) {
					try {
						thread.terminate();
					} catch (InterruptedException ex) {
						//
					}
					thread = null;
				}
			}
		}
	}
}

class ThreadLogChecker extends Thread {

	private static final Logger logger = Logger.getLogger(EntityLogWatcher.class.getName());
	private static final int WAIT_TIME = 10000;
	private volatile boolean finishing, finished, running, terminating;
	private boolean firstTime;
	private long lastIdLog;

	private final Object SYNC_OBJECT = new Object() {
		@Override
		public String toString() {
			return "SYNC_OBJECT_" + ThreadLogChecker.class.getSimpleName();
		}
	};

	ThreadLogChecker() {
		super(ThreadLogChecker.class.getSimpleName());
	}

	private boolean canExecute() {
		return (running && !(finishing || finished || terminating));
	}

	private static final String SQL_ENTITYLOG = "SELECT id_log, ds_entidade, ds_id_entidade, tp_operacao, ds_package FROM tb_log WHERE id_log>:idLog ORDER BY id_log";
	private static final int SQL_ENTITYLOG_ID_LOG = 0;
	private static final int SQL_ENTITYLOG_DS_ENTIDADE = 1;
	private static final int SQL_ENTITYLOG_DS_ID_ENTIDADE = 2;
	private static final int SQL_ENTITYLOG_TP_OPERACAO = 3;
	private static final int SQL_ENTITYLOG_DS_PACKAGE = 4;

	@SuppressWarnings("rawtypes")
	private void execute() throws InterruptedException {
		if (Lifecycle.isDestroying()) {
			this.finishing = true;
			return;
		}

		if (firstTime) {
			sleep(10000);
			logger.info("[ThreadLogChecker] Observador de log iniciado.");
		}

		if (Lifecycle.isDestroying()) {
			this.finishing = true;
			return;
		}

		if (!Lifecycle.isApplicationInitialized()) {
			return;
		}

		Lifecycle.beginCall();
		List<EntityLog> logs = new ArrayList<>();
		try {
			EntityManager emLog;
			try {
				emLog = (EntityManager) Component.getInstance("entityManagerLog");
			} catch (IllegalArgumentException ex) {
				return;
			}

			if (firstTime) {
				Number maxIdLog = EntityUtil.getSingleResult(emLog, "select max(o.idLog) from EntityLog o");
				lastIdLog = (maxIdLog==null) ? 0 : maxIdLog.longValue();
				firstTime = false;
				return;
			}
			
			Query qry = EntityUtil.createNativeQuery(emLog, SQL_ENTITYLOG)
					.setHint("org.hibernate.cacheable", Boolean.FALSE)
					.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
					.setHint("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS)
					.setParameter("idLog", lastIdLog);
			List list = qry.getResultList();
			for (Object reg : list) {
				final Object[] log = (Object[]) reg;
				final Number idLog = (Number) log[SQL_ENTITYLOG_ID_LOG];
			
				lastIdLog = idLog.longValue();
				final Character tpOperacao = (Character) log[SQL_ENTITYLOG_TP_OPERACAO];
				final String entityName = (String) log[SQL_ENTITYLOG_DS_ENTIDADE];
				final String packageName = (String) log[SQL_ENTITYLOG_DS_PACKAGE];
				Object idEntity = log[SQL_ENTITYLOG_DS_ID_ENTIDADE];
				
				EntityLog entityLog = new EntityLog();
				entityLog.setIdLog(idLog.longValue());
				entityLog.setIdEntidade((String)idEntity);
				entityLog.setNomeEntidade(entityName);
				entityLog.setNomePackage(packageName);
				entityLog.setTipoOperacao(TipoOperacaoLogEnum.valueOf(tpOperacao.toString()));
				logs.add(entityLog);
			}
			emLog.clear();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (!logs.isEmpty()) {
				Events.instance().raiseEvent(EntityLogWatcher.ULTIMAS_ENTIDADES_MODIFICADAS, logs);
			}
			Lifecycle.endCall();
		}
	}
	
	@Override
	public void run() {
		if (running || finishing || finished) {
			throw new IllegalStateException("Já foi executado!");
		}
		running = true;
		try {
			this.firstTime = true;

			while (canExecute()) {
				try {
					synchronized (SYNC_OBJECT) {
						if (canExecute()) {
							execute();
						}
						if (canExecute()) {
							SYNC_OBJECT.wait(WAIT_TIME);
						}
					}
				} catch (InterruptedException ex) {
					//It's okay
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (terminating) {
					finishing = true;
				}
			}

		} finally {
			finished = true;
			running = false;
			finishing = false;
			
			logger.info("[ThreadLogChecker] Observador de log finalizado.");
		}
	}

	public void terminate() throws InterruptedException {
		logger.info("[ThreadLogChecker] Tentando finalizar o observador de log...");
		this.terminating = true;
		interrupt();
		if (this.running && !finishing) {
			join();
		}
	}

}
