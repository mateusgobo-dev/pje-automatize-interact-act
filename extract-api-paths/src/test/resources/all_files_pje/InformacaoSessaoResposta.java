package br.jus.cnj.pje.webservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class InformacaoSessaoResposta implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7044959423391988062L;
	
	private String sessao;
	private String tipoSessao;
	private String data;
	private String horarioInicio;
	private String dataFim;
	private String horarioFim;
	private List<InformacaoSessaoProcesso> processos = new ArrayList<InformacaoSessaoProcesso>(0);
	private List<InformacaoSessaoProcesso> processosEmJulgamento = new ArrayList<InformacaoSessaoProcesso>(0);
	private List<InformacaoSessaoResumo> resumo = new ArrayList<InformacaoSessaoResumo>(0);
	private String status;
	private Integer id;
	private Integer qtdProcessos;
	private Boolean virtual;
	private Boolean iniciar;
	private String dataRealizacaoSessao;
	
	
	@XmlElement(name="sessao")
	public String getSessao() {
		return sessao;
	}
	public void setSessao(String sessao) {
		this.sessao = sessao;
	}
	
	@XmlElement(name="tipoSessao")
	public String getTipoSessao() {
		return tipoSessao;
	}
	public void setTipoSessao(String tipoSessao) {
		this.tipoSessao = tipoSessao;
	}
	
	@XmlElement(name="data")
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
	@XmlElement(name="horarioInicio")
	public String getHorarioInicio() {
		return horarioInicio;
	}
	public void setHorarioInicio(String horarioInicio) {
		this.horarioInicio = horarioInicio;
	}
	
	@XmlElement(name="dataFim")
	public String getDataFim() {
		return dataFim;
	}
	public void setDataFim(String dataFim) {
		this.dataFim = dataFim;
	}
	
	@XmlElement(name="horarioFim")
	public String getHorarioFim() {
		return horarioFim;
	}
	public void setHorarioFim(String horarioFim) {
		this.horarioFim = horarioFim;
	}
	
	@XmlElement(name="processos")
	public List<InformacaoSessaoProcesso> getProcessos() {
		return processos;
	}
	public void setProcessos(List<InformacaoSessaoProcesso> processos) {
		this.processos = processos;
	}
	
	@XmlElement(name="processosEmJulgamento")
	public List<InformacaoSessaoProcesso> getProcessosEmJulgamento() {
		return processosEmJulgamento;
	}
	public void setProcessosEmJulgamento(List<InformacaoSessaoProcesso> processosEmJulgamento) {
		this.processosEmJulgamento = processosEmJulgamento;
	}
	
	@XmlElement(name="resumo")
	public List<InformacaoSessaoResumo> getResumo() {
		return resumo;
	}
	public void setResumo(List<InformacaoSessaoResumo> resumo) {
		this.resumo = resumo;
	}
	
	@XmlElement(name="status")
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	@XmlElement(name="idSessao")
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public void setQtdProcessos(Integer qtdProcessos) {
		this.qtdProcessos = qtdProcessos;
	}
	@XmlElement(name="quantidadeProcessos")
	public Integer getQtdProcessos() {
		return qtdProcessos;
	}
	
	@XmlElement(name="virtual")
	public Boolean getVirtual() {
		return virtual;
	}
	public void setVirtual(Boolean virtual) {
		this.virtual = virtual;
	}
	@XmlElement(name="iniciada")
	public Boolean getIniciar() {
		return iniciar;
	}
	public void setIniciar(Boolean iniciar) {
		this.iniciar = iniciar;
	}
	@XmlElement(name="dataRealizacaoSessao")
	public String getDataRealizacaoSessao() {
		return dataRealizacaoSessao;
	}
	public void setDataRealizacaoSessao(String dataRealizacaoSessao) {
		this.dataRealizacaoSessao = dataRealizacaoSessao;
	}
	
}