/**
 * DevedorCda.java
 */
package br.jus.pje.nucleo.entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;

import br.jus.pje.nucleo.enums.EnumTipoDevedor;

/**
 * Classe que representa a entidade persistente <code>DevedorCda</code>.
 *
 * @author Hibernatetools
 */
@Entity
@Table(name="tb_devedor_cda")
@SuppressWarnings("all")
public class DevedorCda implements Serializable {

	/**
	 * Construtor
	 *
	 */
	public DevedorCda() {
		// Construtor
	}

	@Id
	@GeneratedValue(generator = "generator")
	@GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
			@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_devedor_cda"),
			@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Column(name="id_devedor_cda", unique=true, nullable=false, precision=10, scale=0)
	private Long id;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_cda", nullable=false)
	private Cda cda;

	@Column(name="nome", length=200)
	private String nome;

	@Enumerated(EnumType.STRING)
	@Column(name="tipo_devedor", nullable=false, length=1)
	private EnumTipoDevedor tipoDevedor;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch=FetchType.LAZY, mappedBy="devedorCda")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private List<DevedorDocIdentificacao> colecaoDevedorDocIdentificacao = new ArrayList<DevedorDocIdentificacao>(0);

	/**
	 * @return O valor do atributo id
	 */
    public Long getId() {
        return this.id;
    }
    
	/**
	 * @param id atribui um valor ao atributo id
	 */
    public void setId(Long id) {
        this.id = id;
    }

	/**
	 * @return O valor do atributo cda
	 */
	public Cda getCda() {
        return this.cda;
    }
    
    /**
	 * @param cda atribui um valor ao atributo cda
	 */
	public void setCda(Cda cda) {
        this.cda = cda;
    }
    
	/**
	 * @return O valor do atributo nome
	 */
	public String getNome() {
        return this.nome;
    }
    
    /**
	 * @param nome atribui um valor ao atributo nome
	 */
	public void setNome(String nome) {
        this.nome = nome;
    }
    
	/**
	 * @return O valor do atributo tipoDevedor
	 */
	public EnumTipoDevedor getTipoDevedor() {
        return this.tipoDevedor;
    }
    
    /**
	 * @param tipoDevedor atribui um valor ao atributo tipoDevedor
	 */
	public void setTipoDevedor(EnumTipoDevedor tipoDevedor) {
        this.tipoDevedor = tipoDevedor;
    }
    
	/**
	 * @return O valor do atributo colecaoDevedorDocIdentificacao
	 */
	public List<DevedorDocIdentificacao> getColecaoDevedorDocIdentificacao() {
        return this.colecaoDevedorDocIdentificacao;
    }
    
    /**
	 * @param colecaoDevedorDocIdentificacao atribui um valor ao atributo colecaoDevedorDocIdentificacao
	 */
	public void setColecaoDevedorDocIdentificacao(List<DevedorDocIdentificacao> colecaoDevedorDocIdentificacao) {
        this.colecaoDevedorDocIdentificacao = colecaoDevedorDocIdentificacao;
    }
    
	/**
	 * Classe estática com as constantes dos atributos da entidade.
	 *
	 */
	public static final class ATTR {
		
		/**
		 * Contrutor
		 * 
		 */
		private ATTR() {
			// Construtor.
		}
		
		public static final String ID = "id";

		public static final String CDA = "cda";

		public static final String NOME = "nome";

		public static final String TIPO_DEVEDOR = "tipoDevedor";

		public static final String COLECAO_DEVEDOR_DOC_IDENTIFICACAO = "colecaoDevedorDocIdentificacao";

	}
}
