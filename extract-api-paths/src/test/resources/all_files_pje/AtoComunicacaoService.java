package br.jus.cnj.pje.nucleo.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.persistence.Query;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.dom4j.IllegalAddException;
import org.hibernate.Hibernate;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.util.JSONUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.PessoaProcuradoriaEntidadeManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.ProcessoParteExpedienteDAO;
import br.jus.cnj.pje.business.dao.ProcessoParteExpedienteDAO.CriterioPesquisa;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodicoComum;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.CienciaAutomatica;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.CienciaAutomatizadaDiarioEletronico;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.DecursoPrazo;
import br.jus.cnj.pje.entidades.vo.MiniPacVO;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.PublicadorDJE;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.MuralException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.enums.TipoProcessoDocumentoEnum;
import br.jus.cnj.pje.nucleo.PessoaInvalidaException;
import br.jus.cnj.pje.nucleo.PjeRestClientException;
import br.jus.cnj.pje.nucleo.manager.AgrupamentoPessoasManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.cnj.pje.nucleo.manager.MuralService;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoAlertaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoVisibilidadeSegredoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.PublicacaoDiarioEletronicoManager;
import br.jus.cnj.pje.nucleo.manager.RespostaExpedienteManager;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.fluxo.PreparaAtoComunicacaoAction.ParAnexo;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pdpj.commons.models.dtos.webhooks.ModeloEventoDTO;
import br.jus.pdpj.commons.models.dtos.webhooks.PayloadDTO;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.dto.domicilioeletronico.ComunicacaoRecebida;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.LiberacaoPublicacaoDecisao;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.MeioContato;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaProcuradoria;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoAssociacao;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.PublicacaoDiarioEletronico;
import br.jus.pje.nucleo.entidades.RespostaExpediente;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.CategoriaPrazoEnum;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.util.ArrayUtil;
import br.jus.pje.nucleo.util.DateUtil;

@Name(AtoComunicacaoService.NAME)
public class AtoComunicacaoService extends BaseService {

	private static final String SEPARATOR = "+";

	public static final String NAME = "atoComunicacaoService";

	public static final int PRAZO_COMUNICACAO_DEFAULT = 5;
	public static final String PARAMETRO_PRAZO_COMUNICACAO_TELEGRAMA = "prazoComunicacaoTelegrama";
	private Evento movimentoDisponibilizacaoDJE;
	private Evento movimentoPublicacaoDJE;

	@In
	private ProcessoJudicialService processoJudicialService;

	@In
	private ProcessoExpedienteManager processoExpedienteManager;

	@In
	private ProcessoParteExpedienteManager processoParteExpedienteManager;

	@In
	private ProcessoDocumentoExpedienteManager processoDocumentoExpedienteManager;

	@In
	private ProcessoDocumentoBinManager processoDocumentoBinManager;

	@In
	private DocumentoBinManager documentoBinManager;

	@In
	private ProcessoDocumentoBinPessoaAssinaturaManager processoDocumentoBinPessoaAssinaturaManager;

	@In
	private ProcessoDocumentoManager processoDocumentoManager;

	@In
	private DocumentoJudicialService documentoJudicialService;

	@In
	private PessoaService pessoaService;

	@In
	private PessoaFisicaService pessoaFisicaService;

	@In
	private UsuarioService usuarioService;

	@In
	private ParametroService parametroService;

	@In(create = true)
	private PrazosProcessuaisService prazosProcessuaisService;

	@In
	private ProcessoAlertaManager processoAlertaManager;

	@In
	private PessoaProcuradoriaManager pessoaProcuradoriaManager;

	@In
	private RespostaExpedienteManager respostaExpedienteManager;

	@In
	private AgrupamentoPessoasManager agrupamentoPessoasManager;

	@Logger
	private Log logger;

	@In
	private Events events;

	@In
	private CienciaAutomatizadaDiarioEletronico cienciaAutomatizadaDiarioEletronico;

	@In
	private CienciaAutomatica cienciaAutomatica;

	@In
	private DecursoPrazo decursoPrazo;

	@In
	private ProcessoParteManager processoParteManager;

	@In
	private EventoManager eventoManager;

	@In
	private ProcuradoriaManager procuradoriaManager;

	@In
	private PessoaManager pessoaManager;

	@In(create = true)
	PublicacaoDiarioEletronicoManager publicacaoDiarioEletronicoManager;
	
	@In
	private VerificadorPeriodicoComum verificadorPeriodicoComum;

	@In(create = true, required = false)
    private DomicilioEletronicoService domicilioEletronicoService;

	public boolean publicadorDJeDisponivel() {
		return cienciaAutomatizadaDiarioEletronico.publicadorDJeDisponivel();
	}
	
	/**
	 * @return Instância única.
	 */
	public static AtoComunicacaoService instance() {
		return (AtoComunicacaoService)Component.getInstance(AtoComunicacaoService.NAME);
	}

	
	public ProcessoParteExpediente getAtoComunicacaoPessoal(Integer id) throws PJeBusinessException {
		String select = "SELECT DISTINCT ppe FROM ProcessoParteExpediente ppe WHERE "
				+ "idProcessoParteExpediente = :idProcessoParteExpediente";
		Query q = EntityUtil.getEntityManager().createQuery(select);
		q.setParameter("idProcessoParteExpediente", id);

		return (ProcessoParteExpediente) q.getSingleResult();
	}

	private void validarDocumentosExpedientes(ProcessoExpediente[] expedientes) throws PJeBusinessException {

		for (ProcessoExpediente expediente : expedientes) {
			ProcessoDocumento documento = expediente.getProcessoDocumento();

			ProcessoDocumentoBin docBin = null;

			boolean documentoValido = false;

			if (documento != null) {
				docBin = documento.getProcessoDocumentoBin();
			}

			if (docBin != null && (docBin.isBinario() && docBin.getIdProcessoDocumentoBin() > 0)) {
					documentoValido = true; // Significa que o documento associado realmente existe na base de dados.
			} else if (!docBin.isBinario()
					&& (docBin.getModeloDocumento() != null && docBin.getModeloDocumento().trim().length() > 0)) {
				documentoValido = true; // Significa que há conteudo no documento elaborado para o expediente
			}

			if (!documentoValido) {
				throw new PJeBusinessException("pje.atoComunicacaoService.error.documentoInvalido", null, documento);
			}
		}
	}

	public ProcessoExpediente getAtoComunicacao() {
		ProcessoExpediente pe = this.processoExpedienteManager.getExpediente();
		pe.setProcessoDocumento(this.documentoJudicialService.getDocumento());
		return pe;
	}

	public void copiaDadosDocumento(ProcessoDocumento from, ProcessoDocumento to) {
		ProcessoDocumento pd = null;
		if (from.getIdProcessoDocumento() != 0) {
			try {
				pd = documentoJudicialService.getDocumento(from.getIdProcessoDocumento());
			} catch (PJeBusinessException e) {
				logger.error("Erro de negócio ao tentar obter o documento {0} para realização de cópia. {1}",
						from.getIdProcessoDocumento(), e.getLocalizedMessage());
			} catch (PJeDAOException e) {
				logger.error("Erro ao tentar acessar o documento {0} para realização de cópia. {1}",
						from.getIdProcessoDocumento(), e.getLocalizedMessage());
			}
		} else {
			pd = from;
		}
		if (pd != null) {
			to.setAtivo(pd.getAtivo());
			to.setDocumentoPrincipal(pd.getDocumentoPrincipal());
			to.setDocumentoSigiloso(pd.getDocumentoSigiloso());
			to.getDocumentosVinculados().addAll(pd.getDocumentosVinculados());
			to.setProcessoDocumentoBin(pd.getProcessoDocumentoBin());
		}
	}


	public ProcessoExpediente getAtoComunicacao(Integer identificador) throws PJeBusinessException, PJeDAOException {
		if (identificador == null) {
			throw new IllegalArgumentException("O identificador do expediente não pode ser nulo.");
		}

		return this.processoExpedienteManager.findById(identificador);
	}

	public ProcessoParteExpediente getAtoPessoal(Integer identificador) throws PJeBusinessException {
		if (identificador == null) {
			throw new IllegalArgumentException("O identificador do expediente não pode ser nulo.");
		}

		return verificadorPeriodicoComum.getAtoComunicacaoPessoal(identificador);
	}

	public List<ProcessoParteExpediente> getAtosComunicacao(Pessoa advogado, int firstRow, int maxRows,
			CriterioPesquisa criterio) throws PJeDAOException, PJeBusinessException {
		return getAtosComunicacao(advogado, null, firstRow, maxRows, criterio);
	}

	public List<ProcessoParteExpediente> getAtosComunicacao(Pessoa advogado, ProcessoTrf processoJudicial, int firstRow,
			int maxRows, CriterioPesquisa criterio) throws PJeDAOException, PJeBusinessException {
		List<Pessoa> representados = pessoaService.getRepresentados(advogado);

		return processoParteExpedienteManager.getAtosComunicacao(advogado, processoJudicial, firstRow, maxRows,
				criterio, representados.toArray(new Pessoa[representados.size()]));
	}

	public long contagemAtos(Pessoa advogado, CriterioPesquisa criterio) throws PJeBusinessException {
		List<Pessoa> representados = pessoaService.getRepresentados(advogado);

		return processoParteExpedienteManager.contagemAtos(advogado, null, criterio,
				representados.toArray(new Pessoa[representados.size()]));
	}

	public long contagemAtos(Pessoa advogado, ProcessoTrf processoJudicial, CriterioPesquisa criterio)
			throws PJeBusinessException {
		List<Pessoa> representados = pessoaService.getRepresentados(advogado);

		return processoParteExpedienteManager.contagemAtos(advogado, processoJudicial, criterio,
				representados.toArray(new Pessoa[representados.size()]));
	}

	public ProcessoExpediente persist(ProcessoExpediente pe, ProcessoTrf processo, Long jbpmTask)
			throws PJeBusinessException, PJeDAOException, CertificadoException {
		for (ProcessoParteExpediente ppe : pe.getProcessoParteExpedienteList()) {
			processoParteExpedienteManager.persist(ppe, pe, processo,
					prazosProcessuaisService.obtemCalendario(processo.getOrgaoJulgador()));
		}

		this.documentoJudicialService.finalizaDocumento(pe.getProcessoDocumento(), processo, jbpmTask, true);

		pe.setProcessoTrf(processo);

		return processoExpedienteManager.persist(pe);
	}

	public boolean acrescentaDocumentoReferido(ProcessoExpediente pe, ProcessoDocumento doc) {
		return this.processoExpedienteManager.addDocumentoReferido(pe, doc);
	}

	/**
	 * Retorna uma lista com os meios de comunicação padrão.
	 *
	 * @return Meios de comunicação retornados:
	 * <ul>
	 * <li>Correios</li>
	 * <li>Central de Mandados</li>
	 * <li>Carta</li>
	 * <li>Diário Eletrônico</li>
	 * <li>Pessoalmente</li>
	 * <li>Telefone</li>
	 * <li>Edital <b>* não é retornado caso o tipo da justiça seja Justiça do
	 * Trabalho.</b></li>
	 * </ul>
	 */
	private List<ExpedicaoExpedienteEnum> getMeiosComunicacaoPadrao() {
		List<ExpedicaoExpedienteEnum> meiosComunicacao = new ArrayList<ExpedicaoExpedienteEnum>(
				ExpedicaoExpedienteEnum.values().length);

		meiosComunicacao.add(ExpedicaoExpedienteEnum.C);
		meiosComunicacao.add(ExpedicaoExpedienteEnum.M);
		meiosComunicacao.add(ExpedicaoExpedienteEnum.L);
		meiosComunicacao.add(ExpedicaoExpedienteEnum.P);
		meiosComunicacao.add(ExpedicaoExpedienteEnum.S);
		meiosComunicacao.add(ExpedicaoExpedienteEnum.T);
		meiosComunicacao.add(ExpedicaoExpedienteEnum.G);
		meiosComunicacao.add(ExpedicaoExpedienteEnum.D);

		if (ParametroJtUtil.instance().justicaEleitoral()) {
			meiosComunicacao.add(ExpedicaoExpedienteEnum.R);
		}
		return meiosComunicacao;
	}

	/**
	 * Método responsável por verificar se o {@link Usuario} está devidamente
	 * cadastrado no PJe.
	 *
	 * @param usuario {@link Usuario} cujos atributos serão validadas.
	 * @return Será retornado verdadeiro quando todas as condições abaixo forem
	 * verdadeiras:
	 * <ul>
	 * <li>Usuário ativo.</li>
	 * <li>Usuário possui certificado definido.</li>
	 * </ul>
	 * Falso, caso contrário.
	 */
	private boolean verificarCadastroUsuario(Usuario usuario) {
		try {
			Pessoa p = pessoaManager.findById(usuario.getIdUsuario());

			List<Procuradoria> listProcuradoria = procuradoriaManager.getlistProcuradorias(p);

			if (p != null & listProcuradoria != null && !listProcuradoria.isEmpty()
					&& p.getInTipoPessoa() == TipoPessoaEnum.J && "S".equalsIgnoreCase(
							ParametroUtil.getParametro(Parametros.PJE_USUARIO_SEM_CERTIFICADO_ENVIO_COMUNICACAO))) {
				return usuario != null && usuario.getAtivo();
			} else {
				return usuario != null && usuario.getAtivo()
						&& (usuario.getTemCertificado() || usuario.getCertChain() != null);
			}
		} catch (Exception e) {
			return usuario != null && usuario.getAtivo()
					&& (usuario.getTemCertificado() || usuario.getCertChain() != null);
		}
	}

	/**
	 * Método responsável por verificar se a {@link Pessoa} está devidamente
	 * cadastrado no PJe.
	 *
	 * @param pessoa {@link Pessoa} cujos atributos serão validadas.
	 * @param especializacao Indica o papel que será validado.
	 * @return Será retornado verdadeiro quando todas as condições abaixo forem
	 * verdadeiras:
	 * <ul>
	 * <li>Usuário ativo.</li>
	 * <li>Usuário possui certificado definido.</li>
	 * <li>Usuário possui papel ativo.</li>
	 * </ul>
	 * Falso, caso contrário.
	 */
	public boolean verificarCadastroPessoa(Pessoa pessoa, Integer especializacao) {
		boolean retorno = false;
		if (pessoa instanceof PessoaFisica && pessoa.getAtivo() && (((PessoaFisica) pessoa).getEspecializacoes() & PessoaFisica.PRO) == PessoaFisica.PRO) {
			return true;
		}
		if (verificarCadastroUsuario(pessoa)) {
			retorno = pessoa instanceof PessoaFisica && (((PessoaFisica) pessoa).getEspecializacoes() & especializacao) == especializacao;
		}
		return retorno;
	}

	public boolean verificarCadastroPessoa(Pessoa pessoa) {
		return verificarCadastroPessoa(pessoa, false);
	}

	/**
	 * Método responsável por verificar se a {@link Pessoa} está devidamente
	 * cadastrado no PJe.
	 *
	 * @param pessoa {@link Pessoa} cujos atributos serão validadas.
	 * @return Será retornado verdadeiro quando todas as condições abaixo forem
	 * verdadeiras:
	 * <ul>
	 * <li>Usuário ativo.</li>
	 * <li>Usuário possui certificado definido.</li>
	 * <li>Usuário possui algum papel ativo ou é JusPostulandi</li>
	 * </ul>
	 * Falso, caso contrário.
	 */
	public boolean verificarCadastroPessoa(Pessoa pessoa, boolean isReenvioCitacaoExpirada) {
		if (domicilioEletronicoService.isPessoaHabilitada(pessoa) && !isReenvioCitacaoExpirada) {
			return true;
		}

		boolean isPessoaFisicaEspecializacao = (pessoa instanceof PessoaFisica
				&& ((PessoaFisica) pessoa).getEspecializacoes() > 0);

		return verificarCadastroUsuario(pessoa) && (isPessoaFisicaEspecializacao || isJusPostulandi(pessoa));
	}

