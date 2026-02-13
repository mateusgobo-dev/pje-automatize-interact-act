package br.jus.cnj.pje.servicos;

import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.DefinicaoCompetenciaService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.util.CustomJbpmTransactional;
import br.jus.cnj.pje.util.CustomJbpmTransactionalClass;
import br.jus.csjt.pje.view.action.ProcessoJTHome;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

@Name(AutuacaoService.NAME)
@Scope(ScopeType.EVENT)
@CustomJbpmTransactionalClass
public class AutuacaoService {

	public final static String NAME = "autuacaoService";
	
	@In
	private DefinicaoCompetenciaService definicaoCompetenciaService;
	
	@In
	private ParametroService parametroService;
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	
	@In
	private ProcessoJudicialService processoJudicialService;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	

	public static AutuacaoService instance() {
		return ComponentUtil.getComponent(AutuacaoService.NAME);
	}

	public void autuarProcesso(ProcessoTrf processoTrf) throws Exception {
		try {
			switch (processoTrf.getProcessoStatus()) {
			case D: // distribuído
				throw new IllegalStateException("Processo consta como já distribuído."); 
			case V: // já foi validado em chamada anterior
				break;
			default: // não validado
				ProcessoTrfHome.instance().validarAutuacao(processoTrf);
				break;
			}
			
			processoTrf.setProcessoStatus(ProcessoStatusEnum.V);
			if(!ProcessoTrfHome.instance().isCadastroProcessoClet()){
				processoTrf.setDataAutuacao(new Date());
			}
		} catch (IllegalStateException e) { 
			throw e;
		} catch (Exception e) {
			processoTrf.setProcessoStatus(ProcessoStatusEnum.E);
			processoTrf.setDataAutuacao(null);
			throw e;
		}
	}
	
	public void validarAutuacao(ProcessoTrf processo){
		if(processo.getProcessoStatus() == ProcessoStatusEnum.V){
			return;
		}
		// Verifica se há ao menos um documento inicial juntado.
		
		TipoProcessoDocumento inicial = processo.getClasseJudicial().getTipoProcessoDocumentoInicial();
		
		try {
			int documentosIniciais = documentoJudicialService.contagemDocumentos(processo, true, false, inicial);
			if(documentosIniciais == 0){
				
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		// Verificar se todos os documentos juntados estão assinados
		// assegura o respeito aos critérios relevantes para a classe do processo
		// assegura o respeito aos critérios relevantes de assuntos processuais
		// assegura o respeito à necessidade de definição do valor da causa
	}
	
	public void numerarProcesso(ProcessoTrf processoJudicial) throws Exception {
		// Usar o número do processo informado no caso de a classe judicial permitir a numeração manual.
		if(processoJudicial.getProcesso().getNumeroProcessoTemp() != null && processoJudicial.getClasseJudicial().getPermiteNumeracaoManual()) {
			NumeroProcessoUtil.numerarProcesso(processoJudicial);
		} else {
			String tipoJustica = parametroService.valueOf("tipoJustica");
			Integer numeroOrigem = null;
			
			if(ParametroUtil.instance().isPrimeiroGrau() && (tipoJustica.equals("JE") || tipoJustica.equals("JME") || tipoJustica.equals("JMU"))){
				String codigoOrigem = processoJudicial.getOrgaoJulgador().getCodigoOrigem();
				if(codigoOrigem == null || codigoOrigem.trim().isEmpty()){
					throw new PJeBusinessException("pje.autuacaoService.numerarProcesso.error.codigoOrgaoJulgadorNaoDefinido", null, processoJudicial.getOrgaoJulgador().getOrgaoJulgador());
				}
				try{
					numeroOrigem = Integer.parseInt(codigoOrigem.trim());
				}catch (NumberFormatException e) {
					throw new PJeBusinessException("pje.autuacaoService.numerarProcesso.error.codigoOrgaoJulgadorNaoNumerico", null, processoJudicial.getOrgaoJulgador().getCodigoOrigem());
				}
			}else{
				numeroOrigem = processoJudicial.getJurisdicao().getNumeroOrigem();

				if(numeroOrigem == null){
					throw new PJeBusinessException("pje.autuacaoService.numerarProcesso.error.codigoJurisdicaoNaoDefinido");
				}
			}
			Integer numeroOrgaoJustica = processoJudicial.getJurisdicao().getNumeroOrgaoJustica();
			if(numeroOrgaoJustica == null) {
				try {
					numeroOrgaoJustica = Integer.parseInt(parametroService.valueOf("numeroOrgaoJustica"));
				} catch (NumberFormatException e) {
					throw new PJeBusinessException("pje.autuacaoService.numerarProcesso.error.numeroOrgaoJusticaNaoNumerico", null, parametroService.valueOf("numeroOrgaoJustica"));
				}
			}
			NumeroProcessoUtil.numerarProcesso(processoJudicial, numeroOrgaoJustica, numeroOrigem);
		}
	}
	
	public List<Competencia> recuperaCompetenciasPossiveis(ProcessoTrf proc){
		return definicaoCompetenciaService.getCompetencias(proc);
	}

	public List<Competencia> recuperaCompetenciasPossiveis(ProcessoTrf processo, Jurisdicao jurisdicao){
		return definicaoCompetenciaService.getCompetencias(processo, jurisdicao);
	}
	
	@CustomJbpmTransactional
	public void autuarProcessoEmLote(ProcessoTrf processoTrf) throws Exception{
		//limpa instancia do ProcessoJT
		ProcessoJTHome.instance().limpaProcessoJT();
		ProcessoTrfHome.instance().setInstance(null);
		ProcessoTrfHome.instance().setId(processoTrf.getIdProcessoTrf());
		Contexts.removeFromAllContexts("pje:fluxo:variables:startState");
		/*
		* PJE-JT: Valério Wittler: [PJEII-506] Correção de problema que impedia o protocolo em lote, pois não eram obtidas competências. O método
		* abaixo (ProcessoTrfHome.acoesAntesDeProtocolarProcesso()) foi extraído das ações que já faziam parte do protocolo individual. Até o
		* momento o que ele faz é obter as competências e verificar se no caso de marcado segredo de justiça foi também marcado o motivo.
		*/
		/*
		* TODO: Talvez seja o caso de inserir a chamada ProcessoTrfHome.acoesAntesDeProtocolarProcesso() dentro do método
		*/
		ProcessoTrfHome.instance().acoesAntesDeProtocolarProcesso();
		PrevencaoService.instance().limparService();
		if (ProcessoTrfHome.instance().validar()) {
			/* 
			* Verifica se ocorreu algum problema na protocolação/autuação.
			* Caso tenha ocorrido adiciona como inconsistência para o processo em questão na lista apropriada
			*/
			String mensagemProcotocolacao = ProcessoTrfHome.instance().getMensagemErroProtocolacao();
			if (!mensagemProcotocolacao.isEmpty()){
				throw new Exception(mensagemProcotocolacao);
			}
			
		}else{//Caso validar()==false 
			String mensagemProcotocolacao = ProcessoTrfHome.instance().getMensagemErroProtocolacao();
			throw new Exception(mensagemProcotocolacao);
		}
	}
}
