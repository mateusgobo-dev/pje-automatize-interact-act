package br.com.infox.bpm.taskPage.Di;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.bpm.taskPage.FGPJE.EditorAbstractTaskPageAction;

@Name(value = VerificarPrevencaoTaskPageAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class VerificarPrevencaoTaskPageAction extends EditorAbstractTaskPageAction {

	private static final String DESCRICAO_PROCESSO_DOCUMENTO = "Certidão da Prevenção";

	private static final long serialVersionUID = 1L;

	public static final String NAME = "verificarPrevencaoTaskPageAction";

	private static final String NOME_VARIAVEL_EDITOR = "textEditSignature:Certidao_da_Prevencao";

	@Override
	protected String getNomeVariavelIdDocumento() {
		return NOME_VARIAVEL_EDITOR;
	}

	@Override
	protected void setDescricaoProcessoDocumento() {
		processoDocumento.setProcessoDocumento(DESCRICAO_PROCESSO_DOCUMENTO);
	}

}
