package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.PasswordHash;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.trf.webservice.ConsultaClienteOAB;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.intercomunicacao.v222.beans.CadastroIdentificador;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoIdentificacao;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeDocumentoIdentificador;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeGeneroPessoa;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadePoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadesRelacionamentoPessoal;
import br.jus.cnj.intercomunicacao.v222.beans.PoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.TipoQualificacaoPessoa;
import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.v222.servico.IntercomunicacaoService.PessoaQualificacaoEnum;
import br.jus.cnj.pje.intercomunicacao.v222.util.IntercomunicacaoUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.CepManager;
import br.jus.cnj.pje.nucleo.manager.EnderecoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAutoridadeManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaNomeAlternativoManager;
import br.jus.cnj.pje.nucleo.manager.RelacaoPessoalManager;
import br.jus.cnj.pje.nucleo.manager.TipoPessoaManager;
import br.jus.cnj.pje.nucleo.manager.TipoRelacaoPessoalManager;
import br.jus.cnj.pje.nucleo.service.PessoaFisicaService;
import br.jus.cnj.pje.nucleo.service.PessoaJuridicaService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.entidades.RelacaoPessoal;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.TipoRelacaoPessoal;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.enums.TipoPessoaRelacaoEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.cna.entidades.DadosAdvogadoOAB;


/**
 * Conversor de br.jus.cnj.intercomunicacao.v222.beans.Pessoa para br.jus.pje.nucleo.entidades.Pessoa
 */
@AutoCreate
@Name(PessoaMNIParaPessoaPJEConverter.NAME)
public class PessoaMNIParaPessoaPJEConverter extends IntercomunicacaoConverterAbstrato<br.jus.cnj.intercomunicacao.v222.beans.Pessoa, Pessoa> {
	
	public static final String NAME = "v222.pessoaMNIParaPessoaPJEConverter";
	
	@Logger
	private Log log;

	@In
	private UsuarioService usuarioService;
	
	@In
	private PessoaService pessoaService;
	
	@In
	private TipoPessoaManager tipoPessoaManager;
	
	@In
	private PessoaNomeAlternativoManager pessoaNomeAlternativoManager;
	
	@In
	private EnderecoManager enderecoManager;
	
	@In
	private CepManager cepManager;
	
	@In
	private PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager;
	
	@In
	private RelacaoPessoalManager relacaoPessoalManager;
	
	@In
	private TipoRelacaoPessoalManager tipoRelacaoPessoalManager;
	
	@In
	private PessoaAutoridadeManager pessoaAutoridadeManager;
	
	@In (value = DocumentoIdentificacaoParaTipoDocumentoIdentificacaoConverter.NAME)
	private DocumentoIdentificacaoParaTipoDocumentoIdentificacaoConverter tipoDocumentoConverter;
	
	@Override
	public Pessoa converter(br.jus.cnj.intercomunicacao.v222.beans.Pessoa tipoPessoa) {
		return converter(null, tipoPessoa, null);
	}
	
	public Pessoa converter(br.jus.cnj.intercomunicacao.v222.beans.Pessoa tipoPessoa, PessoaQualificacaoEnum qualificacaoPessoa) {
		return converter(null, tipoPessoa, qualificacaoPessoa);
	}
	
	public Pessoa converter(PoloProcessual polo, br.jus.cnj.intercomunicacao.v222.beans.Pessoa tipoPessoa, PessoaQualificacaoEnum qualificacaoPessoa) {
		Pessoa pessoa = null;
		
		try {
			if (tipoPessoa == null) {
				throw new NegocioException("Pessoa nula");
			}
	
			if (tipoPessoa.getNome() == null || tipoPessoa.getNome().trim().isEmpty()) {
				throw new NegocioException("Não foi informado um nome para a pessoa");
			}
	
			pessoa = recuperaPessoa(tipoPessoa);
			
			//inclui, se não localizou a pessoa
			if(pessoa == null){
				pessoa = incluirPessoa(tipoPessoa, qualificacaoPessoa);
			}
			
			validarEnderecoDePessoa(polo, tipoPessoa);
			carregarColecaoEndereco(pessoa, tipoPessoa);
			
			//Especializa Advogado
			if(tipoPessoa != null && qualificacaoPessoa != null && pessoa != null){
				if(qualificacaoPessoa == PessoaQualificacaoEnum.ADVOGADO){
					if(EntityUtil.find(PessoaAdvogado.class, pessoa.getIdUsuario()) == null){
					 especializa(tipoPessoa, pessoa, PessoaAdvogado.class);	
					}
				}
			}
			
			// Carrega os nomes alternativos.
			pessoa.getPessoaNomeAlternativoList().addAll(carregarColecaoPessoaNomeAlternativo(tipoPessoa, pessoa));
			
			// Carrega os documentos de identificação
			List<PessoaDocumentoIdentificacao> pessoaDocumentoIdentificacaoList = carregarDocumentosIdentificacao(tipoPessoa.getDocumento(), pessoa);
			for (PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao : pessoaDocumentoIdentificacaoList) {
				if (! isContemDocumentoIdentificacao(pessoa.getPessoaDocumentoIdentificacaoList(), pessoaDocumentoIdentificacao)) {
				//if (!pessoa.getPessoaDocumentoIdentificacaoList().contains(pessoaDocumentoIdentificacao)) {
					pessoaDocumentoIdentificacaoManager.persist(pessoaDocumentoIdentificacao);
					pessoa.getPessoaDocumentoIdentificacaoList().add(pessoaDocumentoIdentificacao);
				}
			}
	
			// Carrega as relações pessoais
			List<RelacaoPessoal> relacaoPessoalList = carregarRelacoesPessoais(tipoPessoa.getPessoaRelacionada(), pessoa);
			for (RelacaoPessoal relacaoPessoal : relacaoPessoalList) {
				if (!pessoa.getRelacaoPessoalList().contains(relacaoPessoal)) {
					relacaoPessoalManager.persist(relacaoPessoal);
					pessoa.getRelacaoPessoalList().add(relacaoPessoal);
				}
			}
			
			//recuperar a autoridade vinculada ao orgao de representacao
			if(tipoPessoa.getTipoPessoa() == TipoQualificacaoPessoa.AUTORIDADE && !(pessoa instanceof PessoaAutoridade)){
				pessoa = recuperarAutoridadeVinculada(pessoa);
			}
		
		} catch (PJeBusinessException e) {
			throw new NegocioException(e.getLocalizedMessage());
		}
		
		return pessoa;
	}

