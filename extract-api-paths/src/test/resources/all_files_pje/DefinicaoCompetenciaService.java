/**
 * 
 */
package br.jus.cnj.pje.nucleo.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.nucleo.manager.CompetenciaManager;
import br.jus.cnj.pje.nucleo.manager.DimensaoFuncionalManager;
import br.jus.cnj.pje.nucleo.manager.DimensaoPessoalManager;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.AssociacaoDimensaoPessoalEnum;
import br.jus.pje.nucleo.entidades.AutoridadeAfetada;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.DimensaoAlcada;
import br.jus.pje.nucleo.entidades.DimensaoFuncional;
import br.jus.pje.nucleo.entidades.DimensaoPessoal;
import br.jus.pje.nucleo.entidades.DimensaoPessoalPessoa;
import br.jus.pje.nucleo.entidades.DimensaoPessoalTipoPessoa;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.DimensaoAlcada.TipoIntervalo;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

/**
 * @author cristof
 *
 */
@Name("definicaoCompetenciaService")
@Scope(ScopeType.EVENT)
public class DefinicaoCompetenciaService {
	
	@In
	private CompetenciaManager competenciaManager;
	
	@In
	private DimensaoPessoalManager dimensaoPessoalManager;
	
	@In
	private DimensaoFuncionalManager dimensaoFuncionalManager;
	
	@Logger
	private Log logger;
	
	
	public List<Competencia> getCompetencias(Jurisdicao jurisdicao, OrgaoJulgador orgaoJulgador, 
			OrgaoJulgadorColegiado orgaoJulgadorColegiado, boolean somenteIncidental){
		
		return competenciaManager.getCompetenciasPossiveisUsuario(jurisdicao, orgaoJulgador, orgaoJulgadorColegiado, somenteIncidental);
	}
	
	
	/**
	 * Retorna competência possíveis de serem aplicadas a um processo numa dada jurisdição
	 * @param processo ProcessoTrf em questão
	 * @param jurisdicao Jurisdicao em questão. Parâmetro útil em processos de redistribuição, onde a jurisdição de destino pode não
	 * ser a mesma jurisdição em que o processo está atualmente vinculado.
	 * @return
	 */
	public List<Competencia> getCompetencias(ProcessoTrf processo, Jurisdicao jurisdicao){
		if (jurisdicao == null) {
			return new ArrayList<Competencia>();
		}
		
		if(processo.getClasseJudicial() == null || processo.getAssuntoTrfList().size() == 0){
			boolean isProcessoIncidental = processo.getIsIncidente();
			OrgaoJulgador ojProcesso = processo.getOrgaoJulgador();
			OrgaoJulgadorColegiado ojcProcesso = processo.getOrgaoJulgadorColegiado();
			return getCompetencias(jurisdicao, ojProcesso, ojcProcesso, isProcessoIncidental);
		}
		List<Competencia> essenciais = competenciaManager.getCompetenciasBasicas(processo, jurisdicao);

		List<Competencia> pessoaisVedadas = new ArrayList<Competencia>();
		List<Competencia> pessoaisExigidas = new ArrayList<Competencia>();
		carregaCompetenciasPessoais(processo, essenciais, pessoaisExigidas, pessoaisVedadas, jurisdicao);
		
		List<Competencia> funcionaisVedadas = new ArrayList<Competencia>();
		List<Competencia> funcionaisExigidas = new ArrayList<Competencia>();
		carregaCompetenciasFuncionais(processo, essenciais, funcionaisExigidas, funcionaisVedadas, jurisdicao);
		
		List<Competencia> alcadaVedadas = new ArrayList<Competencia>(0);
		List<Competencia> alcadaExclusivas = new ArrayList<Competencia>(0);
		carregaCompetenciasAlcadas(processo, essenciais, alcadaExclusivas, alcadaVedadas);
		List<Competencia> competencias = identificaCompetenciasAptas(essenciais, pessoaisVedadas, pessoaisExigidas, funcionaisVedadas, funcionaisExigidas, alcadaVedadas, alcadaExclusivas);
		return competencias;
	}
	
	
	/**
	 * Retorna competência possíveis de serem aplicadas a um processo na jurisdição em que o mesmo se encontra
	 * @param processo ProcessoTrf em questão
	 * @return
	 */
	public List<Competencia> getCompetencias(ProcessoTrf processo){
		return getCompetencias(processo, processo.getJurisdicao());
	}

