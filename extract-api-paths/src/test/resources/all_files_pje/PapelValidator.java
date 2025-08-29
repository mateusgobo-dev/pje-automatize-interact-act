package br.com.infox.ibpm.validator;

public class PapelValidator extends LocalizacaoValidator {

	@Override
	protected String getSql() {
		return "select id_papel from tb_papel where ds_identificador = ?";
	}

	@Override
	protected String getOldIdPattern(String oldId) {
		return ":" + oldId + "\\b";
	}

	@Override
	protected String getNewIdPattern(String newId) {
		return ":" + newId;
	}

}
