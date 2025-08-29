package br.jus.cnj.pje.webservice.client.bnmp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.jus.cnj.pje.pjecommons.model.services.bnmp.OrgaoDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.domain.StatusDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PecaDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

    private String numeroIndividuo;

	private String numeroPeca;
    
    private String numeroProcesso;
    
    private StatusDTO status;
    
    private OrgaoDTO orgao;
    
    
    
    
    public PecaDTO() {
	}



	public PecaDTO(Long id, String numeroIndividuo, String numeroPeca, String numeroProcesso, Long idStatus,
            String descricaoStatus) {
		this.id = id;
		this.numeroIndividuo = numeroIndividuo;
		this.numeroPeca = numeroPeca;
		this.numeroProcesso = numeroProcesso;
		this.status = new StatusDTO(idStatus,descricaoStatus);
	}
    
    

	public PecaDTO(String numeroIndividuo) {
		this.numeroIndividuo = numeroIndividuo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


    public String getNumeroIndividuo() {
		return numeroIndividuo;
	}

	public void setNumeroIndividuo(String numeroIndividuo) {
		this.numeroIndividuo = numeroIndividuo;
	}
	
	
	public String getNumeroPeca() {
		return numeroPeca;
	}

	public void setNumeroPeca(String numeroPeca) {
		this.numeroPeca = numeroPeca;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public StatusDTO getStatus() {
		return status;
	}

	public void setStatus(StatusDTO status) {
		this.status = status;
	}



	public OrgaoDTO getOrgao() {
		return orgao;
	}



	public void setOrgao(OrgaoDTO orgao) {
		this.orgao = orgao;
	}


}
