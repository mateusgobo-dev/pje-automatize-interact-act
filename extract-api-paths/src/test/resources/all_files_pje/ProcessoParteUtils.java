package br.jus.cnj.pje.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import org.jboss.seam.annotations.Name;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe utilitária que retorna os tipos e nomes das partes de um processo formatado.
 * 
 * @author lourival
 */
@Name("processoParteUtils")
public class ProcessoParteUtils {

	/**
	 * Obtêm os nomes das partes do polo ativo de um processo, juntamente de seus respectivos advogados.
	 * @param processo O processo do qual se deseja obter as partes do polo ativo.
	 * @param separadorLinha O separador de linhas que será utilizado, caso não seja informado, 
	 * será utilizado a quebra de linha padrão do Unix("\n").
	 * @param considerarSegredoSigilo marcador para levar em consideração se o sigilo do processo/parte será considerado para retorno do nome
	 * @return String - nome das partes do polo ativo.
	 */
	public static String obterNomesPartesPoloAtivo(ProcessoTrf processo, String separadorLinha, boolean considerarSegredoSigilo) {
		return obterProcessoParte(processo, ProcessoParteParticipacaoEnum.A, separadorLinha, considerarSegredoSigilo);
	}
	
	/**
	 * Obtêm os nomes das partes do polo passivo de um processo, juntamente de seus respectivos advogados.
	 * @param processo O processo do qual se deseja obter as partes do polo passivo.
	 * @param separadorLinha O separador de linhas que será utilizado, caso não seja informado, 
	 * será utilizado a quebra de linha padrão do Unix("\n").
	 * @param considerarSegredoSigilo marcador para levar em consideração se o sigilo do processo/parte será considerado para retorno do nome
	 * @return String - nome das partes do polo passivo.
	 */
	public static String obterNomesPartesPoloPassivo(ProcessoTrf processo, String separadorLinha, boolean considerarSegredoSigilo) {
		return obterProcessoParte(processo, ProcessoParteParticipacaoEnum.P, separadorLinha, considerarSegredoSigilo);
	}
	
	/**
	 * Obtêm uma String contendo as partes de um processo, juntamente de seus respectivos advogados.
	 * @param processo O processo do qual se deseja obter as partes.
	 * @param participacao O tipo de participação que se deseja filtrar as partes.
	 * @param separadorLinha O separador de linhas que será utilizado, caso não seja informado, 
	 * será utilizado a quebra de linha padrão do Unix("\n").
	 * @param considerarSegredoSigilo marcador para levar em consideração se o sigilo do processo/parte será considerado para retorno do nome
	 * @return String - tipo e nome das partes formatados.
	 */
	public static String obterProcessoParte(ProcessoTrf processo, ProcessoParteParticipacaoEnum participacao, 
			String separadorLinha, boolean considerarSegredoSigilo) {
		String str = null;
		if (processo != null) {
			if (StringUtils.isEmpty(separadorLinha)) {
				separadorLinha = "\n";
			}

			StringBuilder builder = new StringBuilder();

			List<ProcessoParte> processoParteList = obterPartesProcessoPorPolo(processo, participacao);

			String partesStr = obterPartes(processoParteList, separadorLinha, considerarSegredoSigilo);
			String advogadosStr = obterAdvogados(processoParteList, separadorLinha, considerarSegredoSigilo);

			if (StringUtils.isNotBlank(partesStr)) {
				builder.append(partesStr);
				if (StringUtils.isNotEmpty(advogadosStr)) {
					builder.append(separadorLinha);
					builder.append(advogadosStr);
				}
			} else if (StringUtils.isNotBlank(advogadosStr)) {
				builder.append(advogadosStr);
			}
			str = builder.toString().trim();
		}
		return str;
	}

	/**
	 * Obtem partes do processo por polo.
	 * @param processo - processo que contem as partes.
	 * @param participacao - polo da parte.
	 * @return List<ProcessoParte> - lista das partes do processo.
	 */
	private static List<ProcessoParte> obterPartesProcessoPorPolo(ProcessoTrf processo, ProcessoParteParticipacaoEnum participacao) {
		List<ProcessoParte> processoParteList = null;
		switch (participacao) {
			case A:
				processoParteList = processo.getProcessoPartePoloAtivoSemAdvogadoList();
				break;
			case P:
				processoParteList = processo.getProcessoPartePoloPassivoSemAdvogadoList();
				break;
			default:
				processoParteList = processo.getProcessoParteSemAdvogadoList();
				break;
		}
		return processoParteList;
	}

