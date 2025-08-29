package br.jus.cnj.pje.business.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.query.PessoaMagistradoQuery;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * @author cristof
 */
@Name("pessoaMagistradoDAO")
public class PessoaMagistradoDAO extends AbstractPessoaFisicaEspecializadaDAO<PessoaMagistrado>
    implements Serializable, PessoaMagistradoQuery {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public PessoaMagistrado getMagistradoRecebeDistribuicao(
        OrgaoJulgador orgaoJulgador) {
        String queryString = "select o from PessoaMagistrado o where " +
            "o.idUsuario in (select ul.usuarioLocalizacao.usuario.idUsuario " +
            "from UsuarioLocalizacaoMagistradoServidor ul where " +
            "ul.orgaoJulgadorCargo.recebeDistribuicao = true and " +
            "ul.orgaoJulgador = :orgaoJulgador " +
            "and ul.usuarioLocalizacao.papel = :papel)";
        Query q = this.entityManager.createQuery(queryString);
        q.setParameter("orgaoJulgador", orgaoJulgador);
        q.setParameter("papel", ParametroUtil.instance().getPapelMagistrado());
        q.setMaxResults(1);

        List<PessoaMagistrado> list = q.getResultList();

        if ((list != null) && (list.size() > 0)) {
            return list.get(0);
        }

        return null;
    }

    @Override
    public Integer getId(PessoaMagistrado e) {
        return e.getIdUsuario();
    }

    @SuppressWarnings("unchecked")
    public List<PessoaMagistrado> magistradoPresenteItems(
        OrgaoJulgador orgaoJulgador, SessaoJT sessao) {
        Query q = getEntityManager().createQuery(MAGISTRADO_PRESENTE_ITEMS_QUERY);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<PessoaMagistrado> list = q.getResultList();

        return list;
    }

    @SuppressWarnings("unchecked")
    public List<PessoaMagistrado> magistradoSubstitutoItems(
        OrgaoJulgador orgaoJulgador, SessaoJT sessao, ProcessoTrf processoTrf) {
        Query q = getEntityManager()
                      .createQuery(MAGISTRADO_SUBSTITUTO_ITEMS_QUERY);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);

        List<PessoaMagistrado> list = q.getResultList();

        return list;
    }

    @SuppressWarnings("unchecked")
    public List<PessoaMagistrado> magistradoSubstitutoSessaoItems(
        OrgaoJulgador orgaoJulgador, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(MAGISTRADO_SUBSTITUTO_SESSAO_ITEMS_QUERY);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<PessoaMagistrado> list = q.getResultList();

        return list;
    }
    
    @SuppressWarnings("unchecked")  
  	public List<PessoaMagistrado> magistradoSubstitutoSessaoItems(  
  			OrgaoJulgador orgaoJulgador, SessaoJT sessao, OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
    	Query q = getEntityManager().createQuery(MAGISTRADO_SUBSTITUTO_SESSAO_ITEMS_2GRAU_QUERY);  
	  	q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);  
	  	q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO, orgaoJulgadorColegiado);  
	  	q.setParameter(QUERY_PARAMETER_SESSAO, sessao);  
	  	List<PessoaMagistrado> list = q.getResultList();  
	  	return list;  
  	}  

    @SuppressWarnings("unchecked")
    public List<PessoaMagistrado> magistradoSubstitutoItems(
        OrgaoJulgador orgaoJulgador, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(MAGISTRADO_SUBSTITUTO_ITEMS_QUERY);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<PessoaMagistrado> list = q.getResultList();

        return list;
    }

    public PessoaMagistrado getMagistradoTitular(OrgaoJulgador orgaoJulgador) {
        Query q = getEntityManager().createQuery(MAGISTRADO_TITULAR_QUERY);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
        q.setParameter(QUERY_PARAMETER_PAPEL, ParametroUtil.instance().getPapelMagistrado());
        q.setParameter(QUERY_PARAMETER_DATA, DateUtil.getDataAtual());
        PessoaMagistrado result = EntityUtil.getSingleResult(q);

        return result;
    }
    
    public PessoaMagistrado getMagistradoTitular(OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado) {  
    	Query q = getEntityManager().createQuery(MAGISTRADO_TITULAR_2GRAU_QUERY);  
      	q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO, orgaoJulgadorColegiado);  
      	q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);  
      	q.setParameter(QUERY_PARAMETER_PAPEL, ParametroUtil.instance().getPapelMagistrado());  
        PessoaMagistrado result = EntityUtil.getSingleResult(q);

        return result;
    }

    @SuppressWarnings("unchecked")
    public List<PessoaMagistrado> magistradoPorOrgaoJulgador(OrgaoJulgador orgaoJulgador, Boolean somenteAtivosNoOrgao) {
        Query q = getEntityManager().createQuery(somenteAtivosNoOrgao ? 
        		MAGISTRADOS_ATIVOS_POR_LOCALIZACAO_QUERY : MAGISTRADO_POR_LOCALIZACAO_QUERY);
        
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
        List<PessoaMagistrado> list = q.getResultList();
        return list;
    }

    @SuppressWarnings("unchecked")
    public List<PessoaMagistrado> magistradoList() {
        String hql = "select o from PessoaMagistrado o where o.ativo = true";
        Query q = getEntityManager().createQuery(hql);
        List<PessoaMagistrado> list = q.getResultList();

        return list;
    }
    
	/**
	 * Atribui a uma pessoa física dada um perfil de magistrado.
	 * 
	 * @param pessoa a pessoa física a quem será atribuído o perfil
	 * @return a {@link PessoaMagistrado} que foi atribuída à pessoa física.
	 */
	@Override
	public PessoaMagistrado especializa(PessoaFisica pessoa){
		if(!entityManager.contains(pessoa)){
			entityManager.persist(pessoa);
		}
		entityManager.flush();
		String query = "INSERT INTO tb_pessoa_magistrado (id, nr_matricula) VALUES (?1, '')";
		Query q = EntityUtil.createNativeQuery(entityManager, query, "tb_pessoa_magistrado");
		q.setParameter(1, pessoa.getIdUsuario());
		if(q.executeUpdate() > 0) {
			return entityManager.find(PessoaMagistrado.class, pessoa.getIdUsuario());
		} else {
			return null;
		}
	}

	/**
	 * Suprime de uma pessoa física o perfil de magistrado
	 * @param pessoa
	 * @return
	 */
	@Override
	public PessoaMagistrado desespecializa(PessoaFisica pessoa){
		PessoaMagistrado mag = null;
		mag = entityManager.find(PessoaMagistrado.class, pessoa.getIdPessoa());
		if(mag != null){
			mag.getPessoa().suprimePessoaEspecializada(mag);
			entityManager.flush();
			return mag;
		}
		
		return null;
	}	

	@SuppressWarnings("unchecked")
	public List<PessoaMagistrado> recuperarAtivosOrdernadosPorPessoaNome() {
		String hql = "select m from PessoaMagistrado m where m.ativo = true order by m.pessoa.nome ";
		Query q = getEntityManager().createQuery(hql);
	    return q.getResultList();
	}
	
	/**
	 * Método responsável por recuperar o magistrado marcado como presente na sessão de julgamento e que seja representante do órgão julgador.
	 * 
	 * @param idSessao Identificador da sessão de julgamento.
	 * @param idOrgaoJulgador Identificador do órgão julgador.
	 * @return Magistrado marcado como presente na sessão de julgamento e que seja representante do órgão julgador.
	 */
	public PessoaMagistrado recuperarMagistrado(Integer idSessao, Integer idOrgaoJulgador) {
		StringBuilder sb = new StringBuilder("select o from SessaoComposicaoOrdem o ");
		sb.append("where (o.magistradoTitularPresenteSessao = true or o.magistradoSubstitutoSessao is not null) ");
		sb.append("and o.sessao.idSessao = :idSessao ");
		sb.append("and o.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador");
		
		Query q = getEntityManager().createQuery(sb.toString())
				.setParameter("idSessao", idSessao)
				.setParameter("idOrgaoJulgador", idOrgaoJulgador);

		SessaoComposicaoOrdem sessaoComposicaoOrdem = EntityUtil.getSingleResult(q);
		if (ParametroUtil.instance().isMostrarJuizSubstitutoNaCertidao()) {
			if (sessaoComposicaoOrdem.getMagistradoSubstitutoSessao()!= null)  
			   { return sessaoComposicaoOrdem.getMagistradoSubstitutoSessao(); } 
			else 
				{return sessaoComposicaoOrdem.getMagistradoPresenteSessao();}
		} else {
			return sessaoComposicaoOrdem.getMagistradoPresenteSessao();
		}
	}

	/**
	 * Retorna os órgãos julgadores onde o magistrado é o titular.
	 * 
	 * @param magistrado PessoaMagistrado
	 * @return órgãos julgadores onde o magistrado é o titular.
	 */
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> consultarOrgaoJulgadorMagistradoTitular(PessoaMagistrado magistrado) {
		StringBuilder hql = new StringBuilder();
		hql.append("select distinct o.orgaoJulgador from UsuarioLocalizacaoMagistradoServidor o ");
		hql.append("where ");
		hql.append("o.usuarioLocalizacao.usuario = :magistrado and ");
		hql.append("o.magistradoTitular = true");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("magistrado", magistrado.getPessoa());
		
		return query.getResultList();
	}
	
	/**
	 * Metodo que obtem os magistrados substitutos para uma determinada sessao e orgao julgador.
	 * 
	 * @param idSessao Id da Sessao
	 * @param idOrgaoJulgador Id do Orgao Julgador
	 * @return Lista de Magistrados substitutos.
	 */
	@SuppressWarnings("unchecked")
	public List<PessoaMagistrado> obterSubstitutos(Integer idSessao, Integer idOrgaoJulgador){
		StringBuilder sbQuery = new StringBuilder();
		
		sbQuery.append("SELECT pm FROM PessoaMagistrado pm ");
		sbQuery.append("WHERE pm NOT IN (SELECT sco.magistradoSubstitutoSessao FROM SessaoComposicaoOrdem sco ");
		sbQuery.append("				 WHERE sco.sessao.idSessao = :idSessao ");
		sbQuery.append("				 AND sco.orgaoJulgador.idOrgaoJulgador != :idOrgaoJulgador) ");
		sbQuery.append("AND pm NOT IN (SELECT sco.magistradoPresenteSessao FROM SessaoComposicaoOrdem sco ");
		sbQuery.append("			   WHERE sco.sessao.idSessao = :idSessao and sco.magistradoTitularPresenteSessao = true) ");
		sbQuery.append("AND pm.idUsuario IN (SELECT ul.usuario.idUsuario ");
		sbQuery.append("					 FROM UsuarioLocalizacaoMagistradoServidor ulms "); 
		sbQuery.append("					 JOIN ulms.usuarioLocalizacao ul ");
		sbQuery.append("					 JOIN ulms.orgaoJulgadorCargo ojc ");
		sbQuery.append("					 WHERE ulms.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ");
		sbQuery.append("					 AND CAST(ulms.dtInicio as date) <= :dataCorrente ");
		sbQuery.append("					 AND (ulms.dtFinal is null or CAST(ulms.dtFinal as date) >= :dataCorrente)");
		sbQuery.append("					 AND ulms.orgaoJulgadorColegiado = ( ");
		sbQuery.append("				 			SELECT s.orgaoJulgadorColegiado ");
		sbQuery.append("				 			FROM Sessao s ");
		sbQuery.append("				 			WHERE s.idSessao = :idSessao) ");
		sbQuery.append("				 	 AND ojc.auxiliar = true ");
		sbQuery.append("				 	 AND ojc.recebeDistribuicao = false )");
		
		
		sbQuery.append("ORDER BY pm.nome ");
		Query query = getEntityManager().createQuery(sbQuery.toString());
		query.setParameter("idSessao", idSessao);
		query.setParameter("idOrgaoJulgador", idOrgaoJulgador);
		query.setParameter("dataCorrente", DateUtil.getDataAtual());
		return query.getResultList();
	}
	
	/**
	 * Metodo que obtem os magistrados aptos para uma sessao.
	 *  + Verifica se o magistrado pode compor a sessao, e se ele é apto de acordo com os magistrados vinculados a um
	 *  	Orgao Julgador
	 *  + Verifica qual o magistrado mais apto mais antigo para um Orgao Julgador e coloca ele como primeiro da lista 
	 * 
	 * @param idOrgaoJulgador Id do Orgao Julgador
	 * @param idSessao Id da Sessao
	 * @param dataAberturaSessao Data de Inicio da Sessao
	 * @return Magistrados aptos para uma sessao.
	 */
	@SuppressWarnings("unchecked")
	public List<PessoaMagistrado> obterAptos(Integer idOrgaoJulgador, Integer idSessao, Date dataAberturaSessao) {
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" SELECT magistrado FROM UsuarioLocalizacaoMagistradoServidor user_local_magistrado, PessoaMagistrado magistrado");
		sbQuery.append("   JOIN user_local_magistrado.usuarioLocalizacao user_local");
		sbQuery.append("   JOIN user_local.papel papel");
		sbQuery.append("   JOIN user_local.usuario usuario");
		sbQuery.append(" WHERE magistrado.idUsuario = usuario.idUsuario");
		sbQuery.append("   AND papel.identificador = :papel");
		sbQuery.append("   AND (user_local_magistrado.dtFinal IS NULL OR user_local_magistrado.dtFinal > :dataSessao)");
		sbQuery.append("   AND user_local_magistrado.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador");
		sbQuery.append("   AND magistrado.idUsuario NOT IN (");
		sbQuery.append(" 				  SELECT sessaoCompOrdem.magistradoPresenteSessao FROM SessaoComposicaoOrdem sessaoCompOrdem ");
		sbQuery.append("				  		WHERE sessaoCompOrdem.sessao.idSessao = :idSessao ");
		sbQuery.append("				  		AND sessaoCompOrdem.orgaoJulgador.idOrgaoJulgador != :idOrgaoJulgador");
		sbQuery.append(" 							)");
		sbQuery.append("   AND magistrado.idUsuario NOT IN (");
		sbQuery.append("                  SELECT sessaoCompOrdem.magistradoSubstitutoSessao FROM SessaoComposicaoOrdem sessaoCompOrdem ");
		sbQuery.append("					     WHERE sessaoCompOrdem.sessao.idSessao = :idSessao");
		sbQuery.append(" 						    )");
		sbQuery.append(" ORDER BY user_local_magistrado.magistradoTitular DESC, user_local_magistrado.dtInicio");
		
		Query query = getEntityManager().createQuery(sbQuery.toString());
		query.setParameter("idSessao", idSessao);
		query.setParameter("idOrgaoJulgador", idOrgaoJulgador);
		query.setParameter("dataSessao", dataAberturaSessao);
		query.setParameter("papel", Papeis.MAGISTRADO);

				
		return query.getResultList();
	}
	
	/**
	 * Obtem os magistrados em orderm ativos para o orgao julgador
	 * 
	 * @param idOrgaoJulgador 
	 * @return Lista de magistrados para o OJ
	 */
	@SuppressWarnings("unchecked")
	public List<PessoaMagistrado> obterAptos(Integer idOrgaoJulgador){
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" SELECT magistrado FROM UsuarioLocalizacaoMagistradoServidor user_local_magistrado, PessoaMagistrado magistrado");
		sbQuery.append("   JOIN user_local_magistrado.usuarioLocalizacao user_local");
		sbQuery.append("   JOIN user_local.usuario usuario");
		sbQuery.append(" WHERE magistrado.idUsuario = usuario.idUsuario");
		sbQuery.append("   AND (user_local_magistrado.dtFinal IS NULL)");
		sbQuery.append("   AND user_local_magistrado.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador");
		sbQuery.append(" ORDER BY user_local_magistrado.magistradoTitular DESC, user_local_magistrado.dtInicio");
		
		Query query = getEntityManager().createQuery(sbQuery.toString());
		query.setParameter("idOrgaoJulgador", idOrgaoJulgador);
		return query.getResultList();
	}

}
