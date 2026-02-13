package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.LembretePermissao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("lembretePermissaoDAO")
public class LembretePermissaoDAO extends BaseDAO<LembretePermissao> {

	@Override
	public Object getId(LembretePermissao e) {
		return e.getIdLembretePermissao();
	}
	
	/**
	 * Metodo que remove todas as permissões do lembrete
	 * @param idLembrete
	 */
	public void removePermissoesPorIdLembrete(Integer idLembrete){
		String sql = "delete from tb_lembrete_permissao where id_lembrete = "+idLembrete;
		EntityUtil.createNativeQuery(getEntityManager(), sql, "tb_lembrete_permissao").executeUpdate();
		EntityUtil.flush(getEntityManager());
	}

	/**
	 * metodo responsavel por recuperar todoas as permissoes lemebretes da pessoa passada em parametro.
	 * @param _pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<LembretePermissao> recuperarLembretesPermissao(Pessoa _pessoa) throws Exception {
		List<LembretePermissao> resultado = null;
		Search search = new Search(LembretePermissao.class);
		try {
			search.addCriteria(Criteria.equals("usuario.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar as permissões de lembretes da pessoa ");
			sb.append(_pessoa.getNome());
			sb.append(". Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}
	
}
