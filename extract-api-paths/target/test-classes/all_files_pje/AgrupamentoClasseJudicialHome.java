package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.AgrupamentoClasseJudicial;
import br.jus.pje.nucleo.entidades.ClasseJudicialAgrupamento;

@Name(AgrupamentoClasseJudicialHome.NAME)
@BypassInterceptors
public class AgrupamentoClasseJudicialHome extends
		AbstractHome<AgrupamentoClasseJudicial> {

	public static final String NAME = "agrupamentoClasseJudicialHome";
	private static final long serialVersionUID = 1L;

	public void setAgrupamentoClasseJudicialIdAgrupamentoClasseJudicial(
			Integer id) {
		setId(id);
	}

	public Integer getAgrupamentoClasseJudicialIdAgrupamentoClasseJudicial() {
		return (Integer) getId();
	}

	public static AgrupamentoClasseJudicialHome instance() {
		return ComponentUtil.getComponent(AgrupamentoClasseJudicialHome.NAME);
	}

	public List<ClasseJudicialAgrupamento> getClasseJudicialAgrupamentoList() {
		return getInstance().getClasseJudicialAgrupamentoList();
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		refreshGrid("agrupamentoClasseJudicialGrid");
		return super.afterPersistOrUpdate(ret);
	}

	@Override
	public String remove(AgrupamentoClasseJudicial obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("agrupamentoClasseJudicialGrid");
		return ret;
	}

	@Override
	public String inactive(AgrupamentoClasseJudicial instance) {
		
		instance.setAtivo(Boolean.FALSE);
		setInstance(instance);

		String ret = super.update();

		if (ret != null && ret != "") {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO,
					super.getInactiveSuccess());
		}

		return ret;

	}

	private boolean verificaAgrupamento() {
		StringBuilder sb = new StringBuilder();
		sb.append(" select o from AgrupamentoClasseJudicial o ");
		sb.append("where lower(o.agrupamento) = lower(:agrupamento) ");
		if (getInstance().getIdAgrupamento() != 0) {
			sb.append("and o.idAgrupamento <> :idAgrupamento ");
		}
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("agrupamento", getInstance().getAgrupamento().trim()
				.toLowerCase());
		if (getInstance().getIdAgrupamento() != 0) {
			q.setParameter("idAgrupamento", getInstance().getIdAgrupamento());
		}
		if (q.getResultList().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (verificaAgrupamento()) {
			FacesMessages.instance().add(Severity.ERROR,
					"Já existe um Agrupamento com essa descrição cadastrado.");
			refreshGrid("agrupamentoClasseJudicialGrid");
			getEntityManager().clear();
			return false;
		}
		return super.beforePersistOrUpdate();
	}
}