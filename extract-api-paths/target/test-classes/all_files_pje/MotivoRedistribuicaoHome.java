package br.com.infox.cliente.home;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.MotivoEventoRedistribuicao;
import br.jus.pje.nucleo.entidades.MotivoRedistribuicao;

@Name(MotivoRedistribuicaoHome.NAME)
@BypassInterceptors
public class MotivoRedistribuicaoHome extends AbstractHome<MotivoRedistribuicao> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "motivoRedistribuicaoHome";

	private String eventoRedistribuicao;

	public static MotivoRedistribuicaoHome instance() {
		return ComponentUtil.getComponent(MotivoRedistribuicaoHome.NAME);
	}

	public void addTipoRedistribuicao(MotivoRedistribuicao motivoRedistribuicao, Evento eventoRedistribuicao) {
		MotivoEventoRedistribuicao motivoEventoRedistribuicao = new MotivoEventoRedistribuicao();
		FacesMessages.instance().clear();
		if (motivoEventoRedistribuicao != null) {
			motivoEventoRedistribuicao.setMotivoRedistribuicao(motivoRedistribuicao);
			motivoEventoRedistribuicao.setEventoRedistribuicao(eventoRedistribuicao);
			getEntityManager().persist(motivoEventoRedistribuicao);
			EntityUtil.flush();
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "tipoRedistribuicao_created"));
		}
	}

	public void removeTipoRedistribuicao(MotivoEventoRedistribuicao motivoEventoRedistribuicao) {
		FacesMessages.instance().clear();
		getEntityManager().remove(motivoEventoRedistribuicao);
		EntityUtil.flush();
		FacesMessages.instance().add(Severity.INFO, "Registro inativado com sucesso.");
	}

	public void setEventoRedistribuicao(String eventoRedistribuicao) {
		this.eventoRedistribuicao = eventoRedistribuicao;
	}

	public String getEventoRedistribuicao() {
		return eventoRedistribuicao;
	}

	public boolean verificarMotivoRedistribuicao() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from MotivoRedistribuicao o ");
		sb.append("where lower(o.motivoRedistribuicao) = lower(:descricao) ");
		if ((getInstance().getIdMotivoRedistribuicao() != null) && (getInstance().getIdMotivoRedistribuicao() != 0)) {
			sb.append("and o.idMotivoRedistribuicao <> :idMotivoRedistribuicao");
		}
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("descricao", getInstance().getMotivoRedistribuicao());
		if ((getInstance().getIdMotivoRedistribuicao() != null) && (getInstance().getIdMotivoRedistribuicao() != 0)) {
			q.setParameter("idMotivoRedistribuicao", getInstance().getIdMotivoRedistribuicao());
		}
		
		Long retorno = 0L;
		try {
			retorno = (Long) q.getSingleResult();
		} catch (NoResultException no) {
			return Boolean.TRUE;
		}
		if (retorno > 0) {
			getInstance().setMotivoRedistribuicao(null);
			FacesMessages.instance().add(Severity.ERROR, "Registro já cadastrado!");
			return false;
		}
		return true;
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (verificarMotivoRedistribuicao()) {
			return super.beforePersistOrUpdate();
		} else {
			return false;
		}
	}
	
	@Override
	protected String afterPersistOrUpdate(String ret) {
		FacesMessages.instance().clear();
		if (ret.equals("updated"))
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "motivoRedistribuicao_updated"));
		if (ret.equals("persisted"))
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "motivoRedistribuicao_created"));

		return ret;
	}
	
}