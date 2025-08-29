package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(ProcessoDAO.NAME)
public class ProcessoDAO extends BaseDAO<Processo>{
	public static final String NAME = "processoDAO";

	@Override
	public Object getId(Processo e) {
		return e.getIdProcesso();
	}
	
	@SuppressWarnings("unchecked")
	public Processo findByNumeroProcesso(String numeroProcesso){
		String queryString = "from Processo o where o.numeroProcesso = :numeroProcesso";
		
		Query query = getEntityManager().createQuery(queryString);
		query.setParameter("numeroProcesso", numeroProcesso);
		List<Processo> processoList = query.getResultList();
		
		if(processoList != null && !processoList.isEmpty()){
			return processoList.get(0);
		}
		
		return null;
	}

	/**
	 * metodo responsavel por recuperar os processos da pessoa passada em parametro.
	 * @param _pessoaCadastradora
	 * @param protocolados - true se devera restringir a pesquisa a somente os processos protocolados
	 * @return
	 * @throws Exception 
	 */
	public List<Processo> recuperarProcessos(Pessoa _pessoaCadastradora, boolean protocolados) throws Exception {
		List<Processo> resultado = null;
		Search search = new Search(Processo.class);
		try {
			search.addCriteria(Criteria.equals("usuarioCadastroProcesso.idUsuario", _pessoaCadastradora.getIdPessoa()));
			if(protocolados) {
				search.addCriteria(Criteria.not(Criteria.isNull("numeroProcesso")));
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar os processos da pessoa ");
			sb.append(_pessoaCadastradora.getNome());
			sb.append(". Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}
	
	public List<Integer> recuperarIdsProcessosPorNumero(List<String> processos){
		String queryString = "select o.idProcesso from Processo o where o.numeroProcesso in :numeroProcessoList";
		Query query = getEntityManager().createQuery(queryString);
		query.setParameter("numeroProcessoList", processos);
		@SuppressWarnings("unchecked")
		List<Integer> processoIdsList = query.getResultList();
		
		return processoIdsList;
	}

}