	/**
	 * Carrega os endereços da pessoa passada por parâmetro.
	 * 1) Se a pessoa não tiver endereço então será consultado a pessoa na receita federal para ver 
	 * se algum endereço é retornado.
	 * 2) Os endereços passados pela PessoaMNI serão adicionados na PessoaPJE se já não estiver na 
	 * lista.
	 * 
	 * @param pessoa Pessoa PJE.
	 * @param pessoaMNI Pessoa MNI.
	 * @throws PJeBusinessException 
	 */
	private void carregarColecaoEndereco(Pessoa pessoa, br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoaMNI) throws PJeBusinessException {

		List<Endereco> enderecos = pessoa.getEnderecoList();
		List<br.jus.cnj.intercomunicacao.v222.beans.Endereco> enderecosMNI = pessoaMNI.getEndereco();
		
		if (!ProjetoUtil.isVazio(enderecosMNI)) {
			for (final br.jus.cnj.intercomunicacao.v222.beans.Endereco enderecoMNI : enderecosMNI) {
				Boolean jaCadastrado = CollectionUtils.exists(enderecos, novoFiltroEnderecoPeloCep(enderecoMNI));
				
				if (!jaCadastrado) {
					IntercomunicacaoEnderecoParaEnderecoConverter converter = new IntercomunicacaoEnderecoParaEnderecoConverter();
					Endereco endereco = converter.converter(enderecoMNI, pessoa);
					
					enderecos.add(endereco);
					try {
						enderecoManager.persist(endereco);
					} catch (PJeBusinessException e) {
						throw new NegocioException(e.getLocalizedMessage());
					}
				}
			}
		}
	}

	private Pessoa recuperarAutoridadeVinculada(Pessoa pessoa) {
		List<PessoaAutoridade> pessoaAutoridadeList = pessoaAutoridadeManager.findByOrgaoVinculacao(pessoa);
		if(isVazio(pessoaAutoridadeList)){
			throw new NegocioException(String.format("A pessoa \"%s\" não está vinculada a nenhuma autoridade", pessoa.getNome()));
		}
		if(pessoaAutoridadeList.size() > 1){
			throw new NegocioException(String.format("Existe mais de uma autoridade vinculada à pessoa \"%s\"", pessoa.getNome()));
		}
		
		return pessoaAutoridadeList.get(0);
	}

	private Pessoa recuperaPessoa(br.jus.cnj.intercomunicacao.v222.beans.Pessoa tipoPessoa){
		Pessoa pessoa = null;
		
		switch (tipoPessoa.getTipoPessoa()) {
		case FISICA:
		case JURIDICA:
			pessoa = recuperaPessoaPeloLogin(tipoPessoa.getNumeroDocumentoPrincipal());		

			if (pessoa==null && tipoPessoa.getDocumento() != null && tipoPessoa.getDocumento().size() > 0) {
				try {
					pessoa = buscarPessoaPorDocumento(tipoPessoa.getTipoPessoa(), tipoPessoa.getDocumento());
					if(pessoa == null){
						pessoa = incluirPessoa(tipoPessoa, null);
					}
					
					if (pessoa.getIdPessoa() == null) {
						pessoa.setIdPessoa(pessoa.getIdUsuario());
						pessoa = pessoaService.persist(pessoa);
					} else {
						pessoa = EntityUtil.getEntityManager().merge(pessoa);
					}
					
				} catch (PJeBusinessException e) {
					throw new NegocioException(e.getLocalizedMessage());
				}
			}
			break;

		case AUTORIDADE:
		case ORGAOREPRESENTACAO:
			if(tipoPessoa.getPessoaVinculada() == null){
				throw new NegocioException(String.format("A autoridade %s não está vinculada a nenhuma pessoa jurídica", tipoPessoa.getNome()));
			}
			Pessoa pessoaVinculada = converter(tipoPessoa.getPessoaVinculada());
			pessoa = pessoaAutoridadeManager.findByOrgaoVinculacaoENome(pessoaVinculada, tipoPessoa.getNome());
			break;
		}
		
		return pessoa;
	}

