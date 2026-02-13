package br.jus.cnj.pje.nucleo.service;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.webservice.client.ConsultaClienteReceitaPJCNJ;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaJuridica;

/**
 * @author cristof
 * 
 */
@Name(PessoaJuridicaService.NAME)
public class PessoaJuridicaService{

	public static final String NAME = "pessoaJuridicaService";

	@In(create=true)
	private ConsultaClienteReceitaPJCNJ consultaClienteReceitaPJCNJ;

	@In
	private PessoaJuridicaManager pessoaJuridicaManager;

	@In
	private CepService cepService;

	public Pessoa findByCNPJ(String cnpj) throws PJeBusinessException{
		Usuario usuario = Authenticator.getUsuarioLogado();
		String cpf = null;
		if(usuario instanceof PessoaFisica){
			cpf = ((PessoaFisica)usuario).getNumeroCPF();
		}
		if(usuario instanceof PessoaJuridica){
			cpf = ((PessoaJuridica)usuario).getNumeroCpfResponsavel();
		}
		if(cpf == null){
			return null;
		}
		return this.findByCNPJ(cnpj, cpf, true);
	}

	/**
	 * Consulta a pessoa jurídica pelo CNPJ, o CNPJ da pessoa jurídica precisa ser o documento
	 * principal, ativo e não ter sido usado falsamente.
	 * 
	 * @param cnpj CNPJ
	 * @return PessoaJuridica
	 */
	public Pessoa findByCNPJ(String cnpj, String inscricaoConsulente, boolean exigeReceita) throws PJeBusinessException {
		PessoaJuridica resultado = null;
		validarCNPJ(cnpj);
		resultado = pessoaJuridicaManager.findByCNPJ(cnpj);
				
		if (resultado == null) {
			resultado = novaPessoaJuridicaComDadosDaReceita(cnpj, inscricaoConsulente, exigeReceita);
		}

		return resultado;
	}

	/**
	 * Consulta a pessoa jurídica pelo CNPJ, a consulta não leva em consideração se o documento 
	 * consultado é o principal ou não.
	 * 
	 * @param cnpj CNPJ
	 * @return PessoaJuridica
	 */
	public Pessoa findByDocumentoCNPJ(String cnpj, String inscricaoConsulente, boolean exigeReceita) throws PJeBusinessException {
		PessoaJuridica resultado = null;
		validarCNPJ(cnpj);
		resultado = pessoaJuridicaManager.findByDocumentoCNPJ(cnpj);
		
		if (resultado == null) {
			resultado = novaPessoaJuridicaComDadosDaReceita(cnpj, inscricaoConsulente, exigeReceita);
		}

		return resultado;
	}

	public PessoaJuridica persist(PessoaJuridica pessoaJuridica) throws PJeBusinessException{
		pessoaJuridicaManager.persistAndFlush(pessoaJuridica);
		return pessoaJuridica;
	}

	private void carregaDadosReceita(PessoaJuridica pessoa, DadosReceitaPessoaJuridica dados){
		pessoa.setNome(dados.getRazaoSocial());
		pessoa.setNomeFantasia(dados.getNomeFantasia());
		pessoa.setDataAbertura(dados.getDataRegistro());
		pessoa.setNumeroCpfResponsavel(dados.getNumCpfResponsavel());
		// Endereco
		Cep cep = cepService.findByCodigo(dados.getNumCep());
		if (cep != null){
			Endereco endereco = new Endereco();
			endereco.setCep(cep);
			endereco.setNomeBairro(dados.getDescricaoBairro());
			endereco.setNomeLogradouro(dados.getDescricaoLogradouro());
			endereco.setNumeroEndereco(dados.getNumLogradouro());
			endereco.setComplemento(dados.getDescricaoComplemento());
			endereco.setUsuario(pessoa);
			pessoa.getEnderecoList().add(endereco);
		}
		/* DADOS INCLUIDOS POR CAUSA DA AMARRACAO DE USUARIO COM PESSOA */
		pessoa.setLogin(InscricaoMFUtil.retiraMascara(pessoa.getNumeroCNPJ()));
		pessoa.setAtivo(Boolean.TRUE);
		pessoa.setBloqueio(Boolean.FALSE);
		pessoa.setProvisorio(Boolean.FALSE);
	}

	/**
	 * Cria uma PessoaJuridica com dados obtidos da Receita Federal.
	 * 
	 * @param cnpj CNPJ da pessoa jurídica que será criada.
	 * @param inscricaoConsulente Inscrição do consulente.
	 * @param exigeReceita Booleano que indica se a operação exija que existam dados na receita. 
	 * @return PessoaJuridica.
	 */
	protected PessoaJuridica novaPessoaJuridicaComDadosDaReceita(String cnpj,
			String inscricaoConsulente, boolean exigeReceita) throws PJeBusinessException {
		
		PessoaJuridica resultado = null;
		
		if (StringUtils.isNotBlank(cnpj)) {
			cnpj = InscricaoMFUtil.formatarCNPJComComplemento(cnpj);
			resultado = new PessoaJuridica();
			resultado.setInTipoPessoa(TipoPessoaEnum.J);
			resultado.setTipoPessoa(ParametroUtil.instance().getTipoPessoaJuridica());
			resultado.setNumeroCNPJ(cnpj);
			resultado.setMatriz(InscricaoMFUtil.isCNPJMatriz(cnpj));
			try {
				inscricaoConsulente = StringUtil.removeNaoNumericos(inscricaoConsulente);
				if(inscricaoConsulente.length() > 11) {
					String cpfResponsavel = recuperaNumeroCpfResponsavel(inscricaoConsulente);
					if(cpfResponsavel != null) {
						inscricaoConsulente = cpfResponsavel;
					} else {
						throw new PJeBusinessException("O CNPJ "+ inscricaoConsulente +" não possui número de CPF responsável.");
					}
				}
				DadosReceitaPessoaJuridica dados = consultaClienteReceitaPJCNJ.consultaDados(
						cnpj,
						inscricaoConsulente,
						true);
				carregaDadosReceita(resultado, dados);
				//Redundância ocorre afim de salvar documento CNPJ com o nome correto da receita
				resultado.setNumeroCNPJ(cnpj);
			} catch (PJeBusinessException e) {
				throw e;
			} catch (Exception e) {
				if (exigeReceita) {
					throw new PJeBusinessException("pje.consultaClienteReceitaPJCNJ.error.erroGenerico");
				}
				throw new PJeBusinessException(e.getMessage());
			}
		}
		return resultado;
	}

	/**
	 * Valida se o CNPJ passado por parâmetro é válido.
	 * 
	 * @param cnpj CNPJ
	 * @throws PJeBusinessException
	 */
	protected static void validarCNPJ(String cnpj) throws PJeBusinessException {
		if (InscricaoMFUtil.validarCpfCnpj(cnpj) == false) {
			throw new PJeBusinessException("pje.pessoaJuridica.error.cnpjInvalido", null, cnpj);
		}
	}
	public String recuperaNumeroCpfResponsavel(String cnpj) {
		String numeroCpfResponsavel = null;
		PessoaJuridica pessoaJuridica = ComponentUtil.getComponent(PessoaJuridicaManager.class).findByCNPJ(cnpj);
		if(pessoaJuridica != null && pessoaJuridica.getNumeroCpfResponsavel() != null) {
			numeroCpfResponsavel = pessoaJuridica.getNumeroCpfResponsavel();
		}
		return numeroCpfResponsavel;
	}
}
