package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.util.NumeracaoUnicaUtil;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe que representa fase do processo usado pelo Domicílio Eletrônico.
 * 
 */
public class FaseAtualDTO implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	private ProcessoDTO faseAtual;
	private String numeroProcessoAntigo;
	
	/**
	 * Construtor.
	 * 
	 * @param processo
	 */
	public FaseAtualDTO() {
		// Construtor
	}
	
	/**
	 * Construtor.
	 * 
	 * @param processo
	 */
	public FaseAtualDTO(ProcessoTrf processo) {
		setFaseAtual(new ProcessoDTO(processo));
		setNumeroProcessoAntigo(obterNumeroProcessoAntigo(processo));
	}

	/**
	 * @return the faseAtual
	 */
	public ProcessoDTO getFaseAtual() {
		return faseAtual;
	}

	/**
	 * @param faseAtual the faseAtual to set
	 */
	public void setFaseAtual(ProcessoDTO faseAtual) {
		this.faseAtual = faseAtual;
	}

	/**
	 * @return the numeroProcessoAntigo
	 */
	public String getNumeroProcessoAntigo() {
		return numeroProcessoAntigo;
	}

	/**
	 * @param numeroProcessoAntigo the numeroProcessoAntigo to set
	 */
	public void setNumeroProcessoAntigo(String numeroProcessoAntigo) {
		this.numeroProcessoAntigo = numeroProcessoAntigo;
	}
	
	/**
	 * Processo de referência.
	 * 
	 * @param processo
	 * @return
	 */
	protected String obterNumeroProcessoAntigo(ProcessoTrf processo) {
		String resultado = null;
		if (processo != null) {
			resultado = (processo.getProcessoReferencia() != null ? 
					processo.getProcessoReferencia().getNumeroProcesso() : 
					processo.getDesProcReferencia());
		}
		return NumeracaoUnicaUtil.formatNumeroProcesso(StringUtil.removeNaoNumericos(resultado));
	}
}
