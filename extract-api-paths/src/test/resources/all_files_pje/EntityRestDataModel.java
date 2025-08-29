package br.jus.cnj.pje.view;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.ws.rs.ClientErrorException;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.ExtendedDataModel;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;

import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.pje.nucleo.dto.EntityPageDTO;
import br.jus.pje.nucleo.dto.PJeServiceApiDTO;

public class EntityRestDataModel<E extends PJeServiceApiDTO> extends ExtendedDataModel{

	private EntityPageDTO<E> page;
	
	private BaseRestClient<E> client;
	
	private Integer rowKey;
	
	private FacesContext context;
	
	private Integer count;
	
	protected E current;
	
	protected E searchTemplate;
	
	public EntityRestDataModel(EntityPageDTO<E> page, FacesContext context, BaseRestClient<E> client, E searchTemplate) {
		super();
		this.context = context;
		this.page = page;
		this.client = client;
		this.searchTemplate = searchTemplate;
	}

	public EntityRestDataModel() {
		super();
	}

	@Override
	public void setRowKey(Object key) {
		this.rowKey = (Integer)key;		
	}

	@Override
	public Object getRowKey() {
		return this.rowKey;
	}

	@Override
	public void walk(FacesContext context, DataVisitor visitor, Range range, Object argument) throws IOException {
		int firstRow = ((SequenceRange) range).getFirstRow();
		int numberOfLines = ((SequenceRange) range).getRows();
		int totalPages = 0;
		int pageIndex = 0;
		
		if(this.getRowCount() != 0){
			totalPages = (int) Math.ceil((double)this.getRowCount()/ numberOfLines);
			pageIndex = (firstRow * totalPages) / this.getRowCount();
		}
		
		try{
			this.doSearch(pageIndex, numberOfLines);
			
			for(E e: this.page.getContent()){
				current = e;
				visitor.process(context, e.getId(), argument);
			}
		} catch (Exception e) {
			e.printStackTrace();
			FacesMessage msg = new FacesMessage(String.format("Erro ao tentar recuperar os registros: %s", e.getLocalizedMessage()));
			this.context.addMessage(null, msg);
		}
		
		
	}
	
	private void doSearch(int pageIndex, int numberOfLines){
		if(this.searchTemplate == null) {
			this.page = this.client.searchResources(pageIndex, numberOfLines);
		} else {
			this.page = this.client.searchResources(pageIndex, numberOfLines, this.searchTemplate);
		}			
	}

	@Override
	public boolean isRowAvailable() {
		return true;
	}

	@Override
	public int getRowCount() {
		try{
			if(this.page == null && count == null){
				if(this.searchTemplate == null){
					count = this.client.countResources().intValue();
				} else {
					count = this.client.countResources(this.searchTemplate).intValue();
				}			
			} else if(this.page != null && count != null){
				count = this.page.getTotalElements();
			}
		} catch (ClientErrorException e) {
			e.printStackTrace();
			FacesMessage msg = new FacesMessage(String.format("Erro ao tentar recuperar os registros: %s", e.getLocalizedMessage()));
			context.addMessage(null, msg);
			count = 0;
		}
		//return this.page == null? 0 : this.page.getTotalElements();
		return count;
	}

	@Override
	public Object getRowData() {
		if(current != null && !current.getId().equals(this.getRowKey())){
			try {
				return this.client.getResourceById(rowKey);
			} catch (ClientErrorException | PJeException e) {
				e.printStackTrace();
			}
		}
		return current;
	}

	@Override
	public int getRowIndex() {
		return rowKey == null ? 0 : rowKey;
	}

	@Override
	public void setRowIndex(int rowIndex) {
		throw new UnsupportedOperationException(); 		
	}

	@Override
	public Object getWrappedData() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setWrappedData(Object data) {
		throw new UnsupportedOperationException();
	}

}
