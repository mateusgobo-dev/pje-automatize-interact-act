package br.jus.cnj.pje.webservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InformacaoAudiencia {

	private Date dtInicio;
	private String nomeRealizador;
	private String nomeConciliador;
	private String tipoAudiencia;
	private String numeroProcesso;
	private String nomeOrgaoJulgador;
	private String nomeJurisdicao;
	private Integer idSala;
	
	private List<InformacaoAudienciaParte> partes = new ArrayList<InformacaoAudienciaParte>();

	public Date getDtInicio() {
		return dtInicio;
	}

	public void setDtInicio(Date dtInicio) {
		this.dtInicio = dtInicio;
	}

	public String getNomeRealizador() {
		return nomeRealizador;
	}

	public void setNomeRealizador(String nomeRealizador) {
		this.nomeRealizador = nomeRealizador;
	}

	public String getNomeConciliador() {
		return nomeConciliador;
	}

	public void setNomeConciliador(String nomeConciliador) {
		this.nomeConciliador = nomeConciliador;
	}

	public String getTipoAudiencia() {
		return tipoAudiencia;
	}

	public void setTipoAudiencia(String tipoAudiencia) {
		this.tipoAudiencia = tipoAudiencia;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNomeOrgaoJulgador() {
		return nomeOrgaoJulgador;
	}

	public void setNomeOrgaoJulgador(String nomeOrgaoJulgador) {
		this.nomeOrgaoJulgador = nomeOrgaoJulgador;
	}
	
	public String getNomeJurisdicao() {
		return nomeJurisdicao;
	}
	
	public void setNomeJurisdicao(String nomeJurisdicao) {
		this.nomeJurisdicao = nomeJurisdicao;
	}

	public List<InformacaoAudienciaParte> getPartes() {
		return partes;
	}

	public void setPartes(List<InformacaoAudienciaParte> partes) {
		this.partes = partes;
	}
	
	public Integer getIdSala() {
		return idSala;
	}
	
	public void setIdSala(Integer idSala) {
		this.idSala = idSala;
	}
}
