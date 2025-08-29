package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.seam.international.StatusMessage.Severity;
import br.jus.pje.nucleo.entidades.ConcursoCrime;
import br.jus.pje.nucleo.entidades.IcrSentencaCondenatoria;
import br.jus.pje.nucleo.entidades.Pena;
import br.jus.pje.nucleo.entidades.PenaConcursoCrime;
import br.jus.pje.nucleo.entidades.PenaIndividualizada;
import br.jus.pje.nucleo.entidades.PenaTipificacao;
import br.jus.pje.nucleo.entidades.TipificacaoDelito;
import br.jus.pje.nucleo.entidades.TipoPena;
import br.jus.pje.nucleo.entidades.UnidadeMonetaria;
import br.jus.pje.nucleo.enums.GeneroPenaEnum;
import br.jus.pje.nucleo.enums.UnidadeMultaEnum;

public abstract class IcrAssociarPenaIndividualizadaAction<T extends IcrSentencaCondenatoria, J extends IcrAssociarPenaIndividualizadaManager<T>>
		extends InformacaoCriminalRelevanteAction<T, J>{

	public static final String TAB_PENA_INDIVIDUALIZADA = "abaPenaIndividualizada";

	private static final long serialVersionUID = 1L;
	private Pena penaEdit;
	private Pena penaOriginal;
	private GeneroPenaEnum generoPena;
	private List<TipoPena> tiposPena = new ArrayList<TipoPena>(0);
	private Map<Pena, Boolean> selecaoPenas = new HashMap<Pena, Boolean>();
	private Boolean exibirConfirmacaoCadastroPenaSubstitutiva = false;
	private Boolean substitutivaParaVariasPenas = false;
	// indica se o registro esta sendo editado, ou é um novo registro.
	private Boolean modoEdicao = false;

	public Pena getPenaEdit(){
		return penaEdit;
	}

	public void setPenaEdit(Pena penaEdit){
		this.penaEdit = penaEdit;
		if (penaEdit != null && penaEdit.getTipoPena() != null){
			generoPena = penaEdit.getTipoPena().getGeneroPena();
			getTiposPena().clear();
			getTiposPena().addAll(getManager().recuperarTiposPena(getGeneroPena()));
		}
		else{
			generoPena = null;
		}
		modoEdicao = true;
	}

	// -- operações para controle de gravação de penas
	public Boolean getModoEdicao(){
		return modoEdicao;
	}

	public void setModoEdicao(Boolean modoEdicao){
		this.modoEdicao = modoEdicao;
	}

	public GeneroPenaEnum getGeneroPena(){
		return generoPena;
	}

	public List<GeneroPenaEnum> getGeneros(){
		return getManager().recuperarGeneros(!getPenaEdit().getPenasOriginais().isEmpty() || substitutivaParaVariasPenas);
	}

	public void setGeneroPena(GeneroPenaEnum generoPena){
		this.generoPena = generoPena;
	}

	public List<TipoPena> getTiposPena(){
		return tiposPena;
	}

	public Map<Pena, Boolean> getSelecaoPenas(){
		return selecaoPenas;
	}

	public void setSelecaoPenas(Map<Pena, Boolean> selecaoPenas){
		this.selecaoPenas = selecaoPenas;
	}

	public Boolean getExibirConfirmacaoCadastroPenaSubstitutiva(){
		return exibirConfirmacaoCadastroPenaSubstitutiva;
	}

	public void setExibirConfirmacaoCadastroPenaSubstitutiva(Boolean exibirConfirmacaoCadastroPenaSubstitutiva){
		this.exibirConfirmacaoCadastroPenaSubstitutiva = exibirConfirmacaoCadastroPenaSubstitutiva;
	}

	public Boolean getSubstitutivaParaVariasPenas(){
		return substitutivaParaVariasPenas;
	}

	public void setSubstitutivaParaVariasPenas(Boolean substitutivaParaVariasPenas){
		this.substitutivaParaVariasPenas = substitutivaParaVariasPenas;

		if (substitutivaParaVariasPenas){
			selecaoPenas.clear();
			setPenaEdit(new PenaIndividualizada());

			for (Pena pena : getPenaPrivativaLiberdadeList()){
				selecaoPenas.put(pena, false);
			}
		}

		modoEdicao = false;
	}

	public void selecionarPenasAgrupadas(){
		for (Pena pena : selecaoPenas.keySet()){
			if (selecaoPenas.get(pena)){
				for (Pena substitutiva : pena.getPenasSubstitutivas()){
					for (Pena original : substitutiva.getPenasOriginais()){
						if (selecaoPenas.containsKey(original) && !selecaoPenas.get(original)){
							selecaoPenas.put(original, true);
						}
					}
				}
			}
		}
	}

	public void carregarTiposPena(){
		clearPenaEdit();
		getTiposPena().clear();
		getTiposPena().addAll(getManager().recuperarTiposPena(getGeneroPena()));
	}

	public void novaPena(){
		if (penaEdit instanceof PenaTipificacao){
			novaPenaTipificacao(((PenaTipificacao) penaEdit).getTipificacaoDelito());
		}
		else if (penaEdit instanceof PenaConcursoCrime){
			novaPenaConcursoCrime(((PenaConcursoCrime) penaEdit).getConcursoCrime());
		}
		modoEdicao = false;
		penaEdit.setIcrSentencaCondenatoria(getInstance());
	}

	public void novaPenaTipificacao(TipificacaoDelito tipificacaoDelito){
		exibirConfirmacaoCadastroPenaSubstitutiva = false;
		setPenaEdit(new PenaTipificacao());
		modoEdicao = false;
		((PenaTipificacao) penaEdit).setTipificacaoDelito(tipificacaoDelito);
	}

	public void novaPenaConcursoCrime(ConcursoCrime concursoCrime){
		exibirConfirmacaoCadastroPenaSubstitutiva = false;
		setPenaEdit(new PenaConcursoCrime());
		modoEdicao = false;
		((PenaConcursoCrime) penaEdit).setConcursoCrime(concursoCrime);
	}

	public void novaPenaSubstitutiva(Pena pena){
		exibirConfirmacaoCadastroPenaSubstitutiva = false;
		setPenaOriginal(pena);
		setPenaEdit(new PenaIndividualizada());
		penaEdit.getPenasOriginais().add(pena);
		penaEdit.setIcrSentencaCondenatoria(getInstance());
		modoEdicao = false;
	}

	public List<Pena> getPenaPrivativaLiberdadeList(){
		List<Pena> returnValue = new ArrayList<Pena>();
		for (Pena pena : getInstance().getPenaIndividualizadaList()){
			if (pena.getTipoPena().getGeneroPena() == GeneroPenaEnum.PL){
				returnValue.add(pena);
			}
		}
		return returnValue;
	}

	public void adicionarPena(){
		 GeneroPenaEnum generoPenaLocal = null;
		try{
			getManager().validarPena(penaEdit);
			if (!getInstance().getPenas().contains(penaEdit)){
				getInstance().getPenas().add(penaEdit);
				generoPenaLocal = penaEdit.getTipoPena().getGeneroPena();
				if (generoPenaLocal == GeneroPenaEnum.PL){
					exibirConfirmacaoCadastroPenaSubstitutiva = true;
				}
				else{
					novaPena();
				}
			
			}
		} catch (IcrValidationException e){
			addMessage(Severity.ERROR, e.getMessage(), null);
		}
	}

	public void adicionarPenaSubstitutiva(){
		List<Pena> penasCache = new ArrayList<Pena>();
		try{
			exibirConfirmacaoCadastroPenaSubstitutiva = false;
			getManager().validarPena(penaEdit);
			// adicao para varias penas originais
			if (substitutivaParaVariasPenas){
				/*
				 * Se as penas selecionadas não possuirem penas substitutivas em comum, limpar toda vinculação de penas
				 */
				List<Pena> originaisEmComum = new ArrayList<Pena>(0);
				boolean limpar = false;
				for (Pena pena : selecaoPenas.keySet()){
					if (selecaoPenas.get(pena)){
						if (pena.getPenasSubstitutivas().isEmpty()){
							limpar = true;
						}
						for (Pena substitutiva : pena.getPenasSubstitutivas()){
							if (originaisEmComum.isEmpty()){
								originaisEmComum.addAll(substitutiva.getPenasOriginais());
							}
							else if (!originaisEmComum.equals(substitutiva.getPenasOriginais())){
								limpar = true;
								break;
							}
						}
					}
				}

				for (Pena pena : selecaoPenas.keySet()){

					if (selecaoPenas.get(pena)){
						if (limpar){
							for (Pena substitutiva : pena.getPenasSubstitutivas()){
								for (Pena original : substitutiva.getPenasOriginais()){
									if (original != pena){
										original.getPenasSubstitutivas().clear();
									}
									getInstance().getPenas().removeAll(original.getPenasSubstitutivas());
								}
								substitutiva.getPenasOriginais().clear();
							}
							pena.getPenasSubstitutivas().clear();
						}
						penaEdit.getPenasOriginais().add(pena);
						pena.getPenasSubstitutivas().add(penaEdit);
						penasCache.add(penaEdit);
						penaOriginal = pena;

					}

				}
				if (penasCache.size() == 0){
					addMessage(Severity.ERROR, "Nenhuma pena Selecionada", null);
				}
				else{
					setSubstitutivaParaVariasPenas(true);
					novaPenaSubstitutiva(penaOriginal);
				}
			}
			// adição para uma unica pena original
			else{
				if (!penaOriginal.getPenasSubstitutivas().contains(penaEdit)){
				
					/*
					  guardar as penas originais do agrupamento de penas para vinculação das penas substitutivas, 
					  para evitar ConcurrentModificationException
					*/
					Set<Pena> penasOriginais = new HashSet<Pena>(0);

					penasCache.add(penaEdit);

					// adicionar esta pena substitutiva em todas penas originais em comum
					for (Pena penaSubstitutiva : penaOriginal.getPenasSubstitutivas()){
						for (Pena penaOriginal : penaSubstitutiva.getPenasOriginais()){
							if (!penaOriginal.getPenasSubstitutivas().contains(penaEdit)){
								penasOriginais.add(penaOriginal);
								// adicionar a substitutiva aqui, causa ConcurrentModificationException
							}
						}
					} 
					if(penasOriginais.isEmpty()){
						penaOriginal.getPenasSubstitutivas().add(penaEdit);
						penaEdit.getPenasOriginais().add(penaOriginal);
					}

					for (Pena penaOriginal : penasOriginais){
						if(!penaOriginal.getPenasSubstitutivas().contains(penaEdit)){
							penaOriginal.getPenasSubstitutivas().add(penaEdit);
						}
						if(!penaEdit.getPenasOriginais().contains(penaOriginal)){
							penaEdit.getPenasOriginais().add(penaOriginal);
						}
					}
				}
				novaPenaSubstitutiva(penaOriginal);
			}
			
			getManager().validarPenasIndividualizadas(getInstance());

		} catch (IcrValidationException e){
			addMessage(Severity.ERROR, "Operação Cancelada: " + e.getMessage(), e);
			for (Pena pena : penasCache){
				for (Pena penaOriginal : pena.getPenasOriginais()){
					if (penaOriginal != null){
						penaOriginal.getPenasSubstitutivas().remove(pena);
					}
				}
			}

		} catch (Exception e){
			addMessage(Severity.ERROR, e.getMessage(), e);
		}
	}

	public void remove(Pena pena){
		getInstance().getPenas().remove(pena);
		List<Pena> penaSubstitutivaList = new ArrayList<Pena>(0);
		for (Pena penaOriginal : getInstance().getPenaIndividualizadaOriginalList()){
			penaSubstitutivaList.addAll(penaOriginal.getPenasSubstitutivas());
		}

		for (Pena penaSubstitutiva : penaSubstitutivaList){
			penaSubstitutiva.getPenasOriginais().remove(pena);
		}
	}

	public void removePenaSubstitutiva(Pena pena){
		getInstance().getPenas().remove(pena);
		for (Pena penaOriginal : pena.getPenasOriginais()){
			penaOriginal.getPenasSubstitutivas().remove(pena);
		}
	}

	public String getDelitoString(){
		return getDelitoString(penaEdit);
	}

	public String getQtdIncidencia(){
		return getQtdIncidencia(penaEdit);
	}

	public String getDelitoString(Pena pena){
		if (pena != null){
			if (pena instanceof PenaTipificacao){
				return ((PenaTipificacao) pena).getTipificacaoDelito().getDelitoString();
			}
			else if (pena instanceof PenaConcursoCrime){
				return ((PenaConcursoCrime) pena).getConcursoCrime().getDelitosAssociadosString();
			}
		}
		return "";
	}

	public String getQtdIncidencia(Pena pena){
		if (pena instanceof PenaTipificacao){
			return String.valueOf(((PenaTipificacao) pena).getTipificacaoDelito().getQuantidadeIncidencia());
		}
		else{
			return "";
		}

	}

	public void fecharModalPena(){
		generoPena = null;
		penaEdit = null;
		exibirConfirmacaoCadastroPenaSubstitutiva = false;
		substitutivaParaVariasPenas = false;
	}

	public UnidadeMultaEnum[] getUnidadesMulta(){
		return UnidadeMultaEnum.values();
	}

	public void clearPenaEdit(){
		getPenaEdit().setAnosPenaInicial(null);
		getPenaEdit().setDescricaoBem(null);
		getPenaEdit().setDescricaoLocal(null);
		getPenaEdit().setDiasMulta(null);
		getPenaEdit().setDiasPenaInicial(null);
		getPenaEdit().setHorasPenaInicial(null);
		getPenaEdit().setMesesPenaInicial(null);
		getPenaEdit().setMultiplicadorPena(null);
		getPenaEdit().setUnidadeMonetaria(getUnidadeReal());
		getPenaEdit().setUnidadeMulta(null);
		getPenaEdit().setValorFracaoDiaMultaSalarioMinimo(null);
		getPenaEdit().setValorHistoricoPrevisto(null);
		getPenaEdit().setValorMulta(null);

		if (getPenaEdit() instanceof PenaIndividualizada){
			((PenaIndividualizada) getPenaEdit()).setAnosPenaAcrescimo(null);
			((PenaIndividualizada) getPenaEdit()).setDiasPenaAcrescimo(null);
			((PenaIndividualizada) getPenaEdit()).setHorasPenaAcrescimo(null);
			((PenaIndividualizada) getPenaEdit()).setMesesPenaAcrescimo(null);
		}
		modoEdicao = false;

	}

	private UnidadeMonetaria getUnidadeReal(){
		List<UnidadeMonetaria> unidades = getManager().recuperarUnidadesMonetarias();
		for (UnidadeMonetaria unidade : unidades){
			if (unidade.getDescricao().equalsIgnoreCase("real")){
				return unidade;
			}
		}
		return null;

	}

	public Pena getPenaOriginal(){
		return penaOriginal;
	}

	public void setPenaOriginal(Pena penaOriginal){
		this.penaOriginal = penaOriginal;
		if (penaOriginal != null && penaOriginal.getTipoPena() != null){
			generoPena = penaOriginal.getTipoPena().getGeneroPena();
			getTiposPena().clear();
			getTiposPena().addAll(getManager().recuperarTiposPena(getGeneroPena()));
		}
		else{
			generoPena = null;
		}
	}

	@Override
	public boolean exibirBotaoProximoPasso(){
		return !isManaged() && !getHome().getTab().equals(TAB_PENA_INDIVIDUALIZADA);
	}

	@Override
	public void next(){
		try{
			if (getHome().getTab().equals(InformacaoCriminalRelevanteHome.TAB_TIPIFICACAO_DELITO_ID)){
				getManager().validarPreenchimentoTipificacoes(getInstance());
				getHome().setTab(TAB_PENA_INDIVIDUALIZADA);
			}
			else{
				super.next();
			}
		} catch (IcrValidationException e){
			addMessage(Severity.ERROR, e.getMessage(), e);
		}
	}

	@Override
	public boolean exibirBotaoIncluir(){
		return !exibirBotaoProximoPasso() && !isManaged();
	}

	public boolean isPenaAgrupada(Pena penaOriginal){
		if (penaOriginal != null){
			for (Pena penaSubsitutiva : penaOriginal.getPenasSubstitutivas()){
				if (penaSubsitutiva.getPenasOriginais().size() > 1){
					return true;
				}
			}
		}

		return false;
	}

	public void editarPenaOriginal(){
		if (penaOriginal != null){
			setPenaEdit(penaOriginal);
		}
		else{
			setSubstitutivaParaVariasPenas(true);
		}
	}

	/**
	 * @return Penas associadas com o delito de PenaEdit
	 */
	public List<Pena> getPenasAssociadas(){
		List<Pena> returnValue = new ArrayList<Pena>();

		for (Pena pena : getInstance().getPenaIndividualizadaOriginalList()){
			if (penaEdit instanceof PenaTipificacao && pena instanceof PenaTipificacao){
				returnValue.add(pena);
			}
			else if (penaEdit instanceof PenaConcursoCrime && pena instanceof PenaConcursoCrime){
				returnValue.add(pena);
			}
		}

		return returnValue;
	}

	public List<PenaAposSubstituicao> getPenaAposSubstituicaoList(){
		List<PenaAposSubstituicao> penaAposSubstituicaoList = new ArrayList<PenaAposSubstituicao>(0);
		Set<Pena> penasOriginaisJaVerificadas = new HashSet<Pena>(0);

		for (PenaIndividualizada penaOriginal : getInstance().getPenaIndividualizadaOriginalList()){
			PenaAposSubstituicao penaAposSubstituicao = new PenaAposSubstituicao();
			if (!penaOriginal.getPenasSubstitutivas().isEmpty()){
				for (Pena penaSubstitutiva : penaOriginal.getPenasSubstitutivas()){
					if (!penasOriginaisJaVerificadas.contains(penaOriginal)){
						penaAposSubstituicao.getPenasOriginais().addAll(penaSubstitutiva.getPenasOriginais());

						for (Pena penaOriginalDaSubstitutiva : penaSubstitutiva.getPenasOriginais()){
							if (penaOriginalDaSubstitutiva != null){
								penasOriginaisJaVerificadas.add(penaOriginalDaSubstitutiva);
								penaAposSubstituicao.getDelitos().add(((PenaIndividualizada) penaOriginalDaSubstitutiva).getDetalheDelito());
							}
						}
					}
					penaAposSubstituicao.getPenasSubstitutivas().add(penaSubstitutiva);
				}
			}
			else{
				penaAposSubstituicao.getPenasOriginais().add(penaOriginal);
				penaAposSubstituicao.getDelitos().add(penaOriginal.getDetalheDelito());
			}
			if (!penaAposSubstituicao.getPenasOriginais().isEmpty()){
				penaAposSubstituicaoList.add(penaAposSubstituicao);
			}
		}

		return penaAposSubstituicaoList;
	}

	public class PenaAposSubstituicao{

		private List<Pena> penasOriginais = new ArrayList<Pena>(0);
		private List<Pena> penasSubstitutivas = new ArrayList<Pena>(0);
		private List<String> delitos = new ArrayList<String>(0);

		public List<Pena> getPenasOriginais(){
			return penasOriginais;
		}

		public void setPenasOriginais(List<Pena> penasOriginais){
			this.penasOriginais = penasOriginais;
		}

		public List<Pena> getPenasSubstitutivas(){
			return penasSubstitutivas;
		}

		public void setPenasSubstitutivas(List<Pena> penasSubstitutivas){
			this.penasSubstitutivas = penasSubstitutivas;
		}

		public List<String> getDelitos(){
			return delitos;
		}

		public void setDelitos(List<String> delitos){
			this.delitos = delitos;
		}

	}
	
	
	@Override
	public void remove(TipificacaoDelito tipificacaoDelito) {
		boolean canRemove = true;
		for(Pena pena : getInstance().getPenas()){
			if(pena instanceof PenaTipificacao){
				if(((PenaTipificacao) pena).getTipificacaoDelito().equals(tipificacaoDelito)){
					canRemove = false;
					break;
				}
			}
			if(pena instanceof PenaConcursoCrime){
				if(((PenaConcursoCrime) pena).getConcursoCrime().getTipificacoes().contains(tipificacaoDelito)){
					canRemove = false;
					break;
				}
			}
		}
		if(canRemove){
			super.remove(tipificacaoDelito);
		}
		else{
			addMessage(Severity.ERROR, "Não é Possível Excluir!\nExistem Penas Cadastradas Para esta Tipificação/Concurso", null);
		}
	}
}
