package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoVisibilidadeSegredo;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;

@Name(ProcessoDocumentoVisibilidadeSegredoHome.NAME)
@BypassInterceptors
public class ProcessoDocumentoVisibilidadeSegredoHome extends
		AbstractProcessoDocumentoVisibilidadeSegredoHome<ProcessoDocumentoVisibilidadeSegredo> {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "processoDocumentoVisibilidadeSegredoHome";

	private Boolean checkBox = Boolean.FALSE;
	private Boolean checkBoxParte = Boolean.FALSE;
	private Boolean checkBoxExpediente = Boolean.FALSE;
	
	
	private final String processoParteSigiloGrid = "processoParteSigiloGrid";
	private final String processoExpedienteSigiloGrid = "processoExpedienteSigiloGrid";
	private final String processoServidorSigiloGrid = "processoServidorSigiloGrid";
	private final String processoServidorSigilo2GrauGrid = "processoServidorSigilo2GrauGrid";
	
	private boolean isLote = false;
	

	public ProcessoDocumentoVisibilidadeSegredoHome instance() {
		return ComponentUtil.getComponent(ProcessoDocumentoVisibilidadeSegredoHome.class);
	}

	public void checkAll(String grid) {
		if (grid.equals(this.processoParteSigiloGrid))
			this.marcaGridCom(this.processoParteSigiloGrid, checkBoxParte, false);
		
		if (grid.equals(this.processoExpedienteSigiloGrid))
			this.marcaGridCom(this.processoExpedienteSigiloGrid, checkBoxExpediente, false);
		
		if (grid.equals(this.processoServidorSigiloGrid))
			this.marcaGridCom(this.processoServidorSigiloGrid, checkBox, false);
		
		if (grid.equals(this.processoServidorSigilo2GrauGrid))
			this.marcaGridCom(this.processoServidorSigilo2GrauGrid, checkBox, false);
		
		this.refreshGrid(grid);
	}
	
	/**
	 * dado um documento, marca nos grids os seus visualizadores
	 * a chamada desse metodo indica que a tela foi aberta nao utilizando a funcionalidade em lote
	 * @param processoDocumento
	 */
	public void verificaPessoasMarcadas(ProcessoDocumento processoDocumento) {
		ProcessoDocumentoHome.instance().setInstance(processoDocumento);
		this.limpaPessoasMarcadas();
		
		// Consulta as pessoas que têm permissão
		List<Pessoa> pessoasComPermissao = this.buscaPessoasComPermissao(ProcessoDocumentoHome.instance().getInstance());		
		
		for (Pessoa p : pessoasComPermissao) {			
			this.iteraGridEMarcaPessoa(this.processoParteSigiloGrid, p);
			this.iteraGridEMarcaPessoa(this.processoExpedienteSigiloGrid, p);
			
			if(ParametroUtil.instance().isPrimeiroGrau()) { 
				this.iteraGridEMarcaPessoa(this.processoServidorSigiloGrid, p);
			}else { 
				this.iteraGridEMarcaPessoa(this.processoServidorSigilo2GrauGrid, p);
			}
		}
		this.isLote = false;
	}
	
	
	/**
	 * limpa possiveis marcacoes existentes nos grids e indica que esta sendo feita uma operacao em lote
	 * na funcionalidade de definir visualizadores em lote, a tela para marcar os visualizadores vem sem nenhum visalizador marcado
	 */
	public void limpaPessoasMarcadasEIndicaLote() {
		this.limpaPessoasMarcadas();		
		this.isLote = true;
	}
	
	private void limpaPessoasMarcadas() {
		this.marcaGridCom(this.processoParteSigiloGrid, false, true);
		this.marcaGridCom(this.processoExpedienteSigiloGrid, false, true);
		
		if(ParametroUtil.instance().isPrimeiroGrau()) {
			this.marcaGridCom(this.processoServidorSigiloGrid, false, true);
		}else{
			this.marcaGridCom(this.processoServidorSigilo2GrauGrid, false, true);
		}
	}
	
	
	private void salvarVisualisadoresMarcadosParaOsDocumentos(String grid, List<ProcessoDocumento> docs) {		
		if (grid.equals(this.processoServidorSigiloGrid) || grid.equals(this.processoServidorSigilo2GrauGrid)) {			
			this.gravaItensGridServidor(getComponent(grid), docs);
			return;
		}
		
		if (grid.equals(this.processoParteSigiloGrid)) {
			this.gravaItensGridProcessoParte(docs);
			return;
		}
		
		if (grid.equals(this.processoExpedienteSigiloGrid)) {
			this.gravaItensGridExpediente(docs);
			return;
		}		
		refreshGrid(grid);
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro atualizado com sucesso!");
	}
	
	
	public void gravar(String grid) {
		List<ProcessoDocumento> documentos = new ArrayList<ProcessoDocumento>();
		
		if(this.isLote) documentos = ProcessoDocumentoHome.instance().getListaSigilo();
		else documentos.add(ProcessoDocumentoHome.instance().getInstance());
		
		this.salvarVisualisadoresMarcadosParaOsDocumentos(grid, documentos);
	}

	/**
	 * Retorna TRUE se não existir na base de dados e FALSE se existir
	 * 
	 * @param instance
	 * @return
	 */
	public boolean verificaDuplicidade(ProcessoDocumentoVisibilidadeSegredo instance, ProcessoDocumento processoDocumento) {
		ProcessoDocumentoVisibilidadeSegredo segredo = pegaPermissao(instance.getPessoa(), processoDocumento);
		return (segredo == null);
	}

	public ProcessoDocumentoVisibilidadeSegredo pegaPermissao(Pessoa pessoa, ProcessoDocumento processoDocumento) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(ProcessoDocumentoVisibilidadeSegredo.class);
		criteria.add(Restrictions.eq("pessoa", pessoa));
		criteria.add(Restrictions.eq("processoDocumento", processoDocumento));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (ProcessoDocumentoVisibilidadeSegredo) criteria.uniqueResult();
	}
	
	
	/**
	 * marca os grids da tela com o valor informado
	 * 
	 * @param gridId id do grid a ser marcado
	 * @param valor valor que deve ser aplicado para todos os itens do grid
	 * @param useFullList indica que o metodo getfullList deve ser usado para obter os itens do grid, ao inves do getResultList
	 */
	private void marcaGridCom(String gridId, boolean valor, boolean useFullList) {				
		GridQuery gq = getComponent(gridId);
		
		if (gridId == null || gq == null) return;
		
		List lista;
		if (useFullList) lista = gq.getFullList();
		else lista = gq.getResultList();
		
		if (gridId.equals(this.processoParteSigiloGrid)) {			
			ProcessoParte parte;
			for (int i = 0; i < lista.size(); i++) {
				parte = (ProcessoParte) lista.get(i);
				parte.setCheckVisibilidade(valor);				
			}
			return;
		}
		
		if (gridId.equals(this.processoExpedienteSigiloGrid)) {
			ProcessoParteExpediente parteExpediente;
			for (int i = 0; i < lista.size(); i++) {
				parteExpediente = (ProcessoParteExpediente) lista.get(i);
				parteExpediente.setCheck(valor);
			}
			return;
		}
		
		if (gridId.equals(this.processoServidorSigiloGrid) || gridId.equals(this.processoServidorSigilo2GrauGrid)) {
			PessoaServidor servidor;
			for (int i = 0; i < lista.size(); i++) {
				servidor = (PessoaServidor) lista.get(i);
				servidor.setCheckVisibilidade(valor);				
			}
			return;
		}
	}
	
	
	private void iteraGridEMarcaPessoa(String gridId, Pessoa p) {
		GridQuery grid = getComponent(gridId); 
		
		if (gridId == null || grid == null) return;
		
		List listaItensGrid = grid.getFullList();
		
		if (gridId.equals(this.processoParteSigiloGrid)) {			
			ProcessoParte parte;
			for (int i = 0; i < listaItensGrid.size(); i++) {
				parte = (ProcessoParte) listaItensGrid.get(i);
				if (p.equals(parte.getPessoa()))
					parte.setCheckVisibilidade(true);				
			}
			return;
		}
		
		if (gridId.equals(this.processoExpedienteSigiloGrid)) {
			ProcessoParteExpediente parteExpediente;
			for (int i = 0; i < listaItensGrid.size(); i++) {
				parteExpediente = (ProcessoParteExpediente) listaItensGrid.get(i);
				if (p.equals(parteExpediente.getPessoaParte()))
					parteExpediente.setCheck(true);
			}
			return;
		}
		
		if (gridId.equals(this.processoServidorSigiloGrid) || gridId.equals(this.processoServidorSigilo2GrauGrid)) {
			PessoaServidor servidor;
			for (int i = 0; i < listaItensGrid.size(); i++) {
				servidor = (PessoaServidor) listaItensGrid.get(i);
				if (p.getIdPessoa().equals(servidor.getIdUsuario()))
					servidor.setCheckVisibilidade(true);				
			}
			return;
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<Pessoa> buscaPessoasComPermissao(ProcessoDocumento processoDocumento) {
		EntityManager em = EntityUtil.getEntityManager();
		
		String sql = "select o.pessoa from ProcessoDocumentoVisibilidadeSegredo o "
				+ "where o.processoDocumento = :documento";
		Query query = em.createQuery(sql);
		query.setParameter("documento", processoDocumento);		
		
		List<Pessoa> pessoas = query.getResultList();		
		return pessoas;
	}
	
	private void gravaItensGridServidor(GridQuery grid, List<ProcessoDocumento> documentos) {
		List itensGrid = grid.getFullList();
		for (int i = 0; i < itensGrid.size(); i++) {
			PessoaServidor servidor = (PessoaServidor) itensGrid.get(i);			
			
			for (ProcessoDocumento doc : documentos) {
				if (servidor.getCheckVisibilidade()) {
					getInstance().setPessoa(servidor.getPessoa());
					getInstance().setProcessoDocumento(doc);
					if (verificaDuplicidade(getInstance(), doc)) 
						persist(getInstance());
				} else{
					setInstance(pegaPermissao(servidor.getPessoa(), doc));
					remove(getInstance());
					grid.refresh();
				}				
				getEntityManager().flush();					
			}			
		}
	}	
	
	private void gravaItensGridProcessoParte(List<ProcessoDocumento> documentos) {
		GridQuery grid = getComponent(this.processoParteSigiloGrid);
		List itensGrid = grid.getFullList();
		
		for (int i = 0; i < itensGrid.size(); i++) {
			ProcessoParte parte = (ProcessoParte) grid.getFullList().get(i);			
			
			for (ProcessoDocumento doc : documentos) {
				if (parte.getCheckVisibilidade()) {
					getInstance().setPessoa(parte.getPessoa());
					getInstance().setProcuradoria(parte.getProcuradoria());
					getInstance().setProcessoDocumento(doc);
					if (verificaDuplicidade(getInstance(), doc))
						persist(getInstance());
				} else {
					setInstance(pegaPermissao(parte.getPessoa(), doc));
					remove(getInstance());
					grid.refresh();
				}
				getEntityManager().flush();
			}
		}
	}
	
	private void gravaItensGridExpediente(List<ProcessoDocumento> documentos) {
		GridQuery grid = getComponent(this.processoExpedienteSigiloGrid);
		List itensGrid = grid.getFullList();
		for (int i = 0; i < itensGrid.size(); i++) {
			ProcessoParteExpediente parteExpediente = (ProcessoParteExpediente) grid.getFullList().get(i);			
			
			for (ProcessoDocumento doc : documentos) {
				if (parteExpediente.getCheck()) {
					getInstance().setPessoa(parteExpediente.getPessoaParte());
					getInstance().setProcuradoria(parteExpediente.getProcuradoria());
					getInstance().setProcessoDocumento(doc);
					if (verificaDuplicidade(getInstance(), doc))
						persist(getInstance());						
				} else {
					setInstance(pegaPermissao(parteExpediente.getPessoaParte(), doc));
					remove(getInstance());
					grid.refresh();
				}
				getEntityManager().flush();
			}
		}
	}	
	
	public void setCheckBox(Boolean checkBox) {
		this.checkBox = checkBox;
	}

	public Boolean getCheckBox() {
		return checkBox;
	}

	public Boolean getCheckBoxParte() {
		return checkBoxParte;
	}

	public void setCheckBoxParte(Boolean checkBoxParte) {
		this.checkBoxParte = checkBoxParte;
	}
	
	public Boolean getCheckBoxExpediente() {
		return checkBoxExpediente;
	}

	public void setCheckBoxExpediente(Boolean checkBoxExpediente) {
		this.checkBoxExpediente = checkBoxExpediente;
	}
}
