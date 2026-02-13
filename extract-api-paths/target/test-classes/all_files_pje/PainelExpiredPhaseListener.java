package br.com.itx.util;

import com.sun.faces.util.Util;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

public class PainelExpiredPhaseListener implements PhaseListener {

    private static final String POST_METHOD = "POST";
	private static final String LOGIN_SEAM = "/dev.seam";
	/**
	 * 
	 */
	private static final long serialVersionUID = 4530024363119263001L;

	public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }

    public void beforePhase(PhaseEvent event) {
    	// Se estiver na página de login e for HTTP POST
    	if (isLoginPageAndHttpPost()) {
    		// Tentar recuperar a árvore JSF    		
    		FacesContext fc = event.getFacesContext();
    		ViewHandler viewHandler = Util.getViewHandler(fc);
    		UIViewRoot viewRoot = viewHandler.restoreView(fc, "/ng2/dev.seam");

    		// Se estiver nula -> árvore JSF expirou
            if (viewRoot == null) {
            	// Criar a árvore JSF
                viewRoot = viewHandler.createView(fc, "/ng2/dev.seam");
                fc.setViewRoot(viewRoot);
                
                // Simular renderização
                try{
					fc.getApplication().getViewHandler().renderView(fc, fc.getViewRoot());
				} catch (Exception e){ 
					// swallow
				}
                
                // Irá continuar na fase RESTORE_VIEW como se a árvore JSF estivesse intacta
            }
    	}
    }
    

    public void afterPhase(PhaseEvent event) {
    }
    
	private HttpServletRequest getRequest() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext != null && facesContext.getExternalContext() != null) {
			Object requestObj = facesContext.getExternalContext().getRequest();
			if (requestObj instanceof HttpServletRequest) {
				return (HttpServletRequest) requestObj;
			}
		}
		return null;
	}
	
	private boolean isLoginPageAndHttpPost() {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return false;
		}
		String requestURL = request.getRequestURL().toString();
		return requestURL.endsWith(LOGIN_SEAM) && request.getMethod().equals(POST_METHOD);
	}
	
}