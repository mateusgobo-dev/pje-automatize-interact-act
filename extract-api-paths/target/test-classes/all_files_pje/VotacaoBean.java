/**
 * 
 */
package br.jus.cnj.pje.visao.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import br.com.jt.pje.manager.DerrubadaVotoManager;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.cnj.pje.vo.PlacarSessaoVO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.enums.PJeEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoDocumentoEnum;
import org.jboss.seam.contexts.Contexts;

/**
 * Bean de controle de votação.
 * 
 * @author cristof
 *
 */
public class VotacaoBean {

	public Map<Integer, String> getMapaCores() {
		return mapaCores;
	}

	public void setMapaCores(Map<Integer, String> mapaCores) {
		this.mapaCores = mapaCores;
	}

	public enum ObjetoVoto implements PJeEnum{
		/**
		 * 
		 */
		EMENTA ("Ementa", 'E'),
		RELATORIO ("Relatório", 'R'),
		VOTO ("Voto", 'V'),
		NOTAS_ORAIS ("Notas orais", 'N');
		
		private String label;
		
		private char codigo;
		
		private ObjetoVoto(String label, char codigo) {
			this.label = label;
			this.codigo = codigo;
		}

		@Override
		public String getLabel() {
			return this.label;
		}
		
		public char getCodigo() {
			return codigo;
		}
	}

	private boolean relator;
	
	private boolean redigir;
	
	private boolean liberar;
	
	private DocumentoJudicialService documentoJudicialService;
	
	private ProcessoTrf processo;
	
	private OrgaoJulgador orgaoAtual;
	
	private OrgaoJulgador orgaoRelator;
	
	private OrgaoJulgadorColegiado orgaoColegiado;
	
	private Map<Integer, Set<Integer>> placar;
	
	private Set<Integer> impedidos;
	
	private Set<Integer> omissos;
	
	private Set<Integer> divergentes;
	
	private Set<Integer> listaTipoVotoNaoConhece;
	
	private Map<String, TipoProcessoDocumento> tiposDocumentos;
	
	private Sessao sessao;
	
	private SessaoPautaProcessoTrf julgamento;
	
	private SessaoProcessoDocumentoVoto voto;
	
	private SessaoProcessoDocumentoVotoManager votosManager;
	
	private SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager;
	
	private TipoVotoManager tipoVotoManager;
	
	private Map<String, SessaoProcessoDocumento> objetosVotacao;
	
	private List<SessaoProcessoDocumento> objetosJuntados;

	private Map<Integer,String> mapaCores;
	
	
	private DerrubadaVotoManager derrubadaVotoManager;
	
	public VotacaoBean(ProcessoTrf processo, OrgaoJulgador orgaoAtual, Sessao sessao, SessaoPautaProcessoTrf julgamento,
			boolean liberar, SessaoProcessoDocumentoVotoManager votosManager, SessaoProcessoDocumentoManager spdManager,
			Map<String, TipoProcessoDocumento> tiposDocumentos, TipoVotoManager tipoVotoManager, 
			DocumentoJudicialService documentoJudicialService, DerrubadaVotoManager derrubadaVotoManager) {
		this.processo = processo;
		this.sessao = sessao;
		this.julgamento = julgamento;
		this.orgaoAtual = orgaoAtual;
		this.orgaoRelator = processo.getOrgaoJulgador();
		this.orgaoColegiado = processo.getOrgaoJulgadorColegiado();
		this.votosManager = votosManager;
		this.sessaoProcessoDocumentoManager = spdManager;
		this.documentoJudicialService = documentoJudicialService;
		this.tiposDocumentos = tiposDocumentos;
		this.tipoVotoManager = tipoVotoManager;
		this.relator = orgaoAtual != null ? orgaoAtual.equals(orgaoRelator) : false;
		this.liberar = liberar;
		this.impedidos = new HashSet<Integer>();
		this.derrubadaVotoManager = derrubadaVotoManager;
	}
	
