package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.business.dao.TagDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioPesquisa;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TagDTO;
import br.jus.pje.nucleo.entidades.Tag;
import br.jus.pje.nucleo.entidades.TagMin;

@Name(TagManager.NAME)
public class TagManager extends BaseManager<Tag> {

	public static final String NAME = "tagManager";


	@In
	private TagDAO tagDAO;

	@Override
	protected TagDAO getDAO() {
		return this.tagDAO;
	}

	public List<Tag> listarTagsParaAutomacao(List<Integer> idsLocalizacoes){
		return tagDAO.listarTagsParaAutomacao(idsLocalizacoes);
	}
	
	/**
	 * Retorna as tags disponíveis para automação ainda não associadas ao processo informado.
	 * @param idsLocalizacoes
	 * @param idProcessoTrf
	 * @return
	 */
	public List<Tag> listarTagsParaAutomacao(List<Integer> idsLocalizacoes, Integer idProcessoTrf){
		return tagDAO.listarTagsParaAutomacao(idsLocalizacoes, idProcessoTrf);
	}
	
	/**
	 * Retorna todas as subetiquetas de uma etiqueta (filhas e netas).
	 * @param idTag
	 */
	public List<TagDTO> listarTodasSubetiquetasDTO(Integer idTag) {
		return tagDAO.listarTodasSubetiquetasDTO(idTag);
	}
	
	/**
	 * Retorna a etiqueta pai da etiqueta informada.
	 * @param idTag
	 * @return null se a etiqueta informda não possuir pai.
	 */
	public TagDTO retornaEtiquetaPaiDTO(Integer idTag) {
		return tagDAO.retornaEtiquetaPaiDTO(idTag);
	}
	
	/**
	 * Atualiza o campo ds_tag_completo da tabela tb_tag.
	 */
	public int atualizarNomeTagCompleto(Integer idTag, String nomeTagCompleto) {
		return tagDAO.atualizarNomeTagCompleto(idTag, nomeTagCompleto);
	}
	
	/**
	 * Desvincula as etiquetas de automação (ou seja, as que não tem usuário associado), do processo informado.
	 * @param idProcessoTrf
	 * @return
	 */
	public int excluirVinculosProcessoTag(Integer idProcessoTrf) {
		return tagDAO.excluirVinculosProcessoTag(idProcessoTrf);
	}
	
	public Tag atualizarTag(TagDTO tagDTO) throws PJeBusinessException {
		Tag tag = tagDAO.find(tagDTO.getId());
		
		if(tag != null && tag.getId() != 0) {
        	tag.setNomeTag(tagDTO.getNomeTag());
            if ( tag.getPai()!=null ) {
            	if ( tagDAO.isNeto(tag.getPai()) ) {
        			return null;
        		}
            	Tag tagPai = tagDAO.find(tag.getPai().getId());
            	tag.setNomeTagCompleto(tagPai.getNomeTagCompleto()+" > "+tag.getNomeTag());
            } else {
            	tag.setNomeTagCompleto(tag.getNomeTag());
            }
		}
		
		return tag;
	}

	/**
	 * 
	 * @param nomeTag
	 * @param idLocalizacao
	 * @param tagPai
	 * @return
	 */
	public TagMin criarTag(String nomeTag, Integer idLocalizacao, Tag tagPai){
		CriterioPesquisa query = new CriterioPesquisa();
		query.setTags(new String[]{nomeTag});
		if(tagPai != null){
			query.setIdTagPai(tagPai.getId());
		}
		List<TagMin> tags = tagDAO.listarTagsUsuario(query,idLocalizacao);
		if(tags.size() == 0){
			Tag tagFull = null;
			if(tagPai == null){
				tagFull = new Tag(nomeTag,nomeTag,idLocalizacao, null);
			} else {
				if(tagDAO.isNeto(tagPai)){
					return null;
				}
				tagFull = new Tag(nomeTag, tagPai.getNomeTagCompleto()+" > "+nomeTag, idLocalizacao, tagPai.getId());
			}
			tagDAO.persist(tagFull);
			tagDAO.flush();
			return tagDAO.recuperarTagMinPorId(tagFull.getId());
		}else{
			return tags.get(0);
		}
	}

	public Tag criarTag(TagDTO tagDTO) {
        Integer idLocalizacaoFisica = Authenticator.getIdLocalizacaoFisicaAtual();
        Tag tag = new Tag(tagDTO.getId(), tagDTO.getNomeTag(), tagDTO.getNomeTagCompleto(), idLocalizacaoFisica, tagDTO.getIdPai()); 
		tag = tagDAO.persist(tag);
		tagDAO.flush();
		return tag; 
	}

	public List<TagMin> recuperarTagMinPorLocalizacao(String nomeTag, Integer idLocalizacao){
		return tagDAO.recuperarTagMinPorLocalizacao(nomeTag, idLocalizacao);
	}
}
