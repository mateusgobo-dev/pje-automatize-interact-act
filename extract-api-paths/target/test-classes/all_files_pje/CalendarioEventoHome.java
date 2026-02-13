package br.com.infox.cliente.home;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.CalendarioEvento;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.AbrangenciaEnum;

@Name(CalendarioEventoHome.NAME)
@BypassInterceptors
public class CalendarioEventoHome extends AbstractCalendarioEventoHome<CalendarioEvento> {

	private static final long serialVersionUID = 1L;
	private boolean repeteAno = false;
	private boolean periodicidade = true;
	public static final String NAME = "calendarioEventoHome";
	private List<CalendarioEvento> listaFeriados;
	private AbrangenciaEnum inAbrangencia;

	public static CalendarioEventoHome instance() {
		return ComponentUtil.getComponent(CalendarioEventoHome.NAME);
	}

	@Override
	public void newInstance() {
		super.newInstance();
		setRepeteAno(false);
		setPeriodicidade(true);
		if (getListaFeriados() != null && getListaFeriados().size() > 0) {
			getListaFeriados().clear();
		}
		getInstance().setInAbrangencia(AbrangenciaEnum.N);
	}

	public void clearSearchAbrangencia() {
		CalendarioEvento calendarioEvento = getComponent("calendarioEventoSearch");
		calendarioEvento.setEstado(null);
		calendarioEvento.setMunicipio(null);
		calendarioEvento.setOrgaoJulgador(null);
	}

	public AbrangenciaEnum[] getAbrangenciaEnumValues() {
		return AbrangenciaEnum.values();
	}

	public AbrangenciaEnum[] getAbrangenciaEnumO() {
		AbrangenciaEnum p[] = { AbrangenciaEnum.O };
		return p;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		Calendar calendar = Calendar.getInstance();
		super.setId(id);
		if (changed) {
			/* preenche a data Incial */
			if (getInstance().getDtAno() == null) {
				repeteAno = Boolean.TRUE;
				calendar.set(Calendar.DATE, getInstance().getDtDia());
				calendar.set(Calendar.MONTH, getInstance().getDtMes() - 1);
				calendar.set(Calendar.YEAR, Integer.parseInt(sdf.format(new Date())));
				getInstance().setDataEvento(calendar.getTime());

			} else {
				repeteAno = Boolean.FALSE;
				calendar = Calendar.getInstance();
				calendar.set(Calendar.DATE, getInstance().getDtDia());
				calendar.set(Calendar.MONTH, getInstance().getDtMes() - 1);
				calendar.set(Calendar.YEAR, getInstance().getDtAno());
				getInstance().setDataEvento(calendar.getTime());
			}

			/* preenche a data Final */
			if (getInstance().getDtDiaFinal() != null) {
				if (getInstance().getDtAnoFinal() == null) {
					repeteAno = Boolean.TRUE;
					calendar = Calendar.getInstance();
					calendar.set(Calendar.DATE, getInstance().getDtDiaFinal());
					calendar.set(Calendar.MONTH, getInstance().getDtMesFinal() - 1);
					calendar.set(Calendar.YEAR, Integer.parseInt(sdf.format(new Date())));
					getInstance().setDataEventoFim(calendar.getTime());
				} else {
					repeteAno = Boolean.FALSE;
					calendar = Calendar.getInstance();
					calendar.set(Calendar.DATE, getInstance().getDtDiaFinal());
					calendar.set(Calendar.MONTH, getInstance().getDtMesFinal() - 1);
					calendar.set(Calendar.YEAR, getInstance().getDtAnoFinal());
					getInstance().setDataEventoFim(calendar.getTime());
				}
			}
			/* verifiva de a periodicidade e "Dia" ou "Período" */
			if ((getInstance().getDtDia() != null) && (getInstance().getDtDiaFinal() == null)) {
				periodicidade = Boolean.TRUE;
			} else {
				periodicidade = Boolean.FALSE;
			}
		}
	}