	/**
	 * Recupera a pessoa pelo login
	 * @param login Login do usuário
	 * @return Pessoa
	 */
	private Pessoa recuperaPessoaPeloLogin(CadastroIdentificador cadastroIdentificador){
		if(!isNull(cadastroIdentificador)){
			String login = cadastroIdentificador.getValue();
			
			if (!isVazio(login)) {
				login = StringUtil.removeNaoAlphaNumericos(login);
				return (Pessoa) IntercomunicacaoUtil.existePessoa(login);
			}
		}
		return null;
	}

	/**
	 * Busca a pessoa pelo documento
	 * @param tipoPessoa TipoQualificacaoPessoa
	 * @param tipoDocumentoIdentificacaoList lista dos documentos de identificacao
	 * @return Pessoa pesquisada por documento
	 * @throws PJeBusinessException 
	 */
	private Pessoa buscarPessoaPorDocumento(TipoQualificacaoPessoa tipoPessoa, 
			List<br.jus.cnj.intercomunicacao.v222.beans.DocumentoIdentificacao> tipoDocumentoIdentificacaoList) throws PJeBusinessException {
		Pessoa pessoa = null;
		
		String consulente = (String) Contexts.getSessionContext().get(MNIParametro.PARAM_CPF_CNPJ_USUARIO);
		
		

		for(br.jus.cnj.intercomunicacao.v222.beans.DocumentoIdentificacao tipoDocumento : tipoDocumentoIdentificacaoList) {
			TipoDocumentoIdentificacao tipoDocumentoIdentificacao = tipoDocumentoConverter.converter(tipoDocumento, tipoPessoa);
			
			if(tipoDocumentoIdentificacao != null){
				if(tipoDocumentoIdentificacao.getCodTipo().equalsIgnoreCase(
						DocumentoIdentificacaoParaTipoDocumentoIdentificacaoConverter.CODIGO_TIPO_DOCUMENTO_CPF)) {
					PessoaFisicaService pfs = (PessoaFisicaService)Component.getInstance(PessoaFisicaService.class);
					pessoa = pfs.findByCPF(tipoDocumento.getCodigoDocumento(),consulente, false);
					if(pessoa != null) {
						break;
					}
				}
				
				else if(tipoDocumentoIdentificacao.getCodTipo().equalsIgnoreCase(
						DocumentoIdentificacaoParaTipoDocumentoIdentificacaoConverter.CODIGO_TIPO_DOCUMENTO_CNPJ)) {
					PessoaJuridicaService pjs = (PessoaJuridicaService)Component.getInstance(PessoaJuridicaService.class);
					try {
						pessoa = pjs.findByDocumentoCNPJ(tipoDocumento.getCodigoDocumento(),consulente,false);
					} catch (PJeBusinessException e) {
						throw new NegocioException(((PJeBusinessException) e).getLocalizedMessage());
					} catch (Exception e) {
						for (Object object : ((PJeDAOException)e).getParams()) {
							((IllegalStateException) object).getMessage();
							throw new NegocioException(((IllegalStateException) object).getMessage());
						}
					} 
					if(pessoa != null) {
						List<PessoaDocumentoIdentificacao> pessoaDocumentoIdentificacaoList = 
								pessoaDocumentoIdentificacaoManager.findByNumeroDocumento(tipoDocumento.getCodigoDocumento());
						if (pessoaDocumentoIdentificacaoList != null && !pessoaDocumentoIdentificacaoList.isEmpty()) {
							pessoa = pessoaDocumentoIdentificacaoList.get(0).getPessoa();
							if(pessoa.getInTipoPessoa() == TipoPessoaEnum.J) {
								pessoa = EntityUtil.find(PessoaJuridica.class, pessoa.getIdUsuario());
							} else if(pessoa.getInTipoPessoa() == TipoPessoaEnum.F) {
								pessoa = EntityUtil.find(PessoaFisica.class, pessoa.getIdUsuario());
								
							}
						break;
						}
					}
				} else {
					// Busca uma pessoa por outro documento, será usada a busca que retornar somente uma pessoa para evitar
					// o uso de registro errado.
					ModalidadeDocumentoIdentificadorParaTipoDocumentoIdentificacaoConverter converter = 
							ComponentUtil.getComponent(ModalidadeDocumentoIdentificadorParaTipoDocumentoIdentificacaoConverter.class); 
					
					String numeroDocumento = tipoDocumento.getCodigoDocumento();
					ModalidadeDocumentoIdentificador modalidadeDocumento = tipoDocumento.getTipoDocumento();
					String codigoTipoDocumento = converter.converter(modalidadeDocumento, tipoPessoa);
					PessoaManager pessoaManager = (PessoaManager) Component.getInstance(PessoaManager.class);
					List<Pessoa> pessoas = pessoaManager.findByDocument(numeroDocumento, codigoTipoDocumento);
					if (ProjetoUtil.getTamanho(pessoas) >= 1) {
						pessoa = pessoas.get(0);
						break;
					}
				}
			}
		}

		return pessoa;
	}
		
