package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaPeritoEspecialidade;

public abstract class AbstractPessoaPeritoEspecialidadeHome<T> extends AbstractHome<PessoaPeritoEspecialidade> {

	private static final long serialVersionUID = 1L;

	public void setPessoaPeritoEspecialidadeIdPessoaPeritoEspecialidade(Integer id) {
		setId(id);
	}

	public Integer getPessoaPeritoEspecialidadeIdPessoaPeritoEspecialidade() {
		return (Integer) getId();
	}
	/*
	 * AJUSTAR
	 * 
	 * @Override protected PessoaPeritoEspecialidade createInstance() {
	 * PessoaGrupoOficialJustica pessoaGrupoOficialJustica = new
	 * PessoaGrupoOficialJustica();
	 * 
	 * PessoaHome pessoaHome = PessoaHome.instance(); if (pessoaHome != null) {
	 * pessoaGrupoOficialJustica.setPessoa(pessoaHome.getDefinedInstance()); }
	 * 
	 * GrupoOficialJusticaHome grupoOficialJusticaHome =
	 * GrupoOficialJusticaHome.instance(); if (grupoOficialJusticaHome != null)
	 * {
	 * pessoaGrupoOficialJustica.setGrupoOficialJustica(grupoOficialJusticaHome
	 * .getDefinedInstance()); } return pessoaGrupoOficialJustica; }
	 * 
	 * @Override public String remove() { PessoaHome pessoa =
	 * PessoaHome.instance(); if (pessoa != null) {
	 * pessoa.getInstance().getPessoaGrupoOficialJusticaList().remove(instance);
	 * } GrupoOficialJusticaHome grupoOficialJustica =
	 * GrupoOficialJusticaHome.instance(); if (grupoOficialJustica != null) {
	 * grupoOficialJustica
	 * .getInstance().getPessoaGrupoOficialJusticaList().remove(instance); }
	 * return super.remove(); }
	 */

}