	public void setarDatas() {
		if (instance.getDataEvento() == null) {
			return;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		if (repeteAno) {
			instance.setDtAnoFinal(null);
			if (periodicidade) {
				instance.setDtDiaFinal(null);
				instance.setDtMesFinal(null);
				instance.setDtAnoFinal(null);
			} else {
				if (instance.getDataEventoFim() != null) {
					String[] dataFim = sdf.format(instance.getDataEventoFim()).split("/");
					instance.setDtDiaFinal(Integer.parseInt(dataFim[0]));
					instance.setDtMesFinal(Integer.parseInt(dataFim[1]));
				}
			}

			String[] dataInicio = sdf.format(instance.getDataEvento()).split("/");

			instance.setDtDia(Integer.parseInt(dataInicio[0]));
			instance.setDtMes(Integer.parseInt(dataInicio[1]));
		} else {
			String[] dataInicio = sdf.format(instance.getDataEvento()).split("/");

			instance.setDtDia(Integer.parseInt(dataInicio[0]));
			instance.setDtMes(Integer.parseInt(dataInicio[1]));
			instance.setDtAno(Integer.parseInt(dataInicio[2]));

			if ((instance.getDataEventoFim() != null) && (!periodicidade)) {
				String[] dataFim = sdf.format(instance.getDataEventoFim()).split("/");
				instance.setDtDiaFinal(Integer.parseInt(dataFim[0]));
				instance.setDtMesFinal(Integer.parseInt(dataFim[1]));
				instance.setDtAnoFinal(Integer.parseInt(dataFim[2]));
			} else {
				instance.setDtDiaFinal(null);
				instance.setDtMesFinal(null);
				instance.setDtAnoFinal(null);
			}
		}

		if (Authenticator.getOrgaoJulgadorAtual() != null) {
			instance.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
			instance.setInAbrangencia(AbrangenciaEnum.O);
		}
	}

	@SuppressWarnings("unchecked")
	/**
	 * Método que valida se já exise algum evento ou periodo cadastrado no dia
	 * ou periodo informado.
	 */
	public boolean validarDuplicacao() {
		setarDatas();

		StringBuilder sb = new StringBuilder();
		sb.append("select o from CalendarioEvento o ");
		sb.append("where ");
		if (getInstance().getIdCalendarioEvento() != 0) {
			sb.append("o.idCalendarioEvento != :id and ");
		}

		if (getInstance().getDtDiaFinal() != null) {
			sb.append("(o.dsEvento = :dsEvento or  ").append("(:diaIni >= o.dtDia and :diaIni <= o.dtDiaFinal ")
					.append("and ").append(":mesIni >= o.dtMes and :mesIni <= o.dtMesFinal ").append("and  ")
					.append("((:anoIni >= o.dtAno or o.dtAno is null) and ")
					.append("(:anoIni <= o.dtAnoFinal or o.dtAnoFinal is null))) ").append("or ")
					.append("(:diaFim >= o.dtDia and :diaFim <= o.dtDiaFinal and ")
					.append(":mesFim >= o.dtMes and :mesFim <= o.dtMesFinal and  ")
					.append("((:dtAnoFim >= o.dtAno or o.dtAno is null) and ")
					.append("(:dtAnoFim <= o.dtAnoFinal or o.dtAnoFinal is null)))) ").append(" or ")
					.append(" (o.dtDia >= :diaIni and o.dtDia <= :diaFim ")
					.append("and o.dtMes >= :mesIni and o.dtMes <= :mesFim and ")
					.append("((o.dtAno >= :anoIni or :anoIni is null) and ")
					.append("(o.dtAno <= :dtAnoFim or :dtAnoFim is null))))");
		} else {
			sb.append("(o.dsEvento = :dsEvento or  ").append("(:diaIni >= o.dtDia and :diaIni <= o.dtDiaFinal ")
					.append("and :mesIni >= o.dtMes and :mesIni <= o.dtMesFinal ")
					.append("and  ((:anoIni >= o.dtAno or o.dtAno is null) ")
					.append("and (:anoIni <= o.dtAnoFinal or o.dtAnoFinal is null))) ")
					.append("or (o.dtDia = :diaIni and o.dtMes = :mesIni and ")
					.append("(o.dtAno = :anoIni or o.dtAno is null))) ");
		}
		sb.append("and o.ativo = true ");

		Query q = getEntityManager().createQuery(sb.toString());
		if (getInstance().getIdCalendarioEvento() != 0) {
			q.setParameter("id", getInstance().getIdCalendarioEvento());
		}

		q.setParameter("dsEvento", getInstance().getDsEvento());

		if (getInstance().getDtDiaFinal() != null) {
			q.setParameter("diaFim", getInstance().getDtDiaFinal());
			q.setParameter("mesFim", getInstance().getDtMesFinal());
			q.setParameter("dtAnoFim", getInstance().getDtAnoFinal());
		}

		q.setParameter("diaIni", getInstance().getDtDia());
		q.setParameter("mesIni", getInstance().getDtMes());
		q.setParameter("anoIni", getInstance().getDtAno());
		
		List<CalendarioEvento> lista = (List<CalendarioEvento>)q.getResultList();
		if (lista != null && !lista.isEmpty()) {
			listaFeriados = new ArrayList<CalendarioEvento>(0);
			listaFeriados.addAll(lista);
			return true;
		} else {
			return false;
		}
	}

	public boolean validarDatas() {
		if (!periodicidade)
			if (instance.getDataEvento().after(instance.getDataEventoFim())) {
				return true;
			}
		return false;
	}

	/**
	 * Método que verifica se o feriado repete anualmente. Caso o
	 * CalendarioEvento esteja com o Ano nulo, então é porque repete anualmente.
	 * 
	 * @param ce
	 * @return true caso o ano seja nulo ou false caso o ano não seja nulo.
	 */
	public boolean repeteAnualmente(CalendarioEvento ce) {
		if (ce.getDtAno() == null) {
			return true;
		}
		return false;
	}

	public boolean getRepeteAno() {
		return repeteAno;
	}

	public void setRepeteAno(boolean repeteAno) {
		this.repeteAno = repeteAno;
	}

	public boolean getPeriodicidade() {
		return periodicidade;
	}

	public void setPeriodicidade(boolean periodicidade) {
		this.periodicidade = periodicidade;
	}

	public Localizacao getLocalizacaoUsuario() {
		return Authenticator.getLocalizacaoFisicaAtual();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		setarDatas();
		if(getInAbrangencia() != null){
			getInstance().setInAbrangencia(inAbrangencia);
		}
		
		if (validarDatas()) {
			FacesMessages.instance()
					.add(StatusMessage.Severity.ERROR, "A Data Inicial deve ser menor que a Data Final");
			return false;
		} else {
			if (this.instance.getInAbrangencia() == AbrangenciaEnum.E) {
				this.instance.setMunicipio(null);
			} else if (this.instance.getInAbrangencia() == AbrangenciaEnum.N || this.instance.getInAbrangencia() == AbrangenciaEnum.O) {
				this.instance.setMunicipio(null);
				this.instance.setEstado(null);
			}
		}

		if (getInstance().getInSuspendeDistribuicao() == null) {
			getInstance().setInSuspendeDistribuicao(false);
		}

		if (repeteAno) {
			getInstance().setDtAno(null);
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			String[] dataInicio = sdf.format(instance.getDataEvento()).split("/");
			getInstance().setDtAno(Integer.parseInt(dataInicio[2]));
		}

		return super.beforePersistOrUpdate();
	}

	public void setListaFeriados(List<CalendarioEvento> listaFeriados) {
		this.listaFeriados = listaFeriados;
	}

	public List<CalendarioEvento> getListaFeriados() {
		return listaFeriados;
	}

	@Override
	public void onClickSearchTab() {
		newInstance();
		super.onClickSearchTab();
	}

	@Override
	public void onClickFormTab() {
		newInstance();
		super.onClickFormTab();
	}

	public void setarDataFinal() {
		if (getPeriodicidade()) {
			getInstance().setDataEventoFim(null);
		}
	}

	public int calcularHeightModal() {
		if (getListaFeriados() == null) {
			return 150;
		} else {
			int tamLista = getListaFeriados().size();
			if (tamLista == 0 || tamLista == 1) {
				return 150;
			} else {
				int multiplicador = tamLista - 2;
				return 150 + (multiplicador * 20);
			}
		}
	}

	@Override
	public String update() {
		if (getInstance().getInAbrangencia() != AbrangenciaEnum.O) {
			getInstance().setOrgaoJulgador(null);
		}
		String ret = super.update();
		return ret;
	}

	@Override
	public String persist() {
		String ret = super.persist();
		return ret;
	}
	
	/**
	 * Método que verifica se o usuário logado pode editar o CalendarioEvento
	 * @param row
	 * @return
	 */
	public boolean canEditCalendarioEvento(CalendarioEvento row) {
		if (row != null) {
			boolean mesmoOrgaoJulgador = Authenticator.getOrgaoJulgadorAtual() != null && 
					Authenticator.getOrgaoJulgadorAtual().equals(row.getOrgaoJulgador());
					
			Identity identity = Identity.instance();
			boolean usuarioAdministrador = identity.hasRole("admin") || 
					identity.hasRole("administrador") || identity.hasRole(Papeis.ADMINISTRADOR);
					
			boolean ojcAssociadoOj = verificarAssociacaoOjOjc(row.getOrgaoJulgador());
			
			return mesmoOrgaoJulgador || usuarioAdministrador || ojcAssociadoOj;
		}
		return false;
	}
	
	private boolean verificarAssociacaoOjOjc(OrgaoJulgador orgaoJulgador){
		if(orgaoJulgador == null){
			return false;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from OrgaoJulgadorColegiadoOrgaoJulgador o ");
		sb.append("where o.orgaoJulgadorColegiado = :ojc ");
		sb.append("and o.orgaoJulgador = :oj ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("ojc", Authenticator.getOrgaoJulgadorColegiadoAtual());
		q.setParameter("oj", orgaoJulgador);
		Long count = (Long)q.getSingleResult();
		return count > 0;
		
	}

	public AbrangenciaEnum getInAbrangencia() {
		if(Authenticator.getOrgaoJulgadorAtual() != null || Authenticator.getOrgaoJulgadorColegiadoAtual() != null){
			inAbrangencia = AbrangenciaEnum.O;
		}		
		return inAbrangencia;
	}

	public void setInAbrangencia(AbrangenciaEnum inAbrangencia) {
		this.inAbrangencia = inAbrangencia;
	}	
	
	
	/**
	 * PJEII-4428 
	 * Retorna corretamente a data final quando o periodo for repetido anualmente. 
	 * Ao repetir anualmente, no banco nao é registrado o ano da data
	 * ao exibir, retorna o ano corrente, mas quando o periodo começa no final de um
	 * ano e termina no começo de outro, como acontece no recesso judiciario no final
	 * do ano, a data final adiciona um ano, mas não armazena o ano no banco.
	 * Este metodo é somente-leitura.
	 * @author jose.borges@tst.jus.br
	 */
	public Date getDataEventoFim() {
		CalendarioEvento ce = getInstance();
		// se for período com repetição e a data final for anterior à data
		// inicial, adicionar um ano para a data final
		if (ce.getDataEvento() != null && ce.getDataEventoFim() != null) {
			if (ce.getDataEventoFim().compareTo(ce.getDataEvento()) < 0) {
				if ((ce.getDtDiaFinal() != null)
						&& (ce.getDtMesFinal() != null)) {
					GregorianCalendar dtAgora = new GregorianCalendar();
					int anoAtual = dtAgora.get(GregorianCalendar.YEAR);
					GregorianCalendar dtEvtFinal = new GregorianCalendar();
					dtEvtFinal.set(GregorianCalendar.DAY_OF_MONTH,
							ce.getDtDiaFinal());
					dtEvtFinal.set(GregorianCalendar.MONTH,
							ce.getDtMesFinal() - 1);
					dtEvtFinal.set(
							GregorianCalendar.YEAR,
							ce.getDtAnoFinal() == null ? anoAtual + 1 : ce
									.getDtAnoFinal());
					return dtEvtFinal.getTime();
				}
			}
		}
		return ce.getDataEventoFim();
	}
   
   /** PJEII-4428
    * Este metodo foi criado para possivel necessidade de um método set ao
    * método getDataEventoFim 
    * @see CalendarioEventoHome.getDataEventoFim
    */
	public void setDataEventoFim(Date def) {
		getInstance().setDataEventoFim(def);
	}
}
