package br.com.infox.editor.manager;

import java.io.Serializable;
import java.text.MessageFormat;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.persistence.FullTextHibernateSessionProxy;

import br.com.itx.component.MeasureTime;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.HibernateUtil;

@Name(ReindexManager.NAME)
public class ReindexManager implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "reindexManager";
	
	private static final LogProvider log = Logging.getLogProvider(ReindexManager.class);
	
	public void reindex(Class<?> entityClass) {
		MeasureTime mt = new MeasureTime(true);
		log.warn("Iniciando o reidex da entidade " + entityClass.getName());
		
		int batchSize = 50;
		FullTextHibernateSessionProxy fullTextSession = (FullTextHibernateSessionProxy) HibernateUtil.getSession();
		
		log.warn("Purge All: " + entityClass.getName());
		fullTextSession.purgeAll(entityClass);
		
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);
		Criteria criteria = fullTextSession.createCriteria(entityClass);
		criteria.setFetchSize(batchSize);
		
		ScrollableResults results = criteria.scroll(ScrollMode.FORWARD_ONLY);
		int index = 0;
		while(results.next()) {
		    index++;
		    fullTextSession.index( results.get(0)); //index each element
		    if (index % batchSize == 0) {
		    	fullTextSession.flushToIndexes(); //apply changes to indexes
		        fullTextSession.clear(); //free memory since the queue is processed
		    } 
		    if (index % 500 == 0) {
		    	log.info("reindex.flushToIndexes()/" + entityClass.getName() + ": "  +  index);
		    }
		}		
		fullTextSession.flushToIndexes();
		results.close();
		log.warn(MessageFormat.format("Fim reindex {0} [{1} | {2} ms]", entityClass.getName(), index, mt.getTime()));
	}	
	
	public static ReindexManager instance() {
		return ComponentUtil.getComponent(NAME);
	}

}
