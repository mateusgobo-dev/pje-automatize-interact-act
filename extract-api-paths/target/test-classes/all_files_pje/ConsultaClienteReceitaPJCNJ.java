package br.jus.cnj.pje.webservice.client;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.namespace.QName;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Strings;

import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.trf.webservice.ConsultaClienteReceitaPJ;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.webservice.client.consultacnpj.ArrayOfCNPJPerfil3;
import br.jus.cnj.pje.webservice.client.consultacnpj.CNPJPerfil3;
import br.jus.cnj.pje.webservice.client.consultacnpj.ConsultarCNPJ;
import br.jus.cnj.pje.webservice.client.consultacnpj.ConsultarCNPJSoap;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaJuridica;

/**
 * 
 * @author Rodrigo Menezes
 * 
 */
@Name(ConsultaClienteReceitaPJCNJ.NAME)
@Scope(ScopeType.EVENT)
public class ConsultaClienteReceitaPJCNJ extends ConsultaClienteReceitaPJ {

	public static final String NAME = "consultaClienteReceitaPJCNJ";
	
	@Logger
	private Log logger;
	
	@In
	private PessoaService pessoaService;
	
	@In
	private ParametroService parametroService;

	@In
	private UsuarioService usuarioService;

	@Override
	public DadosReceitaPessoaJuridica consultaDados(String inscricao, String inscricaoConsulente, boolean forceUpdate) throws Exception {
		DadosReceitaPessoaJuridica ret = null;
		String inscricaoPesquisada = InscricaoMFUtil.retiraMascara(inscricao);
		if (StringUtil.isEmpty(inscricaoConsulente))
			throw new NegocioException("É necessário um consulente para efetuar a consulta de dados");
		String inscricaoConsulta = InscricaoMFUtil.retiraMascara(inscricaoConsulente);
		try {
			if (forceUpdate) {
				try {
					ret = consultaWebService(inscricaoPesquisada, inscricaoConsulta);
					atualizarDados(ret, inscricaoPesquisada);
				} catch (Exception e) {
					ret = consultaDadosBase(inscricaoPesquisada);
					if (ret == null){
						throw new Exception("Erro ao realizar a consulta pelo CNPJ [" + inscricaoPesquisada + "]. ERRO: "+e.getLocalizedMessage(), e);
					}
				}
			} else {
				ret = consultaDadosBase(inscricaoPesquisada);
				if (ret == null) {
					ret = consultaWebService(inscricaoPesquisada, inscricaoConsulta);
					atualizarDados(ret, inscricaoPesquisada);
				}
			}
		} catch (Exception e) {
			throw new Exception("Erro ao realizar a consulta pelo CNPJ [" + inscricaoPesquisada + "]. ERRO: "+e.getLocalizedMessage(), e);
		}
		return ret;
	}
	
	@Override
	public DadosReceitaPessoaJuridica consultaDados(String numeroCNPJ, boolean forceUpdate) throws Exception {
		DadosReceitaPessoaJuridica dadosReceitaPessoaJuridica = null;
		String pesqCNPJ = InscricaoMFUtil.retiraMascara(numeroCNPJ);
		if (forceUpdate) {
			try {
				dadosReceitaPessoaJuridica = consultaWebService(pesqCNPJ);
				atualizarDados(dadosReceitaPessoaJuridica, pesqCNPJ);
			} catch (Exception e) {
				dadosReceitaPessoaJuridica = consultaDadosBase(pesqCNPJ);
				if (dadosReceitaPessoaJuridica == null)
					throw new Exception("Erro ao realizar a consulta pelo CNPJ [" + pesqCNPJ + "]. ERRO: "+e.getLocalizedMessage(), e);
			}
		} else {
			dadosReceitaPessoaJuridica = consultaDadosBase(pesqCNPJ);
			if (dadosReceitaPessoaJuridica == null) {
				dadosReceitaPessoaJuridica = consultaWebService(pesqCNPJ);
				atualizarDados(dadosReceitaPessoaJuridica, pesqCNPJ);
			}
		}
		return dadosReceitaPessoaJuridica;
	}
	
	private DadosReceitaPessoaJuridica consultaWebService(String inscricaoPesquisada, String inscricaoConsulente) throws Exception {
		if(!Authenticator.isLogouComCertificado()){
			throw new Exception("Não foi possível recuperar os dados de '"+inscricaoPesquisada+"' junto à Receita Federal.\n"+
		                        "Funcionalidade permitida apenas para usuários com certificado digital.");
		}
		
		String wsdl = parametroService.valueOf("urlWsdlReceitaCnpj");
		if (Strings.isEmpty(wsdl)) {
			throw new Exception("O endereço WSDL do serviço de consulta de inscrições de pessoas jurídicas junto à Receita Federal não foi definido.");
		}
		ConsultarCNPJ service = new ConsultarCNPJ(new URL(wsdl), new QName("nsProxyRFBCNJ", "ConsultarCNPJ"));
		ConsultarCNPJSoap port = service.getConsultarCNPJSoap();
		ArrayOfCNPJPerfil3 arrayCNPJPerfil3 = port.consultarCNPJP3(inscricaoPesquisada, inscricaoConsulente);
		if (arrayCNPJPerfil3.getCNPJPerfil3().isEmpty()) {
			throw new Exception("Não foi encontrado registro de pessoa jurídica com a inscrição [" + inscricaoPesquisada + "] na Receita Federal do Brasil.");
		}
		return processaResposta(arrayCNPJPerfil3);
	}

