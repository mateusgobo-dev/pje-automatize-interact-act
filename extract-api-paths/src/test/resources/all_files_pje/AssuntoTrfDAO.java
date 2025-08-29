package br.jus.cnj.pje.business.dao;

import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.AssuntoTrfDTO;
import br.jus.pje.nucleo.entidades.AssuntoTrf;


@Name(AssuntoTrfDAO.NAME)
public class AssuntoTrfDAO extends BaseDAO<AssuntoTrf> {
    public static final String NAME = "assuntoTrfDAO";

    @Override
    public Object getId(AssuntoTrf e) {
        return e.getIdAssuntoTrf();
    }

    @SuppressWarnings("unchecked")
    public List<AssuntoTrf> carregarAssuntosJudiciais(Integer idJurisdicao,
        Integer idClasseJudicial, List<String> codigoAssuntoList) {
        String sql = "select distinct(o) from AssuntoTrf o " +
            "inner join o.competenciaClasseAssuntoList cca " +
            "inner join cca.competencia comp " +
            "inner join cca.classeAplicacao.classeJudicial cla " +
            "where o.ativo = true " + 
            "and cca.competencia.ativo = true " + "and cla.ativo = true " +
            "and cla.idClasseJudicial = :idClasse " +
            "and o.codAssuntoTrf in (:codAssuntoList) " +
            "and cla.idClasseJudicial = :idClasse " +
            "and (comp in (select c from Competencia c " +
            "              inner join c.orgaoJulgadorCompetenciaList ojComp " +
            "              inner join ojComp.orgaoJulgador oj " +
            "              where oj.ativo = true " +
            "              and oj.jurisdicao.idJurisdicao = :idJurisdicao " +
            "              and ojComp.competencia = comp " +
            "              and current_date >= ojComp.dataInicio " +
            "              and (ojComp.dataFim >= current_date or ojComp.dataFim is null)) " +
            "     or " + "     comp in (select c from Competencia c " +
            "              inner join c.orgaoJulgadorColegiadoCompetenciaList ojCompC " +
            "              inner join ojCompC.orgaoJulgadorColegiado ojc " +
            "              where ojc.ativo = true " +
            "              and ojc.jurisdicao.idJurisdicao = :idJurisdicao " +
            "              and ojCompC.competencia = comp " +
            "              and current_date >= ojCompC.dataInicio " +
            "              and (ojCompC.dataFim >= current_date or ojCompC.dataFim is null))) ";

        Query query = EntityUtil.getEntityManager().createQuery(sql);
        query.setParameter("idJurisdicao", idJurisdicao);
        query.setParameter("idClasse", idClasseJudicial);
        query.setParameter("codAssuntoList", codigoAssuntoList == null || codigoAssuntoList.size() == 0 ? null : codigoAssuntoList);

        List<AssuntoTrf> assuntoTrfList = query.getResultList();

        return assuntoTrfList;
    }

    /**
     * Recupera um assunto por seu código.
     *
     * @param codigoInterno o código do assunto na instalação.
     * @return o assunto, se existente
     * @throws NoResultException caso não exista assunto com o código indicado na instalação.
     * @throws NonUniqueResultException caso haja mais de um assunto com o código indicado na instalação.
     */
    public AssuntoTrf findByCodigo(String codigoInterno, Boolean ativos) {
        StringBuilder query = new StringBuilder("SELECT a FROM AssuntoTrf AS a WHERE a.codAssuntoTrf = :codigo ");
        
        if(ativos != null) {
        	query.append(" AND a.ativo = :ativo ");
        }
        
        Query q = entityManager.createQuery(query.toString());
        q.setParameter("codigo", codigoInterno.trim());
        
        if(ativos != null) {
        	q.setParameter("ativo", ativos);
        }

        return (AssuntoTrf) q.getSingleResult();
    }
    
    public AssuntoTrf findByCodigo(String codigoInterno) {
    	return this.findByCodigo(codigoInterno, null);
    }
    
    public AssuntoTrf findByNome(String nome) {
        return EntityUtil.getSingleResult(this.entityManager.createQuery(
        	"FROM AssuntoTrf WHERE assuntoTrf = :nome").setParameter("nome", nome));
    }
    
