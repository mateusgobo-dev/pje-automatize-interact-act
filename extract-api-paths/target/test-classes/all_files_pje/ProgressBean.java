package br.jus.cnj.pje.util.checkup.spi;

import java.io.Serializable;
import java.util.List;

// Como usamos o JobStoreCMT no quartz, não é possível obter o retorno para um ProgressBar(em um método @Asynchronous).
// Também não é possível armazenar no escopo de EVENT, CONVERSATION ou SESSION, pois o método assíncrono roda em
// um contexto diferente do caller.
public class ProgressBean implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Boolean isFinished = true;
	protected List<CheckupError> errors;
	
	public Boolean getIsFinished() {
		return isFinished;
	}
	public void setIsFinished(Boolean isFinished) {
		this.isFinished = isFinished;
	}
	public List<CheckupError> getErrors() {
		return errors;
	}
	public void setErrors(List<CheckupError> errors) {
		this.errors = errors;
	}
	
}
