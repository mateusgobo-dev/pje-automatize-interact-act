/**
 * pje-web
 * Copyright (C) 2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.nucleo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.business.dao.LocalizacaoDAO;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

/**
 * Componente de controle negocial de localizações.
 * 
 * @author cristof
 * 
 */
@Name("localizacaoService")
@Scope(ScopeType.EVENT)
@Transactional
public class LocalizacaoService {

	@In(create = true)
	private LocalizacaoDAO localizacaoDAO;
	
	/**
	 * Metodo que valida usuario interno pertence ao orgao julgador ou orgao jugaldor colegiado do processo  
	 * @param ProcessoTrf processoTrf
	 * @return true caso pertenca a um dos dois e false caso contrario
	 */
    public boolean validarUsuarioInterno(ProcessoTrf processoTrf){
        boolean retorno = false;
        if( Authenticator.hasRole("servidor")) {
            UsuarioLocalizacao usuarioLocalizacaoAtual = Authenticator.getUsuarioLocalizacaoAtual();
            UsuarioLocalizacaoMagistradoServidor li = usuarioLocalizacaoAtual.getUsuarioLocalizacaoMagistradoServidor();
            if(li != null){
                if(li.getOrgaoJulgador() != null){
                    retorno = processoTrf.getOrgaoJulgador() == li.getOrgaoJulgador();
                }else if(li.getOrgaoJulgadorColegiado() != null && processoTrf.getOrgaoJulgadorColegiado() != null ){
                    retorno = processoTrf.getOrgaoJulgadorColegiado() == li.getOrgaoJulgadorColegiado();
                }
            }           
        }
        return retorno;
    }
	
