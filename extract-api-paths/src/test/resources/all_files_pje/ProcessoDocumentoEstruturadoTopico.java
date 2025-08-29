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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.log.Ignore;
import br.jus.pje.nucleo.enums.editor.Hierarchical;
import br.jus.pje.nucleo.util.StringUtil;

@Entity
@Table(name = ProcessoDocumentoEstruturadoTopico.TABLE_NAME)
@Analyzer(impl = BrazilianAnalyzer.class)
@Indexed
@Ignore
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_doc_est_topico", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_doc_est_topico"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoDocumentoEstruturadoTopico implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoDocumentoEstruturadoTopico,Integer>, Hierarchical {

	public static final String TABLE_NAME = "tb_proc_doc_est_topico";
	public static final String NOME_INDEX_TITULO = "ProcessoDocumentoEstruturadoTopico.titulo";
	public static final String NOME_INDEX_PESSOA = "ProcessoDocumentoEstruturadoTopico.idPessoa";
	public static final String NOME_INDEX_CONTEUDO = "ProcessoDocumentoEstruturadoTopico.conteudo";
	public static final String NOME_INDEX_DATA_MODIFICACAO = "ProcessoDocumentoEstruturadoTopico.dataModificacao";
	public static final String NOME_INDEX_TIPO_DOCUMENTO = "ProcessoDocumentoEstruturadoTopico.idTipoDocumento";

	private static final long serialVersionUID = 1L;

	private Integer idProcessoDocumentoEstruturadoTopico;
	private ProcessoDocumentoEstruturado processoDocumentoEstruturado;
	private Topico topico;
	private EstruturaDocumentoTopico estruturaDocumentoTopico;
	private Integer nivel;
	private Integer ordem;
	private Integer numeracao;
	private ProcessoDocumentoEstruturadoTopico processoDocumentoEstruturadoBloco;
	private String titulo;
	private String sha1Titulo;
	private String conteudo;
	private String sha1Conteudo;
	private Date dataModificacao;
	private Pessoa pessoa;
	private boolean habilitado = Boolean.TRUE;
	private boolean exibirTitulo = Boolean.TRUE;
	private boolean numerado = Boolean.TRUE;
	private boolean ativo = Boolean.TRUE;
	private Integer codIdentificador;

	@Id
	@GeneratedValue(generator = "gen_proc_doc_est_topico")
	@Column(name = "id_proc_doc_estruturado_topico", unique = true, nullable = false)
	public Integer getIdProcessoDocumentoEstruturadoTopico() {
		return idProcessoDocumentoEstruturadoTopico;
	}

	public void setIdProcessoDocumentoEstruturadoTopico(Integer idProcessoDocumentoEstruturadoTopico) {
		this.idProcessoDocumentoEstruturadoTopico = idProcessoDocumentoEstruturadoTopico;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_proc_doc_estruturado", nullable = false)
	public ProcessoDocumentoEstruturado getProcessoDocumentoEstruturado() {
		return processoDocumentoEstruturado;
	}

	public void setProcessoDocumentoEstruturado(ProcessoDocumentoEstruturado processoDocumentoEstruturado) {
		this.processoDocumentoEstruturado = processoDocumentoEstruturado;
	}

	@Override
	@Column(name = "nr_nivel", nullable = false)
	public Integer getNivel() {
		return nivel;
	}

	@Override
	public void setNivel(Integer nivel) {
		this.nivel = nivel;
	}

	@Override
	@Column(name = "nr_ordem", nullable = false)
	public Integer getOrdem() {
		return ordem;
	}

	@Override
	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	@Override
	@Column(name = "nr_numeracao")
	public Integer getNumeracao() {
		return numeracao;
	}

	@Override
	public void setNumeracao(Integer numeracao) {
		this.numeracao = numeracao;
	}

	@OneToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "id_estrutura_documento_topico")
	public EstruturaDocumentoTopico getEstruturaDocumentoTopico() {
		if (estruturaDocumentoTopico != null) {
			return estruturaDocumentoTopico;
		} else {
			return processoDocumentoEstruturadoBloco.getEstruturaDocumentoTopico();
		}
	}

	public void setEstruturaDocumentoTopico(EstruturaDocumentoTopico estruturaDocumentoTopico) {
		this.estruturaDocumentoTopico = estruturaDocumentoTopico;
	}

	@Column(name = "ds_titulo", nullable = false)
	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	@Column(name = "ds_sha1_titulo", nullable = false)
	public String getSha1Titulo() {
		return sha1Titulo;
	}

	public void setSha1Titulo(String sha1Titulo) {
		this.sha1Titulo = sha1Titulo;
	}

	@Column(name = "ds_conteudo", nullable = false)
	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	@Column(name = "ds_sha1_conteudo", nullable = false)
	public String getSha1Conteudo() {
		return sha1Conteudo;
	}

	public void setSha1Conteudo(String sha1Conteudo) {
		this.sha1Conteudo = sha1Conteudo;
	}

	@Column(name = "dt_modificacao", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDataModificacao() {
		return dataModificacao;
	}

	public void setDataModificacao(Date dataModificacao) {
		this.dataModificacao = dataModificacao;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa")
	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "in_exibir_titulo", nullable = false)
	@NotNull
	public boolean isExibirTitulo() {
		return exibirTitulo;
	}

	public void setExibirTitulo(boolean exibirTitulo) {
		this.exibirTitulo = exibirTitulo;
	}

	@Override
	@Column(name = "in_numerado", nullable = false)
	@NotNull
	public boolean isNumerado() {
		return numerado;
	}

	public void setNumerado(boolean numerado) {
		this.numerado = numerado;
	}

	@Column(name = "in_habilitado", nullable = false)
	@NotNull
	public boolean isHabilitado() {
		return habilitado;
	}

	public void setHabilitado(boolean habilitado) {
		this.habilitado = habilitado;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_topico", nullable = false)
	public Topico getTopico() {
		return topico;
	}

	public void setTopico(Topico topico) {
		this.topico = topico;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "id_proc_doc_estruturado_bloco")
	public ProcessoDocumentoEstruturadoTopico getProcessoDocumentoEstruturadoBloco() {
		return processoDocumentoEstruturadoBloco;
	}

	public void setProcessoDocumentoEstruturadoBloco(ProcessoDocumentoEstruturadoTopico processoDocumentoEstruturadoBloco) {
		this.processoDocumentoEstruturadoBloco = processoDocumentoEstruturadoBloco;
	}

	@Override
	public int hashCode() {
		if (getIdProcessoDocumentoEstruturadoTopico() == null) {
			return super.hashCode();
		}
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoDocumentoEstruturadoTopico();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProcessoDocumentoEstruturadoTopico other = (ProcessoDocumentoEstruturadoTopico) obj;
		if (getCodIdentificador() != other.getCodIdentificador()) {
			return false;
		}
		return true;
	}

	@Transient
	public int getCodIdentificador() {
		if (codIdentificador == null) {
			Integer id = getIdProcessoDocumentoEstruturadoTopico();
			codIdentificador = id == null ? super.hashCode() : id;
		}
		return codIdentificador;
	}

	@Transient
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO, name = NOME_INDEX_TITULO)
	public String getTextoIndexavelTitulo() {
		return StringUtil.removeHtmlTags(titulo);
	}

	@Transient
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO, name = NOME_INDEX_CONTEUDO)
	public String getTextoIndexavelConteudo() {
		return StringUtil.removeHtmlTags(conteudo);
	}

	@Transient
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO, name = NOME_INDEX_PESSOA, analyzer = @Analyzer(impl = StandardAnalyzer.class))
	public String getIndexavelAutor() {
		return String.valueOf(getPessoa().getIdUsuario());
	}

	@Transient
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO, name = NOME_INDEX_DATA_MODIFICACAO, analyzer = @Analyzer(impl = StandardAnalyzer.class))
	public String getIndexavelData() {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sf.format(dataModificacao);
	}

	@Transient
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO, name = NOME_INDEX_TIPO_DOCUMENTO, analyzer = @Analyzer(impl = StandardAnalyzer.class))
	public String getIndexavelTipoDocumento() {
		return String.valueOf(getProcessoDocumentoEstruturado().getProcessoDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento());
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumentoEstruturadoTopico> getEntityClass() {
		return ProcessoDocumentoEstruturadoTopico.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdProcessoDocumentoEstruturadoTopico();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return false;
	}

}
