/**
 * CNJ - Conselho Nacional de Justiça
 *
 * Data: 29/04/2016
 */
package br.com.infox.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectMany;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ui.component.html.HtmlSelectItems;

import br.com.itx.util.ReflectionsUtil;

/**
 * JSF converter uso nas combos, radios e checkboxes. O conversor consiste na
 * recuperacao do objeto selecionado na combo do próprio objeto da combo, assim é
 *  possivel evitar a ida ao banco de dados para recupera o objeto selecionado.
 * <br/>
 * Exemplo:<br/>
 * <code>
 * &lt;h:selectOneMenu id="#{id_input_combo}" value="#{value}"/&gt;<br/>
 * 		&lt;s:selectItems <br/>
 * 				value="#{values}"<br/> 
 * 				var="entidadeSelectOneMenu"<br/>
 *                 label="#{entidadeSelectOneMenu[atributoLabel]}"<br/>
 *                 noSelectionLabel="#{label['selecione']}"/&gt;<br/>
 * 		&lt;pje:select-converter atributoValue="#{atributoValue}" atributoLabel="#{atributoLabel}"/&gt;<br/>
 * &lt;h:selectOneMenu/&gt;
 * </code>
 * @author Adriano Pamplona
 */
@SuppressWarnings("unchecked")
public class SelectConverter extends StateHolderAbstrato implements Converter {

	private String	atributoValue;
	private String	atributoLabel;

	/**
	 * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent, java.lang.String)
	 */
	@Override
	public Object getAsObject(FacesContext facesContext,
			UIComponent uiComponent, String valor) {
		Object resultado = null;

		if (isSelect(uiComponent)) {
			UIInput select = (UIInput) uiComponent;
			Iterator<SelectItem> iterator = getSelectItemsIterator(select);
			while (iterator.hasNext() && !isReferencia(resultado)) {
				SelectItem selectItem = iterator.next();
				Object objeto = selectItem.getValue();
				String id = getValorAtributoId(objeto);

				if (StringUtils.equals(valor, id)) {
					resultado = objeto;
				}
			}
		}
		return resultado;
	}

	/**
	 * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent, java.lang.Object)
	 */
	@Override
	public String getAsString(FacesContext facesContext,
			UIComponent uiComponent, Object valor) {
		String resultado = null;
		
		String atributo = getAtributoValue();
		Object object = ReflectionsUtil.getValue(valor, atributo);
		resultado = (object != null ? object.toString() : null);
		return resultado;
	}

	/**
	 * @return the atributoLabel
	 */
	public String getAtributoLabel() {
		return atributoLabel;
	}

	/**
	 * @param atributoLabel
	 *            the atributoLabel to set
	 */
	public void setAtributoLabel(String atributoLabel) {
		this.atributoLabel = atributoLabel;
	}

	/**
	 * @return the atributoValue
	 */
	public String getAtributoValue() {
		if (!isReferencia(atributoValue)) {
			atributoValue = "id";
		}
		return atributoValue;
	}

	/**
	 * @param atributoValue
	 *            the atributoValue to set
	 */
	public void setAtributoValue(String atributoValue) {
		this.atributoValue = atributoValue;
	}

	/**
	 * @param uiComponent
	 * @return true se o componente for um UISelectOne ou UISelectMany.
	 */
	protected Boolean isSelect(UIComponent uiComponent) {
		return isReferencia(uiComponent)
				&& ((uiComponent instanceof UISelectOne) || (uiComponent instanceof UISelectMany));
	}

	/**
	 * Retorna o valor do atributo 'id' do objeto, o valor sera recuperado por
	 * reflexao.
	 * 
	 * @param objeto
	 * @return valor do atributo 'id'.
	 */
	protected String getValorAtributoId(Object objeto) {
		String resultado = null;
		
		if (isReferencia(objeto)) {
			String atributo = getAtributoValue();
			Object id = ReflectionsUtil.getValue(objeto, atributo);
			resultado = (id != null ? id.toString() : null);
		}
		return resultado;
	}

	/**
	 * Retorna o HtmlSelectItems do select.
	 * 
	 * @param select
	 * @return HtmlSelectItems do select.
	 */
	protected HtmlSelectItems getSelectItems(UIInput select) {
		HtmlSelectItems resultado = null;

		Iterator<UIComponent> iterator = novoIterator(select.getChildren());
		while (iterator.hasNext() && !isReferencia(resultado)) {
			UIComponent component = iterator.next();
			if (component instanceof HtmlSelectItems) {
				resultado = (HtmlSelectItems) component;
			}
		}
		return resultado;
	}

	/**
	 * @param select
	 * @return iterator de selectitem do select.
	 */
	protected Iterator<SelectItem> getSelectItemsIterator(UIInput select) {
		Iterator<SelectItem> resultado = new ArrayList<SelectItem>().iterator();
		HtmlSelectItems selectItems = getSelectItems(select);

		if (isReferencia(selectItems) && isReferencia(selectItems.getValue())) {
			List<SelectItem> selectItemsList = (List<SelectItem>) selectItems
					.getValue();
			resultado = novoIterator(selectItemsList);
		}
		return resultado;
	}


	/**
	 * Retorna novo iterator da colecao.
	 * 
	 * @param <T>
	 *            Tipo de retorno
	 * @param colecao
	 *            Collection
	 * @return novo iterator da colecao.
	 */
	protected <T> Iterator<T> novoIterator(Collection<T> colecao) {
		Iterator<T> resultado = null;
		if (colecao != null) {
			resultado = colecao.iterator();
		}
		return resultado;
	}

	/**
	 * @param objetos
	 *            Objetos validados.
	 * @return true se os objetos tiverem referência.
	 */
	protected boolean isReferencia(Object... objetos) {
		boolean res = false;

		if (objetos != null) {
			res = true;
			for (int idx = 0; idx < objetos.length && (res == true); idx++) {
				res = (objetos[idx] != null && !objetos[idx].equals(""));
			}
		}
		return res;
	}
}
