package br.com.infox.component.tree;
 
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.ajax4jsf.model.DataComponentState;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.richfaces.event.NodeSelectedEvent;

import br.com.infox.cliente.home.ConsultaProcessoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.dao.SituacaoProcessoDAO;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.filters.SituacaoProcessoFilter;

@Name(TarefaTree.NAME)
@Install(precedence = Install.FRAMEWORK)
@Scope(ScopeType.CONVERSATION)
public class TarefaTree extends AbstractTreeHandler<Map<String, Object>> {

	private static final long serialVersionUID = 8534708848510558120L;
	public static final String NAME = "tarefaTree";
	public static final String CLEAR_TAREFA_TREE_EVENT = "clearTarefaTreeEvent";
	protected List<TarefaNode<Map<String, Object>>> rootList;
	protected DataComponentState componentState;
	private Set<Integer> listaIdCaixaAtualizar;
	private boolean segredo;
	private Competencia competencia;
	private String numeroProcesso;
	

	private String idsLocalizacoesfisicasStr;
	private boolean isServidorExclusivoColegiado;
	private Integer idOJCAtual;
	private boolean isVisualizaSigiloso;
	private Integer idUsrLocAtual;
	private Boolean existeDefinicaoVisibilidadePorCargo;
	
	public TarefaTree(){
		idsLocalizacoesfisicasStr = Authenticator.getIdsLocalizacoesFilhasAtuais();
		isServidorExclusivoColegiado = Authenticator.isServidorExclusivoColegiado();
		idOJCAtual = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
		isVisualizaSigiloso = Authenticator.isVisualizaSigiloso();
		idUsrLocAtual = Authenticator.getIdUsuarioLocalizacaoAtual();		
		existeDefinicaoVisibilidadePorCargo = Authenticator.temOrgaoVisivel();
	}

	@Override
	protected String getQueryRoots() {
		return this.getQueryRoots(null);
	}
	
	/**
	 *  Recebe o idTask para atualizar somente a tarefa desejada. Este cenário é necessário no drop e drag da listagem dos processos. 
	 * @param idTask
	 * @return
	 */
	protected String getQueryRoots(String nomeTask) { 
		StringBuilder sb = new StringBuilder();
		sb.append("select new map(max(s.idSituacaoProcesso) as id, ");
		sb.append("s.nomeTarefa as nomeTarefa, ");
		sb.append("max(s.idTarefa) as idTask, ");
		sb.append("count(s.idLote) as qtdEmLote, ");
		sb.append("count(s.nomeCaixa) as qtdEmCaixa, ");
		sb.append("count(s.idSituacaoProcesso) as qtd,");
		sb.append("'Task' as type,");
		sb.append("'");
		sb.append(isSegredo());
		sb.append("' as segredo,");
		sb.append("'");
		sb.append(getTreeType());
		sb.append("' as tree, ");
		sb.append("1 as qtdCaixaFiltro) "); 	

		SituacaoProcessoDAO situacaoProcessoDAO = (SituacaoProcessoDAO) Component.getInstance(SituacaoProcessoDAO.class, true);
		sb.append(situacaoProcessoDAO.getQueryFromTarefasPermissoes("s", isSegredo(), isVisualizaSigiloso, idsLocalizacoesfisicasStr, 
				isServidorExclusivoColegiado, idOJCAtual));
				
		aplicarFiltrosCaixa();
	
		if(nomeTask != null){
			sb.append(" WHERE s.nomeTarefa = '" + nomeTask + "'");
		}
		
		sb.append(" group by s.nomeTarefa ");
		sb.append(" order by 2");
		
		return sb.toString();
	}
	
	/**
	 * Método responsável por aplicar os filtros de acordo com os parâmetros inseridos no formulário.
	 */
	private void aplicarFiltrosCaixa() {
		if (this.competencia != null) {
			HibernateUtil.setFilterParameter(
					SituacaoProcessoFilter.FILTER_COMPETENCIA, "idCompetencia", this.competencia.getIdCompetencia());
		} else {
			HibernateUtil.getSession().disableFilter(SituacaoProcessoFilter.FILTER_COMPETENCIA);
		}
		
		if (StringUtils.isNotBlank(this.numeroProcesso)) {
			HibernateUtil.setFilterParameter(
					SituacaoProcessoFilter.FILTER_NUMERO_PROCESSO, "numeroProcesso", this.numeroProcesso);
		} else {
			HibernateUtil.getSession().disableFilter(SituacaoProcessoFilter.FILTER_NUMERO_PROCESSO);
		}
	}
	
	/**
	 * Filtra a caixas de acordo com os parâmetros inseridos no formulário.
	 */
	public void filtrar() {
		clearTree();
		getTarefasRoots();
	}
	
	/**
	 * Limpar os filtros inseridos na pagina
	 */
	public void limparFiltros() {
		this.competencia = null;
		this.numeroProcesso = null;
		filtrar();
	}

