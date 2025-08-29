package br.jus.pje.nucleo.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.PlataformaDispositivoEnum;

@Entity
@Table(name = UsuarioMobile.TABLE_NAME)
public class UsuarioMobile implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_usuario_mobile";
	private static final long serialVersionUID = 1L;

	private Integer idUsuarioMobile;
	private Usuario usuario;
	private String codigoPareamento;
	private Date dataCadastro;
	private PlataformaDispositivoEnum plataforma;
	private Boolean ativo;
	private Boolean pareamentoRealizado;
	private String versaoPlataforma;
	private String nomeDispositivo;
	
	
	public UsuarioMobile() {
		super();
	}
	
	public UsuarioMobile(Integer idUsuarioMobile) {
		this();
		this.idUsuarioMobile = idUsuarioMobile;
	}


	@SequenceGenerator(name = "generator", sequenceName = "sq_tb_usuario_mobile")
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id_usuario_mobile", columnDefinition = "integer", unique = true, nullable = false)
	@NotNull
	public Integer getIdUsuarioMobile() {
		return idUsuarioMobile;
	}


	public void setIdUsuarioMobile(Integer idUsuarioMobile) {
		this.idUsuarioMobile = idUsuarioMobile;
	}

	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", nullable = false)
	@NotNull
	public Usuario getUsuario() {
		return usuario;
	}


	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Column(name = "ds_codigo_pareamento",columnDefinition = "character varying(200)")
	public String getCodigoPareamento() {
		return codigoPareamento;
	}


	public void setCodigoPareamento(String codigoPareamento) {
		this.codigoPareamento = codigoPareamento;
	}
	
	
	@Column(name = "dt_cadastro")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDataCadastro() {
		return dataCadastro;
	}


	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	@Column(name = "tp_plataforma_mobile",columnDefinition = "character(1)")
	@Enumerated(EnumType.STRING)
	public PlataformaDispositivoEnum getPlataforma() {
		return plataforma;
	}


	public void setPlataforma(PlataformaDispositivoEnum plataforma) {
		this.plataforma = plataforma;
	}
	
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}


	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Column(name = "in_pareamento_realizado", nullable = false)
	@NotNull
	public Boolean getPareamentoRealizado() {
		return pareamentoRealizado;
	}


	public void setPareamentoRealizado(Boolean pareamentoRealizado) {
		this.pareamentoRealizado = pareamentoRealizado;
	}
	
	@Column(name = "versao_plataforma")
	public String getVersaoPlataforma() {
		return versaoPlataforma;
	}


	public void setVersaoPlataforma(String versaoPlataforma) {
		this.versaoPlataforma = versaoPlataforma;
	}

	@Column(name = "nome_dispositivo")
	public String getNomeDispositivo() {
		return nomeDispositivo;
	}


	public void setNomeDispositivo(String nomeDispositivo) {
		this.nomeDispositivo = nomeDispositivo;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UsuarioMobile)) {
			return false;
		}
		UsuarioMobile other = (UsuarioMobile) obj;
		if (getIdUsuarioMobile() != other.getIdUsuarioMobile()){
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdUsuarioMobile();
		return result;
	}


}