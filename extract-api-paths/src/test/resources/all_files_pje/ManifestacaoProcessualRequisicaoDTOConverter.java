/*
 * ManifestacaoProcessualRequisicaoDTOConverter.java
 *
 * Data: 05/08/2020
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.ArrayList;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;
import br.jus.cnj.pje.intercomunicacao.v222.util.ConversorUtil;
import br.jus.cnj.pje.intercomunicacao.v222.util.IntercomunicacaoUtil;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;

/**
 * Conversor de RespostaManifestacaoProcessual para
 * ManifestacaoProcessualRespostaDTO.
 * 
 * @author Adriano Pamplona
 */
@Name (ManifestacaoProcessualRequisicaoDTOConverter.NAME)
public class ManifestacaoProcessualRequisicaoDTOConverter extends IntercomunicacaoConverterAbstrato<ManifestacaoProcessualRequisicaoDTO, ManifestacaoProcessual> {

	public static final String NAME = "v222.manifestacaoProcessualRequisicaoDTOConverter";
	
	public static ManifestacaoProcessualRequisicaoDTOConverter instance() {
		return ComponentUtil.getComponent(ManifestacaoProcessualRequisicaoDTOConverter.class);
	}
	
	/**
	 * Use o método converter(ManifestacaoProcessualRequisicaoDTO, EnderecoWsdl) para atribuir login e senha. 
	 */
	@Override
	public ManifestacaoProcessual converter(ManifestacaoProcessualRequisicaoDTO objeto) {
		return converter(objeto, null);
	}

	/**
	 * Converter.
	 * 
	 * @param objeto
	 * @param wsdl
	 */
	public ManifestacaoProcessual converter(ManifestacaoProcessualRequisicaoDTO objeto, EnderecoWsdl wsdl) throws IntercomunicacaoException {
		ManifestacaoProcessual resultado = null;
		
		try {
			String login = (wsdl != null ? wsdl.getLogin() : null);
			String senha = (wsdl != null ? wsdl.getSenha() : null);
			
			resultado = IntercomunicacaoUtil.montarManifestacaoProcessual(
					login, 
					senha, 
					objeto, 
					new ArrayList<>());
			resultado.setNumeroProcesso(ConversorUtil.converterParaNumeroUnico(objeto.getNumeroProcesso()));
		} catch (Exception e) {
			throw new IntercomunicacaoException(e);
		} 
		return resultado;
	}

	
}
