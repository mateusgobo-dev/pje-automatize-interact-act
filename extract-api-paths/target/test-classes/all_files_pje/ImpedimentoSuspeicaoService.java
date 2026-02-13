package br.jus.cnj.pje.nucleo.service;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.springframework.util.CollectionUtils;

import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.business.dao.ImpedimentoSuspeicaoDAO;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.pje.je.entidades.ComplementoProcessoJE;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.entidades.ImpedimentoSuspeicao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.RegraImpedimentoSuspeicaoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.search.Search;

/**
 * Classe responsavel pela parte negocial em que se refere a aba impedimento/suspeicao no cadastro do processo.
 * 
 */
@Name(ImpedimentoSuspeicaoService.NAME)
public class ImpedimentoSuspeicaoService extends BaseService implements Serializable {
	
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 8218568201922227136L;
	
	public static final String NAME = "impedimentoSuspeicaoService";
	
	@Logger
	private transient Log log;
	
	/**
	 * Metodo responsavel pelas regras de negocio da pesquisa de regra impedimento.
	 * 
	 * @param filtroRegra RegraImpedimentoSuspeicao regra selecionada.
	 * @param filtroPessoaMagistrado PessoaMagistrado filtro do magistrado.
	 * @param search Search objeto com os dados da paginacao.
	 * @return List<ImpedimentoSuspeicao> lista com os impedimento/suspeicao.
	 */
	public List<ImpedimentoSuspeicao> pesquisar(RegraImpedimentoSuspeicaoEnum filtroRegra, PessoaMagistrado filtroPessoaMagistrado, Search search) throws PJeBusinessException {
		ImpedimentoSuspeicaoDAO impedimentoSuspeicaoDAO = ComponentUtil.getComponent(ImpedimentoSuspeicaoDAO.class);
		List<ImpedimentoSuspeicao> listaRetorno = impedimentoSuspeicaoDAO.pesquisar(filtroRegra, filtroPessoaMagistrado, search);
		if (CollectionUtils.isEmpty(listaRetorno)) {
			throw new PJeBusinessException("impedimentoSuspeicao.nenhum.resultado.encontrado", new Object());
		}
		return listaRetorno;
	}
	
	/**
	 * Metodo responsavel por remover a entidade ImpedimentoSuspeicao.
	 * 
	 * @param impedimentoSuspeicao ImpedimentoSuspeicao entidade a ser removida.
	 */
	public void remover(ImpedimentoSuspeicao impedimentoSuspeicao) {
		try {
			ImpedimentoSuspeicaoDAO impedimentoSuspeicaoDAO = ComponentUtil.getComponent(ImpedimentoSuspeicaoDAO.class);
			impedimentoSuspeicaoDAO.remove(impedimentoSuspeicao);
			impedimentoSuspeicaoDAO.flush();
		} catch (PJeDAOException e){
			FacesMessages.instance().add(Severity.ERROR, "Houve um erro de banco de dados ao tentar remover o impedimento suspeição.");
			throw e;
		}		
	}
	
	/**
	 * Metodo responsavel pelas regras de negocio no momento da inclusao do impedimento suspeicao.
	 * 
	 * @param impedimentoSuspeicao ImpedimentoSuspeicao entidade a ser persistida.
	 * @throws PJeBusinessException 
	 */
	public void salvar(ImpedimentoSuspeicao impedimentoSuspeicao) throws PJeBusinessException {
		try {
			if (impedimentoSuspeicao == null) {
				throw new PJeBusinessException("Parâmetros inválidos!");
			}
			ImpedimentoSuspeicaoDAO impedimentoSuspeicaoDAO = ComponentUtil.getComponent(ImpedimentoSuspeicaoDAO.class);
			HibernateUtil.getSession().clear();
			preencherNumeroCPF(impedimentoSuspeicao);
			validarDuplicacaoRegraMotivo(impedimentoSuspeicao);
			if (impedimentoSuspeicao.getId() == null) {
				impedimentoSuspeicaoDAO.persist(impedimentoSuspeicao);	
			} else {
				impedimentoSuspeicaoDAO.merge(impedimentoSuspeicao);
			}
			impedimentoSuspeicaoDAO.flush();
		} catch (PJeDAOException e){
			throw new PJeDAOException("Houve um erro de banco de dados ao tentar salvar o impedimento suspeição: " + e.getMessage());
		} 
	}
	
