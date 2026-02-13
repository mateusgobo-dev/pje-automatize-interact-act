package br.com.jt.pje.action;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.list.CssDocumentoList;
import br.com.infox.view.GenericCrudAction;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.editor.CssDocumento;

@Name(CssDocumentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate 
public class CssDocumentoAction extends GenericCrudAction<CssDocumento>{

	private static final long serialVersionUID = -6272377835787635556L;
	
	public static final String NAME = "cssDocumentoAction";
	
	private Boolean padrao;
	private CssDocumentoList cssDocumentoList;
	
	public void setIdInstance(Integer id){
		super.setIdInstance(id);
		padrao = getInstance().getPadrao();
	}
	
	public void inativar(CssDocumento cssDocumento){
		if(cssDocumento.getPadrao()){
			String msg = "Este é um estilo padrão, não pode ser inativado. Selecione outro estilo como padrão para poder inativado este.";
			FacesMessages.instance().add(Severity.ERROR, msg);
		}else{
			cssDocumento.setAtivo(false);
			super.update(cssDocumento);
		}
	}
	
	private void persistOuUpdate(char persistOuUpdate){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from CssDocumento o ");
		sb.append("where o.idCssDocumento <> :idCssDocumento ");
		sb.append("and o.nome = :nome ");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		if(getInstance().getIdCssDocumento() != null){
			q.setParameter("idCssDocumento", getInstance().getIdCssDocumento());
		}else{
			q.setParameter("idCssDocumento", 0);
		}
		q.setParameter("nome", getInstance().getNome());
		Long result = (Long) q.getSingleResult();
		if(result.longValue() > 0){
			String msg = "Nome já cadastrado!";
			FacesMessages.instance().add(Severity.ERROR, msg);
		}else if(!getInstance().getAtivo() && getInstance().getPadrao()){
			String msg = "Este é um estilo padrão, não pode ser inativado. Selecione outro estilo como padrão para poder inativar este.";
			FacesMessages.instance().add(Severity.ERROR, msg);
			getInstance().setAtivo(true);
		}else{
			if(persistOuUpdate == 'p'){
				super.persist(getInstance());
			}else if(persistOuUpdate == 'u'){
				super.update(getInstance());
			}
			padrao = getInstance().getPadrao();
			setarRegistrosNaoPadrao();
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
		setPadrao(false);
	}
	
	@SuppressWarnings("unchecked")
	private void setarRegistrosNaoPadrao(){
		if(getInstance().getPadrao()){
			StringBuilder sb = new StringBuilder();
			sb.append("select o from CssDocumento o ");
			sb.append("where o.idCssDocumento <> :idCssDocumento ");
			sb.append("and o.padrao = true ");
			Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
			q.setParameter("idCssDocumento", getInstance().getIdCssDocumento());
			List<CssDocumento> lista = q.getResultList();
			for(CssDocumento d : lista){
				if(d.getIdCssDocumento().intValue() != getInstance().getIdCssDocumento().intValue()){
					d.setPadrao(false);
					EntityUtil.getEntityManager().merge(d);
				}
			}
			EntityUtil.getEntityManager().flush();
		}
	}

	public Boolean getPadrao() {
		return padrao;
	}

	public void setPadrao(Boolean padrao) {
		this.padrao = padrao;
	}

	public CssDocumentoList getCssDocumentoList() {
		if(cssDocumentoList == null){
			cssDocumentoList = ComponentUtil.getComponent("cssDocumentoList");
		}
		return cssDocumentoList;
	}

	public void setCssDocumentoList(CssDocumentoList cssDocumentoList) {
		this.cssDocumentoList = cssDocumentoList;
	}
	
}
