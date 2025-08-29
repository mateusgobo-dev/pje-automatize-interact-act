package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;

public class EtiquetaProcesso implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String nomeTag;
    private String nomeTagCompleto;
    private Boolean favorita;
    private Integer idUsuario;
    private Long idProcesso;

    public EtiquetaProcesso(Integer id,String nomeTag,Integer idUsuario, Long idProcesso){
        this.id = id;
        this.nomeTag = nomeTag;
        this.idUsuario = idUsuario;
        this.idProcesso = idProcesso;
    }

    public EtiquetaProcesso(Integer id,String nomeTag, String nomeTagCompleto,Integer idUsuario, Long idProcesso){
        this.id = id;
        this.nomeTag = nomeTag;
        this.idUsuario = idUsuario;
        this.idProcesso = idProcesso;
        this.nomeTagCompleto = nomeTagCompleto;
    }    
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeTag() {
        return nomeTag;
    }

    public void setNomeTag(String nomeTag) {
        this.nomeTag = nomeTag;
    }

    public Boolean getFavorita() {
        return favorita;
    }

    public void setFavorita(Boolean favorita) {
        this.favorita = favorita;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdProcesso() {
        return idProcesso;
    }

    public void setIdProcesso(Long idProcesso) {
        this.idProcesso = idProcesso;
    }
    
    public String getNomeTagCompleto() {
		return nomeTagCompleto;
	}
    
    public void setNomeTagCompleto(String nomeTagCompleto) {
		this.nomeTagCompleto = nomeTagCompleto;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		EtiquetaProcesso other = (EtiquetaProcesso) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
    
    
}