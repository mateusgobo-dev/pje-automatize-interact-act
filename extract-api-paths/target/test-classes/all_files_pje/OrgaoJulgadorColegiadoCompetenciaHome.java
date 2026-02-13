package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoCompetencia;

@Name("orgaoJulgadorColegiadoCompetenciaHome")
@BypassInterceptors
public class OrgaoJulgadorColegiadoCompetenciaHome extends
		AbstractOrgaoJulgadorColegiadoCompetenciaHome<OrgaoJulgadorColegiadoCompetencia> {

	private static final long serialVersionUID = 1L;
	private String tipo;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;

	public static OrgaoJulgadorColegiadoCompetenciaHome instance() {
		return ComponentUtil.getComponent("orgaoJulgadorColegiadoCompetenciaHome");
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			if (getInstance().getDataFim() != null && getInstance().getDataFim().before(getInstance().getDataInicio())) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR,
						"A data final tem que ser maior do que a data de início");
			} else {
				getInstance().setOrgaoJulgadorColegiado(OrgaoJulgadorColegiadoHome.instance().getInstance());
				refreshGrid("orgaoJulgadorColegiadoCompetenciaGrid");
				ret = super.persist();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	public void setIdOrgao(Object id) {
		OrgaoJulgadorCompetenciaHome.instance().newInstance();
		this.setId(id);
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (changed) {
			if (CompetenciaHome.instance().getInstance().getCompetencia() != null) {
				tipo = "C";
				orgaoJulgadorColegiado = getInstance().getOrgaoJulgadorColegiado();
			}
		}
	}

	@Override
	public String remove(OrgaoJulgadorColegiadoCompetencia ojc) {
		newInstance();
		try {
			getEntityManager().remove(ojc);
			EntityUtil.flush();
			newInstance();
		} catch (Exception e) {
			// TODO: handle exception
		}
		refreshGrid("orgaoJulgadorColegiadoCompetenciaGrid");
		newInstance();
		super.remove();
		return "removed";
	}

	@Override
	public String update() {
		String ret = null;
		if (getInstance().getDataFim() != null && getInstance().getDataFim().before(getInstance().getDataInicio())) {
			FacesMessages.instance().add(StatusMessage.Severity.INFO,
					"A data final tem que ser maior do que a data de início");
			getEntityManager().refresh(instance);
		} else {
			if (CompetenciaHome.instance().getInstance().getCompetencia() != null) {
				instance.setOrgaoJulgadorColegiado(orgaoJulgadorColegiado);
			}
			ret = super.update();
		}
		refreshGrid("orgaoJulgadorColegiadoCompetenciaGrid");
		return ret;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getTipo() {
		return tipo;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

}