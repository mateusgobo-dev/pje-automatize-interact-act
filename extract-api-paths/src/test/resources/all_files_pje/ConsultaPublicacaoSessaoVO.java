package br.jus.cnj.pje.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.enums.SituacaoPublicacaoLiberacaoEnum;
import br.jus.pje.nucleo.enums.TipoPublicacaoEnum;

public class ConsultaPublicacaoSessaoVO {

	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private Integer numeroOrigem;
	private String ramoJustica;
	private String respectivoTribunal;
	
	private Date dataSessao;
	private Date dataPublicacao;
	
	private TipoPublicacaoEnum tipoPublicacao;
	private OrgaoJulgador orgaoJulgador;
	
	private List<SituacaoPublicacaoLiberacaoEnum> situacaoLiberacao;
	
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}
	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}
	public Integer getDigitoVerificador() {
		return digitoVerificador;
	}
	public void setDigitoVerificador(Integer digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}
	public Integer getAno() {
		return ano;
	}
	public void setAno(Integer ano) {
		this.ano = ano;
	}
	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}
	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}
	public String getRamoJustica() {
		return ramoJustica;
	}
	public void setRamoJustica(String ramoJustica) {
		this.ramoJustica = ramoJustica;
	}
	public String getRespectivoTribunal() {
		return respectivoTribunal;
	}
	public void setRespectivoTribunal(String respectivoTribunal) {
		this.respectivoTribunal = respectivoTribunal;
	}
	public Date getDataSessao() {
		return dataSessao;
	}
	public void setDataSessao(Date dataSessao) {
		this.dataSessao = dataSessao;
	}
	public Date getDataPublicacao() {
		return dataPublicacao;
	}
	public void setDataPublicacao(Date dataPublicacao) {
		this.dataPublicacao = dataPublicacao;
	}
	public TipoPublicacaoEnum getTipoPublicacao() {
		return tipoPublicacao;
	}
	public void setTipoPublicacao(TipoPublicacaoEnum tipoPublicacao) {
		this.tipoPublicacao = tipoPublicacao;
	}
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	public List<SituacaoPublicacaoLiberacaoEnum> getSituacaoLiberacao() {
		if(situacaoLiberacao == null){
			situacaoLiberacao = new ArrayList<SituacaoPublicacaoLiberacaoEnum>();
		}
		return situacaoLiberacao;
	}
	public void setSituacaoLiberacao(List<SituacaoPublicacaoLiberacaoEnum> situacaoLiberacao) {
		this.situacaoLiberacao = situacaoLiberacao;
	}
}