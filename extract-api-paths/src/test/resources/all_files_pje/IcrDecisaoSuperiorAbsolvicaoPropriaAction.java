package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrDecisaoSuperiorAbsolvicaoPropria;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.enums.EfeitoSobreSentencaAnteriorEnum;

@Name("icrDecisaoSuperiorAbsolvicaoPropriaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrDecisaoSuperiorAbsolvicaoPropriaAction extends
		IcrAssociarIcrAction<IcrDecisaoSuperiorAbsolvicaoPropria, IcrDecisaoSuperiorAbsolvicaoPropriaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 902829982398035288L;

	@Override
	public void init() {
		super.init();
		/* RI079 */
		List<ProcessoEvento> movimentacoes = getInstance().getProcessoEventoList();
		if (movimentacoes != null && movimentacoes.size() > 0 && getInstance().getIcrAfetada() == null) {
			getInstance().setData(movimentacoes.get(movimentacoes.size() - 1).getDataAtualizacao());
			getInstance().setDataPublicacao(movimentacoes.get(movimentacoes.size() - 1).getDataAtualizacao());
		}
	}

	@Override
	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada) {
		getInstance().setIcrAfetada(icrAfetada);
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDataPublicacao(dtPublicacao);
	}

	public EfeitoSobreSentencaAnteriorEnum[] getEfeitos() {
		return EfeitoSobreSentencaAnteriorEnum.values();
	}

	@Override
	public String getTextNaoHaReusComSentencaParaAssociar() {
		return "Não foram encontrados Réus com Sentenças passíveis de Absolvição";
	}

	@Override
	public InformacaoCriminalRelevante getIcrAfetada() {
		return getInstance().getIcrAfetada();
	}
}
