package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.faces.model.SelectItem;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.CondicaoSuspensao;
import br.jus.pje.nucleo.entidades.TipoSuspensao;
import br.jus.pje.nucleo.util.DateUtil;

@Name("tipoSuspensaoHome")
@BypassInterceptors
public class TipoSuspensaoHome extends AbstractHome<TipoSuspensao>{

	private static final String PRAZO_SUSPENSAO_DIA = "D";

	private static final String PRAZO_SUSPENSAO_MES = "M";

	private static final String PRAZO_SUSPENSAO_ANO = "A";

	private static final long serialVersionUID = 1L;
	private Integer page;

	private CondicaoSuspensao condicaoSuspensao = new CondicaoSuspensao();
	List<String> prazoSuspensaoSelectedList;

	public void setTipoSuspensaoIdTipoSuspensao(Integer id){
		setId(id);
	}

	public Integer getTipoSuspensaoIdTipoSuspensao(){
		return (Integer) getId();
	}

	public static TipoSuspensaoHome instance(){
		return ComponentUtil.getComponent("tipoSuspensaoHome");
	}

	public CondicaoSuspensao getCondicaoSuspensao(){
		return condicaoSuspensao;
	}

	public List<CondicaoSuspensao> getCondicaoSuspensaoList(){
		return getInstance().getCondicoesParaSuspensao();
	}

	public void setCondicaoSuspensao(CondicaoSuspensao condicaoSuspensao){
		this.condicaoSuspensao = condicaoSuspensao;
	}

	public List<String> getPrazoSuspensaoSelectedList(){
		prazoSuspensaoSelectedList = new ArrayList<String>();
		if (isManaged()){
			if (getInstance().getPrazoSuspencaoAno() != null && getInstance().getPrazoSuspencaoAno()){
				prazoSuspensaoSelectedList.add(PRAZO_SUSPENSAO_ANO);
			}
			if (getInstance().getPrazoSuspencaoMes() != null && getInstance().getPrazoSuspencaoMes()){
				prazoSuspensaoSelectedList.add(PRAZO_SUSPENSAO_MES);
			}
			if (getInstance().getPrazoSuspencaoDia() != null && getInstance().getPrazoSuspencaoDia()){
				prazoSuspensaoSelectedList.add(PRAZO_SUSPENSAO_DIA);
			}
		}

		return prazoSuspensaoSelectedList;
	}

	@Override
	public String persist(){
		instance.setAtivo(true);
		return super.persist();
	}

	@Override
	public String inactive(TipoSuspensao instance){
		try{
			verificaIntegridadeReferencialLogica(instance);
			inativarCondicoes(instance);
			return super.inactive(instance);
		} catch (Exception e){
			addFacesMessageFromResourceBundle("tipoSuspensao.registro_referenciado");
		}
		return "";
	}

	private void inativarCondicoes(TipoSuspensao instance) {
		String queryString = "select c from CondicaoSuspensao c where  c.ativo = true and c.tipoSuspensao = :tipo ";
		Query query = getEntityManager().createQuery(queryString);
		query.setParameter("tipo", instance);
		List<CondicaoSuspensao> condicoes = query.getResultList();
		if (condicoes != null) {
			for(CondicaoSuspensao c: condicoes) {
				CondicaoSuspencaoHome.instance().inactive(c);
			}
		}		
	}

