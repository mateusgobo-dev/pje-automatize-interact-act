package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.home.ProcessoParteHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.PessoaProcuradorProcuradoriaManager;
import br.com.infox.pje.manager.TipoParteConfigClJudicialManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.ProcessoParteDAO;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.identity.PjeIdentity;
import br.jus.cnj.pje.nucleo.manager.cache.ProcessoParteCache;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaProcuradorProcuradoria;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteMin;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoParteVisibilidadeSigilo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.ProcessoParteOrdenadorEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(ProcessoParteManager.NAME)
public class ProcessoParteManager extends BaseManager<ProcessoParte>{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "processoParteManager";

	@In
	private ProcessoParteDAO processoParteDAO;

	private ProcessoParteCache processoParteCache;

	@In
	private TipoParteManager tipoParteManager;

	@Logger
	private Log logger;

	@Override
	protected ProcessoParteDAO getDAO(){
		return processoParteDAO;
	}
	
	public static ProcessoParteManager instance() {
		return ComponentUtil.getComponent(ProcessoParteManager.class);
	}

	private ProcessoParteCache getProcessoParteCache() {
		if (processoParteCache == null) {
			processoParteCache = (ProcessoParteCache) Component.getInstance(ProcessoParteCache.COMPONENT_NAME);
		}

		return processoParteCache;
	}

	/**
	 * Método que traz o primeiro autor e o primeiro réu do processo separados por um "x"
	 * 
	 * @param processoTrf
	 * @return retorna uma string com os nomes Ex.: "1º Autor: João X 1º Réu: Maria"
	 */
	public String primeiroAutorXprimeiroReu(ProcessoTrf processoTrf){
		String autor = "";
		if (processoParteDAO.getProcessoParteByProcessoTrf(processoTrf, "A") != null){
			autor = processoParteDAO.getProcessoParteByProcessoTrf(processoTrf, "A").getNomeParte();
		}
		String reu = "";
		if (processoParteDAO.getProcessoParteByProcessoTrf(processoTrf, "P") != null){
			reu = processoParteDAO.getProcessoParteByProcessoTrf(processoTrf, "P").getNomeParte();
		}
		return "1º Autor: " + autor + " x 1º Réu: " + reu;
	}
	
	public String nomeExibicao(ProcessoTrf processo, ProcessoParteParticipacaoEnum polo){
		long[] valores = processoParteDAO.contagemPartesComSigilosas(processo, true, polo);
		long cont = valores[0];
		boolean contemParteSigilosa = (valores[1] > 0);
		if(cont > 0){
			ProcessoParte cabeca = processoParteDAO.recuperaCabeca(processo, polo);
			StringBuilder sb = new StringBuilder();
			if (podeVisualizarNomeDoPolo(cabeca)) {
				String nome = cabeca.getNomeParteSemSegredo();
				if(cabeca.getIsBaixado() || cabeca.getIsSuspenso()){
					sb.append("<span class=text-strike>"+ nome +"</span>");
				} else {
					sb.append(nome);
				}
			}
			if (sb.length() == 0 && contemParteSigilosa) {
				sb.append("<span class=\"text-danger\">(Parte em segredo de justiça)</span>");
			} 
			if(cont > 1) {
				sb.append(" e outros (");
				sb.append(cont - 1);
				sb.append(")");
			}
			return sb.toString();
		}else{
			return "Não definida";
		}
	}
	
	/**
	 * Recupera o nome da parte.
	 * 
	 * @param processoTrf ProcessoTrf entidade com os dados do processo.
	 * @param cont
	 * @param processoParteParticipacao String indica se é ativo ou passivo.
	 * @return
	 * @throws PJeBusinessException
	 */
	public String nomeParaExibicao(ProcessoTrf processoTrf, long cont, String processoParteParticipacao) throws PJeBusinessException {
		String parte = "";
		if (processoParteParticipacao != null && String.valueOf(ProcessoParteParticipacaoEnum.A).equals(processoParteParticipacao)) {
			if (processoTrf != null && CollectionUtils.isNotEmpty(processoTrf.getProcessoPartePoloAtivoSemAdvogadoList()) == false) {
				ProcessoJudicialManager processoJudicialManager = ComponentUtil.getProcessoJudicialManager();
				processoTrf = processoJudicialManager.findById(processoTrf.getIdProcessoTrf());
			}

			if (processoTrf != null && CollectionUtils.isNotEmpty(processoTrf.getProcessoPartePoloAtivoSemAdvogadoList())) {
				List<ProcessoParte> listaProcessoParte = processoTrf.getProcessoPartePoloAtivoSemAdvogadoList();
				ordenarListaPolosPorOrdem(listaProcessoParte, ProcessoParteParticipacaoEnum.A);
				parte = recuperaNomeUsadoNoProcesso(listaProcessoParte.get(0));
			}
		}
		if (processoParteParticipacao != null && String.valueOf(ProcessoParteParticipacaoEnum.P).equals(processoParteParticipacao)) {
			if (processoTrf != null && CollectionUtils.isNotEmpty(processoTrf.getProcessoPartePoloPassivoSemAdvogadoList()) == false) {
				ProcessoJudicialManager processoJudicialManager = ComponentUtil.getProcessoJudicialManager();
				processoTrf = processoJudicialManager.findById(processoTrf.getIdProcessoTrf());
			}

			if (processoTrf != null && CollectionUtils.isNotEmpty(processoTrf.getProcessoPartePoloPassivoSemAdvogadoList())) {
				List<ProcessoParte> listaProcessoParte = processoTrf.getProcessoPartePoloPassivoSemAdvogadoList();
				ordenarListaPolosPorOrdem(listaProcessoParte, ProcessoParteParticipacaoEnum.P);
				parte = recuperaNomeUsadoNoProcesso(listaProcessoParte.get(0));
			}
		}
		return ComponentUtil.getProcessoParteManager().nomeExibicao(parte, cont);
	}
	
