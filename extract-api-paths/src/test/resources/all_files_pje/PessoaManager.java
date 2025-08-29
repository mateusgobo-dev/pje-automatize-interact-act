package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.cliente.home.RamoAtividadeHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.PessoaDAO;
import br.jus.cnj.pje.business.dao.UsuarioLoginDAO;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.RamoAtividade;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.util.ArrayUtil;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoa;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaJuridica;

/**
 * Componente de tratamento negocial de {@link Pessoa}, a ser utilizado exclusivamente para
 * os casos em que a entidade invocante não teria condições de distinguir entre os três tipos de pessoa
 * do sistema ({@link PessoaFisica}, {@link PessoaJuridica} ou {@link PessoaAutoridade}).
 * 
 * @author cristof
 *
 */
@Name(PessoaManager.NAME)
public class PessoaManager extends AbstractUsuarioManager<Pessoa, PessoaDAO> {

	public static final String NAME = "pessoaManager";
	
	public static final String USUARIO_LOGADO = "usuarioLogado";
	
	@In
	private PessoaDAO pessoaDAO;

	@In
	private UsuarioLoginDAO usuarioLoginDAO;
	
	@In
	private UsuarioManager usuarioManager;

  	@In
  	private PessoaFisicaManager pessoaFisicaManager;
  	
  	@In
  	private PessoaJuridicaManager pessoaJuridicaManager;
  	
  	@In
  	private ProcuradoriaManager procuradoriaManager;
  	
	@Override
	protected PessoaDAO getDAO() {
		return pessoaDAO;
	}
	
	public static PessoaManager instance() {
		return ComponentUtil.getComponent(PessoaManager.class);
	}

	/**
	 * Este método recebe como argumento uma pessoa e valida se é uma pessoa com
	 * a situação <i>ativa</i> e se possui com certificado cadastrado no sistema.</li>
	 * 
	 * @param pessoa
	 *            pessoa a ser validada.
	 * @return <code>True</code> caso seja uma pessoa certificada ou
	 *         <code>False</code> caso não seja.
	 * @author Joao Paulo Lacerda
	 * @author cristof
	 */
	public boolean isPessoaCertificada(Pessoa pessoa) {
		return pessoa.getAtivo() && pessoaDAO.isCertificada(pessoa);
	}

	/**
	 * Este método recebe como argumento uma pessoa e valida se é uma pessoa
	 * Física ou Jurídica com a situação <i>Ativa</i> e se possui certificado
	 * cadastrado no sistema.</li>
	 * 
	 * @author Joao Paulo Lacerda
	 * @param pessoa
	 *            pessoa a ser validada.
	 * @return <code>True</code> caso seja uma pessoa fisica ou jurídica
	 *         certificada ou <code>False</code> caso não seja.
	 */
	public boolean isPessoaFisicaOuJuridicaCertificada(Pessoa pessoa) {
		return isPessoaFisicaCertificada(pessoa) || isPessoaJuridicaCertificada(pessoa);
	}

	/**
	 * Este método recebe como argumento uma pessoa e valida se é uma pessoa
	 * Física com a situação <i>Ativa</i> e se possui certificado cadastrado no
	 * sistema.</li>
	 * 
	 * @author Joao Paulo Lacerda
	 * @param pessoa
	 *            pessoa a ser validada.
	 * @return <code>True</code> caso seja uma pessoa fisica certificada ou
	 *         <code>False</code> caso não seja.
	 */
	public boolean isPessoaFisicaCertificada(Pessoa pessoa) {
		return pessoa.getInTipoPessoa() == TipoPessoaEnum.F ? isPessoaCertificada(pessoa) : false;
	}

	/**
	 * Este método recebe como argumento uma pessoa e valida se é uma pessoa
	 * Jurídica com a situação <i>Ativa</i> e se possui com certificado
	 * cadastrado no sistema.</li>
	 * 
	 * @author Joao Paulo Lacerda
	 * @param pessoa
	 *            pessoa a ser validada.
	 * @return <code>True</code> caso seja uma pessoa jurídica certificada ou
	 *         <code>False</code> caso não seja.
	 */
	public boolean isPessoaJuridicaCertificada(Pessoa pessoa) {
		return pessoa.getInTipoPessoa() == TipoPessoaEnum.J ? isPessoaCertificada(pessoa) : false;
	}

	/**
	 * Atalho para o pessoa logada
	 * 
	 * @return pessoa logada
	 */
	@Deprecated
	public Pessoa getPessoaLogada() {
		Pessoa pessoa = (Pessoa) Contexts.getSessionContext().get(USUARIO_LOGADO);
		if (pessoa != null) {
			pessoa = pessoaDAO.find(pessoa.getIdUsuario());
		}
		return pessoa;
	}
	
