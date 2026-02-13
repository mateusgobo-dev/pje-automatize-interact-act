package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoPapel;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

/**
 * Componente de acesso a dados da entidade {@link ProcessoDocumentoBinPessoaAssinatura}.
 *  
 * @author cristof
 */
@Name("processoDocumentoBinPessoaAssinaturaDAO")
public class ProcessoDocumentoBinPessoaAssinaturaDAO extends BaseDAO<ProcessoDocumentoBinPessoaAssinatura>{
	
    @SuppressWarnings("unchecked")
    public List<ProcessoDocumentoBinPessoaAssinatura> getAssinaturas(Integer idProcessoDocumentoBin) {
        if (idProcessoDocumentoBin == null || idProcessoDocumentoBin == 0) {
            return new ArrayList<ProcessoDocumentoBinPessoaAssinatura>(0);
        }

        String query = "SELECT a FROM ProcessoDocumentoBinPessoaAssinatura a WHERE a.processoDocumentoBin.idProcessoDocumentoBin = :idProcessoDocumentoBin";
        Query q = this.entityManager.createQuery(query);
        q.setParameter("idProcessoDocumentoBin", idProcessoDocumentoBin);

        List<ProcessoDocumentoBinPessoaAssinatura> list = q.getResultList();

        return list;
    }

    public void persistAll(Collection<ProcessoDocumentoBinPessoaAssinatura> pdbpa) {
        Collection<ProcessoDocumentoBinPessoaAssinatura> aux = new ArrayList<ProcessoDocumentoBinPessoaAssinatura>(pdbpa.size());
        for (ProcessoDocumentoBinPessoaAssinatura assinatura : pdbpa) {
            ProcessoDocumentoBinPessoaAssinatura assAux = this.persist(assinatura);
            if (assAux != null) {
                aux.add(assAux);
            }
        }
        pdbpa.clear();
        pdbpa.addAll(aux);
    }

    @SuppressWarnings("unchecked")
    public List<ProcessoDocumentoBinPessoaAssinatura> getAssinaturaDocumento(
        ProcessoDocumentoBin processoDocumentoBin) {
        StringBuilder sb = new StringBuilder();
        sb.append("Select o from ProcessoDocumentoBinPessoaAssinatura o ");
        sb.append("where o.processoDocumentoBin = :pdBin ");

        Query q = getEntityManager().createQuery(sb.toString());
        q.setParameter("pdBin", processoDocumentoBin);

        List<ProcessoDocumentoBinPessoaAssinatura> list = q.getResultList();

        return list;
    }
    
    @SuppressWarnings("unchecked")
    public List<ProcessoDocumentoBinPessoaAssinatura> getAssinaturaDocumento(
        ProcessoDocumentoBin pdBin,
        Pessoa pessoa) {
    	String hql = "SELECT o FROM ProcessoDocumentoBinPessoaAssinatura AS o "
    			+ "WHERE o.processoDocumentoBin = :pdBin AND o.pessoa = :pessoa "
    			+ "ORDER BY o.idProcessoDocumentoBinPessoaAssinatura";
        Query query = getEntityManager().createQuery(hql);
        query.setParameter("pdBin", pdBin);
        query.setParameter("pessoa", pessoa);

        List<ProcessoDocumentoBinPessoaAssinatura> list = query.getResultList();
        return list;
    }

    @Override
    public Integer getId(ProcessoDocumentoBinPessoaAssinatura e) {
        return e.getIdProcessoDocumentoBinPessoaAssinatura();
    }

    public Integer countDocumentoAcordaoAssinado(ProcessoTrf processoTrf, SessaoJT sessao, Voto voto) {
        String query = "SELECT COUNT(pdbpa) FROM ProcessoDocumentoBinPessoaAssinatura AS pdbpa "
        		+ "	WHERE "
        		+ "	EXISTS(SELECT dv FROM DocumentoVoto AS dv "
        		+ "					WHERE dv.processoDocumentoBin.idProcessoDocumentoBin = pdbpa.processoDocumentoBin.idProcessoDocumentoBin "
        		+ "						AND dv.tipoProcessoDocumento = :tipoProcessoDocumento"
        		+ "						AND dv.processo = :processo "
        		+ "						AND dv.ativo = true "
        		+ "						AND dv.voto = :voto "
        		+ "						AND EXISTS (SELECT ps FROM PautaSessao ps "
        		+ "												WHERE ps.processoTrf.idProcessoTrf = dv.processo.idProcesso "
        		+ "													AND ps.sessao = :sessao)"
        		+ "		)";
        Query q = getEntityManager().createQuery(query);
        q.setParameter("sessao", sessao);
        q.setParameter("processo", processoTrf);
        q.setParameter("tipoProcessoDocumento", ParametroUtil.instance().getTipoProcessoDocumentoAcordao());
        q.setParameter("voto", voto);
        q.setMaxResults(1);
        Number result = (Number) q.getSingleResult();
        return result.intValue();
    }