	private List<Competencia> identificaCompetenciasAptas(List<Competencia> essenciais, 
																											List<Competencia> pessoaisVedadas, 
																											List<Competencia> pessoaisExclusivas, 
																											List<Competencia> funcionaisVedadas, 
																											List<Competencia> funcionaisNecessarias, 
																											List<Competencia> alcadaVedadas,
																											List<Competencia> alcadaNecessarias) {
		Set<Competencia> aptas = new HashSet<Competencia>(essenciais);
		aptas.removeAll(pessoaisVedadas);
		aptas.removeAll(funcionaisVedadas);
		aptas.removeAll(alcadaVedadas);
		if(aptas.size() > 0){
			Set<Competencia> aptasPessoais = new HashSet<Competencia>(aptas);
			Set<Competencia> aptasFuncionais = new HashSet<Competencia>(aptas);
			Set<Competencia> aptasAlcada = new HashSet<Competencia>(aptas);
			if(pessoaisExclusivas.size() > 0){
				aptasPessoais.retainAll(pessoaisExclusivas);
			}
			if(funcionaisNecessarias.size() > 0){
				aptasFuncionais.retainAll(funcionaisNecessarias);
			}
			if(alcadaNecessarias.size() > 0 ){
				aptasAlcada.retainAll(alcadaNecessarias);
			}
			aptas.retainAll(aptasPessoais);
			aptas.retainAll(aptasFuncionais);
			aptas.retainAll(aptasAlcada);
			if(aptas.size() == 0){
				aptas.addAll(aptasPessoais);
				aptas.addAll(aptasFuncionais);
				aptas.addAll(aptasAlcada);
			}
		}
		return new ArrayList<Competencia>(aptas);
	}
	
