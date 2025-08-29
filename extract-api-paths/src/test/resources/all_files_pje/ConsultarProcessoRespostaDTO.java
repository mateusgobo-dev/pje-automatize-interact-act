/*
 * ConsultarProcessoRespostaTO.java
 *
 * Data: 29/07/2020
 */
package br.jus.cnj.pje.intercomunicacao.dto;

import java.io.Serializable;
import java.util.List;

import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * @author Adriano Pamplona
 *
 */
public class ConsultarProcessoRespostaDTO implements Serializable {
	private Boolean sucesso;
	private String mensagem;
	private ProcessoTrf processoTrf;

	/**
	 * @return sucesso.
	 */
	public Boolean getSucesso() {
		return sucesso;
	}

	/**
	 * @param sucesso Atribui sucesso.
	 */
	public void setSucesso(Boolean sucesso) {
		this.sucesso = sucesso;
	}

	/**
	 * @return mensagem.
	 */
	public String getMensagem() {
		return mensagem;
	}

	/**
	 * @param mensagem Atribui mensagem.
	 */
	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	/**
	 * @return processoTrf.
	 */
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	/**
	 * @param processoTrf Atribui processoTrf.
	 */
	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	/**
	 * @return processo.
	 */
	public Processo getProcesso() {
		Processo resultado = null;

		if (getProcessoTrf() != null) {
			resultado = getProcessoTrf().getProcesso();
		}
		return resultado;
	}

	/**
	 * @return numeroProcesso.
	 */
	public String getNumeroProcesso() {
		String resultado = null;

		if (getProcesso() != null) {
			resultado = getProcesso().getNumeroProcesso();
		}
		return resultado;
	}

	public List<ProcessoDocumento> getProcessoDocumentoList() {
		return getProcesso().getProcessoDocumentoList();
	}

	private void inicializarProcessoTrf() {
		if (processoTrf == null) {
			setProcessoTrf(new ProcessoTrf());
		}
	}

	protected void inicializarProcesso() {
		inicializarProcessoTrf();

		if (getProcessoTrf().getProcesso() == null) {
			getProcessoTrf().setProcesso(new Processo());
		}
	}

}
