package br.jus.cnj.pje.distribuicao;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.jbpm.taskmgmt.exe.SwimlaneInstance;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.EventoHome;
import br.com.infox.ibpm.home.ParametroHome;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.assignment.LocalizacaoAssignment;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.service.DefinicaoCompetenciaService;
import br.jus.cnj.pje.servicos.DistribuicaoService;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.CompetenciaClasseAssunto;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCompetencia;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;
import br.jus.pje.nucleo.enums.ClasseJudicialInicialEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;

/**
 * Componente Seam destinado a permitir a distribuição aleatória de processos judiciais.
 * 
 * @author cristof
 *
 */
@Name("distribuidorAleatorio")
public class DistribuidorAleatorio {

	private static final Integer NUM_TOTAL = 100;
	
	private static SecureRandom rnd = new SecureRandom();
	
	@Logger
	private Log logger;
	
	@In(create=true)
	private DistribuicaoService distribuicaoService;
	
	@In(create=true)
	private EntityManager entityManager;
	
	@In(create=true)
	private ProcessoHome processoHome;
	
    @In(create=true)
    private ProcessoTrfHome processoTrfHome;	
	
	private List<Jurisdicao> jurisdicoes;
	
	private List<Pessoa> pessoas;
	
	private List<PessoaAdvogado> advogados;
	
	private ProcessoDocumento documentoBase;
	private ProcessoDocumentoBin documentoBaseBin;
	private ProcessoDocumentoBinPessoaAssinatura documentoProcessoAssinaturaBase;
	private ProcessoDocumento documentoExtra;
	private ProcessoDocumentoBin documentoExtraBin;
	private ProcessoDocumentoBinPessoaAssinatura documentoProcessoAssinaturaExtra;
	private Integer quantidadeDeProcessos;
	
	public Integer getQuantidadeDeProcessos() {
		return quantidadeDeProcessos;
	}


	public void setQuantidadeDeProcessos(Integer quantidadeDeProcessos) {
		this.quantidadeDeProcessos = quantidadeDeProcessos;
	}


	@Create
	public void create(){
		this.quantidadeDeProcessos = new Integer(25);
		carregaPessoas();
		carregaJurisdicoes();
		carregaAdvogados();
	}

	
	public void criarProcessos(int n){
		if (quantidadeDeProcessos!=null&&quantidadeDeProcessos>0){
			n=quantidadeDeProcessos;
		}
		entityManager =  EntityUtil.getEntityManager();
		int cont = 0;
		
		for(int i = 0; i < n; i++){
			if((i%10) == 0 && i > 0)
				entityManager.flush();
			ProcessoTrf p = criarProcesso();
			
			entityManager.persist(p.getProcesso().getProcessoDocumentoList().get(0).getProcessoDocumentoBin());
			entityManager.persist(obtemAssinaturaProcessoDocumento(p.getProcesso().getProcessoDocumentoList().get(0)));
			
			for (int j = 1; j < p.getProcesso().getProcessoDocumentoList().size(); j++) {
				entityManager.persist(p.getProcesso().getProcessoDocumentoList().get(j).getProcessoDocumentoBin());
				entityManager.persist(obtemAssinaturaProcessoDocumento(p.getProcesso().getProcessoDocumentoList().get(j), documentoProcessoAssinaturaExtra));
			}
			
			entityManager.persist(p.getProcesso());
			//entityManager.persist(p.getProcesso().getProcessoDocumentoList().get(0));
			
			for (int j = 0; j < p.getProcesso().getProcessoDocumentoList().size(); j++) {
				entityManager.persist(p.getProcesso().getProcessoDocumentoList().get(0));
			}
			
			p.setIdProcessoTrf(p.getProcesso().getIdProcesso());
			entityManager.persist(p);
			
			for(ProcessoParte pp: p.getProcessoParteList()){
				entityManager.persist(pp);
				for(ProcessoParteRepresentante ppr: pp.getProcessoParteRepresentanteList()){
					ppr.setProcessoParte(pp);
					entityManager.persist(ppr);
				}
			}
			
			for (ProcessoAssunto pa :p.getProcessoAssuntoList()){
				pa.setProcessoTrf(p);
				entityManager.persist(pa);
			}
			
			EntityUtil.flush();
			
			try {
				this.distribuir(p);
				cont++;
			} catch (Exception e) {
				logger.error("Erro ao distribuir o processo [" + i + "].");
				e.printStackTrace();
			}
			EntityUtil.flush();
		}
		logger.error("Distribuído(s) {0} processo(s)", cont);
	}
	
