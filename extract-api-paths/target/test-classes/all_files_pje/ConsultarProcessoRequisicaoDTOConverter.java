/*
 * ConsultarProcessoRequisicaoDTOConverter.java
 *
 * Data: 05/08/2020
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaProcesso;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.v222.util.ConversorUtil;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;

/**
 * Conversor de ConsultarProcessoRequisicaoDTO para
 * RequisicaoConsultaProcesso.
 * 
 * @author Adriano Pamplona
 */
@Name (ConsultarProcessoRequisicaoDTOConverter.NAME)
public class ConsultarProcessoRequisicaoDTOConverter extends IntercomunicacaoConverterAbstrato<ConsultarProcessoRequisicaoDTO, RequisicaoConsultaProcesso> {

	public static final String NAME = "v222.consultarProcessoRequisicaoDTOConverter";
	
	public static ConsultarProcessoRequisicaoDTOConverter instance() {
		return ComponentUtil.getComponent(ConsultarProcessoRequisicaoDTOConverter.class);
	}
	
	/**
	 * Use o método converter(ConsultarProcessoRequisicaoDTO, EnderecoWsdl) para atribuir login e senha. 
	 */
	@Override
	public RequisicaoConsultaProcesso converter(ConsultarProcessoRequisicaoDTO objeto) {
		return converter(objeto, null);
	}
	
	/**
	 * Converter.
	 * 
	 * @param objeto
	 * @param wsdl
	 */
	public RequisicaoConsultaProcesso converter(ConsultarProcessoRequisicaoDTO objeto, EnderecoWsdl wsdl) {
		
		String login = (wsdl != null ? wsdl.getLogin() : null);
		String senha = (wsdl != null ? wsdl.getSenha() : null);
		
		RequisicaoConsultaProcesso resultado = new RequisicaoConsultaProcesso();
		resultado.setIdConsultante(login);
		resultado.setSenhaConsultante(senha);
		resultado.setDataReferencia(ConversorUtil.converterParaDataHora(objeto.getDataReferencia()));
		resultado.getDocumento().addAll(objeto.getDocumento());
		resultado.setIncluirCabecalho(objeto.getIncluirCabecalho());
		resultado.setIncluirDocumentos(objeto.getIncluirDocumentos());
		resultado.setMovimentos(objeto.getMovimentos());
		resultado.setNumeroProcesso(ConversorUtil.converterParaNumeroUnico(objeto.getNumeroProcesso()));
		
		return resultado;
	}
}
