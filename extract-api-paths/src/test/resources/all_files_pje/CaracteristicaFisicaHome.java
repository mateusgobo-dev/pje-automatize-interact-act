package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.CaracteristicaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.enums.CaracteristicaFisicaEnum;
import br.jus.pje.nucleo.enums.TipoCaracteristicaFisicaEnum;

@Name("caracteristicaFisicaHome")
public class CaracteristicaFisicaHome extends AbstractHome<CaracteristicaFisica> {
	
	private static final long serialVersionUID = 3889933584393176822L;
	
	private TipoCaracteristicaFisicaEnum tipoCaracteristicaFisica;
	private TipoCaracteristicaFisicaEnum[] tiposCaracteristicasFisicas = TipoCaracteristicaFisicaEnum.values();
	private List<CaracteristicaFisica> caracteristicasFisicas = new ArrayList<CaracteristicaFisica>(0);
	
	
	public TipoCaracteristicaFisicaEnum getTipoCaracteristicaFisica() {
		return tipoCaracteristicaFisica;
	}
	
	public void setTipoCaracteristicaFisica(TipoCaracteristicaFisicaEnum tipoCaracteristicaFisica) {
		this.tipoCaracteristicaFisica = tipoCaracteristicaFisica;
	}
	
	public TipoCaracteristicaFisicaEnum[] getTiposCaracteristicasFisicas() {
		return tiposCaracteristicasFisicas;
	}
	
	public void setTiposCaracteristicasFisicas(TipoCaracteristicaFisicaEnum[] tiposCaracteristicasFisicas) {
		this.tiposCaracteristicasFisicas = tiposCaracteristicasFisicas;
	}
	
	public List<CaracteristicaFisica> getCaracteristicasFisicas() {
		return caracteristicasFisicas;
	}
	
	public void setCaracteristicasFisicas(List<CaracteristicaFisica> caracteristicasFisicas) {
		this.caracteristicasFisicas = caracteristicasFisicas;
	}
	
	public void carregarCaracteristicasPessoais(){
		if (getInstance() != null && getInstance().getPessoaFisica() != null
				&& getInstance().getPessoaFisica().getIdUsuario() != null) {
			String hql = " select o from CaracteristicaFisica o "+
		                 " where o.pessoaFisica.idUsuario = :idUsuario ";
			Query qry = getEntityManager().createQuery(hql);
			qry.setParameter("idUsuario", getPessoaFisica().getIdUsuario());
			caracteristicasFisicas = qry.getResultList();
		}
	}
	
	public boolean isTipoCaracteristicaCadastrado(TipoCaracteristicaFisicaEnum tipo){
		if(getInstance() != null){
			for(CaracteristicaFisica aux : getInstance().getPessoaFisica().getCaracteristicasFisicas()){
				if(aux.getCaracteristicaFisica().getTipoCaracteristicaFisica() == tipo){
					return true;
				}
			}
		}
		return false;
	}
	
	private PessoaFisica getPessoaFisica(){
		PessoaFisica pessoaFisica = ((PessoaFisicaHome)getComponent("pessoaFisicaHome")).getInstance();
		
		return pessoaFisica.getIdUsuario() != null ?
			pessoaFisica : ((PessoaAdvogadoHome)getComponent("pessoaAdvogadoHome")).getInstance().getPessoa();
	}
	
	@Override
	public void newInstance() {
		super.newInstance();
		this.tipoCaracteristicaFisica = null;
		getInstance().setPessoaFisica(getPessoaFisica());
		carregarCaracteristicasPessoais();
	}	
	
	@Override
	protected boolean beforePersistOrUpdate() {
		if(getInstance().getId() == null){			
			getInstance().setPessoaFisica(getPessoaFisica());
		}
		
		return ((super.beforePersistOrUpdate()) && 
				(!isTipoCaracteristicaCadastrado(getInstance().getCaracteristicaFisica().getTipoCaracteristicaFisica())));
	}
	
	@Override
	public String persist() {
		String result = null;
		
		if(possuiCaracteristica(getInstance().getCaracteristicaFisica())) {
			FacesMessages.instance().add(Severity.INFO, "Registro já cadastrado.");
		} else {
			result = super.persist();
			newInstance();
		}

		return result;
	}
	
	/**
	 * Método responsável por verificar se o usuário possui determinada característica física.
	 * 
	 * @param caracteristicaFisica Característica física.
	 * @return Verdadeiro caso o usuário possua a característica. Falso, caso contrário.
	 */
	private boolean possuiCaracteristica(CaracteristicaFisicaEnum caracteristicaFisica){
		StringBuilder jpql = new StringBuilder("SELECT o FROM CaracteristicaFisica o ")
			.append("WHERE o.pessoaFisica.idUsuario = :idUsuario ")
			.append("AND o.caracteristicaFisica = :caracteristicaFisica");
		
		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("idUsuario", getPessoaFisica().getIdUsuario());
		query.setParameter("caracteristicaFisica", caracteristicaFisica);
		
		return !query.getResultList().isEmpty();
	}
	
	@Override
	public String remove(CaracteristicaFisica obj) {
		String result = super.remove(obj);
		newInstance();
		return result;
	}
}
