package br.com.infox.cliente.component.signfile;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;
import org.quartz.SchedulerException;

import br.com.infox.ibpm.service.LogService;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.acesso.HashSession;

/**
 * 
 * @author rodrigo
 * 
 */
@Name(RemoveHashSessionExpiradosProcessor.NAME)
@AutoCreate
public class RemoveHashSessionExpiradosProcessor implements Serializable{

	@In
	private LogService logService;
	@Logger
	private Log log;

	private static final long serialVersionUID = 1L;

	public final static String NAME = "removeHashSessionExpiradosProcessor";

	private EntityManager entityManager;
	
	public EntityManager getEntityManager(){
		if (entityManager == null){
			entityManager = EntityUtil.getEntityManager();
		}
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager){
		this.entityManager = entityManager;
	}

	public static RemoveHashSessionExpiradosProcessor instance(){
		return (RemoveHashSessionExpiradosProcessor) Component.getInstance(NAME);
	}

	/**
	 * Metodo que busca e remove os HashSession expirados
	 * 
	 * @param inicio
	 * @param cron
	 * @return
	 * @throws SchedulerException
	 */
	@Asynchronous
	@Transactional
	public QuartzTriggerHandle removeExpirados(@Expiration Date inicio, @IntervalCron String cron){
		// PJEII-4881  Tratamento de excecao para evitar que a aplicação nao inicie.
		try {
			removeExpirados();
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "removeExpirados");
		}
		return null;
	}
	
	public void removeExpirados() {
		Collection<HashSession> values = EntityUtil.getEntityList(HashSession.class, getEntityManager());
		for (HashSession hashSession : values){
			removeExpirado(hashSession);
		}
	}

	private void removeExpirado(HashSession hashSession){
		if (hashSession.isExpired()){
			getEntityManager().remove(hashSession);
			getEntityManager().flush();
		}
	}

}