	public List<Pessoa> findByTypeANDDocument(String typeId, String documentId){
		return pessoaDAO.findByTypeANDDocument(typeId, documentId);
	}

	public List<Pessoa> findByDocument(String codigoDocumento, String...tipoDocumento){
		return pessoaDAO.findByDocument(codigoDocumento, tipoDocumento, true);
	}
	
	/**
	 * Recupera a lista de pessoas que têm o nome dado.
	 * 
	 * @param name o nome a ser pesquisado.
	 * @return a lista de pessoas que têm o nome dado.
	 * @see PessoaDAO#findByName(String, int, int)
	 */
	public List<Pessoa> findByName(String name) {
		return pessoaDAO.findByName(name);
	}
	
	public Pessoa findById(int id){
		return pessoaDAO.find(id);
	}
	
	/**
 	 * Recupera as pessoas cujo nome seja o nome dado, seja ele o nome principal cadastrado, 
 	 * seja ele qualquer dos nomes constantes nos documentos da pessoa ou que figurem como
 	 * seus nomes alternativos.
 	 * 
 	 * @param name o nome a ser pesquisado
 	 * @param firstRow o primeiro a registro a ser recuperado.
 	 * @param maxLength o máximo de registros a serem recuperados.
 	 * @return a lista de pessoas que tenham o nome dado.
 	 */
 	public List<Pessoa> findByName(String nome, int firstRow, int maxLength) {
 		return pessoaDAO.findByName(nome, firstRow, maxLength);
 	}

	public List<Pessoa> pesquisarPessoasSemMandados(Integer idProcessoTrf, String nome, String cpf) {
		return pessoaDAO.pesquisarPessoasSemMandados(idProcessoTrf, nome, cpf);
	}

	/**
	 * Recupera o conjunto de procuradorias que representam a pessoa dada.
	 * 
	 * @param p a pessoa a ser pesquisada.
	 * @return a lista de procuradorias que representam a pessoa
	 */
	public List<Procuradoria> getOrgaosRepresentantes(Pessoa pessoa) {
		Search s = new Search(PessoaProcuradoriaEntidade.class);
		addCriteria(s,
				Criteria.equals("procuradoria.ativo", true),
				Criteria.not(Criteria.empty("procuradoria.pessoaProcuradoriaList")),
				Criteria.equals("pessoa", pessoa));
		s.setRetrieveField("procuradoria");
		return list(s);
	}

	/**
	 * Consulta a pessoa pelo cpf ou cnpj informado.
	 * 
	 * @param cpfCnpj CPF ou CNPJ.
	 * @return Pessoa
	 */
	public Pessoa findByCPFouCNPJ(String cpfCnpj) {
		Pessoa resultado = null;
		
		if (StringUtils.isNotBlank(cpfCnpj)) {
			resultado = pessoaDAO.findByCPFouCNPJ(cpfCnpj);
		}
		return resultado;
	}

	/**
	 * Este método encontra-se descontinuado.
	 * Favor utilizar {@link AtoComunicacaoService#verificarCadastroPessoa(Pessoa)} ou 
	 * {@link AtoComunicacaoService#verificarCadastroPessoa(Pessoa, Integer) 
	 */
	@Deprecated
	public boolean aptoIntimacaoEletronica(Pessoa p) {
		Search s = new Search(UsuarioLogin.class);
		addCriteria(s, 
				Criteria.equals("idUsuario", p.getIdPessoa()),
				Criteria.equals("ativo", true), 
				Criteria.not(Criteria.isNull("certChain")));
		return count(s) > 0;
	}

	/**
	 * Indica se uma determinada pessoa está representada por alguma procuradoria.
	 * 
	 * @param p a pessoa possivelmente representada
	 * @return true, se houver pelo menos uma procuradoria com procuradores cadastrados
	 * que representem a pessoa 
	 */
	public boolean temRepresentantes(Pessoa pessoa) {
		Search s = new Search(PessoaProcuradoriaEntidade.class);
		addCriteria(s,
				Criteria.equals("procuradoria.ativo", true),
				Criteria.not(Criteria.empty("procuradoria.pessoaProcuradoriaList")),
				Criteria.equals("pessoa", pessoa));
		return count(s) > 0;
	}
	
	@Override
	public Pessoa persist(Pessoa entity) throws PJeBusinessException {
		if (entity.getIdPessoa() == null || entity.getLogin() == null){
			if (entity.getDocumentoCpfCnpj() == null){
				entity.setLogin(UUID.randomUUID().toString());
			}else{
				entity.setLogin(InscricaoMFUtil.retiraMascara(entity.getDocumentoCpfCnpj()));
			}
		}
		
		return super.persist(entity);
	}
	
