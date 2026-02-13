package br.com.infox.cliente.home.icrrefactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrDecisaoSuperiorAbsolvicaoImpropria;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.enums.EfeitoSobreSentencaAnteriorEnum;
import br.jus.pje.nucleo.enums.PrazoMinAnosMedidaSegurancaEnum;
import br.jus.pje.nucleo.enums.PrazoMinMesesMedidaSegurancaEnum;
import br.jus.pje.nucleo.enums.TipoMedidaSegurancaEnum;

@Name("icrDecisaoSuperiorAbsolvicaoImpropriaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrDecisaoSuperiorAbsolvicaoImpropriaAction extends
		IcrAssociarIcrAction<IcrDecisaoSuperiorAbsolvicaoImpropria, IcrDecisaoSuperiorAbsolvicaoImpropriaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3740954116768924880L;

	@Override
	public String getTextNaoHaReusComSentencaParaAssociar() {
		return "Não foram encontrados Réus com Sentenças passíveis de Absolvição Imprópria";
	}

	@Override
	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada) {
		getInstance().setIcrAfetada(icrAfetada);
	}

	@Override
	public InformacaoCriminalRelevante getIcrAfetada() {
		return getInstance().getIcrAfetada();
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDtPublicacao(dtPublicacao);
	}

	@Override
	public void init() {
		super.init();
		if (getInstance().getNrAnoPrazo() != null && getInstance().getNrAnoPrazo() == 3)
			verificaPrazoMinMedida();
		if (getInstance().getId() == null) {
			getInstance().setNrAnoPrazo(1);
		}
	}

	private boolean bloquearPrazoMes;

	/**
	 * Método utilizado pelo view para carregar o Prazo Mínimo para Medida de
	 * Segurança em anos segundo a RD016
	 * 
	 * @return List<PrazoMinAnosMedidaSegurancaEnum>
	 */
	public List<PrazoMinAnosMedidaSegurancaEnum> getListNrPrazoAno() {
		return Arrays.asList(PrazoMinAnosMedidaSegurancaEnum.values());
	}

	/**
	 * Método utilizado pelo view para carregar o Prazo Mínimo para Medida de
	 * Segurança em meses segundo a RD016
	 * 
	 * @return List<PrazoMinAnosMedidaSegurancaEnum>
	 */
	public List<PrazoMinMesesMedidaSegurancaEnum> getListNrPrazoMes() {
		return Arrays.asList(PrazoMinMesesMedidaSegurancaEnum.values());
	}

	/**
	 * Método para a validação do campo Prazo Mínimo para Medida de Segurança em
	 * Anos conforme RI026
	 * 
	 */
	public void verificaPrazoMinMedida() {
		if (getInstance().getNrAnoPrazo() == 3) {
			setBloquearPrazoMes(true);
		} else {
			setBloquearPrazoMes(false);
		}
	}

	public List<TipoMedidaSegurancaEnum> getTipoMedidaSegurancaEnum() {
		return Arrays.asList(TipoMedidaSegurancaEnum.values());
	}

	public List<EfeitoSobreSentencaAnteriorEnum> getEfeitoSobreSentencaAnterior() {
		return Arrays.asList(EfeitoSobreSentencaAnteriorEnum.values());
	}

	public boolean isBloquearPrazoMes() {
		return bloquearPrazoMes;
	}

	public void setBloquearPrazoMes(boolean bloquearPrazoMes) {
		this.bloquearPrazoMes = bloquearPrazoMes;
	}
}
