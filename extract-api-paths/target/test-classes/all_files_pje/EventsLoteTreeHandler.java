package br.com.infox.ibpm.component.tree;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;

import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.Evento;

/**
 * PJE-JT: Estavão Mognatto/Frederico Carneiro
 * 
 * Componente responsável por gerenciar o lançamento de movimento nas tarefas em lote.
 */
@Name(EventsLoteTreeHandler.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EventsLoteTreeHandler extends EventsTreeHandler {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "eventsLoteTree";
	

	@Override
	public void selectListener(NodeSelectedEvent ev) {
		HtmlTree tree = (HtmlTree) ev.getSource();
		treeId = tree.getId();
		EventsEntityNode en = (EventsEntityNode) tree.getData();
		
		if(!possueRestricao(en)){
			
			setSelected(en.getEntity(), isMultiplo(en));
			
			Events.instance().raiseEvent(getEventSelected(), getSelected());
		} else {
			
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Movimento selecionado não pode ser lançado em lote.");
			
		}
	}
	
	/**
	 * Verifica se o movimento tem alguma restrição para ser lançado nas tarefas em lote.
	 * Até o momento é verificado se tem complemento de tipo dinâmico e/ou se foi marcado no CRUD de 
	 * Movimentos Processuais se pode ou não lançar em lote.
	 */
	public Boolean possueRestricao(EventsEntityNode en) {
		Boolean possueRestricao = false;
		
		Evento entity = HibernateUtil.deproxy(en.getEntity(),Evento.class);
		
		EventoBean eb = new EventoBean();
		eb.setIdEvento(entity.getIdEvento());
		eb.setDescricaoMovimento(entity.toString());
		
		preencherEventoBean(eb);
		
		//verifica se o movimento possui complemento dinamico
		if(eb.possuiComplementoDinamico()) {
			possueRestricao = true;
		}
		
		//verifica se foi configurado no CRUD de Movimentos Processuais se pode ou não lançar em lote
		if(!entity.getPermiteLancarLote()) {
			possueRestricao = true;
		}
		
		return possueRestricao;
		
	}
	
	
	public static EventsLoteTreeHandler instance() {
		return (EventsLoteTreeHandler) org.jboss.seam.Component.getInstance(EventsLoteTreeHandler.NAME);
	}

	
	@Override
	protected void validacoesAposLancamento() {
		
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

	
}
