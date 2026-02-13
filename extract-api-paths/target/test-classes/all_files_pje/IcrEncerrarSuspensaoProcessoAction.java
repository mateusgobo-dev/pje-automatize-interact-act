package br.com.infox.cliente.home.icrrefactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import br.jus.pje.nucleo.entidades.IcrEncerrarSuspensaoProcesso;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.enums.MotivoEncerramentoSuspensaoEnum;

@Name("icrEncerrarSuspensaoProcessoAction")
@Scope(ScopeType.CONVERSATION)
@AutoCreate()
public class IcrEncerrarSuspensaoProcessoAction extends
		IcrAssociarIcrAction<IcrEncerrarSuspensaoProcesso, IcrEncerrarSuspensaoProcessoManager>{

	private static final long serialVersionUID = 4463174127738957585L;

	@Override
	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada){
		getInstance().setIcrAfetada(icrAfetada);
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao){
		getInstance().setDtDecisaoEncerramento(dtPublicacao);
	}

	public List<MotivoEncerramentoSuspensaoEnum> getMotivoEncerramentoSuspensaoEnum(){
		return Arrays.asList(MotivoEncerramentoSuspensaoEnum.values());
	}

	@Override
	public String getTextNaoHaReusComSentencaParaAssociar(){
		return "IcrEncerrarSuspensaoProcesso.label_nao_ha_reus";
	}

	@Override
	public InformacaoCriminalRelevante getIcrAfetada(){
		return getInstance().getIcrAfetada();
	}

	public Boolean getExibirModal(){
		return false;
	}
}
