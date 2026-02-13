package br.com.infox.ibpm.util;

import java.util.List;

import br.jus.pje.nucleo.entidades.log.EntityLog;

public abstract class AbstractGerenciadorCache {
	public abstract void execute(List<EntityLog> logs);
}
