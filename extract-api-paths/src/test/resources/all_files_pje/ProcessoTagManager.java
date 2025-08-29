package br.jus.cnj.pje.nucleo.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ProcessoTagDAO;
import br.jus.cnj.pje.business.dao.TagDAO;
import br.jus.cnj.pje.business.dao.TagFavoritaDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioPesquisa;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.Etiqueta;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.EtiquetaProcesso;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.PagedQueryResult;
import br.jus.cnj.pje.webservice.json.InformacaoUsuarioSessao;
import br.jus.pje.nucleo.entidades.ProcessoTag;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Tag;
import br.jus.pje.nucleo.entidades.TagFavorita;
import br.jus.pje.nucleo.entidades.TagHierarquia;
import br.jus.pje.nucleo.entidades.TagMin;

@Name(ProcessoTagManager.NAME)
public class ProcessoTagManager extends BaseManager<ProcessoTag>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "processoTagManager";

	@In
	ProcessoTagDAO processoTagDAO;
	
	@In
	TagDAO tagDAO;
	
	@In
	TagFavoritaDAO tagFavoritaDAO;
	
	@In
	TagManager tagManager;

	@Override
	protected ProcessoTagDAO getDAO() {
		return processoTagDAO;
	}
	
	public ProcessoTag associarProcessoTag(Long idProcesso, Tag tag, Integer idUsuario) throws PJeBusinessException{
		if(existeTag(idProcesso,tag.getNomeTag(), tag.getIdLocalizacao())){
			throw new PJeBusinessException("Processo já possui a tag"+ tag.getNomeTag());
		}
		
		TagMin tagMin = new TagMin();
		tagMin.setId(tag.getId());
		tagMin.setIdLocalizacao(tag.getIdLocalizacao());
		if(tag.getPai() != null){
			tagMin.setIdTagPai(tag.getPai().getId());			
		}
		tagMin.setNomeTag(tag.getNomeTag());
		tagMin.setNomeTagCompleto(tag.getNomeTagCompleto());
		
		ProcessoTag ptag = new ProcessoTag();
		ptag.setIdProcesso(idProcesso);
		ptag.setTag(tagMin);
		ptag.setIdUsuarioInclusao(idUsuario);

		persistAndFlush(ptag);
		return ptag;
	}	
	
    public ProcessoTag criarProcessoTag(Long idProcesso, String nomeTag, Integer idLocalizacao, Integer idUsuario) throws PJeBusinessException {
        if(existeTag(idProcesso,nomeTag,idLocalizacao)){
            throw new PJeBusinessException("Tag já existente para o processo.");
        }
        return inserirProcessoTag(idProcesso, nomeTag, idLocalizacao, idUsuario, true);
    }
    
	public ProcessoTag inserirProcessoTag(Long idProcesso, String nomeTag, Integer idLocalizacao, Integer idUsuario, boolean flush) throws PJeBusinessException {
		TagMin t = tagManager.criarTag(nomeTag, idLocalizacao, null);
        ProcessoTag ptag = new ProcessoTag(idProcesso,t,idUsuario);

		try{
			ProcessoTag retorno = processoTagDAO.persist(ptag);
			processoTagDAO.flush();
			if (flush) {
				processoTagDAO.flush();
			}
			return retorno;
        }
        catch(Exception exception){
            throw new PJeBusinessException("Tag j existente para o processo.");
        }
    }


	public Boolean existeTag(Long idProcesso, String nomeTag, Integer idLocalizacao) {
        if(idProcesso == null || nomeTag == null){
            return false;
        }
        Boolean existeTag = processoTagDAO.existeTag(idProcesso, nomeTag, idLocalizacao);
        return existeTag;
    }

	public List<ProcessoTag> recuperaProcessoTag(Long idProcesso, String nomeTag, Integer idLocalizacao) {
		if(idProcesso == null || nomeTag == null){
			return null;
		}
		return processoTagDAO.recuperaProcessoTag(idProcesso, nomeTag, idLocalizacao);
	}

	public void removerTag(Long idProcesso, TagMin tag, boolean flush) {
		getDAO().removeTag(idProcesso, tag, flush);
	}
	
	public void removerTag(Long idProcesso, TagMin tag) {
		getDAO().removeTag(idProcesso, tag, true);
	}
	
	/***
	 * Método responsável por remover a tag do processo de acordo com os
	 * parâmetros passados: id do processo, id da localizacao física do usuario.
	 * 
	 * @param processo
	 * @param idLocalizacao
	 * @throws PJeBusinessException
	 */
	public void removerTag(ProcessoTrf processo, Integer idLocalizacao) throws PJeBusinessException {
		removerTag(processo, idLocalizacao, true);
	}
	
	public void removerTag(ProcessoTrf processo, Integer idLocalizacao, boolean flush) throws PJeBusinessException {
		if (processo == null || processo.getIdProcessoTrf() == 0) {
			throw new PJeBusinessException("O processo é nulo ou seu id é igual a zero.");
		}
		getDAO().removerTag(processo.getIdProcessoTrf(), idLocalizacao, flush);
	}
	
	public void removerTag(Long idProcesso, Integer idTag, boolean flush) {
		TagMin t = tagDAO.recuperarTagMinPorId(idTag);
		processoTagDAO.removeTag(idProcesso, t, flush);
	}
	
    public void removerTag(Long idProcesso, Integer idTag) {
    	removerTag(idProcesso, idTag, true);
    }

	public PagedQueryResult<Tag> listarTagsArvoreUsuarioPaginado(CriterioPesquisa query, Integer idLocalizacao) {
        Long qtd = tagDAO.listarQtdTagsArvoreUsuario(query,idLocalizacao);
        List<Tag> tags = tagDAO.listarTagsArvoreUsuario(query,idLocalizacao);

        return new PagedQueryResult<Tag>(qtd,tags);
    }
	
	public PagedQueryResult<Etiqueta> listarEtiquetasUsuario(CriterioPesquisa query, Integer idLocalizacao, Integer idUsuario){
		Long qtd = tagDAO.listarQtdEtiquetasUsuario(query, idLocalizacao, idUsuario);
		List<Etiqueta> etiquetas = tagDAO.listarEtiquetasUsuario(query, idLocalizacao, idUsuario);
		
		return new PagedQueryResult<Etiqueta>(qtd, etiquetas);
	}
	
	public List<TagMin> listarTagsFavoritasUsuario(CriterioPesquisa query, Integer idLocalizacao, Integer idUsuario) {
        List<TagMin> tags = tagFavoritaDAO.listarTagsFavoritasUsuario(query, idLocalizacao, idUsuario);

        return tags;
    }
	
	public Tag listarTagParaEdicao(Integer idTag) {
        return tagDAO.recuperarPorIdCompleto(idTag);
    }
	
	public Tag alterarTag(Tag tag) {
        if ( tag.getPai()!=null ) {
        	if ( tagDAO.isNeto(tag.getPai()) ) {
    			return null;
    		}
        	Tag tagPai = tagDAO.find(tag.getPai().getId());
        	tag.setNomeTagCompleto(tagPai.getNomeTagCompleto()+" > "+tag.getNomeTag());
        } else {
        	tag.setNomeTagCompleto(tag.getNomeTag());
        }
        
        Tag tagAtual = tagDAO.recuperarPorIdCompleto(tag.getId());
        tag.setIdLocalizacao(tagAtual.getIdLocalizacao());
        
        this.tagDAO.merge(tag);
        return tagDAO.recuperarPorIdCompleto(tag.getId());
    }
	
	public List<TagMin> listarTagsUsuario(CriterioPesquisa query, Integer idLocalizacao) {
        List<TagMin> tags = tagDAO.listarTagsUsuario(query,idLocalizacao);

        return tags;
    }
	
	public void excluirHierarquiaTag(Integer idTag){
    	TagHierarquia th = tagDAO.recuperarTagHierarquiaPorId(idTag);
    	for (TagHierarquia filho: th.getFilhos()) {
    		for (TagHierarquia neto: filho.getFilhos()) {
    			excluirTag(neto.getId());
    		}
    		excluirTag(filho.getId());
    	}
    	excluirTag(th.getId());
    }
	
	public void excluirTag(Integer idTag){
        tagFavoritaDAO.excluirPorIdTag(idTag);
        processoTagDAO.excluirPorIdTag(idTag);
        tagDAO.excluirPorId(idTag);
    }

	public void adicionarTagSessaoUsuario(Integer idTag, InformacaoUsuarioSessao usuarioSessao) {
        TagMin tag = tagDAO.recuperarTagMinPorId(idTag);
        if(tag == null){
            throw new RuntimeException("Tag não encontrada");
        }

        if(!tagFavoritaDAO.existeTagFavorita(usuarioSessao.getIdUsuario(),tag)){
            TagFavorita t = new TagFavorita();
            t.setIdUsuario(usuarioSessao.getIdUsuario());
            t.setTag(tag);
            tagFavoritaDAO.persist(t);
        }
        else{
            throw new RuntimeException("Etiqueta já favorita");
        }
    }
	
	public void removerTagSessaoUsuario(Integer idTag, InformacaoUsuarioSessao usuarioSessao) {
        TagFavorita t = tagFavoritaDAO.getTagFavorita(usuarioSessao.getIdUsuario(),idTag);
        if(t == null){
            throw new RuntimeException("Etiqueta não encontrada");
        }
        tagFavoritaDAO.remove(t);
    }
	
    public List<EtiquetaProcesso> listarTags(Long idProcesso, Integer idLocalizacao, String nomeTag){
        Set<Long> idsProcesso = new HashSet<Long>(1);
        idsProcesso.add(idProcesso);
        return processoTagDAO.listarTags(idsProcesso,idLocalizacao,nomeTag);
    }
    
    public List<EtiquetaProcesso> listarTags(Set<Long> idsProcesso, Integer idLocalizacao, String nomeTag){
        return processoTagDAO.listarTags(idsProcesso,idLocalizacao,nomeTag);
    }
	
	
	public List<ProcessoTag> listarTags(Long idProcesso){
        return getDAO().listarTags(idProcesso);
    }	
}
