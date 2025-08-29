package br.com.infox.ibpm.expression;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory para ExpressionHandler
 * 
 * @author ruiz
 * 
 */
public class ExpressionHandlerFactory {

	/**
	 * Expressões que fazem referencia a registros da base de dados
	 */
	private static Map<String, ExpressionHandler> expressionMap;

	static {
		expressionMap = new HashMap<String, ExpressionHandler>();
		expressionMap.put("localizacaoAssignment.getPooledActors", new LocalizacaoHandler());
		expressionMap.put("registraEventoAction.registra", new RegistraEventoHandler());
		expressionMap.put("verificaEventoAction.canTransit", new VerificaEventoHandler());
		expressionMap.put("verificaEventoAction.verificarEventos", new VerificaEventoHandler());
		expressionMap.put("modeloDocumento.set", new ModeloDocumentoHandler());
	}

	/**
	 * 
	 * @param expressao
	 *            a ser tratada
	 * @return tratador da expressão
	 */
	public static ExpressionHandler getOutputHandler(String expressao) {
		expressao = expressao.replaceFirst("^!", "");
		String prefix = expressao.split("\\(")[0];
		ExpressionHandler eh = expressionMap.get(prefix);
		if (eh == null) {
			GenericExpression ge = new GenericExpression();
			ge.validateOn();
			eh = ge;
		}
		eh.setExpression(expressao);
		return eh;
	}

}
