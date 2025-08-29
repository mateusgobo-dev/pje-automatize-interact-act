package br.jus.csjt.pje.persistence.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;

import br.com.infox.cliente.home.ProcessoAudienciaHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.utils.Constantes;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.manager.SalaManager;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.csjt.pje.business.service.ResultadoSentencaService;
import br.jus.pje.jt.entidades.Acordo;
import br.jus.pje.jt.entidades.AcordoParcela;
import br.jus.pje.jt.entidades.AcordoVerba;
import br.jus.pje.jt.entidades.AudImportacao;
import br.jus.pje.jt.entidades.AudParcelaImportacao;
import br.jus.pje.jt.entidades.AudParteImportacao;
import br.jus.pje.jt.entidades.AudVerbaImportacao;
import br.jus.pje.jt.entidades.Pericia;
import br.jus.pje.jt.entidades.ProcessoAudienciaJT;
import br.jus.pje.jt.entidades.TipoVerba;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.TipoAudiencia;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.StatusAudienciaEnum;
import br.jus.pje.nucleo.util.Crypto;

@Name(AudImportacaoDAOImpl.NAME)
@Scope(ScopeType.CONVERSATION)
public class AudImportacaoDAOImpl implements AudImportacaoDAO, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7317791383405443443L;

	public static final String NAME = "audImportacaoDAO";

	@In (create = true)
	private EntityManager entityManager;
	
	private EntityManager getEntityManager(){
		return entityManager;
	}

	
	private void lancarAudienciaRealizada(AudImportacao audImportacao) {
		ProcessoAudiencia processoAudiencia = getEntityManager().find(ProcessoAudiencia.class,
				audImportacao.getIdProcessoAudiencia());
		ProcessoTrf processoTrf = audImportacao.getProcessoTrf();
		String dtAudiencia = processoAudiencia.getDtInicioFormatada();
		String localAudiencia = processoTrf.getOrgaoJulgador().getOrgaoJulgador();

		ComponentUtil.getProcessoAudienciaManager().lancarMovimentoAudiencia(processoAudiencia.getTipoAudiencia(), dtAudiencia, localAudiencia,
				processoTrf.getProcesso(), null, StatusAudienciaEnum.F);
	}
	/**
	 * Grava o(s) resultado(s) da sentença cadastrado(s) durante a importação
	 * dos dados do AUD.
	 */
	private void gravarResultadoSentenca(ProcessoTrf processoTrf) {
		// Setando a instancia em ProcessoHome porque ResultadoSentencaService
		// espera que o ProcessoHome tenha uma instância
		ProcessoHome.instance().setId(processoTrf.getIdProcessoTrf());
		ResultadoSentencaService resultadoSentencaService = ComponentUtil.getComponent(ResultadoSentencaService.NAME);
		// Testando se há resultado(s) sentença para o processo
		if (resultadoSentencaService.getResultadoSentenca(processoTrf) != null) {
			// Homologando o(s) resultado(s) da sentença
			resultadoSentencaService.homologarResultadoSentencaAud();
		}
	}


	/**
	 * Verifica se a há imcompetência rejeitada
	 * 
	 * @param audImportacao
	 * @return boolean
	 * @author U006184 - Gabriel Azevedo Data: 01/11/2011
	 */
	public boolean verificaIncompetenciaRejeitada(AudImportacao audImportacao) {
		boolean contingencia = false;
		// Verifica se a incompetência é rejeitada
		if (audImportacao.getIncompetencia() != null
				&& audImportacao.getIncompetencia().equalsIgnoreCase(Constantes.AudImportacao.REJEITADA)) {
			contingencia = true;
		}
		return contingencia;
	}

	/**
	 * Verifica se as todas as partes são ausentes.
	 * 
	 * @param audImportacao
	 * @return booelan
	 * @author U006184 - Thiago Oliveira Data: 26/10/2011
	 */
	public boolean verificaAusenciaTotalPoloAtivo(AudImportacao audImportacao) {

		boolean ausencia = false;

		// Verifica se todos os polos ativos estão presentes
		List<AudParteImportacao> listaAudParteImportacao = audImportacao.getAudParteImportacao();

		if (listaAudParteImportacao.size() == 0) {
			return ausencia;
		} else {
			for (AudParteImportacao audParteImportacao : listaAudParteImportacao) {
				if (audParteImportacao.getPoloAtivoParte() != null
						&& "S".equalsIgnoreCase(audParteImportacao.getPoloAtivoParte())
						&& "S".equalsIgnoreCase(audParteImportacao.getPartePresente()))
					return ausencia;
			}
		}

		ausencia = true;

		return ausencia;
	}


	/**
	 * Verifica se há desistência total
	 * 
	 * @param audImportacao
	 * @return boolean
	 * @author U006184 - Thiago de Almeida Olveira Data: 26/10/2011
	 */
	public boolean verificaDesistencia(AudImportacao audImportacao) {
		boolean contingencia = false;
		// Verifica se a desistência é total
		if (audImportacao.getDesistencia() != null
				&& audImportacao.getDesistencia().equalsIgnoreCase(Constantes.AudImportacao.TOTAL)) {
			contingencia = true;
		}
		return contingencia;
	}

	/**
	 * Verifica se a há imcompetência acolhida
	 * 
	 * @param audImportacao
	 * @return boolean
	 * @author U006184 - Thiago Oliveira Data: 26/10/2011
	 */
	public boolean verificaIncompetenciaAcolhida(AudImportacao audImportacao) {
		boolean contingencia = false;
		// Verifica se a incompetência é acolhida
		if (audImportacao.getIncompetencia() != null
				&& audImportacao.getIncompetencia().equalsIgnoreCase(Constantes.AudImportacao.ACOLHIDA)) {
			contingencia = true;
		}
		return contingencia;
	}

	/*
	 * Busca uma sala de audiência que esteja ativa para a data desejada
	 */
	public Sala buscaSalaAudiencia(TipoAudiencia tipoAudiencia, Date dtAudiencia) {
		ProcessoAudienciaHome processoAudienciaHome = ComponentUtil.getComponent("processoAudienciaHomeConversation");
		processoAudienciaHome.setTipoAudiencia(tipoAudiencia);
		processoAudienciaHome.consultaBloqueiosPauta(dtAudiencia);
		
		List<Sala> salasAudiencia = ComponentUtil.getComponent(SalaManager.class).recuperar(
				ProcessoTrfHome.instance().getInstance().getOrgaoJulgador(), tipoAudiencia);
		
		return salasAudiencia.isEmpty() ? null : salasAudiencia.get(0);
	}
	/**
	 * Cria uma nova audiência no PJE através dos dados recebidos do AUD.
	 */
	private void criarNovaAudiencia(AudImportacao audImportacao) {
		ProcessoTrf processoTrf = audImportacao.getProcessoTrf();
		ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent("processoTrfHome");
		processoTrfHome.setInstance(processoTrf);
		Date dtAudiencia = audImportacao.getDtAdiamento();
		String tipoAudienciaAUD = audImportacao.getAndamentoEncerramento();
		ProcessoAudienciaHome processoAudienciaHome = ComponentUtil.getComponent(ProcessoAudienciaHome.NAME);

		ProcessoAudiencia processoAudiencia = new ProcessoAudiencia();
		processoAudiencia.setProcessoTrf(processoTrf);
		processoAudiencia.setDtMarcacao(new Date());
		processoAudiencia.setDtAudiencia(dtAudiencia);
		processoAudiencia.setDtInicio(dtAudiencia);
		processoAudiencia.setInAtivo(Boolean.TRUE);
		processoAudiencia.setStatusAudiencia(StatusAudienciaEnum.M);
		PessoaMagistrado magistrado = getEntityManager().find(PessoaMagistrado.class, audImportacao.getIdPessoaMagistrado());
		processoAudiencia.setPessoaRealizador(magistrado.getPessoa());
		
		// Erro gerado quando o AUD estiver desatualizado
		if (Constantes.MapaTipoAudienciaAudParaPje.get(tipoAudienciaAUD) == null) {
			throw new AplicationException("Não foi possível assinar a ata de audiência ("+processoTrf.getNumeroProcesso()+") pois o AUD não enviou o tipo de audiência a ser marcada. Favor entrar em contato com a central de atendimento.");
		}

		TipoAudiencia tipoAudiencia = getEntityManager().find(TipoAudiencia.class, Constantes.MapaTipoAudienciaAudParaPje.get(tipoAudienciaAUD));
		processoAudiencia.setTipoAudiencia(tipoAudiencia);

		// Calcula o horário de fim da audiência baseado no tempo de audiência
		// padrão para o orgão julgador e tipo de audiência
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dtAudiencia);
		Integer tempoAudiencia = processoAudienciaHome.getTempoAudienciaPadraoOrgaoJulgador(processoTrf.getOrgaoJulgador(), tipoAudiencia);
		calendar.add(Calendar.MINUTE, tempoAudiencia);
		processoAudiencia.setDtFim(calendar.getTime());

		// Sala da audiência
		Sala salaAudiencia = buscaSalaAudiencia(tipoAudiencia, dtAudiencia);
		
		if(salaAudiencia == null) {
			throw new AplicationException("Não foi encontrada nenhuma sala de audiência disponível para a data informada.");
		}
				
		processoAudiencia.setSalaAudiencia(salaAudiencia);
		
		processoAudienciaHome.setInstance(processoAudiencia);
		processoAudienciaHome.setTipoAudiencia(tipoAudiencia);
		processoAudienciaHome.consultaBloqueiosPauta(dtAudiencia);
		processoAudienciaHome.marcarAudiencia(processoAudiencia, false);
	}

	@Transactional
	public void importarDados(AudImportacao audImportacao, ProcessoTrf processoTrf, Integer idProcessoAudiencia) {
		Processo processo = audImportacao.getProcessoTrf().getProcesso();

		this.importarDados(audImportacao);

		// Lança o movimento da audiência realizada
		lancarAudienciaRealizada(audImportacao);

		// Lança movimentos relacionados ao resultado da audiência
		// (conciliação, contingência ou nova audiência).
		// A conciliação, as contingências e a designação de audiência são
		// mutuamente exclusivos.

		gravarResultadoSentenca(processoTrf);
		
		if (verificaIncompetenciaRejeitada(audImportacao)) {

			// Contingência - Incompetência rejeitada
			// Código = 374 - Descrição = Rejeitada a exceção de incompetência
			// **************************************************************************************
			MovimentoAutomaticoService.preencherMovimento().deCodigo(CodigoMovimentoNacional.COD_MOVIMENTO_INCOMPETENCIA_REJEITADA)
			  						  .associarAoProcesso(processo)
			  						  .lancarMovimento();

		} else if (!(audImportacao.getValorAcordo() != null && audImportacao.getValorAcordo() > 0)
				&& !verificaAusenciaTotalPoloAtivo(audImportacao) && !verificaDesistencia(audImportacao)
				&& !verificaIncompetenciaAcolhida(audImportacao)
				&& audImportacao.getDtAdiamento() != null) {
			// Cria a nova audiência, que internamente já lança o movimento
			criarNovaAudiencia(audImportacao);
		}
		
		/*
		 * Replicar para ProcessoDocumentoBin os dados da assinatura que 
		 * foram gravados apenas em ProcessoDocumentoBinPessoaAssinatura
		 */
		replicarAssinatura(idProcessoAudiencia);

		

	}

	private void replicarAssinatura(Integer idProcessoAudiencia) {
		ProcessoAudiencia processoAudiencia = getEntityManager().find(ProcessoAudiencia.class, idProcessoAudiencia);
		Query query = getEntityManager().createQuery("from ProcessoDocumentoBinPessoaAssinatura where processoDocumentoBin = :processoDocumentoBin");
		query.setParameter("processoDocumentoBin", processoAudiencia.getProcessoDocumento().getProcessoDocumentoBin());
		ProcessoDocumentoBinPessoaAssinatura pdbpa = (ProcessoDocumentoBinPessoaAssinatura) query.getSingleResult();
		ProcessoDocumentoBin pdb = processoAudiencia.getProcessoDocumento().getProcessoDocumentoBin();
		pdb.setSignature(pdbpa.getAssinatura());
		pdb.setCertChain(pdbpa.getCertChain());
		pdb.setValido(ProcessoDocumentoHome.instance().estaValido());
		
		getEntityManager().persist(pdb);
		getEntityManager().flush();
	}
	
	
	@Override
	public void importarDados(AudImportacao ai) {
		// jogar dados da tabela temporária para as do sistema
		ProcessoAudiencia processoAudiencia = entityManager.find(ProcessoAudiencia.class, ai.getIdProcessoAudiencia());
		processoAudiencia.setStatusAudiencia(StatusAudienciaEnum.F);
		processoAudiencia.setDtInicio(ai.getDtInicio());
		processoAudiencia.setDtFim(ai.getDtInicio());

		// Acordo
		Acordo acordo = new Acordo();
		acordo.setProcessoAudiencia(processoAudiencia);

		acordo.setValorAcordo(ai.getValorAcordo());
		Integer numParcerlas = ai.getNumParcelas() == null || ai.getNumParcelas().equals("") ? 0 : Integer.valueOf(ai
				.getNumParcelas());
		acordo.setNumParcelas(numParcerlas);

		// Verbas
		List<AcordoVerba> verbas = new ArrayList<AcordoVerba>();
		Query query = null;

		for (AudVerbaImportacao audVerbaImportacao : ai.getAudVerbaImportacao()) {
			query = entityManager.createQuery("from TipoVerba where nomeVerba = :nomeVerba");
			query.setParameter("nomeVerba", audVerbaImportacao.getNomeVerba());
			TipoVerba tipoVerba = (TipoVerba) query.getSingleResult();
			AcordoVerba acordoVerba = new AcordoVerba();
			acordoVerba.setAcordo(acordo);
			acordoVerba.setIdAcordo(acordo.getIdAcordo());
			acordoVerba.setIdTipoVerba(tipoVerba.getIdTipoVerba());
			acordoVerba.setTipoVerba(tipoVerba);
			acordoVerba.setValorVerba(audVerbaImportacao.getValorVerba());

			verbas.add(acordoVerba);
		}

		acordo.setAcordoVerbas(verbas);

		// Parcelas do acordo
		List<AcordoParcela> parcelas = new ArrayList<AcordoParcela>();
		for (AudParcelaImportacao parcelaImp : ai.getAudParcelaImportacao()) {
			AcordoParcela acordoParcela = new AcordoParcela();
			acordoParcela.setNumParcela(parcelaImp.getNumParcela());
			acordoParcela.setValorParcela(parcelaImp.getValorParcela());
			acordoParcela.setDataVencimento(parcelaImp.getDataVencimento());
			acordoParcela.setNomeBanco(parcelaImp.getNumBanco());
			acordoParcela.setNumAgencia(parcelaImp.getNumAgencia());
			acordoParcela.setNumCheque(parcelaImp.getNumCheque());
			acordoParcela.setAcordo(acordo);

			parcelas.add(acordoParcela);
		}

		acordo.setAcordoParcelas(parcelas);

		entityManager.persist(acordo);

		// Pericia e Perito
		if (ai.getNomePerito() != null && !ai.getNomePerito().equals("")) {
			Pericia pericia = new Pericia();
			pericia.setProcessoAudiencia(processoAudiencia);
			pericia.setPrazoQuesitos(ai.getPrazoQuesitos());
			pericia.setDtComumQuesitos(ai.getDtComumQuesitos());
			pericia.setDtAutorQuesitos(ai.getDtAutorQuesitos());
			pericia.setDtReuQuesitos(ai.getDtReuQuesitos());
			pericia.setPrazoLaudo(ai.getPrazoLaudo());
			pericia.setDtInicioPrazoLaudo(ai.getDtInicioPrazoLaudo());
			pericia.setPrazoPartes(ai.getPrazoPartes());
			pericia.setDtInicioConstestarAutor(ai.getDtInicioConstestarAutor());
			pericia.setDtInicioConstestarReu(ai.getDtInicioConstestarReu());
			pericia.setNomePerito(ai.getNomePerito());

			entityManager.persist(pericia);
		}

		// Processo Audiência JT
		ProcessoAudienciaJT processoAudienciaJT = new ProcessoAudienciaJT();
		processoAudienciaJT.setIdProcessoAudienciaJt(processoAudiencia.getIdProcessoAudiencia());
		processoAudienciaJT.setProcessoAudiencia(processoAudiencia);
		processoAudienciaJT.setVerificada(Boolean.FALSE);
		processoAudienciaJT.setObservacoes(ai.getObservacoes());

		entityManager.persist(processoAudienciaJT);

		ai.setDtConsolidacao(new Date());
		entityManager.persist(ai);

		entityManager.flush();
	}

	@Override
	public void criarDocumentoAta(AudImportacao ai, Usuario usuarioInclusao) {
		ProcessoTrf processoTrf = entityManager.find(ProcessoTrf.class, ai.getIdProcesso());
		ProcessoAudiencia processoAudiencia = entityManager.find(ProcessoAudiencia.class, ai.getIdProcessoAudiencia());

		PessoaMagistrado magistrado = entityManager.find(PessoaMagistrado.class, ai.getIdPessoaMagistrado());
		processoAudiencia.setPessoaRealizador(magistrado.getPessoa());

		// Documento
		/*
		 * PJE-JT: Athos Reiser : PJE-1089 - 2012-01-12 
		 * Criado parametro de sistema para id do tipo de documento de ata de audiencia
		 */
		String idTipoProcessoDocumento = ParametroUtil.getFromContext("idTipoProcessoDocumentoAtaAudiencia", true);
		
		ProcessoDocumento processoDocumento = new ProcessoDocumento();
		Query query = entityManager.createQuery("from TipoProcessoDocumento where idTipoProcessoDocumento = :idTipoProcessoDocumento");
		query.setParameter("idTipoProcessoDocumento", 
				Integer.parseInt(idTipoProcessoDocumento != null ? idTipoProcessoDocumento : "0"));
		/*
		 * PJE-JT: fim
		 */
		TipoProcessoDocumento tipoProcessoDocumento = (TipoProcessoDocumento) query.getSingleResult();

		processoDocumento.setTipoProcessoDocumento(tipoProcessoDocumento);
		processoDocumento.setAtivo(Boolean.TRUE);
		processoDocumento.setDocumentoSigiloso(Boolean.FALSE);
		Date agora = new Date();
		processoDocumento.setDataInclusao(agora);
		processoDocumento.setProcessoDocumento(tipoProcessoDocumento.getTipoProcessoDocumento());
		processoDocumento.setUsuarioInclusao(usuarioInclusao);
		processoDocumento.setNomeUsuarioInclusao(usuarioInclusao.getNome());

		// Documento Bin
		ProcessoDocumentoBin processoDocumentoBin = new ProcessoDocumentoBin();
		processoDocumentoBin.setDataInclusao(agora);
		String conteudoDocumento = ai.getConteudoDocumento();
		processoDocumentoBin.setModeloDocumento(conteudoDocumento);
		processoDocumentoBin.setUsuario(usuarioInclusao);

		processoDocumento.setProcessoDocumentoBin(processoDocumentoBin);

		// Associando documento
		processoAudiencia.setProcessoDocumento(processoDocumento);
		processoTrf.getProcesso().getProcessoDocumentoList().add(processoDocumento);
		processoDocumento.setProcesso(processoTrf.getProcesso());

		ai.setDtValidacao(agora);

		entityManager.persist(processoDocumentoBin);
		entityManager.persist(processoDocumento);
		entityManager.persist(ai);

		entityManager.flush();
	}
}
