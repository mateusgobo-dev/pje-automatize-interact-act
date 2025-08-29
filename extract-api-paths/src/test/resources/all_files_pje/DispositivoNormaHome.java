package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.cliente.component.tree.AssuntoAtoInfracionalTreeHandler;
import br.com.infox.cliente.component.tree.AssuntoTrfCriminalTreeHandler;
import br.com.infox.cliente.component.tree.DispositivoNormaTreeHandler;
import br.com.infox.cliente.component.tree.TipoPenaTreeHandler;
import br.com.infox.pje.list.DispositivoNormaList;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.DispositivoNorma;
import br.jus.pje.nucleo.entidades.MultaPenaPrivativa;
import br.jus.pje.nucleo.entidades.TipoDispositivoNorma;
import br.jus.pje.nucleo.entidades.TipoPena;
import br.jus.pje.nucleo.enums.SimboloDispositivoNormaEnum;
import br.jus.pje.nucleo.enums.UsoDispositivoEnum;

@Name("dispositivoNormaHome")
@BypassInterceptors
public class DispositivoNormaHome extends
		AbstractDispositivoNormaHome<DispositivoNorma> {

	private static final String TIPO_DISPOSITIVO_PARAGRAFO_UNICO = "Parágrafo Único";
	private static final String TIPO_DISPOSITIVO_PARAGRADO_SIMBOLO = "§";
	private static final String TIPO_DISPOSITIVO_PARAGRAFO = "Parágrafo";
	private static final String TIPO_DISPOSITIVO_ARTIGO_SIMBOLO = "Art.";
	private static final String TIPO_DISPOSITIVO_ARTIGO = "Artigo";
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private Boolean ehPrevisaoTipoPenal;

	/*
	 * *****************************************************************
	 * PERSISTÊNCIA
	 * ******************************************************************
	 */

	@Override
	public String inactive(DispositivoNorma dn) {
		Query query = getEntityManager()
				.createQuery(
						"from DispositivoNorma dn where dn.ativo = true and dn.dispositivoNormaPai.idDispositivoNorma = ?");
		query.setParameter(1, dn.getIdDispositivoNorma());
		Integer quantidadeFilhos = query.getResultList().size();
		String result = "";
		if (quantidadeFilhos == 0) {
			dn.setAtivo(Boolean.FALSE);
			result = super.inactive(dn);
		} else {
			result = "";
			imprimirMensagem("Não é permitido excluir este dispositivo, pois existem dispositivos dependentes.");
		}

		return result;
	}

	/*
	 * ************ VALIDAÇÕES - REGRAS DE NEGÓCIO
	 * ********************************************
	 */

	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setDispositivoNormaPai(dispositivoNormaTree);
		getInstance().setTipoPena(tipoPenaTree);
		getInstance().setAssuntoTrf(getAssuntoTrfCriminalTree());
		getInstance().setAssuntoAtoInfracional(getAssuntoAtoInfracionalTree());
		NormaPenalHome normaPenalHome;
		normaPenalHome = (NormaPenalHome) Component
				.getInstance("normaPenalHome");
		instance.setNormaPenal(normaPenalHome.getInstance());

		// RN120
		// if(getInstance().getInPrevisaoTipoPenal()){
		if (getInstance().getUsoDispositivo() == UsoDispositivoEnum.TP) {
			if (!getInstance().getInPrevisaoPenaMulta()
					&& !getInstance().getInPrevisaoPenaPrivativa()
					&& !getInstance().getInPrevisaoPenaRestritiva()) {
				String mensagem = Messages.instance().get(
						"dispositivoNorma.marcacaoInvalida");
				FacesMessages.instance().add(StatusMessage.Severity.ERROR,
						mensagem);
				return false;
			}
		}

		if (validarNumeroDeMesesDias() == false) {
			String mensagem = Messages.instance().get(
					"dispositivoNorma.validarNumeroDeMesesDias");
			FacesMessages.instance()
					.add(StatusMessage.Severity.ERROR, mensagem);
			return false;
		}

		if (verificaArtigoComNormaPenal() == false) {
			String mensagem = Messages.instance().get(
					"dispositivoNorma.verificaArtigoComNormaPenal");
			FacesMessages.instance()
					.add(StatusMessage.Severity.ERROR, mensagem);
			return false;
		}

		if (verificaData() == false) {
			String mensagem = Messages.instance().get(
					"dispositivoNorma.verificaData");
			FacesMessages.instance()
					.add(StatusMessage.Severity.ERROR, mensagem);
			return false;
		}
		if (getInstance().getDtHediondo() != null
				&& getInstance().getDtHediondo().before(
						getInstance().getDtInicioVigencia())) {
			String mensagem = "A Data no Qual o Crime Passou a Ser Considerado Hediondo "
					+ "não pode ser inferior a Data Inicial da Vigência!";
			FacesMessages.instance()
					.add(StatusMessage.Severity.ERROR, mensagem);
			return false;
		}
		if (falhaSeJaExisteDispositivoSemTermino()) {
			FacesMessages
					.instance()
					.add(StatusMessage.Severity.ERROR,
							getInstance().getTipoDispositivoNorma()
									+ " possui um cadastro com data de término da vigência em aberto."
									+ " Por favor, informe a data de término do registro antes de cadastrar um novo.");
			return false;
		}

		if (falhaSeJaExisteDispositivo()) {
			if (getInstance().getTipoDispositivoNorma().getDsTipoDispositivo()
					.equals("Alínea")
					|| getInstance().getTipoDispositivoNorma()
							.getDsTipoDispositivo().equals("Parte")) {
				FacesMessages.instance().add(
						StatusMessage.Severity.ERROR,
						"A " + getInstance().getTipoDispositivoNorma()
								+ " informada já existe.");
			} else {
				FacesMessages.instance().add(
						StatusMessage.Severity.ERROR,
						"O " + getInstance().getTipoDispositivoNorma()
								+ " informado já existe.");
			}
			return false;
		}

		if (falhaArtigoPodeDesdobrar()) {
			String mensagem = Messages.instance().get(
					"dispositivoNorma.falhaArtigoPodeDesdobrar");
			FacesMessages.instance()
					.add(StatusMessage.Severity.ERROR, mensagem);
			return false;
		}

		if (falhaParagrafoPodeDesdobrar()) {
			String mensagem = Messages.instance().get(
					"dispositivoNorma.falhaParagrafoPodeDesdobrar");
			FacesMessages.instance()
					.add(StatusMessage.Severity.ERROR, mensagem);
			return false;
		}

		if (falhaIncisoPodeDesdobrar()) {
			String mensagem = Messages.instance().get(
					"dispositivoNorma.falhaIncisoPodeDesdobrar");
			FacesMessages.instance()
					.add(StatusMessage.Severity.ERROR, mensagem);
			return false;
		}

		if (falhaAlineaPodeDesdobrar()) {
			String mensagem = Messages.instance().get(
					"dispositivoNorma.falhaAlineaPodeDesdobrar");
			FacesMessages.instance()
					.add(StatusMessage.Severity.ERROR, mensagem);
			return false;
		}

		if (falhaItemPodeDesdobrar()) {
			String mensagem = Messages.instance().get(
					"dispositivoNorma.falhaItemPodeDesdobrar");
			FacesMessages.instance()
					.add(StatusMessage.Severity.ERROR, mensagem);
			return false;
		}

		if (naoPodeSerPai()) {
			String mensagem = Messages.instance().get(
					"dispositivoNorma.naoPodeSerPai");
			FacesMessages.instance()
					.add(StatusMessage.Severity.ERROR, mensagem);
			return false;
		}

		if (falhaAssuntoNaoSelecionado()) {
			String mensagem = Messages.instance().get(
					"dispositivoNorma.assuntoNaoSelecionado");
			FacesMessages.instance()
					.add(StatusMessage.Severity.ERROR, mensagem);
			return false;
		}

		setarOrdenacao(getInstance());
		return true;
	}

	public boolean validarNumeroDeMesesDias() {

		if (getInstance().getNrPenaMinimaDias() != null) {
			if (getInstance().getNrPenaMinimaDias() > 30) {
				return false;
			}
		}

		if (getInstance().getNrPenaMinimaMeses() != null) {
			if (getInstance().getNrPenaMinimaMeses() > 12) {
				return false;
			}
		}

		if (getInstance().getNrPenaMaximaDias() != null) {
			if (getInstance().getNrPenaMaximaDias() > 30) {
				return false;
			}
		}

		if (getInstance().getNrPenaMaximaMeses() != null) {
			if (getInstance().getNrPenaMaximaMeses() > 12) {
				return false;
			}
		}

		return true;
	}

	public boolean falhaAssuntoNaoSelecionado() {
		// if (getInstance().getUsoDispositivo().equals(UsoDispositivoEnum.TP)
		if (getInstance().getUsoDispositivo() != null
				&& getInstance().getUsoDispositivo().equals(
						UsoDispositivoEnum.TP)
				&& getInstance().getAssuntoTrf() == null) {
			return true;
		}

		return false;
	}

	public boolean numeroNoIntervalo(Integer numero, int maximo) {
		return numero != null && numero <= maximo;
	}

	public boolean verificaArtigoComNormaPenal() {
		if (getInstance().getTipoDispositivoNorma().getDsTipoDispositivo()
				.equals(TIPO_DISPOSITIVO_ARTIGO)) {
			if (getInstance().getNormaPenal() == null
					|| getInstance().getNormaPenal().getIdNormaPenal() == null) {
				return false;
			}
		}
		return true;
	}

	public boolean verificaData() {
		if (getInstance().getDtFimVigencia() != null) {
			if (getInstance().getDtInicioVigencia().after(
					getInstance().getDtFimVigencia())) {
				return false;
			}
		}
		return true;
	}

	public boolean falhaSeJaExisteDispositivo() {

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o FROM DispositivoNorma o WHERE ");

		boolean ehRaiz = getInstance().getDispositivoNormaPai() == null;
		if (ehRaiz) {
			sb.append(" o.dispositivoNormaPai IS NULL AND "); // DISPOSITIVO
			// ATUAL É PAI
		} else {
			sb.append(" o.dispositivoNormaPai IS NOT NULL AND "); // DISPOSITIVO
			// ATUAL É
			// FILHO,
			// POSSUI UM
			// PAI
			sb.append(" o.dispositivoNormaPai.idDispositivoNorma = "
					+ getInstance().getDispositivoNormaPai()
							.getIdDispositivoNorma() + " AND ");
		}

		boolean ehAtualizacao = getInstance().getIdDispositivoNorma() != 0;
		if (ehAtualizacao) {
			sb.append(" o.idDispositivoNorma <> "
					+ getInstance().getIdDispositivoNorma() + " AND ");
		}

		String identificador = getInstance().getDsIdentificador().toUpperCase();
		sb.append(" upper(to_ascii(o.dsIdentificador)) = upper(TO_ASCII('"
				+ identificador + "')) and ");
		sb.append(" o.tipoDispositivoNorma.idTipoDispositivo = '"
				+ getInstance().getTipoDispositivoNorma()
						.getIdTipoDispositivo() + "' AND ");
		sb.append(" o.normaPenal IS NOT NULL AND ");
		sb.append(" o.normaPenal.idNormaPenal = "
				+ getInstance().getNormaPenal().getIdNormaPenal() + " AND ");
		if (getInstance().getDtFimVigencia() == null) {
			/*
			 * se não informar o fim da vigência, verifica a existência de
			 * dispositivos com término maior ou igual ao início informado
			 */
			sb.append("(o.dtFimVigencia >= :dtInicioVigencia) AND ");
		} else {
			sb.append(" (o.dtFimVigencia >= :dtInicioVigencia and o.dtInicioVigencia <= :dtFimVigencia) AND ");
		}

		sb.append(" 0=0 ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("dtInicioVigencia", getInstance().getDtInicioVigencia());
		if (getInstance().getDtFimVigencia() != null) {
			q.setParameter("dtFimVigencia", getInstance().getDtFimVigencia());
		}

		boolean falhaSeJaExisteDispositivo = q.getResultList().isEmpty() == false;

		return falhaSeJaExisteDispositivo;
	}

	public boolean falhaSeJaExisteDispositivoSemTermino() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o FROM DispositivoNorma o WHERE ");

		boolean ehRaiz = getInstance().getDispositivoNormaPai() == null;
		if (ehRaiz) {
			sb.append(" o.dispositivoNormaPai IS NULL AND "); // DISPOSITIVO
			// ATUAL É PAI
		} else {
			sb.append(" o.dispositivoNormaPai IS NOT NULL AND "); // DISPOSITIVO
			// ATUAL É
			// FILHO,
			// POSSUI UM
			// PAI
			sb.append(" o.dispositivoNormaPai.idDispositivoNorma = "
					+ getInstance().getDispositivoNormaPai()
							.getIdDispositivoNorma() + " AND ");
		}

		boolean ehAtualizacao = getInstance().getIdDispositivoNorma() != 0;
		if (ehAtualizacao) {
			sb.append(" o.idDispositivoNorma <> "
					+ getInstance().getIdDispositivoNorma() + " AND ");
		}

		String identificador = getInstance().getDsIdentificador().toUpperCase();
		// sb.append(" upper(o.dsIdentificador) = '" + identificador +
		// "' AND ");
		sb.append(" upper(to_ascii(o.dsIdentificador)) = upper(TO_ASCII('"
				+ identificador + "')) and ");
		sb.append(" o.tipoDispositivoNorma.idTipoDispositivo = '"
				+ getInstance().getTipoDispositivoNorma()
						.getIdTipoDispositivo() + "' AND ");
		sb.append(" o.normaPenal IS NOT NULL AND ");
		sb.append(" o.normaPenal.idNormaPenal = "
				+ getInstance().getNormaPenal().getIdNormaPenal() + " AND ");
		sb.append(" o.dtFimVigencia is null AND ");
		sb.append(" 0=0 ");

		Query q = getEntityManager().createQuery(sb.toString());
		boolean falhaSeJaExisteDispositivoSemTermino = q.getResultList()
				.isEmpty() == false;
		return falhaSeJaExisteDispositivoSemTermino;
	}

	public boolean falhaArtigoPodeDesdobrar() {
		DispositivoNorma este = getInstance();
		if (este.seuPaiEh(TIPO_DISPOSITIVO_ARTIGO)) {
			return este.naoEh(TIPO_DISPOSITIVO_PARAGRAFO, "Inciso", "Alínea",
					"Parte");
		}
		return false;
	}

	public boolean falhaParagrafoPodeDesdobrar() {
		DispositivoNorma este = getInstance();
		if (este.seuPaiEh(TIPO_DISPOSITIVO_PARAGRAFO)) {
			return este.naoEh("Inciso", "Alínea", "Parte");
		}
		return false;
	}

	public boolean falhaIncisoPodeDesdobrar() {
		DispositivoNorma este = getInstance();
		if (este.seuPaiEh("Inciso")) {
			return este.naoEh("Alínea", "Parte");
		}
		return false;
	}

	public boolean falhaAlineaPodeDesdobrar() {
		DispositivoNorma este = getInstance();
		if (este.seuPaiEh("Alínea")) {
			return este.naoEh("Item", "Parte");
		}
		return false;
	}

	public boolean falhaItemPodeDesdobrar() {
		DispositivoNorma este = getInstance();
		if (este.seuPaiEh("Item")) {
			return este.naoEh("Parte");
		}
		return false;
	}

	public boolean naoPodeSerPai() {
		boolean ehParte = getInstance().eh("Parte");
		boolean parteEhPai = getInstance().getDispositivoNormaPai() == null;

		if (ehParte) {
			if (parteEhPai) {
				return true;
			}
		}
		return false;
	}

	/*
	 * ****************** TREE HANDLER
	 * ***************************************************
	 */

	@Override
	public void onClickFormTab() {
		if (isManaged()) {
			dispositivoNormaTree = getInstance().getDispositivoNormaPai();
			tipoPenaTree = getInstance().getTipoPena();
			setAssuntoTrfCriminalTree(getInstance().getAssuntoTrf());
			setAssuntoAtoInfracionalTree(getInstance()
					.getAssuntoAtoInfracional());
		} else {
			dispositivoNormaTree = null;
			tipoPenaTree = null;
			setAssuntoTrfCriminalTree(null);
			setAssuntoAtoInfracionalTree(null);
		}
		super.onClickFormTab();
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			dispositivoNormaTree = getInstance().getDispositivoNormaPai();
			tipoPenaTree = getInstance().getTipoPena();
			setAssuntoTrfCriminalTree(getInstance().getAssuntoTrf());
			setAssuntoAtoInfracionalTree(getInstance()
					.getAssuntoAtoInfracional());
		}
		if (id == null) {
			dispositivoNormaTree = null;
			tipoPenaTree = null;
			setAssuntoTrfCriminalTree(null);
			setAssuntoAtoInfracionalTree(null);
		}
	}

	@Override
	public void limparTrees() {
		DispositivoNormaTreeHandler dispositivoNormaTree = getComponent("dispositivoNormaTree");
		TipoPenaTreeHandler tipoPenaTree = getComponent("tipoPenaTree");
		AssuntoTrfCriminalTreeHandler assuntoTrfCriminalTree = getComponent("assuntoTrfCriminalTree");
		AssuntoAtoInfracionalTreeHandler assuntoAtoInfracionalTree = getComponent("assuntoAtoInfracionalTree");

		dispositivoNormaTree.clearTree();
		tipoPenaTree.clearTree();
		assuntoTrfCriminalTree.clearTree();
		assuntoAtoInfracionalTree.clearTree();

		if (getInstance().getDispositivoNormaPai() != null) {
			Integer id = getInstance().getDispositivoNormaPai()
					.getIdDispositivoNorma();
			DispositivoNorma dn = new DispositivoNorma(id);
			this.dispositivoNormaTree = dn;
		}

		if (getInstance().getTipoPena() != null) {
			Integer id = getInstance().getTipoPena().getIdTipoPena();
			TipoPena tp = new TipoPena(id);
			this.tipoPenaTree = tp;
		}

		if (getInstance().getAssuntoTrf() != null) {
			Integer id = getInstance().getAssuntoTrf().getIdAssuntoTrf();
			AssuntoTrf assunto = new AssuntoTrf();
			assunto.setIdAssuntoTrf(id);
			setAssuntoTrfCriminalTree(assunto);
		}

		if (getInstance().getAssuntoAtoInfracional() != null) {
			Integer id = getInstance().getAssuntoAtoInfracional()
					.getIdAssuntoTrf();
			AssuntoTrf assunto = new AssuntoTrf();
			assunto.setIdAssuntoTrf(id);
			setAssuntoAtoInfracionalTree(assunto);
		}
	}

	/*
	 * *************** MÉTODOS DE NEGÓCIO
	 * **********************************************
	 */
	public List<SelectItem> listarSimbolos() {

		List<SelectItem> listaResultado = new ArrayList<SelectItem>();

		if (getInstance().getTipoDispositivoNorma() == null) {
			listaResultado.add(new SelectItem("", ""));
		} else {
			if (getInstance().getTipoDispositivoNorma().getDsTipoDispositivo()
					.equals(TIPO_DISPOSITIVO_ARTIGO)) {
				listaResultado.add(new SelectItem(
						TIPO_DISPOSITIVO_ARTIGO_SIMBOLO,
						TIPO_DISPOSITIVO_ARTIGO_SIMBOLO));
				getInstance().setDsSimbolo(TIPO_DISPOSITIVO_ARTIGO_SIMBOLO);
			} else if (getInstance().getTipoDispositivoNorma()
					.getDsTipoDispositivo().equals(TIPO_DISPOSITIVO_PARAGRAFO)) {
				listaResultado.add(new SelectItem(
						TIPO_DISPOSITIVO_PARAGRADO_SIMBOLO,
						TIPO_DISPOSITIVO_PARAGRADO_SIMBOLO));
				listaResultado.add(new SelectItem(
						TIPO_DISPOSITIVO_PARAGRAFO_UNICO,
						TIPO_DISPOSITIVO_PARAGRAFO_UNICO));
			}
		}

		return listaResultado;
	}

	public void preveTipoPenalSim() {
		// if(getInstance().getInPrevisaoTipoPenal()== true){
		if (getInstance().getUsoDispositivo() == UsoDispositivoEnum.TP) {
			getInstance().setInPrevisaoPenaPrivativa(true);
		} else {
			getInstance().setInPrevisaoPenaPrivativa(false);
			getInstance().setInHediondo(false);
			getInstance().setInPrevisaoPenaMulta(false);
			getInstance().setInPrevisaoPenaRestritiva(false);
			// getInstance().setInPrevisaoTipoPenal(false);
			// getInstance().setUsoDispositivo(null);
			getInstance().setDtHediondo(null);
			getInstance().setTipoPena(null);
			getInstance().setNrPenaMaximaAnos(null);
			getInstance().setNrPenaMaximaDias(null);
			getInstance().setNrPenaMaximaMeses(null);
			getInstance().setNrPenaMinimaAnos(null);
			getInstance().setNrPenaMinimaDias(null);
			getInstance().setNrPenaMinimaMeses(null);
			getInstance().setPermitirAssociacaoMultipla(
					getInstance().getUsoDispositivo() != null);
		}
	}

	public void hediondoSim() {
		if (getInstance().getInHediondo()) {
			setBloquearDataHediondo(false);
			getInstance().setDtHediondo(getInstance().getDtInicioVigencia());
		} else if (!getInstance().getInHediondo()
				|| getInstance().getInHediondo() == null) {
			setBloquearDataHediondo(true);
		}
	}

	public void previsaoPenaMulta() {
		if (getInstance().getInPrevisaoPenaPrivativa() != null) {
			if (getInstance().getInPrevisaoPenaMulta()
					&& getInstance().getInPrevisaoPenaPrivativa()) {
				setBloquearMultaPenaPrivativa(false);
			} else {
				setBloquearMultaPenaPrivativa(true);
			}
			if (!getInstance().getInPrevisaoPenaMulta()) {
				setIdMultaPenaPvt(null);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<SelectItem> getListaTipoDispositivoNorma() {
		StringBuilder sb = new StringBuilder();
		sb.append("select tdn from TipoDispositivoNorma tdn where tdn.inAtivo = true order by tdn.idTipoDispositivo");
		Query q = getEntityManager().createQuery(sb.toString());

		List<SelectItem> listaResultado = new ArrayList<SelectItem>();
		listaResultado.add(new SelectItem(0, "Selecione"));

		List<TipoDispositivoNorma> listTipoDispositivoNorma;
		listTipoDispositivoNorma = q.getResultList();

		for (TipoDispositivoNorma tipoDispositivoNorma : listTipoDispositivoNorma) {
			long id = tipoDispositivoNorma.getIdTipoDispositivo();
			String descricao = tipoDispositivoNorma.getDsTipoDispositivo();
			SelectItem selectItem = new SelectItem(id, descricao);
			listaResultado.add(selectItem);
		}
		return listaResultado;
	}

	public SimboloDispositivoNormaEnum[] getSimboloDispositivoValues() {
		return SimboloDispositivoNormaEnum.values();
	}

	public Boolean getEhPrevisaoTipoPenal() {
		if (getInstance() != null && getInstance().getUsoDispositivo() != null) {
			return (getInstance().getUsoDispositivo()
					.equals(UsoDispositivoEnum.TP));
		}

		return false;
	}

	public void setEhPrevisaoTipoPenal(Boolean ehPrevisaoTipoPenal) {
		this.ehPrevisaoTipoPenal = ehPrevisaoTipoPenal;
	}

	/*
	 * ************* GETTER'S AND SETTER'S
	 * ****************************************************
	 */

	private Integer tipoPenal = 2;
	private DispositivoNorma dispositivoNormaTree;
	private AssuntoTrf assuntoTrfCriminalTree;
	private AssuntoTrf assuntoAtoInfracionalTree;
	private TipoPena tipoPenaTree;
	// private String idMultaPenaPvt = "";

	// RN
	private boolean bloquearDataHediondo = true;
	private boolean bloquearMultaPenaPrivativa = true;

	public void setTipoDispositivo(TipoDispositivoNorma tipoDispositivoNorma) {
		getInstance().setTipoDispositivoNorma(tipoDispositivoNorma);
	}

	public TipoDispositivoNorma getTipoDispositivo() {
		return getInstance().getTipoDispositivoNorma();
	}

	private boolean comparaDispositivo(String enumDispositivo) {
		if (getInstance() != null
				&& getInstance().getTipoDispositivoNorma() != null
				&& getInstance().getTipoDispositivoNorma()
						.getDsTipoDispositivo().equals(enumDispositivo)) {
			return true;
		}
		return false;
	}

	public boolean isBloquearDispositivoPai() {
		// RN13
		if (comparaDispositivo(TIPO_DISPOSITIVO_ARTIGO)) {
			return true;
		}
		return false;
	}

	public boolean isBloquearComboSimbolo() {
		// RN35
		if (comparaDispositivo(TIPO_DISPOSITIVO_PARAGRAFO)) {
			return false;
		}
		// getInstance().setDsSimbolo("");
		return true;
	}

	public boolean isBloquearIdentificadorDispositivo() {
		// RN126
		if (comparaDispositivo(TIPO_DISPOSITIVO_PARAGRAFO)
				&& getInstance().getDsSimbolo() != null
				&& getInstance().getDsSimbolo().equals(
						TIPO_DISPOSITIVO_PARAGRAFO_UNICO)) {
			getInstance().setDsIdentificador("");
			return true;
		}
		return false;
	}

	public boolean isBloquearDataHediondo() {
		return bloquearDataHediondo;
	}

	public void setBloquearDataHediondo(boolean bloquearDataHediondo) {
		this.bloquearDataHediondo = bloquearDataHediondo;
	}

	public boolean isBloquearMultaPenaPrivativa() {
		return bloquearMultaPenaPrivativa;
	}

	public void setBloquearMultaPenaPrivativa(boolean bloquearMultaPenaPrivativa) {
		this.bloquearMultaPenaPrivativa = bloquearMultaPenaPrivativa;
	}

	@Override
	public Integer getDispositivoNormaId() {
		return (Integer) getId();
	}

	@Override
	public void setDispositivoNormaId(Integer id) {
		setId(id);
	}

	public Integer getTipoPenal() {
		return tipoPenal;
	}

	public void setTipoPenal(Integer tipoPenal) {
		this.tipoPenal = tipoPenal;
	}

	public String getIdMultaPenaPvt() {
		String ret = "";
		if (getInstance() != null
				&& getInstance().getMultaPenaPrivativa() != null
				&& getInstance().getMultaPenaPrivativa()
						.getIdMultaPenaPrivativa() != null) {
			Integer id = getInstance().getMultaPenaPrivativa()
					.getIdMultaPenaPrivativa();
			ret = (id == null ? "" : id) + "";
		}
		return ret;
	}

	public void setIdMultaPenaPvt(String idMultaPenaPvt) {
		if (getInstance() != null && idMultaPenaPvt != null
				&& !idMultaPenaPvt.isEmpty()) {
			getInstance().setMultaPenaPrivativa(
					new MultaPenaPrivativa(Integer.parseInt(idMultaPenaPvt)));
		} else {
			getInstance().setMultaPenaPrivativa(null);
		}
	}

	public DispositivoNorma getDispositivoNormaTree() {
		return dispositivoNormaTree;
	}

	public void setDispositivoNormaTree(DispositivoNorma dispositivoNormaTree) {
		this.dispositivoNormaTree = dispositivoNormaTree;
	}

	public TipoPena getTipoPenaTree() {
		return tipoPenaTree;
	}

	public void setTipoPenaTree(TipoPena tipoPenaTree) {
		this.tipoPenaTree = tipoPenaTree;
	}

	public AssuntoTrf getAssuntoTrfCriminalTree() {
		return assuntoTrfCriminalTree;
	}

	public void setAssuntoTrfCriminalTree(AssuntoTrf assuntoTrfCriminalTree) {
		this.assuntoTrfCriminalTree = assuntoTrfCriminalTree;
	}

	public AssuntoTrf getAssuntoAtoInfracionalTree() {
		return assuntoAtoInfracionalTree;
	}

	public void setAssuntoAtoInfracionalTree(
			AssuntoTrf assuntoAtoInfracionalTree) {
		this.assuntoAtoInfracionalTree = assuntoAtoInfracionalTree;
	}

	public boolean desabilitarMultaIsolada() {
		if (getInstance().getUsoDispositivo() == UsoDispositivoEnum.TP) {
			if (getInstance().getMultaPenaPrivativa() != null
					&& getInstance()
							.getMultaPenaPrivativa()
							.getIdMultaPenaPrivativa()
							.equals(MultaPenaPrivativa.ISOLADA
									.getIdMultaPenaPrivativa())) {
				getInstance().setMultaPenaPrivativa(null);
			}
			if (getInstance().getInPrevisaoPenaPrivativa()) {
				return true;
			}
			if (getInstance().getInPrevisaoPenaMulta()) {
				setIdMultaPenaPvt(String.valueOf(MultaPenaPrivativa.ISOLADA
						.getIdMultaPenaPrivativa()));
				return false;
			}
		}

		return true;

	}

	public boolean desabilitarMultaCumulativaEAlternativa() {
		if (getInstance().getUsoDispositivo() == UsoDispositivoEnum.TP) {
			if (getInstance().getInPrevisaoPenaMulta()) {
				return !getInstance().getInPrevisaoPenaPrivativa();
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public List<SelectItem> getTiposMultaPenaPrivativa() {
		List<SelectItem> returnValue = new ArrayList<SelectItem>(0);
		Query query = getEntityManager().createQuery(
				"from MultaPenaPrivativa where inAtivo = true");
		List<MultaPenaPrivativa> list = query.getResultList();
		for (MultaPenaPrivativa item : list) {
			SelectItem si = new SelectItem();
			si.setLabel(item.getDsMultaPenaPrivativa());
			si.setValue(item.getIdMultaPenaPrivativa());
			if (item.getIdMultaPenaPrivativa().equals(
					MultaPenaPrivativa.ISOLADA.getIdMultaPenaPrivativa())) {
				si.setDisabled(desabilitarMultaIsolada());
			} else {
				si.setDisabled(desabilitarMultaCumulativaEAlternativa());
			}
			returnValue.add(si);
		}
		return returnValue;
	}

	public List<SelectItem> getUsosDispositivoNorma() {
		List<SelectItem> items = new ArrayList<SelectItem>();
		items.add(new SelectItem(null, "Nenhum dos tipos"));
		for (UsoDispositivoEnum aux : UsoDispositivoEnum.values()) {
			items.add(new SelectItem(aux, aux.getLabel()));
		}
		return items;
	}

	@SuppressWarnings("unchecked")
	public void subirOrdenacao(DispositivoNorma dispositivoNorma) {
		Query query = getEntityManager().createQuery(
				"select o from DispositivoNorma o where o.numeroOrdem < :numeroOrdem and "
						+ "o.normaPenal.idNormaPenal = :idNormaPenal "
						+ "order by numeroOrdem desc");

		query.setParameter("numeroOrdem", dispositivoNorma.getNumeroOrdem());
		query.setParameter("idNormaPenal", NormaPenalHome.instance()
				.getInstance().getIdNormaPenal());

		List<DispositivoNorma> result = query.getResultList();

		if (!result.isEmpty()) {
			// recuperar o item anterior na mesma hierarquia
			DispositivoNorma anteriorMesmaHierarquia = null;

			for (DispositivoNorma anterior : result) {
				if (anterior.getDispositivoNormaPai() == dispositivoNorma
						.getDispositivoNormaPai()) {
					anteriorMesmaHierarquia = anterior;
					break;
				}
			}

			if (anteriorMesmaHierarquia == null) {
				return;
			}

			// reordenar
			Comparator<DispositivoNorma> comparator = new Comparator<DispositivoNorma>() {
				@Override
				public int compare(DispositivoNorma o1, DispositivoNorma o2) {
					return o1.getNumeroOrdem().compareTo(o2.getNumeroOrdem());
				}
			};

			// ordem desejada
			List<DispositivoNorma> itensReordenados = getListaHierarquica(dispositivoNorma);
			itensReordenados
					.addAll(getListaHierarquica(anteriorMesmaHierarquia));

			// ordem atual
			List<DispositivoNorma> itensParaReordenar = new ArrayList<DispositivoNorma>(
					itensReordenados);

			Collections.sort(itensParaReordenar, comparator);

			Map<Integer, Integer> dePara = new HashMap<Integer, Integer>(
					itensReordenados.size());

			// setar numero ordem
			for (int i = 0; i < itensReordenados.size(); i++) {
				int numeroOrdem = itensParaReordenar.get(i).getNumeroOrdem();
				DispositivoNorma item = itensReordenados.get(i);
				dePara.put(item.getNumeroOrdem(), numeroOrdem);
			}

			for (DispositivoNorma item : itensParaReordenar) {
				item.setNumeroOrdem(dePara.get(item.getNumeroOrdem()));
			}

			getEntityManager().flush();
		}
	}

	private List<DispositivoNorma> getListaHierarquica(
			DispositivoNorma dispositivoNorma) {
		List<DispositivoNorma> returnValue = new ArrayList<DispositivoNorma>(0);
		returnValue.add(dispositivoNorma);
		// reordenar
		Comparator<DispositivoNorma> comparator = new Comparator<DispositivoNorma>() {
			@Override
			public int compare(DispositivoNorma o1, DispositivoNorma o2) {
				return o1.getNumeroOrdem().compareTo(o2.getNumeroOrdem());
			}
		};
		Collections
				.sort(dispositivoNorma.getDispositivoNormaList(), comparator);
		for (DispositivoNorma item : dispositivoNorma.getDispositivoNormaList()) {
			if (item.getDispositivoNormaList().isEmpty()) {
				returnValue.add(item);
			} else {
				returnValue.addAll(getListaHierarquica(item));
			}
		}
		return returnValue;
	}

	@SuppressWarnings("unchecked")
	public void descerOrdenacao(DispositivoNorma dispositivoNorma) {
		Query query = getEntityManager()
				.createQuery(
						"select o from DispositivoNorma o where o.numeroOrdem > :numeroOrdem and "
								+ "o.normaPenal.idNormaPenal = :idNormaPenal order by numeroOrdem");

		query.setParameter("numeroOrdem", dispositivoNorma.getNumeroOrdem());
		query.setParameter("idNormaPenal", NormaPenalHome.instance()
				.getInstance().getIdNormaPenal());

		List<DispositivoNorma> result = query.getResultList();
		if (!result.isEmpty()) {
			// recuperar o item posterior na mesma hierarquia
			DispositivoNorma posteriorMesmaHierarquia = null;

			for (DispositivoNorma posterior : result) {
				if (posterior.getDispositivoNormaPai() == dispositivoNorma
						.getDispositivoNormaPai()) {
					posteriorMesmaHierarquia = posterior;
					break;
				}
			}

			if (posteriorMesmaHierarquia == null) {
				return;
			}

			// reordenar
			Comparator<DispositivoNorma> comparator = new Comparator<DispositivoNorma>() {
				@Override
				public int compare(DispositivoNorma o1, DispositivoNorma o2) {
					return o1.getNumeroOrdem().compareTo(o2.getNumeroOrdem());
				}
			};

			// ordem desejada
			List<DispositivoNorma> itensReordenados = getListaHierarquica(posteriorMesmaHierarquia);
			itensReordenados.addAll(getListaHierarquica(dispositivoNorma));

			// ordem atual
			List<DispositivoNorma> itensParaReordenar = new ArrayList<DispositivoNorma>(
					itensReordenados);

			Collections.sort(itensParaReordenar, comparator);

			Map<Integer, Integer> dePara = new HashMap<Integer, Integer>(
					itensReordenados.size());

			// setar numero ordem
			for (int i = 0; i < itensReordenados.size(); i++) {
				int numeroOrdem = itensParaReordenar.get(i).getNumeroOrdem();
				DispositivoNorma item = itensReordenados.get(i);
				dePara.put(item.getNumeroOrdem(), numeroOrdem);
			}

			for (DispositivoNorma item : itensParaReordenar) {
				item.setNumeroOrdem(dePara.get(item.getNumeroOrdem()));
			}

			getEntityManager().flush();
		}
	}

	public void limparDadosPrivativaLiberdade() {
		getInstance().setTipoPena(null);
		setTipoPenaTree(null);
		getInstance().setNrPenaMaximaAnos(null);
		getInstance().setNrPenaMaximaMeses(null);
		getInstance().setNrPenaMaximaDias(null);
		getInstance().setNrPenaMinimaAnos(null);
		getInstance().setNrPenaMinimaMeses(null);
		getInstance().setNrPenaMinimaDias(null);
	}

	@SuppressWarnings("unchecked")
	public void setarOrdenacao(DispositivoNorma dispositivoNorma) {
		// se insercao - colocará sempre como último
		if (dispositivoNorma != null
				&& dispositivoNorma.getIdDispositivoNorma() == 0) {
			Query query = getEntityManager().createQuery(
					"select max(d.numeroOrdem) from DispositivoNorma d");
			List<Integer> result = query.getResultList();

			if (dispositivoNorma.getDispositivoNormaPai() != null) {
				// shift nas normas
				List<DispositivoNorma> normas = getListaHierarquica(dispositivoNorma
						.getDispositivoNormaPai());
				int ultimoNumeroOrdem = normas.get(normas.size() - 1)
						.getNumeroOrdem();
				dispositivoNorma.setNumeroOrdem(ultimoNumeroOrdem + 1);
				Query queryUpdate = getEntityManager()
						.createQuery(
								"update DispositivoNorma set numeroOrdem = numeroOrdem + 1 where numeroOrdem >= :ultimoNumeroOrdem");
				queryUpdate.setParameter("ultimoNumeroOrdem",
						dispositivoNorma.getNumeroOrdem());
				queryUpdate.executeUpdate();
			} else {
				if (result.get(0) != null) {
					dispositivoNorma.setNumeroOrdem(result.get(0) + 1);
				} else {
					dispositivoNorma.setNumeroOrdem(1);
				}

			}			
		}
	}
	
	@Override
	protected String afterPersistOrUpdate(String ret) {
		Integer idNorma = NormaPenalHome.instance().getInstance().getIdNormaPenal();
		Integer idDispositivo = getInstance().getIdDispositivoNorma();
		NormaPenalHome.instance().setId(null);
		setId(null);
		getEntityManager().clear();
		NormaPenalHome.instance().setId(idNorma);
		setId(idDispositivo);
		return super.afterPersistOrUpdate(ret);
	}

	public int getIndentacao(DispositivoNorma dispositivoNorma) {
		int depth = 5;
		DispositivoNorma pai = dispositivoNorma.getDispositivoNormaPai();
		while (pai != null) {
			depth = depth * 2;
			pai = pai.getDispositivoNormaPai();
		}
		return depth;
	}

}
