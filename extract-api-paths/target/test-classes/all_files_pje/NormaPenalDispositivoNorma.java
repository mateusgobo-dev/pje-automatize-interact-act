package br.com.infox.cliente.bean;

import br.jus.pje.nucleo.entidades.DispositivoNorma;
import br.jus.pje.nucleo.entidades.NormaPenal;

public class NormaPenalDispositivoNorma {

	private NormaPenal normaPenal;
	private DispositivoNorma dispositivoNorma;

	public NormaPenal getNormaPenal() {
		return normaPenal;
	}

	public void setNormaPenal(NormaPenal normaPenal) {
		this.normaPenal = normaPenal;
	}

	public DispositivoNorma getDispositivoNorma() {
		return dispositivoNorma;
	}

	public void setDispositivoNorma(DispositivoNorma dispositivoNorma) {
		this.dispositivoNorma = dispositivoNorma;
	}

}
