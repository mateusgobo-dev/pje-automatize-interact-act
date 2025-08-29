/*
 * RespostaManifestacaoProcessualConverter.java
 *
 * Data: 05/08/2020
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaManifestacaoProcessual;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.v222.util.ConversorUtil;

/**
 * Conversor de RespostaManifestacaoProcessual para
 * ManifestacaoProcessualRespostaDTO.
 * 
 * @author Adriano Pamplona
 */
@Name(RespostaManifestacaoProcessualConverter.NAME)

public class RespostaManifestacaoProcessualConverter
		extends IntercomunicacaoConverterAbstrato<RespostaManifestacaoProcessual, ManifestacaoProcessualRespostaDTO> {

	public static final String NAME = "v222.respostaManifestacaoProcessualConverter";

	/**
	 * @return RespostaManifestacaoProcessualConverter
	 */
	public static RespostaManifestacaoProcessualConverter instance() {
		return ComponentUtil.getComponent(RespostaManifestacaoProcessualConverter.class);
	}

	@Override
	public ManifestacaoProcessualRespostaDTO converter(RespostaManifestacaoProcessual objeto) {

		ManifestacaoProcessualRespostaDTO resultado = new ManifestacaoProcessualRespostaDTO();
		resultado.setSucesso(objeto.isSucesso());
		resultado.setMensagem(objeto.getMensagem());
		resultado.setDataOperacao(ConversorUtil.converterParaDate(objeto.getDataOperacao()));
		resultado.setNumeroProcesso(objeto.getProtocoloRecebimento());
		resultado.setRecibo(ProjetoUtil.converterParaBytes(objeto.getRecibo()));
		resultado.setParametro(ConversorUtil.converterParaProperties(objeto.getParametro()));

		return resultado;
	}
}
