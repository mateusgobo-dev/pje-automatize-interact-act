package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.lang.StringUtils;

@Entity
@Table(name = "tb_tag")
@org.hibernate.annotations.GenericGenerator(name = "gen_tag", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tag"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tag implements Serializable {

	private static final long serialVersionUID = 1L;


	@Id
	@GeneratedValue(generator = "gen_tag")
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;

	@Column(name = "ds_tag", length = 30)
	private String nomeTag;

    @Column(name = "ds_tag_completo", length = 200)
    private String nomeTagCompleto;

	@Column(name = "id_localizacao")
	private Integer idLocalizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tag_pai")
    private Tag pai;

	@Column(name = "in_sistema")
	private Boolean deSistema = Boolean.FALSE;

	@Column(name = "in_publica")
	private Boolean visivelPublicamente = Boolean.FALSE;

	public Tag() {
	}

	public Tag(String nomeTag, Integer idLocalizacao) {
		this.nomeTag = nomeTag;
		this.idLocalizacao = idLocalizacao;
		this.nomeTagCompleto = nomeTag;
	}

    public Tag(String nomeTag, String nomeTagCompleto, Integer idLocalizacao, Integer idTagPai) {
    	this(nomeTag, idLocalizacao);
        if ( idTagPai!=null ) {
	        this.pai = new Tag();
	        this.pai.setId(idTagPai);
        }
		this.nomeTagCompleto = nomeTagCompleto;
		if (StringUtils.isBlank(nomeTagCompleto)) {
			this.nomeTagCompleto = nomeTag;
		}
    }
    
    public Tag(Integer id, String nomeTag, String nomeTagCompleto, Integer idLocalizacao, Integer idTagPai) {
    	this(nomeTag, nomeTagCompleto, idLocalizacao, idTagPai);
    	this.id = id;
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deSistema == null) ? 0 : deSistema.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((idLocalizacao == null) ? 0 : idLocalizacao.hashCode());
		result = prime * result + ((nomeTag == null) ? 0 : nomeTag.hashCode());
		result = prime * result + ((nomeTagCompleto == null) ? 0 : nomeTagCompleto.hashCode());
		result = prime * result + ((pai == null) ? 0 : pai.hashCode());
		result = prime * result + ((visivelPublicamente == null) ? 0 : visivelPublicamente.hashCode());
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
		Tag other = (Tag) obj;
		if (deSistema == null) {
			if (other.deSistema != null)
				return false;
		} else if (!deSistema.equals(other.deSistema))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (idLocalizacao == null) {
			if (other.idLocalizacao != null)
				return false;
		} else if (!idLocalizacao.equals(other.idLocalizacao))
			return false;
		if (nomeTag == null) {
			if (other.nomeTag != null)
				return false;
		} else if (!nomeTag.equals(other.nomeTag))
			return false;
		if (nomeTagCompleto == null) {
			if (other.nomeTagCompleto != null)
				return false;
		} else if (!nomeTagCompleto.equals(other.nomeTagCompleto))
			return false;
		if (pai == null) {
			if (other.pai != null)
				return false;
		} else if (!pai.equals(other.pai))
			return false;
		if (visivelPublicamente == null) {
			if (other.visivelPublicamente != null)
				return false;
		} else if (!visivelPublicamente.equals(other.visivelPublicamente))
			return false;
		return true;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id= id;
	}
	public String getNomeTag() {
		return nomeTag;
	}
	public void setNomeTag(String nomeTag) {
		this.nomeTag = nomeTag;
	}
	public Integer getIdLocalizacao() {
		return idLocalizacao;
	}
	public void setIdLocalizacao(Integer idLocalizacao) {
		this.idLocalizacao = idLocalizacao;
	}
	
	public Tag getPai() {
		return pai;
	}

	public void setPai(Tag pai) {
		this.pai = pai;
	}

	public String getNomeTagCompleto() {
		return nomeTagCompleto;
	}

	public void setNomeTagCompleto(String nomeTagCompleto) {
		this.nomeTagCompleto = nomeTagCompleto;
	}

	public Boolean getDeSistema() {
		return deSistema;
	}

	public void setDeSistema(Boolean deSistema) {
		this.deSistema = deSistema;
	}

	public Boolean getVisivelPublicamente() {
		return visivelPublicamente;
	}

	public void setVisivelPublicamente(Boolean visivelPublicamente) {
		this.visivelPublicamente = visivelPublicamente;
	}	
}