	private void verificaIntegridadeReferencialLogica(TipoSuspensao obj) throws Exception{
		//String queryString = "select c.tipoSuspensao from CondicaoSuspensao c where  c.ativo = true and c.tipoSuspensao = :tipo ";
		String queryString = /* Union */"select icr.tipoSuspensao from IcrSuspensao icr where  icr.ativo = true and icr.tipoSuspensao = :tipo ";
		Query query = getEntityManager().createQuery(queryString);
		query.setParameter("tipo", obj);

		if (!query.getResultList().isEmpty()){
			throw new Exception("Este Tipo de Suspensao está sendo referenciado por outras entidades ativas no sistema.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean beforePersistOrUpdate(){
		// verificar data inicial maior que data final
		if (getInstance().getDataTerminoVigencia() != null){
			if (DateUtil.isDataMenor(getInstance().getDataTerminoVigencia(), getInstance().getDataInicioVigencia())){
				addFacesMessageFromResourceBundle("tipoSuspensao.dataInicial_maior_dataFinal");
				return false;
			}
		}

		// verificar se registro já cadastrado
		String queryString = "from TipoSuspensao ts where lower(ts.descricao) = lower(:descricao) and  ts.ativo = true ";
		if (isManaged()){
			queryString += "and ts.id !=:id ";
		}

		Query query = getEntityManager().createQuery(queryString);
		query.setParameter("descricao", getInstance().getDescricao());

		if (isManaged()){
			query.setParameter("id", getInstance().getId());
		}

		List<TipoSuspensao> tiposEncontrados = query.getResultList();
		if (!tiposEncontrados.isEmpty()){
			Calendar calendar = Calendar.getInstance();
			// data substituta para os casos em que a data de termino == null
			calendar.set(7000, 11, 1, 0, 0, 0);
			Date dataFutura = calendar.getTime();
			Date dataInicioInstance = getInstance().getDataInicioVigencia();
			Date dataTerminoInstance = getInstance().getDataTerminoVigencia() == null ? dataFutura : getInstance()
					.getDataTerminoVigencia();
			for (TipoSuspensao tipoSuspensao : tiposEncontrados){
				Date dataInicioItem = tipoSuspensao.getDataInicioVigencia();
				Date dataTerminoItem = tipoSuspensao.getDataTerminoVigencia() == null ? dataFutura : tipoSuspensao
						.getDataTerminoVigencia();
				if (DateUtil.isDataEntre(dataInicioInstance, dataInicioItem, dataTerminoItem)
					|| DateUtil.isDataEntre(dataTerminoInstance, dataInicioItem, dataTerminoItem)
					|| DateUtil.isDataEntre(dataInicioItem, dataInicioInstance, dataTerminoInstance)
					|| DateUtil.isDataEntre(dataTerminoItem, dataInicioInstance, dataTerminoInstance)){
					addFacesMessageFromResourceBundle("tipoSuspensao.registro_ja_cadastrado");
					return false;
				}
			}
		}

		return true;
	}

	public void setPrazoSuspensaoSelectedList(List<String> prazoSuspensaoSelectedList){
		getInstance().setPrazoSuspencaoAno(false);
		getInstance().setPrazoSuspencaoMes(false);
		getInstance().setPrazoSuspencaoDia(false);
		this.prazoSuspensaoSelectedList = prazoSuspensaoSelectedList;
		if (prazoSuspensaoSelectedList != null){
			for (String item : prazoSuspensaoSelectedList){
				if (item.equals(PRAZO_SUSPENSAO_ANO)){
					getInstance().setPrazoSuspencaoAno(true);
				}
				if (item.equals(PRAZO_SUSPENSAO_MES)){
					getInstance().setPrazoSuspencaoMes(true);
				}
				if (item.equals(PRAZO_SUSPENSAO_DIA)){
					getInstance().setPrazoSuspencaoDia(true);
				}
			}
		}
	}

	public List<SelectItem> getPrazoSuspensaoList(){
		List<SelectItem> prazoSuspensaoList = new ArrayList<SelectItem>();
		prazoSuspensaoList.add(new SelectItem(PRAZO_SUSPENSAO_ANO, "Ano"));
		prazoSuspensaoList.add(new SelectItem(PRAZO_SUSPENSAO_MES, "Mês"));
		prazoSuspensaoList.add(new SelectItem(PRAZO_SUSPENSAO_DIA, "Dia"));
		return prazoSuspensaoList;
	}

	public void adicionarCondicaoSuspensao(){
		getCondicaoSuspensao().setTipoSuspensao(getInstance());
		if (verificaDuplicidade(getCondicaoSuspensao())){
			addFacesMessageFromResourceBundle("CondicaoSuspensao_duplicada");
			setCondicaoSuspensao(new CondicaoSuspensao());
			return;
		}
		getEntityManager().merge(getCondicaoSuspensao());
		getEntityManager().flush();
		getEntityManager().refresh(getInstance());
		setCondicaoSuspensao(new CondicaoSuspensao());
		addFacesMessageFromResourceBundle("CondicaoSuspensao_created");
	}

	public void alterarCondicaoSuspensao(){
		getCondicaoSuspensao().setTipoSuspensao(getInstance());
		getEntityManager().merge(getCondicaoSuspensao());
		getEntityManager().flush();
		getEntityManager().refresh(getInstance());
		setCondicaoSuspensao(new CondicaoSuspensao());
		addFacesMessageFromResourceBundle("CondicaoSuspensao_updated");
	}

	public void removerCondicaoSuspensao(CondicaoSuspensao condicaoSuspensao){
		condicaoSuspensao.setAtivo(false);
		getEntityManager().merge(condicaoSuspensao);
		getEntityManager().flush();
		getEntityManager().refresh(getInstance());
		addFacesMessageFromResourceBundle("CondicaoSuspensao_deleted");
	}

	public boolean verificaDuplicidade(CondicaoSuspensao condicaoSuspensao){
		for (CondicaoSuspensao condicao : getCondicoesSuspensaoAtivas(condicaoSuspensao.getTipoSuspensao())){
			if (condicao.equals(condicaoSuspensao))
				return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public List<CondicaoSuspensao> getCondicoesSuspensaoAtivas(TipoSuspensao tipoSuspensao){
		Query query = getEntityManager().createQuery(
				"from CondicaoSuspensao o where o.ativo=true and o.tipoSuspensao = :tipo order by o.descricao ASC ");
		query.setParameter("tipo", tipoSuspensao);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<CondicaoSuspensao> consultaCondicoesSuspensao(){
		Query query = getEntityManager().createQuery("from CondicaoSuspensao o where o.tipoSuspensao.id = ? order by o.ativo DESC,o.descricao ASC ");
		query.setParameter(1, getInstance().getId());
		return query.getResultList();
	}

	public Integer getPage(){
		return page;
	}

	public void setPage(Integer page){
		this.page = page;
	}
}