	public boolean podeVisualizarNomeDoPolo(ProcessoParte processoParte) {
		boolean podeVisualizar = true;
		if (processoParte.getParteSigilosa()) {
			podeVisualizar = PjeIdentity.instance().hasRole(Papeis.VISUALIZA_SIGILOSO) && Authenticator.isUsuarioInterno();
			if (podeVisualizar) {
				Integer idOrgaoJulgadorUsuario = Authenticator.getIdOrgaoJulgadorAtual();
				Integer idOrgaoJulgadorProcesso = recuperarIdOrgaoJulgadorProcessoPorProcessoParte(processoParte);
				if (idOrgaoJulgadorUsuario != null && idOrgaoJulgadorProcesso != null) {
					podeVisualizar = idOrgaoJulgadorUsuario.equals(idOrgaoJulgadorProcesso);
				}
			}
			if (!podeVisualizar) {
				ProcessoParteVisibilidadeSigiloManager processoParteVisibilidadeSigiloManager = 
						ComponentUtil.getComponent(ProcessoParteVisibilidadeSigiloManager.class);
				Integer idUsuarioLogado = Authenticator.getIdUsuarioLogado();
				List<ProcessoParteVisibilidadeSigilo> visualizadores = processoParteVisibilidadeSigiloManager.recuperarVisualizadores(processoParte);
				for (ProcessoParteVisibilidadeSigilo visualizador : visualizadores) {
					if (visualizador.getPessoa().getIdUsuario().equals(idUsuarioLogado)) {
						podeVisualizar = true;
						break;
					}
				}
			}
		}
		return podeVisualizar;
	}
	
	private Integer recuperarIdOrgaoJulgadorProcessoPorProcessoParte(ProcessoParte processoParte) {
		if (processoParte.getProcessoTrf() != null && processoParte.getProcessoTrf().getOrgaoJulgador() != null) {
			return processoParte.getProcessoTrf().getOrgaoJulgador().getIdOrgaoJulgador();
		} 
		return recuperarIdOrgaoJulgadorPorProcessoParte(processoParte);
	}
	
	/** 
	 * Retorna o nome da parte não sigilosa do processo. Caso seja sigilosa, retorna "(Em segredo de justiça)".
	 * 
	 * @param processo Processo a respeito do qual se quer a informação.
	 * @param polo 'A' Ativo , 'P 'Passivo, 'O' Outros.
	 * @return O nome da parte não sigilosa do processo. Se sigilosa, retorna "(Em segredo de justiça)".
	 */
	public String nomeExibicaoConsultaPublica(ProcessoTrf processo, ProcessoParteParticipacaoEnum polo){
		List<ProcessoParte> processoParteList = processo.getListaPartePrincipal(polo);
		processoParteList = retirarProcessoParteConsultaPublica(processoParteList);
		int cont = processoParteList.size();
		boolean contemParteSigilosa = false;
		if(cont > 0){
			StringBuilder sb = new StringBuilder();
			for (ProcessoParte p: processoParteList) {
				contemParteSigilosa = p.getParteSigilosa() ? true : contemParteSigilosa;
				if (!p.getParteSigilosa() && sb.length() == 0) { 
					if(p.getIsSuspenso()){
						sb.append("<span class=text-strike>"+ p.getNomeParte() +"</span>");
					} else {
						sb.append(p.getNomeParte());
					}
				}
			}
			if (sb.length() == 0 && contemParteSigilosa) {
				sb.append("<span class=\"text-danger\">(Parte em segredo de justiça)</span>");
			} 
			if(cont > 1) {
				sb.append(" e outros (");
				sb.append(cont - 1);
				sb.append(")");
			}
			return sb.toString();
		}else{
			return "Não definida";
		}
	}
	
	
	private List<ProcessoParte> retirarProcessoParteConsultaPublica(List<ProcessoParte> processoParteList) {
		List<ProcessoParte> novaLista = new ArrayList<>();
		for (ProcessoParte processoParte : processoParteList) {
			if (!processoParte.getIsBaixado() && processoParte.getInSituacao() != ProcessoParteSituacaoEnum.I) {
				novaLista.add(processoParte);
			}
		}
		return novaLista;
	}

	public String nomeExibicao(String parte, long cont){
		if(cont > 0){
			StringBuilder sb = new StringBuilder();
			sb.append(parte);
			if(cont > 1){
				sb.append(" (");
				sb.append(cont - 1);
				sb.append(")");
			}
			return sb.toString();
		}else{
			return "Não definida";
		}
	}

	/**
	 * Este método recebe como argumento uma parte do processo e valida se é do
	 * tipo <i> Autoridade Coatora</i>
	 * 
	 * @author Joao Paulo Lacerda
	 * @param processoParte
	 *            parte do processo a ser validada.
	 * @return <code>True</code> caso seja uma <i>Autoridade Coatora</i> ou
	 *         <code>False</code> caso não seja.
	 */
	public boolean isAutoridadeCoatora(ProcessoParte processoParte) {
		if (!processoParte.getTipoParte().equals(ParametroUtil.instance().getTipoParteAutoridadeCoatora())) {
			return false;
		}
		return true;
	}

	/**
	 * Este método recebe como argumento uma parte do processo e valida se é um
	 * advogado com a situação <i>Ativa</i> e se possui certificado cadastrado
	 * no sistema.</li>
	 * 
	 * @author Joao Paulo Lacerda
	 * @param processoParte
	 *            parte do processo a ser validada.
	 * @return <code>True</code> caso não seja um advogado certificado ou
	 *         <code>False</code> caso seja.
	 */
	public boolean isAdvogadoNaoCertificado(ProcessoParte processoParte) {
		if (processoParte.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())) {
			if (!processoParte.getPessoa().getAtivo() || processoParte.getPessoa().getCertChain() == null
					|| processoParte.getPessoa().getAssinatura() == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Este método recebe como argumento uma parte do processo e valida se a
	 * parte é uma pessoa física com um advogado certificado e se este advogado
	 * só representa esta pessoa física.
	 * 
	 * @author Joao Paulo Lacerda
	 * @param processoParte
	 *            parte do processo a ser validada.
	 */
	public boolean isUmaPessoaFisicaComUmAdvogadoCertificado(ProcessoParte processoParte) {
		Integer partesAtivasSize = 0;
		List<ProcessoParte> processoParteAdvogadoAtivoList = new ArrayList<ProcessoParte>();
		for (ProcessoParte pp : processoParte.getProcessoTrf().getProcessoParteList()) {
			if (pp.getInParticipacao() == ProcessoParteParticipacaoEnum.A) {
				partesAtivasSize++;
				if (pp.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())) {
					processoParteAdvogadoAtivoList.add(pp);
				}
			}
		}
		if (partesAtivasSize == 2 && processoParte.getPessoa().getInTipoPessoa() == TipoPessoaEnum.F) {
			if (processoParteAdvogadoAtivoList.size() == 1) {
				if (ComponentUtil.getComponent(PessoaManager.class).isPessoaCertificada(processoParteAdvogadoAtivoList.get(0).getPessoa())) {
					return true;
				}
			}
		}
		return false;
	}

	public List<Pessoa> getPartesComEmail(ProcessoParte processoParte) {
		List<Pessoa> pessoaList = new ArrayList<Pessoa>();
		Pessoa advogadoComEmail = getAdvogadoComEmail(processoParte);
		if (advogadoComEmail != null) {
			pessoaList.add(advogadoComEmail);
		}
		pessoaList.addAll(getRepresentantesComEmail(processoParte));
		pessoaList.addAll(getProcuradoresComEmail(processoParte.getPessoa()));
		return pessoaList;
	}

	public Pessoa getAdvogadoComEmail(ProcessoParte processoParte) {
		Pessoa pessoa = processoParte.getPessoa();
		if (pessoa.getInTipoPessoa() == TipoPessoaEnum.F) {
			if (processoParte.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())) {
				if (pessoa.getEmail() != null) {
					return pessoa;
				}
			}
		}
		return null;
	}

