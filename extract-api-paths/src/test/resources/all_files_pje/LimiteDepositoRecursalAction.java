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
import br.com.jt.pje.list.LimiteDepositoRecursalList;
import br.com.jt.pje.manager.LimiteDepositoRecursalManager;
import br.jus.pje.jt.entidades.LimiteDepositoRecursal;

@Name(LimiteDepositoRecursalAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class LimiteDepositoRecursalAction extends GenericCrudAction<LimiteDepositoRecursal>{

	private static final long serialVersionUID = 1329774118611608690L;

	public static final String NAME = "limiteDepositoRecursalAction";
	
	private boolean vigente;
	/**
	 * <html>
	 * 	Se o valor for TRUE &eacute; persist, se o valor for FALSE &eacute; update
	 * </html>
	 */
	private Boolean persistUpdate;
	
	@In
	private LimiteDepositoRecursalManager limiteDepositoRecursalManager;
	
	private LimiteDepositoRecursalList limiteDepositoRecursalList;
	
	private List<LimiteDepositoRecursal> lista = getLimiteDepositoRecursalList().list();
	
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
	
	public void persist(){
		if(!beforPersistUpdate()){
			return;
		}
		try {
			limiteDepositoRecursalManager.persist(getInstance());
			newInstance();
			vigente = false;
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Limite de depósito recursal inserido com sucesso.");
		} catch (NegocioException e) {
			vigente = false;
			String mensagem = e.getMensagem();
			if("vigente".equals(mensagem)){
				vigente = true;
				persistUpdate = true;
				mensagem = "Já existe um limite de depósito recursal em vigência. Deseja fechá-lo?";
			}
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, mensagem);
		}
	}
	
	public void update(){
		if(!beforPersistUpdate()){
			return;
		}
		try {
			limiteDepositoRecursalManager.update(getInstance());
			vigente = false;
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Limite de depósito recursal alterado com sucesso.");
		} catch (NegocioException e) {
			vigente = false;
			String mensagem = e.getMensagem();
			if("vigente".equals(mensagem)){
				vigente = true;
				persistUpdate = false;
				mensagem = "Já existe um limite de depósito recursal em vigência. Deseja fechá-lo?";
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
	
	public void remove(LimiteDepositoRecursal limiteDepositoRecursal){
		try {
			limiteDepositoRecursalManager.remove(limiteDepositoRecursal);
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Limite de depósito recursal excluído com sucesso.");
		} catch (NegocioException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, e.getMensagem());
		}
	}
	
	public void fecharDepositoRecursalVigente(){
		try {
			limiteDepositoRecursalManager.fecharDepositoRecursalVigente(getInstance().getDataInicioVigencia());
			continuarOperacao();
		} catch (NegocioException e) {
			vigente = false;
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, e.getMensagem());
		}
	}
	
	public void pesquisar(){
		if(getLimiteDepositoRecursalList().getEntity().getDataInicioVigencia() != null &&
				getLimiteDepositoRecursalList().getEntity().getDataFimVigencia() != null &&
				getLimiteDepositoRecursalList().getEntity().getDataInicioVigencia().after(getLimiteDepositoRecursalList().getEntity().getDataFimVigencia())){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "A data final deve ser maior que a inicial.");
			return;
		}
		if(getLimiteDepositoRecursalList().getValorInicio1Grau() != null &&
				getLimiteDepositoRecursalList().getValorFim1Grau() != null &&
				getLimiteDepositoRecursalList().getValorInicio1Grau().doubleValue() > getLimiteDepositoRecursalList().getValorFim1Grau().doubleValue()){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "O valor inicial do 1º Grau deve ser menor ou igual ao valor final.");
			return;
		}
		if(getLimiteDepositoRecursalList().getValorInicio2Grau() != null &&
				getLimiteDepositoRecursalList().getValorFim2Grau() != null &&
				getLimiteDepositoRecursalList().getValorInicio2Grau().doubleValue() > getLimiteDepositoRecursalList().getValorFim2Grau().doubleValue()){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "O valor inicial do 2º Grau deve ser menor ou igual ao valor final.");
			return;
		}
		lista = limiteDepositoRecursalList.list();
	}
	
	public void limparPesquisa(){
		getLimiteDepositoRecursalList().newInstance();
		lista = limiteDepositoRecursalList.list();
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

	public LimiteDepositoRecursalList getLimiteDepositoRecursalList() {
		if(limiteDepositoRecursalList == null){
			limiteDepositoRecursalList = ComponentUtil.getComponent(LimiteDepositoRecursalList.NAME);
		}
		return limiteDepositoRecursalList;
	}

	public void setLimiteDepositoRecursalList(LimiteDepositoRecursalList limiteDepositoRecursalList) {
		this.limiteDepositoRecursalList = limiteDepositoRecursalList;
	}

	public List<LimiteDepositoRecursal> getLista() {
		return lista;
	}

	public void setLista(List<LimiteDepositoRecursal> lista) {
		this.lista = lista;
	}

}
