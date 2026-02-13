package br.jus.cnj.pje.business.dao;
 
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.jboss.seam.annotations.Name;
 
 import br.com.itx.util.EntityUtil;
 import br.jus.pje.nucleo.entidades.Unificacao;
 import br.jus.pje.nucleo.entidades.UnificacaoPessoas;
 
 /**
  * 
  * @author luiz.mendes
  *
  */
 @Name(UnificacaoPessoasDAO.NAME)
 public class UnificacaoPessoasDAO extends BaseDAO<UnificacaoPessoas>{
 
 	public static final String NAME = "unificacaoPessoasDAO";
 
 	@Override
 	public Object getId(UnificacaoPessoas e) {
 		return e.getIdUnificacaoPessoas();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<UnificacaoPessoas> recuperaUnificacoesPessoasPorUnificacao(Unificacao unificacao, boolean unificacaoAtiva) {
 		EntityManager em = EntityUtil.getEntityManager();
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT o FROM UnificacaoPessoas o ");
 		sql.append("WHERE o.unificacao.idUnificacao = :idUnificacao ");
 		if(unificacaoAtiva) {
 			sql.append("AND o.ativo = true");
 		} else {
 			sql.append("AND o.ativo = false");
 		}
 		
 		Query query = em.createQuery(sql.toString());
 		query.setParameter("idUnificacao", unificacao.getIdUnificacao());
 		
 		return query.getResultList();
 	}
 }