	/**
	 * Verifica se a {@link Pessoa} possui o papel de Juspostulandi.
	 *
	 * @param pessoa {@link Pessoa}.
	 * @return Verdadeiro se a {@link Pessoa} possui o papel de
	 * <b>Juspostulandi</b>. Falso, caso contrário.
	 */
	private boolean isJusPostulandi(Pessoa pessoa) {
		if (pessoa != null && pessoa.getUsuarioLocalizacaoList() != null) {
			for (UsuarioLocalizacao usuarioLocalizacao : pessoa.getUsuarioLocalizacaoList()) {
				if (usuarioLocalizacao.getPapel().getIdPapel() == ParametroUtil.instance().getPapelJusPostulandi()
						.getIdPapel()) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Método responsável por verificar se o(s) representante(s) da parte
	 * está(ão) devidamente cadastro(s) no PJe.
	 *
	 * @param processoParteRepresentanteList Representante(s) da parte.
	 * @return Será retornado verdadeiro quando todas as condições abaixo forem
	 * verdadeiras para um dado representante:
	 * <ul>
	 * <li>Usuário ativo.</li>
	 * <li>Usuário possui certificado definido.</li>
	 * <li>Usuário possui papel ativo.</li>
	 * </ul>
	 * Falso, caso contrário.
	 */
	private boolean verificarCadastroRepresentantes(List<ProcessoParteRepresentante> processoParteRepresentanteList) {
		if (processoParteRepresentanteList != null) {
			Integer especializacao = null;

			for (ProcessoParteRepresentante processoParteRepresentante : processoParteRepresentanteList) {
				if (processoParteRepresentante.isAtivo()) {
					TipoParte tipoParte = processoParteRepresentante.getTipoRepresentante();

					/*
					 *  Verifica se a representação é feita por um advogado ou procurador.
					 *  Hoje, considera-se apenas advogado ou procurador como representantes da pessoa (física ou jurídica).
					 */
					if (tipoParte.equals(ParametroUtil.instance().getTipoParteAdvogado())) {
						especializacao = PessoaFisica.ADV;
					} else if (tipoParte.equals(ParametroUtil.instance().getTipoParteProcurador())) {
						especializacao = PessoaFisica.PRO;
					}

					if (especializacao == null) { // O representante não é advogado ou procurador.
						if (verificarCadastroPessoa(processoParteRepresentante.getRepresentante())) {
							return true;
						}
					} else { // O representante é advogado ou procurador.
						if (verificarCadastroPessoa(processoParteRepresentante.getRepresentante(), especializacao)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Recupera os meios de comunicação que podem ser utilizados pelo usuário
	 * (RN501).
	 *
	 * @param pessoa {@link Pessoa} que receberá o expediente.
	 * @param processoTrf {@link ProcessoTrf}.
	 * @param intimacaoPessoal Indica se trata de envio pessoal.
	 * @param procuradoria Procuradoria que representa a pessoa no processo.
	 * @return Retorna os meios de comunicação que podem ser utilizados pelo
	 * usuário (RN501).
	 */
	public List<ExpedicaoExpedienteEnum> recuperarMeiosComunicacao(final Pessoa pessoa, final ProcessoTrf processoTrf,
			final boolean intimacaoPessoal, final Procuradoria procuradoria) {

		// Recupera os meios de comunicação padrão.
		final List<ExpedicaoExpedienteEnum> meiosComunicacao = this.getMeiosComunicacaoPadrao();

		if (verificarPossibilidadeIntimacaoEletronica(pessoa, processoTrf, intimacaoPessoal, procuradoria)) {
			meiosComunicacao.add(ExpedicaoExpedienteEnum.E);
		}

		// Implementação da regra RN541
		if (!possuiTelefoneCadastrado(pessoa)) {
			meiosComunicacao.remove(ExpedicaoExpedienteEnum.T);
		}

		return meiosComunicacao;
	}

	public boolean verificarPossibilidadeIntimacaoEletronica(final Integer idProcessoTrf, final String args) {
		return verificarPossibilidadeIntimacaoEletronica(idProcessoTrf, args, false);
	}

	/**
	 * Recebe a informação do processo e uma string de valores separados por vírgula de:
	 * tipo de parte: A (polo ativo), P (polo passivo), T (outros participantes)
	 * Ou CPFs ou CNPJs
	 * Retornará
	 * 	true - se todos os participantes indicados puderem ser intimados eletronicamente
	 *  false - se ao menos 1 dos participantes da consulta não puder ser intimado
	 * Exemplos de uso:
	 * (, "A") -- para o processo xxxxx, verifica se pode intimar todo o polo ativo 
	 * (, "A,P") -- para o processo xxxxx, verifica se pode intimar todo o polo ativo e o polo passivo
	 * (, "A,99999999999") -- para o processo xxxxx, verifica se pode intimar todo o polo ativo e a pessoa de CPF: 99999999999
	 * (, "99999999999,999999999999999") -- para o processo xxxxx, verifica se pode intimar pessoa de CPF: 99999999999 e a PJ de CNPJ 99999999999999
	 * (, "T,99999999999") -- para o processo xxxxx, verifica se pode intimar todas as partes de outros interessados e a pessoa de CPF: 99999999999
	 * 
	 * **serão consideras apenas intimações não pessoais, ou seja, as intimações que podem ser encaminhadas aos representantes também
	 * 
	 * @param idProcessoTrf
	 * @param args
	 * @return
	 */
	public boolean verificarPossibilidadeIntimacaoEletronica(final Integer idProcessoTrf, final String args, final boolean isTipoIntimacao) {
		boolean resultado = Boolean.FALSE;

		if (idProcessoTrf != null && args != null) {
			ProcessoTrf processoTrf = ProcessoTrfManager.instance().find(ProcessoTrf.class, idProcessoTrf);

			if (processoTrf != null) {
				boolean parteIntimavel;
				boolean encontrouPessoa;

				for (String arg : args.split(",")) {
					encontrouPessoa = Boolean.FALSE;
					parteIntimavel = Boolean.FALSE;

					if (obterProcessoParteParticipacaoEnum(arg) != null) {
						List<ProcessoParte> partes = processoTrf.getListaPartePrincipal(true, false,
								obterProcessoParteParticipacaoEnum(arg));

						if (!partes.isEmpty()) {
							encontrouPessoa = Boolean.TRUE;

							for (ProcessoParte parte : partes) {
								if (verificarPossibilidadeIntimacaoEletronica(parte.getPessoa(), processoTrf, false,
										parte.getProcuradoria(), isTipoIntimacao)) {
									parteIntimavel = Boolean.TRUE;
								} else {
									parteIntimavel = Boolean.FALSE;
									break;
								}
							}
						} else {
							encontrouPessoa = Boolean.FALSE;
						}
					} else {
						Procuradoria procuradoria = null;
						Pessoa pessoa = ComponentUtil.getComponent(PessoaManager.class).findByCPFouCNPJ(arg);

						if (pessoa != null) {
							encontrouPessoa = Boolean.TRUE;
							procuradoria = ComponentUtil.getComponent(PessoaProcuradoriaEntidadeManager.class)
									.getProcuradoriaPadraoPessoa(pessoa);

							if (pessoa != null && verificarPossibilidadeIntimacaoEletronica(pessoa, processoTrf, false,
									procuradoria, isTipoIntimacao)) {
								parteIntimavel = Boolean.TRUE;
							} else {
								parteIntimavel = Boolean.FALSE;
								break;
							}
						} else {
							encontrouPessoa = Boolean.FALSE;
						}
					}

					resultado = Boolean.TRUE;

					if (!parteIntimavel || !encontrouPessoa) {
						resultado = Boolean.FALSE;

						break;
					}
				}
			}
		}

		return resultado;
	}

	public boolean verificarPossibilidadeIntimacaoEletronica(final Pessoa pessoa, final ProcessoTrf processoTrf,
			final boolean intimacaoPessoal, final Procuradoria procuradoria) {
		return verificarPossibilidadeIntimacaoEletronica(pessoa, processoTrf, intimacaoPessoal, procuradoria, false);
	}

	/**
	 * 
	 * 
	 * @param pessoa
	 * @param processoTrf
	 * @param intimacaoPessoal
	 * @param procuradoria
	 * @return
	 */
	public boolean verificarPossibilidadeIntimacaoEletronica(final Pessoa pessoa, final ProcessoTrf processoTrf,
			final boolean intimacaoPessoal, final Procuradoria procuradoria, final boolean isTipoIntimacao) {

		boolean resultado = false;
		final ProcessoParte parte = getProcessoParte(true, false, pessoa, processoTrf);

		if (parte != null) {  // A pessoa selecionada é parte de um dos polos do processo.
			resultado = verificarPossibilidadeIntimacaoEletronica(parte, intimacaoPessoal, false, isTipoIntimacao);
		} else { // A pessoa selecionada não é parte de um dos polos do processo, ou seja, é representante ou outros destinatários.
			ProcessoParteRepresentante representante = recuperarRepresentante(processoTrf, pessoa);

			// Verifica se é representante e se o cadastro está de acordo.
			if ((representante != null && verificarCadastroRepresentantes(Arrays.asList(representante)))) {
				resultado = true;
			} else if (verificarCadastroPessoa(pessoa)
					|| (!intimacaoPessoal && isOrgaoRepresentacaoValido(procuradoria))) {
				resultado = true;
			}
		}

		return resultado;
	}

	/**
	 *
	 *
	 * @param parte
	 * @param intimacaoPessoal
	 * @return
	 */
	public boolean verificarPossibilidadeIntimacaoEletronica(final ProcessoParte parte, final boolean intimacaoPessoal) {
		return verificarPossibilidadeIntimacaoEletronica(parte, intimacaoPessoal, false, false);
	}

	public boolean verificarPossibilidadeIntimacaoEletronica(final ProcessoParte parte, final boolean intimacaoPessoal, final boolean isReenvioCitacaoExpirada) {
		return verificarPossibilidadeIntimacaoEletronica(parte, intimacaoPessoal, isReenvioCitacaoExpirada, false);
	}

	/**
	 * 
	 * 
	 * @param parte
	 * @param intimacaoPessoal
	 * @param isReenvioCitacaoExpirada -> indica se é reenvio de citação expirada (citação enviada ao Domicílio sem ciência). Este reenvio é feito automaticamente através do fluxo TCI e não é enviado ao Domicílio Eletrônico.
	 * @param isTipoIntimacao -> indica se o tipo de comunicação é intimação  
	 * @return
	 */
	public boolean verificarPossibilidadeIntimacaoEletronica(final ProcessoParte parte, final boolean intimacaoPessoal, final boolean isReenvioCitacaoExpirada, final boolean isTipoIntimacao) {
		boolean domicilioEletronicoValido = !isTipoIntimacao && validaDomicilioEletronico(parte, isReenvioCitacaoExpirada);

		if (domicilioEletronicoValido) {
			return true;
		}

		boolean isParteAtiva = BooleanUtils.isTrue(parte.getIsAtivo());
		boolean tipoIntimacaoValido = validaTipoIntimacao(parte, intimacaoPessoal, isTipoIntimacao);
		boolean pessoaComCadastro = verificarCadastroPessoa(parte.getPessoa(), isReenvioCitacaoExpirada);

		if (isParteAtiva && tipoIntimacaoValido && pessoaComCadastro) {
			return true;
		}

		boolean representacaoValida = !isTipoIntimacao && !intimacaoPessoal
				&& (verificarCadastroRepresentantes(parte.getProcessoParteRepresentanteList())
						|| isOrgaoRepresentacaoValido(parte.getProcuradoria()));

		return representacaoValida;
	}

	private boolean validaTipoIntimacao(ProcessoParte parte, boolean intimacaoPessoal, boolean isTipoIntimacao) {
		if (!isTipoIntimacao) {
			return true;
		}

		boolean isPessoaJuridicaOrgaoPublico = parte.getPessoa() instanceof PessoaJuridica
				&& ((PessoaJuridica) parte.getPessoa()).getOrgaoPublico();

		return intimacaoPessoal || isPessoaJuridicaOrgaoPublico;
	}

	private boolean validaDomicilioEletronico(ProcessoParte parte, boolean isReenvioCitacaoExpirada) {
		boolean pessoaHabilitada = domicilioEletronicoService.isPessoaHabilitada(parte.getPessoa());

		if (!pessoaHabilitada || isReenvioCitacaoExpirada) {
			return false;
		}

		boolean bloqueioEnvio = domicilioEletronicoService.verificaBloqueioPorCompetencia(parte.getProcessoTrf())
				|| domicilioEletronicoService.existeBloqueioEnvioDomicilio(parte.getPessoa());

		return !bloqueioEnvio;
	}

	/*
	 * Recebe a informação do processo e uma string de valores separados por vírgula de:
	 * tipo de parte: A (polo ativo), P (polo passivo), T (outros participantes)
	 * Ou CPFs ou CNPJs
	 * Retornará
	 * 	true - se todos os participantes indicados forem representados por procuradorias/ ou defensorias
	 *  false - se ao menos 1 dos participantes da consulta não for representado por procuradorias/defensorias
	 * Exemplos de uso para verificacao de representacao por procuradoria:
	 * (, "A") -- para o processo xxxxx, verifica se todo o polo ativo  
	 * (, "A,P") -- para o processo xxxxx, verifica se todo o polo ativo e o polo passivo
	 * (, "A,99999999999") -- para o processo xxxxx, verifica se todo o polo ativo e a pessoa de CPF: 99999999999
	 * (, "99999999999,999999999999999") -- para o processo xxxxx, verifica se a pessoa de CPF: 99999999999 e a PJ de CNPJ 99999999999999
	 * (, "T,99999999999") -- para o processo xxxxx, verifica todas as partes de outros interessados e a pessoa de CPF: 99999999999
	 * 
	 * @param idProcessoTrf
	 * @param args
	 * @return
	 */
	public boolean verificarRepresentacaoProcuradoria(final Integer idProcessoTrf, final String args) {
		boolean resultado = Boolean.FALSE;

		if (idProcessoTrf != null && args != null && !args.trim().isEmpty()) {
			ProcessoTrf processoTrf = ProcessoTrfManager.instance().find(ProcessoTrf.class, idProcessoTrf);

			if (processoTrf != null) {
				boolean encontrouParteSemProcuradoria = false;

				for (String arg : args.split(",")) {
					arg = arg.trim();

					if (obterProcessoParteParticipacaoEnum(arg) != null) {
						List<ProcessoParte> partes = processoTrf.getListaPartePrincipal(true, false,
								obterProcessoParteParticipacaoEnum(arg));

						for (ProcessoParte parte : partes) {
							if (parte.getProcuradoria() != null) {
								resultado = Boolean.TRUE;
							} else {
								resultado = Boolean.FALSE;
								encontrouParteSemProcuradoria = Boolean.TRUE;

								break;
							}
						}
					} else {
						ProcessoParte parte = getProcessoParte(true, false, arg, processoTrf);

						if (parte != null && parte.getProcuradoria() != null) {
							resultado = Boolean.TRUE;
						} else {
							resultado = Boolean.FALSE;
							encontrouParteSemProcuradoria = Boolean.TRUE;

							break;
						}
					}

					if (encontrouParteSemProcuradoria) {
						break;
					}
				}
			}
		}

		return resultado;
	}

	/**
	 * 
	 * 
	 * @param idProcessoTrf
	 * @param args
	 * @return
	 */
	public boolean verificarRepresentacaoAdvogado(final Integer idProcessoTrf, final String args) {
		boolean resultado = Boolean.FALSE;

		if (idProcessoTrf != null && args != null && !args.trim().isEmpty()) {
			ProcessoTrf processoTrf = ProcessoTrfManager.instance().find(ProcessoTrf.class, idProcessoTrf);

			if (processoTrf != null) {
				boolean encontrouParteSemAdvogado = false;

				for (String arg : args.split(",")) {
					arg = arg.trim();

					if (obterProcessoParteParticipacaoEnum(arg) != null) {
						List<ProcessoParte> partes = processoTrf.getListaPartePrincipal(true, false,
								obterProcessoParteParticipacaoEnum(arg));

						for (ProcessoParte parte : partes) {
							if (verificarExistenciaAdvogado(parte.getProcessoParteRepresentanteList())) {
								resultado = Boolean.TRUE;
							} else {
								resultado = Boolean.FALSE;
								encontrouParteSemAdvogado = Boolean.TRUE;

								break;
							}
						}
					} else {
						ProcessoParte parte = getProcessoParte(true, false, arg, processoTrf);

						if (parte != null && verificarExistenciaAdvogado(parte.getProcessoParteRepresentanteList())) {
							resultado = Boolean.TRUE;
						} else {
							resultado = Boolean.FALSE;
							encontrouParteSemAdvogado = Boolean.TRUE;

							break;
						}
					}

					if (encontrouParteSemAdvogado) {
						break;
					}
				}
			}
		}

		return resultado;
	}

	/**
	 * 
	 * 
	 * @param arg
	 * @return
	 */
	private ProcessoParteParticipacaoEnum obterProcessoParteParticipacaoEnum(final String arg) {
		try {
			return ProcessoParteParticipacaoEnum.valueOf(arg);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	/**
	 * 
	 * 
	 * @param processoParteRepresentanteList
	 * @return
	 */
	private boolean verificarExistenciaAdvogado(final List<ProcessoParteRepresentante> processoParteRepresentanteList) {
		boolean resultado = false;

		if (processoParteRepresentanteList != null) {
			for (ProcessoParteRepresentante processoParteRepresentante : processoParteRepresentanteList) {
				if (processoParteRepresentante.isAtivo()) {
					if (processoParteRepresentante.getTipoRepresentante()
							.equals(ParametroUtil.instance().getTipoParteAdvogado())) {

						resultado = true;
						break;
					}
				}
			}
		}

		return resultado;
	}

	/**
	 * Método responsável por recuperar o representante de uma parte do
	 * processo.
	 *
	 * @param processoTrf {@link ProcessoTrf}.
	 * @param pessoa {@link Pessoa}.
	 * @return O representante de uma parte do processo.
	 */
	private ProcessoParteRepresentante recuperarRepresentante(ProcessoTrf processoTrf, Pessoa pessoa) {
		for (ProcessoParte processoParte : getProcessoParteList(false, false, processoTrf)) {
			for (ProcessoParteRepresentante processoParteRepresentante : processoParte
					.getProcessoParteRepresentanteList()) {
				if (processoParteRepresentante.getRepresentante().equals(pessoa)) {
					return processoParteRepresentante; // Obtém o objeto que corresponde ao representante selecionado.
				}
			}
		}

		return null;
	}

	public ProcessoParte getProcessoParte(boolean apenasSituacaoAtivos, boolean incluiInativos, Pessoa pessoa,
			ProcessoTrf processoTrf) {
		final List<ProcessoParte> processoParteList = getProcessoParteList(apenasSituacaoAtivos, incluiInativos,
				processoTrf);

		for (ProcessoParte processoParte : processoParteList) {
			// Verifica se a pessoa selecionada é parte de um dos polos do processo.
			if (processoParte.getPessoa().equals(pessoa)) {
				return processoParte;
			}
		}

		return null;
	}

	/**
	 * Método responsável por retornar a parte do processo.
	 *
	 * @param pessoa      {@link Pessoa}.
	 * @param processoTrf {@link ProcessoTrf}.
	 * @return A parte do processo.
	 */
	public ProcessoParte getProcessoParte(Pessoa pessoa, ProcessoTrf processoTrf) {
		return getProcessoParte(false, false, pessoa, processoTrf);
	}

	public ProcessoParte getProcessoParte(boolean apenasSituacaoAtivos, boolean incluiInativos,
			String numeroIdentificacao, ProcessoTrf processoTrf) {
		Pessoa pessoa = ComponentUtil.getComponent(PessoaManager.class).findByCPFouCNPJ(numeroIdentificacao);

		if (pessoa != null) {
			return getProcessoParte(apenasSituacaoAtivos, incluiInativos, pessoa, processoTrf);
		}

		return null;
	}

	/**
	 * Método responsável por retornar as partes principais do processo (ativo,
	 * passivo e terceiros).
	 *
	 * @param processoTrf {@link ProcessoTrf}.
	 * @return As partes principais do processo (ativo, passivo e terceiros).
	 */
	private List<ProcessoParte> getProcessoParteList(boolean apenasSituacaoAtivos, boolean incluiInativos,
			ProcessoTrf processoTrf) {
		return processoTrf.getListaPartePrincipal(apenasSituacaoAtivos, incluiInativos, ProcessoParteParticipacaoEnum.A,
				ProcessoParteParticipacaoEnum.P, ProcessoParteParticipacaoEnum.T);
	}

	/**
	 * Método responsável por verificar se algum Órgão de Representação está
	 * ativo e possui pelo menos um Procurador/Defensor relacionado com a
	 * situação do perfil ativa e que já tenha confirmado o seu cadastramento
	 * através de login no sistema.
	 *
	 * @param procuradoria Órgão de Representação.
	 * @return Verdadeiro se algum Órgão de Representação está ativo e possui
	 * pelo menos um Procurador/Defensor relacionado com a situação do perfil
	 * ativa e que já tenha confirmado o seu cadastramento através de login no
	 * sistema. Falso, caso contrário.
	 */
	public boolean isOrgaoRepresentacaoValido(Procuradoria procuradoria) {
		if (procuradoria != null && procuradoria.getAtivo()) {  // A Procuradoria/Defensoria deve estar ativa.
			for (PessoaProcuradoria pessoaProcuradoria : procuradoria.getPessoaProcuradoriaList()) {
				// Verifica se o cadastro do Procurador/Defensor está de acordo.
				if (verificarCadastroPessoa(pessoaProcuradoria.getPessoa().getPessoa(), PessoaFisica.PRO)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Método que verifica se a {@link Pessoa} possui telefone cadastrado.
	 *
	 * @param pessoa {@link Pessoa} cujo meio de contato (telefone) será
	 * verificado a existência.
	 * @return Verdadeiro caso a {@link Pessoa} tenha telefone cadastrado. Falso
	 * caso contrário.
	 */
	public boolean possuiTelefoneCadastrado(Pessoa pessoa) {
		boolean resultado = false;

		if (pessoa != null) {
			List<MeioContato> meioContatoList = pessoa.getMeioContatoList();

			if (meioContatoList != null) {
				for (MeioContato meioContato : meioContatoList) {
					if (meioContato.getTipoContato().getTipoContato().toLowerCase().contains("telefone")) {
						resultado = true;
					}
				}
			}
		}

		return resultado;
	}

	public ProcessoParteExpediente getAtoPessoal(ProcessoExpediente pe, Pessoa p) {
		return getAtoPessoal(pe, p, null);
	}

	public ProcessoParteExpediente getAtoPessoal(ProcessoExpediente pe, Pessoa p,
			Procuradoria procuradoriaSelecionada) {
		ProcessoParteExpediente ret = this.processoExpedienteManager.getExpedientePessoal(pe, p);
		if (ret == null) {
			ret = this.processoParteExpedienteManager.getExpedientePessoal(p, procuradoriaSelecionada);
			ret.setProcessoExpediente(pe);

			pe.getProcessoParteExpedienteList().add(ret);
		}

		return ret;
	}

	public ProcessoParteExpediente getAtoPessoal(ProcessoExpediente pe, ProcessoParte pp) {
		ProcessoParteExpediente ret = this.processoExpedienteManager.getExpedientePessoal(pe, pp);
		if (ret == null) {
			ret = this.processoParteExpedienteManager.getExpedientePessoal(pp);

			ret.setProcuradoria(pp.getProcuradoria());

			pe.getProcessoParteExpedienteList().add(ret);

		}

		return ret;
	}

	public void vincularDocumentos(ProcessoExpediente pe, Collection<ProcessoDocumento> documentosVinculados) {
		pe.getProcessoDocumentoExpedienteList().clear();

		for (ProcessoDocumento pd : documentosVinculados) {
			ProcessoDocumentoExpediente pde = processoDocumentoExpedienteManager.getDocumentoReferido(pe, pd);

			pde.setAnexo(true);
			pde.setProcessoExpediente(pe);

			pe.getProcessoDocumentoExpedienteList().add(pde);
		}
	}
	
	
	/* Vincula documentos ao expediente sem limpar a lista de documentos */
	public void vincularDocumentosSemLimpar(ProcessoExpediente pe, Collection<ProcessoDocumento> documentosVinculados) {
 
		for (ProcessoDocumento pd : documentosVinculados) {
			ProcessoDocumentoExpediente pde = processoDocumentoExpedienteManager.getDocumentoReferido(pe, pd);

			pde.setAnexo(true);
			pde.setProcessoExpediente(pe);

			pe.getProcessoDocumentoExpedienteList().add(pde);
		}
	}

	public boolean validaExpediente(ProcessoExpediente pe, Pessoa destinatario) {
		return validaExpediente(pe, destinatario, null);
	}

	public boolean validaExpediente(ProcessoExpediente pe, Pessoa destinatario, Procuradoria procuradoriaSelecionada) {
		if (!pe.getProcessoDocumento().getProcessoDocumentoBin().isBinario()
				&& (pe.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento() == null
						|| pe.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento().isEmpty())) {
			return false;
		}

		ProcessoParteExpediente ppe = getAtoPessoal(pe, destinatario, procuradoriaSelecionada);

		switch (pe.getMeioExpedicaoExpediente()) {
		case C:
		case M:
		case L:
			if (ppe.getProcessoParteExpedienteEnderecoList().size() == 0) {
				return false;
			}

			break;
		default:
			break;
		}

		return true;
	}

	/**
	 * Método criado para permitir executar a tarefa (task) no contexto de 
	 * chamada do método que retornou a tarefa. Isto é essencial para execuções 
	 * postergardas.
	 * @param task a tarefa a ser executada, originalmente retornada por um 
	 * método deste componente.
	 */
	public void runTask(Runnable task) {
		task.run();
	}

	/**
	 * Método criado para permitir executar a tarefa (task) no contexto de 
	 * chamada do método que retornou a tarefa. Isto é essencial para execuções 
	 * postergardas.
	 * @param <V> o tipo do resultado a ser devolvido.
	 * @param task a tarefa a ser executada, originalmente retornada por um 
	 * método deste componente.
	 * @return o resultado da tarefa.
	 * @throws Exception para o caso de uma exceção ser produzida no momento da 
	 * execução da tarefa, essa exceção é propagada.
	 */
	public <V> V runTask(Callable<V> task) throws Exception {
		V v = task.call();

		return v;
	}

	/**
	 * Finaliza uma lista de atos de comunicação
	 *
	 * @param expedientes Lista de expedientes que serão gerados
	 * @param assinaturas Lista de assinaturas dos documentos destes expedientes
	 * @param certChain Chave pública do certificado que está assinando os
	 * expedientes
	 * @param processoJudicial Processo Judicial onde estes expedientes serão
	 * gerados
	 * @param jbpmTask Código da instância da tarefa atual (idTaskInstance)
	 */
	public List<ProcessoExpediente> finalizarAtosComunicacao(ProcessoExpediente[] expedientes, String[] assinaturas,
			String certChain, ProcessoTrf processoJudicial, Long jbpmTask, List<ParAnexo> anexosPdf)
			throws PJeBusinessException {
		return finalizarAtosComunicacao(expedientes, assinaturas, certChain, processoJudicial, jbpmTask, anexosPdf,
				true);
	}

	/**
	 * Finaliza uma lista de atos de comunicação
	 *
	 * @param expedientes Lista de expedientes que serão gerados
	 * @param assinaturas Lista de assinaturas dos documentos destes expedientes
	 * @param certChain Chave pública do certificado que está assinando os
	 * expedientes
	 * @param processoJudicial Processo Judicial onde estes expedientes serão
	 * gerados
	 * @param jbpmTask Código da instância da tarefa atual (idTaskInstance)
	 * @param autoFlush se o método deve automaticamente invocar o flush no EntityManager.
	 */
	public List<ProcessoExpediente> finalizarAtosComunicacao(ProcessoExpediente[] expedientes, String[] assinaturas,
			String certChain, ProcessoTrf processoJudicial, Long jbpmTask, List<ParAnexo> anexosPdf, boolean autoFlush)
			throws PJeBusinessException {

		/* Verifica se ao menos um expediente foi enviado */
		if (expedientes == null || expedientes.length == 0) {
			throw new PJeBusinessException("pje.atoComunicacaoService.error.semExpedientes");
		}

		/* Verifica se existe uma assinatura para cada documento de cada expediente */
		if (expedientes.length != assinaturas.length) {
			throw new PJeBusinessException("pje.atoComunicacaoService.error.numeroAssinaturasDivergente", null,
					expedientes.length, assinaturas.length);
		}

		/* Verifica se cada documento de cada expediente foi elaborado corretamente */
		this.validarDocumentosExpedientes(expedientes);

		// Cria uma lista para guardar os documentos do expediente vinculados a outros
		// processos e remove-os do expediente para posterior vinculação ao processo e
		// expedientes atuais
		List<ProcessoDocumentoExpediente> pdOutrosProcessos = new ArrayList<ProcessoDocumentoExpediente>();

		for (int i = 0; i < expedientes.length; i++) {
			ProcessoExpediente pe = expedientes[i];

			for (int j = 0; j < pe.getProcessoDocumentoExpedienteList().size(); j++) {
				ProcessoDocumentoExpediente aux = pe.getProcessoDocumentoExpedienteList().get(j);
				if (aux.getProcessoDocumento().getProcesso().getIdProcesso() != processoJudicial.getIdProcessoTrf()) {
					// adiciona apenas na primeira iteração (primeiro expediente) para evitar
					// duplicação
					if (i == 0) {
						pdOutrosProcessos.add(aux);
					}

					pe.getProcessoDocumentoExpedienteList().remove(j);

					j--;
				}
			}
		}

		if (anexosPdf != null) {
			for (ParAnexo parAnexo : anexosPdf) {
				parAnexo.getPdPdf().getProcessoDocumentoBin().setCertChain(certChain);
				parAnexo.getPdPdf().getProcessoDocumentoBin().setSignature(assinaturas[0]);
			}
		}

		// persiste os documentos de outros processos como documentos do processo atual
		for (ProcessoDocumentoExpediente aux : pdOutrosProcessos) {
			ProcessoDocumento novoPd = copiaDocumentoOutroProcesso(aux.getProcessoDocumento(), processoJudicial);

			// vincula o novo documento a todos os expedientes
			for (int i = 0; i < expedientes.length; i++) {
				ProcessoExpediente pe = expedientes[i];
				ProcessoDocumentoExpediente pde = processoDocumentoExpedienteManager.getDocumentoReferido(pe, novoPd);

				pde.setAnexo(true);
				pde.setProcessoExpediente(pe);
				pe.getProcessoDocumentoExpedienteList().add(pde);
			}
		}

		List<ProcessoExpediente> expedientesGerados = new ArrayList<>(0);
		/* Gera cada um dos expedientes */
		for (int i = 0; i < expedientes.length; i++) {

			ProcessoExpediente expediente = expedientes[i];

			// Adiciona assinatura em todos os documentos criados na própria funcionalidade
			if (!expedientes[i].getDocumentoExistente()) {
				ProcessoDocumento pd = expedientes[i].getProcessoDocumento();
				pd.getProcessoDocumentoBin().setCertChain(certChain);
				pd.getProcessoDocumentoBin().setSignature(assinaturas[i]);
				if (pd.getProcesso() == null) {
					pd.setProcesso(processoJudicial.getProcesso());
				}
			} else {
				// Necessário porque o objeto pode ter sido serializado e recuperado em outro EntityManager
				ProcessoDocumentoBin documentoBinAtual = expediente.getProcessoDocumento().getProcessoDocumentoBin();
				ProcessoDocumentoBin docBin = processoDocumentoBinManager
						.findById(documentoBinAtual.getIdProcessoDocumentoBin());

				expediente.getProcessoDocumento().setProcessoDocumentoBin(docBin);
			}
			
			ProcessoExpediente pe = this.finalizarAtoComunicacao(expediente, processoJudicial, jbpmTask, anexosPdf, autoFlush);
			expedientesGerados.add(pe);
			
		}

		// Verifica se a Integração com o Domicílio Eletrônico está habilitada
		// Verifica se a lista não nula ou não vazia
		if (Boolean.TRUE.equals(domicilioEletronicoService.isIntegracaoHabilitada()) && 
				expedientesGerados != null && 
				!expedientesGerados.isEmpty()) {
			domicilioEletronicoService.enviarExpedientesAsync(expedientesGerados);
		}

		if (autoFlush) {
			processoDocumentoBinManager.flush(); // Sincroniza os dados em memória com a base de dados.
		}

		return expedientesGerados;

	}

	public ProcessoExpediente finalizarAtoComunicacao(ProcessoExpediente pe, ProcessoTrf processo, Long jbpmTask,
			List<ParAnexo> anexosPdf) throws PJeBusinessException, MuralException {
		return finalizarAtoComunicacao(pe, processo, jbpmTask, anexosPdf, true);
	}

	public ProcessoExpediente finalizarAtoComunicacao(ProcessoExpediente pe, ProcessoTrf processo, Long jbpmTask,
			List<ParAnexo> anexosPdf, boolean autoFlush) throws PJeBusinessException {
		Pessoa p = ComponentUtil.getComponent(ParametroUtil.class).getPessoaSistema();

		ProcessoDocumento pd = null;

		if (p != null && p.getIdPessoa() != null && pe.getProcessoDocumento() != null
				&& pe.getProcessoDocumento().getProcessoDocumentoBin() != null
				&& pe.getProcessoDocumento().getProcessoDocumentoBin().getUsuario() != null && pe.getProcessoDocumento()
						.getProcessoDocumentoBin().getUsuario().getIdUsuario().equals(p.getIdUsuario())) {

			PapelService papelService = ComponentUtil.getComponent(PapelService.class);

			Papel papelUsuarioSistema = papelService.findByCodeName(Papeis.SISTEMA);

			Localizacao localizacaoSistema = ParametroUtil.instance().getLocalizacaoTribunal();

			pd = documentoJudicialService.finalizaDocumento(pe.getProcessoDocumento(), processo, jbpmTask, true,
					!pe.getDocumentoExistente(), true, p, localizacaoSistema, papelUsuarioSistema, true);
		} else {
			pd = documentoJudicialService.finalizaDocumento(pe.getProcessoDocumento(), processo, jbpmTask, true,
					!pe.getDocumentoExistente());
		}

		pe.setTipoProcessoDocumento(pe.getProcessoDocumento().getTipoProcessoDocumento());
		pe.setProcessoDocumento(pd);

		ProcessoDocumentoExpediente pde = processoDocumentoExpedienteManager.getDocumentoReferido(pe,
				pe.getProcessoDocumento());

		pe.getProcessoDocumentoExpedienteList().add(pde);
		pe.setProcessoTrf(processo);
		pe.setDtCriacao(new Date());

		Calendario calendario = prazosProcessuaisService.obtemCalendario(processo.getOrgaoJulgador());

		Procuradoria procuradoria = null;

		List<Pessoa> advogados = new ArrayList<Pessoa>(0);
		List<Pessoa> visualizadoresProcesso = new ArrayList<>();

		for (ProcessoParteExpediente ppe : pe.getProcessoParteExpedienteList()) {
			processoParteExpedienteManager.preparaComunicacao(ppe, pe, processo, calendario);

			if (!ppe.getIntimacaoPessoal()) {
				try {
					advogados = processoJudicialService.recuperaAdvogados(processo, ppe.getPessoaParte());
				} catch (PJeBusinessException ex) {
					advogados = new ArrayList<>();
				}

				for (Pessoa advogado : advogados) {
					if (!visualizadoresProcesso.stream().anyMatch((v) -> v.getIdPessoa() == advogado.getIdPessoa())) {
						processoJudicialService.acrescentaVisualizador(processo, advogado, null, autoFlush);
						visualizadoresProcesso.add(advogado);
					}
				}

				ProcessoParte processoParte = processoParteManager.findProcessoParte(processo, ppe.getPessoaParte(),
						false);

				if (processoParte == null) {
					procuradoria = ppe.getProcuradoria();
				} else {
					procuradoria = processoParte.getProcuradoria();
				}
			}
			if (!visualizadoresProcesso.stream()
					.anyMatch((v) -> v.getIdPessoa() == ppe.getPessoaParte().getIdPessoa())) {
				processoJudicialService.acrescentaVisualizador(processo, ppe.getPessoaParte(), procuradoria, autoFlush);

				visualizadoresProcesso.add(ppe.getPessoaParte());

				if (autoFlush) {
					EntityUtil.flush();
				}
			}
		}

		visualizadoresProcesso = null;

		pe.setInTemporario(false);
		pe = processoExpedienteManager.persist(pe);

		if (autoFlush) {
			EntityUtil.flush();
		}

		Date hoje = new Date();

		// Persiste e vincula os anexos ao expediente e ao processo
		if (anexosPdf != null) {
			for (ParAnexo parAnexo : anexosPdf) {
				ProcessoDocumento pdPrincipal = pe.getProcessoDocumento();
				ProcessoDocumento pdPdf = new ProcessoDocumento(parAnexo.getPdPdf());

				pdPdf.setInstancia(ParametroUtil.instance().getInstancia());
				pdPdf.setDataJuntada(hoje);
				pdPdf.setIdProcessoDocumento(0);
				pdPdf.setDocumentoPrincipal(pdPrincipal);
				pdPrincipal.getDocumentosVinculados().add(pdPdf);

				gravarPdf(pdPrincipal, pdPdf, parAnexo.getConteudo(), hoje, autoFlush);

				pde = processoDocumentoExpedienteManager.getDocumentoReferido(pe, pdPdf);

				pde.setAnexo(true);

				pde.setProcessoExpediente(pe);

				pe.getProcessoDocumentoExpedienteList().add(pde);

				processoDocumentoExpedienteManager.persist(pde);
			}
		}

		if (pe.getMeioExpedicaoExpediente().equals(ExpedicaoExpedienteEnum.R)) {
			List<ProcessoParteExpediente> processoPartesExpediente = pe.getProcessoParteExpedienteList();
			List<Pessoa> pessoasExpediente = new ArrayList<>();
			List<ProcessoParte> partesDoExpediente = new ArrayList<>();

			for (ProcessoParteExpediente pessoaExpediente : processoPartesExpediente) {
				pessoasExpediente.add(pessoaExpediente.getPessoaParte());
			}

			for (ProcessoParte parte : processo.getProcessoParteList()) {
				if (pessoasExpediente.contains(parte.getPessoa())) {
					partesDoExpediente.add(parte);
				}
			}
		}

		return pe;
	}

	private ProcessoDocumento copiaDocumentoOutroProcesso(ProcessoDocumento pdOutroProcesso,
			ProcessoTrf processoJudicial) throws PJeBusinessException {
		Date hoje = new Date();

		ProcessoDocumentoBin fromPdb = processoDocumentoBinManager
				.findById(pdOutroProcesso.getProcessoDocumentoBin().getIdProcessoDocumentoBin());

		// insere em tb_processo_documento_bin
		ProcessoDocumentoBin toPdb = processoDocumentoBinManager.inserirProcessoDocumentoBin(hoje,
				fromPdb.getModeloDocumento());

		// copia a(s) assinatura(s)
		List<ProcessoDocumentoBinPessoaAssinatura> fromAssinaturas = processoDocumentoBinManager
				.obtemAssinaturas(fromPdb);
		List<ProcessoDocumentoBinPessoaAssinatura> assinaturasCopiadas = new ArrayList<ProcessoDocumentoBinPessoaAssinatura>();

		for (ProcessoDocumentoBinPessoaAssinatura pdba : fromAssinaturas) {
			ProcessoDocumentoBinPessoaAssinatura assinatura = new ProcessoDocumentoBinPessoaAssinatura();
			assinatura.setPessoa(pdba.getPessoa());
			assinatura.setNomePessoa(pdba.getNomePessoa());
			assinatura.setAssinatura(pdba.getAssinatura());
			assinatura.setCertChain(pdba.getCertChain());
			assinatura.setProcessoDocumentoBin(toPdb);
			assinatura.setDataAssinatura(pdba.getDataAssinatura());
			assinaturasCopiadas.add(assinatura);
		}

		toPdb.setValido(fromPdb.getValido());
		toPdb.setCertChain(fromPdb.getCertChain());
		toPdb.setSignature(fromPdb.getSignature());
		toPdb.setDataAssinatura(fromPdb.getDataAssinatura());
		toPdb.getSignatarios().addAll(assinaturasCopiadas);

		// grava a(s) assinatura(s)
		for (ProcessoDocumentoBinPessoaAssinatura pdba : fromAssinaturas) {
			processoDocumentoBinPessoaAssinaturaManager.persist(pdba);
		}

		// atualiza o processoDocumentoBin
		processoDocumentoBinManager.persist(toPdb);

		// grava o novo documento
		ProcessoDocumento novoPd = new ProcessoDocumento();

		novoPd.setProcessoDocumento(pdOutroProcesso.getProcessoDocumento());
		novoPd.setDataInclusao(hoje);
		novoPd.setProcessoDocumentoBin(toPdb);
		novoPd.setTipoProcessoDocumento(pdOutroProcesso.getTipoProcessoDocumento());
		novoPd.setAtivo(pdOutroProcesso.getAtivo());
		novoPd.setDocumentoSigiloso(pdOutroProcesso.getDocumentoSigiloso());
		novoPd.setInstancia(pdOutroProcesso.getInstancia());

		novoPd = processoDocumentoManager.inserirProcessoDocumento(novoPd, processoJudicial, toPdb);

		return novoPd;
	}

	private ProcessoDocumento copiaDocumento(ProcessoDocumento pdOriginal) throws PJeBusinessException {
		Date hoje = new Date();
		// grava o novo documento
		ProcessoDocumento novoPd = new ProcessoDocumento();

		novoPd.setProcessoDocumento(pdOriginal.getProcessoDocumento());
		novoPd.setDataInclusao(hoje);
		novoPd.setProcessoDocumentoBin(pdOriginal.getProcessoDocumentoBin());
		novoPd.setTipoProcessoDocumento(pdOriginal.getTipoProcessoDocumento());
		novoPd.setAtivo(pdOriginal.getAtivo());
		novoPd.setDocumentoSigiloso(pdOriginal.getDocumentoSigiloso());
		novoPd.setInstancia(pdOriginal.getInstancia());

		novoPd = processoDocumentoManager.inserirProcessoDocumento(novoPd, pdOriginal.getProcessoTrf(),
				pdOriginal.getProcessoDocumentoBin());

		return novoPd;
	}

	private ProcessoDocumento gravarPdf(ProcessoDocumento processoDocumento, ProcessoDocumento pdPdf, byte[] conteudo,
			Date data, boolean autoFlush) throws PJeBusinessException {
		// cria nova instância de ProcessoDocumentoBin a partir do 
		ProcessoDocumentoBin processoDocumentoBin = new ProcessoDocumentoBin(pdPdf.getProcessoDocumentoBin());

		// Inserindo o bináriono storage
		processoDocumentoBin.setNumeroDocumentoStorage(documentoBinManager.persist(conteudo, "application/pdf"));

		processoDocumentoBin = processoDocumentoBinManager.persist(processoDocumentoBin);

		processoDocumentoBinManager.finalizaProcessoDocumentoBin(processoDocumentoBin,
				processoDocumento.getTipoProcessoDocumento(),
				pessoaFisicaService.find(usuarioService.getUsuarioLogado().getIdUsuario()));

		if (autoFlush) {
			processoDocumentoBinManager.flush();
		}

		// Inserindo o processoDocumento
		pdPdf.setProcessoDocumentoBin(processoDocumentoBin);

		if (autoFlush) {
			processoDocumentoManager.persistAndFlush(pdPdf);
		} else {
			processoDocumentoManager.persist(pdPdf);
		}

		// Inserindo o processoDocumentoAssociacao
		ProcessoDocumentoAssociacao pda = new ProcessoDocumentoAssociacao();

		pda.setProcessoDocumento(processoDocumento);
		pda.setDocumentoAssociado(pdPdf);

		EntityUtil.getEntityManager().persist(pda);

		if (autoFlush) {
			EntityUtil.flush();
		}

		return pdPdf;
	}

	public boolean exigeAssinatura(ProcessoExpediente pe) {
		return documentoJudicialService.exigeAssinatura(pe.getProcessoDocumento());
	}

	public boolean possivelSignatario(ProcessoExpediente pe) {
		return documentoJudicialService.possivelSignatario(pe.getProcessoDocumento(), Authenticator.getUsuarioLogado());
	}
	
	@Transactional
	public void registraCienciaAutomatica(Integer idExpediente, Date date, Integer prazoPresuncaoCorreios,
			Map<Integer, Calendario> mapaCalendarios, boolean forcarPresuncaoCorreios, boolean forcarAtualizacaoCiencia) throws PJeBusinessException, PjeRestClientException, PessoaInvalidaException {
		cienciaAutomatica.registraCienciaAutomatica(idExpediente, date, prazoPresuncaoCorreios, mapaCalendarios, forcarPresuncaoCorreios, forcarAtualizacaoCiencia, false);
	}

	/**
	 * Registra a ciência automática para todos os atos de comunicação do tipo
	 * eletrônico, considerando as normas relativas à expiração do prazo de
	 * graça de que trata a Lei n. 11.419/2006.
	 *
	 * O método presume que eventual verificação de indisponibilidade já foi
	 * previamente executado e gerou os pertinentes eventos de calendário
	 * relativos à suspensão ou interrupção de prazos.
	 *
	 * @param date o momento limite para a realização da ciência pessoal.
	 * @throws PjeRestClientException 
	 * @throws PessoaInvalidaException 
	 */
	@Transactional
	public void registraCienciaAutomatica(Integer idExpediente, Date date, Integer prazoPresuncaoCorreios,
			Map<Integer, Calendario> mapaCalendarios, boolean forcarPresuncaoCorreios, boolean forcarAtualizacaoCiencia, boolean flush) throws PJeBusinessException, PjeRestClientException, PessoaInvalidaException {
		Date dataFinal = new Date();
		ProcessoParteExpediente ppe = getAtoComunicacaoPessoal(idExpediente);
		if (((ppe.getProcessoExpediente().getMeioExpedicaoExpediente() != ExpedicaoExpedienteEnum.E) // não é eletrônico 
				&& (ppe.getProcessoExpediente().getMeioExpedicaoExpediente() != ExpedicaoExpedienteEnum.C) // não é correios
				&& (ppe.getProcessoExpediente().getMeioExpedicaoExpediente() != ExpedicaoExpedienteEnum.G)) // não é telegrama

				|| (ppe.getDtCienciaParte() != null && !forcarAtualizacaoCiencia)) { // já houve a ciência, mas se solicitou forçar novo registro
			return;
		}
		
		Calendario calendario = null;
		if (mapaCalendarios == null || mapaCalendarios.isEmpty()) {
			calendario = prazosProcessuaisService.obtemCalendario(ppe.getProcessoJudicial().getOrgaoJulgador());
		} else {
			calendario = mapaCalendarios.get(ppe.getProcessoJudicial().getOrgaoJulgador().getIdOrgaoJulgador());
		}

		if (calendario == null) {
			throw new PJeBusinessException("Não há calendario de eventos para o órgão julgador.");
		}
		//Calculo da presunção de prazos para MEIO CORREIOS 
		if (ppe.getProcessoExpediente().getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.C
				&& (forcarPresuncaoCorreios || (prazoPresuncaoCorreios != null && prazoPresuncaoCorreios > 0))) {
			if (ppe.getProcessoExpediente().getProcessoTrf().getOrgaoJulgador().getPresuncaoCorreios() != null) {
				dataFinal = prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(ppe.getDataDisponibilizacao(), calendario, Integer.parseInt(ppe.getProcessoExpediente().getProcessoTrf().getOrgaoJulgador().getPresuncaoCorreios()), ppe.getProcessoExpediente().getProcessoTrf().getCompetencia().getCategoriaPrazoCiencia(), ContagemPrazoEnum.C);
			} else if (prazoPresuncaoCorreios != null && prazoPresuncaoCorreios > 0) {
				dataFinal = prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(ppe.getDataDisponibilizacao(), calendario, prazoPresuncaoCorreios, ppe.getProcessoExpediente().getProcessoTrf().getCompetencia().getCategoriaPrazoCiencia(), ContagemPrazoEnum.C);
			} else {
				return;
			}
			// Cálculo da presunção de prazos para o meio Telegrama:
		} else if (ppe.getProcessoExpediente().getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.G) {
			return;
		} else if (ppe.getTipoPrazo() == TipoPrazoEnum.C && ppe.getDtPrazoLegal().before(prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(ppe.getDataDisponibilizacao(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.C))) { // Data Certa
			dataFinal = ppe.getDtPrazoLegal();
			date = new Date(System.currentTimeMillis());
		} else {
			dataFinal = cienciaAutomatica.obterDataPrazoLegalCiencia(ppe, prazoPresuncaoCorreios, mapaCalendarios, forcarPresuncaoCorreios);
		}
		if (dataFinal.before(date)) {
			if (domicilioEletronicoService.isCitacaoEnviadaDomicilioEletronico(ppe) && !domicilioEletronicoService.isPessoaJuridicaDeDireitoPublico(ppe.getPessoaParte().getDocumentoCpfCnpj())) {
				processoParteExpedienteManager.fecharExpediente(ppe);
				// TJRJ criou o fluxo TCI que já faz esse controle
				// domicilioEletronicoService.criarFluxoCitacaoExpiradaAsync(ppe);
			}
			else {
				ppe.setCienciaSistema(true);
				try {
					processoParteExpedienteManager.registraCiencia(ppe, dataFinal, forcarAtualizacaoCiencia, calendario, flush);
				} catch (Exception e) {
					logger.error("{registraCiencia} Erro ao transitar processo: " + e.getMessage());
					logger.error(e);
				}
			}
		} else {
			ppe.setDtPrazoLegal(dataFinal);
			processoParteExpedienteManager.persist(ppe);
			if(flush){
				processoParteExpedienteManager.flush();
			}
		}
	}

	public List<ProcessoParteExpediente> recuperaExpedientesPendentes(ProcessoTrf processoJudicial) {
		return processoParteExpedienteManager.getAtosComunicacaoPendentesCiencia(processoJudicial);
	}

	/**
	 * Registra que um ato de comunicação foi considerado como comunicado a seu
	 * destinatário pelo sistema.
	 *
	 * @param expedientes lista de expedientes sobre os quais deveria ser registrada
	 *                    a ciência
	 */
	public void registraCienciaAutomatizada(List<ProcessoParteExpediente> expedientes) {
		registraCienciaAutomatizada(new Date(), expedientes);
	}

	/**
	 * Registra que um ato de comunicação foi considerado como comunicado a seu
	 * destinatário pelo sistema.
	 *
	 * @param dataCiencia a data considerada como de ciência
	 * @param expedientes lista de expedientes sobre os quais deverá ser
	 * registrada a ciência
	 */
	public void registraCienciaAutomatizada(Date dataCiencia, List<ProcessoParteExpediente> expedientes) {
		registraCienciaAutomatizada(dataCiencia, false, expedientes);
	}

	/**
	 * Registra que um ato de comunicação foi considerado como comunicado a seu
	 * destinatário pelo sistema.
	 *
	 * @param dataCiencia a data considerada como de ciência
	 * @param force indicativo de que a data de ciência eventualmente já
	 * existente deverá ser substituída pela nova data
	 * @param expedientes lista de expedientes sobre os quais deverá ser
	 * registrada a ciência
	 */
	public void registraCienciaAutomatizada(Date dataCiencia, boolean force, List<ProcessoParteExpediente> expedientes) {
		registraCiencia(dataCiencia, force, true, expedientes, true);
	}
	
	/**
	 * Registra que um ato de comunicação foi considerado como comunicado a seu
	 * destinatário pelo sistema.
	 *
	 * @param dataCiencia a data considerada como de ciência
	 * @param force indicativo de que a data de ciência eventualmente já
	 * existente deverá ser substituída pela nova data
	 * @param expedientes lista de expedientes sobre os quais deverá ser
	 * registrada a ciência
	 * @param isIntegrarDomicilio True indica que a ciência será sinalizada 
	 * também ao Domicílio Eletrônico.
	 */
	public void registraCienciaAutomatizada(Date dataCiencia, boolean force, List<ProcessoParteExpediente> expedientes, boolean isIntegrarDomicilio) {
		registraCiencia(dataCiencia, force, true, expedientes, isIntegrarDomicilio);
	}

	/**
	 * Registra a existência de conhecimento de uma comunicação por seu
	 * destinátario no momento da chamada.
	 *
	 * @param expediente Expediente sobre o qual deverá ser registrada a
	 * ciência.
	 */
	public void registraCienciaPessoal(ProcessoParteExpediente expediente) {
		registraCienciaPessoal(new Date(), Arrays.asList(expediente));
	}

	/**
	 * Registra a existência de conhecimento de uma comunicação por seu
	 * destinátario no momento da chamada.
	 *
	 * @param expediente Expediente sobre o qual deverá ser registrada a ciência.
	 * @param isCienciaDomicilio é ciência pessoal via domicílio eletrônico.
	 */
	public void registraCienciaPessoal(ProcessoParteExpediente expediente, boolean isCienciaDomicilio) {
		registraCienciaPessoal(new Date(), Arrays.asList(expediente), isCienciaDomicilio);
	}

	/**
	 * Registra a existência de conhecimento de uma comunicação por seu
	 * destinátario. Existem casos onde o job verificador falhou em lançar a
	 * ciencia automatica. neste caso, este metodo deverá executar a ciencia
	 * automatica que o job deixou de executar, somente para o documento passado
	 * em parametro. a verificacao deverá observar a data maxima (DT_MAX) para
	 * ciencia. se a DT_MAX for maior que a data atual, a ciencia pessoal é
	 * lançada. se a DT_MAX for menor que a data atual, a ciencia automatica é
	 * lançada.
	 *
	 *
	 * @param expediente Expediente sobre o qual deverá ser registrada a
	 * ciência.
	 */
	public void registraCienciaNaResposta(ProcessoParteExpediente expediente) {
		if (expediente.getDtCienciaParte() == null
				&& (expediente.getDtPrazoLegal() != null && expediente.getDtPrazoLegal().before(new Date()))) {
			registraCienciaAutomatizada(expediente.getDtPrazoLegal(), false, Arrays.asList(expediente));
		} else {
			registraCienciaPessoal(new Date(), Arrays.asList(expediente));
		}
	}

	/**
	 * Registra a existência de conhecimento de uma comunicação por seu
	 * destinátario no momento da chamada.
	 *
	 * @param expedientes lista de expedientes sobre os quais deverá ser
	 * registrada a ciência
	 */
	public void registraCienciaPessoal(ProcessoParteExpediente... expedientes) {
		registraCienciaPessoal(new Date(), Arrays.asList(expedientes));
	}

	public void registraCienciaPessoal(Integer idAtoComunicacaoPessoal) {
		try {
			ProcessoParteExpediente expediente = verificadorPeriodicoComum
					.getAtoComunicacaoPessoal(idAtoComunicacaoPessoal);

			registraCienciaPessoal(expediente);
		} catch (PJeBusinessException e) {
			logger.error("Não foi possível registrar a ciência para o ato de comunicação pessoal com id [{0}]",
					idAtoComunicacaoPessoal);
		}
	}

	/**
	 * Registra a existência de conhecimento de uma comunicação por seu
	 * destinátario no momento da chamada.
	 *
	 * @param expedientes lista de expedientes sobre os quais deverá ser
	 * registrada a ciência
	 */
	public void registraCienciaPessoal(List<ProcessoParteExpediente> expedientes) {
		registraCienciaPessoal(new Date(), expedientes);
	}

	/**
	 * Registra a existência de conhecimento de uma comunicação por seu
	 * destinátario no momento da chamada.
	 *
	 * @param expedientes lista de expedientes sobre os quais deverá ser registrada a ciência
	 * @param isCienciaDomicilio é ciência pessoal via domicílio eletrônico.
	 */
	public void registraCienciaPessoal(List<ProcessoParteExpediente> expedientes, boolean isCienciaDomicilio) {
		registraCienciaPessoal(new Date(), expedientes, isCienciaDomicilio);
	}

	/**
	 * Registra a existência de conhecimento de uma comunicação por seu
	 * destinátario.
	 *
	 * @param dataCiencia a data considerada para ciência
	 * @param expedientes lista de expedientes sobre os quais deverá ser
	 * registrada a ciência
	 */
	public void registraCienciaPessoal(Date dataCiencia, List<ProcessoParteExpediente> expedientes) {
		registraCienciaPessoal(dataCiencia, false, expedientes);
	}

	/**
	 * Registra a existência de conhecimento de uma comunicação por seu
	 * destinátario.
	 *
	 * @param dataCiencia a data considerada para ciência
	 * @param expedientes lista de expedientes sobre os quais deverá ser registrada a ciência
	 * @param isCienciaDomicilio é ciência pessoal via domicílio eletrônico.
	 */
	public void registraCienciaPessoal(Date dataCiencia, List<ProcessoParteExpediente> expedientes, boolean isCienciaDomicilio) {
		registraCienciaPessoal(dataCiencia, false, expedientes, isCienciaDomicilio);
	}

	/**
	 * Registra a existência de conhecimento de uma comunicação por seu
	 * destinátario.
	 *
	 * @param dataCiencia a data considerada para ciência
	 * @param force indicativo de que a data de ciência eventualmente já
	 * existente deverá ser substituída pela nova data
	 * @param expedientes lista de expedientes sobre os quais deverá ser
	 * registrada a ciência
	 */
	public void registraCienciaPessoal(Date dataCiencia, boolean force, List<ProcessoParteExpediente> expedientes) {
		registraCiencia(dataCiencia, force, false, expedientes, true);
	}

	/**
	 * Registra a existência de conhecimento de uma comunicação por seu
	 * destinátario.
	 *
	 * @param dataCiencia a data considerada para ciência
	 * @param force indicativo de que a data de ciência eventualmente já
	 * existente deverá ser substituída pela nova data
	 * @param expedientes lista de expedientes sobre os quais deverá ser
	 * registrada a ciência
	 * @param isCienciaDomicilio é ciência pessoal via domicílio eletrônico.
	 */
	public void registraCienciaPessoal(Date dataCiencia, boolean force, List<ProcessoParteExpediente> expedientes, boolean isCienciaDomicilio) {
		registraCiencia(dataCiencia, force, false, expedientes, true, isCienciaDomicilio);
	}

	/**
	 * Registra a existência de conhecimento de uma comunicação por seu
	 * destinátario.
	 *
	 * @param dataCiencia a data considerada para ciência
	 * @param force indicativo de que a data de ciência eventualmente já
	 * existente deverá ser substituída pela nova data
	 * @param sistema indicativo de que a ciência registrada é feita pelo
	 * sistema, e não pelo efetivo destinatário
	 * @param expedientes lista de expedientes sobre os quais deverá ser
	 * registrada a ciência
	 */
	@SuppressWarnings("unused")
	private void registraCiencia(Date dataCiencia, boolean force, boolean sistema, List<ProcessoParteExpediente> expedientes) {

		registraCiencia(dataCiencia, force, sistema, expedientes, true);
	}

	/**
	 * Registra a existência de conhecimento de uma comunicação por seu
	 * destinátario.
	 *
	 * @param dataCiencia a data considerada para ciência
	 * @param force indicativo de que a data de ciência eventualmente já
	 * existente deverá ser substituída pela nova data
	 * @param sistema indicativo de que a ciência registrada é feita pelo
	 * sistema, e não pelo efetivo destinatário
	 * @param expedientes lista de expedientes sobre os quais deverá ser
	 * registrada a ciência
	 * @param isIntegrarDomicilio True indica que a ciência será sinalizada 
	 * também ao Domicílio Eletrônico.
	 */
	private void registraCiencia(Date dataCiencia, boolean force, boolean sistema, List<ProcessoParteExpediente> expedientes, boolean isIntegrarDomicilio) {
		registraCiencia(dataCiencia, force, sistema, expedientes, true, false);
	}

	/**
	 * Registra a existência de conhecimento de uma comunicação por seu
	 * destinátario.
	 *
	 * @param dataCiencia a data considerada para ciência
	 * @param force indicativo de que a data de ciência eventualmente já
	 * existente deverá ser substituída pela nova data
	 * @param sistema indicativo de que a ciência registrada é feita pelo
	 * sistema, e não pelo efetivo destinatário
	 * @param expedientes lista de expedientes sobre os quais deverá ser
	 * registrada a ciência
	 * @param isIntegrarDomicilio True indica que a ciência será sinalizada 
	 * também ao Domicílio Eletrônico.
	 * @param isCienciaDomicilio é ciência pessoal via domicílio eletrônico.
	 */
	private void registraCiencia(Date dataCiencia, boolean force, boolean sistema, List<ProcessoParteExpediente> expedientes, boolean isIntegrarDomicilio, boolean isCienciaDomicilio) {
		Map<OrgaoJulgador, Calendario> mapaCalendarios = new HashMap<OrgaoJulgador, Calendario>();

		for (ProcessoParteExpediente ppe : expedientes) {
			if (ppe.getDtCienciaParte() == null || force) {
				Calendario calendario = mapaCalendarios.get(ppe.getProcessoJudicial().getOrgaoJulgador());

				if (calendario == null) {
					OrgaoJulgador o = ppe.getProcessoJudicial().getOrgaoJulgador();

					calendario = prazosProcessuaisService.obtemCalendario(o);

					mapaCalendarios.put(o, calendario);
				}

				/*
				 * Verifica se o usuario logado é um advogado do processo com o
				 * expediente passado como paramêtro. PJEII-1067
				 * Corrigido porque mudava a identificação de quem tomou ciência
				 */
				boolean podeRegistrarCiencia = true;

				Pessoa p = null;
				if (!sistema) {
					try {
						if (ppe.getProcessoExpediente().getMeioExpedicaoExpediente().isExpedicaoRealizadaPessoalmente()) {
						    p = (Pessoa) pessoaService.findById(
						            documentoJudicialService.getUltimoAtoJudicial(ppe.getProcessoJudicial().getProcesso())
						                    .getUsuarioJuntada().getIdUsuario());
						} else {
						    Integer usuario = isCienciaDomicilio ? 
						            Authenticator.instance().recuperaUsuarioDomicilio().getIdUsuario() : 
						            usuarioService.getUsuarioLogado().getIdUsuario();
						    p = (Pessoa) pessoaService.findById(usuario);
						}

						/*
						 * se o destinatário tomou ciência do expediente por meio "pessoal", por exemplo telefone ou pessoalmente no próprio cartório,
						 * autorizar o usuário logado a registrar a ciência. 
						 */
						podeRegistrarCiencia = (aptoParaCiencia(ppe, p)) || (ppe.getProcessoExpediente()
								.getMeioExpedicaoExpediente().isExpedicaoRealizadaPessoalmente());
					} catch (PJeBusinessException e) {
						logger.warn(
								"Não foi possível recuperar a pessoa que tomaria ciência do expediente com identificador [{0}].",
								ppe.getIdProcessoParteExpediente());

						e.printStackTrace();
					}
				}
				if (podeRegistrarCiencia) {
					ppe.setCienciaSistema(sistema);
					if (!sistema && p != null && (force || ppe.getDtCienciaParte() == null)) {
						ppe.setNomePessoaCiencia(p.getNome());
						ppe.setPessoaCiencia(p);
						ppe.setFechado(false);
					}
					processoParteExpedienteManager.registraCiencia(ppe, dataCiencia, force, calendario, true, isIntegrarDomicilio);
					Date now = new Date();

					if (!ppe.getTipoPrazo().equals(TipoPrazoEnum.S) && ppe.getDtPrazoLegal() != null
							&& ppe.getDtPrazoLegal().before(now)) {
						Map<Integer, Calendario> calendariosexp = new HashMap<Integer, Calendario>();

						for (OrgaoJulgador o : mapaCalendarios.keySet()) {
							calendariosexp.put(o.getIdOrgaoJulgador(), mapaCalendarios.get(o));
						}

						registrarDecursoPrazo(ppe.getIdProcessoParteExpediente(), now, false, calendariosexp);
					}
				} else {
					throw new AplicationException("Você não está apto a tomar ciência desse processo.");
				}
			}
		}
	}
	
	public boolean aptoParaCiencia(ProcessoParteExpediente ppe) {
		boolean result = false;

		try {
			Pessoa p = (Pessoa) pessoaService.findById(usuarioService.getUsuarioLogado().getIdUsuario());

			result = aptoParaCiencia(ppe, p);
		} catch (PJeBusinessException e) {
			logger.error(e.getMessage());
		}

		return result;
	}

	/**
	 * Método responsável por verificar se a {@link Pessoa} está apta a tomar
	 * ciência.
	 *
	 * @param processoParteExpediente Expediente sobre o qual deverá ser
	 * registrada a ciência.
	 * @param pessoa Pessoa que deverá tomar ciência.
	 * @return Verdadeiro se:
	 * <ul>
	 * <li>A {@link Pessoa} pertence à Procuradoria / Defensoria o qual
	 * representa a parte <b>E</b> não é intimação pessoal.</li>
	 * <li>A {@link Pessoa} é advogado da parte <b>E</b> não é intimação
	 * pessoal.</li>
	 * <li>A {@link Pessoa} é a destinatária do expediente.</li>
	 * </ul>
	 * Falso, caso contrário.
	 */
	public boolean aptoParaCiencia(ProcessoParteExpediente processoParteExpediente, Pessoa pessoa) {
		boolean result = false;

		try {
			if (processoParteExpediente.getFechado() == null
					|| Boolean.FALSE.equals(processoParteExpediente.getFechado())) {
				result = aptoParaVisualizar(processoParteExpediente, pessoa);
			}
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
		}

		return result;
	}

	public boolean aptoParaVisualizar(ProcessoParteExpediente processoParteExpediente, Pessoa pessoa) throws PJeBusinessException {
		boolean result = false;
		if (Authenticator.isProcurador() && processoParteExpediente.getProcuradoria() != null && (PessoaFisica.class.isAssignableFrom(pessoa.getClass()) || br.jus.pje.nucleo.entidades.PessoaJuridica.class.isAssignableFrom(pessoa.getClass()))) {

			if (!processoParteExpediente.getProcuradoria().equals(Authenticator.getProcuradoriaAtualUsuarioLogado())) {
				return false;
			}

			if (PessoaFisica.class.isAssignableFrom(pessoa.getClass())){
				result = pessoaProcuradoriaManager.getPessoaProcuradoria(pessoa.getIdUsuario(), processoParteExpediente.getProcuradoria().getIdProcuradoria()) != null;
			}else {
				result = true;
			}
			
		}
		if (Authenticator.getPapelAtual() == ParametroUtil.instance().getPapelAdvogado() && !result) {
			result = processoJudicialService.isAdvogado(processoParteExpediente.getProcessoJudicial(), pessoa, processoParteExpediente.getPessoaParte());
		}

		if(!result && Authenticator.instance().recuperaUsuarioDomicilio()!= null && Authenticator.instance().recuperaUsuarioDomicilio().getIdUsuario().equals(pessoa.getIdPessoa())) {
			result = true;
		}
		else {
			result = Identity.instance().hasRole(Papeis.PERMITE_RESPONDER_EXPEDIENTE) || processoParteExpediente.getPessoaParte().equals(pessoa) || (result && !processoParteExpediente.getIntimacaoPessoal());
		}
		
		return result;
	}


	public boolean temDocumentoPendenteCiencia(ProcessoParteExpediente exp) {
		return processoParteExpedienteManager.temDocumentoPendenteCiencia(exp);
	}

	@Transactional
	public void registrarDecursoPrazo(Integer idAtoComunicacao, Date date, boolean force,
			Map<Integer, Calendario> mapaCalendarios) throws PJeRuntimeException {
		decursoPrazo.registrarDecursoPrazoLocal(idAtoComunicacao, date, force, mapaCalendarios);
	}

	public int getAtosComunicacaoAbertos(ProcessoTrf processoJudicial) {
		return processoParteExpedienteManager.getAtosComunicacaoPendentes(processoJudicial).size();
	}

	/**
	 * Registra ciêncie e resposta para uma lista de expedientes.
	 *
	 * @param resposta Resposta com o documento.
	 * @param atosPessoais Lista de expedientes.
	 * @throws PJeBusinessException
	 */
	public void registraResposta(RespostaExpediente resposta, ProcessoParteExpediente... atosPessoais)
			throws PJeBusinessException {
		registraResposta(resposta, true, atosPessoais);
	}

	/**
	 * Registra a resposta para uma lista de expedientes.
	 *
	 * @param resposta Resposta com documento.
	 * @param registrarCiencia Booleano que indica se será registrado ciência.
	 * @param atosPessoais Lista de expedientes.
	 * @throws PJeBusinessException
	 */
	public void registraResposta(RespostaExpediente resposta, boolean registrarCiencia,
			ProcessoParteExpediente... atosPessoais) throws PJeBusinessException {
		if (resposta.getProcessoDocumento() == null) {
			throw new PJeBusinessException("Não é possível registrar uma resposta sem um documento associado");
		}

		if (atosPessoais == null || atosPessoais.length == 0) {
			throw new PJeBusinessException(
					"Não é possível registrar uma resposta sem a indicação dos atos de comunicação respondidos.");
		}

		ProcessoTrf processoJudicial = atosPessoais[0].getProcessoJudicial();

		Date d = new Date();

		resposta.setData(d);

		if (registrarCiencia) {
			registraCiencia(d, false, false, Arrays.asList(atosPessoais), true);
		}

		respostaExpedienteManager.persistAndFlush(resposta);
		resposta = respostaExpedienteManager.merge(resposta);
		respostaExpedienteManager.flush();

		for (ProcessoParteExpediente ppe : atosPessoais) {
			ppe.setResposta(resposta);
			ppe.setFechado(true);
			ppe.setPendenteManifestacao(false);
			processoParteExpedienteManager.persist(ppe);
			processoParteExpedienteManager.flush();
		}

		Events.instance().raiseEvent(Eventos.EVENTO_PRECLUSAO_MANIFESTACAO, processoJudicial);
	}

	/**
	 * Prepara e envia um ato de comunicação nos autos do processo com
	 * identificador dado, para o polo processual indicado, com o tipo de prazo
	 * e prazos definidos e utilizando um documento já existente. Esse método
	 * também faz o lançamento da movimentação vinculada (expedido outro
	 * documento). Se o documento não existir, o envio é ignorado, sendo
	 * registrado o evento no log de aplicação
	 *
	 * @param idProcessoJudicial o identificador do processo judicial no qual a intimação será expedida.
	 * @param polo o polo processual em que a intimação será expedida ('A' para polo ativo, 'P' para polo passivo e 'T' para os terceiros vinculados)
	 * @param tipoPrazo o tipo de prazo a ser adotado ('A' - anos, 'N' - meses, 'D' - dias, 'H' - horas, 'M' - minutos)
	 * @param prazo o prazo na unidade do tipo de prazos
	 * @param idDocumento o identificador do documento
	 * @return true, se a intimação foi enviada
	 */
	public boolean intimarEletronicamente(Integer idProcessoJudicial, ProcessoParteParticipacaoEnum polo,
			TipoPrazoEnum tipoPrazo, Integer prazo, Integer idDocumento) {
		try {
			ProcessoDocumento doc = documentoJudicialService.getDocumento(idDocumento);

			if (doc == null) {
				logger.warn(
						"O documento com identificador [{0}] não foi encontrado. Não será possível realizar a intimação eletrônica nos autos com identificador [{1}].",
						idDocumento, idProcessoJudicial);

				return false;
			}

			return intimarEletronicamente(idProcessoJudicial, polo, tipoPrazo, prazo, doc);
		} catch (PJeBusinessException e) {
			logger.warn(
					"Não foi possível realizar o envio de intimação para o processo com identificador [{0}] quanto ao documento com identificador [{1}]",
					idProcessoJudicial, idDocumento);

			e.printStackTrace();

			return false;
		}
	}

	/**
	 * Prepara e envia um ato de comunicação nos autos do processo com
	 * identificador dado, para o polo processual indicado, com o tipo de prazo
	 * e prazos definidos e utilizando um documento já existente. Esse método
	 * também faz o lançamento da movimentação vinculada (expedido outro
	 * documento). Se o documento não existir, o envio é ignorado, sendo
	 * registrado o evento no log de aplicação sessao
	 *
	 * @param idProcessoJudicial o identificador do processo judicial no qual a intimação será expedida.
	 * @param polo o polo processual em que a intimação será expedida ('A' para polo ativo, 'P' para polo passivo e 'T' para os terceiros vinculados)
	 * @param tipoPrazo o tipo de prazo a ser adotado ('A' - anos, 'N' - meses, 'D' - dias, 'H' - horas, 'M' - minutos)
	 * @param prazo o prazo na unidade do tipo de prazos
	 * @param documento o documento a ser utilizado na intimação
	 * @return tru, se a intimação foi enviada
	 */
	public boolean intimarEletronicamente(Integer idProcessoJudicial, ProcessoParteParticipacaoEnum polo,
			TipoPrazoEnum tipoPrazo, Integer prazo, ProcessoDocumento documento) {
		try {
			ProcessoTrf processoJudicial = processoJudicialService.findById(idProcessoJudicial);

			if (processoJudicial == null) {
				logger.warn("O processo com identificador [{0}] não foi encontrado..", idProcessoJudicial);
				return false;
			}

			List<Pessoa> destinatarios = new ArrayList<Pessoa>();

			for (ProcessoParte pp : processoJudicial.getListaPartePrincipal(polo)) {
				if (verificarPossibilidadeIntimacaoEletronica(pp, false)) {
					destinatarios.add(pp.getPessoa());
				}
			}

			return intimarEletronicamente(processoJudicial, null, destinatarios.toArray(new Pessoa[] {}), tipoPrazo,
					prazo, null, documento, false, false);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Prepara e envia um ato de comunicação nos autos do processo com
	 * identificador dado, para o polo processual indicado, com o tipo de prazo
	 * e prazos definidos e utilizando um documento novo produzido a partir de
	 * um modelo e um tipo. Esse método também faz o lançamento da movimentação
	 * vinculada (expedido outro documento).
	 *
	 * @param idProcessoJudicial o identificador do processo judicial no qual a intimação será expedida.
	 * @param polo o polo processual em que a intimação será expedida ('A' para polo ativo, 'P' para polo passivo e 'T' para os terceiros vinculados)
	 * @param tipoPrazo o tipo de prazo a ser adotado ('A' - anos, 'N' - meses, 'D' - dias, 'H' - horas, 'M' - minutos)
	 * @param prazo o prazo na unidade do tipo de prazos
	 * @param idModelo identificador do modelo a ser adotado
	 * @param idTipoDocumento identificador do tipo de documento a ser adotado.
	 * @return true, se a intimação foi realizada.
	 */
	public boolean intimarEletronicamente(Integer idProcessoJudicial, ProcessoParteParticipacaoEnum polo,
			TipoPrazoEnum tipoPrazo, Integer prazo, int idModelo, int idTipoDocumento) {
		try {
			ProcessoTrf processoJudicial = processoJudicialService.findById(idProcessoJudicial);

			if (processoJudicial == null) {
				logger.warn("O processo com identificador [{0}] não foi encontrado..", idProcessoJudicial);
				return false;
			}

			List<Pessoa> destinatarios = new ArrayList<Pessoa>();

			for (ProcessoParte pp : processoJudicial.getListaPartePrincipal(polo)) {
				if (verificarPossibilidadeIntimacaoEletronica(pp, false)) {
					destinatarios.add(pp.getPessoa());
				}
			}

			return intimarEletronicamente(processoJudicial, destinatarios.toArray(new Pessoa[] {}), tipoPrazo, prazo,
					null, idModelo, idTipoDocumento);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Prepara e envia um ato de comunicação nos autos do processo com
	 * identificador dado, para o polo processual indicado, com o tipo de prazo
	 * e prazos definidos e utilizando um documento novo produzido a partir de
	 * um modelo e um tipo. Esse método também faz o lançamento da movimentação
	 * vinculada (expedido outro documento).
	 *
	 * @param idProcessoJudicial o identificador do processo judicial no qual a intimação será expedida.
	 * @param polo o polo processual em que a intimação será expedida ('A' para polo ativo, 'P' para polo passivo e 'T' para os terceiros vinculados)
	 * @param tipoPrazo o tipo de prazo a ser adotado ('A' - anos, 'N' - meses, 'D' - dias, 'H' - horas, 'M' - minutos)
	 * @param prazo o prazo na unidade do tipo de prazos
	 * @param idModelo identificador do modelo a ser adotado
	 * @param idTipoDocumento identificador do tipo de documento a ser adotado.
	 * @return true, se a intimação foi realizada.
	 */
	public boolean intimarEletronicamente(Integer idProcessoJudicial, String polo, String tipoPrazo, Integer prazo,
			int idModelo, int idTipoDocumento) {
		try {
			ProcessoTrf processoJudicial = processoJudicialService.findById(idProcessoJudicial);

			if (processoJudicial == null) {
				logger.warn("O processo com identificador [{0}] não foi encontrado..", idProcessoJudicial);
				return false;
			}

			List<Pessoa> destinatarios = new ArrayList<Pessoa>();

			for (ProcessoParte pp : processoJudicial
					.getListaPartePrincipal(ProcessoParteParticipacaoEnum.valueOf(polo))) {
				if (verificarPossibilidadeIntimacaoEletronica(pp, false)) {
					destinatarios.add(pp.getPessoa());
				}
			}

			return intimarEletronicamente(processoJudicial, destinatarios.toArray(new Pessoa[] {}),
					TipoPrazoEnum.valueOf(tipoPrazo), prazo, null, idModelo, idTipoDocumento);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean intimarDestinatarioEletronicamente(Integer idProcessoJudicial, Integer idDestinatario,
			TipoPrazoEnum tipoPrazo, Integer prazo, int idModelo, int idTipoDocumento) {
		try {
			ProcessoTrf processoJudicial = processoJudicialService.findById(idProcessoJudicial);

			Pessoa p = pessoaService.findById(idDestinatario);

			return intimarEletronicamente(processoJudicial, new Pessoa[] { p }, tipoPrazo, prazo, null, idModelo,
					idTipoDocumento);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean intimarDestinatarioEletronicamente(Integer idProcessoJudicial, Integer idDestinatario,
			TipoPrazoEnum tipoPrazo, Integer prazo, Integer idDocumento) {
		try {
			ProcessoTrf processoJudicial = processoJudicialService.findById(idProcessoJudicial);
			if (processoJudicial == null) {
				logger.warn("O processo com identificador [{0}] não foi encontrado..", idProcessoJudicial);
				return false;
			}
			Pessoa p = pessoaService.findById(idDestinatario);

			if (p == null) {
				return false;
			}

			ProcessoDocumento doc = documentoJudicialService.getDocumento(idDocumento);

			if (doc == null) {
				logger.warn(
						"O documento com identificador [{0}] não foi encontrado. Não será possível realizar a intimação eletrônica nos autos com identificador [{1}].",
						idDocumento, idProcessoJudicial);
				return false;
			}

			return intimarEletronicamente(processoJudicial, null, new Pessoa[] { p }, tipoPrazo, prazo, null, doc,
					false, false);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Intima eletronicamente uma ou mais pessoas, nao utilizando o Diario
	 * Eletronico.
	 *
	 * @param processoJudicial
	 * @param sessao
	 * @param destinatarios
	 * @param tipoPrazo
	 * @param prazo
	 * @param dataCerta
	 * @param idModelo
	 * @param idTipoDocumento
	 * @return verdadeiro se foi publicado com sucesso
	 */
	public boolean intimarEletronicamente(ProcessoTrf processoJudicial, Sessao sessao, Pessoa[] destinatarios,
			TipoPrazoEnum tipoPrazo, Integer prazo, Date dataCerta, int idModelo, int idTipoDocumento) {
		return this.intimarEletronicamente(processoJudicial, sessao, destinatarios, tipoPrazo, prazo, dataCerta,
				idModelo, idTipoDocumento, false);
	}

	/**
	 * Intima eletronicamente uma ou mais pessoas
	 *
	 * @param processoJudicial O processo que sera publicado
	 * @param sessao           A sessao que esta vinculada com a publicaco
	 * @param destinatarios    As pessoas que serao intimadas
	 * @param tipoPrazo        O tipo do prazo de resposta
	 * @param prazo            O prazo de resposta
	 * @param dataCerta        A data da publicacao
	 * @param idModelo         O modelo de documento da intimacao
	 * @param idTipoDocumento  O tipo de documento da intimacao
	 * @param usarDiario       Se deve ser publicado no DJe a intimacao
	 * @return boolean
	 */
	public boolean intimarEletronicamente(ProcessoTrf processoJudicial, Sessao sessao, Pessoa[] destinatarios,
			TipoPrazoEnum tipoPrazo, Integer prazo, Date dataCerta, int idModelo, int idTipoDocumento,
			boolean usarDiario) {
		try {
			ModeloDocumento modelo = documentoJudicialService.getModeloDocumento(idModelo);
			TipoProcessoDocumento tpd = documentoJudicialService.getTiposDisponiveis(idTipoDocumento).get(0);
			ProcessoDocumento pd = documentoJudicialService.getDocumento();

			pd.setTipoProcessoDocumento(tpd);
			pd.setProcesso(processoJudicial.getProcesso());

			Contexts.getEventContext().set("destinatarios", obterNomeDestinatarios(destinatarios));
			Contexts.getEventContext().set("prazoCumprimento", prazoParaTexto(tipoPrazo, prazo, dataCerta));

			documentoJudicialService.substituirModelo(pd, modelo);

			return intimarEletronicamente(processoJudicial, sessao, destinatarios, tipoPrazo, prazo, dataCerta, pd,
					true, usarDiario);
		} catch (Exception e) {
			logger.error(e);
		}

		return false;
	}

	/**
	 * Obtém o nome dos destinarios separados por vígula
	 *
	 * @param destinatarios
	 * @return String
	 */
	private String obterNomeDestinatarios(Pessoa[] destinatarios) {
		StringBuilder nomes = new StringBuilder();

		if (ArrayUtils.isNotEmpty(destinatarios)) {
			int cont = 0;

			for (Pessoa destinatario : destinatarios) {
				String nome = destinatario.getNome();

				if (StringUtils.isNotBlank(nome)) {
					nomes.append(obterComplemento(destinatarios.length, cont, nome));
				}
				cont++;
			}
		}

		return nomes.toString();
	}

	/**
	 * Obteém o complemento para separação dos nomes por virgula.
	 *
	 * @param qtde
	 * @param cont
	 * @param nome
	 * @return String
	 */
	private String obterComplemento(int qtde, int cont, String nome) {
		String retorno;

		if (qtde > 1 && cont == (qtde - 1)) {
			retorno = " e " + nome;
		} else if (cont != 0) {
			retorno = ", " + nome;
		} else {
			retorno = nome;
		}

		return retorno;
	}

	public boolean intimarEletronicamente(ProcessoTrf processoJudicial, Pessoa[] destinatarios, TipoPrazoEnum tipoPrazo,
			Integer prazo, Date dataCerta, int idModelo, int idTipoDocumento) throws PJeBusinessException {
		return intimarEletronicamente(processoJudicial, null, destinatarios, tipoPrazo, prazo, dataCerta, idModelo,
				idTipoDocumento, false);
	}

	public boolean intimarEletronicamente(ProcessoTrf processoJudicial, Sessao sessao, Pessoa[] destinatarios,
			TipoPrazoEnum tipoPrazo, Integer prazo, Date dataCerta, ProcessoDocumento pd) throws PJeBusinessException {
		return intimarEletronicamente(processoJudicial, sessao, destinatarios, tipoPrazo, prazo, dataCerta, pd, false,
				false);
	}

	public boolean intimarEletronicamente(ProcessoTrf processoJudicial, String codAgrupamentoPessoas,
			TipoPrazoEnum tipoPrazo, Integer prazo, Date dataCerta, Integer idProcessoDocumento)
			throws PJeBusinessException {
		ProcessoDocumento pd = processoDocumentoManager.findById(idProcessoDocumento);

		Set<Pessoa> dest = agrupamentoPessoasManager.recuperaPessoasPorCodigo(codAgrupamentoPessoas);

		ArrayList<Pessoa> destinatarios = new ArrayList<Pessoa>(dest.size());

		StringBuilder frust = new StringBuilder();

		Procuradoria procuradoria = null;

		for (Pessoa p : dest) {
			procuradoria = ComponentUtil.getComponent(PessoaProcuradoriaEntidadeManager.class)
					.getProcuradoriaPadraoPessoa(p);

			if (verificarPossibilidadeIntimacaoEletronica(p, processoJudicial, false, procuradoria)) {
				destinatarios.add(p);
			} else {
				if (frust.length() > 0) {
					frust.append(',');
				}

				frust.append(p.getIdPessoa());
			}
		}

		Contexts.getEventContext().set("pje:atoComunicacao:eletronico:naoIntimados", frust.toString());

		return intimarEletronicamente(processoJudicial, null, destinatarios.toArray(new Pessoa[destinatarios.size()]),
				tipoPrazo, prazo, dataCerta, pd, false, false);
	}

	private boolean intimarEletronicamente(ProcessoTrf processoJudicial, Sessao sessao, Pessoa[] destinatarios,
			TipoPrazoEnum tipoPrazo, Integer prazo, Date dataCerta, ProcessoDocumento pd, boolean novo,
			boolean usarDiario) throws PJeBusinessException {
		return intimarEletronicamente(processoJudicial, sessao, destinatarios, tipoPrazo, prazo, dataCerta, pd, novo,
				usarDiario, false, null, null, null);
	}

	private boolean intimarEletronicamente(final ProcessoTrf processoJudicial, final Sessao sessao,
			Pessoa[] destinatarios, TipoPrazoEnum tipoPrazo, Integer prazo, Date dataCerta, final ProcessoDocumento pd,
			boolean novo, final boolean usarDiario, final boolean sessaoMural, final ExpedicaoExpedienteEnum meio,
			Date dataEnvio, ProcessoDocumento documentoVinculado) throws PJeBusinessException {
		try {
			Callable<Boolean> callable = intimarEletronicamente(processoJudicial, sessao, destinatarios, tipoPrazo,
					prazo, dataCerta, pd, novo, usarDiario, sessaoMural, meio, dataEnvio, documentoVinculado, true,
					false);

			if (callable == null) {
				return false;
			}

			return callable.call();
		} catch (Exception ex) {
			throw new PJeBusinessException(ex);
		}
	}

	public Callable<Boolean> intimarEletronicamente(final ProcessoTrf processoJudicial, final Sessao sessao,
			final Pessoa[] destinatarios, TipoPrazoEnum tipoPrazo, Integer prazo, Date momentoInicio,
			ModeloDocumento modelo, TipoProcessoDocumento tipoDocumento, final boolean autoFlush,
			final boolean signAsync) throws PJeBusinessException {
		ProcessoDocumento pd = documentoJudicialService.getDocumento();

		pd.setTipoProcessoDocumento(tipoDocumento);
		pd.setProcesso(processoJudicial.getProcesso());

		Contexts.getEventContext().set("destinatarios", obterNomeDestinatarios(destinatarios));
		Contexts.getEventContext().set("prazoCumprimento", prazoParaTexto(tipoPrazo, prazo, momentoInicio));

		documentoJudicialService.substituirModelo(pd, modelo);

		return intimarEletronicamente(processoJudicial, sessao, destinatarios, tipoPrazo, prazo, momentoInicio, pd,
				true, false, false, null, null, null, autoFlush, signAsync);
	}

	private Callable<Boolean> intimarEletronicamente(final ProcessoTrf processoJudicial, final Sessao sessao,
			final Pessoa[] destinatarios, TipoPrazoEnum tipoPrazo, Integer prazo, Date dataCerta,
			final ProcessoDocumento pd, boolean novo, final boolean usarDiario, final boolean sessaoMural,
			final ExpedicaoExpedienteEnum meio, Date dataEnvio, ProcessoDocumento documentoVinculado,
			final boolean autoFlush, final boolean signAsync) throws PJeBusinessException {
		try {
			if (destinatarios == null || destinatarios.length == 0) {
				return null;
			}

			final Future<Boolean> futureProcessoDocumentoBin;

			final ProcessoExpediente pe = processoExpedienteManager.getExpediente();

			if (meio != null) {
				pe.setMeioExpedicaoExpediente(meio);
			} else {
				pe.setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum.E);
			}

			pe.setProcessoDocumento(pd);

			if (novo) {
				if (signAsync) {
					futureProcessoDocumentoBin = documentoJudicialService
							.assinaSistemaAsync(pd.getProcessoDocumentoBin());
				} else {
					if (pd.getProcessoDocumentoBin() != null) {
						documentoJudicialService.assinaSistema(pd.getProcessoDocumentoBin());
					}
					futureProcessoDocumentoBin = null;
				}

				pe.setDocumentoExistente(Boolean.FALSE);
			} else {
				pe.setDocumentoExistente(Boolean.TRUE);

				if (documentoVinculado != null) {
					pe.setProcessoDocumentoVinculadoExpediente(documentoVinculado);
				}

				futureProcessoDocumentoBin = null;
			}

			for (Pessoa destinatario : destinatarios) {
				ProcessoParte pp = processoParteManager.findProcessoParte(processoJudicial, destinatario, false);

				ProcessoParteExpediente ppe = processoParteExpedienteManager.getExpedientePessoal(destinatario,
						pp != null ? pp.getProcuradoria() : null);

				if (tipoPrazo == TipoPrazoEnum.C && dataCerta != null) {
					ppe.setDtPrazoLegal(dataCerta);
				} else if ((prazo == null || prazo == 0) && tipoPrazo != TipoPrazoEnum.C) {
					ppe.setTipoPrazo(TipoPrazoEnum.S);
				} else {
					ppe.setTipoPrazo(tipoPrazo);
				}
				if (prazo != null) {
					ppe.setPrazoLegal(prazo);
				}

				OrgaoJulgador orgaoJulgador = processoJudicial.getOrgaoJulgador();

				Calendario calendario = prazosProcessuaisService.obtemCalendario(orgaoJulgador);

				ppe.setProcessoJudicial(processoJudicial);

				if (meio == ExpedicaoExpedienteEnum.P) { // DJE
					PublicacaoDiarioEletronico publicacaoDJE = ComponentUtil
							.getComponent(PublicacaoDiarioEletronicoManager.class)
							.novo(pe.getDtCriacao(), ppe, calendario);
					ppe.getPublicacaoDiarioEletronicoList().add(publicacaoDJE);
				}

				if (sessaoMural) {
					ppe.setProcessoJudicial(processoJudicial);

					if (DateUtil.isDataMenorIgual(dataEnvio, new Date())) {
						if (ppe.getTipoPrazo() != TipoPrazoEnum.S && ppe.getTipoPrazo() != TipoPrazoEnum.C
								&& ppe.getPrazoLegal() != null && ppe.getPrazoLegal() != 0) {
							ppe.setDtCienciaParte(pe.getDtCriacao());

							Date fimPrazo = prazosProcessuaisService.calculaPrazoProcessual(ppe.getDtCienciaParte(),
									ppe.getPrazoLegal(), ppe.getTipoPrazo(), calendario,
									ppe.getProcessoJudicial().getCompetencia().getCategoriaPrazoProcessual(),
									ContagemPrazoEnum.M);

							ppe.setDtPrazoLegal(fimPrazo);
						}
					}

					ppe.setFechado(Boolean.FALSE);
				}

				pe.getProcessoParteExpedienteList().add(ppe);
			}

			Callable<Boolean> posRunner = new Callable<Boolean>() {
				private Boolean result;

				@Override
				public Boolean call() throws Exception {
					if (result != null) {
						return result;
					}

					try {
						if ((futureProcessoDocumentoBin != null) && !futureProcessoDocumentoBin.isCancelled()) {
							if (Boolean.FALSE.equals(futureProcessoDocumentoBin.get()))
								return Boolean.FALSE;
						}

						if (ArrayUtil.isListNotEmpty(pe.getProcessoParteExpedienteList())) {
						List<ProcessoExpediente> expedientes = finalizarAtosComunicacao(new ProcessoExpediente[] { pe },
								new String[] { pd.getProcessoDocumentoBin().getSignature() },
								pd.getProcessoDocumentoBin().getCertChain(), processoJudicial, null, null, autoFlush);

						if (sessao != null) {
							for (ProcessoExpediente exp : expedientes) {
								exp.setSessao(sessao);
							}
						}

						if (sessaoMural) {
							lancarMovimentacaoPublicacaoJulgamento(processoJudicial, pd, meio.getLabel());
						} else {
							MovimentoAutomaticoService.preencherMovimento()
									.deCodigo(CodigoMovimentoNacional.COD_MOVIMENTO_EXPEDICAO_DOCUMENTO)
									.associarAoProcesso(processoJudicial).associarAoDocumento(pe.getProcessoDocumento())
									.comComplementoDeNome(CodigoMovimentoNacional.NOME_COMPLEMENTO_TIPO_DOCUMENTO)
									.doTipoDominio()
									.preencherComElementoDeCodigo(
											CodigoMovimentoNacional.COD_COMPLEMENTO_TIPO_DOCUMENTO.OUTROS_DOCUMENTOS)
									.lancarMovimento(autoFlush);
						}

						if (usarDiario) {
							pe.setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum.P);
							intimarEletronicamenteDJe(pe);
						}

						result = !expedientes.isEmpty();
						}
					} catch (Exception e) {
						result = false;

						StringBuilder sb = new StringBuilder();

						for (Pessoa destinatario : destinatarios) {
							if (sb.length() > 0) {
								sb.append(',');
							}
							sb.append(destinatario.getIdPessoa());
						}

						logger.error("Erro ao intimar eletronicamente os destinatários {0} do processo {1}", e, sb,
								processoJudicial.getIdProcessoTrf());
					}

					return result;
				}
			};

			if (!signAsync) {
				posRunner.call();
			}

			return posRunner;
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder(200);

			for (Pessoa destinatario : destinatarios) {
				if (sb.length() > 0)
					sb.append(',');
				sb.append(destinatario.getIdPessoa());
			}

			logger.error("Erro ao intimar eletronicamente os destinatários {0} do processo {1}", e, sb,
					processoJudicial.getIdProcessoTrf());
		}

		return null;
	}

	/**
	 * Envia o expediente para o diario de justica eletronico(DJe)
	 *
	 * @param processoExpediente O expediente que sera publicado no DJe
	 * @throws PJeBusinessException
	 * @throws PontoExtensaoException
	 */
	private void intimarEletronicamenteDJe(ProcessoExpediente processoExpediente) throws PJeBusinessException {
		cienciaAutomatizadaDiarioEletronico.intimarEletronicamenteDJe(processoExpediente);
	}

	private String prazoParaTexto(TipoPrazoEnum tipoPrazo, Integer prazo, Date dataCerta) {
		if (tipoPrazo != TipoPrazoEnum.S && tipoPrazo != TipoPrazoEnum.C && prazo == null) {
			throw new IllegalArgumentException(
					"Argumentos inválidos: não é possível converter um prazo para texto sem a indicação do prazo.");
		}

		StringBuilder sb = new StringBuilder();

		switch (tipoPrazo) {
		case S:
			sb.append("sem prazo definido");

			break;
		case C:
			if (dataCerta == null) {
				throw new IllegalArgumentException(
						"Argumentos inválidos: não é possível converter um prazo com data certa sem indicar a data certa de sua concretização");
			}

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MMMM/yyyy HH:mm");

			sb.append("até ");
			sb.append(sdf.format(dataCerta));

			break;
		case A:
			sb.append("em ");
			sb.append(prazo.toString());
			sb.append(" ");

			if (prazo > 1) {
				sb.append("anos");
			} else {
				sb.append("ano");
			}

			break;
		case M:
			sb.append("em ");
			sb.append(prazo.toString());
			sb.append(" ");

			if (prazo > 1) {
				sb.append("meses");
			} else {
				sb.append("mês");
			}

			break;
		case D:
			sb.append("em ");
			sb.append(prazo.toString());
			sb.append(" ");

			if (prazo > 1) {
				sb.append("dias");
			} else {
				sb.append("dia");
			}

			break;
		case H:
			sb.append("em ");
			sb.append(prazo.toString());
			sb.append(" ");

			if (prazo > 1) {
				sb.append("horas");
			} else {
				sb.append("hora");
			}

			break;
		case N:
			sb.append("em ");
			sb.append(prazo.toString());
			sb.append(" ");

			if (prazo > 1) {
				sb.append("minutos");
			} else {
				sb.append("minuto");
			}

			break;
		default:
			throw new IllegalAddException(
					"Os argumentos fornecidos não são suficientes para converter o prazo para texto.");
		}

		return sb.toString();
	}

	/**
	 * @see ProcessoParteExpedienteDAO#contagemExpedientesPendentesCiencia(ProcessoDocumento)
	 */
	public Long contagemExpedientesPendentesCiencia(ProcessoDocumento documento) {
		return processoParteExpedienteManager.contagemExpedientesPendentesCiencia(documento);
	}

	/**
	 * Busca a úlltima data de execução com sucesso do job, na tabela de parâmetros
	 * do sistema.
	 *
	 * @return Date
	 * @throws PJeBusinessException
	 */
	public Date getUltimaDataJobDiario() throws PJeBusinessException {
		return cienciaAutomatizadaDiarioEletronico.getUltimaDataJobDiario();
	}

	public Calendar recuperarDataPublicacao(String idMateria) throws PontoExtensaoException {
		// O conector retorna a data de disponibilizacao. A data de
		// publicacao sera calculada e persistida pelo job na tabela
		return null;
	}

    /**
	 * Retorna true se o usuário estiver apto para responder o expediente ciente.
	 * 
	 * @param ppe Expediente ciente, ou seja, a data de ciência deve ser diferente de nulo.
	 * @param pessoa Pessoa que será verificada se pode responder o expediente.
	 * @return booleano
	 */
	public Boolean isAptoParaResponder(ProcessoParteExpediente ppe, Pessoa pessoa) {
		return (ppe != null) && (pessoa != null) && (aptoParaCiencia(ppe, pessoa)) && (ppe.getDtCienciaParte() != null)
				&& (ppe.getFechado() == null || ppe.getFechado() == false);
	}

	@SuppressWarnings("unused")
	private Evento getMovimentoDisponibilizacaoDJE() {
		if (movimentoDisponibilizacaoDJE == null) {
			String codMovimentoDisponibilizacaoDiario = CodigoMovimentoNacional.CODIGO_MOVIMENTO_DJE_DISPONIBILIZACAO;
			movimentoDisponibilizacaoDJE = eventoManager.findByCodigoCNJ(codMovimentoDisponibilizacaoDiario);
		}
		return movimentoDisponibilizacaoDJE;
	}

	@SuppressWarnings("unused")
	private Evento getMovimentoPublicacaoDJE() {
		if (movimentoPublicacaoDJE == null) {
			String codMovimentoPublicacaoDiario = CodigoMovimentoNacional.CODIGO_MOVIMENTO_DJE_PUBLICACAO;
			movimentoPublicacaoDJE = eventoManager.findByCodigoCNJ(codMovimentoPublicacaoDiario);
		}
		return movimentoPublicacaoDJE;
	}
	
    @SuppressWarnings("unused")
	private boolean isPrecisaAtualizarMateria(ProcessoParteExpediente processoParteExpediente) {
        return processoParteExpediente.getProcessoExpediente().getDtCriacao().before(DateUtil.dataMenosDias(new Date(), 1)) && 
        	(
        		processoParteExpediente.getDtCienciaParte() == null || 
        		(
        			processoParteExpediente.getDtPrazoLegal() == null && 
        			processoParteExpediente.getPrazoLegal() != null && 
        			processoParteExpediente.getPrazoLegal() != 0 && 
        			processoParteExpediente.getTipoPrazo() != TipoPrazoEnum.S && 
        			processoParteExpediente.getTipoPrazo() != TipoPrazoEnum.C
        		)
        	);
	}
	
    /**
     * Cria expedientes ({@link ProcessoExpediente} baseando-se nas informações que foram definidas pelo usuário no frame do miniPAC.
     * 
     * O mapa de destinatários é percorrido, por meio de comunicação (que é a chave do mapa).
     * Se possível, será criado apenas um expediente {@link ProcessoExpediente} por processo/urgência.
     * Se houver procuradoria/defensoria que representa o destinatário do ato de comunicação, 
     * o órgão será inserido nos registros para poder tomar ciência e responder ao expediente.
     * 
     * @param processoTrf no qual o expediente será criado
     * @param documentoPrincipal que será vinculado ao {@link ProcessoExpediente}
     * @param miniPacVOMap mapa de destinatários, de onde as informações serão extraídas para criação dos atos de comunicação.
     * @param documentosVinculadosSet lista de documentos vinculados ao documento principal
     * @param isProcDocExistente 
     * 
     * @return lista dos atos de comunicação criados
     */
	public Collection<ProcessoExpediente> criarAtosComunicacao(ProcessoTrf processoTrf,
			ProcessoDocumento documentoPrincipal, Map<ExpedicaoExpedienteEnum, List<MiniPacVO>> miniPacVOMap,
			List<ProcessoDocumento> documentosVinculadosSet, Boolean isProcDocExistente) {

		return criarAtosComunicacao(processoTrf, documentoPrincipal, miniPacVOMap, documentosVinculadosSet,
				isProcDocExistente, null);

	}

    /**
     * Cria expedientes ({@link ProcessoExpediente} baseando-se nas informações que foram definidas pelo usuário no frame do miniPAC.
     * 
     * O mapa de destinatários é percorrido, por meio de comunicação (que é a chave do mapa).
     * Se possível, será criado apenas um expediente {@link ProcessoExpediente} por processo/urgência.
     * Se houver procuradoria/defensoria que representa o destinatário do ato de comunicação, 
     * o órgão será inserido nos registros para poder tomar ciência e responder ao expediente.
     * 
     * @param processoTrf no qual o expediente será criado
     * @param documentoPrincipal que será vinculado ao {@link ProcessoExpediente}
     * @param miniPacVOMap mapa de destinatários, de onde as informações serão extraídas para criação dos atos de comunicação.
     * @param documentosVinculadosSet lista de documentos vinculados ao documento principal
     * @param isProcDocExistente 
	 * @param tipoProcessoDocumento é enviado pela funcionalidade de intimação em lote. Quando isProcDocExistente é true, o tipo é preenchido é definido no processoDocumentoAto
	 * 
	 * @return lista dos atos de comunicação criados
	 */
	public Collection<ProcessoExpediente> criarAtosComunicacao(ProcessoTrf processoTrf,
			ProcessoDocumento documentoPrincipal, Map<ExpedicaoExpedienteEnum, List<MiniPacVO>> miniPacVOMap,
			List<ProcessoDocumento> documentosVinculadosSet, Boolean isProcDocExistente,
			TipoProcessoDocumentoEnum tipoProcessoDocumentoEnum) {

		return criarAtosComunicacao(processoTrf, documentoPrincipal, miniPacVOMap, documentosVinculadosSet,
				isProcDocExistente, tipoProcessoDocumentoEnum, false);
	}

    /**
     * Cria expedientes ({@link ProcessoExpediente} baseando-se nas informações que foram definidas pelo usuário no frame do miniPAC.
     * 
     * O mapa de destinatários é percorrido, por meio de comunicação (que é a chave do mapa).
     * Se possível, será criado apenas um expediente {@link ProcessoExpediente} por processo/urgência.
     * Se houver procuradoria/defensoria que representa o destinatário do ato de comunicação, 
     * o órgão será inserido nos registros para poder tomar ciência e responder ao expediente.
     * 
     * @param processoTrf no qual o expediente será criado
     * @param documentoPrincipal que será vinculado ao {@link ProcessoExpediente}
     * @param miniPacVOMap mapa de destinatários, de onde as informações serão extraídas para criação dos atos de comunicação.
     * @param documentosVinculadosSet lista de documentos vinculados ao documento principal
     * @param isProcDocExistente 
	 * @param tipoProcessoDocumento é enviado pela funcionalidade de intimação em lote. Quando isProcDocExistente é true, o tipo é preenchido é definido no processoDocumentoAto
	 * @param isReenvioCitacaoExpirada -> indica se é reenvio de citação expirada (citação enviada ao Domicílio sem ciência). Este reenvio é feito automaticamente através do fluxo TCI e não é enviado ao Domicílio Eletrônico. 
	 * 
	 * @return lista dos atos de comunicação criados
	 */
	public Collection<ProcessoExpediente> criarAtosComunicacao(ProcessoTrf processoTrf,
			ProcessoDocumento documentoPrincipal, Map<ExpedicaoExpedienteEnum, List<MiniPacVO>> miniPacVOMap,
			List<ProcessoDocumento> documentosVinculadosSet, Boolean isProcDocExistente,
			TipoProcessoDocumentoEnum tipoProcessoDocumentoEnum, boolean isReenvioCitacaoExpirada) {

    	TipoProcessoDocumento tipoProcessoDocumento = null;
    	try {
			Date dataAtual = DateService.instance().getDataHoraAtual();
			ProcessoExpediente pe;
			ProcessoParteExpediente ppe;
			ProcessoDocumentoExpediente pde;
			List<ProcessoExpediente> expedientesGerados = new ArrayList<>(0);
			ProcessoDocumento processoDocumentoAto = documentoPrincipal;

			HashMap<String, ProcessoExpediente> mapaExpedientes = new HashMap<>();

			if (isProcDocExistente) {
				processoDocumentoAto = copiaDocumento(documentoPrincipal);
				processoDocumentoAto.setDataJuntada(processoDocumentoAto.getDataInclusao());
				processoDocumentoAto
						.setIdJbpmTask(TaskInstance.instance() != null ? TaskInstance.instance().getId() : null);
				processoDocumentoAto.setExclusivoAtividadeEspecifica((TaskInstance.instance() != null));

				if (tipoProcessoDocumentoEnum != null) {
					tipoProcessoDocumento = ComponentUtil.getTipoProcessoDocumentoManager().findByDescricaoTipoDocumento(tipoProcessoDocumentoEnum.getLabel());
					processoDocumentoAto.setTipoProcessoDocumento(tipoProcessoDocumento);
					processoDocumentoAto.setProcessoDocumento(tipoProcessoDocumentoEnum.getLabel());
				}
			}

			for(Map.Entry<ExpedicaoExpedienteEnum, List<MiniPacVO>> entry : miniPacVOMap.entrySet()){
				ExpedicaoExpedienteEnum meio = entry.getKey();
				for (MiniPacVO destinatario : miniPacVOMap.get(meio)) {
					if (!destinatario.getAtivo()) {
						continue;
					}

					String key = null;

					if (tipoProcessoDocumentoEnum != null) {
						key = createKey(meio, destinatario, processoDocumentoAto.getTipoProcessoDocumento());
					} else {
						tipoProcessoDocumento = getTipoProcessoDocumentoDoExpediente(destinatario, documentoPrincipal);
						key = createKey(meio, destinatario, tipoProcessoDocumento);
					}

					//Verifica se já existe um processo expediente para o Meio-Urgencia atual
					if (mapaExpedientes.containsKey(key)) {
						pe = mapaExpedientes.get(key);
					} else {
					    pe = criarProcessoExpediente(processoTrf, documentoPrincipal, isProcDocExistente, dataAtual, processoDocumentoAto, meio, tipoProcessoDocumento, destinatario);
			            pde = criarProcessoDocumentoExpediente(pe, processoDocumentoAto);

			            pe.getProcessoDocumentoExpedienteList().add(pde);
			            
			            processoDocumentoManager.persist(processoDocumentoAto);                    
			            processoExpedienteManager.persist(pe);                    
			            processoDocumentoExpedienteManager.persist(pde);
			            	            			            
			            processarDocumentosVinculados(documentosVinculadosSet, pe);
			            mapaExpedientes.put(key, pe);
					}

					ppe = criarProcessoParteExpediente(pe, destinatario);
					pe.getProcessoParteExpedienteList().add(ppe);
					processarEnderecos(ppe, meio, destinatario);
					processoParteExpedienteManager.persist(ppe);
	            	tratarVisualizadoresEmCasoDeSigiloDoProcesso(processoTrf, ppe);
	            	tratarVisualizadoresEmCasoDeSigiloDoDocumento(ppe);

					if (meio.isExigeParteCadastradaComCertificado()) {
            			int idProcessoExpediente = pe.getIdProcessoExpediente();

            			ProcessoExpediente expediente = expedientesGerados.stream().filter(pex -> pex.getIdProcessoExpediente()==idProcessoExpediente).findFirst().orElse(null);

            			if (Objects.nonNull(expediente)) {	            				
            				expedientesGerados.set(expedientesGerados.indexOf(expediente), pe);
            			} else if (domicilioEletronicoService.isIntegracaoHabilitada() 
            				&& Boolean.TRUE.equals(domicilioEletronicoService.isPessoaHabilitada(ppe.getPessoaParte()))) {		
            				expedientesGerados.add(pe);
            			}  			
	            	}
				}
			}

			if (!isReenvioCitacaoExpirada
					&& domicilioEletronicoService.isIntegracaoHabilitada() 
					&& expedientesGerados != null
					&& !expedientesGerados.isEmpty()) {
				domicilioEletronicoService.enviarExpedientesAsync(expedientesGerados);
			}

			EntityUtil.flush();

			return mapaExpedientes.values();
		} catch (PJeBusinessException e) {
			logger.error("Erro ao tentar criar ato de comunicação pela variável de fluxo: %s", e.getLocalizedMessage());
		}

		return null;
	}

	private void tratarVisualizadoresEmCasoDeSigiloDoDocumento(ProcessoParteExpediente ppe) throws PJeBusinessException {
		if(ppe.getProcessoExpediente().getProcessoDocumento().getDocumentoSigiloso()){
			 ComponentUtil.getComponent(ProcessoDocumentoVisibilidadeSegredoManager.class).acrescentaVisualizador(ppe.getProcessoExpediente().getProcessoDocumento(), ppe.getPessoaParte(),ppe.getProcuradoria(), true);
		}
	}

	private void tratarVisualizadoresEmCasoDeSigiloDoProcesso(ProcessoTrf processoTrf, ProcessoParteExpediente ppe) throws PJeBusinessException {
	    if(processoTrf.getSegredoJustica()){
		    processoJudicialService.acrescentaVisualizador(processoTrf, ppe.getPessoaParte(), ppe.getProcuradoria());
		}
	}	

	private void processarEnderecos(ProcessoParteExpediente ppe, ExpedicaoExpedienteEnum meio, MiniPacVO destinatario) {
		if (meio.isExigeEndereco() && ProjetoUtil.isNotVazio(destinatario.getEnderecos())) {
			ProcessoParteExpedienteEndereco ppee;
			for (Endereco endereco : destinatario.getEnderecos()) {
				ppee = new ProcessoParteExpedienteEndereco();
				ppee.setProcessoParteExpediente(ppe);
				ppee.setEndereco(endereco);
				ppe.getProcessoParteExpedienteEnderecoList().add(ppee);
			}
		}
	}

	private ProcessoParteExpediente criarProcessoParteExpediente(ProcessoExpediente pe, MiniPacVO destinatario) {
		ProcessoParteExpediente ppe = new ProcessoParteExpediente();
		ppe.setCienciaSistema(false);
		ppe.setDtCienciaParte(null);
		ppe.setPessoaParte(destinatario.getProcessoParte().getPessoa());
		ppe.setIntimacaoPessoal(destinatario.isPessoal());
		ppe.setPrazoLegal((destinatario.getPrazo() == null || destinatario.getPrazo() == 0) ? 0 : destinatario.getPrazo());
		ppe.setTipoPrazo((destinatario.getPrazo() == null || destinatario.getPrazo() == 0) ? TipoPrazoEnum.S : TipoPrazoEnum.D);
		ppe.setProcessoExpediente(pe);
		ppe.setFechado(false);
		ppe.setTipoCalculoMeioComunicacao(destinatario.getTipoCalculo());
		Date dataPrazoLegal = null;
		Date dataPrazoProcessual = null;
		if (ExpedicaoExpedienteEnum.E.equals(pe.getMeioExpedicaoExpediente())) {
			Calendario calendario = prazosProcessuaisService.obtemCalendario(pe.getProcessoTrf().getOrgaoJulgador());
			dataPrazoLegal = prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(pe.getDtCriacao(), calendario, pe.getProcessoTrf().getCompetencia().getCategoriaPrazoCiencia(), ContagemPrazoEnum.C);
			dataPrazoProcessual = pe.getDtCriacao();
		}
		ppe.setDtPrazoLegal(dataPrazoLegal);
		ppe.setDtPrazoProcessual(dataPrazoProcessual);
		ppe.setProcessoJudicial(pe.getProcessoTrf());
		if (Objects.isNull(destinatario.getProcessoParte().getProcuradoria())) {
			List<Procuradoria> procuradoriaList = pessoaService.obtemOrgaosRepresentantes(ppe.getPessoaParte());
			if (!procuradoriaList.isEmpty()) {
				ppe.setProcuradoria(procuradoriaList.get(0));
			} 
		} else {
			ppe.setProcuradoria(destinatario.getProcessoParte().getProcuradoria());
		}
		return ppe;
	}

	private void processarDocumentosVinculados(List<ProcessoDocumento> documentosVinculadosSet, ProcessoExpediente pe) throws PJeBusinessException {
		if(documentosVinculadosSet != null){
			ProcessoDocumentoExpediente pde;
		    for (ProcessoDocumento doc : documentosVinculadosSet) {
		        pde = new ProcessoDocumentoExpediente();                    
		        pde.setAnexo(true);
		        pde.setDtImpressao(null);
		        pde.setProcessoDocumento(doc);
		        pde.setProcessoExpediente(pe);
		        pe.getProcessoDocumentoExpedienteList().add(pde);
		        processoDocumentoExpedienteManager.persist(pde);
		    }
		}
	}

	private ProcessoDocumentoExpediente criarProcessoDocumentoExpediente(ProcessoExpediente pe, ProcessoDocumento processoDocumentoAto) {
		ProcessoDocumentoExpediente pde = new ProcessoDocumentoExpediente();                    
		pde.setAnexo(false);
		pde.setDtImpressao(null);
		pde.setProcessoDocumento(processoDocumentoAto);
		pde.setProcessoExpediente(pe);
		return pde;
	}

	private ProcessoExpediente criarProcessoExpediente(ProcessoTrf processoTrf, ProcessoDocumento documentoPrincipal, boolean isProcDocExistente, Date dataAtual, ProcessoDocumento processoDocumentoAto, ExpedicaoExpedienteEnum meio, TipoProcessoDocumento tipoProcessoDocumento, MiniPacVO destinatario) {
		ProcessoExpediente pe = new ProcessoExpediente();
		pe.setProcessoTrf(processoTrf);
		pe.setDtCriacao(dataAtual);
		pe.setProcessoDocumento(processoDocumentoAto);
		if(isProcDocExistente){
			pe.setProcessoDocumentoVinculadoExpediente(documentoPrincipal);
		}
		pe.setDocumentoExistente(isProcDocExistente);
		pe.setTipoProcessoDocumento(processoDocumentoAto.getTipoProcessoDocumento());
		pe.setTipoProcessoDocumento(tipoProcessoDocumento);
		pe.setUrgencia( (destinatario.getUrgente() == null) ? false : destinatario.getUrgente() );
		pe.setInTemporario(Boolean.FALSE);
		pe.setMeioExpedicaoExpediente(meio);
		return pe;
	}

	public Boolean isPodeSerIntimadoEletronicamente(ProcessoParte processoParte, boolean isPessoal) {
		return recuperarMeiosComunicacao(processoParte.getPessoa(), processoParte.getProcessoTrf(), isPessoal,
				processoParte.getProcuradoria()).contains(ExpedicaoExpedienteEnum.E);
	}

	public Endereco getMelhorEnderecoParaComunicacao(ProcessoParte processoParte, Boolean isPessoal) {
		List<Endereco> enderecoList = new ArrayList<Endereco>(0);

		Endereco enderecoSelecionado = null;

		processoParte = EntityUtil.getEntityManager().find(ProcessoParte.class, processoParte.getIdProcessoParte());
		List<ProcessoParteRepresentante> processoParteRepresentanteList = processoParte.getProcessoParteRepresentanteListAtivos();
		if (isPessoal || CollectionUtilsPje.isEmpty(processoParteRepresentanteList)) {
			if (!processoParte.getProcessoParteEnderecoList().isEmpty()) {
				enderecoList = processoParte.getEnderecos();
			} else {
				return null;
			}
		} else {
			for (ProcessoParteRepresentante ppr : processoParteRepresentanteList) {
				enderecoList.addAll(ppr.getRepresentante().getEnderecoList());
			}
		}

		if (!enderecoList.isEmpty()) {
			enderecoSelecionado = CollectionUtilsPje.max(enderecoList, "dataAlteracao", false);
		}

		return enderecoSelecionado;
	}

	public Set<Endereco> getEnderecosParaComunicacao(ProcessoParte processoParte, boolean isPessoal) {
		Set<Endereco> enderecoSet = new HashSet<Endereco>();

		List<ProcessoParteRepresentante> processoParteRepresentanteList = processoParte
				.getProcessoParteRepresentanteList();

	    // Reanexa a entidade `Pessoa` ao contexto de persistência
	    processoParte.setPessoa(EntityUtil.getEntityManager().merge(processoParte.getPessoa()));

		if (isPessoal || CollectionUtilsPje.isEmpty(processoParteRepresentanteList)) {
			if (!processoParte.getPessoa().getEnderecoList().isEmpty()) {
				enderecoSet.addAll(processoParte.getPessoa().getEnderecoList());
			} else {
				return null;
			}
		} else {
			for (ProcessoParteRepresentante ppr : processoParteRepresentanteList) {
				enderecoSet.addAll(ppr.getRepresentante().getEnderecoList());
			}
		}

		for (Endereco endereco : enderecoSet) {
			Hibernate.initialize(endereco.getCep().getMunicipio());
			Hibernate.initialize(endereco.getCep().getMunicipio().getEstado());
		}

		return enderecoSet;
	}

	public boolean isPodeInserirMeio(ProcessoParte pp, ExpedicaoExpedienteEnum meio, boolean isIntimacaoPessoal) {
		boolean retorno = true;

		retorno = verificarServicoDisponivel(meio);

		if (retorno) {
			if (meio.isExigeEndereco() && getMelhorEnderecoParaComunicacao(pp, isIntimacaoPessoal) == null) {
				retorno = false;
			} else {
				if (meio.isExigeParteCadastradaComCertificado()
						&& !isPodeSerIntimadoEletronicamente(pp, isIntimacaoPessoal)) {
					retorno = false;
				} else {
					if (meio.isExigeTelefone() && !possuiTelefoneCadastrado(pp.getPessoa())) {
						retorno = false;
					}
				}
			}
		}

		return retorno;
	}

	/**
	 * Retorna a data do prazo legal da ciência.
	 *
	 * @param ppe
	 * @param prazoPresuncaoCorreios
	 * @param mapaCalendarios
	 * @param forcarPresuncaoCorreios
	 * @return Data do prazo legal de ciência.
	 * @throws PJeBusinessException
	 * @throws PjeRestClientException 
	 * @throws PessoaInvalidaException 
	 */
	public Date obterDataPrazoLegalCiencia(ProcessoParteExpediente ppe, Integer prazoPresuncaoCorreios,
			Map<Integer, Calendario> mapaCalendarios, boolean forcarPresuncaoCorreios) throws PJeBusinessException, PjeRestClientException, PessoaInvalidaException {
		return cienciaAutomatica.obterDataPrazoLegalCiencia(ppe, prazoPresuncaoCorreios, mapaCalendarios,
				forcarPresuncaoCorreios);
	}

	/**
	 * Metodo que cria expedientes para o processo, documento e publicacao de
	 * decisao.
	 *
	 * @param processoTrf
	 * @param documento
	 * @param libPubDecisao
	 * @throws PJeBusinessException
	 */
	public boolean intimarDestinatariosPublicacao(ProcessoTrf processoTrf, ProcessoDocumento documento,
			LiberacaoPublicacaoDecisao libPubDecisao) throws PJeBusinessException {
		boolean retorno = false;

		if (processoTrf == null) {
			logger.warn("O processo não foi encontrado..");
		} else {
			if (documento == null) {
				logger.warn(
						"O documento não foi encontrado. Não será possível realizar a intimação eletrônica nos autos para o processo [{1}].",
						processoTrf.getIdProcessoTrf());
			} else {
				if (!(libPubDecisao.getTipoPrazo().isSemPrazo()) && libPubDecisao.getDataPrazoLegal() == null
						&& libPubDecisao.getPrazoLegal() == null) {
					logger.warn("O prazo para a liberação [{0}] não foi inserido",
							libPubDecisao.getIdLiberacaoPublicacaoDecisao());
				} else {
					List<ProcessoParte> partesProcesso = processoParteManager.getPartes(processoTrf);

					List<Pessoa> destinatarios = new ArrayList<Pessoa>();

					for (ProcessoParte pp : partesProcesso) {
						destinatarios.add(pp.getPessoa());
					}

					ExpedicaoExpedienteEnum meio;

					if (libPubDecisao.getTipoPublicacao().isMural()) {
						meio = ExpedicaoExpedienteEnum.R;
					} else {
						meio = ExpedicaoExpedienteEnum.A;
					}

					ProcessoDocumento novoPd = copiaDocumento(documento);

					if (intimarEletronicamente(processoTrf, libPubDecisao.getSessao(),
							destinatarios.toArray(new Pessoa[] {}), libPubDecisao.getTipoPrazo(),
							libPubDecisao.getPrazoLegal(), libPubDecisao.getDataPrazoLegal(), novoPd, false, false,
							true, meio, libPubDecisao.getDataPublicacao(), documento)) {
						retorno = true;
					}
				}
			}
		}

		return retorno;
	}

	/**
	 * Metodo que lanca o movimento para o processo e o documento publicado.
	 * 
	 * @params
	 */
	private void lancarMovimentacaoPublicacaoJulgamento(ProcessoTrf processoJudicial, ProcessoDocumento pd,
			String meio) {
		String atoPublicadoComComplemento = pd.getTipoProcessoDocumento().getTipoProcessoDocumento() + " " + meio;

		MovimentoAutomaticoService.preencherMovimento().deCodigo(92).associarAoProcesso(processoJudicial)
				.comProximoComplementoVazio().preencherComTexto(atoPublicadoComComplemento).comProximoComplementoVazio()
				.preencherComTexto(DateUtil.dateHourToString(new Date())).lancarMovimento();
	}

	public boolean verificarServicoDisponivel(ExpedicaoExpedienteEnum meioExpediente) {
		boolean retorno = true;

		if (meioExpediente != null) {
			if (meioExpediente.equals(ExpedicaoExpedienteEnum.R)) {
				MuralService muralService = ComponentUtil.getMuralService();

				String urlMural = ComponentUtil.getParametroUtil().recuperarUrlServicoMural();

				boolean muralDisponivel = muralService.verificarServicoDisponivel(2500, urlMural);

				if (!muralDisponivel) {
					retorno = false;
				}
			} else {
				if (meioExpediente.equals(ExpedicaoExpedienteEnum.P)) {
					PublicadorDJE publicadorDJE = null;

					try {
						publicadorDJE = ComponentUtil.getComponent("publicadorDJE");

						if (publicadorDJE == null) {
							retorno = false;
						}
					} catch (Exception e) {
						retorno = false;
					}
				}
			}
		}

		return retorno;
	}

	public Map<ExpedicaoExpedienteEnum, List<MiniPacVO>> recuperaInformacoesPartes(List<ProcessoParte> listaPartes,
			ExpedicaoExpedienteEnum tipoComunicacao, Integer prazoInformado) {
		if (listaPartes == null) {
			return null;
		}

		List<MiniPacVO> listaMiniPacVO = new ArrayList<MiniPacVO>();

		for (ProcessoParte parte : listaPartes) {
			MiniPacVO miniPacVO = new MiniPacVO(parte);
			atribuiParte(parte, miniPacVO, tipoComunicacao, prazoInformado);
			listaMiniPacVO.add(miniPacVO);
		}

		Map<ExpedicaoExpedienteEnum, List<MiniPacVO>> mapaMiniPacVO = new HashMap<ExpedicaoExpedienteEnum, List<MiniPacVO>>();

		mapaMiniPacVO.put(tipoComunicacao, listaMiniPacVO);

		return mapaMiniPacVO;
	}

	private void atribuiParte(ProcessoParte parte, MiniPacVO miniPacVO, ExpedicaoExpedienteEnum tipoComunicacao, Integer prazoInformado) {
		if (parte == null || miniPacVO == null) {
			return;
		}

		miniPacVO.setAtivo(parte.getIsAtivo());
		miniPacVO.setEndereco(parte.getEnderecos());
		miniPacVO.setIdProcessoParte(parte.getIdProcessoParte());
		miniPacVO.setInParticipacao(parte.getInParticipacao());

		Set<ExpedicaoExpedienteEnum> meios = new HashSet<>();

		meios.add(tipoComunicacao);

		miniPacVO.setMeios(meios);
		miniPacVO.setNome(parte.getNomeParte());
		miniPacVO.setPessoal(false);
		miniPacVO.setPrazo(prazoInformado != null ? prazoInformado : getPrazoComunicacao(tipoComunicacao));
		miniPacVO.setProcessoParte(parte);
		miniPacVO.setUrgente(false);
		miniPacVO.setPessoa(parte.getPessoa());
		miniPacVO.setProcuradoria(parte.getProcuradoria());
	}

	private int getPrazoComunicacao(ExpedicaoExpedienteEnum tipoComunicacao) {
		int prazoComunicacao = PRAZO_COMUNICACAO_DEFAULT;

		if (tipoComunicacao == ExpedicaoExpedienteEnum.G) {
			String parametroPrazoComunicacaoString = parametroService.valueOf(PARAMETRO_PRAZO_COMUNICACAO_TELEGRAMA);

			if (parametroPrazoComunicacaoString != null && !parametroPrazoComunicacaoString.isEmpty()) {
				prazoComunicacao = Integer.parseInt(parametroPrazoComunicacaoString);
			}
		}

		return prazoComunicacao;
	}
	
	public boolean jusPostulandiPodeTomarCiencia(ProcessoParteExpediente processoParteExpediente) throws PJeBusinessException {
		if (processoParteExpediente != null && Authenticator.isPapelAtual(ParametroUtil.instance().getPapelJusPostulandi())) {
			processoParteExpediente = ComponentUtil.getProcessoParteExpedienteManager().findById(processoParteExpediente.getIdProcessoParteExpediente());
			
			return !ComponentUtil.getComponent(ProcessoParteManager.class).isPartePossuiRepresentanteProcessual(
				ComponentUtil.getProcessoJudicialService().findById(processoParteExpediente.getProcessoJudicial().getIdProcessoTrf()), 
				processoParteExpediente.getPessoaParte());
		}
	
		return false;
	}

	private String createKey(ExpedicaoExpedienteEnum meio, MiniPacVO destinatario,
			TipoProcessoDocumento tipoProcessoDocumento) {
		StringBuilder key = new StringBuilder();
		key.setLength(0);
		key.append(meio.toString());
		key.append(SEPARATOR);
		key.append(destinatario.getUrgente());
		key.append(SEPARATOR);
		key.append(tipoProcessoDocumento.getIdTipoProcessoDocumento());

		return key.toString();
	}

	private TipoProcessoDocumento getTipoProcessoDocumentoDoExpediente(MiniPacVO destinatario,
			ProcessoDocumento documentoPrincipal) {
		TipoProcessoDocumento tipoProcessoDocumento = null;					
		if(destinatario.getIsHabilitaDomicilioEletronico() && destinatario.getTipoProcessoDocumento() != null) {
			tipoProcessoDocumento = destinatario.getTipoProcessoDocumento();
    	}else {
    		tipoProcessoDocumento= documentoPrincipal.getTipoProcessoDocumento();
    	}
		return tipoProcessoDocumento;
	}

	@Transactional
	public void registrarCienciaTacita(ModeloEventoDTO modeloevento, PayloadDTO payload)
			throws LoginException, PJeException {

		ObjectMapper mapper = JSONUtil.novoObjectMapper();

		ComunicacaoRecebida comunicacaoRecebida = mapper.convertValue(payload.getConteudo(), ComunicacaoRecebida.class);
		if (Objects.nonNull(comunicacaoRecebida.getIdProcessoParteExpediente())) {

			ProcessoParteExpediente ppe = ProcessoParteExpedienteManager.instance()
					.findById(comunicacaoRecebida.getIdProcessoParteExpediente());
			if (Objects.nonNull(ppe) && NumeroProcessoUtil
					.retiraMascaraNumeroProcesso(ppe.getProcessoJudicial().getProcesso().getNumeroProcesso())
					.equals(NumeroProcessoUtil.retiraMascaraNumeroProcesso(comunicacaoRecebida.getNumeroProcesso()))) {
				Authenticator.instance().authenticateUsuarioSistema();
				cienciaAutomatica.registraCienciaAutomatica(ppe.getIdProcessoParteExpediente(), new Date(), 0, null, false, false, true);
			} else {
				throw newPJeExceptionValorInvalidoParaExpediente();
			}
		} else {
			throw newPJeExceptionValorInvalidoParaExpediente();
		}
	}
	
	@Transactional
	public void registrarCienciaAutomatica(ModeloEventoDTO modeloevento, PayloadDTO payload)
			throws LoginException, PJeException {

		if (!modeloevento.getNome().equalsIgnoreCase(DomicilioEletronicoService.COMUNICACAO_ABERTA)) {

			throw new PJeException("Modelo de evento não habilitado.");
		}
		ObjectMapper mapper = JSONUtil.novoObjectMapper();

		ComunicacaoRecebida comunicacaoRecebida = mapper.convertValue(payload.getConteudo(), ComunicacaoRecebida.class);
		if (Objects.nonNull(comunicacaoRecebida.getIdProcessoParteExpediente())) {
			ProcessoParteExpediente ppe = ProcessoParteExpedienteManager.instance()
					.findById(comunicacaoRecebida.getIdProcessoParteExpediente());
			if (Objects.nonNull(ppe) && NumeroProcessoUtil
					.retiraMascaraNumeroProcesso(ppe.getProcessoJudicial().getProcesso().getNumeroProcesso())
					.equals(NumeroProcessoUtil.retiraMascaraNumeroProcesso(comunicacaoRecebida.getNumeroProcesso()))) {
				if (comunicacaoRecebida.isCienciaAutomatica()) {
					Authenticator.instance().authenticateUsuarioSistema();
					cienciaAutomatica.registraCienciaAutomatica(ppe.getIdProcessoParteExpediente(), new Date(), 0, null, false, false, true);
				} else {
					domicilioEletronicoService.juntarCertidaoCienciaDomicilio(ppe);
					registraCienciaPessoal(ppe, true);

				}
			} else {
				throw newPJeExceptionValorInvalidoParaExpediente();	
			}

		} else {
			throw newPJeExceptionValorInvalidoParaExpediente();

		}

	}
	
	/**
	 * @return PJeException (Valor inválido para expediente)
	 */
	protected PJeException newPJeExceptionValorInvalidoParaExpediente() {
		return new PJeException("Valor inválido para expediente");
	}
}