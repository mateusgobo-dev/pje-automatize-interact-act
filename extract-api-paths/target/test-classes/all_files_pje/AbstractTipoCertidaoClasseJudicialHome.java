package br.com.infox.cliente.home;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ClasseJudicialTipoCertidao;

public abstract class AbstractTipoCertidaoClasseJudicialHome<T> extends AbstractHome<ClasseJudicialTipoCertidao> {

	private static final long serialVersionUID = 1L;

	public void setClasseJudicialTipoCertidaoIdClasseJudicialTipoCertidao(Integer id) {
		setId(id);
	}

	public Integer getClasseJudicialTipoCertidaoIdClasseJudicialTipoCertidao() {
		return (Integer) getId();
	}

	@Override
	protected ClasseJudicialTipoCertidao createInstance() {
		ClasseJudicialTipoCertidao tipoCertidaoClasseJudicial = new ClasseJudicialTipoCertidao();
		ClasseJudicialHome classeJudicialHome = (ClasseJudicialHome) Component.getInstance("classeJudicialHome", false);
		if (classeJudicialHome != null) {
			tipoCertidaoClasseJudicial.setClasseJudicial(classeJudicialHome.getDefinedInstance());
		}
		TipoCertidaoHome tipoCertidaoHome = (TipoCertidaoHome) Component.getInstance("tipoCertidaoHome", false);
		if (tipoCertidaoHome != null) {
			tipoCertidaoClasseJudicial.setTipoCertidao(tipoCertidaoHome.getDefinedInstance());
		}
		return tipoCertidaoClasseJudicial;
	}

	@Override
	public String remove() {
		ClasseJudicialHome classeJudicial = (ClasseJudicialHome) Component.getInstance("classeJudicialHome", false);
		if (classeJudicial != null) {
			classeJudicial.getInstance().getTipoCertidaoClasseJudicialList().remove(instance);
		}
		TipoCertidaoHome tipoCertidao = (TipoCertidaoHome) Component.getInstance("tipoCertidaoHome", false);
		if (tipoCertidao != null) {
			tipoCertidao.getInstance().getTipoCertidaoClasseJudicialList().remove(instance);
		}
		return super.remove();
	}

	public String remover(ClasseJudicialTipoCertidao obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("tipoCertidaoClasseJudicialGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}

}