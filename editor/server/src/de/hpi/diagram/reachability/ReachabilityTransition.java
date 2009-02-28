package de.hpi.diagram.reachability;

import de.hpi.bpt.graph.abs.AbstractDirectedEdge;
import de.hpi.bpt.graph.abs.AbstractDirectedGraph;

public class ReachabilityTransition<FlowObject, Marking> extends AbstractDirectedEdge<ReachabilityNode<Marking>> {
	FlowObject flowObject;
	
	@SuppressWarnings("unchecked")
	protected ReachabilityTransition(AbstractDirectedGraph g, ReachabilityNode<Marking> source,
			ReachabilityNode<Marking> target) {
		super(g, source, target);
	}
	
	public FlowObject getFlowObject() {
		return flowObject;
	}

	public void setFlowObject(FlowObject flowObject) {
		this.flowObject = flowObject;
	}	
}