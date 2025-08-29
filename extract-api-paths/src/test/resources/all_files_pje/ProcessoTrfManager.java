package br.com.infox.pje.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.dao.ProcessoTrfDAO;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.OrgaoJulgadorDAO;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.CompetenciaClasseAssuntoManager;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.entidades.Cda;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Rpv;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.SubstituicaoMagistrado;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.VinculacaoDependenciaEleitoral;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.ClasseJudicialInicialEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.RelatorRevisorEnum;
import br.jus.pje.nucleo.enums.TipoNomePessoaEnum;
import br.jus.pje.search.Criteria;

/**
 * Classe que acessa o DAO e contem a regra de negocios referente a entidade de
 * ProcessoTrf
 * 
 * @author Laércio
 */
@Name(ProcessoTrfManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ProcessoTrfManager extends GenericManager {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "processoTrfManager";

	@In
	private ProcessoTrfDAO processoTrfDAO;
	@In
	private OrgaoJulgadorDAO orgaoJulgadorDAO;

    /**
     * @return Instância da classe.
     */
    public static ProcessoTrfManager instance() {
        return ComponentUtil.getComponent(NAME);
    }
    
	public ProcessoTrf getProcessoTrfByProcesso(Processo processo) {
		return processoTrfDAO.find(ProcessoTrf.class, processo.getIdProcesso());
	}
	
	public ProcessoTrf getProcessoTrfByProcesso(ProcessoTrf processo) {
		return processoTrfDAO.find(ProcessoTrf.class, processo.getIdProcessoTrf());
	}

	/**
	 * Método que traz o primeiro autor e o primeiro réu do processo separados
	 * por um "x"
	 * 
	 * @param processoTrf
	 * @return retorna uma string com os nomes Ex.:
	 *         "1º Autor: João X 1º Réu: Maria"
	 */
	public String primeiroAutorXprimeiroReu(ProcessoTrf processoTrf) {
		String autor = "";
		String reu = "";

		ProcessoParte pp = processoTrfDAO.getProcessoParteByProcessoTrf(processoTrf, "A");
		if (pp != null) {
			autor = pp.getNomeParte();
		}
		pp = processoTrfDAO.getProcessoParteByProcessoTrf(processoTrf, "P");
		if (pp != null) {
			reu = pp.getNomeParte();
		}
		return "1º Autor: " + autor + " x 1º Réu: " + reu;
	}

	public String getNomeTarefaAtual(ProcessoTrf processoTrf) {
		SituacaoProcesso situacaoProcesso = processoTrfDAO.getSituacaoProcesso(processoTrf);
		return situacaoProcesso != null ? situacaoProcesso.getNomeTarefa() : null;
	}

	public SituacaoProcesso getSituacaoProcesso(ProcessoTrf processoTrf) {
		return getSituacaoProcesso(processoTrf, false);
	}

	/**
	 * Retorna a situação do processo informado
	 * @param processoTrf processo a ser consultada a situação
	 * @param refresh Boolean: informe TRUE caso queira os dados atualizados, sem cache. 
	 * @return retorna o objeto SituacaoProcesso
	 */
	public SituacaoProcesso getSituacaoProcesso(ProcessoTrf processoTrf, boolean refresh) {
		return processoTrfDAO.getSituacaoProcesso(processoTrf, refresh);
	}

	/**
	 * Traz uma lista de ProcessoParteRepresentante de uma parte do processo
	 * 
	 * @param processoTrf
	 * @param pessoa
	 * @return lista de ProcessoParteRepresentante
	 */
	public List<ProcessoParteRepresentante> getListRepresentanteByPessoaAndProcessoTrf(ProcessoTrf processoTrf,
			Pessoa pessoa) {
		return processoTrfDAO.getListRepresentanteByPessoaAndProcessoTrf(processoTrf, pessoa);
	}

	/**
	 * Método que retorna o primeiro autor cadastrado no processo
	 * 
	 * @param processoTrf
	 * @return Pessoa
	 */
	public ProcessoParte getAutorCabeca(ProcessoTrf processoTrf) {
		return processoTrfDAO.getProcessoParteByProcessoTrf(processoTrf, "A");
	}

	/**
	 * Método que retorna uma lista das pericias do processo
	 * 
	 * @param processoTrf
	 * @return lista de ProcessoPericia
	 */
	public List<PessoaPerito> getPeritosProcesso(ProcessoTrf processoTrf) {
		return processoTrfDAO.getPeritosByProcessoTrf(processoTrf);
	}

	/**
	 * Retorna as rpvs não canceladas ou rejeitadas de um processo
	 * 
	 * @param processoTrf
	 * @return lista de rpv
	 */
	public List<Rpv> getRpvsByProcessoTrfList(ProcessoTrf processoTrf) {
		return processoTrfDAO.getRpvsByProcessoTrfList(processoTrf);
	}

	/**
	 * Traz o Diretor de Secretaria da Vara (OrgaoJulgador)
	 * 
	 * @param orgaoJulgador
	 * @return retorna um Usuario do sistema
	 */
	public Usuario getDiretorVara(OrgaoJulgador orgaoJulgador) {
		return orgaoJulgadorDAO.getDiretorVaraByOrgaoJulgador(orgaoJulgador);
	}

	/**
	 * Traz o Juiz Federal da Vara (OrgaoJulgador)
	 * 
	 * @param orgaoJulgador
	 * @return retorna um Usuario do sistema
	 */
	public Usuario getJuizFederal(OrgaoJulgador orgaoJulgador) {
		return orgaoJulgadorDAO.getJuizFederalByOrgaoJulgador(orgaoJulgador);
	}

	/**
	 * Traz um processoParte especifico, filtrando por processo, pessoa e polo
	 * 
	 * @param pessoa
	 * @param processoTrf
	 * @param polo
	 * @return processoParte
	 */
	public ProcessoParte getParteByPessoaPoloAndProcesso(Pessoa pessoa, ProcessoTrf processoTrf, String polo) {
		return processoTrfDAO.getParteByPessoaPoloAndProcesso(pessoa, processoTrf, polo);
	}

	/**
	 * Lista das partes autoras de um processo
	 * 
	 * @param procTrf
	 * @return list ProcessoParte
	 */
	public List<ProcessoParte> getProcessoParteAutoreList(ProcessoTrf procTrf) {
		return processoTrfDAO.getListProcessoPartePrincipalByProcessoTrf(procTrf, "A");
	}

	/**
	 * Lista dos réus de um processo
	 * 
	 * @param procTrf
	 * @return list ProcessoParte
	 */
	public List<ProcessoParte> getProcessoParteReuList(ProcessoTrf procTrf) {
		return processoTrfDAO.getListProcessoPartePrincipalByProcessoTrf(procTrf, "P");
	}
	
	public Boolean isProcessoAptoParaSessao(ProcessoTrf processoTrf){
		if(processoTrf == null){
			return false;
		}
		return processoTrfDAO.isProcessoAptoParaSessao(processoTrf);
	}
        
	/**
	 * [PJEII-4329] Criado para verificar se o processo está concluso
	 * @param processoTrf Processo
	 * @return flag indicando se o processo está concluso
	 */
	public Boolean isProcessoConcluso(ProcessoTrf processoTrf) {
		return processoTrfDAO.isProcessoConcluso(processoTrf);
	}
	
	public String getPartesNomesResumidoPoloAtivo(ProcessoTrf processoTrf){
		return getPartesNomesResumidoPorPolo(processoTrf, ProcessoParteParticipacaoEnum.A);
	}
	
	public String getPartesNomesResumidoPoloPassivo(ProcessoTrf processoTrf){
		return getPartesNomesResumidoPorPolo(processoTrf, ProcessoParteParticipacaoEnum.P);		
	}
	
	public String getPartesNomesResumidoPoloTerceiros(ProcessoTrf processoTrf){
		return getPartesNomesResumidoPorPolo(processoTrf, ProcessoParteParticipacaoEnum.T);
	}
	
	private String getPartesNomesResumidoPorPolo(ProcessoTrf processoTrf, ProcessoParteParticipacaoEnum polo){
		
		List<ProcessoParte> partes = null;
		
		if(ProcessoParteParticipacaoEnum.A.equals(polo)){
			partes = processoTrf.getProcessoPartePoloAtivoSemAdvogadoList();
		}
		else if(ProcessoParteParticipacaoEnum.P.equals(polo)){
			partes = processoTrf.getProcessoPartePoloPassivoSemAdvogadoList();	
		}
		else{
			partes = processoTrf.getListaPartePrincipal(polo);
		}
		
		StringBuilder sb = new StringBuilder();
		
		if (partes.size() == 0) {
			sb.append("Não definido");
		}
		else{
			sb.append(partes.get(0).getNomeParte().toUpperCase());
			if (partes.size() > 1){
				sb.append(" e outros");
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Gera a lista de critérios para pesquisa de processos quando envolvem nome da parte ou documento da parte(CPF/ CNPJ)
	 * 
	 * O nome da parte é pesquisado apenas no campo nome da tabela pessoa.
	 * 
	 * @link	http://www.cnj.jus.br/jira/browse/PJESPRTII-2
	 * @param   nome	Nome da parte a ser pesquisado
	 * @param   documento	Documento da parte a ser pesquisado (CPF / CNPJ) 
	 * @return  Lista de 'Criteria' para a pesquisa
	*/
	public List<Criteria> getCriteriasPesquisaNomeDocumento(String nome, String documento) throws Exception{
		List<Criteria> criterios = new ArrayList<Criteria>();
		
		if (Strings.isEmpty(nome) && Strings.isEmpty(documento)){
			return criterios;
		} 
		criterios.add(Criteria.equals("processoParteList.parteSigilosa", false));
		
		criterios.add(Criteria.equals("processoParteList.partePrincipal", true));

		if (!Strings.isEmpty(nome)) {
			Criteria criterioNome = Criteria.contains("processoParteList.pessoa.nomesPessoa.nome", nome.replace(' ', '%'));
			criterios.add(criterioNome);

			Criteria tipoNomeCriteria = Criteria.in("processoParteList.pessoa.nomesPessoa.tipo", new TipoNomePessoaEnum[] {TipoNomePessoaEnum.C, TipoNomePessoaEnum.S} );
			criterios.add(tipoNomeCriteria);
		}

		if (!Strings.isEmpty(documento)) {

			if (!InscricaoMFUtil.validarCpfCnpj(documento)) {
				throw new Exception("Documento de identificação inválido");
			}

			if (documento != null) {
				criterios.add(Criteria.equals("processoParteList.pessoa.pessoaDocumentoIdentificacaoList.numeroDocumento", documento));
			}
			
			if (documento.length() == 14) {
				criterios.add(Criteria.equals("processoParteList.pessoa.pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo", "CPF"));
			} else {
				criterios.add(Criteria.equals("processoParteList.pessoa.pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo", "CPJ"));
				
			}
		}
		
		return criterios;
	}
	
	/**
	 *  Atualiza o status do processo (Na sua própria instancia) após ele ter sido remetido
	 * @param idProcessoTrf 
	 */
	public void atualizaStatusRemetidoOutraInstancia(Integer idProcessoTrf){
		ProcessoTrf processoTrf = processoTrfDAO.find(ProcessoTrf.class, idProcessoTrf);
		processoTrf.setInOutraInstancia(true);
		update(processoTrf);
		
	}

	/**
	 * Atualiza o status do processo quando ele for recebido
	 * @param idProcessoTrf 
	 */
	public void atualizaStatusRecebidoOutraInstancia(Integer idProcessoTrf){
		ProcessoTrf processoTrf = processoTrfDAO.find(ProcessoTrf.class, idProcessoTrf);
		processoTrf.setInOutraInstancia(false);
		update(processoTrf);
		
	}

	/**
	 * verifica o status do processo
	 * retorna true se o processo foi remetido e se o bloqueio estiver ativo
	 * @param idProcessoTrf
	 * @return
	 */
	public boolean isProcessoRemetidoBloqueado(Integer idProcessoTrf){
		if (ParametroUtil.instance().isBloquearProcessoRemetido()) { 
			boolean isBloqueioFluxoComunicacaoEntreInstancias = false;
			TramitacaoProcessualService tramitacaoProcessualService = ComponentUtil.getTramitacaoProcessualService();
			
			if (!tramitacaoProcessualService.isNullTaskInstance()) {
				if (tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.PJE_FLUXO_BLOQUEIO_COMUNICACAO_ENTRE_INSTANCIAS) != null) {
					isBloqueioFluxoComunicacaoEntreInstancias = BooleanUtils.toBoolean((String)tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.PJE_FLUXO_BLOQUEIO_COMUNICACAO_ENTRE_INSTANCIAS));
				}
	
				if (tramitacaoProcessualService.recuperaVariavel(Variaveis.PJE_FLUXO_BLOQUEIO_COMUNICACAO_ENTRE_INSTANCIAS) != null) {
					isBloqueioFluxoComunicacaoEntreInstancias = BooleanUtils.toBoolean((String)tramitacaoProcessualService.recuperaVariavel(Variaveis.PJE_FLUXO_BLOQUEIO_COMUNICACAO_ENTRE_INSTANCIAS));
				}
			}
			
			if (!isBloqueioFluxoComunicacaoEntreInstancias) {
				return processoTrfDAO.isProcessoRemetido(idProcessoTrf);	
			}
		}
		return false;
	}
	
	/**
	 * Verifica se o processo está bloqueado para remessa entre instâncias, caso o parâmetro {@linkplain ParametroUtil#isBloquearProcessoRemetido()} seja verdadeiro.
	 * 
	 * @param processoTrf
	 * @return true/false
	 */
	public boolean isProcessoRemetidoBloqueado(ProcessoTrf processoTrf) {
        return ParametroUtil.instance().isBloquearProcessoRemetido() && processoTrf.getInOutraInstancia();
	}
	
	
	/**
	 * Método responsável por recuperar os processos dos quais a {@link Pessoa} faz parte.
	 * 
	 * @param pessoa {@link Pessoa}.
	 * @return Os processos dos quais a {@link Pessoa} faz parte.
	 */
	public List<ProcessoTrf> recuperarProcessosRelacionados(Pessoa pessoa) {
		return this.processoTrfDAO.recuperarProcessosRelacionados(pessoa);
	}
	
	/**
	 * Método responsável por recuperar um {@link ProcessoTrf}.
	 * 
	 * @param numeroProcesso Número do processo.
	 * @param pessoa {@link Pessoa}.
	 * @return {@link ProcessoTrf}.
	 */
	public ProcessoTrf recuperarProcesso(String numeroProcesso, Pessoa pessoa) {
		return this.processoTrfDAO.recuperarProcesso(numeroProcesso, pessoa);
	}
	
	/**
	 * Método responsável por recuperar um {@link ProcessoTrf}.
	 * 
	 * @param numeroProcesso Número do processo
	 * @param classes Array de {@link ClasseJudicialInicialEnum}.
	 * @return {@link ProcessoTrf}.
	 */
	public ProcessoTrf recuperarProcesso(String numeroProcesso, ClasseJudicialInicialEnum... classes) {
		return this.processoTrfDAO.recuperarProcesso(numeroProcesso, classes);
	}

	/**
	 * Verifica se um processo foi protocolado
	 * 
	 * @param Integer idProcesso
	 * @return verdadeiro ou falso
	 */
	public boolean isProcessoProtocolado(Integer idProcesso) {
		ProcessoTrf processoTrf = processoTrfDAO.find(ProcessoTrf.class, idProcesso);
		
		if(processoTrf != null && processoTrf.getProcesso() != null){
			return processoTrf.getProcessoStatus().equals(ProcessoStatusEnum.D);
		}
		
		return false;
	}

	/**
	 * Retorna o tamanho do processo, ou seja, a soma do tamanho dos documentos ativos do processo.
	 * 
	 * @param processo ProcessoTrf
	 * @return Long do tamanho do processo.
	 */
	public Long obterTamanho(ProcessoTrf processo) {
		Long resultado = new Long(0);
		if (processo != null) {
			resultado = this.processoTrfDAO.obterTamanho(processo);
		}
		return resultado;
	}
	
	/**
	 * Metodo responsavel por remover o processo
	 * 
	 * @param processoTrf O {@link ProcessoTrf}
	 */
	public void removerProcesso(ProcessoTrf processoTrf) {
		if (processoTrf != null && processoTrf.getIdProcessoTrf() > 0) {
			this.processoTrfDAO.removerProcesso(processoTrf);			
		}
	}
	
	/**
	 * Dada uma substituição de magistrado, retorna os processos que foram distribuídos durante a substituição no órgão julgador em 
	 * que tal afastamento ocorreu.
	 * 
	 * @param substituicaoMagistrado substituição a ser considerada.
	 * @return processos distribuidos.
	 */
	public List<ProcessoTrf> obterProcessosDistribuidosDuranteSubstituicao(SubstituicaoMagistrado substituicaoMagistrado){
		return this.processoTrfDAO.obterProcessosDistribuidosDuranteSubstituicao(substituicaoMagistrado);
	}
	
	/**
	 * Bloqueia/desbloqueia peticionamento para determinado processo
	 * @param idProcessoTrf 
	 * @param Boolean bloqueio
	 */
	public void bloquearPeticionamento(Integer idProcessoTrf, boolean bloqueio){
		ProcessoTrf processoTrf = EntityUtil.getEntityManager().getReference(ProcessoTrf.class, idProcessoTrf);
		processoTrf.setInBloqueiaPeticao(bloqueio);
		update(processoTrf);
	}
	
	public void merge(ProcessoTrf processoTrf) {
		this.processoTrfDAO.merge(processoTrf);
	}
	
	public ProcessoTrf obterProcessoDistribuido(Eleicao eleicao, VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral, Estado estado, Municipio municipio) {
		return this.processoTrfDAO.obterProcessoDistribuido(eleicao, vinculacaoDependenciaEleitoral, estado, municipio);
	}
	
	public ProcessoTrf obterPorNumero(String nrProcesso){
		return processoTrfDAO.obterPorNumero(nrProcesso);
	}
	
	public ProcessoTrf recuperarProcesso(ProcessoTrf processoTrf) {
		ProcessoTrf retorno = processoTrf;
		if(processoTrf == null) {
			retorno = ProcessoTrfHome.instance().getProcessoTrf();
			if(retorno == null) {
                org.jbpm.graph.exe.ProcessInstance instancia = org.jboss.seam.bpm.ProcessInstance.instance(); 
				if( instancia != null && instancia.getContextInstance() != null ) {
					retorno = ComponentUtil.getTramitacaoProcessualService().recuperaProcesso();
				}
			}
		}
		return retorno;
	}
	
	public boolean permiteIndicarPauta(ProcessoTrf processo) {
		boolean retorno = false;
		boolean podePedir = false;
		boolean prontoRevisao = processo.getProntoRevisao() == null ? false : processo.getProntoRevisao();
		boolean processoSemRevisao = processo.getExigeRevisor() == null || processo.getExigeRevisor() == false;
		boolean ehRelator = processo.getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual()); 
		boolean ehRevisor = processo.getOrgaoJulgadorRevisor() != null && processo.getOrgaoJulgadorRevisor().equals(Authenticator.getOrgaoJulgadorAtual()); 
		if( processo.getOrgaoJulgadorColegiado().getRelatorRevisor().equals(RelatorRevisorEnum.REL)) {
			podePedir =  ehRelator && (processo.getOrgaoJulgadorColegiado().getPautaAntecRevisao() || processoSemRevisao || (processo.getExigeRevisor() && prontoRevisao));
		} else {
			podePedir = (processoSemRevisao && ehRelator) || (!processoSemRevisao && ehRevisor);
		}
		boolean processoNaoPautado = true;
		boolean ehRelatorVista = false;
		SessaoPautaProcessoTrf sessaoPauta = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperaUltimaPautaProcessoNaoExcluido(processo);
		if( sessaoPauta != null ) {
			if( sessaoPauta.getSessao().getDataRealizacaoSessao() == null ) {
				processoNaoPautado = false;
			} else {
				if(AdiadoVistaEnum.PV.equals(sessaoPauta.getAdiadoVista()) ) {
					if(Authenticator.getOrgaoJulgadorAtual().equals(sessaoPauta.getOrgaoJulgadorPedidoVista())) {
						ehRelatorVista = true;
					}
				}
			}
		}
		retorno = ( ehRelatorVista || podePedir ) && processoNaoPautado;
		return retorno;
	}
	
	public void gravarSugestaoSessao(ProcessoTrf processo, Sessao sessao) {
		this.processoTrfDAO.gravarSugestaoSessao(processo, sessao);
	}
	
	/**
	 * Dado um processo X, o juizo será formado por todas as localizações físicas superiores deste processo até a raiz do tribunal e
	 * as pessoas estarão no juizo deste processo se estiverem em uma dessas localizacoes e não tiverem OJC configurado ou estiverem no mesmo OJC do processo
	 * 
	 * @param processo
	 * @param localizacaoFisicaPessoa
	 * @param ojcPessoa
	 * @return
	 */
	public boolean isPessoaJuizoProcesso(ProcessoTrf processo, Localizacao localizacaoFisicaPessoa, OrgaoJulgadorColegiado ojcPessoa, boolean isServidorExclusivoOJC) {
		boolean isJuizoProcesso = false;
		if((ojcPessoa == null) || (ojcPessoa != null && processo.getOrgaoJulgadorColegiado() != null &&
				processo.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado() == ojcPessoa.getIdOrgaoJulgadorColegiado())) {

			if(isServidorExclusivoOJC) {
				isJuizoProcesso = true;
			}else {
				if(processo.getOrgaoJulgador() != null && localizacaoFisicaPessoa != null) {
					Localizacao localizacaoOJ = processo.getOrgaoJulgador().getLocalizacao();
					List<Localizacao> localizacaoOJList = LocalizacaoManager.instance().getArvoreAscendente(localizacaoOJ.getIdLocalizacao(), true);
					for (Localizacao localizacao : localizacaoOJList) {
						if(localizacaoFisicaPessoa.getIdLocalizacao() == localizacao.getIdLocalizacao()) {
							isJuizoProcesso = true;
						}
					}
				}
			}
		}
		
		return isJuizoProcesso;
	}
	
	public boolean isPessoaLogadaJuizoProcesso(ProcessoTrf processo) {
		Localizacao localizacaoFisicaPessoa = Authenticator.getLocalizacaoFisicaAtual(); 
		OrgaoJulgadorColegiado ojcPessoa = Authenticator.getOrgaoJulgadorColegiadoAtual();
		boolean isServidorExclusivoOJC = Authenticator.isServidorExclusivoColegiado();
		
		return this.isPessoaJuizoProcesso(processo, localizacaoFisicaPessoa, ojcPessoa, isServidorExclusivoOJC);
	}
	
	public boolean isProcessoDeslocadoParaLocalizacaoPessoaLogada(ProcessoTrf processo) {
		Localizacao localizacaoFisicaPessoa = Authenticator.getLocalizacaoFisicaAtual(); 
		SituacaoProcessoManager spm = ComponentUtil.getComponent(SituacaoProcessoManager.NAME);
		SituacaoProcesso sp = spm.getByIdTaskInstance(ProcessoTrfHome.instance().getIdTaskInstance());
		
		if(sp != null && localizacaoFisicaPessoa.getIdLocalizacao() == sp.getIdLocalizacao().intValue()) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * Ao protocolar um processo que tenha configurações de competência, classe e assunto determinantes do segredo do processo, o sistema deve
	 * atribuir a característica de segredo ao processo.
	 * Também ao protocolar um processo marcado como Segredo de Justiça, o sistema deverá verificar os níveis de acesso configurados previamente 
	 * para a Competência, Classe e Assunto e atribuir ao nível de acesso o maior valor da configuração
	 */
	public void aplicarNivelSigilo(ProcessoTrf processoTrf) {
		CompetenciaClasseAssuntoManager ccaManager = ComponentUtil.getComponent(CompetenciaClasseAssuntoManager.class);

		if (!processoTrf.getSegredoJustica()) {
			boolean isConfigSegredo = ccaManager.recuperarConfiguracaoSegredo(processoTrf, processoTrf.getCompetencia());
			processoTrf.setSegredoJustica(isConfigSegredo);
		}

		if (processoTrf.getSegredoJustica()) {
			Integer nivelAcessoMaximo = ccaManager.recuperarNivelAcesso(processoTrf, processoTrf.getCompetencia());
			if (processoTrf.getNivelAcesso() < nivelAcessoMaximo) {
				processoTrf.setNivelAcesso(nivelAcessoMaximo);
			}else if (processoTrf.getNivelAcesso() == 0 && nivelAcessoMaximo == 0)
			{
				processoTrf.setNivelAcesso(1);
			}
		}
	}

	public String getLabelNivelAcesso(int nivelAcesso) {
		String descricao = null;
		switch (nivelAcesso) {
		case 1:
			descricao = "1 - Segredo de justiça";
			break;
		case 2:
			descricao = "2 - Sigilo mínimo";
			break;
		case 3:
			descricao = "3 - Sigilo médio";
			break;
		case 4:
			descricao = "4 - Sigilo intenso";
			break;
		case 5:
			descricao = "5 - Sigilo absoluto";
			break;

		default:
			break;
		}
		return descricao;
	}

	public String getTooltipNivelAcesso(int nivelAcesso) {
		String descricao = null;
		switch (nivelAcesso) {
		case 1:
			descricao = "Segredo de justiça: acessíveis aos servidores do Judiciário, aos servidores dos órgãos públicos de colaboração na administração da Justiça e às partes do processo.";
			break;
		case 2:
			descricao = "Sigilo mínimo: acessível aos servidores do Judiciário e aos demais órgãos públicos de colaboração na administração da Justiça.";
			break;
		case 3:
			descricao = "Sigilo médio: acessível aos servidores do órgão em que tramita o processo, à(s) parte(s) que provocou(ram) o incidente e àqueles que forem expressamente incluídos.";
			break;
		case 4:
			descricao = "Sigilo intenso: acessível a classes de servidores qualificados (magistrado, diretor de secretaria/escrivão, oficial de gabinete/assessor) do órgão em que tramita o processo, às partes que provocaram o incidente e àqueles que forem expressamente incluídos.";
			break;
		case 5:
			descricao = "Sigilo absoluto: acessível apenas ao magistrado do órgão em que tramita, aos servidores e demais usuários por ele indicado e às partes que provocaram o incidente.";
			break;
			
		default:
			break;
		}
		return descricao;
	}

	/**
	 * Recupera o relator do processo informado.
	 * 
	 * @param processoTrf ProcessoTrf processoTrf.
	 * @return Usuario relator do processo informado.
	 */
	public Usuario recuperarRelator(ProcessoTrf processoTrf) {
		ProcessoTrfHome processoTrfHome = ComponentUtil.getProcessoTrfHome();
		return processoTrfHome.getRelator(processoTrf);
	}
	
	/**
	 * Recupera o órgão julgador do processo.
	 * 
	 * @param processoTrf ProcessoTrf dados do processo.
	 * @return String descricao do orgao julgador.
	 */
	public String recuperarOrgaoJulgador(ProcessoTrf processoTrf) {
		if (processoTrf != null && processoTrf.getOrgaoJulgador() != null) {
			return processoTrf.getOrgaoJulgador().getOrgaoJulgador();
		}
		return null;
	}
	
	/**
	 * Recupera a descrição do processo. 
	 * Sigla da classe concatenada com o n?mero do processo.
	 * 
	 * @param processoTrf ProcessoTrf dados do processo.
	 * @return String descricao.
	 */
	public String recuperarDescricaoProcesso(ProcessoTrf processoTrf) {
		StringBuilder descricaoProcesso = new StringBuilder();
		String siglaClasseJudicial = processoTrf .getClasseJudicial().getClasseJudicialSigla();
		descricaoProcesso.append(siglaClasseJudicial != null ? siglaClasseJudicial + " " : "");
		descricaoProcesso.append(processoTrf .getProcesso().getNumeroProcesso());
		return descricaoProcesso.toString();
	}
	
	/**
	 * Atualiza o valor do processo com a soma das CDA's.
	 * 
	 * @param processo
	 * @return Valor do processo atualizado.
	 */
	public void atribuirValorProcessoComSomaCda(ProcessoTrf processo) {
		
		if (processo != null) {
			BigDecimal soma = BigDecimal.ZERO;
			List<Cda> cdas = processo.getColecaoCda();
			
			if(ProjetoUtil.isNotVazio(processo.getColecaoCda())) {
				for (Cda cda : cdas) {
					if (cda.getAtivo()) {
						soma = soma.add(cda.getValor());
					}
				}
			}
			processo.setValorCausa(soma.doubleValue());
			update(processo);
		}
	}

	public ProcessoTrf getProcessoTrfByIdProcessoTrf(Integer idProcessoTrf) {
		return processoTrfDAO.getProcessoTrfByIdProcessoTrf(idProcessoTrf);
	}

	public int[] getProcessosEnviadosAoDomicilioEletronico() throws Exception {
		return processoTrfDAO.getProcessosEnviadosAoDomicilioEletronico();
	}

	/**
	 * Consulta os ids de processos distribuidos filtrando por idPessoa que esteja ativa
	 * @param idPessoa
	 * @return List<Integer>
	 */
	public List<Integer> consultarIdProcessoDistribuidoPorIdPessoaAtivo(Integer idPessoa) {
		return processoTrfDAO.consultarIdProcessoDistribuidoPorIdPessoaAtivo(idPessoa);
	}
	
	public List<ProcessoTrf> recuperarProcessosApensados(ProcessoTrf processoTrf) {
		return this.processoTrfDAO.recuperarProcessosApensados(processoTrf);
	}
}
