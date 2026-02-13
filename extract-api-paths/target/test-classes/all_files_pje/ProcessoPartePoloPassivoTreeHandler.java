package br.com.infox.ibpm.component.tree;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name("processoPartePoloPassivoTree")
@BypassInterceptors
public class ProcessoPartePoloPassivoTreeHandler extends AbstractProcessoParteTreeHandler<ProcessoParte> {

	private static final long serialVersionUID = 1L;

	public ProcessoPartePoloPassivoTreeHandler() {
		setInParticipacao(ProcessoParteParticipacaoEnum.P);
		setNumRows(numRows());
	}
	
	private Long numRows(){
		Query query = getEntityManager().createQuery(getQueryCountRoots());
		return(Long) query.getSingleResult();
	}
}