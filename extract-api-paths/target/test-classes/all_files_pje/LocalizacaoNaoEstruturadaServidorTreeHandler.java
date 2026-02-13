package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;
import org.richfaces.event.NodeSelectedEvent;

import br.com.infox.cliente.home.UsuarioLocalizacaoMagistradoServidorHome;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.ibpm.component.tree.LocalizacaoEstruturaSearchTreeHandler;
import br.com.infox.ibpm.component.tree.LocalizacaoEstruturaTreeHandler;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.pje.nucleo.entidades.Localizacao;

@Name("localizacaoNaoEstruturadaServidorTree")
@BypassInterceptors
public class LocalizacaoNaoEstruturadaServidorTreeHandler extends LocalizacaoEstruturaSearchTreeHandler {

	private static final long serialVersionUID = -3108287866771999436L;
	private int idLocalizacao;

	/**
	 * @return instância do componente.
	 */
	public static LocalizacaoNaoEstruturadaServidorTreeHandler instance() {
		return ComponentUtil.getComponent(LocalizacaoNaoEstruturadaServidorTreeHandler.class);
	}
	
	public static UsuarioLocalizacaoMagistradoServidorHome usuarioLocalizacaoMagistradoServidorHome() {
        return ComponentUtil.getComponent(UsuarioLocalizacaoMagistradoServidorHome.class);
    }
	
	public void setIdLocalizacao(int idLocalizacao){
		this.idLocalizacao = idLocalizacao;
	}

	@Override
	protected String getQueryRoots() {
//		StringBuilder sb = new StringBuilder();
//		sb.append(" SELECT n FROM Localizacao n WHERE localizacaoPai.idLocalizacao = " + idLocalizacao);
//		sb.append(" ORDER BY n.faixaInferior");
//		return sb.toString();
		Localizacao localizacaoModelo = getLocalizacaoModeloRaiz();
		Integer localizacaoModeloId = (localizacaoModelo != null ? localizacaoModelo.getIdLocalizacao() : null);

//		Localizacao localizacaoModeloSelecionado = getLocalizacaoModeloRaiz();
//		Integer localizacaoModeloId = (localizacaoModelo != null ? localizacaoModelo.getIdLocalizacao() : null);
		
		return getLocalizacaoManager().getQueryRootsLocalizacao(
				getUsrLocalizacaoHome().getInstance(), null, localizacaoModeloId);
	}

	@Override
	protected String getEventSelected() {
		return null;
	}

	@Override
	protected Localizacao getEntityToIgnore() {
		return ComponentUtil.getInstance("localizacaoHome");
	}

	@Override
	protected EntityNode<Localizacao> createNode() {
		LocalizacaoNodeServidor node = new LocalizacaoNodeServidor(getQueryChildrenList());
		String queryFilhoPrimeiroNivel = getQueryFilhoPrimeiroNivel();
		if (queryFilhoPrimeiroNivel != null) {
			node.setQueryFilhoNoRaiz(queryFilhoPrimeiroNivel);
		}
		return node;
	}

	/**
	 * Obtem a lista dos ids que deverão ser filtrados para o filho de TRF5
	 * 
	 * @return sql para retornar a lista de localizações
	 */
	private String getQueryFilhoPrimeiroNivel() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT l FROM Localizacao l WHERE l.localizacaoPai =:");
		sb.append(EntityNode.PARENT_NODE);
		sb.append(" ORDER BY l.faixaInferior");
		return sb.toString();
	}
	
	@Override
	public void selectListener(NodeSelectedEvent ev) {
		EntityNode<Localizacao> en = getEntityNode(ev);
		
		if (en != null) {
			setSelected(en.getEntity());
			Events.instance().raiseEvent("evtSelectLocalizacaoEstrutura", getSelected(), getEstrutura(en));
		}
	}
	
	@Override
	public void clearTree() {
		super.clearTree();
	}
}
