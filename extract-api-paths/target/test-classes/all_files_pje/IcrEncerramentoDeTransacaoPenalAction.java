package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrEncerramentoDeTransacaoPenal;
import br.jus.pje.nucleo.entidades.IcrTransacaoPenal;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.MotivoEncerramentoTransacaoPenal;

@Name("icrEncerramentoDeTransacaoPenalAction")
@Scope(ScopeType.CONVERSATION)
public class IcrEncerramentoDeTransacaoPenalAction extends
		IcrAssociarIcrAction<IcrEncerramentoDeTransacaoPenal, IcrEncerramentoDeTransacaoPenalManager>{

	private static final long serialVersionUID = -2600719996030358813L;

	public List<MotivoEncerramentoTransacaoPenal> getMotivosEncerramento(){
		return getManager().listarMotivosEncerramento();
	}

	@Override
	public String getTextNaoHaReusComSentencaParaAssociar(){
		return "IcrEncerramentoDeTransacaoPenal.label_nao_ha_reus";
	}

	@Override
	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada){
		getInstance().setTransacaoPenal((IcrTransacaoPenal) icrAfetada);
	}

	@Override
	public IcrTransacaoPenal getIcrAfetada(){
		return getInstance().getTransacaoPenal();
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao){
		// TODO Auto-generated method stub
	}
}
