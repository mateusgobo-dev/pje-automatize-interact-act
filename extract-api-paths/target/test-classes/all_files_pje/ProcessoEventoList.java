package br.com.infox.pje.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.visao.beans.ProcessoEventoBean;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoMovimento;

@Name(ProcessoEventoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoEventoList extends EntityList<ProcessoEvento> {

	public static final String NAME = "processoEventoList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_EJBQL = "select o from ProcessoEvento o where o.evento.segredoJustica = false"; 
	private static final String DEFAULT_ORDER = "o.dataAtualizacao desc";

	private static final String R1 = "o.processo.numeroProcesso = #{processoEventoList.entity.processo.numeroProcesso}";
	private static final String R2 = "o.processo = #{processoTrfHome.instance.processo} ";
	private static final String R3 = "o.visibilidadeExterna = #{processoEventoHome.instance().visibilidadeExterna == true ? true : null}";
	
	private List<ProcessoEventoBean> processoEventoBeanList = new ArrayList<ProcessoEventoBean>();

	@Override
	protected void addSearchFields() {
		addSearchField("processo.numeroProcesso", SearchCriteria.igual, R1);
		addSearchField("processo", SearchCriteria.igual, R2);
		addSearchField("visibilidadeExterna", SearchCriteria.igual, R3);
	}

	@Override
	public void newInstance() {
		processoEventoBeanList = new ArrayList<ProcessoEventoBean>();
		super.newInstance();
		getEntity().setProcesso(new Processo());
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}
	
	public Boolean getAtivarGrid() {
		if ((getEntity().getProcesso() == null || 
				getEntity().getProcesso().getNumeroProcesso() == null || 
				getEntity().getProcesso().getNumeroProcesso().isEmpty())
				&& new Util().eval("#{processoTrfHome.instance.processo}") == null) {
			return Boolean.FALSE;
		}
		else { 
			return Boolean.TRUE;
		}
	}
	
	@Override
	public List<ProcessoEvento> getResultList() {
		return getAtivarGrid() ? super.getResultList() : new ArrayList<ProcessoEvento>();
	}

	@Override
	public Long getResultCount() {
		return getAtivarGrid() ? super.getResultCount() : 0;
	}
	
	/**
	 * Retorna se um Movimento é passível de exclusão (para habilitar o checkbox na tela de exclusão de movimentos) 
	 * dada sua aplicabilidade (Órgão de Justiça, Grau e Sujeito Ativo).
	 * 
	 * @param processoEvento Movimento em questão.
	 * @return Boolean
	 */
	public Boolean getRenderedCheckboxRemover(ProcessoEvento processoEvento) {
		Boolean retorno = false;

		String movimentoExclusao = CodigoMovimentoNacional.CODIGO_MOVIMENTO_EXCLUSAO_MOVIMENTO;
		String codEvento = ((Evento) HibernateUtil.deproxy(processoEvento.getEvento(), Evento.class)).getCodEvento();
		
		if (codEvento.equals(movimentoExclusao) || processoEvento.getProcessoEventoExcludente()!=null) {
			retorno = false;
		} else {
			AplicacaoMovimento aplicacaoMovimento = ComponentUtil.getComponent(LancadorMovimentosService.class)
				.getAplicacaoMovimentoByEvento(processoEvento.getEvento());
				
			retorno = aplicacaoMovimento != null && aplicacaoMovimento.getPermiteExclusao();
		}
		
		return retorno;
	}

	/**
	 * Retorna a lista de objetos encapsulados, fazendo uma chamada ao método
	 * {@link ProcessoEventoList#getProcessoEventoBeanList(int, boolean)}
	 * passando <code>false</code> como parâmetro booleano.
	 * @param maxResult
	 *            Quantidade máxima de resultados por página.
	 * @return List<ProcessoEventoBean>
	 */
	public List<ProcessoEventoBean> getProcessoEventoBeanList(int maxResult){
		return this.getProcessoEventoBeanList(maxResult, false);
	}
	
	/**
	 * Retorna a lista de objetos encapsulados para exibição na tela de exclusão
	 * de movimentos.
	 * 
	 * @param maxResult
	 *            Quantidade máxima de resultados por página.
	 * @param visibilidadeExterna
	 * 			  Se <code>true</code>, indica que só eventos com visibilidade
	 * 				externa serão retornados.
	 * @return List<ProcessoEventoBean>
	 */
	public List<ProcessoEventoBean> getProcessoEventoBeanList(int maxResult, boolean visibilidadeExterna) {
		List<ProcessoEventoBean> resultado = new ArrayList<ProcessoEventoBean>();
		
		List<ProcessoEvento> eventosPaginaAtual = list(maxResult);
		
		for (ProcessoEvento eventoPaginaAtual : eventosPaginaAtual) {
			if (!visibilidadeExterna	|| eventoPaginaAtual.getVisibilidadeExterna()) {
				ProcessoEventoBean processoEventoBean = new ProcessoEventoBean();
				processoEventoBean.setProcessoEvento(eventoPaginaAtual);
				
				Integer indice = this.processoEventoBeanList.indexOf (processoEventoBean);
				
				if (indice >=0){
					 resultado.add(processoEventoBeanList.get(indice));
				}
				else {
					atualizarProcessoEventoBean(processoEventoBean);
					resultado.add(processoEventoBean);
					this.processoEventoBeanList.add(processoEventoBean);
				}
			}	
		}

		return resultado;

	}
	
	/**
	 *   Atualiza atributos de um ProcessoEventoBean a partir dos atributos do ProcessoEvento
	 *   contido (encapsulado) nele.  
	 * @param bean
	 * 			ProcessoEventoBean em questão.
	*/
	private void atualizarProcessoEventoBean(ProcessoEventoBean bean) {
		ProcessoEvento processoEvento = bean.getProcessoEvento();
		bean.setRenderCheckBox(getRenderedCheckboxRemover(processoEvento));
		
		if (processoEvento.getVisibilidadeExterna() == null) {
			Evento eventoProcessual = (Evento) HibernateUtil.deproxy(processoEvento.getEvento(), Evento.class);
			bean.setVisibilidadeExterna(eventoProcessual.getVisibilidadeExterna());
		} else {
			bean.setVisibilidadeExterna(processoEvento.getVisibilidadeExterna());
		}

		boolean isCheckboxComplementosRendered = processoEvento.getComplementoSegmentadoList().size() > 0;
		
		bean.setRenderCheckboxVisibilidadeComplemento(isCheckboxComplementosRendered);
	}
	
	public List<ProcessoEventoBean> getProcessoEventoBeanList() {
		return processoEventoBeanList;
	}
	
	/**
	 * Método utilizado para atualizar tabela de movimentos após a exclusão de algum registro.
	 */
	public void refreshList() {
		processoEventoBeanList = new ArrayList<ProcessoEventoBean>();
		getProcessoEventoBeanList(20);
	}
}
