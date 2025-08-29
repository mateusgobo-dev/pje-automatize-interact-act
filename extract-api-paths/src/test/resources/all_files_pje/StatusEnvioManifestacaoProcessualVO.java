/**
 * StatusEnvioManifestacaoProcessualVO.java
 * 
 * Data: 26/10/2015
 */
package br.jus.cnj.pje.entidades.vo;

import java.io.Serializable;
import java.util.Date;

import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRespostaDTO;

/**
 * Classe que representa o status de uma requisição de entrega de manifestação (remessa e retorno) 
 * para controle do processo.
 * 
 * @author Adriano Pamplona
 */
@SuppressWarnings("serial")
public class StatusEnvioManifestacaoProcessualVO implements Serializable {

	private Boolean sucesso;
	private String mensagem;
	private String protocoloRecebimento;
	
	private Date dataEnvio;

	/**
	 * @return true se a requisição foi finalizada com erro.
	 */
	public Boolean isRequisicaoFinalizadaComErro() {
		return 	getSucesso() != null && 
				getMensagem() != null &&
				(!getSucesso() || !getMensagem().equals("Manifestação processual recebida com sucesso"));
	}
	
	/**
	 * Atribui os atributos da resposta para uso posterior.
	 * 
	 * @param resposta
	 */
	public void setRespostaManifestacaoProcessual(ManifestacaoProcessualRespostaDTO resposta) {
		if (resposta != null) {
			setSucesso(resposta.getSucesso());
			setMensagem(resposta.getMensagem());
			setProtocoloRecebimento(resposta.getNumeroProcesso());
		}
	}
	/**
	 * @return nova RespostaManifestacaoProcessual
	 */
	public ManifestacaoProcessualRespostaDTO getRespostaManifestacaoProcessual() {
		ManifestacaoProcessualRespostaDTO resposta = new ManifestacaoProcessualRespostaDTO();
		resposta.setSucesso(getSucesso());
		resposta.setMensagem(getMensagem());
		resposta.setNumeroProcesso(getProtocoloRecebimento());
		return resposta;
	}
	
	/**
	 * @return the sucesso
	 */
	public Boolean getSucesso() {
		return sucesso;
	}

	/**
	 * @param sucesso
	 *            the sucesso to set
	 */
	public void setSucesso(Boolean sucesso) {
		this.sucesso = sucesso;
	}

	/**
	 * @return the mensagem
	 */
	public String getMensagem() {
		return mensagem;
	}

	/**
	 * @param mensagem
	 *            the mensagem to set
	 */
	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	/**
	 * @return the protocoloRecebimento
	 */
	public String getProtocoloRecebimento() {
		return protocoloRecebimento;
	}

	/**
	 * @param protocoloRecebimento
	 *            the protocoloRecebimento to set
	 */
	public void setProtocoloRecebimento(String protocoloRecebimento) {
		this.protocoloRecebimento = protocoloRecebimento;
	}

	/**
	 * @return the dataEnvio
	 */
	public Date getDataEnvio() {
		return dataEnvio;
	}

	/**
	 * @param dataEnvio
	 *            the dataEnvio to set
	 */
	public void setDataEnvio(Date dataEnvio) {
		this.dataEnvio = dataEnvio;
	}
}
