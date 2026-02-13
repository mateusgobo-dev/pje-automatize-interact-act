package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcessoParteRepresentanteDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteHistorico;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(ProcessoParteRepresentanteManager.NAME)
public class ProcessoParteRepresentanteManager extends BaseManager<ProcessoParteRepresentante>{
	
	public static final String NAME = "processoParteRepresentanteManager";
	
	@In
	private ProcessoParteRepresentanteDAO processoParteRepresentanteDAO; 

	@Override
	protected ProcessoParteRepresentanteDAO getDAO() {
		return processoParteRepresentanteDAO;
	}
	
	public static ProcessoParteRepresentanteManager instance() {
		return ComponentUtil.getComponent(ProcessoParteRepresentanteManager.class);
	}
	
	/**
	 * Indica se uma dada pessoa é representante de outra em um dado processo.
	 * 
	 * @param processoJudicial o processo em relação ao qual se pretende obter a informação 
	 * @param representante a pessoa que se pretende identificar como representante
	 * @param representado a pessoa pretensamente representada
	 * @param tipoRepresentacao o tipo de representação que se pretende identificar
	 * @return true, se representado for representante do tipo dado no processo judicial
	 */
	public boolean isRepresentante(ProcessoTrf processoJudicial, Pessoa representante, Pessoa representado, TipoParte tipoRepresentacao) {
		Search s = new Search(ProcessoParteRepresentante.class);
		addCriteria(s, 
				Criteria.equals("inSituacao", ProcessoParteSituacaoEnum.A),
				Criteria.equals("processoParte.processoTrf", processoJudicial),
				Criteria.equals("representante", representante),
				Criteria.equals("processoParte.pessoa", representado),
				Criteria.equals("tipoRepresentante", tipoRepresentacao));
		s.setCount(true);
		return count(s) > 0 ? true : false;
	}

	/**
	 * Recupera a lista de representantes de uma pessoa em um dado processo judicial.
	 * 
	 * @param processoJudicial o processo em relação ao qual se pretende obter a informação
	 * @param representado a pessoa cujos advogados se pretende identificar
	 * @param tipoRepresentacao o tipo de representação que se pretende identificar
	 * @return a lista de representantes do tipo dado da pessoa no processo, ou uma lista vazia, se ela não tiver representantes desse tipo
	 */
	public List<Pessoa> recuperaRepresentantes(ProcessoTrf processoJudicial, Pessoa representado, TipoParte tipoRepresentacao) {
		Search s = new Search(ProcessoParteRepresentante.class);
		addCriteria(s, 
				Criteria.equals("inSituacao", ProcessoParteSituacaoEnum.A),
				Criteria.equals("processoParte.processoTrf", processoJudicial),
				Criteria.equals("tipoRepresentante", tipoRepresentacao),
				Criteria.equals("processoParte.pessoa", representado));
		s.setRetrieveField("representante");
		return list(s);
	}
	
	/**
	 * Consulta as pessoas que são representadas pelo representante passado por parâmetro.
	 * 
	 * @param representante Pessoa do representante.
	 * @param processo ProcessoTrf.
	 * @return lista de representados.
	 */
	public List<Pessoa> consultarRepresentados(Pessoa representante, ProcessoTrf processo) {
		return processoParteRepresentanteDAO.consultarRepresentados(representante, processo);
	}
	
	/**
	 * Método responsável por consultar as pessoas representadas pelo
	 * representante no polo em questão
	 * 
	 * @param representante
	 *            a pessoa do representante
	 * @param processo
	 *            o processo
	 * @param polo
	 *            o polo
	 * @return <code>List</code>, pessoas representadas pelo representante
	 */
	public List<Pessoa> consultarRepresentadosPeloPolo(Pessoa representante, ProcessoTrf processo, ProcessoParteParticipacaoEnum polo) {
		return processoParteRepresentanteDAO.consultarRepresentadosPeloPolo(representante, processo, polo);
	}

	/**
	 * Retorna um ProcessoParteRepresentante caso o representante represente a parte (representado) no processo, senão retorna nulo.
	 * 
	 * @param representante Pessoa do representante.
	 * @param representando Pessoa da parte representada.
	 * @param processo		ProcessoTrf
	 * @return ProcessoParteRepresentante
	 */
	public ProcessoParteRepresentante consultarProcessoParteRepresentante(Pessoa representante, Pessoa representado, ProcessoTrf processo) {
		return processoParteRepresentanteDAO.consultarProcessoParteRepresentante(representante, representado, processo);
	}
	
