 package br.com.infox.cliente.component.suggest;
 
 import org.jboss.seam.annotations.Name;
 
 import br.com.infox.component.suggest.AbstractSuggestBean;
 import br.com.itx.util.ComponentUtil;
 import br.jus.cnj.pje.view.PessoaPushAction;
 import br.jus.pje.nucleo.entidades.Cep;
 
 @Name(PessoaPushCepSuggestBean.NAME)
 public class PessoaPushCepSuggestBean extends AbstractSuggestBean<Cep> {
 
 	public static final String NAME = "pessoaPushCepSuggestBean";
 	private static final long serialVersionUID = 1L;
 	private String defaultValue;
 
 	@Override
 	public String getEjbql() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT o FROM Cep AS o ");
 		sb.append("WHERE o.ativo = TRUE ");
 		sb.append("AND o.numeroCep = :" + INPUT_PARAMETER);
 		return sb.toString();
 	}
 
 	@Override
 	public String getDefaultValue() {
 		if (defaultValue == null) {
 			if (getAction() != null && getAction().getInstance() != null) {
 				return getAction().getInstance().getCep();
 			} else {
 				return "";
 			}
 		} else {
 			return defaultValue;
 		}
 	}
 
 	@Override
 	public void setDefaultValue(String defaultValue) {
 		this.defaultValue = defaultValue;
 	}
 
 	private PessoaPushAction getAction() {
 		PessoaPushAction action = ComponentUtil.getComponent(PessoaPushAction.class);
 		return action;
 	}
 
 }