	/**
	 * Inclui uma nova pessoa
	 * @param tipoPessoa 
	 * @return Pessoa
	 * @throws PJeBusinessException 
	 */
	public Pessoa incluirPessoa(br.jus.cnj.intercomunicacao.v222.beans.Pessoa tipoPessoa, PessoaQualificacaoEnum qualificacaoPessoa) throws PJeBusinessException {
		Pessoa pessoa = null;
		
		// luis sergio PJEII-18635
		if (tipoPessoa.getTipoPessoa() == null) {
			throw new NegocioException("Não foi definido um tipo de Pessoa.");
		}
		// luis sergio
					
		switch (tipoPessoa.getTipoPessoa()) {
			case FISICA:
				pessoa = incluirPessoaFisica(tipoPessoa);
				break;

			case JURIDICA:
				pessoa = incluirPessoaJuridica(tipoPessoa);
				break;

			case AUTORIDADE:
				pessoa = incluirPessoaAutoridade(tipoPessoa);
				break;

			// ecnj
			case ORGAOREPRESENTACAO:
				pessoa = incluirPessoaAutoridade(tipoPessoa);
				break;
		}
		
		if (qualificacaoPessoa != null) {
			switch (qualificacaoPessoa) {
			case ADVOGADO:
				especializa(tipoPessoa, pessoa, PessoaAdvogado.class);
				break;
			case ESCRITORIO_ADVOCACIA:
				// pessoa = carregarPessoaAdvogado
				break;

			case MINISTERIO_PUBLICO:
				// pessoa = carregarPessoaMinisterioPublico
				break;

			case DEFENSORIA_PUBLICA:
				// pessoa = carregarPessoaDefensoriaPublica
				break;

			case PROCURADORIA:
				// pessoa = carregarPessoaAdvocaciaPublica
				break;

			default:
				break;
			}
		}

		String login = null;
			if (tipoPessoa.getNumeroDocumentoPrincipal() != null && tipoPessoa.getNumeroDocumentoPrincipal().getValue().trim().length() > 0) {
				login = tipoPessoa.getNumeroDocumentoPrincipal().getValue().replace(".", "").replace("-", "").replace("/", "");
		} else {
			login = java.util.UUID.randomUUID().toString();
		}
		
		pessoa.setLogin(login);
		String senhaSalt = new PasswordHash().generateSaltedHash(login, login, "SHA1");
		pessoa.setSenha(senhaSalt);
		pessoa.setNome(tipoPessoa.getNome());
		if (pessoa instanceof PessoaJuridica) {
			((PessoaJuridica) pessoa).setNomeFantasia(tipoPessoa.getNome());
		}
		pessoa.setAtivo(true);
		pessoa.setBloqueio(true);
		
		if (pessoa.getIdPessoa()==null){
			pessoaService.persist(pessoa);
		}
		
		return pessoa;

	}

	/**
	 * Inclui pessoa física
	 * @param tipoPessoa
	 * @return
	 * @throws Exception
	 */
	private PessoaFisica incluirPessoaFisica(br.jus.cnj.intercomunicacao.v222.beans.Pessoa tipoPessoa) {
		PessoaFisica pessoaFisica = new PessoaFisica();
	
		pessoaFisica.setTipoPessoa(ParametroUtil.instance().getTipoPessoaFisica());
		if (tipoPessoa.getDataNascimento() != null) {
			pessoaFisica.setDataNascimento(DateUtil.stringToDate(tipoPessoa.getDataNascimento().getValue(), MNIParametro.PARAM_FORMATO_DATA));
		}
		if (tipoPessoa.getDataObito() != null) {
			pessoaFisica.setDataObito(DateUtil.stringToDate(tipoPessoa.getDataObito().getValue(), MNIParametro.PARAM_FORMATO_DATA));
		}
		if(tipoPessoa.getNomeGenitor() != null && !tipoPessoa.getNomeGenitor().trim().equals(""))
		pessoaFisica.setNomeGenitor(tipoPessoa.getNomeGenitor());
		if(tipoPessoa.getNomeGenitora() != null && !tipoPessoa.getNomeGenitora().trim().equals(""))
		pessoaFisica.setNomeGenitora(tipoPessoa.getNomeGenitora());
		if (tipoPessoa.getSexo() != null) {
			if (tipoPessoa.getSexo().equals(ModalidadeGeneroPessoa.M)) {
				pessoaFisica.setSexo(SexoEnum.M);
			} else {
				pessoaFisica.setSexo(SexoEnum.F);
			}
		}
		
		DocumentoIdentificacao cpf = obterDocumentoIdentificacao(tipoPessoa, ModalidadeDocumentoIdentificador.CMF);
		if (cpf != null) {
			pessoaFisica.setNumeroCPF(cpf.getCodigoDocumento());
		}
	
		return pessoaFisica;
	}

