package br.jus.cnj.pje.business.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.EtiquetaProcesso;
import br.jus.pje.nucleo.entidades.ProcessoTag;
import br.jus.pje.nucleo.entidades.TagMin;

@Name(ProcessoTagDAO.NAME)
public class ProcessoTagDAO extends BaseDAO<ProcessoTag>{

	public static final String NAME = "processoTagDAO";
	
	@Override
	public Object getId(ProcessoTag e) {
		return null;
	}

    @SuppressWarnings("unchecked")
	public Boolean existeTag(Long idProcesso, String nomeTag, Integer idLocalizacao) {
        StringBuilder hql = new StringBuilder();
        hql.append( "select count(o) from ProcessoTag o " +
                    "join o.tag t " +
                    "where lower(to_ascii(t.nomeTagCompleto)) = lower(to_ascii(:nomeTag)) and o.idProcesso=:idProcesso ");
        if(idLocalizacao != null){
            hql.append("and t.idLocalizacao = :idLocalizacao ");
        }
        Query q = getEntityManager().createQuery(hql.toString());
        q.setParameter("nomeTag", nomeTag);
        q.setParameter("idProcesso", idProcesso);
        if(idLocalizacao != null){
            q.setParameter("idLocalizacao", idLocalizacao);
        }
        q.setMaxResults(1);
        List<Long> ret = q.getResultList();
        Long l = ret.get(0);
        return l > 0;
    }

    public void removeTag(Long idProcesso, TagMin tag, boolean flush) {
        StringBuilder hql = new StringBuilder();
        hql.append("select o from ProcessoTag o where o.tag = :tag and o.idProcesso=:idProcesso");
        Query q = getEntityManager().createQuery(hql.toString());
        q.setParameter("tag", tag);
        q.setParameter("idProcesso", idProcesso);
        q.setMaxResults(1);

        List<ProcessoTag> resultados = q.getResultList();
		for(ProcessoTag resultado : resultados) {
			this.getEntityManager().remove(resultado);
		}
		if (flush) {
			this.getEntityManager().flush();
		}
    }
    
    /**
	 * Método responsável por remover a tag do processo de acordo com os
	 * parâmetros passados: id do processo, id da localização física do usuário e da tag
	 * 
     * @param idProcesso
     * @param idLocalizacao
     */
	public void removerTag(Integer idProcesso, Integer idLocalizacao, boolean flush) {
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT pt FROM ProcessoTag AS pt ");
		hql.append("WHERE pt.idProcesso = :idProcesso ");
		hql.append("AND EXISTS ( ");
		hql.append("	SELECT 1 FROM Tag AS tag ");
		hql.append("	WHERE tag = pt.tag ");
		if (idLocalizacao != null) {
			hql.append("	AND tag.idLocalizacao = :idLocalizacao ");			
		}
		hql.append(" )");
		
		Query q = entityManager.createQuery(hql.toString());
		q.setParameter("idProcesso", idProcesso.longValue());
		if (idLocalizacao != null) {
			q.setParameter("idLocalizacao", idLocalizacao);
		}
		
		List<ProcessoTag> resultados = q.getResultList();
		for(ProcessoTag resultado : resultados) {
			this.getEntityManager().remove(resultado);
		}
		if (flush) {
			this.getEntityManager().flush();
		}
	}    


