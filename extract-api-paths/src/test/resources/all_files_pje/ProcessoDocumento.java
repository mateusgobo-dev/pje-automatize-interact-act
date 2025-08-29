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
package br.jus.pje.nucleo.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.bytecode.internal.javassist.FieldHandled;
import org.hibernate.bytecode.internal.javassist.FieldHandler;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.ExigibilidadeAssinaturaEnum;
import br.jus.pje.nucleo.enums.TipoOrigemAcaoEnum;
import br.jus.pje.nucleo.type.StringJsonUserType;

@Entity
@Table(name = "tb_processo_documento")
@Inheritance(strategy = InheritanceType.JOINED)
@IndexedEntity(
		id="idProcessoDocumento",
		value="documento",
		mappings={
			@Mapping(beanPath="documentoPrincipal.idProcessoDocumento", mappedPath="id_principal"),
			@Mapping(beanPath="usuarioInclusao.idUsuario", mappedPath="id_usuario_autor"),
			@Mapping(beanPath="nomeUsuarioInclusao", mappedPath="nome_usuario_autor"),
			@Mapping(beanPath="ativo", mappedPath="ativo"),
			@Mapping(beanPath="documentoSigiloso", mappedPath="sigiloso"),
			@Mapping(beanPath="processo.idProcesso", mappedPath="id_processo"),
			@Mapping(beanPath="processo.numeroProcesso", mappedPath="numero_processo"),
			@Mapping(beanPath="processoTrf.classeJudicial.idClasseJudicial", mappedPath="id_classe"),
			@Mapping(beanPath="processoTrf.classeJudicial.classeJudicialSigla", mappedPath="sigla_classe"),
			@Mapping(beanPath="processoTrf.classeJudicial.classeJudicial", mappedPath="nome_classe"),
			@Mapping(beanPath="processoTrf.orgaoJulgador.idOrgaoJulgador", mappedPath="id_orgao_julgador_processo"),
			@Mapping(beanPath="processoTrf.orgaoJulgador.orgaoJulgador", mappedPath="nome_orgao_julgador_processo"),
			@Mapping(beanPath="processoTrf.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado", mappedPath="id_orgao_colegiado_processo"),
			@Mapping(beanPath="processoTrf.orgaoJulgadorColegiado.orgaoJulgadorColegiado", mappedPath="nome_orgao_colegiado_processo"),
			@Mapping(beanPath="processoTrf.assuntoTrfList", mappedPath="assuntos_do_processo"),
			@Mapping(beanPath="processoTrf.processoParteList", mappedPath="partes_do_processo", when="isAtivo"),
			@Mapping(beanPath="tipoProcessoDocumento.idTipoProcessoDocumento", mappedPath="id_tipo"),
			@Mapping(beanPath="tipoProcessoDocumento.tipoProcessoDocumento", mappedPath="nome_tipo"),
			@Mapping(beanPath="dataInclusao", mappedPath="data_inclusao"),
			@Mapping(beanPath="dataJuntada", mappedPath="data_juntada"),
			@Mapping(beanPath="dataAlteracao", mappedPath="data_alteracao"),
			@Mapping(beanPath="usuarioJuntada.idUsuario", mappedPath="id_usuario_juntada"),
			@Mapping(beanPath="nomeUsuarioJuntada", mappedPath="nome_usuario_juntada"),
			@Mapping(beanPath="localizacaoJuntada", mappedPath="nome_localizacao_juntada"),
			@Mapping(beanPath="processoDocumento", mappedPath="descricao"),
			@Mapping(beanPath="processoDocumentoBin", mappedPath="binario", when="extensao", extractor="br.jus.pje.indexacao.ExtratorDocumento"),
		}
)
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_documento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@TypeDefs({
    @TypeDef(name = "json", typeClass = StringJsonUserType.class)
})
public class ProcessoDocumento implements java.io.Serializable, IEntidade<ProcessoDocumento,Integer>, FieldHandled {

	private static final long serialVersionUID = 1L;

	private int idProcessoDocumento;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private ProcessoDocumentoBin processoDocumentoBin;
	private Usuario usuarioInclusao;
	private String nomeUsuarioInclusao;
	private Processo processo;
	private ProcessoTrf processoTrf;
	