	/**
	 * Inclui Pessoa Jurídica
	 * @param tipoPessoa
	 * @return
	 */
	private PessoaJuridica incluirPessoaJuridica(br.jus.cnj.intercomunicacao.v222.beans.Pessoa tipoPessoa) {
		PessoaJuridica pessoaJuridica = new PessoaJuridica();
		String documentoPrincipal = null;
		
		pessoaJuridica.setTipoPessoa(ParametroUtil.instance().getTipoPessoaJuridica());
		pessoaJuridica.setInTipoPessoa(TipoPessoaEnum.J);
		
		if(tipoPessoa.getNumeroDocumentoPrincipal() != null){
			documentoPrincipal = tipoPessoa.getNumeroDocumentoPrincipal().getValue();
		}
		pessoaJuridica.setMatriz(InscricaoMFUtil.isCNPJMatriz(documentoPrincipal));			
		
		if (tipoPessoa.getDataNascimento() != null) {
			pessoaJuridica.setDataAbertura(DateUtil.stringToDate(tipoPessoa.getDataNascimento().getValue(), MNIParametro.PARAM_FORMATO_DATA));
		}
		if (tipoPessoa.getDataObito() != null) {
			pessoaJuridica.setDataFimAtividade(DateUtil.stringToDate(tipoPessoa.getDataObito().getValue(), MNIParametro.PARAM_FORMATO_DATA));
		}
		return pessoaJuridica;
	}

	/**
	 * Incluir PessoaAutoridade
	 * @param tipoPessoa
	 * @return
	 * @throws PJeBusinessException 
	 */
	private PessoaAutoridade incluirPessoaAutoridade(br.jus.cnj.intercomunicacao.v222.beans.Pessoa tipoPessoa) throws PJeBusinessException {
		Pessoa pessoaVinculada = recuperaPessoa(tipoPessoa.getPessoaVinculada()); 
		
		if (!(pessoaVinculada instanceof PessoaJuridica)) {
			throw new NegocioException(String.format("A autoridade %s deve estar vinculada a uma pessoa jurídica", tipoPessoa.getNome()));
		}
		
		PessoaAutoridade pessoaAutoridade = new PessoaAutoridade();
	    TipoPessoa tipoPessoaAutoridade = tipoPessoaManager.findByCdTipoPessoa(tipoPessoa.getTipoPessoa().toString());
	   
	    pessoaAutoridade = new PessoaAutoridade();
	    pessoaAutoridade.setTipoPessoa(tipoPessoaAutoridade);
	    pessoaAutoridade.setOrgaoVinculacao((PessoaJuridica) pessoaVinculada);
	   
	    return pessoaAutoridade;
	}
	
	/**
	 * Carrega os documentos da pessoa
	 * @param documentos
	 * @param pessoa
	 * @return
	 */	
	private List<PessoaDocumentoIdentificacao> carregarDocumentosIdentificacao(List<br.jus.cnj.intercomunicacao.v222.beans.DocumentoIdentificacao> documentos, Pessoa pessoa) {
		TipoQualificacaoPessoa tqp;
		if (pessoa.getInTipoPessoa().equals(TipoPessoaEnum.F)) {
			tqp = TipoQualificacaoPessoa.FISICA;
		} else if (pessoa.getInTipoPessoa().equals(TipoPessoaEnum.J)) {
			tqp = TipoQualificacaoPessoa.JURIDICA;
		} else {
			tqp = TipoQualificacaoPessoa.AUTORIDADE;
		}
		List<PessoaDocumentoIdentificacao> pessoaDocumentoIdentificacaoList = new ArrayList<PessoaDocumentoIdentificacao>();
		for (br.jus.cnj.intercomunicacao.v222.beans.DocumentoIdentificacao documento : documentos) {
			TipoDocumentoIdentificacao tipoDocumentoIdentificacao = tipoDocumentoConverter.converter(documento, tqp);
			String nomePessoaDocumentoIdentificacao = documento.getNome();
			String codigoDocumento = documento.getCodigoDocumento();
			
			if(nomePessoaDocumentoIdentificacao == null){
				nomePessoaDocumentoIdentificacao = pessoa.getNome();
			}
			
			if (tipoDocumentoIdentificacao != null && StringUtils.isNotBlank(codigoDocumento)) {
				// Verifica se o documento já está associado a pessoa. Se já
				// existir, o documento é atualizado.
				// Caso contrário é criado um novo e inserido na base de dados.
				PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao = IntercomunicacaoUtil.existeDocumento(pessoa, tipoDocumentoIdentificacao, codigoDocumento);
				if (pessoaDocumentoIdentificacao == null) {
					pessoaDocumentoIdentificacao = new PessoaDocumentoIdentificacao();
					pessoaDocumentoIdentificacao.setPessoa(pessoa);
					pessoaDocumentoIdentificacao.setTipoDocumento(tipoDocumentoIdentificacao);
				}
				if(documento.getTipoDocumento() == ModalidadeDocumentoIdentificador.CMF) {
					codigoDocumento = InscricaoMFUtil.acrescentaMascaraMF(codigoDocumento);
				}
				String cnpjSemUpdate = ParametroUtil.getParametro("tjrj:mni:semUpdate:cnpj");
				if (cnpjSemUpdate == null || pessoa.getDocumentoCpfCnpj() == null || !cnpjSemUpdate.contains(pessoa.getDocumentoCpfCnpj().replace(".", "").replace("-", "").replace("/", ""))) {
					pessoaDocumentoIdentificacao.setDocumentoPrincipal(tipoDocumentoIdentificacao.getIdentificador());
					pessoaDocumentoIdentificacao.setNome(nomePessoaDocumentoIdentificacao);
					pessoaDocumentoIdentificacao.setNumeroDocumento(codigoDocumento);
					pessoaDocumentoIdentificacao.setOrgaoExpedidor(documento.getEmissorDocumento());
					pessoaDocumentoIdentificacao.setUsuarioCadastrador(obterUsuarioLogado());
					pessoaDocumentoIdentificacao.setAtivo(true);
					pessoaDocumentoIdentificacaoList.add(pessoaDocumentoIdentificacao);
				}
			}
		}
		return pessoaDocumentoIdentificacaoList;
	}
	
