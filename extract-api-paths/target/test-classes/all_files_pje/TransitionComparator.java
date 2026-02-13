package br.jus.cnj.pje.util;

import java.util.Comparator;
import org.jbpm.graph.def.Transition;

public class TransitionComparator implements Comparator<Transition> {
	
	@Override
	public int compare(Transition o1, Transition o2) {
		return o1.getName().compareToIgnoreCase(o2.getName());
	}
}