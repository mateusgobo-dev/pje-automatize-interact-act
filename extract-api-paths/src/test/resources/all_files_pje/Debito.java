/**
 * Debito.java
 */
package br.jus.pje.nucleo.entidades;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.json.JSONObject;

import br.jus.pje.nucleo.type.StringJsonUserType;

/**
 * Classe que representa a entidade persistente <code>Debito</code>.
 *
 * @author Hibernatetools
 */
@Entity
@Table(name="tb_debito")
@TypeDefs({
    @TypeDef(name = "jsonb", typeClass = StringJsonUserType.class)
})
@SuppressWarnings("all")
public class Debito implements Serializable {

	/**
	 * Construtor
	 *
	 */
	public Debito() {
		// Construtor
	}

	@Id
	@GeneratedValue(generator = "generator")
	@GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
			@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_debito"),
			@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Column(name="id_debito", unique=true, nullable=false, precision=10, scale=0)
	private Long id;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_cda", nullable=false)
	private Cda cda;

	@Temporal(TemporalType.DATE)
	@Column(name="dt_exercicio", length=13)
	private Date dataExercicio;

	@Column(name="cd_natureza", length=50)
	private String codigoNatureza;

	@Column(name="ds_natureza", length=200)
	private String descricaoNatureza;

	@Type(type = "jsonb")
	@Column(name="dados")
	private String dados;
	
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
	 * @return O valor do atributo dataExercicio
	 */
	public Date getDataExercicio() {
        return this.dataExercicio;
    }
    
    /**
	 * @param dataExercicio atribui um valor ao atributo dataExercicio
	 */
	public void setDataExercicio(Date dataExercicio) {
        this.dataExercicio = dataExercicio;
    }
    
	/**
	 * @return O valor do atributo codigoNatureza
	 */
	public String getCodigoNatureza() {
        return this.codigoNatureza;
    }
    
    /**
	 * @param codigoNatureza atribui um valor ao atributo codigoNatureza
	 */
	public void setCodigoNatureza(String codigoNatureza) {
        this.codigoNatureza = codigoNatureza;
    }
    
	/**
	 * @return O valor do atributo descricaoNatureza
	 */
	public String getDescricaoNatureza() {
        return this.descricaoNatureza;
    }
    
    /**
	 * @param descricaoNatureza atribui um valor ao atributo descricaoNatureza
	 */
	public void setDescricaoNatureza(String descricaoNatureza) {
        this.descricaoNatureza = descricaoNatureza;
    }
    
	/**
	 * @return O valor do atributo dados
	 */
	public String getDados() {
        return this.dados;
    }
    
    /**
	 * @param dados atribui um valor ao atributo dados
	 */
	public void setDados(String dados) {
        this.dados = dados;
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

		public static final String DATA_EXERCICIO = "dataExercicio";

		public static final String CODIGO_NATUREZA = "codigoNatureza";

		public static final String DESCRICAO_NATUREZA = "descricaoNatureza";

		public static final String DADOS = "dados";

	}
}
