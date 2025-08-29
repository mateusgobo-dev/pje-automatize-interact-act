package br.jus.cnj.pje.util.checkup.spi;

import java.io.Serializable;

import br.jus.pje.nucleo.util.Crypto;


/**
 * Classe auxiliar para facilitar a criação de CheckupError
 * @author davidhsv
 *
 */
public class CheckupErrorImpl implements CheckupError, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String workerId;
	private String errorMessage;
	private String fixMessage;
	
	public CheckupErrorImpl(CheckupWorker worker, String errorMessage, String fixMessage) {
		this.workerId = worker.getID();
		this.errorMessage = errorMessage;
		this.fixMessage = fixMessage;
	}

	public CheckupErrorImpl(CheckupWorker worker, String errorMessage) {
		this.workerId = worker.getID();
		this.errorMessage = errorMessage;
	}

	@Override
	public String getID() {
		return Crypto.encodeMD5(workerId + getErrorMessage());
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public String getFixMessage() {
		return fixMessage;
	}
	
	@Override
	public String toString() {
		return this.errorMessage + "<br/>\n" + this.fixMessage;
	}

}
