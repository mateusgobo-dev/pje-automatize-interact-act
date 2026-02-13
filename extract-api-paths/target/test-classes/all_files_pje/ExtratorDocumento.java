/**
 * 
 */
package br.jus.pje.indexacao;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.jboss.seam.Component;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.ContentHandler;

import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;

/**
 * @author cristof
 *
 */
public class ExtratorDocumento implements IndexingExtractor{
	

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.IndexingExtractor#extract(java.lang.Object)
	 */
	@Override
	public JSONObject extract(Object o) {
		JSONObject extractedData = null;
		
		try {
			extractedData = getData((ProcessoDocumentoBin) o);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return extractedData;
	}

	private JSONObject getData(ProcessoDocumentoBin pdb) throws JSONException{
		JSONObject ret = new JSONObject();
		ret.put("id_", pdb.getIdProcessoDocumentoBin());
		ret.put("tamanho", pdb.getSize());
		ret.put("mimetype", pdb.getExtensao());
		ret.put("arquivooriginario", pdb.getNomeArquivo());
		ret.put("data_assinatura", pdb.getDataAssinatura());
		JSONArray assinaturas = new JSONArray();
		for(ProcessoDocumentoBinPessoaAssinatura sig: pdb.getSignatarios()){
			JSONObject sign = new JSONObject();
			sign.put("data_assinatura", sig.getDataAssinatura());
			sign.put("signatario", sig.getNomePessoa());
			sign.put("identificador", sig.getPessoa() != null ? sig.getPessoa().getLogin() : null);
			assinaturas.put(sign);
		}
		ret.put("assinaturas", assinaturas);
		try {
			AbstractParser parser = null;
			InputStream is = null;
			ContentHandler handler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			ParseContext context = new ParseContext();
			if (pdb.isBinario()) {
				DocumentoBinManager documentoBinManager = getConteudoManager();
				byte[] bin = documentoBinManager.getData(pdb.getNumeroDocumentoStorage());
				if(bin != null){
					is = new ByteArrayInputStream(bin);
				}
				parser = new AutoDetectParser();
			} else if(pdb.getModeloDocumento() != null) {
				is = new ByteArrayInputStream(pdb.getModeloDocumento().getBytes());
				parser = new HtmlParser();
			}
			
			if(is != null){
				parser.parse(is, handler, metadata, context);
				ret.put("modeloDocumento", handler.toString());
			}
			if(metadata.get("title")!=null){
				ret.put("titulo", metadata.get("title"));
			}
			if(metadata.get("Author")!=null){
				ret.put("autor", metadata.get("Author"));
			}
			if(metadata.get("size") != null){
				ret.put("tamanho", metadata.get("size"));
			}
			for(String s: metadata.names()){
				if(s.equals("title") || s.equals("Author") || s.equals("size")){
					continue;
				}
				ret.put(s, metadata.get(s));
			}
			return ret;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private DocumentoBinManager getConteudoManager(){
		return (DocumentoBinManager) Component.getInstance("documentoBinManager");
	}
	
}
