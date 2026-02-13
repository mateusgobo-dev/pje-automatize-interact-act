package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.jus.pje.nucleo.beans.criminal.TipoProcessoEnum;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TipoEventoCriminalDTO extends PJeServiceApiDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private String codTipoIc;
	private String descricao;
	private Boolean ativo;
	private Boolean exigeTipificacaoDelito;
	private String codigoTribunal;
	private TipoProcessoEnum tipoProcesso;
	
	public String getCodTipoIc() {
		return codTipoIc;
	}
	public void setCodTipoIc(String codTipoIc) {
		this.codTipoIc = codTipoIc;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public Boolean getAtivo() {
		return ativo;
	}
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	public Boolean getExigeTipificacaoDelito() {
		return exigeTipificacaoDelito;
	}
	public void setExigeTipificacaoDelito(Boolean exigeTipificacaoDelito) {
		this.exigeTipificacaoDelito = exigeTipificacaoDelito;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getCodigoTribunal() {
		return codigoTribunal;
	}
	public void setCodigoTribunal(String codigoTribunal) {
		this.codigoTribunal = codigoTribunal;
	}
	public TipoProcessoEnum getTipoProcesso() {
		return tipoProcesso;
	}
	public void setTipoProcesso(TipoProcessoEnum tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(ativo, codTipoIc, codigoTribunal, descricao, exigeTipificacaoDelito, id, tipoProcesso);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TipoEventoCriminalDTO other = (TipoEventoCriminalDTO) obj;
		return Objects.equals(ativo, other.ativo) && Objects.equals(codTipoIc, other.codTipoIc)
				&& Objects.equals(codigoTribunal, other.codigoTribunal) && Objects.equals(descricao, other.descricao)
				&& Objects.equals(exigeTipificacaoDelito, other.exigeTipificacaoDelito) && Objects.equals(id, other.id)
				&& tipoProcesso == other.tipoProcesso;
	}
}
