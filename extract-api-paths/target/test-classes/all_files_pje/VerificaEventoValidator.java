package br.com.infox.ibpm.validator;

public class VerificaEventoValidator extends AbstractValidator {

	@Override
	protected String getSql() {
		return "select id_agrupamento from tb_agrupamento where ds_agrupamento = ?";
	}

	@Override
	protected String getExpression() {
		return "verificaEventoAction.verificarEventos";
	}

}
