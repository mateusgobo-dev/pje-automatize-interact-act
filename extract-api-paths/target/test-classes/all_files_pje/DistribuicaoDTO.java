package br.jus.pje.nucleo.dto.portal;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;


public class DistribuicaoDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
    private String numeroProcesso;
    
	@NotNull
	@JsonFormat(pattern="dd/MM/yyyy HH:mm:ss")
    private Date dataHoraDistribuicao;
    
	@NotNull
	@Size(max = 255)
    private String nomeOrgaoJulgadorDistribuido;
    
	@NotNull
    private Long idOrgaoJulgadorDistribuidoLocal;
    
    private Long idOrgaoJulgadorDistribuidoLocalCorporativo;
	
	@Override
	public String toString() {
		return "DistribuicaoDTO [numeroProcesso=" + numeroProcesso + ", dataHoraDistribuicao="
				+ dataHoraDistribuicao + ", nomeOrgaoJulgadorDistribuido=" + nomeOrgaoJulgadorDistribuido
				+ ", idOrgaoJulgadorDistribuidoLocal=" + idOrgaoJulgadorDistribuidoLocal
				+ ", idOrgaoJulgadorDistribuidoLocalCorporativo=" + idOrgaoJulgadorDistribuidoLocalCorporativo + "]";
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public Date getDataHoraDistribuicao() {
		return dataHoraDistribuicao;
	}

	public void setDataHoraDistribuicao(Date dataHoraDistribuicao) {
		this.dataHoraDistribuicao = dataHoraDistribuicao;
	}

	public String getNomeOrgaoJulgadorDistribuido() {
		return nomeOrgaoJulgadorDistribuido;
	}

	public void setNomeOrgaoJulgadorDistribuido(String nomeOrgaoJulgadorDistribuido) {
		this.nomeOrgaoJulgadorDistribuido = nomeOrgaoJulgadorDistribuido;
	}

	public Long getIdOrgaoJulgadorDistribuidoLocal() {
		return idOrgaoJulgadorDistribuidoLocal;
	}

	public void setIdOrgaoJulgadorDistribuidoLocal(Long idOrgaoJulgadorDistribuidoLocal) {
		this.idOrgaoJulgadorDistribuidoLocal = idOrgaoJulgadorDistribuidoLocal;
	}

	public Long getIdOrgaoJulgadorDistribuidoLocalCorporativo() {
		return idOrgaoJulgadorDistribuidoLocalCorporativo;
	}

	public void setIdOrgaoJulgadorDistribuidoLocalCorporativo(Long idOrgaoJulgadorDistribuidoLocalCorporativo) {
		this.idOrgaoJulgadorDistribuidoLocalCorporativo = idOrgaoJulgadorDistribuidoLocalCorporativo;
	}
}
