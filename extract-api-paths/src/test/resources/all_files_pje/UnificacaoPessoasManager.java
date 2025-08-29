package br.jus.cnj.pje.nucleo.manager;
 
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.manager.SessaoManager;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.UnificacaoPessoasDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.pje.nucleo.entidades.CaixaRepresentante;
import br.jus.pje.nucleo.entidades.CaracteristicaFisica;
import br.jus.pje.nucleo.entidades.DesunificacaoVO;
import br.jus.pje.nucleo.entidades.Lembrete;
import br.jus.pje.nucleo.entidades.LembretePermissao;
import br.jus.pje.nucleo.entidades.LogHistoricoMovimentacao;
import br.jus.pje.nucleo.entidades.MeioContato;
import br.jus.pje.nucleo.entidades.ModeloProclamacaoJulgamento;
import br.jus.pje.nucleo.entidades.NotaSessaoJulgamento;
import br.jus.pje.nucleo.entidades.Parametro;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoFavorito;
import br.jus.pje.nucleo.entidades.ProcessoParteHistorico;
import br.jus.pje.nucleo.entidades.ProcessoParteSigilo;
import br.jus.pje.nucleo.entidades.ProcessoSegredo;
import br.jus.pje.nucleo.entidades.ProcessoTag;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.entidades.ProcessoTrfRedistribuicao;
import br.jus.pje.nucleo.entidades.QuadroAviso;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoEnteExterno;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SolicitacaoNoDesvio;
import br.jus.pje.nucleo.entidades.UnificacaoPessoas;
import br.jus.pje.nucleo.entidades.UnificacaoPessoasObjeto;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.VisibilidadePessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.identidade.LogAcesso;
import br.jus.pje.nucleo.entidades.log.EntityLog;
 
 /**
  * 
  * @author luiz.mendes
  *
  */
 @Name(UnificacaoPessoasManager.NAME)
 public class UnificacaoPessoasManager extends BaseManager<UnificacaoPessoas>{
 
 	public static final String NAME = "unificacaoPessoasManager";
 	
 	@In
 	private UnificacaoPessoasDAO unificacaoPessoasDAO;
 	
 	@In
 	private UnificacaoManager unificacaoManager;
 	
 	@In
	private UsuarioService usuarioService;
 	
 	private LogAcessoManager logAcessoManager = ComponentUtil.getComponent("logAcessoManager");
 	private CaracteristicaFisicaManager caracteristicaFisicaManager = ComponentUtil.getComponent("caracteristicaFisicaManager");
 	private MeioContatoManager meioContatoManager = ComponentUtil.getComponent("meioContatoManager");
 	private PessoaNomeAlternativoManager nomeAlternativoManager = ComponentUtil.getComponent("pessoaNomeAlternativoManager");
 	private ProcessoSegredoManager processoSegredoManager = ComponentUtil.getComponent("processoSegredoManager");
 	private ProcessoParteSigiloManager processoParteSigiloManager = ComponentUtil.getComponent("processoParteSigiloManager");
 	private CaixaRepresentanteManager caixaRepresentanteManager = ComponentUtil.getComponent("caixaRepresentanteManager");
 	private SessaoEnteExternoManager sessaoEnteExternoManager = ComponentUtil.getComponent("sessaoEnteExternoManager");
 	private ProcessoTrfRedistribuicaoManager processoTrfRedistribuicaoManager = ComponentUtil.getComponent("processoTrfRedistribuicaoManager");
 	private ProcessoParteHistoricoManager processoParteHistoricoManager = ComponentUtil.getComponent("processoParteHistoricoManager");
 	private ProcessoTagManager processoTagManager = ComponentUtil.getComponent("processoTagManager");
 	private LembreteManager lembreteManager = ComponentUtil.getComponent("lembreteManager");
 	private ProcessoManager processoManager = ComponentUtil.getComponent("processoManager");
 	private ParametroManager parametroManager = ComponentUtil.getComponent("parametroManager");
 	private EntityLogManager entityLogManager = ComponentUtil.getComponent("entityLogManager");
 	private SolicitacaoNoDesvioManager solicitacaoNoDesvioManager = ComponentUtil.getComponent("solicitacaoNoDesvioManager");
 	private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent("sessaoPautaProcessoTrfManager");
 	private SessaoManager sessaoManager = ComponentUtil.getComponent("sessaoManager");
 	private QuadroAvisoManager quadroAvisoManager = ComponentUtil.getComponent("quadroAvisoManager");
 	private ProcessoDocumentoFavoritoManager processoDocumentoFavoritoManager = ComponentUtil.getComponent("processoDocumentoFavoritoManager");
 	private NotaSessaoJulgamentoManager notaSessaoJulgamentoManager = ComponentUtil.getComponent("notaSessaoJulgamentoManager");
 	private ModeloProclamacaoJulgamentoManager modeloProclamacaoJulgamentoManager = ComponentUtil.getComponent("modeloProclamacaoJulgamentoManager");
 	private LogHistoricoMovimentacaoManager logHistoricoMovimentacaoManager = ComponentUtil.getComponent("logHistoricoMovimentacaoManager");
 	private VisibilidadePessoaDocumentoIdentificacaoManager visibilidadePessoaDocumentoIdentificacaoManager = ComponentUtil.getComponent("visibilidadePessoaDocumentoIdentificacaoManager");
 	
 	@In
 	private LembretePermissaoManager lembretePermissaoManager;
 	
 	@In
	private ProcessoTrfConexaoManager processoTrfConexaoManager;
 	
 	@Override
 	protected BaseDAO<UnificacaoPessoas> getDAO() {
 		return unificacaoPessoasDAO;
 	}
 	
 	private static final Integer QTD_ELEMENTOS_ARRAY_LOGIN_UNIFICADO = 2;
 	private static final Integer ELEMENTO_ARRAY_LOGIN_UNIFICADO_VALIDO = 0;
 	
 	private DesunificacaoVO desunificacaoVO = null;
 
 	/**
 	 * metodo responsavel por encaminhar para alterao os objetos da desunificacao e persistir a alterao
 	 * @param desunificacaoVO 
 	 * @throws Exception
 	 */
 	@Transactional
 	public void finalizarDesunificacaoPessoas(DesunificacaoVO desunificacaoVO) throws Exception {
 		this.desunificacaoVO = desunificacaoVO;
 		alteraPessoaSecundaria();
 		alteraPessoaPrincipal();
 		alteraUnificacaoPessoas();
 		persistirAlteracoesObjetosUnificados();
 		alteraUnificacao();
 		unificacaoManager.persisteAlteracoesUnificacao(desunificacaoVO.getUnificacao());
 	}
 
 	/**
 	 * metodo auxiliar que verifica se a unificacao tem somente uma unificacao de pessoas.
 	 * caso afirmativo, seta a unificacao com o atributo ativo = false, pois na desunificacao de pessoas, nao existiro mais unificacoes que justifiquem
 	 * que a unificacao permanea ativa.
 	 */
 	private void alteraUnificacao() {
 		if(unificacaoPessoasDAO.recuperaUnificacoesPessoasPorUnificacao(desunificacaoVO.getUnificacao(), true).size() == 1) {
 			desunificacaoVO.getUnificacao().setAtivo(false);
 		}
 	}
 	
 	/**
 	 * altera as propriedades da unificacao de pessoas para a desunificacao.
 	 */
 	private void alteraUnificacaoPessoas() {
 		desunificacaoVO.getUnificacaoPessoa().setAtivo(false);
 		desunificacaoVO.getUnificacaoPessoa().setDataDesunificacao(new Date());
 		desunificacaoVO.getUnificacaoPessoa().setUsuarioDesunificador((Usuario) Contexts.getSessionContext().get("usuarioLogado"));		
 	}
 
 	/**
 	 * altera as propriedades da pessoa principal para a desunificacao.
 	 */
 	private void alteraPessoaPrincipal() {
 		desunificacaoVO.getUnificacao().getPessoaPrincipal().setAtivo(Boolean.TRUE);
 		desunificacaoVO.getUnificacao().getPessoaPrincipal().setBloqueio(Boolean.FALSE);
 		desunificacaoVO.getUnificacao().getPessoaPrincipal().setUnificada(Boolean.FALSE);
 		desunificacaoVO.getUnificacao().getPessoaPrincipal().setNome(desunificacaoVO.getUnificacao().getPessoaPrincipal().getNome().toUpperCase());
 	}
 
 	/**
 	 * altera as propriedades da pessoa secundria para a desunificacao.
 	 */
 	private void alteraPessoaSecundaria() {
 		desunificacaoVO.getPessoaSecundaria().setAtivo(Boolean.TRUE);
 		desunificacaoVO.getPessoaSecundaria().setBloqueio(Boolean.FALSE);
 		desunificacaoVO.getPessoaSecundaria().setUnificada(Boolean.FALSE);
 		desunificacaoVO.getPessoaSecundaria().setNome(desunificacaoVO.getPessoaSecundaria().getNome().toUpperCase());
 		
 		String login = limparLoginParaDesunificacao(desunificacaoVO.getPessoaSecundaria().getLogin());
 		if(usuarioService.findByLogin(login) == null) {
 			desunificacaoVO.getPessoaSecundaria().setLogin(login); 			
 		}else {
 			desunificacaoVO.getPessoaSecundaria().setLogin(login+"--Desunificada em "+new Date()); 			
 		}
 	}
 	
 	/**
 	 * metodo que separa o login dos dados da unificacao de pessoas.
 	 * @param login
 	 * @return login sem dados da unificacao
 	 */
 	private String limparLoginParaDesunificacao(String loginUnificado) {
 		String array[] = new String[QTD_ELEMENTOS_ARRAY_LOGIN_UNIFICADO];
 		array = loginUnificado.split("--");
 		return array[ELEMENTO_ARRAY_LOGIN_UNIFICADO_VALIDO];
 	}
 	
 	/**
 	 * metodo responsavel por encaminhar para persistencia os objetos alterados pela desunificacao.
 	 * @throws Exception 
 	 */
 	private void persistirAlteracoesObjetosUnificados() throws Exception {
		alteracoesLogAcesso();
		alteracoesCaractFis();
		alteracoesMeiosContatoCadastrados();
		alteracoesMeiosContatoProprietarias();
		alteracoesNomesAlternativosCadastrados();
		alteracoesNomesAlternativosProprietarias();
		alteracoesConexoesProcessos();
		alteracoesSegredosProcessos();
		alteracoesSigiloProcessosParte();
		alteracoesCaixasRepresentantes();
		alteracoesSessoesEnteExternos();
		alteracoesRedistribuicoesProcessos();
		alteracoesProcessosParteHistoricos();
		alteracoesProcessosTags();
		alteracoesLembretes();
		alteracoesPermissoesLembretes();
		alteracoesProcessosProtocolados();
		alteracoesParametrosInseridosModificados();
		alteracoesEntityLogsModificados();
		alteracoesSolicitacaoNoDesvioModificados();
		alteracoesSessaoPautaProcessosInclusora();
		alteracoesSessaoPautaProcessosExclusora();
		alteracoesSessaoInclusora();
		alteracoesSessaoExclusora();
		alteracoesAvisosQuadroAvisos();
		alteracoesProcessosDocumentosFavoritos();
		alteracoesNotasSessoesJulgamento();
		alteracoesModelosProclamacaoJulgamento();
		alteracoesLogHistoricoMovimentacao();
		alteracoesVisibilidadesDocumentosIdentificacao();
 	}

	private boolean houveAlteracaoMeioContato(UnificacaoPessoasObjeto _objetoUnificado, MeioContato _objeto) {
		return (!_objeto.getValorMeioContato().equals(_objetoUnificado.getInformacao()));
	}
	
	private boolean houveAlteracaoNomeAlternativo(UnificacaoPessoasObjeto _objetoUnificado,PessoaNomeAlternativo _objeto) {
		return (!_objeto.getPessoaNomeAlternativo().equals(_objetoUnificado.getInformacao()));
	}
	
	/**
	 * metodo responsavel por verificar se houve alteracoes no parametro, desde que foi unificado.
	 * logica:
	 * -> se o usuario modificador ainda for a pessoa principal da unificacao e;
	 * --> se o valor do parametro ainda for igual ao valor salvo no campo informacao
	 * logo, nao houve alteracao.
	 * 
	 * @param _objetoUnificado
	 * @param _objeto
	 * @return true/false
	 */
	private boolean houveAlteracaoParametro(UnificacaoPessoasObjeto _objetoUnificado, Parametro _objeto) {
		boolean retorno = true;
		if(_objeto.getUsuarioModificacao().getIdUsuario().equals(_objetoUnificado.getPessoaPrincipal().getIdPessoa())) {
			if(_objeto.getValorVariavel().equals(_objetoUnificado.getInformacao())) {
				retorno = false;
			}
		}
		return retorno;
	}

	/**
 	 * metodo responsavel por encaminhar para persistencia as alteracoes feitas no LogAcesso, na desunificacao.
 	 * @throws NumberFormatException
 	 * @throws PJeBusinessException
 	 */
	private void alteracoesLogAcesso() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getLogAcessoUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				LogAcesso _objeto = logAcessoManager.findById(Long.parseLong(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioLogin(desunificacaoVO.getPessoaSecundaria());
				} else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesConexoesProcessos() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getConexoesPrevencaoUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				ProcessoTrfConexao _objeto = processoTrfConexaoManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setPessoaFisica(desunificacaoVO.getPessoaFisicaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
					if(!desunificacaoVO.getConexaoProcessoUnificadosNaoEncontradosObject().contains(_objetoUnificado)){
						desunificacaoVO.getConexaoProcessoUnificadosNaoEncontradosObject().add(_objetoUnificado);
					}
				}
			}
		}
	}
	
	private void alteracoesSegredosProcessos() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getSegredosProcessosUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				ProcessoSegredo _objeto = processoSegredoManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioLogin(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
					if(!desunificacaoVO.getSegredosProcessosUnificadosNaoEncontradosObject().contains(_objetoUnificado)){
						desunificacaoVO.getSegredosProcessosUnificadosNaoEncontradosObject().add(_objetoUnificado);
					}
				}
			}
		}
	}
	
	private void alteracoesSigiloProcessosParte() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getSigiloProcessosParteUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				ProcessoParteSigilo _objeto = processoParteSigiloManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioCadastro(desunificacaoVO.getPessoaFisicaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
					if(!desunificacaoVO.getSigiloProcessosParteUnificadosNaoEncontradosObject().contains(_objetoUnificado)){
						desunificacaoVO.getSigiloProcessosParteUnificadosNaoEncontradosObject().add(_objetoUnificado);
					}
				}
			}
		}
	}
	
	private void alteracoesCaixasRepresentantes() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getCaixaRepresentanteUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				CaixaRepresentante _objeto = caixaRepresentanteManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setRepresentante(desunificacaoVO.getPessoaFisicaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
					if(!desunificacaoVO.getCaixaRepresentanteUnificadosNaoEncontradosObject().contains(_objetoUnificado)){
						desunificacaoVO.getCaixaRepresentanteUnificadosNaoEncontradosObject().add(_objetoUnificado);
					}
				}
			}
		}
	}
	
	private void alteracoesSessoesEnteExternos() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getSessaoEnteExternoUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				SessaoEnteExterno _objeto = sessaoEnteExternoManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setPessoaAcompanhaSessao(desunificacaoVO.getPessoaPrincipal());
					_objeto.setNomePessoa(desunificacaoVO.getPessoaPrincipal().getNome());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesRedistribuicoesProcessos() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getRedistribuicaoProcessosUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				ProcessoTrfRedistribuicao _objeto = processoTrfRedistribuicaoManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuario(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesProcessosParteHistoricos() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getProcessoParteHistoricosUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				ProcessoParteHistorico _objeto = processoParteHistoricoManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioLogin(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesProcessosTags() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getProcessosTagsUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				ProcessoTag _objeto = processoTagManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setIdUsuarioInclusao(desunificacaoVO.getPessoaSecundaria().getIdPessoa());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesLembretes() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getLembretesUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				Lembrete _objeto = lembreteManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioLocalizacao(desunificacaoVO.getUsuarioPessoaSecundaria().getUsuarioLocalizacaoInicial());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesPermissoesLembretes() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getPermissaoLembretesUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				LembretePermissao _objeto = lembretePermissaoManager.recuperaLembretePermissao(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuario(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesProcessosProtocolados() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getProcessosUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				Processo _objeto = processoManager.recuperaProcesso(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioCadastroProcesso(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesParametrosInseridosModificados() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getParametrosUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				Parametro _objeto = parametroManager.recuperaParametro(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null && !houveAlteracaoParametro(_objetoUnificado, _objeto)) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioModificacao(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesEntityLogsModificados() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getEntityLogsUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				EntityLog _objeto = entityLogManager.recuperaEntityLog(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setIdUsuario(desunificacaoVO.getPessoaSecundaria().getIdPessoa());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesSolicitacaoNoDesvioModificados() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getSolicitacoesNoDesvioUnificadasObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				SolicitacaoNoDesvio _objeto = solicitacaoNoDesvioManager.recuperaSolicitacaoNoDesvio(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuario(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesSessaoPautaProcessosInclusora() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getSessaoPautaProcessoInclusoraUnificadasObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				SessaoPautaProcessoTrf _objeto = sessaoPautaProcessoTrfManager.recuperaSessaoPautaProcessoTrf(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioInclusao(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesSessaoPautaProcessosExclusora() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getSessaoPautaProcessoExclusoraUnificadasObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				SessaoPautaProcessoTrf _objeto = sessaoPautaProcessoTrfManager.recuperaSessaoPautaProcessoTrf(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioExclusao(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesSessaoInclusora() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getSessaoInclusoraUnificadasObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				Sessao _objeto = sessaoManager.recuperarPorId(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioInclusao(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesSessaoExclusora() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getSessaoExclusoraUnificadasObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				Sessao _objeto = sessaoManager.recuperarPorId(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioExclusao(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesAvisosQuadroAvisos() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getAvisoQuadroAvisoUnificadasObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				QuadroAviso _objeto = quadroAvisoManager.recuperarPorId(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioInclusao(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesProcessosDocumentosFavoritos() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getProcessosDocumentoFavoritosUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				ProcessoDocumentoFavorito _objeto = processoDocumentoFavoritoManager.recuperarPorId(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuario(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesNotasSessoesJulgamento() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getNotasSessaoJulgamentoUnificadasObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				NotaSessaoJulgamento _objeto = notaSessaoJulgamentoManager.recuperarPorId(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioCadastro(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesModelosProclamacaoJulgamento() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getModelosProclamacaoJulgamentoUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				ModeloProclamacaoJulgamento _objeto = modeloProclamacaoJulgamentoManager.recuperarPorId(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuario(desunificacaoVO.getUsuarioPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesLogHistoricoMovimentacao() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getLogsHistoricoMovimentacaoUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				LogHistoricoMovimentacao _objeto = logHistoricoMovimentacaoManager.recuperarPorId(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuario(desunificacaoVO.getUsuarioLoginPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	private void alteracoesVisibilidadesDocumentosIdentificacao() {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getVisibilidadesDocIdentificacaoUnificadasObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				VisibilidadePessoaDocumentoIdentificacao _objeto = visibilidadePessoaDocumentoIdentificacaoManager.recuperarPorId(Long.parseLong(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setPessoa(desunificacaoVO.getPessoaSecundaria());
				}else {
					_objetoUnificado.setEncontradoDesunificacao(false);
				}
			}
		}
	}
	
	/**
	 * metodo responsavel por encaminhar para persistencia as alteracoes feitas na caracteristica fisica.
	 * caso a mesma tenha sido removida do sistema antes da unificacao, é inserida na lista de CaractFisUnificadasNaoEncontradasObject
	 * caso a mesma continue no sistema, é inserida na lista de caractFisUnificadas
	 * @throws NumberFormatException
	 * @throws PJeBusinessException
	 */
	private void alteracoesCaractFis() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getCaractFisUnificadasObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				CaracteristicaFisica _objeto = caracteristicaFisicaManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setPessoaFisica(desunificacaoVO.getPessoaFisicaSecundaria());
					if(!desunificacaoVO.getCaractFisUnificadas().contains(_objeto)) {
						desunificacaoVO.getCaractFisUnificadas().add(_objeto);
					}
				} else {
					_objetoUnificado.setEncontradoDesunificacao(false);
					if(!desunificacaoVO.getCaractFisUnificadasNaoEncontradasObject().contains(_objetoUnificado)){
						desunificacaoVO.getCaractFisUnificadasNaoEncontradasObject().add(_objetoUnificado);
					}
				}
			}
		}
	}
	
	private void alteracoesMeiosContatoCadastrados() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getMeiosContatoCadastradosUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				MeioContato _objeto = meioContatoManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null && !houveAlteracaoMeioContato(_objetoUnificado, _objeto)) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioCadastrador(desunificacaoVO.getUsuarioPessoaSecundaria());
				} else {
					_objetoUnificado.setEncontradoDesunificacao(false);
					if(!desunificacaoVO.getMeioContatoCadastradoUnificadoNaoEncontradoObject().contains(_objetoUnificado)){
						desunificacaoVO.getMeioContatoCadastradoUnificadoNaoEncontradoObject().add(_objetoUnificado);
					}
				}
			}
		}
	}
	
	private void alteracoesNomesAlternativosCadastrados() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getNomesAlternativosCadastradosUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				PessoaNomeAlternativo _objeto = nomeAlternativoManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null && !houveAlteracaoNomeAlternativo(_objetoUnificado, _objeto)) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setUsuarioCadastrador(desunificacaoVO.getUsuarioPessoaSecundaria());
				} else {
					_objetoUnificado.setEncontradoDesunificacao(false);
					if(!desunificacaoVO.getNomesAlternativosCadastradosUnificadosAlteradosOuNaoEncontradosObject().contains(_objetoUnificado)){
						desunificacaoVO.getNomesAlternativosCadastradosUnificadosAlteradosOuNaoEncontradosObject().add(_objetoUnificado);
					}
				}
			}
		}
	}

	private void alteracoesMeiosContatoProprietarias() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getMeiosContatoProprietariasUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				MeioContato _objeto = meioContatoManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null && !houveAlteracaoMeioContato(_objetoUnificado, _objeto)) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setPessoa(desunificacaoVO.getPessoaSecundaria());
				} else {
					_objetoUnificado.setEncontradoDesunificacao(false);
					if(!desunificacaoVO.getMeiosContatoProprietariasUnificadosAlteradosOuNaoEncontradasObject().contains(_objetoUnificado)){
						desunificacaoVO.getMeiosContatoProprietariasUnificadosAlteradosOuNaoEncontradasObject().add(_objetoUnificado);
					}
				}
			}
		}
	}
	
	private void alteracoesNomesAlternativosProprietarias() throws NumberFormatException, PJeBusinessException {
		List<UnificacaoPessoasObjeto> listaObjetos = desunificacaoVO.getNomesAlternativosProprietariasUnificadosObject();
		if(listaObjetos != null && listaObjetos.size() > 0) {
			for (UnificacaoPessoasObjeto _objetoUnificado : listaObjetos) {
				_objetoUnificado.setAtivo(Boolean.FALSE);
				
				PessoaNomeAlternativo _objeto = nomeAlternativoManager.findById(Integer.parseInt(_objetoUnificado.getId_objeto_unificado()));
				if(_objeto != null && !houveAlteracaoNomeAlternativo(_objetoUnificado, _objeto)) {
					_objetoUnificado.setEncontradoDesunificacao(true);
					_objeto.setPessoa(desunificacaoVO.getPessoaSecundaria());
				} else {
					_objetoUnificado.setEncontradoDesunificacao(false);
					if(!desunificacaoVO.getNomesAlternativosProprietariasUnificadosAlteradosOuNaoEncontradasObject().contains(_objetoUnificado)){
						desunificacaoVO.getNomesAlternativosProprietariasUnificadosAlteradosOuNaoEncontradasObject().add(_objetoUnificado);
					}
				}
			}
		}
	}
 }