	private void carregaCompetenciasPessoais(ProcessoTrf processo, List<Competencia> competencias, List<Competencia> necessarias, List<Competencia> naoAtendidas, Jurisdicao jurisdicao){
		if(necessarias == null || naoAtendidas == null){
			throw new IllegalArgumentException("As listas de competências necessárias e não atendidas devem ser passadas para o método já inicializadas");
		}
		necessarias.clear();
		naoAtendidas.clear();
		Set<Competencia> competenciasAptas = new HashSet<Competencia>();
		Set<Competencia> competenciasInaptas = new HashSet<Competencia>();
		
		List<DimensaoPessoal> dimensoesExclusivas = dimensaoPessoalManager.getDimensoesPessoais(processo, competencias, jurisdicao);
		if(dimensoesExclusivas.size() == 0) {
			return;
		}
		List<Pessoa> autores = processo.getPessoaPoloAtivoList();
		List<Pessoa> reus = processo.getPessoaPoloPassivoList();
		List<TipoPessoa> tiposAutores = processo.getTipoPessoaPoloAtivoList();
		List<TipoPessoa> tiposReus = processo.getTipoPessoaPoloPassivoList();
		List<PessoaAutoridade> autoridadesAtivo = processo.getAutoridadesPoloAtivo();
		List<PessoaAutoridade> autoridadesPassivo = processo.getAutoridadesPoloPassivo();

		if(autoridadesAtivo.size() > 0 || autoridadesPassivo.size() > 0){
			for(PessoaAutoridade aut: autoridadesAtivo){
				PessoaJuridica ov = aut.getOrgaoVinculacao();
				if(ov != null) {
					if(!autores.contains(ov)){
						autores.add(ov);
					}
					if(!tiposAutores.contains(ov.getTipoPessoa())){
						tiposAutores.add(ov.getTipoPessoa());
					}
				}
			}
			for(PessoaAutoridade aut: autoridadesPassivo){
				PessoaJuridica ov = aut.getOrgaoVinculacao();
				if(ov != null) {
					if(!reus.contains(ov)){
						reus.add(ov);
					}
					if(!tiposReus.contains(ov.getTipoPessoa())){
						tiposReus.add(ov.getTipoPessoa());
					}
				}
			}
		}
		
		for (DimensaoPessoal dp : dimensoesExclusivas) {
			int total = 0;
			int parcial = 0;
			for (DimensaoPessoalPessoa dpp : dp.getPessoasAfetadasList()) {
				total++;
				boolean autoresContem = autores.contains(dpp.getPessoa());
				boolean reusContem = reus.contains(dpp.getPessoa());
				if (((autoresContem && dpp.getTipoAssociacao() == AssociacaoDimensaoPessoalEnum.A) || (!autoresContem && dpp.getTipoAssociacao() == AssociacaoDimensaoPessoalEnum.E))
						&& (dpp.getPolo() == ProcessoParteParticipacaoEnum.A || dpp.getPolo() == ProcessoParteParticipacaoEnum.T)) {
					parcial++;
				}
				if(((reusContem && dpp.getTipoAssociacao() == AssociacaoDimensaoPessoalEnum.A) || (!reusContem && dpp.getTipoAssociacao() == AssociacaoDimensaoPessoalEnum.E)) 
						&& (dpp.getPolo() == ProcessoParteParticipacaoEnum.P || dpp.getPolo() == ProcessoParteParticipacaoEnum.T)) {
					parcial++;
				}
			}
			if(total != parcial) {
				competenciasInaptas.addAll(dp.getCompetencias());
				continue;
			}
			for (DimensaoPessoalTipoPessoa dpt : dp.getTiposDePessoasAfetadosList()) {
				total++;
				TipoPessoa tp = dpt.getTipoPessoa();
				boolean tiposAutoresContem = tiposAutores.contains(tp);
				boolean tiposReusContem = tiposReus.contains(tp);
				boolean exige = dpt.getTipoAssociacao() == AssociacaoDimensaoPessoalEnum.A;
				boolean proibe = dpt.getTipoAssociacao() == AssociacaoDimensaoPessoalEnum.E;
				boolean ativo = (dpt.getPolo() == ProcessoParteParticipacaoEnum.A || dpt.getPolo() == ProcessoParteParticipacaoEnum.T);
				boolean passivo = (dpt.getPolo() == ProcessoParteParticipacaoEnum.P || dpt.getPolo() == ProcessoParteParticipacaoEnum.T); 
				if (ativo && ((tiposAutoresContem && exige) || (!tiposAutoresContem && proibe))) {
					parcial++;
				}else if(ativo && exige){
					for(TipoPessoa tpAut: tiposAutores){
						if(tpAut.isChildrenOf(tp)){
							parcial++;
							break;
						}
					}
				}else if(ativo && proibe){
					boolean contem = false;
					for(TipoPessoa tpAut: tiposAutores){
						if(tpAut.isChildrenOf(tp)){
							contem = true;
							break;
						}
					}
					if(!contem){
						parcial++;
					}
				}
				if(passivo && ((tiposReusContem && exige) || (!tiposReusContem && proibe))) {
						parcial++;
				}else if(passivo && exige){
					for(TipoPessoa tpReu: tiposReus){
						if(tpReu.isChildrenOf(tp)){
							parcial++;
							break;
						}
					}
				}else if(passivo && proibe){
					boolean contem = false;
					for(TipoPessoa tpReu: tiposReus){
						if(tpReu.isChildrenOf(tp)){
							contem = true;
							break;
						}
					}
					if(!contem){
						parcial++;
					}
				}
			}
			if(total > 0 && total == parcial) {
				competenciasInaptas.removeAll(dp.getCompetencias());
				competenciasAptas.addAll(dp.getCompetencias());
			} else {
				competenciasInaptas.addAll(dp.getCompetencias());
			}
		}
		competenciasInaptas.removeAll(competenciasAptas);
		necessarias.addAll(competenciasAptas);
		naoAtendidas.addAll(competenciasInaptas);
	}
	
	
	private void carregaCompetenciasFuncionais(ProcessoTrf processo, List<Competencia> competencias, List<Competencia> necessarias, List<Competencia> naoAtendidas, Jurisdicao jurisdicao) {
		if(necessarias == null || naoAtendidas == null){
			throw new IllegalArgumentException("As listas de competências necessárias e não atendidas devem ser passadas para o método já inicializadas");
		}
		necessarias.clear();
		naoAtendidas.clear();
		Set<Competencia> competenciasAptas = new HashSet<Competencia>();
		Set<Competencia> competenciasInaptas = new HashSet<Competencia>();
		
		List<DimensaoFuncional> dimensoesExclusivas = dimensaoFuncionalManager.getDimensoesFuncionais(processo, competencias, jurisdicao);
		if(dimensoesExclusivas.size() == 0) {
			return;
		}
		List<PessoaAutoridade> autores = processo.getAutoridadesPoloAtivo();
		List<PessoaAutoridade> reus = processo.getAutoridadesPoloPassivo();
		
		for (DimensaoFuncional df : dimensoesExclusivas) {
			int total = 0;
			int parcial = 0;
			for (AutoridadeAfetada aa : df.getAutoridadesAfetadas()) {
				total++;
				boolean autoresContem = autores.contains(aa.getAutoridade());
				boolean reusContem = reus.contains(aa.getAutoridade());
				if (((autoresContem && aa.getTipoRestricao() == AssociacaoDimensaoPessoalEnum.A) || (!autoresContem && aa.getTipoRestricao() == AssociacaoDimensaoPessoalEnum.E))
						&& (aa.getPolo() == ProcessoParteParticipacaoEnum.A || aa.getPolo() == ProcessoParteParticipacaoEnum.T)) {
					parcial++;
				}
				if(((reusContem && aa.getTipoRestricao() == AssociacaoDimensaoPessoalEnum.A) || (!reusContem && aa.getTipoRestricao() == AssociacaoDimensaoPessoalEnum.E)) 
						&& (aa.getPolo() == ProcessoParteParticipacaoEnum.P || aa.getPolo() == ProcessoParteParticipacaoEnum.T)) {
					parcial++;
				}
			}
			if(total > 0 && total == parcial) {
				competenciasInaptas.removeAll(df.getCompetencias());
				competenciasAptas.addAll(df.getCompetencias());
			} else {
				competenciasInaptas.addAll(df.getCompetencias());
			}
		}
		competenciasInaptas.removeAll(competenciasAptas);
		necessarias.addAll(competenciasAptas);
		naoAtendidas.addAll(competenciasInaptas);
	}
	
