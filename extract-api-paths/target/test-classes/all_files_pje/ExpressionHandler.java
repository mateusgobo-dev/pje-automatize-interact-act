package br.com.infox.ibpm.expression;

import java.util.List;

import org.jdom.Element;

public interface ExpressionHandler {

	void execute();

	void setExpression(String expression);

	Element getXml();

	List<String> getWarningList();

}
