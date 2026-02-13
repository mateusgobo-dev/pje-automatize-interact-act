/**
 * TipoNaturezaDebito.java
 */
package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;

/**
 * Classe que representa a entidade persistente <code>TipoNaturezaDebito</code>.
 *
 * @author Hibernatetools
 */
@Entity
@Table(name="tb_tipo_natureza_debito")
@SuppressWarnings("all")
public class TipoNaturezaDebito implements java.io.Serializable {

	/**
	 * Construtor
	 *
	 */
	public TipoNaturezaDebito() {
		// Construtor
	}

	@Id
	@GeneratedValue(generator = "generator")
	@GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
			@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_natureza_debito"),
			@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Column(name="id_tipo_natureza_debito", unique=true, nullable=false, precision=10, scale=0)
	private Long id;

	@Column(name="cd_tipo_natureza_debito", nullable=false, length=20)
	private String codigo;

	@Column(name="ds_tipo_natureza_debito", nullable = false, length=100)
	@Length(max = 100)
	private String descricao;

	/**
	 * @return id.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id Atribui id.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return codigo.
	 */
	public String getCodigo() {
		return codigo;
	}

	/**
	 * @param codigo Atribui codigo.
	 */
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	/**
	 * @return descricao.
	 */
	public String getDescricao() {
		return descricao;
	}

	/**
	 * @param descricao Atribui descricao.
	 */
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}