	protected String getQueryChildren(boolean isCount) {
		StringBuilder sb = new StringBuilder();
		if(isCount){
			sb.append("select count(c.idCaixa) ");
			sb.append("from CaixaFiltro c where c.tarefa.tarefa = s.nomeTarefa ");
		}else{
			sb.append("select new map(c.idCaixa as idCaixa, ");
			sb.append("c.tarefa.idTarefa as idTarefa, ");
			sb.append("c.nomeCaixa as nomeCaixa, ");
			sb.append("'Caixa' as type, ");
			sb.append("'");
			sb.append(isSegredo());
			sb.append("' as segredo, ");
			
			sb.append("(SELECT count(v.nomeCaixa) ");

			SituacaoProcessoDAO situacaoProcessoDAO = (SituacaoProcessoDAO) Component.getInstance(SituacaoProcessoDAO.class, true);
			sb.append(situacaoProcessoDAO.getQueryFromTarefasPermissoes("v", isSegredo(), isVisualizaSigiloso, idsLocalizacoesfisicasStr, 
					isServidorExclusivoColegiado, idOJCAtual));
			
			sb.append(" AND v.idCaixa = c.idCaixa ");
									
			if(existeDefinicaoVisibilidadePorCargo){
				String dataAtual = new String("'" + new Timestamp(DateUtils.truncate(new Date(), Calendar.DATE).getTime()).toString() + "'");
				sb.append(" and exists ( select 1 from UsuarioLocalizacaoVisibilidade as ulv ");							
				sb.append(" where ulv.usuarioLocalizacaoMagistradoServidor = ").append(idUsrLocAtual);
				sb.append(" and ulv.orgaoJulgadorCargo = v.idOrgaoJulgadorCargo");
				sb.append(" and ulv.dtInicio <= ").append(dataAtual);
				sb.append(" and ( ulv.dtFinal is null or (ulv.dtFinal >= ").append(dataAtual).append(" ))) ");				
			}
					
			sb.append(" ) as qtdEmCaixa) ");
			sb.append("from CaixaFiltro c where c.tarefa.tarefa = :taskId ");
		}
				
		if (!isCount) {
			sb.append("order by 3");
		}
		
		return sb.toString();
	}
	
	protected String getTreeType() {
		return "caixa";
	}
	
	@Override
	public void selectListener(NodeSelectedEvent ev) {
		ConsultaProcessoHome.instance().limparTela("classeJudicialCompletoSuggest");
		super.selectListener(ev);
	}
	
	@Override
	protected String getEventSelectedCaixa() {
		return Eventos.SELECIONADA_CAIXA_DE_TAREFA;
	}

	public boolean isSegredo() {
		return segredo;
	}

	public void setSegredo(boolean segredo) {
		this.segredo = segredo;
	}	
	
	@Override
	protected String getEventSelected() {
		return Eventos.SELECIONADA_TAREFA;
	}

	public Integer getTaskId() {
		if (getSelected() != null) {
			return (Integer) getSelected().get("idTask");
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public List<TarefaNode<Map<String, Object>>> getTarefasRoots(){
		if (this.rootList == null){
			this.rootList = new ArrayList<TarefaNode<Map<String, Object>>>();
			String query = this.getQueryRoots();
			Query queryRoots = getEntityManager().createQuery(query);
			List<Map<String, Object>> roots = queryRoots.getResultList();
			for (Map<String, Object> e : roots){
				TarefaNode<Map<String, Object>> node = new TarefaNode<Map<String, Object>>(null, e, getQueryChildren());
				this.rootList.add(node);
			}			
		}
		return this.rootList;
	}	
	
	@Override
	protected String getQueryChildren() {
		return this.getQueryChildren(false);
	}
	
	public static void adicionarIdTarefa(Integer idTarefa){
		TarefaTree tree = ComponentUtil.getComponent("tarefaTree");
		tree.setListaIdCaixaAtualizar(new HashSet<Integer>());
		if(idTarefa != null){
			tree.getListaIdCaixaAtualizar().add(idTarefa);
		}
	}	
	
	@Override
	public void clearTree() {
		Events.instance().raiseEvent(CLEAR_TAREFA_TREE_EVENT);
		ConsultaProcessoHome.instance().setTab("caixa");
		this.rootList = null;
		this.componentState = null;
		super.clearTree();
	}

	public DataComponentState getComponentState() {
		return componentState;
	}

	public void setComponentState(DataComponentState componentState) {
		this.componentState = componentState;
	}

	public Set<Integer> getListaIdCaixaAtualizar() {
		return listaIdCaixaAtualizar;
	}

	public void setListaIdCaixaAtualizar(Set<Integer> listaIdCaixaAtualizar) {
		this.listaIdCaixaAtualizar = listaIdCaixaAtualizar;
	}
	
	public Competencia getCompetencia() {
		return competencia;
	}
	
	public void setCompetencia(Competencia competencia) {
		this.competencia = competencia;
	}
	
	public String getNumeroProcesso() {
		return numeroProcesso;
	}
	
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

}