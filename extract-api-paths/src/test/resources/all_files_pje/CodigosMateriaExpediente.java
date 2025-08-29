package br.jus.cnj.pje.controleprazos;

import java.io.Serializable;
import java.util.List;

public class CodigosMateriaExpediente implements Serializable {

	private static final long serialVersionUID = 1L;

	Integer idExpediente;

	List<String> codigosMateria;

	public CodigosMateriaExpediente() {
		super();
	}

	public CodigosMateriaExpediente(Integer idExpediente, List<String> codigosMateria) {
		super();
		this.idExpediente = idExpediente;
		this.codigosMateria = codigosMateria;
	}

	public Integer getIdExpediente() {
		return idExpediente;
	}

	public void setIdExpediente(Integer idExpediente) {
		this.idExpediente = idExpediente;
	}

	public List<String> getCodigosMateria() {
		return codigosMateria;
	}

	public void setCodigosMateria(List<String> codigosMateria) {
		this.codigosMateria = codigosMateria;
	}
}
