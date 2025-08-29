package br.com.itx.util;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Conversation;
import br.com.infox.access.MenuItem;
import br.com.infox.ibpm.home.Authenticator;

@Name(ConversationUtil.NAME)
public class ConversationUtil {

	public static final String NAME = "conversationUtil";
	
	/**
	 * [PJEII-2855]
	 * Recupera o texto referente ao item de menu selecionado, armazenado na sessão.
	 * 
	 * @author Fernando Barreira
	 * @category PJE-JT
	 * @return
	 */
	public String getMenuOrigem(){
		String retorno = "";
		if(Contexts.getSessionContext().get("menuOrigem") != null){
			retorno = Contexts.getSessionContext().get("menuOrigem").toString(); 
		} else {
			retorno = "Painel do " + Authenticator.getPapelAtual().toString().substring(0, 1)+Authenticator.getPapelAtual().toString().substring(1);
		}
		return retorno;
		
	}

	/**
	 * Finaliza a conversação
	 * 
	 * [PJEII-2855] : Método alterado para private, em função da criação de novo método que recebe o MenuItem selecionado como parâmetro.
	 * 
	 * @param toUrl
	 * @return
	 */
	
	// TODO Verificar como utilizar outcome em vez da url nos menus
	private String endBeforeRedirect(String toUrl) {
		Conversation.instance().root();
		Conversation.instance().endBeforeRedirect();
		return toUrl;
	}

	public String endBeforeRedirect() {
		return this.endBeforeRedirect("home");
	}
	
	/**
	 * [PJEII-2855]
	 * Armazena na sessão a label do item de menu selecionado, antes do redirecionamento para a respectiva url. 
	 * 
	 * @author Fernando Barreira
	 * @category PJE-JT
	 * @param menuItem
	 * @return
	 */
	public String endBeforeRedirect(MenuItem menuItem) {
		Conversation.instance().root();
		Conversation.instance().endBeforeRedirect();
		
		if (menuItem != null) {
			Contexts.getSessionContext().set("menuOrigem", menuItem.getLabel());
			return menuItem.getUrl();
		} else {
			return null;
		}
	}
	
}
