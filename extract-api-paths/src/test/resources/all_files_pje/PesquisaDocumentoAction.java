package br.com.infox.editor.action;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Base64;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.Util;
import br.com.infox.editor.interpretadorDocumento.LinguagemFormalException;
import br.com.infox.editor.manager.ProcessoDocumentoEstruturadoIndexManager;
import br.com.infox.editor.manager.ProcessoDocumentoEstruturadoIndexManager.BeanPesquisaIndexadaId;
import br.com.infox.editor.manager.ProcessoDocumentoEstruturadoTopicoManager;
import br.com.infox.editor.service.ProcessaModeloService;
import br.com.infox.ibpm.help.HelpUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FileUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;

@Name(PesquisaDocumentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PesquisaDocumentoAction implements Serializable {

	public static final String NAME = "pesquisaDocumentoAction";

	private static final long serialVersionUID = 1L;

	@In(value=ProcessoDocumentoEstruturadoIndexManager.NAME)
	private ProcessoDocumentoEstruturadoIndexManager pdEstruturadoTopicoIndexManager;	
	
	@In
	private ProcessoDocumentoEstruturadoTopicoManager processoDocumentoEstruturadoTopicoManager;
	
	@In
	private ProcessoDocumentoBinPessoaAssinaturaManager processoDocumentoBinPessoaAssinaturaManager;
	
	@In
	private br.jus.cnj.pje.business.dao.OrgaoJulgadorDAO orgaoJulgadorDAO;
	
	@In
	private ProcessaModeloService processaModeloService;
	
	private Query query;
	private List<Integer> resultListId;
	
	private TipoProcessoDocumento tipoProcessoDocumento;
	
	private ProcessoDocumentoEstruturado processoDocumentoEstruturadoEdicao;
	private ProcessoDocumentoEstruturadoTopico processoDocumentoEstruturadoTopicoCopia;
	private Integer idProcessoDocumentoEstruturadoEdicao;
	private ProcessoDocumentoEstruturadoTopico topicoSelecionado;
	
	private PessoaMagistrado pessoaMagistrado;
	private PessoaFisica autor;
	private OrgaoJulgador orgaoJulgador;
	private String termoPesquisa;

	private SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");
	
	@Create
	@Observer("org.jboss.seam.postCreate." + NAME)
	public void init() {
		termoPesquisa = null;
		orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
		if (processoDocumentoEstruturadoEdicao != null) {
			tipoProcessoDocumento = processoDocumentoEstruturadoEdicao.getProcessoDocumento().getTipoProcessoDocumento();
		} else {
			tipoProcessoDocumento = null;
		}
		Pessoa pessoa = (Pessoa) EntityUtil.removeProxy(Authenticator.getPessoaLogada());
		autor = (PessoaFisica) pessoa;
	}
	
	public PesquisaDocumentoAction() {
		resultListId = new ArrayList<Integer>();
	}
	
	public String getTermoPesquisa() {
		return termoPesquisa;
	}
	
	public void setTermoPesquisa(String termoPesquisa) {
		this.termoPesquisa = termoPesquisa;
	}
	
	public String getConteudoProcessado() throws LinguagemFormalException {
		return processaModeloService.processaVariaveisModelo(processoDocumentoEstruturadoTopicoCopia.getConteudo());
	}
	
	public void pesquisar() {
		if (Strings.isEmpty(termoPesquisa)) {
			return;
		}
		try {
			BeanPesquisaIndexadaId beanPesquisaIndexadaId = getResultListIdPesquisa();
			query = beanPesquisaIndexadaId.getQuery();
			resultListId = createIdList(beanPesquisaIndexadaId.getResultList());
		} catch (ParseException e) {
			e.printStackTrace();
			FacesMessages.instance().add(Severity.ERROR, "Estrutura da pesquisa está incorreta.");
		}
	}
	
	private List<Integer> createIdList(List<Object[]> list) {
		List<Integer> ids = new ArrayList<Integer>(list.size());
		for (Object[] objects : list) {
			ids.add((Integer) objects[0]);
		}
		return ids;
	}

	private BeanPesquisaIndexadaId getResultListIdPesquisa() throws ParseException {
		List<Query> queries = new ArrayList<Query>();
		if (autor != null) {
			Query q = pdEstruturadoTopicoIndexManager.createLuceneQueryIdPessoa(autor.getIdUsuario());
			queries.add(q);
		}
		if (tipoProcessoDocumento != null) {
			Query q = pdEstruturadoTopicoIndexManager.createLuceneQueryIdTipoDocumento(tipoProcessoDocumento.getIdTipoProcessoDocumento());
			queries.add(q);			
		}
		return pdEstruturadoTopicoIndexManager.getFullTextQueryIdPesquisa(termoPesquisa, queries.toArray(new Query[queries.size()]));
	}
	
	public String getTextoHighlight(String texto) {
		if (query != null) {
			return HelpUtil.highlightHtmlText((Query) query, texto);
		} else {
			return texto;
		}
	}
	
	public List<Integer> getResultListId() {
		if (resultListId.isEmpty()){
			resultListId.add(-1);
		}
		return resultListId;
	}
	
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}
	
	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}
	
	//TODO colocar isso em um manager ou criar uma combo
	@SuppressWarnings("unchecked")
	@Factory(scope=ScopeType.CONVERSATION, value="tipoProcessoDocumentoPesquisaDocumentoItems")
	public List<TipoProcessoDocumento> getTipoProcessoDocumentoPesquisaList() {
		String hql = "select o from TipoProcessoDocumento o where o.ativo = true " + 
				"and o in (select tpd.tipoProcessoDocumento from AplicacaoClasseTipoProcessoDocumento tpd " +
				"where tpd.aplicacaoClasse.idAplicacaoClasse = #{parametroUtil.aplicacaoSistema.idAplicacaoClasse}) " +
				"order by o.tipoProcessoDocumento";
		return EntityUtil.createQuery(hql).getResultList();
	}
	
	public ProcessoDocumentoEstruturado getProcessoDocumentoEstruturadoEdicao() {
		return processoDocumentoEstruturadoEdicao;
	}
	
	public void setProcessoDocumentoEstruturadoEdicao(ProcessoDocumentoEstruturado processoDocumentoEstruturadoEdicao) {
		this.processoDocumentoEstruturadoEdicao = processoDocumentoEstruturadoEdicao;
		if (processoDocumentoEstruturadoEdicao != null) {
			tipoProcessoDocumento = processoDocumentoEstruturadoEdicao.getProcessoDocumento().getTipoProcessoDocumento();
		}
	}
	
	public void setIdProcessoDocumentoEstruturadoEdicao(Integer id) {
		idProcessoDocumentoEstruturadoEdicao = id;
		if (id != null) {
			ProcessoDocumentoEstruturado processoDocumentoEstruturado = EntityUtil.getEntityManager().find(ProcessoDocumentoEstruturado.class, id);
			setProcessoDocumentoEstruturadoEdicao(processoDocumentoEstruturado);
		}
	}
	
	public Integer getIdProcessoDocumentoEstruturadoEdicao() {
		return idProcessoDocumentoEstruturadoEdicao;
	}
	
	public void setIdProcessoDocumentoEstruturadoTopicoCopia(Integer id) {
		setProcessoDocumentoEstruturadoTopicoCopia(EntityUtil.find(ProcessoDocumentoEstruturadoTopico.class, id));
	}
	
	public Integer getIdTopicoSelecionado() {
		if (getTopicoSelecionado() != null) {
			return getTopicoSelecionado().getIdProcessoDocumentoEstruturadoTopico();
		}
		return null;
	}
	
	public void setIdTopicoSelecionado(Integer id) {
		if (id != null) {
			setTopicoSelecionado(EntityUtil.find(ProcessoDocumentoEstruturadoTopico.class, id));
		}
	}
	
	public List<ProcessoDocumentoEstruturadoTopico> getTopicosDocumentoEdicao() {
		if (processoDocumentoEstruturadoEdicao == null) {
			return Collections.emptyList();
		}
		List<ProcessoDocumentoEstruturadoTopico> list = new ArrayList<ProcessoDocumentoEstruturadoTopico>();
		for (ProcessoDocumentoEstruturadoTopico pdeTopico : processoDocumentoEstruturadoEdicao.getProcessoDocumentoEstruturadoTopicoList()) {
			if (processoDocumentoEstruturadoTopicoManager.podeEditarConteudo(pdeTopico) && pdeTopico.isHabilitado()) {
				list.add(pdeTopico);
			}
		}
		return list;
	}

	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}
	
	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}
	
	public boolean getMostrarGrid() {
		return !Strings.isEmpty(termoPesquisa);
	}
	
	public String getDataFormatada(Date date) {
		return date != null ? dateFormater.format(date) : null;
	}
	
	public void appendConteudoTopico() {
		String textoApend = getProcessoDocumentoEstruturadoTopicoCopia().getConteudo();
		topicoSelecionado.setConteudo(topicoSelecionado.getConteudo() + textoApend);
		EntityUtil.getEntityManager().merge(topicoSelecionado);
		EntityUtil.getEntityManager().flush();
	}
	
	public String getNomeUsuarioUltimaAssinatura(int idProcessoDocumentoBin) {
		return processoDocumentoBinPessoaAssinaturaManager.getNomeUsuarioUltimaAssinatura(idProcessoDocumentoBin);
	}		
	
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}
	
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	
	public PessoaFisica getAutor() {
		return autor;
	}
	
	public void setAutor(PessoaFisica autor) {
		this.autor = autor;
	}
	
	public List<OrgaoJulgador> getOrgaoJulgadorList() {
		return orgaoJulgadorDAO.findAll();
	}
	
	public static void main(String[] args) {
		String imgConverter = "C:\\Users\\rodrigo\\Desktop\\Editor Estruturado\\capTela.png";
		String imgBase64 = Base64.encodeFromFile(imgConverter);
		System.out.println(imgBase64);
		FileUtil.writeText(new File(imgConverter + ".base64.txt"), false, imgBase64);
	}

	public ProcessoDocumentoEstruturadoTopico getTopicoSelecionado() {
		return topicoSelecionado;
	}

	public void setTopicoSelecionado(ProcessoDocumentoEstruturadoTopico topicoSelecionado) {
		this.topicoSelecionado = topicoSelecionado;
	}

	public ProcessoDocumentoEstruturadoTopico getProcessoDocumentoEstruturadoTopicoCopia() {
		return processoDocumentoEstruturadoTopicoCopia;
	}

	public void setProcessoDocumentoEstruturadoTopicoCopia(
			ProcessoDocumentoEstruturadoTopico processoDocumentoEstruturadoTopicoCopia) {
		this.processoDocumentoEstruturadoTopicoCopia = processoDocumentoEstruturadoTopicoCopia;
	}
	
	public String converterCodigoParaCaracteresEspeciais(String string){
		return Util.converterCodigoParaCaracteresEspeciais(string);
	}
}
