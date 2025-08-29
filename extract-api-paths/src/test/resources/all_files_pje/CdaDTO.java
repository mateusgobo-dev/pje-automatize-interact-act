/*
 * CdaDTO.java
 *
 * Data: 20/05/2021
 */
package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import br.jus.pje.nucleo.entidades.Cda;
import br.jus.pje.nucleo.entidades.Debito;
import br.jus.pje.nucleo.entidades.DevedorCda;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.EnumTipoValorCda;

/**
 * Classe decorator da entidade Cda.
 * 
 * @author Adriano Pamplona
 */
public class CdaDTO implements Serializable {

	private Cda cda;
	private Collection<DevedorCdaDTO> colecaoDevedorCdaDTO;
	private Collection<DebitoDTO> colecaoDebitoDTO;

	/**
	 * Construtor.
	 *
	 * @param cda
	 */
	public CdaDTO(Cda cda) {
		setCda(cda);
	}

	/**
	 * @return cda.
	 */
	public Cda getCda() {
		if (this.cda == null) {
			this.cda = new Cda();
		} 
		return cda;
	}
	
	/**
	 * @param cda Atribui cda.
	 */
	public void setCda(Cda cda) {
		this.cda = cda;

		getColecaoDevedorCdaDTO().clear();
		getColecaoDebitoDTO().clear();
		if (cda != null) {
			List<DevedorCda> devedores = getColecaoDevedorCda();
			for (DevedorCda devedor : devedores) {
				getColecaoDevedorCdaDTO().add(new DevedorCdaDTO(devedor, this));
			}
			List<Debito> debitos = getColecaoDebito();
			for (Debito debito : debitos) {
				getColecaoDebitoDTO().add(new DebitoDTO(debito, this));
			}
		}
	}

	/**
	 * @return colecaoDebitoDTO.
	 */
	public Collection<DebitoDTO> getColecaoDebitoDTO() {
		if (colecaoDebitoDTO == null) {
			colecaoDebitoDTO = new ArrayList<>();
		}
		return colecaoDebitoDTO;
	}

