package br.jus.cnj.pje.nucleo.service;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.list.ProcessoAudienciaList;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.infox.trf.webservice.ConsultaClienteReceitaPF;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;

@Name(PessoaFisicaService.NAME)
public class PessoaFisicaService extends BaseService{

	public static final String NAME = "pessoaFisicaService";
	
	@In
	private PessoaFisicaManager pessoaFisicaManager;
	
	@In(create=true)
	private ConsultaClienteReceitaPF consultaClienteReceitaPFCNJ;

	@In
	private CepService cepService;

	@In
	private UsuarioManager usuarioManager;

	public PessoaFisica persist(PessoaFisica pessoa){
		try{
			pessoaFisicaManager.persistAndFlush(pessoa);
			return pessoa;
		} catch (PJeBusinessException e){
			e.printStackTrace();
			return null;
		}
	}

	public PessoaFisica findByCPF(String cpf, String consulente) throws PJeBusinessException{
		return this.findByCPF(cpf, consulente, true);
	}
	
	public PessoaFisica findByCPF(String cpf, String consulente, boolean exigeReceita) throws PJeBusinessException {
		int size = cpf.length();
		if (size == 11 && !InscricaoMFUtil.verificaCPF(cpf)) {
			throw new PJeBusinessException("pje.pessoaFisica.error.cpfInvalido", null, cpf);
		}
		cpf = InscricaoMFUtil.acrescentaMascaraMF(cpf);
		PessoaFisica pessoa = pessoaFisicaManager.findByCPF(cpf);
		if (pessoa == null) {
			validarExistenciaDeUsuario(cpf);
			pessoa = new PessoaFisica();
			pessoa.setInTipoPessoa(TipoPessoaEnum.F);
			pessoa.setTipoPessoa(ParametroUtil.instance().getTipoPessoaFisica());
			DadosReceitaPessoaFisica dados = null;
			try {
				consulente = StringUtil.removeNaoNumericos(consulente);
				if(consulente.length() > 11) {
					String cpfResponsavel = ComponentUtil.getComponent(PessoaJuridicaService.class).recuperaNumeroCpfResponsavel(consulente);
					if(cpfResponsavel != null) {
						consulente = cpfResponsavel;
					} else {
						throw new PJeBusinessException("O CNPJ "+ consulente +" não possui número de CPF responsável.");
					}
				}
				dados = (DadosReceitaPessoaFisica) consultaClienteReceitaPFCNJ.consultaDados(cpf,consulente, true);
				carregaDadosReceita(pessoa, dados);
			} catch (PJeBusinessException e) {
				throw e;
			} catch (Exception e) {
				if (exigeReceita) {
					throw new PJeBusinessException("pje.consultaClienteReceitaPFCNJ.error.erroGenerico");
				}
				throw new PJeBusinessException(e);
			}
		}
		return pessoa;
	}
	
	public PessoaFisica find(Integer id){
		try{
			return pessoaFisicaManager.findById(id);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	protected void carregaDadosReceita(PessoaFisica pessoa, DadosReceitaPessoaFisica dados){
		pessoa.setNome(dados.getNome());
		pessoa.setDataNascimento(dados.getDataNascimento());
		pessoa.setSexo(dados.getSexo().equals("1") ? SexoEnum.M : SexoEnum.F);
		pessoa.setNomeGenitora(dados.getNomeMae());

		// Documentos de identificacao
		pessoa.setNumeroCPF(InscricaoMFUtil.acrescentaMascaraMF(dados.getNumCPF()));
		pessoa.setNumeroTituloEleitor(dados.getNumTituloEleitor());
		// Endereco
		Cep cep = cepService.findByCodigo(dados.getNumCEP());
		if (cep != null){
			Endereco endereco = new Endereco();
			endereco.setCep(cep);
			endereco.setNomeBairro(dados.getBairro());
			endereco.setNomeLogradouro(dados.getLogradouro());
			endereco.setNumeroEndereco(dados.getNumLogradouro());
			endereco.setComplemento(dados.getComplemento());
			endereco.setUsuario(pessoa);
			pessoa.getEnderecoList().add(endereco);
		}

		/* DADOS INCLUIDOS POR CAUSA DA AMARRACAO DE USUARIO COM PESSOA */
/*		pessoa.setLogin(InscricaoMFUtil.retiraMascara(pessoa.getNumeroCPF()));
		pessoa.setAtivo(true);
		pessoa.setBloqueio(false);
		pessoa.setProvisorio(false);*/

		if (Pessoa.instanceOf(pessoa, PessoaMagistrado.class)){
			pessoa.getPessoaMagistrado().setMatricula("");
		}
	}

	/**
	 * Valida a existência de um Usuário na aplicação, se o usuário existir será lançado um erro, 
	 * pois a validação será invocada no processo de criação de uma nova pessoa física.
	 * 
	 * @param cpf CPF que será consultado na tabela usuario_login.
	 * @throws PJeBusinessException 
	 */
	private void validarExistenciaDeUsuario(String cpf) throws PJeBusinessException {
		
		cpf = InscricaoMFUtil.retiraMascara(cpf);
		Usuario usuario = usuarioManager.findByLogin(cpf);
		if (usuario != null) {
			String classeUsuario = usuario.getClass().getSimpleName();
			String mensagem = "A pessoa do cpf '%s' já está cadastrada como '%s'. "
					+ "Contacte o administrador para verificar o cadastro!";
			throw new PJeBusinessException(String.format(mensagem, cpf, classeUsuario));
		}
		
	}
}
