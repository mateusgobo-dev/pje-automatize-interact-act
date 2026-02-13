package br.com.infox.jbpm.layout;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.jboss.seam.core.Expressions;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.jbpm.layout.cell.JbpmDefaultCell;
import br.jus.cnj.pje.servicos.NoDeDesvioService;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

public class JbpmLayout {
	private ProcessDefinition processDefinition;
	private String map;
	private JGraph graph;
	private Map<Node, TaskInstance> taskInstanceMap;

	public JbpmLayout(ProcessDefinition processDefinition) {
		this.processDefinition = processDefinition;
	}

	public JbpmLayout(ProcessDefinition processDefinition,
			Map<Node, TaskInstance> taskInstanceMap) {
		this.processDefinition = processDefinition;
		this.taskInstanceMap = taskInstanceMap;
	}

	public void paint(OutputStream out) throws IOException {
		if (this.graph == null) {
			makeGraph();
		}
		ImageIO.write(Util.toImage(this.graph), "png", out);
		makeMap();
	}

	public void drawGraph(File file) throws Exception {
		JGraph graph = makeGraph();
		file.getParentFile().mkdirs();
		ImageIO.write(Util.toImage(graph), "png", file);
	}

	private String makeMap() {
		if (this.graph == null) {
			makeGraph();
		}
		if (this.map != null) {
			return this.map;
		}
		StringBuilder sb = new StringBuilder();
		for (CellView view : this.graph.getGraphLayoutCache().getCellViews()) {
			if ((view.getCell() instanceof JbpmDefaultCell)) {
				JbpmDefaultCell cell = (JbpmDefaultCell) view.getCell();
				Rectangle2D r = view.getBounds();
				sb.append("nodes.addToMap('").append(cell.getNodeIndex())
						.append("','").append(r.getMinX() + 5.0D).append(",")
						.append(r.getMinY() + 5.0D).append(",")
						.append(r.getMaxX() + 5.0D).append(",")
						.append(r.getMaxY() + 5.0D).append("', '")
						.append(cell.getNode().getName()).append("'");
				if (cell.isTaskNode()) {
					TaskNode node = (TaskNode) cell.getNode();
					sb.append(",{");
					for (Task t : node.getTasks()) {
						if (t.getTaskController() != null) {
							List<VariableAccess> list = t.getTaskController()
									.getVariableAccesses();
							getTaskInfo(sb, list);
						}
					}
					sb.append("}");
				}
				sb.append(");\n");
			}
		}
		return sb.toString();
	}

	private void getTaskInfo(StringBuilder sb, List<VariableAccess> list) {
		for (int i = 0; (list != null) && (i < list.size()); i++) {
			VariableAccess v = (VariableAccess) list.get(i);
			String[] mappedName = v.getMappedName().split(":");
			String name;
			if (mappedName.length == 1)
				name = "";
			else {
				name = mappedName[1];
			}
			String exp = "#{processBuilder.getTypeLabel('" + mappedName[0]
					+ "')}";
			String component = (String) Expressions.instance()
					.createValueExpression(exp).getValue();
			sb.append(i).append(": {name:'")
					.append((String) JbpmUtil.getJbpmMessages().get(name))
					.append("', type:'").append(component)
					.append("', readonly:'").append(!v.isWritable())
					.append("'}");
			if (i < list.size() - 1)
				sb.append(",");
		}
	}

	public String getMap() {
		if (this.map == null) {
			this.map = makeMap();
		}
		return this.map;
	}

	private JGraph makeGraph() {
		GraphModel model = new DefaultGraphModel();
		this.graph = new JbpmGraph(model);

		this.graph.setCloneable(true);

		this.graph.setGridEnabled(true);
		this.graph.setGridVisible(true);

		this.graph.setInvokesStopCellEditing(true);

		this.graph.setJumpToDefaultPort(true);

		List<DefaultGraphCell> cellList = new ArrayList<DefaultGraphCell>();
		Map<Node, DefaultGraphCell> nodes = new HashMap<Node, DefaultGraphCell>();
		this.processDefinition.getNodes();
		insertNodes(cellList, nodes);

		this.graph.getGraphLayoutCache().insert(cellList.toArray());

		performLayout();

		Object[] roots = this.graph.getRoots();

		JGraphFacade facade = new JGraphFacade(this.graph, roots);

		JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();

		layout.setDeterministic(true);

		layout.run(facade);
		Map nested = facade.createNestedMap(true, true);

		this.graph.getGraphLayoutCache().edit(nested);

		JPanel panel = new JPanel();
		panel.setDoubleBuffered(false);
		panel.add(this.graph);
		panel.setVisible(true);
		panel.setEnabled(true);
		panel.addNotify();
		panel.validate();
		return this.graph;
	}

