package br.com.infox.pje.manager;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.dao.BaseCalculoIrDAO;
import br.jus.pje.nucleo.entidades.BaseCalculoIr;

/**
 * Classe que acessa o DAO e contem a regra de negocios referente a entidade
 * BaseCalculoIr
 * 
 * @author Silas
 * 
 */
@Name(BaseCalculoIrManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class BaseCalculoIrManager extends GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "baseCalculoIrManager";

	@In
	private BaseCalculoIrDAO baseCalculoIrDAO;

	public BaseCalculoIr getBaseCalculoIrByValor(Double valor) {
		return baseCalculoIrDAO.getBaseCalculoIrByValor(valor);
	}

	public BaseCalculoIr getBaseCalculoIr(){
		return baseCalculoIrDAO.getBaseCalculoIr();
	}	
}