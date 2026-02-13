package br.com.infox.editor.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.persistence.Query;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.TransactionPropagationType;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.remoting.WebRemote;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.editor.bean.AnotacaoBean;
import br.com.infox.editor.bean.DadosEditor;
import br.com.infox.editor.bean.DadosTopico;
import br.com.infox.editor.bean.Estilo;
import br.com.infox.editor.bean.TabelaTopicosBean;
import br.com.infox.editor.interpretadorDocumento.LinguagemFormalException;
import br.com.infox.editor.manager.AnotacaoManager;
import br.com.infox.editor.manager.CssDocumentoManager;
import br.com.infox.editor.manager.PreferenciaManager;
import br.com.infox.editor.manager.ProcessoDocumentoEstruturadoTopicoManager;
import br.com.infox.editor.service.EditorService;
import br.com.infox.editor.service.NumeracaoDocumentoService;
import br.com.infox.editor.tree.RichHierarchicalListTree;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.action.ContingenciaAction;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.editor.Anotacao;
import br.jus.pje.nucleo.entidades.editor.Preferencia;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;
import br.jus.pje.nucleo.entidades.editor.topico.ITopicoComConclusao;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoItemConsideracoes;
import br.jus.pje.nucleo.enums.editor.PreferenciaEditorEnum;
import br.jus.pje.nucleo.enums.editor.StatusAnotacao;
import br.jus.pje.nucleo.enums.editor.TipoAnotacao;
import br.jus.pje.nucleo.enums.editor.TipoTopicoEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(DocumentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class DocumentoAction implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "documentoAction";

	@In(create = true)
	private EditorAction editorAction;
	
	@In(value = ProcessoDocumentoEstruturadoTopicoManager.NAME)
	private ProcessoDocumentoEstruturadoTopicoManager topicoManager;
	
	@In
	private EditorService editorService;
	
	@In
	private NumeracaoDocumentoService numeracaoDocumentoService;
	
	@In
	private CssDocumentoManager cssDocumentoManager;
	
	@In(create = true)
	private ContingenciaAction contingenciaAction;
	
	@In
	private PreferenciaManager preferenciaManager;

	private Usuario usuarioLogado = Authenticator.getUsuarioLogado();
	
	@In
	private AnotacaoManager anotacaoManager;
	
	private OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
	
	private RichHierarchicalListTree<ProcessoDocumentoEstruturadoTopico> tree = new RichHierarchicalListTree<ProcessoDocumentoEstruturadoTopico>();
	private Map<Integer, ProcessoDocumentoEstruturadoTopico> topicos = new HashMap<Integer, ProcessoDocumentoEstruturadoTopico>(0);
	private List<Estilo> estilos;
	private Map<Integer, Anotacao> anotacoes = new HashMap<Integer, Anotacao>(0);
	private boolean exibirAnotacoes = true;
	
	private String textoEmentaMigracao = "";
	private String textoRelatorioMigracao = "";
	private String textoFundamentacaoMigracao = "";
	private String textoDispositivoMigracao = "";
	private String textoAlertMigracao = "";
	private List<Map<String, Object>> textoVotoRevisoresMigracao; 
	
	private boolean abrirSomenteLeitura = false;
	private boolean habilitarAnotacoes = true;
	private boolean habilitarDivergencias = true;
	private boolean habilitarDestaques = true;
	
	@WebRemote
	@Begin(flushMode = FlushModeType.MANUAL, join = true)
	public void begin() {
		if (getDocumento() != null) {
			refreshTree();
			anotacoes.clear();
			topicos.clear();
			anotacaoManager.setAnotacoesDoDocumento(null);
		}
	}
	
	@WebRemote
	public void setIdDocumento(Integer idDocumento) {
		editorAction.setDocumento(EntityUtil.find(ProcessoDocumentoEstruturado.class, idDocumento));
		refreshTree();
	}
	
	@WebRemote
	public DadosEditor getDadosEditor() {
		DadosEditor dados = new DadosEditor();
		
		dados.setParametros(construirParametrosEditor());
		dados.setHtml(editorService.getHtmlEditor(getDocumento()));
		
		dados.setTopicos(buildTopicos());
		dados.setTabelaTopicos(buildTabelaTopicos());
		dados.setAnotacoes(buildAnotacoes());
		dados.setEstilos(getEstilos());
		dados.setTemplates(getTemplates());
		
		return dados;
	}

	private Map<Integer, AnotacaoBean> buildAnotacoes() {
		buildAnotacoesMap();
		Map<Integer, AnotacaoBean> anotacoesBeanMap = new HashMap<Integer, AnotacaoBean>();
		for (Anotacao anotacao : anotacoes.values()) {
			anotacoesBeanMap.put(anotacao.getCodigoIdentificador(), construirAnotacaoBean(anotacao));
		}
		return anotacoesBeanMap;
	}
	
	private Map<Integer, DadosTopico> buildTopicos() {
		buildTopicosMap();
		Map<Integer, DadosTopico> dadosTopicos = new HashMap<Integer, DadosTopico>();
		for (ProcessoDocumentoEstruturadoTopico topico: getDocumento().getProcessoDocumentoEstruturadoTopicoList()) {
			dadosTopicos.put(topico.getCodIdentificador(), construirDadosTopico(topico));
		}
		return dadosTopicos;
	}
	
	private Map<Integer, TabelaTopicosBean> buildTabelaTopicos() {
		Map<Integer, TabelaTopicosBean> tabelaTopicosBeanMap = new HashMap<Integer, TabelaTopicosBean>();
		for (ProcessoDocumentoEstruturadoTopico topico: getDocumento().getProcessoDocumentoEstruturadoTopicoList()) {
			tabelaTopicosBeanMap.put(topico.getCodIdentificador(), construirTabelaTopicosBean(topico));
		}
		return tabelaTopicosBeanMap;
	}
	
	private Map<String, Object> construirParametrosEditor() {
		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("idDocumento", getDocumento().getIdProcessoDocumentoEstruturado());
		parametros.put("idDocumentoBin", getDocumento().getProcessoDocumentoBin().getIdProcessoDocumentoBin());
		
		Preferencia zoom = preferenciaManager.getPreferenciaPorUsuario(usuarioLogado, PreferenciaEditorEnum.ZO);
		String porcentagemZoom;
		if (zoom == null) {
			porcentagemZoom = "100";
		} else  {
			porcentagemZoom = zoom.getValor();
		}
		parametros.put("porcentagemZoom", porcentagemZoom);
		
		parametros.put("idOrgaoJulgador", orgaoJulgadorAtual.getIdOrgaoJulgador());
		parametros.put("abrirSomenteLeitura", isAbrirSomenteLeitura());
		parametros.put("habilitarAnotacoes", isHabilitarAnotacoes());
		parametros.put("habilitarDivergencias", isHabilitarDivergencias());
		parametros.put("habilitarDestaques", isHabilitarDestaques());
		parametros.put("textoEmentaMigracao", getTextoEmentaMigracao());
		parametros.put("textoRelatorioMigracao", getTextoRelatorioMigracao());
		parametros.put("textoFundamentacaoMigracao", getTextoFundamentacaoMigracao());
		parametros.put("textoDispositivoMigracao", getTextoDispositivoMigracao());
		parametros.put("textoAlertMigracao", getTextoAlertMigracao());
		parametros.put("textoVotoRevisoresMigracao", getTextoVotoRevisoresMigracao());

		return parametros;
	}

	@WebRemote
	@Transactional(TransactionPropagationType.REQUIRED)
	public DadosEditor salvarDocumento(List<DadosTopico> dadosTopicos, List<AnotacaoBean> anotacoesBean) {
		atualizarTopicos(dadosTopicos);
		atualizarAnotacoes(anotacoesBean);
		
		// Evita a exceção HibernateException "Found two representations of the same collection"
		Session session = (Session) EntityUtil.getEntityManager().getDelegate();
		session.evict(getDocumento());
		
		boolean isNovoDocumento = getDocumento().getIdProcessoDocumentoEstruturado() == null;
		editorAction.save();

		if (!isNovoDocumento) {
			return null;
		} else {
			return getDadosEditor();
		}
	}
	
	@Transactional(TransactionPropagationType.REQUIRED)
	public void salvarDocumento() {
		begin();
		DadosEditor dadosEditor = getDadosEditor();
		List<DadosTopico> dadosTopicos = new ArrayList<DadosTopico>(dadosEditor.getTopicos().values());
		List<AnotacaoBean> dadosAnotacoes = new ArrayList<AnotacaoBean>(dadosEditor.getAnotacoes().values());
		
		salvarDocumento(dadosTopicos, dadosAnotacoes);
	}
	
	@WebRemote
	public DadosEditor importarDocumento(List<DadosTopico> dadosTopicos, List<AnotacaoBean> anotacoesBean) {
		atualizarTopicos(dadosTopicos);
		atualizarAnotacoes(anotacoesBean);
		
		if(!getDocumento().getEstruturaDocumento().getEstruturaDocumento().equals("Acórdão legado")) {
			textoAlertMigracao = "Para realizar a importação, a estrutura do documento deve ser Acórdão legado";
		}
		
		else {
			List<String> textoTopicos = editorService.getVotoAntigosRelator(getDocumento().getProcessoTrf());
			
			textoEmentaMigracao = textoTopicos.get(0);
			textoRelatorioMigracao = textoTopicos.get(1);
			textoFundamentacaoMigracao = textoTopicos.get(2);
			textoDispositivoMigracao = textoTopicos.get(3);
			textoAlertMigracao = textoTopicos.get(4);
			
			textoVotoRevisoresMigracao = editorService.getVotosAntigosRevisores(getDocumento().getProcessoTrf());
		}
		
		return getDadosEditor();
	}
	
	@WebRemote
	public DadosEditor mudarHabilitacaoTopico(DadosTopico dadosTopico, List<DadosTopico> dadosTopicos, List<AnotacaoBean> anotacaoBeans) {
		ProcessoDocumentoEstruturadoTopico topico = topicos.get(dadosTopico.getId());
		if (!topico.getTopico().isOpcional()) {
			throw new RuntimeException("Não foi possível " + (topico.isHabilitado() ? "desabilitar" : "habilitar") + " o tópico");
		}
		topico.setHabilitado(!topico.isHabilitado());
		if (!topico.isHabilitado()) {
			topico.setNumerado(false);
		} else {
			topico.setNumerado(topico.getEstruturaDocumentoTopico().isNumerado());
		}
		for (ProcessoDocumentoEstruturadoTopico topicoFilho : tree.getChildren(topico)) {
			topicoFilho.setHabilitado(topico.isHabilitado());
			topicoFilho.setNumerado(topico.isNumerado());
		}
		renumerarTopicos();
		atualizarTopicos(dadosTopicos);
		atualizarAnotacoes(anotacaoBeans);
		return getDadosEditor();
	}
	
	@WebRemote
	public DadosTopico recarregarTopico(DadosTopico dadosTopico) {
		ProcessoDocumentoEstruturadoTopico topico = topicos.get(dadosTopico.getId());
		try {
			topicoManager.recarregarConteudoTopico(topico);
			dadosTopico.setConteudo(topico.getConteudo());
		} catch (LinguagemFormalException e) {
			e.printStackTrace();
		}
		return dadosTopico;
	}
	
	@WebRemote
	public DadosEditor moverParaCima(DadosTopico dadosTopico, List<DadosTopico> dadosTopicos, List<AnotacaoBean> anotacaoBeans) {
		ProcessoDocumentoEstruturadoTopico topico = topicos.get(dadosTopico.getId());
		if (!podeMoverParaCima(topico)) {
			throw new RuntimeException("Não foi possível mover o tópico");
		}
		tree.moveUp(topico);
		getDocumento().setProcessoDocumentoEstruturadoTopicoList(tree.getHierarchicalList());
		atualizarTopicos(dadosTopicos);
		atualizarAnotacoes(anotacaoBeans);
		return getDadosEditor();
	}
	
	@WebRemote
	public DadosEditor moverParaBaixo(DadosTopico dadosTopico, List<DadosTopico> dadosTopicos, List<AnotacaoBean> anotacaoBeans) {
		ProcessoDocumentoEstruturadoTopico topico = topicos.get(dadosTopico.getId());
		if (!podeMoverParaBaixo(topico)) {
			throw new RuntimeException("Não foi possível mover o tópico");
		}
		tree.moveDown(topico);
		getDocumento().setProcessoDocumentoEstruturadoTopicoList(tree.getHierarchicalList());
		atualizarTopicos(dadosTopicos);
		atualizarAnotacoes(anotacaoBeans);
		return getDadosEditor();
	}
	
	@WebRemote
	public DadosEditor removerTopico(DadosTopico dadosTopico, List<DadosTopico> dadosTopicos, List<AnotacaoBean> anotacaoBeans) {
		ProcessoDocumentoEstruturadoTopico topico = topicos.get(dadosTopico.getId());
		if (!podeRemoverTopico(topico)) {
			throw new RuntimeException("Não foi possível remover o tópico");
		}
		topico.setAtivo(false);
		topico.setNumerado(false);

		tree.removeNode(topico);
		getDocumento().setProcessoDocumentoEstruturadoTopicoList(tree.getHierarchicalList());
		
		atualizarTopicos(dadosTopicos);
		atualizarAnotacoes(anotacaoBeans);
		
		return getDadosEditor();
	}
	
	@WebRemote
	public DadosEditor adicionarTopico(DadosTopico dadosTopico, List<DadosTopico> dadosTopicos, List<AnotacaoBean> anotacaoBeans) {
		ProcessoDocumentoEstruturadoTopico topico = topicos.get(dadosTopico.getId());
		if (!podeAdicionarTopico(topico)) {
			throw new RuntimeException("Não foi possível adicionar o tópico");
		}

		try {
			adicionarTopicoRecursivo(topico);
			getDocumento().setProcessoDocumentoEstruturadoTopicoList(tree.getHierarchicalList());
		} catch (LinguagemFormalException e) {
			e.printStackTrace();
		}
		atualizarTopicos(dadosTopicos);
		atualizarAnotacoes(anotacaoBeans);
		return getDadosEditor();
	}
	
	private void adicionarTopicoRecursivo(ProcessoDocumentoEstruturadoTopico topico) throws LinguagemFormalException{
		ProcessoDocumentoEstruturadoTopico itemTopico;
		if (topico.getTopico().getItemTopico() != null) {
			itemTopico = topicoManager.criarProcessoDocumentoEstruturadoItemTopico(topico);
			addNode(topico, itemTopico);
			if(topico.getTopico().getItemTopico().getItemTopico() != null){
				adicionarTopicoRecursivo(itemTopico);
			}
			adicionarConclusao(itemTopico);
		} else {
			itemTopico = topicoManager.criarProcessoDocumentoEstruturadoItemTopico(topico.getProcessoDocumentoEstruturadoBloco());
			addNode(topico, itemTopico);
			adicionarConclusao(itemTopico);
		}
		
	}
	
	private void adicionarConclusao(ProcessoDocumentoEstruturadoTopico topico) throws LinguagemFormalException{
		if(topico.getTopico() instanceof ITopicoComConclusao<?>){
			ProcessoDocumentoEstruturadoTopico itemTopico = topicoManager.criarProcessoDocumentoEstruturadoConclusaoTopico(topico);
			addNode(topico, itemTopico);
		}
	}
	
	
	@WebRemote
	public DadosEditor renumerarTopico(DadosTopico dadosTopico, List<DadosTopico> dadosTopicos, List<AnotacaoBean> anotacoes) {
		ProcessoDocumentoEstruturadoTopico topico = topicos.get(dadosTopico.getId());
		topico.setNumerado(!dadosTopico.getNumerado());
		renumerarTopicos();
		atualizarTopicos(dadosTopicos);
		atualizarAnotacoes(anotacoes);
		return getDadosEditor();
	}
	
	@WebRemote
	public DadosEditor controleExibicaoTitulo(DadosTopico dadosTopico, List<DadosTopico> dadosTopicos, List<AnotacaoBean> anotacaoBeans) {
		ProcessoDocumentoEstruturadoTopico topico = topicos.get(dadosTopico.getId());
		topico.setExibirTitulo(!dadosTopico.getExibirTitulo());
		if (!topico.isExibirTitulo()) {
			topico.setNumerado(false);
		} else {
			topico.setNumerado(topico.getEstruturaDocumentoTopico().isNumerado());
		}
		renumerarTopicos();
		atualizarTopicos(dadosTopicos);
		atualizarAnotacoes(anotacaoBeans);
		return getDadosEditor();
	}
	
	@WebRemote
	public void exportarArquivos() {
		try {
			contingenciaAction.exportarArquivos(getDocumento().getIdProcessoDocumentoEstruturado());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@WebRemote
	@Transactional(TransactionPropagationType.REQUIRED)
	public void salvarPorcentagemZoom(int porcentagem) {
		Preferencia zoom = preferenciaManager.getPreferenciaPorUsuario(usuarioLogado, PreferenciaEditorEnum.ZO);
		if (zoom == null) {
			zoom = new Preferencia();
			zoom.setPreferenciaEditor(PreferenciaEditorEnum.ZO);
			zoom.setUsuario(usuarioLogado);
		}
		zoom.setValor(String.valueOf(porcentagem));
		if (zoom.getIdPreferencia() != 0) {
			preferenciaManager.update(zoom);
		} else {
			preferenciaManager.persist(zoom);
		}
	}
	
	@WebRemote
	public Map<String, Object> adicionarAnotacao(Integer idTopico) {
		Anotacao anotacao = anotacaoManager.criarAnotacao(topicos.get(idTopico));
		anotacoes.put(anotacao.getCodigoIdentificador(), anotacao);
		AnotacaoBean anotacaoBean = construirAnotacaoBean(anotacao);
		anotacaoBean.setData(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
		
		Map<String, Object> dados = new HashMap<String, Object>();
		dados.put("anotacao", anotacaoBean);
		dados.put("tabelaTopicos", buildTabelaTopicos());
		return dados;
	}

	@WebRemote
	public Map<String, Object> atualizarStatusAnotacao(AnotacaoBean anotacaoBean) {
		Anotacao anotacao = anotacoes.get(anotacaoBean.getIdAnotacao());
		anotacao.setStatusAnotacao(anotacaoBean.getStatusAnotacao());
		anotacao.setConteudo(anotacaoBean.getConteudo());
		anotacao = anotacaoManager.atualizarStatusAnotacao(anotacao);
		anotacao.setDataAlteracao(new Date());
		if (anotacao.getStatusAnotacao() == StatusAnotacao.E) {
			anotacoes.remove(anotacao.getCodigoIdentificador());
		}
		
		anotacaoBean = construirAnotacaoBean(anotacao);
		anotacaoBean.setData(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
		
		Map<String, Object> dados = new HashMap<String, Object>();
		dados.put("anotacao", anotacaoBean);
		dados.put("tabelaTopicos", buildTabelaTopicos());
		return dados;
	}
	
	@WebRemote
	public Map<String, Object> destacarAnotacao(AnotacaoBean anotacaoBean) {
		Anotacao anotacao = anotacoes.get(anotacaoBean.getIdAnotacao());
		anotacao.setDestaque(anotacaoBean.getDestaque());
		anotacao.setDataAlteracao(new Date());
		if (anotacao.getStatusAnotacao() == StatusAnotacao.N) {
			anotacao.setConteudo(anotacaoBean.getConteudo());
		}
		
		Map<String, Object> dados = new HashMap<String, Object>();
		dados.put("anotacao", construirAnotacaoBean(anotacao));
		dados.put("tabelaTopicos", buildTabelaTopicos());
		return dados;
	}
	
	@WebRemote
	public DadosEditor incorporarAnotacao(AnotacaoBean anotacaoBean, List<DadosTopico> dadosTopicos, String magistrado) throws LinguagemFormalException {
		Anotacao anotacao = anotacoes.get(anotacaoBean.getIdAnotacao());
		anotacao.setConteudo(anotacaoBean.getConteudo());
		
		criarTopicoItemConsideracoes(anotacao, magistrado);
		atualizarTopicos(dadosTopicos);
		anotacoes.remove(anotacaoBean.getIdAnotacao());
		anotacao.setStatusAnotacao(StatusAnotacao.E);
		anotacaoManager.atualizarStatusAnotacao(anotacao);
		return getDadosEditor();
	}
	
	@WebRemote
	public Map<String, Object> criarDivergencia(AnotacaoBean anotacaoBean, boolean criarDivergencia) {
		Anotacao anotacao = anotacoes.get(anotacaoBean.getIdAnotacao());
		anotacao.setDataAlteracao(new Date());
		if (anotacao.getStatusAnotacao() == StatusAnotacao.N) {
			anotacao.setConteudo(anotacaoBean.getConteudo());
		}
		if (criarDivergencia) {
			anotacao.setTipoAnotacao(TipoAnotacao.DIVERGENCIA);
		} else if (anotacao.getTopico().getTopico().getTipoTopico() == TipoTopicoEnum.ITEM_CONSIDERACOES){
			anotacao.setTipoAnotacao(TipoAnotacao.VOTO);
		} else {
			anotacao.setTipoAnotacao(TipoAnotacao.ANOTACAO);
		}
		
		Map<String, Object> dados = new HashMap<String, Object>();
		dados.put("anotacao", construirAnotacaoBean(anotacao));
		dados.put("tabelaTopicos", buildTabelaTopicos());
		return dados;
	}
	
	@WebRemote
	public Map<String, Object> reabrirAnotacao(AnotacaoBean anotacaoBean) {
		Anotacao anotacao = anotacaoManager.reabrirAnotacao(anotacoes.get(anotacaoBean.getIdAnotacao()));
		Map<String, Object> dados = new HashMap<String, Object>();
		dados.put("anotacao", construirAnotacaoBean(anotacao));
		dados.put("tabelaTopicos", buildTabelaTopicos());
		return dados;
	}
	
	@WebRemote
	public Map<String, Object> atualizarStatusCienciaAnotacao(AnotacaoBean anotacaoBean) {
		Anotacao anotacao = anotacoes.get(anotacaoBean.getIdAnotacao());
		anotacao.setStatusCienciaAnotacao(anotacaoBean.getStatusCienciaAnotacao());
		anotacao.setDataAlteracao(new Date());
		Map<String, Object> dados = new HashMap<String, Object>();
		dados.put("anotacao", construirAnotacaoBean(anotacao));
		dados.put("tabelaTopicos", buildTabelaTopicos());
		return dados;
	}
	
	@WebRemote
	public Map<String, Object> atualizarStatusAcolhidoAnotacao(AnotacaoBean anotacaoBean) {
		Anotacao anotacao = anotacoes.get(anotacaoBean.getIdAnotacao());
		anotacao.setStatusAcolhidoAnotacao(anotacaoBean.getStatusAcolhidoAnotacao());
		anotacao.setDataAlteracao(new Date());
		Map<String, Object> dados = new HashMap<String, Object>();
		dados.put("anotacao", construirAnotacaoBean(anotacao));
		dados.put("tabelaTopicos", buildTabelaTopicos());
		return dados;
	}
	
	@WebRemote
	@SuppressWarnings("unchecked")
	public List<String> getMagistradosOrgaoJulgador() {
		Query query = EntityUtil.createQuery("select o.usuarioLocalizacao.usuario.nome from UsuarioLocalizacaoMagistradoServidor o " +
				"where o.orgaoJulgador = :orgaoJulgador and o.dtInicio <= :inicio and (o.dtFinal is null or o.dtFinal >= :fim) " +
				"and o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado " +
				"and o.usuarioLocalizacao.papel = :papelMagistrado " +
				"and exists (select 1 from PessoaMagistrado pm where pm = o.usuarioLocalizacao.usuario) " +
				"order by o.magistradoTitular desc");
		query.setParameter("orgaoJulgador", orgaoJulgadorAtual);
		query.setParameter("orgaoJulgadorColegiado", Authenticator.getOrgaoJulgadorColegiadoAtual());
		query.setParameter("inicio", DateUtil.getBeginningOfDay(new Date()));
		query.setParameter("fim", DateUtil.getEndOfDay(new Date()));
		query.setParameter("papelMagistrado", ParametroUtil.instance().getPapelMagistrado());
		
		return query.getResultList();
	}
	
	public String getHtmlEditor() {
		return editorService.getHtmlEditor(getDocumento(), false);
	}
	
	private void atualizarAcoesAnotacao(AnotacaoBean anotacaoBean, Anotacao anotacao) {
		anotacaoBean.setPodeConcluir(anotacaoManager.podeConcluirAnotacao(anotacao));
		anotacaoBean.setPodeRetirar(anotacaoManager.podeRetirarAnotacao(anotacao));
		anotacaoBean.setPodeExcluir(anotacaoManager.podeExcluirAnotacao(anotacao));
		anotacaoBean.setPodeLiberar(anotacaoManager.podeLiberarAnotacao(anotacao));
		anotacaoBean.setPodeCriarDivergencia(anotacaoManager.podeCriarDivergencia(anotacao) && isHabilitarDivergencias());
		anotacaoBean.setPodeDestacar(anotacaoManager.podeDestacarAnotacao(anotacao) && isHabilitarDestaques());
		anotacaoBean.setPodeMostrarAcoesDivergenciaRelator(anotacaoManager.podeMostrarAcoesDivergenciaRelator(anotacao));
		anotacaoBean.setPodeManterDivergencia(anotacaoManager.podeManterDivergencia(anotacao));
		anotacaoBean.setPodeMarcarDivergenciaComoCiente(anotacaoManager.podeMarcarDivergenciaComoCiente(anotacao));
		anotacaoBean.setPodeEditar(anotacaoManager.podeEditarAnotacao(anotacao));
	}
	
	private void criarTopicoItemConsideracoes(Anotacao anotacao, String magistrado) throws LinguagemFormalException {
		ProcessoDocumentoEstruturadoTopico topicoVoto = null;
		for (ProcessoDocumentoEstruturadoTopico topico : getDocumento().getProcessoDocumentoEstruturadoTopicoList()) {
			if (topico.getTopico().getTipoTopico() == TipoTopicoEnum.CONSIDERACOES) {
				topicoVoto = topico;
				break;
			}
		}
		
		if (topicoVoto == null) {
			return;
		}
		
		ProcessoDocumentoEstruturadoTopico topico = topicoManager.criarProcessoDocumentoEstruturadoItemTopico(topicoVoto);
		topico.setConteudo(anotacao.getConteudo());
		topico.setTitulo("Voto do(a) Des(a). " + magistrado);
		((TopicoItemConsideracoes) topico.getTopico()).setOrgaoJulgador(anotacao.getOrgaoJulgador());
		
		List<ProcessoDocumentoEstruturadoTopico> topicosItemConsideracoes = tree.getChildren(topicoVoto);
		if (topicosItemConsideracoes.isEmpty()) {
			addNode(topicoVoto, topico);
		} else {
			int index = -1;
			for (int i = 0; i < topicosItemConsideracoes.size(); i++) {
				TopicoItemConsideracoes topicoNovo = (TopicoItemConsideracoes) EntityUtil.removeProxy(topico.getTopico());
				TopicoItemConsideracoes topicoAtual = (TopicoItemConsideracoes) EntityUtil.removeProxy(topicosItemConsideracoes.get(i).getTopico());
				
				if (Integer.valueOf(topicoAtual.getOrgaoJulgador().getCodigoOrigem().trim()) > Integer.valueOf(topicoNovo.getOrgaoJulgador().getCodigoOrigem().trim())) {
					index = i;
					break;
				}
			}
			if (index == -1) {
				tree.addChild(topico, topicoVoto);
			} else {
				tree.addChildAtIndex(topico, topicoVoto, index);
			}
		}
		
		getDocumento().setProcessoDocumentoEstruturadoTopicoList(getHierarchicalList());
	}

	public ProcessoDocumentoEstruturado getDocumento() {
		return editorAction.getDocumento();
	}
	
	private List<Estilo> getEstilos() {
		if (estilos == null) {
			estilos = cssDocumentoManager.getEstilos();
		}
		return estilos;
	}
	
	private void addNode(ProcessoDocumentoEstruturadoTopico topico, ProcessoDocumentoEstruturadoTopico itemTopico) {
		itemTopico.setNumerado(topico.isNumerado());
		if (topico.getTopico().getItemTopico() != null) {
			List<ProcessoDocumentoEstruturadoTopico> children = tree.getChildren(topico);
			if(topico.getTopico() instanceof ITopicoComConclusao<?> && children.size() >= 2){
				tree.addNode(itemTopico, children.get(children.size()-2));
			}else{
				tree.addChild(itemTopico, topico);
			}
		} else {
			tree.addNode(itemTopico, topico);
		}
		getDocumento().setProcessoDocumentoEstruturadoTopicoList(tree.getHierarchicalList());
	}
	
	private void atualizarTopicos(List<DadosTopico> dadosTopicos) {
		for (DadosTopico dadosTopico: dadosTopicos) {
			ProcessoDocumentoEstruturadoTopico topico = topicos.get(dadosTopico.getId());
			if (topicoManager.podeEditarTitulo(topico)) {
				topico.setTitulo(dadosTopico.getTitulo());
			}
			if (topicoManager.podeEditarConteudo(topico)) {
				topico.setConteudo(dadosTopico.getConteudo());
			}
			if (topico.getTopico().isSomenteLeitura()) {
				topico.setConteudo(dadosTopico.getConteudo());
			}
		}
	}
	
	private void atualizarAnotacoes(List<AnotacaoBean> anotacaoBeanList) {
		for (AnotacaoBean anotacaoBean : anotacaoBeanList) {
			Anotacao anotacao = anotacoes.get(anotacaoBean.getIdAnotacao());
			if (anotacao.getStatusAnotacao() == StatusAnotacao.N || anotacao.getStatusAnotacao() == StatusAnotacao.C) {
				anotacao.setConteudo(anotacaoBean.getConteudo());
			}
		}
		anotacaoManager.setAnotacoesDoDocumento(new ArrayList<Anotacao>(anotacoes.values()));
	}
	
	private DadosTopico construirDadosTopico(ProcessoDocumentoEstruturadoTopico topico) {
		DadosTopico dadosTopico = new DadosTopico(topico);
		dadosTopico.setPodeEditarTitulo(topicoManager.podeEditarTitulo(topico) && !isAbrirSomenteLeitura());
		dadosTopico.setPodeEditarConteudo(topicoManager.podeEditarConteudo(topico) && !isAbrirSomenteLeitura());
		dadosTopico.setPodeAdicionarTopico(podeAdicionarTopico(topico));
		dadosTopico.setPodeRemoverTopico(podeRemoverTopico(topico));
		dadosTopico.setPodeMoverParaBaixo(podeMoverParaBaixo(topico));
		dadosTopico.setPodeMoverParaCima(podeMoverParaCima(topico));
		dadosTopico.setPodeMudarHabilitacao(podeMudarHabilitacao(topico));
		dadosTopico.setPodeRecarregarTopico(podeRecarregarTopico(topico));
		dadosTopico.setNumeracaoFormatada(numeracaoDocumentoService.getNumeracaoDocumento(topico));
		return dadosTopico;
	}
	
	private AnotacaoBean construirAnotacaoBean(Anotacao anotacao) {
		AnotacaoBean anotacaoBean = new AnotacaoBean(anotacao);
		anotacaoBean.setPodeReabrir(anotacaoManager.podeReabrirAnotacao(anotacao));
		anotacaoBean.setNomePessoaCriacao(anotacaoManager.getNomePessoaCriacao(anotacao));
		anotacaoBean.setIdOrgaoJulgador(anotacao.getOrgaoJulgador().getIdOrgaoJulgador());
		atualizarAcoesAnotacao(anotacaoBean, anotacao);
		anotacaoBean.setTitulo(anotacaoManager.buildTituloAnotacao(anotacao));
		anotacaoBean.setObservacao(anotacaoManager.buildObservacaoAnotacao(anotacao));
		anotacaoBean.setStatusAcolhidoAnotacao(anotacao.getStatusAcolhidoAnotacao());
		anotacaoBean.setStatusCienciaAnotacao(anotacao.getStatusCienciaAnotacao());
		anotacaoBean.setTopicoAssociadoExcluido(!anotacao.getTopico().isAtivo());
		return anotacaoBean;
	}
	
	private TabelaTopicosBean construirTabelaTopicosBean(ProcessoDocumentoEstruturadoTopico topico){
		TabelaTopicosBean tabelaTopicosBean = new TabelaTopicosBean();
		tabelaTopicosBean.setColegiado(!ParametroUtil.instance().isPrimeiroGrau());
		tabelaTopicosBean.setTemDivergencia(anotacaoManager.temDivergencia(topico));
		tabelaTopicosBean.setTemDivergenciaNaoConcluidaNaoLiberada(anotacaoManager.temDivergenciaNaoConcluidaNaoLiberada(topico));
		tabelaTopicosBean.setTemDivergenciaAcaoPendente(anotacaoManager.temDivergenciaAcaoPendente(topico));
		tabelaTopicosBean.setTemAnotacao(anotacaoManager.temAnotacao(topico));
		tabelaTopicosBean.setTemAnotacaoNaoConcluida(anotacaoManager.temAnotacaoNaoConcluida(topico));
		tabelaTopicosBean.setTemDestaqueDivergencia(anotacaoManager.temDestaqueDivergencia(topico));
		tabelaTopicosBean.setTemDestaqueDivergenciaNaoConcluidoNaoLiberado(anotacaoManager.temDestaqueDivergenciaNaoConcluidoNaoLiberado(topico));
		tabelaTopicosBean.setTemDestaqueAnotacao(anotacaoManager.temDestaqueAnotacao(topico));
		tabelaTopicosBean.setTemDestaqueAnotacaoNaoConcluidoNaoLiberado(anotacaoManager.temDestaqueAnotacaoNaoConcluidoNaoLiberado(topico));
		return tabelaTopicosBean;
	}

	private void renumerarTopicos() {
		tree.buildHierarchicalList();
		getDocumento().setProcessoDocumentoEstruturadoTopicoList(tree.getHierarchicalList());
	}

	public void refreshTree() {
		tree.clear();
		tree.setHierarchicalList(getDocumento().getProcessoDocumentoEstruturadoTopicoList());
		tree.buildHierarchicalList();
	}
	
	private boolean podeAdicionarTopico(ProcessoDocumentoEstruturadoTopico topico) {
		if(isAbrirSomenteLeitura()){
			return false;
		}
		if(topico.getTopico().getTipoTopico() == TipoTopicoEnum.IT_DISP_SESSAO ||
			topico.getTopico().getTipoTopico() == TipoTopicoEnum.IT_DISP_VOTO ||
			topico.getTopico().getTipoTopico() == TipoTopicoEnum.CONSIDERACOES || 
			topico.getTopico().getTipoTopico() == TipoTopicoEnum.ITEM_CONSIDERACOES){
			return false;
		}
		return !topico.getTopico().isConclusao() && (topico.getTopico().getItemTopico() != null || topico.getProcessoDocumentoEstruturadoBloco() != null);
	}
	
	private boolean podeRemoverTopico(ProcessoDocumentoEstruturadoTopico topico) {
		if(isAbrirSomenteLeitura()){
			return false;
		}
		if(topico.getProcessoDocumentoEstruturadoBloco() == null){
			return false;
		}
		if (topico.getTopico().getTipoTopico() == TipoTopicoEnum.IT_DISP_SESSAO || 
			topico.getTopico().getTipoTopico() == TipoTopicoEnum.IT_DISP_VOTO ||
			topico.getTopico().getTipoTopico() == TipoTopicoEnum.ITEM_CONSIDERACOES) {
			return false;
		}
		List<ProcessoDocumentoEstruturadoTopico> children = tree.getChildren(topico.getProcessoDocumentoEstruturadoBloco());
		if(EntityUtil.removeProxy(topico.getProcessoDocumentoEstruturadoBloco().getTopico()) instanceof ITopicoComConclusao<?> &&
		   topico.getTopico().isItem() &&
		   children.size() <= 2){
			return false;
		}
		return !topico.getTopico().isConclusao() && children.size() > 1;
	}
	
	private boolean podeMoverParaCima(ProcessoDocumentoEstruturadoTopico topico) {
		if(isAbrirSomenteLeitura()){
			return false;
		}
		if (topico.getTopico().getTipoTopico() == TipoTopicoEnum.IT_DISP_SESSAO || 
			topico.getTopico().getTipoTopico() == TipoTopicoEnum.IT_DISP_VOTO) {
			return false;
		}
		return !topico.getTopico().isConclusao() && topico.getProcessoDocumentoEstruturadoBloco() != null && tree.canMoveUp(topico);
	}
	
	private boolean podeMoverParaBaixo(ProcessoDocumentoEstruturadoTopico topico) {
		if(isAbrirSomenteLeitura()){
			return false;
		}
		if(topico.getProcessoDocumentoEstruturadoBloco() == null){
			return false;
		}
		if(!tree.canMoveDown(topico)){
			return false;
		}
		if (topico.getTopico().getTipoTopico() == TipoTopicoEnum.IT_DISP_SESSAO || 
			topico.getTopico().getTipoTopico() == TipoTopicoEnum.IT_DISP_VOTO) {
			return false;
		}
		List<ProcessoDocumentoEstruturadoTopico> children = tree.getChildren(topico.getProcessoDocumentoEstruturadoBloco());
		boolean isUltimoItemDeTopicoComConclusao = EntityUtil.removeProxy(topico.getProcessoDocumentoEstruturadoBloco().getTopico()) instanceof ITopicoComConclusao<?> && 
												   children.indexOf(topico) == children.size()-2;
		return !topico.getTopico().isConclusao() && !isUltimoItemDeTopicoComConclusao;
	}
	
	private boolean podeMudarHabilitacao(ProcessoDocumentoEstruturadoTopico topico) {
		return !isAbrirSomenteLeitura() && topico.getTopico().isOpcional();
	}
	
	private boolean podeRecarregarTopico(ProcessoDocumentoEstruturadoTopico topico) {
		return !isAbrirSomenteLeitura() && topicoManager.podeEditarConteudo(topico);
	}
	
	private void buildTopicosMap() {
		topicos.clear();
		for (ProcessoDocumentoEstruturadoTopico topico : getDocumento().getProcessoDocumentoEstruturadoTopicoList()) {
			topicos.put(topico.getCodIdentificador(), topico);
		}
	}
	
	private void buildAnotacoesMap() {
		anotacoes.clear();
		for (Anotacao anotacao : anotacaoManager.getAnotacoesDoDocumento(getDocumento())) {
			anotacoes.put(anotacao.getCodigoIdentificador(), anotacao);
		}
	}
	
	public List<ProcessoDocumentoEstruturadoTopico> getHierarchicalList() {
		return tree.getHierarchicalList();
	}
	
	private Map<String, String> getTemplates() {
		Map<String, String> templates = new HashMap<String, String>();
		Util util = new Util();
		File templateDir = new File(util.getContextRealPath("/Editor/templates/"));
		
		for (File templateFile : templateDir.listFiles()) {
			String template = "";
			try {
				Scanner sc = new Scanner(templateFile).useDelimiter("\\Z");
				if (sc.hasNext())
					template = sc.next();
				sc.close();
				templates.put(templateFile.getName().replace(".xhtml", ""), template);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return templates;
	}
	
	public boolean isExibirAnotacoes() {
		return exibirAnotacoes;
	}
	
	public void setExibirAnotacoes(boolean exibirAnotacoes) {
		this.exibirAnotacoes = exibirAnotacoes;
	}

	public boolean isAbrirSomenteLeitura() {
		return abrirSomenteLeitura;
	}

	public void setAbrirSomenteLeitura(boolean abrirSomenteLeitura) {
		this.abrirSomenteLeitura = abrirSomenteLeitura;
	}

	public boolean isHabilitarAnotacoes() {
		return habilitarAnotacoes;
	}

	public void setHabilitarAnotacoes(boolean habilitarAnotacoes) {
		this.habilitarAnotacoes = habilitarAnotacoes;
	}

	public boolean isHabilitarDivergencias() {
		return habilitarDivergencias;
	}

	public void setHabilitarDivergencias(boolean habilitarDivergencias) {
		this.habilitarDivergencias = habilitarDivergencias;
	}

	public boolean isHabilitarDestaques() {
		return habilitarDestaques;
	}

	public void setHabilitarDestaques(boolean habilitarDestaques) {
		this.habilitarDestaques = habilitarDestaques;
	}

	public String getTextoEmentaMigracao() {
		return textoEmentaMigracao;
	}

	public void setTextoEmentaMigracao(String textoEmentaMigracao) {
		this.textoEmentaMigracao = textoEmentaMigracao;
	}

	public String getTextoRelatorioMigracao() {
		return textoRelatorioMigracao;
	}

	public void setTextoRelatorioMigracao(String textoRelatorioMigracao) {
		this.textoRelatorioMigracao = textoRelatorioMigracao;
	}

	public String getTextoFundamentacaoMigracao() {
		return textoFundamentacaoMigracao;
	}

	public void setTextoFundamentacaoMigracao(String textoFundamentacaoMigracao) {
		this.textoFundamentacaoMigracao = textoFundamentacaoMigracao;
	}

	public String getTextoDispositivoMigracao() {
		return textoDispositivoMigracao;
	}

	public void setTextoDispositivoMigracao(String textoDispositivoMigracao) {
		this.textoDispositivoMigracao = textoDispositivoMigracao;
	}

	public String getTextoAlertMigracao() {
		return textoAlertMigracao;
	}

	public void setTextoAlertMigracao(String textoAlertMigracao) {
		this.textoAlertMigracao = textoAlertMigracao;
	}

	public List<Map<String, Object>> getTextoVotoRevisoresMigracao() {
		return textoVotoRevisoresMigracao;
	}

	public void setTextoVotoRevisoresMigracao(
			List<Map<String, Object>> textoVotoRevisoresMigracao) {
		this.textoVotoRevisoresMigracao = textoVotoRevisoresMigracao;
	}

	
	
}
