/**
 * pje
 * Copyright (C) 2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.ajax4jsf.model.SerializableDataModel;
import org.richfaces.model.FilterField;
import org.richfaces.model.Ordering;
import org.richfaces.model.SortField2;


import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de paginação de dados para componentes de controle de visão.
 * 
 * @author Paulo Cristovão de Araújo Silva Filho
 * @since 1.4.6.2.R4
 * 
 */
public class EntityDataModel<E> extends SerializableDataModel implements org.richfaces.model.Modifiable{
	
	/**
	 * Interface que deve ser implementada pelo componente de controle de página para recuperação
	 * dos objetos pagináveis e de suas informações básicas necessárias para a paginação.
	 * 
	 * @author Paulo Cristovão de Araújo Silva Filho
	 *
	 * @param <E> a classe do objeto que será tratado por este modelo de dados.
	 */
	public interface DataRetriever<E>{
		/**
		 * Recupera o identificador unívoco do objeto.
		 * 
		 * @param obj o objeto cujo identificador unívoco se pretende recuperar
		 * @return o identificador unívoco do objeto.
		 */
		public Object getId(E obj);
		
		/**
		 * Recupera um objeto por seu identificador unívoco.
		 * 
		 * @param id o identificador unívoco do objeto que se pretende recuperar
		 * @return o objeto com o identificador dado, ou null, se ele não existir.
		 * @throws Exception caso tenha havido algum erro durante a recuperação.
		 */
		public E findById(Object id) throws Exception;
		
		/**
		 * Recupera a lista de objetos dados que satisfaçam os critérios de consulta ({@link Search});
		 * 
		 * @param search o objeto de consulta
		 * @return a lista de objetos que satisfazem os critérios
		 */
		public List<E> list(Search search);
		
		/**
		 * Recupera o número de objetos que satisfazem os critérios de consulta ({@link Search}).
		 *  
		 * @param search o objeto de consulta
		 * @return o número total de objetos de consulta que satisfazem os critérios
		 */
		public long count(Search search);
	};

	private static final long serialVersionUID = 9680004819858360L;
	
	/**
	 * Armazena o número total de objetos que satisfazem os critérios de consulta deste modelo
	 */
	private Long count;
	
	/**
	 * O identificador do objeto atualmente tratado no design pattern visitor.
	 */
	protected Object id;
	
	/**
	 * O objeto atualmente tratado no design pattern visitor.
	 */
	protected E current;
	
	/**
	 * O objeto de consulta deste modelo.
	 */
	protected Search search;
	
	/**
	 * A implementação do recuperador de dados que é utilizada por este modelo de dados.
	 */
	protected DataRetriever<E> retreiver;
	
	/**
	 * A lista de objetos contidos nesta página.
	 */
	protected List<E> page;
	
	/**
	 * O contexto faces aplicável a este modelo de dados, a fim de assegurar que eventuais mensagens sejam repassadas ao usuário.
	 */
	private FacesContext context;
	
	/**
	 * Indica que os critérios do Search sofreram modificação e, por isso, demandam a atualização do contador de objetos.
	 */
	private boolean changedCriterias = false;
	
	private Integer first = null;
	
	private Integer max = null;
	
	private Boolean refreshPage = false;

	public Boolean getRefreshPage() {
		return refreshPage;
	}

	public void setRefreshPage(Boolean refreshPage) {
		this.refreshPage = refreshPage;
	}

	/**
	 * Construtor padrão de um modelo de dados paginado.
	 * 
	 * @param clazz a classe do objeto a que se aplica este modelo.
	 * @param context o contexto Faces aplicável.
	 * @param retriever implementação de {@link DataRetriever} que permite a recuperação concreta dos dados
	 */
	public EntityDataModel(Class<E> clazz, FacesContext context, DataRetriever<E> retriever){
		this(clazz, clazz, context, retriever);
	}
	
	public EntityDataModel(Class<E> clazz, Class<?> searchClass, FacesContext context, DataRetriever<E> retriever){
		search = new Search(searchClass);
		this.context = context;
		this.retreiver = retriever;
		changedCriterias = true;
	}
	
	/**
	 * Indica que este modelo de dados deve recuperar um conjunto dos dados que satisfazem os critérios,
	 * ou seja, que a lista de retorno contenha uma, e apenas uma, referência para cada um dos objetos que
	 * satisfazem os critérios de pesquisa.
	 *  
	 * @param distinct true, para assegurar que a consulta deve recuperar um conjunto, false, para permitir 
	 * a repetição de um mesmo objeto na lista de pesquisa
	 */
	public void setDistinct(boolean distinct){
		search.setDistinct(distinct);
		changedCriterias = true;
	}
	
	/**
	 * Define todos os critérios que devem ser aplicados à lista de resposta deste modelo de dados.
	 * 
	 * @param criterias os critérios a serem incluídos.
	 * @throws NoSuchFieldException  caso algum dos critérios repassados inclua campo inexistente no objeto de pesquisa
	 */
	public void setCriterias(List<Criteria> criterias) throws NoSuchFieldException{
		search.clear();
		for(Criteria c: criterias){
			search.addCriteria(c);
		}
		changedCriterias = true;
	}
	
