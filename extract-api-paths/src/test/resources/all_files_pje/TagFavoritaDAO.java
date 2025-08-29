package br.jus.cnj.pje.business.dao;

import java.util.Arrays;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioPesquisa;
import br.jus.pje.nucleo.entidades.TagFavorita;
import br.jus.pje.nucleo.entidades.TagMin;

@Name("tagFavoritaDAO")
public class TagFavoritaDAO extends BaseDAO<TagFavorita> {


    public Boolean existeTagFavorita(Integer idUsuario, TagMin tag){
        String hql = "select count(o) from TagFavorita o where o.idUsuario = :idUsuario and o.tag = :tag";
        Query q = getEntityManager().createQuery(hql);
        q.setParameter("idUsuario",idUsuario);
        q.setParameter("tag",tag);
        q.setMaxResults(1);
        Long ret = (Long) q.getSingleResult();
        return ret > 0;

    }

    public TagFavorita getTagFavorita(Integer idUsuario, Integer idTag){
        String hql = "select o from TagFavorita o where o.idUsuario = :idUsuario and o.tag.id = :tag";
        Query q = getEntityManager().createQuery(hql);
        q.setParameter("idUsuario",idUsuario);
        q.setParameter("tag",idTag);
        q.setMaxResults(1);
        try{
            return (TagFavorita) q.getSingleResult();

        }
        catch(NoResultException e){
            return null;
        }

    }

    @SuppressWarnings("unchecked")
	public List<TagMin> listarTagsFavoritasUsuario(CriterioPesquisa query, Integer idLocalizacao, Integer idUsuario) {
        StringBuilder hql = new StringBuilder();
        hql.append("select t from TagFavorita o join o.tag t where 1=1 ");

        if(query != null){
            if(StringUtils.isNotEmpty(query.getTagsString())){
                hql.append("and lower(to_ascii(t.nomeTagCompleto)) like '%' || lower(to_ascii(:nomeTag)) || '%' ");
            }
            if(query.getTags() != null && query.getTags().length > 0){
                hql.append("and t.nomeTagCompleto in (:tags) ");
            }
        }

        if(idUsuario != null){
            hql.append("and o.idUsuario = :idUsuario ");
        }

        if(idLocalizacao != null){
            hql.append("and t.idLocalizacao = :idLocalizacao");
        }

        hql.append(" order by t.nomeTag");

        Query q = getEntityManager().createQuery(hql.toString());

        if(idUsuario != null){
            q.setParameter("idUsuario", idUsuario);
        }

        if(idLocalizacao != null){
            q.setParameter("idLocalizacao", idLocalizacao);
        }

        if(query != null){
            if(StringUtils.isNotEmpty(query.getTagsString())){
                q.setParameter("nomeTag", query.getTagsString());
            }
            if(query.getTags() != null && query.getTags().length > 0){
                q.setParameter("tags", Arrays.asList(query.getTags()));
            }
            if(query.getPage() != null){
                q.setFirstResult(query.getPage());
            }
            if(query.getMaxResults() != null){
                q.setMaxResults(query.getMaxResults());
            }
        }
        return q.getResultList();
    }

	public void excluirPorIdTag(Integer idTag) {
		List<TagFavorita> tagsFavoritas = getTagFavoritaByTag(idTag);
		if (tagsFavoritas != null && !tagsFavoritas.isEmpty()) {
			for (TagFavorita tagFavorita : tagsFavoritas) {
				this.getEntityManager().remove(tagFavorita);
			}
		}
	}
    
	/**
	 * Obtem a listas de tags favoritas a partir de uma tag
	 * 
	 * @param idTag
	 * @return
	 */
	private List<TagFavorita> getTagFavoritaByTag(Integer idTag) {
		String hql = "select o from TagFavorita o where o.tag.id = :tag";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("tag", idTag);
		try {
			return q.getResultList();

		} catch (NoResultException e) {
			return null;
		}

	}
    
  
	@Override
	public Object getId(TagFavorita e) {
		return e.getId();
	}
}

