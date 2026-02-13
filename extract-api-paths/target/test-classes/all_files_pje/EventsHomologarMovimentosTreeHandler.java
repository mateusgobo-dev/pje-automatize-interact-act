package br.com.infox.ibpm.component.tree;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.EntityNode;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.nucleo.entidades.Evento;

/**
 * PJE-JT: David Vieira: [[PJEII-X] Alteracoes feitas pela JT: 2012-02-29
 * 
 * Componente responsável por gerenciar o lançamento de movimento no homologador de movimentos.
 */
@Name(EventsHomologarMovimentosTreeHandler.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EventsHomologarMovimentosTreeHandler extends EventsTreeHandler {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "eventsHomologarMovimentosTree";
	public static EventsHomologarMovimentosTreeHandler instance() {
		return (EventsHomologarMovimentosTreeHandler) org.jboss.seam.Component.getInstance(EventsHomologarMovimentosTreeHandler.NAME);
	}

	/**
	 * PJE-JT: David Vieira: [PJE-779] Não disparar o evento de registrar os
	 * movimentos no TarevaEvento se não for relacionado ao fluxo
	 */
	@Override
	protected boolean isRegisterAfterRegisterEvent() {
		return true;
	}
	
	public List<EntityNode<Evento>> getRoots(Integer agrupamentos) {
		// TODO Refatorar para usar composição e evitar acoplamento de código de lançadores filhos
		boolean deveRefazerInicializacao = (super.getAgrupamentosInstance() == null || !super.getAgrupamentosInstance().equals(agrupamentos));
		if(deveRefazerInicializacao) {
			clearList();
		}
		List<EntityNode<Evento>> roots = super.getRoots(agrupamentos); // Acoplamento necessário pela arquitetura de lancadores atual
		return roots;
	}

	@Override
	protected void validacoesAposLancamento(){
		super.validacoesAposLancamento();
	}

	/**
	 * Método responsável por atualizar a variável com as alterações feitas pelo
	 * usuário em tela
	 */
	public void gravarAlteracoes() {
		LancadorMovimentosService.instance().setMovimentosTemporarios(org.jboss.seam.bpm.ProcessInstance.instance(), EventsHomologarMovimentosTreeHandler.instance().getEventoBeanList());
	}
}
