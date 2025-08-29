package br.jus.pje.jt.entidades;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.Usuario;

@Entity
@Table(name = ControleVersaoDocumento.TABLE_NAME)
public class ControleVersaoDocumento implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ControleVersaoDocumento,Integer> {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_controle_versao_documento";
	
	private Integer idControleVersaoDocumento;
	private Date dataModificacao;
	private String conteudo;
	private String sha1Conteudo;
	private Usuario usuario;
	private ProcessoDocumentoBin processoDocumentoBin;
	private boolean ativo;
	private Integer versao;
	private String localizacaoAtual;
	private String observacao;

	@org.hibernate.annotations.GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_controle_versao_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id_controle_versao_documento", unique = true, nullable = false, updatable = false)
	public Integer getIdControleVersaoDocumento() {
		return idControleVersaoDocumento;
	}
	public void setIdControleVersaoDocumento(Integer idControleVersaoDocumento) {
		this.idControleVersaoDocumento = idControleVersaoDocumento;
	}

	@Column(name="ds_conteudo", nullable=false)
	public String getConteudo() {
		return conteudo;
	}
	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}
	
    @OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario")
	public Usuario getUsuario() {
		return usuario;
	}
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
    @OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento_bin")
	public ProcessoDocumentoBin getProcessoDocumentoBin() {
		return processoDocumentoBin;
	}
	public void setProcessoDocumentoBin(ProcessoDocumentoBin processoDocumentoBin) {
		this.processoDocumentoBin = processoDocumentoBin;
	}

	@Column(name="in_ativo", nullable=false)
	public boolean isAtivo() {
		return ativo;
	}
	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "id_versao_documento", nullable = false)
	public Integer getVersao() {
		return versao;
	}
	public void setVersao(Integer versao) {
		this.versao = versao;
	}

	@Column(name="ds_sha1_conteudo", nullable=false)
	public String getSha1Conteudo() {
		return sha1Conteudo;
	}
	public void setSha1Conteudo(String sha1Conteudo) {
		this.sha1Conteudo = sha1Conteudo;
	}

	@Column(name="dt_modificacao", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDataModificacao() {
		return dataModificacao;
	}
	public void setDataModificacao(Date dataModificacao) {
		this.dataModificacao = dataModificacao;
	}

	@Column(name="ds_observacao", nullable=true)
	public String getObservacao() {
		return observacao;
	}
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
	
	@Column(name="ds_localizacao_atual", nullable=true)
	public String getLocalizacaoAtual() {
		return localizacaoAtual;
	}
	public void setLocalizacaoAtual(String localizacaoAtual) {
		this.localizacaoAtual = localizacaoAtual;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ControleVersaoDocumento> getEntityClass() {
		return ControleVersaoDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdControleVersaoDocumento();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return false;
	}

}
