package br.jus.pje.nucleo.entidades;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import br.jus.pje.nucleo.enums.TipoDocumentoSessaoEnum;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = DocumentoSessao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_documento_sessao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_documento_sessao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DocumentoSessao implements IEntidade<DocumentoSessao,Integer> {
	
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_documento_sessao";
	
	private Integer idDocumentoSessao;
	private String modeloDocumentoSessao;
	private Usuario usuario;
	private String signature;
	private String certChain;
	private Date dataAssinatura;
	private Sessao sessao;
	private TipoDocumentoSessaoEnum tipoDocumento = TipoDocumentoSessaoEnum.A;
	
	@Id
	@GeneratedValue(generator = "gen_documento_sessao")
	@Column(name = "id_documento_sessao", unique = true, nullable = false)
	@NotNull	
	public Integer getIdDocumentoSessao() {
		return idDocumentoSessao;
	}

	public void setIdDocumentoSessao(Integer idDocumentoSessao) {
		this.idDocumentoSessao = idDocumentoSessao;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_modelo_documento_sessao")	
	public String getModeloDocumentoSessao() {
		return modeloDocumentoSessao;
	}
	public void setModeloDocumentoSessao(String modeloDocumentoSessao) {
		this.modeloDocumentoSessao = modeloDocumentoSessao;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario")	
	public Usuario getUsuario() {
		return usuario;
	}
	
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_signature")
	public String getSignature() {
		return signature;
	}
	
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_cert_chain")	
	public String getCertChain() {
		return certChain;
	}
	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}
	
	@Column(name = "dt_assinatura")
	public Date getDataAssinatura() {
		return dataAssinatura;
	}
	
	public void setDataAssinatura(Date dataAssinatura) {
		this.dataAssinatura = dataAssinatura;
	}
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "id_sessao")
	public Sessao getSessao() {
		return sessao;
	}
	
	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}
	
	@Column(name = "tp_documento_sessao", nullable = false, length = 1)
	@Enumerated(EnumType.STRING)
	@NotNull	
	public TipoDocumentoSessaoEnum getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumentoSessaoEnum tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DocumentoSessao> getEntityClass() {
		return DocumentoSessao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdDocumentoSessao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}