package br.jus.cnj.pje.amqp.model.dto;

import java.io.Serializable;
import java.util.Date;

import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;

public class ProcessoParteExpedienteCloudEvent implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Integer idProcessoParteExpediente;
	private Integer idProcesso;
	private String numeroProcesso;
	private Integer idProcessoDocumento;
	
	private Date dtCriacao;
	private Date dtPrazoLegal;
	private String tipoPrazo;
	private Integer prazoLegal;
	private Date dtCienciaParte;
	private Boolean cienciaSistema;

	private Integer idPessoa;
	private String nomePessoa;
	private String cpfCnpjPessoa;
	private Boolean fechado = Boolean.FALSE;

	public ProcessoParteExpedienteCloudEvent(ProcessoParteExpediente processoParteExpediente) {
		super();
		this.idProcessoParteExpediente = processoParteExpediente.getIdProcessoParteExpediente();
		if(processoParteExpediente.getProcessoJudicial() != null){
			this.idProcesso = processoParteExpediente.getProcessoJudicial().getIdProcessoTrf();
			this.numeroProcesso = processoParteExpediente.getProcessoJudicial().getNumeroProcesso();
		}
		if(processoParteExpediente.getProcessoDocumento() != null) {
			this.idProcessoDocumento = processoParteExpediente.getProcessoDocumento().getIdProcessoDocumento();
		}
		this.dtCriacao = processoParteExpediente.getProcessoExpediente().getDtCriacao();
		this.dtPrazoLegal = processoParteExpediente.getDtPrazoLegal();
		this.tipoPrazo = processoParteExpediente.getTipoPrazo().toString();
		this.prazoLegal = processoParteExpediente.getPrazoLegal();
		this.dtCienciaParte = processoParteExpediente.getDtCienciaParte();
		this.cienciaSistema = processoParteExpediente.getCienciaSistema();

		if(processoParteExpediente.getPessoaParte() != null) {
			this.idPessoa = processoParteExpediente.getPessoaParte().getIdPessoa();
			this.cpfCnpjPessoa = processoParteExpediente.getPessoaParte().getDocumentoCpfCnpj();
		}
		this.nomePessoa = processoParteExpediente.getNomePessoaParte();
		this.fechado = processoParteExpediente.getFechado();
	}
	
	public ProcessoParteExpedienteCloudEvent() {
		super();
	}
	
	public Integer getIdProcessoParteExpediente() {
		return idProcessoParteExpediente;
	}

	public void setIdProcessoParteExpediente(Integer idProcessoParteExpediente) {
		this.idProcessoParteExpediente = idProcessoParteExpediente;
	}

	public Integer getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso = idProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public Integer getIdProcessoDocumento() {
		return idProcessoDocumento;
	}

	public void setIdProcessoDocumento(Integer idProcessoDocumento) {
		this.idProcessoDocumento = idProcessoDocumento;
	}

	public Date getDtCriacao() {
		return dtCriacao;
	}

	public void setDtCriacao(Date dtCriacao) {
		this.dtCriacao = dtCriacao;
	}

	public Date getDtPrazoLegal() {
		return dtPrazoLegal;
	}

	public void setDtPrazoLegal(Date dtPrazoLegal) {
		this.dtPrazoLegal = dtPrazoLegal;
	}

	public String getTipoPrazo() {
		return tipoPrazo;
	}

	public void setTipoPrazo(String tipoPrazo) {
		this.tipoPrazo = tipoPrazo;
	}

	public Integer getPrazoLegal() {
		return prazoLegal;
	}

	public void setPrazoLegal(Integer prazoLegal) {
		this.prazoLegal = prazoLegal;
	}

	public Date getDtCienciaParte() {
		return dtCienciaParte;
	}

	public void setDtCienciaParte(Date dtCienciaParte) {
		this.dtCienciaParte = dtCienciaParte;
	}

	public Boolean getCienciaSistema() {
		return cienciaSistema;
	}

	public void setCienciaSistema(Boolean cienciaSistema) {
		this.cienciaSistema = cienciaSistema;
	}

	public Integer getIdPessoa() {
		return idPessoa;
	}

	public void setIdPessoa(Integer idPessoa) {
		this.idPessoa = idPessoa;
	}

	public String getNomePessoa() {
		return nomePessoa;
	}

	public void setNomePessoa(String nomePessoa) {
		this.nomePessoa = nomePessoa;
	}

	public String getCpfCnpjPessoa() {
		return cpfCnpjPessoa;
	}

	public void setCpfCnpjPessoa(String cpfCnpjPessoa) {
		this.cpfCnpjPessoa = cpfCnpjPessoa;
	}

	public Boolean getFechado() {
		return fechado;
	}

	public void setFechado(Boolean fechado) {
		this.fechado = fechado;
	}
}
