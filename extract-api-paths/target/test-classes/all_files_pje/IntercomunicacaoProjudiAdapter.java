package br.jus.cnj.pje.intercomunicacao.v222.servico;

import java.io.File;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.pje.manager.PessoaProcuradoriaEntidadeManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.FileUtil;
import br.com.jt.pje.manager.SalaManager;
import br.jus.cnj.intercomunicacao.v222.beans.AssuntoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.AudienciaProjudi;
import br.jus.cnj.intercomunicacao.v222.beans.CabecalhoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.CadastroIdentificador;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.Endereco;
import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeRepresentanteProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.MovimentacaoProcessualProjudi;
import br.jus.cnj.intercomunicacao.v222.beans.NumeroUnico;
import br.jus.cnj.intercomunicacao.v222.beans.Parametro;
import br.jus.cnj.intercomunicacao.v222.beans.Parte;
import br.jus.cnj.intercomunicacao.v222.beans.PoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.RepresentanteProcessual;
import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.v222.converter.PessoaMNIParaPessoaPJEConverter;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIParametroUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AssuntoTrfManager;
import br.jus.cnj.pje.nucleo.manager.CepManager;
import br.jus.cnj.pje.nucleo.manager.ClasseAplicacaoManager;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.CompetenciaManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.cnj.pje.nucleo.manager.JurisdicaoManager;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.TipoAudienciaManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.service.CertificadoDigitalService;
import br.jus.cnj.pje.nucleo.service.PessoaFisicaService;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.ClasseAplicacao;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.CompetenciaClasseAssunto;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.TipoAudiencia;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.StatusAudienciaEnum;

