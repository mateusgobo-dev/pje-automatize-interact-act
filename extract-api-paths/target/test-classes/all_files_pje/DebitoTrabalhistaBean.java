package br.jus.cnj.pje.visao.beans;

import java.io.Serializable;

import br.jus.pje.jt.entidades.DebitoTrabalhista;
import br.jus.pje.nucleo.entidades.ProcessoParte;

public class DebitoTrabalhistaBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5880643716274672443L;
	private Boolean selected = Boolean.FALSE;
	private DebitoTrabalhista debitoTrabalhista;

	public DebitoTrabalhistaBean(ProcessoParte processoParte) {

		DebitoTrabalhista debitoTrabalhistaTemp = new DebitoTrabalhista();
		debitoTrabalhistaTemp.setProcessoParte(processoParte);
		debitoTrabalhista = debitoTrabalhistaTemp;

	}

	public ProcessoParte getProcessoParte() {

		return debitoTrabalhista.getProcessoParte();
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public Boolean getSelected() {
		return selected;
	}

	public void setDebitoTrabalhista(DebitoTrabalhista debitoTrabalhista) {
		this.debitoTrabalhista = debitoTrabalhista;
	}

	public DebitoTrabalhista getDebitoTrabalhista() {
		return debitoTrabalhista;
	}
}
