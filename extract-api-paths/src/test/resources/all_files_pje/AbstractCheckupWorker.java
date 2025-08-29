package br.jus.cnj.pje.util.checkup.spi;

import java.io.Serializable;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.util.checkup.CheckupService;
import br.jus.pje.nucleo.util.Crypto;

/**
 * Classe auxiliar para facilitar criação de checkups para o PJe.
 * @author davidhsv
 *
 */
public abstract class AbstractCheckupWorker implements CheckupWorker, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Logger
	private Log log;
	
	@Observer(value=CheckupService.REGISTER_EVENT)
	public void register(CheckupRegister checkupRegister) {
		//se registrar na lista de checkups
		checkupRegister.registerCheckup((CheckupWorker) Component.getInstance(this.getClass()));
	}

	@Override
	public String getID() {
		// Identificar o checkup pela canonicalName - Ex.: br.jus.br.Classe
		return Crypto.encodeMD5(this.getClass().getCanonicalName().getBytes());
	}

	@Override
	public Boolean shouldRun() {
		return true;
	}

}
