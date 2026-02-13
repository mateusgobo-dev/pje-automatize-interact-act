package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoFavorito;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("processoDocumentoFavoritoDAO")
public class ProcessoDocumentoFavoritoDAO extends BaseDAO<ProcessoDocumentoFavorito> {

	@Override
	public Integer getId(ProcessoDocumentoFavorito p) {
		return p.getIdProcessoDocumentoFavorito();
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoDocumentoFavorito> findByProcesso(Integer idProcesso, Integer idUsuario) {
		String hql = "from ProcessoDocumentoFavorito fav where fav.processoDocumento.processo.idProcesso = :idProcesso and fav.usuario.idUsuario = :idUsuario order by fav.indice";
		Query query = getEntityManager().createQuery(hql);
		query.setParameter("idProcesso", idProcesso);
		query.setParameter("idUsuario", idUsuario);
		return query.getResultList();
	}
	
	@Override
	public void remove(ProcessoDocumentoFavorito e){
		super.remove(e);
		getEntityManager().flush();
	}

	/**
	 * recupera todos os processos documentos favoritos da pessoa passada em parametro.
	 * @param pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<ProcessoDocumentoFavorito> recuperarProcessosDocumentosFavoritos(Pessoa _pessoa) throws Exception {
		List<ProcessoDocumentoFavorito> resultado = null;
		Search search = new Search(ProcessoDocumentoFavorito.class);
		try {
			search.addCriteria(Criteria.equals("usuario.idUsuario", _pessoa.getIdPessoa()));			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar os Processos documentos favoritos da pessoa ");
			sb.append(_pessoa.getNome());
			sb.append(". Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}

}
