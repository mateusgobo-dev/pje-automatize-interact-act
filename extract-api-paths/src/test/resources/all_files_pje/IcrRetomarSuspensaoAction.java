package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.Messages;

import br.jus.pje.nucleo.entidades.IcrRetomarSuspensao;
import br.jus.pje.nucleo.entidades.IcrSuspenderSuspensao;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrRetomarSuspensaoAction")
@Scope(ScopeType.CONVERSATION)
public class IcrRetomarSuspensaoAction extends IcrAssociarIcrAction<IcrRetomarSuspensao, IcrRetomarSuspensaoManager>{

	private static final long serialVersionUID = -5543850324679709087L;

	@Override
	public String getTextNaoHaReusComSentencaParaAssociar(){
		return Messages.instance().get("IcrRetomarSuspensao.label_nao_ha_reus");
	}

	@Override
	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada){
		getInstance().setIcrAfetada((IcrSuspenderSuspensao) icrAfetada);
	}

	@Override
	public void setDtPublicacao(Date dtPublicacao){
		getInstance().setDataDecisao(dtPublicacao);
	}

	@Override
	public InformacaoCriminalRelevante getIcrAfetada(){
		return getInstance().getIcrAfetada();
	}
}
