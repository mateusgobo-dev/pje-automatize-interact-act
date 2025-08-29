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
 * PessoaServidor.
 * 
 * @author Marcone
 * 
 */
@Name("servidorPainelTree")
@SuppressWarnings("unchecked")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ServidorPainelTree implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String[] ROOT_NODES = { "Processos novos", "Processos não distribuidos", "Distribuidos",
			"Distribuidos com novas petições" };
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
				List localizacoes = getLocalizacoesProcessosNovos();
				this.localizacaoMap.put(type, localizacoes);
			} else if (type.equals(ROOT_NODES[1])) {
				List localizacoes = getLocalizacoesProcessosNaoDistribuidos();
				this.localizacaoMap.put(type, localizacoes);
			} else if (type.equals(ROOT_NODES[2])) {
				List localizacoes = getLocalizacoesProcessosDistribuidos();
				this.localizacaoMap.put(type, localizacoes);
			} else if (type.equals(ROOT_NODES[3])) {
				List localizacoes = getLocalizacoesProcessosDistribuidosPeticoes();
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

	private List getLocalizacoesProcessosNovos() {
		String hql = "SELECT pl.idLocalizacao as idLocalizacao, pl.localizacao as localizacao, COUNT(pt.idProcessoTrf) "
				+ "FROM ProcessoTrf pt "
				+ "JOIN pt.processo.localizacaoList pl "
				+ "JOIN pl.usuarioLocalizacaoList ul "
				+ "WHERE ul.usuario.idUsuario = :idUsuario "
				+ "AND pt.processoStatus = 'V' "
				+ "AND NOT EXISTS (SELECT ip FROM ProcessoTrfImpresso ip WHERE ip.processo.idProcesso = pt.processo.idProcesso) "
				+ "GROUP BY pl.idLocalizacao, pl.localizacao ";

		javax.persistence.Query query = EntityUtil.getEntityManager().createQuery(hql);
		Pessoa pessoa = getPessoaLogada();
		query.setParameter("idUsuario", pessoa.getIdUsuario());
		List list = query.getResultList();
		return list;
	}

	private List getLocalizacoesProcessosNaoDistribuidos() {
		String hql = "SELECT pl.idLocalizacao as idLocalizacao, pl.localizacao as localizacao, COUNT(pt.idProcessoTrf) "
				+ "FROM ProcessoTrf pt "
				+ "JOIN pt.processo.localizacaoList pl "
				+ "JOIN pl.usuarioLocalizacaoList ul "
				+ "WHERE ul.usuario.idUsuario = :idUsuario "
				+ "AND pt.processoStatus = 'V' "
				+ "AND EXISTS (SELECT ipd FROM ProcessoTrfDocumentoImpresso ipd WHERE ipd.processoDocumento.processo.idProcesso = pt.processo.idProcesso) "
				+ "GROUP BY pl.idLocalizacao, pl.localizacao ";

		javax.persistence.Query query = EntityUtil.getEntityManager().createQuery(hql);
		Pessoa pessoa = getPessoaLogada();
		query.setParameter("idUsuario", pessoa.getIdUsuario());
		List list = query.getResultList();
		return list;
	}

	private List getLocalizacoesProcessosDistribuidos() {
		String hql = "SELECT pl.idLocalizacao as idLocalizacao, pl.localizacao as localizacao, COUNT(pt.idProcessoTrf) "
				+ "FROM ProcessoTrf pt "
				+ "JOIN pt.processo.localizacaoList pl "
				+ "JOIN pl.usuarioLocalizacaoList ul "
				+ "WHERE ul.usuario.idUsuario = :idUsuario "
				+ "AND pt.processoStatus = 'D' " +
				// "AND EXISTS (SELECT ipd FROM ProcessoTrfDocumentoImpresso ipd WHERE ipd.processoDocumento.processo.idProcesso = pt.processo.idProcesso) "
				// +
				"GROUP BY pl.idLocalizacao, pl.localizacao ";

		javax.persistence.Query query = EntityUtil.getEntityManager().createQuery(hql);
		Pessoa pessoa = getPessoaLogada();
		query.setParameter("idUsuario", pessoa.getIdUsuario());
		List list = query.getResultList();
		return list;
	}

	private List getLocalizacoesProcessosDistribuidosPeticoes() {
		String hql = "SELECT pl.idLocalizacao as idLocalizacao, pl.localizacao as localizacao, COUNT(pt.idProcessoTrf) "
				+ "FROM ProcessoTrf pt "
				+ "JOIN pt.processo.localizacaoList pl "
				+ "JOIN pl.usuarioLocalizacaoList ul "
				+ "WHERE ul.usuario.idUsuario = :idUsuario "
				+ "AND pt.processoStatus = 'D' "
				+ "AND EXISTS (SELECT ipd FROM ProcessoTrfDocumentoImpresso ipd "
				+ "WHERE ipd.processoDocumento.processo.idProcesso = pt.processo.idProcesso) "
				+ "GROUP BY pl.idLocalizacao, pl.localizacao ";

		javax.persistence.Query query = EntityUtil.getEntityManager().createQuery(hql);
		Pessoa pessoa = getPessoaLogada();
		query.setParameter("idUsuario", pessoa.getIdUsuario());
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
			this.tipoNodeRoot = ProcessoStatusEnum.V;
		} else if (type.equals(ROOT_NODES[1])) {
			this.tipoNodeRoot = ProcessoStatusEnum.V;
		} else if (type.equals(ROOT_NODES[2])) {
			this.tipoNodeRoot = ProcessoStatusEnum.D;
		} else if (type.equals(ROOT_NODES[3])) {
			this.tipoNodeRoot = ProcessoStatusEnum.D;
		}
	}

	public void setTipoNodeRoot(ProcessoStatusEnum tipoNodeRoot) {
		this.tipoNodeRoot = tipoNodeRoot;
	}

	public ProcessoStatusEnum getTipoNodeRoot() {
		return this.tipoNodeRoot;
	}

}
