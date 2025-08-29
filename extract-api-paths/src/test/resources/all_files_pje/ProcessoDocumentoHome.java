package br.com.infox.cliente.home;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.hibernate.AssertionFailure;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.bpm.ProcessInstance;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.SeamResourceBundle;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.actions.anexarDocumentos.AnexarDocumentos;
import br.com.infox.cliente.component.ValidacaoAssinaturaProcessoDocumento;
import br.com.infox.cliente.exception.ExclusaoDocumentoException;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.infox.ibpm.component.tree.EventosTreeHandler;
import br.com.infox.ibpm.component.tree.EventsTipoDocumentoTreeHandler;
import br.com.infox.ibpm.home.AbstractProcessoDocumentoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ParametroHome;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.home.api.IProcessoDocumentoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.pje.webservice.consultaoutrasessao.EncryptionSecurity;
import br.com.itx.component.Util;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.component.grid.ProcessoDocumentoGridQuery;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.csjt.pje.business.pdf.GeradorPdfUnificado;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.jt.entidades.ControleVersaoDocumento;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogadoLocal;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoriaLocal;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoPapel;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.util.StringUtil;


@Install(precedence = Install.APPLICATION)
@Name(ProcessoDocumentoHome.NAME)
public class ProcessoDocumentoHome extends AbstractProcessoDocumentoHome<ProcessoDocumento> implements
		IProcessoDocumentoHome {

	

	private static final long serialVersionUID = -6317758464581746307L;
	
	public static final String NAME = "processoDocumentoHome";
	public static final String PETICAO_INSERIDA = "peticaoInseridaMap";
	public static final String CONSULTA_NUMERO_DOCUMENTO_INVALIDO = "pje.consulta.numeroDocumento.invalido";
	private static final LogProvider log = Logging.getLogProvider(ProcessoDocumentoHome.class);
	private boolean isTrue = Boolean.FALSE;
	private boolean apreciado = Boolean.FALSE;
	private boolean rendBtn = Boolean.FALSE;
	private TipoProcessoDocumento tipoDocumentoAcordao;
	private TipoProcessoDocumento tipoDocumentoEmenta;
	private TipoProcessoDocumento tipoDocumentoNotasOrais;
	private Boolean checkBox;
	private Boolean checkAllSigilo = Boolean.FALSE;
	private List<ProcessoDocumento> listaSigilo = new ArrayList<ProcessoDocumento>(0);
	private String modeloDocumento;
	private ModeloDocumento modeloDocumentoLocalTemp;
	private Boolean tipoDocAgrupamento = Boolean.FALSE;
	private boolean mostrarModalMotivo = false;
	// Lista contendo os Documentos que serao selecionados na impressao em pdf
	private List<ProcessoDocumento> processoDocumentoBeanList;
	// Lista contendo os Documentos selecionados na impressao em pdf
	private List<ProcessoDocumento> processoDocumentoBeanListSelecionados = new ArrayList<ProcessoDocumento>();
	private boolean haDocSigiloso = false;
	private boolean haDocSemAssinar = false;
	// ordem da impressao de documentos em pdf
	private Boolean crescente = false;
	// flag para mostrar ou não o modal de impressao de documentos em pdf
	private Boolean showModalDownloadDocumentos = false;
	private boolean adicionarModeloPeticaoNewInstance = true;
	private Map<Integer, Boolean> validaDocumentoDetalheMap;
	private Map<Integer, Boolean> mapaPendenciaCiencia;

	// Campo de consulta de documento
	private String codigoDocumento;
	private Integer numeroDocumento;
	private Integer idProcessoDocumentoBin;
	private String numeroDocumentoStorage;
	private String nomeArquivo;
	private Integer size;
	private boolean binario;
	private Integer idProcessoDoc;
	
	private int idDocumentoDestacar;
	
	// Variável destinada a armazenar a mensagem que será exibida ao usuário quando do download dos documentos do processo em PDF.
	private String msgAlert;

	private List<ProcessoDocumento> documentosFiltrados;

	public Integer getIdProcessoDocumentoBin() {
		return idProcessoDocumentoBin;
	}

	public Integer getIdProcessoDocumento() {
		return this.idProcessoDocumentoBin;
	}

	public void setIdProcessoDocumentoBin(Integer idProcessoDocumentoBin) {
		this.idProcessoDocumentoBin = idProcessoDocumentoBin;
	}
	
	public String getNumeroDocumentoStorage() {
		return numeroDocumentoStorage;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public Integer getSize() {
		return size;
	}
	
	public boolean isBinario() {
		return binario;
	}
	
	public Integer getIdProcessoDoc() {
		return idProcessoDoc;
	}

	public String getMsgAlert() {
		return msgAlert;
	}

	public String getConsultaDocumentosProcesso() {
		return "select o from ProcessoDocumento o  "
				+ "		   	where  "
				+ "		   	 "
				+ "				   	(( (lower(o.processoDocumentoBin.extensao) = '.pdf' or lower(o.processoDocumentoBin.extensao) = 'pdf') "
				+ "					 and (o in (select ass.documentoAssociado from ProcessoDocumentoAssociacao ass "
				+ "						        where ass.documentoAssociado = o)  "
				+ "					      and o.processoDocumentoBin in (select pdbpa.processoDocumentoBin from ProcessoDocumentoBinPessoaAssinatura pdbpa "
				+ "												         where pdbpa.processoDocumentoBin = o.processoDocumentoBin)) "
				+ "					 or (o not in (select ass.documentoAssociado from ProcessoDocumentoAssociacao ass "
				+ "					               where ass.documentoAssociado = o)))  "
				+ "					 or o.processoDocumentoBin.extensao is null)  "
				+ "					and "
				+ "		   	 "
				+ "		   			(o.tipoProcessoDocumento in (select tipopapel.tipoProcessoDocumento from  "
				+ " "
				+ " "
				+ "											   TipoProcessoDocumentoPapel tipopapel,  "
				+ "											   ProcessoDocumento doc, "
				+ "											   ProcessoDocumentoBin bin  "
				+ " "
				+ "												where tipopapel.tipoProcessoDocumento = doc.tipoProcessoDocumento "
				+ "												and (bin.signature is not null and bin.signature != '') "
				+ "												and (bin.certChain is not null and bin.certChain != '') "
				+ " "
				+ "												and bin = o.processoDocumentoBin) "
				+ "										   or "
				+ "										   	  (o in (select pdt from ProcessoDocumentoTrfLocal pdt "
				+ "										   	  								where pdt.exibirDocMinuta = true) "
				+ "										   	  	and (#{identificadorPapelAtual.equals('dirSecretaria')} = true "
				+ "										   	  		or #{authenticator.isMagistrado()} = true "
				+ "										   	  		or #{identificadorPapelAtual.equals('assessor')} = true)) "
				+ "										   or "
				+ "											  (o.usuarioInclusao.idUsuario = #{processoDocumentoHome.usuarioLogado.idUsuario}) "
				+ " "
				+ " "
				+ " "
				+ " "
				+ "										   or "
				+ "											  (o.usuarioInclusao.idUsuario in (select local.usuario.idUsuario from UsuarioLocalizacao local "
				+ " "
				+ " "
				+ " "
				+ " "
				+ "																				where local.papel.identificador in ('servidor', "
				+ "																													'oficial de justica', "
				+ "																													'conciliador', "
				+ "																													'dirSecretaria')) "
				+ "																				and "
				+ "																				#{authenticator.isUsuarioInterno()} = true) "
				+ "										   or "
				+ "											  (o.usuarioInclusao.idUsuario in (select local.usuario.idUsuario from UsuarioLocalizacao local "
				+ "																				where local.papel.identificador in ('advogado', "
				+ "																												    'procurador', "
				+ "																												    'perito')) "
				+ "																				and "
				+ "																				(o.tipoProcessoDocumento in (select tipopapel.tipoProcessoDocumento from  "
				+ "																											TipoProcessoDocumentoPapel tipopapel,  "
				+ "																											ProcessoDocumento doc, "
				+ "																											ProcessoDocumentoBin bin  "
				+ "																											where tipopapel.tipoProcessoDocumento = doc.tipoProcessoDocumento "
				+ "																											and (bin.signature is not null and bin.signature != '') "
				+ "																											and (bin.certChain is not null and bin.certChain != '') "
				+ "																											and bin = o.processoDocumentoBin))) "
				+ "										   or "
				+ "											  (o.usuarioInclusao.idUsuario in (select local.usuario.idUsuario from UsuarioLocalizacao local "
				+ "																			   where local.papel.identificador in ('magistrado')) "
				+ "																			   and (#{identificadorPapelAtual.equals('Asses')} = true)) "
				+ "			)	 "
				+ "			and ((o.documentoSigiloso = true and exists(select 1 from ProcessoDocumentoVisibilidadeSegredo pdvs  "
				+ "														where pdvs.processoDocumento.idProcessoDocumento = o.idProcessoDocumento "
				+ "														and pdvs.pessoa.idUsuario = #{processoDocumentoHome.usuarioLogado.idUsuario})  "
				+ "									         or #{authenticator.isMagistrado()} = true "
				+ "									         or (o.usuarioInclusao.idUsuario = #{usuarioLogado.idUsuario})) "
				+ "				 or o.documentoSigiloso = false)	 "
				+ "			and not exists  "
				+ "			(select pda from ProcessoDocumentoAssociacao pda "
				+ "				   where (pda.processoDocumento.processoDocumentoBin.certChain is null or pda.processoDocumento.processoDocumentoBin.certChain = '') "
				+ "					 and (pda.processoDocumento.processoDocumentoBin.signature is null or pda.processoDocumento.processoDocumentoBin.signature = '') "
				+ "					 and pda.documentoAssociado = o) "
				+ "			and o.idProcessoDocumento not in (select spd.idSessaoProcessoDocumento from SessaoProcessoDocumento spd  "
				+ "											  inner join spd.processoDocumento pd "
				+ "											  where pd.processo.idProcesso = #{processoHome.instance.idProcesso} "
				+ "											  and not exists (select o from SessaoProcessoDocumento o  "
				+ "													   		  where o.processoDocumento.tipoProcessoDocumento.idTipoProcessoDocumento = #{parametroUtil.getTipoProcessoDocumentoAcordao().getIdTipoProcessoDocumento()} "
				+ "													      	  and o.processoDocumento.processo.idProcesso = #{processoHome.instance.idProcesso} "
				+ "													      	  and o.processoDocumento.processoDocumentoBin.idProcessoDocumentoBin in (select pdbpa.processoDocumentoBin.idProcessoDocumentoBin from ProcessoDocumentoBinPessoaAssinatura pdbpa)) "
				+ "											  ) "
				+ "			and o.tipoProcessoDocumento.idTipoProcessoDocumento != #{parametroUtil.getTipoProcessoDocumentoEmenta().getIdTipoProcessoDocumento()}";
	}

	public Integer getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(Integer numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public Boolean getRendBtn() {
		return rendBtn;
	}

	public void setRendBtn(Boolean rendBtn) {
		this.rendBtn = rendBtn;
	}

	public int getIdDocumentoDestacar() {
		return idDocumentoDestacar;
	}

	public void setIdDocumentoDestacar(int idDocumentoDestacar,Boolean setarInstancia){
		if (idDocumentoDestacar != 0 && setarInstancia) {
			setId(idDocumentoDestacar);
		}
		this.idDocumentoDestacar = idDocumentoDestacar;
	}
	
	public void setIdDocumentoDestacar(int idDocumentoDestacar) {
		setIdDocumentoDestacar(idDocumentoDestacar,true);
	}

	public boolean isLinhaSelecionada(ProcessoDocumento pd) {
		return pd.getIdProcessoDocumento() == getIdDocumentoDestacar();
	}

	public void limparProcessoDocumentoDestacado() {
		ProcessoTrfHome.instance().setarInstancia();
		setIdDocumentoDestacar(0);
	}

	/**
	 * método criado para retornar a data formatada
	 * 
	 * @return
	 */
	public String dataInclusaoFormatada() {
		if (getInstance().getProcessoDocumentoBin() != null
				&& getInstance().getProcessoDocumentoBin().getDataInclusao() != null) {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

			return df.format(getInstance().getProcessoDocumentoBin()
					.getDataInclusao());
		}
		return "Ocorreu erro na data!";
	}

	public String dataJuntadaFormatada() {
		if (getInstance().getDataJuntada() != null) {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			return df.format(getInstance().getDataJuntada());
		}
		return "Ocorreu erro na data!";
	}

	public void processarModeloDocumento() {
		if (modeloDocumentoLocalTemp != null) {
			setModeloDocumentoCombo(EntityUtil.find(ModeloDocumento.class,
					modeloDocumentoLocalTemp.getIdModeloDocumento()));
			processarModelo();
		} else {
			ComponentUtil.getComponent(ProcessoDocumentoBinHome.class).getInstance().setModeloDocumento("");
		}
	}

	public void obtemModeloDocumento() {

		if (modeloDocumentoLocalTemp != null) {
			setModeloDocumentoCombo(EntityUtil.find(ModeloDocumento.class,
					modeloDocumentoLocalTemp.getIdModeloDocumento()));

			if (getModeloDocumentoCombo() != null) {
				ModeloDocumento modeloDocumento = getEntityManager().merge(
						getModeloDocumentoCombo());
				ComponentUtil.getComponent(ProcessoDocumentoBinHome.class).getInstance().setModeloDocumento(
						modeloDocumento.getModeloDocumento());
				ProcessoHome
						.instance()
						.getProcessoDocumentoBin()
						.setModeloDocumento(
								modeloDocumento.getModeloDocumento());
			}

		} else {
			ComponentUtil.getComponent(ProcessoDocumentoBinHome.class).getInstance().setModeloDocumento("");
		}

	}

	public String getEntityProcessoDocumento(Integer idProcessoDocumento) {
		ProcessoDocumento doc;
		if (idProcessoDocumento != 0) {
			doc = getEntityManager().find(ProcessoDocumento.class,
					idProcessoDocumento);
			return doc.getTipoProcessoDocumento().getTipoProcessoDocumento();
		}
		return null;
	}

	public String getProcessoDocUsuarioInclusao(Integer idProcessoDocumento) {
		if (idProcessoDocumento != 0) {
			ProcessoDocumento doc = getEntityManager().find(
					ProcessoDocumento.class, idProcessoDocumento);
			if (doc.getUsuarioInclusao() != null) {
				return doc.getUsuarioInclusao().getNome();
			} else {
				return "";
			}
		}
		return null;
	}

	public void setIsTrue(boolean isTrue) {
		this.isTrue = isTrue;
	}

	public Boolean getIsTrue() {
		return isTrue;
	}

	public static ProcessoDocumentoHome instance() {
		return ComponentUtil.getComponent(ProcessoDocumentoHome.class);
	}

	public void setModelo(Boolean isModelo, boolean isNewInstance) {
		if (isNewInstance) {
			newInstance();
		}
		setModelo(isModelo);
	}

	@Override
	public void newInstance() {
		super.newInstance();
		if (getInstance().getIdProcessoDocumento() == 0) {
			afterNewInstance();
		}
	}

	public void verificaDocumento() {
		ProcessoDocumentoHome.instance().setId(null);
		ProcessoDocumentoBinHome.instance().setId(null);
		ProcessoTrf trf = ProcessoTrfHome.instance().getInstance();
		Processo processo = trf.getProcesso();
		// [PJEII-1243] Padronizado para recuperar o id do TipoProcessoDocumento
		// configurado na tabela de parâmetros como Petição inicial
		int idTipoProcessoDocumentoPeticaoInicial = trf.getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento();
		if (processo.getProcessoDocumentoList().size() > 0) {
			for (int i = 0; i < processo.getProcessoDocumentoList().size(); i++) {
				Integer id = processo.getProcessoDocumentoList().get(i)
						.getIdProcessoDocumento();
				ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class,
						id);
				if (pd.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == idTipoProcessoDocumentoPeticaoInicial) {
					setInstance(pd);
					if (isAssinado()) {
						newInstance();
						return;
					} else {
						ProcessoDocumentoBinHome.instance().setInstance(
								pd.getProcessoDocumentoBin());
						return;
					}
				}
			}
		}
		newInstance();
	}

	private void afterNewInstance() {
		if (getModeloDocumentoCombo() == null) {
			setModeloDocumentoCombo(new ModeloDocumento());
			String query = "select o from ModeloDocumento o "
					+ "where o = :idModeloPeticao";
			Query q = getEntityManager().createQuery(query).setMaxResults(1);
			if (getNaoTemAlgumDocumentoAnexado()) {
				q.setParameter("idModeloPeticao", ParametroUtil.instance()
						.getModeloPeticaoInicial());
			} else {
				q.setParameter("idModeloPeticao", ParametroUtil.instance()
						.getModeloPeticaoIncidental());
			}
			ModeloDocumento result = EntityUtil.getSingleResult(q);
			setModeloDocumentoCombo(result);
			processarModelo();
		}
		if (getNaoTemAlgumDocumentoAnexado()) {
			if (ProcessoTrfHome.instance().getInstance().getClasseJudicial() != null) {
				getInstance().setTipoProcessoDocumento(ProcessoTrfHome.instance().getInstance().getClasseJudicial().getTipoProcessoDocumentoInicial());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Boolean verificaDocumentoDespacho(ProcessoDocumento pd) {
		UsuarioLocalizacao usuarioLocalizacaoAtual = (UsuarioLocalizacao) Contexts
				.getSessionContext().get("usuarioLogadoLocalizacaoAtual");
		if (Authenticator.isMagistrado()) {
			StringBuilder sqlPes = new StringBuilder();
			sqlPes.append(" select o from ");
			sqlPes.append(" ProcessoDocumentoBinPessoaAssinatura o ");
			sqlPes.append(" where o.processoDocumentoBin = :pdb");
			EntityManager em = getEntityManager();
			Query query = em.createQuery(sqlPes.toString());
			query.setParameter("pdb", pd.getProcessoDocumentoBin());
			List<ProcessoDocumentoBinPessoaAssinatura> listPA = query
					.getResultList();
			int i = 0;
			while (i < listPA.size()) {
				if (usuarioLocalizacaoAtual.getUsuario().getCertChain()
						.toString()
						.equals(listPA.get(i).getCertChain().toString())) {
					return false;
				}
				i++;
			}
			return true;
		}
		return (pd.getProcessoDocumentoBin().getSignatarios().isEmpty());
	}

	public String persistAvulsa() {
		String ret = null;
		ProcessoDocumentoPeticaoNaoLidaHome.instance().newInstance();
		if (getInstance().getIdProcessoDocumento() != 0) {
			ret = update();
		} else {
			ret = persist();
			gravarProcessoDocumentoPeticao();
		}
		if (ret != null) {
			ProcessoDocumentoBinHome.instance().assinarDocumento();
		}
		return ret;
	}

	public String persistComAssinatura() {
		ProcessoDocumentoBinHome.instance().setIsAssinarDocumento(Boolean.TRUE);
		return persist();
	}

	public String persistSemAssinatura() {
		ProcessoDocumentoBinHome.instance()
				.setIsAssinarDocumento(Boolean.FALSE);
		return persist();
	}

	public void gravarPdf() {
		persist();
		newInstance();
		ProcessoTrfHome.instance().setInstance(
				EntityUtil.find(ProcessoTrf.class, ProcessoHome.instance()
						.getInstance().getIdProcesso()));
	}

	@Override
	public String persist() {
		ProcessoTrfHome processoTrf = ProcessoTrfHome.instance();
		if (getInstance().getDocumentoSigiloso()) {
			processoTrf.getInstance().setApreciadoSigilo(
					ProcessoTrfApreciadoEnum.A);
			processoTrf.update();
		}

		ProcessoTrfConexaoHome conexao = ProcessoTrfConexaoHome.instance();
		if (conexao.isManaged()) {
			getInstance().setProcesso(conexao.getProcesso());
			conexao.setProcessoDocumento(getInstance());
		}

		getInstance().setLocalizacao(
				Authenticator.getLocalizacaoFisicaAtual());
		if (getInstance().getPapel() == null) {
			getInstance().setPapel(
					Authenticator.getUsuarioLocalizacaoAtual().getPapel());
		}

		if(getInstance().getInstancia() == null){
			getInstance().setInstancia(ParametroUtil.instance().getInstancia());
		}
		
		getInstance().setNomeUsuarioInclusao(
				Authenticator.getUsuarioLogado().getNome());

		String ret = super.persist();
		if (ret != null
				&& processoTrf.isManaged()
				&& !processoTrf.getInstance().getProcesso()
						.getProcessoDocumentoList().contains(getInstance())) {
			processoTrf.getInstance().getProcesso().getProcessoDocumentoList()
					.add(getInstance());
		}
		ProcessoHome.instance().setIdProcessoDocumento(
				getInstance().getIdProcessoDocumento());
		ProcessoDocumentoBinHome.instance().setProcessoDocumento(getInstance());
		if (!EventsTipoDocumentoTreeHandler.instance().getEventoBeanList()
				.isEmpty()) {
			EventosTreeHandler.instance().setEventoBeanList(
					EventsTipoDocumentoTreeHandler.instance()
							.getEventoBeanList());
		}
		ProcessoDocumentoPeticaoNaoLidaHome.instance().newInstance();
		refreshGrid();
		return ret;
	}
	
	public String persistCertidao(ProcessoTrf processoTrf) {
		if(processoTrf == null){
			return "";
		}

		ProcessoTrfConexaoHome conexao = ProcessoTrfConexaoHome.instance();
		if (conexao.isManaged()) {
			getInstance().setProcesso(conexao.getProcesso());
			conexao.setProcessoDocumento(getInstance());
		}

		getInstance().setLocalizacao(Authenticator.getLocalizacaoFisicaAtual());
		if (getInstance().getPapel() == null) {
			getInstance().setPapel(Authenticator.getPapelAtual());
		}

		getInstance().setNomeUsuarioInclusao(
				Authenticator.getUsuarioLogado().getNome());

		String ret = super.persist();

		try {
			getDocumentoJudicialService().finalizaDocumento(getInstance(),
					processoTrf, null, false, false);
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR,
					"Erro ao finalizar documento");
			getLog().error(e);
		}

		if (ret != null
				&& !processoTrf.getProcesso()
						.getProcessoDocumentoList().contains(getInstance())) {
			processoTrf.getProcesso().getProcessoDocumentoList()
					.add(getInstance());
		}
		ProcessoHome.instance().setIdProcessoDocumento(
				getInstance().getIdProcessoDocumento());
		ProcessoDocumentoBinHome.instance().setProcessoDocumento(getInstance());
		if (!EventsTipoDocumentoTreeHandler.instance().getEventoBeanList()
				.isEmpty()) {
			EventosTreeHandler.instance().setEventoBeanList(
					EventsTipoDocumentoTreeHandler.instance()
							.getEventoBeanList());
		}
		ProcessoDocumentoPeticaoNaoLidaHome.instance().newInstance();
		refreshGrid();
		return ret;
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		String afterPersistOrUpdate = super.afterPersistOrUpdate(ret);
		ProcessoHome.instance().setIdProcessoDocumento(getInstance().getIdProcessoDocumento());
		EventosTreeHandler.instance().registraEventos();
		return afterPersistOrUpdate;
	}

	@Override
	protected void refreshGrid() {
		refreshGrid("documentoProcessoAnexadoDocumentoGrid");
		refreshGrid("processoDocumentoGrid");
		refreshGrid("processoTrfDocumentoImpressoGrid");
		refreshGrid("processoTrfDocumentoGrid");
		refreshGrid("processoTrfDocumentoAdvogadoGrid");
		refreshGrid("processoEventoGrid");
		refreshGrid("processoTrfDocumentoPaginatorGrid");

		super.refreshGrid();
	}

	public String getTipoUsuario() {
		if (getInstance().getProcessoDocumentoBin() != null) {
			for (int i = 0; i < getInstance().getProcesso()
					.getUsuarioCadastroProcesso().getUsuarioLocalizacaoList()
					.size(); i++) {
				if (getInstance().getProcesso().getUsuarioCadastroProcesso()
						.getUsuarioLocalizacaoList().get(i).getPapel()
						.getNome().equals("Advogado")) {
					return "Advogado";
				} else {
					if (getInstance().getProcesso()
							.getUsuarioCadastroProcesso()
							.getUsuarioLocalizacaoList().get(i).getPapel()
							.getNome().equals("Procurador")) {
						return "Procurador";
					} else {
						if (getInstance().getProcesso()
								.getUsuarioCadastroProcesso()
								.getUsuarioLocalizacaoList().get(i).getPapel()
								.getNome().equals("PessoaServidor")) {
							return "PessoaServidor";
						}
					}
				}
			}
		}
		return "Não Encontrado!";
	}

	public boolean podeAssociarArquivo() {
		if ((isMesmaLocalizacao() || Authenticator.getPapelAtual()
				.getIdentificador().equalsIgnoreCase("advogado"))
				&& ((getModelo() && isAssinado()) || !getModelo()
						&& isManaged())) {
			return true;
		}
		return false;
	}

	public Boolean isMesmaLocalizacao() {
		return Authenticator.getLocalizacaoFisicaAtual().equals(
				getInstance().getLocalizacao());
	}

	@Override
	public String labelTipoProcessoDocumento() {
		String label = null;
		if (Identity.instance().hasRole("advogado")) {
			label = "Tipo de Petição";
		} else {
			label = "Tipo do Documento";
		}
		return label;
	}

	public String labelAnexarPeticaoDocumento() {
		String label = null;
		if (Authenticator.isUsuarioExterno()) {
			label = "Anexar Petições/Documentos";
		} else {
			label = "Anexar Documento";
		}
		return label;
	}

	public boolean possuiPeticaoInicialAssinada(Processo processo) {
		String hql = "select count(o) from ProcessoDocumentoBinPessoaAssinatura o "
				+ "inner join o.processoDocumentoBin.processoDocumentoList pd "
				+ "where pd.processo = :processo and pd.tipoProcessoDocumento = "
				+ ":tipoProcessoDocumento";
		Query query = getEntityManager().createQuery(hql);
		query.setParameter("processo", processo);
		query.setParameter(
				"tipoProcessoDocumento",
				Util.instance().eval(
						"tipoProcessoDocumentoPeticaoInicial"));

		try {
			Long count = EntityUtil.getSingleResultCount(query);
			return count > 0;
		} catch (NoResultException ex) {
			return false;
		}

	}

	/**
	 * Método que verifica se existe algum documento anexado e se algum dos
	 * documentos é uma petição inicial.
	 * 
	 * @return Boolean
	 */
	public Boolean getNaoTemAlgumDocumentoAnexado() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		Processo processo = processoTrf.getProcesso();
		if (processo != null) {
			return !possuiPeticaoInicialAssinada(processo);
		}
		return true;
	}

	public void setarProcessoDocumento(ProcessoDocumento processoDocumento) {
		Contexts.removeFromAllContexts("abrirModalMotivoDataExclusao");
		this.mostrarModalMotivo = true;
		setInstance(processoDocumento);
	}

	public void gravarMotivoExclusao() {
		try {
			gravarExclusaoDoDocumento();
		} catch (ExclusaoDocumentoException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					e.getMessage());
		}
	}
	
	public void gravarExclusaoDoDocumento() throws ExclusaoDocumentoException {	
		try {
			if (Authenticator.isUsuarioInterno()) {
				String motivoExclusao = getInstance().getMotivoExclusao();
				if (motivoExclusao != null && !motivoExclusao.equals("")) {
					boolean exclusaoPermitida = true;
					if (!this.isAssinado(getInstance().getProcessoDocumentoBin())) {
						exclusaoPermitida = this.excluiReferenciaJbpm(getInstance());
					}
					Date dataAtual = new Date();
					if (exclusaoPermitida) {
						instance.setAtivo(Boolean.FALSE);
						instance.setDataExclusao(dataAtual);
						instance.setUsuarioExclusao(Authenticator.getUsuarioLogado());
						instance.setNomeUsuarioExclusao(Authenticator.getUsuarioLogado().getNome());
						FacesMessages.instance().clear();
						super.update();
	
						ArrayList<ProcessoDocumento> listaPdfAssociados = ComponentUtil.getComponent(AnexarDocumentos.class).listaPdfAssociados(instance);
						listaPdfAssociados.addAll(instance.getDocumentosVinculados());
						for (ProcessoDocumento pd : listaPdfAssociados) {
							pd.setAtivo(Boolean.FALSE);
							pd.setDataExclusao(dataAtual);
							pd.setUsuarioExclusao(Authenticator.getUsuarioLogado());
							pd.setNomeUsuarioExclusao(Authenticator.getUsuarioLogado().getNome());
							pd.setMotivoExclusao(motivoExclusao);
							setInstance(pd);
							FacesMessages.instance().clear();
							super.update();
						}
						try {
							ComponentUtil.getComponent(LancadorMovimentosService.class).excluirMovimento(ComponentUtil.getComponent(ProcessoEventoManager.class).recuperar(instance), true);
						} catch (Exception ex) {
							throw new  ExclusaoDocumentoException(ex.getLocalizedMessage());
						}
						lancarMovimentacaoExclusaoDocumento();
					} else {
						throw new  ExclusaoDocumentoException("Não foi possível excluir a referência a este documento no fluxo.");
					}
				} else {
					throw new  ExclusaoDocumentoException("O motivo da exclusão deve ser informado!");
				}
			} else {
				throw new  ExclusaoDocumentoException("Você não tem permissão para remover esse documento.");
			}
		} finally {
			setInstance(null);
		}
	}

	/**
	 * Verifica se um determinado <code>ProcessoDocumento</code> é uma minuta. O
	 * conceito de minuta, para efeitos deste método, é de um documento que
	 * exige assinatura, mas não está assinado.
	 * 
	 * @param pd
	 * @return
	 */
	public boolean isMinuta(ProcessoDocumento pd) {
		return !this.isAssinado(pd.getProcessoDocumentoBin())
				&& this.requerAssinatura(pd.getTipoProcessoDocumento());
	}

	/**
	 * Verifica se um determinado <code>TipoProcessoDocumento</code> requer
	 * assinatura. O método retorna <code>true</code> quando qualquer dos papeis
	 * associados ao documento têm exigibilidade 'Obrigatória' ou 'Suficiente'.
	 * 
	 * @param tpd
	 * @return
	 */
	public boolean requerAssinatura(TipoProcessoDocumento tpd) {
		boolean resultado = false;
		List<TipoProcessoDocumentoPapel> papeis = tpd.getPapeis();
		for (TipoProcessoDocumentoPapel papel : papeis) {
			if (papel.getExigibilidade().isObrigatorio() || papel.getExigibilidade().isSuficiente()) {
				resultado = true;
				break;
			}
		}
		return resultado;
	}

	/**
	 * Verifica se um determinado <code>ProcessoDocumentoBin</code> está
	 * assinado. A verificação é realizada por meio da lista de assinaturas
	 * na tabela tb_proc_doc_bin_pess_assin
	 * @see ProcessoDocumentoBinPessoaAssinatura
	 * 
	 * @param bin
	 * @return
	 */
	private boolean isAssinado(ProcessoDocumentoBin bin) {
		return (bin != null && !bin.getSignatarios().isEmpty());
	}

	/**
	 * Método que realiza a exclusão das referências de uma minuta no JBPM. O
	 * método retorna <code>true</code> se a exclusão ocorreu com sucesso, ou
	 * <code>false</code> caso alguma exceção seja levantada.
	 * 
	 * @param pd
	 * @return
	 */
	private boolean excluiReferenciaJbpm(ProcessoDocumento pd) {

		if (pd == null)
			return false;

		boolean excluido = true;

		StringBuffer sb = new StringBuffer();
		sb.append("delete from jbpm_variableinstance where ");
		sb.append("(lower(name_) like lower('%MinutaEmElabora__o') ");
		sb.append("or lower(name_) like lower('%ProduzirMinutaSecretaria')) ");
		sb.append("and longvalue_ = ");
		sb.append(pd.getIdProcessoDocumento());

		try {
			Query r = EntityUtil.createNativeQuery(getEntityManager(), sb, "jbpm_variableinstance");
			int i = r.executeUpdate();
			System.out.println("Exclusão de Minuta no JBPM (Documento "
					+ pd.getIdProcessoDocumento() + "): " + i
					+ " linhas excluídas em 'jbpm_variableinstance'.");
		} catch (Throwable t) {
			excluido = false;
			t.printStackTrace();
		}
		return excluido;
	}

	public boolean verificarDocumentoEventoRelacionado(
			ProcessoDocumento documento) {
		StringBuilder sb = new StringBuilder();
		sb = new StringBuilder();
		sb.append("select * from tb_processo_evento ev ");
		sb.append("where id_processo_documento = :id and exists (select * from tb_complemento_segmentado where id_movimento_processo = ev.id_processo_evento)");

		Query query = getEntityManager().createNativeQuery(sb.toString());
		query.setParameter("id", documento.getIdProcessoDocumento());

		return EntityUtil.getSingleResult(query) != null;

	}

	public void removerDocumento(ProcessoDocumento documento, Integer idProcessoTrf) {
		try {
			removerDocumentoSemTratamentoDeErro(documento, idProcessoTrf);
		} catch (Exception e) {
			log.debug("Erro ao remover: " + e.getLocalizedMessage());
			throw new AplicationException("Erro ao remover: " + e.getLocalizedMessage(), e);
		}
	}

	private void removerDocumento(ProcessoDocumento documento) {
		StringBuilder sb = new StringBuilder();
		sb = new StringBuilder();
		sb.append("delete from tb_processo_documento pd ");
		sb.append("where id_processo_documento = :id ");

		Query query = getEntityManager().createNativeQuery(sb.toString());
		query.setParameter("id", documento.getIdProcessoDocumento());

		query.executeUpdate();
	}

	private void removerDocumentoBinPessoaAssinatura(ProcessoDocumentoBin documentoBinRemocao) {
		StringBuilder sb = new StringBuilder();
		sb = new StringBuilder();
		sb.append("delete from tb_proc_doc_bin_pess_assin pdb ");
		sb.append("where id_processo_documento_bin = :id ");

		Query query = getEntityManager().createNativeQuery(sb.toString());
		query.setParameter("id", documentoBinRemocao.getIdProcessoDocumentoBin());

		query.executeUpdate();
	}

	private void removerDocumentoBin(ProcessoDocumentoBin documentoBinRemocao) {
		StringBuilder sb = new StringBuilder();
		sb = new StringBuilder();
		sb.append("delete from tb_processo_documento_bin pdb ");
		sb.append("where id_processo_documento_bin = :id ");

		Query query = getEntityManager().createNativeQuery(sb.toString());
		query.setParameter("id", documentoBinRemocao.getIdProcessoDocumentoBin());

		query.executeUpdate();
	}

	private void removerVersoesDocumento(ProcessoDocumentoBin documentoBinRemocao) {
		if (documentoBinRemocao.getVersoes() == null) {
			List<ControleVersaoDocumento> versoes = ComponentUtil.getControleVersaoDocumentoManager()
					.getControleVersaoDocumentoDAO()
					.obterTodasVersoesPorIdDocumento(documentoBinRemocao.getIdProcessoDocumentoBin());

			if (versoes != null && !versoes.isEmpty()) {
				documentoBinRemocao.setVersoes(versoes);
			}
		}

		List<ControleVersaoDocumento> versoes = documentoBinRemocao.getVersoes();

		if (versoes != null && versoes.isEmpty() == false) {
			List<Integer> ids = versoes.stream().map(ControleVersaoDocumento::getIdControleVersaoDocumento)
					.collect(Collectors.toList());

			StringBuilder sb = new StringBuilder();
			sb = new StringBuilder();
			sb.append("DELETE FROM tb_controle_versao_documento cvd ");
			sb.append("WHERE id_controle_versao_documento in (:ids) ");

			Query query = getEntityManager().createNativeQuery(sb.toString());
			query.setParameter("ids", ids);

			query.executeUpdate();
		}
	}

	private void removerProcessoTrfConexaoDocumento(Integer idDocumento) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"update ProcessoTrfConexao set processoDocumento = null where processoDocumento.idProcessoDocumento = :idDocumento");

		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("idDocumento", idDocumento);

		query.executeUpdate();
	}

	private void removerDocumentoTrf(ProcessoDocumento documento) {
		StringBuilder sb = new StringBuilder();
		sb = new StringBuilder();
		sb.append("delete from tb_processo_documento_trf ");
		sb.append("where id_processo_documento_trf = :id ");

		Query query = getEntityManager().createNativeQuery(sb.toString());
		query.setParameter("id", documento.getIdProcessoDocumento());

		query.executeUpdate();
	}

	@Transactional
	public void removerDocumentoSemTratamentoDeErro(ProcessoDocumento documento, Integer idProcessoTrf)
			throws PJeBusinessException {
		if (verificarDocumentoEventoRelacionado(documento)) {
			throw new PJeBusinessException(
					"Não foi possível excluir o documento, porque há evento(s) relacionado(s) a ele.");
		}

		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, idProcessoTrf);
		ProcessoTrfHome.instance().setInstance(processoTrf);
		ProcessoTrfHome processoTrfHome = ProcessoTrfHome.instance();

		if (processoTrfHome.getInstance().getProcesso() != null) {
			processoTrfHome.getInstance().getProcesso().getProcessoDocumentoList().remove(documento);
		}

		ProcessoDocumentoBin documentoBinRemocao = documento.getProcessoDocumentoBin();

		removerVersoesDocumento(documentoBinRemocao);
		getEntityManager().flush();

		removerProcessoTrfConexaoDocumento(documento.getIdProcessoDocumento());
		getEntityManager().flush();

		removerDocumentoTrf(documento);
		getEntityManager().flush();

		removerDocumento(documento);
		getEntityManager().flush();

		removerDocumentoBinPessoaAssinatura(documentoBinRemocao);
		getEntityManager().flush();

		removerDocumentoBin(documentoBinRemocao);
		getEntityManager().flush();

		refreshGrid("processoTrfDocumentoGrid");
		refreshGrid("processoTrfDocumentoAdvogadoGrid");

		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Documento excluído com sucesso.");

		this.setInstance(null);
	}

	/**
	 * refatoração para evitar duplicação de código
	 */
	private void removerDocumento(ProcessoDocumento documento, ProcessoTrfHome processoTrfHome) {
		ProcessoDocumento processoDocumentoAux = null;

		try {
			documento = getProcessoDocumentoManager().findById(documento.getIdProcessoDocumento());
			documento = getProcessoDocumentoManager().refresh(documento);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}

		Set<ProcessoDocumento> lista = documento.getDocumentosVinculados();

		if (lista.size() > 0) {
			for (ProcessoDocumento processoDocumento : lista) {
				removerDocumento(processoDocumento, processoDocumento
						.getProcesso().getIdProcesso());
			}
		}
		removerDocumento(documento, processoTrfHome.getInstance()
				.getIdProcessoTrf());
	}

	public void excluir(ProcessoDocumento documento) {
		ProcessoTrfHome processoTrfHome = ProcessoTrfHome.instance();
		if (processoTrfHome.isEmElaboracao() || documento.getDataJuntada() == null) {
			removerDocumento(documento, processoTrfHome);
		}
	}

	public void excluirDocNaoAssinado(ProcessoDocumento documento) {
		ProcessoTrfHome processoTrfHome = ProcessoTrfHome.instance();
		removerDocumento(documento, processoTrfHome);
	}

	/**
	 * Método wrapper que remove um ProcessoDocumento com o id vindo do fluxo.
	 * 
	 * @param variavelDeFluxo
	 *            contém o id do documento a ser excluído
	 * @see {@link #excluirDocumentoNaoAssinadoAtravesDeVariavelDeFluxo(String)}
	 */
	public void removerDocumentoNaoAssinado(String variavelDeFluxo) {
		excluirDocumentoNaoAssinadoAtravesDeVariavelDeFluxo(variavelDeFluxo);
		FacesMessages.instance().clear();
	}

	/**
	 * Método que remove um ProcessoDocumento com o id vindo do fluxo.
	 * 
	 * @param var nome da variável que vem do fluxo que contém o id do documento a ser excluído.
	 */
	public void excluirDocumentoNaoAssinadoAtravesDeVariavelDeFluxo(String var) {
		try {
			String idProcessoDocumento = Contexts.getBusinessProcessContext().get(var).toString();
			if (idProcessoDocumento != null) {
				ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class, Integer.valueOf(idProcessoDocumento));
				if (pd != null) {
					excluirDocNaoAssinado(pd);
				}
			}
		} catch (Exception e) {
			log.debug("Erro ao tentar remover ProcessoDocumento de id: " + var
					+ "através de variável de fluxo: " + e.getMessage());
			log.debug(e);
		}
	}

	/**
	 * Método wrapper que altera o tipo de um ProcessoDocumento com o id vindo
	 * do fluxo.
	 * 
	 * @param variavelDeFluxo
	 *            contém o id do documento a ser alterado
	 * @param idTipoProcessoDocumento
	 *            novo tipo do documento
	 * @see {@link #alterarTipoDocumentoNaoAssinadoAtravesDeVariavelDeFluxo(String, Integer)}
	 */
	public void alterarTipoDocumento(String variavelDeFluxo,
			Integer idTipoProcessoDocumento) {
		alterarTipoDocumentoNaoAssinadoAtravesDeVariavelDeFluxo(
				variavelDeFluxo, idTipoProcessoDocumento);
	}

	/**
	 * Método que altera o tipo de um ProcessoDocumento com o id vindo do fluxo.
	 * 
	 * @param var nome da variável que vem do fluxo que contém o id do documento a ser excluído.
	 * @param idTipoProcessoDocumento id do novo tipo de documento.
	 */
	public void alterarTipoDocumentoNaoAssinadoAtravesDeVariavelDeFluxo(
			String var, Integer idTipoProcessoDocumento) {
		Integer idProcessoDocumento = (Integer) Contexts
				.getBusinessProcessContext().get(var);
		if (idProcessoDocumento != null) {
			ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class,
					idProcessoDocumento);
			if (pd != null) {
				TipoProcessoDocumento tipoProcessoDocumento = EntityUtil.find(
						TipoProcessoDocumento.class, idTipoProcessoDocumento);
				pd.setTipoProcessoDocumento(tipoProcessoDocumento);
				pd.setProcessoDocumento(tipoProcessoDocumento
						.getTipoProcessoDocumento());
				getEntityManager().merge(pd);
				getEntityManager().flush();
			}
		}
	}

	@Override
	public void removeFromContext() {
		super.removeFromContext();
		ProcessoDocumentoBinHome.instance().removeFromContext();
		refreshGrid("processoTrfDocumentoGrid");
	}

	public String inactive() {
		getInstance().setUsuarioExclusao(Authenticator.getUsuarioLogado());
		getInstance().setDataExclusao(new Date());
		getInstance().setAtivo(Boolean.FALSE);
		return super.inactive(getInstance());
	}

	@Override
	public String update() {
		getInstance().setUsuarioAlteracao(Authenticator.getUsuarioLogado());
		getInstance().setDataAlteracao(new Date());
		getInstance().setNomeUsuarioAlteracao(
				Authenticator.getUsuarioLogado().getNome());
		ProcessoDocumentoBinHome pdbh = ProcessoDocumentoBinHome.instance();
		pdbh.update();
		ProcessoHome.instance().setIdProcessoDocumento(
				getInstance().getIdProcessoDocumento());
		return super.update();
	}

	public void updateComAssinatura() {
		update();
		ProcessoDocumentoBinHome.instance().assinarDocumento();
	}

	public Pessoa getUsuarioLogado() {
		return Authenticator.getPessoaLogada();
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getProcessoDocumentoMinutaList() {
		// Agora é validado se pelo menos um dos papeis obrigatórios assinou
		Query query;
		Papel papel = Authenticator.getPapelAtual();

		List<Integer> listPapeis = new ArrayList<Integer>();
		String[] papeis = ParametroHome.getParametro("idPapelEditarMinuta")
				.split(",");

		for (int i = 0; i < papeis.length; i++) {
			if (papel.getIdPapel() != Integer.parseInt(papeis[i].trim())) {
				listPapeis.add(Integer.parseInt(papeis[i].trim()));
			}
		}
		listPapeis.add(papel.getIdPapel());

		StringBuilder sqlPes = new StringBuilder();
		EntityManager em = getEntityManager();
		sqlPes.append("select o from ProcessoDocumento o where ");
		sqlPes.append(" o.processoDocumentoBin not in ( select a.processoDocumentoBin from ProcessoDocumentoBinPessoaAssinatura a where a.processoDocumentoBin = o.processoDocumentoBin ) ");
		sqlPes.append(" and o.tipoProcessoDocumento in (select tpd from TipoProcessoDocumentoTrf tpd ");
		sqlPes.append("where tpd.visivelAnaliseMinuta = true) and ");
		sqlPes.append("o.processo.idProcesso = :idProcesso  and ");
		sqlPes.append("o.tipoProcessoDocumento in (select u.tipoProcessoDocumento from TipoProcessoDocumentoPapel u where u.papel.idPapel in (:idPapeis)) and ");
		sqlPes.append("o.ativo = true");

		query = em.createQuery(sqlPes.toString());
		query.setParameter("idProcesso", ComponentUtil.getComponent(ProcessoHome.class).getInstance().getIdProcesso());
		query.setParameter("idPapeis", listPapeis);

		List<ProcessoDocumento> pdL = query.getResultList();

		return pdL;
	}

	/**
	 * [PJEII-3785] Segundo esclarecido pelo Felipe Barros, a validação do
	 * documento pelo papel do assinador deve ocorrer no momento da assinatura,
	 * atualizando o valor do atributo 'valido' da entida ProcessoDocumentoBin,
	 * a ser verificado neste método.
	 * 
	 * @param documento
	 * @return
	 */
	public Boolean estaValidado(ProcessoDocumento documento) {

		if (documento != null && documento.getProcessoDocumentoBin() != null && documento.getProcessoDocumentoBin().getValido() == null) {
			
			if(getEntityManager().contains(documento.getProcessoDocumentoBin())){
				getEntityManager().refresh(documento.getProcessoDocumentoBin());
			}
			
			return documento.getProcessoDocumentoBin().getValido() == null ? false
					: documento.getProcessoDocumentoBin().getValido();

		}
		
		if( documento.getProcessoDocumentoBin().getValido() != null )
			return documento.getProcessoDocumentoBin().getValido();
		
		return false;
	}

	public String tipoDocumentoPrevento() {
		EntityManager em = getEntityManager();
		StringBuilder sqlPes = new StringBuilder();
		sqlPes.append(" select o from TipoProcessoDocumento o where ");
		sqlPes.append(" o.tipoProcessoDocumento = :tipo ");
		Query query = em.createQuery(sqlPes.toString());
		query.setParameter("tipo", "Despacho");
		query.setMaxResults(1);
		TipoProcessoDocumento tpd = (TipoProcessoDocumento) EntityUtil.getSingleResult(query);
		getInstance().setTipoProcessoDocumento(tpd);
		return tpd != null ? tpd.getTipoProcessoDocumento() : null;
	}

	private int idPD = 0;
	private int idPDB = 0;

	public void setarPD(String idPD) {
		if (!idPD.equals(""))
			this.idPD = Integer.parseInt(idPD);
	}

	public void setarPDB(String idPDB) {
		if (!idPDB.equals("")) {
			this.idPDB = Integer.parseInt(idPDB);
		}
	}

	private ProcessoDocumento buscaPD() {
		EntityManager em = getEntityManager();
		StringBuilder sqlPes = new StringBuilder();
		sqlPes.append(" select o from ");
		sqlPes.append(" ProcessoDocumento o");
		sqlPes.append(" where o.idProcessoDocumento = :id");
		Query query = em.createQuery(sqlPes.toString());
		query.setParameter("id", idPD);

		return (ProcessoDocumento) EntityUtil.getSingleResult(query);
	}

	@SuppressWarnings("unchecked")
	public List<String> listaAssinatura() {
		EntityManager em = getEntityManager();
		StringBuilder sqlPes = new StringBuilder();
		sqlPes.append(" select o from ");
		sqlPes.append(" ProcessoDocumentoBinPessoaAssinatura o");
		sqlPes.append(" where o.processoDocumentoBin.idProcessoDocumentoBin = :id");
		Query query = em.createQuery(sqlPes.toString());
		query.setParameter("id", idPDB);
		List<ProcessoDocumentoBinPessoaAssinatura> list = query.getResultList();

		List<String> listRet = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++)
			listRet.add(list.get(i).getNomePessoa());

		return listRet;
	}

	/**
	 * método que verifica se o documento possui assinatura
	 */
	public boolean pdBinAssiando() {
		return !instance.getProcessoDocumentoBin().getSignatarios().isEmpty();
	}

	/**
	 * método usado para liberar o botão de certificação digital de acordo com o
	 * papel do documento e papel do usuário e verifica se usuário já assinou o
	 * documento.
	 * 
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	public boolean liberaCertificacao(ProcessoDocumento processoDoc) {
		boolean achou = false;
		if (processoDoc != null && !processoDoc.getAtivo()) {
			return false;
		}
		
		// verifica se o papel tem a exigibilidade necessária para assinar
		if ((processoDoc != null && processoDoc.getTipoProcessoDocumento() != null)
				|| (idPD != 0)) {
			ProcessoDocumento pd = processoDoc;
			EntityManager em = getEntityManager();
			int id = 0;

			if (idPD == 0) {
				id = processoDoc.getTipoProcessoDocumento()
						.getIdTipoProcessoDocumento();
			} else {
				pd = buscaPD();
				id = pd.getTipoProcessoDocumento().getIdTipoProcessoDocumento();
			}

			StringBuilder sqlPes = new StringBuilder();
			sqlPes.append(" select o from ");
			sqlPes.append(" TipoProcessoDocumentoPapel o");
			sqlPes.append(" where o.tipoProcessoDocumento.idTipoProcessoDocumento = :id");
			Query query = em.createQuery(sqlPes.toString());
			query.setParameter("id", id);
			List<TipoProcessoDocumentoPapel> list = query.getResultList();
			UsuarioLocalizacao usuarioLocalizacaoAtual = (UsuarioLocalizacao) Contexts
					.getSessionContext().get("usuarioLogadoLocalizacaoAtual");

			int i = 0;
			while (!achou && i < list.size()) {
				if (usuarioLocalizacaoAtual.getPapel().getNome()
						.equals(list.get(i).getPapel().getNome()) 
						&& !list.get(i).getExigibilidade().isSemAssinatura()) {
					achou = true;
				}
				i++;
			}

			if (achou) {
				// busca a lista de assinaturas do documento e se o usuário atual já tiver assinado não permite assinar novamente
				sqlPes = new StringBuilder();
				sqlPes.append(" select o.pessoa.idUsuario from ");
				sqlPes.append(" ProcessoDocumentoBinPessoaAssinatura o ");
				sqlPes.append(" where o.processoDocumentoBin = (select a.processoDocumentoBin from ProcessoDocumento a where a.idProcessoDocumento = :idPD and a.processoDocumentoBin.signature is not null and a.processoDocumentoBin.certChain is not null)");
				em = getEntityManager();
				query = em.createQuery(sqlPes.toString());
				query.setParameter("idPD", pd.getIdProcessoDocumento());

				List<Integer> listPA = Collections.EMPTY_LIST;
				try {
					listPA = query.getResultList();
				} catch (AssertionFailure e) {
					getEntityManager().flush();
					// PJEII-1954
					listPA = query.getResultList();
				}
				i = 0;
				Usuario usuarioLogado = Authenticator.getUsuarioLogado();
				while (i < listPA.size()) {
					if (listPA.get(i) != null
							&& listPA.get(i).equals(
									usuarioLogado.getIdUsuario())) {
						achou = false;
						break;
					}
					i++;
				}
			}

			if (processoDoc.getProcesso() == null) {
				return achou;
			}

			// Verifica se o processo está em elaboração. Caso esteja, ignora o
			// próximo if
			ProcessoTrf pt = EntityUtil.find(ProcessoTrf.class, processoDoc
					.getProcesso().getIdProcesso());

			// verifica se o tipo deste documento necessita de algum evento para
			// transitar no fluxo.
			if (!pt.getProcessoStatus().equals(ProcessoStatusEnum.E) && achou
					&& TaskInstance.instance() == null) {
				sqlPes = new StringBuilder();
				sqlPes.append("select tpd.id_tipo_processo_documento from tb_tipo_processo_documento tpd join tb_agrupamento a using (id_agrupamento)  ");
				sqlPes.append("join tb_evento_agrupamento ea using (id_agrupamento) join tb_evento e using (id_evento) ");
				sqlPes.append("where tpd.id_tipo_processo_documento = :id ");
				sqlPes.append("and lower(e.ds_caminho_completo) like lower('Magistrado%') ");
				em = getEntityManager();
				query = em.createNativeQuery(sqlPes.toString());
				query.setParameter("id", id);

				List<?> resultado = query.getResultList();

				if (resultado.size() > 0) {
					achou = false;
					setTipoDocAgrupamento(true);
					if (pd.getIdProcessoDocumento() != 0
							&& !StringUtils.isEmpty(pd.toString())) {
						// Verifica se o evento já foi lançado.
						if (EventsTipoDocumentoTreeHandler.instance()
								.verificaRegistroEventos(pd)) {
							achou = true;
							setTipoDocAgrupamento(false);
						}
					}
				}
			}
		}
		return achou;
	}

	// Método incluído por Rafael Barros devido à ISSUE PJEII-3396 em 30/10/2012
	public boolean liberaCertificacao(Integer idProcessoDocumento) {
		ProcessoDocumento processoDocumento = EntityUtil.find(
				ProcessoDocumento.class, idProcessoDocumento);
		return liberaCertificacao(processoDocumento);
	}

	public boolean liberaCertificacao() {
		boolean retorno = liberaCertificacao(instance);
 		if (retorno){
 			TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService = ComponentUtil.getComponent(TipoProcessoDocumentoPapelService.class);
 			retorno = !tipoProcessoDocumentoPapelService.verificarExigibilidadeNaoAssina(
 					Authenticator.getPapelAtual(),
 					instance.getTipoProcessoDocumento());
 		}
		return retorno;
	}

	public ModeloDocumento modeloDocMagistrado(int idMD) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(
				ModeloDocumento.class);
		criteria.add(Restrictions.eq("idModeloDocumento", idMD));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		ModeloDocumento md = (ModeloDocumento) criteria.uniqueResult();
		if (md == null) {
			md = new ModeloDocumento();
		}
		setModeloDocumentoCombo(md);
		ProcessoDocumentoBinHome.instance().getInstance()
				.setModeloDocumento(md.getModeloDocumento());
		return md;
	}

	@SuppressWarnings("unchecked")
	public Date getDataIntimacao(Integer idProcessoDocumento) {
		String query = "select ppe.dtCienciaParte from ProcessoDocumentoExpediente o , ProcessoParteExpediente ppe where "
				+ "o.processoDocumento.idProcessoDocumento = :id and "
				+ "o.processoExpediente.idProcessoExpediente = ppe.processoExpediente.idProcessoExpediente";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("id", idProcessoDocumento);
		q.setMaxResults(1);
		List<Date> dtCienciaParteList = q.getResultList();

		if (dtCienciaParteList != null && dtCienciaParteList.size() > 0) {
			return dtCienciaParteList.get(0);
		} else {
			return null;
		}
	}

	public Boolean getDocumentoLido(Integer idProcessoDocumento) {
		String query = "select count(o) from ProcessoDocumentoLido o where "
				+ "o.processoDocumento.idProcessoDocumento = :id";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("id", idProcessoDocumento);

		Long count = EntityUtil.getSingleResultCount(q);
			
		if (count.compareTo(0L) > 0) {
			setApreciado(Boolean.TRUE);
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	public List<TipoProcessoDocumento> getTipoProcessoDocumentoMinutaItems() {
		List<TipoProcessoDocumento> ret = getDocumentoJudicialService().getTiposDocumentoMinuta(); 
		if(ret.isEmpty()){
			return getTiposDocumentosPorParametrosFixos();
		} else{
			return ret;
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<TipoProcessoDocumento> getTiposDocumentosPorParametrosFixos(){
		StringBuilder sb = new StringBuilder("select o from TipoProcessoDocumento o where ")
				.append(" o.documentoAtoProferido = true");

		Query q = getEntityManager().createQuery(sb.toString());
		return q.getResultList();
	}
	
	public void setApreciado(Boolean apreciado) {
		this.apreciado = apreciado;
	}

	public Boolean getApreciado() {
		return apreciado;
	}

	public Boolean verificaApreciado() {
		if (apreciado) {
			setApreciado(Boolean.FALSE);
			return Boolean.TRUE;
		} else {
			return apreciado;
		}
	}

	public boolean isAssinado() {
		if (getInstance().getIdProcessoDocumento() == 0) {
			return false;
		}
		return isAssinado(getInstance());
	}

	public boolean isAssinado(ProcessoDocumento processoDocumento) {
		return processoDocumento != null && !processoDocumento.getProcessoDocumentoBin().getSignatarios().isEmpty();
	}

	public void consultarProcessoDocumento() {
		ResourceBundle bundle = SeamResourceBundle.getBundle();
		String retorno = null;
		if (codigoDocumento != null) {
			ProcessoDocumentoBin bin = ValidacaoAssinaturaProcessoDocumento.instance().getProcessoDocumentoBin(codigoDocumento);
			ProcessoDocumentoBinManager processoDocumentoBinManager = ComponentUtil.getComponent(ProcessoDocumentoBinManager.class);
			if (bin != null && !processoDocumentoBinManager.verificaAssinaturas(bin).isEmpty()) {
				ProcessoDocumento processoDocumento = getProcessoDocumentoManager().getProcessoDocumentoByProcessoDocumentoBin(bin);
				if(!ProcessoStatusEnum.D.equals(processoDocumento.getProcessoTrf().getProcessoStatus())) {
					retorno = bundle.getString(CONSULTA_NUMERO_DOCUMENTO_INVALIDO);
				} else if (!processoDocumento.getAtivo()) {
					ProcessoDocumentoHome.instance().setarProcessoDocumento(processoDocumento);
					retorno = bundle.getString("pje.consulta.numeroDocumento.excluido");	
				} else if (Boolean.TRUE.equals(existePendenciaCiencia(processoDocumento))) {
					retorno = bundle.getString("pje.consulta.numeroDocumento.pendenteCiencia");
				} else {
					ProcessoDocumentoBinHome.instance().setInstance(bin);
					retorno = ProcessoDocumentoBinHome.instance().verificaSegredo(true, true);
				}
				if(retorno.isEmpty()){
					idProcessoDocumentoBin = bin.getIdProcessoDocumentoBin();
					numeroDocumentoStorage = bin.getNumeroDocumentoStorage();
					nomeArquivo = bin.getNomeArquivo();
					size = bin.getSize();
					binario = bin.getBinario();
					idProcessoDoc = ValidacaoAssinaturaProcessoDocumento.instance().getIdProcessoDocumento(bin);
					
					retorno = bundle.getString("pje.consulta.numeroDocumento.valido"); 
				}
			} else {
				retorno = bundle.getString(CONSULTA_NUMERO_DOCUMENTO_INVALIDO);
			}
		} else {
			retorno = bundle.getString(CONSULTA_NUMERO_DOCUMENTO_INVALIDO);
		}
		Contexts.getEventContext().set("retornoTelaConsultaDocumento", retorno);
	}

	/**
	 * Carrega a pagina de validacao do documento com o numero informado.
	 * @param numeroValidacao Numero informado.
	 */
	public void consultaDocumentoPorQr(String numeroValidacao) {
		this.codigoDocumento = numeroValidacao;
		this.consultarProcessoDocumento();
	}
	
	public void clearConsultaProcessoDocumento() {
		numeroDocumento = null;
		setCodigoDocumento(null);
		idProcessoDocumentoBin = null;
		numeroDocumentoStorage = null;
		nomeArquivo = null;
		idProcessoDoc = null;
	}

	@SuppressWarnings("unchecked")
	public List<ModeloDocumento> getModeloItems() {
		EntityManager em = getEntityManager();
		String sql = " select o from ModeloDocumento o ";
		Query query = em.createQuery(sql);
		List<ModeloDocumento> list = query.getResultList();
		if (list != null && !list.isEmpty()) {
			return list;
		}
		return null;
	}

	public String signDocumento(Integer id, String codIni, String md5,
			String sign, String certChain, Date dataAssinatura, Pessoa pessoa) throws Exception {
		ProcessoDocumento pd = getEntityManager().find(ProcessoDocumento.class, id);
		ProcessoDocumentoBin bin = pd.getProcessoDocumentoBin();
		Long idTaskInstance = TaskInstance.instance() == null ? null : TaskInstance.instance().getId();

		if (certChain != null && sign != null) {
			bin.setCertChain(certChain);
			bin.setSignature(sign);

			if (bin.getDataAssinatura() == null) {
				bin.setDataAssinatura(dataAssinatura);
			}

			getDocumentoJudicialService().finalizaDocumento(pd, pd.getProcessoTrf(), idTaskInstance, false, true, true, pessoa, false);
			EntityUtil.flush();
		}

		return StringUtils.EMPTY;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getProcessoDocumentoAtoOrdinatorioList() {
		String hql = "select d from ProcessoDocumento d where "
				+ "d.processo.idProcesso = :idProcesso and "
				+ "d.tipoProcessoDocumento = :tipoAto and "
				+ "d.processoDocumentoBin not in( "
				+ "select bin from ProcessoDocumentoBinPessoaAssinatura o "
				+ "inner join o.processoDocumentoBin bin "
				+ "inner join o.pessoa p "
				+ "where p in (select u from UsuarioLocalizacao u "
				+ "inner join u.papel papel "
				+ "where papel.identificador = 'dirSecretaria' and u.usuario.idUsuario = p.idUsuario))";
		Query query = getEntityManager().createQuery(hql);
		query.setParameter("idProcesso", ProcessoHome.instance().getInstance()
				.getIdProcesso());
		query.setParameter("tipoAto", ParametroUtil.instance()
				.getTipoProcessoDocumentoAtoOrdinatorio());

		return query.getResultList();
	}

	private Boolean verificaAssinaturaAssistente(
			UsuarioLocalizacao usuarioLocalizacao) {
		StringBuilder sb = new StringBuilder();
		if (usuarioLocalizacao instanceof PessoaAssistenteProcuradoriaLocal) {
			sb.append("select o from PessoaAssistenteProcuradoriaLocal o where ");
			sb.append("o.assinaDigitalmente = true ");
		} else {
			sb.append("select o from PessoaAssistenteAdvogadoLocal o where ");
			sb.append("o.assinadoDigitalmente = true ");
		}
		sb.append("and o.idUsuarioLocalizacao = :id");
		Query q = getEntityManager().createQuery(sb.toString()).setParameter(
				"id", usuarioLocalizacao.getIdUsuarioLocalizacao());
		if (q.getResultList().isEmpty()) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public List<ProcessoDocumento> getProcessoDocumentoMinutaList(
			Boolean apenasDecisaoDespachoSentenca) {

		// Agora é validado se pelo menos um dos papeis obrigatórios assinou
		StringBuilder sqlPes;
		ParametroUtil parametroUtil = new ParametroUtil();
		EntityManager em = getEntityManager();
		Query query;

		Papel papel = Authenticator.getPapelAtual();

		List<Integer> listPapeis = new ArrayList<Integer>();
		String[] papeis = ParametroHome.getParametro("idPapelEditarMinuta")
				.split(",");

		for (int i = 0; i < papeis.length; i++) {
			if (papel.getIdPapel() != Integer.parseInt(papeis[i]))
				listPapeis.add(Integer.parseInt(papeis[i]));
		}
		listPapeis.add(papel.getIdPapel());

		sqlPes = new StringBuilder();
		em = getEntityManager();
		TipoProcessoDocumento tpdS = parametroUtil
				.getTipoProcessoDocumentoSentenca();
		TipoProcessoDocumento tpdDs = parametroUtil
				.getTipoProcessoDocumentoDespacho();
		TipoProcessoDocumento tpdDe = parametroUtil
				.getTipoProcessoDocumentoDecisao();
		ProcessoHome processoHome = ComponentUtil.getComponent(ProcessoHome.class);

		sqlPes.append("select o from ProcessoDocumento o where ");
		sqlPes.append(" o.dataExclusao is null and o.processoDocumentoBin not in ( select a.processoDocumentoBin from ProcessoDocumentoBinPessoaAssinatura a where a.processoDocumentoBin = o.processoDocumentoBin ) ");

		if (apenasDecisaoDespachoSentenca) {
			sqlPes.append(" and (o.tipoProcessoDocumento.idTipoProcessoDocumento = :idTipoProcessoDocumentoS or ");
			sqlPes.append("o.tipoProcessoDocumento.idTipoProcessoDocumento       = :idTipoProcessoDocumentoDs or ");
			sqlPes.append("o.tipoProcessoDocumento.idTipoProcessoDocumento       = :idTipoProcessoDocumentoDe ) and ");
		} else {
			sqlPes.append("and ");
		}

		sqlPes.append("o.processo.idProcesso                                 = :idProcesso  and ");
		sqlPes.append("o.tipoProcessoDocumento in (select u.tipoProcessoDocumento from TipoProcessoDocumentoPapel u where u.papel.idPapel in (:idPapeis))");

		query = em.createQuery(sqlPes.toString());

		if (apenasDecisaoDespachoSentenca) {
			query.setParameter("idTipoProcessoDocumentoS",
					tpdS.getIdTipoProcessoDocumento());
			query.setParameter("idTipoProcessoDocumentoDs",
					tpdDs.getIdTipoProcessoDocumento());
			query.setParameter("idTipoProcessoDocumentoDe",
					tpdDe.getIdTipoProcessoDocumento());
		}

		query.setParameter("idProcesso", processoHome.getInstance()
				.getIdProcesso());
		query.setParameter("idPapeis", listPapeis);

		@SuppressWarnings("unchecked")
		List<ProcessoDocumento> pdL = query
				.getResultList();

		return pdL;
	}

	/**
	 * Informa se pode assinar o documento.
	 * 
	 * @return boolean
	 */
	public Boolean assinaturaPermitida() {
		if (getProcessoDocumentoManager().verificarDocumentoDeAtividadeEspecifica(instance)) {
			return false;
		}
		
		UsuarioLocalizacao usuarioLocalizacaoAtual = Authenticator.getUsuarioLocalizacaoAtual();
		if (!(usuarioLocalizacaoAtual instanceof PessoaAssistenteProcuradoriaLocal || usuarioLocalizacaoAtual instanceof PessoaAssistenteAdvogadoLocal)) {
			return true;
		}
		
		return verificaAssinaturaAssistente(usuarioLocalizacaoAtual);
	}

	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumentoTrf> tipoProcessoDocumentoItems() {
		StringBuilder sb = new StringBuilder();
		List<TipoProcessoDocumentoTrf> list = new ArrayList<TipoProcessoDocumentoTrf>();

		if (getModelo()) {
			sb.append("select o from TipoProcessoDocumentoTrf o ");
			sb.append("where o  in (select tpdp.tipoProcessoDocumento from ");
			sb.append("TipoProcessoDocumentoPapel tpdp where tpdp.papel.idPapel = :idPapel ) ");
			sb.append("and o in (select tpd.tipoProcessoDocumento from AplicacaoClasseTipoProcessoDocumento tpd ");
			sb.append("where tpd.aplicacaoClasse.idAplicacaoClasse = :aplicacaoClasse ) ");
			Query query = EntityUtil.createQuery(sb.toString());
			query.setParameter("idPapel", Authenticator.getPapelAtual()
					.getIdPapel());
			query.setParameter("aplicacaoClasse", ParametroUtil.instance()
					.getAplicacaoSistema().getIdAplicacaoClasse());
			list = query.getResultList();
		} else if (!getModelo()) {
			sb.append("select o from TipoProcessoDocumentoTrf o ");
			sb.append("where o  in (select tpdp.tipoProcessoDocumento from ");
			sb.append("TipoProcessoDocumentoPapel tpdp where tpdp.papel.idPapel = :idPapel ) ");
			sb.append("and o.inTipoDocumento = 'D' ");

			Query query = EntityUtil.createQuery(sb.toString());
			query.setParameter("idPapel", Authenticator.getPapelAtual()
					.getIdPapel());
			list = query.getResultList();
		}

		return list;
	}

	public List<TipoProcessoDocumentoTrf> itemsHabilitacaoAutos() {
		List<TipoProcessoDocumentoTrf> list = tipoProcessoDocumentoItems();
		List<TipoProcessoDocumentoTrf> listHabilitacaoAutos = new ArrayList<TipoProcessoDocumentoTrf>();
		for (TipoProcessoDocumentoTrf tipoProcessoDocumento : list) {
			if (tipoProcessoDocumento.getHabilitacaoAutos()) {
				listHabilitacaoAutos.add(tipoProcessoDocumento);
			}
		}
		return listHabilitacaoAutos;

	}

	public void gravarAssistente() {
		persist();
		gravarProcessoDocumentoPeticao();
	}

	public void gravarProcessoDocumentoPeticao() {
		ProcessoDocumentoPeticaoNaoLida processo = ProcessoDocumentoPeticaoNaoLidaHome
				.instance().getInstance();
		processo.setProcessoDocumento(getInstance());
		processo.setRetificado(Boolean.FALSE);
		processo.setRetirado(Boolean.FALSE);
		getEntityManager().persist(processo);
		getEntityManager().flush();
	}

	public String getUltimaAlteracaoDocumento(ProcessoDocumento pd) {
		String retorno = null;
		if (pd.getNomeUsuarioAlteracao() != null || pd
						.getUsuarioAlteracao() != null) {
			/* PJEII-14624 [TJDFT] Impressão de lista de documentos contém campos com problemas
			 * Ajustei o método para que, se a data de alteração for null, a data da última alteração passe a ser a data de juntada ou de inclusão.
			 * Vanessa Rocha Schriver em 07/04/2014 */			
			SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yy HH:mm");
			String data = "";
			if (pd.getDataAlteracao()!=null){
				data = " em " + fm.format(pd.getDataAlteracao());
			} else if (pd.getDataJuntada()!=null){
				data = " em " + fm.format(pd.getDataJuntada());
			}else if (pd.getDataInclusao()!=null && pd.getUsuarioAlteracao()==pd.getUsuarioInclusao()){
				data = " em " + fm.format(pd.getDataInclusao());
			}
			String nome = pd.getNomeUsuarioAlteracao() != null ? pd
					.getNomeUsuarioAlteracao() : pd.getUsuarioAlteracao()
					.getNome();
			retorno = nome + data;
		} else if (pd.getDataInclusao() != null
				&& (pd.getNomeUsuarioInclusao() != null || pd
						.getUsuarioInclusao() != null)) {
			SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yy HH:mm");
			String data = fm.format(pd.getDataInclusao());
			String nome = pd.getNomeUsuarioInclusao() != null ? pd
					.getNomeUsuarioInclusao() : pd.getUsuarioInclusao()
					.getNome();
			retorno = nome + " em " + data;
		}
		return retorno;
	}

	public TipoProcessoDocumento getTipoDocumentoAcordao() {
		if (tipoDocumentoAcordao == null) {
			tipoDocumentoAcordao = ParametroUtil.instance()
					.getTipoProcessoDocumentoAcordao();
		}
		return tipoDocumentoAcordao;
	}

	public TipoProcessoDocumento getTipoDocumentoEmenta() {
		if (tipoDocumentoEmenta == null) {
			tipoDocumentoEmenta = ParametroUtil.instance()
					.getTipoProcessoDocumentoEmenta();
		}
		return tipoDocumentoEmenta;
	}

	/**
	 * [PJEII-4330]
	 * 
	 * @return TipoProcessoDocumento de Notas orais
	 */
	public TipoProcessoDocumento getTipoDocumentoNotasOrais() {
		if (tipoDocumentoNotasOrais == null) {
			String idParametro = getParametroService().valueOf(Parametros.ID_TIPO_DOCUMENTO_NOTAS_ORAIS);
			tipoDocumentoNotasOrais = getEntityManager().find(TipoProcessoDocumento.class, new Integer(idParametro));
		}
		return tipoDocumentoNotasOrais;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> checkAll(String grid,
			List<ProcessoDocumento> lista, Boolean checkAll) {
		GridQuery gq = getComponent(grid);
		lista.clear();
		if (checkAll) {
			// Insere os valores da Grid em uma lista
			lista.addAll(gq.getFullList());
			for (ProcessoDocumento processoDocumento : lista) {
				processoDocumento.setDocumentoSigilosoTela(Boolean.TRUE);
			}
		} else {
			for (ProcessoDocumento processoDocumento : (List<ProcessoDocumento>)gq.getFullList()) {
				processoDocumento.setDocumentoSigilosoTela(Boolean.FALSE);
			}
		}
		return lista;
	}

	public void criarListaPedido(ProcessoDocumento processoDocumento,
			String grid) {
		if (grid.equals("processoTrfDocumentoMagistradoGrid")) {
			
			if (processoDocumento == null) {
				listaSigilo = checkAll(grid, listaSigilo, checkAllSigilo);
			} else {
				
				if(processoDocumento.getDocumentoSigilosoTela()){
					if (!listaSigilo.contains(processoDocumento)) {
						listaSigilo.add(processoDocumento);
					}
				} else {
					listaSigilo.remove(processoDocumento);
				}
			}
			
			GridQuery gq = getComponent(grid);
			if(listaSigilo.size() == gq.getFullList().size()){
				checkAllSigilo = Boolean.TRUE;
			} else {
				checkAllSigilo = Boolean.FALSE;
			}
		}
	}

	public void modeloExpediente() {
		List<AssuntoTrf> list = ProcessoTrfHome.instance().getInstance()
				.getAssuntoTrfList();
		String assuntos = new String();
		StringBuilder sb = new StringBuilder();
		for (AssuntoTrf lista : list) {
			assuntos = assuntos.concat(lista.getAssuntoCompleto());
			sb.append(lista.getAssuntoCompleto());
		}
		ProcessoTrfHome.instance().setAssuntoTrfCompletoList(assuntos);
		ProcessoTrfHome.instance().setAssuntoTrfCompletoList(sb.toString());
		String modelo = AbstractProcessoDocumentoHome
				.processarModelo(ParametroUtil.instance()
						.getModeloDocExpediente().getModeloDocumento());
		ProcessoDocumentoBinHome.instance().getInstance()
				.setModeloDocumento(modelo);
	}
	
	public Boolean getCheckBox() {
		return checkBox;
	}

	public void setCheckBox(Boolean checkBox) {
		this.checkBox = checkBox;
	}

	public Boolean getCheckAllSigilo() {
		return checkAllSigilo;
	}

	public void setCheckAllSigilo(Boolean checkAllSigilo) {
		this.checkAllSigilo = checkAllSigilo;
	}

	public List<ProcessoDocumento> getListaSigilo() {
		return listaSigilo;
	}

	public void setListaSigilo(List<ProcessoDocumento> listaSigilo) {
		this.listaSigilo = listaSigilo;
	}

	public void setModeloDocumento(String modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public String getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumentoLocalTemp(
			ModeloDocumento modeloDocumentoLocalTemp) {
		this.modeloDocumentoLocalTemp = modeloDocumentoLocalTemp;
	}

	public ModeloDocumento getModeloDocumentoLocalTemp() {
		return modeloDocumentoLocalTemp;
	}

	public void setTipoDocAgrupamento(Boolean tipoDocAgrupamento) {
		this.tipoDocAgrupamento = tipoDocAgrupamento;
	}

	public Boolean getTipoDocAgrupamento() {
		return tipoDocAgrupamento;
	}

	public void renderizarGrid() {
		getEntityManager().clear();
		refreshGrid("documentosNaoAssinadosGrid");
		refreshGrid("documentosNaoAssinadosMagAuxiliarGrid");
		Contexts.removeFromAllContexts("documentosNaoAssinadosGrid");
	}

	public void refreshGridsDocumentos() {
		refreshGrid("processoTrfDocumentoGrid");
		refreshGrid("processoTrfDocumentoAdvogadoGrid");
		refreshGrid("documentosNaoAssinadosMagAuxiliarGrid");
		refreshGrid("documentosNaoAssinadosGrid");
		refreshGrid("documentoProcessoGrid");
		refreshGrid("processoTrfDocumentoImpressoGrid");
		refreshGrid("assinaturasGrid");
		refreshGrid("processoTrfDocumentoPaginatorGrid");
	}

	@SuppressWarnings("unchecked")
	public void removerAssociados(ProcessoDocumento pd) {
		List<ProcessoDocumento> lista;
		StringBuilder sb = new StringBuilder();
		sb.append("select o.documentoAssociado from ProcessoDocumentoAssociacao o ");
		sb.append("where o.processoDocumento = :pd");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("pd", pd);
		lista = q.getResultList();
		excluir(pd);
		if (lista != null && !lista.isEmpty()) {
			for (ProcessoDocumento processoDocumento : lista) {
				excluir(processoDocumento);
			}
		}
	}

	public boolean getMostrarModalMotivo() {
		Contexts.removeFromAllContexts("abrirModalMotivoDataExclusao");
		return mostrarModalMotivo;
	}

	public void setMostrarModalMotivo(boolean mostrarModalMotivo) {
		this.mostrarModalMotivo = mostrarModalMotivo;
	}

	public void assinarDocumentoHabilitacaoAutos() throws CertificadoException {
		AnexarDocumentos anexarDocumentos = ComponentUtil.getComponent(AnexarDocumentos.class);
		try {
			VerificaCertificadoPessoa
					.verificaCertificadoPessoaLogada(anexarDocumentos
							.getCertChain());
			anexarDocumentos.assinar();
			anexarDocumentos.setFlagLimparTela(true);
			gravarProcessoDocumentoPeticao();
			anexarDocumentos.limparTela();
		} catch (CertificadoException e) {
			anexarDocumentos.setFlagLimparTela(false);
			String msgErro = "Erro na verificação do certificado: "
					+ e.getMessage();
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, msgErro);
			log.error(e.getMessage());
			throw new CertificadoException(msgErro);
		}
	}
	
	public void assinarDocumentoPeticionamentoAvulso() throws CertificadoException {
		assinarDocumentoHabilitacaoAutos();
		FacesMessages.instance().clear();
	}

	public void setAdicionarModeloPeticaoNewInstance(
			boolean adicionarModeloPeticaoNewInstance) {
		this.adicionarModeloPeticaoNewInstance = adicionarModeloPeticaoNewInstance;
	}

	public boolean isAdicionarModeloPeticaoNewInstance() {
		return adicionarModeloPeticaoNewInstance;
	}

	public boolean validaDocumentoAdvogadoDetalheProcesso(ProcessoDocumento pd) {
		if (validaDocumentoDetalheMap == null) {
			validaDocumentoDetalheMap = new HashMap<Integer, Boolean>();
		}
		Boolean ret = validaDocumentoDetalheMap
				.get(pd.getIdProcessoDocumento());
		if (ret == null) {
			boolean isAdvogado = Authenticator.getPapelAtual() == ParametroUtil
					.instance().getPapelAdvogado();
			boolean isAssistenteAdvogado = Authenticator.getPapelAtual() == ParametroUtil
					.instance().getPapelAssistenteAdvogado();
			boolean isProcurador = Authenticator.getPapelAtual() == ParametroUtil
					.instance().getPapelProcurador();
			boolean isAssistenteProcurador = Authenticator.getPapelAtual() == ParametroUtil
					.instance().getPapelAssistenteProcuradoria();
			if (!pd.getProcessoDocumentoBin().getSignatarios().isEmpty()
					&& pd.getAtivo()
					&& (pd.getTipoProcessoDocumento() == ParametroUtil
							.instance().getTipoProcessoDocumentoSentenca()
							|| pd.getTipoProcessoDocumento() == ParametroUtil
									.instance()
									.getTipoProcessoDocumentoAcordao()
							|| pd.getTipoProcessoDocumento() == ParametroUtil
									.instance()
									.getTipoProcessoDocumentoDespacho()
							|| pd.getTipoProcessoDocumento() == ParametroUtil
									.instance()
									.getTipoProcessoDocumentoDecisao() || pd
							.getTipoProcessoDocumento() == ParametroUtil
							.instance()
							.getTipoProcessoDocumentoAtoOrdinatorio())
					&& (isAdvogado || isAssistenteAdvogado || isProcurador || isAssistenteProcurador)) {
				StringBuilder sb;
				if (isAdvogado || isProcurador) {
					sb = new StringBuilder(
							"select count(o) from ProcessoExpediente o ");
					sb.append(" where o.dtExclusao is null and o.processoTrf = :pt ");
					sb.append("and exists (select a from ProcessoParteExpediente a ");
					sb.append(" where  a.dtCienciaParte != null and a.pessoaParte = :pp and a.processoExpediente = o) ");
				} else {
					if (isAssistenteAdvogado) {
						sb = new StringBuilder(
								" select count(a) from ProcessoExpediente a ");
						sb.append(" where a.dtExclusao is null and a.processoTrf = :pt ");
						sb.append(" and exists (select b from ProcessoParteExpediente b ");
						sb.append(" where b.dtCienciaParte != null and b.processoExpediente = a ");
						sb.append(" and b.pessoaParte = (select c.pessoa from PessoaLocalizacao c ");
						sb.append(" where c.localizacao.idLocalizacao = :loc ");
						sb.append(" and c.pessoa.idUsuario in (select d.idUsuario from PessoaAdvogado d) ) ) ");
					} else {
						sb = new StringBuilder(
								"select count(a) from ProcessoExpediente a ");
						sb.append(" where a.dtExclusao is null and a.processoTrf.idProcessoTrf = 560 ");
						sb.append(" and exists (select b from ProcessoParteExpediente b ");
						sb.append(" where b.dtCienciaParte != null and b.processoExpediente = a  ");
						sb.append(" and b.pessoaParte = (select d.pessoa from PessoaProcuradoriaEntidade d ");
						sb.append(" where d.procuradoria.idProcuradoria in ");
						sb.append(" (select e.idProcuradoria from Procuradoria e ");
						sb.append(" where e.localizacao.idLocalizacao = :loc))) ");
					}
				}

				Query query = EntityUtil.createQuery(sb.toString());
				query.setParameter("pt", ProcessoTrfHome.instance()
						.getInstance());

				if (isAdvogado || isProcurador) {
					query.setParameter("pp", Authenticator.getPessoaLogada());
				} else {
					query.setParameter("loc", Authenticator
							.getLocalizacaoAtual().getIdLocalizacao());
				}
				Long retorno = EntityUtil.getSingleResultCount(query);
				ret = retorno > 0;
			} else {
				ret = true;
			}
			validaDocumentoDetalheMap.put(pd.getIdProcessoDocumento(), true);
		}
		return ret;
	}

	public void setarProcessoDocumento(String idProcessoDocumento) {
		int id = Integer.parseInt(idProcessoDocumento);
		setId(id);
	}

	public List<ProcessoDocumento> getProcessoDocumentoBeanList() {
		return this.processoDocumentoBeanList;
	}

	/**
	 * Método que retorna o último documento.
	 * 
	 * @author Guilherme D. Bispo
	 */
	private ProcessoDocumento getUltimoProcessoDocumento() {
		List<ProcessoDocumento> lista = ProcessoHome.instance()
				.getProcessoDocumentoList();
		ProcessoDocumento ultimoProcessoDocumento = null;
		for (ProcessoDocumento procDocList : lista) {
			if (ultimoProcessoDocumento == null) {
				ultimoProcessoDocumento = procDocList;
			} else {
				if (ultimoProcessoDocumento.getDataInclusao().before(
						procDocList.getDataInclusao())) {
					ultimoProcessoDocumento = procDocList;
				}
			}
		}
		return ultimoProcessoDocumento;
	}

	/**
	 * Método que verifica se o último documento foi assinado.
	 * 
	 * @author Guilherme D. Bispo
	 */
	public Boolean isUltimoDocumentoAssinado() {
		ProcessoDocumento ultimoProcessoDocumento = getUltimoProcessoDocumento();
		if (ultimoProcessoDocumento == null) {
			return Boolean.TRUE;
		} else {
			return ProcessoHome.instance().isDocumentoAssinado(
					ultimoProcessoDocumento.getIdProcessoDocumento());
		}
	}

	public ProcessoDocumento getUltimoProcessoDocumentoNaoAssinado(ProcessoTrf processoTrf) {
		List<ProcessoDocumento> lista = processoTrf.getProcesso().getProcessoDocumentoList();
		ProcessoDocumento ultimoProcessoDocumentoNaoAssinado = null;
		for (ProcessoDocumento procDocList : lista) {
			if (ultimoProcessoDocumentoNaoAssinado == null) {
				if (ProcessoHome.instance().isDocumentoAssinado(procDocList.getIdProcessoDocumento()) == false) {
					ultimoProcessoDocumentoNaoAssinado = procDocList;
				}
			} else {
				if (ultimoProcessoDocumentoNaoAssinado.getDataInclusao().before(procDocList.getDataInclusao())) {
					if (ProcessoHome.instance().isDocumentoAssinado(procDocList.getIdProcessoDocumento()) == false) {
						ultimoProcessoDocumentoNaoAssinado = procDocList;
					}
				}
			}
		}
		return ultimoProcessoDocumentoNaoAssinado;
	}
	
	public void setCrescente(Boolean crescente) {
		this.crescente = crescente;
	}

	public Boolean getCrescente() {
		return crescente;
	}

	public Boolean getShowModalDownloadDocumentos() {
		return showModalDownloadDocumentos;
	}

	public void setShowModalDownloadDocumentos(Boolean showModalDownloadDocumentos) {
		this.showModalDownloadDocumentos = showModalDownloadDocumentos;
	}

	
	
	public void downloadDocumentosPdfUnificado(){
		downloadDocumentosPdfUnificado(true, true);
	}
	
	public void downloadDocumentosPdfUnificado(boolean isGerarIndiceDosDocumentos, boolean isAbrirPdfComoDownload) {
		List<ProcessoDocumento> processoDocumentoList = getProcessoDocumentoListSelected();

		final boolean ordenarCrescente = this.crescente;

		Comparator<ProcessoDocumento> comparator = new ProcessoDocumentoComparator();
		if(ordenarCrescente){
			Collections.sort(processoDocumentoList,	comparator);	
		}else{
			Collections.sort(processoDocumentoList,	 Collections.reverseOrder(comparator));
		}

		for (Iterator<ProcessoDocumento> iterator = processoDocumentoList
				.iterator(); iterator.hasNext();) {
			ProcessoDocumento processoDocumento = iterator.next();
			if (ProcessoDocumentoHome.isUsuarioExterno()
					&& ProcessoDocumentoHome.instance()
							.existePendenciaCienciaSemCache(processoDocumento)) {
				iterator.remove();
			}
		}

		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) facesContext
				.getExternalContext().getResponse();
		response.setContentType("application/pdf");

		String filename = (processoTrf.getNumeroProcesso() == null || processoTrf
				.getNumeroProcesso().isEmpty()) ? "minutaProcesso"
				: processoTrf.getNumeroProcesso();
		String extensao = ".pdf";
		if(isAbrirPdfComoDownload){
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ filename + extensao + "\"");
		}
		OutputStream out = null;
		try {
			GeradorPdfUnificado geradorPdf = new GeradorPdfUnificado();
			geradorPdf.setGerarIndiceDosDocumentos(isGerarIndiceDosDocumentos);
			geradorPdf.setResurcePath(new Util().getUrlProject());
			out = response.getOutputStream();
			geradorPdf.gerarPdfUnificado(processoTrf, processoDocumentoList, out);
			out.flush();
			
			getPjeUtil().registrarCookieTemporizadorDownload(response);
			facesContext.responseComplete();
		} catch (IOException ex) {
			FacesMessages.instance().add(Severity.ERROR,
					"Error while downloading the file: " + filename);
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private PjeUtil getPjeUtil() {
		return ComponentUtil.getComponent(PjeUtil.class);
	}

	public static boolean isUsuarioExterno() {
		if (Authenticator.getPapelAtual() == null) {
			return true;
		}
		return Authenticator.isUsuarioExterno()
				|| Authenticator.getPapelAtual().equals(
						ParametroUtil.instance().getPapelJusPostulandi());
	}

	public static boolean isUsuarioExterno(ProcessoDocumento pd) {
		Usuario u = pd.getUsuarioAlteracao() == null ? pd.getUsuarioInclusao()
				: pd.getUsuarioAlteracao();
		boolean usuarioExterno = false;

		if (u == null) {
			return usuarioExterno;
		} else if (u.getUsuarioLocalizacaoList() == null
				|| u.getUsuarioLocalizacaoList().isEmpty()) {
			usuarioExterno = true;
		} else {
			for (UsuarioLocalizacao ul : u.getUsuarioLocalizacaoList()) {
				Papel papel = ul.getPapel();
				usuarioExterno = (papel == null) || (Authenticator.isUsuarioExterno(papel));
				break;

			}
		}

		return usuarioExterno;
	}

	public List<ProcessoDocumento> getProcessoDocumentoListSelected() {
		List<ProcessoDocumento> selecionados = new ArrayList<ProcessoDocumento>();
		for (ProcessoDocumento processoDocumentoBean : getProcessoDocumentoBeanList()) {
			if (processoDocumentoBean.getSelected()) {
				selecionados.add(processoDocumentoBean);
			}
		}
		return selecionados;
	}

	public String getCodigoDocumento() {
		return codigoDocumento;
	}

	public void setCodigoDocumento(String codigoDocumento) {
		this.codigoDocumento = codigoDocumento;
	}

	public void apreciarDocumentoAutomaticamente() {
		if (Authenticator.isUsuarioInterno()) {
			ProcessoDocumentoLidoHome pdlHome = ComponentUtil.getComponent(ProcessoDocumentoLidoHome.class);
			GridQuery grid = (GridQuery) Component.getInstance(pdlHome
					.getIdGridQuery());
			grid.getSelectedRowsList().add(getInstance());

			pdlHome.persist();
		}
		
		ProcessoDocumento pd = getInstance(); 
		EntityUtil.getEntityManager().refresh(pd);
		if(pd != null && pd.getProcessoTrf().getProcessoStatus() == ProcessoStatusEnum.D && pd.getDataJuntada() == null) {
			pd.setDataJuntada(new Date());
			try {
				getProcessoDocumentoManager().persistAndFlush(pd);
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao registrar a data de juntada do documento");
				getLog().error(e);
			}
		}
	}
	
	/**
	 * Verifica se o documento do processo foi produzido pelo AUD.
	 * 
	 * @param processoDocumento
	 *            documento do processo.
	 * @return Boolean true caso o documento tenha sido produzido pelo AUD,
	 *         false caso contrario.
	 * @author Athos Reiser
	 */
	public Boolean verificaDocumentoGeradoAUD(
			ProcessoDocumento processoDocumento) {
		String idTipoProcessoDocumento = ParametroUtil.getFromContext(
				"idTipoProcessoDocumentoAtaAudiencia", true);

		if (idTipoProcessoDocumento == null) {
			return false;
		}

		return Integer.parseInt(idTipoProcessoDocumento) == processoDocumento
				.getTipoProcessoDocumento().getIdTipoProcessoDocumento();
	}

	public Boolean verificaVinculacaoRelatorio(ProcessoDocumento pd) {
		if (pd == null)
			return false;
		boolean resultado = false;
		int idTipoProcessoDocumentoRelatorio = ParametroUtil.instance()
				.getIdTipoProcessoDocumentoRelatorio();

		if (pd.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == idTipoProcessoDocumentoRelatorio) {
			if (pd.getUsuarioInclusao()
					.equals(Authenticator.getUsuarioLogado())) {
				resultado = true;
			}
		}

		return resultado;
	}

	public Boolean verificaAssinaturaMinutaEmElaboracao() {
		Integer id = null;
		if (ProcessInstance.instance() != null) {
			id = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(TaskInstance.instance());
		}

		if (id != null) {
			ProcessoDocumento processoDocumento = EntityUtil.getEntityManager()
					.find(ProcessoDocumento.class, id);
			if (processoDocumento != null) {
				if (processoDocumento.getProcessoDocumentoBin().getSignatarios().isEmpty()) {
					return Boolean.FALSE;
				} else {
					return Boolean.TRUE;
				}

			} else {
				return Boolean.TRUE;
			}
		}
		return Boolean.TRUE;
	}

	public Boolean verificaAssinaturaMinutaEmElaboracao(ProcessoDocumento pd) {
		if (pd == null)
			return false;
		Boolean teste = verificaAssinaturaMinutaEmElaboracao();
		Long idBpm = pd.getProcesso().getIdJbpm();
		Long idTaskBpm = pd.getIdJbpmTask();
		if (idBpm != null) {
			org.jbpm.graph.exe.ProcessInstance pi = null;
			if (idTaskBpm != null) {
				pi = ManagedJbpmContext.instance().getTaskInstance(idTaskBpm)
						.getProcessInstance();
			} else {
				pi = ManagedJbpmContext.instance().getProcessInstance(idBpm);
			}

			Integer j = (Integer) pi.getContextInstance().getVariable(
					Variaveis.MINUTA_EM_ELABORACAO);

			if (j != null) {
				teste = Boolean.FALSE;
			} else {
				return Boolean.FALSE;
			}
			if (teste.equals(Boolean.FALSE)) {
				if (pd.getIdProcessoDocumento() == j.intValue()) {
					teste = Boolean.TRUE;
				}
			}
		}
		if (teste && idBpm == null) {
			return Boolean.FALSE;
		}
		return teste;
	}
	
	public Boolean isPeticaoInicial(int id) {
		boolean resultado = false;
		try {
			ProcessoDocumento processoDocumento = this.instance;
			if (processoDocumento == null
					|| id != processoDocumento.getIdProcessoDocumento()) {
				processoDocumento = EntityUtil.getEntityManager().find(
						ProcessoDocumento.class, id);
			}
			int idTipoDocPetInicial = Integer.parseInt(ParametroUtil.getParametro(Parametros.ID_TIPO_PROCESSO_DOCUMENTO_PETICAO_INICIAL));
			if (processoDocumento != null) {
				resultado = (idTipoDocPetInicial == processoDocumento
						.getTipoProcessoDocumento()
						.getIdTipoProcessoDocumento());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultado;
	}
	
	/**
	 * Método que retorna se um documento possui expedientes que necessitam de
	 * ciência, de acordo com as regras da [PJEII-3117]
	 * 
	 * @param documento
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Boolean existePendenciaCiencia(ProcessoDocumento documento) {
		
		List<Integer> lista = (List<Integer>)Contexts.getConversationContext().get(ProcessoDocumentoGridQuery.MAPA);
		
		if (documento == null || documento.getIdProcessoDocumento() == 0) {
			return false;
		}
		
		if(lista == null || lista.isEmpty()){
			lista = new ArrayList<Integer>();
			lista.add(documento.getIdProcessoDocumento());
		}
		
		if (mapaPendenciaCiencia == null) {
			mapaPendenciaCiencia = new HashMap<Integer, Boolean>();
			
		}
		
		if (!mapaPendenciaCiencia.containsKey(documento
				.getIdProcessoDocumento())) {
			ProcessoParteExpedienteManager processoParteExpedienteManager = ComponentUtil.getComponent(ProcessoParteExpedienteManager.class);
			List<Integer> listaRetorno = processoParteExpedienteManager.mapaExpedientesPendentes(lista);
			for(Integer i : lista){
				mapaPendenciaCiencia.put(i, listaRetorno.contains(i));
			}
		}
		
		return mapaPendenciaCiencia.get(documento.getIdProcessoDocumento());
	}

	public boolean existePendenciaCienciaSemCache(ProcessoDocumento documento) {
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		return atoComunicacaoService.contagemExpedientesPendentesCiencia(documento) > 0;
	}
	
	@Observer(value = { Eventos.EVENTO_CIENCIA_DADA })
	public void removerCachePendenciaCiencia(ProcessoTrf processo) {
		if (mapaPendenciaCiencia != null) {
			mapaPendenciaCiencia.clear();
		}
	}
	
	/**
	 * PJEII-5514
	 * Método responsável por verificar se o sistema pode exibir a informação "Juntado por", na 
	 * certidão de juntada de documento, para o tipo de documento do objeto "ProcessoDocumento"
	 * @param processoDocumento
	 * @return
	 */
	public Boolean exibirInformacaoJuntadoPor(ProcessoDocumento processoDocumento) {
		String idsTiposDocumento = ParametroUtil.instance().getNaoExibirInformacaoJuntadoPorIds();
		
		if (idsTiposDocumento != null && idsTiposDocumento.trim().length() > 0) {		
			String[] arrayIdsTiposDocumento = idsTiposDocumento.split("\\,");
			
			for (int i = 0; i < arrayIdsTiposDocumento.length; i++) {
				try {
					int idTipoDocumento = Integer.valueOf(arrayIdsTiposDocumento[i].trim());
					
					// Se o ID do tipo de documento foi encontrado no parâmetro "pje:processo:consulta:documentos:naoExibirInformacaoJuntadoPor:ids"...
					if (processoDocumento.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == idTipoDocumento) {
						return false;
					}
				} catch (NumberFormatException e) {
					FacesMessages.instance().add(Severity.ERROR, "O valor do parâmetro 'pje:processo:consulta:documentos:naoExibirInformacaoJuntadoPor:ids' é inválido. Ele deve conter os ID's dos tipos de documento separados pelo caracter ','.");
					System.out.println("O valor do parâmetro 'pje:processo:consulta:documentos:naoExibirInformacaoJuntadoPor:ids' é inválido. Ele deve conter os ID's dos tipos de documento separados pelo caracter ','.");
					getLog().error(e);
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Busca modelos de documentos da localização física do usuário logado, independentemente se este é interno ou externo
	 * Os modelos buscados são aqueles das localizações superiores ou inferiores do usuário logado
	 * ver: http://www.pje.jus.br/wiki/index.php/Regras_de_neg%C3%B3cio#RN298 
	 * @return Lista com modelos de documentos filtrado por tipo e localizacao
	 */
	public List<ModeloDocumento> getModelosDocumentos() throws PJeBusinessException
	{
		if (this.instance.getTipoProcessoDocumento() != null)
		{
			Localizacao localizacaoFisicaUsuario = Authenticator.getLocalizacaoFisicaAtual();
			LocalizacaoService localizacaoService = ComponentUtil.getComponent(LocalizacaoService.class, true);
			List<Localizacao> listLocalizacoes = localizacaoService.getLocalizacoesAscendentesDescendentes(localizacaoFisicaUsuario);
			ModeloDocumentoManager modeloDocumentoManager = ComponentUtil.getComponent(ModeloDocumentoManager.class);

			return modeloDocumentoManager.getModelos(this.instance.getTipoProcessoDocumento(), listLocalizacoes);
		}
		else
		{
			return null; 
		}
	}

	/**
	 * PJEII-18075
	 * Método responsável por recuperar a lista de documentos do processo especificado
	 * @param idProcesso Código identificador do processo
	 * @return Lista de documentos
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> listarDocumentosDoProcesso(int idProcesso) {
		List<ProcessoDocumento> listaDeDocumentos = new ArrayList<ProcessoDocumento>();
		GridQuery query = ComponentUtil.getComponent("processoTrfDocumentoGrid");
		
		// ORDER BY ADICIONADO PARA A ISSUE PJEII-1297 em 30/05/2012 por Rafael Barros
		// Query q = EntityUtil.createQuery(hql.toString() + " and o.ativo = true order by o.idProcessoDocumento ");

		// ORDER BY MODIFICADO PARA A ISSUE PJEII-3621 em 13/11/2012 por
		// Fernando Barreira
		Query q = EntityUtil
				.createQuery(query.getEjbql()
						+ " and o.processo.idProcesso = ?"
						+ " and o.dataJuntada is not null"
						+ " and o.ativo = true order by o.dataJuntada desc, o.dataInclusao desc");
		
		q.setParameter(1, idProcesso);
		List<ProcessoDocumento> lista = q.getResultList();
		
		for (ProcessoDocumento processoDocumento : lista) {
			if(Identity.instance().hasRole(Papeis.INTERNO) || !existePendenciaCiencia(processoDocumento)) {
				listaDeDocumentos.add(processoDocumento);
			}
		}
		
		return listaDeDocumentos;
	}
	
	/*PJEII-19550 - Exibir na modal de download de documentos pdf, aviso de que o processo possui documentos sigilosos*/
	public boolean verificaNecessidadeDoAvisoDocumentoSigiloso() {
		/*
		 * verifica se deve mostrar o painel do aviso na modal de download de arquivo
		 * 
		 * retorna true se tiver um documento sigiloso ou nao assinado
		 */
		if(processoDocumentoBeanListSelecionados.isEmpty()) {
			processoDocumentoBeanListSelecionados = listarDocumentosGrid();
		}
		
		for (ProcessoDocumento processoDocumento : processoDocumentoBeanListSelecionados) {
			if(!haDocSigiloso) {//if para realizar o procedimento somente ate encontrar um doc sigiloso
				haDocSigiloso = verificaSeDocumentoEhSigiloso(processoDocumento);
			}
			if(!haDocSemAssinar) {
				//faz busca no banco para procurar assinaturas --ProcessoDocumentoBinPessoaAssinatura
				haDocSemAssinar = verificaSeDocumentoNaoContemDataJuntada(processoDocumento);
			}
		}
		if(haDocSigiloso || haDocSemAssinar) {
			return true;
		} else {
			return false;
		}
	}
	
	/*PJEII-19550 - Exibir na modal de download de documentos pdf, aviso de que o processo possui documentos sigilosos*/
	private boolean verificaSeDocumentoEhSigiloso(ProcessoDocumento processoDocumento) {
		return processoDocumento.getDocumentoSigiloso();
	}
	
	/*PJEII-19550 - Exibir na modal de download de documentos pdf, aviso de que o processo possui documentos sigilosos*/
	private boolean verificaSeDocumentoNaoContemDataJuntada(ProcessoDocumento processoDocumento) {
		/*
		 * metodo que verifica se o documento NAO contem data de juntada
		 * retorna true se nao tiver data de juntada
		 */
		if(processoDocumento.getDataJuntada() == null) {//se documento tem data de juntada, logo esta assinado
			return true;
		} else {
			return false;
		}
	}
	
	/*PJEII-19550 - Exibir na modal de download de documentos pdf, aviso de que o processo possui documentos sigilosos*/
	public String montaFraseAvisoDocSigilosoDocNaoAssinado() {
		/* método que monta a frase do AVISO dependendo de haver documentos sigilosos e/ou nao assinados */
		String resposta = "Há documentos";

		if(haDocSigiloso) {
			resposta = resposta + " sigilosos";
		}
		
		if(haDocSigiloso && haDocSemAssinar) {
			resposta = resposta + " e ";
		}
		
		if(haDocSemAssinar) {
			resposta = resposta + " não assinados";
		}
		resposta +=" neste processo.";
		return resposta;
	}
	
	/*PJEII-19550 - Exibir na modal de download de documentos pdf, aviso de que o processo possui documentos sigilosos*/
	public String retornaStyleDocSigilosoOrUnassigned(ProcessoDocumento documento) {
		/*
		 * metodo que retorna uma STRING com o CSS para que a frase fique vermelha e em negrito, sobressaindo o documento sigiloso ou nao assinado
		 */
		if(verificaSeDocumentoEhSigiloso(documento) || verificaSeDocumentoNaoContemDataJuntada(documento)) {
			return "color: red !important; font-weight: bold;";
		} else {
			return "";
		}
	}	
	
	/**
	 * Método responsável por recuperar a lista de documentos do processo especificado a ser listada no Grid 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> listarDocumentosGrid() {
		
		GridQuery query = ComponentUtil.getComponent("processoTrfDocumentoGrid");
		StringBuffer hql = new StringBuffer(query.getEjbql());

		hql.append(" AND o.processo.idProcesso = #{processoHome.instance.idProcesso} ")
				.append(" AND o.ativo = true ")
				.append(" AND o.dataJuntada IS NOT NULL")
				.append(" ORDER BY o.dataJuntada DESC, o.dataInclusao DESC" );
		
		Query q = EntityUtil.createQuery(hql.toString());			
		List<ProcessoDocumento> pesquisaDocumentos = q.getResultList();
		
		if (pesquisaDocumentos != null && isUsuarioExterno()) {
			// retira da lista os documentos com pendência de ciência quando algum usuário externo estiver acessando
			List<ProcessoDocumento> documentosFiltrados = new ArrayList<ProcessoDocumento>();
			for (ProcessoDocumento processoDocumento : pesquisaDocumentos) {
				if (!existePendenciaCienciaSemCache(processoDocumento)) {
					documentosFiltrados.add(processoDocumento);
				}			
			}
			pesquisaDocumentos = documentosFiltrados;
		}
		
		this.processoDocumentoBeanList = pesquisaDocumentos;
		
		return this.processoDocumentoBeanList;
	}
	
	public void exibirTodosOsDocumentosNoPaginadorEmPDF(List<ProcessoDocumento> listaDeDocumentos) {
		marcarTodosOsDocumentosComoSelecionados(listaDeDocumentos);
		setCrescente(false);
		boolean isGerarIndiceDosDocumentos = false;
		boolean isAbrirPdfComoDownload = false;
		downloadDocumentosPdfUnificado(isGerarIndiceDosDocumentos, isAbrirPdfComoDownload);
	}
	
	private void marcarTodosOsDocumentosComoSelecionados(List<ProcessoDocumento> listaDeDocumentos) {
		listarDocumentosGrid(listaDeDocumentos);
		selecionarTodosOsDocumentos();
	}
	
	private void selecionarTodosOsDocumentos() {
		for (ProcessoDocumento processoDocumentoBean : getProcessoDocumentoBeanList()) {
			processoDocumentoBean.setSelected(true);
		}
	}
	
	public List<ProcessoDocumento> listarDocumentosGrid(List<ProcessoDocumento> listaDeDocumentos) {
		this.processoDocumentoBeanList = listaDeDocumentos;
		return this.processoDocumentoBeanList;
	}
	
	/**
	 * Método responsável por verificar se dentre os documentos selecionados pelo usuário
	 * existe algum que é sigoloso ou não foi juntado ao processo.
	 */
	public void verificarDocumentosSelecionados() {
		this.msgAlert = null;
		String msgSigiloso = StringUtils.EMPTY, msgNaoAssinado = StringUtils.EMPTY;
		
		List<ProcessoDocumento> processoDocumentoList = getProcessoDocumentoListSelected();
		for (ProcessoDocumento processoDocumento : processoDocumentoList) {
			if (msgSigiloso.isEmpty() && processoDocumento.getDocumentoSigiloso() == true) {
				msgSigiloso = "são sigilosos";
			} 
			if (msgNaoAssinado.isEmpty() && processoDocumento.getDataJuntada() == null) {
				msgNaoAssinado = "não estão assinados";
			}
		}
		
		if (!msgSigiloso.isEmpty() || !msgNaoAssinado.isEmpty()) {
			this.msgAlert = String.format("Existem documentos selecionados que %s %s %s. Deseja continuar?", 
					msgSigiloso, (!msgSigiloso.isEmpty() && !msgNaoAssinado.isEmpty() ? "e" : StringUtils.EMPTY), msgNaoAssinado);
		}
	}
	
	/**
	 * Recupera uma lista de documentos (documento principal e os documentos vinculados ao principal).
	 * 
	 * return Lista de documentos (documento principal e os documentos vinculados ao principal)
	 */
	public List<ProcessoDocumento> recuperarDocumentos() {
		ProcessoDocumento documentoPrincipal = getInstance();
		List<ProcessoDocumento> documentos = new ArrayList<ProcessoDocumento>();
		documentos.add(documentoPrincipal);
		for(ProcessoDocumento documento : documentoPrincipal.getDocumentosVinculados()){
			documentos.add(documento);
		}
		return documentos;
	}

	public void pesquisarDocumentos() {
		refreshGridsDocumentos();
	}
	
	
	public String recuperaMensagemDocumentoCriadoViaFluxo() {
		String retorno = "Não foi possível verificar se o documento foi criado por meio de tarefas de fluxo";
		Boolean documentoCriadoFluxo = null;
		if(this.isManaged() && instance != null) {
			try {
				documentoCriadoFluxo = getProcessoDocumentoManager().verificarDocumentoDeAtividadeEspecifica(instance);
			} catch( Exception e ) {
				log.info("Não foi possível verificar se o documento foi criado por meio de tarefas de fluxo: "+ e.getMessage());
			}
			if( documentoCriadoFluxo ) {
				retorno = "*Este documento foi criado em uma tarefa de fluxo e não pode ser assinado neste local";
			} else {
				retorno = "";
			}
		} 
		return retorno;
	}
	
	/**
	 * Método responsável por verificar se o documento foi criado em alguma tarefa de fluxo.
	 * 
	 * @return Verdadeiro se o documento foi criado em alguma tarefa de fluxo. Falso, caso contrário.
	 */
	public Boolean isDocumentoCriadoViaFluxo(){
		Boolean retorno = false;
		if(this.isManaged() && instance != null){
			try {
				retorno = getProcessoDocumentoManager().verificarDocumentoDeAtividadeEspecifica(instance);
			} catch (Exception e) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR,"Não foi possível verificar se o documento foi criado por meio de tarefas de fluxo");
				log.info("Não foi possível verificar se o documento foi criado por meio de tarefas de fluxo: "+ e.getMessage());
			}
		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR,"Não foi possível verificar se o documento foi criado por meio de tarefas de fluxo");
		}
		return retorno;
	}
	
	private DocumentoJudicialService getDocumentoJudicialService() {
		return ComponentUtil.getComponent(DocumentoJudicialService.class);
	}
	
	private ParametroService getParametroService() {
		return ComponentUtil.getComponent(ParametroService.class);
	}
	
	private ProcessoDocumentoManager getProcessoDocumentoManager() {
		return ComponentUtil.getComponent(ProcessoDocumentoManager.class);
	}

	
	public Boolean apenasSigilososMarcados() {		
		Boolean retornoMetodo = Boolean.TRUE;
		
		if (listaSigilo == null || listaSigilo.isEmpty()) {
			retornoMetodo = Boolean.FALSE;
		}
		
		for (ProcessoDocumento processoDocumento : listaSigilo) {
			if (!processoDocumento.getDocumentoSigiloso()) {
				retornoMetodo = Boolean.FALSE;
			}
		}
		return retornoMetodo;
	}

	private void lancarMovimentacaoExclusaoDocumento() {
		MovimentoAutomaticoService.preencherMovimento()
			.deCodigo(CodigoMovimentoNacional.CODIGO_MOVIMENTO_DESENTRANHAMENTO_DOCUMENTO)
			.comProximoComplementoVazio()
			.doTipoLivre()
			.preencherComTexto(obterDescricaoDocumento(getInstance()))
			.associarAoProcesso(getInstance()
			.getProcessoTrf())
			.lancarMovimento();
	}

	private String obterDescricaoDocumento(final ProcessoDocumento documento) {
		StringBuilder nome = new StringBuilder();
		nome.append("(ID: ");
		nome.append(documento.getIdProcessoDocumento());
		if (StringUtil.isNotEmpty(documento.getProcessoDocumento())) {
			nome.append(" - ");
			nome.append(documento.getProcessoDocumento());
		} else if (documento.getTipoProcessoDocumento() != null) {
			nome.append(" - ");
			nome.append(documento.getTipoProcessoDocumento().getTipoProcessoDocumento());
		}
		nome.append(")");
		return nome.toString();

	}

	public List<ProcessoDocumento> consultarDocumentosPorQrCode(String parametros) throws InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		parametros = new String(Base64.getDecoder().decode(parametros));
		parametros = EncryptionSecurity.decrypt(parametros);
		String[] arrayParametros = parametros.split("&");
		Integer idProcessoTrf = Integer.parseInt(arrayParametros[0]);
		Integer idTipoProcessoDocumento = Integer.parseInt(arrayParametros[1]);
		Boolean exibirApenasUltimoDocumento = Boolean.parseBoolean(arrayParametros[2]);
		StringBuffer hql = new StringBuffer("SELECT pd FROM ProcessoDocumento pd");
		hql.append(" WHERE pd.processo.idProcesso = :idProcessoTrf");
		hql.append(" AND EXISTS (SELECT 1 FROM ProcessoTrf pTrf WHERE pTrf.processo = pd.processo AND pTrf.segredoJustica = false)");


		if (idTipoProcessoDocumento != 0) {
			hql.append(" AND pd.tipoProcessoDocumento.idTipoProcessoDocumento = :idTipoProcessoDocumento");
		}

		hql.append(" AND pd.ativo = true AND pd.documentoSigiloso = false");
		hql.append(" AND pd.dataJuntada IS NOT NULL");
		hql.append(" ORDER BY pd.dataJuntada DESC, pd.dataInclusao DESC");

		Query q = EntityUtil.createQuery(hql.toString());
		q.setParameter("idProcessoTrf", idProcessoTrf);

		if (idTipoProcessoDocumento != 0) {
			q.setParameter("idTipoProcessoDocumento", idTipoProcessoDocumento);
		}

		List<ProcessoDocumento> pesquisaDocumentos = q.getResultList();

		// Retirar da lista os documentos com pendncia de cincia quando algum usurio externo estiver acessando
		documentosFiltrados = new ArrayList<ProcessoDocumento>();

		for (ProcessoDocumento processoDocumento : pesquisaDocumentos) {
			if (!existePendenciaCienciaSemCache(processoDocumento)) {
				documentosFiltrados.add(processoDocumento);
				documentosFiltrados.addAll(
					processoDocumento.getDocumentosVinculados().stream()
					.filter(doc -> !doc.getDocumentoSigiloso())
					.collect(Collectors.toList()));

				if (exibirApenasUltimoDocumento) break;
			}
		}

		return documentosFiltrados;
	}

	public List<ProcessoDocumento> getDocumentosFiltrados() {
		return documentosFiltrados;
	}
}