	/**
	 * Carrega as relações pessoais da pessoa
	 * @param relacionamentosPessoais
	 * @param pessoa
	 * @return
	 * @throws Exception
	 */
	private List<RelacaoPessoal> carregarRelacoesPessoais(List<br.jus.cnj.intercomunicacao.v222.beans.RelacionamentoPessoal> relacionamentosPessoais, Pessoa pessoa) {
		List<RelacaoPessoal> relacaoPessoalList = new ArrayList<RelacaoPessoal>();
		for (br.jus.cnj.intercomunicacao.v222.beans.RelacionamentoPessoal relacionamentoPessoal : relacionamentosPessoais) {
			RelacaoPessoal relacaoPessoal = new RelacaoPessoal();
			relacaoPessoal.setAtivo(true);
			relacaoPessoal.setPessoaRepresentada(pessoa);
			relacaoPessoal.setPessoaRepresentante(converter(relacionamentoPessoal.getPessoa()));
			relacaoPessoal.setDataInicioRelacao(new Date());
			relacaoPessoal.setTipoRelacaoPessoal(buscarTipoRelacaoPessoal(relacionamentoPessoal.getModalidadeRelacionamento(), 
					relacionamentoPessoal.getPessoa().getTipoPessoa()));
			relacaoPessoalList.add(relacaoPessoal);
		}
		return relacaoPessoalList;
	}
	
	/**
	 * Retorna o tipo de relação pessoal
	 * @param tipoRelacionamento
	 * @param tipoQualificacaoPessoa
	 * @return
	 */
	private TipoRelacaoPessoal buscarTipoRelacaoPessoal(ModalidadesRelacionamentoPessoal tipoRelacionamento, TipoQualificacaoPessoa tipoQualificacaoPessoa) {

		if(tipoRelacionamento == null) {
			throw new NegocioException("Não foi definido um tipo de relação pessoal.");
		}
		
		
		String codigo = null;
		TipoPessoaRelacaoEnum tipoPessoaRelacaoEnum = null;

		switch (tipoRelacionamento) {
		case P: // representação legal de ascendente (pais)
			codigo = "RLP";
			break;

		case AP: // assistência dos pais
			codigo = "ASP";
			break;

		case T: // tutoria
			codigo = "TUT";
			break;

		case C: // curadoria
			codigo = "CUR";
			break;
		
		case SP: //TODO VALIDAR
			break;
		
		default:
			break;
		}
		
		switch (tipoQualificacaoPessoa){
		case FISICA:
			tipoPessoaRelacaoEnum = TipoPessoaRelacaoEnum.F;
			break;
		case JURIDICA:
			tipoPessoaRelacaoEnum = TipoPessoaRelacaoEnum.J;
			break;
		case AUTORIDADE:
			tipoPessoaRelacaoEnum = TipoPessoaRelacaoEnum.A;
			break;
		case ORGAOREPRESENTACAO:
			tipoPessoaRelacaoEnum = TipoPessoaRelacaoEnum.J;
			break;
		}
		
		List<TipoPessoaRelacaoEnum> tiposPessoaRelacaoList = new ArrayList<TipoPessoaRelacaoEnum>();
		tiposPessoaRelacaoList.add(TipoPessoaRelacaoEnum.A);
		
		if(tipoPessoaRelacaoEnum != null){
			tiposPessoaRelacaoList.add(tipoPessoaRelacaoEnum);
		}
		
		TipoPessoaRelacaoEnum[] tiposPessoaRelacaoArray = new TipoPessoaRelacaoEnum[tiposPessoaRelacaoList.size()];

		List<TipoRelacaoPessoal> tiposRelacaoPessoal = tipoRelacaoPessoalManager.
				findByCodigoAndTipoPessoaRelacaoEnum(codigo, tiposPessoaRelacaoList.toArray(tiposPessoaRelacaoArray));
		
		TipoRelacaoPessoal tipoRelacaoPessoal = tiposRelacaoPessoal == null || tiposRelacaoPessoal.isEmpty() ? null :
			tiposRelacaoPessoal.get(0);
		
		if(tipoRelacaoPessoal == null){
			throw new NegocioException(String.format("Tipo de relação pessoal \"%s\" não cadastrado para o tipo de pessoa %s", 
					codigo, tipoPessoaRelacaoEnum.getLabel()));
		}

		return tipoRelacaoPessoal;
	}

