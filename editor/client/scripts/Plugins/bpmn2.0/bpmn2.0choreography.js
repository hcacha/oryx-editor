/**
 * Copyright (c) 2009
 * Sven Wagner-Boysen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 **/

/**
   @namespace Oryx name space for plugins
   @name ORYX.Plugins
*/
 if(!ORYX.Plugins)
	ORYX.Plugins = new Object();
	

/**
 * This plugin provides methodes to layout the choreography diagrams of BPMN 2.0.
 * 
 * @class ORYX.Plugins.Bpmn2_0Choreography
 * @extends ORYX.Plugins.AbstractPlugin
 * @param {Object} facade
 * 		The facade of the Editor
 */
ORYX.Plugins.Bpmn2_0Choreography = {
	
	/**
	 *	Constructor
	 *	@param {Object} Facade: The Facade of the Editor
	 */
	construct: function(facade) {
		this.facade = facade;
		
		/* Register on event ORYX.CONFIG.EVENT_STENCIL_SET_LOADED and ensure that
		 * the stencil set extension is loaded.
		 */
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_STENCIL_SET_LOADED, 
										this.handleStencilSetLoaded.bind(this));
		
		this.participantSize = 20;
		this.extensionSizeForMarker = 10;
		this.choreographyTasksMeta = new Hash();
	},
	
	
	/**
	 * Check if the 'http://oryx-editor.org/stencilsets/extensions/bpmn2.0choreography#'
	 * stencil set extension is loaded and thus register or unregisters on the 
	 * appropriated events.
	 */
	handleStencilSetLoaded : function() {
		if(this.isStencilSetExtensionLoaded('http://oryx-editor.org/stencilsets/extensions/bpmn2.0choreography#')) {
			this.registerPluginOnEvents();
		} else {
			this.unregisterPluginOnEvents();
		}
	},
	
	/**
	 * Register this plugin on the events.
	 */
	registerPluginOnEvents: function() {
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_PROPWINDOW_PROP_CHANGED, this.handlePropertyChanged.bind(this));
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_SHAPEADDED, this.addParticipantsOnCreation.bind(this));
		this.facade.registerOnEvent('layout.bpmn2_0.choreography.task', this.handleLayoutChoreographyTask.bind(this));
		this.facade.registerOnEvent('layout.bpmn2_0.choreography.subprocess.expanded', this.handleLayoutChoreographySubprocessExpanded.bind(this));
		this.facade.registerOnEvent('layout.bpmn2_0.choreography.subprocess.collapsed', this.handleLayoutChoreographySubprocessCollapsed.bind(this));