	private DadosReceitaPessoaJuridica consultaWebService(String inscricaoPesquisada) throws Exception {
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
		return consultaWebService(inscricaoPesquisada, consulente);
	}

	private DadosReceitaPessoaJuridica processaResposta(ArrayOfCNPJPerfil3 arrayCNPJPerfil3) throws Exception {
		DadosReceitaPessoaJuridica dadoPessoaReceita = new DadosReceitaPessoaJuridica();
		if (arrayCNPJPerfil3 != null) {

			for (CNPJPerfil3 pessoaPerfil3 : arrayCNPJPerfil3.getCNPJPerfil3()) {
				if (pessoaPerfil3.getErro() != null && !pessoaPerfil3.getErro().isEmpty()) {
					throw new PJeBusinessException(pessoaPerfil3.getErro());
				}
				
				dadoPessoaReceita.setNumCNPJ(pessoaPerfil3.getCNPJ());
				// dadoPessoaReceita.setTipoMatrizFilial(pessoaPerfil3.get);
				dadoPessoaReceita.setRazaoSocial(pessoaPerfil3.getNomeEmpresarial());
				dadoPessoaReceita.setNomeFantasia(pessoaPerfil3.getNomeFantasia());
				dadoPessoaReceita.setTipoLogradouro(pessoaPerfil3.getTipoLogradouro());
				dadoPessoaReceita.setDescricaoLogradouro(pessoaPerfil3.getLogradouro());
				dadoPessoaReceita.setNumLogradouro(pessoaPerfil3.getNumeroLogradouro());
				dadoPessoaReceita.setDescricaoComplemento(pessoaPerfil3.getComplemento());
				dadoPessoaReceita.setDescricaoBairro(pessoaPerfil3.getBairro());
				dadoPessoaReceita.setNumCep(pessoaPerfil3.getCEP());
				dadoPessoaReceita.setCodigoMunicipio(pessoaPerfil3.getCodigoMunicipio());
				dadoPessoaReceita.setDescricaoMunicipio(pessoaPerfil3.getNomeMunicipio());
				dadoPessoaReceita.setSiglaUf(pessoaPerfil3.getUF());
				dadoPessoaReceita.setNumDdd1(pessoaPerfil3.getDDD1());
				dadoPessoaReceita.setNumTelefone1(pessoaPerfil3.getTelefone1());
				dadoPessoaReceita.setNumDdd2(pessoaPerfil3.getDDD2());
				dadoPessoaReceita.setNumTelefone2(pessoaPerfil3.getTelefone2());
				// dadoPessoaReceita.setNumFax(pessoaPerfil3.get);
				// dadoPessoaReceita.setNumDddFax(split[18]);
				dadoPessoaReceita.setCorreioEletronico(pessoaPerfil3.getEmail());
				// dadoPessoaReceita.setInSocio(split[20]);
				dadoPessoaReceita.setCodigoCnaeFiscal(pessoaPerfil3.getCNAEPrincipal());
				//dadoPessoaReceita.setDescricaoCnaeFiscal(pessoaPerfil3.getCNAEPrincipal());
				dadoPessoaReceita.setCodigoNaturezaJuridica(pessoaPerfil3.getNaturezaJuridica());
				//dadoPessoaReceita.setDescricaoNaturezaJuridica(pessoaPerfil3.getNaturezaJuridica());

				try {
					dadoPessoaReceita.setDataRegistro(new SimpleDateFormat("yyyyMMdd").parse(pessoaPerfil3
							.getDataAbertura()));
				} catch (ParseException e) {
					dadoPessoaReceita.setDataRegistro(null);
				}

				try {
					dadoPessoaReceita.setDataSituacaoCnpj(new SimpleDateFormat("yyyyMMdd").parse(pessoaPerfil3
							.getDataSituacaoCadastral()));
				} catch (ParseException e) {
					dadoPessoaReceita.setDataSituacaoCnpj(null);
				}

				dadoPessoaReceita.setStatusCadastralPessoaJuridica(pessoaPerfil3.getSituacaoCadastral());

				dadoPessoaReceita.setNumCpfResponsavel(pessoaPerfil3.getCPFResponsavel());
				dadoPessoaReceita.setNomeResponsavel(pessoaPerfil3.getNomeResponsavel());

				return dadoPessoaReceita;
			}
		}

		return null;
	}

	public static ConsultaClienteReceitaPJCNJ instance() {
		return ComponentUtil.getComponent(ConsultaClienteReceitaPJCNJ.NAME);
	}

}
