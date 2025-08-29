/**
 * TipoConexaoParaModalidadeVinculacaoProcessoConverter.java
 * 
 * Data de criação: 17/03/2015
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeVinculacaoProcesso;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;

/**
 * Conversor de TipoConexaoEnum para ModalidadeVinculacaoProcesso.
 * 
 * @author Adriano Pamplona
 */
@Name (TipoConexaoParaModalidadeVinculacaoProcessoConverter.NAME)
public class TipoConexaoParaModalidadeVinculacaoProcessoConverter
		extends
		IntercomunicacaoConverterAbstrato<TipoConexaoEnum, ModalidadeVinculacaoProcesso> {

	public static final String NAME = "v222.tipoConexaoParaModalidadeVinculacaoProcessoConverter";
	
	@Override
	public ModalidadeVinculacaoProcesso converter(TipoConexaoEnum tipoConexao) {
		ModalidadeVinculacaoProcesso resultado = null;
		
		if (isNotNull(tipoConexao)) {
			
			Map<TipoConexaoEnum ,ModalidadeVinculacaoProcesso> mapa = new HashMap<TipoConexaoEnum, ModalidadeVinculacaoProcesso>();
			//FIXME (alexander): verificar o mapeamento entre TipoConexaoEnum X ModalidadeVinculacaoProcesso.
			mapa.put(TipoConexaoEnum.AS, ModalidadeVinculacaoProcesso.OR);
//			mapa.put(TipoConexaoEnum.AS, ModalidadeVinculacaoProcesso.AR);
//			mapa.put(TipoConexaoEnum.AS, ModalidadeVinculacaoProcesso.CD);
//			mapa.put(TipoConexaoEnum.AS, ModalidadeVinculacaoProcesso.RR);
//			mapa.put(TipoConexaoEnum.AS, ModalidadeVinculacaoProcesso.RG);

//			mapa.put(TipoConexaoEnum.PR, ModalidadeVinculacaoProcesso.CT);
			mapa.put(TipoConexaoEnum.PR, ModalidadeVinculacaoProcesso.CX);
			
			mapa.put(TipoConexaoEnum.DP, ModalidadeVinculacaoProcesso.DP);
		    
			mapa.put(TipoConexaoEnum.DM, ModalidadeVinculacaoProcesso.OR);
			
			resultado = mapa.get(tipoConexao);
		}
		return resultado;
	}
}
