package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.com.itx.component.grid.GridQuery;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCompetencia;

public abstract class AbstractOrgaoJulgadorCompetenciaHome<T> extends AbstractHome<OrgaoJulgadorCompetencia> {

	private static final long serialVersionUID = 1L;

	public void setOrgaoJulgadorCompetenciaIdOrgaoJulgadorCompetencia(Integer id) {
		setId(id);
	}

	public Integer getOrgaoJulgadorCompetenciaIdOrgaoJulgadorCompetencia() {
		return (Integer) getId();
	}

	@Override
	protected OrgaoJulgadorCompetencia createInstance() {
		OrgaoJulgadorCompetencia orgaoJulgadorCompetencia = new OrgaoJulgadorCompetencia();
		OrgaoJulgadorHome orgaoJulgadorHome = (OrgaoJulgadorHome) Component.getInstance("orgaoJulgadorHome", false);
		if (orgaoJulgadorHome != null) {
			orgaoJulgadorCompetencia.setOrgaoJulgador(orgaoJulgadorHome.getDefinedInstance());
		}
		CompetenciaHome competencia = (CompetenciaHome) Component.getInstance("competenciaHome", false);
		if (competencia != null) {
			orgaoJulgadorCompetencia.setCompetencia(competencia.getDefinedInstance());
		}
		return orgaoJulgadorCompetencia;
	}

	@Override
	public String remove() {
		OrgaoJulgadorHome orgaoJulgador = (OrgaoJulgadorHome) Component.getInstance("orgaoJulgadorHome", false);
		
		// [PJEII-504] Tiago Zanon/Haroldo Arouca - 29/02/2012
		Competencia competencia = instance.getCompetencia();
		
		if (orgaoJulgador != null) {
			orgaoJulgador.getInstance().getOrgaoJulgadorCompetenciaList().remove(instance);
		}
		
		if (competencia != null) {
			competencia.getOrgaoJulgadorCompetenciaList().remove(instance);
		}
		// PJE-FIM
		
		refreshGrid("orgaoJulgadorCompetenciasGrid");
		return super.remove();
	}

	@Override
	public String remove(OrgaoJulgadorCompetencia obj) {
		setInstance(obj);
		String ret = remove();
		newInstance();
		GridQuery grid = (GridQuery) Component.getInstance("orgaoJulgadorCompetenciasGrid");
		refreshGrid("orgaoJulgadorCompetenciasGrid");
		grid.refresh();
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getOrgaoJulgador() != null) {
			List<OrgaoJulgadorCompetencia> orgaoJulgadorList = getInstance().getOrgaoJulgador()
					.getOrgaoJulgadorCompetenciaList();
			if (orgaoJulgadorList != null && !orgaoJulgadorList.contains(instance)) {
				getEntityManager().refresh(getInstance().getOrgaoJulgador());
			}
		}
		if (getInstance().getCompetencia() != null) {
			List<OrgaoJulgadorCompetencia> compClassAssuList = getInstance().getCompetencia()
					.getOrgaoJulgadorCompetenciaList();
			if (compClassAssuList != null && !compClassAssuList.contains(instance)) {
				getEntityManager().refresh(getInstance().getCompetencia());
			}
		}
		newInstance();
		return action;
	}

}