	public SessaoProcessoDocumentoVoto getVoto() {
		if(voto == null){
			carregarVoto();
		}
		return voto;
	}
	
	private void carregarVoto(){
		voto = votosManager.recuperarVoto(sessao, processo, orgaoAtual);
		if(voto == null){
			voto = new SessaoProcessoDocumentoVoto();
			voto.setSessao(sessao);
			voto.setCheckAcompanhaRelator(relator);
			voto.setDestaqueSessao(false);
			voto.setImpedimentoSuspeicao(false);
			voto.setProcessoTrf(processo);
			voto.setOrgaoJulgador(orgaoAtual);

			ajustarLiberacao();
		}
	}
	
	public void acompanharRelator() throws PJeBusinessException{
		acompanharRelator(true);
	}
	
	public void acompanharRelator(boolean integralmente) throws PJeBusinessException{
		if(relator){
			throw new PJeBusinessException("Não é possível realizar acompanhamento de si próprio");
		}
		
		if(integralmente){
			votar(orgaoRelator, tipoVotoManager.recuperaAcompanhaRelator());
		} else {
			votar(orgaoAtual, tipoVotoManager.recuperaAcompanhaParteRelator());
		}
	}
	
	/**
	 * Metodo responsável por incluir o tipo de voto não conhece ao processo.
	 * 
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-17870
	 * @throws PJeBusinessException
	 */
	public void incluirTipoVotoNaoConhece() throws PJeBusinessException{
		if(relator){
			throw new PJeBusinessException("Não é possível realizar acompanhamento de si próprio");
		}
		votar(orgaoRelator, tipoVotoManager.recuperaNaoConhece());
	}
	
	public void abrirDivergencia() throws PJeBusinessException{
		if(relator){
			throw new PJeBusinessException("Não é possível abrir divergência de relatório próprio");
		}
		TipoVoto tipoVoto = tipoVotoManager.recuperaTipoDivergente();
		votar(orgaoAtual, tipoVoto);
	}

	public void votar(TipoVoto tipoVoto) throws PJeBusinessException {
		if(relator){
			throw new PJeBusinessException("Não é possível abrir divergência de relatório próprio");
		}
		if(tipoVoto == null){
			throw new PJeBusinessException("Não é possível votar sem indicação do tipo de voto");
		}
		if(tipoVoto.getContexto().equals("C")){
			votar(orgaoRelator, tipoVoto);
		}
		else{
			votar(orgaoAtual, tipoVoto);
		}

	}
	
	public void acompanharDivergencia(OrgaoJulgador acompanhado) throws PJeBusinessException{
		if(relator){
			throw new PJeBusinessException("Não é possível acompanhar divergência como relator do processo");
		}
		SessaoProcessoDocumentoVoto votoAcompanhado = votosManager.recuperarVoto(sessao, processo, acompanhado);
		if(votoAcompanhado == null){
			throw new PJeBusinessException("Não é possível acompanhar voto não existente.");
		}
		votar(acompanhado, votoAcompanhado.getTipoVoto());
	}
	
	public void redigirVoto() throws PJeBusinessException{
		this.redigir = true;
		ProcessoDocumento doc = getVoto().getProcessoDocumento();
		if(doc == null){
			TipoProcessoDocumento tipoDocumentoVoto = getTipoDocumento(ObjetoVoto.VOTO);
			doc = documentoJudicialService.getNovoDocumento(" ");
			doc.setProcessoDocumento(tipoDocumentoVoto.getTipoProcessoDocumento());
			doc.setTipoProcessoDocumento(tipoDocumentoVoto);
			doc.setProcesso(processo.getProcesso());
			doc.setProcessoTrf(processo);
			documentoJudicialService.persist(doc, true);
			documentoJudicialService.flush();
			voto.setProcessoDocumento(doc);
			votosManager.persistAndFlush(voto);
		}
	}
	
	public void removerConteudo() throws PJeBusinessException{
		this.redigir = false;
		ProcessoDocumento doc = getVoto().getProcessoDocumento();
		getVoto().setProcessoDocumento(null);
		if(doc != null){
			documentoJudicialService.remove(doc);
			documentoJudicialService.flush();
		}
		sessaoProcessoDocumentoManager.persistAndFlush(getVoto());
	}
	
