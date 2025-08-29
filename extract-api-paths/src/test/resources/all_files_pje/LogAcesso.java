package br.jus.pje.nucleo.entidades.identidade;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.log.Ignore;

@Entity
@Table(name = LogAcesso.TABLE_NAME)
@Ignore
@org.hibernate.annotations.GenericGenerator(name = "gen_log_acesso", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_log_acesso"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1") })
public class LogAcesso implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<LogAcesso, Long> {

	private static final long serialVersionUID = 4146585544072615355L;

	public static final String TABLE_NAME = "tb_log_acesso";

	private Long idLogAcesso;
	private Date dataEvento;
	private Boolean bemSucedido;
	private UsuarioLogin usuarioLogin;

	private String server;

	private Boolean comCertificado;

	private String ip;

	@Id
	@GeneratedValue(generator = "gen_log_acesso")
	@Column(name = "id_log_acesso", unique = true, nullable = false)
	public Long getIdLogAcesso() {
		return idLogAcesso;
	}

	public void setIdLogAcesso(Long idLogAcesso) {
		this.idLogAcesso = idLogAcesso;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_evento", unique = false, nullable = false)
	public Date getDataEvento() {
		return dataEvento;
	}

	public void setDataEvento(Date dataEvento) {
		this.dataEvento = dataEvento;
	}

	@Column(name = "in_bem_sucedido", nullable = false)
	public Boolean getBemSucedido() {
		return bemSucedido;
	}

	public void setBemSucedido(Boolean bemSucedido) {
		this.bemSucedido = bemSucedido;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", nullable = false)
	public UsuarioLogin getUsuarioLogin() {
		return usuarioLogin;
	}

	public void setUsuarioLogin(UsuarioLogin usuarioLogin) {
		this.usuarioLogin = usuarioLogin;
	}

	@Override
	public String toString() {
		if (getDataEvento() != null && getUsuarioLogin().getLogin() != null && getBemSucedido() != null) {
			return getDataEvento() + " - " + getUsuarioLogin().getLogin() + " - " + getBemSucedido();
		}

		return null;
	}

	/**
	 * compara se a pessoa passada em parametro é igual a pessoa deste objeto
	 * 
	 * @param _pessoa
	 * @return
	 */
	public boolean isUsuarioEquals(Pessoa _pessoa) {
		return (usuarioLogin.getIdUsuario().equals(_pessoa.getIdPessoa()));
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends LogAcesso> getEntityClass() {
		return LogAcesso.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdLogAcesso();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
	
	@Column(name = "ds_ip", length = 100)
	@Length(max = 100)
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	

	@Column(name = "ds_server", length = 100)
	@Length(max = 100)
	public String getServer() {
		return this.server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	@Column(name = "in_com_certificado", nullable = false)
	public Boolean getComCertificado() {
		return comCertificado;
	}

	public void setComCertificado(Boolean comCertificado) {
		this.comCertificado = comCertificado;
	}
}
