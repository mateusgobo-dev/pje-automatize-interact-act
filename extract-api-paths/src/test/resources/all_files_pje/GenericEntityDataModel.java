/**
 * 
 */
package br.jus.cnj.pje.view;

import java.io.IOException;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SerializableDataModel;

/**
 * @author cristof
 * 
 */
public abstract class GenericEntityDataModel extends SerializableDataModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9680004819858360L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.model.SerializableDataModel#update()
	 */
	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
	 */
	@Override
	public Object getRowKey() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.model.ExtendedDataModel#setRowKey(java.lang.Object)
	 */
	@Override
	public void setRowKey(Object arg0) {
		// TODO Auto-generated method stub

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
	public void walk(FacesContext arg0, DataVisitor arg1, Range arg2, Object arg3) throws IOException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#getRowData()
	 */
	@Override
	public Object getRowData() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#getRowIndex()
	 */
	@Override
	public int getRowIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#getWrappedData()
	 */
	@Override
	public Object getWrappedData() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#isRowAvailable()
	 */
	@Override
	public boolean isRowAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#setRowIndex(int)
	 */
	@Override
	public void setRowIndex(int arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#setWrappedData(java.lang.Object)
	 */
	@Override
	public void setWrappedData(Object arg0) {
		// TODO Auto-generated method stub

	}

}
