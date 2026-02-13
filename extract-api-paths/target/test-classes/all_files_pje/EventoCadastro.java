package br.com.infox.cliente.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.ibpm.component.tree.EventoBean;
import br.com.infox.ibpm.component.tree.EventsAllTreeHandler;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Home criado pois será utilizado somente para lançar os eventos durante a
 * homologação do dia 16/01/2012
 * 
 * @author edsonaraujo
 * 
 */

@Name("eventoCadastro")
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EventoCadastro implements Serializable {

	private static final long serialVersionUID = 1L;
	private ProcessoTrf processoTrf;
	private Date dtEvento;

	public void inserirEventos() {
		List<EventoBean> eventosParaInserir = EventsAllTreeHandler.instance().getEventoBeanList();
		for (EventoBean eventoBean : eventosParaInserir) {
			ProcessoEvento pe = new ProcessoEvento();
			if (dtEvento != null) {
				pe.setDataAtualizacao(dtEvento);
			} else {
				pe.setDataAtualizacao(new Date());
			}
			pe.setProcesso(processoTrf.getProcesso());
			pe.setEvento(EntityUtil.find(Evento.class, eventoBean.getIdEvento()));
			pe.setProcessado(false);
			pe.setVerificadoProcessado(false);
			EntityUtil.getEntityManager().merge(pe);
			EntityUtil.flush();
		}
		limpar();
	}

	public void limpar() {
		processoTrf = null;
		dtEvento = null;
		Contexts.removeFromAllContexts("processoTrf");
		Contexts.removeFromAllContexts("dtEvento");
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public Date getDtEvento() {
		return dtEvento;
	}

	public void setDtEvento(Date dtEvento) {
		this.dtEvento = dtEvento;
	}
}