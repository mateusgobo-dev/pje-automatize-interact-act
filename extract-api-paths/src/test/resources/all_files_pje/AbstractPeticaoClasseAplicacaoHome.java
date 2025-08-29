package br.com.infox.cliente.home;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PeticaoClasseAplicacao;

public abstract class AbstractPeticaoClasseAplicacaoHome<T> extends AbstractHome<PeticaoClasseAplicacao> {

	private static final long serialVersionUID = 1L;

	public void setPeticaoClasseAplicacaoIdPeticaoClasseAplicacao(Integer id) {
		setId(id);
	}

	public Integer getPeticaoClasseAplicacaoIdPeticaoClasseAplicacao() {
		return (Integer) getId();
	}

	@Override
	protected PeticaoClasseAplicacao createInstance() {
		PeticaoClasseAplicacao peticaoClasseAplicacao = new PeticaoClasseAplicacao();
		PeticaoHome peticaoHome = (PeticaoHome) Component.getInstance("peticaoHome", false);
		if (peticaoHome != null) {
			peticaoClasseAplicacao.setPeticao(peticaoHome.getDefinedInstance());
		}
		ClasseAplicacaoHome classeAplicacaoHome = (ClasseAplicacaoHome) Component.getInstance("classeAplicacaoHome",
				false);
		if (classeAplicacaoHome != null) {
			peticaoClasseAplicacao.setClasseAplicacao(classeAplicacaoHome.getDefinedInstance());
		}
		return peticaoClasseAplicacao;
	}

	@Override
	public String remove() {
		PeticaoHome peticao = (PeticaoHome) Component.getInstance("peticaoHome", false);
		if (peticao != null) {
			peticao.getInstance().getPeticaoClasseAplicacaoList().remove(instance);
		}
		ClasseAplicacaoHome classeAplicacao = (ClasseAplicacaoHome) Component.getInstance("classeAplicacaoHome", false);
		if (classeAplicacao != null) {
			classeAplicacao.getInstance().getPeticaoClasseAplicacaoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(PeticaoClasseAplicacao obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("peticaoClasseAplicacaoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		newInstance();
		return action;
	}

}