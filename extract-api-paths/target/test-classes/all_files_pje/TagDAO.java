package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioPesquisa;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.Etiqueta;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TagDTO;
import br.jus.pje.nucleo.entidades.Tag;
import br.jus.pje.nucleo.entidades.TagHierarquia;
import br.jus.pje.nucleo.entidades.TagMin;

@Name("tagDAO")
public class TagDAO extends BaseDAO<Tag>{

	@Override
	public Integer getId(Tag e){
		return e.getId();
	}

	public List<Tag> listarTagsParaAutomacao(List<Integer> idsLocalizacoes){
		return listarTagsParaAutomacao(idsLocalizacoes, null);
    }
	
	@SuppressWarnings("unchecked")
	public List<Tag> listarTagsParaAutomacao(List<Integer> idsLocalizacoes, Integer idProcessoTrf){
        StringBuilder hql = new StringBuilder();
        
        hql.append("SELECT DISTINCT o FROM Tag o, FiltroTag f WHERE o.id = f.idTag ");
        
        if (idProcessoTrf!=null) {
        	hql.append("AND NOT EXISTS (");
        	hql.append("  SELECT 1 FROM ProcessoTag pt ");
        	hql.append("  WHERE o.id=pt.tag.id AND pt.idProcesso=:idProcessoTrf ");
        	hql.append(")");
        }
        
        if(CollectionUtilsPje.isNotEmpty(idsLocalizacoes)){
            String idsFormatado = StringUtils.join(idsLocalizacoes, ",");
            hql.append(" AND o.idLocalizacao IN ( ");
            hql.append(idsFormatado);
            hql.append( ") ");
        }
        
        hql.append("ORDER BY o.nomeTagCompleto ");
        
        Query q = getEntityManager().createQuery(hql.toString());
        
        if (idProcessoTrf!=null) {
        	q.setParameter("idProcessoTrf", new Long(idProcessoTrf.intValue()));
        }
        
        return q.getResultList();
    }
	
	public int excluirVinculosProcessoTag(Integer idProcessoTrf) {
		StringBuilder hql = new StringBuilder();
        hql.append("DELETE FROM ProcessoTag pt  ");
        hql.append("WHERE pt.idProcesso=:idProcessoTrf ");
        hql.append("AND idUsuarioInclusao IS NULL");
        Query q = getEntityManager().createQuery(hql.toString());
       	q.setParameter("idProcessoTrf", new Long(idProcessoTrf.intValue()));
        return q.executeUpdate();
	}
	
	public Long listarQtdTagsArvoreUsuario(CriterioPesquisa query, Integer idLocalizacao) {
        Query q = montarHQLTagsArvoreUsuario(query, idLocalizacao,true);
        return (Long)q.getResultList().get(0);
    }
	
	@SuppressWarnings("unchecked")
	public List<Tag> listarTagsArvoreUsuario(CriterioPesquisa query, Integer idLocalizacao) {
        Query q = montarHQLTagsArvoreUsuario(query, idLocalizacao,false);
        return q.getResultList();
    }

	public Long listarQtdEtiquetasUsuario(CriterioPesquisa query, Integer idLocalizacao, Integer idUsuario) {
        Query q = montarHQLEtiquetasUsuario(query, idLocalizacao, idUsuario, true);
        return (Long)q.getResultList().get(0);
    }
	
	@SuppressWarnings("unchecked")
	public List<Etiqueta> listarEtiquetasUsuario(CriterioPesquisa query, Integer idLocalizacao, Integer idUsuario) {
        Query q = montarHQLEtiquetasUsuario(query, idLocalizacao, idUsuario, false);
        return q.getResultList();
    }	
	
	private Query montarHQLEtiquetasUsuario(CriterioPesquisa query, Integer idLocalizacao, Integer idUsuario, boolean isCount){
		StringBuilder hql = new StringBuilder("");
		
		String sqlSelect = "SELECT new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.Etiqueta(o.id, o.nomeTag, o.nomeTagCompleto, o.idTagPai, tf.id) FROM TagFavorita tf ";
		
        if(isCount){
        	sqlSelect = "SELECT count(o) FROM TagFavorita tf ";
        }
    	hql.append(sqlSelect);
        hql.append("RIGHT JOIN tf.tag as o WITH tf.idUsuario = :idUsuario WHERE 1=1 ");
        
        if (isCriterioTagsStringPresente(query)) {
        	hql.append("and lower(to_ascii(o.nomeTag)) like '%' || lower(to_ascii(:nomeTag)) || '%' ");
        }
        
        if(isCriterioTagsPresente(query)) {
        	hql.append("and o.nomeTag in (:tags) ");
        }

        if(idLocalizacao != null){
            hql.append("and o.idLocalizacao = :idLocalizacao");
        }
        if(!isCount) {
            hql.append(" order by o.nomeTagCompleto");
        }

        Query q = getEntityManager().createQuery(hql.toString());
        this.adicionarParametrosQueryEtiquetasUsuario(query, idLocalizacao, idUsuario, isCount, q);

        return q;
	}

