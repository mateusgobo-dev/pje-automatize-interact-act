/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

/**
 * Entidade representativa de um tipo de voto proferido em decisão colegiada.
 * Os votos têm, necessariamente, um contexto de produção. Esse contexto indica
 * se o voto vale por si só ou se demanda ou presume uma vinculação a outro voto.
 * 
 * @see SessaoProcessoDocumentoVoto
 *
 */
@Entity
@Table(name = TipoVoto.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_voto", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_voto"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoVoto implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoVoto,Integer> {

	public static final String TABLE_NAME = "tb_tipo_voto";
	private static final long serialVersionUID = 1L;

	private int idTipoVoto;
	private String tipoVoto;
	private String textoCertidao;
	private Boolean relator;
	private String contexto;
	private Boolean ativo;
	private String cor;

	/**
	 * Construtor padrão da entidade
	 */
	public TipoVoto() {
	}

	@Override
	public String toString() {
		return this.tipoVoto;
	}

	/**
	 * Identificador da entidade na camada de persistência.
	 * É autogerado.
	 * 
	 * @return o identificador
	 */
	@Id
	@GeneratedValue(generator = "gen_tipo_voto")
	@Column(name = "id_tipo_voto", unique = true, nullable = false)
	public int getIdTipoVoto() {
		return idTipoVoto;
	}

	/**
	 * Atribui um identificador a esta entidade na camada de persistência.
	 * Por se tratar de parâmetro autogerado, a utilização manual desse método
	 * levará a uma exceção.
	 * 
	 * @param idTipoVoto o identificador a ser atribuído.
	 */
	public void setIdTipoVoto(int idTipoVoto) {
		this.idTipoVoto = idTipoVoto;
	}

	/**
	 * Recupera um descritor do tipo de voto proferido. 
	 * O descritor deverá ser um texto em primeira pessoa ("Julgo procedente", "Dou provimento" etc.).
	 * 
	 * @return o descritor
	 */
	@Column(name = "ds_tipo_voto", length = 100)
	@Length(max = 100)
	public String getTipoVoto() {
		return tipoVoto;
	}

	/**
	 * Atribui um descritor a este tipo de voto. O descritor deverá ser 
	 * definido na primeira pessoa ("Julgo procedente", "Dou provimento" etc.).
	 * 
	 * @param tipoVoto o descritor a ser atribuído
	 */
	public void setTipoVoto(String tipoVoto) {
		this.tipoVoto = tipoVoto;
	}

	/**
	 * Recupera o texto a ser exibido em certidões em que este tipo de voto
	 * foi o vencedor. Assim, se um tipo de voto tiver o descritor "Julgo procedente",
	 * o texto da certidão deverá ser "julgou procedente".
	 * 
	 * @return o texto a ser ordinariamente utilizado em certidões
	 */
	@Column(name = "ds_texto_certidao", length = 100)
	@Length(max = 100)
	public String getTextoCertidao() {
		return textoCertidao;
	}

	/**
	 * Atribui o texto a ser exibido em certidões em que este tipo de voto
	 * foi o vencedor. Esse texto deve ser definido na terceira pessoa, sempre
	 * considerando como sujeito o órgão colegiado. Desse modo, se um tipo de 
	 * voto tiver o descritor "Julgo procedente", o texto da certidão deverá 
	 * ser "julgou procedente".
	 * 
	 * @param textoCertidao o texto a ser utilizado em certidões
	 */
	public void setTextoCertidao(String textoCertidao) {
		this.textoCertidao = textoCertidao;
	}

	/**
	 * Recupera marca indicativa de que este tipo de voto somente se aplica ao
	 * relator ou ao relator para o acórdão.
	 * 
	 * @return true, se este tipo de voto se aplicar apenas ao relator ou ao vencedor
	 */
	@Column(name = "in_relator", nullable = false)
	@NotNull
	public Boolean getRelator() {
		return relator;
	}

	/**
	 * Atribui marca indicativa quanto à aplicabilidade única deste tipo de voto ao
	 * relator ou ao relator para o acórdão.
	 * 
	 * @param relator true, se o tipo de voto for exclusivo de relator ou vencedor
	 */
	public void setRelator(Boolean relator) {
		this.relator = relator;
	}

	/**
	 * Recupera o contexto em que este tipo de voto é proferido.
	 * Especificamente, são valores vários:
	 * <li>C - em que houve a concordância do prolator do voto com o voto vencedor, que pode ser o próprio voto</li>
	 * <li>P - em que houve a concordância parcial do prolator do voto com voto do relator</li>
	 * <li>D - em que houve a discordância completa do prolator do voto com o voto do relator</li>
	 * <li>N - em que o prolator do voto discorda do voto do relator para não conhecer do recurso</li>
	 * <li>I - em que o prolator do voto declara-se impedido</li>
	 * <li>S - em que o prolator do voto declara-se suspeito</li>
	 *    
	 * @return o contexto
	 */
	@Column(name = "in_contexto", length = 1)
	@Length(max = 1)
	public String getContexto() {
		return contexto;
	}

	/**
	 * Atribui a este tipo de voto um contexto de utilização.
	 * 
	 * @param contexto o contexto a ser aplicado
	 * @see #getContexto()
	 */
	public void setContexto(String contexto) {
		this.contexto = contexto;
	}

	/**
	 * Indica se este tipo de voto está ativo na aplicação.
	 * 
	 * @return true, se o tipo de voto estiver ativo
	 */
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	/**
	 * Permitir indicar se este tipo de voto está ativo no sistema.
	 * 
	 * @param ativo indicação quanto à atividade deste tipo de voto
	 */
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoVoto)) {
			return false;
		}
		TipoVoto other = (TipoVoto) obj;
		if (getIdTipoVoto() != other.getIdTipoVoto()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoVoto();
		return result;
	}

	@Column(name = "ds_cor")
	public String getCor() {
		return cor;
	}

	public void setCor(String cor) {
		this.cor = cor;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoVoto> getEntityClass() {
		return TipoVoto.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoVoto());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
