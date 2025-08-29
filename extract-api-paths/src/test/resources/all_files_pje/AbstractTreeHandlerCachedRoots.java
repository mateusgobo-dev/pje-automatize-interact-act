package br.com.infox.cliente.component.tree;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;

// Implementação para evitar que seja feita N + 1 consultas ao carregar o componente de 
// tela que mostra as árvores. 1 corresponde à consulta das entidades raízes e N às
// consultas dos filhos para cada nó raiz. Para essa classe abstrata ter efeito, é necessário
// que o getQueryRoots() utilize o fetch eager (como é usado no AssuntoTrfTreeHandler).
//
// Vale ressaltar que o problema de N + 1 continua ao explorar as árvores. Resta clara a importância
// de se criar uma arquitetura de Tree mais eficiente.
public abstract class AbstractTreeHandlerCachedRoots<T> extends AbstractTreeHandler<T>{

	private static final long serialVersionUID = -1286324926823463934L;

	@Override
	public List<EntityNode<T>> getRoots(){
		if (this.rootList == null){
			Query queryRoots = getEntityManager().createQuery(getQueryRoots());
			EntityNode<T> entityNode = createNode();
			entityNode.setIgnore(getEntityToIgnore());
			this.rootList = entityNode.getRoots(queryRoots);
			criarFilhosDasRaizes();
		}
		return this.rootList;
	}

	// Evitar N + 1 consultas somente para mostrar o tree, ainda persiste o problema do N + 1 
	// ao expandir a árvore, mas diminui o tempo de abertura de tela
	private void criarFilhosDasRaizes(){
		
		for (EntityNode<T> node : this.rootList){
			node.setNodes(new ArrayList<EntityNode<T>>());
			
			List<EntityNode<T>> nodes = new ArrayList<EntityNode<T>>();
			for (T filho : getChildren(node.getEntity())){
				EntityNode<T> noFilho = new EntityNode<T>(node, filho, node.getQueryChildren());;
				nodes.add(noFilho);
			}
			
			node.getNodes().addAll(nodes);
		}
	}
	
	protected abstract List<T> getChildren(T entity);
	
}
