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
package br.com.infox.ibpm.jbpm.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.faces.Redirect;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.taskmgmt.def.TaskController;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Iterators;

import br.com.infox.ibpm.help.HelpUtil;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.handler.VariableHandler;
import br.com.infox.ibpm.jbpm.handler.VariableHandler.Variavel;
import br.com.infox.ibpm.search.Indexer;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.servicos.PesquisaService;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("search")
@Scope(ScopeType.CONVERSATION)
//@BypassInterceptors
public class SearchHandler implements Serializable {

	private static final String NUMERO_PROCESSO_PATTERN = "(\\d{1,7}-\\d{2}\\.\\d{4}\\.\\d\\.\\d{2}\\.\\d{4}|\\d{3,20})";
	private static final long serialVersionUID = 1L;
	private String searchText;
	private List<Map<String, Object>> searchResult;
	private Integer resultSize;
	private int pageSize = 8;
	private int page;
	private int maxPageSize = 100;
	
	private JSONObject searchData;
	
	@In(create=true)
	private PesquisaService pesquisaService;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		page = 0;
		this.searchText = searchText;
	}

	public List<Map<String, Object>> getSearchResult() {
		return searchResult;
	}

	public void search() {
		if (searchText == null || "".equals(searchText.trim())) {
			return;
		}
		if(searchProcesso(searchText)){
			return;
		}
		try {
			searchData = pesquisaService.search_(searchText);
			if(Iterators.size(searchData.keys()) > 0){
				return;
			}
			searchResult = new ArrayList<Map<String, Object>>();
			if(pesquisaService != null){
				return;
			}
			try {
				Indexer indexer = new Indexer();
				String[] fields = new String[] { "conteudo", "texto" };
				Query query = indexer.getQuery(searchText, fields);
				List<Document> search = indexer.search(searchText, fields, 100);
				Session session = ManagedJbpmContext.instance().getSession();
				for (Document d : search) {
					long taskId = Long.parseLong(d.get("id"));
					TaskInstance ti = (TaskInstance) session.get(TaskInstance.class, taskId);
					String s = HelpUtil.getBestFragments(query, getConteudo(ti));
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("texto", s);
					m.put("taskName", ti.getTask().getName());
					m.put("taskId", ti.getId());
					m.put("processo", ti.getProcessInstance().getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO));
					searchResult.add(m);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			resultSize = searchResult.size();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
	}
	
	public List<JSONObject> getHits(){
		try {
			if(searchData != null && Iterators.size(searchData.keys()) > 0){
					JSONArray aux = ((JSONArray) ((JSONObject) searchData.get("hits")).get("hits"));
				List<JSONObject> ret = new ArrayList<JSONObject>(aux.length());
				for(int i = 0; i < aux.length(); i++){
					ret.add((JSONObject) aux.get(i));
				}
				return ret;
			}else{
				return Collections.emptyList();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		return Collections.emptyList();		
	}
	
	public List<JSONObject> toArray(JSONObject o, String prop){
		try {
			Object aux = o.get(prop);
			if(aux != null && JSONArray.class.isAssignableFrom(aux.getClass())){
				JSONArray arr = ((JSONArray) aux);
				List<JSONObject> ret = new ArrayList<JSONObject>(arr.length());
				for(int i = 0; i < arr.length(); i++){
					ret.add((JSONObject) arr.get(i));
				}
				return ret;
			}else{
				return Arrays.asList(o);
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return Arrays.asList(o);
	}
	
	private boolean searchProcesso(String query){
		if (query.matches(NUMERO_PROCESSO_PATTERN)) {
			ProcessoTrf proc = null;
			try {
				List<ProcessoTrf> procs = processoJudicialManager.findByNumeracaoUnicaParcial(searchText);
				if(!procs.isEmpty()){
					proc = procs.get(0);
				}
			} catch (PJeBusinessException e) {
				return false;
			}
			if (proc != null) {
				Redirect.instance().setConversationPropagationEnabled(false);
				Redirect.instance().setViewId("/Processo/Consulta/list.xhtml");
				Redirect.instance().setParameter("id", proc.getIdProcessoTrf());
				Redirect.instance().setParameter("idJbpm", proc.getProcesso().getIdJbpm());
				Redirect.instance().execute();
				return true;
			}
		}
		return false;
	}

	public static String getConteudo(TaskInstance ti) {
		StringBuilder sb = new StringBuilder();
		TaskController taskController = getTaskControler(ti);
		if (taskController != null) {
			List<VariableAccess> vaList = taskController.getVariableAccesses();
			for (VariableAccess v : vaList) {
				Object conteudo = ti.getVariable(v.getMappedName());
				if (v.isWritable() && conteudo != null) {
					conteudo = JbpmUtil.instance().getConteudo(v, ti);
					sb.append(VariableHandler.getLabel(v.getVariableName())).append(": ").append(conteudo).append("\n");
				}
			}
		}
		return sb.toString();
	}

	private static TaskController getTaskControler(TaskInstance ti) {
		if (ti != null && ti.getTask() != null) {
			return ti.getTask().getTaskController();
		} else {
			return null;
		}
	}

	public int getResultSize() {
		if (resultSize == null) {
			search();
		}
		return resultSize;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void nextPage() {
		page++;
		search();
	}

	public void previousPage() {
		page--;
		search();
	}

	public void firstPage() {
		page = 0;
		search();
	}

	public void lastPage() {
		page = (resultSize / pageSize);
		if (resultSize % pageSize == 0)
			page--;
		search();
	}

	public boolean isNextPageAvailable() {
		return resultSize > ((page * pageSize) + pageSize);
	}

	public boolean isPreviousPageAvailable() {
		return page > 0;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize > maxPageSize ? maxPageSize : pageSize;
	}

	public long getFirstRow() {
		if(resultSize == 0){
			return 0;
		}else{
			return page * pageSize + 1;
		}
	}

	public long getLastRow() {
		return (page * pageSize + pageSize) > resultSize ? resultSize : page * pageSize + pageSize;
	}

	public String getTextoDestacado(Variavel v) {
		if (v.getValue() == null) {
			return null;
		}
		String texto = null;
		if (JbpmUtil.isTypeEditor(v.getType(), v.getLabel())) {
			texto = JbpmUtil.instance().valorProcessoDocumento((Integer) v.getValue());
		} else {
			texto = v.getValue().toString();
		}
		if (searchText != null) {
			String[] fields = new String[] { "conteudo" };
			QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_31, fields, HelpUtil.getAnalyzer());
			try {
				org.apache.lucene.search.Query query = parser.parse(searchText);
				String highlighted = HelpUtil.highlightText(query, texto, false);
				if (!highlighted.equals("")) {
					texto = highlighted;
				}
			} catch (ParseException e) {
			}
		}
		return texto;
	}
	
	public JSONObject getSearchData() {
		return searchData;
	}

}