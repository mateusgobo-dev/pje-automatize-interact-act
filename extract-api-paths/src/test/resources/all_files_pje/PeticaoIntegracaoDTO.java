package br.jus.pje.nucleo.dto.portal;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

public class PeticaoIntegracaoDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
    private String protocolo;

	@NotNull
	private List<DocumentoDTO> documentos;

	@NotNull
	@JsonFormat(pattern="dd/MM/yyyy HH:mm:ss")
	private Date dataEnvio;
	
	@Size(max = 25)
    private String numeroProcesso;
	
	private Long idAvisoExpediente;
	
    @NotNull
    @Size(max = 11)
    private String cpfPeticionante;

    @NotNull
    @Size(max = 255)
    private String nomePeticionante;
	
	@Override
	public String toString() {
		return "PeticaoRequestDTO [protocolo=" + protocolo + ", documentos=" + documentos
				+ ", dataEnvio=" + dataEnvio + "]";
	}

	public String getProtocolo() {
		return protocolo;
	}

	public void setProtocolo(String protocolo) {
		this.protocolo = protocolo;
	}

	public List<DocumentoDTO> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<DocumentoDTO> documentos) {
		this.documentos = documentos;
	}

	public Date getDataEnvio() {
		return dataEnvio;
	}

	public void setDataEnvio(Date dataEnvio) {
		this.dataEnvio = dataEnvio;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public Long getIdAvisoExpediente() {
		return idAvisoExpediente;
	}

	public void setIdAvisoExpediente(Long idAvisoExpediente) {
		this.idAvisoExpediente = idAvisoExpediente;
	}

	public String getCpfPeticionante() {
		return cpfPeticionante;
	}

	public void setCpfPeticionante(String cpfPeticionante) {
		this.cpfPeticionante = cpfPeticionante;
	}

	public String getNomePeticionante() {
		return nomePeticionante;
	}

	public void setNomePeticionante(String nomePeticionante) {
		this.nomePeticionante = nomePeticionante;
	}
}
