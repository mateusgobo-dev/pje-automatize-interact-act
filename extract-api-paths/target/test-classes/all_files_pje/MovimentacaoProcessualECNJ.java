package br.jus.cnj.intercomunicacao.v222.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class MovimentacaoProcessualECNJ {
	
	private List<String> complemento = new ArrayList<String>(0);
    private MovimentoNacional movimentoNacional;
    private String movimentoLocal;
    private List<String> idDocumentoVinculado = new ArrayList<String>(0);
    @XmlAttribute(required = true)
    private DataHora dataHora;
    @XmlAttribute
    private Integer nivelSigilo;
    @XmlAttribute
    private String identificadorMovimento;
    @XmlAttribute
    private Integer numeroProtocolo;
    @XmlAttribute
    private Boolean ativo;
   
    private String observacao;
    
	public List<String> getComplemento() {
		return complemento;
	}
	public void setComplemento(List<String> complemento) {
		this.complemento = complemento;
	}
	public MovimentoNacional getMovimentoNacional() {
		return movimentoNacional;
	}
	public void setMovimentoNacional(MovimentoNacional movimentoNacional) {
		this.movimentoNacional = movimentoNacional;
	}
	public String getMovimentoLocal() {
		return movimentoLocal;
	}
	public void setMovimentoLocal(String movimentoLocal) {
		this.movimentoLocal = movimentoLocal;
	}
	public List<String> getIdDocumentoVinculado() {
		return idDocumentoVinculado;
	}
	public void setIdDocumentoVinculado(List<String> idDocumentoVinculado) {
		this.idDocumentoVinculado = idDocumentoVinculado;
	}
	public DataHora getDataHora() {
		return dataHora;
	}
	public void setDataHora(DataHora dataHora) {
		this.dataHora = dataHora;
	}
	public Integer getNivelSigilo() {
		return nivelSigilo;
	}
	public void setNivelSigilo(Integer nivelSigilo) {
		this.nivelSigilo = nivelSigilo;
	}
	public String getIdentificadorMovimento() {
		return identificadorMovimento;
	}
	public void setIdentificadorMovimento(String identificadorMovimento) {
		this.identificadorMovimento = identificadorMovimento;
	}
	public Integer getNumeroProtocolo() {
		return numeroProtocolo;
	}
	public void setNumeroProtocolo(Integer numeroProtocolo) {
		this.numeroProtocolo = numeroProtocolo;
	}
	public Boolean getAtivo() {
		return ativo;
	}
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	public String getObservacao() {
		return observacao;
	}
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
}
