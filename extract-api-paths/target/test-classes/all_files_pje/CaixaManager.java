package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.CaixaDAO;
import br.jus.pje.nucleo.entidades.Caixa;

@Name(CaixaManager.NAME)
public class CaixaManager extends BaseManager<Caixa>{

	public static final String NAME = "caixaManager";

	@In
	CaixaDAO caixaDAO;
	
	@Override
	protected CaixaDAO getDAO() {
		return caixaDAO;
	}
	
	public List<Caixa> getCaixasByNomeTarefa(String nomeTarefa, Integer idLocalizacaoFisica){
		return caixaDAO.getCaixasByNomeTarefa(nomeTarefa,idLocalizacaoFisica);
	}
	
}
