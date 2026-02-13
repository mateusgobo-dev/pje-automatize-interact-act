package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Peticao;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoPichacao;

public abstract class AbstractClasseJudicialHome<T> extends AbstractHome<ClasseJudicial> {

	private static final long serialVersionUID = 1L;

	public void setClasseJudicialIdClasseJudicial(Integer id) {
		setId(id);
	}

	public Integer getClasseJudicialIdClasseJudicial() {
		return (Integer) getId();
	}

	@Override
	protected ClasseJudicial createInstance() {
		ClasseJudicial classeJudicial = new ClasseJudicial();
		return classeJudicial;
	}

	@Override
	public String remove(ClasseJudicial obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("classeJudicialGrid");
		return ret;
	}

	@Override
	public String remove() {
		ClasseJudicialHome classeJudicial = (ClasseJudicialHome) Component.getInstance("classeJudicialHome", false);
		if (classeJudicial != null) {
			classeJudicial.getInstance().getClasseJudicialList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (action != null) {
			if (getInstance().getClasseJudicialPai() != null) {
				List<ClasseJudicial> classeJudicialList = getInstance().getClasseJudicialPai().getClasseJudicialList();
				if (!classeJudicialList.contains(instance)) {
					getEntityManager().refresh(getInstance().getClasseJudicialPai());
				}
			}
		} else {
			getInstance().setPeticaoList(new ArrayList<Peticao>(0));
			getInstance().setTipoParteList(new ArrayList<TipoParte>(0));
			getInstance().setTipoPichacaoList(new ArrayList<TipoPichacao>(0));
		}
		return action;
	}

	public List<ClasseJudicial> getClasseJudicialList() {
		return getInstance() == null ? null : getInstance().getClasseJudicialList();

	}
}