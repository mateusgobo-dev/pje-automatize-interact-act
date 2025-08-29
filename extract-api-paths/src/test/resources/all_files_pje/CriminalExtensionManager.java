/**
 * CriminalExtensionManager.java.
 *
 * Data: 10 de jan de 2019
 */
package br.jus.cnj.pje.intercomunicacao.v223.extensionmanager;

import java.util.List;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v223.beans.DocumentoProcessual;
import br.jus.cnj.intercomunicacao.v223.criminal.Processo;
import br.jus.cnj.pje.intercomunicacao.v223.converter.ProcessoCriminalMNIParaColecaoInformacaoCriminalRascunhoConverter;
import br.jus.cnj.pje.intercomunicacao.v223.converter.ProcessoCriminalMNIParaProcessoCriminalDTOConverter;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.manager.InformacaoCriminalRascunhoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoRascunhoManager;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRascunho;
import br.jus.pje.nucleo.entidades.ProcessoRascunho;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Manager para tratar a extensão do criminal.
 * 
 * @author Adriano Pamplona
 */
public class CriminalExtensionManager implements ExtensionManager<Processo> {
	@Override
	public void execute(ProcessoTrf processoTrf, Processo processoCriminal) {
		if (processoTrf != null && processoCriminal != null) {
			try {
				ProcessoRascunho processoRascunho = salvarRascunhoProcessoCriminal(processoTrf, processoCriminal);
				salvarRascunhoInformacaoCriminal(processoTrf, processoCriminal, processoRascunho);
			} catch (PJeBusinessException e) {
				throw new PJeRuntimeException(e);
			}
		}
	}
	
	private void salvarRascunhoInformacaoCriminal(ProcessoTrf processoTrf, Processo processoCriminal, ProcessoRascunho processoRascunho) throws PJeBusinessException {
		InformacaoCriminalRascunhoManager manager = ComponentUtil.getComponent(InformacaoCriminalRascunhoManager.class);
		
		ProcessoCriminalDTO processoCriminalDTO = converterParaProcessoCriminalDTO(processoCriminal, processoTrf);
		
		ProcessoCriminalMNIParaColecaoInformacaoCriminalRascunhoConverter converter = 
				ComponentUtil.getComponent(ProcessoCriminalMNIParaColecaoInformacaoCriminalRascunhoConverter.class);
		List<InformacaoCriminalRascunho> informacoes = converter.converter(processoCriminalDTO, processoCriminal, processoTrf, processoRascunho);
		for (InformacaoCriminalRascunho informacao : informacoes) {
			manager.persist(informacao);
		}
	}

	private ProcessoRascunho salvarRascunhoProcessoCriminal(ProcessoTrf processoTrf, Processo processoCriminal) throws PJeBusinessException {
		ProcessoCriminalDTO processoCriminalDTO = converterParaProcessoCriminalDTO(processoCriminal, processoTrf);
		ProcessoRascunho rascunho = new ProcessoRascunho();
		rascunho.setProcesso(processoTrf);
		rascunho.setJsonProcessoCriminal(processoCriminalDTO);
		
		ProcessoRascunhoManager manager = ComponentUtil.getComponent(ProcessoRascunhoManager.class);
		return manager.persist(rascunho);
	}

	/**
	 * Converte Processo criminal para ProcessoCriminalDTO.
	 * 
	 * @param processo
	 * @param processoTrf
	 * @return ProcessoCriminalDTO
	 */
	private ProcessoCriminalDTO converterParaProcessoCriminalDTO(Processo processo, ProcessoTrf processoTrf) {
		return new ProcessoCriminalMNIParaProcessoCriminalDTOConverter().converter(processo, processoTrf);
	}
	
	public static boolean possuiExtensaoCriminal(DocumentoProcessual documento) {
		if (documento == null) {
			return false;
		}
		Object any = documento.getAny();
		if (any == null) {
			return false;
		}		if (any instanceof Processo) {
			return true;
		}
		return false;
	}
}
