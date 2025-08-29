package br.com.infox.cliente.home;

import static org.jboss.seam.faces.FacesMessages.instance;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.AudAcordoList;
import br.com.infox.pje.list.AudAcordoVerbaList;
import br.com.infox.pje.list.AudParteImportacaoList;
import br.com.infox.utils.Constantes;
import br.com.itx.component.AbstractHome;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.csjt.pje.persistence.dao.AudImportacaoDAO;
import br.jus.csjt.pje.persistence.dao.AudImportacaoDAOImpl;
import br.jus.pje.jt.entidades.AudImportacao;
import br.jus.pje.jt.entidades.AudParteImportacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.TipoAudiencia;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.StatusAudienciaEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name(AudImportacaoHome.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class AudImportacaoHome extends AbstractHome<AudImportacao> {

	private static final long serialVersionUID = -3051812073561084083L;
	public static final String NAME = "audImportacaoHome";

	private PessoaMagistrado pessoaMagistrado;
	private String mensagemSala = "";
	

	public void pesquisarMagistrado(AudImportacao audImportacao) {
		setId(audImportacao.getIdAudImportacao());
		setInstance(audImportacao);
		this.pessoaMagistrado = getEntityManager().find(PessoaMagistrado.class, audImportacao.getIdPessoaMagistrado());

		AudParteImportacaoList audParteImportacaoList = (AudParteImportacaoList) ComponentUtil
				.getComponent(AudParteImportacaoList.NAME);
		audParteImportacaoList.setIdAudImportacao(getInstance().getIdAudImportacao());

		AudAcordoList audAcordoList = (AudAcordoList) ComponentUtil.getComponent(AudAcordoList.NAME);
		audAcordoList.getEntity().setAudImportacao(getInstance());

		AudAcordoVerbaList audAcordoVerbaList = (AudAcordoVerbaList) ComponentUtil
				.getComponent(AudAcordoVerbaList.NAME);
		audAcordoVerbaList.getEntity().setAudImportacao(getInstance());
	}

	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		if (pessoaMagistrado != null)
			this.pessoaMagistrado = pessoaMagistrado;
	}

	public String recuperaNomePartePJe(AudParteImportacao audParteImportacao) {
		String nomeParte = "";

		ProcessoParte processoParte = getEntityManager().find(ProcessoParte.class,
				audParteImportacao.getIdProcessoParte());
		if (processoParte != null && processoParte.getPessoa() != null) {
			nomeParte = processoParte.getNomeParte();
		}
		if ("".equals(nomeParte)) {
			String sql = "select p from PessoaFisica p, PessoaDocumentoIdentificacao pdi where"
					+ " p=pdi.pessoa and pdi.tipoDocumento.codTipo='CPF' " + " and pdi.numeroDocumento = :numeroCPF ";
			Query query = EntityUtil.getEntityManager().createQuery(sql);
			query.setParameter("numeroCPF", audParteImportacao.getDocCpfCnpjParte());
			PessoaFisica pessoa = (PessoaFisica) EntityUtil.getSingleResult(query);
			nomeParte = pessoa.getNome();
		}

		return nomeParte;
	}

	public String recuperaCPFCNPJ(AudParteImportacao audParteImportacao) {
		String docCpfCnpjParte = "";

		ProcessoParte processoParte = getEntityManager().find(ProcessoParte.class,
				audParteImportacao.getIdProcessoParte());
		if (processoParte != null && processoParte.getPessoa() != null) {
			Pessoa pessoaParte = processoParte.getPessoa();
			if (pessoaParte.getNomeParte().equalsIgnoreCase(audParteImportacao.getNomeParte())) {
				docCpfCnpjParte = processoParte.getPessoa().getDocumentoCpfCnpj();
			}
		} else {
			docCpfCnpjParte = audParteImportacao.getDocCpfCnpjParte();
		}

		return docCpfCnpjParte;
	}

	public String formataDocCpfCnpj(String docCpfCnpj) {
		if (docCpfCnpj == null) {
			return null;
		}
		return docCpfCnpj.replace(".", "").replace("-", "").replace("/", "");
	}

	public boolean verificaNomeReceita(AudParteImportacao audParteImportacao) {
		ProcessoParte processoParte = getEntityManager().find(ProcessoParte.class,
				audParteImportacao.getIdProcessoParte());
		if (processoParte != null && processoParte.getPessoa() != null) {
			return true;
		} else {
			List<ProcessoParte> listaProcessoParte = audParteImportacao.getAudImportacao().getProcessoTrf()
					.getProcessoParteList();
			for (Object object : listaProcessoParte) {
				ProcessoParte parte = (ProcessoParte) object;
				if (("A".equals(parte.getInParticipacao().name()) && "S".equals(audParteImportacao.getPoloAtivoParte()))
						|| ("P".equals(parte.getInParticipacao().name()) && "N".equals(audParteImportacao
								.getPoloAtivoParte()))) {
					String cpf = formataDocCpfCnpj(parte.getPessoa().getDocumentoCpfCnpj());
					String cpfParte = formataDocCpfCnpj(audParteImportacao.getDocCpfCnpjParte());
					if (cpf != null && cpfParte != null && cpf.equals(cpfParte)) {
						if (parte.getNomeParte().equalsIgnoreCase(audParteImportacao.getNomeParte())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public PessoaAdvogado consultaAdvogadoOAB(String pesqOAB, String pesqUfOAB, String pesqCPF) {
		if (pesqCPF != null && pesqCPF.length() == 11 && !pesqCPF.contains(".") && !pesqCPF.contains("-")) {
			pesqCPF = StringUtil.formartCpf(pesqCPF);
		}
		PessoaAdvogado advogadoOAB = new PessoaAdvogado();
		String sql = "select p from PessoaAdvogado p, Estado e, PessoaDocumentoIdentificacao pdi where"
				+ " p=pdi.pessoa and pdi.tipoDocumento.codTipo='CPF' " + " and pdi.numeroDocumento = :numeroCPF ";
		Query query = EntityUtil.getEntityManager().createQuery(sql);
		query.setParameter("numeroCPF", pesqCPF);
		List<PessoaAdvogado> list = query.getResultList();
		if (list != null && list.size() > 0) {
			advogadoOAB = list.get(0);
		}
		return advogadoOAB;
	}

	public String consultaNomeAdvogadoOAB(String pesqOAB, String pesqUfOAB, String pesqCPF) {
		String nomeAdvogadoOAB = "";
		PessoaAdvogado advogadoOAB = consultaAdvogadoOAB(pesqOAB, pesqUfOAB, pesqCPF);
		if (advogadoOAB != null) {
			nomeAdvogadoOAB = advogadoOAB.getNome();
		}
		return nomeAdvogadoOAB;
	}

	public boolean verificaHabilitacao(AudParteImportacao audParteImportacao) {
		String cpfParteImportacao = audParteImportacao.getDocCpfAdvParte();
		if (cpfParteImportacao != null && cpfParteImportacao.length() == 11 && !cpfParteImportacao.contains(".")
				&& !cpfParteImportacao.contains("-")) {
			cpfParteImportacao = StringUtil.formartCpf(cpfParteImportacao);
		}
		ProcessoParte processoParte = getEntityManager().find(ProcessoParte.class,
				audParteImportacao.getIdProcessoParte());
		if (processoParte != null) {
			List<ProcessoParteRepresentante> listaProcessoParteRepresentante = processoParte.getProcessoParteRepresentanteList();
			for (ProcessoParteRepresentante processoParteRepresentante : listaProcessoParteRepresentante) {
				if (processoParteRepresentante.getTipoRepresentante().getIdTipoParte() == 7) {
					String cpf = processoParteRepresentante.getParteRepresentante().getPessoa().getDocumentoCpfCnpj();
					if (cpf.equals(cpfParteImportacao)) {
						return true;
					}
				}
			}
		} else {
			List<ProcessoParte> listaProcessoParte = audParteImportacao.getAudImportacao().getProcessoTrf()
					.getProcessoParteList();
			for (Object object : listaProcessoParte) {
				ProcessoParte parte = (ProcessoParte) object;
				String cpfParte = formataDocCpfCnpj(parte.getPessoa().getDocumentoCpfCnpj());
				List<ProcessoParteRepresentante> listaProcessoParteRepresentante = parte.getProcessoParteRepresentanteList();
				for (ProcessoParteRepresentante processoParteRepresentante : listaProcessoParteRepresentante) {
					if (processoParteRepresentante.getTipoRepresentante().getIdTipoParte() == 7) {
						String cpf = processoParteRepresentante.getParteRepresentante().getPessoa()
								.getDocumentoCpfCnpj();
						String cpfCnpjParte = formataDocCpfCnpj(audParteImportacao.getDocCpfCnpjParte());
						if (cpf != null && cpfParte != null && cpf.equals(cpfParteImportacao) && cpfParte.equals(cpfCnpjParte)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public void habilitaAdvogado(AudParteImportacao audParteImportacao) {
		ProcessoParte processoParte = getEntityManager().find(ProcessoParte.class,
				audParteImportacao.getIdProcessoParte());
		if (processoParte == null) {
			List<ProcessoParte> listaProcessoParte = audParteImportacao.getAudImportacao().getProcessoTrf()
					.getProcessoParteList();
			for (Object object : listaProcessoParte) {
				ProcessoParte parte = (ProcessoParte) object;
				String cpf = formataDocCpfCnpj(parte.getPessoa().getDocumentoCpfCnpj());
				String cpfParte = formataDocCpfCnpj(audParteImportacao.getDocCpfCnpjParte());
				if (cpf != null && cpfParte != null && cpf.equals(cpfParte)) {
					if (parte.getNomeParte().equalsIgnoreCase(audParteImportacao.getNomeParte())) {
						processoParte = parte;
						break;
					}
				}
			}
		}

		PessoaAdvogado pessoaAdvogado = consultaAdvogadoOAB(audParteImportacao.getOabAdvParte(),
				audParteImportacao.getUfAdvParte(), audParteImportacao.getDocCpfAdvParte());

		ProcessoParteParticipacaoEnum polo;

		if (audParteImportacao.getPoloAtivoParte().equalsIgnoreCase("S"))
			polo = ProcessoParteParticipacaoEnum.A;
		else
			polo = ProcessoParteParticipacaoEnum.P;

		TipoParte tipoParte = getEntityManager().find(TipoParte.class, 7);

		String sql = "select p from ProcessoParte p where" + " p.pessoa= :pessoa and p.processoTrf= :processoTrf "
				+ " and p.tipoParte.idTipoParte = 7";
		Query query = EntityUtil.getEntityManager().createQuery(sql);
		query.setParameter("pessoa", pessoaAdvogado);
		query.setParameter("processoTrf", audParteImportacao.getAudImportacao().getProcessoTrf());
		ProcessoParte parte = EntityUtil.getSingleResult(query);

		ProcessoParte processoParteRepresentante = new ProcessoParte();
		if (parte == null) {

			processoParteRepresentante.setProcessoTrf(processoParte.getProcessoTrf());
			processoParteRepresentante.setPessoa(pessoaAdvogado.getPessoa());
			processoParteRepresentante.setTipoParte(tipoParte);
			processoParteRepresentante.setInParticipacao(polo);

			getEntityManager().persist(processoParteRepresentante);
		} else {
			processoParteRepresentante = parte;
		}

		ProcessoParteRepresentante parteRep = new ProcessoParteRepresentante();
		parteRep.setParteRepresentante(processoParteRepresentante);
		parteRep.setRepresentante(pessoaAdvogado.getPessoa());
		parteRep.setTipoRepresentante(tipoParte);
		parteRep.setProcessoParte(processoParte);

		getEntityManager().persist(parteRep);

		getEntityManager().flush();
		getEntityManager().refresh(processoParte);

		instance().add(StatusMessage.Severity.INFO, "Advogado habilitado com sucesso");
	}

	public boolean verificaPossibilidadeHabilitar(AudParteImportacao audParteImportacao) {
		String nomeAdvogadoOAB = consultaNomeAdvogadoOAB(audParteImportacao.getOabAdvParte(),
				audParteImportacao.getUfAdvParte(), audParteImportacao.getDocCpfAdvParte());
		if (!verificaHabilitacao(audParteImportacao) && nomeAdvogadoOAB != null
				&& nomeAdvogadoOAB.equalsIgnoreCase(audParteImportacao.getNomeAdvParte())
				&& verificaNomeReceita(audParteImportacao)) {
			return true;
		}

		return false;
	}

	/**
	 * Verifica se o registro da importação tem alguma contingência para ser
	 * apresentada na tela.
	 * 
	 * @param audImportacaoContigencia
	 * @return boolean
	 * @author U006184 - Thiago Oliveira
	 * @author U002063 - Levi Mota Data: 12/10/2011
	 */
	public boolean verificaContingencia(AudImportacao audImportacaoContingencia) {

		boolean contingencia = verificaDesistencia(audImportacaoContingencia)
				|| verificaIncompetenciaAcolhida(audImportacaoContingencia)
				|| verificaAusenciaTotalPoloAtivo(audImportacaoContingencia);

		return contingencia;
	}

	/**
	 * Valida que não haja mais de uma contingência ao mesmo tempo (RN09 do
	 * PJE_UC008.1).
	 * 
	 * @param audImportacaoContingencia
	 */
	private void validaContingencias(AudImportacao audImportacaoContingencia) {
		boolean desistencia = verificaDesistencia(audImportacaoContingencia);
		boolean incompetencia = verificaIncompetenciaAcolhida(audImportacaoContingencia);
		boolean ausencia = verificaAusenciaTotalPoloAtivo(audImportacaoContingencia);

		if ((desistencia && incompetencia) || (desistencia && ausencia) || (incompetencia && ausencia)) {
			throw new AplicationException(
					"Houve erro de lançamentos durante a realização da Audiência, não há possibilidade de haver a ocorrência de mais de uma contingência para o processo. Tal situação impossibilita o Sistema de lançar o movimento correto correspondente!");
		}
	}

	/**
	 * Verifica se há desistência total
	 * 
	 * @param audImportacao
	 * @return boolean
	 * @author U006184 - Thiago de Almeida Olveira Data: 26/10/2011
	 * @author U013683 - Antonio Lucas Data: 11/12/2012 - Movido pro DAO pra evitar duplicação
	 */
	private boolean verificaDesistencia(AudImportacao audImportacao) {
		AudImportacaoDAOImpl audImportacaoDAO = ComponentUtil.getComponent(AudImportacaoDAOImpl.NAME);
		return audImportacaoDAO.verificaDesistencia(audImportacao);
	}

	/**
	 * Verifica se a há imcompetência acolhida
	 * 
	 * @param audImportacao
	 * @return boolean
	 * @author U006184 - Thiago Oliveira Data: 26/10/2011
	 * @author U013683 - Antonio Lucas Data: 11/12/2012 - Movido pro DAO pra evitar duplicação
	 */
	private boolean verificaIncompetenciaAcolhida(AudImportacao audImportacao) {
		AudImportacaoDAOImpl audImportacaoDAO = ComponentUtil.getComponent(AudImportacaoDAOImpl.NAME);
		return audImportacaoDAO.verificaIncompetenciaAcolhida(audImportacao);
	}

	/**
	 * Verifica se as todas as partes são ausentes.
	 * 
	 * @param audImportacao
	 * @return booelan
	 * @author U006184 - Thiago Oliveira Data: 26/10/2011
	 * @author U013683 - Antonio Lucas Data: 11/12/2012 - Movido pro DAO pra evitar duplicação
	 */
	private boolean verificaAusenciaTotalPoloAtivo(AudImportacao audImportacao) {
		AudImportacaoDAOImpl audImportacaoDAO = ComponentUtil.getComponent(AudImportacaoDAOImpl.NAME);
		return audImportacaoDAO.verificaAusenciaTotalPoloAtivo(audImportacao);
	}

	public boolean verificaAusenciaTotalPoloAtivo() {
		return verificaAusenciaTotalPoloAtivo(getInstance());
	}

	public boolean verificaAusenciaParcialPoloAtivo() {
		return obtemListaParteAtivaAusente().size() > 0;
	}

	/**
	 * @param audImportacao
	 * @return List<AudParteImportacao>
	 */
	public List<AudParteImportacao> obtemListaParteAtivaAusente() {
		List<AudParteImportacao> listaAudParteImportacao = this.getInstance().getAudParteImportacao();

		List<AudParteImportacao> listaAudParteAtivaAusente = new ArrayList<AudParteImportacao>();

		if (listaAudParteImportacao.size() > 0) {
			for (AudParteImportacao audParteImportacao : listaAudParteImportacao) {
				// Verifica se é polo ativo
				if (audParteImportacao.getPoloAtivoParte() != null
						&& "S".equalsIgnoreCase(audParteImportacao.getPoloAtivoParte()))
					// Se for polo ativo (if anterior) e não estiver presente
					// lança cria sentença
					if (!audParteImportacao.getPartePresente().equalsIgnoreCase("S")) {
						listaAudParteAtivaAusente.add(audParteImportacao);
					}
			}
		}
		return listaAudParteAtivaAusente;
	}

	public String verificaMovimento(AudImportacao audImportacao) {
		String movimento = "";
		String codEncerramento = audImportacao.getAndamentoEncerramento();
		if (codEncerramento != null & !"".equals(codEncerramento)) {
			ProcessoAudiencia processoAudiencia = getEntityManager().find(ProcessoAudiencia.class,
					audImportacao.getIdProcessoAudiencia());
			if (processoAudiencia != null && processoAudiencia.getProcessoTrf() != null
					&& processoAudiencia.getProcessoTrf().getOrgaoJulgador() != null
					&& audImportacao.getDtAdiamento() != null) {
				String orgaoJulgador = processoAudiencia.getProcessoTrf().getOrgaoJulgador().getOrgaoJulgador();

				if ("001".equals(codEncerramento)) {
					movimento = "970 - Audiência Inicial designada";
				} else if ("002".equals(codEncerramento)) {
					movimento = "970 - Audiência de Instrução designada";
				} else if ("003".equals(codEncerramento)) {
					movimento = "970 - Audiência de Julgamento designada";
				} else if ("008".equals(codEncerramento)) {
					movimento = "970 - Audiência Una designada";
				} else if ("201".equals(codEncerramento)) {
					movimento = "970 - Audiência de Conciliação (fase de execução)";
				} else if ("999".equals(codEncerramento)) {
					movimento = "970 - Audiência de Instrução designada";
				}

				SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy, HH:mm");

				movimento += " " + formatoData.format(audImportacao.getDtAdiamento()) + " e " + orgaoJulgador + ".";
			}
		}
		return movimento;
	}

	/**
	 * Verifica se o perito já está cadastrado no Pje
	 * 
	 * @return boolean
	 * @author U006184 Data: 26/10/2011
	 */
	@SuppressWarnings("unchecked")
	public boolean verificaPeritoPreCadastrado() {
		if (getInstance().getNomePerito() != null) {
			String sql = "select p from PessoaPerito p where upper(p.nome) = upper(:nomePerito)";
			Query query = EntityUtil.getEntityManager().createQuery(sql);
			query.setParameter("nomePerito", getInstance().getNomePerito());
			List<PessoaPerito> list = query.getResultList();
			if (list != null && list.size() > 0) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Método que atualiza o Magistrado
	 * 
	 * @return void
	 * @author U006184 - Thiago Oliveira Data: 26/10/2011
	 */
	public void atualizaMagistrado() {
		if (pessoaMagistrado != null) {
			getInstance().setNomeMagistrado(pessoaMagistrado.getNome());
			getInstance().setIdPessoaMagistrado(pessoaMagistrado.getIdUsuario());
			EntityUtil.getEntityManager().merge(getInstance());
			EntityUtil.getEntityManager().flush();
			EntityUtil.getEntityManager().refresh(getInstance());
			instance().add(StatusMessage.Severity.INFO, "Magistrado atualizado com sucesso!");
			this.pesquisarMagistrado(getInstance());
		}
	}

	/**
	 * Realiza a importação definitva dos dados para as tabelas definitivas,
	 * lançando os movimento necessários em função do resultado da audiência.
	 * 
	 * @param audImportacao
	 *            Instância do AudImportação
	 */
	public void importarDados(AudImportacao audImportacao) {
		AudImportacaoDAOImpl audImportacaoDAO = ComponentUtil.getComponent(AudImportacaoDAOImpl.NAME);
		audImportacaoDAO.importarDados(audImportacao, getInstance().getProcessoTrf(), getInstance().getIdProcessoAudiencia());
	}
	
	/**
	 * Cria uma audiência no PJE através dos dados recebidos do AUD, sendo que o
	 * processo foi inserido manualmente através do AUD e não de uma pauta
	 * carregada do PJe.
	 */
	public void criarAudienciaProcessoInseridoAud(AudImportacao audImportacao) {
		ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent("processoTrfHome");
		ProcessoTrf processoTrf = processoTrfHome.carregarProcesso(audImportacao);
		processoTrfHome.setInstance(processoTrf);

		Date dtAudiencia = audImportacao.getDtInicio();
		String tipoAudienciaAUD = "001";

		ProcessoAudiencia processoAudiencia = new ProcessoAudiencia();
		processoAudiencia.setProcessoTrf(processoTrf);
		processoAudiencia.setDtMarcacao(new Date());
		processoAudiencia.setDtAudiencia(dtAudiencia);
		processoAudiencia.setDtInicio(dtAudiencia);
		TipoAudiencia tipoAudiencia = getEntityManager().find(TipoAudiencia.class, Constantes.MapaTipoAudienciaAudParaPje.get(tipoAudienciaAUD));
		processoAudiencia.setTipoAudiencia(tipoAudiencia);

		ProcessoAudienciaHome processoAudienciaHome = ComponentUtil.getComponent("processoAudienciaHomeConversation");
		if (audImportacao.getDtFim() == null) {
			// Calcula o horário de fim da audiência baseado no tempo de
			// audiência padrão para o orgão julgador e tipo de audiência
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dtAudiencia);

			Integer tempoAudienciaPadraoOrgaoJulgador = processoAudienciaHome.getTempoAudienciaPadraoOrgaoJulgador(processoTrf.getOrgaoJulgador(), tipoAudiencia);
			calendar.add(Calendar.MINUTE, tempoAudienciaPadraoOrgaoJulgador);
			processoAudiencia.setDtFim(calendar.getTime());
		} else {
			processoAudiencia.setDtFim(audImportacao.getDtFim());
		}

		processoAudiencia.setInAtivo(Boolean.TRUE);
		processoAudiencia.setStatusAudiencia(StatusAudienciaEnum.F);

		// Setando como pessoa criador da audiência o magistrado
		Pessoa pessoa = new Pessoa();
		pessoa.setIdUsuario(audImportacao.getIdPessoaMagistrado());
		Contexts.getSessionContext().set(Authenticator.USUARIO_LOGADO, pessoa);

		Sala salaAudiencia = buscaSalaAudiencia(tipoAudiencia, dtAudiencia);
				
		if(salaAudiencia == null) {
			throw new AplicationException("Não foi encontrada nenhuma sala de audiência disponível para a data informada.");
		}
				
		processoAudiencia.setSalaAudiencia(salaAudiencia);

		Pessoa pessoaLogada = Authenticator.getPessoaLogada();
		processoAudiencia.setInAtivo(true);
		processoAudiencia.setPessoaMarcador(pessoaLogada);
		processoAudiencia.setDtMarcacao(new Date());

		getEntityManager().persist(processoAudiencia);
		getEntityManager().flush();

		ComponentUtil.getProcessoAudienciaManager().lancarMovimentoAudiencia(processoAudiencia.getTipoAudiencia(),
				processoAudiencia.getDtInicioFormatada(), processoTrf.getOrgaoJulgador().getOrgaoJulgador(),
				processoTrf.getProcesso(), null, processoAudiencia.getStatusAudiencia());
		getEntityManager().flush();

		audImportacao.setIdProcessoAudiencia(processoAudiencia.getIdProcessoAudiencia());
	}

	/*
	 * Busca uma sala de audiência que esteja ativa para a data desejada
	 * @author U013683 - Antonio Lucas Data: 11/12/2012 - Movido pro DAO pra evitar duplicação
	 */
	private Sala buscaSalaAudiencia(TipoAudiencia tipoAudiencia, Date dtAudiencia) {
		AudImportacaoDAOImpl audImportacaoDAO = ComponentUtil.getComponent(AudImportacaoDAOImpl.NAME);
		return audImportacaoDAO.buscaSalaAudiencia(tipoAudiencia, dtAudiencia);
	}
	
	public String getNumCNJ(AudImportacao audImportacao) {
		/*
		 * PJE-JT: Ricardo Scholz : PJEII-4058 - 2013-01-24 Alteracoes feitas pela JT.
		 * Substituição do método de formatação do número do processo. O método anterior
		 * utilizava as informações de número de processo carregadas do AUD, enquanto 
		 * o método atual baseia-se no objeto 'ProcessoTrf' referenciado pelo objeto 
		 * 'AudImportacao', deixando que aquele realize a formatação. Na issue em questão, 
		 * o objeto 'AudImportacao' continha um valor equivocado no atributo 
		 * 'origemProcesso', que resultava em um erro na string retornada.
		 */
		return audImportacao != null ? audImportacao.getProcessoTrf().getNumeroProcesso():"";
		/*
		 * PJE-JT: Fim.
		 */
	}

	public String converteString(Integer numero) {
		String retorno = "";
		if (numero != null) {
			retorno = numero + "";
		}
		return retorno;
	}

	public void confirmar() {
		EntityUtil.getEntityManager().merge(getInstance());
		EntityUtil.getEntityManager().flush();
		EntityUtil.getEntityManager().refresh(getInstance());
		AudImportacaoDAO audImportacaoDAO = ComponentUtil.getComponent(AudImportacaoDAOImpl.NAME);
		Usuario pessoaLogada = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
		validaContingencias(getInstance());
		audImportacaoDAO.criarDocumentoAta(getInstance(), pessoaLogada);
		instance().add(StatusMessage.Severity.INFO, "Registro inserido com sucesso");
	}

	/**
	 * Método que visualiza a Ata na tela
	 * 
	 * @return void
	 * @author U006184 - Thiago Oliveira Data: 26/10/2011
	 */
	public void visualizarAta() {

		if (getInstance().getConteudoDocumento() != null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			response.setContentType("text/html;charset=ISO-8859-1");
			response.setCharacterEncoding("ISO-8859-1");
			byte arquivo[] = getInstance().getConteudoDocumento().getBytes();

			response.setContentLength(arquivo.length);
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Content-Disposition", "inline");

			OutputStream out = null;
			try {
				out = response.getOutputStream();
				out.write(arquivo);
				out.flush();
				facesContext.responseComplete();
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				try {
					if(out != null){
						out.close();
					}else{
						instance().add(StatusMessage.Severity.ERROR, "Não existe conexão.");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			instance().add(StatusMessage.Severity.INFO, "Não existe ata para essa audiência!");
		}
	}

	public void setTab() {
		super.setTab("magistradoTab");
	}

	@Override
	public void setTab(String tab) {
		super.setTab(tab);
	}

	public boolean verificaDesistencia() {
		return verificaDesistencia(getInstance());
	}

	public boolean verificaIncompetenciaAcolhida() {
		return verificaIncompetenciaAcolhida(getInstance());
	}

	public boolean verificaIncompetenciaRejeitada() {
		AudImportacaoDAOImpl audImportacaoDAO = ComponentUtil.getComponent(AudImportacaoDAOImpl.NAME);
		return audImportacaoDAO.verificaIncompetenciaRejeitada(getInstance());
	}

	@Override
	public void setId(Object id) {
		super.setId(id);
		// Setando o id em ProcessoTrfHome para que a aba de Resultado Sentença
		// funcione corretamente
		ProcessoTrfHome.instance().setId(this.getInstance().getIdProcesso());
	}

	public void verificaImpedimentosSala() {
		AudImportacao audImportacao = getInstance();
		ProcessoTrf processoTrf = audImportacao.getProcessoTrf();
		ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent("processoTrfHome");
		processoTrfHome.setInstance(processoTrf);
		ProcessoAudienciaHome processoAudienciaHome = ComponentUtil.getComponent(ProcessoAudienciaHome.NAME);
		
		Date dtAudiencia = audImportacao.getDtAdiamento();
		String tipoAudienciaAUD = audImportacao.getAndamentoEncerramento();
		
		if(tipoAudienciaAUD != null && ! tipoAudienciaAUD.equals("")) {
			ProcessoAudiencia processoAudiencia = new ProcessoAudiencia();
			processoAudiencia.setDtAudiencia(dtAudiencia);
			processoAudiencia.setDtInicio(dtAudiencia);
			
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
	
			if (salaAudiencia == null) {
				throw new AplicationException("Não foi encontrada nenhuma sala de audiência disponível para a data informada.");
			}
	
			processoAudiencia.setSalaAudiencia(salaAudiencia);
			
			processoAudienciaHome.consultaBloqueiosPauta(dtAudiencia);
			processoAudienciaHome.verificarImpedimentosData(processoAudiencia);
			List<String> observacoesConfirmacao = processoAudienciaHome.getObservacoesConfirmacao();
			if(!observacoesConfirmacao.isEmpty()) {
				mensagemSala = observacoesConfirmacao.get(0) + ". A designação da audiência no PJe deve ser feita manualmente.";
				
				if( audImportacao.getObservacoes() != null && audImportacao.getObservacoes().indexOf("A designação da audiência no PJe deve ser feita manualmente") == -1 ){ 
					String observacoes = audImportacao.getObservacoes();
					audImportacao.setObservacoes(mensagemSala + "\n" + observacoes); 
				}
				
			} else {
				mensagemSala = "";
			}
		}
	}
	
	public String getMensagemSala() {
		return mensagemSala;
	}

	public void setMensagemSala(String mensagemSala) {
		this.mensagemSala = mensagemSala;
	}

	
}