	public void redigirEmenta() throws PJeBusinessException{
		redigirDocumento(ObjetoVoto.EMENTA.getLabel(), true);
	}
	
	public void redigirRelatorio() throws PJeBusinessException{
		redigirDocumento(ObjetoVoto.RELATORIO.getLabel(), true);
	}
	
	public void redigirNotasOrais() throws PJeBusinessException{
		redigirDocumento(ObjetoVoto.NOTAS_ORAIS.getLabel(), false);
	}
	
	private void redigirDocumento(String tipoObjeto, boolean apenasRelator) throws PJeBusinessException{
		if(!relator && apenasRelator){
			throw new PJeBusinessException("Aparentemente você não pertence ao órgão relator do processo.");
		}
		SessaoProcessoDocumento objeto = getObjetosVotacao().get(tipoObjeto);
		if(objeto == null){
			objeto = new SessaoProcessoDocumento();
			objeto.setOrgaoJulgador(orgaoAtual);
			objeto.setSessao(sessao);
			objeto.setTipoInclusao(TipoInclusaoDocumentoEnum.S);
			ProcessoDocumento doc = documentoJudicialService.getDocumento();
			TipoProcessoDocumento td = getTipoDocumento(tipoObjeto);
			if(td == null){
				throw new PJeBusinessException("Erro ao tentar recuperar o tipo de documento para o objeto " + tipoObjeto);
			}
			sessaoProcessoDocumentoManager.persistAndFlush(objeto);
			doc.setProcessoDocumento(td.getTipoProcessoDocumento());
			doc.setTipoProcessoDocumento(td);
			doc.setProcesso(processo.getProcesso());
			doc.setProcessoTrf(processo);
			objeto.setProcessoDocumento(doc);
			getObjetosVotacao().put(ObjetoVoto.RELATORIO.getLabel(), objeto);
		}
	}

	public void removerVoto() throws PJeBusinessException{
		ProcessoDocumento doc = getVoto().getProcessoDocumento();
		if(doc != null){
			getVoto().setProcessoDocumento(null);
			votosManager.persistAndFlush(getVoto());
			documentoJudicialService.remove(doc);
		}
	}
	
	public Map<Integer, Set<Integer>> getPlacar() {
		carregarPlacar(false);
		return placar;
	}
	
	public Map<Integer, Set<Integer>> getDivergentes() {
		Map<Integer, Set<Integer>> ret = new HashMap<Integer, Set<Integer>>(getPlacar());
		ret.remove(orgaoRelator.getIdOrgaoJulgador());
		ret.remove(-1);
		ret.remove(-2);
		return ret;
	}
	
	/**
	 * Metodo responsável por retorna uma lista apenas com os órgãos julgadores 
	 * que votaram com o tipo de voto não conhece.
	 * 
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-17870
	 * @return Map<Integer, Set<Integer>> 
	 */
	public Map<Integer, Set<Integer>> getListaTipoVotoNaoConhece() {
		Map<Integer, Set<Integer>> ret = new HashMap<Integer, Set<Integer>>(getPlacar());
		ret.remove(orgaoRelator.getIdOrgaoJulgador());
		ret.remove(-1);
		ret.remove(-2);
		ret.remove(-3);
		return ret;
	}

	private void carregarPlacar(boolean reload){
		if(placar == null || reload){
			PlacarSessaoVO p = votosManager.getPlacarCondutores(sessao, processo, true);
			placar = p.getMapaPlacar();
			mapaCores = p.getMapaCor();
			impedidos = votosManager.getImpedidos(sessao, processo, true);
			omissos = votosManager.getOmissos(sessao, processo, true);
			omissos.removeAll(impedidos);
			placar.put(-1, omissos);
			placar.put(-2, impedidos);
		}
	}


