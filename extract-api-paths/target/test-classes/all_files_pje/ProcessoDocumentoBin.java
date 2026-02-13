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

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.apache.http.ParseException;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.jt.entidades.ControleVersaoDocumento;
import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.util.ArrayUtil;

@Entity
@Table(name = "tb_processo_documento_bin")
@IndexedEntity(
		value="conteudodocumento", 
		id="idProcessoDocumentoBin", 
		owners={"processoDocumentoList"},
	mappings={
		@Mapping(beanPath="extensao", mappedPath="mimetype"),
		@Mapping(beanPath="nomeArquivo", mappedPath="arquivooriginario"),
		@Mapping(beanPath="size", mappedPath="tamanho"),
		@Mapping(beanPath="modeloDocumento", mappedPath="modeloDocumento"),
		@Mapping(beanPath="dataAssinatura", mappedPath="data_assinatura"),
		@Mapping(beanPath="signatarios", mappedPath="assinaturas")
})
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_documento_bin", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_documento_bin"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoDocumentoBin implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String REMOVE_TEXTO = "";
	private static final String REGEX_FORMULARIO_DOCUMENTO = "<input .* type=\"hidden\" />|<form.*>|</form>";
	
	@Id
	@GeneratedValue(generator = "gen_processo_documento_bin")
	@Column(name = "id_processo_documento_bin", unique = true, nullable = false)
	@NotNull
	private int idProcessoDocumentoBin;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario")
	private Usuario usuario;
	
	@Column(name = "ds_nome_usuario", length = 100)
	@Length(max = 100)
	private String nomeUsuario;
	
	@Column(name = "ds_nome_usuario_ultimo_assinar", length = 100)
	@Length(max = 100)
	private String usuarioUltimoAssinar;
	
	@Transient
	private byte[] processoDocumento;
	
	@Column(name = "ds_extensao", length = 50)
	@Length(max = 50)
	private String extensao;
	
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_modelo_documento")
	private String modeloDocumento;
	
	@Column(name = "ds_md5_documento", nullable = false, length = 255)
	@NotNull
	@Length(max = 255)
	private String md5Documento;
	
	@Column(name = "nm_arquivo", length = 300)
	@Length(max = 300)
	private String nomeArquivo;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao", nullable = false)
	@NotNull
	private Date dataInclusao = new Date();
	
	@Column(name = "nr_tamanho")
	private Integer size;
	
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_signature")
	private String signature;
	
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_cert_chain")
	private String certChain;
	
	@Column(name = "dt_primeira_assinatura")
	private Date dataAssinatura;
	
	@Column(name = "in_valido", nullable = false)
	private Boolean valido = Boolean.FALSE;
	
	@Column(name="in_binario", nullable=false)
	private Boolean binario = Boolean.FALSE;
	
	@Column(name = "nr_documento_storage")
	private String numeroDocumentoStorage;
	
	@Column(name = "nm_documento_wopi")
	private String nomeDocumentoWopi;
	
	@Transient
	private String context;

	@Transient
	private File file;
	
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "processoDocumentoBin")
	private List<ProcessoDocumento> processoDocumentoList = new ArrayList<>(0);
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "processoDocumentoBin")
	private List<ProcessoDocumentoBinPessoaAssinatura> signatarios = new ArrayList<>(0);
	
	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "processoDocumentoBin")
	private List<ControleVersaoDocumento> versoes;

	public ProcessoDocumentoBin() {
	}
	
	public ProcessoDocumentoBin(ProcessoDocumentoBin pdb) {
		this.usuario = pdb.getUsuario();
		this.nomeUsuario = pdb.getNomeUsuario();
		this.usuarioUltimoAssinar = pdb.getUsuarioUltimoAssinar();
		this.processoDocumento = pdb.getProcessoDocumento();
		this.extensao = pdb.getExtensao();
		this.modeloDocumento = pdb.getModeloDocumento();
		this.md5Documento = pdb.getMd5Documento();
		this.nomeArquivo = pdb.getNomeArquivo();
		this.dataInclusao = pdb.getDataInclusao();
		this.size = pdb.getSize();
		this.signature = pdb.getSignature();
		this.certChain = pdb.getCertChain();
		this.dataAssinatura = pdb.getDataAssinatura();
		this.valido = pdb.getValido();
		this.binario = pdb.getBinario();
		this.processoDocumentoList = pdb.getProcessoDocumentoList();
		this.signatarios = pdb.getSignatarios();
		this.versoes = pdb.versoes;
	}
	
	public int getIdProcessoDocumentoBin() {
		return this.idProcessoDocumentoBin;
	}

	public void setIdProcessoDocumentoBin(int idProcessoDocumentoBin) {
		this.idProcessoDocumentoBin = idProcessoDocumentoBin;
	}
	
	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
	public String getExtensao() {
		return this.extensao;
	}

	public void setExtensao(String extensao) {
		this.extensao = extensao;
	}
	
	public String getModeloDocumento() {
		return this.modeloDocumento;
	}

	public void setModeloDocumento(String modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public String getDocumentoOriginal() {
		return this.modeloDocumento;
	}
	
	public String getDocumentoApenasComFormatacaoTexto() {
		String documentoFormatado = getModeloDocumento();
		
		if (documentoFormatado != null) {
			documentoFormatado = documentoFormatado.replaceAll(REGEX_FORMULARIO_DOCUMENTO, REMOVE_TEXTO);
		}
			
		return documentoFormatado;
	}
	
	public String getMd5Documento() {
		return this.md5Documento;
	}

	public void setMd5Documento(String md5Documento) {
		this.md5Documento = md5Documento;
	}
	
	public String getNomeArquivo() {
		return this.nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	public Date getDataInclusao() {
		return this.dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}
	
	public String getDataInclusaoFormatada() throws ParseException {
		return dataInclusao != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(dataInclusao) : null;
	}
	
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	
	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}
	
	public List<ProcessoDocumento> getProcessoDocumentoList() {
		return this.processoDocumentoList;
	}

	public void setProcessoDocumentoList(List<ProcessoDocumento> processoDocumentoList) {
		this.processoDocumentoList = processoDocumentoList;
	}
	
	public List<ProcessoDocumentoBinPessoaAssinatura> getSignatarios() {
		if ( signatarios == null ){
			return new ArrayList<ProcessoDocumentoBinPessoaAssinatura>(0);
		}
		return signatarios;
	}

	public void setSignatarios(
			List<ProcessoDocumentoBinPessoaAssinatura> signatarios) {
		this.signatarios = signatarios;
	}

	public List<ControleVersaoDocumento> getVersoes() {
		return versoes;
	}

	public void setVersoes(List<ControleVersaoDocumento> versoes) {
		this.versoes = versoes;
	}

	@Override
	public String toString() {
		return isBinario() ? nomeArquivo : md5Documento;
	}
	
	public byte[] getProcessoDocumento() {
		return ArrayUtil.copyOf(processoDocumento);
	}

	public void setProcessoDocumento(byte[] processoDocumento) {
		this.processoDocumento = ArrayUtil.copyOf(processoDocumento);
	}
	
	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public boolean isBinario() {
		return getBinario();
	}

	public String getSizeFormatado() {
		if (size != null && size > 0) {
			NumberFormat formatter = new DecimalFormat("###,##0.00");
			float sizeF = size / 1024f;
			return formatter.format(sizeF) + " Kb";
		} else if (!binario){
			NumberFormat formatter = new DecimalFormat("###,##0.00");
			float sizeF = getModeloDocumento().length() / 1024f;
			return formatter.format(sizeF) + " Kb";
		} else {
			return "0 Kb";
		}
	}

	public void setContext(String context) {
		this.context = context;
	}

	
	public String getUsuarioUltimoAssinar() {
		return usuarioUltimoAssinar;
	}

	public void setUsuarioUltimoAssinar(String usuarioUltimoAssinar) {
		this.usuarioUltimoAssinar = usuarioUltimoAssinar;
	}
	
	public String getNomeUsuario() {
		return nomeUsuario;
	}

	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}
	
	public Date getDataAssinatura() {
		return dataAssinatura;
	}

	public void setDataAssinatura(Date dataAssinatura) {
		this.dataAssinatura = dataAssinatura;
	}

	/**
	 * Retorna a indicação relativa a um documento ser ou não válido.
	 * 
	 * @return true, se o documento for considerado válido pelo sistema.
	 */
	
	public Boolean getValido() {
		return valido;
	}

	/**
	 * Atribui marca relativa ao documento ter sido considerado válido pelo
	 * sistema.
	 * 
	 * @param valido
	 *            true, se o documento tiver sido considerado válido
	 *            considerando as regras postas em
	 *            {@link TipoProcessoDocumentoPapel#setExigibilidade(br.jus.pje.nucleo.entidades.TipoProcessoDocumentoPapel.Exigibilidade)}
	 */
	public void setValido(Boolean valido) {
		this.valido = valido;
	}
	
	/**
	 * Indica se o documento representado por esta entidade tem seu conteúdo armazenado em um
	 * {@link DocumentoBin}, ao invés de estar armazenado no campo {@link #modeloDocumento}.
	 * 
	 * @return true, se o documento está armazenado em um objeto {@link DocumentoBin}
	 * 
	 * @see ProcessoDocumentoBinManager#getBinaryData(ProcessoDocumentoBin)
	 */
	
	public Boolean getBinario(){
		return this.binario;
	}
	
	public String getNumeroDocumentoStorage() {
		return numeroDocumentoStorage;
	}
	
	public void setBinario(Boolean binario){
		this.binario = binario;
	}
	
	public void setNumeroDocumentoStorage(String numeroDocumentoStorage) {
		this.numeroDocumentoStorage = numeroDocumentoStorage;
	}

	public File getFile(){
		return this.file;
	}

	public void setFile(File f){
		this.file = f;
	}

	public boolean isAssinado() {
		return getDataAssinatura() != null;
	}
	
	public String getIconeDocumento(){
		String icone = "fa-file-o";
		Map<String, String> variacoes = new HashMap<String, String>();
		variacoes.put("image/png", "fa fa-file-image-o");
		variacoes.put("image/jpeg", "fa fa-file-image-o");
		variacoes.put("image/jpg", "fa fa-file-image-o");
		variacoes.put("image/gif", "fa fa-file-image-o");
		variacoes.put("video/mp4", "fa fa-file-video-o");
		variacoes.put("video/ogg", "fa fa-file-video-o");
		variacoes.put("audio/mpeg", "fa fa-file-audio-o");
		variacoes.put("application/pdf", "fa fa-file-pdf-o");
		variacoes.put("application/octet-stream", "fa fa-file-pdf-o");
		variacoes.put("text/plain", "fa fa-file-text-o");
		variacoes.put("text/html", "fa fa-file-text-o");
		variacoes.put("application/excel", "fa fa-file-excel-o");
		variacoes.put("application/msword", "fa fa-file-word-o");
		variacoes.put("multipart/x-zip", "fa fa-file-archive-o");
		variacoes.put("application/zip", "fa fa-file-archive-o");
		variacoes.put("application/x-compressed", "fa fa-file-archive-o");
		
		if(!StringUtils.isBlank(this.getExtensao())){
			icone = variacoes.get(this.getExtensao());
		}
		return icone;
	}

	public String getNomeDocumentoWopi() {
		return nomeDocumentoWopi;
	}

	public void setNomeDocumentoWopi(String nomeDocumentoWopi) {
		this.nomeDocumentoWopi = nomeDocumentoWopi;
	}
	
	
}
