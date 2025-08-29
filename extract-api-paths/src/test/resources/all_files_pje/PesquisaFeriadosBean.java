package br.com.infox.ibpm.component.tree;

import java.io.Serializable;
import java.util.Date;

public class PesquisaFeriadosBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Date dataInicial;
	private Date dataFinal;
	private Integer idEstado;
	private Integer idMunicipio;
	private Integer idOrgaoJulgador;

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public Integer getIdEstado() {
		return idEstado;
	}

	public void setIdEstado(Integer idEstado) {
		this.idEstado = idEstado;
	}

	public Integer getIdMunicipio() {
		return idMunicipio;
	}

	public void setIdMunicipio(Integer idMunicipio) {
		this.idMunicipio = idMunicipio;
	}

	public Integer getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}

	public void setIdOrgaoJulgador(Integer idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
	}
}
