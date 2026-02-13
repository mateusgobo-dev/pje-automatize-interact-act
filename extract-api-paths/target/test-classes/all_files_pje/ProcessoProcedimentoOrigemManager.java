package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ProcessoProcedimentoOrigemDAO;
import br.jus.pje.nucleo.entidades.ProcessoProcedimentoOrigem;

@Name(ProcessoProcedimentoOrigemManager.NAME)
public class ProcessoProcedimentoOrigemManager extends BaseManager<ProcessoProcedimentoOrigem> {

	@In
	private ProcessoProcedimentoOrigemDAO processoProcedimentoOrigemDAO; 
	
	public static final String NAME = "processoProcedimentoOrigemManager";

	@Override
	protected BaseDAO<ProcessoProcedimentoOrigem> getDAO() {
		return this.processoProcedimentoOrigemDAO;
	}
	
	public Integer countProcessosCriminaisLegadosPendentes() {
		return this.processoProcedimentoOrigemDAO.countProcessosCriminaisLegadosPendentes();
	}
	
	public Integer countProcedimentosCriminaisLegadosPendentes() {
		return this.processoProcedimentoOrigemDAO.countProcedimentosOrigemLegadosPendentes();
	}

	public List<Integer> recuperarIdsProcessosCriminaisLegadosPendentes(){
		return this.processoProcedimentoOrigemDAO.recuperarIdsProcessosCriminaisLegadosPendentes();
	}
	
	public List<ProcessoProcedimentoOrigem> recuperarPorIdProcessTrf(Integer idProcessoTrf){
		return this.processoProcedimentoOrigemDAO.recuperarPorIdProcessTrf(idProcessoTrf);
	}
}
