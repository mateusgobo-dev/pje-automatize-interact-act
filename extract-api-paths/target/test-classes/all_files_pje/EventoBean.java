package br.com.infox.ibpm.component.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

/**
 * Bean de interface que representa uma abstração de uma linha de movimento do
 * Grid de movimentos selecionados. O EventoBean possui uma lista de
 * MovimentoBean, um para cada movimento a ser lançado efetivamente.
 * 
 * A quantidade de movimentos a serem lançados é determinada pela quantidade do
 * controle inputNumberSpinner.
 * 
 * Caso o movimento seja múltiplo, cada movimento adicionado será armazenado em
 * movimentoBeanList.
 * 
 * Caso o movimento seja não-múltiplo, o movimento selecionado pelo usuário será
 * armazenado em movimentoBeanList[0]
 * 
 * @author David, Kelly
 */
public class EventoBean implements Serializable {

	private static final long serialVersionUID = 4977600962991537730L;

	private Integer idEvento; //Mudado de Evento para Integer para permitir
	private String codEvento;
	private String descricaoMovimento;
	private String descricaoCompletaMovimento;
	private String descricaoCaminhoCompletoMovimento;
	private String glossario;
	private Integer quantidade;
	private Boolean valido = false;
	private Boolean multiplo;
	private Boolean excluir;
	private Integer idProcessoDocumento;
	private Integer idTipoProcessoDocumento;
	private Long idJbpmTask;
	private Boolean temComplemento;
	private Boolean selected;
	// Lista de complementos para os complementos múltiplos
	private List<MovimentoBean> movimentoBeanList = new ArrayList<MovimentoBean>();

	// TODO método que irá gravar o movimento na tabela temporária
    
	/**
	 * Valida a gravação de complementos e salvará os complementos numa tabela
	 * temporária(TODO)
	 * 
	 * @return null (para manter na mesma página, não redirect)
	 * 
	 * @author David, Kelly
	 * 
	 */
	public String gravarMovimento() {
		Boolean error = false;
		Boolean test = true;

		for (MovimentoBean movimentoBean : this.getMovimentoBeanList()) {
			test = true;
			for (ComplementoBean complementoBean : movimentoBean.getComplementoBeanList()) {
				// Verifica se existem movimentos sem complementos;
				test = test && complementoBean.semComplementos();
				// Apenas complementos livres precisam de validação
				if (complementoBean.getTipoComplementoEnum().equals(ComplementoBean.TipoComplementoEnum.LIVRE)) {
					String validacao = complementoBean.getValidacao();
					String mensagemErro = complementoBean.getMensagemErro();
					try {
						Pattern pattern = Pattern.compile(validacao);
						for (ValorComplementoBean valorComplementoBean : complementoBean.getValorComplementoBeanList()) {
							Matcher matcher = pattern.matcher(valorComplementoBean.getValor());
							if (!matcher.find()) {
								if (mensagemErro == null) {
									FacesMessages.instance().add(Severity.ERROR, "Complemento inválido!");
								} else {
									/*
									 * [PJEII-4354] Rodrigo S. Menezes: Mensagem de erro na validação dos complementos
									 * não estavam sendo rederizadas na view. Alterada a linha abaixo para melhor formatação
									 * da mensagem de erro.
									 */
									FacesMessages.instance().add(Severity.ERROR, mensagemErro.concat(": " + valorComplementoBean.getValor()));
								}
								this.setValido(false);
								error = true;
							}
						}
					} catch (Exception e) {
						// Erro na expressão REGEX -> swallow
					}
				}
				if (test) break;
			}
			if (test) {
				error=true;
				break;
			}			
		}

		if (!error) {
			this.setValido(true);
			FacesMessages.instance().add(Severity.INFO, "Complementos incluídos com sucesso!");
		} else 
		if (error) {
			this.setValido(false);
			FacesMessages.instance().add(Severity.ERROR, "Faltam complementos a serem adicionados!!");
		}
		return null;
	}

	public Boolean getTemComplemento() {
		return temComplemento;
	}

	public void setTemComplemento(Boolean temComplemento) {
		this.temComplemento = temComplemento;
	}

	public Boolean getValido() {
		return valido;
	}

	public void setValido(Boolean valido) {
		this.valido = valido;
	}

	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public List<MovimentoBean> getMovimentoBeanList() {
		return movimentoBeanList;
	}

	public void setMovimentoBeanList(List<MovimentoBean> movimentoBeanList) {
		this.movimentoBeanList = movimentoBeanList;
	}

	public Integer getIdEvento() {
		return idEvento;
	}

	public void setIdEvento(Integer idEvento) {
		this.idEvento = idEvento;
	}

	public Boolean getMultiplo() {
		return multiplo;
	}

	public void setMultiplo(Boolean multiplo) {
		this.multiplo = multiplo;
	}

	public Boolean getExcluir() {
		return excluir;
	}

	public void setExcluir(Boolean excluir) {
		this.excluir = excluir;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public Integer getIdProcessoDocumento() {
		return idProcessoDocumento;
	}

	public void setIdProcessoDocumento(Integer idProcessoDocumento) {
		this.idProcessoDocumento = idProcessoDocumento;
	}

	public Integer getIdTipoProcessoDocumento() {
		return idTipoProcessoDocumento;
	}

	public void setIdTipoProcessoDocumento(Integer idTipoProcessoDocumento) {
		this.idTipoProcessoDocumento = idTipoProcessoDocumento;
	}

	public Long getIdJbpmTask() {
		return idJbpmTask;
	}

	public void setIdJbpmTask(Long idJbpmTask) {
		this.idJbpmTask = idJbpmTask;
	}

	public Boolean getMovimentoCompleto() {
		return Boolean.FALSE;
	}
	
	public String getCodEvento() {
		return codEvento;
	}

	public void setCodEvento(String codEvento) {
		this.codEvento = codEvento;
	}

	public String getDescricaoMovimento() {
		return descricaoMovimento;
	}

	public void setDescricaoMovimento(String descricaoMovimento) {
		this.descricaoMovimento = descricaoMovimento;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idEvento == null) ? 0 : idEvento.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EventoBean))
			return false;
		EventoBean other = (EventoBean) obj;
		if (this.getIdEvento() == null){
			if (other.getIdEvento() != null)
				return false;
		}
		else if (!this.getIdEvento().equals(other.getIdEvento()))
			return false;
		return true;
	}
	
	/**
	 * [PJEII-2393]
	 * Verifica se o EventoBean possui algum complemento dinâmico.
	 * @return true, caso algum de seus complementos seja dinâmico.
	 */
	public boolean possuiComplementoDinamico() {
		if (movimentoBeanList == null) {
			return false;
		}
		
		if (movimentoBeanList.size() == 0) {
			return false;
		}
		
		return movimentoBeanList.get(0).possuiComplementoDinamico();
	}

	public String getDescricaoCompletaMovimento() {
		return descricaoCompletaMovimento;
	}

	public void setDescricaoCompletaMovimento(String descricaoCompletaMovimento) {
		this.descricaoCompletaMovimento = descricaoCompletaMovimento;
	}

	public String getDescricaoCaminhoCompletoMovimento() {
		return descricaoCaminhoCompletoMovimento;
	}

	public void setDescricaoCaminhoCompletoMovimento(String descricaoCaminhoCompletoMovimento) {
		this.descricaoCaminhoCompletoMovimento = descricaoCaminhoCompletoMovimento;
	}

	public String getGlossario() {
		return glossario;
	}

	public void setGlossario(String glossario) {
		this.glossario = glossario;
	}
}