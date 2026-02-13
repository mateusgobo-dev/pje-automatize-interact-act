package br.jus.pje.nucleo.enums;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.CriterioFiltro;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

public enum TipoCriterioEnum implements PJeEnum {
	SQ("Número sequência") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return isProcessInPattern(processoTrf, criterio.getValorCriterio()) || testaNumero(processoTrf.getNumeroSequencia(), criterio.getValorCriterio());
		}
	},
	DV("Dígito verificador") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return testaNumero(processoTrf.getNumeroDigitoVerificador(), criterio.getValorCriterio());
		}
	},
	AN("Ano do processo") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return testaNumero(processoTrf.getAno(), criterio.getValorCriterio());
		}
	},
	OR("Número da origem") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return false;
		}
	},
	IO("Identificação do órgão") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return false;
		}
	},
	NP("Expressão número do processo") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return false;
		}
	}, 
	AS("Assunto") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return isAssuntoInProcessoAssuntoList(processoTrf, criterio.getValorCriterio());
		}
	}, 
	CJ("Classe judicial") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return testaNumero(processoTrf.getClasseJudicial().getIdClasseJudicial(), criterio.getValorCriterio());
		}
	}, 
	PA("Nome da parte") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return isNomeInProcessoParteList(processoTrf, criterio.getValorCriterio());
		}
	}, 
	CC("CPF | CNPJ") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return isCNPJouCPFProcessoParteList(processoTrf, criterio.getValorCriterio());
		}
	}, 
	PR("Prioridade") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return isPrioridadeInProcessoPrioridadeList(processoTrf, criterio.getValorCriterio());
		}
	}, 
	ID("Data de distribuição (início)") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			Date dataInicial = DateUtil.stringToDate(criterio.getValorCriterio(), "dd/MM/yyyy");
			return processoTrf.getDataDistribuicao().equals(dataInicial) || processoTrf.getDataDistribuicao().after(dataInicial);
		}
	}, 
	FD("Data de distribuição (fim)") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			Date dataFim = DateUtil.stringToDate(criterio.getValorCriterio()+" 23:59:59", "dd/MM/yyyy HH:mm:ss");
			return processoTrf.getDataDistribuicao().equals(dataFim) || processoTrf.getDataDistribuicao().before(dataFim);
		}
	}, 
	ME("Envolve criança ou adolescente") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return temMenor(processoTrf);
		}
	}, 
	SI("Somente sigiloso") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return processoTrf.getSegredoJustica();
		}
	}, 
	TA("Tarefa") {
		@SuppressWarnings("unchecked")
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			if (ArrayUtils.isNotEmpty(objetosOpcionais)) {
				List<SituacaoProcesso> situacoesProcesso = (List<SituacaoProcesso>) objetosOpcionais[0];
				return situacoesProcesso.stream().anyMatch(s -> s.getNomeTarefa().equals(criterio.getTextoCriterio()));
			}
			return false;
		}
	},
	OJ("Órgão julgador") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return testaNumero(processoTrf.getOrgaoJulgador().getIdOrgaoJulgador(), criterio.getValorCriterio());
		}
	},
	OC("Órgão julgador colegiado") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return processoTrf.getOrgaoJulgadorColegiado() != null && testaNumero(processoTrf.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado(), criterio.getValorCriterio());
		}
	},
	JU("Jurisdição") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return testaNumero(processoTrf.getJurisdicao().getIdJurisdicao(), criterio.getValorCriterio());
		}
	},
	PL("Pedido de liminar") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return processoTrf.getTutelaLiminar();
		}
	},
	/*dados eleitorais*/
	ES("Estado") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return processoTrf.getComplementoJE() != null && processoTrf.getComplementoJE().getEstadoEleicao() != null && testaNumero(processoTrf.getComplementoJE().getEstadoEleicao().getIdEstado(), criterio.getValorCriterio());
		}
	},
	MU("Município") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return processoTrf.getComplementoJE() != null && processoTrf.getComplementoJE().getMunicipioEleicao() != null && testaNumero(processoTrf.getComplementoJE().getMunicipioEleicao().getIdMunicipio(), criterio.getValorCriterio());
		}
	},
	AE("Ano eleição") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			return processoTrf.getComplementoJE() != null && processoTrf.getComplementoJE().getEleicao() != null && testaNumero(processoTrf.getComplementoJE().getEleicao().getAno(), criterio.getValorCriterio());
		}
	},
	MO("Movimento") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			if (ArrayUtils.isNotEmpty(objetosOpcionais) && objetosOpcionais[0] != null && objetosOpcionais[0] instanceof ProcessoEvento) {
				ProcessoEvento ultimoMovimento = (ProcessoEvento) objetosOpcionais[0];
				return testaNumero(ultimoMovimento.getEvento().getIdEvento(), criterio.getValorCriterio());
			}
			return false;
		}
	},
	DO("Documento") {
		@Override
		public boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais) {
			if (ArrayUtils.isNotEmpty(objetosOpcionais) && objetosOpcionais[0] != null && objetosOpcionais[0] instanceof ProcessoDocumento) {
				ProcessoDocumento ultimoDocumento = (ProcessoDocumento) objetosOpcionais[0];
				return testaNumero(ultimoDocumento.getTipoProcessoDocumento().getIdTipoProcessoDocumento(), criterio.getValorCriterio());
			}
			return false;
		}
	};
	

	
	private String label;
	
	private TipoCriterioEnum (String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}
	
	public abstract boolean isMatched(ProcessoTrf processoTrf, CriterioFiltro criterio, Object... objetosOpcionais);
	
	/**
	 * Verifica se o numeroSequencia de um processo se encaixa com algum filtro das caixas
	 * @param processoTrf processo a ser testado
	 * @param pattern filtro da caixa
	 * @return true se for compativel
	 */
	private static boolean isProcessInPattern(ProcessoTrf processoTrf, String pattern) {
		Integer seqProcesso = processoTrf.getNumeroSequencia();
		if(!StringUtil.isEmpty(pattern) && seqProcesso!=null){
			List<String> quebrados = new CopyOnWriteArrayList<>(Arrays.asList(pattern.split(";")));
			HashMap<Integer, Integer> rangesProcessos = new HashMap<>();
			for (String item : quebrados) {
				if (item.length()>1){
					String[] strRange = item.split("-");
					if (strRange.length==2 && 
						StringUtil.ehInteiro(strRange[0]) &&
						StringUtil.ehInteiro(strRange[1])) {
						rangesProcessos.put(Integer.parseInt(strRange[0]), Integer.parseInt(strRange[1]));
					}
					quebrados.remove(item);
				}
			}
			String padrao = !quebrados.isEmpty() ? quebrados.toString() : StringUtils.EMPTY;
			
			String strSeqProcesso = String.valueOf(seqProcesso);
			return strSeqProcesso.substring(strSeqProcesso.length()-1) .matches(padrao) || estaEntre(seqProcesso, rangesProcessos);
		}
		return false;
	}
	
	/**
	 * Verifica se um número estáentre algum dos ranges passados no hashmap
	 * @param numero numero a ser verificado
	 * @param ranges ranges possiveis
	 * @return true
	 */
	private static boolean estaEntre(Integer numero, HashMap<Integer, Integer> ranges){
		Set<Integer> chaves  = ranges.keySet();
		for (Integer chave : chaves) {
			Integer inicio;
			Integer fim;
			if (chave>ranges.get(chave)){
				inicio=ranges.get(chave);
				fim = chave;
			}else{
				inicio=chave;
				fim=ranges.get(chave);
			}
			if (numero>=inicio && numero<=fim){
				return true;
			}
		}
		return false;
	}
	
	private static boolean testaNumero(Integer numero, String valorComparacao) {
		try {
			return Integer.parseInt(valorComparacao) == numero;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
	
	/**
	 * Verifica se o assunto está no processo
	 */
	private static boolean isAssuntoInProcessoAssuntoList(ProcessoTrf processoTrf, String valorCriterio) {
		try {
			Integer idAssunto = Integer.parseInt(valorCriterio);
			List<AssuntoTrf> assuntos = processoTrf.getAssuntoTrfList();
			for (AssuntoTrf assunto : assuntos) {
				if (idAssunto.equals(assunto.getIdAssuntoTrf())) {
					return true;
				}
			}
			return false;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
	
	/**
	 * Verifica se o nome está no processo.
	 */
	private static boolean isNomeInProcessoParteList(ProcessoTrf processoTrf, String str) {
		boolean encontrado = false;
		if (str != null && !str.trim().isEmpty()) {
			List<ProcessoParte> ppList = processoTrf.getListaPartePoloObj(false,ProcessoParteParticipacaoEnum.values());
			for (ProcessoParte processoParte : ppList) {
				if (!ObjectUtils.equals(processoParte.getPessoa().getNome(), null) && processoParte.getPessoa().getNome().equalsIgnoreCase(str)){
					encontrado = true;
					break;
				}
			}
		}
		return encontrado;
	}
	
	/**
	 * Verifica se o CPF ou CNPJ estáno processo.
	 * @param processoTrf processo a ser testado
	 * @param String CPF ou CNPJ
	 * @return true se for compativel
	 */
	private static boolean isCNPJouCPFProcessoParteList(ProcessoTrf processoTrf, String str) {
		boolean encontrado = false;
		if (str != null && !str.trim().isEmpty()) {
			List<ProcessoParte> ppList = processoTrf.getListaPartePoloObj(false,ProcessoParteParticipacaoEnum.values());
			for (ProcessoParte processoParte : ppList) {
				if (processoParte.getPessoa().getDocumentoCpfCnpj() != null && ((processoParte.getInParticipacao() == ProcessoParteParticipacaoEnum.A)||(processoParte.getInParticipacao() == ProcessoParteParticipacaoEnum.P)||(processoParte.getInParticipacao() == ProcessoParteParticipacaoEnum.T)) && (processoParte.getPessoa().getDocumentoCpfCnpj().contains(str))){
					encontrado = true;
					break;
				}
			}
		}
		return encontrado;
	}
	
	/**
	 * Verifica se a prioridade está no processo
	 */
	private static boolean isPrioridadeInProcessoPrioridadeList(ProcessoTrf processoTrf, String valorCriterio) {
		try {
			Integer idPrioridade = Integer.parseInt(valorCriterio);
			List<PrioridadeProcesso> prioridades = processoTrf.getPrioridadeProcessoList();
			for (PrioridadeProcesso prioridade : prioridades) {
				if (idPrioridade.equals(prioridade.getIdPrioridadeProcesso())) {
					return true;
				}
			}
			return false;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
	
	private static boolean temMenor(ProcessoTrf processoTrf) {
		boolean encontrado = false;

		List<ProcessoParte> ppList = processoTrf.getListaPartePrincipal(false,ProcessoParteParticipacaoEnum.values());
		for (ProcessoParte processoParte : ppList) {
			if (processoParte.getPessoa().isMenor()){
					encontrado = true;
					break;
			}
		}
		return encontrado;
	}
}