package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.JurisdicaoMunicipioSuggestBean;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.JurisdicaoMunicipio;

@Name("jurisdicaoMunicipioHome")
@BypassInterceptors
public class JurisdicaoMunicipioHome extends AbstractJurisdicaoMunicipioHome<JurisdicaoMunicipio> {

	private static final long serialVersionUID = 1L;

	private Estado estado;

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		if (this.estado != null) {
			if ((estado == null) || (!this.estado.getEstado().equals(estado.getEstado()))) {
				getJurisdicaoMunicipioSuggest().setInstance(null);
			}
		}
		this.estado = estado;
	}

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("jurisdicaoMunicipioSuggest");
		estado = null;
		super.newInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean beforePersistOrUpdate() {
		if (getJurisdicaoMunicipioSuggest().getInstance() == null) {
			FacesMessages.instance().add(Severity.ERROR, "Escolha uma Cidade.");
			return false;
		}
		instance.setMunicipio(getJurisdicaoMunicipioSuggest().getInstance());
		// manter somente uma sede
		if (instance.getSede()) {
			Query query = getEntityManager().createQuery("from JurisdicaoMunicipio where jurisdicao=?");
			query.setParameter(1, instance.getJurisdicao());

			for (JurisdicaoMunicipio jurisdicaoMunicipio : (List<JurisdicaoMunicipio>) query.getResultList()) {
				if (!jurisdicaoMunicipio.equals(instance)) {
					jurisdicaoMunicipio.setSede(false);
				}
			}
			getEntityManager().flush();
		} else {
			Query query = getEntityManager().createQuery("from JurisdicaoMunicipio where jurisdicao=? and sede=true");
			query.setParameter(1, instance.getJurisdicao());
			instance.setSede(query.getResultList().isEmpty());
		}
		instance.getJurisdicao().getMunicipioList().add(getInstance());
		return super.beforePersistOrUpdate();
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (changed) {
			if (getInstance().getMunicipio() != null) {
				estado = getInstance().getMunicipio().getEstado();
			}
			getJurisdicaoMunicipioSuggest().setInstance(getInstance().getMunicipio());
		}
		if (id == null) {
			getJurisdicaoMunicipioSuggest().setInstance(null);
			estado = null;
		}
		if (getInstance().getMunicipio() != null) {
			if ((!changed) && (id != null) && (estado != getInstance().getMunicipio().getEstado())) {
				estado = getInstance().getMunicipio().getEstado();
				getJurisdicaoMunicipioSuggest().setInstance(getInstance().getMunicipio());
			}
		}
	}

	private JurisdicaoMunicipioSuggestBean getJurisdicaoMunicipioSuggest() {
		JurisdicaoMunicipioSuggestBean jurisdicaoMunicipioSuggest = (JurisdicaoMunicipioSuggestBean) Component
				.getInstance("jurisdicaoMunicipioSuggest");
		return jurisdicaoMunicipioSuggest;
	}

	@Override
	public String remove(JurisdicaoMunicipio obj) {
		newInstance();
		getEntityManager().remove(obj);
		getInstance().getJurisdicao().getMunicipioList().remove(obj);
		EntityUtil.flush();
		newInstance();
		super.remove();
		return "removido";
	}
}