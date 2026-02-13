package br.jus.cnj.pje.view;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.LiberacaoPublicacaoDecisaoService;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.cnj.pje.vo.ConsultaPublicacaoSessaoVO;
import br.jus.pje.nucleo.entidades.LiberacaoPublicacaoDecisao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.enums.LiberacaoSessaoJulgamentoOrderEnum;
import br.jus.pje.nucleo.enums.SituacaoPublicacaoLiberacaoEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.enums.TipoPublicacaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(PublicacaoSessaoMuralAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PublicacaoSessaoMuralAction extends BaseAction<LiberacaoPublicacaoDecisao> {

	private static final long serialVersionUID = -5978699681140503562L;

	public static final String NAME = "publicacaoSessaoMuralAction";

	private ConsultaPublicacaoSessaoVO consulta;
	private Integer prazoPadrao;
	private TipoPrazoEnum tipoPrazoPadrao;
	private List<Date> datasSessoesNaoFinalizadas;
	private List<OrgaoJulgador> orgaosJulgadores;
	private List<LiberacaoPublicacaoDecisao> publicacoes;
	private List<LiberacaoPublicacaoDecisao> publicacoesSelecionadas;
	private Boolean selecionarTodos;
	private Boolean apresentaBotaoPublicar;
	
	@In(required = true)
	private LiberacaoPublicacaoDecisaoService liberacaoPublicacaoDecisaoService;
	
	
	@Create
	public void init(){
		selecionarTodos = Boolean.FALSE;
		lerNumeroOrgaoJustica();
		lerParametrosPrazos();
	}
	
	private void lerParametrosPrazos() {
		ParametroUtil parametroUtil = ComponentUtil.getComponent(ParametroUtil.NAME);
		prazoPadrao = parametroUtil.recuperarPrazoPublicacaoSessaoMural();
		tipoPrazoPadrao = parametroUtil.recuperarTipoPrazoPublicacaoSessaoMural();
	}

	private void lerNumeroOrgaoJustica() {
		ParametroUtil parametroUtil = ComponentUtil.getComponent(ParametroUtil.NAME);
		String numeroOrgaoJustica = parametroUtil.recuperarNumeroOrgaoJustica();
		if(!numeroOrgaoJustica.isEmpty()){
			getConsulta().setRamoJustica(numeroOrgaoJustica.substring(0, 1));
		}
	}
	
	public void pesquisar(){
		publicacoes.clear();
		getPublicacoesSelecionadas().clear();
		if(verificarSePesquisaValida()){
			getPublicacoes().addAll(liberacaoPublicacaoDecisaoService.pesquisar(getConsulta(), verificarUsuarioComPerfilGravarProcesso()));
			if(getPublicacoes().isEmpty()){
				facesMessages.addFromResourceBundle("publicacaoSessaoJulgamento.nenhum.resultado");
			} else {
				processarPublicacoesDeAcordoComPerfil();
				Collections.sort(publicacoes, LiberacaoSessaoJulgamentoOrderEnum.POR_PROCESSO);
			}
		}
	}

	private void processarPublicacoesDeAcordoComPerfil() {
		if(verificarUsuarioComPerfilGravarProcesso()){
			processarListaPublicacoesSelecionadas();
		} 
		if(verificarUsuarioComPerfilPublicarProcesso()){
			processarPrazoPadrao();
		}
	}
	
	private void processarListaPublicacoesSelecionadas() {
		for (LiberacaoPublicacaoDecisao libPub : getPublicacoes()) {
			if(libPub.getIdLiberacaoPublicacaoDecisao() > 0){
				libPub.setSelecionado(Boolean.TRUE);
				getPublicacoesSelecionadas().add(libPub);
			}
		}
	}
	
	private boolean verificarSePesquisaValida(){
		boolean retorno = true;
		if(getConsulta().getTipoPublicacao() != null){
			if(getConsulta().getTipoPublicacao().isSessao() && getConsulta().getDataSessao() == null){
				facesMessages.addFromResourceBundle(Severity.WARN,"publicacaoSessaoJulgamento.parametro.datasessao.nao.informado");
				retorno = false;
			}
		} else {
			facesMessages.addFromResourceBundle(Severity.WARN,"publicacaoSessaoJulgamento.parametro.tipo.publicacao.nao.informado");
			retorno = false;
		}
		return retorno;
	}
	
	public void limparCampoDataSessao(){
		getConsulta().setDataSessao(null);
	}
	
	public boolean isApresentaDataSessao(){
		boolean retorno = false;
		if(getConsulta().getTipoPublicacao() != null && getConsulta().getTipoPublicacao().isSessao()){
			retorno = true;
		}
		return retorno;
	}
	
	public void selecionarTodosDocumentos(){
		if(!selecionarTodos){
			marcarSelecionadoOuNaoSelecionado(false);
			setPublicacoesSelecionadas(new ArrayList<LiberacaoPublicacaoDecisao>());
		} else {
			marcarSelecionadoOuNaoSelecionado(true);
			setPublicacoesSelecionadas(new ArrayList<LiberacaoPublicacaoDecisao>(getPublicacoes()));
		}
	}
	
	public void selecionar(LiberacaoPublicacaoDecisao libPub){
		if(getPublicacoesSelecionadas().contains(libPub)){
			getPublicacoesSelecionadas().remove(libPub);
		} else {
			getPublicacoesSelecionadas().add(libPub);
		}
		selecionarTodos = false;
	}
	
	private void marcarSelecionadoOuNaoSelecionado(boolean selecao){
		for (LiberacaoPublicacaoDecisao lib : getPublicacoes()) {
			lib.setSelecionado(selecao);
		}
	}
	
	private void processarPrazoPadrao() {
		for (LiberacaoPublicacaoDecisao publicacao : getPublicacoes()) {
			if(publicacao.getTipoPrazo() == null){
				publicacao.setTipoPrazo(tipoPrazoPadrao);
			}
			if(publicacao.getDataPrazoLegal() == null && publicacao.getPrazoLegal() == null && 
					publicacao.getTipoPrazo() != null && !publicacao.getTipoPrazo().isSemPrazo()){
				publicacao.setPrazoLegal(prazoPadrao);
			}
		}
	}

	public void limparCampos(){
		consulta = null;
		init();
	}
	
	public void gravar(){
		if(!getPublicacoesSelecionadas().isEmpty()){
			if(verificarUsuarioComPerfilGravarProcesso()){
				liberacaoPublicacaoDecisaoService.gravarLiberacoes(getPublicacoes(), getPublicacoesSelecionadas());
				facesMessages.addFromResourceBundle("publicacaoSessaoJulgamento.dados.gravados.sucesso");
			} else {
				List<String> documentosNaoGravados = liberacaoPublicacaoDecisaoService.gravar(getPublicacoesSelecionadas());
				if(documentosNaoGravados.isEmpty()){
					facesMessages.addFromResourceBundle("publicacaoSessaoJulgamento.dados.gravados.sucesso");
				} else {
					facesMessages.addFromResourceBundle("publicacaoSessaoJulgamento.alguns.dados.nao.gravados", documentosNaoGravados.size());
				}
			}
		} else {
			facesMessages.addFromResourceBundle(Severity.ERROR,"publicacaoSessaoJulgamento.nenhum.documento.para.gravar");
		}
	}
	
	public void publicar(){
		if(!getPublicacoesSelecionadas().isEmpty()){
			List<LiberacaoPublicacaoDecisao> documentosNaoPublicados = new ArrayList<LiberacaoPublicacaoDecisao>();
			try {
				documentosNaoPublicados = liberacaoPublicacaoDecisaoService.publicar(getPublicacoesSelecionadas());
				if(documentosNaoPublicados.isEmpty()){
					facesMessages.addFromResourceBundle(Severity.INFO,"publicacaoSessaoJulgamento.dados.publicados.sucesso");
					pesquisar();
					setPublicacoesSelecionadas(new ArrayList<LiberacaoPublicacaoDecisao>());
				} else if(documentosNaoPublicados.size() < getPublicacoesSelecionadas().size()) {
					facesMessages.addFromResourceBundle("publicacaoSessaoJulgamento.alguns.dados.nao.publicados", documentosNaoPublicados.size());
					facesMessages.addFromResourceBundle("publicacaoSessaoJulgamento.alguns.dados.publicados.sucesso", (getPublicacoesSelecionadas().size() - documentosNaoPublicados.size()));
					pesquisar();
					getPublicacoesSelecionadas().retainAll(documentosNaoPublicados);
				} else {
					facesMessages.addFromResourceBundle("publicacaoSessaoJulgamento.alguns.dados.nao.publicados", documentosNaoPublicados.size());
				}
			} catch (PJeBusinessException e) {
				facesMessages.addFromResourceBundle("publicacaoSessaoJulgamento.erro.publicacao.liberacao.sessao.mural");
				facesMessages.addFromResourceBundle(e.getCode());
			}
		} else {
			facesMessages.addFromResourceBundle(Severity.WARN,"publicacaoSessaoJulgamento.nenhum.documento.para.publicar");
		}
	}
	
	public ProcessoDocumentoBin obterDocBin(Integer idProcDoc){
		ProcessoDocumentoBinManager processoDocumentoBinManager = ComponentUtil.getComponent("processoDocumentoBinManager");
		ProcessoDocumentoBin proDocBin = processoDocumentoBinManager.recuperar(idProcDoc);
		return proDocBin;
	}
	
	public void imprimirRelatorioPublicacao(){
		facesMessages.clear();
		List<LiberacaoPublicacaoDecisao> publicacoesRelatorio = liberacaoPublicacaoDecisaoService.pesquisarRelatorioDecisoesMonocraticasEmSessao(getConsulta());
		Collections.sort(publicacoesRelatorio, LiberacaoSessaoJulgamentoOrderEnum.POR_PROCESSO);
		
		if(!publicacoesRelatorio.isEmpty()){
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			response.setContentType("application/pdf");

			String filename = "Decisões_Monocráticas_em_Sessão_" + DateUtil.dateToString(new Date());
			String extensao = ".pdf";
			
			response.setHeader("Content-Disposition", "attachment; filename=\""	+ filename + extensao + "\"");
			
			downloadRelatorioDecisoesMonocraticas(publicacoesRelatorio, facesContext, response);
		} else {
			facesMessages.addFromResourceBundle(Severity.WARN,"publicacaoSessaoJulgamento.nenhum.documento.aptos.relatorio");
		}
	}

	private void downloadRelatorioDecisoesMonocraticas(List<LiberacaoPublicacaoDecisao> publicacoesRelatorio, FacesContext facesContext, HttpServletResponse response) {
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			liberacaoPublicacaoDecisaoService.imprimirRelatorioPublicacao(publicacoesRelatorio, out);
			out.flush();
			facesContext.responseComplete();
		} catch (IOException ex) {
			facesMessages.addFromResourceBundle(Severity.ERROR,"publicacaoSessaoJulgamento.erro.download.relatorio");
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
	
	public String dataFormatada(Date date) {
		return DateUtil.dateToString(date);
	}
	
	public TipoPrazoEnum[] getTiposPrazos(){
		return TipoPrazoEnum.values();
	}
	
	public TipoPublicacaoEnum[] getTiposPublicacoes(){
		return TipoPublicacaoEnum.values();
	}
	
	public List<Date> getDatasSessoesNaoFinalizadas() {
		if(datasSessoesNaoFinalizadas == null){
			datasSessoesNaoFinalizadas = liberacaoPublicacaoDecisaoService.obterDatasSessoesLiberacao();
			Collections.sort(datasSessoesNaoFinalizadas,Collections.reverseOrder());
		}
		return datasSessoesNaoFinalizadas;
	}

	public void setDatasSessoesNaoFinalizadas(List<Date> datasSessoesNaoFinalizadas) {
		this.datasSessoesNaoFinalizadas = datasSessoesNaoFinalizadas;
	}

	public Boolean verificaSeApresentaFuncionalidadesGravarPublicar(){
		return verificarUsuarioComPerfilGravarProcesso() || verificarUsuarioComPerfilPublicarProcesso();
	}

	public Boolean verificarUsuarioComPerfilPublicarProcesso() {
		return Identity.instance().hasRole(Papeis.PUBLICAR_PROCESSO_DECISAO_SESSAO_MURAL);
	}

	public Boolean verificarUsuarioComPerfilGravarProcesso() {
		return Identity.instance().hasRole(Papeis.GRAVAR_PROCESSO_DECISOES_SESSAO_MURAL);
	}
	
	public Boolean getApresentaBotaoPublicar() {
		if(apresentaBotaoPublicar == null){
			apresentaBotaoPublicar = liberacaoPublicacaoDecisaoService.verificarServicosDisponiveisParaPublicacao();
		}
		return apresentaBotaoPublicar;
	}

	public List<OrgaoJulgador> getOrgaosJulgadores() {
		OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent(OrgaoJulgadorManager.NAME);
		if(orgaosJulgadores == null){
			orgaosJulgadores = orgaoJulgadorManager.obterAtivosComCompetencia();
		}
		return orgaosJulgadores;
	}

	public List<LiberacaoPublicacaoDecisao> getPublicacoes() {
		if(publicacoes == null){
			publicacoes = new ArrayList<LiberacaoPublicacaoDecisao>();
		}
		return publicacoes;
	}

	public void setPublicacoes(List<LiberacaoPublicacaoDecisao> publicacoes) {
		this.publicacoes = publicacoes;
	}
	
	public List<LiberacaoPublicacaoDecisao> getPublicacoesSelecionadas() {
		if(publicacoesSelecionadas == null){
			publicacoesSelecionadas = new ArrayList<LiberacaoPublicacaoDecisao>();
		}
		return publicacoesSelecionadas;
	}

	public void setPublicacoesSelecionadas(List<LiberacaoPublicacaoDecisao> publicacoesSelecionadas) {
		this.publicacoesSelecionadas = publicacoesSelecionadas;
	}

	@Override
	protected DataRetriever<LiberacaoPublicacaoDecisao> getRetriever() {
		return null;
	}

	@Override
	public EntityDataModel<LiberacaoPublicacaoDecisao> getModel() {
		return null;
	}
	
	@Override
	protected BaseManager<LiberacaoPublicacaoDecisao> getManager() {
		return null;
	}
	
	public ConsultaPublicacaoSessaoVO getConsulta() {
		if(consulta == null){
			consulta = new ConsultaPublicacaoSessaoVO();
			if(verificarUsuarioComPerfilGravarProcesso()){
				consulta.getSituacaoLiberacao().addAll(SituacaoPublicacaoLiberacaoEnum.getListSituacaoPerfilGravarPublicacao());
			} else if(verificarUsuarioComPerfilPublicarProcesso()){
				consulta.getSituacaoLiberacao().addAll(SituacaoPublicacaoLiberacaoEnum.getListSituacaoPerfilPublicar());
			}
		}
		return consulta;
	}

	public void setConsulta(ConsultaPublicacaoSessaoVO consulta) {
		this.consulta = consulta;
	}

	public Boolean getSelecionarTodos() {
		return selecionarTodos;
	}

	public void setSelecionarTodos(Boolean selecionarTodos) {
		this.selecionarTodos = selecionarTodos;
	}

	public TipoPrazoEnum getTipoPrazoPadrao() {
		return tipoPrazoPadrao;
	}

	public Integer getPrazoPadrao() {
		return prazoPadrao;
	}
}