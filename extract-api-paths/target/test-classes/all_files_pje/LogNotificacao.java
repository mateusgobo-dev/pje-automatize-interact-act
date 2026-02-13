package br.jus.pje.nucleo.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "tb_log_notificacao")
public class LogNotificacao implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private Date data;
	private String nrProcesso;
	private String payload;
	private String idNotificacao;
	private Boolean sucesso;
	private String mensagemErro;
	private String ipRequisicao;
	
	@org.hibernate.annotations.GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
			@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_log_notificacao"),
			@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id", unique = true, nullable = false)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data")
	public Date getData() {
		return data;
	}
	public void setData(Date data) {
		this.data = data;
	}
	
	@Column(name = "nr_processo")
	public String getNrProcesso() {
		return nrProcesso;
	}
	
	public void setNrProcesso(String nrProcesso) {
		this.nrProcesso = nrProcesso;
	}
	
	@Column(name = "ds_payload")
	public String getPayload() {
		return payload;
	}
	
	public void setPayload(String payload) {
		this.payload = payload;
	}
	
	@Column(name = "id_notificacao")
	public String getIdNotificacao() {
		return idNotificacao;
	}
	
	public void setIdNotificacao(String idNotificacao) {
		this.idNotificacao = idNotificacao;
	}
	
	@Column(name = "in_sucesso")
	public Boolean getSucesso() {
		return sucesso;
	}
	
	public void setSucesso(Boolean sucesso) {
		this.sucesso = sucesso;
	}
	
	@Column(name = "ds_mensagem_erro")
	public String getMensagemErro() {
		return mensagemErro;
	}
	
	public void setMensagemErro(String mensagemErro) {
		this.mensagemErro = mensagemErro;
	}
	
	@Column(name = "ds_ip_requisicao")
	public String getIpRequisicao() {
		return ipRequisicao;
	}

	public void setIpRequisicao(String ipRequisicao) {
		this.ipRequisicao = ipRequisicao;
	}
	
	public static LogNotificacao createNew() {
		return new LogNotificacao();
	}
	

		
}
