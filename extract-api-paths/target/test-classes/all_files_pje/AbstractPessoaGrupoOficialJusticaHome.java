package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaGrupoOficialJustica;

public abstract class AbstractPessoaGrupoOficialJusticaHome<T> extends AbstractHome<PessoaGrupoOficialJustica>{

	private static final long serialVersionUID = 1L;

	public void setPessoaGrupoOficialJusticaIdPessoaGrupoOficialJustica(Integer id){
		setId(id);
	}

	public Integer getPessoaGrupoOficialJusticaIdPessoaGrupoOficialJustica(){
		return (Integer) getId();
	}

	@Override
	protected PessoaGrupoOficialJustica createInstance(){
		PessoaGrupoOficialJustica pessoaGrupoOficialJustica = new PessoaGrupoOficialJustica();

		PessoaHome pessoaHome = PessoaHome.instance();
		if (pessoaHome != null){
			pessoaGrupoOficialJustica.setPessoa((PessoaFisica) pessoaHome.getDefinedInstance());
		}

		GrupoOficialJusticaHome grupoOficialJusticaHome = GrupoOficialJusticaHome.instance();
		if (grupoOficialJusticaHome != null){
			pessoaGrupoOficialJustica.setGrupoOficialJustica(grupoOficialJusticaHome.getDefinedInstance());
		}
		return pessoaGrupoOficialJustica;
	}

	@Override
	public String remove(){
		GrupoOficialJusticaHome grupoOficialJustica = GrupoOficialJusticaHome.instance();
		if (grupoOficialJustica != null){
			grupoOficialJustica.getInstance().getPessoaGrupoOficialJusticaList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(PessoaGrupoOficialJustica obj){
		setInstance(obj);
		getInstance().setAtivo(Boolean.FALSE);
		String ret = super.update();
		newInstance();
		refreshGrid("processoExpedienteDiligenciaGrid");
		return ret;
	}
	

	@Override
	public String persist(){
		String action = super.persist();
		// if (action != null) {
		// newInstance();
		// }
		newInstance();
		return action;
	}

}