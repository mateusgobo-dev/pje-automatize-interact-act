package br.jus.csjt.pje.view.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.home.SalaHome;
import br.com.itx.component.AbstractHome;
import br.jus.csjt.pje.business.manager.PeriodoDeInatividadeDaSalaManager;
import br.jus.csjt.pje.persistence.dao.PeriodoDeInatividadeDaSalaList;
import br.jus.pje.jt.entidades.PeriodoDeInatividadeDaSala;

/**
 * Classe para gerenciar uma instancia do periodo de inatividade da sala.
 * 
 * @category PJE-JT
 * @since versao 1.2.0
 * @author Rafael Carvalho
 */
@Name(PeriodoDeInatividadeDaSalaHome.COMPONENT_NAME)
@Scope(ScopeType.CONVERSATION)
public class PeriodoDeInatividadeDaSalaHome extends AbstractHome<PeriodoDeInatividadeDaSala> {

	private static final long serialVersionUID = 1L;
	public static final String COMPONENT_NAME = "periodoDeInatividadeDaSalaHome";

	@In(create = true)
	SalaHome salaHome;
	@In(create = true)
	PeriodoDeInatividadeDaSalaManager periodoDeInatividadeDaSalaManager;
	@In(create = true)
	PeriodoDeInatividadeDaSalaList periodoDeInatividadeDaSalaList;

	public void setPeriodoId(Integer periodoId) {
		setId(periodoId);
	}

	public Integer getPeriodoId() {
		return (Integer) getId();
	}

	@Override
	public void newInstance() {
		super.newInstance();
		getInstance().setSala(salaHome.getInstance());
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		return periodoDeInatividadeDaSalaManager.validarPeriodo(getInstance());
	}

	@Override
	public String inactive(PeriodoDeInatividadeDaSala instance) {
		instance.setAtivo(false);
		getEntityManager().merge(instance);
		getEntityManager().flush();
		newInstance();
		periodoDeInatividadeDaSalaList.refresh();
		getStatusMessages().add("Registro Inativado com Sucesso!");
		return null;
	}

}
