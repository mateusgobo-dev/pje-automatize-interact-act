package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;

import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

/**
 * Classe controladora do Frame "/Processo/Fluxo/documentosProcessuais.xhtml"
 * utilizado para visualização dos documentos "internos" (em xhtml, exceto
 * minutas) vinculados ao processo.
 */
@Name(DocumentosProcessuaisAction.NAME)
@Scope(ScopeType.PAGE)
public class DocumentosProcessuaisAction implements Serializable {

	private static final long serialVersionUID = -8053678961592072568L;

	@In
	private TramitacaoProcessualService tramitacaoProcessualService;

	public static final String NAME = "documentosProcessuaisAction";

	private boolean ocultarAnexos = Boolean.FALSE;

	private boolean ordemJuntada = Boolean.FALSE;

	private List<ProcessoDocumento> processoDocumentoList;

	@Create
	public void init() {
		ocultarAnexos = recuperarVariavelOcultarAnexos();
		ordemJuntada = recuperarVariavelOrdemJuntada();
		carregarDocumentosConformeOrdemJuntada();
	}

	/**
	 * Método responsável por retornar o valor boleano da variavel de fluxo
	 * "pje:fluxo:documentosProcessuais:ocultarAnexos". Caso a variável não seja
	 * definida no fluxo, o método retornará o valor <code>false</code>
	 * 
	 * Esta variável de fluxo é responsável pela apresentação ou não do grid de
	 * documentos anexos no frame "/Processo/Fluxo/documentosProcessuais.xhtml" (visualizador de
	 * documentos)
	 * 
	 * @return <code>true</code> ou <code>false</code> dependendo da definição
	 *         da variavel de fluxo
	 */
	private Boolean recuperarVariavelOcultarAnexos() {
		Object result = tramitacaoProcessualService.recuperaVariavelTarefa("pje:fluxo:documentosProcessuais:ocultarAnexos");
		return (result != null) ? (Boolean) result : Boolean.FALSE;
	}

	/**
	 * Método responsável por retornar o valor boleano da variavel de fluxo
	 * "pje:fluxo:documentosProcessuais:ordemJuntada". Caso a variável não seja
	 * definida no fluxo, o método retornará o valor <code>false</code>
	 * 
	 * @return <code>true</code> ou <code>false</code> dependendo da definição
	 *         da variavel de fluxo
	 */
	private Boolean recuperarVariavelOrdemJuntada() {
		Object result = tramitacaoProcessualService.recuperaVariavelTarefa("pje:fluxo:documentosProcessuais:ordemJuntada");
		return (result != null) ? (Boolean) result : Boolean.FALSE;
	}

	/**
	 * Método responsável por carregar a listagem de documentos conforme o valor
	 * da variável "ordemJuntada". Caso o valor seja <code>true</code>, os
	 * documentos serão listados na ordem de juntada do processo, caso contrario
	 * virão na ordem inversa a ordem de juntada (padrão do sistema). A variável
	 * "ordemJuntada" pode ser definida via EL no fluxo usando-se a variável
	 * "pje:fluxo:documentosProcessuais:ocultarAnexos" <br>
	 * EX:<br>
	 * <code> #{tramitacaoProcessualService.gravaVariavelTarefa('pje:fluxo:documentosProcessuais:ocultarAnexos',true)}</code>
	 */
	private void carregarDocumentosConformeOrdemJuntada() {
		if (isOrdemJuntada()) {
			processoDocumentoList = getProcessoDocumentoGridListOrdemJuntada();
		} else {
			processoDocumentoList = getProcessoDocumentoGridListOrdemInversaJuntada();
		}
	}

	/**
	 * Método getter de acesso a lista de documentos associados ao processo.
	 * 
	 * @return <code>List &lt;ProcessoDocumento&gt; </code> na ordem definida
	 *         pela variavel de fluxo
	 *         "pje:fluxo:documentosProcessuais:ordemJuntada" , se for
	 *         configurada com valor <code>true</code> os documentos virão na
	 *         ordem de juntada, e na ordem inversa da juntada caso contrario ou
	 *         se a variavel não for definida (comportamento default).
	 */
	public List<ProcessoDocumento> getProcessoTrfDocumentoGridList() {
		if (processoDocumentoList == null) {
			carregarDocumentosConformeOrdemJuntada();
		}
		return processoDocumentoList;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getProcessoTrfDocumentoAnexoGridList(){
		GridQuery grid = ComponentUtil.getComponent("documentosProcessuaisAnexosGrid");
		grid.setOrder("o.dataInclusao desc");
		return grid.getResultList();
	}

	/**
	 * Método que recupera a lista de documentos associados ao processo na ordem
	 * cronológica de juntada
	 * 
	 * @return <code>List &lt;ProcessoDocumento&gt; </code>
	 */
	@SuppressWarnings("unchecked")
	private List<ProcessoDocumento> getProcessoDocumentoGridListOrdemJuntada() {
		GridQuery grid = ComponentUtil.getComponent("processoTrfDocumentoGrid");
		List<ValueExpression<Object>> conditions = grid.getRestrictions();
		conditions.add(Expressions.instance().createValueExpression("o.processoDocumentoBin.binario = #{false}"));
		conditions.add(Expressions.instance().createValueExpression("o.dataJuntada is not null and true = #{true}"));
		conditions.add(Expressions.instance().createValueExpression("o.dataExclusao is null and true = #{true}"));
		grid.setRestrictions(conditions);
		grid.setOrder("o.processoDocumentoBin.dataAssinatura asc, o.dataInclusao asc");
		return grid.getResultList();
	}

	/**
	 * Método que recupera a lista de documentos associados ao processo na ordem
	 * inversa de juntada
	 * 
	 * @return <code>List &lt;ProcessoDocumento&gt; </code>
	 */
	@SuppressWarnings("unchecked")
	private List<ProcessoDocumento> getProcessoDocumentoGridListOrdemInversaJuntada() {
		GridQuery grid = ComponentUtil.getComponent("processoTrfDocumentoGrid");
		List<ValueExpression<Object>> conditions = grid.getRestrictions();
		conditions.add(Expressions.instance().createValueExpression("o.processoDocumentoBin.binario = #{false}"));
		conditions.add(Expressions.instance().createValueExpression("o.dataJuntada is not null and true = #{true}"));
		conditions.add(Expressions.instance().createValueExpression("o.dataExclusao is null and true = #{true}"));
		grid.setRestrictions(conditions);
		grid.setOrder("o.processoDocumentoBin.dataAssinatura desc, o.dataInclusao desc");
		return grid.getResultList();
	}

	/**
	 * Método responsável por forçar a inversão da ordem da lista de documentos
	 * contidos na variavel "processoDocumentoList".
	 */
	public void inverterOrdemApresentacaoDocumentos() {
		setOrdemJuntada(!isOrdemJuntada());
		carregarDocumentosConformeOrdemJuntada();
	}

	public boolean isOcultarAnexos() {
		return ocultarAnexos;
	}

	public void setOcultarAnexos(boolean ocultarAnexos) {
		this.ocultarAnexos = ocultarAnexos;
	}

	public boolean isOrdemJuntada() {
		return ordemJuntada;
	}

	public void setOrdemJuntada(boolean ordemJuntada) {
		this.ordemJuntada = ordemJuntada;
	}
	
}