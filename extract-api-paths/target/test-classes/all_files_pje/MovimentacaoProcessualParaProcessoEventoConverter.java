/**
 * MovimentacaoProcessualParaProcessoEventoConverter.java
 * 
 * Data de criação: 12/08/2020
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.Iterator;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.Complemento;
import br.jus.cnj.intercomunicacao.v222.beans.MovimentacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.MovimentoLocal;
import br.jus.cnj.intercomunicacao.v222.beans.MovimentoNacional;
import br.jus.cnj.pje.intercomunicacao.v222.util.ConversorUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;

/**
 * Conversor de ProcessoEvento para MovimentacaoProcessual da intercomunicação.
 * 
 * @author Adriano Pamplona
 */
@AutoCreate
@Name (MovimentacaoProcessualParaProcessoEventoConverter.NAME)
public class MovimentacaoProcessualParaProcessoEventoConverter
		extends
		IntercomunicacaoConverterAbstrato<MovimentacaoProcessual, ProcessoEvento> {

	public static final String NAME = "V222.movimentacaoProcessualParaProcessoEventoConverter";
	
	/**
	 * @return Instância de MovimentacaoProcessualParaProcessoEventoConverter.
	 */
	public static MovimentacaoProcessualParaProcessoEventoConverter instance() {
		return ComponentUtil.getComponent(MovimentacaoProcessualParaProcessoEventoConverter.class);
	}
	
	@Override
	public ProcessoEvento converter(MovimentacaoProcessual objeto) {
		ProcessoEvento resultado = null;

		if (isNotNull(objeto)) {
			resultado = new ProcessoEvento();
			resultado.setEvento(new Evento());
			resultado.setIdProcessoEvento(converterParaInt(objeto.getIdentificadorMovimento()));
			resultado.setDataAtualizacao(ConversorUtil.converterParaDate(objeto.getDataHora()));
			
			if (objeto.getMovimentoLocal() != null) { // movimento local
				MovimentoLocal movimentoLocal = objeto.getMovimentoLocal();
				
				resultado.getEvento().setCodEvento(converterParaString(movimentoLocal.getCodigoMovimento()));
				if (movimentoLocal.getCodigoPaiNacional() != 0) {
					Evento superior = new Evento();
					superior.setCodEvento(converterParaString(movimentoLocal.getCodigoPaiNacional()));
					resultado.getEvento().setEventoSuperior(superior);
				}
				resultado.setTextoFinalExterno(movimentoLocal.getDescricao());
			} else { // movimento nacional
				MovimentoNacional movimentoNacional = objeto.getMovimentoNacional();
				
				resultado.getEvento().setCodEvento(converterParaString(movimentoNacional.getCodigoNacional()));
				if (ProjetoUtil.isNotVazio(movimentoNacional.getComplemento())) {
					Complemento complemento = movimentoNacional.getComplemento().get(0);
					resultado.setTextoFinalExterno(complemento.getValue());
				}
			}
			if (ProjetoUtil.isNotVazio(objeto.getIdDocumentoVinculado())) {
				String idDocumentoVinculado = objeto.getIdDocumentoVinculado().get(0);
				ProcessoDocumento documento = new ProcessoDocumento();
				documento.setIdProcessoDocumento(converterParaInt(idDocumentoVinculado));
				documento.setDocumentoSigiloso(objeto.getNivelSigilo() == 5);
				
				resultado.setProcessoDocumento(documento);
			}
		}

		return resultado;
	}

}
