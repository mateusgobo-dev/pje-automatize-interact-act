package br.com.infox.ibpm.component.tree;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name("processoPartePoloAtivoTree")
public class ProcessoPartePoloAtivoTreeHandler extends AbstractProcessoParteTreeHandler<ProcessoParte> {

	private static final long serialVersionUID = 1L;
	
	public ProcessoPartePoloAtivoTreeHandler() {
		setInParticipacao(ProcessoParteParticipacaoEnum.A);
		setNumRows(numRows());
	}
	
	private Long numRows(){
		Query query = getEntityManager().createQuery(getQueryCountRoots());
		return(Long) query.getSingleResult();
	}
}