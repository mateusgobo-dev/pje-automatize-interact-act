package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;


public class RegistroCienciaDTO implements Serializable {

	
	private static final long serialVersionUID = 1L;
	private Integer idProcessoParteExpediente;
	private String numeroProcesso;
	private String usuario;
	private String nomeUsuario;
	private String dataCiencia;
	
	public RegistroCienciaDTO() {
		//Construtor.
	}

	/**
	 * @return the idProcessoParteExpediente
	 */
	public Integer getIdProcessoParteExpediente() {
		return idProcessoParteExpediente;
	}

	/**
	 * @param idProcessoParteExpediente the idProcessoParteExpediente to set
	 */
	public void setIdProcessoParteExpediente(Integer idProcessoParteExpediente) {
		this.idProcessoParteExpediente = idProcessoParteExpediente;
	}

	/**
	 * @return the numeroProcesso
	 */
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	/**
	 * @param numeroProcesso the numeroProcesso to set
	 */
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	/**
	 * @return the usuario
	 */
	public String getUsuario() {
		return usuario;
	}

	/**
	 * @param usuario the usuario to set
	 */
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	/**
	 * @return the nomeUsuario
	 */
	public String getNomeUsuario() {
		return nomeUsuario;
	}

	/**
	 * @param nomeUsuario the nomeUsuario to set
	 */
	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}

	/**
	 * @return the dataCiencia
	 */
	public String getDataCiencia() {
		return dataCiencia;
	}

	/**
	 * @param dataCiencia the dataCiencia to set
	 */
	public void setDataCiencia(String dataCiencia) {
		this.dataCiencia = dataCiencia;
	}
	
	/**
	 * @param dataCiencia the dataCiencia to set
	 */
	public void setDataCiencia(Date dataCiencia) {
		if (!StringUtils.isBlank(nomeUsuario)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		    sdf.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
		    setDataCiencia(sdf.format(dataCiencia));
		}
	}
}
