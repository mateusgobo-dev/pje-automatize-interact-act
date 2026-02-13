package br.jus.cnj.pje.webservice.client;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

import javax.xml.namespace.QName;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.trf.webservice.ConsultaClienteReceitaPF;
import br.com.infox.trf.webservice.WebserviceReceitaException;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.webservice.client.consultacpf.ArrayOfPessoaPerfil3;
import br.jus.cnj.pje.webservice.client.consultacpf.ConsultarCPF;
import br.jus.cnj.pje.webservice.client.consultacpf.ConsultarCPFSoap;
import br.jus.cnj.pje.webservice.client.consultacpf.PessoaPerfil3;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;

/**
 * 
 * @author Rodrigo Menezes
 * 
 */
@Name(ConsultaClienteReceitaPFCNJ.NAME)
@Scope(ScopeType.EVENT)
public class ConsultaClienteReceitaPFCNJ extends ConsultaClienteReceitaPF {

	public static final String NAME = "consultaClienteReceitaPFCNJ";
	
	@Logger
	private Log logger;
	
	@In
	private ParametroService parametroService;

	@In
	private PessoaService pessoaService;
	
	@In
	private UsuarioService usuarioService;
	
	@Override
	public DadosReceitaPessoaFisica consultaDados(String inscricao, String inscricaoConsulente, boolean forceUpdate) throws Exception 
	{
		DadosReceitaPessoaFisica ret = null;
		
		String inscricaoPesquisada = InscricaoMFUtil.retiraMascara(inscricao);
		
		if (StringUtil.isEmpty(inscricaoConsulente))
			throw new Exception("É necessário um consulente para efetuar a consulta de dados");
		
		String inscricaoConsulta = InscricaoMFUtil.retiraMascara(inscricaoConsulente);
		
		if (forceUpdate) {
			try {
				ret = consultaWebService(inscricaoPesquisada, inscricaoConsulta);
				
				atualizarDados(ret, inscricaoPesquisada);
			} catch (Exception e) 
			{
				ret = consultaDadosBase(inscricaoPesquisada);

				if (ret == null) 
					throw e;
			}
		} else {
			ret = consultaDadosBase(inscricaoPesquisada);
			
			if (ret == null) {
				ret = consultaWebService(inscricaoPesquisada, inscricaoConsulta);
				atualizarDados(ret, inscricaoPesquisada);
			}
		}
		
		return ret;
	}

	@Override
	public DadosReceitaPessoaFisica consultaDados(String numeroCPF, boolean forceUpdate) throws Exception {		
		Usuario u = usuarioService.getUsuarioLogado();
		Pessoa usuarioLogado = (Pessoa) pessoaService.findById(u.getIdUsuario());
		if (usuarioLogado == null) {
			throw new Exception("É preciso estar logado no sistema para fazer consultas à Receita Federal do Brasil.");
		}
		String consulente = null;
		if(PessoaFisica.class.isAssignableFrom(usuarioLogado.getClass())){
			consulente = ((PessoaFisica) usuarioLogado).getNumeroCPF();
		}else if(PessoaJuridica.class.isAssignableFrom(usuarioLogado.getClass())){
			consulente = ((PessoaJuridica) usuarioLogado).getNumeroCpfResponsavel();
		}
		if(consulente == null){
			throw new Exception("Não foi possível recuperar o número de inscrição do usuário junto ao Ministério da Fazenda para fazer consultas à Receita Federal do Brasil.");
		}
		consulente = InscricaoMFUtil.retiraMascara(consulente);
		return consultaDados(numeroCPF, consulente, forceUpdate);
	}

	@Deprecated
	public DadosReceitaPessoaFisica consultaDadosSemLogin(String numeroCPF, boolean forceUpdate) throws Exception {
		return consultaDados(numeroCPF, numeroCPF, forceUpdate);
	}

	private DadosReceitaPessoaFisica consultaWebService(String inscricaoPesquisada, String inscricaoConsulente, Boolean isHealthCheck)
			throws WebserviceReceitaException, Exception {
		
		Usuario sistema = ParametroUtil.instance().getUsuarioSistema();

		if(Boolean.TRUE.equals((!Objects.equals(Authenticator.getIdUsuarioLogado(), sistema.getIdUsuario())) && !Authenticator.isLogouComCertificado()) && Boolean.TRUE.equals(!isHealthCheck)){
			throw new Exception("Não foi possível recuperar os dados de '"+inscricaoPesquisada+"' junto à Receita Federal.\n"+
		                        "Funcionalidade permitida apenas para usuários com certificado digital.");
		}
		
		String wsdl = parametroService.valueOf("urlWsdlReceita");
		if (Strings.isEmpty(wsdl)) {
			throw new Exception("O endereço WSDL do serviço de consulta de inscrições de pessoas físicas junto à Receita Federal do Brasil não foi definido.");
		}

		ArrayOfPessoaPerfil3 arrayPessoaPerfil3 = null;
		try {
			ConsultarCPF service = new ConsultarCPF(new URL(wsdl), new QName("nsProxyRFBCNJ", "ConsultarCPF"));
			ConsultarCPFSoap port = service.getConsultarCPFSoap();
			arrayPessoaPerfil3 = port.consultarCPFP3(inscricaoPesquisada, inscricaoConsulente);
		} catch (Exception e) {
			throw new WebserviceReceitaException(e);
		}
		if (arrayPessoaPerfil3.getPessoaPerfil3().isEmpty()) {
			throw new Exception("Não foi encontrado registro de pessoa física com a inscrição [" + inscricaoPesquisada + "] na Receita Federal do Brasil.");
		}
		return processaResposta(arrayPessoaPerfil3);
	}
	
