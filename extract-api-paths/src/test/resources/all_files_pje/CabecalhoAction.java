package br.com.jt.pje.action;

import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.editor.list.CabecalhoList;
import br.com.infox.view.GenericCrudAction;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Variavel;
import br.jus.pje.nucleo.entidades.editor.Cabecalho;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumento;

@Name(CabecalhoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate 
public class CabecalhoAction extends GenericCrudAction<Cabecalho>{

	private static final long serialVersionUID = 5126510191651629826L;
	
	public static final String NAME = "cabecalhoAction";
	
	private CabecalhoList cabecalhoList;
	
	private Variavel variavel;
	
	public void inativar(Cabecalho cabecalho){
		List<EstruturaDocumento> lista = validaInativacao();
		if(lista.size() > 0){
			StringBuilder msg = new StringBuilder("Este cabeçalho está sendo utilizado pela(s) estrutura(s) ");
			for(EstruturaDocumento e : lista){
				msg.append(e.getEstruturaDocumento());
				msg.append(", ");
			}
			msg.append("e não poderá ser excluído.");
			FacesMessages.instance().add(Severity.ERROR, msg.toString());
			return;
		}
		cabecalho.setAtivo(false);
		super.update(cabecalho);
		EntityUtil.getEntityManager().flush();
	}
	
	@SuppressWarnings("unchecked")
	private List<EstruturaDocumento> validaInativacao(){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from EstruturaDocumento o ");
		sb.append("where o.ativo = true ");
		sb.append("and o.cabecalho.idCabecalho = :idCabecalho ");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("idCabecalho", getInstance().getIdCabecalho());
		return q.getResultList();
	}
	
	private void persistOuUpdate(char persistOuUpdate){
		if(getInstance().getIdCabecalho() != null && !getInstance().getAtivo()){
			List<EstruturaDocumento> lista = validaInativacao();
			if(lista.size() > 0){
				StringBuilder msg = new StringBuilder("Este cabeçalho está sendo utilizado pela(s) estrutura(s) ");
				for(EstruturaDocumento e : lista){
					msg.append(e.getEstruturaDocumento());
					msg.append(", ");
				}
				msg.append("e não poderá ser excluído.");
				FacesMessages.instance().add(Severity.ERROR, msg.toString());
				return;
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from Cabecalho o ");
		sb.append("where o.idCabecalho <> :idCabecalho ");
		sb.append("and o.cabecalho = :cabecalho ");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		if(getInstance().getIdCabecalho() != null){
			q.setParameter("idCabecalho", getInstance().getIdCabecalho());
		}else{
			q.setParameter("idCabecalho", 0);
		}
		q.setParameter("cabecalho", getInstance().getCabecalho());
		Long result = (Long) q.getSingleResult();
		if(result.longValue() > 0){
			String msg = "Já existe cabeçalho cadastro com este nome.";
			FacesMessages.instance().add(Severity.ERROR, msg);
		}else{
			if(persistOuUpdate == 'p'){
				super.persist(getInstance());
			}else if(persistOuUpdate == 'u'){
				super.update(getInstance());
			}
			EntityUtil.getEntityManager().flush();
		}
	}
	
	public void persist(){
		persistOuUpdate('p');
	}
	
	public void update(){
		persistOuUpdate('u');
	}
	
	@Override
	public void newInstance(){
		super.newInstance();
	}
	
	public void adicionarVariavel(int maxLength){
		if(getInstance().getConteudo() == null){
			getInstance().setConteudo(StringUtils.EMPTY);
		}
		
		String cabecalho = getInstance().getConteudo()+"{VAL $"+variavel.getVariavel()+"}";
		if (cabecalho.length() <= maxLength) {
			getInstance().setConteudo(cabecalho);
		} else {
			FacesMessages.instance().add(Severity.ERROR, "Quantidade máxima de caracteres excedida");
		}
	}

	public CabecalhoList getCabecalhoList() {
		if(cabecalhoList == null){
			cabecalhoList = ComponentUtil.getComponent("cabecalhoList");
		}
		return cabecalhoList;
	}

	public void setCabecalhoList(CabecalhoList cabecalhoList) {
		this.cabecalhoList = cabecalhoList;
	}

	public Variavel getVariavel() {
		return variavel;
	}

	public void setVariavel(Variavel variavel) {
		this.variavel = variavel;
	}
}
