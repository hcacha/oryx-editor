package de.hpi.bpmn2_0.factory;

/**
 * Copyright (c) 2009
 * Philipp Giese, Sven Wagner-Boysen
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.oryxeditor.server.diagram.Point;
import org.oryxeditor.server.diagram.Shape;

import de.hpi.bpmn2_0.factory.annotations.StencilId;
import de.hpi.bpmn2_0.model.SequenceFlowConnector;
import de.hpi.bpmn2_0.model.TBaseElement;
import de.hpi.bpmn2_0.model.TSequenceFlow;
import de.hpi.bpmn2_0.model.BpmnConnectorType.Bendpoint;

/**
 * @author Philipp Giese
 * @author Sven Wagner-Boysen
 *
 */
@StencilId("SequenceFlow")
public class SequenceFlowFactory extends AbstractBpmnFactory {

	/* (non-Javadoc)
	 * @see de.hpi.bpmn2_0.factory.AbstractBpmnFactory#createBpmnElement(org.oryxeditor.server.diagram.Shape)
	 */
	@Override
	public BPMNElement createBpmnElement(Shape shape) {
		SequenceFlowConnector seqConnector = (SequenceFlowConnector) this.createDiagramElement(shape);
		TSequenceFlow seqFlow = (TSequenceFlow) this.createProcessElement(shape);
		
		seqConnector.setSequenceFlowRef(seqFlow);
		
		return new BPMNElement(seqConnector, seqFlow, shape.getResourceId());
	}

	/* (non-Javadoc)
	 * @see de.hpi.bpmn2_0.factory.AbstractBpmnFactory#createDiagramElement(org.oryxeditor.server.diagram.Shape)
	 */
	@Override
	protected Object createDiagramElement(Shape shape) {
		SequenceFlowConnector sequenceFlowConnector = new SequenceFlowConnector();
		sequenceFlowConnector.setId(shape.getResourceId());
		
		// TODO: Gedanken machen zu Label-Positioning
		sequenceFlowConnector.setLabel(shape.getProperty("name"));
		
		for(int i = 1; i < shape.getDockers().size() - 2; i++) {
			Point point = shape.getDockers().get(i);
			
			Bendpoint bp = new Bendpoint();
			bp.setX(point.getX());
			bp.setY(point.getY());
			
			sequenceFlowConnector.getBendpoint().add(bp);
		}
		
		return sequenceFlowConnector;
	}

	/* (non-Javadoc)
	 * @see de.hpi.bpmn2_0.factory.AbstractBpmnFactory#createProcessElement(org.oryxeditor.server.diagram.Shape)
	 */
	@Override
	protected TBaseElement createProcessElement(Shape shape) {
		TSequenceFlow seqFlow = new TSequenceFlow();
		seqFlow.setId(shape.getResourceId());
		seqFlow.setName(shape.getProperty("name"));
		
		return seqFlow;
	}

}