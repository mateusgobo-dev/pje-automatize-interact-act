/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades.editor;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.log.Ignore;
import br.jus.pje.nucleo.enums.editor.TipoOperacaoTopicoEnum;

@Entity
@Ignore
@Table(name=HistoricoProcessoDocumentoEstruturadoTopico.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_hist_proc_doc_est_topico", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_hist_proc_doc_est_topico"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HistoricoProcessoDocumentoEstruturadoTopico implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<HistoricoProcessoDocumentoEstruturadoTopico,Integer> {
	
	public static final String TABLE_NAME = "tb_hist_proc_doc_est_topico";

	private static final long serialVersionUID = 1L;
	
	private int idHistoricoProcessoDocumentoEstruturadoTopico;
	private ProcessoDocumentoEstruturadoTopico processoDocumentoEstruturadoTopico;
	private String titulo;
	private String sha1Titulo;
	private String conteudo;
	private String sha1Conteudo;
	private Date dataModificacao;
	private Pessoa pessoa;
	private boolean ativo;
	private TipoOperacaoTopicoEnum tipoOperacaoTopico;
	
	@Id
	@GeneratedValue(generator = "gen_hist_proc_doc_est_topico")
	@Column(name = "id_hist_proc_doc_est_topico", unique = true, nullable = false)	
	public int getIdHistoricoProcessoDocumentoEstruturadoTopico() {
		return idHistoricoProcessoDocumentoEstruturadoTopico;
	}
	
	public void setIdHistoricoProcessoDocumentoEstruturadoTopico(int idHistoricoProcessoDocumentoEstruturadoTopico) {
		this.idHistoricoProcessoDocumentoEstruturadoTopico = idHistoricoProcessoDocumentoEstruturadoTopico;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_proc_doc_estruturado_topico", nullable = false)
	public ProcessoDocumentoEstruturadoTopico getProcessoDocumentoEstruturadoTopico() {
		return processoDocumentoEstruturadoTopico;
	}
	
	public void setProcessoDocumentoEstruturadoTopico(ProcessoDocumentoEstruturadoTopico processoDocumentoEstruturadoTopico) {
		this.processoDocumentoEstruturadoTopico = processoDocumentoEstruturadoTopico;
	}

	@Column(name="ds_titulo", nullable=false)
	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	@Column(name="ds_sha1_titulo", nullable=false)
	public String getSha1Titulo() {
		return sha1Titulo;
	}

	public void setSha1Titulo(String sha1Titulo) {
		this.sha1Titulo = sha1Titulo;
	}

	@Column(name="ds_conteudo", nullable=false)
	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_pessoa")	
	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}
	
	@Column(name="in_ativo", nullable=false)
	@NotNull
	public boolean isAtivo() {
		return ativo;
	}
	
	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
	
	@Column(name = "in_tipo_operacao", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	public TipoOperacaoTopicoEnum getTipoOperacaoTopico() {
		return tipoOperacaoTopico;
	}
	
	public void setTipoOperacaoTopico(TipoOperacaoTopicoEnum tipoOperacaoTopico) {
		this.tipoOperacaoTopico = tipoOperacaoTopico;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdHistoricoProcessoDocumentoEstruturadoTopico();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HistoricoProcessoDocumentoEstruturadoTopico other = (HistoricoProcessoDocumentoEstruturadoTopico) obj;
		if (getIdHistoricoProcessoDocumentoEstruturadoTopico() != other.getIdHistoricoProcessoDocumentoEstruturadoTopico())
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HistoricoProcessoDocumentoEstruturadoTopico> getEntityClass() {
		return HistoricoProcessoDocumentoEstruturadoTopico.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdHistoricoProcessoDocumentoEstruturadoTopico());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return false;
	}

}
