package br.jus.csjt.pje.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.component.tree.ValorComplementoBean;

/**
 * Converter do JSF do valorComplementoBean
 * 
 * Estes valores serão usados para preencher a comboBox dos complementos
 * (dinâmico e com domínio) dos movimentos.
 * 
 * @author David, Kelly
 * 
 */
@org.jboss.seam.annotations.faces.Converter
@Name("valorComplementoBeanConverter")
@BypassInterceptors
public class ValorComplementoBeanConverter implements Converter {

	private final String DELIMITADOR = "###";

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
		String[] values = value.split(DELIMITADOR);
		ValorComplementoBean vcb = new ValorComplementoBean();
		vcb.setCodigo(values[0]);
		vcb.setValor(values[1]);
		return vcb;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
		ValorComplementoBean vcb = (ValorComplementoBean) value;
		return vcb.getCodigo() + DELIMITADOR + vcb.getValor();
	}

}