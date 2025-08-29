package br.com.infox.ibpm.component.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Bean que representa a abstração de um complemento da modelagem de movimentos
 * com complementos. O movimentoBean possui uma lista de complementoBean e cada
 * elemento desta representa um complemento.
 * 
 * Caso o complemento seja múltiplo, os valores escolhidos pelo usuário serão
 * armazenados em valorComplementoBeanList.
 * 
 * Caso o complemento seja não-múltiplo, o valor selecionado pelo usuário será
 * armazenado em valorComplementoBeanList[0]
 * 
 * @author David, Kelly
 */
public class ComplementoBean implements Serializable {

	private static final long serialVersionUID = 7256605365049997340L;
	private String label;
	private Boolean multiplo;
	private String validacao;
	private String mensagemErro;
	private ComplementoBean.TipoComplementoEnum tipoComplementoEnum;
	private List<ValorComplementoBean> elementosComboboxList = new ArrayList<ValorComplementoBean>();
	private List<ValorComplementoBean> valorComplementoBeanList = new ArrayList<ValorComplementoBean>();
	private String glossario = "";
	private Long idTipoComplemento;
	private Boolean temMascara;
	private String mascara;
	
	public boolean semComplementos(){
		return (this.getValorComplementoBeanList().size() <= 0);
	}
		
	public Long getIdTipoComplemento() {
		return idTipoComplemento;
	}

	public void setIdTipoComplemento(Long idTipoComplemento) {
		this.idTipoComplemento = idTipoComplemento;
	}

	public String getValidacao() {
		return validacao;
	}

	public void setValidacao(String validacao) {
		this.validacao = validacao;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}

	public void setMensagemErro(String mensagemErro) {
		this.mensagemErro = mensagemErro;
	}

	public String getGlossario() {
		return glossario;
	}

	public void setGlossario(String glossario) {
		this.glossario = glossario;
	}

	public List<ValorComplementoBean> getElementosComboboxList() {
		return elementosComboboxList;
	}

	public void setElementosComboboxList(List<ValorComplementoBean> elementosComboboxList) {
		this.elementosComboboxList = elementosComboboxList;
	}

	public Boolean getMultiplo() {
		return multiplo;
	}

	public void setMultiplo(Boolean multiplo) {
		this.multiplo = multiplo;
	}

	public List<ValorComplementoBean> getValorComplementoBeanList() {
		return valorComplementoBeanList;
	}

	public void setValorComplementoBeanList(List<ValorComplementoBean> valorComplementoBeanList) {
		this.valorComplementoBeanList = valorComplementoBeanList;
	}

	public ComplementoBean.TipoComplementoEnum getTipoComplementoEnum() {
		return tipoComplementoEnum;
	}

	public void setTipoComplementoEnum(ComplementoBean.TipoComplementoEnum tipoComplementoEnum) {
		this.tipoComplementoEnum = tipoComplementoEnum;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String adicionarValorComplementoBean() {
		boolean found;
		if (this.getValorComplementoBeanList().size() < this.elementosComboboxList.size()){	
			for (int i=0;i <= elementosComboboxList.size(); i++){
				found=false;
				for (ValorComplementoBean complemento: this.getValorComplementoBeanList()){
	              found = (complemento.getValor().equals(elementosComboboxList.get(i).getValor()));
	              if (found) 
	               break;
				}			
				if (! found) {
					ValorComplementoBean temp = new ValorComplementoBean();
					temp.setValorComplementoBean(elementosComboboxList.get(i));
					this.getValorComplementoBeanList().add(temp);
					break;
				}
			 }    
		} 
		return null;
	}
	
	public String removerTodosValoresComplementoBean(){
		this.setValorComplementoBeanList(this.getValorComplementoBeanList().subList(0, 1));
		return null;
	}
	
	public String adicionarTodosValoresComplementoBean(){
		boolean found;
		if (getValorComplementoBeanList().size() < elementosComboboxList.size()){
		 for (int i=0;i < elementosComboboxList.size(); i++){
			found=false;
			for (ValorComplementoBean complemento: this.getValorComplementoBeanList()){
              found = (complemento.getValor().equals(elementosComboboxList.get(i).getValor()));
              if (found) 
               break;
			}			
			if (! found) {
				ValorComplementoBean temp = new ValorComplementoBean();
				temp.setValorComplementoBean(elementosComboboxList.get(i));
				this.getValorComplementoBeanList().add(temp);
			}
		 }    
		}
		return null;
	}
	
	public String removerValorComplementoBean(Integer indexValorComplementoBean) {
		if (this.getValorComplementoBeanList().size() > 0) {
			this.getValorComplementoBeanList().remove((int) indexValorComplementoBean);
		}
		return null;
	}

	public Boolean getRequired() {
		// Se parametro performValidation estiver sendo passado, então
		// componente não será validado.
		// Uso pratico, para botoes com <a:actionparam name="performValidation"
		// value="false" />
		// não irá validar os componentes de complemento.
		// Usado em ocasiões como o de adicionar novo complemento múltiplo
		if ("false".equalsIgnoreCase(((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest()).getParameter("performValidation"))) {
			return false;
		} else {
			return true;
		}
	}

	public enum TipoComplementoEnum implements Serializable {
		LIVRE("/WEB-INF/xhtml/components/lancadorMovimentos/tipoComplementoInputText.xhtml"), 
		DINAMICO("/WEB-INF/xhtml/components/lancadorMovimentos/tipoComplementoComboBox.xhtml"), 
		COM_DOMINIO("/WEB-INF/xhtml/components/lancadorMovimentos/tipoComplementoComboBox.xhtml");
		private String xhtml;

		TipoComplementoEnum(String xhtml) {
			this.xhtml = xhtml;
		}

		public String getXhtml() {
			return xhtml;
		}

		@Override
		public String toString() {
			return this.xhtml;
		}
	}

	/**
	 * [PJEII-2393]
	 * Verifica se o tipo do complemento é Dinâmico.
	 * @return true, caso seja dinâmico.
	 */
	public boolean isTipoDinamico() {
		return this.getTipoComplementoEnum() == ComplementoBean.TipoComplementoEnum.DINAMICO;
	}

	public Boolean getTemMascara() {
		return temMascara;
	}

	public void setTemMascara(Boolean temMascara) {
		this.temMascara = temMascara;
	}

	public String getMascara() {
		return mascara;
	}

	public void setMascara(String mascara) {
		this.mascara = mascara;
	}

}
