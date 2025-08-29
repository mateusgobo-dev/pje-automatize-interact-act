package br.jus.pje.indexacao;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;

public class ExtratorDocsPessoaProcessoDocumentoBinPessoaAssinatura implements IndexingExtractor{

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject extract(Object o) {
		JSONObject data = null;
		
		try {
			data = getData((Set<PessoaDocumentoIdentificacao>) o);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return data;
	}

	private JSONObject getData(Set<PessoaDocumentoIdentificacao> pessoaDocumentoIdentificacaoSet) throws JSONException{
		JSONObject ret = new JSONObject();
		JSONArray documentos = new JSONArray();
		JSONObject documento;
		for (PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao : pessoaDocumentoIdentificacaoSet) {
			documento = new JSONObject();
			documento.put("id_documento", pessoaDocumentoIdentificacao.getIdDocumentoIdentificacao());
			documento.put("tipo_documento", pessoaDocumentoIdentificacao.getTipoDocumento());
			documento.put("numero_documento", pessoaDocumentoIdentificacao.getNumeroDocumento());
			documento.put("letra_oab", pessoaDocumentoIdentificacao.getLetraOAB());
			documentos.put(documento);
		}
		ret.put("documentos", documentos);
		return ret;
	}
}