 	/**
 	 * Responsavel por retirar a mascara do CPF e preencher no objeto ImpedimentoSuspeicao.
 	 * 
 	 * @param impedimentoSuspeicao ImpedimentoSuspeicao objeto a ser persistido.
 	 */
	private void preencherNumeroCPF(ImpedimentoSuspeicao impedimentoSuspeicao) {
		Pessoa pessoa = impedimentoSuspeicao.getPessoaParteAdvogado();
		if (pessoa != null && pessoa.getIdPessoa() != null && pessoa instanceof PessoaFisica) {
			PessoaFisica pessoaFisica = ((PessoaFisica)pessoa);
			String cpf = pessoaFisica.getNumeroCPF();
			String cpfSemMascara = InscricaoMFUtil.retiraMascara(cpf);
			pessoaFisica.setNumeroCPF(cpfSemMascara);
		}
	}
	
	/**
	 * Valida a duplicacao do motivo para a mesma regra informada.
	 * 
	 * @param impedimentoSuspeicao ImpedimentoSuspeicao dados do impedimento/suspeicao a ser inserido.
	 * @throws PJeBusinessException 
	 */
	private void validarDuplicacaoRegraMotivo(ImpedimentoSuspeicao impedimentoSuspeicao) throws PJeBusinessException {
		if (impedimentoSuspeicao != null 
				&& StringUtils.isNotEmpty(impedimentoSuspeicao.getDescricaoMotivo()) 
					&& impedimentoSuspeicao.getPessoaMagistrado() != null) {
			RegraImpedimentoSuspeicaoEnum regraImpedimentoSuspeicao = impedimentoSuspeicao.getRegraImpedimentoSuspeicaoEnum();
			ImpedimentoSuspeicaoDAO impedimentoSuspeicaoDAO = ComponentUtil.getComponent(ImpedimentoSuspeicaoDAO.class);
			ImpedimentoSuspeicao resultado = impedimentoSuspeicaoDAO.recuperarImpedimentoSuspeicaoPeloMotivo(regraImpedimentoSuspeicao, impedimentoSuspeicao.getDescricaoMotivo().trim(), impedimentoSuspeicao.getPessoaMagistrado().getIdUsuario());
			if (resultado != null && !resultado.getId().equals(impedimentoSuspeicao.getId())) {
				throw new PJeBusinessException("impedimentoSuspeicao.cadastro.duplicacao.motivo", regraImpedimentoSuspeicao.getLabel());
			}
			
		}
	}
	
	/**
	 * Valida a impedimento/suspeicao por advogado.
	 * 
	 * @param impedimentoSuspeicao
	 *            ImpedimentoSuspeicao impedimento/suspeicao a ser validado.
	 * @param processoTrf
	 *            ProcessoTrf dados do processo.
	 * @return List<ImpedimentoSuspeicao> lista com os impedimentos/suspeicao
	 *         enquadrados.
	 * @throws PJeBusinessException
	 */
	private List<ImpedimentoSuspeicao> validarRegraPorAdvogado(ImpedimentoSuspeicao impedimentoSuspeicao,
			ProcessoTrf processoTrf) throws PJeBusinessException {
		List<ImpedimentoSuspeicao> listaRetorno = new ArrayList<>();
		if (impedimentoSuspeicao.getPoloAtivo()) {
			List<ProcessoParte> listaAdvogadosAtivo = processoTrf.getListaAdvogadosPoloAtivo();
			listaRetorno.addAll(validarRegraCpfOuOAB(listaAdvogadosAtivo, impedimentoSuspeicao));
		}
		if (impedimentoSuspeicao.getPoloPassivo()) {
			List<ProcessoParte> listaAdvogadosPassivo = processoTrf.getListaAdvogadosPoloPassivo();
			listaRetorno.addAll(validarRegraCpfOuOAB(listaAdvogadosPassivo, impedimentoSuspeicao));
		}
		if (impedimentoSuspeicao.getPoloIndefinido()) {
			List<ProcessoParte> listaTodosAdvogados = ComponentUtil.getProcessoParteManager().getListaAdvogadosTodasPartes(processoTrf);
			listaRetorno.addAll(validarRegraCpfOuOAB(listaTodosAdvogados, impedimentoSuspeicao));
		}
		return listaRetorno;
	}
	
	
	
