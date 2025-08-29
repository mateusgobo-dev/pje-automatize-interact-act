package br.com.infox.access;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.Messages;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;
import org.json.JSONObject;
import org.richfaces.event.DropEvent;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PapelManager;
import br.jus.pje.nucleo.entidades.identidade.Papel;

/**
 * Monta o menu do usurio baseado nas permisses de acesso s pginas
 * 
 * @author luizruiz
 * 
 */
@Scope(ScopeType.SESSION)
@BypassInterceptors
public class Menu implements Serializable{

	private static final long serialVersionUID = 1L;

	protected List<MenuItem> bookmark = new ArrayList<MenuItem>();

	protected List<MenuItemJson> menusJson;
	
	private JSONObject objMenusJson = new JSONObject();
	
	/**
	 * Flag para indicar que as paginas ja foram verificadas Isso ocorre apenas uma vez (static)
	 */
	private static boolean pagesChecked;
	
	public void drop(DropEvent o){
		bookmark.add((MenuItem) o.getDragValue());
	}

	/**
	 * Esta funo  chamada pelo arquivo mainMenu.components.xml
	 * 
	 * Cria o menu, verificando para cada item se o usuario e admin ou tem um role no formato SecurityUtil.PAGES_PREFIX seguido da url da pagina a ser
	 * acessada.
	 * O terceiro parametro, caso exista e tenha o valor "target/popup", permite abrir a URL da pagina em uma nova janela (target="_blank").
	 * Pode-se inativar o menu adicionando a informao: rendered=false ao final do identificador do menu.
	 * 
	 *   Exemplos 1 - inativando menu:
	 *   <value>/Cadastro/Audincia.menuText:Audiencia.seam:rendered=false</value>
	 *   
	 *   O menu nao leva em conta em qual posicao esta localizado o atributo rendered=false, portanto pode ser colocado
	 *   em qualquer posicao do item do menu.
	 *   
	 *   Exemplo 2 - indicando abertura do menu como um popup:
 	 *   <value>/Cadastro/Audincia.menuText:Audiencia.seam:target/popup</value>
	 *    
	 * @param items
	 */
	public void setItems(List<String> items){
		menusJson = new ArrayList<MenuItemJson>();
		boolean ok = true;
		for (String key : items){
			try{
				String[] split = key.split(":");
				boolean exibeMenu = verificaSePodeExibirOMenu(split);
				if(exibeMenu) {
					boolean popup = false;
					key = split[0];
					String url = null;
					if (split.length > 1){
						url = split[1];
						if (split.length > 2){
							popup = split[2].equals("target/popup");
						}
					}
					String pageRole = SecurityUtil.PAGES_PREFIX + url;
					if (!pagesChecked && Identity.instance().isLoggedIn()){
						checkPage(pageRole, key);
					}
					// Verifica se possui o papel necessario
					if (Identity.instance().hasRole(pageRole)){
						buildItemJson(key, url, popup);
					}
				}
			} catch (Exception e){
				e.printStackTrace();
				ok = false;
				break;
			}
		}
		if(ok) {
			buildObjMenuJson();
		}
		pagesChecked = ok;
	}

	/**
	 * Faz uma avaiacao da string[] dos itens de um menu e 
	 * verifica se possui a palavra chave (rendered=false),
	 * caso verdadeiro retorna false, caso contrario retorna true -> deixando o comportamento original 
	 * do componente mainMenu que por padrao exibe todos os itens do menu.
	 * 
	 * Mtodo alterado para tratar novos valores para rendered e sinal de desigualdade. 
	 * Exemplos: rendered=false, rendered=2grau, rendered!=1grau
	 * 
	 * @param split
	 * @return false se houver a propiedade rendered=false e true se nao.
	 */
	private boolean verificaSePodeExibirOMenu(String[] split){
		String instancia = ParametroUtil.instance().getInstancia();

		for (String s : split) {
			if (s.trim().contains("rendered")) {
				String item = s.trim();    
				String[] itemSplit = item.split("=");
				for (String subStr : itemSplit) {
					if (subStr.equalsIgnoreCase("false"))
						return false;
					else if (subStr.equalsIgnoreCase("true"))
						return true;
					else if (subStr.contains(instancia)) {
						if (item.contains("!"))
							return false;
						else 
							return true;
					}   
				}   
			}   
		}   
		return true;
	}

	/**
	 * Verifica se existe o role para a pagina, se nao existir registra o role e atribui permissao ao role admin
	 * 
	 * @param pageRole
	 */
	private void checkPage(final String pageRole, String name){
		if (!IdentityManager.instance().roleExists(pageRole)){
			new RunAsOperation(true){

				public void execute(){
					IdentityManager.instance().createRole(pageRole);
					EntityUtil.getEntityManager().flush();
				}
			}.run();
			Papel role;
			PapelManager papelManager = (PapelManager) Component.getInstance("papelManager");
			try {
				role = papelManager.findByCodeName(pageRole);
				String[] parts = name.split("/");
				StringBuilder nameSB = new StringBuilder();
				for (String s : parts) {
					if (nameSB.length() != 0) {
						nameSB.append("/");
					}
					nameSB.append(Messages.instance().get(s));
				}
				role.setNome("Pgina " + nameSB.toString());
				new RunAsOperation(true) {

					public void execute() {
						IdentityManager.instance().addRoleToGroup("admin",
								pageRole);
					}
				}.run();
				if (Identity.instance().hasRole("admin")) {
					Identity.instance().addRole(pageRole);
				}
			} catch (PJeBusinessException e) {
				System.err.println("Erro ao tentar recuperar o papel: " + e.getLocalizedMessage());
			}
		}
	}

	protected void buildItemJson(String key, String url, boolean popup) throws Exception{
		url = Util.instance().getContextPath() + url;
		if (key.startsWith("/")){
			key = key.substring(1);
		}
		String[] groups = key.split("/");
		MenuItemJson parent = null;
		for (int i = 0; i < groups.length; i++){
			String label = groups[i];
			if (!label.startsWith("#{messages['")){
				label = "#{messages['" + label + "']}";
			}
			MenuItemJson item = new MenuItemJson(label);
			if (i == 0){
				int j = menusJson.indexOf(item);
				if (j != -1){
					parent = menusJson.get(j);
				}
				else{
					parent = item;
					if (groups.length == 1){
						parent.setUrl(url);
						parent.setPopup(popup);
					}
					menusJson.add(parent);
				}
			}
			else if (i < (groups.length - 1)){
				parent = parent.add(item);
			}
			else{
				item.setUrl(url);
				item.setPopup(popup);
				parent.getItens().add(item);
			}
		}
	}
	
	public void buildObjMenuJson() {
		objMenusJson = new JSONObject();
		if(this.menusJson != null) {
			objMenusJson.put("Menu", menusJson);
		}
	}

	public List<MenuItem> getBookmark(){
		return bookmark;
	}

	public JSONObject getObjMenusJson() {
		return objMenusJson;
	}
}