	private void votar(OrgaoJulgador acompanhado, TipoVoto tipoVoto) throws PJeBusinessException{
		getVoto().setCheckAcompanhaRelator(acompanhado.equals(orgaoRelator));
		getVoto().setDtVoto(new Date());
		getVoto().setTipoVoto(tipoVoto);
		getVoto().setOjAcompanhado(acompanhado);
		getVoto().setImpedimentoSuspeicao(false);
		ajustarLiberacao();
		sessaoProcessoDocumentoManager.persistAndFlush(getVoto());

		if (derrubadaVotoManager != null) {
			derrubadaVotoManager.analisarTramitacaoFluxoVotoDerrubado(getVoto());
		}	

		carregarPlacar(true);
	}
	
	private void ajustarLiberacao(){
		if(liberar){
			getVoto().setLiberacao(true);
		}
	}
	
	private void carregarObjetos(boolean reload){
		if(objetosVotacao == null || reload){
			List<SessaoProcessoDocumento> elementos = sessaoProcessoDocumentoManager
					.recuperaElementosJulgamento(processo, sessao, orgaoAtual, true, true);
			objetosVotacao = new HashMap<String, SessaoProcessoDocumento>(elementos.size());
			objetosJuntados = new ArrayList<SessaoProcessoDocumento>();
			for(SessaoProcessoDocumento e: elementos){
				if(e.getProcessoDocumento() != null && e.getProcessoDocumento().getDataJuntada() != null){
					objetosJuntados.add(e);
				}
				if(e.getProcessoDocumento() != null){
					String name = reversedName(e.getProcessoDocumento().getTipoProcessoDocumento());
					if(name != null){
						if(name.toLowerCase().startsWith(ObjetoVoto.VOTO.label.toLowerCase())){
							name = "VOTO" + e.getOrgaoJulgador().getIdOrgaoJulgador();
						}
						objetosVotacao.put(name, e);
					}
				}
			}
		}
	}
	
	private String reversedName(TipoProcessoDocumento tipo){
		for(Entry<String, TipoProcessoDocumento> e: tiposDocumentos.entrySet()){
			if(e.getValue().equals(tipo)){
				return e.getKey();
			}
		}
		return null;
	}
	
	private TipoProcessoDocumento getTipoDocumento(ObjetoVoto tipoObjeto){
		return getTipoDocumento(tipoObjeto.getLabel());
	}
	
	private TipoProcessoDocumento getTipoDocumento(String tipo){
		for(String obj: tiposDocumentos.keySet()){
			if(tipo.equalsIgnoreCase(obj)){
				return tiposDocumentos.get(obj);
			}
		}
		return null;
	}
	
	public void gravar() throws PJeBusinessException{
		votosManager.persistAndFlush(voto);
	}
	
	public boolean isRedigir() {
		return redigir;
	}
	
	public boolean isRelator() {
		return relator;
	}
	
	public OrgaoJulgadorColegiado getOrgaoColegiado() {
		return orgaoColegiado;
	}
	
	public OrgaoJulgador getOrgaoAtual() {
		return orgaoAtual;
	}
	
	public OrgaoJulgador getOrgaoRelator() {
		return orgaoRelator;
	}
	
	public Map<String, SessaoProcessoDocumento> getObjetosVotacao() {
		carregarObjetos(true);
		return objetosVotacao;
	}
	
	public List<SessaoProcessoDocumento> getObjetosJuntados() {
		carregarObjetos(false);
		return objetosJuntados;
	}

	public void inverterImpedimento() throws PJeBusinessException {
		getVoto().setImpedimentoSuspeicao(!getVoto().getImpedimentoSuspeicao());
		getVoto().setOjAcompanhado(orgaoAtual);
		getVoto().setTipoVoto(tipoVotoManager.recuperaImpedido());
		sessaoProcessoDocumentoManager.persistAndFlush(getVoto());
		carregarPlacar(true);
	}
	
	public SessaoPautaProcessoTrf getJulgamento() {
		return julgamento;
	}
	
	public ProcessoTrf getProcesso() {
		return processo;
	}
	
}
