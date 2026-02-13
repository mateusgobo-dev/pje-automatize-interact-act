/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.EventoDAO;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.MovimentoDTO;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de tratamento negocial da entidade {@link Evento}.
 * 
 * @author cristof
 *
 */
@Name(EventoManager.NAME)
public class EventoManager extends BaseManager<Evento>{
	public static final String NAME = "eventoManager";
	
	@In
	private ParametroService parametroService;
	
	@In
	private EventoDAO eventoDAO;

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected EventoDAO getDAO() {
		return eventoDAO;
	}
	
	/**
	 * Recupera a lista de tipos de movimentação que têm os códigos identificadores dados.
	 * 
	 * @param codigo os códigos identificadores (nacionais ou locais) dos tipos de movimentação 
	 * @return os tipos de movimentação
	 */
	public List<Evento> findByIds(String... codEvento) throws PJeBusinessException{
		return getDAO().findByIds(codEvento);
	}
	
	/**
	 * Recupera o tipo de movimentação ativo que tem o código identificador dado.
	 * 
	 * @param codigo o código identificador
	 * @return o tipo de movimentação
	 * @throws PJeDAOException caso não exista tipo de movimentação ativa com o código dado ou quando
	 * houver mais de um tipo ativo com o mesmo código
	 */
	public Evento findByCodigoCNJ(String codEvento) throws PJeDAOException{
		return eventoDAO.findByCodigoCNJ(codEvento);
	}
	
	/**
	 * Carrega os critérios básicos de pesquisa por tipos de movimentação.
	 * 
	 * Os critérios básicos são:
	 * <li>o tipo de movimentação deve estar ativo</li>
	 * <li>o tipo de movimentação deve ser aplicável para o segmento do Judiciário desta instalação,
	 * ou seja, deve haver uma aplicabilidade ativa vinculada ao tipo de justiça</li>
	 * 
	 * @param search o elemento de pesquisa que terá os critérios básicos incluídos
	 * @throws PJeBusinessException caso tenha havido a definição de algum caminho JavaBean
	 *  inválido nos critérios de pesquisa.
	 */
	private void loadBasicCriterias(Search search) throws PJeBusinessException{
		search.setDistinct(true);
		loadActiveCriterias(search);
	}
	
	/**
	 * Inclui, como critério, que o tipo de movimentação deve estar ativo e ser aplicável para o segmento do Judiciário 
	 * desta instalação, ou seja, deve haver uma aplicabilidade ativa vinculada ao tipo de justiça</li>
	 * 
	 * @param search o elemento de pesquisa que terá os critérios básicos incluídos
	 * @throws PJeBusinessException caso tenha havido a definição de algum caminho JavaBean
	 *  inválido nos critérios de pesquisa.
	 */
	private void loadActiveCriterias(Search search) throws PJeBusinessException{
		// O tipo de movimentação deve estar ativo
		addCriteria(search, Criteria.equals("ativo", true)); 
		// O tipo de movimentação deve ser aplicável para o órgão de justiça atual 
		addCriteria(search, Criteria.equals("aplicacaoMovimentoList.aplicabilidade.orgaoJustica", parametroService.valueOf(Parametros.TIPOJUSTICA)));   
	}
	
	/**
	 * Recupera os tipos de movimentação raiz de uma dada pesquisa.
	 * 
	 * @param search o objeto de consulta capaz de limitar o resultado da resposta
	 * @return a lista de tipos de movimentação raízes dos tipos de movimentação resultantes da pesquisa.
	 * @throws PJeBusinessException se tiver havido um erro ao recuperar os tipos de movimentação.
	 */
	public List<Evento> findRoots(Search search) throws PJeBusinessException{
		Search s = null; 
		if(search != null){
			try {
				s = search.copy();
			} catch (NoSuchFieldException e) {
				throw new PJeBusinessException("pje.dao.noSuchFieldException", e, e.getLocalizedMessage());
			}
		}else{
			s = new Search(Evento.class);
		}
		loadBasicCriterias(s);
		Set<Evento> ret = new HashSet<Evento>();
		s.setDistinct(true);
		List<Evento> tm = eventoDAO.list(s); 
		for(Evento e: tm){
			ret.add(e.getHierarchy().get(0));
		}
		return new ArrayList<Evento>(ret);
	}
	
