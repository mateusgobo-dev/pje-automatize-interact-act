/**
 * Cda.java
 */
package br.jus.pje.nucleo.entidades;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;

import br.jus.pje.nucleo.enums.EnumTipoValorCda;

/**
 * Classe que representa a entidade persistente <code>Cda</code>.
 *
 * @author Hibernatetools
 */
@Entity
@Table(name="tb_cda")
@SuppressWarnings("all")
public class Cda implements Serializable {

	/**
	 * Construtor
	 *
	 */
	public Cda() {
		// Construtor
	}

	@Id
	@GeneratedValue(generator = "generator")
	@GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
			@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_cda"),
			@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Column(name="id_cda", unique=true, nullable=false, precision=10, scale=0)
	private Long id;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_processo_trf", nullable=false)
	private ProcessoTrf processoTrf;

	@Column(name="numero", nullable=false, length=20)
	private String numero;

	@Column(name="nr_processo_adm", length=20)
	private String numeroProcessoAdm;

	@Column(name="nr_controle", length=20)
	private String numeroControle;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="dt_apuracao", nullable=false, length=29)
	private Date dataApuracao;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="dt_prescricao", length=29)
	private Date dataPrescricao;

	@Column(name="in_credito_tributario", nullable=false)
	private Boolean inCreditoTributario = Boolean.FALSE;

	@Column(name="valor", nullable=false, precision=10)
	private BigDecimal valor;

	@Column(name="moeda_valor", nullable=false, length=30)
	private String moedaValor;

	@Column(name="in_ativo")
	private Boolean ativo = Boolean.TRUE;
	
	@Enumerated(EnumType.STRING)
	@Column(name="tipo_valor_cda", nullable=false, length=1)
	private EnumTipoValorCda tipoValorCda;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch=FetchType.LAZY, mappedBy="cda")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private List<Debito> colecaoDebito = new ArrayList<Debito>(0);

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch=FetchType.LAZY, mappedBy="cda")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private List<DevedorCda> colecaoDevedorCda = new ArrayList<DevedorCda>(0);
	
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
	 * @return O valor do atributo processoTrf
	 */
	public ProcessoTrf getProcessoTrf() {
        return this.processoTrf;
    }
    
    /**
	 * @param processoTrf atribui um valor ao atributo processoTrf
	 */
	public void setProcessoTrf(ProcessoTrf processoTrf) {
        this.processoTrf = processoTrf;
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
	 * @return O valor do atributo numeroProcessoAdm
	 */
	public String getNumeroProcessoAdm() {
        return this.numeroProcessoAdm;
    }
    
    /**
	 * @param numeroProcessoAdm atribui um valor ao atributo numeroProcessoAdm
	 */
	public void setNumeroProcessoAdm(String numeroProcessoAdm) {
        this.numeroProcessoAdm = numeroProcessoAdm;
    }
    
	/**
	 * @return O valor do atributo numeroControle
	 */
	public String getNumeroControle() {
        return this.numeroControle;
    }
    
    /**
	 * @param numeroControle atribui um valor ao atributo numeroControle
	 */
	public void setNumeroControle(String numeroControle) {
        this.numeroControle = numeroControle;
    }
    
	/**
	 * @return O valor do atributo dataApuracao
	 */
	public Date getDataApuracao() {
        return this.dataApuracao;
    }
    
    /**
	 * @param dataApuracao atribui um valor ao atributo dataApuracao
	 */
	public void setDataApuracao(Date dataApuracao) {
        this.dataApuracao = dataApuracao;
    }
    
	/**
	 * @return O valor do atributo dataPrescricao
	 */
	public Date getDataPrescricao() {
        return this.dataPrescricao;
    }
    
    /**
	 * @param dataPrescricao atribui um valor ao atributo dataPrescricao
	 */
	public void setDataPrescricao(Date dataPrescricao) {
        this.dataPrescricao = dataPrescricao;
    }
    
	/**
	 * @return O valor do atributo inCreditoTributario
	 */
	public Boolean getInCreditoTributario() {
        return this.inCreditoTributario;
    }
    
    /**
	 * @param inCreditoTributario atribui um valor ao atributo inCreditoTributario
	 */
	public void setInCreditoTributario(Boolean inCreditoTributario) {
        this.inCreditoTributario = inCreditoTributario;
    }
    
	/**
	 * @return O valor do atributo valor
	 */
	public BigDecimal getValor() {
        return this.valor;
    }
    
    /**
	 * @param valor atribui um valor ao atributo valor
	 */
	public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    
	/**
	 * @return O valor do atributo moedaValor
	 */
	public String getMoedaValor() {
        return this.moedaValor;
    }
    
    /**
	 * @param moedaValor atribui um valor ao atributo moedaValor
	 */
	public void setMoedaValor(String moedaValor) {
        this.moedaValor = moedaValor;
    }
	
	/**
	 * @return ativo
	 */
	public Boolean getAtivo(){
		return this.ativo;
	}

	/**
	 * @param ativo Atribui um valor ao atributo ativo.
	 */
	public void setAtivo(Boolean ativo){
		this.ativo = ativo;
	}
    
	/**
	 * @return O valor do atributo tipoValorCda
	 */
	public EnumTipoValorCda getTipoValorCda() {
        return this.tipoValorCda;
    }
    
    /**
	 * @param tipoValorCda atribui um valor ao atributo tipoValorCda
	 */
	public void setTipoValorCda(EnumTipoValorCda tipoValorCda) {
        this.tipoValorCda = tipoValorCda;
    }
    
	/**
	 * @return O valor do atributo colecaoDebito
	 */
	public List<Debito> getColecaoDebito() {
        return this.colecaoDebito;
    }
    
    /**
	 * @param colecaoDebito atribui um valor ao atributo colecaoDebito
	 */
	public void setColecaoDebito(List<Debito> colecaoDebito) {
        this.colecaoDebito = colecaoDebito;
    }
    
	/**
	 * @return O valor do atributo colecaoDevedorCda
	 */
	public List<DevedorCda> getColecaoDevedorCda() {
        return this.colecaoDevedorCda;
    }
    
    /**
	 * @param colecaoDevedorCda atribui um valor ao atributo colecaoDevedorCda
	 */
	public void setColecaoDevedorCda(List<DevedorCda> colecaoDevedorCda) {
        this.colecaoDevedorCda = colecaoDevedorCda;
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

		public static final String PROCESSO_TRF = "processoTrf";

		public static final String NUMERO = "numero";

		public static final String NUMERO_PROCESSO_ADM = "numeroProcessoAdm";

		public static final String NUMERO_CONTROLE = "numeroControle";

		public static final String NUMERO_APA = "numeroApa";

		public static final String DATA_APURACAO = "dataApuracao";

		public static final String DATA_PRESCRICAO = "dataPrescricao";

		public static final String IN_CREDITO_TRIBUTARIO = "inCreditoTributario";

		public static final String VALOR = "valor";

		public static final String MOEDA_VALOR = "moedaValor";

		public static final String TIPO_VALOR_CDA = "tipoValorCda";

		public static final String COLECAO_DEBITO = "colecaoDebito";

		public static final String COLECAO_DEVEDOR_CDA = "colecaoDevedorCda";

	}
}
