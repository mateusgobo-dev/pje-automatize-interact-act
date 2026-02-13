package br.com.infox.pje.action;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.view.GenericCrudAction;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.NaturezaClet;
import br.jus.pje.nucleo.enums.TipoNaturezaCletEnum;

@Name(NaturezaExecucaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class NaturezaExecucaoAction extends GenericCrudAction<NaturezaClet> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "naturezaExecucaoAction";

	public void persist() {
		if(validaNomeNaturezaPersist(getInstance().getDsNatureza())){
			FacesMessages.instance().add(Severity.ERROR, "Já existe uma Natureza com essa descrição.");
			return;
		}
		getInstance().setTipoNatureza(TipoNaturezaCletEnum.E);
		super.persist(getInstance());
	}

	public void update() {
		if(validaNomeNaturezaUpdate(getInstance().getIdNaturezaClet(), getInstance().getDsNatureza())){
			FacesMessages.instance().add(Severity.ERROR, "Já existe uma Natureza com essa descrição.");
			return;
		}
		super.update(getInstance());
	}

	public void inativar(NaturezaClet naturezaClet) {
		naturezaClet.setAtivo(false);
		super.update(naturezaClet);
	}
	
	private boolean validaNomeNaturezaPersist(String nome){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from NaturezaClet o ");
		sb.append("where o.tipoNatureza = 'E' ");
		sb.append("and o.dsNatureza = :dsNatureza ");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("dsNatureza", nome);
		long result = (Long)q.getSingleResult();
		return result > 0;
	}
	
	private boolean validaNomeNaturezaUpdate(Integer id, String nome){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from NaturezaClet o ");
		sb.append("where o.tipoNatureza = 'E' ");
		sb.append("and o.idNaturezaClet <> :id ");
		sb.append("and o.dsNatureza = :dsNatureza ");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("dsNatureza", nome);
		q.setParameter("id", id);
		long result = (Long)q.getSingleResult();
		return result > 0;
	}

}