	/**
	 * Carrega nas listas de competências necessárias e não atendidas aquelas competências, dentre as fornecidas, cuja 
	 * dimensão de alçada existente está ou não está preenchida pelo processo dado.
	 * 
	 * @param processo o processo paradigma que será utilizado para comparação.
	 * @param competencias a lista de competências que será objeto de avaliação
	 * @param necessarias lista vazia que será preenchida com as competências contidas no parâmetro competencias 
	 * cujas dimensões de alçada comportam o processo dado. Caso a lista não esteja vazia, ela será esvaziada.
	 * @param naoAtendidas lista vazia que será preenchida com as competências contidas no parâmetro competencias 
	 * cujas dimensões de alçada impedem a subsunção do processo dado. Caso a lista não esteja vazia, ela será esvaziada.
	 */
	private void carregaCompetenciasAlcadas(ProcessoTrf processo, List<Competencia> competencias, List<Competencia> necessarias, List<Competencia> naoAtendidas){
		if(necessarias == null || naoAtendidas == null){
			throw new IllegalArgumentException("As listas de competências necessárias e não atendidas devem ser passadas para o método já inicializadas");
		}
		
		boolean alteradoValorDaCausa = false;
		
		if(processo.getValorCausa() == null){
			processo.setValorCausa(0.0d);
			alteradoValorDaCausa = true;
		}
		
		Double vc = processo.getValorCausa();
		for(Competencia c: competencias){
			DimensaoAlcada da = c.getDimensaoAlcada();
			if(da == null){
				continue;
			}
			switch (da.getTipoCompetencia()) {
			case C: // cível
				Double piso = da.getIntervaloInicial();
				Double teto = da.getIntervaloFinal();
				TipoIntervalo ti = da.getTipoIntervalo();
				if(ti == TipoIntervalo.T){
					logger.error("Houve um erro ao configurar a dimensão alçada da competência [{0}]. Foi utilizado o tipo de intervalo tempo em uma dimensão de alçada cível.", c.getCompetencia());
					throw new IllegalArgumentException("Houve um erro ao configurar a dimensão alçada da competência [" + c.getCompetencia() + "]. Foi utilizado o tipo de intervalo tempo em uma dimensão de alçada cível.");
				}
				if(teto != null){
					if((vc.compareTo(piso) >= 0 && vc.compareTo(teto) <= 0)){
						necessarias.add(c);
					}else{
						naoAtendidas.add(c);
					}
				}else{
					if(vc.compareTo(piso) >= 0){
						necessarias.add(c);
					}else{
						naoAtendidas.add(c);
					}
				}
				break;
			case R: // criminal
				logger.warn("O tratamento de alçada criminal ainda não foi implementado.");
				break;
			default:
				logger.error("Foi adotado um tipo de competência de alçada inválido [{0}].", da.getTipoCompetencia().name());
				throw new IllegalArgumentException("Foi adotado um tipo de competência de alçada inválido.");
			}
		}
		
		if(alteradoValorDaCausa && new ParametroJtUtil().justicaTrabalho()) {
			processo.setValorCausa(null);
		}
	}
	
	
}