	public List<Pessoa> getRepresentantesComEmail(ProcessoParte processoParte) {
		List<Pessoa> pessoaList = new ArrayList<Pessoa>();
		if (processoParte.getPessoa().getInTipoPessoa() == TipoPessoaEnum.F) {
			if (!processoParte.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())) {
				for (ProcessoParteRepresentante ppr : processoParte.getProcessoParteRepresentanteList2()) {
					if (ppr.getRepresentante().getEmail() != null) {
						pessoaList.add(ppr.getRepresentante());
					}
				}
			}
		}
		return pessoaList;
	}

	public List<Pessoa> getProcuradoresComEmail(Pessoa pessoa) {
		List<Pessoa> pessoaList = new ArrayList<Pessoa>();
		if (pessoa.getInTipoPessoa() == TipoPessoaEnum.J && pessoa.getAtraiCompetencia()) {
			List<PessoaProcuradorProcuradoria> pessoaProcuradorProcuradoriaList = ComponentUtil.getComponent(PessoaProcuradorProcuradoriaManager.class)
					.getPessoaProcuradorProcuradoriaList(pessoa);
			if (pessoaProcuradorProcuradoriaList != null) {
				for (PessoaProcuradorProcuradoria ppp : pessoaProcuradorProcuradoriaList) {
					if (ppp.getPessoaProcurador().getEmail() != null) {
						pessoaList.add(ppp.getPessoaProcurador().getPessoa());
					}
				}
			}
		}
		return pessoaList;
	}

	public List<Pessoa> getPessoasPoloAtivoList(ProcessoTrf processoTrf) {
		return getPartesProcesso(processoTrf, ProcessoParteParticipacaoEnum.A);
	}

	public List<Pessoa> getPessoasPoloPassivoList(ProcessoTrf processoTrf) {
		return getPartesProcesso(processoTrf, ProcessoParteParticipacaoEnum.P);
	}

	private List<Pessoa> getPartesProcesso(ProcessoTrf processoTrf, ProcessoParteParticipacaoEnum participacao) {
		List<Pessoa> partes = new ArrayList<Pessoa>();
		for (ProcessoParte processoParte : processoTrf.getProcessoParteList()) {
			if (processoParte.getInParticipacao().equals(participacao)) {
				partes.add(processoParte.getPessoa());
			}
		}
		return partes;
	}
	
	/**
	 * Retorna todas as partes ativas de um processo judicial
	 * 
	 * @param processoJudicial processoTrf
	 * @return uma lista de ProcessoParte
	 */
	public List<ProcessoParte> getPartes(ProcessoTrf processoJudicial) throws PJeBusinessException {
		List<ProcessoParte> partesProcesso = new ArrayList<ProcessoParte>();
		partesProcesso.addAll(processoJudicial.getListaParteAtivo());
		partesProcesso.addAll(processoJudicial.getListaPartePassivo());
		try{
			List<Pessoa> pessoas = new ArrayList<Pessoa>();
			for (ProcessoParte partePessoa : partesProcesso) {
				pessoas.add(partePessoa.getPessoa());
			}
			for (ProcessoParte processoParte : processoJudicial.getListaParteTerceiro()) {
				if(!pessoas.contains(processoParte.getPessoa())){
					partesProcesso.add(processoParte);
				}
			}
		} catch (Exception e) {
			logger.error("Erro ao tentar processar as partes ", e.getMessage());
		}
	
		return partesProcesso;
	}

	
	
	/**
	 * Retorna o ProcessoParte baseado nos parametros.
	 * 
	 * @param processoJudicial processoTrf
	 * @param tipoParte tipo da parte
	 * @param pessoa Pessoa
	 * @return A parte referente a pesquisa
	 */
	public ProcessoParte findProcessoParte(ProcessoTrf processoJudicial, TipoParte tipoParte, Pessoa pessoa){
		return processoParteDAO.findProcessoParte(processoJudicial, tipoParte, pessoa);
	}

	/**
	 * Retorna o ProcessoParte baseado nos parametros.
	 * 
	 * @param processoJudicial processoTrf
	 * @param pessoa Pessoa
	 * @return A parte referente a pesquisa
	 */
	public ProcessoParte findProcessoParte(ProcessoTrf processoJudicial, Pessoa pessoa, boolean verificaSeProcurador){
		return findProcessoParte(processoJudicial, null, pessoa, verificaSeProcurador, null, true);
	}
	
	public ProcessoParteParticipacaoEnum identificaParticipacaoPessoa(ProcessoTrf processo, Pessoa pessoa, boolean verificaSeProcurador) {
		ProcessoParteParticipacaoEnum participacaoPessoa = ProcessoParteParticipacaoEnum.N;
		
		ProcessoParte parte = findProcessoParte(processo, pessoa, verificaSeProcurador);
		if(parte != null && parte.getInParticipacao() != null) {
			participacaoPessoa = parte.getInParticipacao();
		}
		
		return participacaoPessoa;
	}
	
	/**
	 * Retorna o ProcessoParte baseado nos parametros.
	 * 
	 * @param processoJudicial processoTrf
	 * @param tipoParte tipo da parte
	 * @param pessoa Pessoa
	 * @param inParticipacao Ativo, Passivo, outros
	 * @return A parte referente a pesquisa
	 */
	public ProcessoParte findProcessoParte(ProcessoTrf processoJudicial, TipoParte tipoParte, Pessoa pessoa, ProcessoParteParticipacaoEnum processoParteParticipacaoEnum){
		return findProcessoParte(processoJudicial, tipoParte, pessoa, false, processoParteParticipacaoEnum, false);
	}
	
	public ProcessoParte findProcessoParte(ProcessoTrf processoJudicial, TipoParte tipoParte, Pessoa pessoa, boolean verificaSeProcurador, ProcessoParteParticipacaoEnum processoParteParticipacaoEnum, boolean excluiInativos){
		return processoParteDAO.findProcessoParte(processoJudicial, tipoParte, pessoa, processoParteParticipacaoEnum, excluiInativos, verificaSeProcurador);
	}
	
	/**
	 * Indica se uma pessoa compõe o processo judicial como uma parte de um ou mais tipos dados.
	 * 
	 * @param processoJudicial o processo judicial a respeito do qual se pretende buscar a informação
	 * @param pessoa a pessoa que se pretende identificar como parte
	 * @param tipoParte os tipos de parte que se pretende investigar
	 * @return true, se a pessoa for uma parte do tipo dado no processo judicial indicado
	 */
	public boolean isParte(ProcessoTrf processoJudicial, Pessoa pessoa, TipoParte...tipoParte){
		return isParte(processoJudicial, pessoa, null, tipoParte);
	}
	
	/**
	 * Indica se uma pessoa compõe o processo judicial como uma parte de um ou mais tipos dados no polo indicado.
	 * 
	 * @param processoJudicial o processo judicial a respeito do qual se pretende buscar a informação
	 * @param pessoa a pessoa que se pretende identificar como parte
	 * @param polo o polo de interesse, ou null, se o polo for indiferente
	 * @param tipoParte os tipos de parte que se pretende investigar
	 * @return true, se a pessoa for uma parte do tipo dado no processo judicial indicado
	 */
	public boolean isParte(ProcessoTrf processoJudicial, Pessoa pessoa, ProcessoParteParticipacaoEnum polo, TipoParte...tipoParte){
		return processoParteDAO.isParte(processoJudicial, pessoa, null, tipoParte);
	}

	/**
	 * Recupera uma lista com todas as partes sigilosas de um dado processo judicial.
	 * 
	 * @param processo o processo judicial
	 * @param somenteAtivas marca indicativa de que se pretende recuperar somente as partes ativas
	 * @param first indicação do primeiro resultado da lista que se pretende recuperar (nulo para recuperar a partir do primeiro)
	 * @param maxResults indicação do máximo de resultados que se pretende recuperar (nulo para recuperar todos)
	 * @return a lista de partes sigilosas
	 * @throws PJeBusinessException
	 */
	public List<ProcessoParte> recuperaPartesSigilosas(ProcessoTrf processo, boolean somenteAtivas, Integer first, Integer maxResults) throws PJeBusinessException{
		return processoParteDAO.recuperaPartesSigilosas(processo, somenteAtivas, first, maxResults);
	}

	public long contagemPartesSigilosas(ProcessoTrf processo, boolean somenteAtivas) throws PJeBusinessException {
		return processoParteDAO.contagemPartesSigilosas(processo, somenteAtivas);
	}

	/**
	 * Recupera o número de partes existentes no processo.
	 * 
	 * @param processoJudicial o processo a respeito do qual se quer a informação
	 * @param somenteAtivas marca indicativa de que a contagem deve se limitar às partes ativas.
	 * @return o número de partes
	 */
	public long contagemPartes(ProcessoTrf processo, boolean somenteAtivas) {
		return processoParteDAO.contagemPartes(processo, somenteAtivas);
	}

	/**
	 * Recupera uma lista com todas as partes de um dado processo judicial.
	 * 
	 * @param processo o processo judicial
	 * @param somenteAtivas marca indicativa de que se pretende recuperar somente as partes ativas
	 * @param first indicação do primeiro resultado da lista que se pretende recuperar (nulo para recuperar a partir do primeiro)
	 * @param maxResults indicação do máximo de resultados que se pretende recuperar (nulo para recuperar todos)
	 * @return a lista de partes
	 * @throws PJeBusinessException
	 */
	public List<ProcessoParte> recuperaPartes(ProcessoTrf processo, boolean somenteAtivas, Integer first, Integer maxResults) throws PJeBusinessException{
		return processoParteDAO.recuperaPartes(processo, somenteAtivas, first, maxResults);
	}
	
	public boolean visivel(ProcessoParte parte, UsuarioLocalizacao loc, Identity identity) {
		ProcessoTrf processoJudicial = parte.getProcessoTrf();
		if(parte.getParteSigilosa() || processoJudicial.getSegredoJustica()) {
			ProcessoJudicialService processoJudicialService = (ProcessoJudicialService) Component.getInstance(ProcessoJudicialService.class);
			return processoJudicialService.visivel(processoJudicial, loc, identity, true);
		}
		return true;
	}
	
	
	/**
	 * Este método encontra-se descontinuado.
	 * Favor utilizar {@link AtoComunicacaoService#verificarPossibilidadeIntimacaoEletronica(ProcessoParte, boolean)} 
	 */
	@Deprecated
	public boolean temRepresentanteAptoIntimacaoEletronica(ProcessoParte parte, TipoParte tipoRepresentacao){
		if(parte.getInSituacao() != ProcessoParteSituacaoEnum.A){
			return false;
		}
		Search s = new Search(ProcessoParteRepresentante.class);
		addCriteria(s, 
				Criteria.equals("processoParte", parte),
				Criteria.equals("inSituacao", ProcessoParteSituacaoEnum.A),
				Criteria.equals("tipoRepresentante", tipoRepresentacao),
				Criteria.equals("parteRepresentante.inSituacao", ProcessoParteSituacaoEnum.A),
				Criteria.equals("representante.ativo", true),
				Criteria.not(Criteria.isNull("representante.certChain")));
		return count(s) > 0 ? true : false;
	}


	/**
	 * Recupera a lista de partes que estão em situação baixado, inativo ou excluído do processo dado.
	 * 
	 * @param processo o processo de referência
	 * @return a lista de partes excluídas
	 */
	public List<ProcessoParte> recuperaPartesExcluidas(ProcessoTrf processo) {
		Search s = new Search(ProcessoParte.class);
		addCriteria(s, 
				Criteria.equals("processoTrf", processo),
				Criteria.not(Criteria.equals("inSituacao", ProcessoParteSituacaoEnum.A)));
		return list(s);
	}
	
	/**
	 * Exibe a OAB do advogado junto com o nome e CPF. 
	 * @param processoParte parte processual a ser analisada, se for advogado, exibe o CPF junto com o nome.
	 * @return 	Se for advogado: Nome Completo - OAB 9999 - CPF 999.999.999-99
	 * 			Se não for advogado: Nome Completo - CPF 999.999.999-99
	 */
	public String processoParteToString(ProcessoParte processoParte) {
		return recuperaNomeComInformacoesUsadoNoProcesso(processoParte);
	}
	
	/**
	 * Método responsável por recuperar os advogados que atuam nos polos ativo e passivo do processo.
	 * 
	 * @param processoTrf Processo.
	 * @return Lista de advogados ativos que atuam no processo.
	 */
	public List<ProcessoParte> recuperarAdvogados(ProcessoTrf processoTrf) {
		return processoParteDAO.recuperarAdvogados(processoTrf);
	}

	/**
	 * Indica se uma pessoa compõe o processo judicial como uma parte.
	 * 
	 * @param idProcessoTrf o processo judicial a respeito do qual se pretende buscar a informação
	 * @param usuario a pessoa que se pretende identificar como parte
	 * @param idProcuradoria Procuradoria atual do usuário
	 * @return true, se a pessoa for uma parte do tipo dado no processo judicial indicado
	 */
	public boolean isParte(Integer idProcessoTrf, Usuario usuario, Integer idProcuradoria) {
		Boolean resultado = Boolean.FALSE;
		
		if (idProcessoTrf != null && idProcuradoria != null && usuario != null) {
			resultado = processoParteDAO.isParte(idProcessoTrf, usuario, idProcuradoria);
		}
		return resultado;
	}

	/**
	 * Método que verifica se o usuário faz parte do processo nas partes ativas, bem como se ele é procurador
	 * de alguma das partes (se faz parte de alguma procuradoria/defensoria de alguma parte do processo)
	 * 
	 * 
	 * @param processoTRF
	 * @return true se o usuário faz parte do processo
	 * @throws PJeBusinessException
	 */
	public boolean isParte(ProcessoTrf processoTRF) throws PJeBusinessException {
		boolean isParte = isParte(processoTRF, Authenticator.getPessoaLogada());

		if(!isParte){
			isParte = verificaParteProcuradoria(processoTRF, isParte);
		}
		
		return isParte;
	}

	/**
	 * Recuperar as partes que sao representadas por determinado pessoa no processo.
	 * 
	 * @param representante Pessoa que representa a parte
	 * @param processoTrf ProcessoTrf
	 * @return ProcessoPartes
	 */
	public List<ProcessoParte> recuperarRepresentados(Pessoa representante, ProcessoTrf processoTrf) {
		return processoParteDAO.recuperarRepresentados(representante, processoTrf);
	}
	
	/**
	 * Recuperar as Partes do Processo baseado nos parametros passados, a pessoa pode ser null.
	 * 
	 * @param somentePartes True vai restringir os (Advogados, Curadores, Procuradores)
	 * @param processoTrf ProcessoTrf
	 * @param pessoa Pessoa que e representante
	 * @param tipoParte Tipo da Parte 
	 * @param tipoParticipacao Ativo, Passivo ou Todos
	 * @return ProcessoPartes
	 */
	public List<ProcessoParte> recuperar(boolean somentePartes, ProcessoTrf processoTrf, Pessoa pessoa, TipoParte tipoParte,
			ProcessoParteParticipacaoEnum tipoParticipacao) {
		return processoParteDAO.recuperar(somentePartes, processoTrf, pessoa, tipoParte, tipoParticipacao);
	}
	
	/**
	 * Recuperar as Partes do Processo baseado nos parametros passados, a pessoa pode ser null.
	 * 
	 * @param somentePartes True vai restringir os (Advogados, Curadores, Procuradores)
	 * @param processoTrf ProcessoTrf
	 * @param tipoParte Tipo da Parte 
	 * @param tipoParticipacao Ativo, Passivo ou Todos
	 * @return ProcessoPartes
	 */
	public List<ProcessoParte> recuperar(boolean somentePartes, ProcessoTrf processoTrf, TipoParte tipoParte,
			ProcessoParteParticipacaoEnum tipoParticipacao) {
		return processoParteDAO.recuperar(somentePartes, processoTrf, null, tipoParte, tipoParticipacao);
	}
	
	
	/**
	 * Método específico para verificação de parte/procuradoria do usuário logado
	 * 
	 * @param processoTrf
	 * @param isParte
	 * @return
	 * @throws PJeBusinessException
	 */
	private boolean verificaParteProcuradoria(ProcessoTrf processoTrf, boolean isParte) throws PJeBusinessException{
		for(ProcessoParte processoParte : processoTrf.getProcessoParteList()){
			if(processoParte != null && processoParte.getProcuradoria() != null && 
					processoParte.getProcuradoria().equals(Authenticator.getProcuradoriaAtualUsuarioLogado())){
				isParte = true;
				break;
			}
		}
		return isParte;
	}
	
	/**
	 * Altera o Polo ({@link ProcessoParteParticipacaoEnum}) e, caso necessario, o Tipo de Parte ({@link TipoParte})
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-20808
	 * @param processoTrf ProcessoTrf - Processo que possui as Partes a serem invertidas 
	 */
	@Transactional
	public void inverterPolo(ProcessoTrf processoTrf) {
		List<ProcessoParte> partesList = processoTrf.getProcessoParteList();
		ClasseJudicial classeJudicial = processoTrf.getClasseJudicial();
		List<TipoParteConfigClJudicial> tipoPartesConfigClJudicial = ComponentUtil.getComponent(TipoParteConfigClJudicialManager.class).recuperarTipoParteConfiguracao(classeJudicial);
		for (ProcessoParte pp : partesList) {
			for(TipoParteConfigClJudicial  tipoParteConfigClJudicial : tipoPartesConfigClJudicial){
				if(tipoParteConfigClJudicial.getTipoParteConfiguracao() != null && tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte() != null && tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().equals(pp.getTipoParte())){
					if(!pp.getInParticipacao().equals(ProcessoParteParticipacaoEnum.T)){
						TipoParte novoTipoParte = retornarNovoTipoParte(pp, processoTrf, true);
						if(novoTipoParte != null){
							pp.setTipoParte(novoTipoParte);
						}
						ProcessoParteParticipacaoEnum novoPolo = ProcessoParteParticipacaoEnum.A.equals(pp.getInParticipacao()) ? 
								ProcessoParteParticipacaoEnum.P : ProcessoParteParticipacaoEnum.A;
						
						pp.setInParticipacao(novoPolo);
						getDAO().merge(pp);
						break;
					}
				}
			}
		}
		getDAO().flush();
	}

	/**
	 * Retorna o Novo Tipo de Parte ({@link TipoParte}) e, caso nao seja necessario, retorna null
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-20808
	 * @param processoTrf ProcessoTrf - Processo que possui a Parte envolvida
	 * @param processoParte ProcessoParte - parte do processo
	 * @param inverterPolo boolean - flag que verifica se Novo Tipo de Parte sera do Polo Oposto ou nao
	 */
	public TipoParte retornarNovoTipoParte(ProcessoParte processoParte, ProcessoTrf processoTrf, boolean inverterPolo) {
		boolean isMesmoTipoAmbosPolos = isTipoParteAmbos(processoTrf.getClasseJudicial(), processoParte.getTipoParte());
		ClasseJudicial classeJudicial = processoTrf.getClasseJudicial();
		if (!isMesmoTipoAmbosPolos && !ProcessoParteParticipacaoEnum.T.equals(processoParte.getInParticipacao())) {
			if(inverterPolo){
				return ProcessoParteParticipacaoEnum.A.equals(processoParte.getInParticipacao()) ? tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.P) : tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.A);
			} else{
				return tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, processoParte.getInParticipacao());
			}
		}
		return null;
	}
	
	/**
	 * Retorna se o Tipo de Parte e o mesmo em ambos os polos (Ativo e Passivo)
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-20808
	 * @param classe ClasseJudicial - Classe Judicial do Processo
	 * @param tipo TipoParte - Tipo de Parte do Processo 
	 */
	public Boolean isTipoParteAmbos(ClasseJudicial classe, TipoParte tipo) {
		for (TipoParteConfigClJudicial tp : classe.getTipoParteConfigClJudicial()) {
			if (tp.getTipoParteConfiguracao().getTipoParte().equals(tipo)) {
				if(tp.getTipoParteConfiguracao().getOutrosParticipantes()){
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}
	
	/**
	* Recupera uma lista das partes de um dado processo judicial a ser usada em situações de leitura (detached).
	* 
	* @param processo o processo judicial
	* @param excluiInativas marca indicativa de que se pretende excluir as partes inativas
	* @param first indicação do primeiro resultado da lista que se pretende recuperar (nulo para recuperar a partir do primeiro)
	* @param maxResults indicação do máximo de resultados que se pretende recuperar (nulo para recuperar todos)
	* @return a lista de partes
	* @throws PJeBusinessException
	*/
	public List<ProcessoParte> recuperaPartesParaExibicao(ProcessoTrf processo, boolean excluiInativas, Integer first, Integer maxResults) throws PJeBusinessException{
		return recuperaPartesParaExibicao(processo.getIdProcessoTrf(), excluiInativas, first, maxResults);
	}
	
	public List<ProcessoParte> recuperaPartesParaExibicao(Integer idProcesso, boolean excluiInativas, Integer first, Integer maxResults) throws PJeBusinessException{
		return processoParteDAO.recuperaPartesParaExibicao(idProcesso, excluiInativas, first, maxResults, null);
	}

	/**
	 * Método responsável por verificar se o assistente de advogado faz parte do
	 * processo.
	 * 
	 * @param idPapelAdvogado
	 *            id papel advogado
	 * @param idProcesso
	 *            id processo
	 * @param idTipoParte
	 *            id tipo parte advogado
	 * @param idUsuarioLocalizacao
	 *            id usuário localização
	 * 
	 * @return <code>Boolean</code>, <code>true</code> caso faça parte.
	 */
	public boolean isAssistenteAdvogadoProcesso(Integer idPapelAdvogado, Integer idProcesso, Integer idTipoParte, Integer idUsuarioLocalizacao) {
		return processoParteDAO.isAssistenteAdvogadoProcesso(idPapelAdvogado, idProcesso, idTipoParte, idUsuarioLocalizacao);
	}
	
	/**
	 * Método responsável por recuperar a parte ativa do processo.
	 * 
	 * @param processoTrf ProcessoTrf.
	 * @return Lista de ativos que atuam no processo.
	 */
	public List<ProcessoParte> getPartesPoloAtivo(ProcessoTrf processoTrf) {
		return processoTrf.getListaPartePrincipalAtivo();
	}
	
	/**
	 * Método responsável por recuperar a parte passiva do processo.
	 * 
	 * @param processoTrf ProcessoTrf.
	 * @return Lista de passivos que atuam no processo.
	 */
	public List<ProcessoParte> getPartesPoloPassivo(ProcessoTrf processoTrf) {
		return processoTrf.getListaPartePrincipalPassivo();
	}
	
	/**
	 * Ordena as lista de polos ativo ou passivo com a mesma ordenao do cadastro do processo.
	 * 
	 * @param listaProcessoParte List<ProcessoParte> lista com as parte do processo.
	 * @param enumParte ProcessoParteParticipacaoEnum indica o tipo de ordenação Ativo, Passivo e Terceiro. 
	 */
	public void ordenarListaPolosPorOrdem(List<ProcessoParte> listaProcessoParte, ProcessoParteParticipacaoEnum enumParte) {
		if (CollectionUtils.isNotEmpty(listaProcessoParte)) {
			if (enumParte.equals(ProcessoParteParticipacaoEnum.A)) {
				Collections.sort(listaProcessoParte, ProcessoParteOrdenadorEnum.ORDERNAR_POR_ORDEM_POLO);
			}
			
			if (enumParte.equals(ProcessoParteParticipacaoEnum.P)) {
				Collections.sort(listaProcessoParte, ProcessoParteOrdenadorEnum.ORDERNAR_POR_ORDEM_POLO);
			}
		}
	}
	
	/**
	 * Método responsável por retornar todas as partes do processo.
	 * 
	 * @param processoTrf
	 * @return lista de ProcessoParte
	 */
	public List<ProcessoParte> acrescentaParticipantes(ProcessoTrf processoJudicial) {
		List<ProcessoParte> partes = null;
		partes = processoJudicial.getListaPartePrincipal(ProcessoParteParticipacaoEnum.A,
				ProcessoParteParticipacaoEnum.P, ProcessoParteParticipacaoEnum.T);
		return partes;
	}
	
	/**
	 * Método responsável por suprimir as partes inativas.
	 * 
	 * @param lista de ProcessoParte
	 * @return lista de ProcessoParte
	 */
	public List<ProcessoParte> suprimirPartesInativas(List<ProcessoParte> lista){
		List<ProcessoParte> listaSemInativos = new ArrayList<ProcessoParte>();
		for(ProcessoParte parte : lista){
			if(parte.getIsAtivo() && !parte.getIsBaixado()){
				listaSemInativos.add(parte);
			}
		}
		return listaSemInativos;
	}
	
	/**
	 * Método para retornar as partes principais
	 * 
	 * @return lista de ProcessoParte
	 */
	public List<ProcessoParte> recuperaListaPartePrincipalAtivo(ProcessoTrf processoJudicial, boolean exibirPartesInativas) {
		List<ProcessoParte> listaParteAtivo = processoJudicial.getListaPartePrincipal(true, ProcessoParteParticipacaoEnum.A);
		
		if(!exibirPartesInativas){
			listaParteAtivo = suprimirPartesInativas(listaParteAtivo);
		}
		
		return listaParteAtivo; 
	}
	
	/**
	 * Método para retornar as partes Passivas
	 * 
	 * @return lista de ProcessoParte
	 */
	public List<ProcessoParte> recuperaListaPartePrincipalPassivo(ProcessoTrf processoJudicial, boolean exibirPartesInativas) {
		List<ProcessoParte> listaPartePassivo = processoJudicial.getListaPartePrincipal(true, ProcessoParteParticipacaoEnum.P);
		
		if(!exibirPartesInativas){
			listaPartePassivo = suprimirPartesInativas(listaPartePassivo);
		}
		
		return listaPartePassivo; 
	}
	
	/**
	 * Método para retornar as partes de Terceiros
	 * 
	 * @return lista de ProcessoParte
	 */
	public List<ProcessoParte> recuperaListaPartePrincipalTerceiro(ProcessoTrf processoJudicial, boolean exibirPartesInativas) {
		List<ProcessoParte> listaParteTerceiro = processoJudicial.getListaPartePrincipal(true, ProcessoParteParticipacaoEnum.T);
		
		if(!exibirPartesInativas){
			listaParteTerceiro = suprimirPartesInativas(listaParteTerceiro);
		}
		
		return listaParteTerceiro; 
	}
	
	public ProcessoParteMin recuperarProcessoParteMinPorId(Long idProcessoParte){
		return this.processoParteDAO.findProcessoParteMinById(idProcessoParte);
	}
	
	public List<ProcessoParte> recuperaPartesParaExibicao(Integer idProcesso, boolean excluiInativas, Integer first, Integer maxResults, ProcessoParteParticipacaoEnum tipoParticipacao, Boolean somentePartePrincipal) throws PJeBusinessException{
		return processoParteDAO.recuperaPartesParaExibicao(idProcesso, excluiInativas, first, maxResults, tipoParticipacao, somentePartePrincipal);
	}
	
	public List<ProcessoParte> recuperaPartesParaExibicao(Integer idProcesso, boolean excluiInativas, Integer first, Integer maxResults, ProcessoParteParticipacaoEnum tipoParticipacao) throws PJeBusinessException{
		return processoParteDAO.recuperaPartesParaExibicao(idProcesso, excluiInativas, first, maxResults, tipoParticipacao, true);
	}
	
	public boolean possuiParteInativa(ProcessoTrf processoTrf, ProcessoParteParticipacaoEnum inParticipacao) {
		return processoParteDAO.possuiParteInativa(processoTrf, inParticipacao);
	}
	
	public ProcessoParte recuperarParteAdvogadoPoloAtivoSemRepresentando(ProcessoTrf processoTrf) {
		for (ProcessoParte parte : processoTrf.getListaParteAtivo()) {
			if (parte.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado()) && 
					parte.getProcessoParteRepresentanteList2().isEmpty()) {
				
				return parte;
			}
		}
		return null;
	}
	
	/**
	 * Remove o fiscal da lei da lista de processoParte.
	 * 
	 * @param processoTrf ProcessoTrf dados do processo.
	 */
	public void removerFiscalDaLei(ProcessoTrf processoTrf) {
        for (ProcessoParte processoParte : processoTrf.getListaPartePrincipalTerceiro()) {
        	String paramTipoParteFiscalDaLei = ParametroUtil.getParametro(Parametros.VAR_TIPO_PARTE_FISCAL_LEI);        	
            if(processoParte.getTipoParte().getTipoParte().equals(paramTipoParteFiscalDaLei)){
            	ProcessoParteHome.instance().newInstance();
                processoTrf.getProcessoParteList().remove(processoParte);
                EntityUtil.getEntityManager().remove(processoParte);
                EntityUtil.getEntityManager().flush();
            }
        }
    }
    
	public void validarParteExistente(Pessoa pessoa, List<ProcessoParte> listaParteProcesso, TipoParte tipoParte) throws PJeBusinessException {
		for (ProcessoParte processoParte : listaParteProcesso){
			if (processoParte.getPartePrincipal() && processoParte.getPessoa().getIdPessoa().equals(pessoa.getIdPessoa()) && tipoParte.equals(processoParte.getTipoParte())) {
				throw new PJeBusinessException(String.format("Parte já cadastrada (%s, Situação: %s).", processoParte.getNomeParte(), processoParte.getInSituacao().getLabel()));
			}
		}
	}

	/**
	 * Recupera todos os advogados das partes ativa e passiva.
	 * 
	 * @param processoTrf ProcessoTrf dados do processo.
	 * @return List<ProcessoParte> lista com os advogados.
	 */
	public List<ProcessoParte> getListaAdvogadosTodasPartes(ProcessoTrf processoTrf) {
		List<ProcessoParte> listaProcessoParteAdvogado = new ArrayList<>(0);
		listaProcessoParteAdvogado.addAll(processoTrf.getListaAdvogadosPoloAtivo());
		listaProcessoParteAdvogado.addAll(processoTrf.getListaAdvogadosPoloPassivo());
		return listaProcessoParteAdvogado;
	}
	
	/**
	 * Recupera se existir o CPF da parte.
	 * 
	 * @param processoParte
	 *            ProcessoParte
	 * @return String CPF.
	 */
	public String recuperarCpfParte(ProcessoParte processoParte) {
		String retorno = null;
		Set<PessoaDocumentoIdentificacao> listaDocumentos = processoParte.getPessoa()
				.getPessoaDocumentoIdentificacaoList();
		if (!CollectionUtils.isEmpty(listaDocumentos)) {
			for (PessoaDocumentoIdentificacao pdi : listaDocumentos) {
				if ("CPF".equals(pdi.getTipoDocumento().getCodTipo())) {
					retorno = InscricaoMFUtil.retiraMascara(pdi.getNumeroDocumento());
					break;
				}
			}
		}
		return retorno;
	}

	public ProcessoParte criaNovo(ProcessoTrf processoTrf, ProcessoParteParticipacaoEnum participacao, Pessoa pessoa) throws PJeBusinessException {
		ProcessoParte reclamante = new ProcessoParte();
		reclamante.setProcessoTrf(processoTrf);
		reclamante.setInParticipacao(participacao);
		reclamante.setPessoa(pessoa);
		reclamante.setPartePrincipal(Boolean.TRUE);
		reclamante.setTipoParte(this.tipoParteManager.tipoPartePorClasseJudicial(processoTrf.getClasseJudicial(), participacao));
		
		List<Procuradoria> procuradorias = ComponentUtil.getComponent(ProcuradoriaManager.class).getlistProcuradorias(pessoa);
		if (!procuradorias.isEmpty()) {
			reclamante.setProcuradoria(procuradorias.get(0));
		}
		
		return this.persist(reclamante);
	}

	public Integer recuperarIdOrgaoJulgadorPorProcessoParte(ProcessoParte processoParte) {
		return getProcessoParteCache().getIdOrgaoJulgadorPorProcessoParteCache(processoParte.getIdProcessoParte());
	}

	/**
	 * Recupera as partes do processo por participao e tipo pessoa
	 * @param processoTrf
	 * @param participacao
	 * @param tipoPessoa
	 * @return
	 */
	public List<Pessoa> getPartesProcessoPorParticipacaoTipoPessoa(ProcessoTrf processoTrf, ProcessoParteParticipacaoEnum participacao, TipoPessoaEnum tipoPessoa) {
		List<Pessoa> partes = new ArrayList<Pessoa>();
		for (ProcessoParte processoParte : processoTrf.getProcessoParteList()) {
			if ((participacao == null || processoParte.getInParticipacao().equals(participacao)) && (tipoPessoa == null || processoParte.getPessoa().getInTipoPessoa().equals(tipoPessoa))){
					partes.add(processoParte.getPessoa());
			}
		}
		return partes;
	}
	
	/**
	 * Recupera array de partes do processo por participao e tipo pessoa
	 * @param processoTrf
	 * @param participacao
	 * @param tipoPessoa
	 * @return
	 */
	public Pessoa[] getArrayPartesProcessoPorParticipacaoTipoPessoa(ProcessoTrf processoTrf, ProcessoParteParticipacaoEnum participacao, TipoPessoaEnum tipoPessoa) {
		List<Pessoa> partes = getPartesProcessoPorParticipacaoTipoPessoa(processoTrf, participacao, tipoPessoa);
		if(partes != null && !partes.isEmpty())
		{
			return partes.toArray(new Pessoa[partes.size()]);
		}
		return null;
	}

	/**
	 * Recupera o nome usado no processo
	 *
	 * @param processoTrf
	 * @param idPessoa
	 * @return
	 */
	public String recuperaNomeUsadoNoProcesso(Integer idProcessoTrf, Integer idPessoa) {
		ProcessoParte processoParte = getProcessoParteCache().getProcessoParteByProcessoTrfEPessoaCache(idProcessoTrf,
				idPessoa);

		if (processoParte == null) {
			PessoaManager pessoaManager = (PessoaManager) ComponentUtil.getComponent("pessoaManager");

			try {
				return pessoaManager.findById(idPessoa).getNomeParte();
			} catch (PJeBusinessException e) {
				return "";
			}
		}

		if (podeVisualizarNomeDoPolo(processoParte)) {
			return processoParte.getNomeParteSemSegredo();
		}

		return processoParte.getNomeParte();
	}

	/**
	 * Método para retorno dos nomes das partes
	 *
	 * @param pessoa
	 * @return
	 */
	public List<String> recuperaNomesUsadosNoProcesso(ProcessoTrf processoTrf,
			List<ProcessoParteExpediente> processoParteExpedienteList) {
		List<String> nomes = new ArrayList<>();

		for (ProcessoParteExpediente processoParteExpediente : processoParteExpedienteList) {
			nomes.add(recuperaNomeUsadoNoProcesso(processoTrf.getIdProcessoTrf(),
					processoParteExpediente.getPessoaParte().getIdPessoa()));
		}

		return nomes;
	}

	/**
	 * Recupera o nome usado no processo
	 *
	 * @param processoTrf
	 * @param idPessoa
	 * @return
	 */
	public String recuperaNomeUsadoNoProcesso(ProcessoParte processoParte) {
		if (processoParte.getPessoaNomeAlternativo() == null) {
			ProcessoParte processoParteCached = getProcessoParteCache().getProcessoParteByProcessoTrfEPessoaCache(
					processoParte.getProcessoTrf().getIdProcessoTrf(), processoParte.getPessoa().getIdPessoa());

			if (processoParteCached != null) {
				if (podeVisualizarNomeDoPolo(processoParteCached)) {
					return processoParteCached.getNomeParteSemSegredo();
				}

				return processoParteCached.getNomeParte();
			}
		}

		if (podeVisualizarNomeDoPolo(processoParte)) {
			return processoParte.getNomeParteSemSegredo();
		}

		return processoParte.getNomeParte();
	}

	/**
	 * Recupera o nome com informações usado no processo
	 *
	 * @param processoTrf
	 * @param idPessoa
	 * @return
	 */
	public String recuperaNomeComInformacoesUsadoNoProcesso(ProcessoParte processoParte) {
		if (processoParte.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())) {
			PessoaAdvogadoManager pam = (PessoaAdvogadoManager) ComponentUtil.getComponent("pessoaAdvogadoManager");
			String nome = recuperaNomeUsadoNoProcesso(processoParte);
			return pam.getPessoaAdvogado(processoParte.getPessoa().getIdUsuario().intValue()).toString(nome);
		} else {
			if (processoParte.getPessoaNomeAlternativo() == null) {
				ProcessoParte processoParteCached = getProcessoParteCache().getProcessoParteByProcessoTrfEPessoaCache(
						processoParte.getProcessoTrf().getIdProcessoTrf(), processoParte.getPessoa().getIdPessoa());

				if (processoParteCached != null) {
					if (podeVisualizarNomeDoPolo(processoParteCached)) {
						return processoParteCached.toStringSemSegredo();
					}

					return processoParteCached.toString();
				}
			}

			if (podeVisualizarNomeDoPolo(processoParte)) {
				return processoParte.toStringSemSegredo();
			}

			return processoParte.toString();
		}
	}

/**
     * Método responsável por verificar se a pessoa especificada possui representante processual no processo 
     * @param processoTrf Processo do qual a pessoa  parte
     * @param pessoa Pessoa
     * @return Retorna "true" caso a pessoa possua representante processual no processo, e "false" caso contrário
     */
    public boolean isPartePossuiRepresentanteProcessual(ProcessoTrf processoTrf, Pessoa pessoa) {
        ProcessoParte parte = findProcessoParte(processoTrf, null, pessoa);

        return 
            (parte != null && 
                (
                    parte.getProcuradoria() != null ||
                    temRepresentanteAptoIntimacaoEletronica(parte, ParametroUtil.instance().getTipoParteAdvogado())
                )
            );
    }
	
	public List<ProcessoParte> findByPessoa(int idPessoa){
		Search s = new Search(ProcessoParte.class);
		addCriteria(s, Criteria.equals("idPessoa", idPessoa));
		return list(s);	
	}
}
