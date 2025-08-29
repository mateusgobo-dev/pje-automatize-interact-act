package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrDesclassificacao;

@Name("icrDesclassificacaoAction")
@Scope(ScopeType.CONVERSATION)
public class IcrDesclassificacaoAction extends
		InformacaoCriminalRelevanteAction<IcrDesclassificacao, IcrDesclassificacaoManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7363288513674845762L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDataPublicacao(dtPublicacao);
	}
}