	/**
	 * Retorna todas as tags do processo cujo nome da tag contenha alguma parte do texto indicado 
	 * e cuja tag esteja na mesma localização física da pessoa ou que a tag seja uma tag de sistema
	 * 
	 * @param idProcesso
	 * @param idLocalizacao
	 * @param nomeTag
	 * @return
	 */
    @SuppressWarnings("unchecked")
	public List<EtiquetaProcesso> listarTags(Set<Long> idProcesso, Integer idLocalizacao, String nomeTag) {
        StringBuilder hql = new StringBuilder();
        hql.append("select new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.EtiquetaProcesso(t.id, t.nomeTag, t.nomeTagCompleto, o.idUsuarioInclusao, o.idProcesso) ");
		hql.append(" from ProcessoTag o join o.tag t where 1 = 1 ");

        if(idProcesso != null && idProcesso.size() > 0){
            hql.append("and o.idProcesso in (:idProcesso) ");
        }
        if(nomeTag != null){
            hql.append("and lower(to_ascii(t.nomeTag)) like '%' || lower(to_ascii(:nomeTag)) || '%' ");
        }

        hql.append("and ( ");
        hql.append(" t.deSistema = true ");
        hql.append(" OR t.visivelPublicamente = true ");

        if(idLocalizacao != null){
            hql.append(" OR t.idLocalizacao = :idLocalizacao ");
        }
        hql.append(" ) ");

        hql.append("order by lower(to_ascii(t.nomeTag))");

        Query q = getEntityManager().createQuery(hql.toString());

        if(idProcesso != null && idProcesso.size() > 0){
            q.setParameter("idProcesso", idProcesso);
        }
        if(nomeTag != null){
            q.setParameter("nomeTag", nomeTag);
        }

        if(idLocalizacao != null){
            q.setParameter("idLocalizacao", idLocalizacao);
        }

        return q.getResultList();
    }

    
	public void excluirPorIdTag(Integer idTag) {
		List<ProcessoTag> processosTag = getProcessoTagByTag(idTag);
		if (processosTag != null && !processosTag.isEmpty()) {
			for (ProcessoTag processoTag : processosTag) {
				this.getEntityManager().remove(processoTag);
			}
		}
	}
    
	/**
	 * Obtem a lista de processotags para um tag
	 * 
	 * @param idTag
	 * @return
	 */
	private List<ProcessoTag> getProcessoTagByTag(Integer idTag) {
		String hql = "select o from ProcessoTag o where o.tag.id = :tag";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("tag", idTag);
		try {
			return q.getResultList();
		} catch (NoResultException e) {
			return null;
		}

	}
    
 
    @SuppressWarnings("unchecked")
	public Map<Integer,BigInteger> listarQtdDeProcessosPorTag(List<TagMin> tags){
        Map<Integer,BigInteger> ret = new HashMap<Integer, BigInteger>(0);
        if(tags == null || tags.isEmpty()){
            return ret;
        }
        List<Integer> ids = new ArrayList<Integer>(tags.size());
        for(TagMin t : tags){
            ids.add(t.getId());
        }
        String sql = "select ptag.id_tag,count(id_processo) from tb_processo_tag ptag where ptag.id_tag in :tags group by ptag.id_tag";
        Query q = getEntityManager().createNativeQuery(sql);
        q.setParameter("tags",ids);
        List<Object[]> resultList = q.getResultList();
        for(Object[] obj : resultList){
            ret.put((Integer)obj[0],(BigInteger)obj[1]);
        }
        return ret;
    }

	@SuppressWarnings("unchecked")
	public List<ProcessoTag> listarTags(Long idProcesso) {
        StringBuilder hql = new StringBuilder();
        hql.append("select o from ProcessoTag o join fetch o.tag t where 1 = 1 ");

        if(idProcesso != null){
            hql.append("and o.idProcesso = :idProcesso ");
        }

        Query q = getEntityManager().createQuery(hql.toString());

        if(idProcesso != null){
            q.setParameter("idProcesso", idProcesso);
        }

        return q.getResultList();
    }
    
	@SuppressWarnings("unchecked")
	public List<ProcessoTag> recuperaProcessoTag(Long idProcesso, String nomeTag, Integer idLocalizacao) {
		StringBuilder hql = new StringBuilder();
		hql.append( "select o from ProcessoTag o " +
					"join o.tag t " +
					"where lower(to_ascii(t.nomeTagCompleto)) = lower(to_ascii(:nomeTag)) and o.idProcesso=:idProcesso ");
		if(idLocalizacao != null){
			hql.append("and t.idLocalizacao = :idLocalizacao ");
		}
		Query q = getEntityManager().createQuery(hql.toString());
		q.setParameter("nomeTag", nomeTag);
		q.setParameter("idProcesso", idProcesso);
		if(idLocalizacao != null){
			q.setParameter("idLocalizacao", idLocalizacao);
		}
		List<ProcessoTag> ret = q.getResultList();
		return ret;
	}
}