	/**
	 * Recupera a localização por seu identificador.
	 * 
	 * @param id o identificador
	 * @return a localização, se existente, ou null.
	 */
	public Localizacao findById(Integer id) {
		try {
			return localizacaoDAO.find(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Método que obtém a lista de localizações ascendentes e descendentes de uma dada Localização, 
	 * a localização do órgão julgador colegiado do usuário logado, se existir, além da própria Localização.
	 * 
	 * @param localizacao Localização a ser usada como base.
	 * @return List<Localizacao> contendo todas as localizações ascendentes ou descendentes, e a própria.

	 * @author Ronny Paterson
	 * @since 1.4.7.1
	 */
	public List<Localizacao> getLocalizacoesAscendentesDescendentesOjsOjcs(Localizacao localizacaoUsuario) {
		// Filtra pela localização do órgão atual e seus ascendentes e descendentes
		List<Localizacao>  localizacoesUsuario = getLocalizacoesAscendentesDescendentes(localizacaoUsuario);		
		//Inclui a localização do órgão julgador colegiado do usuário logado para permitir a hierarquia de localizações entre OJCs e OJs
		adicionaLocalizacaoOjc(localizacoesUsuario);
		return localizacoesUsuario;
	}
	
	/**
	 * Método que adiciona a localização do órgão julgador colegiado do usuário logado a uma lista de localizações.
	 * 
	 * @param localizacoes lista de localizações onde se adicionará a localização do órgão julgador do usuário logado.
	 * @author Ronny Paterson
	 * @since 1.4.7.1
	 */
	public void adicionaLocalizacaoOjc(List<Localizacao> localizacoes) {
		if(!ParametroUtil.instance().isPrimeiroGrau()) {
			Localizacao localizacaoOjc = Authenticator.getLocalizacaoOjcUsuarioLogado();
			if(localizacaoOjc != null) {
				localizacoes.add(localizacaoOjc);
			}
		}
	}
	
	/**
	 * Método que obtém a lista de localizações ascendentes e descendentes de uma dada localização, 
	 * além da própria Localização.
	 * 
	 * @param localizacao Localização a ser usada como base.
	 * @return List<Localizacao> contendo todas as localizações ascendentes ou descendentes, e a própria.
	 * 
	 * @author Tiago Zanon
	 * @author Paulo Cristovão Filho (PJEII-2231)
	 * @since 1.4.7
	 */
	public List<Localizacao> getLocalizacoesAscendentesDescendentes(Localizacao localizacao) {
		if (localizacao == null) {
			return Collections.emptyList();
		}
		Set<Localizacao> ret = new HashSet<Localizacao>();
		ret.addAll(getHierarchy(localizacao));
		ret.addAll(getTree(localizacao));
		return new ArrayList<Localizacao>(ret);
	}
	
	/**
	 * Recupera todas as localizações ascendentes da localização dada, a partir de sua raiz
	 * ou da localização imediatamente descendente daquela em que a hierarquia se tornar circular.
	 * 
	 * @param l a localização dada
	 * @return a lista de localizações ascendentes da localização dada, sendo a primeira a mais remota
	 * 
	 * @author Paulo Cristovão Filho
	 * @since 1.6.0
	 */
	public List<Localizacao> getAscendents(Localizacao l){
		List<Integer> idsLocalizacao = new ArrayList<>();
		idsLocalizacao.add(l.getIdLocalizacao());
		
		return localizacaoDAO.getArvoreDescendente(idsLocalizacao, true);
	}
	
	/**
	 * [PJEII-5382] Retorna a lista de IDs, separados por vírgula, das localizações informadas.
	 * 
	 * @param localizacaoList
	 * @return
	 */
	public String getListaLocalizacaoIdLocalizacao(List<Localizacao> localizacaoList) {
		StringBuilder retorno = new StringBuilder();
		
		if (localizacaoList.size() == 0) {
			return retorno.toString();
		}
		
		for (int i = 0; i < localizacaoList.size() - 1; i++) {
			retorno.append(localizacaoList.get(i).getIdLocalizacao());
			retorno.append(", ");
		}
		
		retorno.append(localizacaoList.get(localizacaoList.size() -1).getIdLocalizacao());
		
		return retorno.toString();
	}

	/**
	 * Recupera a lista de todas as localizações filhas de uma dada localização.
	 * Esse método assegura que, ainda que haja uma recursividade interna na
	 * estrutura de localizações, não haja a repetição.
	 * 
	 * @param localizacao
	 *            a localização cujos descendentes se pretende recuperar
	 * @return o conjunto de localizações descendentes da localização dada.
	 */
	public List<Localizacao> getDescendents(Localizacao localizacao) {
		List<Integer> idsLocalizacao = new ArrayList<>();
		idsLocalizacao.add(localizacao.getIdLocalizacao());
		
		return localizacaoDAO.getArvoreDescendente(idsLocalizacao, true);
	}

	/**
	 * Recupera a lista de entidades que compõe a hierarquia desta entidade, na
	 * ordem da raiz até a própria entidade. Se houver alguma referência
	 * circular, a lista será limitada à entidade ancestral imediatamente
	 * anterior a essa referência, como se essa entidade ancestral fosse a
	 * entidade raiz.
	 * 
	 * @return a lista de entidades da hierarquia desta entidade.
	 */
	public List<Localizacao> getHierarchy(Localizacao localizacao) {
		List<Integer> idsLocalizacao = new ArrayList<>();
		idsLocalizacao.add(localizacao.getIdLocalizacao());
		
		return localizacaoDAO.getArvoreAscendente(idsLocalizacao, true);
	}

	/**
	 * Recupera a árvore de localizações da localização dada, inclusive ela, evitando 
	 * eventual referência circular.
	 * 
	 * @param l a localização cuja árvove descendente se pretende carregar 
	 */
	public List<Localizacao> getTree(Localizacao localizacao) {
		List<Localizacao> ret = new ArrayList<Localizacao>();
		ret.add(localizacao);
		ret.addAll(getDescendents(localizacao));
		return ret;
	}
	
	/**
	 * Recupera uma lista (no formato texto) de identificadores do objeto {@link Localizacao} 
	 * que faz parte da hierarquia do parâmetro {@link Localizacao} passado.
	 * 
	 * @param arg0 {@link Localizacao}
	 * @return Lista (no formato texto) de identificadores do objeto {@link Localizacao} 
	 * que faz parte da hierarquia do parâmetro {@link Localizacao} passado.
	 */
	public String getTreeIds(Localizacao arg0) {
		List<Integer> treeIdsList = getTreeIdsList(arg0);
		
		if (treeIdsList != null) {				
			return treeIdsList.toString().replace("[", "(").replace("]", ")");
		}
		
		return StringUtils.EMPTY;
	}
	
	/**
	 * Recupera uma lista de identificadores do objeto {@link Localizacao} 
	 * que faz parte da hierarquia do parâmetro {@link Localizacao} passado.
	 * 
	 * @param arg0 {@link Localizacao}
	 * @return Lista de identificadores do objeto {@link Localizacao} 
	 * que faz parte da hierarquia do parâmetro {@link Localizacao} passado.
	 */
	public List<Integer> getTreeIdsList(Localizacao arg0) {
		if (arg0 != null) {
			List<Localizacao> localizacaoList = getTree(arg0);
			
			if (localizacaoList != null) {
				List<Integer> localizacaoIdList = new ArrayList<Integer>(localizacaoList.size());
				
				for(Localizacao localizacao : localizacaoList) {
					localizacaoIdList.add(localizacao.getIdLocalizacao());
				}
				
				return localizacaoIdList;
			}
		}
		return null;
	}

	/**
	 * Retorna os ID's das localizacoes ancestrais, ou seja, a localizacao filha mais as localizacoes pai.
	 * 
	 * @param idLocalizacaoFilha Localizacao filha.
	 * @return Lista de ID's das localizacoes ancestrais da localizacao filha.
	 */
	public List<Integer> obterIdsAncestrais(Integer idLocalizacaoFilha){
		List<Integer> idsLocalizacao = new ArrayList<>();
		idsLocalizacao.add(idLocalizacaoFilha);
		
		return localizacaoDAO.obterIdsAncestrais(idsLocalizacao);
	}
}