@Name(IntercomunicacaoProjudiAdapter.NAME)
public class IntercomunicacaoProjudiAdapter implements
		ManifestacaoProcessualHandler {

	public static final String NAME = "v222.intercomunicacaoProjudiAdapter";

	@Logger
	private Log log;

	// utilizado para buscar outros parametros MNI
	public static final String NOME_PARAMETRO_MOVIMENTACAO = "movimentacao";
	public static final String NOME_PARAMETRO_AUDIENCIA = "audiencia";
	public static final String NOME_PARAM_CPF_USUARIO_MOVIMENTO = "cpf_usuario_movimento";
	public static final String NOME_PARAMETRO_NOME_USUARIO_MOVIMENTO = "nome_usuario_movimento";

	// parametro de sistema
	public static final String NOME_PARAMETRO_SISTEMA_DIRETORIO_MIGRACAO_PROJUDI = "diretorioDocumentosMigracaoProjudi";
	public static final String NOME_PARAMETRO_SISTEMA_ID_PROCURADORIA_DEFENSORIA_PUBLICA = "idDefensoriaPublica";

	@In
	private PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager;

	@In
	private ProcessoJudicialManager processoJudicialManager;

	@In
	private ProcessoDocumentoManager processoDocumentoManager;

	@In("eventoManager")
	private EventoManager eventoManager;
	
	@In
	private ProcessoEventoManager processoEventoManager;

	@In
	private ProcessoTrfManager processoTrfManager;

	@In
	private CepManager cepManager;

	@In
	private MunicipioManager municipioManager;

	@In
	private EstadoManager estadoManager;

	@In
	private CertificadoDigitalService certificadoDigitalService;

	@In
	private DocumentoBinManager documentoBinManager;

	@In
	private ProcessoDocumentoBinPessoaAssinaturaManager processoDocumentoBinPessoaAssinaturaManager;

	@In
	private SalaManager salaManager;

	@In
	private TipoAudienciaManager tipoAudienciaManager;

	@In
	private CompetenciaManager competenciaManager;

	@In
	private ClasseJudicialManager classeJudicialManager;

	@In
	private AssuntoTrfManager assuntoTrfManager;

	@In
	private ClasseAplicacaoManager classeAplicacaoManager;

	@In
	private IntercomunicacaoService intercomunicacaoService;
	
	@In
	private PessoaFisicaService pessoaFisicaService;
	
	@In
	private PessoaProcuradoriaEntidadeManager pessoaProcuradoriaEntidadeManager;
	
	@In
	private ProcuradoriaManager procuradoriaManager;
	
	@In
	private PessoaMNIParaPessoaPJEConverter pessoaMNIParaPessoaPJEConverter;
	
	@In
	private UsuarioManager usuarioManager;
	
	@In
	private JurisdicaoManager jurisdicaoManager;

	@Override
	public void onBeforeEntregarManifestacaoProcessual(
			ManifestacaoProcessual manifestacaoProcessual) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Validando CEP...");
			}
			
			CabecalhoProcessual cabecalhoProcessual = manifestacaoProcessual.getDadosBasicos();
			NumeroUnico numeroUnico = cabecalhoProcessual.getNumero();
			
			List<ProcessoTrf> processoList = processoJudicialManager.findByNU(numeroUnico.getValue());
			
			if (!processoList.isEmpty()){
				throw new NegocioException(String.format("O processo de nº %s já existe.", numeroUnico.getValue()));
			}

			Contexts.getSessionContext().set(MNIParametro.PARAM_CPF_CNPJ_USUARIO, MNIParametroUtil.obterValor(manifestacaoProcessual, MNIParametro.PARAM_CPF_CNPJ_USUARIO));
			// cadastrar ceps inválidos e tratar defensoria publica
			for (PoloProcessual polo : manifestacaoProcessual.getDadosBasicos()
					.getPolo()) {
				for (Parte parte : polo.getParte()) {
					
					// das partes
					mergeCep(parte.getPessoa().getEndereco());
					
					// dos representantes != null) {
					List<RepresentanteProcessual> representantesParaRemover = new ArrayList<RepresentanteProcessual>(0);
					for (RepresentanteProcessual advogado : parte.getAdvogado()) {
						mergeCep(advogado.getEndereco());
						
						//tratar defensoria publica
						if(advogado.getTipoRepresentante() == ModalidadeRepresentanteProcessual.D){
							
							List<Procuradoria> defensoriaList = procuradoriaManager.getlistDefensorias();
							
							if (defensoriaList.isEmpty()){
								throw new IntercomunicacaoException("Não existe(m) Defensoria(s) cadastrada(s) ou ativa(s).");
							}
							
							Procuradoria defensoriaPublica = defensoriaList.get(0);
							
							PessoaProcuradoriaEntidade representado = new PessoaProcuradoriaEntidade();
							
							//carregar representado
							Pessoa pessoa = pessoaMNIParaPessoaPJEConverter.converter(parte.getPessoa());
							
							//vinculo da pessoa MNI com pessoa CNJ (login == documento principal)
							CadastroIdentificador cadastroIdentificador = new CadastroIdentificador();
							cadastroIdentificador.setValue(!StringUtils.isBlank(pessoa.getDocumentoCpfCnpj()) ? pessoa.getDocumentoCpfCnpj() : pessoa.getLogin());
							parte.getPessoa().setNumeroDocumentoPrincipal(cadastroIdentificador);
							
							List<Pessoa> representados = procuradoriaManager.getPessoasRepresentadas(defensoriaPublica);
							
							//adicionar a pessoa como representada pela defensoria
							if(representados != null && !representados.contains(pessoa)){
								representado.setPessoa(pessoa);
								representado.setProcuradoria(defensoriaPublica);
								
								pessoaProcuradoriaEntidadeManager.persist(representado);
							}
							
							representantesParaRemover.add(advogado);
						}
						
					}
					parte.getAdvogado().removeAll(representantesParaRemover);					

					// das pessoas relacionadas
					if (parte.getPessoaProcessualRelacionada() != null) {
						for (Parte pessoaRelacionada : parte
								.getPessoaProcessualRelacionada()) {
							mergeCep(pessoaRelacionada.getPessoa()
									.getEndereco());
						}
					}

				}
			}

			/*
			 * [PJEII-6222] atualizar as competencias
			 */
			if (manifestacaoProcessual == null
					|| manifestacaoProcessual.getDadosBasicos() == null
					|| !manifestacaoProcessual.getDadosBasicos()
							.isSetCompetencia()) {
				throw new IntercomunicacaoException(
						"Identificador da Competência não informado");
			}

			int idCompetencia = manifestacaoProcessual.getDadosBasicos()
					.getCompetencia();

			if (log.isDebugEnabled()) {
				log.debug("Verificando Competência");
			}
			
			for (DocumentoProcessual documentoProcessual : manifestacaoProcessual.getDocumento()) {				
				File documento = getDocumentoMigracao(manifestacaoProcessual.getDadosBasicos().getNumero().getValue(), documentoProcessual.getIdDocumento());
				if(documento != null){
					FileDataSource rawData = new FileDataSource(documento);                      	
	                DataHandler data= new DataHandler(rawData);
	                documentoProcessual.setConteudo(data);
				}
			}
			intercomunicacaoService
					.validarManifestacaoProcessual(manifestacaoProcessual);

			// Carregar entidades do PJe
			Competencia competencia = null;
			ClasseJudicial classeJudicial = null;
			List<AssuntoTrf> assuntoTrfList = null;

			// classe
			classeJudicial = classeJudicialManager.findByCodigo(String
					.valueOf(manifestacaoProcessual.getDadosBasicos()
							.getClasseProcessual()));

			// assunto
			String jurisdicaoNumeroOrigem = manifestacaoProcessual.getDadosBasicos().getCodigoLocalidade();
			Jurisdicao jurisdicao = jurisdicaoManager.obterPorNumeroOrigem(jurisdicaoNumeroOrigem);
			Integer idJurisdicao = (jurisdicao != null ? jurisdicao.getIdJurisdicao() : null);

			List<AssuntoProcessual> assuntoProcessualList = manifestacaoProcessual
					.getDadosBasicos().getAssunto();
			List<String> codigoAssuntoList = new ArrayList<String>(
					assuntoProcessualList.size());

			assuntoTrfList = new ArrayList<AssuntoTrf>(0);
			if (assuntoProcessualList != null) {
				for (AssuntoProcessual assuntoProcessual : assuntoProcessualList) {
					String codigo = String.valueOf(assuntoProcessual
							.getCodigoNacional());
					codigoAssuntoList.add(codigo);
					assuntoTrfList.add(assuntoTrfManager.findByCodigo(Integer
							.parseInt(codigo)));
				}
			}

			// competencia
			try {
				competencia = competenciaManager.getCompetencia(idJurisdicao,
						classeJudicial.getIdClasseJudicial(), assuntoTrfList,
						idCompetencia);
			} catch (PJeBusinessException e) {
				// combinação de competencia x classe x assuntos não
				// encontrada -> atualizar a competencia;
				competencia = competenciaManager.findById(idCompetencia);

				if (competencia == null) {
					throw new IntercomunicacaoException("Competência de id = "
							+ idCompetencia + " não cadastrada!");
				}

				// verificar quais os assuntos que não estão na competencia
				List<AssuntoTrf> assuntosNaoCadastrados = new ArrayList<AssuntoTrf>(
						assuntoTrfList);

				for (CompetenciaClasseAssunto competenciaClasseAssunto : competencia
						.getCompetenciaClasseAssuntoList()) {
					AssuntoTrf assuntoCompetencia = competenciaClasseAssunto
							.getAssuntoTrf();
					ClasseJudicial classeCompetencia = competenciaClasseAssunto
							.getClasseAplicacao().getClasseJudicial();

					if (assuntoTrfList.contains(assuntoCompetencia)
							&& classeCompetencia.equals(classeJudicial)) {
						assuntosNaoCadastrados.remove(assuntoCompetencia);
					}
				}

				// atualizar a competencia
				List<ClasseAplicacao> classeAplicacaoList = classeJudicial
						.getClasseAplicacaoList();

				if (classeAplicacaoList == null
						|| classeAplicacaoList.isEmpty()) {
					ClasseAplicacao classeAplicacao = new ClasseAplicacao();
					classeAplicacao.setAplicacaoClasse(ParametroUtil.instance()
							.getAplicacaoSistema());
					classeAplicacao.setAtivo(true);
					classeAplicacao.setClasseJudicial(classeJudicial);
					classeAplicacao.setDistribuicaoAutomatica(!ParametroUtil
							.instance().getDistribuicaoManual());

					classeAplicacaoManager.persist(classeAplicacao);
					classeAplicacaoList.add(classeAplicacao);
				}

				for (ClasseAplicacao classeAplicacao : classeAplicacaoList) {
					for (AssuntoTrf assuntoNaoCadastrado : assuntosNaoCadastrados) {
						CompetenciaClasseAssunto competenciaClasseAssunto = new CompetenciaClasseAssunto();
						competenciaClasseAssunto
								.setAssuntoTrf(assuntoNaoCadastrado);
						competenciaClasseAssunto.setCompetencia(competencia);
						competenciaClasseAssunto.setDataInicio(new Date());

						competenciaClasseAssunto
								.setClasseAplicacao(classeAplicacao);

						competencia.getCompetenciaClasseAssuntoList().add(
								competenciaClasseAssunto);
					}
				}

				competenciaManager.persistAndFlush(competencia);

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Caso o CEP não seja nulo, e não exista no banco do PJe, será realizada a busca do CEP padrão da cidade, 
	 * caso não encontre, será inserido o cep invalido. Caso o CEP seja vazio(99999-999) será gerado um cep unico invalido 
	 * para o municipio, composto pelo id do municipio e completado por 9 a direita
	 * @param endereco
	 * @throws PJeBusinessException
	 */
	private void mergeCep(List<Endereco> enderecos) {
		String cep_nulo = "99999999";
		String cepInvalido = null;
		try {			
			for (Endereco endereco : enderecos) {
				Cep cep = cepManager.findByCep(endereco.getCep());				
				if (cep == null) {
					Municipio municipio = getMunicipio(endereco.getCidade(), endereco.getEstado());
					if (municipio == null) {
						throw new IllegalArgumentException("Município não encontrado: " + endereco.getCidade());
					}
					cep = obterCepDefaultByMunicipio(municipio.getIdMunicipio());
					if(cep == null && cep_nulo.equals(endereco.getCep())){
						cepInvalido = StringUtils.rightPad(String.valueOf(municipio.getIdMunicipio()), 8, "9"); 
						cep = cepManager.findByCep(cepInvalido);
					}
					if(cep == null){
						String cepFormatado = null;
						cep = new Cep();
						cep.setAtivo(true);
						cep.setComplemento("Inexistente");
						cep.setNomeBairro("Inexistente");
						cep.setNomeLogradouro("Inexistente");
									
						if (endereco.getCep().length() == 8) {
							cepFormatado = endereco.getCep().substring(0, 5) + "-" + endereco.getCep().substring(5);
						}
						if(cep_nulo.equals(endereco.getCep())) {
							cep.setNumeroCep(cepInvalido.substring(0, 5) + "-" + cepInvalido.substring(5));
						} else {
							cep.setNumeroCep(cepFormatado);
						}
						cep.setMunicipio(municipio);
						if (log.isDebugEnabled()) {
							log.debug("Cadastrando CEP invalido: " + endereco.getCep());
						}			
						cep = cepManager.persist(cep);					
					}
					if (cep == null) {
						throw new IllegalArgumentException("Cep por município não encontrado: " + endereco.getCidade());						
					}						
				}
				endereco.setCep(cep.getNumeroCep());				
			}
		} catch (PJeBusinessException e) {
			throw new RuntimeException("Erro ao migrar cep", e);
		}
	}
	
	private Cep obterCepDefaultByMunicipio(int IdMunicipio) {				
		List<Cep> listaCep = cepManager.getCepDefaultByIdMunicipio(IdMunicipio);
		if(listaCep != null && listaCep.size() > 0) {
			return listaCep.get(0);
		}
		return null;
	}

	private Municipio getMunicipio(String dsMunicipio, String dsEstado)
			throws PJeBusinessException {
		// carregar os estados
		List<Estado> estados = estadoManager.estadoItems();

		Estado estado = null;
		for (Estado item : estados) {
			if (item.getEstado().equalsIgnoreCase(dsEstado)) {
				estado = item;
				break;
			}
		}

		if (estado != null) {
			// carregar os municípios do estado
			List<Municipio> municipios = municipioManager.findByUf(estado
					.getCodEstado());

			for (Municipio item : municipios) {
				if (item.getMunicipio().equalsIgnoreCase(dsMunicipio)) {
					return item;
				}
			}
		} else {
			throw new IllegalArgumentException("Estado não encontrado: "
					+ dsEstado);
		}
		return null;
	}

	@Override
	public void onAfterEntregarManifestacaoProcessual(ManifestacaoProcessual manifestacaoProcessual, ProcessoTrf processoTrf, ProcessoDocumento documentoPrincipal) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Salvando Movimentacoes... ");
			}
			processoTrf = processoTrfManager.find(ProcessoTrf.class,
					processoTrf.getIdProcessoTrf());
			// Salvar as movimentações
			for (Parametro parametro : manifestacaoProcessual.getParametros()) {
				if (parametro.getNome().equals(NOME_PARAMETRO_MOVIMENTACAO)) {
					List<MovimentacaoProcessualProjudi> movimentacoes = parse(
							parametro.getValor(),
							MovimentacaoProcessualProjudi.class);
					int numeroMovimentacao = 1;
					for (MovimentacaoProcessualProjudi movimentoProjudi : movimentacoes) {
						salvarMovimentacao(manifestacaoProcessual,
								movimentoProjudi, processoTrf, numeroMovimentacao);
						numeroMovimentacao++;
					}
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("Persist movimentações...");
			}

			if (log.isDebugEnabled()) {
				log.debug("Salvando Audiencias... ");
			}

			// Salvar as audiencias
			for (Parametro parametro : manifestacaoProcessual.getParametros()) {
				if (parametro.getNome().equals(NOME_PARAMETRO_AUDIENCIA)) {
					List<AudienciaProjudi> audiencias = parse(
							parametro.getValor(), AudienciaProjudi.class);
					for (AudienciaProjudi audienciaProjudi : audiencias) {
						salvarAudiencia(manifestacaoProcessual,
								audienciaProjudi, processoTrf);
					}
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("Persist Audiencias...");
			}

			if (log.isDebugEnabled()) {
				log.debug("Migrando Documentos...");
			}
			
			corrigirAutorDocumento(manifestacaoProcessual, processoTrf);

			if (log.isDebugEnabled()) {
				log.debug("Persist Documentos...");
			}
			
			Competencia competencia = competenciaManager.findById(manifestacaoProcessual.getDadosBasicos().getCompetencia());
			processoTrf.setCompetencia(competencia);

			processoTrfManager.persist(processoTrf);
			atualizaProcessoParteRepresentadaDefensoria(processoTrf, manifestacaoProcessual);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		finally{
			excluirArquivosTemporarios(manifestacaoProcessual.getDadosBasicos().getNumero().getValue());
		}
	}
	
	private void atualizaProcessoParteRepresentadaDefensoria(ProcessoTrf processoTrf, ManifestacaoProcessual manifestacaoProcessual) {
		
		List<ProcessoParte> processoParteList  = processoTrf.getProcessoParteSemAdvogadoList();
		for(ProcessoParte processoParte : processoParteList){
			if (processoParte.getProcuradoria() == null){
				List<ProcessoParteRepresentante> processoParteRepresentante = processoParte.getProcessoParteRepresentanteList();
				if (processoParteRepresentante == null || processoParteRepresentante.isEmpty()){
					PessoaProcuradoriaEntidade defensoria = pessoaProcuradoriaEntidadeManager.getPessoaProcuradoriaEntidade(processoParte.getPessoa());
					if (defensoria != null){
						processoParte.setProcuradoria(defensoria.getProcuradoria());
					}
				}
			}
		}
		processoTrfManager.update(processoTrf);
	}

	private void corrigirAutorDocumento(ManifestacaoProcessual manifestacaoProcessual, ProcessoTrf processoTrf) throws Exception {
		HashMap<String, Usuario> listaPessoasNovas = new HashMap<String, Usuario>();		
		for (ProcessoDocumento processoDocumento : processoTrf.getProcesso().getProcessoDocumentoList()) {			
			DocumentoProcessual documentoMNI = getDocumentoMNI(manifestacaoProcessual, String.valueOf(processoDocumento.getIdInstanciaOrigem()));
			String cpfUsuarioInclusao = MNIParametroUtil.obterValor(documentoMNI, NOME_PARAM_CPF_USUARIO_MOVIMENTO);
			String nomeUsuarioInclusao = MNIParametroUtil.obterValor(documentoMNI,NOME_PARAMETRO_NOME_USUARIO_MOVIMENTO);

			Usuario usuario = listaPessoasNovas.get(cpfUsuarioInclusao);			
			if(usuario == null){
				usuario = usuarioManager.findByLogin(cpfUsuarioInclusao);				
			}			
			if(usuario == null){				
				usuario = criarPessoaFisica(manifestacaoProcessual, nomeUsuarioInclusao, cpfUsuarioInclusao);				
				listaPessoasNovas.put(cpfUsuarioInclusao, usuario);				
			}
			
			processoDocumento.setUsuarioInclusao(usuario);
			processoDocumento.setNomeUsuarioInclusao(usuario.getNome());
			processoDocumento.setUsuarioAlteracao(usuario);
			processoDocumento.setNomeUsuarioAlteracao(usuario.getNome());
			processoDocumento.setIdInstanciaOrigem(null);
			processoDocumentoManager.merge(processoDocumento);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> List<T> parse(String xml, Class<T> clazz) throws Exception {
		// Substituir os códigos html
		xml = xml.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
				.replaceAll("&quot;", "\"");

		// remover as tags root
		String openTag = xml.substring(0, xml.indexOf(">") + 1);
		String closeTag = openTag.replace("<", "</");

		xml = xml.replaceAll(openTag, "").replaceAll(closeTag, "");

		// delimitar cada nó principal do xml
		String decapitalizedClassName = clazz.getSimpleName().substring(0, 1)
				.toLowerCase()
				+ clazz.getSimpleName().substring(1);

		String delimiter = "</" + decapitalizedClassName + ">";

		if (xml.indexOf(delimiter) < 0) {
			delimiter = "/>";
		}

		// converter os itens string em objeto da classe "clazz"
		String[] items = xml.split(delimiter);

		List<T> returnValue = new ArrayList<T>(0);

		for (String item : items) {
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			StringReader reader = new StringReader(item + delimiter);
			T object = (T) jaxbUnmarshaller.unmarshal(reader);
			reader.close();

			returnValue.add(object);
		}

		return returnValue;
	}

	private void salvarMovimentacao(
			ManifestacaoProcessual manifestacaoProcessual,
			MovimentacaoProcessualProjudi movimentacaoProcessualProjudi,
			ProcessoTrf processoTrf, int numeroMovimentacao) throws Exception {

		try {
			ProcessoEvento processoEvento = new ProcessoEvento();

			processoEvento.setDataAtualizacao(new SimpleDateFormat(
					MNIParametro.PARAM_FORMATO_DATA_HORA)
					.parse(movimentacaoProcessualProjudi.getDataHora()
							.getValue()));
			processoEvento.setDescricaoEvento(movimentacaoProcessualProjudi
					.getMovimentoLocal());

			List<Evento> eventos = eventoManager.findByIds(
					String.valueOf(movimentacaoProcessualProjudi
							.getMovimentoNacional().getCodigoNacional()));
			
			if(eventos == null || eventos.isEmpty()){
				throw new IntercomunicacaoException("Movimento nacional de código " + movimentacaoProcessualProjudi
						.getMovimentoNacional().getCodigoNacional() + " inválido, verifique o SGT");
			}
			
			Evento evento = eventos.get(0);
			processoEvento.setEvento(evento);

			processoEvento.setProcessado(true);
			processoEvento.setProcesso(processoTrf.getProcesso());
			processoEvento.setDescricaoEvento(movimentacaoProcessualProjudi
					.getMovimentoLocal());
			processoEvento.setVisibilidadeExterna(movimentacaoProcessualProjudi
					.getNivelSigilo() == 0);
			processoEvento.setAtivo(movimentacaoProcessualProjudi.getAtivo());

			/*
			 * Usuario usuario = pessoaDocumentoIdentificacaoManager
			 * .findByNumeroDocumento(
			 * movimentacaoProcessualProjudi.getIdentificacaoUsuario())
			 * .get(0).getPessoa();
			 */
			// processoEvento.setUsuario(usuario);
			processoEvento.setCpfUsuario(movimentacaoProcessualProjudi
					.getIdentificacaoUsuario());
			// processoEvento.setNomeUsuario(usuario.getNome());

			for (String complemento : movimentacaoProcessualProjudi
					.getComplemento()) {
				processoEvento.setDescricaoEvento(processoEvento
						.getDescricaoEvento() + "/" + complemento);
			}

			if (processoEvento.getDescricaoEvento() != null) {
				processoEvento.setDescricaoEvento("Mov. [" + numeroMovimentacao + "] - " + processoEvento
						.getDescricaoEvento().replaceFirst("/", ": "));
				processoEvento.setTextoFinalExterno(processoEvento
						.getDescricaoEvento());
				processoEvento.setTextoFinalInterno(processoEvento
						.getDescricaoEvento());

				// vinculacao do evento com o documento
				if (movimentacaoProcessualProjudi.getIdDocumentoVinculado() == null
						|| !movimentacaoProcessualProjudi
								.getIdDocumentoVinculado().isEmpty()) {
					if (movimentacaoProcessualProjudi.getIdDocumentoVinculado()
							.get(0) != null
							&& !movimentacaoProcessualProjudi
									.getIdDocumentoVinculado().get(0).trim()
									.isEmpty()) {

						List<ProcessoDocumento> processoDocumentoList = processoDocumentoManager
								.getDocumentosPorNumero(
										processoTrf.getIdProcessoTrf(),
										movimentacaoProcessualProjudi
												.getIdDocumentoVinculado().get(
														0));

						if (processoDocumentoList != null
								&& !processoDocumentoList.isEmpty()) {
							ProcessoDocumento documentoPrincipal = processoDocumentoList
									.get(0);
							processoEvento
									.setProcessoDocumento(documentoPrincipal);

							// atualizar os documentos vinculados
							for (int i = 1; i < movimentacaoProcessualProjudi
									.getIdDocumentoVinculado().size(); i++) {
								List<ProcessoDocumento> processoDocumentoVinculadoList = processoDocumentoManager
										.getDocumentosPorNumero(
												processoTrf.getIdProcessoTrf(),
												movimentacaoProcessualProjudi
														.getIdDocumentoVinculado()
														.get(i));
								if (processoDocumentoVinculadoList != null
										&& !processoDocumentoVinculadoList
												.isEmpty()) {
									ProcessoDocumento documentoVinculado = processoDocumentoVinculadoList
											.get(0);
									if (documentoVinculado != null
											&& !documentoVinculado
													.equals(documentoPrincipal)) {
										documentoVinculado
												.setDocumentoPrincipal(documentoPrincipal);
									}
								} else {
									log.warn("documento "
											+ movimentacaoProcessualProjudi
													.getIdDocumentoVinculado()
													.get(i)
											+ " não encontrado. Vinculado ao movimento "
											+ movimentacaoProcessualProjudi
													.getMovimentoLocal());
								}

							}
						}

					}
				}
				processoEvento.setVerificadoProcessado(true);

				processoEventoManager.persist(processoEvento);
				
				
			}
		} catch (Exception e) {
			throw new IntercomunicacaoException("Erro ao salvar movimentações",
					e);
		}

	}

	private void salvarAudiencia(ManifestacaoProcessual manifestacaoProcessual,
			AudienciaProjudi audienciaProjudi, ProcessoTrf processoTrf)
			throws Exception {
		try {
			Sala sala = null;
			PessoaFisica realizador = null;
			PessoaFisica conciliador = null;
			TipoAudiencia tipoAudiencia = null;
			Date dataInicio = null;
			Date dataFim = null;
			StatusAudienciaEnum status = null;

			Pattern cpfPattern = Pattern
					.compile("[0-9]{2,3}?\\.[0-9]{3}?\\.[0-9]{3}?\\-[0-9]{2}?");

			if (audienciaProjudi.getCpfRealizador() != null) {
				String cpf = audienciaProjudi.getCpfRealizador();

				if (!cpfPattern.matcher(cpf).matches()) {
					cpf = formataCPF(cpf);
				}

				realizador = pessoaFisicaService.findByCPF(cpf, MNIParametroUtil.obterValor(manifestacaoProcessual, MNIParametro.PARAM_CPF_CNPJ_USUARIO), true);				
				if(realizador != null && realizador.getIdPessoa() == null)
					pessoaFisicaService.persist(realizador);
										
				if(realizador == null)
					throw new IntercomunicacaoException("Cpf " + audienciaProjudi.getCpfRealizador() + " não cadastrado");
			}			

			if (audienciaProjudi.getCpfConciliador() != null) {
				String cpf = audienciaProjudi.getCpfConciliador();

				if (!cpfPattern.matcher(cpf).matches()) {
					cpf = formataCPF(cpf);
				}
				
				conciliador = pessoaFisicaService.findByCPF(cpf, MNIParametroUtil.obterValor(manifestacaoProcessual, MNIParametro.PARAM_CPF_CNPJ_USUARIO), true);				
				if(conciliador != null && conciliador.getIdPessoa() == null)
					pessoaFisicaService.persist(conciliador);
										
				if(conciliador == null)
					throw new IntercomunicacaoException("Cpf " + audienciaProjudi.getCpfConciliador() + " não cadastrado");
								
			}

			if (audienciaProjudi.getIdentificadorTipoAudiencia() == null) {
				throw new IntercomunicacaoException(
						"Identificador do Tipo de Audiência não informado");
			}

			tipoAudiencia = tipoAudienciaManager.findById(audienciaProjudi
					.getIdentificadorTipoAudiencia().intValue());
			if (tipoAudiencia == null) {
				throw new IntercomunicacaoException(
						"Tipo de Audiência de identificador "
								+ audienciaProjudi
										.getIdentificadorTipoAudiencia()
								+ " não encontrado");
			}

			if (audienciaProjudi.getDataInicio() == null
					|| audienciaProjudi.getDataInicio().getValue().isEmpty()) {
				throw new IntercomunicacaoException(
						"Hora inicial dda audiência não informada");
			}

			dataInicio = new SimpleDateFormat(
					MNIParametro.PARAM_FORMATO_DATA_HORA)
					.parse(audienciaProjudi.getDataInicio().getValue());

			if (audienciaProjudi.getDataFim() == null
					|| audienciaProjudi.getDataFim().getValue().isEmpty()) {
				throw new IntercomunicacaoException(
						"Hora final da audiência não informada");
			}

			dataFim = new SimpleDateFormat(
					MNIParametro.PARAM_FORMATO_DATA_HORA)
					.parse(audienciaProjudi.getDataFim().getValue());

			if (audienciaProjudi.getStatus() != null) {
				switch (audienciaProjudi.getStatus()) {
				case CONVERTIDA_EM_DILIGENCIA:
					status = StatusAudienciaEnum.D;
					break;

				case CANCELADA:
					status = StatusAudienciaEnum.C;
					break;

				case DESIGNADA:
					status = StatusAudienciaEnum.M;
					break;

				case NAO_REALIZADA:
					status = StatusAudienciaEnum.N;
					break;

				case REALIZADA:
					status = StatusAudienciaEnum.F;
					break;

				case REDESIGNADA:
					status = StatusAudienciaEnum.R;
					break;

				default:
					throw new IntercomunicacaoException(
							"Status de Audiência inválido");

				}

			}

			// recuperar salas para o período solicitado
			List<Sala> salasPossiveis = salaManager
					.getSalaByPeriodoAudienciaAndTipoAudiencia(dataInicio,
							dataFim, tipoAudiencia,
							processoTrf.getOrgaoJulgador());

			// caso nao existam salas disponiveis mostrar aviso de erro (salas de audiencia devem ser criadas manualmente)
			if (salasPossiveis != null && !salasPossiveis.isEmpty()) {
				sala = salasPossiveis.get(0);
			} else {
				throw new IntercomunicacaoException("Não existem salas disponiveis");
			}

			ProcessoAudiencia processoAudiencia = new ProcessoAudiencia();

			tipoAudiencia.setIdTipoAudiencia(audienciaProjudi
					.getIdentificadorTipoAudiencia().intValue());

			processoAudiencia.setTipoAudiencia(tipoAudiencia);
			processoAudiencia.setSalaAudiencia(sala);
			processoAudiencia.setDtMarcacao(dataInicio);
			processoAudiencia.setDtInicio(dataInicio);
			processoAudiencia.setDtFim(dataFim);
			processoAudiencia.setStatusAudiencia(status);
			processoAudiencia.setProcessoTrf(processoTrf);
			processoAudiencia.setPessoaConciliador((PessoaFisica) conciliador);
			// designação manual
			processoAudiencia.setTipoDesignacao("M");
			processoAudiencia.setPessoaRealizador(realizador);

			processoTrf.getProcessoAudienciaList().add(processoAudiencia);
			
			processoJudicialManager.merge(processoTrf);
		} catch (Exception e) {
			throw new IntercomunicacaoException(e.getMessage());
		}
	}

	private String formataCPF(String cpf) {
		if (cpf != null && cpf.length() == 11) {
			cpf = cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
					+ cpf.substring(6, 9) + "-" + cpf.substring(9);
		}
		return cpf;
	}
	
	/**
	 * Exclui os arquivos temporarios que foram enviados pelo ftp
	 * Nesse momento os arquivos já foram persistidos no dbstorage ou JCR.
	 * @param numeroProcesso
	 */
	private void excluirArquivosTemporarios(String numeroProcesso) {
		File directory = new File(getPathDocumentosProjudi() + "/" + numeroProcesso);
		FileUtil.deleteDir(directory);
	}

	public String encodeMD5(byte[] bytes) {
		StringBuffer resp = new StringBuffer();
		if (bytes != null) {
			try {
				MessageDigest digest = MessageDigest.getInstance("MD5");
				byte[] hash = digest.digest(bytes);
				for (int i = 0; i < hash.length; i++) {
					if ((hash[i] & 0xff) < 0x10) {
						resp.append("0");
					}
					resp.append(Long.toString(hash[i] & 0xff, 16));
				}
			} catch (NoSuchAlgorithmException err) {
				err.printStackTrace(System.err);
				// Nunca deve ocorrer.
			}
		}
		return resp.toString();
	}
	
	private DocumentoProcessual getDocumentoMNI(
			ManifestacaoProcessual manifestacaoProcessual, String idDocumento)
			throws IntercomunicacaoException {

		for (DocumentoProcessual documento : manifestacaoProcessual
				.getDocumento()) {
			if (documento.getIdDocumento().equals(idDocumento)) {
				return documento;
			}
		}
		throw new IntercomunicacaoException("Documento " + idDocumento
				+ " não encontrado");
	}

	private File getDocumentoMigracao(String numeroProcesso, String idDocumento) {
		File directory = new File(getPathDocumentosProjudi() + "/" + numeroProcesso);
		
		if (directory != null && directory.isDirectory()) {
		
			for (File file : directory.listFiles()) {
				if (file.getName().equals(idDocumento)) {
					return file;
				}
			}
		}
		return null;
	}

	private String getPathDocumentosProjudi() {
		String path = null;
		try {
			path = ParametroUtil
					.getParametro(NOME_PARAMETRO_SISTEMA_DIRETORIO_MIGRACAO_PROJUDI);
		} catch (Exception e) {
			;
		}

		if (path == null) {
			path = System.getProperty("jboss.server.data.dir");
			log.warn("Parâmetro de Sistema \""
					+ NOME_PARAMETRO_SISTEMA_DIRETORIO_MIGRACAO_PROJUDI
					+ "\" não configurado, os documentos serão buscados a partir diretório "
					+ path);
		}

		return path;
	}
	
	private Usuario criarPessoaFisica(ManifestacaoProcessual manifestacaoProcessual, String nomePessoa, String cpf) throws Exception{
		PessoaFisica pessoaMovimentacao = pessoaFisicaService.findByCPF(cpf, MNIParametroUtil.obterValor(manifestacaoProcessual, MNIParametro.PARAM_CPF_CNPJ_USUARIO), true);
		
		if(pessoaMovimentacao != null){
			if(pessoaMovimentacao.getIdPessoa() == null){
				return pessoaFisicaService.persist(pessoaMovimentacao);
			} else {
				return pessoaMovimentacao;
			}
		} else {
			return criaUsuario(nomePessoa, cpf);
		}
	}
	
	private Usuario criaUsuario(String nomePessoa, String cpf) throws PJeBusinessException {
		String login = StringUtils.isBlank(cpf) ? nomePessoa : cpf;
		Usuario usuarioMovimentacao = usuarioManager.findByLogin(login);
		
		if(usuarioMovimentacao == null){
			usuarioMovimentacao = new Usuario();
			usuarioMovimentacao.setLogin(login);
			usuarioMovimentacao.setNome(nomePessoa);
			usuarioMovimentacao = usuarioManager.persist(usuarioMovimentacao);
			
			usuarioMovimentacao.setAtivo(false);
			usuarioMovimentacao.setBloqueio(true);
			
			//Necessário devido ao método usuarioManager.persist conter valores predefinidos para novos registros.
			usuarioMovimentacao = usuarioManager.merge(usuarioMovimentacao);
		}
		
		return usuarioMovimentacao;
	}
}
