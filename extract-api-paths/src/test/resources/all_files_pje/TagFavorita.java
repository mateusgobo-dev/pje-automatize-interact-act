package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name = "tb_tag_favorita")
@SequenceGenerator(allocationSize = 1, name = "gen_tag_favorita", sequenceName = "sq_tb_tag_favorita")
public class TagFavorita implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(generator = "gen_tag_favorita")
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name="id_usuario")
    private Integer idUsuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tag", nullable = false)
    private TagMin tag;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public TagMin getTag() {
        return tag;
    }

    public void setTag(TagMin tag) {
        this.tag = tag;
    }
}