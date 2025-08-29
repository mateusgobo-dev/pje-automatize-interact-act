/*
 * DebitoDTO.java
 *
 * Data: 20/05/2021
 */
package br.jus.pje.nucleo.dto;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.DevedorCda;
import br.jus.pje.nucleo.entidades.DevedorDocIdentificacao;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;

/**
 * Classe decorator da entidade DevedorDocIdentificacao.
 * 
 * @author Adriano Pamplona
 */
public class DevedorDocIdentificacaoDTO implements Serializable {
	private DevedorCdaDTO devedorCdaDTO;
	private DevedorDocIdentificacao devedorDocIdentificacao;
	private TipoDocumentoIdentificacao tipoDocumentoIdentificacao;

	/**
	 * Construtor.
	 *
	 * @param devedorDocIdentificacao
	 */
	public DevedorDocIdentificacaoDTO(DevedorDocIdentificacao devedorDocIdentificacao, DevedorCdaDTO devedorCdaDTO) {
		setDevedorDocIdentificacao(devedorDocIdentificacao);
		setDevedorCdaDTO(devedorCdaDTO);
	}
	
	/**
	 * @return devedorDocIdentificacao.
	 */
	public DevedorDocIdentificacao getDevedorDocIdentificacao() {
		return devedorDocIdentificacao;
	}

	/**
	 * @param devedorDocIdentificacao Atribui devedorDocIdentificacao.
	 */
	public void setDevedorDocIdentificacao(DevedorDocIdentificacao devedorDocIdentificacao) {
		this.devedorDocIdentificacao = devedorDocIdentificacao;
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.DevedorDocIdentificacao#getId()
	 */
	public Long getId() {
		return getDevedorDocIdentificacao().getId();
	}

	/**
	 * @param id
	 * @see br.jus.pje.nucleo.entidades.DevedorDocIdentificacao#setId(java.lang.Long)
	 */
	public void setId(Long id) {
		getDevedorDocIdentificacao().setId(id);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.DevedorDocIdentificacao#getDevedorCda()
	 */
	public DevedorCda getDevedorCda() {
		return getDevedorDocIdentificacao().getDevedorCda();
	}

	/**
	 * @param devedorCda
	 * @see br.jus.pje.nucleo.entidades.DevedorDocIdentificacao#setDevedorCda(br.jus.pje.nucleo.entidades.DevedorCda)
	 */
	public void setDevedorCda(DevedorCda devedorCda) {
		getDevedorDocIdentificacao().setDevedorCda(devedorCda);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.DevedorDocIdentificacao#getNomeDevedor()
	 */
	public String getNomeDevedor() {
		return getDevedorDocIdentificacao().getNomeDevedor();
	}

	/**
	 * @param nomeDevedor
	 * @see br.jus.pje.nucleo.entidades.DevedorDocIdentificacao#setNomeDevedor(java.lang.String)
	 */
	public void setNomeDevedor(String nomeDevedor) {
		getDevedorDocIdentificacao().setNomeDevedor(nomeDevedor);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.DevedorDocIdentificacao#getCodigoTipo()
	 */
	public String getCodigoTipo() {
		return getDevedorDocIdentificacao().getCodigoTipo();
	}

	/**
	 * @param codigoTipo
	 * @see br.jus.pje.nucleo.entidades.DevedorDocIdentificacao#setCodigoTipo(java.lang.String)
	 */
	public void setCodigoTipo(String codigoTipo) {
		getDevedorDocIdentificacao().setCodigoTipo(codigoTipo);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.DevedorDocIdentificacao#getNumero()
	 */
	public String getNumero() {
		return getDevedorDocIdentificacao().getNumero();
	}

	/**
	 * @param numero
	 * @see br.jus.pje.nucleo.entidades.DevedorDocIdentificacao#setNumero(java.lang.String)
	 */
	public void setNumero(String numero) {
		getDevedorDocIdentificacao().setNumero(numero);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.DevedorDocIdentificacao#getDescricaoOrgaoExpedidor()
	 */
	public String getDescricaoOrgaoExpedidor() {
		return getDevedorDocIdentificacao().getDescricaoOrgaoExpedidor();
	}

	/**
	 * @param descricaoOrgaoExpedidor
	 * @see br.jus.pje.nucleo.entidades.DevedorDocIdentificacao#setDescricaoOrgaoExpedidor(java.lang.String)
	 */
	public void setDescricaoOrgaoExpedidor(String descricaoOrgaoExpedidor) {
		getDevedorDocIdentificacao().setDescricaoOrgaoExpedidor(descricaoOrgaoExpedidor);
	}

	/**
	 * @return tipoDocumentoIdentificacao.
	 */
	public TipoDocumentoIdentificacao getTipoDocumentoIdentificacao() {
		return tipoDocumentoIdentificacao;
	}

	/**
	 * @param tipoDocumentoIdentificacao Atribui tipoDocumentoIdentificacao.
	 */
	public void setTipoDocumentoIdentificacao(TipoDocumentoIdentificacao tipoDocumentoIdentificacao) {
		this.tipoDocumentoIdentificacao = tipoDocumentoIdentificacao;
		if (tipoDocumentoIdentificacao == null) {
			setCodigoTipo(null);
		} else {
			setCodigoTipo(tipoDocumentoIdentificacao.getCodTipo());
		}
	}

	/**
	 * @return devedorCdaDTO.
	 */
	public DevedorCdaDTO getDevedorCdaDTO() {
		return devedorCdaDTO;
	}

	/**
	 * @param devedorCdaDTO Atribui devedorCdaDTO.
	 */
	public void setDevedorCdaDTO(DevedorCdaDTO devedorCdaDTO) {
		this.devedorCdaDTO = devedorCdaDTO;
		if (devedorCdaDTO != null) {
			setDevedorCda(devedorCdaDTO.getDevedorCda());
		}
	}
	
	
}
