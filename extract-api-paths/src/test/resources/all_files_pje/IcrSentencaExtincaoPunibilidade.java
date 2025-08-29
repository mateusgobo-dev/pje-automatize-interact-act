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

import br.jus.pje.nucleo.enums.TipoExtincaoPunibilidadeEnum;

/**
 * Entidade para o tipo de Informação Criminal Relevante chamado Sentença de Extinção de Punibilidade
 * 
 * Caso de Uso PJE_UC024
 * 
 * @author lucas.souza
 * 
 */
@Entity
@Table(name = "tb_icr_sentenca_ext_puni")
@PrimaryKeyJoinColumn(name = "id_icr_sentenca_ext_puni")
public class IcrSentencaExtincaoPunibilidade extends InformacaoCriminalRelevante{

	private static final long serialVersionUID = 1L;

	private PessoaMagistrado pessoaMagistrado;
	private TipoExtincaoPunibilidadeEnum tipoExtincao;
	private Date dtpublicacao;

	/*
	 * CONSTRUTORES
	 */
	public IcrSentencaExtincaoPunibilidade(){
	}

	/**
	 * Construtor: busca Icr ativo para persistência da instancia ativa
	 * 
	 * @param icr
	 */
	public IcrSentencaExtincaoPunibilidade(InformacaoCriminalRelevante icr){
		copiarPropriedadesIcr(icr);
	}

	/*
	 * UTILS
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

	/*
	 * GETTERS and SETTERS
	 */

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_pessoa_magistrado", nullable = false)
	public PessoaMagistrado getPessoaMagistrado(){
		return pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado){
		this.pessoaMagistrado = pessoaMagistrado;
	}

	@Column(name = "in_tipo_extincao", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.TipoExtincaoPunibilidadeType")
	public TipoExtincaoPunibilidadeEnum getTipoExtincao(){
		return tipoExtincao;
	}

	public void setTipoExtincao(TipoExtincaoPunibilidadeEnum tipoExtincao){
		this.tipoExtincao = tipoExtincao;
	}

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_publicacao", nullable = false)
	public Date getDtPublicacao(){
		return dtpublicacao;
	}

	public void setDtPublicacao(Date dtpublicacao){
		this.dtpublicacao = dtpublicacao;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getDtPublicacao() == null) ? 0 : dtpublicacao.hashCode());
		result = prime * result + ((getPessoaMagistrado() == null) ? 0 : pessoaMagistrado.hashCode());
		result = prime * result + ((getTipoExtincao() == null) ? 0 : tipoExtincao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof IcrSentencaExtincaoPunibilidade))
			return false;
		IcrSentencaExtincaoPunibilidade other = (IcrSentencaExtincaoPunibilidade) obj;
		if (getDtPublicacao() == null){
			if (other.getDtPublicacao() != null)
				return false;
		}
		else if (!dtpublicacao.equals(other.getDtPublicacao()))
			return false;
		if (getPessoaMagistrado() == null){
			if (other.getPessoaMagistrado() != null)
				return false;
		}
		else if (!pessoaMagistrado.equals(other.getPessoaMagistrado()))
			return false;
		if (getTipoExtincao() == null){
			if (other.getTipoExtincao() != null)
				return false;
		}
		else if (!tipoExtincao.equals(other.getTipoExtincao()))
			return false;
		return true;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrSentencaExtincaoPunibilidade.class;
	}
}
