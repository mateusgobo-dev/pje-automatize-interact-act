package br.com.infox.editor.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Expressions;

import br.com.infox.editor.exception.VariavelException;
import br.com.infox.editor.interpretadorDocumento.AnalisadorSintaticoException;
import br.com.infox.editor.interpretadorDocumento.InterpretadorDocumentos;
import br.com.infox.editor.interpretadorDocumento.LinguagemFormalException;
import br.com.infox.editor.interpretadorDocumento.NomeVariavelInvalidoException;
import br.com.infox.editor.interpretadorDocumento.TipoValor;
import br.com.infox.editor.interpretadorDocumento.TipoValorInvalidoException;
import br.com.infox.editor.interpretadorDocumento.Valor;
import br.com.infox.editor.interpretadorDocumento.ValorTexto;
import br.com.infox.editor.util.XSLTransformer;
import br.com.infox.ibpm.home.VariavelHome;
import br.jus.pje.nucleo.entidades.Variavel;

@Name(ProcessaModeloService.NAME)
@AutoCreate
public class ProcessaModeloService {

	public static final String NAME = "processaModeloService";
	private Map<String, String> variavelMap;

	private Map<String, String> getVariavelMap() {
		if (variavelMap == null) {
			variavelMap = new HashMap<String, String>();
			VariavelHome variavelHome = VariavelHome.instance();
			for (Variavel variavel : variavelHome.getVariavelItems()) {
				variavelMap.put(variavel.getVariavel().toLowerCase(), variavel.getValorVariavel());
			}
		}
		return variavelMap;
	}
	
	public String validaModelo(String texto) {
		InterpretadorDocumentos interpretador = new InterpretadorDocumentos();
		interpretador.setMimeType(interpretador.MIME_TYPE_HTML);
		String msgErro = null;
		try {
			if (!interpretador.verificarSintaxe(texto)) {
				msgErro = interpretador.getDetalheErroSintaxe();
			}
		} catch (AnalisadorSintaticoException e) {
			msgErro = interpretador.getDetalheErroSintaxe();
		}
		return msgErro;
	}

	public String processaVariaveisModelo(String modelo) throws LinguagemFormalException {
		String textoFinal = "";
		try {
			InterpretadorDocumentos interpretador = getInterpretador(modelo);
			if (interpretador.verificarSintaxe(modelo)) {
				textoFinal = interpretador.converterDocumento();
			}
			else
				textoFinal = interpretador.getDetalheErroSintaxe();
			
		} catch (Exception e) {
			throw new LinguagemFormalException(new VariavelException("Variável inválida encontrada.", e));
		}
		return textoFinal;
	}

	private InterpretadorDocumentos getInterpretador(String modelo) throws LinguagemFormalException {
		InterpretadorDocumentos interpretador = new InterpretadorDocumentos();
		try {
			interpretador.setModelo(modelo);
			interpretador.setMimeType(interpretador.MIME_TYPE_HTML);
			Map<String, Valor> listaVariaveis = interpretador.getListaVariaveis();
			for (Entry<String, Valor> variavel: listaVariaveis.entrySet()) {
				Valor valor = null;
				try {
					valor = processaValorEL(getVariavelMap().get(variavel.getKey()));
				}
				catch (Exception ex) {
					// loga erro de recuperação da variável e retorna valor vazio para o texto
					ex.printStackTrace();
				}
				interpretador.setValor(variavel.getKey(), valor);
			}
		} catch (AnalisadorSintaticoException e) {
			throw new LinguagemFormalException(e);
		} catch (NomeVariavelInvalidoException e) {
			throw new LinguagemFormalException(e);
		}
		
		return interpretador;
	}
	
	//Alteracao TRT9 - tipo de dado variavel Valor
	public Valor processaValorEL(String text) {
		Valor valor = null;
		Object obj = new String();
		
		//busca valor do EL
		try {
			if (Expressions.instance().createValueExpression(text).getValue() != null) {
				obj = Expressions.instance().createValueExpression(text).getValue();
			}
			valor = TipoValor.converteValor(obj);
		} catch (TipoValorInvalidoException e) {
			valor = new ValorTexto(obj.toString());
		} catch (Exception e) {
			valor = new ValorTexto("");
		}

		return valor;
		
	}

	public String processaModelo(String modelo, String xsl) throws LinguagemFormalException {
		String html = processaModeloXsl(modelo, xsl);
		html = unescapeModelo(html.replaceAll("<\\?xml.*?>", ""));
		String modeloProcessado = processaVariaveisModelo(html);
		return modeloProcessado;		
	}

	private String unescapeModelo(String modelo) {
		modelo = StringEscapeUtils.unescapeXml(modelo);
		modelo = StringEscapeUtils.unescapeHtml(modelo);
		modelo = modelo.replaceAll("&apos;", "'");
		return modelo;
	}

	private String processaModeloXsl(String modelo, String xsl) {
		try {
			XSLTransformer st = new XSLTransformer(xsl);
			String transformado = st.transform(modelo);
			return transformado;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
