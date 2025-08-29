package br.com.jt.pje.action;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.exceptions.NegocioException;
import br.com.infox.view.GenericCrudAction;
import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.list.SalarioMinimoList;
import br.com.jt.pje.manager.SalarioMinimoManager;
import br.jus.cnj.pje.interceptor.FacesTransactionEventsInterceptor;
import br.jus.cnj.pje.interceptor.IgnoreFacesTransactionMessageError;
import br.jus.pje.jt.entidades.SalarioMinimo;

@AutoCreate
@Scope(ScopeType.CONVERSATION)
@Name(SalarioMinimoAction.NAME)
@FacesTransactionEventsInterceptor
public class SalarioMinimoAction extends GenericCrudAction<SalarioMinimo>{

	private static final long serialVersionUID = 7779834046421013677L;

	public static final String NAME = "salarioMinimoAction";
	
	private boolean vigente;
	/**
	 * <html>
	 * 	Se o valor for TRUE &eacute; persist, se o valor for FALSE &eacute; update
	 * </html>
	 */
	private Boolean persistUpdate;
	
	@In
	private SalarioMinimoManager salarioMinimoManager;
	
	private SalarioMinimoList salarioMinimoList;
	
	private List<SalarioMinimo> lista = getSalarioMinimoList().list();
	
	
	@Override
	public void setIdInstance(Integer id) {
		super.setIdInstance(id);
		setTab("form");
	}
	
	@Override
	public void onClickSearchTab() {
		newInstance();
		vigente = false;
		persistUpdate = null;
		pesquisar();
	}
	
	@IgnoreFacesTransactionMessageError
	public void persist(){
		if(!beforPersistUpdate()){
			return;
		}	
		try {
			salarioMinimoManager.persist(getInstance());
			newInstance();
			vigente = false;
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Salario mínimo inserido com sucesso.");
		} catch (NegocioException e) {
			vigente = false;
			String mensagem = e.getMensagem();
			if("vigente".equals(mensagem)){
				vigente = true;
				persistUpdate = true;
				mensagem = "Já existe um salário mínimo em vigência. Deseja fechá-lo?";
			}
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, mensagem);
		}
	}
	
	@IgnoreFacesTransactionMessageError
	public void update(){
		if(!beforPersistUpdate()){
			return;
		}

		try {
			salarioMinimoManager.update(getInstance());
			vigente = false;
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Salario minimo alterado com sucesso.");
		} catch (NegocioException e) {
			vigente = false;
			String mensagem = e.getMensagem();
			if("vigente".equals(mensagem)){
				vigente = true;
				persistUpdate = false;
				mensagem = "Já existe um salário mínimo em vigência. Deseja fechá-lo?";
			}
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, mensagem);
		}
	}
	
	private boolean beforPersistUpdate(){
		if(getInstance().getDataInicioVigencia() != null &&
				getInstance().getDataFimVigencia() != null &&
				getInstance().getDataInicioVigencia().after(getInstance().getDataFimVigencia())){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "A data final deve ser maior que a inicial.");
			return false;
		}
		return true;
	}
	
	public void remove(SalarioMinimo salarioMinimo){
		try {
			salarioMinimoManager.remove(salarioMinimo);
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Salario minimo excluido com sucesso.");
		} catch (NegocioException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, e.getMensagem());
		}
	}
	
	@IgnoreFacesTransactionMessageError
	public void fecharSalarioVigente(){
		try {
			salarioMinimoManager.fecharSalarioVigente(getInstance().getDataInicioVigencia());
			continuarOperacao();
		} catch (NegocioException e) {
			vigente = false;
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, e.getMensagem());
		}
	}
	
	public void pesquisar(){
		if(getSalarioMinimoList().getEntity().getDataInicioVigencia() != null &&
				getSalarioMinimoList().getEntity().getDataFimVigencia() != null &&
				getSalarioMinimoList().getEntity().getDataInicioVigencia().after(getSalarioMinimoList().getEntity().getDataFimVigencia())){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "A data final deve ser maior que a inicial.");
			return;
		}
		if(getSalarioMinimoList().getValorInicio() != null &&
				getSalarioMinimoList().getValorFim() != null &&
				getSalarioMinimoList().getValorInicio().doubleValue() > getSalarioMinimoList().getValorFim().doubleValue()){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "O valor inicial do salário deve ser menor ou igual ao valor final.");
			return;
		}
		lista = getSalarioMinimoList().list();
	}
	
	public void limparPesquisa(){
		getSalarioMinimoList().newInstance();
		lista = getSalarioMinimoList().list();
	}

	private void continuarOperacao() {
		if(persistUpdate){
			persist();
		}else{
			update();
		}
	}

	public boolean isVigente() {
		return vigente;
	}

	public void setVigente(boolean vigente) {
		this.vigente = vigente;
	}

	public SalarioMinimoList getSalarioMinimoList() {
		if(salarioMinimoList == null){
			salarioMinimoList = ComponentUtil.getComponent(SalarioMinimoList.NAME);
		}
		return salarioMinimoList;
	}

	public void setSalarioMinimoList(SalarioMinimoList salarioMinimoList) {
		this.salarioMinimoList = salarioMinimoList;
	}

	public List<SalarioMinimo> getLista() {
		return lista;
	}

	public void setLista(List<SalarioMinimo> lista) {
		this.lista = lista;
	}

}
