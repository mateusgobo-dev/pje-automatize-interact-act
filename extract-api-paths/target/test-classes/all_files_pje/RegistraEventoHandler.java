package br.com.infox.ibpm.expression;

public class RegistraEventoHandler extends GenericExpression {

	@Override
	protected String getRootName() {
		return "registraEventoAction";
	}

	@Override
	protected String getElementName() {
		return "agrupamento";
	}

	@Override
	protected String getSql() {
		return "select ds_agrupamento from tb_agrupamento where id_agrupamento = ?";
	}

}