	private DadosReceitaPessoaFisica consultaWebService(String inscricaoPesquisada, String inscricaoConsulente) 
			throws WebserviceReceitaException, Exception {
		return this.consultaWebService(inscricaoPesquisada, inscricaoConsulente, false);
	}

	private DadosReceitaPessoaFisica processaResposta(ArrayOfPessoaPerfil3 response) throws Exception {
		DadosReceitaPessoaFisica dadoPessoaReceita = new DadosReceitaPessoaFisica();
		if (response != null) {

			for (PessoaPerfil3 pessoalPerfil3 : response.getPessoaPerfil3()) {
				if (pessoalPerfil3.getErro() != null && !pessoalPerfil3.getErro().isEmpty()) {
					throw new PJeBusinessException("Não foi encontrado registro de pessoa física com a inscrição [" + pessoalPerfil3.getCPF() + "] na Receita Federal do Brasil. " + pessoalPerfil3.getErro());
				}
				dadoPessoaReceita.setNumCPF(pessoalPerfil3.getCPF());
				dadoPessoaReceita.setNome(pessoalPerfil3.getNome());
				try {
					dadoPessoaReceita.setDataNascimento(new SimpleDateFormat("yyyyMMdd").parse(pessoalPerfil3
							.getDataNascimento()));
				} catch (ParseException e) {
					dadoPessoaReceita.setDataNascimento(null);
				}
				dadoPessoaReceita.setSexo(pessoalPerfil3.getSexo());
				dadoPessoaReceita.setNomeMae(pessoalPerfil3.getNomeMae());
				dadoPessoaReceita.setNumTituloEleitor(pessoalPerfil3.getTituloEleitor());
				dadoPessoaReceita.setTipoLogradouro(pessoalPerfil3.getTipoLogradouro());
				dadoPessoaReceita.setLogradouro(pessoalPerfil3.getLogradouro());
				dadoPessoaReceita.setNumLogradouro(pessoalPerfil3.getNumeroLogradouro());
				dadoPessoaReceita.setComplemento(pessoalPerfil3.getComplemento());
				dadoPessoaReceita.setBairro(pessoalPerfil3.getBairro());
				dadoPessoaReceita.setMunicipio(pessoalPerfil3.getMunicipio());
				dadoPessoaReceita.setSiglaUF(pessoalPerfil3.getUF());
				dadoPessoaReceita.setNumCEP(pessoalPerfil3.getCEP());
				dadoPessoaReceita.setSituacaoCadastral(pessoalPerfil3.getSituacaoCadastral());
				return dadoPessoaReceita;
			}
		}
		return null;
	}

	/**
	 * 
	 * [PJEII - 3648] CSJT - Credenciamento de Advogados - Proposta de Evolução
	 * ( simplificação )
	 * 
	 * Possibilitar a consulta sem a inclusao dos dados na base de dados .
	 * 
	 * @author Rafael Carvalho (CSJT)
	 * @param cpf
	 * @return Dados da Receita Federal de Pessoa Fisica
	 * @throws Exception
	 * @throws WebserviceReceitaException
	 */

	public DadosReceitaPessoaFisica getDadosReceitaPessoaFisicaSemAtualizarBaseDeDados(String cpf, Boolean isHealthCheck) throws WebserviceReceitaException, Exception {
		if(cpf == null){
			throw new IllegalArgumentException("Invocada consulta à Receita Federal do Brasil com parâmetro nulo.");
		}
		return consultaWebService(cpf, cpf, isHealthCheck);
	}
	
	public DadosReceitaPessoaFisica getDadosReceitaPessoaFisicaSemAtualizarBaseDeDados(String cpf) throws WebserviceReceitaException, Exception {
		return this.getDadosReceitaPessoaFisicaSemAtualizarBaseDeDados(cpf, false);
	}
	
	public static ConsultaClienteReceitaPFCNJ instance() {
		return ComponentUtil.getComponent(ConsultaClienteReceitaPFCNJ.NAME);
	}
	
}