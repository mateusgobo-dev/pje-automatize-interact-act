package br.com.infox.cliente.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.richfaces.component.UITree;
import org.richfaces.component.html.HtmlTreeNode;
import org.richfaces.event.NodeSelectedEvent;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

/**
 * Classe que retorna informações para serem populadas no TreeView do Painel de
 * processos.
 * 
 * @author Marcone
 * 
 */
@Name("advogadoProcuradorPainelTree")
@SuppressWarnings("unchecked")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class AdvogadoProcuradorPainelTree implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String[] ROOT_NODES = { "Processos com cadastro não finalizado",
			"Processos com cadastro finalizado.", "Processos distribuidos." };
	private Integer idLocalizacaoCorrente;
	private ProcessoStatusEnum tipoNodeRoot;
	private Map<String, List<Localizacao>> localizacaoMap = new HashMap<String, List<Localizacao>>();

	/**
	 * Retorna uma Lista de processos baseado na raiz da grid.
	 * 
	 * @param type
	 *            Raiz de cada diretório
	 * @return
	 */
	public List getLocalizacoes(String type) {
		if (this.localizacaoMap.get(type) == null) {
			setTipoNodeRoot(type);
			if (type.equals(ROOT_NODES[0])) {
				List localizacoes = getLocalizacoesPessoa();
				this.localizacaoMap.put(type, localizacoes);
			} else if (type.equals(ROOT_NODES[1])) {
				List localizacoes = getLocalizacoesPessoa();
				this.localizacaoMap.put(type, localizacoes);
			} else if (type.equals(ROOT_NODES[2])) {
				List localizacoes = getLocalizacoesPessoa();
				this.localizacaoMap.put(type, localizacoes);
			}
		}
		return this.localizacaoMap.get(type);
	}

	/**
	 * Retorna a quantidade de processos dentro de cada pasta.
	 * 
	 * @param type
	 * @return
	 */
	public int getQtdLocalizacoes(String type) {
		return getLocalizacoes(type).size();
	}

	/**
	 * Método chamando quando um nó é selecionado. Seta a localização
	 * selecionada.
	 * 
	 * @param event
	 */
	public void selectListener(NodeSelectedEvent event) {
		HtmlTreeNode tn = (HtmlTreeNode) event.getSource();
		UITree tree = tn.getUITree();
		/*
		 * HtmlTreeNodesAdaptor ad = (HtmlTreeNodesAdaptor)
		 * tn.getParent().getParent(); HtmlTreeNode n = (HtmlTreeNode)
		 * ad.getChildren().get(0); Object data = n.getData();
		 */
		Object[] rowData = (Object[]) tree.getRowData();
		setIdLocalizacaoCorrente((Integer) rowData[0]);
		setTipoNodeRoot((String) tn.getData());
	}

	public String[] getRootNodes() {
		return ROOT_NODES;
	}

	private List getLocalizacoesPessoa() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT pl.idLocalizacao as idLocalizacao, pl.localizacao as localizacao, COUNT(pt) as qtdProcessos ");
		sb.append("FROM ProcessoTrf pt   ");
		sb.append("JOIN pt.processoParteList ppl ");
		sb.append("JOIN pt.processo.localizacaoList pl ");
		sb.append("WHERE ppl.pessoa = :pessoa ");
		sb.append("AND pt.processoStatus = :statusProcesso ");
		sb.append("AND EXISTS (SELECT iul.localizacaoFisica FROM Usuario iu JOIN iu.usuarioLocalizacaoList iul WHERE ppl.pessoa.usuario = iu AND iul.localizacaoFisica = pl) ");
		sb.append("GROUP BY pl.idLocalizacao, pl.localizacao");
		javax.persistence.Query query = EntityUtil.getEntityManager().createQuery(sb.toString());
		Pessoa pessoa = getPessoaLogada();
		query.setParameter("pessoa", pessoa);
		query.setParameter("statusProcesso", getTipoNodeRoot());
		List list = query.getResultList();
		return list;
	}

	private Pessoa getPessoaLogada() {
		Pessoa resultado = null;
		Context sessionContext = Contexts.getSessionContext();
		if (sessionContext != null) {
			resultado = (Pessoa) Contexts.getSessionContext().get("pessoaLogada");
		}
		return resultado;
	}

	public void setIdLocalizacaoCorrente(Integer idLocalizacaoCorrente) {
		this.idLocalizacaoCorrente = idLocalizacaoCorrente;
	}

	public Integer getIdLocalizacaoCorrente() {
		return idLocalizacaoCorrente;
	}

	private void setTipoNodeRoot(String type) {
		if (type.equals(ROOT_NODES[0])) {
			this.tipoNodeRoot = ProcessoStatusEnum.E;
		} else if (type.equals(ROOT_NODES[1])) {
			this.tipoNodeRoot = ProcessoStatusEnum.V;
		} else if (type.equals(ROOT_NODES[2])) {
			this.tipoNodeRoot = ProcessoStatusEnum.D;
		} else if (type.equals(ROOT_NODES[3])) {
			this.tipoNodeRoot = ProcessoStatusEnum.E;
		}
	}

	public void setTipoNodeRoot(ProcessoStatusEnum tipoNodeRoot) {
		this.tipoNodeRoot = tipoNodeRoot;
	}

	public ProcessoStatusEnum getTipoNodeRoot() {
		return this.tipoNodeRoot;
	}

}
