package br.com.infox.ibpm.entity.log;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesManager;

/**
 * 
 * @author pje
 *
 * Responsável por definir concurrentRequestTimeout e conversation-timeout
 * 
 * 
 * Este parâmetro será definido no jboss ao inicializar a aplicação através da seguinte chamada
 * Exemplo: -Dpje.org.jboss.seam.conversationTimeout=600000 -Dpje.org.jboss.seam.concurrentRequestTimeout=600000 
 *
 */
@Scope(ScopeType.EVENT)
@Name("org.jboss.seam.core.manager")
@Install(precedence = Install.APPLICATION)
@BypassInterceptors
public class PJeFacesManager extends FacesManager {
    
    private static final Integer PJE_EVENT_CONVERSATION_TIMEOUT;
    private static final Integer PJE_REQUEST_CONVERSATION_TIMEOUT;
    
    static {

        Integer pjeConversetionTimeOut = null;
        Integer pjeConcurrentRequestTimeout = null;
        
        if (System.getProperty("pje.org.jboss.seam.conversationTimeout") != null) {
            pjeConversetionTimeOut = Integer.parseInt(System.getProperty("pje.org.jboss.seam.conversationTimeout"));
        }
        
        if (System.getProperty("pje.org.jboss.seam.concurrentRequestTimeout") != null) {
            pjeConcurrentRequestTimeout = Integer.parseInt(System
                    .getProperty("pje.org.jboss.seam.concurrentRequestTimeout"));
        }
        
        PJE_EVENT_CONVERSATION_TIMEOUT = pjeConversetionTimeOut;
        PJE_REQUEST_CONVERSATION_TIMEOUT = pjeConcurrentRequestTimeout;
    }
    
    /**
     * Define timed-out de conversações conversation-timeout
     * 
     *  Esta informação está presente no components.xml na linha abaixo
     *  
     * 	<core:manager concurrent-request-timeout="${concurrent_request_timeout}" conversation-id-parameter="cid" conversation-timeout="600000"
     * 
     * @param conversationTimeout
     */
    @Override
    public void setConversationTimeout(int conversationTimeout) {
        
        if (PJeFacesManager.PJE_EVENT_CONVERSATION_TIMEOUT == null) {
            super.setConversationTimeout(conversationTimeout);
        } else {
            super.setConversationTimeout(PJeFacesManager.PJE_EVENT_CONVERSATION_TIMEOUT);
        }
    }
    
    /**
     * Define concurrentRequestTimeout concurrent-request-timeout
     * 
     *  Esta informação está presente no components.xml na linha abaixo
     *  
     * 	<core:manager concurrent-request-timeout="${concurrent_request_timeout}"	conversation-id-parameter="cid" conversation-timeout="600000"
     * 
     * @param requestWait
     */
    @Override
    public void setConcurrentRequestTimeout(int requestWait) {
        
        if (PJeFacesManager.PJE_REQUEST_CONVERSATION_TIMEOUT == null) {
            super.setConcurrentRequestTimeout(requestWait);
        } else {
            super.setConcurrentRequestTimeout(PJeFacesManager.PJE_REQUEST_CONVERSATION_TIMEOUT);
        }
    }
}