	/**
	 * Obtem os nomes das partes do processo.
	 * @param processoParteList - lista de partes do processo.
	 * @param separadorLinha  - separador de linha. Ex: \n, <br/>.
	 * @return String - nomes formatados das partes do processo.
	 */
	private static String obterPartes(List<ProcessoParte> processoParteList, String separadorLinha, boolean considerarSegredoSigilo) {
		StringBuilder builder = new StringBuilder();
		Set<TipoParte> tipoParteList = new HashSet<TipoParte>(0);
		int cont = 0;
		for (ProcessoParte processoParte : processoParteList) {
			TipoParte tipoParte = processoParte.getTipoParte();
			if (!tipoParteList.contains(tipoParte)) {
				boolean segredo = considerarSegredoSigilo && (processoParte.getParteSigilosa() || processoParte.getProcessoTrf().getSegredoJustica());
				String partesPorTipo = obterDetalhesPartes(processoParteList, tipoParte, segredo);
				if (cont > 0) {
					builder.append(separadorLinha);
				}
				builder.append(partesPorTipo);
				tipoParteList.add(tipoParte);
			}
			cont++;
		}

		return builder.toString();
	}

	/**
	 * Obtem o nome dos advogados das partes do processo.
	 * @param processoParteList - lista de partes do processo.
	 * @param separadorLinha - separador de linha. Ex: \n, <br/>.
	 * @return String - retorna o nome dos advogados das partes do processo.
	 */
	private static String obterAdvogados(List<ProcessoParte> processoParteList, String separadorLinha, boolean considerarSegredoSigilo) {
		StringBuilder builder = new StringBuilder();
		int cont = 0;
		for (ProcessoParte processoParte : processoParteList) {
			TipoParte tipoParte = processoParte.getTipoParte();
			List<ProcessoParteRepresentante> processoParteRepresentanteList = processoParte.getProcessoParteRepresentanteList();

			if (processoParteRepresentanteList != null && !processoParteRepresentanteList.isEmpty()) {
				if (cont > 0) {
					builder.append(separadorLinha);
				}

				if (processoParte.getProcessoParteRepresentanteList().size() > 1) {
					builder.append("Advogados ");
				} else {
					builder.append("Advogado ");
				}

				builder.append("do(a) " + tipoParte.getTipoParte() + ": ");
				
				boolean segredo = considerarSegredoSigilo && (processoParte.getParteSigilosa() || processoParte.getProcessoTrf().getSegredoJustica());
				String partesRepresentates = obterProcessoParteRepresentante(processoParteRepresentanteList, segredo);

				builder.append(partesRepresentates);
			}
			cont++;
		}

		return builder.toString();
	}

	/**
	 * Obtem a oab e o nome dos advogados das partes do processo.
	 * @param processoParteRepresentanteList - lista de partes representantes do processo.
	 * @return String - retorna a oab e o nome dos advogados das partes do processo formatada.
	 */
	private static String obterProcessoParteRepresentante(List<ProcessoParteRepresentante> processoParteRepresentanteList, boolean segredo) {
		StringBuilder builder = new StringBuilder();
		int cont = 0;
		StringBuilder nomeParte = null;
		for (ProcessoParteRepresentante processoParteRepresentante : processoParteRepresentanteList) {
			if(processoParteRepresentante.getParteRepresentante().getInSituacao().equals(ProcessoParteSituacaoEnum.A)) {
				TipoParte tipoParteAdvogado = ParametroUtil.instance().getTipoParteAdvogado();
				nomeParte = new StringBuilder();
				
				if(processoParteRepresentante.getParteRepresentante().getTipoParte().equals(tipoParteAdvogado)) {
					PessoaAdvogado pessoaAdvogado = ((PessoaFisica) processoParteRepresentante.getParteRepresentante().getPessoa()).getPessoaAdvogado();
					nomeParte.append(pessoaAdvogado.getNomeParte().toUpperCase());
					if (StringUtils.isNotBlank(pessoaAdvogado.getOabFormatado())) {
						nomeParte.append(" - " + pessoaAdvogado.getOabFormatado());
					}
				} else {
					Pessoa pessoa = processoParteRepresentante.getParteRepresentante().getPessoa();
					nomeParte.append(pessoa.getNomeParte().toUpperCase());
				}
				
				if (cont > 0) {
					builder.append(", ");
				}
				if(segredo) {
					if(ParametroUtil.instance().isConteudoSigiloso()) {
						builder.append("SIGILOSO");
					} else {
						builder.append(StringUtil.obtemIniciais(nomeParte.toString()));
					}
				} else {
					builder.append(nomeParte);
				}
				cont++;
			}
		}
		return builder.toString();
	}

	/**
	 * Obtem o tipo e o nome das partes do processo 
	 * @param processoParteList - lista contendo as partes do processo.
	 * @param tipoParte - tipo da parte
	 * @return String - retorna o tipo e o nome das partes do processo.
	 */
	private static String obterDetalhesPartes(List<ProcessoParte> processoParteList, TipoParte tipoParte, boolean segredo) {
		StringBuilder builder = new StringBuilder();
		int count = 0;
		if(segredo && ParametroUtil.instance().isConteudoSigiloso()) {
			builder.append("SIGILOSO");
		} else {
			for (ProcessoParte processoParte : processoParteList) {
				if (tipoParte.equals(processoParte.getTipoParte())) {
					if (count == 0) {
						builder.append(tipoParte.toString().toUpperCase() + ": ");
					}
					if (count > 0) {
						builder.append(", ");
					}
					builder.append(processoParte.getNomeParte().toUpperCase());
					count++;
				}
			}
		}
		return builder.toString();
	}

}
