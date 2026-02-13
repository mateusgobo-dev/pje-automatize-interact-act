package br.jus.cnj.pje.servicos;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.visao.beans.ObjectObserverBean;
import java.util.HashMap;
import java.util.Objects;

@Name(MensagemAMQPService.NAME)
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class MensagemAMQPService {
	
	public static final String NAME = "mensagemAMQPService";
	public static final String MESSAGE_TO_RABBIT = "messageToRabbit";
	private final HashMap<Object, Class<?>> objetosRabbit = new HashMap<>();

	private void popularObjetosRabbit() {

	}
	
	@Observer(MESSAGE_TO_RABBIT)
	@Transactional
	public void enviarMensagem(ObjectObserverBean bean) {
		if (Objects.nonNull(bean)) {
			popularObjetosRabbit();

			AMQPEventManager.instance().enviarMensagem(
					bean.getObj(),
					objetosRabbit.get(bean.getObj()),
					bean.getVerb()
			);
		}
	}
}
