package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jbpm.graph.exe.ProcessInstance;

import br.jus.cnj.pje.business.dao.ConsultaProcessoTrfSemFiltroDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrfSemFiltro;

/**
 * [PJEII-20838] - Alterar a tela de pesquisa por processos para solicitar a habilitação nos autos. 
 * Classe criada para atender a Issue acima, visto que a implementação antiga não utilizava camadas e a consulta era feita via xml.
 */
@Name(ConsultaProcessoTrfSemFiltroManager.NAME)
public class ConsultaProcessoTrfSemFiltroManager extends BaseManager<ConsultaProcessoTrfSemFiltro>{

	public static final String NAME = "consultaProcessoTrfSemFiltroManager";

	@In
	private ConsultaProcessoTrfSemFiltroDAO consultaProcessoTrfSemFiltroDAO;
	
	@Override
	protected ConsultaProcessoTrfSemFiltroDAO getDAO(){
		return this.consultaProcessoTrfSemFiltroDAO;
	}

	public ConsultaProcessoTrfSemFiltro findByProcessInstance(ProcessInstance processInstance) throws PJeBusinessException{
		Integer procId = (Integer) processInstance.getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO);
		return this.findById(procId);
	}
}
