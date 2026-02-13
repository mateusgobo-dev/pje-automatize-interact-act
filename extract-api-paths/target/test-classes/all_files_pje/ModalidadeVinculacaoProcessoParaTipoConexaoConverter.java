/**
 * ModalidadeVinculacaoProcessoParaTipoConexaoConverter.java
 * 
 * Data de criação: 17/03/2015
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeVinculacaoProcesso;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;

/**
 * Conversor de ModalidadeVinculacaoProcesso para TipoConexaoEnum.
 * 
 * @author Adriano Pamplona
 */
@AutoCreate
@Name (ModalidadeVinculacaoProcessoParaTipoConexaoConverter.NAME)
public class ModalidadeVinculacaoProcessoParaTipoConexaoConverter
		extends
		IntercomunicacaoConverterAbstrato<ModalidadeVinculacaoProcesso, TipoConexaoEnum> {

	public static final String NAME = "v222.modalidadeVinculacaoProcessoParaTipoConexaoConverter";
	
	@Override
	public TipoConexaoEnum converter(ModalidadeVinculacaoProcesso vinculacao) {
		TipoConexaoEnum resultado = null;
		
		if (isNotNull(vinculacao)) {
			
			Map<ModalidadeVinculacaoProcesso, TipoConexaoEnum> mapa = new HashMap<ModalidadeVinculacaoProcesso, TipoConexaoEnum>();
			mapa.put(ModalidadeVinculacaoProcesso.OR, TipoConexaoEnum.AS);
			mapa.put(ModalidadeVinculacaoProcesso.AR, TipoConexaoEnum.AS);
			mapa.put(ModalidadeVinculacaoProcesso.CD, TipoConexaoEnum.AS);
			mapa.put(ModalidadeVinculacaoProcesso.RR, TipoConexaoEnum.AS);
			mapa.put(ModalidadeVinculacaoProcesso.RG, TipoConexaoEnum.AS);

			mapa.put(ModalidadeVinculacaoProcesso.CT, TipoConexaoEnum.PR);
			mapa.put(ModalidadeVinculacaoProcesso.CX, TipoConexaoEnum.PR);
			
			mapa.put(ModalidadeVinculacaoProcesso.DP, TipoConexaoEnum.DP);
			
			resultado = mapa.get(vinculacao);
		}
		return resultado;
	}
}
