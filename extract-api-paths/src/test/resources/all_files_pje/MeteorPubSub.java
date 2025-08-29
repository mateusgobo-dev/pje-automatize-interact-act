package br.jus.csjt.pje.commons.jms;

import static org.atmosphere.cpr.AtmosphereResource.TRANSPORT.LONG_POLLING;
import java.io.IOException;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.atmosphere.config.service.MeteorService;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.Meteor;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;

@MeteorService(supportSession = true)
public class MeteorPubSub extends HttpServlet {

	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger(MeteorPubSub.class);

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		// Create a Meteor
		Meteor m = Meteor.build(req);

		// Log all events on the console, including WebSocket events.
		m.addListener(new WebSocketEventListenerAdapter());

		res.setContentType("text/html;charset=ISO-8859-1");

		Broadcaster b = lookupBroadcaster(req.getPathInfo());
		m.setBroadcaster(b);

		m.resumeOnBroadcast(m.transport() == LONG_POLLING ? true : false)
				.suspend(-1, false);

		String[] decodedPath = req.getPathInfo().split("/");
		Integer seletor = Integer.parseInt(decodedPath[decodedPath.length - 1]);
		
		if(JmsUtil.isInfraValida()){
			try {
				JmsUtil.inscricaoSessaoJulgamento(new JmsListener(b), seletor);
			} catch (Exception e) {
				logger.warn("Falha na subscricao do servico de mensageria JMS, "
						+ "verificar a configuracao do servidor de aplicacao.");
			}
		}else{
			logger.fatal("Falha na infraestrutura de mensageria, reparar com urgencia.");
			b.broadcast("notificar");
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		String message = req.getReader().readLine();

		if (message != null) {
			String[] decodedPath = req.getPathInfo().split("/");
			try {
				if (message.contains("atualizar")) {
					executarComando(message, decodedPath);
				}
//				retirado else que nao fazia nada.
			} catch (Exception e) {
				logger.error("Falha na subscricao do servico de mensageria JMS, "
						+ "verificar a configuracao do servidor de aplicacao.", e);
				Broadcaster b = lookupBroadcaster(req.getPathInfo());
				b.broadcast("notificar");
			}
		}
	}

	public void executarComando(String message, String[] decodedPath) throws JMSException, NamingException {
		if(JmsUtil.isInfraValida()){
			JmsUtil.enviarMensagemSessaoJulgamento(message, Integer.parseInt(decodedPath[decodedPath.length - 1]));
		}else{
			logger.fatal("Falha na infraestrutura de mensageria");
			throw new JMSException("Falha na infraestrutura de mensageria.");
		}
		
	}

	private Broadcaster lookupBroadcaster(String pathInfo) {
		String[] decodedPath = pathInfo.split("/");
		Broadcaster b = BroadcasterFactory.getDefault().lookup(
				decodedPath[decodedPath.length - 1], true);
		return b;
	}
}
