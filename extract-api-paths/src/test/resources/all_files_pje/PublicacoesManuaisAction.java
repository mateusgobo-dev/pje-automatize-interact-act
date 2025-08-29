package br.jus.cnj.pje.view.fluxo;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jbpm.graph.exe.ProcessInstance;

import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;

@Name(PublicacoesManuaisAction.NAME)
public class PublicacoesManuaisAction extends BaseAction<ProcessoExpediente>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "publicacoesManuaisAction";
	
	//Variável deve ser informada no evento 'Criar tarefa' do nó de publicação manual do expediente, baseado nos valores da ExpedicaoExpedienteEnum
	public static final String VARIAVEL_TIPOS_PUBLICACOES_MANUAIS = "tiposExpedientePublicacaoManual";
	
	@In
	private ProcessoExpedienteManager processoExpedienteManager;
	
	@In(create = false, required = true)
	private ProcessInstance processInstance;
	
	@In
	private FacesMessages facesMessages;
	
	private List<ProcessoExpediente> expedientes = new ArrayList<ProcessoExpediente>(0);
	
	@Create
	public void init(){
		
		String idsExpedientes = (String)processInstance.getContextInstance().getVariable(ComunicacaoProcessualAction.VARIAVEL_EXPEDIENTE);
		
		if (idsExpedientes == null || idsExpedientes.trim().isEmpty()){
			facesMessages.add(Severity.ERROR, "Nenhum expediente encontrado!");
			return;
		}
		
		List<Integer> idList = new ArrayList<Integer>(0); 
		for (String id : idsExpedientes.split(",")){
			idList.add(Integer.parseInt(id));
		}
		
		List<ProcessoExpediente> expedientesEncontrados = processoExpedienteManager.findByIds(idList);
	
		//Verifica quais expedientes encontrados se encaixam no filtro do nó
		String filtroTiposExpedientes = (String)TaskInstanceUtil.instance().getVariable(VARIAVEL_TIPOS_PUBLICACOES_MANUAIS);
		if (filtroTiposExpedientes != null && !filtroTiposExpedientes.trim().isEmpty()){
			String[] tipos = filtroTiposExpedientes.split(",");
			
			for (ProcessoExpediente expediente : expedientesEncontrados){
				for (String tipoExpediente : tipos){
					if (tipoExpediente.equalsIgnoreCase(expediente.getMeioExpedicaoExpediente().name())){
						expedientes.add(expediente);
						break;
					}
				}
			}
			
		}else{
			expedientes = expedientesEncontrados;
		}
	}
	
	@Override
	protected BaseManager<ProcessoExpediente> getManager(){
		return processoExpedienteManager;
	}

	public List<ProcessoExpediente> getExpedientes(){
		return expedientes;
	}

	public void setExpedientes(List<ProcessoExpediente> expedientes){
		this.expedientes = expedientes;
	}

	@Override
	public EntityDataModel<ProcessoExpediente> getModel() {
		throw new UnsupportedOperationException("Não implementado.");
	}

}