	/**
	 * Inativa o representante informado por parâmetro, caso o representante não represente outra parte no processo inativa o processoParte do representante.
	 *  
	 * @param processoParteRepresentante
	 * @param situacao
	 * @param justificativa
	 * @param usuarioLogado
	 */
	public void inativarRepresentante(ProcessoParteRepresentante processoParteRepresentante, 
										 ProcessoParteSituacaoEnum situacao, 
										 String justificativa, 
										 UsuarioLogin usuarioLogado ){
		
		boolean representaOutraParte = false;
		
		ProcessoParteHistorico processoParteHistorico = new ProcessoParteHistorico();
		
		processoParteHistorico.setInSituacao(situacao);
		processoParteHistorico.setJustificativa(justificativa);
		processoParteHistorico.setDataHistorico(new Date());
		processoParteHistorico.setProcessoParte(processoParteRepresentante.getParteRepresentante());
		processoParteHistorico.setUsuarioLogin(usuarioLogado);
		

		//Verificando se o advogado representa mais alguma parte no processo.
		for(ProcessoParteRepresentante representado : processoParteRepresentante.getParteRepresentante().getProcessoParteRepresentanteList2()){
			
			if(representado.getInSituacao() == ProcessoParteSituacaoEnum.A && representado.getProcessoParte() != processoParteRepresentante.getProcessoParte()){
				representaOutraParte = true;
				break;
			}				
		}
		
		if(!representaOutraParte){
			processoParteRepresentante.getParteRepresentante().setInSituacao(situacao);
		}
				
		processoParteRepresentante.setInSituacao(situacao);
		processoParteRepresentante.getParteRepresentante().getProcessoParteHistoricoList().add(processoParteHistorico);
	}

	public List<ProcessoParte> obtemProcessoParteRepresentantes(ProcessoParte processoparte,ProcessoTrf processo){
		return obtemProcessoParteRepresentantes(processoparte, processo.getIdProcessoTrf());
	}

	public List<ProcessoParte> obtemProcessoParteRepresentantes(ProcessoParte processoparte,Integer idProcesso){
		return processoParteRepresentanteDAO.obtemProcessoParteRepresentantes(processoparte,idProcesso);
	}
	
	public List<ProcessoParte> recuperarRepresentantesParaExibicao(Integer idProcessoParte, boolean somenteAtivas) {
		return processoParteRepresentanteDAO.recuperarRepresentantesParaExibicao(idProcessoParte, somenteAtivas);
	}

	/**
	 * Retorna true se a pessoa passada por parâmetro for representante de outras pessoas 
	 * no processo. O representante será removido da lista para a validação.
	 * 
	 * @param pessoa Pessoa que será validada.
	 * @param processo Processo pesquisado.
	 * @return Boleano
	 */
	public Boolean isPessoaPossuiRepresentadosNoProcesso(Pessoa pessoa, ProcessoTrf processo) {
		List<Pessoa> representados = consultarRepresentados(pessoa, processo);
		if (ProjetoUtil.isNotVazio(representados)) {
			for (Iterator<Pessoa> iterator = representados.iterator(); iterator.hasNext(); ) {
				Pessoa representado = iterator.next();
				
				if (representado.getIdPessoa() == pessoa.getIdPessoa()) {
					iterator.remove();
				}
			}
		}
		return ProjetoUtil.isNotVazio(representados);
	}
	
	/**
	 * Retorna a lista de representantes ativos de uma parte.
	 * 
	 * @param ProcessoParte pp
	 * @return List<ProcessoParteRepresentante>
	 */
	public List<ProcessoParteRepresentante> retornarRepresentantesParte(ProcessoParte pp) {
		return processoParteRepresentanteDAO.retornarRepresentantesParte(pp);
	}
	
	public ProcessoParteRepresentante criarRepresentacao(ProcessoParte representante, ProcessoParte representado,
			TipoParte tipoRepresentacao) {
		ProcessoParteRepresentante parteRep = new ProcessoParteRepresentante();
		parteRep.setParteRepresentante(representante);
		parteRep.setRepresentante(representante.getPessoa());
		parteRep.setTipoRepresentante(tipoRepresentacao);
		parteRep.setProcessoParte(representado);

		return parteRep;

	}

