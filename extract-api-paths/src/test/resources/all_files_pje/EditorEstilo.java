package br.jus.pje.nucleo.entidades.editor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Entidade responsavel pelos estilos de formatação do novo editor baseado no ckEditor
 */
@Entity
@Table(name = "tb_editor_estilo")
public class EditorEstilo {
	
	@Id
	@org.hibernate.annotations.GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_editor_estilo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@GeneratedValue(generator = "generator", strategy = GenerationType.SEQUENCE)
	@Column(name = "id_editor_estilo")
	private Integer id;
	
	@Column(name = "nm_estilo", nullable = false, length = 30, unique = true)
	private String nome;
		
	@Column(name = "json_estilo", nullable = false)
	private String json;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CssDocumento)) {
			return false;
		}
		EditorEstilo other = (EditorEstilo) obj;
		if (nome == null) {
			if (other.nome != null) {
				return false;
			}
		} else if (!nome.equals(other.nome)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return this.nome +"\n"+this.json;
	}
}