	private Usuario usuarioExclusao;
	private String nomeUsuarioExclusao;
	private String processoDocumento;
	private Date dataInclusao = new Date();
	private Date dataExclusao;
	private String motivoExclusao;
	private String numeroDocumento;
	private Boolean ativo = Boolean.TRUE;
	private String observacaoProcedimento;
	private Boolean documentoSigiloso = Boolean.FALSE;
	private Papel papel;
	private String nomePapel;
	private Usuario usuarioAlteracao;
	private String nomeUsuarioAlteracao;
	private Date dataAlteracao;
	private Localizacao localizacao;
	private String nomeLocalizacao;
	private Date dataJuntada;
	/**
	 * Indica que o documento deve ser tratado exclusivamente na atividade especifica que o criou
	 * exemplo: fluxo, diligencia, ata de audiencia, ata de sessao 
	 */
	private Boolean exclusivoAtividadeEspecifica = Boolean.FALSE;
	private Long idJbpmTask;

	private ProcessoDocumento documentoPrincipal;
	private Set<ProcessoDocumento> documentosVinculados = new LinkedHashSet<>(0);
	private List<ProcessoDocumentoVisibilidadeSegredo> visualizadores = new ArrayList<>(0);
	private List<ProcessoEvento> processoEventoList = new ArrayList<>(0);
	
	private Boolean lido = Boolean.FALSE;
	private Integer numeroOrdem;
	
	private String instancia;
	private Boolean selected;
	private String idInstanciaOrigem;

	private Usuario usuarioJuntada;
	private String nomeUsuarioJuntada;
	private String localizacaoJuntada;
	private TipoOrigemAcaoEnum inTipoOrigemJuntada;
	private Boolean documentoSigilosoTela = Boolean.FALSE;
	private Boolean selecionadoParaUploadEmLote = Boolean.FALSE;
	private ProcessoTrfDocumentoImpresso processoTrfDocumentoImpresso;
	private ProcessoEventoTemp processoEventoTemp;
	private ProcessoDocumentoTrfLocal processoDocumentoTrfLocal;
	private ProcessoDocumentoPeticaoNaoLida processoDocumentoPeticaoNaoLida;
	private List<ProcessoDocumentoLido> processoDocumentoLidoList = new ArrayList<>(0);
	private List<DocumentoValidacaoHash> documentoValidacaoHashList = new ArrayList<>(0);
	private DocumentoCertidao documentoCertidao;
	private FieldHandler handler;
	private String numeroGuia;
	private MotivoIsencaoGuia motivoIsencaoGuia;
	private String json;
	
	public ProcessoDocumento() {

	}

