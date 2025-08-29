
package br.jus.cnj.pje.nucleo.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ConsultaProcessualDAO;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name("consultaProcessualManager")
public class ConsultaProcessualManager extends BaseManager<ProcessoTrf> implements Serializable{

	private static final long serialVersionUID = 1L;
	private List<Integer> idsProcessoTrfPesquisa ;
	private Map<String, Order> ordenacao;
	private Integer quantidadeMaxima;
	
	@In
	private ConsultaProcessualDAO consultaProcessualDAO;
	
	@In(required = false)
	protected transient FacesContext facesContext;
	
	@In
	protected FacesMessages facesMessages;
	
	@Override
	protected ConsultaProcessualDAO getDAO() {
		return consultaProcessualDAO;
	}

	/**
	 * Metodo responsavel por devolver um EntityDataModel que recupere 
	 * somente os registros de cada pagina.
	 * 
	 * @param criterios
	 * @param ordenacao
	 * @param quantidadeMaxima
	 * @return
	 * @throws Exception
	 */
	public EntityDataModel<ProcessoTrf> pesquisar(List<Criteria> criterios, Map<String, Order> ordenacao, Integer quantidadeMaxima){
		
		EntityDataModel<ProcessoTrf> model = new EntityDataModel<>(ProcessoTrf.class, facesContext, getRetriever());
		
		idsProcessoTrfPesquisa = consultaProcessualDAO.getIdProcessosByCriterias(criterios, ordenacao, (quantidadeMaxima == null ? null : quantidadeMaxima+1)  );
		
		if(idsProcessoTrfPesquisa.isEmpty()) {
			return null;
		}
		
		this.ordenacao = ordenacao;
		this.quantidadeMaxima = quantidadeMaxima;
		
		return model;
	}
	
	/**
	 * Metodo responsavel por retornar se o numero de registros encontrados eh maior
	 * que o numero de registros maximos solicitados.
	 * 
	 * @return
	 */
	public boolean isNumeroMaximoDeRegistrosExcedido() {
		return idsProcessoTrfPesquisa != null && quantidadeMaxima != null && idsProcessoTrfPesquisa.size() > quantidadeMaxima;
	}
	
	/**
	 * Metodo responsavel por fazer a paginacao do resultado de pesquisa.
	 * @return
	 */
	protected DataRetriever<ProcessoTrf> getRetriever() {
		
		return new DataRetriever<ProcessoTrf>() {
			
			final ConsultaProcessualManager manager = ComponentUtil.getComponent(ConsultaProcessualManager.class);
			
			@Override
			public ProcessoTrf findById(Object id) throws Exception {
				return manager.findById(id);
			}
			
			@Override
			public List<ProcessoTrf> list(Search search) {
				
				paginarPesquisa(search);
				
				return manager.list(search);
			}
				
			@Override
			public long count(Search search) {
				
				if(quantidadeMaxima == null || ((idsProcessoTrfPesquisa.size() -1) < quantidadeMaxima)) {
					return idsProcessoTrfPesquisa.size();
				}else {
					return quantidadeMaxima;
				}
				
			}
			@Override
			public Object getId(ProcessoTrf obj){
				return manager.getId(obj);
			}
		};
	}
	
	/**
	 * Metodo responsavel por setar os parametros da pesquisa da pagina especifica.
	 * 
	 * @param search
	 */
	private void paginarPesquisa(Search search) {
		
		List<Integer> idsDaPagina = getListaIDsDaPagina(search.getFirst(), search.getMax());
		
		Criteria criterio = Criteria.in("idProcessoTrf", Arrays.copyOf(idsDaPagina.toArray(), idsDaPagina.size()));
		
		try {
			
			search.clear();
			search.addCriteria(criterio);
			search.setFirst(0);
			
			for (Map.Entry<String,Order> entry : ordenacao.entrySet()) {
				search.addOrder(entry.getKey(), entry.getValue());
			}
			
		} catch (NoSuchFieldException e) {
			facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
		}
	}
	
	/**
	 * Metodo responsavel por selecionar os registros da pagina.
	 * 
	 * @param inicio
	 * @param maximo
	 * @return
	 */
	private List<Integer> getListaIDsDaPagina(int inicio, int maximo ){
		
		if(idsProcessoTrfPesquisa == null) return new ArrayList<>(0);
		
		List<Integer> ids = new ArrayList<>();
		
		for(int i = inicio; i < (inicio + maximo) ; i++) {
			if(i < idsProcessoTrfPesquisa.size() ) {
				ids.add(idsProcessoTrfPesquisa.get(i));
			}else {
				break;
			}
		}
		
		return ids;
	}

}