	/**
	 * Valida a regra pelo advogado por CPF ou OAB.
	 * 
	 * @param impedimentoSuspeicao
	 *            ImpedimentoSuspeicao impedimento/suspeicao a ser validado.
	 * @param processoTrf
	 *            ProcessoTrf dados do processoTrf.
	 * @param listaRetorno
	 *            List<ImpedimentoSuspeicao> lista com os impedimentos/suspeicao
	 *            enquadrados.
	 * @throws PJeBusinessException
	 */
	private List<ImpedimentoSuspeicao> validarRegraCpfOuOAB(List<ProcessoParte> listaAdvogados, ImpedimentoSuspeicao impedimentoSuspeicao) throws PJeBusinessException {
		List<ImpedimentoSuspeicao> listaRetorno = new ArrayList<>();
		PessoaFisicaManager pessoaFisicaManager = ComponentUtil.getPessoaFisicaManager();
		PessoaAdvogadoManager pessoaAdvogadoManager = ComponentUtil.getComponent(PessoaAdvogadoManager.class);
		if (impedimentoSuspeicao.getPessoaParteAdvogado() != null && impedimentoSuspeicao.getPessoaParteAdvogado().getIdPessoa() != null) {
			PessoaFisica pessoaFisicaIs = pessoaFisicaManager.encontraPessoaFisicaPorPessoa(impedimentoSuspeicao.getPessoaParteAdvogado());
			PessoaAdvogado pessoaAdvogadoIs = pessoaAdvogadoManager.especializa(pessoaFisicaIs);
			for (ProcessoParte processoParte : listaAdvogados) {
				String numeroCpfParte = ComponentUtil.getProcessoParteManager().recuperarCpfParte(processoParte);
				if (StringUtils.isNotEmpty(numeroCpfParte)
						&& pessoaFisicaIs != null
						&& StringUtils.isNotEmpty(pessoaFisicaIs.getNumeroCPF())
						&& numeroCpfParte.equals(InscricaoMFUtil.retiraMascara(pessoaFisicaIs.getNumeroCPF()))
						&& ProcessoParteSituacaoEnum.A.equals(processoParte.getInSituacao())) {
					listaRetorno.add(impedimentoSuspeicao);
				} else {
					PessoaFisica pessoaFisica = pessoaFisicaManager.encontraPessoaFisicaPorPessoa(processoParte.getPessoa());
					PessoaAdvogado pessoaAdvogado = pessoaAdvogadoManager.especializa(pessoaFisica);
					if (pessoaAdvogado != null && StringUtils.isNotEmpty(pessoaAdvogado.getNumeroOAB()) && pessoaAdvogadoIs != null && StringUtils.isNotEmpty(pessoaAdvogadoIs.getNumeroOAB())
							&& pessoaAdvogado.getNumeroOAB().equals(pessoaAdvogadoIs.getNumeroOAB())
							&& pessoaAdvogado.getUfOAB() != null
							&& pessoaAdvogadoIs.getUfOAB() != null
							&& pessoaAdvogado.getUfOAB().getIdEstado() == pessoaAdvogadoIs.getUfOAB().getIdEstado() && ProcessoParteSituacaoEnum.A.equals(processoParte.getInSituacao())) {
						listaRetorno.add(impedimentoSuspeicao);
					}
				}
			}
		}
		return listaRetorno;
	}
	
	/**
	 * Recupera o objeto de acordo com o id informado.
	 * 
	 * @param id Object id do impedimento suspeicao.
	 * @return ImpedimentoSuspeicao
	 */
	public ImpedimentoSuspeicao findById(Object id) {
		ImpedimentoSuspeicaoDAO impedimentoSuspeicaoDAO = ComponentUtil.getComponent(ImpedimentoSuspeicaoDAO.class);
		return impedimentoSuspeicaoDAO.find(id);
	}
	
