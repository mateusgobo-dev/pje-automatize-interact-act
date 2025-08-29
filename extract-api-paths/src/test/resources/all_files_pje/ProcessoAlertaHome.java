/**
 * @author thiago.vieira
 */

package br.com.infox.cliente.home;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.ProcessoTrfAlertaSuggestBean;
import br.com.infox.cliente.component.suggest.ProcessoTrfSuggestBean;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ProcessoAlerta;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;

@Name("processoAlertaHome")
@BypassInterceptors
public class ProcessoAlertaHome extends AbstractHome<ProcessoAlerta> {

	private static final long serialVersionUID = 1L;

	public void setProcessoAlertaIdProcessoAlerta(Integer id) {
		setId(id);
	}

	public Integer getProcessoAlertaIdProcessoAlerta() {
		return (Integer) getId();
	}

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("processoTrfSuggest");
		super.newInstance();
		instance.setAtivo(true);
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		ProcessoTrf proc = getInstance().getProcessoTrf();
		if (changed) {
			getProcessoTrfSuggest().setInstance(proc);
		}
		if (id == null) {
			getProcessoTrfSuggest().setInstance(proc);
		}
	}

	public static ProcessoAlertaHome instance() {
		return ComponentUtil.getComponent("processoAlertaHome");
	}

	@Override
	public String persist() {
		String ret = null;
		ProcessoTrf processo = ((ProcessoTrfAlertaSuggestBean)ComponentUtil.getComponent("processoTrfAlertaSuggest")).getInstance(); 
		if (processo == null) {
			ret = "Selecione um processo";
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, ret);
		} else {
			ret = super.persist();
		}

		return ret;
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		ProcessoTrf processo = ((ProcessoTrfAlertaSuggestBean)ComponentUtil.getComponent("processoTrfAlertaSuggest")).getInstance();
		getInstance().setProcessoTrf(processo);
		getInstance().setAlerta(AlertaHome.instance().getInstance());
		return super.beforePersistOrUpdate();
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		if (ret.equals("persisted")) {
			AlertaHome.instance().getInstance().getProcessoAlertaList().add(getInstance());
		}
		refreshGrid("processoAlertaGrid");
		AlertaHome.instance().refreshGrid("alertaGrid");
		return super.afterPersistOrUpdate(ret);
	}

	public void setarProcesso(ProcessoTrf processo) {
		getInstance().setProcessoTrf(processo);
		getProcessoTrfSuggest().setInstance(getInstance().getProcessoTrf());
	}

	private ProcessoTrfSuggestBean getProcessoTrfSuggest() {
		ProcessoTrfSuggestBean processoTrfSuggestBean = (ProcessoTrfSuggestBean) Component
				.getInstance("processoTrfSuggest");
		return processoTrfSuggestBean;
	}

	public CriticidadeAlertaEnum[] getCriticidadeAlertaValues() {
		return CriticidadeAlertaEnum.values();
	}

	/**
	 * Esse método era chamado quando se tentava remover a relação ProcessoAlerta - Alerta, como já exite uma toolBar que chama
	 * diretamente o métod inactive, esse método ficou sem utilidade.
	 */
	@Override
	@Deprecated
	public String remove(ProcessoAlerta p) {
		return super.inactive(p);
	}
}
