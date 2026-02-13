package br.jus.cnj.pje.servicos;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;

@Name(EditorTextoService.NAME)
@Scope(ScopeType.EVENT)
public class EditorTextoService implements Serializable
{
	private static final LogProvider log = Logging.getLogProvider(EditorTextoService.class);


	private static final long serialVersionUID = 1L;
	public final static String NAME = "editorTextoService";
	public static final String VARIAVEL_LIMPAR_DOCUMENTO_FRAME = "pje:fluxo:editorTexto:limparDocumento";
	public static final String VARIAVEL_OBRIGATORIO = "pje:fluxo:editorTexto:obrigatorio";

	
    @In
    private TramitacaoProcessualService tramitacaoProcessualService;

	/**
	 * [PJEII-4997] - TSE
	 * A chamada deste método no evento de "entrar na tarefa" define que, o documento criado pelo frame editorTexto, será finalizadao após a tarefa ser encerrada, 
	 * não sendo possível recuperar ele em outras tarefa.
	 */
	public void escopoDoDocumentoNaTarefa()
	{
		tramitacaoProcessualService.gravaVariavelTarefa(VARIAVEL_LIMPAR_DOCUMENTO_FRAME, "1");
	}
	
	/**
	 * [PJEII-4997] - TSE
	 * A chamada deste método nos eventos de entrar na tarefa define que o documento criado pelo frame editorTexto, nunca será finalizados, mesmo se assinado,  
	 * sendo possível recuperá-lo em tarefa subsequente que possua o mesmo frame
	 */
	public void escopoDoDocumentoNoFluxo()
	{
		tramitacaoProcessualService.gravaVariavelTarefa(VARIAVEL_LIMPAR_DOCUMENTO_FRAME, "2");
	}

	public void obrigatorio()
	{
		tramitacaoProcessualService.gravaVariavelTarefa(VARIAVEL_OBRIGATORIO, true);
	}
}
