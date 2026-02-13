package br.com.infox.cliente.home;

import javax.persistence.Query;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.NormaPenal;

public abstract class AbstractNormaPenalHome<T> extends AbstractHome<NormaPenal> {

	private static final long serialVersionUID = 1L;

	// ------------ IDENTIFICADOR CONFORME LISTVIEW.XHTML
	// ---------------------------------------------------------------

	private Integer idTipoNormaPenalPesquisa = null;

	public Integer getIdTipoNormaPenalPesquisa() {
		if (idComplementarValorZero()) {
			return null;
		} else {
			return idTipoNormaPenalPesquisa;
		}
	}

	private boolean idComplementarValorZero() {
		return idTipoNormaPenalPesquisa != null && idTipoNormaPenalPesquisa.equals(0);
	}

	public void setIdTipoNormaPenalPesquisa(Integer idTipoNormaPenalPesquisa) {
		this.idTipoNormaPenalPesquisa = idTipoNormaPenalPesquisa;
	}

	// ------------ IDENTIFICADOR CONFORME LISTVIEW.XHTML
	// ---------------------------------------------------------------

	private Integer idComplementarPesquisa = null;

	public Integer getIdComplementarPesquisa() {
		if (idComplementarPesquisaValorZero() == false) {
			return idTipoNormaPenalPesquisa;
		}
		return null;
	}

	private boolean idComplementarPesquisaValorZero() {
		return idComplementarPesquisa != null && idComplementarPesquisa.equals(0);
	}

	public void setIdComplementarPesquisa(Integer idComplementarPesquisa) {
		this.idComplementarPesquisa = idComplementarPesquisa;
	}

	public void setDispositivoNormaIdDispositivoNorma(Integer id) {
		setId(id);
	}

	public Integer getDispositivoNormaIdDispositivoNorma() {
		return (Integer) getId();
	}

	@Override
	public void setTab(String tab) {
		super.setTab(tab);
		refreshGrid("normaPenalGrid");
		refreshGrid("dispositivoNormaGrid");
	}

	@Override
	public void setGoBackTab(String goBackTab) {
		super.setGoBackTab(goBackTab);
		refreshGrid("normaPenalGrid");
		refreshGrid("dispositivoNormaGrid");
	}

	// ------------ IDENTIFICADOR CONFORME LISTVIEW.PAGE.XML
	// -----------------------------------------------------------

	public void setNormaPenalIdNormaPenal(Integer id) {
		setId(id);
	}

	public Integer getNormaPenalIdNormaPenal() {
		return (Integer) getId();
	}

	// ------------ OPERAÇÕES DE INSTANCIAÇÃO
	// --------------------------------------------------------------------------

	@Override
	public void newInstance() {
		refreshGrid("normaPenalGrid");
		super.newInstance();
	}

	public static NormaPenalHome instance() {
		return ComponentUtil.getComponent("normaPenalHome");
	}

	// ------------ OPERAÇÕES DE PERSISTÊNCIA
	// --------------------------------------------------------------------------

	@Override
	public String persist() {
		// getInstance().setAtivo(Boolean.TRUE);
		return super.persist();
	}

	@Override
	public String update() {
		String ret = null;
		try {
			ret = super.update();
		} catch (Exception e) {
			System.out.println("Erro de restrição: possivelmente um campo foi duplicado.");
		}
		return ret;
	}

	@Override
	public String remove(NormaPenal n) {
		inactive(n);
		return "updated";
	}

	@Override
	public String inactive(NormaPenal n) {
		Query query = getEntityManager().createQuery(
				"update DispositivoNorma dn set dn.ativo = false where dn.normaPenal.idNormaPenal = ?");
		query.setParameter(1, n.getIdNormaPenal());
		query.executeUpdate();
		n.setAtivo(Boolean.FALSE);
		String result = super.inactive(n);
		refreshGrid("normaPenalGrid");
		return result;
	}

}