//		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_PROPERTY_CHANGED, this.handlePropertyChanged.bind(this));
	},
	
	/**
	 * Unregisters this plugin from the events.
	 */
	unregisterPluginOnEvents: function() {
		this.facade.unregisterOnEvent(ORYX.CONFIG.EVENT_PROPWINDOW_PROP_CHANGED, this.handlePropertyChanged.bind(this));
		this.facade.unregisterOnEvent(ORYX.CONFIG.EVENT_SHAPEADDED, this.addParticipantsOnCreation.bind(this));
//		this.facade.unregisterOnEvent(ORYX.CONFIG.EVENT_PROPERTY_CHANGED, this.handlePropertyChanged.bind(this));
		this.facade.unregisterOnEvent('layout.bpmn2_0.choreography.task', this.handleLayoutChoreographyTask.bind(this));
		this.facade.unregisterOnEvent('layout.bpmn2_0.choreography.subprocess.expanded', this.handleLayoutChoreographySubprocessExpanded.bind(this));
		this.facade.unregisterOnEvent('layout.bpmn2_0.choreography.subprocess.collapsed', this.handleLayoutChoreographySubprocessCollapsed.bind(this));
	},
	
	/**
	 * Handler for 'layout.bpmn2_0.choreography.subprocess.expanded'
	 * Applies the layout for an expanded subprocess. e.g. positioning of the 
	 * text field.
	 * 
	 * @param {Object} event
	 * 		The layout event.
	 */
	handleLayoutChoreographySubprocessExpanded : function(event) {
		var choreographyTask = event.shape;
		var choreographyTaskMeta = this.addOrGetChoreographyTaskMeta(choreographyTask);
		var heightDelta = choreographyTask.bounds.height() / choreographyTask._oldBounds.height();
	
		/* Handle text field position */
		var textField = choreographyTask._labels[choreographyTask.getId() + 'text_name'];
		if(textField) {
			var top = choreographyTaskMeta.topYEndValue + 5;

			/* Consider changed in update cycle */
			if(choreographyTask.isResized && heightDelta) {
				textField.y = top / heightDelta;
			} else {
				textField.y = top;
			}
			
		}
	},
	
	
	/**
	 * Handler for 'layout.bpmn2_0.choreography.subprocess.collapsed'
	 * Applies the layout for a collapsed subprocess. 
	 * e.g. plus marker
	 * 
	 * @param {Object} event
	 * 		The layout event.
	 */
	handleLayoutChoreographySubprocessCollapsed : function(event) {
		var choreographyTask = event.shape;
		var choreographyTaskMeta = this.addOrGetChoreographyTaskMeta(choreographyTask);
		
		/* Calculate position of the "plus" marker of the subprocess */
		var plusMarker = choreographyTask._svgShapes.find(function(svgShape) {
			return svgShape.element.id == choreographyTask.getId() + 'plus_marker';
		});
		
		var plusMarkerBorder = choreographyTask._svgShapes.find(function(svgShape) {
			return svgShape.element.id == choreographyTask.getId() + 'plus_marker_border';
		});
		
		if(plusMarker && plusMarkerBorder) {
				plusMarker._isYLocked = true;
				plusMarker.y = choreographyTaskMeta.bottomYStartValue - 12;
				
				plusMarkerBorder._isYLocked = true;
				plusMarkerBorder.y = choreographyTaskMeta.bottomYStartValue - 14;
		}
	},
	
	/**
	 * When a choreography task is created, two participants automatically will
	 * be added (one initiating and one returning)
	 * 
	 * @param {Object} event
	 * 		The ORYX.CONFIG.EVENT_SHAPEADDED event
	 */
	addParticipantsOnCreation: function(event) {
		if(event.shape._stencil && 
			(event.shape._stencil.id() === 
				"http://b3mn.org/stencilset/bpmn2.0#ChoreographyTask" ||
			event.shape._stencil.id() === 
				"http://b3mn.org/stencilset/bpmn2.0#ChoreographySubprocessCollapsed" ||
			event.shape._stencil.id() === 
				"http://b3mn.org/stencilset/bpmn2.0#ChoreographySubprocessExpanded")	){
		
			var shape = event.shape;
			var hasParticipants = shape.getChildNodes().find(function(node) {
				return (node.getStencil().id() === 
							"http://b3mn.org/stencilset/bpmn2.0#ChoreographyParticipant");
			});
			
			if(hasParticipants) {return;}
			
			/* Insert initial participants */
			
			var participant1 = {
				type:"http://b3mn.org/stencilset/bpmn2.0#ChoreographyParticipant",
				position:{x:0,y:0},
				namespace:shape.getStencil().namespace(),
				parent:shape
			};
			var shapeParticipant1 = this.facade.createShape(participant1);
			shapeParticipant1.setProperty('oryx-behavior', "Initiating");
			var propEvent = {
				elements 	: [shapeParticipant1],
				key 		: "oryx-behavior",
				value		: "Initiating"
			};
			this.handlePropertyChanged(propEvent);
			
			var participant2 = {
				type:"http://b3mn.org/stencilset/bpmn2.0#ChoreographyParticipant",
				position:{x:0,y:shape.bounds.lowerRight().y},
				namespace:shape.getStencil().namespace(),
				parent:shape
			};
			this.facade.createShape(participant2);
			
			this.facade.getCanvas().update();
			
		}
	},
	
	/**
	 * Initialize the meta data object for the choreography task if necessary and
	 * return it.
	 * 
	 * @param {Object} choregraphyTask
	 * 		The choreography task
	 * @return {Object} choreographyTaskMetaData
	 * 		Positioning values to handle child participants.
	 */
	addOrGetChoreographyTaskMeta: function(choreographyTask) {
		if(!this.choreographyTasksMeta[choreographyTask.getId()]) {
			
			/* Initialize meta values */			
			this.choreographyTasksMeta[choreographyTask.getId()] = new Object();
			this.choreographyTasksMeta[choreographyTask.getId()].numOfParticipantsOnTop = 0;
			this.choreographyTasksMeta[choreographyTask.getId()].numOfParticipantsOnBottom = 0;
			this.choreographyTasksMeta[choreographyTask.getId()].numOfParticipantsExtendedOnBottom = 0;
			this.choreographyTasksMeta[choreographyTask.getId()].numOfParticipantsExtendedOnTop = 0;
			
			this.choreographyTasksMeta[choreographyTask.getId()].bottomYStartValue = 
				choreographyTask.bounds.height();
			this.choreographyTasksMeta[choreographyTask.getId()].topYEndValue = 0;
			
			this.choreographyTasksMeta[choreographyTask.getId()].oldHeight = 
				choreographyTask.bounds.height();
			this.choreographyTasksMeta[choreographyTask.getId()].oldBounds = 
				choreographyTask.bounds.clone();
			
			/* Ensure the side of participants while resizing */
			this.choreographyTasksMeta[choreographyTask.getId()].topParticipants = new Array();
			this.choreographyTasksMeta[choreographyTask.getId()].bottomParticipants = new Array();
			
		}
		return this.choreographyTasksMeta[choreographyTask.getId()];
	},
	
	/**
	 * Adjust the meta values, if the choreography task is resized.
	 * 
	 * @param {Object} choreographyTask
	 * @param {Object} choreographyTaskMeta
	 */
	handleResizingOfChoreographyTask: function(choreographyTask, choreographyTaskMeta) {
		if(choreographyTask.bounds.height() == choreographyTaskMeta.oldHeight) {return;}
		
		/* Ensure that the choreography task is not too small in height */
		
		var minimumHeight = choreographyTaskMeta.numOfParticipantsOnTop 
							* this.participantSize + 
							choreographyTaskMeta.numOfParticipantsExtendedOnTop *
							this.extensionSizeForMarker +
							choreographyTaskMeta.numOfParticipantsOnBottom 
							* this.participantSize +
							choreographyTaskMeta.numOfParticipantsExtendedOnBottom *
							this.extensionSizeForMarker 
							+ 40;
		if(minimumHeight > choreographyTask.bounds.height()) {
			var ul = choreographyTask.bounds.upperLeft();
			var oldUl = choreographyTaskMeta.oldBounds.upperLeft();
			var lr = choreographyTask.bounds.lowerRight();
			var oldLr = choreographyTaskMeta.oldBounds.lowerRight();
			
			if(ul.y != oldUl.y) {
				/* Resized on top side */
				choreographyTask.bounds.set(ul.x, lr.y - minimumHeight, lr.x, lr.y);
			} else if(lr.y != oldLr.y) {
				/* Resized on bottom side */
				choreographyTask.bounds.set(ul.x, ul.y, lr.x, ul.y + minimumHeight);
			}
		}
		
		/* Adjust the y coordinate for the starting position of the bottom participants */
		var yAdjustment = choreographyTaskMeta.oldHeight - choreographyTask.bounds.height();
		choreographyTaskMeta.bottomYStartValue -= yAdjustment;
		
		/* Signals it was resized */
		return true;
	},
	
	/**
	 * Handler for layouting event 'layout.bpmn2_0.choreography.task'
	 * 
	 * @param {Object} event
	 * 		The layout event
	 */
	handleLayoutChoreographyTask: function(event) {
		var choreographyTask = event.shape;
		var choreographyTaskMeta = this.addOrGetChoreographyTaskMeta(choreographyTask);
		
		var isResized = this.handleResizingOfChoreographyTask(choreographyTask, choreographyTaskMeta);
		
		/* ------- Handle participants on top side  ------- */
		
		if(isResized) {
			/* Do not calculate the position of a paraticipant if it was only a resizing */
			var participants = choreographyTaskMeta.topParticipants;
		} else {
			var participants = this.getParticipants(choreographyTask,true,false);
			
			if(!participants) {return;}
			this.ensureParticipantsParent(choreographyTask, participants);
		}
		
		var numOfParticipantsExtended = 0;
		
		/* Put participants into the right position */
		participants.each(function(participant, i) {
			
			/* Disable resizing by the user interface */
			participant.isResizable = false;
			
			participant.setProperty('oryx-corners', "None");
			var isExtended = this.setBoundsOfParticipantDependOnProperties(
													participant,
													i,
													numOfParticipantsExtended,
													choreographyTask.bounds.width(),
													0);
			
			/* Count extended participants */										
			if(isExtended) {numOfParticipantsExtended++;}
													
//			participant.bounds.set(0, i * this.participantSize, 
//								choreographyTask.bounds.width(), 
//								this.participantSize +  i * this.participantSize);
			
			/* The first participants gets rounded corners */
			if(i == 0) {
				participant.setProperty('oryx-corners', "Top");
			}
		}.bind(this));
		
		/* Resize choreography task to top side */
		
		var resizeFactor = participants.length - 
									choreographyTaskMeta.numOfParticipantsOnTop;
		var resizeFactorExtended = numOfParticipantsExtended -
							choreographyTaskMeta.numOfParticipantsExtendedOnTop;
		
		var bounds = choreographyTask.bounds;
		var ul = bounds.upperLeft();
		var lr = bounds.lowerRight();
		bounds.set(ul.x, 
				ul.y - resizeFactor * this.participantSize 
					- resizeFactorExtended * this.extensionSizeForMarker, lr.x, lr.y);
		
		/* Set new top and bottom border values */
		choreographyTaskMeta.topYEndValue = 
							participants.length * this.participantSize 
						+	numOfParticipantsExtended * this.extensionSizeForMarker;
		
		choreographyTaskMeta.bottomYStartValue += 
			resizeFactor * this.participantSize + 
			resizeFactorExtended * this.extensionSizeForMarker;
		
		/* Set new meta value for top participant band */	
		choreographyTaskMeta.numOfParticipantsExtendedOnTop = numOfParticipantsExtended;
		choreographyTaskMeta.numOfParticipantsOnTop = participants.length;
		choreographyTaskMeta.topParticipants = participants;
		
		/* ----- Handle participants on bottom side --------- */
		if(isResized) {
			/* Do not calculate the position of a paraticipant if it was only a resizing */
			var participants = choreographyTaskMeta.bottomParticipants;
		} else {
			var participants = this.getParticipants(choreographyTask,false,true);
			
			if(!participants) {return;}
			this.ensureParticipantsParent(choreographyTask, participants);
		}
		
		var bottomStartYValue = choreographyTaskMeta.bottomYStartValue;
		var numOfParticipantsExtended = 0;
		
		/* Put participants into the right position */
		participants.each(function(participant, i) {
			
			/* Disable resizing by the user interface */
			participant.isResizable = false;
			
			participant.setProperty('oryx-corners', "None");
			
			var isExtendedParticipant = 
				this.setBoundsOfParticipantDependOnProperties(participant, 
								i,
								numOfParticipantsExtended,
								choreographyTask.bounds.width(),
								bottomStartYValue);
			
			/* Count extended participants */
			if(isExtendedParticipant) {numOfParticipantsExtended++;}
			
//			participant.bounds.set(0, bottomStartYValue + 
//														 i * this.participantSize, 
//								choreographyTask.bounds.width(), 
//								bottomStartYValue +
//								this.participantSize +  i * this.participantSize);
			
			/* The last participants gets rounded corners */
			if(i == participants.length - 1) {
				participant.setProperty('oryx-corners', "Bottom");
			}
		}.bind(this));
		
		/* Resize choreography task to top bottom side */
		
		var resizeFactor = participants.length - 
								choreographyTaskMeta.numOfParticipantsOnBottom;
		var resizeFactorExtended = numOfParticipantsExtended - 
						choreographyTaskMeta.numOfParticipantsExtendedOnBottom;
		
		var bounds = choreographyTask.bounds;
		var ul = bounds.upperLeft();
		var lr = bounds.lowerRight();
		
		bounds.set( ul.x, 
					ul.y, 
					lr.x, 
					lr.y + resizeFactor * this.participantSize 
					+ resizeFactorExtended * this.extensionSizeForMarker);
		
		/* Store new meta values */
		choreographyTaskMeta.numOfParticipantsExtendedOnBottom = numOfParticipantsExtended;
		choreographyTaskMeta.numOfParticipantsOnBottom = participants.length;
		choreographyTaskMeta.bottomParticipants = participants;
		
		choreographyTaskMeta.oldHeight = bounds.height();
		choreographyTaskMeta.oldBounds = bounds.clone();
		
		this.adjustTextFieldAndMarkerPosition(choreographyTask);
		
	},
	
	/**
	 * Resizes the participant depending on value of the multi-instances 
	 * property.
	 * 
	 * @param {ORYX.Core.Node} participant
	 * 		The concerning participant
	 * @param {Integer} numParticipantsBefore
	 * 		Number of participants before current
	 * @param {Integer} numParticipantsExtendedBefore
	 *		Number of participants extended in size before current
	 * @param {Float} width
	 * 		The width of the participant
	 * @param {Integer} yOffset
	 * 		Offset for the position of the bottom participants
	 */
	setBoundsOfParticipantDependOnProperties: function(	participant, 
														numParticipantsBefore, 
														numParticipantsExtendedBefore,
														width,
														yOffset) {
		
		var extended = (participant.properties['oryx-multiple_instance'] === "" ? 
						false : participant.properties['oryx-multiple_instance']);
		var ulY = yOffset + 
			numParticipantsBefore * this.participantSize + 
			numParticipantsExtendedBefore * this.extensionSizeForMarker;
		var lrY = yOffset + this.participantSize +
			numParticipantsBefore * this.participantSize + 
			(extended ? (numParticipantsExtendedBefore + 1) * this.extensionSizeForMarker : 
				numParticipantsExtendedBefore * this.extensionSizeForMarker);
		
		participant.bounds.set(	0, 
			ulY, 
			width, 
			lrY);
			
		/* Is a multi-instance participant */
		return extended;
	},
	
	/**
	 * Set the y coordinate for the text field and multiple instance marker 
	 * position in order to ensure that the text or marker is not hidden 
	 * by a participant.
	 * 
	 * @param {ORYX.Core.Node} choreographyTask
	 * 		The choreography task.
	 */
	adjustTextFieldAndMarkerPosition: function(choreographyTask) {
		var choreographyTaskMeta = this.addOrGetChoreographyTaskMeta(choreographyTask);
		var heightDelta = choreographyTask.bounds.height() / choreographyTask._oldBounds.height();
		
		/* Handle text field position */
		var textField = choreographyTask._labels[choreographyTask.getId() + 'text_name'];
		if(textField) {
			var center = choreographyTaskMeta.topYEndValue +
				(choreographyTaskMeta.bottomYStartValue - choreographyTaskMeta.topYEndValue) / 2;

			/* Consider changed in update cycle */
			if(choreographyTask.isResized && heightDelta) {
				textField.y = center / heightDelta;
			} else {
				textField.y = center;
			}
			
		}
		
		/* Handle MI and loop marker position */
		
		var loopMarker = choreographyTask._svgShapes.find(function(svgShape) {
			return svgShape.element.id == choreographyTask.getId() + 'loop_path';
		});
		if(loopMarker) {
				loopMarker._isYLocked = true;
				loopMarker.y = choreographyTaskMeta.bottomYStartValue - 7;
		}
		
		var miMarker = choreographyTask._svgShapes.find(function(svgShape) {
			return svgShape.element.id == choreographyTask.getId() + 'mi_path';
		}); 
		if(miMarker) {
			miMarker._isYLocked = true;
			miMarker.y = choreographyTaskMeta.bottomYStartValue - 11;
		}
		
	},
	
	/**
	 * Ensure that the parent of the participant is the choreography task.
	 * 
	 * @param {Object} shape
	 * 		The choreography task
	 * @param {Object} participants
	 * 		The participants
	 */
	ensureParticipantsParent: function(shape, participants) {
		if(!shape || !participants) {return;}
		
		participants.each(function(participant) {
			if(participant.parent.getId() == shape.getId()) {return;}
			
			
			
			
			/* Set ChoreographyTask as Parent */
			participant.parent.remove(participant);
			shape.add(participant);
		});
	},
	
	/**
	 * Returns the participants of a choreography task ordered by theire position.
	 * 
	 * @param {Object} shape
	 * 		The choreography task
	 * @param {Object} onTop
	 * 		Flag to get the participants from the top side of the task.
	 * @param {Object} onBottom
	 * 		Flag to get the participants from the bottom side of the task.
	 * @return {Array} participants;
	 * 		The child participants
	 */
	getParticipants: function(shape, onTop, onBottom) {
		if(shape.getStencil().id() !== "http://b3mn.org/stencilset/bpmn2.0#ChoreographyTask" &&
			shape.getStencil().id() !== "http://b3mn.org/stencilset/bpmn2.0#ChoreographySubprocessCollapsed" &&
			shape.getStencil().id() !== "http://b3mn.org/stencilset/bpmn2.0#ChoreographySubprocessExpanded") {
			return null;
		}
		
		var choreographyTaskMeta = this.addOrGetChoreographyTaskMeta(shape);
		var center = shape.absoluteBounds().upperLeft().y +
			 choreographyTaskMeta.topYEndValue +
			(choreographyTaskMeta.bottomYStartValue - choreographyTaskMeta.topYEndValue) / 2;
		
		/* Get participants of top side */
		var participantsTop = shape.getChildNodes(true).findAll(function(node) { 
			return (onTop && 
					node.getStencil().id() === "http://b3mn.org/stencilset/bpmn2.0#ChoreographyParticipant" &&
					node.absoluteBounds().center().y <= center &&
					this.isParticipantOfShape(shape, node)); 
		}.bind(this));
		
		/* Get participants of bottom side */
		var participantsBottom = shape.getChildNodes(true).findAll(function(node) { 
			return (onBottom && 
					node.getStencil().id() === "http://b3mn.org/stencilset/bpmn2.0#ChoreographyParticipant" &&
					node.absoluteBounds().center().y > center && 
					this.isParticipantOfShape(shape, node)); 
		}.bind(this));
		
		var participants = participantsTop.concat(participantsBottom);
		
		participants.sort(function(a,b) {return (a.absoluteBounds().upperLeft().y >
													b.absoluteBounds().upperLeft().y);
		});
		
		return participants;
	},
	
	/**
	 * Checks if the participant belongs to the shape. Used to detect choreography
	 * tasks inside an expanded choreography subprocess.
	 * 
	 * @param {ORYX.Core.Node} shape
	 * 		The choreography element
	 * 
	 * @param {ORYX.Core.Node} participant
	 * 		The participant node
	 * 
	 * @return {boolean} 
	 * 		True if the participant is a direct child of the shape and is not
	 * 		contained in aother choreography task or subprocess
	 */
	isParticipantOfShape: function(shape, participant) {
		var participantsParent = participant.parent;
		
		/* Get a non-participant parent of the participant */
		while(participantsParent.getStencil().id() === 
				"http://b3mn.org/stencilset/bpmn2.0#ChoreographyParticipant") {
			participantsParent = participantsParent.parent;			
		}
		
		/* The detected parent should be the shape */
		
		return participantsParent.getId() === shape.getId();
	},
	
	/**
	 * PropertyWindow.PropertyChanged Handler
	 * 
	 * It sets the correct color of the elements of a participant depending on
	 * either initiating or returning nature.
	 * 
	 * @param {Object} event
	 * 		The property changed event
	 */
	handlePropertyChanged: function(event) {
		var shapes = event.elements;
		var propertyKey = event.key || event.name;
		var propertyValue = event.value;
		
		var changed = false;
		shapes.each(function(shape) {
			if( shape.getStencil().id() === 
					"http://b3mn.org/stencilset/bpmn2.0#ChoreographyParticipant" &&
				propertyKey === "oryx-behavior") {
				
				/* Set appropriate color */
				if(propertyValue === "Returning") {
					shape.setProperty("oryx-color", "#c6c6c6");
				} else if(propertyValue === "Initiating") {
					shape.setProperty("oryx-color", "#ffffff");
				}	
				
				changed = true;	
			}
		});
		
		/* Update visualisation if necessary */
		if(changed) {
			this.facade.getCanvas().update();
		}
	}
	
};

ORYX.Plugins.Bpmn2_0Choreography = ORYX.Plugins.AbstractPlugin.extend(ORYX.Plugins.Bpmn2_0Choreography);