	@SuppressWarnings("unchecked")
	private <T extends PessoaFisicaEspecializada> Pessoa especializa(br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoaMNI, Pessoa pessoa, Class<T> classe) throws PJeBusinessException {
		if (!(pessoa instanceof PessoaFisica)) {
			return pessoa;
		}
		if (((PessoaFisica) pessoa).getNumeroCPFAtivo() != null) {
			pessoa = pessoaService.especializa(pessoa, ((PessoaFisica) pessoa).getNumeroCPFAtivo(), classe);
		} else {
			pessoa = pessoaService.especializa(pessoa, classe);
		}
		
		if(classe.equals(PessoaAdvogado.class)){
			List<DadosAdvogadoOAB> dadosAdvogadoOABList = carregarDadosOAB(pessoaMNI);
			if(dadosAdvogadoOABList != null){
				for(DadosAdvogadoOAB dadosAdvogadoOAB : dadosAdvogadoOABList){
					pessoaService.adicionaInscricaoOAB((PessoaFisica)pessoa, dadosAdvogadoOAB);
				}
			}	
		}
		
		return pessoa;
	}

	/**
	 * Consulta os dados do advogado.
	 * 
	 * @param tipoPessoa
	 *            Pessoa
	 * @return Dados do advogado.
	 */
	private List<DadosAdvogadoOAB> carregarDadosOAB(br.jus.cnj.intercomunicacao.v222.beans.Pessoa tipoPessoa) {
		
		List<DadosAdvogadoOAB> resultado = null;
		
		if (ProjetoUtil.isNotVazio(tipoPessoa.getDocumento())) {
			DocumentoIdentificacao documentoCpf = obterDocumentoIdentificacao(tipoPessoa, ModalidadeDocumentoIdentificador.CMF);
			DocumentoIdentificacao documentoOab = obterDocumentoIdentificacao(tipoPessoa, ModalidadeDocumentoIdentificador.OAB);
			
			String cpf = (documentoCpf != null ? InscricaoMFUtil.retiraMascara(documentoCpf.getCodigoDocumento()): null);
			String oab = (documentoOab != null ? documentoOab.getCodigoDocumento(): null);
			String ufOab = StringUtils.substring(oab, 0, 2);
			String numeroOab = StringUtils.substring(oab, 2, 10);
			
			ConsultaClienteOAB consultaClienteOAB = new ConsultaClienteOAB();
			
			try{
				consultaClienteOAB.consultaDados(cpf, numeroOab, ufOab, false);
				resultado = consultaClienteOAB.getDadosAdvogadoList();
			} catch (Exception e) {
				throw new IntercomunicacaoException(String.format("Não foi possível carregar as informações de OAB do Advogado %s - %s.", tipoPessoa.getNome(), e.getMessage()));
			}
		} else {
			throw new NegocioException(String.format("Não foi informado documento para o advogado %s", tipoPessoa.getNome()));
		}
		
		return resultado;
	}


	/**
	 * Retorna o tipo de documento da pessoa.
	 * 
	 * @param pessoa
	 *            Pessoa
	 * @param modalidade
	 *            Tipo do documento (Ex: CMF, OAB etc)
	 * @return documento.
	 */
	private DocumentoIdentificacao obterDocumentoIdentificacao(br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoa, ModalidadeDocumentoIdentificador modalidade) {
		DocumentoIdentificacao resultado = null;
		
		if (pessoa != null) {
			Predicate filtro = novoFiltroDocumentoIdentificacao(modalidade);
			resultado = (DocumentoIdentificacao) CollectionUtils.find(pessoa.getDocumento(), filtro);
		}
		return resultado;
	}
	
	/**
	 * Retorna novo filtro de documento do tipo OAB.
	 * 
	 * @param modalidade
	 *            Tipo do documento (Ex: CMF, OAB etc)
	 * @return filtro de documento.
	 */
	private Predicate novoFiltroDocumentoIdentificacao(final ModalidadeDocumentoIdentificador modalidade) {
		return new Predicate() {
			
			@Override
			public boolean evaluate(Object object) {
				DocumentoIdentificacao documento = (DocumentoIdentificacao) object;
				return (documento.getTipoDocumento() == modalidade);
			}
		};
	}
	
