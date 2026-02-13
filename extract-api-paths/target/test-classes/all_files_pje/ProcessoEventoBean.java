package br.jus.cnj.pje.visao.beans;

import br.jus.pje.nucleo.entidades.ProcessoEvento;

/**
 * Componente que encapsula os atributos da tela de Ajuste de Movimentos.
 * 
 * @since 1.4.2
 * @category PJE-JT
 * @created 2011-08-25
 * @author Emmanuel S. Magalhães, Guilherme Bispo
 */
public class ProcessoEventoBean {

	private Boolean selected = Boolean.FALSE;
	private ProcessoEvento processoEvento;
	private Boolean renderCheckBox = Boolean.FALSE;
	private Boolean renderCheckboxVisibilidadeComplemento = Boolean.FALSE;
	private Boolean visibilidadeAlterada = Boolean.FALSE;
	private Boolean visibilidadeExterna = Boolean.FALSE;

	public Boolean getRenderCheckboxVisibilidadeComplemento() {
		return renderCheckboxVisibilidadeComplemento;
	}

	public void setRenderCheckboxVisibilidadeComplemento(Boolean renderCheckboxVisibilidadeComplemento) {
		this.renderCheckboxVisibilidadeComplemento = renderCheckboxVisibilidadeComplemento;
	}

	public Boolean getVisibilidadeExterna() {
		return visibilidadeExterna;
	}

	public void setVisibilidadeExterna(Boolean visibilidadeExterna) {
		this.visibilidadeExterna = visibilidadeExterna;
	}

	public Boolean getVisibilidadeAlterada() {
		return visibilidadeAlterada;
	}

	public void setVisibilidadeAlterada(Boolean visibilidadeAlterada) {
		this.visibilidadeAlterada = visibilidadeAlterada;
	}

	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public ProcessoEvento getProcessoEvento() {
		return processoEvento;
	}

	public void setProcessoEvento(ProcessoEvento processoEvento) {
		this.processoEvento = processoEvento;
	}

	public void setRenderCheckBox(Boolean renderCheckBox) {
		this.renderCheckBox = renderCheckBox;
	}

	public Boolean getRenderCheckBox() {
		return renderCheckBox;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processoEvento == null) ? 0 : processoEvento.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ProcessoEventoBean))
			return false;
		ProcessoEventoBean other = (ProcessoEventoBean) obj;
		if (processoEvento == null) {
			if (other.processoEvento != null)
				return false;
		} else if (!processoEvento.equals(other.processoEvento))
			return false;
		return true;
	}

}
