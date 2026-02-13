package br.jus.cnj.pje.vo;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlMimeType;

public class MuralInfo {
	
    private Date dataPublicacao;

    private List<ProcessoParteMural> processoPartesMural;

    private String textoAssuntoProcessual;
    private Date dataDecisao;
    private String numeroProcesso;
    private String siglaClasse;
    private String descricaoClasse;
    private String tipoDecisao;
    private String descricaoTipoDocumento;
    private boolean segredoJustica;
    private String ufOrigem;
    private String municipioOrigem;
    private String cargoUsuario;
    private String nomeRelator;
    private String fontePublicacao;
    private String loginUsuario;
    private String nomeUsuario;
    private String unidadeUsuario;
    
    @XmlMimeType("application/octet-stream")
    private byte[] decisaoBinaria;
    
    
    private Integer idZonaEleitoral;
    private boolean prazoMp;
    private String siglaTribunal;
    private String origemDecisao;
    private String caminhoDecisaoIsilon;
    private Integer idDecisaoPJE;
    private String numeroUnico;

    public boolean getPrazoMp() {
	return prazoMp;
    }

    public void setPrazoMp(boolean prazoMp) {
	this.prazoMp = prazoMp;
    }

    @XmlMimeType("application/octet-stream")
    private byte[] pdf;

    public Date getDataPublicacao() {
	return dataPublicacao;
    }

    public void setDataPublicacao(Date dataPublicacao) {
	this.dataPublicacao = dataPublicacao;
    }

    public String getTextoAssuntoProcessual() {
	return textoAssuntoProcessual;
    }

    public void setTextoAssuntoProcessual(String textoAssuntoProcessual) {
	this.textoAssuntoProcessual = textoAssuntoProcessual;
    }

    public Date getDataDecisao() {
	return dataDecisao;
    }

    public void setDataDecisao(Date dataDecisao) {
	this.dataDecisao = dataDecisao;
    }

    public String getCaminhoDecisaoIsilon() {
	return caminhoDecisaoIsilon;
    }

    public void setCaminhoDecisaoIsilon(String caminhoDecisaoIsilon) {
	this.caminhoDecisaoIsilon = caminhoDecisaoIsilon;
    }

    public String getOrigemDecisao() {
	return origemDecisao;
    }

    public void setOrigemDecisao(String origemDecisao) {
	this.origemDecisao = origemDecisao;
    }

    public String getSiglaTribunal() {
	return siglaTribunal;
    }

    public void setSiglaTribunal(String siglaTribunal) {
	this.siglaTribunal = siglaTribunal;
    }

    public String getNumeroProcesso() {
	return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
	this.numeroProcesso = numeroProcesso;
    }

    public String getSiglaClasse() {
	return siglaClasse;
    }

    public void setSiglaClasse(String siglaClasse) {
	this.siglaClasse = siglaClasse;
    }

    public String getDescricaoClasse() {
	return descricaoClasse;
    }

    public void setDescricaoClasse(String descricaoClasse) {
	this.descricaoClasse = descricaoClasse;
    }

    public String getTipoDecisao() {
	return tipoDecisao;
    }

    public void setTipoDecisao(String tipoDecisao) {
	this.tipoDecisao = tipoDecisao;
    }
    
    public String getDescricaoTipoDocumento() {
	return descricaoTipoDocumento;
    }

    public void setDescricaoTipoDocumento(String descricaoTipoDocumento) {
	this.descricaoTipoDocumento = descricaoTipoDocumento;
    }


    public Integer getIdZonaEleitoral() {
	return idZonaEleitoral;
    }

    public void setIdZonaEleitoral(Integer idZonaEleitoral) {
	this.idZonaEleitoral = idZonaEleitoral;
    }

    public boolean getSegredoJustica() {
	return segredoJustica;
    }

    public void setSegredoJustica(boolean segredoJustica) {
	this.segredoJustica = segredoJustica;
    }

    public String getUfOrigem() {
	return ufOrigem;
    }

    public void setUfOrigem(String ufOrigem) {
	this.ufOrigem = ufOrigem;
    }

    public String getMunicipioUfOrigem() {
    return municipioOrigem;
    }

	public void setMunicipioOrigem(String municipioOrigem) {
    this.municipioOrigem = municipioOrigem;
    }

    
    public String getCargoUsuario() {
	return cargoUsuario;
    }

    public void setCargoUsuario(String cargoUsuario) {
	this.cargoUsuario = cargoUsuario;
    }

    public String getNomeRelator() {
	return nomeRelator;
    }

    public void setNomeRelator(String nomeRelator) {
	this.nomeRelator = nomeRelator;
    }

    public List<ProcessoParteMural> getProcessoPartesMural() {
	return processoPartesMural;
    }

    public void setProcessoPartesMural(List<ProcessoParteMural> processoPartesMural) {
	this.processoPartesMural = processoPartesMural;
    }

    public String getFontePublicacao() {
	return fontePublicacao;
    }

    public void setFontePublicacao(String fontePublicacao) {
	this.fontePublicacao = fontePublicacao;
    }

    public String getLoginUsuario() {
	return loginUsuario;
    }

    public void setLoginUsuario(String loginUsuario) {
	this.loginUsuario = loginUsuario;
    }

    public String getNomeUsuario() {
	return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
	this.nomeUsuario = nomeUsuario;
    }

    public String getUnidadeUsuario() {
	return unidadeUsuario;
    }

    public void setUnidadeUsuario(String unidadeUsuario) {
	this.unidadeUsuario = unidadeUsuario;
    }

    public Integer getIdDecisaoPJE() {
	return idDecisaoPJE;
    }

    public void setIdDecisaoPJE(Integer idDecisaoPJE) {
	this.idDecisaoPJE = idDecisaoPJE;
    }
    
    public String getNumeroUnico() {
   	return numeroUnico;
    }

    public void setNumeroUnico(String numeroUnico) {
    this.numeroUnico = numeroUnico;
    }

    public byte[] getDecisaoBinaria() {
   	return decisaoBinaria;
    }

    public void setDecisaoBinaria(byte[] decisaoBinaria) {
    this.decisaoBinaria = decisaoBinaria;
    }
}
