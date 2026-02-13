package br.com.jt.pje.action;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.resteasy.spi.ApplicationException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.editor.action.DocumentoAction;
import br.com.infox.editor.action.EditorAction;
import br.com.infox.ibpm.component.tree.PesquisaFeriadosBean;
import br.com.infox.view.GenericAction;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.list.RecursoParteList;
import br.com.jt.pje.manager.AssistenteAdmissibilidadeManager;
import br.com.jt.pje.manager.AssistenteAdmissibilidadeRecursoManager;
import br.com.jt.pje.manager.RecursoParteManager;
import br.com.jt.pje.manager.ResultadoSentencaParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisServiceImpl;
import br.jus.pje.jt.entidades.AssistenteAdmissibilidade;
import br.jus.pje.jt.entidades.AssistenteAdmissibilidadeRecurso;
import br.jus.pje.jt.entidades.LimiteDepositoRecursal;
import br.jus.pje.jt.entidades.RecursoParte;
import br.jus.pje.jt.entidades.SalarioMinimo;
import br.jus.pje.jt.enums.MotivoDispensaEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;

@Name(AssistenteAdmissibilidadeRecursoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AssistenteAdmissibilidadeRecursoAction extends GenericAction {

	/** Prazo inicial definido na RN 031/03 do Pacote 12 do TRT 9 */
	private static final int PRAZO_DIAS_DEFAULT = 8;

	private static final long serialVersionUID = -5098942720397481855L;

	public static final String NAME = "assistenteAdmissibilidadeRecursoAction";

	private AssistenteAdmissibilidade assistenteAdmissibilidade;
	private AssistenteAdmissibilidadeRecurso assistenteAdmissibilidadeRecurso;
	private List<AssistenteAdmissibilidadeRecurso> listaRecursos = new ArrayList<AssistenteAdmissibilidadeRecurso>();
	private List<ProcessoDocumento> listaDocumentos = new ArrayList<ProcessoDocumento>();
	private List<ProcessoDocumento> listaDocumentosRecurso = new ArrayList<ProcessoDocumento>();
	private ProcessoDocumento processoDocumento = new ProcessoDocumento();
	private Boolean exibirAssistente = Boolean.TRUE;
	private ProcessoTrf processoTrf;
	private Integer indice;
	private Map<Integer, List<ProcessoParte>> mapPartes = new HashMap<Integer, List<ProcessoParte>>();
	private RecursoParteList recursoParteList;
	private boolean houveAlteracoes;
	private boolean alterouTempestividade;
	private boolean alterouPreparo;
	private boolean alterouAdmissibilidade;
	private Boolean houveAlteracoesAssistente;
	
	public boolean isAlterouTempestividade() {
		return alterouTempestividade;
	}

	public void setAlterouTempestividade(boolean alterouTempestividade) {
		this.alterouTempestividade = alterouTempestividade;
		calcularAdmissibilidadeOk();
	}

	public boolean isAlterouPreparo() {
		return alterouPreparo;
	}

	public void setAlterouPreparo(boolean alterouPreparo) {
		this.alterouPreparo = alterouPreparo;
		calcularAdmissibilidadeOk();
	}

	public boolean isAlterouAdmissibilidade() {
		return alterouAdmissibilidade;
	}

	public void setAlterouAdmissibilidade(boolean alterouAdmissibilidade) {
		this.alterouAdmissibilidade = alterouAdmissibilidade;
	}

	@In private AssistenteAdmissibilidadeRecursoManager assistenteAdmissibilidadeRecursoManager;
	@In	private RecursoParteManager recursoParteManager;
	@In	private AssistenteAdmissibilidadeManager assistenteAdmissibilidadeManager;
	@In	private ResultadoSentencaParteManager resultadoSentencaParteManager;
	@In	private ProcessoDocumentoManager processoDocumentoManager;
	@In	private ProcessoParteExpedienteManager processoParteExpedienteManager;
	@In private DocumentoAction documentoAction;
	@In private ProcessoDocumentoBinPessoaAssinaturaManager processoDocumentoBinPessoaAssinaturaManager;
	
	public static AssistenteAdmissibilidadeRecursoAction instance(){
		return ComponentUtil.getComponent(NAME);
	}
	
	public boolean temDespachoNaoAssinado(){
		return processoDocumentoBinPessoaAssinaturaManager.temDocumentoNaoAssinadoPorTipo(processoTrf, ParametroUtil.instance().getTipoProcessoDocumentoDespacho());
	}
	
	public void selecione() throws InstantiationException, IllegalAccessException{
		if(!listaRecursos.isEmpty()){
			assistenteAdmissibilidadeRecurso = new AssistenteAdmissibilidadeRecurso();
			assistenteAdmissibilidadeRecurso = listaRecursos.get(indice);
			consultaRecursoParteList();
			AssistenteAdmissibilidadeRecurso recurso = EntityUtil.cloneEntity(assistenteAdmissibilidadeRecurso, false);
			recurso.getListaProcessoParte().addAll(assistenteAdmissibilidadeRecurso.getListaProcessoParte());
			Contexts.getConversationContext().set("recurso", recurso);
			alterouAdmissibilidade = false;
			alterouPreparo = false;
			alterouTempestividade = false;
		}
	}
	
	public boolean houveAlteracoes() throws Exception{
		AssistenteAdmissibilidadeRecurso recurso = (AssistenteAdmissibilidadeRecurso) Contexts.getConversationContext().get("recurso");

		if(recurso != null && assistenteAdmissibilidadeRecurso != null){
			houveAlteracoes = verificaAlteracao(recurso.getDataCienciaDecisao(), assistenteAdmissibilidadeRecurso.getDataCienciaDecisao()) ||
							  verificaAlteracao(recurso.getDataRecurso(), assistenteAdmissibilidadeRecurso.getDataRecurso()) ||
							  verificaAlteracao(recurso.getPrazoDias(), assistenteAdmissibilidadeRecurso.getPrazoDias()) ||
							  verificaAlteracao(recurso.getTempestividade(), assistenteAdmissibilidadeRecurso.getTempestividade()) ||
							  verificaAlteracao(recurso.getValorDeposito(), assistenteAdmissibilidadeRecurso.getValorDeposito()) ||
							  verificaAlteracao(recurso.getValorCustas(), assistenteAdmissibilidadeRecurso.getValorCustas()) ||
							  verificaAlteracao(recurso.getDispensado(), assistenteAdmissibilidadeRecurso.getDispensado()) ||
							  verificaAlteracao(recurso.getMotivoDispensa(), assistenteAdmissibilidadeRecurso.getMotivoDispensa()) ||
							  verificaAlteracao(recurso.getDispensa(), assistenteAdmissibilidadeRecurso.getDispensa()) ||
							  verificaAlteracao(recurso.getPreparo(), assistenteAdmissibilidadeRecurso.getPreparo()) ||
							  verificaAlteracao(recurso.getRepresentacao(), assistenteAdmissibilidadeRecurso.getRepresentacao()) ||
							  verificaAlteracao(recurso.getAdmissibilidade(), assistenteAdmissibilidadeRecurso.getAdmissibilidade()) ||
							  verificaAlteracao(recurso.getObservacao(), assistenteAdmissibilidadeRecurso.getObservacao());
			if(!houveAlteracoes){
				if(recurso.getListaProcessoParte() != null 
					&& assistenteAdmissibilidadeRecurso.getListaProcessoParte() != null 
					&& recurso.getListaProcessoParte().size() != assistenteAdmissibilidadeRecurso.getListaProcessoParte().size()){
					houveAlteracoes = true;
				}else if(recurso.getListaProcessoParte() != null 
						&& assistenteAdmissibilidadeRecurso.getListaProcessoParte() != null){
					houveAlteracoes = !assistenteAdmissibilidadeRecurso.getListaProcessoParte().equals(recurso.getListaProcessoParte());
				}
			}
		}else{
			houveAlteracoes = false;
		}
		return houveAlteracoes;
	}
	
	public void houveAlteracoesAssistente() throws Exception{
		AssistenteAdmissibilidade assistente = (AssistenteAdmissibilidade) Contexts.getConversationContext().get("assistenteAdmissibilidade");
		if(assistente != null && assistenteAdmissibilidade != null){
			houveAlteracoesAssistente = verificaAlteracao(assistente.getAlcada(), assistenteAdmissibilidade.getAlcada()) ||
										verificaAlteracao(assistente.getAlteradoCursoAcao(), assistenteAdmissibilidade.getAlteradoCursoAcao()) ||
										verificaAlteracao(assistente.getDataAjuizamentoAcao(), assistenteAdmissibilidade.getDataAjuizamentoAcao()) ||
										verificaAlteracao(assistente.getValorCausa(), assistenteAdmissibilidade.getValorCausa()) ||
										verificaAlteracao(assistente.getValorCondenacao(), assistenteAdmissibilidade.getValorCondenacao()) ||
										verificaAlteracao(assistente.getValorFixadoSentenca(), assistenteAdmissibilidade.getValorFixadoSentenca()) ||
										houveAlteracoes();
		}else{
			houveAlteracoesAssistente = false;
		}
	}
	
	private boolean verificaAlteracao(Object objectOld, Object object){
		if(objectOld != null && object != null && !getValue(objectOld).equals(getValue(object))){
			return true;
		}else if(objectOld == null && object != null || objectOld != null && object == null){
			return true;
		}
		return false;
	}
	
	private <T> String getValue(T field){
		if(field != null){
			if(field instanceof Date){
				DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
				return df.format(field);
			}else if(field instanceof Integer){
				return String.valueOf(((Integer) field).intValue());
			}else if(field instanceof Double){
				return String.valueOf(((Double) field).doubleValue());
			}else if(field instanceof Boolean){
				return String.valueOf(((Boolean) field).booleanValue());
			}else if(field instanceof MotivoDispensaEnum){
				return String.valueOf(((MotivoDispensaEnum) field).getLabel());
			}else{
				return field.toString();
			}
		}
		return null;
	}
	
	public RecursoParteList consultaRecursoParteList(){
		recursoParteList = new RecursoParteList();
		recursoParteList = ComponentUtil.getComponent(RecursoParteList.NAME);
		return recursoParteList;
	}
	
	public void addRemoveDocumento(ProcessoDocumento row){
		if(listaDocumentos.contains(row)){
			listaDocumentos.remove(row);
		}else{
			listaDocumentos.add(row);
		}
	}
	
	public void remove(AssistenteAdmissibilidadeRecurso recurso){
		for(AssistenteAdmissibilidadeRecurso aar : listaRecursos){
			if(listaDocumentos.contains(aar.getProcessoDocumento())){
				listaDocumentos.remove(aar.getProcessoDocumento());
			}
		}
		listaRecursos.remove(recurso);
		
		if(assistenteAdmissibilidadeRecurso != null && (recurso == assistenteAdmissibilidadeRecurso && recurso.getIdAssistenteAdmissibilidadeRecurso() == assistenteAdmissibilidadeRecurso.getIdAssistenteAdmissibilidadeRecurso())){
			indice = null;
			assistenteAdmissibilidadeRecurso = null;
		}else if(indice != null && recurso.getPrioridade().intValue()-1 < indice){
			indice--;
		}
		
		for(AssistenteAdmissibilidadeRecurso aar : listaRecursos){
			if(aar.getPrioridade().intValue() > recurso.getPrioridade().intValue()){
				aar.setPrioridade(aar.getPrioridade().intValue() -1);
			}
		}
		
		remover(recurso);
	}
	
	public void subir(int index){
		AssistenteAdmissibilidadeRecurso recurso = listaRecursos.get(index);
		AssistenteAdmissibilidadeRecurso previous = listaRecursos.get(index-1);
		
		recurso.setPrioridade(recurso.getPrioridade()-1);
		previous.setPrioridade(previous.getPrioridade()+1);
		
		listaRecursos.set(index, previous);
		listaRecursos.set(index-1, recurso);
		
		if(indice != null){
			if(indice == index){
				indice--;
			}else if(indice == index -1){
				indice++;
			}
			listaRecursos.set(indice, assistenteAdmissibilidadeRecurso);
		}
	}
	
	public void descer(int index){
		AssistenteAdmissibilidadeRecurso recurso = listaRecursos.get(index);
		AssistenteAdmissibilidadeRecurso next = listaRecursos.get(index+1);
		
		recurso.setPrioridade(recurso.getPrioridade()+1);
		next.setPrioridade(next.getPrioridade()-1);
		
		listaRecursos.set(index, next);
		listaRecursos.set(index+1, recurso);
		
		if(indice != null){
			if(indice == index ){
				indice++;
			}else if(indice == index+1){
				indice--;
			}
			listaRecursos.set(indice, assistenteAdmissibilidadeRecurso);
		}
	}
	
	public void inserirRecursos() throws InstantiationException, IllegalAccessException{
		Integer prioridade = getMaxPrioridadeProcesso();
		boolean adicionouRecurso = false;
		documentos: for (ProcessoDocumento documento : getListaDocumentos()) {
			for(AssistenteAdmissibilidadeRecurso adr : listaRecursos){
				if(adr.getProcessoDocumento().getIdProcessoDocumento() == documento.getIdProcessoDocumento()){
					continue documentos;
				}
			}
			AssistenteAdmissibilidadeRecurso assisAdmisRec = new AssistenteAdmissibilidadeRecurso();
			assisAdmisRec.setAssistenteAdmissibilidade(assistenteAdmissibilidade);
			assisAdmisRec.setProcessoDocumento(documento);
			assisAdmisRec.setPrioridade(++prioridade);
			listaRecursos.add(assisAdmisRec);
			indice = assisAdmisRec.getPrioridade()-1;
			adicionouRecurso = true;
		}
		if(adicionouRecurso){
			selecione();
			carregarDadosRecurso();
		}
	}
	
	private Integer getMaxPrioridadeProcesso() {
		if(getListaRecursos() == null || getListaRecursos().isEmpty()){
			return 0;
		}
		Integer result = 0;
		for(AssistenteAdmissibilidadeRecurso aar : getListaRecursos()){
			if(aar.getPrioridade().intValue() > result.intValue()){
				result = aar.getPrioridade();
			}
		}
		return result;
	}
	
	public void adicionaParte(ProcessoParte parte){
		assistenteAdmissibilidadeRecurso
			.getListaProcessoParte()
			.add(parte);
	}
	
	public void removeParte(ProcessoParte parte){
		assistenteAdmissibilidadeRecurso
			.getListaProcessoParte()
			.remove(parte);
	}
	
	public void adicionaTodasPartes(){
		assistenteAdmissibilidadeRecurso
			.getListaProcessoParte()
			.addAll(getRecursoParteList().getResultList());
	}
	
	public void removeTodasPartes(){
		assistenteAdmissibilidadeRecurso
			.getListaProcessoParte()
			.clear();
	}

	public MotivoDispensaEnum[] getMotivoDispensaValues(){
		return MotivoDispensaEnum.values();
	}
	
	public void gerarDespacho(){
		try{
			gravarDespacho();
		}catch(ApplicationException e){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
		}
	}
	
	public void gravarDespacho(){
		try{
			gravarAssistente();
		}catch(ApplicationException e){
			throw e;
		}
		documentoAction.refreshTree();
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoDocumentoEstruturado o ");
		sb.append("where o.assistenteAdmissibilidade.idAssistenteAdmissibilidade = :idAssistenteAdmissibilidade ");
		Query q = EntityUtil.createQuery(sb.toString());
		q.setParameter("idAssistenteAdmissibilidade", assistenteAdmissibilidade.getIdAssistenteAdmissibilidade());
		ProcessoDocumentoEstruturado processoDocumentoEstruturado = EntityUtil.getSingleResult(q);
		if(processoDocumentoEstruturado != null){
			EditorAction.instance().setDocumento(processoDocumentoEstruturado);
		}
		EditorAction.instance().getDocumento().setAssistenteAdmissibilidade(assistenteAdmissibilidade);
		EditorAction.instance().save();
		exibirAssistente = Boolean.FALSE;
	}

	public void gravar(){
		try{
			gravarAssistente();
		}catch(ApplicationException e){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
		}
	}
	
	public void desfazer(){
		AssistenteAdmissibilidadeRecurso recurso = (AssistenteAdmissibilidadeRecurso) Contexts.getConversationContext().get("recurso");
		assistenteAdmissibilidadeRecurso.setDataCienciaDecisao(recurso.getDataCienciaDecisao());
		assistenteAdmissibilidadeRecurso.setDataRecurso(recurso.getDataRecurso());
		assistenteAdmissibilidadeRecurso.setPrazoDias(recurso.getPrazoDias());
		assistenteAdmissibilidadeRecurso.setTempestividade(recurso.getTempestividade());
		assistenteAdmissibilidadeRecurso.setValorDeposito(recurso.getValorDeposito());
		assistenteAdmissibilidadeRecurso.setValorCustas(recurso.getValorCustas());
		assistenteAdmissibilidadeRecurso.setDispensado(recurso.getDispensado());
		assistenteAdmissibilidadeRecurso.setMotivoDispensa(recurso.getMotivoDispensa());
		assistenteAdmissibilidadeRecurso.setDispensa(recurso.getDispensa());
		assistenteAdmissibilidadeRecurso.setPreparo(recurso.getPreparo());
		assistenteAdmissibilidadeRecurso.setRepresentacao(recurso.getRepresentacao());
		assistenteAdmissibilidadeRecurso.setAdmissibilidade(recurso.getAdmissibilidade());
		assistenteAdmissibilidadeRecurso.setObservacao(recurso.getObservacao());
	}
	
	public void gravarAssistente(){
		for(AssistenteAdmissibilidadeRecurso recurso : listaRecursos){
			if(recurso.getListaProcessoParte() == null || recurso.getListaProcessoParte().isEmpty()){
				throw new ApplicationException("Cada recurso deve ter pelo menos um recorrente.", null);
			}
		}
		if(processoDocumento != null && processoDocumento.getIdProcessoDocumento() != 0){
			assistenteAdmissibilidade.setIdDocumentoRecorrido(processoDocumento.getIdProcessoDocumento());
		}
		if(assistenteAdmissibilidade.getIdAssistenteAdmissibilidade() == 0){
			persist(assistenteAdmissibilidade);
		}else{
			update(assistenteAdmissibilidade);
		}
		for(AssistenteAdmissibilidadeRecurso recurso : listaRecursos){
			gravar(recurso);
		}
		Contexts.getConversationContext().set("recurso", assistenteAdmissibilidadeRecurso);
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro alterado com sucesso!");
	}
	
	public void gravaListaRecursos(){
		for(AssistenteAdmissibilidadeRecurso recurso : listaRecursos){
			gravar(recurso);
		}
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro alterado com sucesso!");
	}
	
	private void remover(AssistenteAdmissibilidadeRecurso recurso) {
		if(recurso.getIdAssistenteAdmissibilidadeRecurso() != 0){
			//remover lista de vinculação de partes
			List<RecursoParte> listaRecursoParteVinculada = recursoParteManager.getRecursoPartesByRecurso(recurso.getIdAssistenteAdmissibilidadeRecurso());
			for (RecursoParte recursoParte : listaRecursoParteVinculada) {
				super.remove(recursoParte);
			}
			super.remove(recurso);
		}
	}
	
	public boolean temParteAdicionada(){
		return assistenteAdmissibilidadeRecurso == null || assistenteAdmissibilidadeRecurso.getListaProcessoParte() != null && !assistenteAdmissibilidadeRecurso.getListaProcessoParte().isEmpty();
	}
	
	public void gravaRecurso(AssistenteAdmissibilidadeRecurso recurso){
		if(assistenteAdmissibilidade.getIdAssistenteAdmissibilidade() == 0){
			persist(assistenteAdmissibilidade);
		}else{
			update(assistenteAdmissibilidade);
		}
		gravar(recurso);
	}

	public void gravar(AssistenteAdmissibilidadeRecurso recurso) {
		recurso.setAssistenteAdmissibilidade(assistenteAdmissibilidade);
		if(recurso.getIdAssistenteAdmissibilidadeRecurso() == 0){
			persist(recurso);
		}else{
			List<ProcessoParte> list = mapPartes.get(recurso.getIdAssistenteAdmissibilidadeRecurso());
			if(list != null){
				for (ProcessoParte processoParte : list) {
					if(!recurso.getListaProcessoParte().contains(processoParte)){
						recursoParteManager
							.removeBy(recurso.getIdAssistenteAdmissibilidadeRecurso(), 
									  processoParte.getIdProcessoParte());
					}
				}
			}
			update(recurso);
		}
		for (ProcessoParte parte : recurso.getListaProcessoParte()) {
			if(!recursoParteManager.existeRecursoParte(recurso.getIdAssistenteAdmissibilidadeRecurso(), parte.getIdProcessoParte())){
				RecursoParte recursoParte = new RecursoParte();
				recursoParte.setAssistenteAdmissibilidadeRecurso(recurso);
				recursoParte.setProcessoParte(parte);
				persist(recursoParte);
			}
		}
	}

	public void carregarDadosAssistente() throws InstantiationException, IllegalAccessException{
		exibirAssistente = Boolean.TRUE;
		
		if(assistenteAdmissibilidade != null){
			EntityUtil.getEntityManager().refresh(assistenteAdmissibilidade);
		}
		if(assistenteAdmissibilidadeRecurso != null){
			EntityUtil.getEntityManager().refresh(assistenteAdmissibilidadeRecurso);
		}
		
		assistenteAdmissibilidade = assistenteAdmissibilidadeManager.getUltimoAssistenteBy(processoTrf.getIdProcessoTrf());
		
		if(assistenteAdmissibilidade != null){
			listaRecursos = assistenteAdmissibilidadeRecursoManager.getRecursosBy(assistenteAdmissibilidade.getIdAssistenteAdmissibilidade());
			processoDocumento = EntityUtil.find(ProcessoDocumento.class, assistenteAdmissibilidade.getIdDocumentoRecorrido());
			
			for (AssistenteAdmissibilidadeRecurso recurso : listaRecursos) {
				List<ProcessoParte> list = recursoParteManager.getPartesBy(recurso.getIdAssistenteAdmissibilidadeRecurso());
				mapPartes.put(recurso.getIdAssistenteAdmissibilidadeRecurso(), list);
				for(ProcessoParte pp : list){
					if(!recurso.getListaProcessoParte().contains(pp)){
						recurso.getListaProcessoParte().add(pp);
					}
				}
			}
		}
		if(assistenteAdmissibilidade == null && processoTrf != null){
			assistenteAdmissibilidade = new AssistenteAdmissibilidade();
			assistenteAdmissibilidade.setDataAjuizamentoAcao(processoTrf.getDataAutuacao()); //data_ajuizamento_acao
			assistenteAdmissibilidade.setValorCausa(processoTrf.getValorCausa()); //valor_causa
		}
		if(assistenteAdmissibilidade.getValorCondenacao() == null){
			assistenteAdmissibilidade.setValorCondenacao(resultadoSentencaParteManager.getSomaValorCondenacaoBy(processoTrf.getIdProcessoTrf()));
		}
		AssistenteAdmissibilidade assistente = EntityUtil.cloneEntity(assistenteAdmissibilidade, false);
		Contexts.getConversationContext().set("assistenteAdmissibilidade", assistente);
	}
	
	public void exibirEditor() throws Exception{
		houveAlteracoesAssistente();
	}
	
	private void carregarDadosRecurso() {
		List<ProcessoParteExpediente> partes = processoParteExpedienteManager.getPartesDoExpedienteBy(getProcessoDocumento().getIdProcessoDocumento());
		ProcessoParteExpediente ppe = partes != null && partes.size() > 0 ? partes.get(0) : null;
		if(ppe != null){
			assistenteAdmissibilidadeRecurso.setDataCienciaDecisao(ppe.getDtCienciaParte());
		}
		
		ProcessoDocumento pd = findById(ProcessoDocumento.class, assistenteAdmissibilidadeRecurso.getProcessoDocumento().getIdProcessoDocumento());
		if(pd != null){
			assistenteAdmissibilidadeRecurso.setDataRecurso(pd.getDataInclusao());
		}
		assistenteAdmissibilidadeRecurso.setPrazoDias(PRAZO_DIAS_DEFAULT);
	}
	
	public String formatarData(Date data){
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(data);
	}

	public List<ProcessoDocumento> listaDecisoesSentenca() throws ParseException{
		int idProcesso = processoTrf != null ? processoTrf.getIdProcessoTrf() : 0;
		List<ProcessoDocumento> documentosTemp = processoDocumentoManager.getDocumentosAssinadosPorMagistradosBy(idProcesso);
		List<ProcessoDocumento> documentos = new ArrayList<ProcessoDocumento>();
		for (ProcessoDocumento processoDocumento : documentosTemp) {
			if(!processoDocumento.getProcessoDocumento().equals("Petição Inicial")){
				documentos.add(processoDocumento);
			}
		}
		return documentos;
	}
	
	public List<ProcessoDocumento> listaDocumentosRecursos() throws ParseException{
		List<Integer> idsDocumentosRecurso = new ArrayList<Integer>();
		for (AssistenteAdmissibilidadeRecurso recurso : listaRecursos) {
			idsDocumentosRecurso.add(recurso.getProcessoDocumento().getIdProcessoDocumento());
		}
		
		int idProcesso = processoTrf != null ? processoTrf.getIdProcessoTrf() : 0;
		List<ProcessoDocumento> documentosTemp = processoDocumentoManager.getDocumentosAssinadosPorAdvogadosOuProcuradoresBy(idProcesso);
		listaDocumentosRecurso = new ArrayList<ProcessoDocumento>();
		listaDocumentosRecurso.addAll(documentosTemp);
		for (ProcessoDocumento processoDocumento : documentosTemp) {
			for (Integer id : idsDocumentosRecurso) {
				if(processoDocumento.getProcessoDocumento().equals("Petição Inicial")){
					listaDocumentosRecurso.remove(processoDocumento);
				}
				if(id == processoDocumento.getIdProcessoDocumento()){
					listaDocumentosRecurso.remove(processoDocumento);
				}
			}
		}
		return listaDocumentosRecurso;
	}
	
	public void verificarAlcada(){
		SalarioMinimo sm = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SalarioMinimo o ");
		sb.append("where o.dataFimVigencia is null and :dataPesquisada >= o.dataInicioVigencia ");
		sb.append("or :dataPesquisada between o.dataInicioVigencia and o.dataFimVigencia ");
		Query q = EntityUtil.createQuery(sb.toString()).setParameter("dataPesquisada", assistenteAdmissibilidade.getDataAjuizamentoAcao());
		sm = EntityUtil.getSingleResult(q);
		if(sm != null){
			Double valorCausa = assistenteAdmissibilidade.getAlteradoCursoAcao() ? assistenteAdmissibilidade.getValorFixadoSentenca() : assistenteAdmissibilidade.getValorCausa();
			if(valorCausa == null){
				FacesMessages.instance().add(Severity.ERROR, "O valor da causa não está preenchido");
				assistenteAdmissibilidade.setAlcada(false);
				return;
			}
			assistenteAdmissibilidade.setAlcada(valorCausa > (sm.getValor()*2));
		}else{
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Não foi possível fazer a verificação da Alçada pois não existe salário mínimo cadastrado para a data de ajuizamento da ação. Favor informar a validade da Alçada.");
			assistenteAdmissibilidade.setAlcada(false);
		}
		calcularAdmissibilidadeOk();
	}
	
	public void verificarTempestividade(){
		alterouTempestividade = false;
		PrazosProcessuaisService prazosProcessuaisService = new PrazosProcessuaisServiceImpl();
		int idProcesso = assistenteAdmissibilidadeRecurso.getProcessoDocumento().getProcesso().getIdProcesso();
		ProcessoTrf procTrf = EntityUtil.find(ProcessoTrf.class, idProcesso);
		OrgaoJulgador oj = procTrf.getOrgaoJulgador();
		
		if(camposTempestividadePreenchidos() && assistenteAdmissibilidadeRecurso.getPrazoDias() != null && !assistenteAdmissibilidadeRecurso.getPrazoDias().equals(0)){
			Date dataPrazo = prazosProcessuaisService
								.calculaPrazoProcessualTempestividade(
										assistenteAdmissibilidadeRecurso.getDataCienciaDecisao(),
										assistenteAdmissibilidadeRecurso.getPrazoDias(),
										TipoPrazoEnum.D,
										prazosProcessuaisService.obtemCalendario(oj),
										procTrf.getCompetencia().getCategoriaPrazoProcessual(),
										ContagemPrazoEnum.M);
			
			boolean tempestividadeOk = assistenteAdmissibilidadeRecurso.getDataRecurso().before(assistenteAdmissibilidadeRecurso.getDataCienciaDecisao()) 
									|| !assistenteAdmissibilidadeRecurso.getDataRecurso().after(dataPrazo) 
									|| dataPrazo.equals(assistenteAdmissibilidadeRecurso.getDataRecurso());
			assistenteAdmissibilidadeRecurso.setTempestividade(tempestividadeOk);
		}else{
			assistenteAdmissibilidadeRecurso.setTempestividade(false);
		}
		calcularAdmissibilidadeOk();
	}
	
	private boolean camposTempestividadePreenchidos(){
		FacesMessages messages = FacesMessages.instance();
		messages.clear();
		boolean validado = true;
		if(assistenteAdmissibilidadeRecurso.getDataCienciaDecisao() == null){
			messages.add(Severity.ERROR, "A data de ciência da decisão não está preenchida");
			validado = false;
		}
		if(assistenteAdmissibilidadeRecurso.getPrazoDias() == null){
			messages.add(Severity.ERROR, "O prazo em dias não está preenchido");
			validado = false;
		}
		if(assistenteAdmissibilidadeRecurso.getDataRecurso() == null){
			messages.add(Severity.ERROR, "A data do recurso não está preenchida");
			validado = false;
		}
		return validado;
	}

	public void verificarPreparo(){
		alterouPreparo = false;
		assistenteAdmissibilidadeRecurso.setPreparo(false);
		Double valorCondenacao = assistenteAdmissibilidade.getValorCondenacao();
		if(assistenteAdmissibilidadeRecurso.getDispensa() && assistenteAdmissibilidadeRecurso.getDispensado()){
			assistenteAdmissibilidadeRecurso.setPreparo(true);
			calcularAdmissibilidadeOk();
			return;
		}
		if(valorCondenacao == null){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "O valor da condenação não está preenchido");
			assistenteAdmissibilidadeRecurso.setPreparo(false);
			calcularAdmissibilidadeOk();
			return;
		}
		if(!assistenteAdmissibilidadeRecurso.getDispensa()){ //não existe dispensa das custas
			Double valorCustas = assistenteAdmissibilidadeRecurso.getValorCustas();
			if(valorCustas == null){
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, "O valor das custas não está preenchido");
				assistenteAdmissibilidadeRecurso.setPreparo(false);
				calcularAdmissibilidadeOk();
				return;
			}
			Double valorCalculado = (valorCondenacao * 2)/100;
			assistenteAdmissibilidadeRecurso.setPreparo(valorCustas >= valorCalculado);
			calcularAdmissibilidadeOk();
			if(!assistenteAdmissibilidadeRecurso.getPreparo()){
				return;
			}
		}else{
			assistenteAdmissibilidadeRecurso.setPreparo(true);
			calcularAdmissibilidadeOk();
		}
		if(!assistenteAdmissibilidadeRecurso.getDispensado()){ //não existe dispensa do depósito recursal
			Double valorLimite = valorCondenacao;
			if(assistenteAdmissibilidadeRecurso.getDataRecurso() != null){
				LimiteDepositoRecursal ldr = null;
				StringBuilder sb = new StringBuilder();
				sb.append("select o from LimiteDepositoRecursal o ");
				sb.append("where o.dataFimVigencia is null and :dataPesquisada >= o.dataInicioVigencia ");
				sb.append("or :dataPesquisada between o.dataInicioVigencia and o.dataFimVigencia ");
				Query q = EntityUtil.createQuery(sb.toString()).setParameter("dataPesquisada", assistenteAdmissibilidadeRecurso.getDataRecurso());
				ldr = EntityUtil.getSingleResult(q);
				if(ldr != null){
					if(ParametroUtil.instance().isPrimeiroGrau()){
						valorLimite = ldr.getValor1Grau();
					}else{
						valorLimite = ldr.getValor2Grau();
					}
				}
			}
			
			Double valorDeposito = assistenteAdmissibilidadeRecurso.getValorDeposito();
			if(valorDeposito == null){
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, "O valor do depósito não está preenchido");
				assistenteAdmissibilidadeRecurso.setPreparo(false);
				calcularAdmissibilidadeOk();
				return;
			}
			if(valorCondenacao > valorLimite){
				assistenteAdmissibilidadeRecurso.setPreparo(valorDeposito >= valorLimite);
			}else{
				assistenteAdmissibilidadeRecurso.setPreparo(valorDeposito >= valorCondenacao);
			}
			calcularAdmissibilidadeOk();
			return;
		}else{
			assistenteAdmissibilidadeRecurso.setPreparo(true);
			calcularAdmissibilidadeOk();
		}
	}
	
	public void calcularAdmissibilidadeOk(){
		alterouAdmissibilidade = false;
		boolean admissibilidadeOk = assistenteAdmissibilidade != null &&
				assistenteAdmissibilidade.getAlcada() && 
				assistenteAdmissibilidadeRecurso != null &&
				assistenteAdmissibilidadeRecurso.getTempestividade() && 
				assistenteAdmissibilidadeRecurso.getPreparo() && 
				assistenteAdmissibilidadeRecurso.getRepresentacao();
		
		if(assistenteAdmissibilidadeRecurso != null){
			assistenteAdmissibilidadeRecurso.setAdmissibilidade(admissibilidadeOk);
		}
	}
	
	public void marcarDispensado(){
		if(!assistenteAdmissibilidadeRecurso.getDispensado() && assistenteAdmissibilidadeRecurso.getMotivoDispensa() != null){
			assistenteAdmissibilidadeRecurso.setMotivoDispensa(null);
		}
	}
	
	public boolean observacaoObrigatoria(){
		return alterouAdmissibilidade || alterouPreparo || alterouTempestividade || (assistenteAdmissibilidadeRecurso != null && !assistenteAdmissibilidadeRecurso.getRepresentacao());
	}
	
	public void zeraListaDocumentos(){
		this.listaDocumentos = new ArrayList<ProcessoDocumento>();
	}

	public AssistenteAdmissibilidade getAssistenteAdmissibilidade() {
		return assistenteAdmissibilidade;
	}

	public void setAssistenteAdmissibilidade(AssistenteAdmissibilidade assistenteAdmissibilidade) {
		this.assistenteAdmissibilidade = assistenteAdmissibilidade;
	}

	public AssistenteAdmissibilidadeRecurso getAssistenteAdmissibilidadeRecurso() {
		return assistenteAdmissibilidadeRecurso;
	}

	public void setAssistenteAdmissibilidadeRecurso(AssistenteAdmissibilidadeRecurso assistenteAdmissibilidadeRecurso) {
		this.assistenteAdmissibilidadeRecurso = assistenteAdmissibilidadeRecurso;
	}

	public List<AssistenteAdmissibilidadeRecurso> getListaRecursos() {
		return listaRecursos;
	}

	public void setListaRecursos(List<AssistenteAdmissibilidadeRecurso> listaRecursos) {
		this.listaRecursos = listaRecursos;
	}
	
	public Boolean getExibirAssistente() {
		return exibirAssistente;
	}
	
	public void setExibirAssistente(Boolean exibirAssistente) {
		this.exibirAssistente = exibirAssistente;
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) throws InstantiationException, IllegalAccessException {
		this.processoTrf = processoTrf;
		EditorAction editorAction = EditorAction.instance();
		editorAction.setProcessoTrf(processoTrf);
		
		AssistenteAdmissibilidade assistenteAdmissibilidade = assistenteAdmissibilidadeManager.getUltimoAssistenteBy(processoTrf.getIdProcessoTrf());
		if(assistenteAdmissibilidade != null){
			StringBuilder sb = new StringBuilder();
			sb.append("select o from ProcessoDocumentoEstruturado o ");
			sb.append("where o.assistenteAdmissibilidade.idAssistenteAdmissibilidade = :idAssistenteAdmissibilidade ");
			Query q = EntityUtil.createQuery(sb.toString());
			q.setParameter("idAssistenteAdmissibilidade", assistenteAdmissibilidade.getIdAssistenteAdmissibilidade());
			ProcessoDocumentoEstruturado processoDocumentoEstruturado = EntityUtil.getSingleResult(q);
			if(processoDocumentoEstruturado != null){
				EditorAction.instance().setDocumento(processoDocumentoEstruturado);
			}
		}
		
		if(editorAction.getDocumento() != null){
			exibirAssistente = Boolean.FALSE;
		}else{
			carregarDadosAssistente();
		}
	}

	public List<ProcessoDocumento> getListaDocumentos() {
		return listaDocumentos;
	}

	public void setListaDocumentos(List<ProcessoDocumento> listaDocumentos) {
		this.listaDocumentos = listaDocumentos;
	}

	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	public Integer getIndice() {
		return indice;
	}

	public void setIndice(Integer indice) {
		this.indice = indice;
	}

	public RecursoParteList getRecursoParteList() {
		return recursoParteList;
	}

	public void setRecursoParteList(RecursoParteList recursoParteList) {
		this.recursoParteList = recursoParteList;
	}

	public List<ProcessoDocumento> getListaDocumentosRecurso() {
		return listaDocumentosRecurso;
	}

	public void setListaDocumentosRecurso(List<ProcessoDocumento> listaDocumentosRecurso) {
		this.listaDocumentosRecurso = listaDocumentosRecurso;
	}

	public boolean isHouveAlteracoes() {
		return houveAlteracoes;
	}

	public void setHouveAlteracoes(boolean houveAlteracoes) {
		this.houveAlteracoes = houveAlteracoes;
	}
	
	public void carregaPesquisaCalendario(){
		PesquisaFeriadosBean pesquisaFeriadosBean = new PesquisaFeriadosBean();
		
		pesquisaFeriadosBean.setDataInicial(assistenteAdmissibilidadeRecurso.getDataCienciaDecisao());
		pesquisaFeriadosBean.setDataFinal(assistenteAdmissibilidadeRecurso.getDataRecurso());
		pesquisaFeriadosBean.setIdMunicipio(processoTrf.getOrgaoJulgador().getJurisdicao().getMunicipioSede().getIdMunicipio());
		pesquisaFeriadosBean.setIdEstado(processoTrf.getOrgaoJulgador().getJurisdicao().getEstado().getIdEstado());
		pesquisaFeriadosBean.setIdOrgaoJulgador(processoTrf.getOrgaoJulgador().getIdOrgaoJulgador());
		
		Contexts.getSessionContext().set("pesquisaFeriadosBeanTemp", pesquisaFeriadosBean);
	}

	public Boolean getHouveAlteracoesAssistente() {
		return houveAlteracoesAssistente;
	}

	public void setHouveAlteracoesAssistente(Boolean houveAlteracoesAssistente) {
		this.houveAlteracoesAssistente = houveAlteracoesAssistente;
	}
	
}