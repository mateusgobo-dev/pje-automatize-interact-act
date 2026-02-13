package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.com.itx.component.grid.GridQuery;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.CompetenciaClasseAssunto;

public abstract class AbstractCompetenciaHome<T> extends AbstractHome<Competencia> {

	private static final long serialVersionUID = 1L;

	public void setCompetenciaIdCompetencia(Integer id) {
		setId(id);
	}

	public Integer getCompetenciaIdCompetencia() {
		return (Integer) getId();
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

	@Override
	protected Competencia createInstance() {
		Competencia competencia = new Competencia();
		return competencia;
	}

	@Override
	public String remove(Competencia obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		GridQuery grid = (GridQuery) Component.getInstance("competenciaGrid");
		grid.refresh();
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}

	public List<CompetenciaClasseAssunto> getCompetenciaClasseAssuntoList() {
		return getInstance() == null ? null : getInstance().getCompetenciaClasseAssuntoList();
	}

}