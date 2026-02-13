/*
 * DebitoDTO.java
 *
 * Data: 20/05/2021
 */
package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.jus.pje.nucleo.entidades.Cda;
import br.jus.pje.nucleo.entidades.Debito;
import br.jus.pje.nucleo.entidades.TipoNaturezaDebito;

/**
 * Classe decorator da entidade Debito.
 * 
 * @author Adriano Pamplona
 */
public class DebitoDTO implements Serializable {
	private CdaDTO cdaDTO;
	private Debito debito;
	private TipoNaturezaDebito tipoNaturezaDebito;
	private IPTUNaturezaDebitoDTO iptuNaturezaDebitoDTO;
	
	/**
	 * Construtor.
	 *
	 * @param debito
	 * @param cdaDTO
	 */
	public DebitoDTO(Debito debito, CdaDTO cdaDTO) {
		setDebito(debito);
		setCdaDTO(cdaDTO);
	}
	
	/**
	 * @return getDebito().
	 */
	public Debito getDebito() {
		return debito;
	}

	/**
	 * @param debito Atribui getDebito().
	 */
	public void setDebito(Debito debito) {
		this.debito = debito;
		
		if (debito != null) {
			try {
				String json = debito.getDados();
				IPTUNaturezaDebitoDTO iptu = new ObjectMapper().readValue(json, IPTUNaturezaDebitoDTO.class);
				setIptuNaturezaDebitoDTO(iptu);
			} catch (Exception e) {
				new RuntimeException(e);
			}
		}
	}

	/**
	 * @return tipoNaturezaDebito.
	 */
	public TipoNaturezaDebito getTipoNaturezaDebito() {
		return tipoNaturezaDebito;
	}

	/**
	 * @param tipoNaturezaDebito Atribui tipoNaturezaDebito.
	 */
	public void setTipoNaturezaDebito(TipoNaturezaDebito tipoNaturezaDebito) {
		this.tipoNaturezaDebito = tipoNaturezaDebito;
		
		setCodigoNatureza(null);
		setDescricaoNatureza(null);
		if (tipoNaturezaDebito != null) {
			setCodigoNatureza(tipoNaturezaDebito.getCodigo());
			setDescricaoNatureza(tipoNaturezaDebito.getDescricao());
		}
	}
	
	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Debito#getId()
	 */
	public Long getId() {
		return getDebito().getId();
	}

	/**
	 * @param id
	 * @see br.jus.pje.nucleo.entidades.Debito#setId(java.lang.Long)
	 */
	public void setId(Long id) {
		getDebito().setId(id);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Debito#getCda()
	 */
	public Cda getCda() {
		return getDebito().getCda();
	}

	/**
	 * @param cda
	 * @see br.jus.pje.nucleo.entidades.Debito#setCda(br.jus.pje.nucleo.entidades.Cda)
	 */
	public void setCda(Cda cda) {
		getDebito().setCda(cda);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Debito#getDataExercicio()
	 */
	public Date getDataExercicio() {
		return getDebito().getDataExercicio();
	}

	/**
	 * @param dataExercicio
	 * @see br.jus.pje.nucleo.entidades.Debito#setDataExercicio(java.util.Date)
	 */
	public void setDataExercicio(Date dataExercicio) {
		getDebito().setDataExercicio(dataExercicio);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Debito#getCodigoNatureza()
	 */
	public String getCodigoNatureza() {
		return getDebito().getCodigoNatureza();
	}

	/**
	 * @param codigoNatureza
	 * @see br.jus.pje.nucleo.entidades.Debito#setCodigoNatureza(java.lang.String)
	 */
	public void setCodigoNatureza(String codigoNatureza) {
		getDebito().setCodigoNatureza(codigoNatureza);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Debito#getDescricaoNatureza()
	 */
	public String getDescricaoNatureza() {
		return getDebito().getDescricaoNatureza();
	}

	/**
	 * @param descricaoNatureza
	 * @see br.jus.pje.nucleo.entidades.Debito#setDescricaoNatureza(java.lang.String)
	 */
	public void setDescricaoNatureza(String descricaoNatureza) {
		getDebito().setDescricaoNatureza(descricaoNatureza);
	}

	/**
	 * @return
	 * @see br.jus.pje.nucleo.entidades.Debito#getDados()
	 */
	public String getDados() {
		return getDebito().getDados();
	}

	/**
	 * @param dados
	 * @see br.jus.pje.nucleo.entidades.Debito#setDados(java.lang.String)
	 */
	public void setDados(String dados) {
		getDebito().setDados(dados);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getDebito().toString();
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

	/**
	 * @return iptuNaturezaDebitoDTO.
	 */
	public IPTUNaturezaDebitoDTO getIptuNaturezaDebitoDTO() {
		if (iptuNaturezaDebitoDTO == null) {
			iptuNaturezaDebitoDTO = new IPTUNaturezaDebitoDTO();
		}
		return iptuNaturezaDebitoDTO;
	}

	/**
	 * @param iptuNaturezaDebitoDTO Atribui iptuNaturezaDebitoDTO.
	 */
	public void setIptuNaturezaDebitoDTO(IPTUNaturezaDebitoDTO iptuNaturezaDebitoDTO) {
		this.iptuNaturezaDebitoDTO = iptuNaturezaDebitoDTO;
		
		if (iptuNaturezaDebitoDTO == null) {
			setDados(null);
		}
	}
}