	public void distribuir(ProcessoTrf p) throws Exception{
		DefinicaoCompetenciaService dcs = (DefinicaoCompetenciaService) Component.getInstance(DefinicaoCompetenciaService.class);
		List<Competencia> comps = dcs.getCompetencias(p);
		Competencia c = null;
		if(comps.isEmpty()){
			logger.error("Não há competências disponíveis.");
			return;
		}else{
			SecureRandom rnd = new SecureRandom();
			c = comps.get(rnd.nextInt(comps.size()));
		}
		distribuicaoService.distribuirProcesso(p, c);
		this.numerarProcesso(p);
		p.getProcesso().setNumeroProcesso(p.getNumeroProcesso());

		Fluxo fluxo = p.getClasseJudicial().getFluxo();
		Map<String, Object> variaveis = new HashMap<String, Object>();
		variaveis.put("orgaoJulgador", p.getOrgaoJulgador().getIdOrgaoJulgador());
		

		processoHome.setInstance(p.getProcesso());
		processoTrfHome.setInstance(p);
		processoHome.adicionarFluxo(fluxo, variaveis);
		SwimlaneInstance swimlaneInstance = org.jboss.seam.bpm.TaskInstance.instance().getSwimlaneInstance();
		String actorsExpression = swimlaneInstance.getSwimlane().getPooledActorsExpression();
		LocalizacaoAssignment.instance().setPooledActors(actorsExpression);
		try {
			EventoHome.instance().registraCodigoCNJ(p.getProcesso(),"26");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void criarProcessos(){
		criarProcessos(NUM_TOTAL);
	}

	private ProcessoTrf criarProcesso(){
		
		int num = rnd.nextInt(23);
		Jurisdicao jurisdicao = obtemJurisdicao();
		ProcessoTrf processo = new ProcessoTrf();
		this.atribuiClasseAssuntoJurisdicao(jurisdicao, processo);
		carregaDocumentoInicial(processo.getClasseJudicial());
		carregaDocumentoExtra(processo.getClasseJudicial());
		this.atribuiPartes(processo);
		processo.setAno((new GregorianCalendar(2011, 4, 1)).get(GregorianCalendar.YEAR));
		processo.setTutelaLiminar((num % 2) == 0 ? Boolean.TRUE : Boolean.FALSE);
		processo.setApreciadoTutelaLiminar(Boolean.FALSE);
		processo.setApreciadoSegredo(ProcessoTrfApreciadoEnum.N);
		processo.setApreciadoSigilo(ProcessoTrfApreciadoEnum.N);
		processo.setDataAutuacao(new Date());
		processo.setDataDistribuicao(new Date());
		processo.setInicial(ClasseJudicialInicialEnum.I);
		processo.setIsIncidente(Boolean.FALSE);
		processo.setJusticaGratuita(Boolean.FALSE);
		processo.setValorCausa(rnd.nextDouble() * 30000D);
		processo.setLocalizacaoInicial(Authenticator.getLocalizacaoFisicaAtual());
		processo.setEstruturaInicial(Authenticator.getLocalizacaoModeloAtual());
		
		Processo proc = new Processo();
		proc.setUsuarioCadastroProcesso(ParametroUtil.instance().getUsuarioSistema());
		proc.setDataInicio(new Date());
		proc.setNumeroProcesso("");
		
		processo.setProcesso(proc);
		proc.getProcessoDocumentoList().add(obtemDocumentoNovo(proc));
		
		// adiciona documentos extras
		int rand = (int) (Math.random() * 8 + 2);
		
		for (int i = 2; i <= rand; i++)
		{
			proc.getProcessoDocumentoList().add(obtemDocumentoNovo(proc, documentoExtra, String.valueOf(i)));
		}
		
		return processo;
	}
	
	private void numerarProcesso(ProcessoTrf processo){
		String numeroOrgaoJustica = ParametroHome.getFromContext("numeroOrgaoJustica", true);
		Integer numeroOrigem = null;
		if(ParametroUtil.instance().getTipoJustica().equalsIgnoreCase("JT") 
				|| ParametroUtil.instance().getTipoJustica().equalsIgnoreCase("JE")
				|| ParametroUtil.instance().getTipoJustica().equalsIgnoreCase("JM")){
			numeroOrigem = Integer.parseInt(processo.getOrgaoJulgador().getCodigoOrigem().trim());
		}else{
			numeroOrigem = processo.getJurisdicao().getNumeroOrigem();
		}
		NumeroProcessoUtil.numerarProcesso(processo, Integer.parseInt(numeroOrgaoJustica), numeroOrigem);
	}
	
	@SuppressWarnings("unchecked")
	private void carregaPessoas(){
		String query = "SELECT p FROM Pessoa AS p WHERE p.ativo = true";
		Query q = entityManager.createQuery(query);
		q.setMaxResults(200);
		pessoas = (List<Pessoa>) q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	private void carregaAdvogados(){
		String query = "SELECT p FROM PessoaAdvogado p WHERE p.ativo = true";
		Query q = entityManager.createQuery(query);
		q.setMaxResults(20);
		advogados = (List<PessoaAdvogado>) q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	private void carregaJurisdicoes(){
		String query = "SELECT j FROM Jurisdicao AS j";
		Query q = entityManager.createQuery(query);
		jurisdicoes = (List<Jurisdicao>) q.getResultList();
		return;
	}
	
	@SuppressWarnings("unchecked")
	private void carregaDocumentoInicial(ClasseJudicial classeJudicial){
		String query = "SELECT d FROM ProcessoDocumento AS d WHERE d.dataJuntada IS NOT NULL AND d.tipoProcessoDocumento.idTipoProcessoDocumento = :tipo and d.processoDocumentoBin.valido = true order by random()";
		
		// [PJEII-1243] Padronizado para recuperar o id do TipoProcessoDocumento configurado na tabela de parâmetros como Petição inicial
		int idTipoProcessoDocumentoPeticaoInicial = classeJudicial.getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento();

		Query q = entityManager.createQuery(query);
		q.setParameter("tipo", idTipoProcessoDocumentoPeticaoInicial);
		q.setMaxResults(1);
		documentoBase = (ProcessoDocumento) q.getSingleResult();
		documentoBaseBin = documentoBase.getProcessoDocumentoBin();
		documentoProcessoAssinaturaBase = documentoBaseBin.getSignatarios().get(0);
	}
	
	@SuppressWarnings("unchecked")
	private void carregaDocumentoExtra(ClasseJudicial classeJudicial){
		// [PJEII-1243] Padronizado para recuperar o id do TipoProcessoDocumento configurado na tabela de parâmetros como Petição inicial
		int idTipoProcessoDocumentoPeticaoInicial = classeJudicial.getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento();
		String query = "SELECT d FROM ProcessoDocumento AS d WHERE d.dataJuntada IS NOT NULL AND d.tipoProcessoDocumento.idTipoProcessoDocumento != :tipo and d.processoDocumentoBin.valido = true order by random()";
		Query q = entityManager.createQuery(query);
		q.setParameter("tipo", idTipoProcessoDocumentoPeticaoInicial);
		q.setMaxResults(1);
		documentoExtra = (ProcessoDocumento) q.getSingleResult();
		documentoExtraBin = documentoExtra.getProcessoDocumentoBin();
		documentoProcessoAssinaturaExtra = documentoExtraBin.getSignatarios().get(0);
	}
	
	private ProcessoDocumento obtemDocumentoNovo(Processo p){
		return obtemDocumentoNovo(p, documentoBase, "1");
		/*
		ProcessoDocumento pd = new ProcessoDocumento();
		pd.setProcesso(p);
		pd.setDataInclusao(new Date());
		pd.setAtivo(Boolean.TRUE);
		pd.setDocumentoSigiloso(Boolean.FALSE);
		pd.setLocalizacao(documentoBase.getLocalizacao());
		pd.setNumeroDocumento(1);
		pd.setPapel(documentoBase.getPapel());
		pd.setProcessoDocumento(documentoBase.getProcessoDocumento());
		pd.setTipoProcessoDocumento(documentoBase.getTipoProcessoDocumento());
		pd.setUsuarioInclusao(documentoBase.getUsuarioInclusao());
		
		ProcessoDocumentoBin pdb = new ProcessoDocumentoBin();
		pdb.setCertChain(documentoBase.getProcessoDocumentoBin().getCertChain());
		pdb.setDataInclusao(new Date());
		pdb.setExtensao(documentoBase.getProcessoDocumentoBin().getExtensao());
		pdb.setMd5Documento(documentoBase.getProcessoDocumentoBin().getMd5Documento());
		pdb.setModeloDocumento(documentoBase.getProcessoDocumentoBin().getModeloDocumento());
		pdb.setNomeArquivo(documentoBase.getProcessoDocumentoBin().getNomeArquivo());
		pdb.setSignature(documentoBase.getProcessoDocumentoBin().getSignature());
		pdb.setSize(documentoBase.getProcessoDocumentoBin().getSize());
		pdb.setUsuario(documentoBase.getProcessoDocumentoBin().getUsuario());
		pd.setProcessoDocumentoBin(pdb);
		
		return pd;*/
	}
	
	private ProcessoDocumento obtemDocumentoNovo(Processo p, ProcessoDocumento documentoOrigem, String numero){
		ProcessoDocumento pd = new ProcessoDocumento();
		pd.setProcesso(p);
		pd.setDataInclusao(new Date());
		pd.setAtivo(Boolean.TRUE);
		pd.setDocumentoSigiloso(Boolean.FALSE);
		pd.setLocalizacao(documentoOrigem.getLocalizacao());
		pd.setNumeroDocumento(numero);
		pd.setPapel(documentoOrigem.getPapel());
		pd.setProcessoDocumento(documentoOrigem.getProcessoDocumento());
		pd.setTipoProcessoDocumento(documentoOrigem.getTipoProcessoDocumento());
		pd.setUsuarioInclusao(documentoOrigem.getUsuarioInclusao());
		
		ProcessoDocumentoBin pdb = new ProcessoDocumentoBin();
		pdb.setCertChain(documentoOrigem.getProcessoDocumentoBin().getCertChain());
		pdb.setDataInclusao(new Date());
		pdb.setExtensao(documentoOrigem.getProcessoDocumentoBin().getExtensao());
		pdb.setModeloDocumento(documentoOrigem.getProcessoDocumentoBin().getModeloDocumento());
		pdb.setNomeArquivo(documentoOrigem.getProcessoDocumentoBin().getNomeArquivo());
		pdb.setSignature(documentoOrigem.getProcessoDocumentoBin().getSignature());
		pdb.setSize(documentoOrigem.getProcessoDocumentoBin().getSize());
		pdb.setUsuario(documentoOrigem.getProcessoDocumentoBin().getUsuario());
		pd.setProcessoDocumentoBin(pdb);
		
		return pd;
	}
	
	private ProcessoDocumentoBinPessoaAssinatura obtemAssinaturaProcessoDocumento(ProcessoDocumento pd){
		return obtemAssinaturaProcessoDocumento(pd, documentoProcessoAssinaturaBase);
	}
	private ProcessoDocumentoBinPessoaAssinatura obtemAssinaturaProcessoDocumento(ProcessoDocumento pd, ProcessoDocumentoBinPessoaAssinatura documentoProcessoAssinaturaOrigem){
		ProcessoDocumentoBinPessoaAssinatura pdbpa = new ProcessoDocumentoBinPessoaAssinatura();
		pdbpa.setProcessoDocumentoBin(pd.getProcessoDocumentoBin());
		pdbpa.setAssinatura(documentoProcessoAssinaturaOrigem.getAssinatura());
		pdbpa.setCertChain(documentoProcessoAssinaturaOrigem.getCertChain());
		pdbpa.setDataAssinatura(documentoProcessoAssinaturaOrigem.getDataAssinatura());
		pdbpa.setNomePessoa(documentoProcessoAssinaturaOrigem.getNomePessoa());
		pdbpa.setPessoa(documentoProcessoAssinaturaOrigem.getPessoa());
		return pdbpa;
	}
	
	private Jurisdicao obtemJurisdicao(){
		return jurisdicoes.get(rnd.nextInt(jurisdicoes.size()));
	}
	
	private Pessoa obtemPessoa(Integer id){
		int selecionado;
		Pessoa p = null;
		do{
			selecionado = rnd.nextInt(pessoas.size());
			p = pessoas.get(selecionado);
		}while(!p.getIdUsuario().equals(id) && 
					(Pessoa.instanceOf(p, PessoaAdvogado.class) || 
							Pessoa.instanceOf(p, PessoaServidor.class) || 
							Pessoa.instanceOf(p, PessoaMagistrado.class)));
		return p;
	}
	
	private PessoaAdvogado obtemAdvogado(Integer id){
		int selecionado;
		PessoaAdvogado p = null;
		if(advogados.size()<=0) {
			carregaAdvogados();
		} 
		
		if (id==-1){
			selecionado = rnd.nextInt(advogados.size());
			p = advogados.get(selecionado);
		} else {
			for(PessoaAdvogado pessoaAdvogado: advogados){
				if (pessoaAdvogado.getIdUsuario() != null 
						&& pessoaAdvogado.getIdUsuario().equals(id)){
					p=pessoaAdvogado;
				}
			}
		}
		return p;
	}
	
	private void atribuiPartes(ProcessoTrf processo){
		ProcessoParte pAtiva = new ProcessoParte();
		ProcessoParte pPassiva = new ProcessoParte();
		TipoParte tipoParteAtivo = null;
		TipoParte tipoPartePassivo = null;
		
		pAtiva.setCheckado(Boolean.TRUE);
		pAtiva.setCheckVisibilidade(Boolean.TRUE);
		pAtiva.setInParticipacao(ProcessoParteParticipacaoEnum.A);
		pAtiva.setInSituacao(ProcessoParteSituacaoEnum.A);
		pAtiva.setIsEnderecoDesconhecido(Boolean.FALSE);
		pAtiva.setPartePrincipal(Boolean.TRUE);
		pAtiva.setParteSigilosa(Boolean.FALSE);
		pAtiva.setPessoa(obtemPessoa(-1));
		pAtiva.setProcessoTrf(processo);
		
		tipoParteAtivo = definirTipoParte(processo, tipoParteAtivo);
		pAtiva.setTipoParte(tipoParteAtivo);
		
		processo.getProcessoParteList().add(pAtiva);
		this.atribuiAdvogado(processo, pAtiva, -1);
		
		pPassiva.setCheckado(Boolean.TRUE);
		pPassiva.setCheckVisibilidade(Boolean.TRUE);
		pPassiva.setInParticipacao(ProcessoParteParticipacaoEnum.P);
		pPassiva.setInSituacao(ProcessoParteSituacaoEnum.A);
		pPassiva.setIsEnderecoDesconhecido(Boolean.FALSE);
		pPassiva.setPartePrincipal(Boolean.TRUE);
		pPassiva.setParteSigilosa(Boolean.FALSE);
		pPassiva.setPessoa(obtemPessoa(pAtiva.getPessoa().getIdUsuario()));
		pPassiva.setProcessoTrf(processo);
		
		tipoPartePassivo = definirTipoParte(processo, tipoPartePassivo);
		pPassiva.setTipoParte(tipoPartePassivo);
		
		processo.getProcessoParteList().add(pPassiva);
		// Atribui o advogado do polo passivo apenas se existir mais de um advogado na lista.
		if (advogados.size()>1){
			this.atribuiAdvogado(processo, pPassiva, pAtiva.getProcessoParteRepresentanteList().get(0).getRepresentante().getIdUsuario());
		}
	}

	private TipoParte definirTipoParte(ProcessoTrf processo, TipoParte tipoParte) {
		List<TipoParteConfigClJudicial> tipoParteConfigClJudicial = processo.getClasseJudicial().getTipoParteConfigClJudicial();
		
		for (TipoParteConfigClJudicial tipoConfig : tipoParteConfigClJudicial) {
			if (tipoConfig.getTipoParteConfiguracao().getTipoParte().getTipoPrincipal()) {
				if (tipoConfig.getTipoParteConfiguracao().getPoloAtivo()) {
					tipoParte = tipoConfig.getTipoParteConfiguracao().getTipoParte();
				} else if (tipoConfig.getTipoParteConfiguracao().getPoloPassivo()) {
					tipoParte = tipoConfig.getTipoParteConfiguracao().getTipoParte();
				}				
			}
		}
		return tipoParte;
	}
	
	private void atribuiAdvogado(ProcessoTrf processo, ProcessoParte pp, Integer id){
		Pessoa advogado = obtemAdvogado(id).getPessoa();
		ProcessoParte ppadv = new ProcessoParte();
		ppadv.setCheckado(Boolean.TRUE);
		ppadv.setCheckVisibilidade(Boolean.TRUE);
		ppadv.setInParticipacao(pp.getInParticipacao());
		ppadv.setInSituacao(ProcessoParteSituacaoEnum.A);
		ppadv.setIsEnderecoDesconhecido(Boolean.FALSE);
		ppadv.setPartePrincipal(Boolean.FALSE);
		ppadv.setParteSigilosa(Boolean.FALSE);
		ppadv.setPessoa(advogado);
		ppadv.setProcessoTrf(processo);
		ppadv.setTipoParte(ParametroUtil.instance().getTipoParteAdvogado());
		processo.getProcessoParteList().add(ppadv);
		
		ProcessoParteRepresentante ppr = new ProcessoParteRepresentante();
		ppr.setInSituacao(ProcessoParteSituacaoEnum.A);
		ppr.setParteRepresentante(ppadv);
		ppr.setProcessoParte(pp);
		ppr.setRepresentante(advogado);
		ppr.setTipoRepresentante(ParametroUtil.instance().getTipoParteAdvogado());
		pp.getProcessoParteRepresentanteList().add(ppr);
		
		return;
	}
	
	@SuppressWarnings("unchecked")
	private void atribuiClasseAssuntoJurisdicao(Jurisdicao jurisdicao, ProcessoTrf processo){
		List<OrgaoJulgador> ojs = null;
		OrgaoJulgador oj = null;
		List<OrgaoJulgadorCompetencia> ojcs = null;
		OrgaoJulgadorCompetencia ojc = null;
		List<CompetenciaClasseAssunto> ccas = null;
		CompetenciaClasseAssunto cca = null;
		
		String query = "SELECT o FROM OrgaoJulgador AS o WHERE o.jurisdicao = ?1";
		Query q = entityManager.createQuery(query);
		q.setParameter(1, jurisdicao);
		ojs = (List<OrgaoJulgador>) q.getResultList();
		int sorteado = rnd.nextInt(ojs.size());
		oj = ojs.get(sorteado);
		ojcs = oj.getOrgaoJulgadorCompetenciaList();
		sorteado = rnd.nextInt(ojcs.size());
		ojc = ojcs.get(sorteado);
		ccas = ojc.getCompetencia().getCompetenciaClasseAssuntoList();
		cca = ojc.getCompetencia().getCompetenciaClasseAssuntoList().get(sorteado);
		sorteado = rnd.nextInt(ccas.size());
//		cca = recuperaAssuntoPorClasse(recuperaRitoOrdinario());
		processo.setClasseJudicial(cca.getClasseAplicacao().getClasseJudicial());
		processo.setAssuntoTrf(cca.getAssuntoTrf());
		
		ProcessoAssunto pa = new ProcessoAssunto();
		pa.setAssuntoPrincipal(Boolean.TRUE);
		pa.setAssuntoTrf(cca.getAssuntoTrf());
		pa.setProcessoTrf(processo);
		
		if (processo.getProcessoAssuntoList()==null){
			processo.setProcessoAssuntoList(new ArrayList<ProcessoAssunto>());
		}
		processo.getProcessoAssuntoList().add(pa);
		
		//processo.getAssuntoTrfList().add(cca.getAssuntoTrf());
		processo.setJurisdicao(jurisdicao);
		return;
	}

	private ClasseJudicial recuperaRitoOrdinario(){
		String query = "SELECT o FROM ClasseJudicial AS o WHERE o.classeJudicial = ?1";
		Query q = entityManager.createQuery(query);
		q.setParameter(1, "AÇÃO TRABALHISTA - RITO ORDINÁRIO");
		return (ClasseJudicial)q.getSingleResult();
	}
	@SuppressWarnings("unchecked")
	private CompetenciaClasseAssunto recuperaAssuntoPorClasse(ClasseJudicial classeJudicial){
		String query = "SELECT o FROM CompetenciaClasseAssunto AS o WHERE o.classeAplicacao.classeJudicial.classeJudicial = ?1";
		Query q = entityManager.createQuery(query);
		q.setParameter(1, classeJudicial.getClasseJudicial());
		List<CompetenciaClasseAssunto>ccas = (List<CompetenciaClasseAssunto>) q.getResultList();
		CompetenciaClasseAssunto cca = null;
		if (ccas.size()>0){
			int sorteado = rnd.nextInt(ccas.size());
			cca=ccas.get(sorteado);
		}
		return cca;
	}
	public boolean isAssinado(ProcessoDocumento processoDocumento) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from ProcessoDocumentoBinPessoaAssinatura o ");
		sb.append("where o.processoDocumentoBin = :bin");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("bin", processoDocumento.getProcessoDocumentoBin());
		Long result = EntityUtil.getSingleResult(q);
		return result > 0;
	}

}
