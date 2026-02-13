/**
 * ProcessoEventoParaMovimentacaoProcessualConverter.java
 * 
 * Data de criação: 25/11/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.Complemento;
import br.jus.cnj.intercomunicacao.v222.beans.MovimentacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.MovimentoLocal;
import br.jus.cnj.intercomunicacao.v222.beans.MovimentoNacional;
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
@Name (ProcessoEventoParaMovimentacaoProcessualConverter.NAME)
public class ProcessoEventoParaMovimentacaoProcessualConverter
		extends
		IntercomunicacaoConverterAbstrato<ProcessoEvento, MovimentacaoProcessual> {

	public static final String NAME = "v222.processoEventoParaMovimentacaoProcessualConverter";
	
	@Override
	public MovimentacaoProcessual converter(ProcessoEvento objeto) {
		MovimentacaoProcessual movimentacao = null;
		Evento evento = obterEvento(objeto);

		if (isNotNull(evento) && !evento.getSegredoJustica()) {
			movimentacao = new MovimentacaoProcessual();
			movimentacao.setIdentificadorMovimento(String.valueOf(objeto.getIdProcessoEvento()));
			movimentacao.setDataHora(converterParaDataHora(objeto.getDataAtualizacao()));
			
			if (isNull(evento.getCodEventoOutro())) {
				movimentacao.setMovimentoLocal(obterMovimentoLocal(objeto, evento));
			}else{
				movimentacao.setMovimentoNacional(obterMovimentoNacional(objeto, evento));
			}
			
			if (isNotNull(objeto.getProcessoDocumento())) {
				ProcessoDocumento processoDocumento = objeto.getProcessoDocumento();
				movimentacao.setNivelSigilo(obterNivelSigilo(objeto, processoDocumento));
				movimentacao.getIdDocumentoVinculado().add(
						String.valueOf(processoDocumento.getIdProcessoDocumento()));
			}
		}

		return movimentacao;
	}

	/**
	 * @param objeto
	 * @param processoDocumento
	 * @return nível de sigilo.
	 */
	private int obterNivelSigilo(ProcessoEvento objeto, ProcessoDocumento processoDocumento) {
		return (isNotNull(processoDocumento.getDocumentoSigiloso()) && objeto.getProcessoDocumento().getDocumentoSigiloso() ? 5 : 0);
	}

	/**
	 * @param objeto
	 * @return Evento
	 */
	private Evento obterEvento(ProcessoEvento objeto) {
		Evento evento = null;
		if (isNotNull(objeto.getEvento()) && isNotNull(objeto.getEvento().getIdEvento())) {
			int idEvento = objeto.getEvento().getIdEvento();
			try {
				evento = getEventoManager().findById(idEvento);
			} catch (PJeBusinessException e) {
				// não faz nada.
			}
		}
		
		return evento;
	}

	/**
	 * @param objeto
	 * @param evento
	 * @return MovimentacaoNacional
	 */
	private MovimentoNacional obterMovimentoNacional(ProcessoEvento objeto, Evento evento) {
		MovimentoNacional movimento = new MovimentoNacional();
		movimento.setCodigoNacional(Integer.parseInt(evento.getCodEvento().replaceAll("[A-Z]", "")));
		
		Complemento complemento = new Complemento();
		complemento.setValue(objeto.getTextoFinal());
		movimento.getComplemento().add(complemento);
		return movimento;
	}
	
	private MovimentoLocal obterMovimentoLocal(ProcessoEvento objeto, Evento evento) {
		MovimentoLocal movimento = new MovimentoLocal();
		movimento.setCodigoMovimento(Integer.parseInt(evento.getCodEvento().replaceAll("[A-Z]", "")));
		if (evento.getEventoSuperior() != null) {
			movimento.setCodigoPaiNacional(evento.getEventoSuperior().getId());
		}
		movimento.setDescricao(objeto.getTextoFinal());
		return movimento;
	}

	/**
	 * @return eventoManager
	 */
	private EventoManager getEventoManager() {
		return ComponentUtil.getComponent(EventoManager.class);
	}
}
