package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import javax.xml.bind.annotation.XmlTransient;

public class ArquivoAssinatura implements Serializable{

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Long idTarefa;
    private String hash;
    
    @XmlTransient
    private String hashAssinatura;
    private String codIni;
    private Boolean isBin;
    @XmlTransient
    private String certChain;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getCodIni() {
        return codIni;
    }

    public void setCodIni(String codIni) {
        this.codIni = codIni;
    }

    public void setCodIniDate(Date data) {
        this.codIni = new SimpleDateFormat("HHmmssSSS").format(data);
    }

    public Boolean getIsBin() {
        return isBin;
    }

    public void setIsBin(Boolean bin) {
        isBin = bin;
    }

    public void setCertChain(String certChain) {
        this.certChain = certChain;
    }

    public String getCertChain() {
        return certChain;
    }

    public String getHashAssinatura() {
        return hashAssinatura;
    }

    public void setHashAssinatura(String hashAssinatura) {
        this.hashAssinatura = hashAssinatura;
    }

    public Long getIdTarefa() {
        return idTarefa;
    }

    public void setIdTarefa(Long idTarefa) {
        this.idTarefa = idTarefa;
    }

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 83 * hash + Objects.hashCode(this.id);
		hash = 83 * hash + Objects.hashCode(this.hashAssinatura);
		return hash;
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
		final ArquivoAssinatura other = (ArquivoAssinatura) obj;
		if (!Objects.equals(this.hashAssinatura, other.hashAssinatura)) {
			return false;
		}
		if (!Objects.equals(this.certChain, other.certChain)) {
			return false;
		}
		if (!Objects.equals(this.id, other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ArquivoAssinatura{" + "id=" + id + ", idTarefa=" + idTarefa + ", hash=" + hash + ", hashAssinatura=" + hashAssinatura + ", codIni=" + codIni + ", isBin=" + isBin + ", certChain=" + certChain + '}';
	}
		
}