	/**
	 * Recupera o numero total de registros que atendem aos criterios do objeto de consulta.
	 * 
	 * @param search o objeto de consulta
	 * @return o numero total de objetos que atendem aos criterios
	 */
	public Long count(Search search) throws PJeBusinessException {
		ImpedimentoSuspeicaoDAO impedimentoSuspeicaoDAO = ComponentUtil.getComponent(ImpedimentoSuspeicaoDAO.class);
		Long count = impedimentoSuspeicaoDAO.count(search);
		if (count.equals(0L)) {
			throw new PJeBusinessException("impedimentoSuspeicao.nenhum.resultado.encontrado", new Object());
		}
		return count;
	}
	
	/**
	 * Valida as regras de impedimento/suspeicao.
	 * 
	 * @param listaImpedimentoSuspeicao
	 *            List<ImpedimentoSuspeicao> lista com os impledimentos a serem
	 *            analisados.
	 * @param processoTrf
	 *            ProcessoTrf processo da tarefa selecionada.
	 * @return List<ImpedimentoSuspeicao> lista com as regras que se encaixam no
	 *         processo.
	 */
	public List<ImpedimentoSuspeicao> validarRegras(List<ImpedimentoSuspeicao> listaImpedimentoSuspeicao, ProcessoTrf processoTrf) {
		List<ImpedimentoSuspeicao> listaRetorno = new ArrayList<>();
		for (ImpedimentoSuspeicao impedimentoSuspeicao : listaImpedimentoSuspeicao) {
			List<ImpedimentoSuspeicao> listaImpedimentoSuspeicaoEnquadrados = null;
			if (RegraImpedimentoSuspeicaoEnum.P.equals(impedimentoSuspeicao.getRegraImpedimentoSuspeicaoEnum())) {
				listaImpedimentoSuspeicaoEnquadrados = validarRegraPorParte(impedimentoSuspeicao, processoTrf);
			}
			if (RegraImpedimentoSuspeicaoEnum.U.equals(impedimentoSuspeicao.getRegraImpedimentoSuspeicaoEnum())) {
				listaImpedimentoSuspeicaoEnquadrados = validarRegraPorEstadoMunicipio(impedimentoSuspeicao, processoTrf);
			}
			if (RegraImpedimentoSuspeicaoEnum.E.equals(impedimentoSuspeicao.getRegraImpedimentoSuspeicaoEnum())) {
				listaImpedimentoSuspeicaoEnquadrados = validarRegraPorAnoEleicao(impedimentoSuspeicao, processoTrf);
			}
			if (RegraImpedimentoSuspeicaoEnum.A.equals(impedimentoSuspeicao.getRegraImpedimentoSuspeicaoEnum())) {
				try {
					listaImpedimentoSuspeicaoEnquadrados = validarRegraPorAdvogado(impedimentoSuspeicao, processoTrf);
				} catch (PJeBusinessException be) {
					log.error("Erro ao recuperar PessoaAdvogado: " + be.getMessage());
				}
			}
			if (!CollectionUtils.isEmpty(listaImpedimentoSuspeicaoEnquadrados)) {
				listaRetorno.addAll(listaImpedimentoSuspeicaoEnquadrados);
			}
		}
		return listaRetorno;
	}
	
	/**
	 * Valida a regra por ano eleicao.
	 * 
	 * @param impedimentoSuspeicao
	 *            ImpedimentoSuspeicao impedimento/suspeicao a ser validado.
	 * @param processoTrf
	 *            ProcessoTrf dados do processo.
	 * @return List<ImpedimentoSuspeicao> lista com os impedimentos/suspeicao
	 *         enquadrados.
	 */
	private List<ImpedimentoSuspeicao> validarRegraPorAnoEleicao(ImpedimentoSuspeicao impedimentoSuspeicao, ProcessoTrf processoTrf) {
		List<ImpedimentoSuspeicao> listaRetorno = new ArrayList<>();
		Eleicao eleicao = processoTrf.getComplementoJE().getEleicao();
		if (eleicao != null && impedimentoSuspeicao.getEleicao().getCodObjeto().equals(eleicao.getCodObjeto())) {
			listaRetorno.add(impedimentoSuspeicao);
		}
		return listaRetorno;
	}
	
