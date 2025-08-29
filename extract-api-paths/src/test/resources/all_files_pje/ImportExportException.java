package br.com.infox.ibpm.exception;

public class ImportExportException extends Exception {

	private static final long serialVersionUID = 1L;
	private String description;

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
