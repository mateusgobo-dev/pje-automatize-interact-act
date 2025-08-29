package br.jus.pje.indexacao;

import org.json.JSONObject;


public interface IndexingExtractor {
	
	public JSONObject extract(Object id); 
	
}
