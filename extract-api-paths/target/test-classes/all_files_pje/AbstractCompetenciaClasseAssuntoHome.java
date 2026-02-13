package br.com.infox.cliente.home;

import static org.jboss.seam.faces.FacesMessages.instance;

import java.util.List;

import javax.persistence.EntityExistsException;

import org.jboss.seam.Component;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.component.AbstractHome;
import br.com.itx.component.grid.GridQuery;
import br.jus.pje.nucleo.entidades.CompetenciaClasseAssunto;

public abstract class AbstractCompetenciaClasseAssuntoHome<T> extends AbstractHome<CompetenciaClasseAssunto> {

	private static final long serialVersionUID = 1L;

	public void setCompetenciaClasseAssuntoIdCompClassAssu(Integer id) {
		setId(id);
	}

	public Integer getCompetenciaClasseAssuntoIdCompClassAssu() {
		return (Integer) getId();
	}

	@Override
	protected CompetenciaClasseAssunto createInstance() {
		CompetenciaClasseAssunto competenciaClasseAssunto = new CompetenciaClasseAssunto();
		AssuntoTrfHome assuntoTrfHome = (AssuntoTrfHome) Component.getInstance("assuntoTrfHome", false);
		if (assuntoTrfHome != null) {
			competenciaClasseAssunto.setAssuntoTrf(assuntoTrfHome.getDefinedInstance());
		}
		ClasseAplicacaoHome classeAplicacaoHome = (ClasseAplicacaoHome) Component.getInstance("classeAplicacaoHome",
				false);
		if (classeAplicacaoHome != null) {
			competenciaClasseAssunto.setClasseAplicacao(classeAplicacaoHome.getDefinedInstance());
		}
		CompetenciaHome competenciaHome = (CompetenciaHome) Component.getInstance("competenciaHome", false);
		if (competenciaHome != null) {
			competenciaClasseAssunto.setCompetencia(competenciaHome.getDefinedInstance());
		}
		return competenciaClasseAssunto;
	}

	@Override
	public String remove() {
		AssuntoTrfHome assuntoTrf = (AssuntoTrfHome) Component.getInstance("assuntoTrfHome", false);
		if (assuntoTrf != null) {
			assuntoTrf.getInstance().getCompetenciaClasseAssuntoList().remove(instance);
		}
		ClasseAplicacaoHome classeAplicacao = (ClasseAplicacaoHome) Component.getInstance("classeAplicacaoHome", false);
		if (classeAplicacao != null) {
			classeAplicacao.getInstance().getCompetenciaClasseAssuntoList().remove(instance);
		}
		CompetenciaHome competencia = (CompetenciaHome) Component.getInstance("competenciaHome", false);
		if (competencia != null) {
			competencia.getInstance().getCompetenciaClasseAssuntoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(CompetenciaClasseAssunto obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		GridQuery grid = (GridQuery) Component.getInstance("competenciaClasseAssuntoGrid");
		grid.refresh();
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getAssuntoTrf() != null) {
			List<CompetenciaClasseAssunto> assuntoList = getInstance().getAssuntoTrf()
					.getCompetenciaClasseAssuntoList();
			if (!assuntoList.contains(instance)) {
				getEntityManager().refresh(getInstance().getAssuntoTrf());
			}
		}
		if (getInstance().getClasseAplicacao() != null) {
			List<CompetenciaClasseAssunto> classeAplicacaoList = getInstance().getClasseAplicacao()
					.getCompetenciaClasseAssuntoList();
			if (!classeAplicacaoList.contains(instance)) {
				getEntityManager().refresh(getInstance().getClasseAplicacao());
			}
		}
		if (getInstance().getCompetencia() != null) {
			List<CompetenciaClasseAssunto> competenciaList = getInstance().getCompetencia()
					.getCompetenciaClasseAssuntoList();
			if (!competenciaList.contains(instance)) {
				getEntityManager().refresh(getInstance().getCompetencia());
			}
		}
		newInstance();
		return action;
	}

	public String persist(CompetenciaClasseAssunto obj) {
		setInstance(obj);

		String ret = null;
		try {
			ret = super.persist();
			refreshGrid("competenciaClasseAssuntoGrid");
		} catch (EntityExistsException e) {
			instance().add(StatusMessage.Severity.ERROR, "Registro já existe.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;

	}

}