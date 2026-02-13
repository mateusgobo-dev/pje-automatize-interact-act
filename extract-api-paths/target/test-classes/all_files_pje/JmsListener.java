package br.jus.csjt.pje.commons.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.atmosphere.cpr.Broadcaster;

public class JmsListener implements MessageListener {

	/**
	 * Broadcaste utilizado pelo atmosphere para propagar.
	 */
	Broadcaster broadcaster;
	
	public JmsListener() {
	}
	
	public JmsListener(Broadcaster b) {
		this.broadcaster = b;
	}
	
	@Override
	public void onMessage(Message message) {

        TextMessage tm = (TextMessage) message;
        if (message != null) {
            try {
				broadcaster.broadcast(tm.getText());
			} catch (JMSException e) {
				e.printStackTrace();
			}
        }
	}
	
}
