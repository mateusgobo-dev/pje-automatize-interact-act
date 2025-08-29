package br.com.infox.pje.action;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.OrgaoJulgadorHome;
import br.com.infox.view.GenericCrudAction;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.manager.PrazoMinimoMarcacaoAudienciaManager;
import br.jus.cnj.pje.nucleo.manager.TipoAudienciaManager;
import br.jus.pje.nucleo.entidades.PrazoMinimoMarcacaoAudiencia;
import br.jus.pje.nucleo.entidades.TipoAudiencia;;

@Name(AbaPrazoMinimoMarcacaoAudienciaAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AbaPrazoMinimoMarcacaoAudienciaAction extends
		GenericCrudAction<PrazoMinimoMarcacaoAudiencia> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "abaPrazoMinimoMarcacaoAudienciaAction";	
	
	@In
	private TipoAudienciaManager tipoAudienciaManager;
	
	@In
	private PrazoMinimoMarcacaoAudienciaManager prazoMinimoMarcacaoAudienciaManager; 
	
	public void persist() {
		getInstance().setOrgaoJulgador(OrgaoJulgadorHome.instance().getInstance());
		
		if (validaPrazoMinimoMarcacaoAudiencia(getInstance())){
			FacesMessages.instance().add(Severity.ERROR, "Já existe um Prazo com esse Tipo de Audiência.");
			return;
		}
		
		super.persist(getInstance());
	}

	public void update() {
		getInstance().setOrgaoJulgador(OrgaoJulgadorHome.instance().getInstance());
		
		if (validaPrazoMinimoMarcacaoAudiencia(getInstance())){
			FacesMessages.instance().add(Severity.ERROR, "Já existe um Prazo com esse Tipo de Audiência.");
			return;
		}
		
		super.update(getInstance());
	}

	public void remove(PrazoMinimoMarcacaoAudiencia prazoMinimoMarcacaoAudiencia) {
		super.remove(prazoMinimoMarcacaoAudiencia);
	}
	
	private boolean validaPrazoMinimoMarcacaoAudiencia(PrazoMinimoMarcacaoAudiencia prazoMinimoMarcacaoAudiencia){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(p) from PrazoMinimoMarcacaoAudiencia p ");
		sb.append("where p.tipoAudiencia = :tipoAudiencia ");
		sb.append("and p.orgaoJulgador = :orgaoJulgador ");
		
		if (prazoMinimoMarcacaoAudiencia.getIdPrazoMinimoMarcacaoAudiencia() != 0) {
			sb.append("and p.idPrazoMinimoMarcacaoAudiencia <> :idPrazoMinimoMarcacaoAudiencia ");	
		}
		
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("tipoAudiencia", prazoMinimoMarcacaoAudiencia.getTipoAudiencia());
		q.setParameter("orgaoJulgador", prazoMinimoMarcacaoAudiencia.getOrgaoJulgador());
		
		if (prazoMinimoMarcacaoAudiencia.getIdPrazoMinimoMarcacaoAudiencia() != 0) {
			q.setParameter("idPrazoMinimoMarcacaoAudiencia", prazoMinimoMarcacaoAudiencia.getIdPrazoMinimoMarcacaoAudiencia());	
		}
		
		long result = (Long) q.getSingleResult();
		return result > 0;
	}	
	
	public List<TipoAudiencia> getTipoAudienciaList() {
		return tipoAudienciaManager.getTipoAudienciaList();
	}

	public List<PrazoMinimoMarcacaoAudiencia> getPrazoMinimoMarcacaoAudienciaList() {
		return prazoMinimoMarcacaoAudienciaManager.getPrazoMinimoMarcacaoAudienciaList(OrgaoJulgadorHome.instance().getInstance()); 
	}
}
