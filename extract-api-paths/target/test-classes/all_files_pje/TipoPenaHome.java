package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.TipoPena;
import br.jus.pje.nucleo.enums.GeneroPenaEnum;

@Name("tipoPenaHome")
@BypassInterceptors
public class TipoPenaHome extends AbstractTipoPenaHome<TipoPena> {

	/*
	 * private static final long serialVersionUID = 1L; private static final int
	 * TIPO_PENA_MULTA_INDEX = 1; private static final int
	 * TIPO_PENA_PRIVATIVA_INDEX = 2; private static final int
	 * TIPO_PENA_RESTRITIVA_INDEX = 3;
	 */

	private static final long serialVersionUID = 1L;

	// private Integer tipoPenaIndex;
	private Integer tipoPenaSearchIndex;

	public TipoPenaHome() {
		inicializar();
	}

	public void inicializar() {
		TipoPena tipoPena = new TipoPena();

		tipoPena.setInTempoAno(false);
		tipoPena.setInTempoMes(false);
		tipoPena.setInTempoDia(false);
		tipoPena.setInTempoHoras(false);
		tipoPena.setInValor(false);
		tipoPena.setInQuantidadeDiasMulta(false);
		tipoPena.setInTipoBem(false);
		tipoPena.setInDescricaoBem(false);
		tipoPena.setInDescricaoLocal(false);
		tipoPena.setInPenaRestritivaDireito(false);
		tipoPena.setInPenaPrivativaLiberdade(false);
		tipoPena.setInMulta(false);
		tipoPena.setAtivo(Boolean.TRUE);

		setInstance(tipoPena);
	}

	@Override
	public void setId(Object id) {
		if (id == null) {
			inicializar();
		}

		super.setId(id);
	}

	@Override
	public void setTab(String tab) {
		if (super.getId() == null) {
			inicializar();
		}
		super.setTab(tab);
	}

	/*
	 *  ************ AUXILIAR PARA TELA DE LISTA (GRID), FAZER PESQUISA
	 * ***************************************************
	 */

	private Integer idTipoPenaClassePesquisa;

	public Integer getIdTipoPenaClassePesquisa() {
		return idTipoPenaClassePesquisa;
	}

	public void setIdTipoPenaClassePesquisa(Integer idTipoPenaClassePesquisa) {
		this.idTipoPenaClassePesquisa = idTipoPenaClassePesquisa;
	}

	/*
	 *  ************ CRIAÇÃO DE OBJETO
	 * *******************************************
	 * ****************************************
	 */

	@Override
	public void newInstance() {
		refreshGrid("tipoPenaGrid");
		super.newInstance();
		getInstance().setInDescricaoBem(false);
		getInstance().setInDescricaoLocal(false);
		getInstance().setInMulta(false);
		getInstance().setInPenaPrivativaLiberdade(false);
		getInstance().setInPenaRestritivaDireito(false);
		getInstance().setInQuantidadeDiasMulta(false);
		getInstance().setInTempoAno(false);
		getInstance().setInTempoDia(false);
		getInstance().setInTempoHoras(false);
		getInstance().setInTempoMes(false);
		getInstance().setInTipoBem(false);
		getInstance().setInValor(false);
		getInstance().setAtivo(true);

	}

	public static TipoPenaHome instance() {
		return ComponentUtil.getComponent("tipoPenaHome");
	}

	/*
	 *  ************ PERSISTÊNCIA
	 * ************************************************
	 * **************************************
	 */

