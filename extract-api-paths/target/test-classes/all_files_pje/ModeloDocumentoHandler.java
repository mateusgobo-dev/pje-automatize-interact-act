package br.com.infox.ibpm.expression;

public class ModeloDocumentoHandler extends GenericExpression {

	@Override
	protected String getRootName() {
		return "modeloDocumento";
	}

	@Override
	protected String getElementName() {
		return "modelo";
	}

	@Override
	protected String getSql() {
		return "select ds_titulo_modelo_documento from tb_modelo_documento where id_modelo_documento = ?";
	}

}