    /**
     * Verifica se um dado assunto tem alguns filhos.
     * 
     * @param assunto assunto a ser pesquisado.
     * @return true, se o assunto for pai de algum outro assunto.
     */
    public boolean hasChildren(AssuntoTrf assunto){
    	String query = "SELECT COUNT(a.codAssuntoTrf) FROM AssuntoTrf AS a WHERE a.assuntoTrfSuperior = :assunto";
    	Query q = entityManager.createQuery(query);
    	q.setParameter("assunto", assunto);
    	Number n = (Number) q.getSingleResult();
    	return n.intValue() > 0;
    }
    
    @SuppressWarnings("unchecked")
    public List<AssuntoTrfDTO> findAllAssuntoTrfDTO(){
    	StringBuilder sb = new StringBuilder("");
    	sb.append("SELECT new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.AssuntoTrfDTO(a.idAssuntoTrf, a.codAssuntoTrf, a.assuntoCompleto) ");
    	sb.append("FROM AssuntoTrf a ");
    	sb.append("WHERE a.ativo=true ");
    	sb.append("ORDER BY a.assuntoCompleto ");
    	
    	Query q = this.entityManager.createQuery(sb.toString());
    	
    	return q.getResultList();
    }
    
    /**
     * Retorna todos os assuntos "folha" (que não possuem filhos) ativos.
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<AssuntoTrfDTO> findAssuntoTrfFolhaDTO(){
    	StringBuilder sb = new StringBuilder("");
    	sb.append("SELECT new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.AssuntoTrfDTO(a.idAssuntoTrf, a.codAssuntoTrf, a.assuntoCompleto) ");
    	sb.append("FROM AssuntoTrf a ");
    	sb.append("WHERE a.ativo=true AND a.possuiFilhos=false ");
    	sb.append("ORDER BY a.assuntoCompleto ");
    	
    	Query q = this.entityManager.createQuery(sb.toString());
    	
    	return q.getResultList();
    }

	public Integer obtemAreaDireito(AssuntoTrf assuntoTrf) {
		String jpaql = "SELECT a.cd_assunto_trf FROM tb_assunto_trf a JOIN tb_assunto_trf b ON "
				+ "(b.nr_faixa_inferior <= a.nr_faixa_inferior AND b.nr_faixa_superior >= a.nr_faixa_superior AND b.in_materia_direito = true ) "
				+ "WHERE a.id_assunto_trf = :idAssuntoTrf";

		Query query = entityManager.createNativeQuery(jpaql);
		query.setParameter("idAssuntoTrf", assuntoTrf.getIdAssuntoTrf());

		return Integer.parseInt((String) query.getSingleResult());
	}
	
	    @SuppressWarnings("unchecked")
    public List<AssuntoTrfDTO> findAllAssuntoTrfDTOByListCodigo(List<String> listaCodigos){
    	StringBuilder sb = new StringBuilder("");
    	sb.append("SELECT new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.AssuntoTrfDTO(a.idAssuntoTrf, a.codAssuntoTrf, a.assuntoTrf) ");
    	sb.append("FROM AssuntoTrf a ");
    	sb.append("where a.codAssuntoTrf in (:listaCodigos) ");
    	Query q = this.entityManager.createQuery(sb.toString());
    	q.setParameter("listaCodigos", listaCodigos);
    	return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<AssuntoTrf> findAllAssuntoTrfByListCodigo(List<String> listaCodigos){
    	StringBuilder sb = new StringBuilder("");
    	sb.append("SELECT a ");
    	sb.append("FROM AssuntoTrf a ");
    	sb.append("where a.codAssuntoTrf in (:listaCodigos) ");
    	Query q = this.entityManager.createQuery(sb.toString());
    	q.setParameter("listaCodigos", listaCodigos);
    	return q.getResultList();
    }

    @SuppressWarnings("unchecked")
	public List<AssuntoTrf> obtemAssuntosJudiciais(Set<Integer> ids) {
    	String sql = "SELECT a from AssuntoTrf a WHERE a.idAssuntoTrf IN (:ids)";
    	Query q = this.entityManager.createQuery(sql);
    	q.setParameter("ids", ids);
    	return q.getResultList();
    }
}
