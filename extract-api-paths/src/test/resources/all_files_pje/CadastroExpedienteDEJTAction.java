package br.jus.csjt.pje.view.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.time.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.home.ProcessoParteExpedienteHome;
import br.com.infox.cliente.home.ProcessoParteHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.HistoricoDeslocamentoOrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.view.fluxo.ComunicacaoProcessualAction;
import br.jus.csjt.pje.commons.util.Base64;
import br.jus.csjt.pje.commons.util.dejt.AdvogadoType;
import br.jus.csjt.pje.commons.util.dejt.ItemMateriaType;
import br.jus.csjt.pje.commons.util.dejt.MateriaDejt;
import br.jus.csjt.pje.commons.util.dejt.MateriaType;
import br.jus.csjt.pje.commons.util.dejt.MateriasType;
import br.jus.csjt.pje.commons.util.dejt.ParteType;
import br.jus.pje.jt.entidades.HistoricoDeslocamentoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

/**
 * Componente Action usado para interface entre a View e a tela de cadastro de
 * expediente do DEJT.
 * 
 * @author David Vieira, Rafael Barros
 */
@Name(CadastroExpedienteDEJTAction.NAME)
@Scope(ScopeType.PAGE)
public class CadastroExpedienteDEJTAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2012345221342392106L;

	public static final String NAME = "cadastroExpedienteDEJTAction";

	private String xmlNaoAssinadoBase64;
	private String p7sBase64;

	private String loginDEJT;
	private String senhaDEJT;
	
	private boolean mostrarModalDejt = false;
	
	private final int TEXTO_ALINHAMENTO_JUSTIFICADO = 1;
	private final int TEXTO_TIPO_CONTEUDO = 2;
	private final int IND_IMPRIME = 1;

	@Logger
	private Log logger;
	
	@In(create = true, required = false)
	private ComunicacaoProcessualAction comunicacaoProcessualAction;
	
	@In(create = true, required = false)
	private ProcessoExpedienteManager processoExpedienteManager;

	private ProcessoExpediente expedienteSelecionadoNaoPublicado;
	private ProcessoExpediente expedienteSelecionadoPublicado;
	
	private Date dataConsulta;
	
	@In
	private HistoricoDeslocamentoOrgaoJulgadorManager historicoDeslocamentoOrgaoJulgadorManager;
	
	@In
	private UsuarioManager usuarioManager;
	
	@In
	private AtoComunicacaoService atoComunicacaoService;
	
	public void geraXMLDEJT() {

		mostrarModalDejt = true;
		
		ProcessoParteHome processoParteHome = ProcessoParteHome.instance();
		
		List<ProcessoParte> processosPartePoloAtivo = processoParteHome.obtemProcessoParte_Parte(ProcessoParteParticipacaoEnum.A);
		HashMap<ProcessoParte, List<ProcessoParte>> mapaPoloAtivo = new HashMap<ProcessoParte, List<ProcessoParte>>();
		
		for (ProcessoParte processoParte : processosPartePoloAtivo) {
			
			List<ProcessoParte> processoParteRepresentantesPoloAtivo = processoParteHome.obtemProcessoParte_Representante(processoParte);
			mapaPoloAtivo.put(processoParte, processoParteRepresentantesPoloAtivo);
		}
		
		List<ProcessoParte> processosPartePoloPassivo = processoParteHome.obtemProcessoParte_Parte(ProcessoParteParticipacaoEnum.P);
		HashMap<ProcessoParte, List<ProcessoParte>> mapaPoloPassivo = new HashMap<ProcessoParte, List<ProcessoParte>>();
		
		for (ProcessoParte processoParte : processosPartePoloPassivo) {
			
			List<ProcessoParte> processoParteRepresentantesPoloPassivo = processoParteHome.obtemProcessoParte_Representante(processoParte);
			mapaPoloPassivo.put(processoParte, processoParteRepresentantesPoloPassivo);
		}
		
		List<ProcessoParte> processosPartePoloOutros = processoParteHome.obtemProcessoParte_Parte(ProcessoParteParticipacaoEnum.T);
		HashMap<ProcessoParte, List<ProcessoParte>> mapaPoloOutros = new HashMap<ProcessoParte, List<ProcessoParte>>();
		
		for (ProcessoParte processoParte : processosPartePoloOutros) {
			
			List<ProcessoParte> processoParteRepresentantesPoloOutros = processoParteHome.obtemProcessoParte_Representante(processoParte);
			mapaPoloOutros.put(processoParte, processoParteRepresentantesPoloOutros);
		}
		
		
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		
		List<ProcessoExpediente> expedientesDiario = comunicacaoProcessualAction. getExpedientesDiario();
		
		//TODO: Usar Pojos como parametros
		xmlNaoAssinadoBase64 = geraXML(processoTrf, expedientesDiario, mapaPoloAtivo, mapaPoloPassivo, mapaPoloOutros);
		
		FacesMessages.instance().clear();
	}
	
	public void enviar(){
		
		
		try {
		
			String xmlNaoAssinado = null;
				
			xmlNaoAssinado = new String(Base64.decode(xmlNaoAssinadoBase64), "ISO-8859-1");
			
			byte[] xmlAssinado = Base64.decode(p7sBase64);
			
			String horarioLimiteEnvioDEJT = (String) ComponentUtil.getComponent("horarioLimiteEnvioDiario");
			
			Calendar dataEnvioUltimaMateria = Calendar.getInstance();
			Calendar dataPublicacao = verificaDataEnvio(dataEnvioUltimaMateria, horarioLimiteEnvioDEJT);
			
			Pessoa pessoaLogada = (Pessoa) ProcessoHome.instance().getUsuarioLogado();
			
			String loginIdLogado = loginDEJT + "," + pessoaLogada.getIdUsuario();
			
			comunicacaoProcessualAction.enviarDEJT(loginIdLogado, senhaDEJT, xmlNaoAssinado, xmlAssinado, dataPublicacao);
			
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Matéria publicada com sucesso.");
		
			mostrarModalDejt = false;
			limpar();


		} catch (PontoExtensaoException e) {

			e.printStackTrace();
			logger.error(e);
			FacesMessages.instance().clear();
			if (e.getMessage() != null)
				FacesMessages.instance().add(Severity.ERROR, e.getMessage());
			else
				FacesMessages.instance().add(Severity.ERROR,
						"Não foi possível enviar para publicação. Tente mais tarde.");
			
			mostrarModalDejt = false;
			limpar();
		
		} catch (UnsupportedEncodingException e) {
			
			logger.error("Erro converter formato Base 64.",e);
			
			mostrarModalDejt = false;
			limpar();
			
			throw new AplicationException("Erro converter formato Base 64.");
			
		} catch (Exception e){
			
			e.printStackTrace();
			logger.error(e);
			FacesMessages.instance().clear();
			if (e.getMessage() != null)
				FacesMessages.instance().add(Severity.ERROR, e.getMessage());
			else
				FacesMessages.instance().add(Severity.ERROR,
						"Não foi possível enviar para publicação. Tente mais tarde.");
		}
		
	}
	
	public void limpar(){
		
		xmlNaoAssinadoBase64 = null;
		p7sBase64 = null;

		loginDEJT = null;
		senhaDEJT = null;
	}

	public void setSenhaDEJT(String senhaDEJT) {
		this.senhaDEJT = senhaDEJT;
	}

	public String getSenhaDEJT() {
		return senhaDEJT;
	}

	public void setXmlNaoAssinadoBase64(String xmlNaoAssinadoBase64_p) {
		
		if(xmlNaoAssinadoBase64_p != null && !xmlNaoAssinadoBase64_p.equals("")){
			
			this.xmlNaoAssinadoBase64 = xmlNaoAssinadoBase64_p;
		}
	}

	public String getXmlNaoAssinadoBase64() {
		return xmlNaoAssinadoBase64;
	}

	public void setP7sBase64(String p7sBase64) {
		this.p7sBase64 = p7sBase64;
	}

	public String getP7sBase64() {
		return p7sBase64;
	}

	public void setLoginDEJT(String loginDEJT) {
		this.loginDEJT = loginDEJT;
	}

	public String getLoginDEJT() {
		return loginDEJT;
	}

	public boolean getMostrarModalDejt() {
		return mostrarModalDejt;
	}

	public void setMostrarModalDejt(boolean mostrarModalDejt) {
		this.mostrarModalDejt = mostrarModalDejt;
	}
	
	
	/**
	 * Verifica se o horario de envio é antes do horario definido no parametro.
	 * Caso sim, retorna a data atual, senão retorna o dia util seguinte
	 * 
	 * @return
	 * @author Frederico Carneiro
	 * @category PJE-JT
	 */
	public Calendar verificaDataEnvio(Calendar dataEnvioUltimaMateria, String horarioLimiteEnvioDEJT) {

		Calendar dataDEJT = (Calendar) dataEnvioUltimaMateria.clone();

		String[] horario = horarioLimiteEnvioDEJT.split(":");

		if (horario.length != 3) {
			FacesMessages.instance().clear();
			throw new AplicationException(
					"O valor do parametro horarioLimiteEnvioDEJT não está no padrão válido (HH:MM:SS).");
		}

		Calendar horaLimite = Calendar.getInstance();
		horaLimite.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horario[0]));
		horaLimite.set(Calendar.MINUTE, Integer.parseInt(horario[1]));
		horaLimite.set(Calendar.SECOND, Integer.parseInt(horario[2]));

		if (dataEnvioUltimaMateria.after(horaLimite)) {
			PrazosProcessuaisService prazosProcessuaisServiceImpl = ComponentUtil.getComponent("prazosProcessuaisService");

			Date dataEnvio = prazosProcessuaisServiceImpl.obtemDiaUtilSeguinte(dataEnvioUltimaMateria.getTime(),
					ProcessoTrfHome.instance().getInstance().getOrgaoJulgador(), false);

			dataDEJT.setTime(dataEnvio);
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			logger.info("Data de envio: " + formatter.format(dataDEJT.getTime()));
		}
		return dataDEJT;
	}
	

	/**
	 * Metodo responsável por gerar o xml a partir da arvore das partes da view
	 * 
	 * 
	 * @author Estevão Mognatto/Frederico Carneiro
	 * @since 1.4.0.3
	 * @category PJE-JT
	 * 
	 */
	public String geraXML(ProcessoTrf processoTrf, List<ProcessoExpediente> expedientesDiario, 
			HashMap<ProcessoParte, List<ProcessoParte>> processoParteComRepresentantes_poloAtivo,
			HashMap<ProcessoParte, List<ProcessoParte>> processoParteComRepresentantes_poloPassivo,
			HashMap<ProcessoParte, List<ProcessoParte>> processoParteComRepresentantes_poloOutros) {

		
		
		if (expedientesDiario == null) {
			FacesMessages.instance().clear();
			throw new AplicationException("O Expediente deve ter pelo menos um documento.");
		}

		MateriasType materiasType = MateriaDejt.criarMaterias();
		
		for (ProcessoExpediente processoExpediente : expedientesDiario) {
			
		
			TipoProcessoDocumento tipoProcessoDocumento = processoExpediente.getTipoProcessoDocumento();
			
			ProcessoDocumento processoDocumento = processoExpediente.getProcessoDocumento();
					
			ProcessoDocumentoBin processoDocumentoBin = obterProcessoDocumentoBin(processoDocumento);
	
			// Coloca o documento no xml como texto
			String processoDocumentoSerEnviado = "<![CDATA[" + processoDocumentoBin.getModeloDocumento() + "]]>";
	
			// Cria materia ser enviada
	
			String tituloMateria = tipoProcessoDocumento.getTipoProcessoDocumento();
			Integer tipoMateria = tipoProcessoDocumento.getCodigoMateria();
	
			if (tipoMateria == null || tipoMateria == 0) {
				FacesMessages.instance().clear();
				throw new AplicationException("O número do tipo de matéria deve estar cadastrado no sistema");
			}
	
			String unidadePublicadora = extrairSigla(processoTrf);
	
			if (unidadePublicadora == null || unidadePublicadora.equals("")) {
				FacesMessages.instance().clear();
				throw new AplicationException("A sigla do orgão julgador deve estar cadastrada no sistema");
			}
	
			Long codOrigemMateria = new Long(processoExpediente.getIdProcessoExpediente());
			
			MateriaType materia = MateriaDejt.criarMateria(String.valueOf(codOrigemMateria), String.valueOf(tipoMateria),
					tituloMateria, unidadePublicadora);
	
			// Criar Item Materia Texto
			ItemMateriaType itemMateriaTexto = MateriaDejt.criarItemMateriaTexto(TEXTO_ALINHAMENTO_JUSTIFICADO,
					TEXTO_TIPO_CONTEUDO, processoDocumentoSerEnviado);
	
			// Dados do processo
			String classeJudicial = processoTrf.getClasseJudicial().getClasseJudicialSigla();
			BigInteger ano = BigInteger.valueOf(processoTrf.getAno());
			/*
			 * PJE-JT: Ricardo Scholz : PJEII-2868 - 2012-10-17 Alteracoes feitas pela JT.
			 * Número da vara vem do ProcessoTrf e não mais do OrgaoJulgador, pois este
			 * é o OJ que está com o processo no momento, nas não necessariamente é o OJ
			 * originário.
			 */
			BigInteger numeroVara = BigInteger.valueOf(processoTrf.getNumeroOrigem());
			/*
			 * PJE-JT: Fim.
			 */
			BigInteger tribunalUnico = new BigInteger(processoTrf.getNumeroOrgaoJustica().toString().substring(1));
			BigInteger numeroUnico = BigInteger.valueOf(processoTrf.getNumeroSequencia());
			BigInteger digitoUnico = BigInteger.valueOf(processoTrf.getNumeroDigitoVerificador());
			String relator = "";
	
			PessoaMagistrado pessoaRelator = processoTrf.getPessoaRelator();
			if (pessoaRelator != null && pessoaRelator.getNome() != null) {
	
				relator = pessoaRelator.getNome();
			}
	
			ItemMateriaType itemMateriaProcesso = MateriaDejt.criarItemMateriaProcessoNumeracaoUnica(classeJudicial, ano,
					numeroVara, tribunalUnico, numeroUnico, digitoUnico, relator);
			itemMateriaProcesso.getProcessos().setIndImprime(IND_IMPRIME);
	
			// Extrai as partes das arvores de polo ativo, passivo e outros
			// participantes
			itemMateriaProcesso.getProcessos().getProcesso().get(0).getParte().addAll(obterPartes(processoParteComRepresentantes_poloAtivo));
			itemMateriaProcesso.getProcessos().getProcesso().get(0).getParte().addAll(obterPartes(processoParteComRepresentantes_poloPassivo));
			itemMateriaProcesso.getProcessos().getProcesso().get(0).getParte().addAll(obterPartes(processoParteComRepresentantes_poloOutros));
	
			materia.getItemMateria().add(itemMateriaProcesso);
			materia.getItemMateria().add(itemMateriaTexto);
			
			materiasType.getMateria().add(materia);
			
		
		}

		ByteArrayOutputStream saidaXml = new ByteArrayOutputStream();

		
		try {
			JAXBElement<MateriasType> materias = MateriaDejt.criarMateriasJaxbElement(materiasType);

			JAXBContext jc = JAXBContext.newInstance("br.jus.pje.jt.util.dejt");

			CharacterEscapeHandler noEscapeHandler = new CharacterEscapeHandler() {

				@Override
				public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
					out.write(ch, start, length);
				}

			};

			Marshaller m = jc.createMarshaller();

			m.setProperty(CharacterEscapeHandler.class.getName(), noEscapeHandler);
			m.setProperty("jaxb.noNamespaceSchemaLocation", "/dejt.xsd");
			m.setProperty("jaxb.encoding", "ISO-8859-1");

			m.marshal(materias, saidaXml);
		} catch (JAXBException jbe) {
			FacesMessages.instance().clear();
			logger.error("Erro ao converter Documento em XML para envio ao DEJT", jbe);
			throw new AplicationException("Erro ao converter Documento em XML para envio ao DEJT");
		}
		return  Base64.encodeBytes(saidaXml.toByteArray());
	}

	private String extrairSigla(ProcessoTrf processoTrf) {
		String sigla = null;
		OrgaoJulgador orgaoJulgador = processoTrf.getOrgaoJulgador();
		
		if(orgaoJulgador.getPostoAvancado() == null || orgaoJulgador.getPostoAvancado() == false) {
			sigla = orgaoJulgador.getSigla();
		} else {
			HistoricoDeslocamentoOrgaoJulgador ultimoDeslocamento = historicoDeslocamentoOrgaoJulgadorManager.obtemUltimoDeslocamento(processoTrf);
			OrgaoJulgador orgaoJulgadorOrigem = ultimoDeslocamento.getOrgaoJulgadorOrigem();
			sigla = orgaoJulgadorOrigem.getSigla();
		}
		
		return sigla;
	}
	
	/**
	 * Obtem o conteúdo de um documento partiular
	 * 
	 * 
	 * * @author Estevão Mognatto/Frederico Carneiro
	 * 
	 * @category PJE-JT
	 * @param processoDocumento
	 * */
	private ProcessoDocumentoBin obterProcessoDocumentoBin(ProcessoDocumento processoDocumento) {

		EntityManager entityManager = ProcessoDocumentoBinHome.instance().getEntityManager();
		
		Query q = entityManager.createQuery(
				"from ProcessoDocumento ss where ss.idProcessoDocumento = :idProcessoDocumento");
		q.setParameter("idProcessoDocumento", processoDocumento.getIdProcessoDocumento());

		ProcessoDocumento processoDocumentoQuery = (ProcessoDocumento) q.getSingleResult();

		ProcessoDocumentoBin processoDocumentoBin = processoDocumentoQuery.getProcessoDocumentoBin();

		return processoDocumentoBin;
	}
	
	/**
	 * Obtem partes dado o polo (ativo, passivo ou outros)
	 * 
	 * @author Estevão Mognatto/Frederico Carneiro
	 * @return ParteType
	 */
	private List<ParteType> obterPartes(HashMap<ProcessoParte, List<ProcessoParte>> processoParteComRepresentantes ) {

		List<ParteType> listaPartes = new ArrayList<ParteType>();
		
		
		for (ProcessoParte parte : processoParteComRepresentantes.keySet()) {
			
			ParteType parteType = null;
		
			List<ProcessoParte> representantes = processoParteComRepresentantes.get(parte);
			
			Boolean advogadoSelecionado = false;
			List<AdvogadoType> listaAdvogadosType = new ArrayList<AdvogadoType>();
			
			for (ProcessoParte representante : representantes) {
				
				// Verifica se o tipo da parte é advogado de acordo com o parametro do sistema
				
				if (representante.getTipoParte().getIdTipoParte() == Integer.parseInt((String) ComponentUtil.getComponent("idTipoParteAdvogado"))) {
					
						logger.info("Advogado selecionado: " + representante.getNomeParte() + " da parte " + representante.getNomeParte());
						advogadoSelecionado = true;
						AdvogadoType advogadoType = obtemAdvogado(representante);
						listaAdvogadosType.add(advogadoType);

				}
			}

			// Se um advogado for selecionado, envia os dados da parte e dos
			// advogados
			if (advogadoSelecionado) {
				logger.info("Gera xml da parte " + parte.getNomeParte() + " com advogados");
				String nomeParte = parte.getNomeParte();
				String tituloParte = parte.getTipoParte().getTipoParte();

				parteType = MateriaDejt.criarParteType(nomeParte, tituloParte, listaAdvogadosType);

				// Se nenhum advogado foi selecionado e a parte foi
				// selecionada, envia apenas a parte
			} else if (!advogadoSelecionado) {
				logger.info("Gera xml só da parte " + parte.getNomeParte());
				String nomeParte = parte.getNomeParte();
				String tituloParte = parte.getTipoParte().getTipoParte();

				parteType = MateriaDejt.criarParteType(nomeParte, tituloParte, listaAdvogadosType);

			}

			listaPartes.add(parteType);
		}

		return listaPartes;
	}
	
	/**
	 * Classe auxiliar responsável em transformar um tipo Processo Parte em um
	 * tipo AdvogadoType que representa um advogado em XML
	 * 
	 * @author Estevão Mognatto/Frederico Carneiro
	 * @since 1.4.0.3
	 * @category PJE-JT
	 * 
	 */
	private AdvogadoType obtemAdvogado(ProcessoParte advogado) {

		PessoaAdvogado pessoaAdvogado = ((PessoaFisica) advogado.getPessoa()).getPessoaAdvogado();

		/*
		 * PJE-JT: Ricardo Scholz : PJEII-3426 - 2012-12-04 Alteracoes feitas pela JT.
		 * Inclusão de '<![CDATA[...]]>' para isolar caracteres especiais nos nomes
		 * das partes e dos advogados.
		 */
		String nome_advogado = "<![CDATA[" + pessoaAdvogado.getNome() + "]]>";
		String titulo_advogado = "<![CDATA[" + advogado.getTipoParte().getTipoParte() + "]]>";
		/*
		 * PJE-JT: Fim.
		 */

		String numeroOAB = pessoaAdvogado.getNumeroOAB();

		AdvogadoType advogadoType = MateriaDejt.criarAdvogadotype(numeroOAB, nome_advogado, titulo_advogado);

		return advogadoType;

	}
	
	public List<ProcessoExpediente> getExpedientesNaoPublicados() {
	
		Pessoa pessoaLogada = (Pessoa) ProcessoHome.instance().getUsuarioLogado();
		
		Integer idUsuario = pessoaLogada.getIdUsuario();
		
		EntityManager entityManager = EntityUtil.getEntityManager();
		
		StringBuilder sqlConsulta = new StringBuilder();
		sqlConsulta.append("SELECT  id_processo_expediente ");
		sqlConsulta.append("FROM jt.tb_jt_mtra_diario_eletronico ");
		sqlConsulta.append("WHERE id_usuario = "+ idUsuario +" and dt_envio is not null and dt_disponibilizacao is null" +  "; ");
		
		
		
		Query query = entityManager.createNativeQuery( sqlConsulta.toString() );
		List<Integer> listaExpedientesNaoPublicados = ((List<Integer>) query.getResultList());
	    
		List<ProcessoExpediente> expedientesPublicados = new ArrayList<ProcessoExpediente>();
		
		if(processoExpedienteManager != null && listaExpedientesNaoPublicados.size() > 0){
			
			expedientesPublicados = processoExpedienteManager.findByIds(listaExpedientesNaoPublicados);
		}
		
		
	    return expedientesPublicados;
		
	}
	
	public List<ProcessoExpediente> getTodosExpedientesPublicados() {
		
		
		
		EntityManager entityManager = EntityUtil.getEntityManager();
		
		StringBuilder sqlConsulta = new StringBuilder();
		sqlConsulta.append("SELECT  id_processo_expediente ");
		sqlConsulta.append("FROM jt.tb_jt_mtra_diario_eletronico ");
		sqlConsulta.append("WHERE dt_envio is not null and dt_disponibilizacao is not null ");
		sqlConsulta.append("ORDER BY dt_disponibilizacao desc;");
		
		
		Query query = entityManager.createNativeQuery( sqlConsulta.toString() );
		List<Integer> listaExpedientesNaoPublicados = ((List<Integer>) query.getResultList());
	    
		List<ProcessoExpediente> expedientesPublicados = new ArrayList<ProcessoExpediente>();
		
		if(processoExpedienteManager != null && listaExpedientesNaoPublicados.size() > 0){
			
			expedientesPublicados = processoExpedienteManager.findByIds(listaExpedientesNaoPublicados);
		}
		
	    return expedientesPublicados;
		
	}
	
	
	public List<ProcessoExpediente> getTodosExpedientesNaoPublicados() {
		
		EntityManager entityManager = EntityUtil.getEntityManager();
		
		StringBuilder sqlConsulta = new StringBuilder();
		sqlConsulta.append("SELECT  id_processo_expediente ");
		sqlConsulta.append("FROM jt.tb_jt_mtra_diario_eletronico ");
		sqlConsulta.append("WHERE dt_envio is not null and dt_disponibilizacao is null" +  "; ");
		
		
		
		Query query = entityManager.createNativeQuery( sqlConsulta.toString() );
		List<Integer> listaExpedientesNaoPublicados = ((List<Integer>) query.getResultList());
	    
		List<ProcessoExpediente> expedientesPublicados = new ArrayList<ProcessoExpediente>();
		
		if(processoExpedienteManager != null && listaExpedientesNaoPublicados.size() > 0){
			
			expedientesPublicados = processoExpedienteManager.findByIds(listaExpedientesNaoPublicados);
		}
		
		
	    return expedientesPublicados;
		
	}
	
	public List<ProcessoExpediente> getExpedientesPublicados() {
		
		Pessoa pessoaLogada = (Pessoa) ProcessoHome.instance().getUsuarioLogado();
		
		Integer idUsuario = pessoaLogada.getIdUsuario();
		
		EntityManager entityManager = EntityUtil.getEntityManager();
		
		StringBuilder sqlConsulta = new StringBuilder();
		sqlConsulta.append("SELECT  id_processo_expediente ");
		sqlConsulta.append("FROM jt.tb_jt_mtra_diario_eletronico ");
		sqlConsulta.append("WHERE id_usuario = "+ idUsuario +" and dt_envio is not null and dt_disponibilizacao is not null ");
		sqlConsulta.append("ORDER BY dt_disponibilizacao desc;");
		
		
		Query query = entityManager.createNativeQuery( sqlConsulta.toString() );
		List<Integer> listaExpedientesNaoPublicados = ((List<Integer>) query.getResultList());
	    
		List<ProcessoExpediente> expedientesPublicados = new ArrayList<ProcessoExpediente>();
		
		if(processoExpedienteManager != null && listaExpedientesNaoPublicados.size() > 0){
			
			expedientesPublicados = processoExpedienteManager.findByIds(listaExpedientesNaoPublicados);
		}
		
	    return expedientesPublicados;
		
	}
	
	public String obterOrgaoJulgador(ProcessoExpediente processoExpediente){
	
		if(processoExpediente != null && processoExpediente.getProcessoTrf() != null 
		   && processoExpediente.getProcessoTrf().getOrgaoJulgador() != null ){
			
			return processoExpediente.getProcessoTrf().getOrgaoJulgador().getOrgaoJulgador();
		}
		
		return "";
	}
	
	public String obterNumeroTribunal(ProcessoExpediente processoExpediente){
		
		if(processoExpediente != null && processoExpediente.getProcessoTrf() != null 
		   && processoExpediente.getProcessoTrf().getOrgaoJulgador() != null 
		   && processoExpediente.getProcessoTrf().getNumeroOrgaoJustica() != null){
			
				return processoExpediente.getProcessoTrf().getNumeroOrgaoJustica().toString().substring(1);
		}
		
		return "";
	}
	
	public String obterUsuario(ProcessoExpediente processoExpediente){
		
		int idProcessoExpediente = processoExpediente.getIdProcessoExpediente();
		
		EntityManager entityManager = EntityUtil.getEntityManager();
		
		StringBuilder sqlConsulta = new StringBuilder();
		sqlConsulta.append("SELECT id_usuario ");
		sqlConsulta.append("FROM jt.tb_jt_mtra_diario_eletronico ");
		sqlConsulta.append("WHERE id_processo_expediente = "+ idProcessoExpediente +" and dt_envio is not null; ");
		
		System.out.println(sqlConsulta.toString());
		
		Query query = entityManager.createNativeQuery( sqlConsulta.toString() );
		List<Integer> resultList = (List<Integer>)query.getResultList();
		
		Integer idUsuario = null;
		
		for (Integer idUsuarioAux : resultList){
			
			idUsuario = idUsuarioAux;
		}
		
		String nomeUsuario = null;
	
		if(idUsuario != null){
			
			Usuario usuario;
			
			try{
				
				usuario = usuarioManager.findById(idUsuario);
				nomeUsuario = usuario.getNome(); 
				
			} catch (PJeBusinessException e){

				e.printStackTrace();
				
			}
			
		}
		
		return nomeUsuario;

	}
	
	public String obterDataEnvio(ProcessoExpediente processoExpediente){
	
		int idProcessoExpediente = processoExpediente.getIdProcessoExpediente();
		
		EntityManager entityManager = EntityUtil.getEntityManager();
		
		StringBuilder sqlConsulta = new StringBuilder();
		sqlConsulta.append("SELECT  dt_envio ");
		sqlConsulta.append("FROM jt.tb_jt_mtra_diario_eletronico ");
		sqlConsulta.append("WHERE id_processo_expediente = "+ idProcessoExpediente +" and dt_envio is not null; ");
		
		System.out.println(sqlConsulta.toString());
		
		Query query = entityManager.createNativeQuery( sqlConsulta.toString() );
		List<Date> resultList = (List<Date>)query.getResultList();
		
		Date dataEnvio = null;
		
		for (Date timestamp : resultList) {
			
			dataEnvio = timestamp;
		}
		
		return DateUtil.dateToString(dataEnvio, "dd/MM/yyyy HH:mm:ss");
		
	}
	
	public List<Date> obterDataEnvioTodosExpedientes(){
		
		EntityManager entityManager = EntityUtil.getEntityManager();
		
		StringBuilder sqlConsulta = new StringBuilder();
		sqlConsulta.append("SELECT  dt_envio ");
		sqlConsulta.append("FROM jt.tb_jt_mtra_diario_eletronico ");
		sqlConsulta.append("WHERE dt_envio is not null and dt_disponibilizacao is null; ");
		
		System.out.println(sqlConsulta.toString());
		
		Query query = entityManager.createNativeQuery( sqlConsulta.toString() );
		List<Date> resultList = (List<Date>)query.getResultList();
		
		return resultList;
		
	}
	
	public String obterDataDisponibilizacao(ProcessoExpediente processoExpediente){
		
		int idProcessoExpediente = processoExpediente.getIdProcessoExpediente();
		
		EntityManager entityManager = EntityUtil.getEntityManager();
		
		StringBuilder sqlConsulta = new StringBuilder();
		sqlConsulta.append("SELECT  dt_disponibilizacao ");
		sqlConsulta.append("FROM jt.tb_jt_mtra_diario_eletronico ");
		sqlConsulta.append("WHERE id_processo_expediente = "+ idProcessoExpediente +" and dt_envio is not null and dt_disponibilizacao is not null; ");
		
		System.out.println(sqlConsulta.toString());
		
		Query query = entityManager.createNativeQuery( sqlConsulta.toString() );
		List<Date> resultList = (List<Date>)query.getResultList();
		
		Date dataDisponibilizacao = null;
		
		for (Date timestamp : resultList) {
			
			dataDisponibilizacao = timestamp;
		}
		
		return DateUtil.dateToString(dataDisponibilizacao);
		
	}

	public Date getDataConsulta() {
		return dataConsulta;
	}

	public void setDataConsulta(Date dataConsulta) {
		this.dataConsulta = dataConsulta;
	}
	
	public String dateToString(Date data){
		
		return DateUtil.dateToString(data);
	}
	
	public void consultarPublicacoes(){
		
		try{
			
		if(dataConsulta == null){
			
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Por favor preencha a data de consulta.");
			
			return;
		}
		
		ProcessoParteExpedienteHome processoParteExpedienteHome = ComponentUtil.getComponent("processoParteExpedienteHome");
		
		processoParteExpedienteHome.consultarMateriasDisponibilizadasNoDia(dataConsulta, false);
		
		} catch (Exception e) {
			
			logger.error("Erro ao consultar matérias publicadas no DEJT para data: " + DateUtil.dateToString(dataConsulta), e);
			
			FacesMessages.instance().clear();
			if (e.getMessage() != null)
				FacesMessages.instance().add(Severity.ERROR, "Erro ao consultar matérias publicadas no DEJT para data: " + DateUtil.dateToString(dataConsulta) + 
											 " Detalhes: " + e.getMessage());
			else
				FacesMessages.instance().add(Severity.ERROR,
						"Erro ao consultar matérias publicadas no DEJT para data: " + DateUtil.dateToString(dataConsulta));
			
		}
		
	}
	
	public void consultarTodasPublicacoes(){
		
		Date diaPosteriorMaisUm = null;
		Date diaPosterior = null;
		Date diaAtual = null; 
		
		try{
			
			List<Date> datasDeEnvio = obterDataEnvioTodosExpedientes();
			
			ProcessoParteExpedienteHome processoParteExpedienteHome = ComponentUtil.getComponent("processoParteExpedienteHome");
			
			for (Date diaEnvio : datasDeEnvio){

				diaAtual = diaEnvio;
				diaPosterior = DateUtils.addDays(diaEnvio, 1);  
				diaPosteriorMaisUm = DateUtils.addDays(diaEnvio, 2);
				
				processoParteExpedienteHome.consultarMateriasDisponibilizadasNoDia(diaEnvio, false);
				processoParteExpedienteHome.consultarMateriasDisponibilizadasNoDia(diaPosterior, false);
				processoParteExpedienteHome.consultarMateriasDisponibilizadasNoDia(diaPosteriorMaisUm, false);
			}
		
		} catch (Exception e) {
			
			logger.error("Erro ao consultar matérias publicadas no DEJT para as datas: " + DateUtil.dateToString(diaPosteriorMaisUm) + " , " + DateUtil.dateToString(diaAtual) + " , " + DateUtil.dateToString(diaPosterior), e);
			
			FacesMessages.instance().clear();
			if (e.getMessage() != null)
				FacesMessages.instance().add(Severity.ERROR, "Erro ao consultar matérias publicadas no DEJT para as datas: " + DateUtil.dateToString(diaPosteriorMaisUm) + " , " + DateUtil.dateToString(diaAtual) + " , " + DateUtil.dateToString(diaPosterior) + 
											 " Detalhes: " + e.getMessage());
			else
				FacesMessages.instance().add(Severity.ERROR,
						"Erro ao consultar matérias publicadas no DEJT para as datas: " + DateUtil.dateToString(diaPosteriorMaisUm) + " , " + DateUtil.dateToString(diaAtual) + " , " + DateUtil.dateToString(diaPosterior));
			
		}
		
	}
	
	public String obterUltimaDataExecucaoJob(){
		Date dataUltimaExecucaoJob = null;
		try{			
			dataUltimaExecucaoJob = this.atoComunicacaoService.getUltimaDataJobDiario();
		} catch (Exception e) {
			logger.error("Erro ao consultar matérias publicadas no DEJT para data: " + DateUtil.dateToString(dataConsulta), e);
			
			FacesMessages.instance().clear();
			if (e.getMessage() != null)
				FacesMessages.instance().add(Severity.ERROR, "Erro ao consultar matérias publicadas no DEJT para data: " + DateUtil.dateToString(dataConsulta) + 
											 " Detalhes: " + e.getMessage());
			else
				FacesMessages.instance().add(Severity.ERROR,
						"Erro ao consultar matérias publicadas no DEJT para data: " + DateUtil.dateToString(dataConsulta));
			
		}
		
		return  DateUtil.dateToString(dataUltimaExecucaoJob);
		
	}

	public ProcessoExpediente getExpedienteSelecionadoNaoPublicado() {
		return expedienteSelecionadoNaoPublicado;
	}

	public void setExpedienteSelecionadoNaoPublicado(
			ProcessoExpediente expedienteSelecionadoNaoPublicado) {
		this.expedienteSelecionadoNaoPublicado = expedienteSelecionadoNaoPublicado;
	}

	public ProcessoExpediente getExpedienteSelecionadoPublicado() {
		return expedienteSelecionadoPublicado;
	}

	public void setExpedienteSelecionadoPublicado(
			ProcessoExpediente expedienteSelecionadoPublicado) {
		this.expedienteSelecionadoPublicado = expedienteSelecionadoPublicado;
	}

	public ProcessoTrf getProcessoTrf() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		return processoTrf;
	}
}
