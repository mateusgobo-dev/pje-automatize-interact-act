package br.jus.pje.nucleo.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.TipoOperacaoDocumentoHistoricoEnum;
import br.jus.pje.nucleo.entidades.log.Ignore;

@Entity
@Ignore
@Table(name = "tb_documento_historico")
@org.hibernate.annotations.GenericGenerator(name = "gen_documento_historico", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_documento_historico"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DocumentoHistorico implements java.io.Serializable{
	
	private static final long serialVersionUID = 5607827904567711771L;
	
	@Id
	@GeneratedValue(generator = "gen_documento_historico")
	@Column(name = "id_documento_historico", unique = true, nullable = false)
	private Long idDocumentoHistorico;
	
	@OneToOne
	@JoinColumn(name = "id_documento", nullable = false)
	@NotNull
	private Documento documento;
	
	@Column(name = "tp_operacao", nullable = false, length = 1)
	@NotNull
	@Enumerated(EnumType.STRING)
	private TipoOperacaoDocumentoHistoricoEnum tipoOperacao;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_operacao", nullable = false)
	@NotNull
	private Date dataOperacao;
	
	@Column(name = "ds_motivo_exclusao", length = 400, nullable = true)
	private String motivoExclusao;
	
	@ManyToOne
	@JoinColumn(name = "id_responsavel", nullable = false)
	@NotNull
	private Usuario responsavel;
	
	@Column(name = "ds_conteudo_documento", nullable = false)
	@NotNull
	private String conteudoDocumento;

	public Long getIdDocumentoHistorico() {
		return idDocumentoHistorico;
	}

	public void setIdDocumentoHistorico(Long idDocumentoHistorico) {
		this.idDocumentoHistorico = idDocumentoHistorico;
	}

	public Documento getDocumento() {
		return documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	public TipoOperacaoDocumentoHistoricoEnum getTipoOperacao() {
		return tipoOperacao;
	}

	public void setTipoOperacao(TipoOperacaoDocumentoHistoricoEnum tipoOperacao) {
		this.tipoOperacao = tipoOperacao;
	}

	public Date getDataOperacao() {
		return dataOperacao;
	}

	public void setDataOperacao(Date dataOperacao) {
		this.dataOperacao = dataOperacao;
	}

	public String getMotivoExclusao() {
		return motivoExclusao;
	}

	public void setMotivoExclusao(String motivoExclusao) {
		this.motivoExclusao = motivoExclusao;
	}

	public Usuario getResponsavel() {
		return responsavel;
	}

	public void setResponsavel(Usuario responsavel) {
		this.responsavel = responsavel;
	}

	public String getConteudoDocumento() {
		return conteudoDocumento;
	}

	public void setConteudoDocumento(String conteudoDocumento) {
		this.conteudoDocumento = conteudoDocumento;
	}
	
}
