/*
 * DebitoDTO.java
 *
 * Data: 20/05/2021
 */
package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import br.jus.pje.nucleo.entidades.Cda;
import br.jus.pje.nucleo.entidades.DevedorCda;
import br.jus.pje.nucleo.entidades.DevedorDocIdentificacao;
import br.jus.pje.nucleo.enums.EnumTipoDevedor;

/**
 * Classe decorator da entidade DevedorCda.
 * 
 * @author Adriano Pamplona
 */
public class DevedorCdaDTO implements Serializable {
	private DevedorCda devedorCda;
	private Collection<DevedorDocIdentificacaoDTO> colecaoDevedorDocIdentificacaoDTO = new ArrayList<>();
	private CdaDTO cdaDTO;
	
	/**
	 * Construtor.
	 *
	 * @param devedorCda
	 */
	public DevedorCdaDTO(DevedorCda devedorCda, CdaDTO cdaDTO) {
		setDevedorCda(devedorCda);
		setCdaDTO(cdaDTO);
	}
	
	/**
	 * @return devedorCda.
	 */
	public DevedorCda getDevedorCda() {
		return devedorCda;
	}

	/**
	 * @param devedorCda Atribui devedorCda.
	 */
	public void setDevedorCda(DevedorCda devedorCda) {
		this.devedorCda = devedorCda;
		
		getColecaoDevedorDocIdentificacaoDTO().clear();
		if (devedorCda != null) {
			List<DevedorDocIdentificacao> documentos = getColecaoDevedorDocIdentificacao();
			for (DevedorDocIdentificacao documento : documentos) {
				getColecaoDevedorDocIdentificacaoDTO().add(new DevedorDocIdentificacaoDTO(documento, this));
			}
		}
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.DevedorCda#getId()
	 */
	public Long getId() {
		return getDevedorCda().getId();
	}

	/**
	 * @param id
	 * @see br.jus.pje.nucleo.entidades.DevedorCda#setId(java.lang.Long)
	 */
	public void setId(Long id) {
		getDevedorCda().setId(id);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.DevedorCda#getCda()
	 */
	public Cda getCda() {
		return getDevedorCda().getCda();
	}

	/**
	 * @param cda
	 * @see br.jus.pje.nucleo.entidades.DevedorCda#setCda(br.jus.pje.nucleo.entidades.Cda)
	 */
	public void setCda(Cda cda) {
		getDevedorCda().setCda(cda);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.DevedorCda#getNome()
	 */
	public String getNome() {
		return getDevedorCda().getNome();
	}

	/**
	 * @param nome
	 * @see br.jus.pje.nucleo.entidades.DevedorCda#setNome(java.lang.String)
	 */
	public void setNome(String nome) {
		getDevedorCda().setNome(nome);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.DevedorCda#getTipoDevedor()
	 */
	public EnumTipoDevedor getTipoDevedor() {
		return getDevedorCda().getTipoDevedor();
	}

	/**
	 * @param tipoDevedor
	 * @see br.jus.pje.nucleo.entidades.DevedorCda#setTipoDevedor(br.jus.pje.nucleo.enums.EnumTipoDevedor)
	 */
	public void setTipoDevedor(EnumTipoDevedor tipoDevedor) {
		getDevedorCda().setTipoDevedor(tipoDevedor);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.DevedorCda#getColecaoDevedorDocIdentificacao()
	 */
	public List<DevedorDocIdentificacao> getColecaoDevedorDocIdentificacao() {
		return getDevedorCda().getColecaoDevedorDocIdentificacao();
	}

	/**
	 * @param colecaoDevedorDocIdentificacao
	 * @see br.jus.pje.nucleo.entidades.DevedorCda#setColecaoDevedorDocIdentificacao(java.util.List)
	 */
	public void setColecaoDevedorDocIdentificacao(List<DevedorDocIdentificacao> colecaoDevedorDocIdentificacao) {
		getDevedorCda().setColecaoDevedorDocIdentificacao(colecaoDevedorDocIdentificacao);
	}

	/**
	 * @return colecaoDevedorDocIdentificacaoDTO.
	 */
	public Collection<DevedorDocIdentificacaoDTO> getColecaoDevedorDocIdentificacaoDTO() {
		if (colecaoDevedorDocIdentificacaoDTO == null) {
			colecaoDevedorDocIdentificacaoDTO = new ArrayList<>();
		}
		return colecaoDevedorDocIdentificacaoDTO;
	}

	/**
	 * @param colecaoDevedorDocIdentificacaoDTO Atribui colecaoDevedorDocIdentificacaoDTO.
	 */
	public void setColecaoDevedorDocIdentificacaoDTO(
			Collection<DevedorDocIdentificacaoDTO> colecaoDevedorDocIdentificacaoDTO) {
		this.colecaoDevedorDocIdentificacaoDTO = colecaoDevedorDocIdentificacaoDTO;
	}

	/**
	 * @return cdaDTO.
	 */
	public CdaDTO getCdaDTO() {
		return cdaDTO;
	}

	/**
	 * @param cdaDTO Atribui cdaDTO.
	 */
	public void setCdaDTO(CdaDTO cdaDTO) {
		this.cdaDTO = cdaDTO;
		if (cdaDTO != null) {
			setCda(cdaDTO.getCda());
		}
	}
}
