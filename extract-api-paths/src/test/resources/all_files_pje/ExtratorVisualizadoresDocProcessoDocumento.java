package br.jus.pje.indexacao;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;

public class ExtratorVisualizadoresDocProcessoDocumento implements IndexingExtractor{

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject extract(Object o) {
		JSONObject extractedData = null;
		
		try {
			extractedData = getData((List<PessoaProcuradoriaEntidade>) o);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return extractedData;
	}

	private JSONObject getData(List<PessoaProcuradoriaEntidade> pessoaProcuradoriaEntidadeList) throws JSONException{
		JSONObject ret = new JSONObject();
		JSONArray procuradorias = new JSONArray();
		JSONObject procuradoria;
		for (PessoaProcuradoriaEntidade pessoaProcuradoriaEntidade : pessoaProcuradoriaEntidadeList) {
			procuradoria = new JSONObject();
			procuradoria.put("id_procuradoria", pessoaProcuradoriaEntidade.getProcuradoria().getIdProcuradoria());
			procuradoria.put("nome_procuradoria", pessoaProcuradoriaEntidade.getProcuradoria().getNome());
			procuradorias.put(procuradoria);
		}
		ret.put("procuradorias", procuradorias);
		return ret;
	}
}
