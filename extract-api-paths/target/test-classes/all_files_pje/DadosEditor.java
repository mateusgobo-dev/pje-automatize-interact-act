package br.com.infox.editor.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DadosEditor implements Serializable {
	private static final long serialVersionUID = 1L;

	private String html;
	private Map<Integer, TabelaTopicosBean> tabelaTopicos;
	private Map<Integer, DadosTopico> topicos;
	private Map<Integer, AnotacaoBean> anotacoes;
	private Map<String, String> templates;
	private List<Estilo> estilos;
	private Map<String, Object> parametros;
	
	public String getHtml() {
		return html;
	}
	
	public void setHtml(String html) {
		this.html = html;
	}

	public Map<Integer, TabelaTopicosBean> getTabelaTopicos() {
		return tabelaTopicos;
	}

	public void setTabelaTopicos(Map<Integer, TabelaTopicosBean> tabelaTopicos) {
		this.tabelaTopicos = tabelaTopicos;
	}

	public Map<Integer, DadosTopico> getTopicos() {
		return topicos;
	}

	public void setTopicos(Map<Integer, DadosTopico> topicos) {
		this.topicos = topicos;
	}

	public Map<Integer, AnotacaoBean> getAnotacoes() {
		return anotacoes;
	}

	public void setAnotacoes(Map<Integer, AnotacaoBean> anotacoes) {
		this.anotacoes = anotacoes;
	}

	public Map<String, String> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<String, String> templates) {
		this.templates = templates;
	}

	public List<Estilo> getEstilos() {
		return estilos;
	}

	public void setEstilos(List<Estilo> estilos) {
		this.estilos = estilos;
	}

	public Map<String, Object> getParametros() {
		return parametros;
	}

	public void setParametros(Map<String, Object> parametros) {
		this.parametros = parametros;
	}
}