	/**
	 * @param colecaoDebitoDTO Atribui colecaoDebitoDTO.
	 */
	public void setColecaoDebitoDTO(Collection<DebitoDTO> colecaoDebitoDTO) {
		this.colecaoDebitoDTO = colecaoDebitoDTO;
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getId()
	 */
	public Long getId() {
		return getCda().getId();
	}

	/**
	 * @param id
	 * @see br.jus.pje.nucleo.entidades.Cda#setId(java.lang.Long)
	 */
	public void setId(Long id) {
		getCda().setId(id);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getProcessoTrf()
	 */
	public ProcessoTrf getProcessoTrf() {
		return getCda().getProcessoTrf();
	}

	/**
	 * @param processoTrf
	 * @see br.jus.pje.nucleo.entidades.Cda#setProcessoTrf(br.jus.pje.nucleo.entidades.ProcessoTrf)
	 */
	public void setProcessoTrf(ProcessoTrf processoTrf) {
		getCda().setProcessoTrf(processoTrf);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getNumero()
	 */
	public String getNumero() {
		return getCda().getNumero();
	}

	/**
	 * @param numero
	 * @see br.jus.pje.nucleo.entidades.Cda#setNumero(java.lang.String)
	 */
	public void setNumero(String numero) {
		getCda().setNumero(numero);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getNumeroProcessoAdm()
	 */
	public String getNumeroProcessoAdm() {
		return getCda().getNumeroProcessoAdm();
	}

	/**
	 * @param numeroProcessoAdm
	 * @see br.jus.pje.nucleo.entidades.Cda#setNumeroProcessoAdm(java.lang.String)
	 */
	public void setNumeroProcessoAdm(String numeroProcessoAdm) {
		getCda().setNumeroProcessoAdm(numeroProcessoAdm);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getNumeroControle()
	 */
	public String getNumeroControle() {
		return getCda().getNumeroControle();
	}

	/**
	 * @param numeroControle
	 * @see br.jus.pje.nucleo.entidades.Cda#setNumeroControle(java.lang.String)
	 */
	public void setNumeroControle(String numeroControle) {
		getCda().setNumeroControle(numeroControle);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getDataApuracao()
	 */
	public Date getDataApuracao() {
		return getCda().getDataApuracao();
	}

	/**
	 * @param dataApuracao
	 * @see br.jus.pje.nucleo.entidades.Cda#setDataApuracao(java.util.Date)
	 */
	public void setDataApuracao(Date dataApuracao) {
		getCda().setDataApuracao(dataApuracao);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getDataPrescricao()
	 */
	public Date getDataPrescricao() {
		return getCda().getDataPrescricao();
	}

	/**
	 * @param dataPrescricao
	 * @see br.jus.pje.nucleo.entidades.Cda#setDataPrescricao(java.util.Date)
	 */
	public void setDataPrescricao(Date dataPrescricao) {
		getCda().setDataPrescricao(dataPrescricao);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getInCreditoTributario()
	 */
	public Boolean getInCreditoTributario() {
		return getCda().getInCreditoTributario();
	}

	/**
	 * @param inCreditoTributario
	 * @see br.jus.pje.nucleo.entidades.Cda#setInCreditoTributario(java.lang.Boolean)
	 */
	public void setInCreditoTributario(Boolean inCreditoTributario) {
		getCda().setInCreditoTributario(inCreditoTributario);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getValor()
	 */
	public BigDecimal getValor() {
		return getCda().getValor();
	}

	/**
	 * @param valor
	 * @see br.jus.pje.nucleo.entidades.Cda#setValor(java.math.BigDecimal)
	 */
	public void setValor(BigDecimal valor) {
		getCda().setValor(valor);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getMoedaValor()
	 */
	public String getMoedaValor() {
		return getCda().getMoedaValor();
	}

	/**
	 * @param moedaValor
	 * @see br.jus.pje.nucleo.entidades.Cda#setMoedaValor(java.lang.String)
	 */
	public void setMoedaValor(String moedaValor) {
		getCda().setMoedaValor(moedaValor);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getAtivo()
	 */
	public Boolean getAtivo() {
		return getCda().getAtivo();
	}

	/**
	 * @param ativo
	 * @see br.jus.pje.nucleo.entidades.Cda#setAtivo(java.lang.Boolean)
	 */
	public void setAtivo(Boolean ativo) {
		getCda().setAtivo(ativo);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getTipoValorCda()
	 */
	public EnumTipoValorCda getTipoValorCda() {
		return getCda().getTipoValorCda();
	}

	/**
	 * @param tipoValorCda
	 * @see br.jus.pje.nucleo.entidades.Cda#setTipoValorCda(br.jus.pje.nucleo.enums.EnumTipoValorCda)
	 */
	public void setTipoValorCda(EnumTipoValorCda tipoValorCda) {
		getCda().setTipoValorCda(tipoValorCda);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getColecaoDebito()
	 */
	public List<Debito> getColecaoDebito() {
		return getCda().getColecaoDebito();
	}

	/**
	 * @param colecaoDebito
	 * @see br.jus.pje.nucleo.entidades.Cda#setColecaoDebito(java.util.List)
	 */
	public void setColecaoDebito(List<Debito> colecaoDebito) {
		getCda().setColecaoDebito(colecaoDebito);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Cda#getColecaoDevedorCda()
	 */
	public List<DevedorCda> getColecaoDevedorCda() {
		return getCda().getColecaoDevedorCda();
	}

	/**
	 * @param colecaoDevedorCda
	 * @see br.jus.pje.nucleo.entidades.Cda#setColecaoDevedorCda(java.util.List)
	 */
	public void setColecaoDevedorCda(List<DevedorCda> colecaoDevedorCda) {
		getCda().setColecaoDevedorCda(colecaoDevedorCda);
	}

	/**
	 * @return colecaoDevedorCdaDTO.
	 */
	public Collection<DevedorCdaDTO> getColecaoDevedorCdaDTO() {
		if (colecaoDevedorCdaDTO == null) {
			colecaoDevedorCdaDTO = new ArrayList<>();
		}
		return colecaoDevedorCdaDTO;
	}

	/**
	 * @param colecaoDevedorCdaDTO Atribui colecaoDevedorCdaDTO.
	 */
	public void setColecaoDevedorCdaDTO(Collection<DevedorCdaDTO> colecaoDevedorCdaDTO) {
		this.colecaoDevedorCdaDTO = colecaoDevedorCdaDTO;
	}

}
