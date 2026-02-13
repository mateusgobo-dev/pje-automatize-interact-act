package br.jus.cnj.pje.util;

/**
 * @author <Everton Nogueira> - <Maio/2016>
 * @Descrição: Enum responsável por mapear situações de um documento com relação a assinatura e relator do mesmo.
 * 1 - Se o orgão julgador selecionado for diferente do orgão julgador da sessão
 * 2 - Se o orgão julgador selecionado for igual ao orgão julador da sessão E o documento estiver assinado.
 * 3 - Se o orgão julgador selecionado for igual ao orgão julador da sessão E o documento NÃO estiver assinado.
 */
public enum SituacaoDocumentoSessao {
	NAO_RELATOR(1), 
	RELATOR_DOCUMENTO_ASSINADO(2), 
	RELATOR_DOCUMENTO_NAO_ASSINADO(3);
	
	private Integer situacao;

	private SituacaoDocumentoSessao(Integer situacao) {
		this.situacao = situacao;
	}

	public Integer getSituacao() {
		return situacao;
	}
}