	/**
	 * Recupera a lista de tipos de movimentações diretamente descententes de um dado 
	 * tipo de movimentação, filtrando a lista pelos critérios repassados.
	 * 
	 * @param parent o tipo de movimentação pai daquelas a serem recuperadas.
	 * @param grp o agrupamento de movimentações
	 * @param criterias os critérios a serem respeitados na lista
	 * @return a lista de movimentações diretamente descentendentes do tipo dado e que têm algum descendente que atende
	 * ao filtro
	 * @throws PJeBusinessException caso o agrupamento seja inexistente ou se houver algum erro ao tentar
	 * recuperar a lista
	 */
	public List<Evento> findFilteredChildren(Evento parent, Agrupamento grp, Criteria...criterias) throws PJeBusinessException{
		if(grp == null){
			throw new PJeBusinessException("pje.business.agrupamentoNulo");
		}
		boolean filtrar= criterias != null && criterias.length > 0;
		boolean parentDefined = parent != null;
		Set<Evento> ret = new HashSet<Evento>();
		List<Evento> tMovs = null;
		List<String> codigos = findBreadCrumb(parent, grp);
		if(codigos.isEmpty()){
			if(parentDefined){
				codigos.add(parent.getBreadcrumb());
			}else{
				return Collections.emptyList();
			}
		}
		List<Criteria> parents = new ArrayList<Criteria>(codigos.size());
		for(String codigo: codigos){
			parents.add(Criteria.startsWith("breadcrumb", codigo));
		}
		Search search = new Search(Evento.class);
		search.setRetrieveField(null);
		loadBasicCriterias(search);
		addCriteria(search, Criteria.or(parents.toArray(new Criteria[parents.size()])));
		if(filtrar){
			addCriteria(search, criterias);
		}
		tMovs = list(search);
		if(tMovs.size() == 1){
			ret.add(tMovs.get(0));
			return new ArrayList<Evento>(ret);
		}
		HashMap<String, Evento> map = new HashMap<String, Evento>();
		for(Evento tMov: tMovs){
			int deep = tMov.getHierarchy().size() - 1;
			String lbc = null;
			if(deep == 0){
				lbc = tMov.getBreadcrumb();
			}else{
				lbc = tMov.getBreadcrumb().substring(0, tMov.getBreadcrumb().lastIndexOf(':'));
				deep--;
			}
			for(String bc: map.keySet()){
				int ndeep = deep;
				while(ndeep > 0){
					Evento add = tMov.getHierarchy().get(ndeep); 
					if(bc.startsWith(lbc)){
						map.remove(bc);
						map.put(add.getBreadcrumb(), add);
						break;
					}else{
						lbc = lbc.substring(0, lbc.lastIndexOf(':'));
					}
					ndeep--;
				}
			}
			if(parentDefined){
				Evento p = tMov.getParent();
				Evento tmp = tMov;
				while(p != null){
					if(p == parent){
						ret.add(tmp);
						break;
					}
					tmp = p;
					p = p.getParent();
				}
			}else{
				ret.add(tMov.getHierarchy().get(0));
			}
		}
		return new ArrayList<Evento>(ret);
	}
	
	public List<Evento> findByTipoDocumento(String filtro, TipoProcessoDocumento tipo) throws PJeBusinessException{
		if(filtro != null && !StringUtil.fullTrim(filtro).isEmpty()){
			return findFilteredChildren(null, tipo.getAgrupamento(), Criteria.or(Criteria.equals("code", StringUtil.fullTrim(filtro)), Criteria.contains("description", StringUtil.fullTrim(filtro))));
		}else{
			return findFilteredChildren(null, tipo.getAgrupamento());
		}
	}
	
	/**
	 * Recupera a lista de trilhas de hierarquia dos tipos de movimento pertencentes ao agrupamento e,
	 * se definido, que tenham o tipo de movimento parent dado nessa hierarquia.
	 * 
	 * @param parent o tipo de movimentação que deve estar contidos em todas as hierarquias
	 * @param grp o agrupamento de movimentação alvo
	 * @return a lista de trilhas de hiearquia
	 * @throws PJeBusinessException
	 */
	private List<String> findBreadCrumb(Evento parent, Agrupamento grp) throws PJeBusinessException{
		Search search = new Search(Evento.class);
		loadBasicCriterias(search);
		search.setDistinct(true);
		addCriteria(search, Criteria.equals("eventoAgrupamentoList.agrupamento", grp));
		search.setRetrieveField("breadcrumb");
		if(parent != null){
			addCriteria(search, Criteria.startsWith("breadcrumb", parent.getBreadcrumb()));
		}
		return list(search);
	}
	
	/**
	 * Identifica se um tipo de movimentação é, para a instalação atual, considerada
	 * "folha", ou seja, se ela não contém nenhuma entidade filha ativa e aplicável
	 * ao tipo de justiça.
	 * 
	 * @param tipoMovimento o tipo de movimentação que se pretende identificar 
	 * @return true, se não houver nenhum tipo de movimentação ativo e aplicável cujo pai seja o tipo dado 
	 * @throws PJeBusinessException
	 * 
	 * @see Evento#getParent()}
	 * @see Evento#getCode()
	 */
	public boolean isLeaf(Evento tipoMovimento) throws PJeBusinessException{
		Search s = new Search(Evento.class);
		loadActiveCriterias(s);
		addCriteria(s, 
				Criteria.equals("parent.code", tipoMovimento.getCode()),
				Criteria.equals("ativo", true));
		long count = count(s);
		return count == 0;
	}
	
	public Evento recuperarMovimentoExclusao() {
		return this.eventoDAO.recuperar(CodigoMovimentoNacional.CODIGO_MOVIMENTO_EXCLUSAO_MOVIMENTO);
	}
	
	/**
	 * Metodo responsavel por buscar os eventos superiores.
	 * 
	 * @return
	 */
	public List<Evento> getEventosSuperiores(Boolean eventoSuperiorAtivo){
		return this.eventoDAO.getEventosSuperiores(eventoSuperiorAtivo);
	}
	
	/**
	 * Metodo responsavel por buscar os eventos ativos.
	 * 
	 * @return
	 */
	public List<Evento> getEventosAtivos(){
		return this.eventoDAO.getEventosAtivos();
	}
	
	public String findComplementoByIdEvento(Integer idEvento) {
		return this.eventoDAO.findComplementoByIdEvento(idEvento);
	}

	public List<MovimentoDTO> getEventosAtivosDTO() {
		return this.eventoDAO.getEventosAtivosDTO();
	}

}
