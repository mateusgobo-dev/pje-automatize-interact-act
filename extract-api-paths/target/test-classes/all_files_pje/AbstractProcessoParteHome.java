package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoParte;

public abstract class AbstractProcessoParteHome<T> extends AbstractHome<ProcessoParte> {

	private static final long serialVersionUID = 1L;

	public void setProcessoParteIdProcessoParte(Integer id) {
		setId(id);
	}

	public Integer getProcessoParteIdProcessoParte() {
		return (Integer) getId();
	}

	public void setAplicacaoClasseIdAplicacaoClasse(Integer id) {
		setId(id);
	}

	public Integer getAplicacaoClasseIdAplicacaoClasse() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoParte createInstance() {
		ProcessoParte processoParte = new ProcessoParte();
		return processoParte;
	}

	@Override
	public String remove(ProcessoParte obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("processoParteGrid");
		refreshGrid("processoPoloAtivoGrid");
		refreshGrid("processoPoloPassivoGrid");
		refreshGrid("processoIncidentePoloAtivoGrid");
		refreshGrid("processoIncidentePoloPassivoGrid");
		refreshGrid("cadastroPartesGrid");
		refreshGrid("cadastroPartesAdvGrid");
		return ret;
	}

	public String persist(ProcessoParte obj) {
		setInstance(obj);
		String action = super.persist();
		newInstance();
		return action;
	}

	@Override
	public String persist() {
		return this.persist(true);
	}
	
	public String persist(boolean createNewInstance){
		String action = super.persist();
		
		if(createNewInstance){
			newInstance();
		}

		return action;		
	}

}