package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.international.StatusMessage.Severity;
import br.jus.pje.nucleo.entidades.IcrSentencaCondenatoria;
import br.jus.pje.nucleo.entidades.Pena;
import br.jus.pje.nucleo.entidades.PenaIndividualizada;
import br.jus.pje.nucleo.entidades.PenaTotal;
import br.jus.pje.nucleo.entidades.TipoPena;
import br.jus.pje.nucleo.enums.GeneroPenaEnum;

public class IcrAssociarPenaTotalAction<T extends IcrSentencaCondenatoria, J extends IcrAssociarPenaTotalManager<T>>
		extends IcrAssociarPenaIndividualizadaAction<T, J> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1322836114041890406L;
	public static final String TAB_PENA_TOTAL = "abaPenaTotal";

	public GeneroPenaEnum[] getGenerosPenaTotal() {
		List<GeneroPenaEnum> generoPenaList = new ArrayList<GeneroPenaEnum>(0);

		for (PenaIndividualizada pena : getInstance()
				.getPenaIndividualizadaList()) {
			if (!generoPenaList.contains(pena.getTipoPena().getGeneroPena())) {
				generoPenaList.add(pena.getTipoPena().getGeneroPena());
			}

			for (Pena penaSubstitutiva : pena.getPenasSubstitutivas()) {
				if (!generoPenaList.contains(penaSubstitutiva.getTipoPena()
						.getGeneroPena())) {
					generoPenaList.add(penaSubstitutiva.getTipoPena()
							.getGeneroPena());
				}
			}
		}

		GeneroPenaEnum[] returnValue = new GeneroPenaEnum[generoPenaList.size()];
		generoPenaList.toArray(returnValue);

		return returnValue;
	}

	public List<TipoPena> getTiposPenaTotal() {
		List<TipoPena> returnValue = new ArrayList<TipoPena>(0);
		if (getGeneroPena() != null) {
			for (PenaIndividualizada pena : getInstance()
					.getPenaIndividualizadaList()) {

				if (pena.getTipoPena().getGeneroPena().equals(getGeneroPena())
						&& !returnValue.contains(pena.getTipoPena())) {
					returnValue.add(pena.getTipoPena());
				}

				for (Pena penaSubstitutiva : pena.getPenasSubstitutivas()) {
					if (penaSubstitutiva.getTipoPena().getGeneroPena()
							.equals(getGeneroPena())
							&& !returnValue.contains(penaSubstitutiva
									.getTipoPena())) {
						returnValue.add(penaSubstitutiva.getTipoPena());
					}
				}

			}
		}
		return returnValue;
	}

	public void sugerirPenaTotal() {
		try {
			getPenaEdit().setMultiplicadorPena(0.0);
			getPenaEdit().setValorFracaoDiaMultaSalarioMinimo(0.0);
			getPenaEdit().setValorHistoricoPrevisto(0.0);
			getPenaEdit().setValorMulta(0.0);

			for (Pena penaIndividualizada : getPenaIndividualizadaList(getPenaEdit()
					.getTipoPena())) {

				getPenaEdit().setDescricaoBem(
						penaIndividualizada.getDescricaoBem());
				getPenaEdit().setDescricaoLocal(
						penaIndividualizada.getDescricaoLocal());
				getPenaEdit().setUnidadeMonetaria(
						penaIndividualizada.getUnidadeMonetaria());
				getPenaEdit().setUnidadeMulta(
						penaIndividualizada.getUnidadeMulta());

				if (penaIndividualizada.getAnosPenaInicial() != null) {
					if (getPenaEdit().getAnosPenaInicial() == null) {
						getPenaEdit().setAnosPenaInicial(0);
					}
					getPenaEdit()
							.setAnosPenaInicial(
									getPenaEdit().getAnosPenaInicial()
											+ (penaIndividualizada
													.getAnosPenaInicial() + ((PenaIndividualizada) penaIndividualizada)
													.getAnosPenaAcrescimo()));
				}
				if (penaIndividualizada.getDiasMulta() != null) {
					if (getPenaEdit().getDiasMulta() == null) {
						getPenaEdit().setDiasMulta(0);
					}
					getPenaEdit().setDiasMulta(
							getPenaEdit().getDiasMulta()
									+ penaIndividualizada.getDiasMulta());
				}
				if (penaIndividualizada.getDiasPenaInicial() != null) {
					if (getPenaEdit().getDiasPenaInicial() == null) {
						getPenaEdit().setDiasPenaInicial(0);
					}
					getPenaEdit()
							.setDiasPenaInicial(
									getPenaEdit().getDiasPenaInicial()
											+ (penaIndividualizada
													.getDiasPenaInicial() + ((PenaIndividualizada) penaIndividualizada)
													.getDiasPenaAcrescimo()));
				}
				if (penaIndividualizada.getHorasPenaInicial() != null) {
					if (getPenaEdit().getHorasPenaInicial() == null) {
						getPenaEdit().setHorasPenaInicial(0);
					}
					getPenaEdit()
							.setHorasPenaInicial(
									getPenaEdit().getHorasPenaInicial()
											+ (penaIndividualizada
													.getHorasPenaInicial() + ((PenaIndividualizada) penaIndividualizada)
													.getHorasPenaAcrescimo()));
				}
				if (penaIndividualizada.getMesesPenaInicial() != null) {
					if (getPenaEdit().getMesesPenaInicial() == null) {
						getPenaEdit().setMesesPenaInicial(0);
					}
					getPenaEdit()
							.setMesesPenaInicial(
									getPenaEdit().getMesesPenaInicial()
											+ (penaIndividualizada
													.getMesesPenaInicial() + ((PenaIndividualizada) penaIndividualizada)
													.getMesesPenaAcrescimo()));
				}
				if (penaIndividualizada.getMultiplicadorPena() != null) {
					if (getPenaEdit().getMultiplicadorPena() == null) {
						getPenaEdit().setMultiplicadorPena(0.0);
					}
					getPenaEdit().setMultiplicadorPena(
							getPenaEdit().getMultiplicadorPena()
									+ penaIndividualizada
											.getMultiplicadorPena());
				}
				if (penaIndividualizada.getValorFracaoDiaMultaSalarioMinimo() != null) {
					if (getPenaEdit().getValorFracaoDiaMultaSalarioMinimo() == null) {
						getPenaEdit().setValorFracaoDiaMultaSalarioMinimo(0.0);
					}
					getPenaEdit()
							.setValorFracaoDiaMultaSalarioMinimo(
									getPenaEdit()
											.getValorFracaoDiaMultaSalarioMinimo()
											+ penaIndividualizada
													.getValorFracaoDiaMultaSalarioMinimo());
				}
				if (penaIndividualizada.getValorHistoricoPrevisto() != null) {
					if (getPenaEdit().getValorHistoricoPrevisto() == null) {
						getPenaEdit().setValorHistoricoPrevisto(0.0);
					}
					getPenaEdit().setValorHistoricoPrevisto(
							getPenaEdit().getValorHistoricoPrevisto()
									+ penaIndividualizada
											.getValorHistoricoPrevisto());
				}
				if (penaIndividualizada.getValorMulta() != null) {
					if (getPenaEdit().getValorMulta() == null) {
						getPenaEdit().setValorMulta(0.0);
					}
					getPenaEdit().setValorMulta(
							getPenaEdit().getValorMulta()
									+ penaIndividualizada.getValorMulta());
				}

			}
		} catch (Exception e) {
			addMessage(Severity.ERROR, e.getMessage(), e);
		}
	}

	@Override
	public void clearPenaEdit() {
		super.clearPenaEdit();
		if (getPenaEdit() instanceof PenaTotal
				&& getPenaEdit().getPenasOriginais().isEmpty()) {

			sugerirPenaTotal();
		}
	}

	public List<Pena> getPenaIndividualizadaList(TipoPena tipoPena) {
		List<Pena> returnValue = new ArrayList<Pena>(0);
		for (Pena pena : getInstance().getPenaIndividualizadaList()) {
			if (pena.getTipoPena().getGeneroPena().equals(GeneroPenaEnum.PL)) {
				returnValue.add(pena);
			}
			else if (pena.getTipoPena().equals(tipoPena)) {
				returnValue.add(pena);
			}
			for (Pena penaSubstitutiva : pena.getPenasSubstitutivas()) {
				if(penaSubstitutiva.getTipoPena().getGeneroPena().equals(GeneroPenaEnum.PL)){
					returnValue.add(penaSubstitutiva);
				}
				else if (penaSubstitutiva.getTipoPena().equals(tipoPena)) {
					returnValue.add(penaSubstitutiva);
				}
			}

		}
		return returnValue;
	}

	@Override
	public void next() {
		try {
			if (getHome().getTab().equals(TAB_PENA_INDIVIDUALIZADA)) {
				getManager().validarPenasIndividualizadas(getInstance());
				getHome().setTab(TAB_PENA_TOTAL);
			} else {
				super.next();
			}
		} catch (IcrValidationException e) {
			addMessage(Severity.ERROR, e.getMessage(), e);
		}
	}

	@Override
	public void adicionarPena() {
		try {
			if (getPenaEdit() instanceof PenaTotal) {
				getPenaEdit().setIcrSentencaCondenatoria(getInstance());
				getManager().validarPena(getPenaEdit());
				if (!getInstance().getPenaTotalList().contains(getPenaEdit())) {
					getInstance().getPenas().add((PenaTotal) getPenaEdit());
				}
				if (getPenaEdit().getTipoPena().getGeneroPena() == GeneroPenaEnum.PL) {
					setExibirConfirmacaoCadastroPenaSubstitutiva(true);
				}
//				novaPenaTotal();
			} else {
				super.adicionarPena();
			}
		} catch (IcrValidationException e) {
			addMessage(Severity.ERROR, e.getMessage(), null);
		}
	}

	@Override
	public void adicionarPenaSubstitutiva() {
		try {
			if (getPenaEdit() instanceof PenaTotal) {
				getPenaEdit().setIcrSentencaCondenatoria(getInstance());
				getManager().validarPena(getPenaEdit());
				if (!getPenaOriginal().getPenasSubstitutivas().contains(
						getPenaEdit())) {
					getPenaOriginal().getPenasSubstitutivas()
							.add(getPenaEdit());
				}

				getManager().validarPenaTotal(getInstance());
				addMessage(Severity.INFO, "Operação Realizada com Sucesso!",
						null);
				fecharModalPena();
			} else {
				super.adicionarPenaSubstitutiva();
			}

		} catch (IcrValidationException e) {
			addMessage(Severity.ERROR, "Operação Cancelada: " + e.getMessage(),
					e);
			getPenaOriginal().getPenasSubstitutivas().remove(getPenaEdit());
		}
	}

	@Override
	public void novaPena() {
		if (getPenaEdit() instanceof PenaTotal) {
			novaPenaTotal();
		} else {
			super.novaPena();
		}
	}

	public void novaPenaTotal() {
		setPenaEdit(new PenaTotal());
		setModoEdicao(false);
	}

	@Override
	public void novaPenaSubstitutiva(Pena pena) {
		if (pena instanceof PenaTotal) {
			setExibirConfirmacaoCadastroPenaSubstitutiva(false);
			setPenaOriginal(pena);
			setPenaEdit(new PenaTotal());
			getPenaEdit().getPenasOriginais().add(pena);
			setModoEdicao(false);
		} else {
			super.novaPenaSubstitutiva(pena);
		}
	}
	
	public void removePenaTotal(PenaTotal penaTotal) {
		if (penaTotal.getPenasOriginais() != null) {
			for (Pena item : penaTotal.getPenasOriginais()) {
				item.getPenasSubstitutivas().remove(penaTotal);
			}
		} else {
			getInstance().getPenas().remove(penaTotal);
		}
	}

	@Override
	public boolean exibirBotaoProximoPasso() {
		return !isManaged() && !getHome().getTab().equals(TAB_PENA_TOTAL);
	}
}