	/**
	 * Valida a regra por estado.
	 * 
	 * @param impedimentoSuspeicao
	 *            ImpedimentoSuspeicao impedimento/suspeicao a ser validado.
	 * @param processoTrf
	 *            ProcessoTrf dados do processo.
	 * @return List<ImpedimentoSuspeicao> lista com os impedimentos/suspeicao
	 *         enquadrados.
	 */
	private List<ImpedimentoSuspeicao> validarRegraPorEstadoMunicipio(ImpedimentoSuspeicao impedimentoSuspeicao, ProcessoTrf processoTrf) {
		List<ImpedimentoSuspeicao> listaRetorno = new ArrayList<>();
		ComplementoProcessoJE complementoJE = processoTrf.getComplementoJE();
		if (complementoJE != null) {
			// Valida estado
			if (impedimentoSuspeicao.getMunicipio() == null && complementoJE.getEstadoEleicao() != null && impedimentoSuspeicao.getEstado().getIdEstado() == complementoJE.getEstadoEleicao().getIdEstado()) {
				listaRetorno.add(impedimentoSuspeicao);
			}
			// Valida municipio
			if (impedimentoSuspeicao.getMunicipio() != null
					&& complementoJE.getMunicipioEleicao() != null && impedimentoSuspeicao.getMunicipio().getIdMunicipio() == complementoJE.getMunicipioEleicao().getIdMunicipio()) {
				listaRetorno.add(impedimentoSuspeicao);
			}
		}
		return listaRetorno;
	}
	
	/**
     * Valida a regra por parte do impedimento/suspeicao.
     * 
     * @param impedimentoSuspeicao ImpedimentoSuspeicao impedimento/suspeicao a ser validado.
     * @param processoTrf ProcessoTrf dados do processo.
     * @return List<ImpedimentoSuspeicao> lista com os impedimentos/suspeicao enquadrados.
    */
	private List<ImpedimentoSuspeicao> validarRegraPorParte(ImpedimentoSuspeicao impedimentoSuspeicao, ProcessoTrf processoTrf) {
		List<ImpedimentoSuspeicao> listaRetorno = new ArrayList<>();
		if (impedimentoSuspeicao.getPoloAtivo()) {
			listaRetorno.addAll(validarRegraPoloAtivo(impedimentoSuspeicao, processoTrf));
		}
		if (impedimentoSuspeicao.getPoloPassivo()) {
			listaRetorno.addAll(validarRegraPoloPassivo(impedimentoSuspeicao, processoTrf));
		}
		if (impedimentoSuspeicao.getPoloIndefinido()) {
			listaRetorno.addAll(validarRegraPoloIndefinido(impedimentoSuspeicao, processoTrf));
		}
		return listaRetorno;
	}
	
	/**
	 * Valida a regra quando o polo selecionado e indefinido.
	 * 
	 * @param impedimentoSuspeicao ImpedimentoSuspeicao impedimento a ser validado.
	 * @param processoTrf ProcessoTrf dados do processo.
	 * @return List<ImpedimentoSuspeicao> lista com os impedimento suspeicao enquadrados.
	 */
	private List<ImpedimentoSuspeicao> validarRegraPoloIndefinido(ImpedimentoSuspeicao impedimentoSuspeicao, ProcessoTrf processoTrf) {
		List<ImpedimentoSuspeicao> listaRetorno = new ArrayList<>();
		List<ProcessoParte> listaParte = processoTrf.getListaPartePoloObj(ProcessoParteParticipacaoEnum.A, ProcessoParteParticipacaoEnum.P, ProcessoParteParticipacaoEnum.T);
		if (isTipoPessoaFisica(impedimentoSuspeicao) || isTipoPessoaJuridica(impedimentoSuspeicao) || isTipoPessoaAutoridade(impedimentoSuspeicao)) {
			listaRetorno.addAll(validarRegraPorPartePessoa(impedimentoSuspeicao, listaParte));
		}
		return listaRetorno;
	}
	
