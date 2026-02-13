package br.com.infox.cliente.home.icrrefactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrSuspenderSuspensao;
import br.jus.pje.nucleo.entidades.IcrSuspensao;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.enums.MotivoEncerramentoSuspensaoEnum;

@Name("icrSuspenderSuspensaoAction")
@Scope(ScopeType.CONVERSATION)
public class IcrSuspenderSuspensaoAction extends
		IcrAssociarIcrAction<IcrSuspenderSuspensao, IcrSuspenderSuspensaoManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8290102869300831510L;

	@Override
	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada) {
		getInstance().setIcrAfetada((IcrSuspensao) icrAfetada);
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		;
	}

	public List<MotivoEncerramentoSuspensaoEnum> getMotivoEncerramentoSuspensaoEnum() {
		return Arrays.asList(MotivoEncerramentoSuspensaoEnum.values());
	}

	@Override
	public String getTextNaoHaReusComSentencaParaAssociar() {
		return "Não foram encontrados réus com solicitação de suspender suspensão do Processo";
	}

	@Override
	public InformacaoCriminalRelevante getIcrAfetada() {
		// TODO Auto-generated method stub
		return getInstance().getIcrAfetada();
	}
}