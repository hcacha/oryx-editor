package de.hpi.bpmn2_0.transformation;

import java.lang.annotation.Annotation;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.Shape;

import de.hpi.bpmn2_0.factory.AbstractBpmnFactory;
import de.hpi.bpmn2_0.factory.BPMNElement;
import de.hpi.bpmn2_0.factory.annotations.StencilId;
import de.hpi.bpmn2_0.model.BpmnConnectorType;
import de.hpi.bpmn2_0.model.BpmnNode;
import de.hpi.bpmn2_0.model.Definitions;
import de.hpi.bpmn2_0.model.LaneCompartment;
import de.hpi.bpmn2_0.model.Process;
import de.hpi.bpmn2_0.model.ProcessDiagram;
import de.hpi.bpmn2_0.model.SequenceFlowConnector;
import de.hpi.bpmn2_0.model.TBaseElement;
import de.hpi.bpmn2_0.model.TFlowElement;
import de.hpi.bpmn2_0.model.TFlowNode;
import de.hpi.bpmn2_0.model.TSequenceFlow;
import de.hpi.util.reflection.ClassFinder;

/**
 * Converter class for Diagram to BPMN 2.0 transformation.
 * 
 * @author Sven Wagner-Boysen
 *
 */
public class Diagram2BpmnConverter {
	/* Hash map of factories for BPMN 2.0 element to enable lazy initialization */
	private HashMap<String, AbstractBpmnFactory> factories;
	private HashMap<String, BPMNElement> bpmnElements;
	private Diagram diagram;
	private final HashSet<String> edgeIds;
	
	public Diagram2BpmnConverter(Diagram diagram) {
		this.factories = new HashMap<String, AbstractBpmnFactory>();
		this.bpmnElements = new HashMap<String, BPMNElement>();
		this.diagram = diagram;
		
		/* Define edge ids */
		this.edgeIds = new HashSet<String>();
		this.edgeIds.add("SequenceFlow");
	}
	
	/**
	 * Retrieves the stencil id related hashed factory.
	 * 
	 * @param stencilId
	 * 		The stencil id
	 * @return
	 * 		The related factory
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private AbstractBpmnFactory getFactoryForStencilId(String stencilId) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		/* Create a new factory instance if necessary */
		if(!factories.containsKey(stencilId)) {
			this.factories.put(stencilId, createFactoryForStencilId(stencilId));
		}
		
		return this.factories.get(stencilId);
	}
	
	/**
	 * Creates a new factory instance for a stencil id.
	 * 
	 * @param stencilId
	 * 		The stencil id
	 * @return
	 * 		The created factory
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * 
	 */
	private AbstractBpmnFactory createFactoryForStencilId(String stencilId) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		List<Class<? extends AbstractBpmnFactory>> factoryClasses =
			ClassFinder.getClassesByPackageName(AbstractBpmnFactory.class, "de.hpi.bpmn2_0.factory");
		
		/* Find factory for stencil id */
		for(Class<? extends AbstractBpmnFactory> factoryClass : factoryClasses) {
			StencilId stencilIdA = (StencilId) factoryClass.getAnnotation(StencilId.class);
			if(stencilIdA != null && stencilIdA.value().equals(stencilId)) {
				return (AbstractBpmnFactory) factoryClass.newInstance();
			}
		}

		throw new ClassNotFoundException("Factory for stencil id not found!");
	}

	/**
	 * Secures uniqueness of an BPMN Element.
	 * @param el
	 * @throws InvalidKeyException
	 */
	private void addBpmnElement(BPMNElement el) throws InvalidKeyException {
		if(this.bpmnElements.containsKey(el.getId())) {
			throw new InvalidKeyException("Key already exists for BPMN element!");
		}
		
		this.bpmnElements.put(el.getId(), el);
	}
	
	/**
	 * Retrieves the edges and updates the source and target references.
	 */
	private void detectConnectors() {
		for(Shape shape : this.diagram.getShapes()) {
			if(!this.edgeIds.contains(shape.getStencilId())) {
				continue;
			}
			
			BPMNElement bpmnConnector = this.bpmnElements.get(shape.getResourceId());
			
			BPMNElement source = null;
			
			for(Shape incomingShape : shape.getIncomings()) {
				if(this.edgeIds.contains(incomingShape.getStencilId())) {
					continue;
				}
				
				source = this.bpmnElements.get(incomingShape.getResourceId());
				break;
			}
			
			BPMNElement target = (shape.getTarget() != null) ? this.bpmnElements.get(shape.getTarget().getResourceId()) : null ;
			
			/* Update source references */
			if(source != null) {
				TFlowNode sourceNode = (TFlowNode) source.getNode();
				sourceNode.getOutgoing().add((TFlowNode) bpmnConnector.getNode());
				
				TSequenceFlow seqFlow = (TSequenceFlow) bpmnConnector.getNode();
				seqFlow.setSourceRef(sourceNode);
				
				BpmnConnectorType seqConnector = (BpmnConnectorType) bpmnConnector.getShape();
				seqConnector.setSourceRef(source.getShape());
			}
			
			/* Update target references */
			if(target != null) {
				TFlowNode targetNode = (TFlowNode) target.getNode();
				targetNode.getIncoming().add((TFlowNode) bpmnConnector.getNode());
				
				TSequenceFlow seqFlow = (TSequenceFlow) bpmnConnector.getNode();
				seqFlow.setTargetRef(targetNode);
				
				BpmnConnectorType seqConnector = (BpmnConnectorType) bpmnConnector.getShape();
				seqConnector.setTargetRef(target.getShape());
			}
		}
	}
	
	/**
	 * Retrieves a BPMN 2.0 diagram and transforms it into the BPMN 2.0 model.
	 * 
	 * @param diagram
	 * 		The BPMN 2.0 {@link Diagram} based on the ORYX JSON.
	 * @return
	 * 		The definitions root element of the BPMN 2.0 model.
	 */
	public Definitions getDefinitionsFormDiagram() {
		/* Build-up standard definitions */
		Process process = new Process();
		process.setId("testProzess");
		LaneCompartment laneComp = new LaneCompartment();
		ProcessDiagram processDia = new ProcessDiagram();
		processDia.getLaneCompartment().add(laneComp);
		processDia.setProcessRef(process);
		
		Definitions definitions = new Definitions();
		definitions.getDiagram().add(processDia);
		definitions.getRootElement().add(process);
		
		/* Convert shapes to BPMN 2.0 elements */
		
		for(Shape shape : this.diagram.getChildShapes()) {
			try {
				AbstractBpmnFactory factory = 
					this.getFactoryForStencilId(shape.getStencilId());
				BPMNElement bpmnElement = factory.createBpmnElement(shape);
				
				this.addBpmnElement(bpmnElement);
				
				process.getFlowElement().add((TFlowElement) bpmnElement.getNode());
				
				Object shapeToAdd = bpmnElement.getShape();
				if(shapeToAdd instanceof BpmnNode) {
					laneComp.getBpmnShape().add((BpmnNode) shapeToAdd);
				} else if (shapeToAdd instanceof SequenceFlowConnector) {
					processDia.getSequenceFlowConnector().add((SequenceFlowConnector) shapeToAdd);
				}
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return definitions;
	}
	
	
}