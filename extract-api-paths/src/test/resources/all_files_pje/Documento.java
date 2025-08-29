package br.jus.pje.nucleo.entidades;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_documento")
@org.hibernate.annotations.GenericGenerator(name = "gen_documento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Documento implements java.io.Serializable{

	private static final long serialVersionUID = 5607827904567711771L;
	
	@Id
	@GeneratedValue(generator = "gen_documento")
	@Column(name = "id_documento", unique = true, nullable = false)
	private Long idDocumento;
	
	@Column(name = "ds_documento", nullable = false, length = 100)
	@NotNull
	private String nome;
	
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	private Boolean ativo;
	
	@Column(name = "ds_mimetype", nullable = true, length = 50)
	private String mimeType;
	
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_conteudo", nullable = true)
	private String conteudo;
	
	@Column(name = "nr_tamanho", nullable = true)
	private Integer tamanho;
	
	@Column(name = "in_binario", nullable = false)
	@NotNull
	private Boolean binario;
	
	@Column(name = "in_valido", nullable = false)
	@NotNull
	private Boolean valido;
	
	@Column(name = "ds_identificador_storage", nullable = true, length = 255)
	@Length(max = 255)
	private String identificadorStorage;
	
	@OneToMany(mappedBy="documento", cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	private List<DocumentoHistorico> historicosDocumento;
	
	public Long getIdDocumento() {
		return idDocumento;
	}
	
	public void setIdDocumento(Long idDocumento) {
		this.idDocumento = idDocumento;
	}
	
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public Boolean getAtivo() {
		return ativo;
	}
	
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	public String getMimeType() {
		return mimeType;
	}
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getConteudo() {
		return conteudo;
	}
	
	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}
	
	public Integer getTamanho() {
		return tamanho;
	}
	
	public void setTamanho(Integer tamanho) {
		this.tamanho = tamanho;
	}
	
	public Boolean getBinario() {
		return binario;
	}
	
	public void setBinario(Boolean binario) {
		this.binario = binario;
	}
	
	public Boolean getValido() {
		return valido;
	}
	
	public void setValido(Boolean valido) {
		this.valido = valido;
	}
	
	public String getIdentificadorStorage() {
		return identificadorStorage;
	}
	
	public void setIdentificadorStorage(String identificadorStorage) {
		this.identificadorStorage = identificadorStorage;
	}

	public List<DocumentoHistorico> getHistoricosDocumento() {
		return historicosDocumento;
	}

	public void setHistoricosDocumento(List<DocumentoHistorico> historicosDocumento) {
		this.historicosDocumento = historicosDocumento;
	}

}
