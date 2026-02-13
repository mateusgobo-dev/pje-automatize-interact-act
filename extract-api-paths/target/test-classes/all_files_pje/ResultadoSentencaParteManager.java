package br.com.jt.pje.manager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.jt.pje.dao.ResultadoSentencaParteDAO;

@Name(ResultadoSentencaParteManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class ResultadoSentencaParteManager extends GenericManager{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "resultadoSentencaParteManager";
	
	@In
	private ResultadoSentencaParteDAO resultadoSentencaParteDAO;
	
	public Double getSomaValorCondenacaoBy(Integer idProcesso) {
		if(idProcesso == null){
			return null;
		}
		return resultadoSentencaParteDAO.getSomaValorCondenacaoBy(idProcesso);
	}
	
}
