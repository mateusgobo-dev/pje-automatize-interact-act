package br.com.infox.listener;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.jboss.seam.core.Events;

import br.com.infox.cliente.Util;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.service.AutomacaoTagService;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

public class AutomacaoTagListener implements PostUpdateEventListener{
	private static final long serialVersionUID = 1L;

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		if(!EntityUtil.getEntityClass(event.getEntity()).equals(ProcessoTrf.class) || !ProcessoStatusEnum.D.equals(((ProcessoTrf)event.getEntity()).getProcessoStatus()) || ((ProcessoTrf)event.getEntity()).getDataDistribuicao() == null) {
			return;
		}
		event.getSession().getTransaction().registerSynchronization(new Synchronization() {
			@Override
			public void beforeCompletion() {
				// a pedido do sonar: nada a fazer
			}
			@Override
			public void afterCompletion(int status) {
				if (Status.STATUS_COMMITTED == status) {
					String atributoList = "classeJudicial,segredoJustica,orgaoJulgador,orgaoJulgadorColegiado,jurisdicao,dataDistribuicao";
					Object[] oldState = event.getOldState();
					Object[] state = event.getState();
					String[] nomes = event.getPersister().getClassMetadata().getPropertyNames();
					for(int i = 0; i < nomes.length; i++) {
						String nome = nomes[i];
						if (Util.listaContem(atributoList, nome) && !LogUtil.compareObj(oldState[i], state[i])) {
							Events.instance().raiseAsynchronousEvent(AutomacaoTagService.EVENTO_AUTOMACAO_TAG, ((ProcessoTrf) event.getEntity()).getIdProcessoTrf());
							break;
						}
					}
				}
			}
		});
	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return false;
	}
}