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
package br.jus.pje.je.entidades;

import java.io.Serializable;
import java.util.Date;

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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;


/**
 * Entidade representativa de uma eleição.
 * 
 * @author TSE
 *
 */
@Entity
@Table(name = "tb_eleicao")
@org.hibernate.annotations.GenericGenerator(name = "gen_eleicao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_eleicao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Eleicao implements Serializable {

	private static final long	serialVersionUID	= -5074463080613110135L;

	private static final Integer GERAL = 1;
	
	private static final Integer MUNICIPAL = 2;

	@Id
	@Column(name = "id_eleicao", nullable = false, unique = true)
	@GeneratedValue(generator = "gen_eleicao")
	private Integer codObjeto;
	
	@Column(name = "cd_cadastro_eleitoral", length = 200)
	private String codCadastroEleitoral;
	
	@Column(name = "dt_ini_periodo_eleitoral")
	@Temporal(TemporalType.DATE)
	private Date dataInicioPeriodoEleitoral;
	
	@Column(name = "dt_fim_periodo_eleitoral")
	@Temporal(TemporalType.DATE)
	private Date dataFimPeriodoEleitoral;

	@Column(name = "nr_ano_eleicao")
	private Integer ano;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_tp_eleicao")
	private TipoEleicao tipoEleicao;

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	private Boolean ativo;
	
	/**
	 * Recupera o identificador desta entidade.
	 * 
	 * @return o identificador
	 */
	public Integer getCodObjeto() {
		return this.codObjeto;
	}

	/**
	 * Atribui a esta entidade um identificador.
	 * Em razão do mapeamento JPA, não deve ser atribuído pelo desenvolvedor.
	 * 
	 * @param codObjeto o identificador a ser atribuído
	 */
	public void setCodObjeto(Integer codObjeto) {
		this.codObjeto = codObjeto;
	}
	
	/**
	 * Recupera o código unívoco do cadastro desta eleição.
	 * 
	 * @return o código
	 */
	public String getCodCadastroEleitoral() {
		return codCadastroEleitoral;
	}

	/**
	 * Atribui a esta eleição um código unívoco.
	 * 
	 * @param codCadastroEleitoral o código a ser atribuído.
	 */
	public void setCodCadastroEleitoral(String codCadastroEleitoral) {
		this.codCadastroEleitoral = codCadastroEleitoral;
	}
	
	/**
	 * Recupera a data de início do período eleitoral de que trata esta eleição.
	 * 
	 * @return a data de início do período eleitoral
	 */
	public Date getDataInicioPeriodoEleitoral() {
		return dataInicioPeriodoEleitoral;
	}

	/**
	 * Atribui a esta eleição uma data de início do período eleitoral.
	 * 
	 * @param dataInicioPeriodoEleitoral a data a ser atribuída
	 */
	public void setDataInicioPeriodoEleitoral(Date dataInicioPeriodoEleitoral) {
		this.dataInicioPeriodoEleitoral = dataInicioPeriodoEleitoral;
	}
	
	/**
	 * Recupera a data final do período eleitoral de que trata esta eleição.
	 * 
	 * @return a data final
	 */
	public Date getDataFimPeriodoEleitoral() {
		return dataFimPeriodoEleitoral;
	}

	/**
	 * Atribui a esta eleição uma data como a final de seu período eleitoral.
	 * 
	 * @param dataFimPeriodoEleitoral a data a ser atribuída
	 */
	public void setDataFimPeriodoEleitoral(Date dataFimPeriodoEleitoral) {
		this.dataFimPeriodoEleitoral = dataFimPeriodoEleitoral;
	}
	
	/**
	 * Recupera o ano desta eleição.
	 * 
	 * @return o ano
	 */
	public Integer getAno() {
		return this.ano;
	}

	/**
	 * Atribui a esta eleição um ano.
	 * 
	 * @param ano a ser atribuído
	 */
	public void setAno(Integer ano) {
		this.ano = ano;
	}
	
	/**
	 * Recupera o tipo desta eleição.
	 * 
	 * @return o tipo desta eleição
	 */
	public TipoEleicao getTipoEleicao() {
		return tipoEleicao;
	}

	/**
	 * Atribui a esta eleição um tipo, dentre os possíveis em {@link TipoEleicao}.
	 * 
	 * @param tipoEleicao o tipo a ser atribuído
	 */
	public void setTipoEleicao(TipoEleicao tipoEleicao) {
		this.tipoEleicao = tipoEleicao;
	}

	/**
	 * Recupera marca indicativa de que este registro está ativo no sistema.
	 * 
	 * @return true, se estiver ativo
	 */
	public Boolean getAtivo() {
		return this.ativo;
	}

	/**
	 * Permite marcar a entidade como ativa ou inativa no sistema.
	 * 
	 * @param ativo a indicação relativa à atividade do registro
	 */
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codObjeto == null) ? 0 : codObjeto.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Eleicao))
			return false;
		Eleicao other = (Eleicao) obj;
		if (codObjeto == null) {
			if (other.codObjeto != null)
				return false;
		} else if (!codObjeto.equals(other.getCodObjeto()))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Eleicao [codObjeto=" + codObjeto + ", codCadastroEleitoral="
				+ codCadastroEleitoral + ", dataInicioPeriodoEleitoral="
				+ dataInicioPeriodoEleitoral + ", dataFimPeriodoEleitoral="
				+ dataFimPeriodoEleitoral + ", ano=" + ano
				+ ", tipoEleicao=" + tipoEleicao + ", ativo=" + ativo + "]";
	}	
	
	/**
	 * Indica se a eleição é geral.
	 * 
	 * @return true, se for uma eleição geral.
	 */
	public boolean isGeral() {
		return GERAL.equals(getIdTipoEleicao());
	}

	/**
	 * Indica se a eleição é municipal.
	 * 
	 * @return true, se for uma eleição municipal.
	 */
	public boolean isMunicipal() {
		return MUNICIPAL.equals(getIdTipoEleicao());
	}

	/**
	 * Recupera o identificador do tipo de eleição.
	 * 
	 * @return o identificador do tipo de eleição.
	 */
	private Integer getIdTipoEleicao() {
		if (tipoEleicao != null) {
			return tipoEleicao.getCodObjeto();
		}
		return null;
	}
	
	@Transient
    public String getDescricao() {
		if (getAno() != null && this.getTipoEleicao() != null) {
			return getAno().toString().concat(" - ").concat(this.getTipoEleicao().getDescricao());
		}
		return "";
    }


}
