/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.component.tree.LocalizacaoFisicaSearchTreeHandler;
import br.com.infox.component.tree.SearchTree2GridList;
import br.com.infox.ibpm.component.tree.LocalizacaoEstruturaSearchTreeHandler;
import br.com.infox.ibpm.component.tree.LocalizacaoTreeHandler;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Municipio;

@Name("localizacaoHome")
@BypassInterceptors
public class LocalizacaoHome extends AbstractLocalizacaoHome<Localizacao> {

	private static final long serialVersionUID = 1L;
	private Localizacao localizacaoPai;
	private SearchTree2GridList<Localizacao> searchTree2GridList;	
	private boolean modeloLocalizacao = false;
	private String cnpj = "0";

	public Localizacao getLocalizacaoPai() {
		return localizacaoPai;
	}

	public void setLocalizacaoPai(Localizacao localizacaoPai) {
		this.localizacaoPai = localizacaoPai;
	}

	@Override
	public void newInstance() {
		getInstance().setEstrutura(modeloLocalizacao);
		getInstance().setAtivo(Boolean.TRUE);
		limparTrees();
		Contexts.removeFromAllContexts("cepSuggest");
		Contexts.removeFromAllContexts("modeloLocalizacaoEstruturadaSuggest");
		Localizacao searchBean = getComponent("localizacaoSearch", ScopeType.CONVERSATION);
		searchBean.setEstruturaFilho(null);
		refreshGrid("localizacaoGrid");
		super.newInstance();		
	}

	public static LocalizacaoHome instance() {
		return ComponentUtil.getComponent("localizacaoHome");
	}

	private void limparTrees() {
		LocalizacaoTreeHandler ret1 = getComponent("localizacaoSearchTree");
		LocalizacaoTreeHandler ret2 = getComponent("localizacaoFormTree");
		ret1.clearTree();
		ret2.clearTree();
		if (getLockedFields().contains("localizacaoPai"))
			ret2.clearTree();
		if (searchTree2GridList != null) {
			searchTree2GridList.refreshTreeList();
			searchTree2GridList = null;
		}
		localizacaoPai = null;
	}

