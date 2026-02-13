/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ProcuradorDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.AutoridadePublica;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente gerenciador da entidade {@link PessoaProcurador}. 
 * 
 * @author cristof
 *
 */
@Name("procuradorManager")
public class ProcuradorManager extends BaseManager<PessoaProcurador> {
	
	@In
	private ProcuradorDAO procuradorDAO;

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected BaseDAO<PessoaProcurador> getDAO() {
		return procuradorDAO;
	}
	
	/**
	 * Atribui a uma pessoa dada, se já não tiver, um perfil de procurador.
	 * 
	 * @param pessoa a pessoa física a quem se pretende dar o perfil de procurador
	 * @return a {@link PessoaProcurador} já vinculada à pessoa física.
	 */
	public PessoaProcurador especializa(PessoaFisica pessoa){
		PessoaProcurador proc = pessoa.getPessoaProcurador();
		if(proc == null){
			proc = procuradorDAO.especializa(pessoa);
			pessoa.setPessoaProcurador(proc);
		}
		pessoa.setEspecializacoes(pessoa.getEspecializacoes() | PessoaFisica.PRO);
		return proc;
	}
	
	/**
	 * Suprime de uma pessoa a especialização de procurador
	 * @param pessoa a pessoa física a quem se prentende suprimir o perfil
	 * @return a {@link PessoaServidor} já vinculada à pessoa física.
	 * @throws PJeBusinessException
	 */
	public PessoaProcurador desespecializa(PessoaFisica pessoa) throws PJeBusinessException{
		PessoaProcurador pro = pessoa.getPessoaProcurador();
		if(pro != null){
			pro = procuradorDAO.desespecializa(pessoa);
			pessoa.setPessoaProcurador(pro);
		}
		return pro;
	}		
	
	/**
	 * Recupera a lista de pessoas representadas por um procurador vinculado a uma dada procuradoria.
	 * 
	 * @param procuradoria a procuradoria a que está vinculado o procurador
	 * @param procurador o procurador a respeito de quem se pretende identificar as pessoas representadas
	 * @return a lista de pessoas representadas.
	 */
	public List<Pessoa> getPessoasRepresentadas(Procuradoria procuradoria, Pessoa procurador){
		return procuradorDAO.getPessoasRepresentadas(procuradoria, procurador);
	}
	
	public List<Integer> getIdsRepresentados(Pessoa representante) throws PJeBusinessException{
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(representante.getIdPessoa());
		ids.addAll(getIdsAutoridadesRepresentadas(representante));
		return ids;
	}
	
	private List<Integer> getIdsAutoridadesRepresentadas(Pessoa representante){
		Date now = new Date();
		Search s = new Search(AutoridadePublica.class);
		s.setRetrieveField("autoridade.idUsuario");
		addCriteria(s, 
				Criteria.equals("pessoa", representante),
				Criteria.equals("ativo", true),
				Criteria.or(
						Criteria.isNull("dataInicio"),
						Criteria.lessOrEquals("dataInicio", now)),
				Criteria.or(
						Criteria.isNull("dataFim"),
						Criteria.greaterOrEquals("dataFim", now)));
		return list(s);
	}

}
