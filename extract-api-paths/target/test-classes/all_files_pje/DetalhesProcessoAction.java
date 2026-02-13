/**
 * 
 */
package br.jus.cnj.pje.view;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * @author cristof
 * 
 */
@Name("detalhesProcessoAction")
@Scope(ScopeType.CONVERSATION)
public class DetalhesProcessoAction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8329340875904349313L;

	@In(create = true)
	private transient ProcessoJudicialManager processoJudicialManager;

	@In(create = true)
	private FacesMessages facesMessages;

	private ProcessoTrf processoJudicial;

	public void setProcessoJudicial(Processo processo) {
		try {
			this.processoJudicial = this.processoJudicialManager.findById(processo.getIdProcesso());
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Não foi possível localizar o processo judicial {0}.",
					processo.getNumeroProcesso());
			e.printStackTrace();
		} catch (PJeDAOException e) {
			facesMessages.add(Severity.ERROR,
					"Erro ao acessar dados. Não foi possível localizar o processo judicial {0}.",
					processo.getNumeroProcesso());
			e.printStackTrace();
		}
	}

	public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}

	public void setProcessoJudicial(ProcessoTrf processoJudicial) {
		this.processoJudicial = processoJudicial;
	}

}
