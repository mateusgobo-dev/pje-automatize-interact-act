package br.com.infox.cliente.home;

import org.jboss.seam.contexts.Contexts;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.OrgaoProcedimentoOriginario;

public abstract class AbstractOrgaoProcedimentoOriginarioHome<T> extends AbstractHome<OrgaoProcedimentoOriginario> {

	private static final long serialVersionUID = 1L;

	// ------------ IDENTIFICADOR CONFORME LISTVIEW.XHTML
	// ---------------------------------------------------------------

	private Integer idTipoOrigemPesquisa = null;

	public Integer getIdTipoOrigemPesquisa() {
		if (idValido()) {
			return null;
		} else {
			return idTipoOrigemPesquisa;
		}
	}

	private boolean idValido() {
		return idTipoOrigemPesquisa != null && idTipoOrigemPesquisa.equals(0);
	}

	public void setIdTipoOrigemPesquisa(Integer idTipoOrigemPesquisa) {
		this.idTipoOrigemPesquisa = idTipoOrigemPesquisa;
	}

	// ------------ ATUALIZAÇÃO 'GRID' APÓS MUDAR ABA -- LISTVIEW.XHTML
	// -------------------------------------------------

	@Override
	public void setTab(String tab) {
		super.setTab(tab);
		if (tab.equals("search")) {
			Contexts.removeFromAllContexts("cepSuggest");
		}
	}

	@Override
	public void setGoBackTab(String goBackTab) {
		super.setGoBackTab(goBackTab);
	}

	// ------------ IDENTIFICADOR CONFORME LISTVIEW.PAGE.XML
	// -----------------------------------------------------------

	public void setOrgaoProcedimentoOriginarioId(Integer id) {
		setId(id);
	}

	public Integer getOrgaoProcedimentoOriginarioId() {
		return (Integer) getId();
	}

	// ------------ OPERAÇÕES DE INSTANCIAÇÃO
	// --------------------------------------------------------------------------

	@Override
	public void newInstance() {
		refreshGrid("orgaoProcedimentoOriginarioGrid");
		super.newInstance();
	}

	public static OrgaoProcedimentoOriginarioHome instance() {
		return ComponentUtil.getComponent("orgaoProcedimentoOriginarioHome");
	}

	// ------------ OPERAÇÕES DE PERSISTÊNCIA
	// --------------------------------------------------------------------------

	@Override
	public String remove(OrgaoProcedimentoOriginario opo) {
		inactive(opo);
		return "updated";
	}

	@Override
	public String inactive(OrgaoProcedimentoOriginario opo) {
		opo.setAtivo(Boolean.FALSE);
		String result = super.inactive(opo);
		refreshGrid("orgaoProcedimentoOriginarioGrid");
		return result;
	}

}
