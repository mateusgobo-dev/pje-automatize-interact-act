package br.jus.cnj.pje.intercomunicacao.v222.servico;

import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public interface ManifestacaoProcessualHandler {
	/**
	 * Método que é disparado antes da entrega da manifestação processual
	 * @param manifestacaoProcessual Dados da manifestação processual
	 */
	void onBeforeEntregarManifestacaoProcessual(ManifestacaoProcessual manifestacaoProcessual);

	/**
	 * Método que é disparado após a entrega da manifestação processual
	 * @param manifestacaoProcessual Dados da manifestação processual
	 * @param processoTrf Dados do processo
	 * @param documentoPrincipal Dados do documento principal entregue
	 */
	void onAfterEntregarManifestacaoProcessual(ManifestacaoProcessual manifestacaoProcessual, ProcessoTrf processoTrf, ProcessoDocumento documentoPrincipal);
	
}
