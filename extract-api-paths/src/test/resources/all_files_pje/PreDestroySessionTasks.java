package br.com.infox.bpm.action;

import java.io.Serializable;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.Actor;
import br.com.infox.ibpm.home.Authenticator;

/**
 * Centralizando nesta classe tudo que precisa ser executado quando uma sessão for finalizada. Essa mudança foi motivada por o problema relatado aqui:
 * http:/ /seamframework.org/Community/TryingToRegisterLogoutWhenSessionIsFinished
 * 
 * Vamos testar para ver se esse erro paar de acontecer em produção/homologação.
 * 
 * @author Rodrigo Menezes
 * 
 */
@Name("preDestroySessionTasks")
@Scope(ScopeType.SESSION)
public class PreDestroySessionTasks implements Serializable{

	private static final long serialVersionUID = 1L;

	/**
	 * Ao encerrar uma sessao, limpa os processos que o servidor estava trabalhando Obs.: usando session do hibernate pq o EM da erro de transação
	 */
	@Observer("org.jboss.seam.preDestroyContext.SESSION")
	public void anulaActorId(){
		String actorId = Actor.instance().getId();
		Authenticator.instance().anulaActorId(actorId);
	}

}
