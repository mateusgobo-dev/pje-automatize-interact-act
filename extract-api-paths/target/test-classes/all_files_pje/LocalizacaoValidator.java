package br.com.infox.ibpm.validator;

public class LocalizacaoValidator extends AbstractValidator {

	@Override
	protected String getSql() {
		return "select id_localizacao from tb_localizacao where ds_localizacao = ?";
	}

	@Override
	protected String getExpression() {
		return "localizacaoAssignment.getPooledActors";
	}

	@Override
	protected String getOldIdPattern(String oldId) {
		return "\\b" + oldId + ":";
	}

	@Override
	protected String getNewIdPattern(String newId) {
		return newId + ":";
	}

}
