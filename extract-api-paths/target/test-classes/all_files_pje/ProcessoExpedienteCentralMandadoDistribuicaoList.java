package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.richfaces.component.UIColumn;
import org.richfaces.component.UIDataTable;
import org.richfaces.model.Ordering;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;

@Name(ProcessoExpedienteCentralMandadoDistribuicaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoExpedienteCentralMandadoDistribuicaoList extends EntityList<ProcessoExpedienteCentralMandado> {

	public static final String NAME = "processoExpedienteCentralMandadoDistribuicaoList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.urgencia desc, o.processoExpediente.dtCriacao asc";

	@Override
	protected void addSearchFields() {
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("processoTrf", "o.processoExpediente.processoTrf");
		map.put("tipoProcessoDocumento", "o.processoExpediente.tipoProcessoDocumento");
		map.put("pessoaGrupoOficialJustica", "o.pessoaGrupoOficialJustica.pessoa");
		map.put("grupoOficialJustica", "o.pessoaGrupoOficialJustica.grupoOficialJustica");
		map.put("dtDistribuicaoExpediente", "o.dtDistribuicaoExpediente");
		map.put("dtCriacao", "o.processoExpediente.dtCriacao");
		return map;
	}

	@Override
	protected String getDefaultEjbql() {
		String q = 
				"select distinct pecm.idProcessoExpedienteCentralMandado from ProcessoExpedienteCentralMandado pecm, ProcessoDocumentoBinPessoaAssinatura assinatura " +
				" join	pecm.processoExpediente pe " +
				" join	pe.processoDocumentoExpedienteList pdel " +
				" join	pdel.processoDocumento pd " +
				" where assinatura.processoDocumentoBin = pd.processoDocumentoBin  " +
				" and pecm.processoExpediente.dtExclusao is null " +
				" and pecm.processoExpediente.inTemporario = false " +
				" and pecm.centralMandado.idCentralMandado in " +
					" ( " +
						" select cml.centralMandado.idCentralMandado from CentralMandadoLocalizacao cml " +
						" where cml.localizacao.idLocalizacao = #{usuarioLogadoLocalizacaoAtual.localizacaoFisica.idLocalizacao} " +
						" or cml.localizacao IN (#{localizacaoService.getTree(usuarioLogadoLocalizacaoAtual.localizacaoFisica)}) " +
					" ) " +
				" and pecm.dtDistribuicaoExpediente is null " +
				" and pecm.pessoaGrupoOficialJustica is null " +
				" and pecm.statusExpedienteCentral = ('A') " +
				" and pecm.processoExpedienteCentralMandadoAnterior is null";
		
		String hql = "SELECT o FROM ProcessoExpedienteCentralMandado o WHERE idProcessoExpedienteCentralMandado in ( " + q + ")";
		
				return hql;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}
	
	@Override
	public void setOrderedColumn(String order) {
		super.setOrderedColumn(order);
		// PJE-17344 Limpa a ordenação das colunas que utilizam o método "sortBy"
		UIDataTable table = (UIDataTable)FacesContext.getCurrentInstance().getViewRoot().findComponent("formDistribuirMandados:processoDistribuirExpedienteTable");
		for (UIComponent column : table.getChildren()) {  
			 ((UIColumn)column).setSortOrder(Ordering.UNSORTED);  
		}
	}
	
}
