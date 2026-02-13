package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto;

import java.io.Serializable;
import java.util.Date;

import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.util.DateUtil;


public class AtualizacaoDataCienciaDTO implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	private Integer idProcessoParteExpediente;
	private String dataFinalCiencia;
	private String numeroProcesso;
	
	public AtualizacaoDataCienciaDTO() {
		//fConstrutor.
	}
	
	/**
	 * @return idProcessoParteExpediente
	 */
	public Integer getIdProcessoParteExpediente() {
		return idProcessoParteExpediente;
	}

	/**
	 * @param idProcessoParteExpediente Atribuir idProcessoParteExpediente
	 */
	public void setIdProcessoParteExpediente(Integer idProcessoParteExpediente) {
		this.idProcessoParteExpediente = idProcessoParteExpediente;
	}

	public AtualizacaoDataCienciaDTO(ProcessoParteExpediente ppe) {
		if (ppe != null) {
			setDataFinalCiencia(ppe.getDtPrazoLegal().toInstant().toString());
		}
	}
	
	public AtualizacaoDataCienciaDTO(Date prazoLegal) {
		setDataFinalCiencia(DateUtil.formatarDataParaISO8601(prazoLegal));
	}

	public String getDataFinalCiencia() {
		return dataFinalCiencia;
	}
	
	public void setDataFinalCiencia(String dataFinalCiencia) {
		this.dataFinalCiencia = dataFinalCiencia;
	}

	/**
	 * @return numeroProcesso
	 */
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	/**
	 * @param numeroProcesso Atribuir numeroProcesso
	 */
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}
	
}
