/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ModeloDocumentoCK;


/**
 * @author cristof
 * 
 */
@Name(ModeloDocumentoCKDAO.NAME)
public class ModeloDocumentoCKDAO extends BaseDAO<ModeloDocumentoCK> {

	public static final String NAME = "modeloDocumentoCKDAO";

	@Override
	public Integer getId(ModeloDocumentoCK e) {
		// TODO Auto-generated method stub
		return e.getIdModeloDocumento();
	}
}