	/**
	 * Método responsável por recuperar a lista de pessoas que têm um nome e documento identificador especificado
	 * 
	 * @param nome Nome a ser pesquisado
	 * @param tipoDocumentoIdentificacao Tipo de documento de identificação
	 * @param documentoIdentificacao Texto do documento a ser pesquisado
	 * @return Lista de pessoas
	 */
	public List<Pessoa> findByNomeAndDocumentoIdentificacao(String nome, String tipoDocumentoIdentificacao, String documentoIdentificacao) {
		return pessoaDAO.findByNomeAndDocumentoIdentificacao(nome, tipoDocumentoIdentificacao, documentoIdentificacao);
	}

	/**
	 * Remove o objeto UsuarioLogin se ele existir de forma independente no sistema, ou seja, existe 
	 * UsuarioLogin porém não existe Pessoa.
	 */
	public void removerRegistroIncompleto(Pessoa pessoa) {
		UsuarioLogin login = obterUsuarioLoginPeloLogin(pessoa);
		if (pessoa != null && login != null && !isExistePessoa(pessoa)) {
			
			usuarioLoginDAO.remove(login);
			usuarioLoginDAO.flush();
		}
	}	
	
	/**
	 * Retorna o UsuarioLogin do 'login' passado por parâmetro.
	 * 
	 * @param pessoa Pessoa com o atributo 'login' preenchido.
	 * @return UsuarioLogin
	 */
	public UsuarioLogin obterUsuarioLoginPeloLogin(Pessoa pessoa) {
		UsuarioLogin resultado = null;
		
		if (pessoa != null && (
				StringUtils.isNotBlank(pessoa.getLogin()) || 
				StringUtils.isNotBlank(pessoa.getDocumentoCpfCnpj()))) {
			List<String> logins = new ArrayList<String>();
			logins.add(pessoa.getLogin());
			logins.add(pessoa.getDocumentoCpfCnpj());
			
			String login = (String) ArrayUtil.firstNonNull(logins.toArray());
			resultado = usuarioLoginDAO.findByLogin(InscricaoMFUtil.retiraMascara(login));
			
		}
		return resultado;
	}
	
	/**
	 * Retorna true se existir a pessoa com o login (pessoa.login) passado por parâmetro.
	 * 
	 * @param pessoa Pessoa com o atributo 'login' preenchido.
	 * @return true se existir a pessoa com o login (pessoa.login) passado por parâmetro.
	 */
	public Boolean isExistePessoa(Pessoa pessoa) {
		Boolean resultado = Boolean.FALSE;
		
		if (pessoa != null && StringUtils.isNotBlank(pessoa.getLogin())) {
			String login = InscricaoMFUtil.retiraMascara(pessoa.getLogin());
			resultado = (pessoaDAO.findByLogin(login) != null);
			
		}
		return resultado;
	}
	  
