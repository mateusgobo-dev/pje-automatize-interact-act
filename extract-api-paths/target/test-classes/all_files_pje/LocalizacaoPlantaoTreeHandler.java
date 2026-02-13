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
package br.com.infox.cliente.component.tree;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.richfaces.function.RichFunction;

import br.com.infox.cliente.home.PlantaoOficialJusticaHome;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Plantao;

@Name("localizacaoPlantaoTree")
@BypassInterceptors
public class LocalizacaoPlantaoTreeHandler extends LocalizacaoCentralMandadoTreeHandler {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getEventSelected() {
		return "evtSelectLocalizacaoPlantao";
	}
	
	/**
	 * [PJEII-850]
	 * Método sobrescrito para que a árvore seja carregada à partir da localização do usuário. 
	 * @author Fernando Barreira
	 * @category PJE-JT
	 */
	@Override
	protected String getQueryRoots() {
		StringBuilder sb = new StringBuilder();
		sb.append("select l from Localizacao l ");
		
		Localizacao loc = Authenticator.getLocalizacaoAtual();
		if (loc != null) {
			sb.append("where l = #{authenticator.getLocalizacaoAtual()} ");
		} else {
			sb.append("where localizacaoPai is null ");
		}
		
		sb.append("order by localizacao");
		return sb.toString();
	}
	
	/**
	 * [PJEII-850]
	 * Método sobrescrito em função do campo suggest vinculado a esta versão da tree. 
	 * @author Fernando Barreira
	 * @category PJE-JT 
	 */
	@Override
	public void clearTree(){
		super.clearTree();
		esconderSuggestOficialJustica();
	}
	
	/**
	 * [PJEII-850]
	 * Esconde o campo suggest após a limpeza do item selecionado no campo tree.
	 * @author Fernando Barreira
	 * @category PJE-JT
	 */
	private void esconderSuggestOficialJustica() {
		String reRender = RichFunction.findComponent("plantaoFormlocalizacaoClose").getAttributes().get("reRender").toString();
		if ((Boolean) RichFunction.findComponent("localizacaopessoaPlantaoSuggest").getAttributes().get("rendered")) { 
			limparSuggestOficialJustica();
			if (!reRender.contains("plantaoSuggestDiv")) {
				reRender += ",plantaoSuggestDiv";
			}
			if ((Boolean) RichFunction.findComponent("divAgendaPlantao").getAttributes().get("rendered") && 
					!reRender.contains("divAgendaPlantao")) {
				reRender += ",divAgendaPlantao";
			}
			RichFunction.findComponent("plantaoFormlocalizacaoClose").getAttributes().put("reRender", reRender);
		}
	}
	
	/**
	 * [PJEII-850]
	 * Limpa o conteúdo do campo suggest. 
	 * @author Fernando Barreira
	 * @category PJE-JT
	 */
	private void limparSuggestOficialJustica() {
		((PlantaoOficialJusticaHome) Component.getInstance("plantaoOficialJusticaHome")).getInstance().setPessoa((Pessoa) null);
		((Plantao) Component.getInstance("plantaoSearch")).setPessoa((Pessoa) null);
	}
}