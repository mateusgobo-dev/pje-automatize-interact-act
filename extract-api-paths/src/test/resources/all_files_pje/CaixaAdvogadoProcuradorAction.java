package br.com.infox.cliente.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.entidades.vo.PesquisaExpedientesVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.TipoSituacaoExpedienteEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name("caixaAdvogadoProcuradorAction")
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
public class CaixaAdvogadoProcuradorAction implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public List<ProcessoTrf> processosCorrespondentesFiltroCaixa(CaixaAdvogadoProcurador caixa) throws PJeBusinessException{
		ConsultaProcessoVO criteriosPesquisa = this.getCriteriosPesquisaProcessos(caixa);
		List<ProcessoTrf> listaProcessosParaDistribuir = new ArrayList<ProcessoTrf>();
		if(criteriosPesquisa != null && caixa.getJurisdicao() != null) {
			ProcessoJudicialManager processoJudicialManager = ProcessoJudicialManager.instance();
			listaProcessosParaDistribuir = processoJudicialManager.getProcessosJurisdicao(caixa.getJurisdicao().getIdJurisdicao(), criteriosPesquisa, null);
		}
		return listaProcessosParaDistribuir;
	}

	public List<ProcessoParteExpediente> processoParteExpedientesCorrespondentesFiltroCaixa(TipoSituacaoExpedienteEnum tipoSituacaoExpediente, CaixaAdvogadoProcurador caixa) throws PJeBusinessException{
		PesquisaExpedientesVO criteriosPesquisa = this.getCriteriosPesquisaExpedientes(tipoSituacaoExpediente, caixa);
		List<ProcessoParteExpediente> listaProcessoParteExpedienteParaDistribuir = new ArrayList<ProcessoParteExpediente>();
		if(criteriosPesquisa != null && caixa.getJurisdicao() != null) {
			ProcessoParteExpedienteManager processoParteExpedienteManager = ProcessoParteExpedienteManager.instance();

			listaProcessoParteExpedienteParaDistribuir = processoParteExpedienteManager.getExpedientesJurisdicao(caixa.getJurisdicao().getIdJurisdicao(), criteriosPesquisa, null);
		}
		return listaProcessoParteExpedienteParaDistribuir;
	}

	/**
	 * Retorna os critérios de pesquisa de processos dada uma caixa com campos de pesquisa - critérios para serem aplicados aos processos que não estão em caixas
	 * @param caixa
	 * @return
	 */
	public ConsultaProcessoVO getCriteriosPesquisaProcessos(CaixaAdvogadoProcurador caixa) {
		ConsultaProcessoVO criteriosPesquisa = null;
		if(possuiFiltrosAcervo(caixa)) {
			criteriosPesquisa = new ConsultaProcessoVO();
			criteriosPesquisa = filtroNumeroProcesso(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroDataDistribuicao(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroPrioridadeProcesso(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroClasseJudicial(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroAssuntoProcesso(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroProcessoParte(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroIntervaloNumeroProcesso(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroOrgaoJulgador(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroOrgaoJulgadorColegiado(caixa, criteriosPesquisa);
			
			criteriosPesquisa.setApenasSemCaixa(true);
		}
		
		return criteriosPesquisa;
	}
	
	/**
	 * Retorna os critérios de pesquisa de expedientes dada uma caixa com campos de pesquisa - critérios para serem aplicados aos expedientes que não estão em caixas
	 * @param caixa
	 * @return
	 */
	public PesquisaExpedientesVO getCriteriosPesquisaExpedientes(TipoSituacaoExpedienteEnum tipoSituacaoExpediente, CaixaAdvogadoProcurador caixa) {
		PesquisaExpedientesVO criteriosPesquisa = null;
		if(possuiFiltrosExpedientes(caixa) && caixa.getJurisdicao() != null) {
			criteriosPesquisa = new PesquisaExpedientesVO(tipoSituacaoExpediente, caixa.getJurisdicao().getIdJurisdicao());
			
			criteriosPesquisa = filtroNumeroProcesso(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroDataCriacaoExpediente(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroPrioridadeProcesso(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroClasseJudicial(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroAssuntoProcesso(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroProcessoParte(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroIntervaloNumeroProcesso(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroOrgaoJulgador(caixa, criteriosPesquisa);
			criteriosPesquisa = filtroOrgaoJulgadorColegiado(caixa, criteriosPesquisa);
			
			criteriosPesquisa.setApenasSemCaixa(true);
		}
		
		return criteriosPesquisa;
	}

	private ConsultaProcessoVO filtroNumeroProcesso(CaixaAdvogadoProcurador caixa, ConsultaProcessoVO criteriosPesquisa) {
		if(caixa.getNumeroSequencia() != null) {
			criteriosPesquisa.setNumeroSequencia(caixa.getNumeroSequencia());
		}
		if(caixa.getNumeroDigitoVerificador() != null) {
			criteriosPesquisa.setDigitoVerificador(caixa.getNumeroDigitoVerificador());
		}
		if(caixa.getAno() != null) {
			criteriosPesquisa.setNumeroAno(caixa.getAno());
		}
		if(caixa.getNumeroOrigemProcesso() != null) {
			criteriosPesquisa.setNumeroOrigem(caixa.getNumeroOrigemProcesso());
		}
		
		return criteriosPesquisa;
	}

	private PesquisaExpedientesVO filtroNumeroProcesso(CaixaAdvogadoProcurador caixa, PesquisaExpedientesVO criteriosPesquisa) {
		if(caixa.getNumeroSequencia() != null) {
			criteriosPesquisa.setNumeroSequencia(caixa.getNumeroSequencia());
		}
		if(caixa.getNumeroDigitoVerificador() != null) {
			criteriosPesquisa.setDigitoVerificador(caixa.getNumeroDigitoVerificador());
		}
		if(caixa.getAno() != null) {
			criteriosPesquisa.setNumeroAno(caixa.getAno());
		}
		if(caixa.getNumeroOrigemProcesso() != null) {
			criteriosPesquisa.setNumeroOrigem(caixa.getNumeroOrigemProcesso());
		}
		
		return criteriosPesquisa;
	}	

	/**
	 * Filtro por {@link OrgaoJulgador}
	 * @param processo
	 * 			Processo a ser inserido na caixa
	 * @param caixa
	 * 			Caixa a receber processo
	 * @param results
	 * 			true se o filtro se aplica ao processo
	 */
	private ConsultaProcessoVO filtroOrgaoJulgador(CaixaAdvogadoProcurador caixa, ConsultaProcessoVO criteriosPesquisa) {
		if(caixa.getOrgaoJulgador() != null){
			criteriosPesquisa.setOrgaoJulgadorObj(caixa.getOrgaoJulgador());
		}
		return criteriosPesquisa;
	}

	private PesquisaExpedientesVO filtroOrgaoJulgador(CaixaAdvogadoProcurador caixa, PesquisaExpedientesVO criteriosPesquisa) {
		if(caixa.getOrgaoJulgador() != null){
			criteriosPesquisa.setOrgaoJulgadorObj(caixa.getOrgaoJulgador());
		}
		return criteriosPesquisa;
	}
	/**
	 * Filtro pro {@link OrgaoJulgadorColegiado}
	 * @param processo
	 * 			Processo a ser inserido na caixa
	 * @param caixa
	 * 			Caixa a receber processo
	 * @param results
	 * 			true se o filtro se aplica ao processo
	 */
	private ConsultaProcessoVO filtroOrgaoJulgadorColegiado(CaixaAdvogadoProcurador caixa, ConsultaProcessoVO criteriosPesquisa) {
		if(caixa.getOrgaoJulgadorColegiado() != null){
			criteriosPesquisa.setOrgaoJulgadorColegiadoObj(caixa.getOrgaoJulgadorColegiado());
		}
		return criteriosPesquisa;
	}

	private PesquisaExpedientesVO filtroOrgaoJulgadorColegiado(CaixaAdvogadoProcurador caixa, PesquisaExpedientesVO criteriosPesquisa) {
		if(caixa.getOrgaoJulgadorColegiado() != null){
			criteriosPesquisa.setOrgaoJulgadorColegiadoObj(caixa.getOrgaoJulgadorColegiado());
		}
		return criteriosPesquisa;
	}

	private ConsultaProcessoVO filtroDataDistribuicao(CaixaAdvogadoProcurador caixa, ConsultaProcessoVO criteriosPesquisa) {
		if(caixa.getDataDistribuicaoInicial() != null) {
			criteriosPesquisa.setDataAutuacaoInicial(caixa.getDataDistribuicaoInicial());
		}
		if(caixa.getDataDistribuicaoFinal() != null) {
			criteriosPesquisa.setDataAutuacaoFinal(caixa.getDataDistribuicaoFinal());
		}
		
		return criteriosPesquisa;
	}
	
	private PesquisaExpedientesVO filtroDataCriacaoExpediente(CaixaAdvogadoProcurador caixa, PesquisaExpedientesVO criteriosPesquisa) {
		if(caixa.getDataCriacaoExpedienteInicial() != null) {
			criteriosPesquisa.setDataCriacaoExpedienteInicial(caixa.getDataCriacaoExpedienteInicial());
		}
		if(caixa.getDataCriacaoExpedienteFinal() != null) {
			criteriosPesquisa.setDataCriacaoExpedienteFinal(caixa.getDataCriacaoExpedienteFinal());
		}
		
		return criteriosPesquisa;
	}
	
	private ConsultaProcessoVO filtroPrioridadeProcesso(CaixaAdvogadoProcurador caixa, ConsultaProcessoVO criteriosPesquisa) {
		if(caixa.getPrioridadeProcesso() != null && caixa.getPrioridadeProcesso().getPrioridade() != null) {
			criteriosPesquisa.setPrioridadeObj(caixa.getPrioridadeProcesso());
		}
		return criteriosPesquisa;
	}

	private PesquisaExpedientesVO filtroPrioridadeProcesso(CaixaAdvogadoProcurador caixa, PesquisaExpedientesVO criteriosPesquisa) {
		if(caixa.getPrioridadeProcesso() != null && caixa.getPrioridadeProcesso().getPrioridade() != null) {
			criteriosPesquisa.setPrioridadeObj(caixa.getPrioridadeProcesso());
		}
		return criteriosPesquisa;
	}
	
	private ConsultaProcessoVO filtroClasseJudicial(CaixaAdvogadoProcurador caixa, ConsultaProcessoVO criteriosPesquisa) {
		if(caixa.getClasseJudicialList() != null && caixa.getClasseJudicialList().size() > 0) {
			criteriosPesquisa.setClasseJudicialList(caixa.getClasseJudicialList());
		}
		return criteriosPesquisa;
	}
	
	private PesquisaExpedientesVO filtroClasseJudicial(CaixaAdvogadoProcurador caixa, PesquisaExpedientesVO criteriosPesquisa) {
		if(caixa.getClasseJudicialList() != null && caixa.getClasseJudicialList().size() > 0) {
			criteriosPesquisa.setClasseJudicialList(caixa.getClasseJudicialList());
		}
		return criteriosPesquisa;
	}
	
	private ConsultaProcessoVO filtroAssuntoProcesso(CaixaAdvogadoProcurador caixa, ConsultaProcessoVO criteriosPesquisa) {
		if(caixa.getAssuntoTrfList() != null && caixa.getAssuntoTrfList().size() > 0) {
			criteriosPesquisa.setAssuntoTrfList(caixa.getAssuntoTrfList());
		}
		return criteriosPesquisa;
	}
	
	private PesquisaExpedientesVO filtroAssuntoProcesso(CaixaAdvogadoProcurador caixa, PesquisaExpedientesVO criteriosPesquisa) {
		if(caixa.getAssuntoTrfList() != null && caixa.getAssuntoTrfList().size() > 0) {
			criteriosPesquisa.setAssuntoTrfList(caixa.getAssuntoTrfList());
		}
		return criteriosPesquisa;
	}
	
	private ConsultaProcessoVO filtroProcessoParte(CaixaAdvogadoProcurador caixa, ConsultaProcessoVO criteriosPesquisa) {
		if(possuiFiltrosDadosPartes(caixa)) {
			criteriosPesquisa = filtroPessoa(caixa, criteriosPesquisa);
		}
		if(possuiFiltrosDadosOAB(caixa)) {
			criteriosPesquisa = filtroPessoaAdvogado(caixa, criteriosPesquisa);
		}
		
		return criteriosPesquisa;
	}

	private PesquisaExpedientesVO filtroProcessoParte(CaixaAdvogadoProcurador caixa, PesquisaExpedientesVO criteriosPesquisa) {
		if(possuiFiltrosDadosPartes(caixa)) {
			criteriosPesquisa = filtroPessoa(caixa, criteriosPesquisa);
		}
		if(possuiFiltrosDadosOAB(caixa)) {
			criteriosPesquisa = filtroPessoaAdvogado(caixa, criteriosPesquisa);
		}
		
		return criteriosPesquisa;
	}
	
	private ConsultaProcessoVO filtroPessoa(CaixaAdvogadoProcurador caixa, ConsultaProcessoVO criteriosPesquisa) {
		if(StringUtil.isNotEmpty(caixa.getNomeParte())) {
			criteriosPesquisa.setNomeParte(caixa.getNomeParte());
		}
		if(StringUtil.isNotEmpty(caixa.getNumeroCpfCnpjParte())) {
			criteriosPesquisa.setDocumentoIdentificacaoParte(caixa.getNumeroCpfCnpjParte());
		}
		if(caixa.getNascimentoInicialParte() != null) {
			criteriosPesquisa.setDataNascimentoInicial(caixa.getNascimentoInicialParte());
		}
		if(caixa.getNascimentoFinalParte() != null) {
			criteriosPesquisa.setDataNascimentoFinal(caixa.getNascimentoFinalParte());
		}		
		
		return criteriosPesquisa;
	}

	private ConsultaProcessoVO filtroPessoaAdvogado(CaixaAdvogadoProcurador caixa, ConsultaProcessoVO criteriosPesquisa) {
		String oabRepresentanteParte = "";
		if(caixa.getUfOABParte() != null) {
			oabRepresentanteParte.concat(caixa.getUfOABParte().getCodEstado());
		}
		if(StringUtil.isNotEmpty(caixa.getNumeroOABParte())) {
			oabRepresentanteParte.concat(caixa.getNumeroOABParte());
		}
		if(caixa.getLetraOABParte() != null && StringUtil.isNotEmpty(caixa.getLetraOABParte().trim())) {
			oabRepresentanteParte.concat(caixa.getLetraOABParte().trim());
		}
		
		if(!oabRepresentanteParte.isEmpty()) {
			criteriosPesquisa.setOabRepresentanteParte(oabRepresentanteParte);
		}
		
		return criteriosPesquisa;
	}
	
	private PesquisaExpedientesVO filtroPessoa(CaixaAdvogadoProcurador caixa, PesquisaExpedientesVO criteriosPesquisa) {
		if(StringUtil.isNotEmpty(caixa.getNomeParte())) {
			criteriosPesquisa.setNomeParte(caixa.getNomeParte());
		}
		if(StringUtil.isNotEmpty(caixa.getNumeroCpfCnpjParte())) {
			criteriosPesquisa.setDocumentoIdentificacaoDestinatario(caixa.getNumeroCpfCnpjParte());
		}
		if(caixa.getNascimentoInicialParte() != null) {
			criteriosPesquisa.setDataNascimentoInicial(caixa.getNascimentoInicialParte());
		}
		if(caixa.getNascimentoFinalParte() != null) {
			criteriosPesquisa.setDataNascimentoFinal(caixa.getNascimentoFinalParte());
		}		
		
		return criteriosPesquisa;
	}

	private PesquisaExpedientesVO filtroPessoaAdvogado(CaixaAdvogadoProcurador caixa, PesquisaExpedientesVO criteriosPesquisa) {
		String oabRepresentanteDestinatario = "";
		if(caixa.getUfOABParte() != null) {
			oabRepresentanteDestinatario.concat(caixa.getUfOABParte().getCodEstado());
		}
		if(StringUtil.isNotEmpty(caixa.getNumeroOABParte())) {
			oabRepresentanteDestinatario.concat(caixa.getNumeroOABParte());
		}
		if(caixa.getLetraOABParte() != null && StringUtil.isNotEmpty(caixa.getLetraOABParte().trim())) {
			oabRepresentanteDestinatario.concat(caixa.getLetraOABParte().trim());
		}
		
		if(!oabRepresentanteDestinatario.isEmpty()) {
			criteriosPesquisa.setOabRepresentanteDestinatario(oabRepresentanteDestinatario);
		}
		
		return criteriosPesquisa;
	}

	private ConsultaProcessoVO filtroIntervaloNumeroProcesso(CaixaAdvogadoProcurador caixa, ConsultaProcessoVO criteriosPesquisa) {
		if(StringUtil.isNotEmpty(caixa.getIntervaloNumeroProcesso())) {
			criteriosPesquisa.setNumeroSequenciaProcessoPattern(caixa.getIntervaloNumeroProcesso());
		}
		
		return criteriosPesquisa;
	}
	
	private PesquisaExpedientesVO filtroIntervaloNumeroProcesso(CaixaAdvogadoProcurador caixa, PesquisaExpedientesVO criteriosPesquisa) {
		if(StringUtil.isNotEmpty(caixa.getIntervaloNumeroProcesso())) {
			criteriosPesquisa.setNumeroSequenciaProcessoPattern(caixa.getIntervaloNumeroProcesso());
		}
		
		return criteriosPesquisa;
	}
	
	/**
	 * Verifica se a caixa possui pelo menos um filtro configurado.
	 * @param caixa - caixa a ser verificada.
	 * @return true - caso a mesma possua pelo menos um filtro configurado.
	 */
	private boolean possuiFiltrosAcervo(CaixaAdvogadoProcurador caixa) {
		return possuiFiltrosExclusivoAcervo(caixa)
				|| possuiFiltrosGeral(caixa);
	}

	/**
	 * Verifica se a caixa possui pelo menos um filtro configurado.
	 * @param caixa - caixa a ser verificada.
	 * @return true - caso a mesma possua pelo menos um filtro configurado.
	 */
	private boolean possuiFiltrosExpedientes(CaixaAdvogadoProcurador caixa) {
		return possuiFiltrosExclusivoExpediente(caixa)
				|| possuiFiltrosGeral(caixa);
	}
	
	private boolean possuiFiltrosGeral(CaixaAdvogadoProcurador caixa) {
		return possuiFiltrosDadosProcessuais(caixa) 
				|| possuiFiltrosDadosPartes(caixa)
				|| possuiFiltrosDadosOAB(caixa);	
	}

	/**
	 * Verifica se pelo menos um campo de dados processuais estar configurado no filtro da caixa.
	 * @param caixa - caixa a ser verificada.
	 * @return true - caso a mesma possua pelo menos um campo de dados processuais do filtro configurado.
	 */
	private boolean possuiFiltrosDadosProcessuais(CaixaAdvogadoProcurador caixa) {
		return caixa.getNumeroSequencia() != null || caixa.getNumeroDigitoVerificador() != null
				|| caixa.getAno() != null || caixa.getNumeroOrigemProcesso() != null
				|| caixa.getPrioridadeProcesso() != null
				|| (caixa.getAssuntoTrfList() != null && !caixa.getAssuntoTrfList().isEmpty()) 
				|| (caixa.getClasseJudicialList() != null && !caixa.getClasseJudicialList().isEmpty())
				|| caixa.getOrgaoJulgador() != null
				|| caixa.getOrgaoJulgadorColegiado() != null
				|| StringUtil.isNotEmpty(caixa.getIntervaloNumeroProcesso());
	}
	
	private boolean possuiFiltrosExclusivoAcervo(CaixaAdvogadoProcurador caixa) {
		return (caixa.getDataDistribuicaoInicial() != null || caixa.getDataDistribuicaoFinal() != null);
	}
	
	private boolean possuiFiltrosExclusivoExpediente(CaixaAdvogadoProcurador caixa) {
		return (caixa.getDataCriacaoExpedienteInicial() != null || caixa.getDataCriacaoExpedienteFinal() != null);
	}
	
	/**
	 * Verifica se pelo menos um campo de dados das partes estar configurado no filtro da caixa.
	 * @param caixa - caixa a ser verificada.
	 * @return true - caso a mesma possua pelo menos um campo de dados das partes do filtro configurado.
	 */
	private boolean possuiFiltrosDadosPartes(CaixaAdvogadoProcurador caixa) {
		return StringUtil.isNotEmpty(caixa.getNomeParte()) 
				|| StringUtil.isNotEmpty(caixa.getNumeroCpfCnpjParte())
				|| caixa.getNascimentoInicialParte() != null
				|| caixa.getNascimentoFinalParte() != null;
	}
	
	/**
	 * Verifica se pelo menos um campo de dados da OAB estar configurado no filtro da caixa.
	 * @param caixa - caixa a ser verificada.
	 * @return true - caso a mesma possua pelo menos um campo de dados da OAB do filtro configurado.
	 */
	private boolean possuiFiltrosDadosOAB(CaixaAdvogadoProcurador caixa) {
			return caixa.getUfOABParte() != null
				|| StringUtil.isNotEmpty(caixa.getNumeroOABParte())
				|| (caixa.getLetraOABParte() != null && StringUtil.isNotEmpty(caixa.getLetraOABParte().trim()));
	}	
}