package br.jus.pje.nucleo.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.jus.pje.nucleo.beans.criminal.ConteudoInformacaoCriminalBean;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InformacaoCriminalDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private Long idInformacaoCriminal;
	private ProcessoCriminalDTO processo;
	private ConteudoInformacaoCriminalBean conteudo = new ConteudoInformacaoCriminalBean();
	private ParteDTO parte;

	public InformacaoCriminalDTO() {
		super();
	}

	public InformacaoCriminalDTO(Long id, ConteudoInformacaoCriminalBean conteudo, ParteDTO parte) {
		super();
		this.id = id;
		this.conteudo = conteudo;
		this.parte = parte;
	}
	
	public Long getIdInformacaoCriminal() {
		return idInformacaoCriminal;
	}

	public void setIdInformacaoCriminal(Long idInformacaoCriminal) {
		this.idInformacaoCriminal = idInformacaoCriminal;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ConteudoInformacaoCriminalBean getConteudo() {
		return conteudo;
	}

	public void setConteudo(ConteudoInformacaoCriminalBean conteudo) {
		this.conteudo = conteudo;
	}

	public ParteDTO getParte() {
		return parte;
	}

	public void setParte(ParteDTO parte) {
		this.parte = parte;
	}

	public ProcessoCriminalDTO getProcesso() {
		return processo;
	}

	public void setProcesso(ProcessoCriminalDTO processo) {
		this.processo = processo;
	}

}
