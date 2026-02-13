package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
@Entity
@Table(name = "tb_tag")
@Immutable
@SequenceGenerator(allocationSize = 1, name = "gen_tag", sequenceName = "sq_tb_tag")
public class TagMin implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(generator = "gen_tag")
    @Column(name = "id", unique = true, nullable = false, insertable = false, updatable = false)
    private Integer id;

    @Column(name = "ds_tag", length = 30)
    private String nomeTag;
    
    @Column(name = "ds_tag_completo", length = 60)
    private String nomeTagCompleto;

	@Column(name = "id_localizacao", insertable = false, updatable = false)
	private Integer idLocalizacao;
    
    @Column(name = "id_tag_pai", insertable = false, updatable = false)
    private Integer idTagPai;

    @Transient
    private Integer idUsuario;
    
	@Column(name = "in_sistema")
	private Boolean deSistema = Boolean.FALSE;

	@Column(name = "in_publica")
	private Boolean visivelPublicamente = Boolean.FALSE;

    public TagMin() {
    }

    public TagMin(String nomeTag, Integer idLocalizacao) {
        this.idLocalizacao = idLocalizacao;
        this.nomeTag = nomeTag;
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
        if (StringUtils.isBlank(this.nomeTagCompleto)) {
            this.setNomeTagCompleto(nomeTag);
        }
    }
    
    public Integer getIdLocalizacao() {
		return idLocalizacao;
	}

	public void setIdLocalizacao(Integer idLocalizacao) {
		this.idLocalizacao = idLocalizacao;
	}

	public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

	public String getNomeTagCompleto() {
		return nomeTagCompleto;
	}

	public void setNomeTagCompleto(String nomeTagCompleto) {
		this.nomeTagCompleto = nomeTagCompleto;
	}

	public Integer getIdTagPai() {
		return idTagPai;
	}

	public void setIdTagPai(Integer idTagPai) {
		this.idTagPai = idTagPai;
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
