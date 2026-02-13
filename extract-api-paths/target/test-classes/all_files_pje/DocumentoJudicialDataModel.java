/**
 * 
 */
package br.jus.cnj.pje.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.ajax4jsf.model.SerializableDataModel;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.TipoOrigemAcaoEnum;

/**
 * @author cristof
 * 
 */
public class DocumentoJudicialDataModel extends SerializableDataModel {

	private static final long serialVersionUID = -8872898734083500610L;
	private Integer currentDocumentId;
	private DocumentoJudicialService documentoJudicialService;
	private Map<Integer, ProcessoDocumento> dados = new HashMap<Integer, ProcessoDocumento>();
	private List<Integer> ids = new ArrayList<Integer>();
	private ProcessoTrf processoJudicial;
	private Integer numeroRegistros;
	private Map<Integer, List<ProcessoDocumento>> pages = new HashMap<Integer, List<ProcessoDocumento>>();
	private Boolean ordemDecrescente = true;
	private Boolean mostrarPdf = true;
	private boolean incluirComAssinaturaInvalidada = false;
	private boolean incluirDocumentoPeticaoInicial = true;
	private boolean soDocumentosJuntados = false;
	private boolean apenasAtosProferidos = false;
	private TipoOrigemAcaoEnum tipoOrigemAcao;

	/**
	 * @param documentoJudicialService
	 *            the documentoJudicialService to set
	 */
	public void setDocumentoJudicialService(DocumentoJudicialService documentoJudicialService) {
		this.documentoJudicialService = documentoJudicialService;
	}

	/**
	 * @param processoJudicial
	 *            the processoJudicial to set
	 */
	public void setProcessoJudicial(ProcessoTrf processoJudicial) {
		this.processoJudicial = processoJudicial;
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
	 * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
	 */
	@Override
	public Object getRowKey() {
		return this.currentDocumentId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.model.ExtendedDataModel#setRowKey(java.lang.Object)
	 */
	@Override
	public void setRowKey(Object arg0) {
		this.currentDocumentId = (Integer) arg0;
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
	public void walk(FacesContext context, DataVisitor visitor, Range range, Object argument) throws IOException {
		int firstRow = ((SequenceRange) range).getFirstRow();
		int numberOfRows = ((SequenceRange) range).getRows();
		if (ids == null) {
			ids = new ArrayList<Integer>();
		}
		List<ProcessoDocumento> docs = pages.get(firstRow);
		if (docs == null) {
			docs = documentoJudicialService.getDocumentos(processoJudicial, firstRow, numberOfRows, this.getOrdemDecrescente(), 
						this.getMostrarPdf(), isIncluirComAssinaturaInvalidada(), 
						this.isIncluirDocumentoPeticaoInicial(), this.isSoDocumentosJuntados(), false, this.isApenasAtosProferidos(), this.getTipoOrigemAcao());
			pages.put(firstRow, docs);
		}
		for (ProcessoDocumento doc : docs) {
			int id = doc.getIdProcessoDocumento();
			dados.put(id, doc);
			ids.add(id);
			visitor.process(context, id, argument);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		if (numeroRegistros == null) {
			numeroRegistros = documentoJudicialService.getCountDocumentos(processoJudicial, this.getMostrarPdf(), isIncluirComAssinaturaInvalidada(), this.isIncluirDocumentoPeticaoInicial(), this.isSoDocumentosJuntados(), this.isApenasAtosProferidos(), this.getTipoOrigemAcao());
		}
		return numeroRegistros;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#getRowData()
	 */
	@Override
	public Object getRowData() {
		if (currentDocumentId == null) {
			return null;
		} else {
			ProcessoDocumento pd = dados.get(currentDocumentId);
			if (pd == null) {
				try {
					pd = documentoJudicialService.getDocumento(currentDocumentId);
					dados.put(currentDocumentId, pd);
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				} catch (PJeDAOException e) {
					e.printStackTrace();
				}
			}
			return pd;
		}
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
	 * @see javax.faces.model.DataModel#getWrappedData()
	 */
	@Override
	public Object getWrappedData() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#isRowAvailable()
	 */
	@Override
	public boolean isRowAvailable() {
		if (currentDocumentId == null) {
			return false;
		} else {
			try {
				return documentoJudicialService.getDocumento(currentDocumentId) != null;
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			} catch (PJeDAOException e) {
				e.printStackTrace();
			}
		}
		return false;
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

	@Override
	public SerializableDataModel getSerializableModel(Range range) {
		if (ids != null) {
			return this;
		} else {
			return null;
		}
	}

	public Boolean getOrdemDecrescente(){
		return ordemDecrescente;
	}

	public void setOrdemDecrescente(Boolean decrescente){
		this.ordemDecrescente = decrescente;
	}

	public Boolean getMostrarPdf(){
		return mostrarPdf;
	}

	public void setMostrarPdf(Boolean mostrarPdf){
		this.mostrarPdf = mostrarPdf;
	}

	public boolean isIncluirComAssinaturaInvalidada(){
		return incluirComAssinaturaInvalidada;
	}

	public void setIncluirComAssinaturaInvalidada(boolean incluirComAssinaturaInvalidada){
		this.incluirComAssinaturaInvalidada = incluirComAssinaturaInvalidada;
	}

	public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}

	public boolean isIncluirDocumentoPeticaoInicial() {
	  	return incluirDocumentoPeticaoInicial;
	}

	public void setIncluirDocumentoPeticaoInicial(
			boolean incluirDocumentoPeticaoInicial) {
		this.incluirDocumentoPeticaoInicial = incluirDocumentoPeticaoInicial;
	}
	
	public boolean isSoDocumentosJuntados() {
	  	return soDocumentosJuntados;
	}

	public void setSoDocumentosJuntados(boolean soDocumentosJuntados) {
		this.soDocumentosJuntados = soDocumentosJuntados;
	}

	public boolean isApenasAtosProferidos() {
		return apenasAtosProferidos;
	}

	public void setApenasAtosProferidos(boolean apenasAtosProferidos) {
		this.apenasAtosProferidos = apenasAtosProferidos;
	}

	public TipoOrigemAcaoEnum getTipoOrigemAcao() {
		return tipoOrigemAcao;
	}

	public void setTipoOrigemAcao(TipoOrigemAcaoEnum tipoOrigemAcao) {
		this.tipoOrigemAcao = tipoOrigemAcao;
	}
}
