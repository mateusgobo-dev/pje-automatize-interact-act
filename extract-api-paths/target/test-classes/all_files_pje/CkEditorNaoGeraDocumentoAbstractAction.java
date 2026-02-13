package br.jus.cnj.pje.nucleo.view;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Classe abstrata com a implementação de referencia para os casos onde o editor
 * não irá gerar um processo documento e poderá utilizar o plugin de tipo de voto.
 *
 */

public abstract class CkEditorNaoGeraDocumentoAbstractAction implements ICkEditorController {

	private int idTipoVotoSelecionado = 0;
    
    public int getIdTipoVotoSelecionado() {
		return idTipoVotoSelecionado;
	}

	public void setIdTipoVotoSelecionado(int idTipoVotoSelecionado) {
		this.idTipoVotoSelecionado = idTipoVotoSelecionado;
	}
	

	public void selecionarTipoVoto(String idTipoVoto) {
		setIdTipoVotoSelecionado(Integer.parseInt(idTipoVoto));
	}
	
	public void limparVotoSelecionado() {
		if(getIdTipoVotoSelecionado() != 0) {
			setIdTipoVotoSelecionado(0);
		}
	}
	
	public String verificarPluginTipoVoto() throws JSONException {
		JSONObject retorno = new JSONObject();
		retorno.put("sucesso", Boolean.TRUE);
		return retorno.toString();
	}
	
	/**
	 * Retorna JSON com os tipos de votos disponíveis
	 */
	public String obterTiposVoto() {
		return "";
	}
}
