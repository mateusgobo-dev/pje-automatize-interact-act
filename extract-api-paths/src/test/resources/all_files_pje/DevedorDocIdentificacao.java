/**
 * DevedorDocIdentificacao.java
 */
package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * Classe que representa a entidade persistente <code>DevedorDocIdentificacao</code>.
 *
 * @author Hibernatetools
 */
@Entity
@Table(name="tb_devedor_doc_identificacao")
@SuppressWarnings("all")
public class DevedorDocIdentificacao implements Serializable {

	/**
	 * Construtor
	 *
	 */
	public DevedorDocIdentificacao() {
		// Construtor
	}

	@Id
	@GeneratedValue(generator = "generator")
	@GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
			@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_devedor_doc_identificacao"),
			@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Column(name="id_devedor_doc_identificacao", unique=true, nullable=false, precision=10, scale=0)
	private Long id;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_devedor_cda", nullable=false)
	private DevedorCda devedorCda;

	@Column(name="nome_devedor", length=200)
	private String nomeDevedor;

	@Column(name="cd_tipo", nullable=false, length=10)
	private String codigoTipo;

	@Column(name="numero", nullable=false, length=50)
	private String numero;

	@Column(name="ds_orgao_expedidor", length=200)
	private String descricaoOrgaoExpedidor;
	
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
	 * @return O valor do atributo devedorCda
	 */
	public DevedorCda getDevedorCda() {
        return this.devedorCda;
    }
    
    /**
	 * @param devedorCda atribui um valor ao atributo devedorCda
	 */
	public void setDevedorCda(DevedorCda devedorCda) {
        this.devedorCda = devedorCda;
    }
    
	/**
	 * @return O valor do atributo nomeDevedor
	 */
	public String getNomeDevedor() {
        return this.nomeDevedor;
    }
    
    /**
	 * @param nomeDevedor atribui um valor ao atributo nomeDevedor
	 */
	public void setNomeDevedor(String nomeDevedor) {
        this.nomeDevedor = nomeDevedor;
    }
    
	/**
	 * @return O valor do atributo codigoTipo
	 */
	public String getCodigoTipo() {
        return this.codigoTipo;
    }
    
    /**
	 * @param codigoTipo atribui um valor ao atributo codigoTipo
	 */
	public void setCodigoTipo(String codigoTipo) {
        this.codigoTipo = codigoTipo;
    }
    
	/**
	 * @return O valor do atributo numero
	 */
	public String getNumero() {
        return this.numero;
    }
    
    /**
	 * @param numero atribui um valor ao atributo numero
	 */
	public void setNumero(String numero) {
        this.numero = numero;
    }
    
	/**
	 * @return O valor do atributo descricaoOrgaoExpedidor
	 */
	public String getDescricaoOrgaoExpedidor() {
        return this.descricaoOrgaoExpedidor;
    }
    
    /**
	 * @param descricaoOrgaoExpedidor atribui um valor ao atributo descricaoOrgaoExpedidor
	 */
	public void setDescricaoOrgaoExpedidor(String descricaoOrgaoExpedidor) {
        this.descricaoOrgaoExpedidor = descricaoOrgaoExpedidor;
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

		public static final String DEVEDOR_CDA = "devedorCda";

		public static final String NOME_DEVEDOR = "nomeDevedor";

		public static final String CODIGO_TIPO = "codigoTipo";

		public static final String NUMERO = "numero";

		public static final String DESCRICAO_ORGAO_EXPEDIDOR = "descricaoOrgaoExpedidor";

	}
}
