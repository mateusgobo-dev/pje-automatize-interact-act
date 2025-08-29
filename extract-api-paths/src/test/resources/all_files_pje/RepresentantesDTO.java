package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;

/**
 * Classe que representa uma coleção de representante usado pelo Domicílio Eletrônico.
 * 
 */
public class RepresentantesDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String numeroProcesso;
	private List<RepresentanteDTO> representantes = new ArrayList<>();

	/**
	 * Construtor.
	 * 
	 */
	public RepresentantesDTO() {
		// Construtor.
	}
	/**
	 * Construtor.
	 * 
	 * @param representantes
	 */
	public RepresentantesDTO(List<ProcessoParteRepresentante> representantes) {
		if (ProjetoUtil.isNotVazio(representantes)) {
			for (ProcessoParteRepresentante representante : representantes) {
				getRepresentantes().add(new RepresentanteDTO(representante));
			}
		}
	}
	/**
	 * @return the numeroProcesso
	 */
	public String getNumeroProcesso() {
		return numeroProcesso;
	}
	/**
	 * @param numeroProcesso the numeroProcesso to set
	 */
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}
	/**
	 * @return the representantes
	 */
	public List<RepresentanteDTO> getRepresentantes() {
		return representantes;
	}

	/**
	 * @param representantes the representantes to set
	 */
	public void setRepresentantes(List<RepresentanteDTO> representantes) {
		this.representantes = representantes;
	}
}