	@Override
	public boolean beforePersistOrUpdate() {

		if (verificaDescricao()) {

			String mensagem = Messages.instance().get("tipoPena.verificarDescricao");
			FacesMessages.instance().add(StatusMessage.Severity.INFO, mensagem);
			return false;

		} else if (getInstance().getDsSigla() != null && !getInstance().getDsSigla().isEmpty()) {

			if (verificaSigla()) {

				String mensagem = Messages.instance().get("tipoPena.verificarSigla");
				FacesMessages.instance().add(StatusMessage.Severity.INFO, mensagem);
				return false;
			}
		}

		/*
		 * switch (tipoPenaIndex){ case TIPO_PENA_MULTA_INDEX:
		 * getInstance().setInMulta(true);
		 * getInstance().setInPenaPrivativaLiberdade(false);
		 * getInstance().setInPenaRestritivaDireito(false); break; case
		 * TIPO_PENA_PRIVATIVA_INDEX: getInstance().setInMulta(false);
		 * getInstance().setInPenaPrivativaLiberdade(true);
		 * getInstance().setInPenaRestritivaDireito(false); break; case
		 * TIPO_PENA_RESTRITIVA_INDEX: getInstance().setInMulta(false);
		 * getInstance().setInPenaPrivativaLiberdade(false);
		 * getInstance().setInPenaRestritivaDireito(true); break; }
		 */

		/*
		 * if(tipoPenaIndex != null){ if(tipoPenaIndex ==
		 * GeneroPenaEnum.MULTA.getValue()){ getInstance().setInMulta(true);
		 * getInstance().setInPenaPrivativaLiberdade(false);
		 * getInstance().setInPenaRestritivaDireito(false); }else
		 * if(tipoPenaIndex == GeneroPenaEnum.PRIVATIVA_LIBERDADE.getValue()){
		 * getInstance().setInMulta(false);
		 * getInstance().setInPenaPrivativaLiberdade(true);
		 * getInstance().setInPenaRestritivaDireito(false); }else
		 * if(tipoPenaIndex == GeneroPenaEnum.RESTRITIVA_DIREITO.getValue()){
		 * getInstance().setInMulta(false);
		 * getInstance().setInPenaPrivativaLiberdade(false);
		 * getInstance().setInPenaRestritivaDireito(true); } }
		 */

		return super.beforePersistOrUpdate();
	}

