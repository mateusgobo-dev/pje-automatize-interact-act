package br.com.infox.cliente.home;

import br.com.infox.cliente.component.suggest.PessoaPlantaoSuggestBean;
import br.com.infox.cliente.component.tree.LocalizacaoPlantaoTreeHandler;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Plantao;

public abstract class AbstractPlantaOficialJusticaHome<T> extends AbstractHome<Plantao> {

	private static final long serialVersionUID = 1L;

	public void setPlantaoIdPlantao(Integer id) {
		setId(id);
	}

	public Integer getPlantaoIdPlantao() {
		return (Integer) getId();
	}

	@Override
	public String remove(Plantao obj) {
		LogUtil.removeEntity(obj);
		newInstance();
		refreshGrid("plantaoGrid");
		return "removed";
	}

	@Override
	public void setId(Object id) {
		super.setId(id);
		if (isManaged()) {
			getPessoaPlantaoSuggest().setInstance(instance.getPessoa());
			LocalizacaoPlantaoTreeHandler lpt = (LocalizacaoPlantaoTreeHandler) getComponent("localizacaoPlantaoTree");
			lpt.setSelected(instance.getLocalizacao());
		}
	}

	private PessoaPlantaoSuggestBean getPessoaPlantaoSuggest() {
		return (PessoaPlantaoSuggestBean) getComponent("pessoaPlantaoSuggest");
	}

}