	/**
	 * Define todos os critérios que devem ser aplicados à lista de resposta deste modelo de dados.
	 * 
	 * @param criterias os critérios a serem incluídos.
	 * @throws NoSuchFieldException  caso algum dos critérios repassados inclua campo inexistente no objeto de pesquisa
	 */
	public void setCriterias(Criteria...criterias) throws NoSuchFieldException{
		setCriterias(Arrays.asList(criterias));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ajax4jsf.model.ExtendedDataModel#walk(javax.faces.context.FacesContext
	 * , org.ajax4jsf.model.DataVisitor, org.ajax4jsf.model.Range,
	 * java.lang.Object)
	 */
	@Override
	public void walk(FacesContext ctx, DataVisitor visitor, Range range, Object argument) throws IOException {
		int first = ((SequenceRange) range).getFirstRow();
		int maxRows = ((SequenceRange) range).getRows();
		boolean redo = this.first == null || this.max == null || this.first != first || this.max != maxRows || getRefreshPage();
		try {
			search.setFirst(first);
			search.setMax(maxRows);
			if(redo){
				page = retreiver.list(search);
				this.first = first;
				this.max = maxRows;
				setRefreshPage(Boolean.FALSE);
			}
			for(E e: page){
				current = e;
				visitor.process(ctx, retreiver.getId(e), argument);
			}
		} catch (Exception e) {
			e.printStackTrace();
			FacesMessage msg = new FacesMessage(String.format("Erro ao tentar recuperar os registros: %s", e.getLocalizedMessage()));
			context.addMessage(null, msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		if(count == null || changedCriterias || refreshPage){
			updateRowCount();
		}
		return count.intValue();
	}
	
	/**
	 * Atualiza o número de objetos que satisfazem os critérios de pesquisa. Caso haja algum erro na atualização,
	 * será enfileirada uma mensagem de erro no FacesContext.
	 * 
	 */
	private void updateRowCount(){
		try {
			count = retreiver.count(search);
			changedCriterias = false;
		} catch (Exception e) {
			count = 0L;
			FacesMessage msg = new FacesMessage(String.format("Erro ao tentar recuperar a quantidade de registros: %s", e.getLocalizedMessage()));
			context.addMessage(null, msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#getRowData()
	 */
	@Override
	public Object getRowData() {
		if(id == null){
			return null;
		} else {
			if (current != null && ! retreiver.getId(current).equals(id)){
				try {
					current = retreiver.findById(id);
				} catch (Exception e) {
					current = null;
				}
			}
		}
		return current;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
	 */
	@Override
	public Object getRowKey() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.model.ExtendedDataModel#setRowKey(java.lang.Object)
	 */
	@Override
	public void setRowKey(Object id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.model.SerializableDataModel#update()
	 */
	@Override
	public void update() {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#getRowIndex()
	 */
	@Override
	public int getRowIndex() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#isRowAvailable()
	 */
	@Override
	public boolean isRowAvailable() {
		return id != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#getWrappedData()
	 */
	@Override
	public Object getWrappedData() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#setRowIndex(int)
	 */
	@Override
	public void setRowIndex(int arg0) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#setWrappedData(java.lang.Object)
	 */
	@Override
	public void setWrappedData(Object arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Permite indicar que a lista de resposta deve ser ordenada pelo campo dado.
	 * 
	 * @param field o campo que deve orientar a ordenação
	 * @param order indicação de que a ordem deve ser ascedente ou descendente
	 */
	public void addOrder(String field, Order order) {
		search.addOrder(field, order);
		changedCriterias = true;
	}
	
	public void setGroupBy(String groupBy) {
		search.setGroupBy(groupBy);
		changedCriterias = true;
	}

	public List<E> getPage() {
		return page;
	}

	/**
	 * Método que será chamado quando um sortBy for selecionado no rich:dataTable.
	 * 
	 * Inclui a operação de ordenação presente no {@link sortFields}  no objeto {@link Search}.
	 */
	@Override
	public void modify(List<FilterField> filterFields, List<SortField2> sortFields) {
		
		if(sortFields != null && !sortFields.isEmpty()){
			search.getOrders().clear();
		}
		
		for(SortField2 sortField :sortFields){
			
			String expressionStr = null;
	        if (!sortField.getExpression().isLiteralText()){
	        	expressionStr = sortField.getExpression().getExpressionString().replaceAll("[#|$]{1}\\{.*?\\.", "").replaceAll("\\}", "");
	        	
	        	Order order = null;
	        	if(sortField.getOrdering() == Ordering.DESCENDING){
	        		order = Order.DESC;
	        	}else{
	        		order = Order.ASC;
	        	}
	        	search.addOrder(expressionStr, order);
	        	setRefreshPage(Boolean.TRUE);
	        }
		}
	}
	
}