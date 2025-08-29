package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.SessaoJudicialDAO;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(SessaoJudicialManager.NAME)
public class SessaoJudicialManager extends BaseManager<Sessao> {
	public static final String NAME = "sessaoJudicialManager";
	
	@In
	private SessaoJudicialDAO sessaoJudicialDAO;
	
	@Override
	protected BaseDAO<Sessao> getDAO() {
		return sessaoJudicialDAO;
	}
	
	public Sessao getSessaoAtual(){
		Search s = new Search(Sessao.class);
		addCriteria(s, 
		Criteria.not(Criteria.isNull("dataAberturaSessao")),
		Criteria.isNull("dataRealizacaoSessao"));
		List<Sessao> sessoes = list(s);
		
		if(sessoes != null && !sessoes.isEmpty()){
			return sessoes.get(0);
		}
		
		return null;
	}
	
	public List<Sessao> findByAno(Integer ano){
		return findByAno(ano,false,false);
	}
	
	public List<Sessao> findByAno(Integer ano,Boolean somenteContinuas,Boolean sessoesFuturas){
		return sessaoJudicialDAO.findByAno(ano, somenteContinuas, sessoesFuturas);
	}
}
