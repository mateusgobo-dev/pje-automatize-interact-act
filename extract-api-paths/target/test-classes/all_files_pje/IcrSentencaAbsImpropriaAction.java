package br.com.infox.cliente.home.icrrefactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrSentencaAbsImpropria;
import br.jus.pje.nucleo.enums.PrazoMinAnosMedidaSegurancaEnum;
import br.jus.pje.nucleo.enums.PrazoMinMesesMedidaSegurancaEnum;
import br.jus.pje.nucleo.enums.TipoMedidaSegurancaEnum;

@Name("icrSentencaAbsImpropriaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrSentencaAbsImpropriaAction extends
		InformacaoCriminalRelevanteAction<IcrSentencaAbsImpropria, IcrSentencaAbsImpropriaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7579922694424877353L;
	private boolean bloquearPrazoMes;

	public boolean isBloquearPrazoMes() {

		return bloquearPrazoMes;
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDtPublicacao(dtPublicacao);
	}

	public void setBloquearPrazoMes(boolean bloquearPrazoMes) {
		this.bloquearPrazoMes = bloquearPrazoMes;
	}

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

}
