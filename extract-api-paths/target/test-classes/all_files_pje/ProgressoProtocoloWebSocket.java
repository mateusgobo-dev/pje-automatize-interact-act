package br.jus.cnj.pje.view;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;

@Name("progressoProtocoloWebSocket")
@ServerEndpoint(value = "/progresso-protocolo/{idProcesso}")
public class ProgressoProtocoloWebSocket {
	
	public static final String ATUALIZA_PROGRESSO_PROTOCOLO = "atualizaProgressoProtocolo";

	private static Map<Integer, Session> sessions = new LinkedHashMap<>();

	@OnOpen
	public void onOpen(@PathParam("idProcesso") Integer idProcesso, Session session) {
		sessions.put(idProcesso, session);
	}

	@OnClose
	public void onClose(Session session) {
		sessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
	}
	
	@Observer(ATUALIZA_PROGRESSO_PROTOCOLO)
	public void atualizaProgressoProtocolo(Integer idProcesso, String msg) {
		try {
			if (sessions.containsKey(idProcesso)) {
				sessions.get(idProcesso).getBasicRemote().sendText(msg);
			}
		} catch (IOException e) {
			// Do nothing.
		}
	}

}
