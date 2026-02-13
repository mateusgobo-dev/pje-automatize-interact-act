package br.com.jt.pje.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.SubstitutoProcessoSessaoBean;
import br.com.infox.pje.processor.SessaoJTFechamentoPautaProcessor;
import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.bean.OrgaoJulgadorFiltroBean;
import br.com.jt.pje.bean.ResultadoVotacaoFiltroBean;
import br.com.jt.pje.list.AbaPautaJulgamentoList;
import br.com.jt.pje.manager.AnotacaoVotoManager;
import br.com.jt.pje.manager.PautaSessaoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.pje.jt.entidades.AnotacaoVoto;
import br.jus.pje.jt.entidades.ComposicaoProcessoSessao;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.jt.enums.ResultadoVotacaoEnum;
import br.jus.pje.jt.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.util.DateUtil;

@Name(AbaPautaJulgamentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AbaPautaJulgamentoAction extends AbstractPautaAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 636133010321473110L;

	public static final String NAME = "abaPautaJulgamentoAction";
	
	private PautaSessao pautaSessao;
	private int qtdSustentacaoOral;
	private int qtdPreferencia;
	private List<PautaSessao> pautaSessaoSubstitutoList = new ArrayList<PautaSessao>(0);
//	private AbaPautaJulgamentoList abaPautaJulgamentoList = new AbaPautaJulgamentoList();
	private boolean checkBoxSelecionarTodos;
	private List<SubstitutoProcessoSessaoBean> listSubstitutoProcessoSessaoBean = new ArrayList<SubstitutoProcessoSessaoBean>(0);
	
	//Filtros
	private List<OrgaoJulgadorFiltroBean> orgaoJulgadorList;
	private List<ResultadoVotacaoFiltroBean> resultadoVotacaoList;
	private List<OrgaoJulgador> orgaoJulgadorCheckedList;
	private List<ResultadoVotacaoEnum> resultadoVotacaoCheckedList;
	private Boolean sustentacaoOral;
	private Boolean preferencia;
	
	@In
	private PautaSessaoManager pautaSessaoManager;
	@In
	private AnotacaoVotoManager anotacaoVotoManager;
	@In
	private ModalComposicaoProcessoAction modalComposicaoProcessoAction;
	@In
	private PessoaMagistradoManager pessoaMagistradoManager;

	@Override
	protected TipoInclusaoEnum getTipoInclusaoEnum() {
		return null;
	}

	@Override
	protected List<ProcessoTrf> getProcessoList() {
		return null;
	}
	
	public ResultadoVotacaoEnum[] getResultadoVotacaoEnumValues(){
		return ResultadoVotacaoEnum.values();
	}
	
	public void limparFiltrosPlacar(){
		setOrgaoJulgadorList(new ArrayList<OrgaoJulgadorFiltroBean>());
		setResultadoVotacaoList(new ArrayList<ResultadoVotacaoFiltroBean>());
		setOrgaoJulgadorCheckedList(null);
		setResultadoVotacaoCheckedList(null);
		setSustentacaoOral(null);
		setPreferencia(null);
		carregarFiltros();
	}

	public void desmarcaSustentacaoOral(){
		getPautaSessao().setSustentacaoOral(false);
		getPautaSessao().setAdvogadoPedidoSustentacaoOral(null);
	}
	
	public String msgDiasFechamantoPauta(){
		OrgaoJulgadorColegiado oJColegiado = getSessao().getOrgaoJulgadorColegiado();
		Date dataFechamento = getSessao().getDataFechamentoPauta();
		if(oJColegiado.getFechamentoAutomatico() && dataFechamento != null){
			Long diasLong = DateUtil.diferencaEntreDias(dataFechamento, new Date());			
			if(diasLong > 0){
				return "Faltam " + diasLong + " dia(s) para o fechamento da Pauta de Julgamento.";
			}
        }
		return null;
	}
	
	public void marcarFiltros(){
		marcarOrgaoJulgadorFiltro();
		marcarResultadoVotacaoFiltro();
	}
	
	private void marcarOrgaoJulgadorFiltro(){
		orgaoJulgadorCheckedList = new ArrayList<OrgaoJulgador>();
		for (OrgaoJulgadorFiltroBean bean : orgaoJulgadorList) {
			if(bean.getCheck()){
				orgaoJulgadorCheckedList.add(bean.getOrgaoJulgador());
			}
		}
	}
	
	private void marcarResultadoVotacaoFiltro(){
		resultadoVotacaoCheckedList = new ArrayList<ResultadoVotacaoEnum>();
		for (ResultadoVotacaoFiltroBean bean : resultadoVotacaoList) {
			if(bean.getCheck()){
				resultadoVotacaoCheckedList.add(bean.getResultadoVotacaoEnum());
			}
		}
	}
	
	public void carregarFiltros(){
		carregarOrgaoJulgadorFiltro();
		carregarResultadoVotacaoFiltro();
	}
	
	private void carregarOrgaoJulgadorFiltro(){
		List<OrgaoJulgador> list = composicaoSessaoManager.getOrgaoJulgadorBySessao(getSessao());
		orgaoJulgadorList = new ArrayList<OrgaoJulgadorFiltroBean>();
		for (OrgaoJulgador orgaoJulgador : list) {
			OrgaoJulgadorFiltroBean bean = new OrgaoJulgadorFiltroBean();
			bean.setOrgaoJulgador(orgaoJulgador);
			orgaoJulgadorList.add(bean);
		}
	}
	
	private void carregarResultadoVotacaoFiltro(){
		ResultadoVotacaoEnum[] enums = ResultadoVotacaoEnum.values();
		resultadoVotacaoList = new ArrayList<ResultadoVotacaoFiltroBean>();
		for (ResultadoVotacaoEnum resultadoVotacaoEnum : enums) {
			ResultadoVotacaoFiltroBean bean = new ResultadoVotacaoFiltroBean();
			bean.setResultadoVotacaoEnum(resultadoVotacaoEnum);
			resultadoVotacaoList.add(bean);
		}
	}
	
	public void quantidadesIndicadores(){
		List<PautaSessao> pautasSessao = pautaSessaoManager.listaPautaSessaoBySessao(getSessao());
		qtdSustentacaoOral = 0;
		qtdPreferencia = 0;
		for (PautaSessao pautaSessao : pautasSessao) {
			if(pautaSessao.getSustentacaoOral()){
				qtdSustentacaoOral++;
			}
			if(pautaSessao.getPreferencia()){
				qtdPreferencia++;
			}
		}
	}
	
	public Integer quantidadesPorOrgaoJulgador(OrgaoJulgador orgaoJulgador){
		return pautaSessaoManager.quantidadeProcessosByOrgaoJulgador(getSessao(), orgaoJulgador);
	}
	
	public Integer quantidadeProcessosResultadoVotacao(ResultadoVotacaoEnum resultadoVotacaoEnum){
		return pautaSessaoManager.quantidadeProcessosResultadoVotacao(getSessao(), resultadoVotacaoEnum);
	}
	
	public Integer quantidadeProcessosEmPauta(){
		return pautaSessaoManager.quantidadeProcessosEmPauta(getSessao());
	}
	
	public void gravarSustentacaoOral(){
		pautaSessaoManager.gravarSustentacaoOral(pautaSessao);
	}
	
	public void gravarPreferencia(PautaSessao pautaSessao){
		pautaSessaoManager.update(pautaSessao);
		FacesMessages.instance().add(Severity.INFO, "Registro atualizado com sucesso!");
	}
	
	public void removerProcessoDePauta(PautaSessao pautaSessao){
		List<Voto> votos = votoManager.getVotosByProcessoSessao(pautaSessao.getProcessoTrf(), getSessao());
		List<AnotacaoVoto> anotacaoList = anotacaoVotoManager.getAnotacoesBySessaoProcesso(pautaSessao.getSessao(), 
																							pautaSessao.getProcessoTrf());
		List<ComposicaoProcessoSessao> composicaoProcessoList = composicaoProcessoSessaoManager
														.getComposicaoProcessoByProcessoSessao(pautaSessao.getProcessoTrf(),
																								pautaSessao.getSessao());
		
		for(AnotacaoVoto av: anotacaoList){
			av.setSessao(null);
			anotacaoVotoManager.update(av);
		}
		
		if(votos != null){
			for (Voto voto : votos) {
				voto.setSessao(null);
				votoManager.update(voto);
			}
		}
		
		for(ComposicaoProcessoSessao cps: composicaoProcessoList){
			composicaoProcessoSessaoManager.remove(cps);
		}
		
		getPautaSessaoSubstitutoList().remove(pautaSessao);
		
		pautaSessaoManager.remove(pautaSessao);
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro removido com sucesso.");
	}
	
	public void carregaModalComposicaoProcesso(PautaSessao row){
		modalComposicaoProcessoAction.carregarComposicao()
									 	.doProcesso(row.getProcessoTrf())
									 	.daPauta(row)
									 	.naSessao(getSessao())
									 .executar();
	}
	
	public void addRemovePautaSessaoSubstituto(PautaSessao row){
		if(row.isCheckBoxSelecionado()){
			getPautaSessaoSubstitutoList().add(row);
			AbaPautaJulgamentoList list = ComponentUtil.getComponent("abaPautaJulgamentoList");
			if(getPautaSessaoSubstitutoList().size() == list.getResultCount()){
				setCheckBoxSelecionarTodos(true);
			}
		}else{
			getPautaSessaoSubstitutoList().remove(row);
			setCheckBoxSelecionarTodos(false);
		}
	}
	
	public void addRemoveAllPautaSessaoSubstituto(){
		if(getCheckBoxSelecionarTodos()){
			addAllPautaSessaoSubstituto();
		}else{
			removeAllPautaSessaoSubstituto();
		}
	}
	
	public void carregarAba(){
		setCheckBoxSelecionarTodos(false);
		removeAllPautaSessaoSubstituto();
	}

	private void removeAllPautaSessaoSubstituto() {
		for(PautaSessao ps: getPautaSessaoSubstitutoList()){
			ps.setCheckBoxSelecionado(false);
		}
		getPautaSessaoSubstitutoList().clear();
	}

	private void addAllPautaSessaoSubstituto() {
		getPautaSessaoSubstitutoList().clear();
		AbaPautaJulgamentoList list = ComponentUtil.getComponent("abaPautaJulgamentoList");
		for(PautaSessao ps: list.getResultList()){
			ps.setCheckBoxSelecionado(true);
			getPautaSessaoSubstitutoList().add(ps);
		}
	}
	
	public void carregarListBean(){
		listSubstitutoProcessoSessaoBean.clear();

		for(OrgaoJulgador oj: composicaoSessaoManager.getOrgaoJulgadorBySessao(getSessao())){
			SubstitutoProcessoSessaoBean bean = new SubstitutoProcessoSessaoBean(oj);
			listSubstitutoProcessoSessaoBean.add(bean);
		}
	}
	
	public void alterarSubstitutoProcessoLote(){
		composicaoProcessoSessaoManager.alterarSubstitutoProcessoLote(getPautaSessaoSubstitutoList(), listSubstitutoProcessoSessaoBean);
		setCheckBoxSelecionarTodos(false);
		removeAllPautaSessaoSubstituto();
	}
	
	public List<PessoaMagistrado> magistradoSubstitutoSessaoItems(OrgaoJulgador orgaoJulgador){
		return pessoaMagistradoManager.magistradoSubstitutoSessaoItems(orgaoJulgador, getSessao());
	}
	
	public void fecharPautaManual(){
		getSessao().setDataFechamentoPauta(new Date());
		pautaSessaoManager.update(getSessao());
		SessaoJTFechamentoPautaProcessor.instance().fecharPauta(Authenticator.getUsuarioLogado(), getSessao());
		
		setCheckBoxSelecionarTodos(false);
		removeAllPautaSessaoSubstituto();
	}
	
	/*
	 * Inicio getters e setters
	 */
	public PautaSessao getPautaSessao() {
		return pautaSessao;
	}

	public void setPautaSessao(PautaSessao pautaSessao) {
		this.pautaSessao = pautaSessao;
	}
	
	public void exibirPautaSessaoSustentacaoOral(PautaSessao pautaSessao){ 
		pautaSessao.setSustentacaoOral(false);
		this.pautaSessao = pautaSessao;
	}

	public int getQtdSustentacaoOral() {
		return qtdSustentacaoOral;
	}

	public void setQtdSustentacaoOral(int qtdSustentacaoOral) {
		this.qtdSustentacaoOral = qtdSustentacaoOral;
	}

	public int getQtdPreferencia() {
		return qtdPreferencia;
	}

	public void setQtdPreferencia(int qtdPreferencia) {
		this.qtdPreferencia = qtdPreferencia;
	}

	public List<OrgaoJulgadorFiltroBean> getOrgaoJulgadorList() {
		return orgaoJulgadorList;
	}

	public void setOrgaoJulgadorList(List<OrgaoJulgadorFiltroBean> orgaoJulgadorList) {
		this.orgaoJulgadorList = orgaoJulgadorList;
	}

	public List<ResultadoVotacaoFiltroBean> getResultadoVotacaoList() {
		return resultadoVotacaoList;
	}

	public void setResultadoVotacaoList(
			List<ResultadoVotacaoFiltroBean> resultadoVotacaoList) {
		this.resultadoVotacaoList = resultadoVotacaoList;
	}

	public List<OrgaoJulgador> getOrgaoJulgadorCheckedList() {
		return orgaoJulgadorCheckedList;
	}

	public void setOrgaoJulgadorCheckedList(List<OrgaoJulgador> orgaoJulgadorCheckedList) {
		this.orgaoJulgadorCheckedList = orgaoJulgadorCheckedList;
	}

	public List<ResultadoVotacaoEnum> getResultadoVotacaoCheckedList() {
		return resultadoVotacaoCheckedList;
	}

	public void setResultadoVotacaoCheckedList(List<ResultadoVotacaoEnum> resultadoVotacaoCheckedList) {
		this.resultadoVotacaoCheckedList = resultadoVotacaoCheckedList;
	}

	public Boolean getSustentacaoOral() {
		return sustentacaoOral;
	}

	public void setSustentacaoOral(Boolean sustentacaoOral) {
		this.sustentacaoOral = sustentacaoOral;
	}

	public Boolean getPreferencia() {
		return preferencia;
	}

	public void setPreferencia(Boolean preferencia) {
		this.preferencia = preferencia;
	}

	public List<PautaSessao> getPautaSessaoSubstitutoList() {
		return pautaSessaoSubstitutoList;
	}

	public void setPautaSessaoSubstitutoList(
			List<PautaSessao> pautaSessaoSubstitutoList) {
		this.pautaSessaoSubstitutoList = pautaSessaoSubstitutoList;
	}

	public boolean getCheckBoxSelecionarTodos() {
		return checkBoxSelecionarTodos;
	}

	public void setCheckBoxSelecionarTodos(boolean checkBoxSelecionarTodos) {
		this.checkBoxSelecionarTodos = checkBoxSelecionarTodos;
	}

	public List<SubstitutoProcessoSessaoBean> getListSubstitutoProcessoSessaoBean() {
		return listSubstitutoProcessoSessaoBean;
	}

	public void setListSubstitutoProcessoSessaoBean(
			List<SubstitutoProcessoSessaoBean> listSubstitutoProcessoSessaoBean) {
		this.listSubstitutoProcessoSessaoBean = listSubstitutoProcessoSessaoBean;
	}
}