	/**
	 * Valida a regra quando o polo selecionado e passivo.
	 * 
	 * @param impedimentoSuspeicao ImpedimentoSuspeicao impedimento a ser validado.
	 * @param processoTrf ProcessoTrf dados do processo.
	 * @return List<ImpedimentoSuspeicao> lista com os impedimento suspeicao enquadrados.
	 */
	private List<ImpedimentoSuspeicao> validarRegraPoloPassivo(ImpedimentoSuspeicao impedimentoSuspeicao, ProcessoTrf processoTrf) {
		List<ImpedimentoSuspeicao> listaRetorno = new ArrayList<>();
		List<ProcessoParte> listaPartePassivo = processoTrf.getListaPartePassivo();
		if (isTipoPessoaFisica(impedimentoSuspeicao) || isTipoPessoaJuridica(impedimentoSuspeicao) || isTipoPessoaAutoridade(impedimentoSuspeicao)) {
			listaRetorno.addAll(validarRegraPorPartePessoa(impedimentoSuspeicao, listaPartePassivo));
		}
		return listaRetorno;
	}
	
	/**
	 * Valida a regra quando o polo selecionado e ativo.
	 * 
	 * @param impedimentoSuspeicao ImpedimentoSuspeicao impedimento a ser validado.
	 * @param processoTrf ProcessoTrf dados do processo.
	 * @return List<ImpedimentoSuspeicao> lista com os impedimento suspeicao enquadrados.
	 */
	private List<ImpedimentoSuspeicao> validarRegraPoloAtivo(ImpedimentoSuspeicao impedimentoSuspeicao, ProcessoTrf processoTrf) {
		List<ImpedimentoSuspeicao> listaRetorno = new ArrayList<>();
		List<ProcessoParte> listaParteAtivo = processoTrf.getListaParteAtivo();
		if (isTipoPessoaFisica(impedimentoSuspeicao) || isTipoPessoaJuridica(impedimentoSuspeicao) || isTipoPessoaAutoridade(impedimentoSuspeicao)) {
			listaRetorno.addAll(validarRegraPorPartePessoa(impedimentoSuspeicao, listaParteAtivo));
		}
		return listaRetorno;
	}
	
	private boolean isTipoPessoaFisica(ImpedimentoSuspeicao impedimentoSuspeicao) {
		return impedimentoSuspeicao.getPessoaParteAdvogado() != null 
					&& impedimentoSuspeicao.getPessoaParteAdvogado().getInTipoPessoa() != null 
						&& TipoPessoaEnum.F.equals(impedimentoSuspeicao.getPessoaParteAdvogado().getInTipoPessoa());
	}
	
	private boolean isTipoPessoaJuridica(ImpedimentoSuspeicao impedimentoSuspeicao) {
		return impedimentoSuspeicao.getPessoaParteAdvogado() != null 
					&& impedimentoSuspeicao.getPessoaParteAdvogado().getInTipoPessoa() != null 
						&& TipoPessoaEnum.J.equals(impedimentoSuspeicao.getPessoaParteAdvogado().getInTipoPessoa());
	}
	
	private boolean isTipoPessoaAutoridade(ImpedimentoSuspeicao impedimentoSuspeicao) {
		return impedimentoSuspeicao.getPessoaParteAdvogado() != null 
					&& impedimentoSuspeicao.getPessoaParteAdvogado().getInTipoPessoa() != null 
						&& TipoPessoaEnum.A.equals(impedimentoSuspeicao.getPessoaParteAdvogado().getInTipoPessoa());
	}
	
	/**
     * Valida a regra por parte e pela pessoa.
     * 
     * @param impedimentoSuspeicao ImpedimentoSuspeicao impedimento/suspeicao a ser validado.
     * @return List<ImpedimentoSuspeicao> lista com os impedimentos/suspeicao enquadrados.
     */
	private List<ImpedimentoSuspeicao> validarRegraPorPartePessoa(ImpedimentoSuspeicao impedimentoSuspeicao, List<ProcessoParte> listaParte) {
		List<ImpedimentoSuspeicao> listaRetorno = new ArrayList<>();
		for (ProcessoParte processoParte : listaParte) {
			if (impedimentoSuspeicao.getPessoaParteAdvogado() != null 
					&&  impedimentoSuspeicao.getPessoaParteAdvogado().getIdPessoa().equals(processoParte.getPessoa().getIdPessoa()) && ProcessoParteSituacaoEnum.A.equals(processoParte.getInSituacao())
					&& !listaRetorno.contains(impedimentoSuspeicao)) {	
				listaRetorno.add(impedimentoSuspeicao);
			}
		}
		return listaRetorno;
	}
	
}