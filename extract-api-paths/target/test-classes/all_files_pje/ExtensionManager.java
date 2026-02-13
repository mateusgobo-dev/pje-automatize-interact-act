/**
 * ExtensionManager.java.
 *
 * Data: 10/01/2019
 */
package br.jus.cnj.pje.intercomunicacao.v223.extensionmanager;

import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Interface responsável pelo contrato da implementação dos manager's das extensões do MNI.
 * 
 * @author Adriano Pamplona
 */
public interface ExtensionManager<T> {

	public void execute(ProcessoTrf processo, T extensao);
}
