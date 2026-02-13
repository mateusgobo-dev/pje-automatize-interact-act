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

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.BeanUtils;

import br.jus.pje.nucleo.enums.TipoMedidaSegurancaEnum;

/**
 * Entidade para o tipo de Informação Criminal Relevante chamado Sentença Absolutória Imprópria
 * 
 * Caso de Uso PJE_UC023
 * 
 * @author lucas.souza
 * 
 */
@Entity
@Table(name = "tb_icr_sent_abs_impropria")
@PrimaryKeyJoinColumn(name = "id_icr_sentenca_abs_impropria")
public class IcrSentencaAbsImpropria extends InformacaoCriminalRelevante{

	private static final long serialVersionUID = 1L;

	private PessoaMagistrado pessoaMagistrado;

	private TipoMedidaSegurancaEnum inTipoMedidaSeguranca;
	private Date dtPublicacao;
	private Integer nrAnoPrazo;
	private Integer nrMesPrazo;

	/*
	 * CONSTRUTORES
	 */

	public IcrSentencaAbsImpropria(){
	}

	/**
	 * Construtor: busca Icr ativo para persistência da instancia ativa
	 * 
	 * @param icr
	 */
	public IcrSentencaAbsImpropria(InformacaoCriminalRelevante icr){
		copiarPropriedadesIcr(icr);
	}

	/*
	 * UTILS
	 */

	/**
	 * Método para tentar copiar o icr passado para a instância ativa
	 * 
	 * @param icr
	 */
	private void copiarPropriedadesIcr(InformacaoCriminalRelevante icr){
		try{
			BeanUtils.copyProperties(this, icr);
		} catch (IllegalAccessException e){
			e.printStackTrace();
		} catch (InvocationTargetException e){
			e.printStackTrace();
		}
	}

	// ------ GETTERS and SETTERS ---------------------------------------//

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_pessoa_magistrado", nullable = false)
	public PessoaMagistrado getPessoaMagistrado(){
		return pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado){
		this.pessoaMagistrado = pessoaMagistrado;
	}

	@Column(name = "in_tipo_medida_seguranca", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.TipoMedidaSegurancaType")
	public TipoMedidaSegurancaEnum getInTipoMedidaSeguranca(){
		return inTipoMedidaSeguranca;
	}

	public void setInTipoMedidaSeguranca(TipoMedidaSegurancaEnum inTipoMedidaSeguranca){
		this.inTipoMedidaSeguranca = inTipoMedidaSeguranca;
	}

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_publicacao", nullable = false)
	public Date getDtPublicacao(){
		return dtPublicacao;
	}

	public void setDtPublicacao(Date dtPublicacao){
		this.dtPublicacao = dtPublicacao;
	}

	@Column(name = "nr_ano_prazo_minimo")
	@NotNull
	public Integer getNrAnoPrazo(){
		return nrAnoPrazo;
	}

	public void setNrAnoPrazo(Integer nrAnoPrazo){
		this.nrAnoPrazo = nrAnoPrazo;
	}

	@Column(name = "nr_mes_prazo_minimo")
	@NotNull
	public Integer getNrMesPrazo(){
		return nrMesPrazo;
	}

	public void setNrMesPrazo(Integer nrMesPrazo){
		this.nrMesPrazo = nrMesPrazo;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getDtPublicacao() == null) ? 0 : dtPublicacao.hashCode());
		result = prime * result + ((getInTipoMedidaSeguranca() == null) ? 0 : inTipoMedidaSeguranca.hashCode());
		result = prime * result + ((getNrAnoPrazo() == null) ? 0 : nrAnoPrazo.hashCode());
		result = prime * result + ((getNrMesPrazo() == null) ? 0 : nrMesPrazo.hashCode());
		result = prime * result + ((getPessoaMagistrado() == null) ? 0 : pessoaMagistrado.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof IcrSentencaAbsImpropria))
			return false;
		IcrSentencaAbsImpropria other = (IcrSentencaAbsImpropria) obj;
		if (getDtPublicacao() == null){
			if (other.getDtPublicacao() != null)
				return false;
		}
		else if (!dtPublicacao.equals(other.getDtPublicacao()))
			return false;
		if (getInTipoMedidaSeguranca() == null){
			if (other.getInTipoMedidaSeguranca() != null)
				return false;
		}
		else if (!inTipoMedidaSeguranca.equals(other.getInTipoMedidaSeguranca()))
			return false;
		if (getNrAnoPrazo() == null){
			if (other.getNrAnoPrazo() != null)
				return false;
		}
		else if (!nrAnoPrazo.equals(other.getNrAnoPrazo()))
			return false;
		if (getNrMesPrazo() == null){
			if (other.getNrMesPrazo() != null)
				return false;
		}
		else if (!nrMesPrazo.equals(other.getNrMesPrazo()))
			return false;
		if (getPessoaMagistrado() == null){
			if (other.getPessoaMagistrado() != null)
				return false;
		}
		else if (!pessoaMagistrado.equals(other.getPessoaMagistrado()))
			return false;
		return true;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrSentencaAbsImpropria.class;
	}
}
