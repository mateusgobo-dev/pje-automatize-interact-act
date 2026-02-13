package br.jus.cnj.pje.webservice.controller.cadastropartes.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoEnum;

public class PessoaDocumentoIdentificacaoDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long id;
	private TipoDocumentoIdentificacao tipoDocumento;
	private String numeroDocumento;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dataExpedicao;
	private String nome;
	private String orgaoExpedidor;
	private String estado;
	private Boolean documentoPrincipal = Boolean.FALSE;
	private Boolean ativo = true;
	private String pais;
	private PessoaAdvogadoTipoInscricaoEnum letraOAB;
	private Boolean temporario = Boolean.FALSE;
	private String nomeMae;
	private String nomePai;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dataNascimento;
	
	
	
	public PessoaDocumentoIdentificacaoDTO() {
		super();
	}

	public PessoaDocumentoIdentificacaoDTO(PessoaDocumentoIdentificacao pdi) {
		super();
		this.id = Long.valueOf(pdi.getIdDocumentoIdentificacao()); 
		this.tipoDocumento = pdi.getTipoDocumento();
		this.numeroDocumento = pdi.getNumeroDocumento();
		this.dataExpedicao = pdi.getDataExpedicao();
		this.nome = pdi.getNome();
		this.orgaoExpedidor = pdi.getOrgaoExpedidor();
		if(pdi.getEstado() != null){
			this.estado = pdi.getEstado().getEstado();
		}
		this.documentoPrincipal = pdi.getDocumentoPrincipal();
		this.ativo = pdi.getAtivo();
		this.pais = pdi.getPais().getDescricao();
		this.letraOAB = pdi.getLetraOAB();
		this.temporario = pdi.getTemporario();
		this.nomeMae = pdi.getNomeMae();
		this.nomePai = pdi.getNomePai();
		this.dataNascimento = pdi.getDataNascimento();
	}	
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public TipoDocumentoIdentificacao getTipoDocumento() {
		return tipoDocumento;
	}
	
	public void setTipoDocumento(TipoDocumentoIdentificacao tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}
	
	public String getNumeroDocumento() {
		return numeroDocumento;
	}
	
	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}
	
	public Date getDataExpedicao() {
		return dataExpedicao;
	}
	
	public void setDataExpedicao(Date dataExpedicao) {
		this.dataExpedicao = dataExpedicao;
	}
	
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public String getOrgaoExpedidor() {
		return orgaoExpedidor;
	}
	
	public void setOrgaoExpedidor(String orgaoExpedidor) {
		this.orgaoExpedidor = orgaoExpedidor;
	}
	
	public String getEstado() {
		return estado;
	}
	
	public void setEstado(String estado) {
		this.estado = estado;
	}
	
	public Boolean getDocumentoPrincipal() {
		return documentoPrincipal;
	}
	
	public void setDocumentoPrincipal(Boolean documentoPrincipal) {
		this.documentoPrincipal = documentoPrincipal;
	}
	
	public Boolean getAtivo() {
		return ativo;
	}
	
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	public String getPais() {
		return pais;
	}
	
	public void setPais(String pais) {
		this.pais = pais;
	}
	
	public PessoaAdvogadoTipoInscricaoEnum getLetraOAB() {
		return letraOAB;
	}
	
	public void setLetraOAB(PessoaAdvogadoTipoInscricaoEnum letraOAB) {
		this.letraOAB = letraOAB;
	}
	
	public Boolean getTemporario() {
		return temporario;
	}
	
	public void setTemporario(Boolean temporario) {
		this.temporario = temporario;
	}
	
	public String getNomeMae() {
		return nomeMae;
	}
	
	public void setNomeMae(String nomeMae) {
		this.nomeMae = nomeMae;
	}
	
	public String getNomePai() {
		return nomePai;
	}
	
	public void setNomePai(String nomePai) {
		this.nomePai = nomePai;
	}
	
	public Date getDataNascimento() {
		return dataNascimento;
	}
	
	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}	
}
