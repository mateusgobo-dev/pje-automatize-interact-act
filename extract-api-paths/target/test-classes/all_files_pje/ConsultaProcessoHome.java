package br.com.infox.ibpm.home;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.ibpm.bean.ConsultaProcesso;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;

@Scope(ScopeType.CONVERSATION)
@Name(ConsultaProcessoHome.NAME)
@Install(precedence = Install.FRAMEWORK)
@BypassInterceptors
public class ConsultaProcessoHome implements Serializable {
	public static final String NAME = "consultaProcessoHome";
	private static final long serialVersionUID = 1L;
	private ConsultaProcesso instance = new ConsultaProcesso();

	public ConsultaProcesso getInstance() {
		return instance;
	}

	public void setInstance(ConsultaProcesso instance) {
		this.instance = instance;
	}

	public EntityManager getEntityManager() {
		return EntityUtil.getEntityManager();
	}

	public boolean isEditable() {
		return true;
	}

	public void limparTela(String obj) {
		instance = new ConsultaProcesso();
		UIComponent form = ComponentUtil.getUIComponent(obj);
		ComponentUtil.clearChildren(form);
	}

	/**
	 * Retorna os resultados do grid
	 * 
	 * @return lista de processos
	 */

	public String getHomeName() {
		return NAME;
	}

	public Class<ConsultaProcesso> getEntityClass() {
		return ConsultaProcesso.class;
	}

	public static ConsultaProcessoHome instance() {
		return (ConsultaProcessoHome) Contexts.getConversationContext().get(NAME);
	}

}