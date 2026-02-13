package br.com.infox.cliente.home;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ClasseJudicialTipoCertidao;

public abstract class AbstractClasseJudicialTipoCertidaoHome<T> extends AbstractHome<ClasseJudicialTipoCertidao> {

	private static final long serialVersionUID = 1L;

	public void setClasseJudicialTipoCertidaoIdClasseJudicialTipoCertidao(Integer id) {
		setId(id);
	}

	public Integer getClasseJudicialTipoCertidaoIdClasseJudicialTipoCertidao() {
		return (Integer) getId();
	}

	@Override
	protected ClasseJudicialTipoCertidao createInstance() {
		ClasseJudicialTipoCertidao classeJudicialTipoCertidao = new ClasseJudicialTipoCertidao();
		TipoCertidaoHome tipoCertidao = (TipoCertidaoHome) Component.getInstance("tipoCertidaoHome", false);
		if (tipoCertidao != null) {
			classeJudicialTipoCertidao.setTipoCertidao(tipoCertidao.getDefinedInstance());
		}
		return classeJudicialTipoCertidao;
	}

	@Override
	public String remove(ClasseJudicialTipoCertidao obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("classeJudicialTipoCertidaoGrid");
		return ret;
	}

	/*
	 * @Override public String remove() { TipoCertidaoHome tipoCertidao =
	 * (TipoCertidaoHome) Component .getInstance("tipoCertidaoHome", false); if
	 * (tipoCertidao != null) {
	 * tipoCertidao.getInstance().getClasseJudicialList() .remove(instance); }
	 * 
	 * return super.remove(); }
	 * 
	 * @Override public String persist() { String persist = super.persist(); if
	 * (getInstance().getOrgaoJulgador() != null) {
	 * List<OrgaoJulgadorCompetencia> orgaoJulgadorList = getInstance()
	 * .getOrgaoJulgador().getOrgaoJulgadorCompetenciaList(); if
	 * (!orgaoJulgadorList.contains(instance)) {
	 * getEntityManager().refresh(getInstance().getOrgaoJulgador()); } } if
	 * (getInstance().getCompetencia() != null) { List<OrgaoJulgadorCompetencia>
	 * compClassAssuList = getInstance()
	 * .getCompetencia().getOrgaoJulgadorCompetenciaList(); if
	 * (!compClassAssuList.contains(instance)) {
	 * getEntityManager().refresh(getInstance().getCompetencia()); } }
	 * newInstance(); return persist; }
	 */

}