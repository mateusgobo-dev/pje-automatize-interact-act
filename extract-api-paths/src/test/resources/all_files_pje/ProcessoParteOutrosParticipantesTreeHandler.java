package br.com.infox.ibpm.component.tree;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name("processoParteOutrosParticipantesTree")
@BypassInterceptors
public class ProcessoParteOutrosParticipantesTreeHandler extends ProcessoParteTreeHandler {

	private static final long serialVersionUID = 1L;

	public ProcessoParteOutrosParticipantesTreeHandler() {
		setInParticipacao(ProcessoParteParticipacaoEnum.T);
		setNumRows(numRows());
	}
	
	private Long numRows(){
		Query query = getEntityManager().createQuery(getQueryCountRoots());
		return(Long) query.getSingleResult();
	}
}