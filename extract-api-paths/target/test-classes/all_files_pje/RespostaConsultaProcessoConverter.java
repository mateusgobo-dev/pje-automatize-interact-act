/*
 * RespostaConsultaProcessoConverter.java
 *
 * Data: 05/08/2020
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaProcesso;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRespostaDTO;

/**
 * Conversor de RespostaConsultaProcesso para
 * ConsultarProcessoRespostaDTO.
 * 
 * @author Adriano Pamplona
 */
@Name (RespostaConsultaProcessoConverter.NAME)
public class RespostaConsultaProcessoConverter extends IntercomunicacaoConverterAbstrato<RespostaConsultaProcesso, ConsultarProcessoRespostaDTO> {

public static final String NAME = "v222.respostaConsultaProcessoConverter";
	
	public static RespostaConsultaProcessoConverter instance() {
		return ComponentUtil.getComponent(RespostaConsultaProcessoConverter.class);
	}
	
	public ConsultarProcessoRespostaDTO converter(RespostaConsultaProcesso objeto) {
		ProcessoJudicialParaProcessoTrfConverter converter = ProcessoJudicialParaProcessoTrfConverter.instance();
		
		ConsultarProcessoRespostaDTO resultado = new ConsultarProcessoRespostaDTO();
		resultado.setSucesso(objeto.isSucesso());
		resultado.setMensagem(objeto.getMensagem());
		resultado.setProcessoTrf(converter.converter(objeto.getProcesso()));
		
		return resultado;
	}
}
