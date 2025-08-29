/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.manager;


import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ProcessoRascunhoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.entidades.ProcessoRascunho;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


@Name(ProcessoRascunhoManager.NAME)
public class ProcessoRascunhoManager extends BaseManager<ProcessoRascunho> {
	
	public static final String NAME = "processoRascunhoManager";
	
	@In
	private ProcessoRascunhoDAO processoRascunhoDAO;
	
	@Override
	protected BaseDAO<ProcessoRascunho> getDAO() {
		return processoRascunhoDAO;
	}
	
	public ProcessoRascunho recuperarRascunhoPeloProcesso(ProcessoTrf processoTrf){
		return processoRascunhoDAO.findByProcessoTrf(processoTrf);
	}
	
	public ProcessoRascunho recuperarRascunhoPorIdProcessoTrf(Integer idProcessoTrf){
		return processoRascunhoDAO.findByProcessoTrf(idProcessoTrf);
	}	
	
	public ProcessoCriminalDTO recuperarRascunhoProcessoCriminal(ProcessoTrf processoTrf){
		ProcessoRascunho processoRascunho = processoRascunhoDAO.findByProcessoTrf(processoTrf);
		
		return (processoRascunho != null ? processoRascunho.getJsonProcessoCriminal() : null);
	}
	
	public ProcessoRascunho recuperarOuCriarProcessoRascunho(Integer idProcessoJudicial){
		
		ProcessoRascunho procRascunho = this.recuperarRascunhoPorIdProcessoTrf(idProcessoJudicial);
		if(procRascunho == null){
			ProcessoTrf proc = new ProcessoTrf();
			proc.setIdProcessoTrf(idProcessoJudicial);
			
			procRascunho = new ProcessoRascunho();
			procRascunho.setProcesso(proc);
			
			try {
				procRascunho = this.persist(procRascunho);
			} catch (PJeBusinessException e) {
				logger.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		
		return procRascunho;
	}
	
}