	/**
	 * Valida o endereço da pessoa para a entrega de manifestação.
	 * As regras estão relacionadas na issue PJEII-19045.
	 * 
	 * @param polo PoloProcessual
	 * @param pessoa Pessoa
	 */
	private void validarEnderecoDePessoa(PoloProcessual polo, br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoaMni) {
		
		if (polo != null && pessoaMni != null) {
			List<br.jus.cnj.intercomunicacao.v222.beans.Endereco> enderecos = pessoaMni.getEndereco();
			boolean isPoloAtivo = (polo.getPolo() == ModalidadePoloProcessual.AT);
			
			//Alterado para atender RN354
			if (Authenticator.isUsuarioExterno() && isPoloAtivo && enderecos.isEmpty()) {
				String mensagem = "Selecione ao menos um endereço para a pessoa '%s'.";
				throw new NegocioException(String.format(mensagem, pessoaMni.getNome()));
			}
		}
	}
	
	/**
	 * Retorna novo filtro de endereço pelo CEP.
	 * 
	 * @param enderecoMNI
	 * @return Filtro
	 */
	private Predicate novoFiltroEnderecoPeloCep(final br.jus.cnj.intercomunicacao.v222.beans.Endereco enderecoMNI) {
		return new Predicate() {
			
			@Override
			public boolean evaluate(Object object) {
				Boolean resultado = Boolean.FALSE;
				if (enderecoMNI != null) {
					Endereco temp = (Endereco) object;
					String numeroCep = StringUtil.removeNaoNumericos(temp.getCep().getNumeroCep());
					String numeroCepMNI = StringUtil.removeNaoNumericos(enderecoMNI.getCep());
	
					resultado = StringUtils.equalsIgnoreCase(numeroCep, numeroCepMNI);
				}
				
				return resultado;
			}
		};
	}
	
	/**
	 * Carrega e persiste os nomes alternativos enviados via MNI para a pessoa do PJE.
	 * 
	 * @param pessoaMNI
	 * @param pessoa
	 * @return Coleção de PessoaNomeAlternativo.
	 * @throws PJeBusinessException
	 */
	private Collection<PessoaNomeAlternativo> carregarColecaoPessoaNomeAlternativo(
			br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoaMNI, Pessoa pessoa) throws PJeBusinessException {
		Collection<PessoaNomeAlternativo> resultado = new ArrayList<PessoaNomeAlternativo>();
		
		if (pessoaMNI != null && ProjetoUtil.isNotVazio(pessoaMNI.getOutroNome())) {
			Set<String> nomesExistentes = getColecaoNomeAlternativoExistente(pessoa);
			
			for (String nome : pessoaMNI.getOutroNome()) {
				if (!nomesExistentes.contains(nome)) {
					PessoaNomeAlternativo pessoaNomeAlternativo = new PessoaNomeAlternativo();
					pessoaNomeAlternativo.setPessoa(pessoa);
					pessoaNomeAlternativo.setPessoaNomeAlternativo(nome);
					pessoaNomeAlternativo.setUsuarioCadastrador(obterUsuarioLogado());
					
					pessoaNomeAlternativoManager.persist(pessoaNomeAlternativo);
					pessoa.getPessoaNomeAlternativoList().add(pessoaNomeAlternativo);
				}
			}
		}
		
		return resultado;
	}
	
	/**
	 * Retorna um TreeSet case insensitive dos nomes alternativos existentes para a pessoa
	 * passada por parâmetro.
	 * 
	 * @param pessoa Pessoa.
	 * @return TreeSet dos nomes existentes.
	 */
	@SuppressWarnings("unchecked")
	private Set<String> getColecaoNomeAlternativoExistente(Pessoa pessoa) {
		Set<String> resultado = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		Transformer conversorDePessoNomeAlternativoParaNome = new Transformer() {
			
			@Override
			public Object transform(Object objeto) {
				return (objeto != null ? ((PessoaNomeAlternativo) objeto).getPessoaNomeAlternativo() : null);
			}
		};
		List<PessoaNomeAlternativo> nomesExistentes = pessoa.getPessoaNomeAlternativoList();
		resultado.addAll(CollectionUtils.collect(nomesExistentes, conversorDePessoNomeAlternativoParaNome));
		return resultado;
	}
	

	/**
	 * Retorna true se existir o documento da lista de documentos.
	 * 
	 * @param documentos Lista de documento.
	 * @param documento Documento que será verificado.
	 * @return Boleano
	 */
	private static Boolean isContemDocumentoIdentificacao(Collection<PessoaDocumentoIdentificacao> documentos,
			PessoaDocumentoIdentificacao documento) {
		return CollectionUtils.exists(documentos, new Predicate() {

			@Override
			public boolean evaluate(Object object) {
				PessoaDocumentoIdentificacao documentoLista = (PessoaDocumentoIdentificacao) object;
				Integer id0 = (documento != null ? documento.getIdDocumentoIdentificacao() : null);
				Integer id1 = (documentoLista != null ? documentoLista.getIdDocumentoIdentificacao() : null);
				return (id0 != null && id1 != null ? id0.intValue() == id1.intValue() : false);
			}});
	}
}