	public ProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.exclusivoAtividadeEspecifica = processoDocumento.getExclusivoAtividadeEspecifica();
		this.idJbpmTask = processoDocumento.getIdJbpmTask();
		this.idProcessoDocumento = processoDocumento.getIdProcessoDocumento();
		this.tipoProcessoDocumento = processoDocumento.getTipoProcessoDocumento();
		this.processoDocumentoBin = processoDocumento.getProcessoDocumentoBin ();
		this.usuarioInclusao = processoDocumento.getUsuarioInclusao();
		this.nomeUsuarioInclusao = processoDocumento.getNomeUsuarioInclusao();
		this.processo = processoDocumento.getProcesso();
		this.usuarioExclusao = processoDocumento.getUsuarioExclusao();
		this.nomeUsuarioExclusao = processoDocumento.getNomeUsuarioExclusao();
		this.processoDocumento = processoDocumento.getProcessoDocumento();
		this.dataInclusao = processoDocumento.getDataInclusao();
		this.dataExclusao = processoDocumento.getDataExclusao();
		this.motivoExclusao = processoDocumento.getMotivoExclusao();
		this.numeroDocumento = processoDocumento.getNumeroDocumento();
		this.ativo = processoDocumento.getAtivo();
		this.observacaoProcedimento = processoDocumento.getObservacaoProcedimento();
		this.documentoSigiloso = processoDocumento.getDocumentoSigiloso();
		this.papel = processoDocumento.getPapel();
		this.nomePapel = processoDocumento.getNomePapel();
		this.usuarioAlteracao = processoDocumento.getUsuarioAlteracao();
		this.nomeUsuarioAlteracao = processoDocumento.getNomeUsuarioAlteracao();
		this.dataAlteracao = processoDocumento.getDataAlteracao();
		this.localizacao = processoDocumento.getLocalizacao();
		this.nomeLocalizacao = processoDocumento.getNomeLocalizacao();
		this.documentoPrincipal = processoDocumento.getDocumentoPrincipal();
		this.documentosVinculados = processoDocumento.getDocumentosVinculados();
	}
	
	@Id
	@GeneratedValue(generator = "gen_processo_documento")
	@Column(name = "id_processo_documento", unique = true, nullable = false)
	@NotNull
	public int getIdProcessoDocumento() {
		return this.idProcessoDocumento;
	}

	public void setIdProcessoDocumento(int idProcessoDocumento) {
		this.idProcessoDocumento = idProcessoDocumento;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_tipo_processo_documento", nullable = false)
	@NotNull
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return this.tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@Column(name = "nr_documento", nullable = true)
	public String getNumeroDocumento() {
		return this.numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento_bin", nullable = false)
	@NotNull
	public ProcessoDocumentoBin getProcessoDocumentoBin() {
		return this.processoDocumentoBin;
	}

	public void setProcessoDocumentoBin(ProcessoDocumentoBin processoDocumentoBin) {
		this.processoDocumentoBin = processoDocumentoBin;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_inclusao")
	public Usuario getUsuarioInclusao() {
		return this.usuarioInclusao;
	}

	public void setUsuarioInclusao(Usuario usuarioInclusao) {
		this.usuarioInclusao = usuarioInclusao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo", nullable = false)
	@NotNull
	public Processo getProcesso() {
		return this.processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo", insertable = false, updatable = false)
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_exclusao")
	public Usuario getUsuarioExclusao() {
		return this.usuarioExclusao;
	}

	public void setUsuarioExclusao(Usuario usuarioExclusao) {
		this.usuarioExclusao = usuarioExclusao;
	}

	@Column(name = "ds_processo_documento", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getProcessoDocumento() {
		return this.processoDocumento;
	}

	public void setProcessoDocumento(String processoDocumento) {
		/** Limite do campo processoDocumento é de 100 caracteres */
		if( processoDocumento != null && processoDocumento.length() > 100 ) 
			processoDocumento = processoDocumento.substring(0, 100);

		this.processoDocumento = processoDocumento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao", nullable = false)
	@NotNull
	public Date getDataInclusao() {
		return this.dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_exclusao")
	public Date getDataExclusao() {
		return this.dataExclusao;
	}

	public void setDataExclusao(Date dataExclusao) {
		this.dataExclusao = dataExclusao;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_motivo_exclusao")
	public String getMotivoExclusao() {
		return this.motivoExclusao;
	}

	public void setMotivoExclusao(String motivoExclusao) {
		this.motivoExclusao = motivoExclusao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_observacao_procedimento")
	public String getObservacaoProcedimento() {
		return this.observacaoProcedimento;
	}

	public void setObservacaoProcedimento(String observacaoProcedimento) {
		this.observacaoProcedimento = observacaoProcedimento;
	}

	@Column(name = "in_documento_sigiloso", nullable = false)
	@NotNull
	public Boolean getDocumentoSigiloso() {
		return this.documentoSigiloso;
	}

	public void setDocumentoSigiloso(Boolean documentoSigiloso) {
		this.documentoSigiloso = documentoSigiloso;
	}

	@Override
	public String toString() {
		if (this.getTipoProcessoDocumento() != null) {
			return Objects.equals(this.getTipoProcessoDocumento().getTipoProcessoDocumento(), processoDocumento) 
					? processoDocumento 
					: this.getTipoProcessoDocumento().getTipoProcessoDocumento() + " (" + processoDocumento + ")";
		}
		return processoDocumento;
	}
	
	@Column(name = "in_atividade_especifica", nullable = false)
	@NotNull
	public Boolean getExclusivoAtividadeEspecifica() {
		return this.exclusivoAtividadeEspecifica;
	}

	public void setExclusivoAtividadeEspecifica(Boolean exclusivoAtividadeEspecifica) {
		this.exclusivoAtividadeEspecifica = exclusivoAtividadeEspecifica;
	}

	@Column(name = "id_jbpm_task")
	public Long getIdJbpmTask() {
		return idJbpmTask;
	}

	public void setIdJbpmTask(Long idJbpmTask) {
		this.idJbpmTask = idJbpmTask;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_papel")
	public Papel getPapel() {
		return this.papel;
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_alteracao")
	public Usuario getUsuarioAlteracao() {
		return this.usuarioAlteracao;
	}

	public void setUsuarioAlteracao(Usuario usuarioAlteracao) {
		this.usuarioAlteracao = usuarioAlteracao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao")
	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@Column(name = "ds_nome_usuario_inclusao", length = 400)
	@Length(max = 400)
	public String getNomeUsuarioInclusao() {
		return nomeUsuarioInclusao;
	}

	public void setNomeUsuarioInclusao(String nomeUsuarioInclusao) {
		this.nomeUsuarioInclusao = nomeUsuarioInclusao;
	}

	@Column(name = "ds_nome_usuario_exclusao", length = 100)
	@Length(max = 100)
	public String getNomeUsuarioExclusao() {
		return nomeUsuarioExclusao;
	}

	public void setNomeUsuarioExclusao(String nomeUsuarioExclusao) {
		this.nomeUsuarioExclusao = nomeUsuarioExclusao;
	}

	@Column(name = "ds_nome_papel", length = 100)
	@Length(max = 100)
	public String getNomePapel() {
		return nomePapel;
	}

	public void setNomePapel(String nomePapel) {
		this.nomePapel = nomePapel;
	}

	@Column(name = "ds_nome_usuario_alteracao", length = 400)
	@Length(max = 400)
	public String getNomeUsuarioAlteracao() {
		return nomeUsuarioAlteracao;
	}

	public void setNomeUsuarioAlteracao(String nomeUsuarioAlteracao) {
		this.nomeUsuarioAlteracao = nomeUsuarioAlteracao;
	}
	
	@Column(name = "ds_nome_localizacao", length = 100)
	@Length(max = 100)
	public String getNomeLocalizacao() {
		return nomeLocalizacao;
	}

	public void setNomeLocalizacao(String nomeLocalizacao) {
		this.nomeLocalizacao = nomeLocalizacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_alteracao")
	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="dt_juntada")
	public Date getDataJuntada(){
		return this.dataJuntada;
	}
	
	public void setDataJuntada(Date dataJuntada){
		this.dataJuntada = dataJuntada;
	}

	/**
	 * Obtém o documento principal relativo a este documento, se existente.
	 * 
	 * @return o documento principal, se existente, ou nulo
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "id_documento_principal")
	public ProcessoDocumento getDocumentoPrincipal() {
		return documentoPrincipal;
	}

	/**
	 * Atribui a este documento um documento princial.
	 * 
	 * @param documentoPrincipal
	 *            o documento a ser atribuído como principal.
	 */
	public void setDocumentoPrincipal(ProcessoDocumento documentoPrincipal) {
		this.documentoPrincipal = documentoPrincipal;
	}
	
	/**
	 * Método responsável por realizar a verificação do documento, se o mesmo é pai ou não.
	 * 
	 * @return true se o documento for o pai
	 */
	@Transient
	public boolean isDocumentoPai(){
		boolean isPai = false;
		
		if(this.getDocumentoPrincipal() == null){
			isPai = true;
		}
		return isPai;
	}
	
	/**
	 * Obtém o conjunto de documentos vinculados deste documento.
	 * 
	 * @return os documentos vinculados a este documento, ou seja, aqueles em
	 *         que este documento figura como
	 *         {@link ProcessoDocumento#documentoPrincipal}
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "documentoPrincipal")
	@OrderBy("numeroOrdem")
	public Set<ProcessoDocumento> getDocumentosVinculados() {
		return documentosVinculados;
	}

	@Column(name = "ds_instancia", length = 1)
	@Length(max = 1)
	public String getInstancia() {
		return instancia;
	}

	public void setInstancia(String instancia) {
		this.instancia = instancia;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_juntada")
	public Usuario getUsuarioJuntada() {
		return usuarioJuntada;
	}

	public void setUsuarioJuntada(Usuario usuarioJuntada) {
		this.usuarioJuntada = usuarioJuntada;
	}

	@Column(name = "ds_nome_usuario_juntada")
	public String getNomeUsuarioJuntada() {
		return nomeUsuarioJuntada;
	}

	public void setNomeUsuarioJuntada(String nomeUsuarioJuntada) {
		this.nomeUsuarioJuntada = nomeUsuarioJuntada;
	}

	@Column(name = "ds_localizacao_usuario_juntada")
	public String getLocalizacaoJuntada() {
		return localizacaoJuntada;
	}

	public void setLocalizacaoJuntada(String localizacaoJuntada) {
		this.localizacaoJuntada = localizacaoJuntada;
	}

	@Column(name = "in_tipo_origem_juntada", length = 2)
	@Enumerated(EnumType.STRING)
	public TipoOrigemAcaoEnum getInTipoOrigemJuntada() {
		return inTipoOrigemJuntada;
	}

	public void setInTipoOrigemJuntada(TipoOrigemAcaoEnum inTipoOrigemJuntada) {
		this.inTipoOrigemJuntada = inTipoOrigemJuntada;
	}

	/**
	 * Atribui a este documento um conjunto de documentos vinculados. Em razão
	 * da implementação JPA do Hibernate, não se deve utilizar esta função
	 * diretamente, sendo recomendável o acréscimo ou retirada por meio da
	 * coleção obtida com o uso de
	 * {@link ProcessoDocumento#getDocumentosVinculados()}.
	 * 
	 * @param documentosVinculados
	 *            os documentos a serem vinculados.
	 */
	public void setDocumentosVinculados(Set<ProcessoDocumento> documentosVinculados) {
		this.documentosVinculados = documentosVinculados;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idProcessoDocumento;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		ProcessoDocumento entity = (ProcessoDocumento) obj;
		if (this.getIdProcessoDocumento() == 0 && entity.getIdProcessoDocumento() == 0) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
		return getIdProcessoDocumento() == entity.getIdProcessoDocumento();
	}

	/**
	 * Verifica se o documento requer assinatura para ser considerado válido
	 * @param processoDocumento
	 * @return TRUE se requer assinatura ou FALSE se não requer assinatura
	 */
	@Transient
	public boolean isAssinaturaObrigatoria() {
		// se nenhum papel é obrigatório ou suficiente
		if ((tipoProcessoDocumento != null) && (tipoProcessoDocumento.getPapeis() != null)) {
			for (TipoProcessoDocumentoPapel tipoProcessoDocumentoPapel : tipoProcessoDocumento.getPapeis()) {
				if (	(tipoProcessoDocumentoPapel != null) &&
						(tipoProcessoDocumentoPapel.getExigibilidade() != null) &&
						(!tipoProcessoDocumentoPapel.getExigibilidade().equals(ExigibilidadeAssinaturaEnum.F))) {
					return true; 
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Verifica se o documento já foi juntado
	 * 
	 * @return boolean
	 */
	@Transient
	public boolean isJuntado() {
		return this.dataJuntada != null;
	}
	
	@Transient
	public Boolean getDocumentoApreciado() {
		return processoDocumento != null;
	}
	
	@Column(name = "in_lido", nullable = false)
	@NotNull
	public Boolean getLido() {
		return this.lido;
	}
	
	public void setLido(Boolean lido) {
		this.lido = lido;
	}
	
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name="id_processo_documento")
	public List<ProcessoDocumentoVisibilidadeSegredo> getVisualizadores() {
		return visualizadores;
	}
	
	public void setVisualizadores(List<ProcessoDocumentoVisibilidadeSegredo> visualizadores) {
		this.visualizadores = visualizadores;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "processoDocumento")
	public List<ProcessoEvento> getProcessoEventoList() {
		return processoEventoList;
	}

	public void setProcessoEventoList(List<ProcessoEvento> processoEventoList) {
		this.processoEventoList = processoEventoList;
	}

	@Transient
	public String getNomeUsuario(){
		if(getNomeUsuarioJuntada() != null && !getNomeUsuarioJuntada().isEmpty()){
 			return getNomeUsuarioJuntada();
 		}

		if(getUsuarioJuntada() != null){
			return getUsuarioJuntada().getNome();
		}
		
		if(getNomeUsuarioAlteracao() != null && !getNomeUsuarioAlteracao().isEmpty()){
			return getNomeUsuarioAlteracao();
		}
		
		if(getUsuarioAlteracao() != null){
			return getUsuarioAlteracao().getNome();
		}
		
		if(getNomeUsuarioInclusao() != null && !getNomeUsuarioInclusao().isEmpty()){
			return getNomeUsuarioInclusao();
		}
		
		if(getUsuarioInclusao() != null){
			return getUsuarioInclusao().getNome();
		}
		
		return null;
	}

	/**
	 * @return Retorna numeroOrdem.
	 */
	@Column(name = "nr_ordem", nullable = true)
	public Integer getNumeroOrdem() {
		return numeroOrdem;
	}

	/**
	 * @param numeroOrdem Atribui numeroOrdem.
	 */
	public void setNumeroOrdem(Integer numeroOrdem) {
		this.numeroOrdem = numeroOrdem;
	}

	@Transient
	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	/**
	 * Armazena o id da instancia de origem quando o documento foi criado em outra instância e remetido
	 * @return
	 */
	@Column(name = "id_instancia_origem", nullable = true)
	public String getIdInstanciaOrigem() {
		return idInstanciaOrigem;
	}

	public void setIdInstanciaOrigem(String idInstanciaOrigem) {
		this.idInstanciaOrigem = idInstanciaOrigem;
	}

	@OneToOne(cascade = CascadeType.REMOVE, mappedBy = "processoDocumento", fetch = FetchType.LAZY)
	@LazyToOne(LazyToOneOption.NO_PROXY)
	public ProcessoTrfDocumentoImpresso getProcessoTrfDocumentoImpresso() {
		if (this.handler != null) {
			return (ProcessoTrfDocumentoImpresso) this.handler.readObject(
					this, "processoTrfDocumentoImpresso", processoTrfDocumentoImpresso);
		}
		return processoTrfDocumentoImpresso;
	}

	public void setProcessoTrfDocumentoImpresso(ProcessoTrfDocumentoImpresso processoTrfDocumentoImpresso) {
		if (this.handler != null) {
			this.processoTrfDocumentoImpresso = (ProcessoTrfDocumentoImpresso) this.handler.writeObject(
					this, "processoTrfDocumentoImpresso", this.processoTrfDocumentoImpresso, processoTrfDocumentoImpresso);
		}
		this.processoTrfDocumentoImpresso = processoTrfDocumentoImpresso;
	}

	@OneToOne(cascade = CascadeType.REMOVE, mappedBy = "processoDocumento", fetch = FetchType.LAZY)
	@LazyToOne(LazyToOneOption.NO_PROXY)
	public ProcessoEventoTemp getProcessoEventoTemp() {
		if (this.handler != null) {
			return (ProcessoEventoTemp) this.handler.readObject(
					this, "processoEventoTemp", processoEventoTemp);
		}
		return processoEventoTemp;
	}

	public void setProcessoEventoTemp(ProcessoEventoTemp processoEventoTemp) {
		if (this.handler != null) {
			this.processoEventoTemp = (ProcessoEventoTemp) this.handler.writeObject(
					this, "processoEventoTemp", this.processoEventoTemp, processoEventoTemp);
		}
		this.processoEventoTemp = processoEventoTemp;
	}

	@OneToOne(cascade = CascadeType.REMOVE, mappedBy = "processoDocumento", fetch = FetchType.LAZY)
	@LazyToOne(LazyToOneOption.NO_PROXY)
	public ProcessoDocumentoTrfLocal getProcessoDocumentoTrfLocal() {
		if (this.handler != null) {
			return (ProcessoDocumentoTrfLocal) this.handler.readObject(
					this, "processoDocumentoTrfLocal", processoDocumentoTrfLocal);
		}
		return processoDocumentoTrfLocal;
	}

	public void setProcessoDocumentoTrfLocal(ProcessoDocumentoTrfLocal processoDocumentoTrfLocal) {
		if (this.handler != null) {
			this.processoDocumentoTrfLocal = (ProcessoDocumentoTrfLocal) this.handler.writeObject(
					this, "processoDocumentoTrfLocal", this.processoDocumentoTrfLocal, processoDocumentoTrfLocal);
		}
		this.processoDocumentoTrfLocal = processoDocumentoTrfLocal;
	}

	@OneToOne(cascade = CascadeType.REMOVE, mappedBy = "processoDocumento", fetch = FetchType.LAZY)
	@LazyToOne(LazyToOneOption.NO_PROXY)
	public ProcessoDocumentoPeticaoNaoLida getProcessoDocumentoPeticaoNaoLida() {
		if (this.handler != null) {
			return (ProcessoDocumentoPeticaoNaoLida) this.handler.readObject(
					this, "processoDocumentoPeticaoNaoLida", processoDocumentoPeticaoNaoLida);
		}
		return processoDocumentoPeticaoNaoLida;
	}

	public void setProcessoDocumentoPeticaoNaoLida(ProcessoDocumentoPeticaoNaoLida processoDocumentoPeticaoNaoLida) {
		if (this.handler != null) {
			this.processoDocumentoPeticaoNaoLida = (ProcessoDocumentoPeticaoNaoLida) this.handler.writeObject(
					this, "processoDocumentoPeticaoNaoLida", this.processoDocumentoPeticaoNaoLida, processoDocumentoPeticaoNaoLida);
		}
		this.processoDocumentoPeticaoNaoLida = processoDocumentoPeticaoNaoLida;
	}
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, mappedBy = "processoDocumento")
	public List<ProcessoDocumentoLido> getProcessoDocumentoLidoList() {
		return processoDocumentoLidoList;
	}

	public void setProcessoDocumentoLidoList(List<ProcessoDocumentoLido> processoDocumentoLidoList) {
		this.processoDocumentoLidoList = processoDocumentoLidoList;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, mappedBy = "processoDocumento")
	public List<DocumentoValidacaoHash> getDocumentoValidacaoHashList() {
		return documentoValidacaoHashList;
	}
	
	@OneToOne(cascade = CascadeType.REMOVE, mappedBy = "processoDocumento", fetch = FetchType.LAZY)
	@LazyToOne(LazyToOneOption.NO_PROXY)
	public DocumentoCertidao getDocumentoCertidao() {
		if (this.handler != null) {
			return (DocumentoCertidao) this.handler.readObject(
					this, "documentoCertidao", documentoCertidao);
		}
		return documentoCertidao;
	}

	public void setDocumentoCertidao(DocumentoCertidao documentoCertidao) {
		if (this.handler != null) {
			this.documentoCertidao = (DocumentoCertidao) this.handler.writeObject(
					this, "documentoCertidao", this.documentoCertidao, documentoCertidao);
		}
		this.documentoCertidao = documentoCertidao;
	}

	public void setDocumentoValidacaoHashList(List<DocumentoValidacaoHash> documentoValidacaoHashList) {
		this.documentoValidacaoHashList = documentoValidacaoHashList;
	}

	@Transient
	public Boolean getDocumentoSigilosoTela() {
		return documentoSigilosoTela;
	}

	public void setDocumentoSigilosoTela(Boolean documentoSigilosoTela) {
		this.documentoSigilosoTela = documentoSigilosoTela;
	}

	@Transient
        public Boolean getSelecionadoParaUploadEmLote() {
                return selecionadoParaUploadEmLote;
        }

        public void setSelecionadoParaUploadEmLote(Boolean selecionadoParaUploadEmLote) {
                this.selecionadoParaUploadEmLote = selecionadoParaUploadEmLote;
        }        
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumento> getEntityClass() {
		return ProcessoDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoDocumento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@Override
	@javax.persistence.Transient
	public void setFieldHandler(FieldHandler handler) {
		this.handler = handler;
	}

	@Override
	@javax.persistence.Transient
	public FieldHandler getFieldHandler() {
		return this.handler;
	}

	@Column(name = "ds_numero_guia")
	public String getNumeroGuia() {
		return numeroGuia;
	}

	public void setNumeroGuia(String numeroGuia) {
		this.numeroGuia = numeroGuia;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "id_motivo_isencao")
	public MotivoIsencaoGuia getMotivoIsencaoGuia() {
		return motivoIsencaoGuia;
	}

	public void setMotivoIsencaoGuia(MotivoIsencaoGuia motivoIsencaoCustas) {
		this.motivoIsencaoGuia = motivoIsencaoCustas;
	}

	@Column(name = "ds_json")
	@Type(type = "jsonb")
	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}
}