	@Override
	protected Localizacao createInstance() {
		instance = super.createInstance();
		instance.setLocalizacaoPai(new Localizacao());
		instance.setEstrutura(modeloLocalizacao);
		getEnderecoHome().newInstance();
		return instance;
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
		} catch (Exception e) {
			e.printStackTrace();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Ocorreu o seguinte erro: " + e.getMessage());
		}
		return ret;
	}

	private void verificaListas() {
		/*
		 * Verifica se o pai atual e o pai selecionado são diferentes de nulo e
		 * se os dois são diferentes um do outro e remove o registro da lista do
		 * pai atual e insere na lista do pai selecionado.
		 */
		if ((getInstance().getLocalizacaoPai() != null) && (localizacaoPai != null)
				&& (!getInstance().getLocalizacaoPai().getLocalizacao().equals(localizacaoPai.getLocalizacao()))) {
			getInstance().getLocalizacaoPai().getLocalizacaoList().remove(getInstance());
			localizacaoPai.getLocalizacaoList().add(getInstance());
			localizacaoPai.getLocalizacaoList();
		}
		/*
		 * Se o pai atual não for nulo e o pai selecionado for, o registro é
		 * excluido da lista do pai atual.
		 */
		if ((getInstance().getLocalizacaoPai() != null) && (localizacaoPai == null)) {
			getInstance().getLocalizacaoPai().getLocalizacaoList().remove(getInstance());
		}
		/*
		 * Se o pai atual for nulo e o pai selecionado não for, o registro é
		 * adicionado à lista do pai atual.
		 */
		if ((getInstance().getLocalizacaoPai() == null) && (localizacaoPai != null)) {
			localizacaoPai.getLocalizacaoList().add(getInstance());
			getInstance().setLocalizacaoPai(localizacaoPai);
		}
	}

	@Override
	public String update() {
		verificaListas();
		/*
		 * Se o registro estiver como inativo na hora do update, todos os seus
		 * filhos serão inativados
		 */
		if (!getInstance().getAtivo()) {
			inactiveRecursive(getInstance());
			return "updated";
		} else {
			String ret = null;
			try {
				ret = super.update();
			} catch (Exception e) {
				e.printStackTrace();
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Ocorreu o seguinte erro: " + e.getMessage());
			}
			return ret;
		}
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setLocalizacaoPai(localizacaoPai);
		
		if (getInstance().getEstrutura()) {
			instance.setEstruturaFilho(null);
			instance.setLocalizacaoPai(null);
			localizacaoPai = null;
			limparTrees();
		}
		
		if ((getEnderecoHome().checkEndereco()) && (getEnderecoHome().getCep() != null)) {
			Endereco endereco = getEnderecoHome().getInstance();
			endereco = getEntityManager().merge(endereco);
			Cep cep = endereco.getCep();
			cep = getEntityManager().merge(cep);
			Municipio municipio = cep.getMunicipio();
			municipio = getEntityManager().merge(municipio);
			Estado estado = municipio.getEstado();
			getEntityManager().merge(estado);
			getInstance().setEndereco(endereco);
		} else {
			getInstance().setEndereco(null);
			if (getEnderecoHome().checkEndereco()) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Cep obrigatório");
				return false;
			}
		}
		
		if (!getInstance().getEstrutura()) {
			LocalizacaoManager localizacaoManager = ComponentUtil.getComponent("localizacaoManager");
			if (getInstance().getLocalizacaoPai() == null && !isLocalizacaoTribunal()) {
	  			FacesMessages.instance().add(StatusMessage.Severity.ERROR, FacesUtil.getMessage("entity_messages", "localizacao.erro.localizacaoSuperiorObrigatoria"));
	  			return false;
	  		} else if (getInstance().getLocalizacaoPai() != null && localizacaoManager.isLocalizacaoPossuiOJ(getInstance().getLocalizacaoPai())) { 
	  			FacesMessages.instance().add(StatusMessage.Severity.ERROR, FacesUtil.getMessage("entity_messages", "localizacao.erro.relacionamentoOJ"));
	 			return false;
	  		} else if (getInstance().getLocalizacaoPai() == getInstance()) {
	  			FacesMessages.instance().add(StatusMessage.Severity.ERROR, FacesUtil.getMessage("entity_messages", "localizacao.erro.localizacaoSuperiorDiferente"));
	  			return false;	  			
	  		}
		}
		
		refreshGrid("localizacaoGrid");
		return true;
	}
	
	/**
	 * Verifica se a localização está vinculada a algum ItemTIpoDocumento. Se
	 * não estiver, realiza a inativação em cascata
	 */
	public String inactiveRecursive(Localizacao localizacao) {
		if (localizacao.getItemTipoDocumentoList().size() <= 0) {
			if (localizacao.getLocalizacaoList().size() > 0) {
				inativarFilhos(localizacao);
			}
			
			localizacao.setAtivo(Boolean.FALSE);
			String ret = super.update();
			limparTrees();
			refreshGrid("localizacaoGrid");
			
			if (ret != null && ret != "") {
			  	FacesMessages.instance().clear();
			  	FacesMessages.instance().add(Severity.INFO, super.getInactiveSuccess());
			}
						
			return ret;
			
		} else {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro está em uso não poderá ser excluido!");
			return "False";
		}
	}

	private void inativarFilhos(Localizacao localizacao) {
		localizacao.setAtivo(Boolean.FALSE);

		Integer quantidadeFilhos = localizacao.getLocalizacaoList().size();
		for (int i = 0; i < quantidadeFilhos; i++) {
			inativarFilhos(localizacao.getLocalizacaoList().get(i));
		}
	}

	@Override
	public void onClickFormTab() {
		if (isManaged()) {
			localizacaoPai = getInstance().getLocalizacaoPai();
		} else {
			localizacaoPai = null;
		}
		super.onClickFormTab();
	}

	private EnderecoHome getEnderecoHome() {
		return getComponent("enderecoHome");
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		
		/* PJEII-4599: Bruno Sales. Alterações feitas pela JT. Ao alterar o cep e gravar, a modificação não estava sendo realizada. 
		 * CEP, cidade e Estado antigos estavam permanecendo. O endereço antigo estava sendo atribuído ao EnderecoHome neste ponto. 
		 * Foi inserida a condição getEnderecoHome().getId() == null para evitar essa atribuição. 
		 */
		if (isManaged() && getInstance().getEndereco() != null && getEnderecoHome().getId() == null) {
			getEnderecoHome().setId(getInstance().getEndereco().getIdEndereco());
		} else if ((changed && getInstance().getEndereco() == null) || id == null) {
			getEnderecoHome().newInstance();
		}
		if (isManaged() && changed) {
			localizacaoPai = getInstance().getLocalizacaoPai();
		}
		if (id == null) {
			localizacaoPai = null;
		}
	}

	/**
	 * Grava o registro atual e seta o localizacaoPai do próximo com o valor da
	 * localização do último registro inserido.
	 */
	public String persistAndNext() {
		String outcome = null;
		try {
			outcome = persist();
		} catch (Exception e) {
			e.printStackTrace();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Ocorreu o seguinte erro: " + e.getMessage());
		}
		
		if (outcome != null) {
			if (!outcome.equals("")) {
				Localizacao me = getInstance();
				newInstance();
				getInstance().setLocalizacaoPai(me);
				getEntityManager().flush();
				localizacaoPai = getInstance().getLocalizacaoPai();
				getInstance().setLocalizacaoPai(localizacaoPai);
			}
		}
		return outcome;
	}

	public SearchTree2GridList<Localizacao> getSearchTree2GridList() {
		Localizacao searchBean = getComponent("localizacaoSearch", ScopeType.CONVERSATION);	
		LocalizacaoFisicaSearchTreeHandler tree = getLocalizacaoFisicaSearch();							
		searchBean.setLocalizacaoPai(this.getLocalizacaoPai());
		String filterName[] = this.getFiltros(true);
		searchTree2GridList = new SearchTree2GridList<Localizacao>(searchBean, tree);
		searchTree2GridList.setFilterName(filterName);
		searchTree2GridList.setGrid(getLocalizacaoGrid());
		getLocalizacaoGrid().setMaxResults(0);
		
		return searchTree2GridList;
	}
	
	public SearchTree2GridList<Localizacao> getModeloLocalizacaoEstruturadaSearchTree2GridList() {		
		Localizacao searchBean = getComponent("localizacaoSearch", ScopeType.CONVERSATION);	
		LocalizacaoEstruturaSearchTreeHandler tree = getLocalizacaoEstruturaSearch();
		tree.setPesquisarApenasModelosLocalizacao(true);
		searchBean.setLocalizacaoPai(this.getLocalizacaoPai());		
		String filterName[] = this.getFiltros(false);		
		searchTree2GridList = new SearchTree2GridList<Localizacao>(searchBean, tree);
		searchTree2GridList.setFilterName(filterName);
		searchTree2GridList.setGrid(getLocalizacaoGrid());		
		
		return searchTree2GridList;
	}
	
	private String[] getFiltros(boolean fisica) {
		List<String> lista = new ArrayList<String>(Arrays.asList("ativo", "localizacao"));	
		
		if (fisica) {
			lista.add("estruturaFilho");						
		}
						
		return lista.toArray(new String[lista.size()]);
	}	

	private GridQuery getLocalizacaoGrid() {
		return getComponent("localizacaoGrid", ScopeType.CONVERSATION);
	}

	private LocalizacaoEstruturaSearchTreeHandler getLocalizacaoEstruturaSearch() {
		return getComponent("localizacaoEstruturaSearchTreeHandler", ScopeType.CONVERSATION);
	}
	
	private LocalizacaoFisicaSearchTreeHandler getLocalizacaoFisicaSearch() {
   		return getComponent("localizacaoFisicaSearchTreeHandler", ScopeType.CONVERSATION);
 	}
	
	public void setCnpj(String cnpj) {
		this.cnpj = Strings.isEmpty(cnpj) ? "0" : cnpj;
	}

	public String getCnpj() {
		return cnpj;
	}
	
	public boolean possuiParenteEstrutura() {
		/*
		 * PJE-JT: Ricardo Scholz : PJE-1310 / PJEII-367 - 2012-02-06 Alteracoes feitas pela JT.
         * Isso obriga as localizacoes filhas de uma estrutura a nao terem estrutura modelo.
		 */
		Localizacao pai = localizacaoPai;
		while (pai != null) {
			if (pai.getEstrutura()) {
				return true;
			}
			pai = pai.getLocalizacaoPai();
		}
		return false;
	}

	public boolean canSelect(EntityNode<Localizacao> node) {
		return true;
	}

	public boolean checkPermissaoLocalizacao(EntityNode<Localizacao> node) {
		Localizacao locAtual = UsuarioHome.getUsuarioLocalizacaoAtual().getLocalizacaoFisica();
		if (node.getEntity().getIdLocalizacao() == locAtual.getIdLocalizacao()) {
			return true;
		} else {
			EntityNode<Localizacao> nodePai = node.getParent();
			while (nodePai != null) {
				if (nodePai.getEntity().getIdLocalizacao() == locAtual.getIdLocalizacao())
					return true;
				nodePai = nodePai.getParent();
			}
		}
		return false;
	}
	
	public String obterLocalizacaoTree() {		
		String treeHandlerName = null;		
		if(ParametroUtil.instance().isPrimeiroGrau()) {
			treeHandlerName = "localizacaoEstruturaServidorTree";
		}else {
			treeHandlerName = "localizacaoEstruturaServidorSegundoGrauTree";
		}		
		return treeHandlerName;
	}
	
 	/**
	 * Verifica se a localizao  a do tribunal.
	 * 
	 * @return Boolean: verdadeiro se for localizao do tribunal. Falso, caso contrrio.
   	 */
  	public boolean isLocalizacaoTribunal(){
   		ParametroService parametroService = (ParametroService)Component.getInstance("parametroService");
   		Integer idLocalizacaoTribunal = Integer.parseInt(parametroService.valueOf("idLocalizacaoTribunal"));
   		
   		if(idLocalizacaoTribunal.equals(getInstance().getIdLocalizacao())){
   			return true;
   		}
   		
   		return false;
  	}
  	
 	/**
	 * Retorna a lista de localizaes superiores.
	 * 
	 * @return List<Localizacao>: Lista de localizaes superiores.
   	 */
   	public List<Localizacao> retornarLocalizacaoSuperior(){	
   		if(isManaged() && getInstance() != null && getInstance().getLocalizacaoPai() != null && !isLocalizacaoTribunal()){
   			List<Localizacao> ret = new ArrayList<Localizacao>();
   			ret.add(getInstance().getLocalizacaoPai());
   			return ret;
   		}
   		
   		return Collections.emptyList();
  	}
   	
  	/**
	 * Retorna a lista de localizaes inferiores.
	 * 
	 * @return List<Localizacao>: Lista de localizaes inferiores.
   	 */
   	public List<Localizacao> retornarLocalizacoesInferiores(Localizacao localizacao){
   		if (localizacao != null && localizacao.getIdLocalizacao() != 0) {
   			LocalizacaoManager localizacaoManager = ComponentUtil.getComponent("localizacaoManager");
   			return localizacaoManager.getArvoreDescendente(localizacao.getIdLocalizacao(), false);
   		}
   		
   		return Collections.emptyList();
  	}
 
   	/**
    	 * Retorna a lista de estruturas modelo.
    	 * 
    	 * @return List<Localizacao>: Lista de estruturas modelo.
   	 */
   	public List<Localizacao> retornarEstruturaModelo(){
   		List<Localizacao> estruturasModelo = null;
   		
   		if(getInstance() != null && getInstance().getEstruturaFilho() != null){
   			LocalizacaoManager localizacaoManager = ComponentUtil.getComponent("localizacaoManager");
   			estruturasModelo = localizacaoManager.getArvoreDescendente(getInstance().getEstruturaFilho().getIdLocalizacao(), false);
   			estruturasModelo.add(getInstance().getEstruturaFilho());
   		}
   		
   		return estruturasModelo;
 	}
   	
   	public boolean isModeloLocalizacao() {
		return modeloLocalizacao;
	}
   	
   	public void setModeloLocalizacao(boolean modeloLocalizacao) {
		this.modeloLocalizacao = modeloLocalizacao;
	}
}