package br.jus.cnj.pje.business.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.identidade.LogAcesso;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("logAcessoDAO")
public class LogAcessoDAO extends BaseDAO<LogAcesso>{

	@Override
	public Object getId(LogAcesso e) {
		return e.getIdLogAcesso();
	}

	/**
	 * Recupera o último login do usuário no sistema.
	 * 
	 * @param idUsuario Identificador do usuário.
	 * @return Texto no formato HH:mm:ss dd/MM/YYYY. 
	 */
	public String recuperarUltimoLogin(Integer idUsuario) {
		StringBuilder sb = new StringBuilder("SELECT o.dataEvento FROM LogAcesso AS o ");
		sb.append("WHERE o.usuarioLogin.idUsuario = :idUsuario AND o.bemSucedido = 'true' ");
		sb.append("ORDER BY o.dataEvento DESC ");
		
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("idUsuario", idUsuario);
		
		return DateUtil.dateToString((Date) EntityUtil.getSingleResult(query), "HH:mm:ss dd/MM/YYYY");
	}

	/**
	 * recupera todos os logs de acesso da pessoa passada em parametro
	 * @param _pessoa
	 * @return
	 */
	public List<LogAcesso> recuperaTodosLogsAcesso(Pessoa _pessoa) {
		List<LogAcesso> resultado = null;
		Search search = new Search(LogAcesso.class);
		try {
			search.addCriteria(Criteria.equals("usuarioLogin.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		resultado = list(search);
		return resultado;
	}
}