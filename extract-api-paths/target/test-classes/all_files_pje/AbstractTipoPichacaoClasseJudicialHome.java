package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoPichacaoClasseJudicial;

public abstract class AbstractTipoPichacaoClasseJudicialHome<T> extends AbstractHome<TipoPichacaoClasseJudicial> {

	private static final long serialVersionUID = 1L;

	public void setTipoPichacaoClasseJudicialIdTipoPichacaoClasseJudicial(Integer id) {
		setId(id);
	}

	public Integer getTipoPichacaoClasseJudicialIdTipoPichacaoClasseJudicial() {
		return (Integer) getId();
	}

	@Override
	protected TipoPichacaoClasseJudicial createInstance() {
		TipoPichacaoClasseJudicial tipoPichacaoClasseJudicial = new TipoPichacaoClasseJudicial();
		ClasseJudicialHome classeJudicialHome = (ClasseJudicialHome) Component.getInstance("classeJudicialHome", false);
		if (classeJudicialHome != null) {
			tipoPichacaoClasseJudicial.setClasseJudicial(classeJudicialHome.getDefinedInstance());
		}
		TipoPichacaoHome tipoPichacaoHome = (TipoPichacaoHome) Component.getInstance("tipoPichacaoHome", false);
		if (tipoPichacaoHome != null) {
			tipoPichacaoClasseJudicial.setTipoPichacao(tipoPichacaoHome.getDefinedInstance());
		}
		return tipoPichacaoClasseJudicial;
	}

	@Override
	public String remove() {
		ClasseJudicialHome classeJudicial = (ClasseJudicialHome) Component.getInstance("classeJudicialHome", false);
		if (classeJudicial != null) {
			classeJudicial.getInstance().getTipoPichacaoClasseJudicialList().remove(instance);
		}
		TipoPichacaoHome tipoPichacao = (TipoPichacaoHome) Component.getInstance("tipoPichacaoHome", false);
		if (tipoPichacao != null) {
			tipoPichacao.getInstance().getTipoPichacaoClasseJudicialList().remove(instance);
		}
		return super.remove();
	}

	public String remover(TipoPichacaoClasseJudicial obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("tipoPichacaoClasseJudicialGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getClasseJudicial() != null) {
			List<TipoPichacaoClasseJudicial> classeJudicialList = getInstance().getClasseJudicial()
					.getTipoPichacaoClasseJudicialList();
			if (!classeJudicialList.contains(instance)) {
				getEntityManager().refresh(getInstance().getClasseJudicial());
			}
		}
		if (getInstance().getTipoPichacao() != null) {
			List<TipoPichacaoClasseJudicial> tipoPichacaoList = getInstance().getTipoPichacao()
					.getTipoPichacaoClasseJudicialList();
			if (!tipoPichacaoList.contains(instance)) {
				getEntityManager().refresh(getInstance().getTipoPichacao());
			}
		}
		newInstance();
		return action;
	}
}