package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;


public class Etiqueta implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String nomeTag;
    private String nomeTagCompleto;
    private Boolean favorita;
    private Boolean marcado = Boolean.FALSE;
    private BigInteger qtdProcessos;
    private Integer idTagPai;
    private List<Etiqueta> filhos;
    private Integer idTagFavorita;

    public Etiqueta(Integer id, String nomeTag, String nomeTagCompleto, Integer idTagPai, Integer idTagFavorita){
    	super();
    	this.id = id;
    	this.nomeTag = nomeTag;
    	this.nomeTagCompleto = nomeTagCompleto;
    	this.idTagFavorita = idTagFavorita;
    	this.idTagPai = idTagPai;
    	this.favorita = idTagFavorita!=null;
    }
    

	public Etiqueta() {
	}

    public Boolean getFavorita() {
        return favorita;
    }

    public void setFavorita(Boolean favorita) {
        this.favorita = favorita;
    }

    public Boolean getMarcado() {
        return marcado;
    }

    public void setMarcado(Boolean marcado) {
        this.marcado = marcado;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigInteger getQtdProcessos() {
        return qtdProcessos;
    }

    public void setQtdProcessos(BigInteger qtdProcessos) {
        this.qtdProcessos = qtdProcessos;
    }

	public Boolean getPossuiFilhos() {
		return filhos!=null && filhos.size()>0;
	}

	public List<Etiqueta> getFilhos() {
		return filhos;
	}

	public void setFilhos(List<Etiqueta> filhos) {
		this.filhos = filhos;
	}


	public String getNomeTag() {
		return nomeTag;
	}


	public void setNomeTag(String nomeTag) {
		this.nomeTag = nomeTag;
	}


	public String getNomeTagCompleto() {
		return nomeTagCompleto;
	}


	public void setNomeTagCompleto(String nomeTagCompleto) {
		this.nomeTagCompleto = nomeTagCompleto;
	}
	
	public Integer getIdTagFavorita() {
		return idTagFavorita;
	}
	
	public void setIdTagFavorita(Integer idTagFavorita) {
		this.idTagFavorita = idTagFavorita;
	}


	public Integer getIdTagPai() {
		return idTagPai;
	}


	public void setIdTagPai(Integer idTagPai) {
		this.idTagPai = idTagPai;
	}
    
}