package br.jus.cnj.pje.util.formatadorLista;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mvel2.MVEL;
import org.mvel2.ParserContext;

public class FormatadorLista {

	private static final String INDEX = ":index";
	
	private FormatadorLista formatadorPai;
	private List<?> lista;
	private String preTexto;
	private boolean mostraPreTextoListaVazia;
	private String posTexto;
	private boolean mostraPosTextoListaVazia;
	private String modelo;
	private String separador;
	private String ultimoSeparador;
	private Filtro filtro;
	private String ordem;
	private Index index;
	private int limite;
	private String textoPosLimite;
	private List<Atributo> atributos;
	private List<FormatadorLista> appendedLists = new ArrayList<FormatadorLista>();
	private String filterExpression;	
	
	
	public FormatadorLista() {
		this.formatadorPai = this;
		this.lista  = new LinkedList<Object>();
		this.preTexto = "";
		this.mostraPreTextoListaVazia = false;
		this.posTexto = "";
		this.mostraPosTextoListaVazia = false;
		this.modelo = "";
		this.separador = "";
		this.ultimoSeparador = "";
		this.filtro = null;
		this.ordem  = "";
		this.index = new Index(0);
		this.limite = Integer.MAX_VALUE;
		this.textoPosLimite = "";
		this.atributos = new ArrayList<Atributo>();
	}
	
	public FormatadorLista(FormatadorLista pai) {
		this();
		this.formatadorPai = pai;
	}
	
	public FormatadorLista setLista(List<?> lista){
		this.lista = new ArrayList<Object>(lista);
		return this;
	}
	
	public FormatadorLista appendLista(List<?> lista){
		FormatadorLista novoFormatador = new FormatadorLista(this);
		novoFormatador.setLista(lista);
		novoFormatador.index = this.index;
		this.appendedLists.add(novoFormatador);
		return novoFormatador;
	}
	
	public FormatadorLista setPreTexto(String preTexto) {
		return setPreTexto(preTexto, this.mostraPreTextoListaVazia);
	}
	
	public FormatadorLista setPreTexto(String preTexto, boolean mostraPreTextoListaVazia) {
		this.preTexto = removeHtml(preTexto);
		this.mostraPreTextoListaVazia = mostraPreTextoListaVazia;
		return this;
	}

	public FormatadorLista setPosTexto(String posTexto) {
		return setPosTexto(posTexto, this.mostraPosTextoListaVazia);
	}
	
	public FormatadorLista setPosTexto(String posTexto, boolean mostraPosTextoListaVazia) {
		this.posTexto = removeHtml(posTexto);
		this.mostraPosTextoListaVazia = mostraPosTextoListaVazia;
		return this;
	}
	
	public FormatadorLista setModelo(String modelo) {
		this.modelo = removeHtml(modelo);
		return this;
	}
	
	public FormatadorLista setSeparador(String separador) {
		return setSeparador(separador, separador);
	}

	public FormatadorLista setSeparador(String separador, String ultimoSeparador) {
		this.separador = removeHtml(separador);
		this.ultimoSeparador = removeHtml(ultimoSeparador);
		return this;
	}
	
	public Filtro setFiltroAnd() {
		return setFiltro(Filtro.AND);
	}

	public Filtro setFiltroOr() {
		return setFiltro(Filtro.OR);
	}
	
	private Filtro setFiltro(int tipo) {
		this.filtro = new Filtro(this, tipo);
		return filtro;
	}
	
	public FormatadorLista setOrdem(String ordem) {
		this.ordem = ordem;
		return this;
	}
	
	public FormatadorLista setLimite(int limite, String textoPosLimite) {
		setLimite(limite);
		return setTextoPosLimite(textoPosLimite);
	}

	public FormatadorLista setLimite(int limite) {
		this.limite = limite;
		return this;
	}

	public FormatadorLista setTextoPosLimite(String textoPosLimite) {
		this.textoPosLimite = removeHtml(textoPosLimite);
		return this;
	}
	
	public FormatadorLista setAtributo(String key, String property) {
		atributos.add(new AtributoSimples(key, property));
		return this;
	}
	
	/**
	 * Na EL não está aceitando o método com parâmetros dinâmicos, por isso fizemos de 0 até 10 parâmetros.
	 */
	public FormatadorLista setAtributo(String key, Object bean, String methodName, String property) {
		return setAtributoInterno(key, bean, methodName, property);
	}

	public FormatadorLista setAtributo(String key, Object bean, String methodName, String property, Object p1) {
		return setAtributoInterno(key, bean, methodName, property, p1);
	}

	public FormatadorLista setAtributo(String key, Object bean, String methodName, String property, Object p1, Object p2) {
		return setAtributoInterno(key, bean, methodName, property, p1, p2);
	}

	public FormatadorLista setAtributo(String key, Object bean, String methodName, String property, Object p1, Object p2, Object p3) {
		return setAtributoInterno(key, bean, methodName, property, p1, p2, p3);
	}

	public FormatadorLista setAtributo(String key, Object bean, String methodName, String property, Object p1, Object p2, Object p3, Object p4) {
		return setAtributoInterno(key, bean, methodName, property, p1, p2, p3, p4);
	}

