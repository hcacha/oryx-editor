package de.hpi.epc.stepthrough;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.hpi.bpt.hypergraph.abs.IGObject;
import de.hpi.bpt.process.epc.ControlFlow;
import de.hpi.bpt.process.epc.Event;
import de.hpi.bpt.process.epc.IControlFlow;
import de.hpi.bpt.process.epc.IEPC;
import de.hpi.bpt.process.epc.IFlowObject;
import de.hpi.bpt.process.epc.INode;
import de.hpi.diagram.stepthrough.IStepThroughInterpreter;
import de.hpi.epc.Marking;
import de.hpi.epc.Marking.NodeNewMarkingPair;
import de.hpi.petrinet.stepthrough.AutoSwitchLevel;

public class EPCStepThroughInterpreter implements IStepThroughInterpreter {

	List<NodeNewMarkingPair> nodeNewMarkings;
	// A list of nodes that have changed because an object has been fired
	List<IGObject> changedObjects;

	IEPC epcDiag;

	public EPCStepThroughInterpreter(IEPC epcDiag) {
		this.epcDiag = epcDiag;

		Marking marking = new Marking();
		for (IControlFlow edge : epcDiag.getControlFlow()) {
			marking.applyContext(edge, Marking.Context.WAIT);
			marking.applyState(edge, Marking.State.NEG_TOKEN);
		}
		
		for (IFlowObject node : epcDiag.getFlowObjects()) {
			if (epcDiag.getIncomingControlFlow(node).size() == 0) {
				marking.applyState(epcDiag.getOutgoingControlFlow(node).iterator().next(),
						Marking.State.POS_TOKEN);
			}
		}


		nodeNewMarkings = marking.propagate(epcDiag);

		changedObjects = new LinkedList<IGObject>();
		changedObjects.addAll(getFireableNodes());
	}

	public void clearChangedObjs() {
		changedObjects.clear();
	}

	public boolean fireObject(String resourceId) {
		// For firing or-splits, resourceId looks like as follows:
		// orSplitId#arc1Id,arc2Id
		List<String> enabledOrSplitArcs = null;
		if(resourceId.indexOf("#") != -1){
			enabledOrSplitArcs = Arrays.asList(resourceId.split("#")[1].split(","));
			resourceId = resourceId.split("#")[0];
		}
		
		for (NodeNewMarkingPair nodeNewMarking : nodeNewMarkings) {
			IFlowObject node = nodeNewMarking.node;
			boolean fire = false; // only fire current marking, fire = true
			List<IGObject> changedObjects = new LinkedList<IGObject>();
			List<NodeNewMarkingPair> nodeNewMarkings = new LinkedList<NodeNewMarkingPair>();
			
			if (node.getId().equals(resourceId)) {
				if(enabledOrSplitArcs == null){//no or split
					changedObjects.add(node);
					nodeNewMarkings = nodeNewMarking.newMarking.propagate(epcDiag);
					fire = true;
				} else if(Marking.isOrConnector(node) && Marking.isSplit(node, epcDiag)){
					// Determine, if current newMarking is this marking which leads exactly
					// to fired control flow in enabledOrSplitArcs
					boolean isRightMarking = true;
					for(IControlFlow cf : epcDiag.getOutgoingControlFlow(node)){
						
						if(		// If cf has token but shouldn't have one
								(nodeNewMarking.newMarking.hasToken(cf) && !enabledOrSplitArcs.contains(cf.getId()))
								// If cf has no token but should have one
								|| (!nodeNewMarking.newMarking.hasToken(cf) && enabledOrSplitArcs.contains(cf.getId()))){
							isRightMarking = false;
							changedObjects.clear();
							break;
						// If cf has token and should have one
						} else if (nodeNewMarking.newMarking.hasToken(cf) && enabledOrSplitArcs.contains(cf.getId())) {
							changedObjects.add(cf);
						}
					}
					
					if(isRightMarking){
						nodeNewMarkings = nodeNewMarking.newMarking.propagate(epcDiag);
						fire = true;
					}
				}
			// If Xor Split is enabled, look for if current marking is for given edge
			} else if (Marking.isXorConnector(node)) {
				for(IControlFlow edge : epcDiag.getOutgoingControlFlow(node)){
					if(edge.getId().equals(resourceId) &&
							nodeNewMarking.newMarking.hasToken(edge)){
						// edge and not xor split changed
						changedObjects.add(edge);
						nodeNewMarkings = nodeNewMarking.newMarking.propagate(epcDiag);
						fire = true;
					}
				}
			}
			
			if(fire){
				this.changedObjects.addAll(changedObjects);
				this.nodeNewMarkings = nodeNewMarkings;
				fireMarking(nodeNewMarking.newMarking);
				return true;
			}
		}
		
		return false;
	}
	
