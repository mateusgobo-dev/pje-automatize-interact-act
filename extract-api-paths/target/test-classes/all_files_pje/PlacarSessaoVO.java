package br.jus.cnj.pje.vo;

import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by thiago on 30/09/16.
 */
public class PlacarSessaoVO {

    private Map<Integer, Set<Integer>> mapaPlacar;
    private Map<Integer,String> mapaCor;
	
	private final Integer idSessao;
	private final Integer idProcesso;
	
	public PlacarSessaoVO() {	
		this(0, 0);
	}

	public PlacarSessaoVO(Integer idSessao, Integer idProcesso) {		
		this.idSessao = idSessao;
		this.idProcesso = idProcesso;
	}

	public PlacarSessaoVO(Sessao sessao, ProcessoTrf processo) {		
		this.idSessao = sessao==null ? 0 : sessao.getEntityIdObject();
		this.idProcesso = processo==null ? 0 : processo.getIdProcessoTrf();
	}

    public Map<Integer, Set<Integer>> getMapaPlacar() {
        return mapaPlacar;
    }

    public void setMapaPlacar(Map<Integer, Set<Integer>> mapaPlacar) {
        this.mapaPlacar = mapaPlacar;
    }

    public Map<Integer, String> getMapaCor() {
        return mapaCor;
    }

    public void setMapaCor(Map<Integer, String> mapaCor) {
        this.mapaCor = mapaCor;
    }

	public int getIdProcesso() {
		return idProcesso;
	}

	@Override
	public String toString() {
		return "PlacarSessaoVO{" + "idSessao=" + idSessao + ", idProcesso=" + idProcesso + '}';
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 13 * hash + Objects.hashCode(this.idSessao);
		hash = 13 * hash + Objects.hashCode(this.idProcesso);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PlacarSessaoVO other = (PlacarSessaoVO) obj;
		if (!Objects.equals(this.idSessao, other.idSessao)) {
			return false;
		}
		if (!Objects.equals(this.idProcesso, other.idProcesso)) {
			return false;
		}
		return true;
	}

	
}