    public String getNomeUsuarioUltimaAssinatura(int idProcessoDocumentoBin) {
        String query = "SELECT COALESCE(o.pessoa.nome, o.nomePessoa) FROM ProcessoDocumentoBinPessoaAssinatura AS o "
        		+ "	WHERE o.idProcessoDocumentoBinPessoaAssinatura = "
        		+ "		(SELECT MAX(a.idProcessoDocumentoBinPessoaAssinatura) "
        		+ "			FROM ProcessoDocumentoBinPessoaAssinatura AS a "
        		+ "			WHERE a.processoDocumentoBin.idProcessoDocumentoBin = :idProcessoDocumentoBin)";
        Query q = getEntityManager().createQuery(query);
        q.setParameter("idProcessoDocumentoBin", idProcessoDocumentoBin);
        q.setMaxResults(1);
        return (String) q.getSingleResult();
    }

	public Long countDocumentoNaoAssinadoPorTipo(ProcessoTrf processoTrf, TipoProcessoDocumento tipoProcessoDocumento){
		String query = "SELECT COUNT(o1) FROM ProcessoDocumento AS o1 "
				+ "	WHERE o1.processo.idProcesso = :idProcesso "
				+ "		AND o1.tipoProcessoDocumento.idTipoProcessoDocumento = :idTipoProcessoDocumento "
				+ "		AND NOT EXISTS("
				+ "			SELECT o FROM ProcessoDocumentoBinPessoaAssinatura AS o "
				+ "				WHERE o1.processoDocumentoBin.idProcessoDocumentoBin = o.processoDocumentoBin.idProcessoDocumentoBin"
				+ "		)";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("idProcesso", processoTrf.getIdProcessoTrf());
		q.setParameter("idTipoProcessoDocumento", tipoProcessoDocumento.getIdTipoProcessoDocumento());
		Number result = (Number) q.getSingleResult();
		return result.longValue();
	}
	
	@SuppressWarnings("unchecked")
	public boolean podeAssinar(TipoProcessoDocumento tipoDocumento, Papel... papeis) {
		TipoProcessoDocumentoPapel achou = null;
		int id = 0;
		
		if (tipoDocumento != null && papeis != null)
		{
			id = tipoDocumento.getIdTipoProcessoDocumento();
		
	 		if (id != 0) 
	 		{
	 			EntityManager em = ProcessoDocumentoHome.instance().getEntityManager();
	 			
				StringBuilder sqlPes = new StringBuilder();
				sqlPes.append(" select o from ");
				sqlPes.append(" TipoProcessoDocumentoPapel o");
				sqlPes.append(" where o.tipoProcessoDocumento.idTipoProcessoDocumento = :id");
				Query query = em.createQuery(sqlPes.toString());
				query.setParameter("id", id);
				
				List<TipoProcessoDocumentoPapel> list = query.getResultList();
				UsuarioLocalizacao usuarioLocalizacaoAtual = (UsuarioLocalizacao) Contexts.getSessionContext().get("usuarioLogadoLocalizacaoAtual");
	
				int i = 0;
				while (achou == null && i < list.size()) 
				{
					if (usuarioLocalizacaoAtual.getPapel().getNome().equals(list.get(i).getPapel().getNome())) 
					{
						achou = list.get(i);
					}
					i++;
				}
			}
		}

		return achou != null;
	}
	
	public boolean verificaSeTemAssinatura(ProcessoDocumentoBin processoDocumentoBin) {
		StringBuilder jpql = new StringBuilder();
		jpql.append("select count(o) from ProcessoDocumentoBinPessoaAssinatura o ");
		jpql.append("where o.processoDocumentoBin.idProcessoDocumentoBin = :idProcessoDocumentoBin");

		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("idProcessoDocumentoBin", processoDocumentoBin.getIdProcessoDocumentoBin());
		
		Long retorno = (Long) query.getSingleResult();
		
		return retorno > 0;
	}

}