	/* Fire given marking. Normally, marking.node would be added to changedObjects,
	 * but by giving a change object directly, this behavior can be avoided. 
	 * This method performs automatic execution of Events, XOR-Joins and AND-Connectors,
	 * depending on auto swtich level
	 */
	//TODO implement auto switch level
	protected void fireMarking(Marking marking){
		boolean changed = true;
		
		// Perform some automatic execution of AND connectors, XOR/ OR-joins and events
		while(changed && this.getFireableNodes().size() > 0){
			NodeNewMarkingPair markingPairToFire = null;
			for(NodeNewMarkingPair nodeNewMarking : nodeNewMarkings){
				IFlowObject node = nodeNewMarking.node;
				if( Marking.isAndConnector(node) ||
						node instanceof Event ||
						(Marking.isJoin(node, epcDiag) && (Marking.isXorConnector(node)) || Marking.isOrConnector(node)) ){
					markingPairToFire = nodeNewMarking;
					break; //leave loop because markings have changed
				}
			}
			changed = (markingPairToFire != null);
			
			if(markingPairToFire != null){
				changedObjects.add(markingPairToFire.node);
				nodeNewMarkings = markingPairToFire.newMarking.propagate(epcDiag);
			}
		}
		
		changedObjects.addAll(getFireableNodes());
	}

	protected List<INode> getFireableNodes() {
		List<INode> list = new LinkedList<INode>();
		for (NodeNewMarkingPair nodeNewMarking : nodeNewMarkings) {
			list.add(nodeNewMarking.node);
		}
		return list;
	}

	public String getChangedObjsAsString() {
		StringBuilder sb = new StringBuilder(15 * changedObjects.size());
		List<INode> fireableObjects = getFireableNodes();

		// Start with the transitions
		Map<IFlowObject, List<IControlFlow>> changedSplitConnectors = new HashMap<IFlowObject, List<IControlFlow>>(); 
		for (IGObject object : changedObjects) {
			// TODO times executed: node.getTimesExecuted()
			// if xor split is enabled (otherwise there would be only his controlflows)
			if(Marking.isXorConnector(object) && Marking.isSplit((IFlowObject)object, epcDiag)){
				for(IControlFlow edge : epcDiag.getOutgoingControlFlow((IFlowObject)object)){
					sb.append(buildChangedObjsString(edge.getId(), 1, true));
				}
			} else {
				sb.append(buildChangedObjsString(object.getId(), 1, fireableObjects.contains(object)));
			}
			
			if(object instanceof ControlFlow) {
				IFlowObject source = ((ControlFlow)object).getSource();
				if(!changedSplitConnectors.containsKey(source)){
					changedSplitConnectors.put(source, new LinkedList<IControlFlow>());
				}
				changedSplitConnectors.get(source).add((ControlFlow)object);
			}
		}
		
		// Deactivate outgoing control flow of connectors
		for(IFlowObject node : changedSplitConnectors.keySet()){
			sb.append(buildChangedObjsString(node.getId(), 0, false));
			for(IControlFlow cf : epcDiag.getOutgoingControlFlow(node)){
				if(!changedSplitConnectors.get(node).contains(cf)){
					sb.append(buildChangedObjsString(cf.getId(), 0, false));
				}
			}
		}

		return sb.toString();
	}

	public void setAutoSwitchLevel(AutoSwitchLevel autoSwitchLevel) {
		// do nothing
	}
	
	//TODO this should be provided by superclass
	private String buildChangedObjsString(String resourceId, int timesExecuted, boolean fireable){
		return resourceId + "," + String.valueOf(timesExecuted) + "," + (fireable ? "t" : "f") + ";";
	}

}