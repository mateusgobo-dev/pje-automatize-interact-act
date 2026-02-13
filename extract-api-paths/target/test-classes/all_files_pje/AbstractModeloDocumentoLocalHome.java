/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.cliente.home;

import org.jboss.seam.Component;

import br.com.infox.ibpm.home.TipoModeloDocumentoHome;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;

public abstract class AbstractModeloDocumentoLocalHome<T> extends AbstractHome<ModeloDocumentoLocal> {

	private static final long serialVersionUID = 1L;

	public void setModeloDocumentoLocalIdModeloDocumentoLocal(Integer id) {
		setId(id);
	}

	public Integer getModeloDocumentoLocalIdModeloDocumentoLocal() {
		return (Integer) getId();
	}

	@Override
	protected ModeloDocumentoLocal createInstance() {
		ModeloDocumentoLocal modeloDocumentoLocal = new ModeloDocumentoLocal();
		TipoModeloDocumentoHome tipoModeloDocumentoHome = (TipoModeloDocumentoHome) Component.getInstance(
				"tipoModeloDocumentoHome", false);
		if (tipoModeloDocumentoHome != null) {
			modeloDocumentoLocal.setTipoModeloDocumento(tipoModeloDocumentoHome.getDefinedInstance());
		}
		return modeloDocumentoLocal;
	}

	@Override
	public String remove(ModeloDocumentoLocal obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("modeloDocumentoLocalGrid");
		return ret;
	}

}