	public void inserirRepresentante(ProcessoParte parte, Pessoa pessoa, TipoParte tipoParte, ProcessoParteParticipacaoEnum polo, Endereco endereco) throws Exception {
		List<ProcessoParte> listaParteProcesso = parte.getProcessoTrf().getListaPartePoloObj(true,polo);
		ComponentUtil.getProcessoParteManager().validarParteExistente(pessoa, listaParteProcesso, tipoParte);
		List<ProcessoParteRepresentante> representacoesAtuais = parte.getProcessoParteRepresentanteList();
		ProcessoParteRepresentante representacaoNova = null;
		boolean representanteJaExiste = false;
		for (ProcessoParteRepresentante representanteAtual : representacoesAtuais) {
			if(representanteAtual.getProcessoParte().equals(parte) && representanteAtual.getRepresentante().equals(pessoa) && representanteAtual.getTipoRepresentante().equals(tipoParte)) {
				if(ProcessoParteSituacaoEnum.A.equals(representanteAtual.getInSituacao()) && ProcessoParteSituacaoEnum.A.equals(representanteAtual.getParteRepresentante().getInSituacao())) {
					representanteJaExiste = true;
					break;
				} else {
					representacaoNova = representanteAtual;
				}
			}
		}
		if(!representanteJaExiste) {
			if(representacaoNova == null) {
				ProcessoParte representante = new ProcessoParte();
				representante.setPessoa(pessoa);
				representante.setProcessoTrf(parte.getProcessoTrf());
				representante.setTipoParte(tipoParte);
				representante.setInParticipacao(polo);
				representante.setInSituacao(ProcessoParteSituacaoEnum.A);
				
				representacaoNova = criarRepresentacao(representante, parte, tipoParte);
				if (representacoesAtuais.contains(representacaoNova)) {
					representacoesAtuais.get(representacoesAtuais.indexOf(representacaoNova)).setInSituacao(ProcessoParteSituacaoEnum.A);
				} else {
					representacoesAtuais.add(representacaoNova);
				}
				if(endereco == null) {
					representante.setIsEnderecoDesconhecido(true);
				} else {
					ComponentUtil.getComponent(ProcessoParteEnderecoManager.class).associarEnderecoParteProcesso(representante, false, endereco);
				}
				ComponentUtil.getProcessoParteManager().persistAndFlush(representante);
				this.persistAndFlush(representacaoNova);
				ProcessoPushManager.instance().inserirNoPush(parte);
			} else {
				representacaoNova.getParteRepresentante().setInSituacao(ProcessoParteSituacaoEnum.A);
				ComponentUtil.getProcessoParteManager().mergeAndFlush(representacaoNova.getParteRepresentante());
				representacaoNova.setInSituacao(ProcessoParteSituacaoEnum.A);
				this.mergeAndFlush(representacaoNova);
			}
		}
		
	}

	public ProcessoParteRepresentante criaNovo(ProcessoParte representado, ProcessoParte representante, Pessoa pessoa, 
			TipoParte tipoParte) throws PJeBusinessException {
		
		ProcessoParteRepresentante processoParteRepresentante = new ProcessoParteRepresentante();
		processoParteRepresentante.setProcessoParte(representado);
		processoParteRepresentante.setRepresentante(pessoa);
		processoParteRepresentante.setTipoRepresentante(tipoParte);
		processoParteRepresentante.setParteRepresentante(representante);
		
		return this.persist(processoParteRepresentante);
	}
	
	/**
	 * Retorna true se as 3 condições abaixo forem satisfeitas: 1) Processo não é
	 * nulo; 2) O processo está distribuído; e 3) Existe expediente aberto e enviado
	 * para o Domicílio Eletrônico.
	 * 
	 * @param processo
	 * @return Booleano
	 */
	public boolean isPermitirAtualizacaoDomicilioEletronico(ProcessoTrf processo) {
		return processo != null && ProcessoStatusEnum.D.equals(processo.getProcessoStatus())
				&& ProcessoParteExpedienteManager.instance()
						.isExisteExpedienteAbertoEnviadoAoDomicilioEletronico(processo);
	}
	
	/**
	 * @param processo ProcessoTrf
	 * @return Todos os representantes ativos do processo.
	 */
	public List<ProcessoParteRepresentante> getRepresentantesAtivos(ProcessoTrf processo) {
		List<ProcessoParteRepresentante> representantes = new ArrayList<>();
		processo.getProcessoParteAutorReuList().stream()
				.forEach(p -> representantes.addAll(p.getProcessoParteRepresentanteListAtivos()));
		return representantes;
	}
}