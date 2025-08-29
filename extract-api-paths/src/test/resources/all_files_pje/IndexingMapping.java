/**
 * 
 */
package br.jus.pje.indexacao;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author cristof
 *
 */
public class IndexingMapping {
	
	private Class<?> classe;
	
	private String id;
	
	private String name;
	
	private Map<String, String> primitivos;
	
	private Map<String, String> listas;
	
	private Map<String, String> condicoes;
	
	private Map<Class<?>, Set<String>> paths;
	
	private Map<String, IndexingMapping> objetos;
	
	private Map<String, IndexingExtractor> extractors;
	
	private Map<Class<?>, String> contidas;
	
	private Map<String, String> mappingData;

	public IndexingMapping(Class<?> classe){
		this.classe = classe;
		primitivos = new HashMap<String, String>(0);
		listas = new HashMap<String, String>();
		condicoes = new HashMap<String, String>(0);
		paths = new HashMap<Class<?>, Set<String>>();
		extractors = new HashMap<String, IndexingExtractor>();
		objetos = new HashMap<String, IndexingMapping>(0);
		contidas = new HashMap<Class<?>, String>(0);
		mappingData = new HashMap<String, String>(0);
	}

	/**
	 * @return the classe
	 */
	public Class<?> getClasse() {
		return classe;
	}

	/**
	 * @param classe the classe to set
	 */
	public void setClasse(Class<?> classe) {
		this.classe = classe;
	}

	/**
	 * @return the primitivos
	 */
	public Map<String, String> getPrimitivos() {
		return primitivos;
	}

	/**
	 * @param primitivos the primitivos to set
	 */
	public void setPrimitivos(Map<String, String> primitivos) {
		this.primitivos = primitivos;
	}

	/**
	 * @return the objetos
	 */
	public Map<String, IndexingMapping> getObjetos() {
		return objetos;
	}

	/**
	 * @param objetos the objetos to set
	 */
	public void setObjetos(Map<String, IndexingMapping> objetos) {
		this.objetos = objetos;
	}

	/**
	 * @return the contidas
	 */
	public Map<Class<?>, String> getContidas() {
		return contidas;
	}

	/**
	 * @param contidas the contidas to set
	 */
	public void setContidas(Map<Class<?>, String> contidas) {
		this.contidas = contidas;
	}

	/**
	 * @return the listas
	 */
	public Map<String, String> getListas() {
		return listas;
	}

	/**
	 * @param listas the listas to set
	 */
	public void setListas(Map<String, String> listas) {
		this.listas = listas;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getCondicoes() {
		return condicoes;
	}

	public void setCondicoes(Map<String, String> condicoes) {
		this.condicoes = condicoes;
	}

	public Map<String, IndexingExtractor> getExtractors() {
		return extractors;
	}

	public void setExtractors(Map<String, IndexingExtractor> extractors) {
		this.extractors = extractors;
	}
	
	public Map<Class<?>, Set<String>> getPaths() {
		return paths;
	}
	
	public void setPaths(Map<Class<?>, Set<String>> paths) {
		this.paths = paths;
	}
	
	public Map<String, String> getMappingData() {
		return mappingData;
	}

}
