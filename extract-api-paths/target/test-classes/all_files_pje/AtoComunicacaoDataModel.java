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
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import br.jus.cnj.pje.business.dao.ProcessoParteExpedienteDAO.CriterioPesquisa;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;

/**
 * @author cristof
 * 
 */
public class AtoComunicacaoDataModel extends SerializableDataModel {

	private static final long serialVersionUID = -8872898734083500610L;
	private Integer currentAtoId;
	private AtoComunicacaoService atoComunicacaoService;
	private Map<Integer, ProcessoParteExpediente> dados = new HashMap<Integer, ProcessoParteExpediente>();
	private List<Integer> ids = new ArrayList<Integer>();
	private Pessoa destinatario;
	private Integer numeroRegistros;
	private CriterioPesquisa criterio;
	private Map<Integer, List<ProcessoParteExpediente>> pages = new HashMap<Integer, List<ProcessoParteExpediente>>();
	private Log logger = Logging.getLog(AtoComunicacaoDataModel.class);
	
	/**
	 * @return the criterio
	 */
	public CriterioPesquisa getCriterio() {
		return criterio;
	}

	/**
	 * @param criterio
	 *            the criterio to set
	 */
	public void setCriterio(CriterioPesquisa criterio) {
		this.criterio = criterio;
	}

	/**
	 * @param atoComunicacaoService
	 *            the AtoComunicacaoService to set
	 */
	public void setAtoComunicacaoService(AtoComunicacaoService atoComunicacaoService) {
		this.atoComunicacaoService = atoComunicacaoService;
	}

	/**
	 * @param destinatario
	 *            the destinatario to set
	 */
	public void setDestinatario(Pessoa destinatario) {
		this.destinatario = destinatario;
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
		return this.currentAtoId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.model.ExtendedDataModel#setRowKey(java.lang.Object)
	 */
	@Override
	public void setRowKey(Object arg0) {
		this.currentAtoId = (Integer) arg0;
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
		List<ProcessoParteExpediente> expedientes = pages.get(firstRow);
		if (expedientes == null) {
			try {
				expedientes = atoComunicacaoService.getAtosComunicacao(destinatario, firstRow, numberOfRows, criterio);
			} catch (PJeDAOException e) {
				logger.error("Erro: {0}", e.getLocalizedMessage());
			} catch (PJeBusinessException e) {
				logger.error("Erro: {0}", e.getLocalizedMessage());
			}
			pages.put(firstRow, expedientes);
		}
		for (ProcessoParteExpediente exp : expedientes) {
			int id = exp.getIdProcessoParteExpediente();
			dados.put(id, exp);
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
			try {
				numeroRegistros = (int) atoComunicacaoService.contagemAtos(destinatario, criterio);
			} catch (PJeBusinessException e) {
				logger.error("Erro: {0}", e.getLocalizedMessage());
				numeroRegistros = -1;
			}
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
		if (currentAtoId == null) {
			return null;
		} else {
			ProcessoParteExpediente exp = dados.get(currentAtoId);
			if (exp == null) {
				try {
					exp = atoComunicacaoService.getAtoPessoal(currentAtoId);
					dados.put(currentAtoId, exp);
				} catch (PJeBusinessException e) {
					logger.error("Erro: {0}", e.getLocalizedMessage());
				} catch (PJeDAOException e) {
					logger.error("Erro: {0}", e.getLocalizedMessage());
				}
			}
			return exp;
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
		if (currentAtoId == null) {
			return false;
		} else {
			try {
				return atoComunicacaoService.getAtoComunicacao(currentAtoId) != null;
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

}
