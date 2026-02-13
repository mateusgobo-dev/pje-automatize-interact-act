package br.jus.pje.nucleo.dto.domicilioeletronico;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.jus.pje.nucleo.enums.domicilioeletronico.TipoPessoaDomicilioEnum;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PessoaDomicilioEletronicoDTO {
	@JsonProperty("cpfCnpj")
	private String documento;

	private String tipoDocumento;

	@JsonProperty("tipoPessoa")
	private TipoPessoaDomicilioEnum tipoPessoa;

	@JsonProperty("habilitada")
	private boolean habilitado;

	@JsonProperty("ehPublico")
	private boolean pessoaJuridicaDireitoPublico;
	
	public PessoaDomicilioEletronicoDTO() {
	}

	public PessoaDomicilioEletronicoDTO(String documento, String tipoDocumento, boolean habilitado, boolean pessoaJuridicaDireitoPublico) {
		super();
		this.documento = documento;
		this.tipoDocumento = tipoDocumento;
		this.habilitado = habilitado;
		this.pessoaJuridicaDireitoPublico = pessoaJuridicaDireitoPublico;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public boolean isHabilitado() {
		return habilitado;
	}

	public void setHabilitado(boolean habilitado) {
		this.habilitado = habilitado;
	}

	public boolean isPessoaJuridicaDireitoPublico() {
		return pessoaJuridicaDireitoPublico;
	}

	public void setPessoaJuridicaDireitoPublico(boolean pessoaJuridicaDireitoPublico) {
		this.pessoaJuridicaDireitoPublico = pessoaJuridicaDireitoPublico;
	}

	public TipoPessoaDomicilioEnum getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoaDomicilioEnum tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
		setTipoDocumento(tipoPessoa.getTipoDocumento());
	}

	@Override
	public String toString() {
		return "PessoaDomicilioEletronicoDto [documento=" + documento + ", tipoDocumento=" + tipoDocumento
				+ ", habilitado=" + habilitado + ", pessoaJuridicaDireitoPublico=" + pessoaJuridicaDireitoPublico + "]";
	}

}