	public FormatadorLista setAtributo(String key, Object bean, String methodName, String property, Object p1, Object p2, Object p3, Object p4, Object p5) {
		return setAtributoInterno(key, bean, methodName, property, p1, p2, p3, p4, p5);
	}

	public FormatadorLista setAtributo(String key, Object bean, String methodName, String property, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
		return setAtributoInterno(key, bean, methodName, property, p1, p2, p3, p4, p5, p6);
	}

	public FormatadorLista setAtributo(String key, Object bean, String methodName, String property, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
		return setAtributoInterno(key, bean, methodName, property, p1, p2, p3, p4, p5, p6, p7);
	}

	public FormatadorLista setAtributo(String key, Object bean, String methodName, String property, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
		return setAtributoInterno(key, bean, methodName, property, p1, p2, p3, p4, p5, p6, p7, p8);
	}

	public FormatadorLista setAtributo(String key, Object bean, String methodName, String property, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
		return setAtributoInterno(key, bean, methodName, property, p1, p2, p3, p4, p5, p6, p7, p8, p9);
	}

	public FormatadorLista setAtributo(String key, Object bean, String methodName, String property, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10) {
		return setAtributoInterno(key, bean, methodName, property, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);
	}
	
	private FormatadorLista setAtributoInterno(String key, Object bean, String methodName, String property, Object... methodParams) {
		atributos.add(new AtributoExterno(key, bean, methodName, property, methodParams));
		return this;
	}
	
	public FormatadorLista setAtributoLista(String key, String property) {
		FormatadorLista novoFormatador = new FormatadorLista(this);
		atributos.add(new AtributoLista(key, property, novoFormatador));
		return novoFormatador;
	}
	
	public String getFilterExpression() {
		return filterExpression;
	}

	public FormatadorLista setFilterExpression(String filterExpression) {
		this.filterExpression = filterExpression;
		return this;
	}

	public FormatadorLista endLista() {
		return formatadorPai;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		tratarFiltro();
		tratarOrdenacao();
		construirPreTexto(builder);
		construirTexto(builder);
		construirPosTexto(builder);
		tratarAppendedLists(builder);
		
		return builder.toString();
	}
	

	private void tratarAppendedLists(StringBuilder builder) {
		for (FormatadorLista formatadorLista : appendedLists) {
			builder.append(formatadorLista.toString());
		}
	}

	private void construirPosTexto(StringBuilder builder) {
		if(limite < lista.size()) {
			builder.append(this.textoPosLimite);
		}

		if(this.mostraPosTextoListaVazia || lista.size() > 0) {
			builder.append(this.posTexto);
		}
	}

	private void construirTexto(StringBuilder builder) {
		int max = Math.min(lista.size(), limite);

		for(int i=0 ; i<max ; i++) {
			if(i > 0) {
				if(i < lista.size() - 1) {
					builder.append(separador);
				} else {
					builder.append(ultimoSeparador);
				}
			}
			builder.append(parse(lista.get(i)));
		}
	}

	private void construirPreTexto(StringBuilder builder) {
		if(this.mostraPreTextoListaVazia || lista.size() > 0) {
			builder.append(this.preTexto);
		}
	}
	

	private void tratarFiltro(){
		if(this.lista != null && !this.lista.isEmpty()) {
			if (filterExpression != null){
				filtrarLista(filterExpression);
			}
			 
			if(filtro != null) {
				filtrarLista(filtro);
			}
		}
	}
	
	
	private void filtrarLista(Filtro filtro) {
		List<Object> tmpList = new LinkedList<Object>();
		for(Object o : this.lista) {
			if(filtro.eval(o)) {
				tmpList.add(o);
			}
		}
		this.lista = tmpList;
	}

	private void filtrarLista(String expressao) {
		List<Object> tmpList = new LinkedList<Object>();
		Set<String> variaveisExpressao = obterVariaveisExpressao(expressao);
		
		for(Object o : this.lista) {
			Map<String, Object> valoresVariaveisMap = new HashMap<String, Object>();

			for (String variavel : variaveisExpressao){
				valoresVariaveisMap.put(variavel, PropUtils.getProperty(o, variavel) );
			}
			
			if (MVEL.evalToBoolean(expressao, valoresVariaveisMap)){
				tmpList.add(o);
			}
		}

		this.lista = tmpList;
	}
	
	private Set<String> obterVariaveisExpressao(String expressao){
		ParserContext pc = new ParserContext();
		MVEL.analysisCompile(expressao, pc);
		return pc.getInputs().keySet();
	}
	

	private void tratarOrdenacao(){
		if(ordem != null && ordem.length() > 0) {
			Collections.sort(lista, new PropertyComparator(ordem));
		}
	}
	
	
	private String parse(Object obj) {
		String result = modelo;
		
		for(Atributo att : atributos) {
			result = result.replaceAll(":" + att.getKey(), att.eval(obj));
		}
		
		result = result.replaceAll(INDEX, Integer.toString(index.next()));
		
		return result;
	}
	
	
	
	
	private String removeHtml(String txt) {
		return txt.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");
	}
	
}