	private void insertNodes(List<DefaultGraphCell> cellList,
			Map<Node, DefaultGraphCell> nodes) {
		String nomeNoDesvio = NoDeDesvioService.getNomeNoDesvio(processDefinition);

		if (this.taskInstanceMap == null) {
			for (Node from : this.processDefinition.getNodes()) {
				if (from.getName().equals(nomeNoDesvio)) {
					continue;
				}
				DefaultGraphCell vertexFrom = addNode(from, nodes, cellList);
				List<Transition> transitions = from.getLeavingTransitions();
				if (transitions != null)
					for (Transition t : transitions) {
						if (t.getName().equals(nomeNoDesvio)) {
							continue;
						}
						Node to = t.getTo();
						DefaultGraphCell vertexTo = addNode(to, nodes, cellList);
						addEdge(vertexFrom, vertexTo, cellList);
					}
			}
		} else
			for (Map.Entry e : this.taskInstanceMap.entrySet()) {
				Node from = (Node) e.getKey();
				if (from.getName().equals(nomeNoDesvio)) {
					continue;
				}
				DefaultGraphCell vertexFrom = addNode(from, nodes, cellList);
				List<Transition> transitions = from.getLeavingTransitions();
				if (transitions != null)
					for (Transition t : transitions) {
						if (t.getName().equals(nomeNoDesvio)) {
							continue;
						}
						Node to = t.getTo();
						if (this.taskInstanceMap.containsKey(to)) {
							if (((TaskInstance) e.getValue()).getEnd() == null)
								break;
							DefaultGraphCell vertexTo = addNode(to, nodes,
									cellList);
							addEdge(vertexFrom, vertexTo, cellList);

							break;
						}
					}
			}
	}

	private void performLayout() {
		SugiyamaLayoutAlgorithm layout = new SugiyamaLayoutAlgorithm();
		Properties p = new Properties();
		p.put("HorizontalSpacing", "300");
		p.put("VerticalSpacing", "100");
		layout.perform(this.graph, true, p);
	}

	private DefaultGraphCell addNode(Node node,
			Map<Node, DefaultGraphCell> nodes, List<DefaultGraphCell> cellList) {
		DefaultGraphCell vertexFrom = (DefaultGraphCell) nodes.get(node);
		if (vertexFrom == null) {
			TaskInstance ti = null;
			if (this.taskInstanceMap != null) {
				ti = (TaskInstance) this.taskInstanceMap.get(node);
			}
			vertexFrom = new JbpmDefaultCell(node, ti);
			nodes.put(node, vertexFrom);
			cellList.add(vertexFrom);
		}
		return vertexFrom;
	}

	private void addEdge(DefaultGraphCell from, DefaultGraphCell to,
			List<DefaultGraphCell> cellList) {
		DefaultEdge edge = new DefaultEdge();
		edge.setSource(from.getFirstChild());
		if (to != null) {
			edge.setTarget(to.getFirstChild());
		}
		cellList.add(edge);
		int arrow = 2;
		GraphConstants.setLineEnd(edge.getAttributes(), arrow);
		GraphConstants.setEndFill(edge.getAttributes(), true);

		GraphConstants.setLineStyle(edge.getAttributes(), 12);
	}

	public static void main(String[] args) throws Exception {
		String path = "/home/luiz/processdefinition.xml";
		ProcessDefinition pd = ProcessDefinition
				.parseXmlInputStream(new FileInputStream(path));

		Map<Node, TaskInstance> taskInstanceMap = new HashMap<Node, TaskInstance>();
		Map<String, Task> tasks = pd.getTaskMgmtDefinition().getTasks();
		for (Map.Entry e : tasks.entrySet()) {
			TaskInstance ti = new TaskInstance(((Task) e.getValue()).getName());
			taskInstanceMap.put(((Task) e.getValue()).getTaskNode(), ti);
			ti.setCreate(new Date());
			ti.setStart(new Date());
			System.out.println(ti);
			if (taskInstanceMap.size() == 4) {
				break;
			}
			ti.setEnd(new Date());
		}

		JbpmLayout jbpmLayout = new JbpmLayout(pd, taskInstanceMap);
		jbpmLayout.drawGraph(new File("/home/luiz/processImage.png"));
	}
}