/**
 * 
 */
package br.jus.cnj.pje.nucleo.service;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.webservice.client.ConsultaClienteReceitaPFCNJ;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;

/**
 * @author cristof
 * 
 */
@Name("pessoaMagistradoService")
@Transactional
@Scope(ScopeType.EVENT)
public class PessoaMagistradoService {

	@In(create = true)
	private PessoaMagistradoManager pessoaMagistradoManager;

	@In(create = true)
	private ConsultaClienteReceitaPFCNJ consultaClienteReceitaPF;

	@In(create = true)
	private CepService cepService;

	@In(create = true)
	private PapelService papelService;

	public PessoaMagistrado create(String cpf, String matricula, Localizacao... localizacoes) {
		PessoaMagistrado pessoa = pessoaMagistradoManager.findByCPF(cpf);
		if (pessoa == null) {
			pessoa = new PessoaMagistrado();
			pessoa.setTipoPessoa(ParametroUtil.instance().getTipoPessoaFisica());
			DadosReceitaPessoaFisica dados = null;
			;
			try {
				dados = consultaClienteReceitaPF.consultaDados(cpf, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (dados != null) {
				carregaDadosReceita(pessoa.getPessoa(), dados);
			}
			pessoa.setMatricula("");
		}
		for (Localizacao loc : localizacoes) {
			incluirLocalizacao(pessoa, loc);
		}
		return pessoa;
	}

	private void incluirLocalizacao(PessoaMagistrado pessoa, Localizacao loc) {
		// Localizacao gabinete = this.obtemLocalizacaoGabinete(loc);
		UsuarioLocalizacao usuLoc = new UsuarioLocalizacao();
		usuLoc.setLocalizacaoFisica(loc);
//		usuLoc.setLocalizacaoModelo(loc);
		usuLoc.setUsuario(pessoa.getPessoa());
		usuLoc.setPapel(papelService.findByCodeName(Papeis.MAGISTRADO));
		usuLoc.setResponsavelLocalizacao(false);
		pessoa.getUsuarioLocalizacaoList().add(usuLoc);
	}

	private String acrescentaMascaraCPF(String cpf) {
		if (cpf.length() != 11) {
			throw new IllegalArgumentException(String.format("O CPF informado [" + cpf
					+ " ] tem número de caracteres diferente de 11.", cpf));
		}
		StringBuilder sb = new StringBuilder();
		sb.append(cpf.substring(0, 3));
		sb.append(".");
		sb.append(cpf.substring(3, 6));
		sb.append(".");
		sb.append(cpf.substring(6, 9));
		sb.append("-");
		sb.append(cpf.substring(9));
		return sb.toString();
	}

	protected void carregaDadosReceita(PessoaFisica pessoa, DadosReceitaPessoaFisica dados) {
		pessoa.setNome(dados.getNome());
		pessoa.setDataNascimento(dados.getDataNascimento());
		pessoa.setSexo(dados.getSexo().equals("1") ? SexoEnum.M : SexoEnum.F);
		pessoa.setNomeGenitora(dados.getNomeMae());

		// Documentos de identificacao
		pessoa.setNumeroCPF(acrescentaMascaraCPF(dados.getNumCPF()));
		pessoa.setNumeroTituloEleitor(dados.getNumTituloEleitor());
		// Endereco
		Cep cep = cepService.findByCodigo(dados.getNumCEP());
		if (cep != null) {
			Endereco endereco = new Endereco();
			endereco.setCep(cep);
			endereco.setNomeBairro(dados.getBairro());
			endereco.setNomeLogradouro(dados.getLogradouro());
			endereco.setNumeroEndereco(dados.getNumLogradouro());
			endereco.setComplemento(dados.getComplemento());
			endereco.setUsuario(pessoa);
			pessoa.getEnderecoList().add(endereco);
		}
	}

	public PessoaMagistrado persist(PessoaMagistrado p) {
		try {
			return pessoaMagistradoManager.persist(p);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void refresh(PessoaMagistrado p) {
		try {
			this.pessoaMagistradoManager.refresh(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}