 	/**
 	 * Método responsável por criar a {@link PessoaFisica} ou
 	 * {@link PessoaJuridica}.
 	 * 
 	 * @param dadosReceita
 	 *            os dados do usuário de acordo com a Receita Federal
 	 * @return <code>Pessoa</code>, a pessoa criada
 	 * @throws PJeBusinessException
 	 */
  	public Pessoa criarPessoaPelaReceita(DadosReceitaPessoa dadosReceita) throws PJeBusinessException {		
  		if (dadosReceita instanceof DadosReceitaPessoaFisica) {
  			PessoaFisica pessoaFisica = new PessoaFisica();
  			pessoaFisica.setNome(((DadosReceitaPessoaFisica) dadosReceita).getNome());
  			pessoaFisica.setDataNascimento(((DadosReceitaPessoaFisica) dadosReceita).getDataNascimento());
  			pessoaFisica.setSexo(((DadosReceitaPessoaFisica) dadosReceita).getSexo().equals("1") ? SexoEnum.M : SexoEnum.F);
  			pessoaFisica.setNomeGenitora(((DadosReceitaPessoaFisica) dadosReceita).getNomeMae());
  			pessoaFisica.setNumeroCPF(InscricaoMFUtil.mascararCpf((((DadosReceitaPessoaFisica) dadosReceita).getNumCPF())));
  			pessoaFisica.setNumeroTituloEleitor(((DadosReceitaPessoaFisica) dadosReceita).getNumTituloEleitor());
  			pessoaFisica.setTipoPessoa(ParametroUtil.instance().getTipoPessoaFisica());
  			pessoaFisica.setLogin(InscricaoMFUtil.retiraMascara(((DadosReceitaPessoaFisica) dadosReceita).getNumCPF()));
  			usuarioManager.preencherEndereco(dadosReceita, pessoaFisica);
  			
  			logger.debug("Persistindo pessoa física");
  			pessoaFisicaManager.persistAndFlush(pessoaFisica);
  			
  			return pessoaFisica;
  			
  		} else if (dadosReceita instanceof DadosReceitaPessoaJuridica) {
  			PessoaJuridica pessoaJuridica = new PessoaJuridica();
  			pessoaJuridica.setDataAbertura(((DadosReceitaPessoaJuridica) dadosReceita).getDataRegistro());
  			pessoaJuridica.setNomeFantasia(((DadosReceitaPessoaJuridica) dadosReceita).getNomeFantasia());
  			pessoaJuridica.setNumeroCNPJ(((DadosReceitaPessoaJuridica) dadosReceita).getNumCNPJ());
  			pessoaJuridica.setNumeroCpfResponsavel(((DadosReceitaPessoaJuridica) dadosReceita).getNumCpfResponsavel());
  			pessoaJuridica.setNomeResponsavel(((DadosReceitaPessoaJuridica) dadosReceita).getNomeResponsavel());
  			pessoaJuridica.setTipoPessoa(ParametroUtil.instance().getTipoPessoaJuridica());
  			
  			if(StringUtil.isNotEmpty(((DadosReceitaPessoaJuridica) dadosReceita).getNomeFantasia())){
  				pessoaJuridica.setNome(((DadosReceitaPessoaJuridica) dadosReceita).getNomeFantasia());  				
  			} else {
  				pessoaJuridica.setNome(((DadosReceitaPessoaJuridica) dadosReceita).getRazaoSocial());
  			}
  			
  			pessoaJuridica.setLogin(InscricaoMFUtil.retiraMascara(((DadosReceitaPessoaJuridica) dadosReceita).getNumCNPJ()));
  			usuarioManager.preencherEndereco(dadosReceita, pessoaJuridica);
  			RamoAtividade ramoAtividade = RamoAtividadeHome.instance().buscarPorCodigo(((DadosReceitaPessoaJuridica) dadosReceita).getCodigoCnaeFiscal());
  			pessoaJuridica.setRamoAtividade(ramoAtividade);
  			
  			logger.debug("Persistindo pessoa jurídica");
  			pessoaJuridicaManager.persistAndFlush(pessoaJuridica);
  			
  			return pessoaJuridica;
  		}
  		return null;
  	}

	public Pessoa getFiscalLei() {
		return this.getFiscalLei(null);
	}

	public Pessoa getFiscalLei(Jurisdicao jurisdicao) {
		Pessoa retorno = null;
		List<Pessoa> fiscaisLei = ParametroUtil.instance().getFiscaisDaLei();
		
		if (fiscaisLei.size() == 1) {
			retorno = fiscaisLei.get(0);
		} else {
			retorno = this.recuperarFiscalLei(fiscaisLei, jurisdicao);
		}
		return retorno;
	}

	/**
	 * 
	 * 
	 * @param fiscaisLei
	 * @param jurisdicao
	 * @return
	 */
	private Pessoa recuperarFiscalLei(List<Pessoa> fiscaisLei, Jurisdicao jurisdicao) {
		Pessoa result = null;

		if (jurisdicao != null) {
			Estado estadoJurisdicao = jurisdicao.getEstado();
			List<Procuradoria> procuradorias = null;

			for (Pessoa fiscalLei: fiscaisLei) {
				procuradorias = this.procuradoriaManager.getlistProcuradorias(fiscalLei);
				if (!procuradorias.isEmpty()) {
					for (Procuradoria procuradoria : procuradorias) {
						Endereco endereco = procuradoria.getLocalizacao().getEndereco();
						if (endereco != null && endereco.getCep() != null && endereco.getCep().getMunicipio() != null
								&& endereco.getCep().getMunicipio().getEstado().equals(estadoJurisdicao)) {

							result = fiscalLei;
							break;
						}
					}
				}
			}
		}

		return result;
	}
	
	/**
	 * @param pessoa Pessoa.
	 * @return True se a pessoa for Física.
	 */
	public boolean isPessoaFisica(Pessoa pessoa) {
		return ((pessoa instanceof PessoaFisica) && TipoPessoaEnum.F.equals(pessoa.getInTipoPessoa()));
	}
	
	/**
	 * @param pessoa Pessoa.
	 * @return True se a pessoa for Jurídica.
	 */
	public boolean isPessoaJuridica(Pessoa pessoa) {
		return ((pessoa instanceof PessoaJuridica) && TipoPessoaEnum.J.equals(pessoa.getInTipoPessoa()));
	}
	
	/**
	 * @param pessoa Pessoa.
	 * @return True se a pessoa for Autoridade.
	 */
	public boolean isPessoaAutoridade(Pessoa pessoa) {
		return ((pessoa instanceof PessoaAutoridade) && TipoPessoaEnum.A.equals(pessoa.getInTipoPessoa()));
	}
}