package br.com.infox.bpm.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.ConversationEntries;
import org.jboss.seam.core.ConversationEntry;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

@Scope(ScopeType.CONVERSATION)
@Name(MovimentarProcessoAction.NAME)
@BypassInterceptors
public class MovimentarProcessoAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "movimentarProcessoAction";
	private static final String VIEW_ID_PAGE_MOVIMENTAR = "/Processo/movimentar.xhtml";
	private static final LogProvider log = Logging.getLogProvider(MovimentarProcessoAction.class);




	/**
	 * Metodo temporario para ajudar no debug. Ele lista as conversações do
	 * usuário que estãoa ssociadas com a pagina de movimentação.
	 * 
	 * @return
	 */
	@Deprecated
	public List<String> listarConversations() {
		List<String> list = new ArrayList<String>();
		Collection<ConversationEntry> conversationEntries = ConversationEntries.instance().getConversationEntries();
		for (ConversationEntry conversationEntry : conversationEntries) {
			if (VIEW_ID_PAGE_MOVIMENTAR.equals(conversationEntry.getViewId())) {
				long timeUltimoAcesso = conversationEntry.getLastDatetime().getTime();
				long timeNow = new Date().getTime();
				long segundosSemAcesso = (timeNow - timeUltimoAcesso) / 1000;
				list.add(conversationEntry.getId() + " - " + conversationEntry.getDescription() + " - "
						+ segundosSemAcesso + " s sem acesso.");
			}
		}
		return list;
	}

}
