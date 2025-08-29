package br.com.infox.cliente.home;

import java.util.List;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.RamoAtividade;

public abstract class AbstractRamoAtividadeHome<T> extends AbstractHome<RamoAtividade> {

	private static final long serialVersionUID = 1L;

	public void setRamoAtividadeIdRamoAtividade(Integer id) {
		setId(id);
	}

	public Integer getRamoAtividadeIdRamoAtividade() {
		return (Integer) getId();
	}

	@Override
	protected RamoAtividade createInstance() {
		RamoAtividade ramoAtividade = new RamoAtividade();
		return ramoAtividade;
	}

	@Override
	public String remove(RamoAtividade obj) {
		setInstance(obj);
		String ret = null;
		try {
			ret = super.remove();
			newInstance();
			refreshGrid("ramoAtividadeGrid");
		} catch (Exception e) {
			// TODO: handle exception
		}
		return ret;
	}

	@Override
	public String persist() {
		String action = null;
		try {
			action = super.persist();
			if (getInstance().getRamoAtividadePai() != null) {
				List<RamoAtividade> ramoAtividadeList = getInstance().getRamoAtividadePai().getRamoAtividadeList();
				if (!ramoAtividadeList.contains(instance)) {
					getEntityManager().refresh(getInstance().getRamoAtividadePai());
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return action;
	}
}