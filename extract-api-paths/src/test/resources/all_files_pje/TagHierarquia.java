package br.jus.pje.nucleo.entidades;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;


@Entity
@Table(name = "tb_tag")
@SequenceGenerator(allocationSize = 1, name = "gen_tag", sequenceName = "sq_tb_tag")
public class TagHierarquia implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(generator = "gen_tag")
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "ds_tag", length = 100)
    private String nomeTag;
    
    @Column(name = "ds_tag_completo", length = 200)
    private String nomeTagCompleto;
    
    @Column(name = "id_localizacao")
	private Integer idLocalizacao;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tag_pai")
    private TagHierarquia pai;
    
    @OneToMany(fetch = FetchType.EAGER, mappedBy="pai")
    private List<TagHierarquia> filhos;
    
    @Transient
    private Boolean possuiFilhos;

    public TagHierarquia() {
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

	public TagHierarquia getPai() {
		return pai;
	}

	public void setPai(TagHierarquia pai) {
		this.pai = pai;
	}

	public String getNomeTagCompleto() {
		return nomeTagCompleto;
	}

	public void setNomeTagCompleto(String nomeTagCompleto) {
		this.nomeTagCompleto = nomeTagCompleto;
	}



	public List<TagHierarquia> getFilhos() {
		return filhos;
	}



	public void setFilhos(List<TagHierarquia> filhos) {
		this.filhos = filhos;
	}



	public Boolean getPossuiFilhos() {
		if ( possuiFilhos==null ) {
			possuiFilhos = filhos!=null && filhos.size()>0;
		}
		return possuiFilhos;
	}



	public void setPossuiFilhos(Boolean possuiFilhos) {
		this.possuiFilhos = possuiFilhos;
	}



	public Integer getIdLocalizacao() {
		return idLocalizacao;
	}



	public void setIdLocalizacao(Integer idLocalizacao) {
		this.idLocalizacao = idLocalizacao;
	}



	
	
	
	
}
