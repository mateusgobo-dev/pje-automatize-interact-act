package br.jus.cnj.pje.view;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;

import br.com.infox.ibpm.util.GerenciadorCachePlacarSessao;
import br.com.infox.ibpm.util.GerenciadorCachePlacarSessao.PlacarSessao;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.identity.PjeIdentity;
import br.jus.cnj.pje.util.WebSocketHttpSessionConfigurator;


@Name(PlacarSessaoJulgamentoAction.NAME)
@Scope(ScopeType.APPLICATION)
@ServerEndpoint(value="/placarSessaoJulgamento/{idSessao}", configurator = WebSocketHttpSessionConfigurator.class)
public class PlacarSessaoJulgamentoAction {
	public static final String NAME = "placarSessaoJulgamentoAction";
	private static Map<Integer, Set<Session>> sessions = new HashMap<Integer, Set<Session>>();
	private List<PlacarSessao> placares;
	
	@In
	private GerenciadorCachePlacarSessao gerenciadorCachePlacarSessao;	

	@Create
	public void init() {
		if(gerenciadorCachePlacarSessao == null) {
			gerenciadorCachePlacarSessao = ComponentUtil.getComponent(GerenciadorCachePlacarSessao.class);
		}
		this.placares = gerenciadorCachePlacarSessao.getPlacares();
	}
	
	public GerenciadorCachePlacarSessao.PlacarSessao getPlacarSessao(Integer idSessao) {
		PlacarSessao placarSessao = null;
		for (PlacarSessao placar : placares) {
			if(idSessao != null && idSessao.intValue() == placar.getIdSessao()) {
				placarSessao = placar;
				break;
			}
		}
		return placarSessao;
	}
	
	@Observer(GerenciadorCachePlacarSessao.PLACAR_SESSAO_ATUALIZADO)
	public void messageReceiver(List<PlacarSessao> placares) {
		this.placares = placares;
		send();
	}
	
	@OnOpen
	public void onOpen(@PathParam("idSessao") Integer idSessao, Session session, EndpointConfig config) throws IOException {
		if(idSessao != null) {
			if(temPermissao(config)) {
				Set<Session> sessions = PlacarSessaoJulgamentoAction.sessions.get(idSessao);
				if(sessions == null) {
					sessions = new HashSet<Session>(0);
				}
				sessions.add(session);
				PlacarSessaoJulgamentoAction.sessions.put(idSessao, sessions);
			}
		}
	}
	
	@OnClose
	public void onClose(Session session) {
		Set<Integer> ids = PlacarSessaoJulgamentoAction.sessions.keySet();
		for (Integer id : ids) {
			Set<Session> sessions = PlacarSessaoJulgamentoAction.sessions.get(id);
			for (Session s : sessions) {
				if(s.equals(session)) {
					sessions.remove(session);
					break;
				}
			}
		}
	}
	
	private void send() {
		for(PlacarSessao placar : this.placares) {
			send(placar);
		}
	}
	
	private void send(PlacarSessao placar) {
		Set<Session> sessions = PlacarSessaoJulgamentoAction.sessions.get(placar.getIdSessao());
		if(sessions != null && !sessions.isEmpty()) {
			for (Session session : sessions) {
				session.getAsyncRemote().sendText(placar.toJsonString());
			}
		}
	}
	
	private boolean temPermissao(EndpointConfig config) {
		HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
		PjeIdentity  identity = ((PjeIdentity)httpSession.getAttribute("org.jboss.seam.security.identity"));
		 return identity.hasRole(Papeis.PLACAR_SESSAO_JULGAMENTO_WEBSOCKET);
	}
}