	private Query adicionarParametrosQueryEtiquetasUsuario(CriterioPesquisa query, Integer idLocalizacao, Integer idUsuario, Boolean isCount, Query q) {
		if(idUsuario != null){
            q.setParameter("idUsuario", idUsuario);
        }

        if(idLocalizacao != null){
            q.setParameter("idLocalizacao", idLocalizacao);
        }
        
        if (isCriterioTagsStringPresente(query)) {
            q.setParameter("nomeTag", query.getTagsString());
        }
        
        if(isCriterioTagsPresente(query)){
            q.setParameter("tags", Arrays.asList(query.getTags()));
        }

        if(query != null) {
            if(query.getPage() != null && !isCount){
                q.setFirstResult(query.getPage());
            }
            if(query.getMaxResults() != null && !isCount){
                q.setMaxResults(query.getMaxResults());
            }
        }
        
        return q;
	}
	
	private boolean isCriterioTagsStringPresente(CriterioPesquisa query) {
		return query != null && StringUtils.isNotEmpty(query.getTagsString());
	}
	
	private boolean isCriterioTagsPresente(CriterioPesquisa query) {
		return query != null && query.getTags() != null && query.getTags().length > 0;
	}
	
	private Query montarHQLTagsArvoreUsuario(CriterioPesquisa query, Integer idLocalizacao, Boolean isCount){
        StringBuilder hql = new StringBuilder();
        if(isCount){
            hql.append("select count(o) from Tag o where o.pai is null ");
        }
        else {
            hql.append("select o from Tag o where o.pai is null ");
        }

        if(query != null){
            if(StringUtils.isNotEmpty(query.getTagsString())){
                hql.append("and lower(to_ascii(o.nomeTag)) like '%' || lower(to_ascii(:nomeTag)) || '%' ");
            }
            if(query.getTags() != null && query.getTags().length > 0){
                hql.append("and o.nomeTag in (:tags) ");
            }
        }

        if(idLocalizacao != null){
            hql.append("and o.idLocalizacao = :idLocalizacao");
        }
        if(!isCount) {
            hql.append(" order by o.nomeTag");
        }

        Query q = getEntityManager().createQuery(hql.toString());
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
            if(query.getPage() != null && !isCount){
                q.setFirstResult(query.getPage());
            }
            if(query.getMaxResults() != null && !isCount){
                q.setMaxResults(query.getMaxResults());
            }
        }
        return q;
    }
	
	public Tag recuperarPorIdCompleto(Integer id) {
        String hql = "select o from Tag o left join fetch o.pai where o.id = :id";
        Query q = getEntityManager().createQuery(hql);
        q.setParameter("id",id);
        return (Tag) q.getSingleResult();
    }
	
	@SuppressWarnings("unchecked")
	public List<TagMin> listarTagsUsuario(CriterioPesquisa query, Integer idLocalizacaoFisica) {
        Query q = montarHQLTagsUsuario(query, idLocalizacaoFisica,false);
        return q.getResultList();
    }
	
	private Query montarHQLTagsUsuario(CriterioPesquisa query, Integer idLocalizacao,Boolean isCount){
        StringBuilder hql = new StringBuilder();
        if(isCount){
            hql.append("select count(o) from TagMin o where 1=1 ");
        }
        else {
            hql.append("select o from TagMin o where 1=1 ");
        }

        if(query != null){
            if(StringUtils.isNotEmpty(query.getTagsString())){
                hql.append("and lower(to_ascii(o.nomeTagCompleto)) like '%' || lower(to_ascii(:nomeTag)) || '%' ");
            }
            if(query.getTags() != null && query.getTags().length > 0){
                hql.append("and o.nomeTagCompleto in (:tags) ");
            }
            if ( query.getIdTagPai()!=null ) {
            	hql.append(" and o.idTagPai = :idTagPai ");
            }
        }

        if(idLocalizacao != null){
            hql.append("and o.idLocalizacao = :idLocalizacao");
        }
        if(!isCount) {
            hql.append(" order by o.nomeTag");
        }

        Query q = getEntityManager().createQuery(hql.toString());
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
            if(query.getIdTagPai() != null ){
                q.setParameter("idTagPai", query.getIdTagPai());
            }
            if(query.getPage() != null && !isCount){
                q.setFirstResult(query.getPage());
            }
            if(query.getMaxResults() != null && !isCount){
                q.setMaxResults(query.getMaxResults());
            }
        }
        return q;
    }
	
	public boolean isNeto(Tag tagPai) {
		TagHierarquia th = recuperarTagHierarquiaPorId(tagPai.getId());
		return th.getPai()!=null && th.getPai().getPai()!=null;
	}
	
	public TagHierarquia recuperarTagHierarquiaPorId(Integer idTag){
        return this.getEntityManager().find(TagHierarquia.class, idTag);
    }
	
	public TagMin recuperarTagMinPorId(Integer idTag){
        return this.getEntityManager().find(TagMin.class, idTag);
    }
	
	public void excluirPorId(Object id) {
		this.getEntityManager().remove(find(id));
	}

	@SuppressWarnings("unchecked")
	public List<TagMin> recuperarTagMinPorLocalizacao(String nomeTag, Integer idLocalizacao){
		StringBuilder hql = new StringBuilder();
		hql.append("select o from TagMin o where 1=1 ");
		if (nomeTag != null){
			hql.append("and o.nomeTag = :nomeTag ");
		}
		if (idLocalizacao != null){
			hql.append("and coalesce(o.idLocalizacao, :idLocalizacao) = :idLocalizacao ");
		}

		Query q = getEntityManager().createQuery(hql.toString());
		if(nomeTag != null){
			q.setParameter("nomeTag", nomeTag);
		}
		if(idLocalizacao != null){
			q.setParameter("idLocalizacao", idLocalizacao);
		}
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<TagDTO> listarTodasSubetiquetasDTO(Integer idTag) {
		StringBuilder hql = new StringBuilder();
		hql.append("with recursive subetiquetas (id, ds_tag, ds_tag_completo, id_tag_pai) as ( ");
		hql.append("		select t.id, t.ds_tag, t.ds_tag_completo, t.id_tag_pai ");
		hql.append("		from client.tb_tag t ");
		hql.append("		where t.id_tag_pai=:idTag ");
		hql.append("	  union all ");
		hql.append("	  	select o.id, o.ds_tag, o.ds_tag_completo, o.id_tag_pai ");
		hql.append("	  	from subetiquetas s, client.tb_tag o ");
		hql.append("	  	where o.id_tag_pai = s.id "); 
		hql.append(") ");
		hql.append("select * from subetiquetas");
		
		Query q = getEntityManager().createNativeQuery(hql.toString());
		q.setParameter("idTag", idTag);
		
		List<Object[]> list = q.getResultList();
		List<TagDTO> listDTO = new ArrayList<TagDTO>();
		for (Object[] tupla: list) {
			listDTO.add(new TagDTO(new Integer(tupla[0].toString()), tupla[1].toString(), tupla[2].toString(), new Integer(tupla[3].toString())));
		}
		
		return listDTO;
		
	}
	
	@SuppressWarnings("unchecked")
	public TagDTO retornaEtiquetaPaiDTO(Integer idTag) {
		StringBuilder hql = new StringBuilder();
		hql.append("select t.id, t.ds_tag, t.ds_tag_completo, t.id_tag_pai ");
		hql.append("from client.tb_tag t ");
		hql.append("where t.id = (select tt.id_tag_pai from client.tb_tag tt where tt.id=:idTag)");
		
		Query q = getEntityManager().createNativeQuery(hql.toString());
		q.setParameter("idTag", idTag);
		
		TagDTO etiquetaPai = null;
		List<Object[]> list = q.getResultList();
		for (Object[] tupla: list) {
			Integer idPai = tupla[3]!=null ? new Integer(tupla[3].toString()) : null;
			etiquetaPai = new TagDTO(new Integer(tupla[0].toString()), tupla[1].toString(), tupla[2].toString(), idPai);
			break;
		}
		
		return etiquetaPai;
		
	}
	
	public int atualizarNomeTagCompleto(Integer idTag, String nomeTagCompleto) {
		StringBuilder hql = new StringBuilder();
		hql.append("UPDATE Tag SET nomeTagCompleto=:nomeTagCompleto ");
		hql.append("WHERE id=:idTag");
		Query q = getEntityManager().createQuery(hql.toString());
		q.setParameter("idTag", idTag);
		q.setParameter("nomeTagCompleto", nomeTagCompleto);
		return q.executeUpdate();
	}
}