	/**
	 * Através de uma consulta no banco verifica a existência de um Tipo de Pena
	 * com a mesma Descrição inserida
	 * 
	 * @return boolean
	 */
	private boolean verificaDescricao() {
		StringBuilder sb = new StringBuilder();

		sb.append(" select lower(dsTipoPena) from TipoPena tp ");
		sb.append(" where lower(to_ascii(tp.dsTipoPena)) like ");
		sb.append(" lower(concat('%', TO_ASCII('" + getInstance().getDsTipoPena().trim() + "'), '%')) ");
		sb.append(" and tp.generoPena = ? ");
		sb.append(" and tp.ativo = true ");
		if (getInstance().getIdTipoPena() != null) {
			sb.append(" and    tp.idTipoPena <> " + getInstance().getIdTipoPena());
		}

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter(1, getInstance().getGeneroPena());

		if (q.getResultList().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Através de uma consulta no banco verifica a existência de um Tipo de Pena
	 * com a mesma Sigla
	 * 
	 * @return boolean
	 */
	private boolean verificaSigla() {
		StringBuilder sb = new StringBuilder();

		sb.append(" select lower(dsSigla) from TipoPena tp ");
		sb.append(" where  tp.dsSigla = '");
		sb.append(getInstance().getDsSigla().trim().toLowerCase());
		sb.append("'");
		sb.append(" and    tp.idTipoPena <> " + getInstance().getIdTipoPena());

		Query q = getEntityManager().createQuery(sb.toString());

		if (q.getResultList().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	protected boolean falhouVerificarClassificacaoDoTipoPena() {
		TipoPena tp = getInstance();
		boolean apenasUmVerdadeiro = tp.getInPenaPrivativaLiberdade() ^ tp.getInPenaRestritivaDireito()
				^ tp.getInMulta();
		return !apenasUmVerdadeiro;
	}

	@Override
	public String update() {
		String ret = null;
		try {
			ret = super.update();
		} catch (Exception e) {
			System.out.println("Erro de restrição: possivelmente um campo foi duplicado.");
		}
		return ret;
	}

	@Override
	public String remove(TipoPena obj) {
		obj.setAtivo(Boolean.FALSE);
		setInstance(obj);
		super.update();
		newInstance();
		refreshGrid("tipoPenaGrid");
		return "updated";
	}

	/*
	 *  ************ CONSULTA A DADOS ESPECÍFICA
	 * *************************************************************************
	 */

	@SuppressWarnings("unchecked")
	public List<SelectItem> getListaTipoPena() {

		StringBuilder sb = new StringBuilder();
		sb.append(" select tp from TipoPena tp ");
		sb.append(" where tp.ativo = true ");
		if (isInstanciaAtualPreenchida()) {
			Integer id = getInstance().getIdTipoPena();
			sb.append("   and tp.idTipoPena <> ").append(id);
		}

		Query q = getEntityManager().createQuery(sb.toString());

		List<SelectItem> listaResultado = new ArrayList<SelectItem>();
		listaResultado.add(new SelectItem(null, "Selecione"));

		List<TipoPena> listTipoPena = q.getResultList();

		for (TipoPena tipoPena : listTipoPena) {
			long id = tipoPena.getIdTipoPena();
			String descricao = tipoPena.getDsTipoPena();
			SelectItem selectItem = new SelectItem(id, id + " - " + descricao);
			listaResultado.add(selectItem);
		}

		return listaResultado;
	}

	private boolean isInstanciaAtualPreenchida() {
		Integer id = getInstance().getIdTipoPena();
		return id != null && id.intValue() != 0;
	}

	/*
	 * public void setTipoPenaIndex(Integer tipoPenaIndex) { this.tipoPenaIndex
	 * = tipoPenaIndex; }
	 * 
	 * public Integer getTipoPenaIndex() { return tipoPenaIndex; }
	 */

	public Integer getTipoPenaSearchIndex() {
		return tipoPenaSearchIndex;
	}

	public void setTipoPenaSearchIndex(Integer tipoPenaSearchIndex) {
		this.tipoPenaSearchIndex = tipoPenaSearchIndex;

		/*
		 * switch (tipoPenaSearchIndex){ case TIPO_PENA_MULTA_INDEX:
		 * tipoPenaSearch.setInMulta(true);
		 * tipoPenaSearch.setInPenaPrivativaLiberdade(false);
		 * tipoPenaSearch.setInPenaRestritivaDireito(false); break; case
		 * TIPO_PENA_PRIVATIVA_INDEX: tipoPenaSearch.setInMulta(false);
		 * tipoPenaSearch.setInPenaPrivativaLiberdade(true);
		 * tipoPenaSearch.setInPenaRestritivaDireito(false); break; case
		 * TIPO_PENA_RESTRITIVA_INDEX: tipoPenaSearch.setInMulta(false);
		 * tipoPenaSearch.setInPenaPrivativaLiberdade(false);
		 * tipoPenaSearch.setInPenaRestritivaDireito(true); break; default:
		 * tipoPenaSearch.setInMulta(null);
		 * tipoPenaSearch.setInPenaPrivativaLiberdade(null);
		 * tipoPenaSearch.setInPenaRestritivaDireito(null); break; }
		 */

		/*
		 * if(tipoPenaSearchIndex != null){ if(tipoPenaSearchIndex ==
		 * GeneroPenaEnum.MULTA.getValue()){ tipoPenaSearch.setInMulta(true);
		 * tipoPenaSearch.setInPenaPrivativaLiberdade(false);
		 * tipoPenaSearch.setInPenaRestritivaDireito(false); }else
		 * if(tipoPenaSearchIndex ==
		 * GeneroPenaEnum.PRIVATIVA_LIBERDADE.getValue()){
		 * tipoPenaSearch.setInMulta(false);
		 * tipoPenaSearch.setInPenaPrivativaLiberdade(true);
		 * tipoPenaSearch.setInPenaRestritivaDireito(false); }else
		 * if(tipoPenaSearchIndex ==
		 * GeneroPenaEnum.RESTRITIVA_DIREITO.getValue()){
		 * tipoPenaSearch.setInMulta(false);
		 * tipoPenaSearch.setInPenaPrivativaLiberdade(false);
		 * tipoPenaSearch.setInPenaRestritivaDireito(true); }else{
		 * tipoPenaSearch.setInMulta(null);
		 * tipoPenaSearch.setInPenaPrivativaLiberdade(null);
		 * tipoPenaSearch.setInPenaRestritivaDireito(null); } }
		 */
	}

	public List<SelectItem> getGeneros() {
		List<SelectItem> lista = new ArrayList<SelectItem>();
		for (GeneroPenaEnum aux : GeneroPenaEnum.values()) {
			SelectItem item = new SelectItem(aux, aux.getLabel());
			lista.add(item);
		}

		return lista;
	}
}
