package br.com.infox.listener;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.event.spi.PostCollectionRecreateEvent;
import org.hibernate.event.spi.PostCollectionRecreateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreCollectionRemoveEvent;
import org.hibernate.event.spi.PreCollectionRemoveEventListener;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;

import br.com.infox.ibpm.entity.log.LogException;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.auditoria.LogLoadEvent;
import br.com.itx.util.EntityUtil;

public class LogEventListener implements PostUpdateEventListener, PostInsertEventListener, PostDeleteEventListener,
		PostCollectionRecreateEventListener, PreCollectionUpdateEventListener, PreCollectionRemoveEventListener {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(LogEventListener.class);
	private static String ENABLE_LOG_VAR_NAME = "executeLog";
	private static final AtomicInteger disableLogCounter = new AtomicInteger();

	private static String getIp(final HttpServletRequest request) {
		String ip = null;

		try {
			ip = LogUtil.getIpRequest(request);
		} catch (LogException e) {
			e.printStackTrace();
		}

		return ip;
	}

	private static Integer getIdUsuario() {
		Context context = Contexts.getSessionContext();
		if (context == null) {
			context = Contexts.getEventContext();
		}
		if (context==null)
			return null;
		return Authenticator.getIdUsuarioLogado();
	}

	private static String getPagina(final HttpServletRequest request) {
		String pagina = null;
		try {
			pagina = LogUtil.getUrlRequest(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pagina;
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		if (LogUtil.isLogable(event.getEntity()) && isLogEnabled() && !LogUtil.isRequisicaoIntercomunicacaoRest()) {
			final HttpServletRequest request = LogUtil.getRequest();
			carregarPropriedadesLazy(event.getOldState(), event.getState(), event.getPersister().getClassMetadata().getPropertyNames());
			final String[] properties = event.getPersister().getClassMetadata().getPropertyNames();
			final Class<?> clazz = event.getEntity().getClass();
			Object id = EntityUtil.getEntityIdObject(event.getEntity());
			Events.instance().raiseAsynchronousEvent(LogLoadEvent.UPDATE_EVENT_NAME,
					clazz,
					id,
					this.parseState(event.getOldState(), clazz, properties),
					this.parseState(event.getState(), clazz, properties),
					properties,
					getIdUsuario(),
					getIp(request),
					getPagina(request));
		}
	}

	@Override
	public void onPostInsert(PostInsertEvent event) {
		if (LogUtil.isLogable(event.getEntity()) && isLogEnabled() && !LogUtil.isRequisicaoIntercomunicacaoRest()) {
			final HttpServletRequest request = LogUtil.getRequest();
			final String[] properties = event.getPersister().getClassMetadata().getPropertyNames();
			final Class<?> clazz = event.getEntity().getClass();
			Object id = EntityUtil.getEntityIdObject(event.getEntity());
			Events.instance().raiseAsynchronousEvent(LogLoadEvent.INSERT_EVENT_NAME,
					clazz,
					id,
					this.parseState(event.getState(), clazz, properties),
					properties,
					getIdUsuario(),
					getIp(request),
					getPagina(request));
		}
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		if(LogUtil.isLogable(event.getEntity()) && isLogEnabled()) {
			final HttpServletRequest request = LogUtil.getRequest();
			carregarPropriedadesLazy(event.getDeletedState(), null, event.getPersister().getClassMetadata().getPropertyNames());
			final String[] properties = event.getPersister().getClassMetadata().getPropertyNames();
			final Class<?> clazz = event.getEntity().getClass();
			Object id = EntityUtil.getEntityIdObject(event.getEntity());
			Events.instance().raiseAsynchronousEvent(LogLoadEvent.DELETE_EVENT_NAME, 
					clazz, 
					id,
					this.parseState(event.getDeletedState(), clazz, properties),
					properties,
					getIdUsuario(),
					getIp(request),
					getPagina(request));
		}
	}
	
	private void carregarPropriedadesLazy(Object[] oldState, Object[] state, String[] propertyNames) {
		for(int i = 0; i < propertyNames.length; i++){
			if(state == null && oldState != null) {
				LogUtil.toStringForLogWithCatchNullPointer(oldState[i], propertyNames[i]);
			} else if(oldState != null && state != null){
				LogUtil.compareObj(oldState[i], state[i]);
			}
		}

	}

	public static void disableLogForEvent() {
		synchronized (disableLogCounter) {
			disableLogCounter.incrementAndGet();
			Contexts.getEventContext().set(ENABLE_LOG_VAR_NAME, "false");
		}
	}

	public static void enableLog() {
		synchronized (disableLogCounter) {
			if (disableLogCounter.decrementAndGet() < 0) {
				disableLogCounter.set(0);
			}
			Contexts.getEventContext().set(ENABLE_LOG_VAR_NAME, "true");
		}
	}

	@Override
	public void onPostRecreateCollection(PostCollectionRecreateEvent arg0) {

	}

	@Override
	public void onPreUpdateCollection(PreCollectionUpdateEvent arg0) {

	}

	@Override
	public void onPreRemoveCollection(PreCollectionRemoveEvent arg0) {

	}

	private final boolean isLogEnabled() {
		if (disableLogCounter.get() == 0) {
			return true; 
		}
		final Context eventContext = Contexts.getEventContext();
		if (eventContext == null) {
			return false;
		}
		final Object test = eventContext.get(ENABLE_LOG_VAR_NAME);
		if (test == null) {
			return true;
		}
		if (test instanceof Boolean) {
			return (Boolean) test;
		}
		try {
			return test.toString().equalsIgnoreCase("true");
		} catch (Exception e) {
			return true;
		}
	}

	private Object[] parseState(Object[] state, Class<?> clazz, String[] properties) {
		Object[] result = null;
		try {
			if (state != null) {
				result = new Object[state.length];
				for (int i=0; i<state.length; i++) {
					String name = properties[i];
					Object value = LogUtil.toStringForLog(clazz, name, state[i]);
					result[i] = value;
				}
			}
		} catch(Exception ex) {
			logger.warn("[LogEventListener] Erro ao criar log de auditoria, entidade: " + clazz.getSimpleName(), ex);
		}
		return result;
	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		// TODO Auto-generated method stub
		return false;
	}

}