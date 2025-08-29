package br.jus.cnj.pje.nucleo.manager;


import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.business.dao.ModeloDocumentoCKDAO;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoCK;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.TipoEditorEnum;

@Name("modeloDocumentoCKManager")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ModeloDocumentoCKManager extends BaseManager<ModeloDocumentoCK>{

	@In(create = true)
	private ModeloDocumentoCKDAO modeloDocumentoCKDAO;
	
	@Override
	protected ModeloDocumentoCKDAO getDAO(){
		return this.modeloDocumentoCKDAO;
	}
	
	public ModeloDocumentoCK montaModeloDocumentoCK(ModeloDocumentoLocal modeloOrigem) {
		ModeloDocumentoCK modeloCK = new ModeloDocumentoCK();
		modeloCK.setAtivo(modeloOrigem.getAtivo());
		modeloCK.setTipoModeloDocumento(modeloOrigem.getTipoModeloDocumento());
		modeloCK.setModeloDocumento(modeloOrigem.getModeloDocumento());
		modeloCK.setTituloModeloDocumento(modeloOrigem.getTituloModeloDocumento());
		modeloCK.setLocalizacao(modeloOrigem.getLocalizacao());
		modeloCK.setTipoProcessoDocumento(modeloOrigem.getTipoProcessoDocumento());
		modeloCK.setTipoEditor(TipoEditorEnum.C);
		return modeloCK;
	}
	
	public ModeloDocumentoCK montaModeloDocumentoCK(TipoProcessoDocumento tipoProcessoDocumento) {
		ModeloDocumentoCK modeloCK = new ModeloDocumentoCK();
		modeloCK.setAtivo(true);
		modeloCK.setTipoModeloDocumento(ParametroUtil.instance().getTipoModeloDocumentoCKEditorRaiz());
		modeloCK.setModeloDocumento(" ");
		modeloCK.setTituloModeloDocumento("Modelo de documento " + tipoProcessoDocumento.getTipoProcessoDocumento());
		modeloCK.setLocalizacao(ParametroUtil.instance().getLocalizacaoTribunal());
		modeloCK.setTipoProcessoDocumento(tipoProcessoDocumento);
		modeloCK.setTipoEditor(TipoEditorEnum.C);
		return modeloCK;
	}
}