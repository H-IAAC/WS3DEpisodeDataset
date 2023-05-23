/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DEpisodeDataset;

import WS3DEpisodeDataset.behavior.Collect;
import WS3DEpisodeDataset.behavior.Move;
import WS3DEpisodeDataset.categories.LinearEventCategory;
import WS3DEpisodeDataset.categories.StepEventCategory;
import WS3DEpisodeDataset.core.codelets.EventTracker;
import WS3DEpisodeDataset.episodic.TimelineBufferCodelet;
import WS3DEpisodeDataset.impulses.CollectJewelImpulse;
import WS3DEpisodeDataset.impulses.ExploreImpulse;
import WS3DEpisodeDataset.impulses.GoToJewelImpulse;
import WS3DEpisodeDataset.motor.HandsActuatorCodelet;
import WS3DEpisodeDataset.motor.LegsActuatorCodelet;
import WS3DEpisodeDataset.perception.JewelDetector;
import WS3DEpisodeDataset.perception.RoomDetector;
import WS3DEpisodeDataset.perception.WallDetector;
import WS3DEpisodeDataset.sensor.InnerSense;
import WS3DEpisodeDataset.sensor.LeafletSense;
import WS3DEpisodeDataset.sensor.Vision;
import WS3DEpisodeDataset.util.Vector2D;
import WS3DCoppelia.util.Constants;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;
import com.google.common.graph.MutableValueGraph;

import java.util.*;

/**
 *
 * @author bruno
 */
public class AgentMind extends Mind {

    private boolean debug = false;
    public List<Codelet> bList = new ArrayList<Codelet>();

    public AgentMind(Environment env, boolean debug){
        this.debug = debug;
        initializeMindAndStar(env);
    }
    public AgentMind(Environment env){
        super();
        initializeMindAndStar(env);
    }

    private void initializeMindAndStar(Environment env){
        // Create CodeletGroups and MemoryGroups for organizing Codelets and Memories
        createCodeletGroup("Sensory");
        createCodeletGroup("Motor");
        createCodeletGroup("Perception");
        createCodeletGroup("Context");
        createCodeletGroup("Behavioral");
        //createMemoryGroup("Sensory");
        //createMemoryGroup("Motor");
        //createMemoryGroup("Working");

        Memory innerSenseMO;
        Memory visionMO;
        Memory knownJewelsMO;
        Memory jewelsCounterMO;
        Memory wallsMO;
        Memory bufferMO;
        Memory eventsMO;
        Memory leafletsMO;
        MemoryContainer impulsesMO;
        MemoryContainer handsMO;
        MemoryContainer legsMO;

        //Inner Sense
        Idea innerSenseIdea = initializeInnerSenseIdea();
        innerSenseMO = createMemoryObject("INNER", innerSenseIdea);
        //Vision sensor
        visionMO = createMemoryObject("VISION");
        //Detected Jewels
        Idea jewelsIdea = new Idea("Jewels", null, 5);
        knownJewelsMO = createMemoryObject("KNOWN_JEWELS", jewelsIdea);
        //Jewels Counter
        List<Idea> jewelsCounters = new ArrayList<>();
        for (Constants.JewelTypes type : Constants.JewelTypes.values()){
            jewelsCounters.add(new Idea(type.typeName(), 0, "Property", 1));
        }
        Idea jewelCountersIdea = new Idea("JewelsCounters", null, 5);
        jewelCountersIdea.setL(jewelsCounters);
        jewelCountersIdea.add(new Idea("Step", 0, "TimeStep", 1));
        jewelCountersIdea.add(new Idea("TimeStamp", System.currentTimeMillis(), "Property", 1));
        jewelsCounterMO = createMemoryObject("JEWELS_COUNTERS", jewelCountersIdea);
        //Detected Walls
        Idea wallsIdea = new Idea("Walls", null, 5);
        wallsMO = createMemoryObject("WALLS", wallsIdea);

        //Buffer
        Idea bufferIdea = new Idea("Buffer", null, "Configuration", 1);
        bufferMO = createMemoryObject("BUFFER", bufferIdea);

        //Events
        Idea eventsIdea = new Idea("Events", null, 5);
        eventsMO = createMemoryObject("EVENTS", eventsIdea);

        //----Categories----
        //Events
        List<Idea> eventsCategoriesIdea = new ArrayList<>();
        // Elements are added when event tracker codelets are instantiated
        //--------

        //Leaflets
        Idea leafletsIdea = new Idea("Leaflets", null, 0);
        leafletsMO = createMemoryObject("LEAFLETS", leafletsIdea);
        //Impulses
        //Idea impulsesIdea = new Idea("Impulses", null, 0);
        impulsesMO = createMemoryContainer("IMPULSES");

        //Hands
        handsMO = createMemoryContainer("HANDS");
        //Leags
        legsMO = createMemoryContainer("LEGS");

        //Inner Sense Codelet
        Codelet innerSenseCodelet = new InnerSense(env.creature);
        innerSenseCodelet.addOutput(innerSenseMO);
        insertCodelet(innerSenseCodelet, "Sensory");

        //Vision Sensor Codelet
        Codelet visionCodelet = new Vision(env.creature);
        visionCodelet.addOutput(visionMO);
        insertCodelet(visionCodelet, "Sensory");

        //Leaflet Sense Codelet
        Codelet leafletSenseCodelet = new LeafletSense(env.creature);
        leafletSenseCodelet.addOutput(leafletsMO);
        insertCodelet(leafletSenseCodelet, "Sensory");

        //Jewel Detector Codelet
        Codelet jewelDetectorCodelet = new JewelDetector(env.creature, debug);
        jewelDetectorCodelet.addInput(visionMO);
        jewelDetectorCodelet.addOutput(knownJewelsMO);
        jewelDetectorCodelet.addOutput(jewelsCounterMO);
        insertCodelet(jewelDetectorCodelet, "Perception");

        //Walls Detector Codelet
        Codelet wallsDetectorCodelet = new WallDetector(debug);
        wallsDetectorCodelet.addInput(visionMO);
        wallsDetectorCodelet.addOutput(wallsMO);
        insertCodelet(wallsDetectorCodelet, "Perception");

        //Move Event Codelet
        Idea moveEventCategory = constructEventCategory("Move", Arrays.asList("Self.Position.X", "Self.Position.Y"), "Linear");
        eventsCategoriesIdea.add(moveEventCategory);
        EventTracker moveEventTracker = new EventTracker("INNER", "EVENTS", moveEventCategory, debug);
        moveEventTracker.setBufferSize(2);
        moveEventTracker.setBufferStepSize(2);
        moveEventTracker.addInput(innerSenseMO);
        moveEventTracker.addOutput(eventsMO);
        insertCodelet(moveEventTracker, "Perception");

        //Rotate Event Codelet
        Idea rotateEventCategory = constructEventCategory("Rotate", Arrays.asList("Self.Pitch"), "Linear");
        EventTracker rotateEventTracker = new EventTracker("INNER", "EVENTS", rotateEventCategory, debug);
        rotateEventTracker.setBufferSize(2);
        rotateEventTracker.setBufferStepSize(2);
        rotateEventTracker.addInput(innerSenseMO);
        rotateEventTracker.addOutput(eventsMO);
        insertCodelet(rotateEventTracker, "Perception");

        //Found Jewel Event
        for (Constants.JewelTypes type : Constants.JewelTypes.values()){
            Idea foundJewelEventCategory = constructEventCategory("Found_" + type.typeName(), Arrays.asList("JewelsCounters." + type.typeName()), "StepUp");
            EventTracker jewelFoundEventTracker = new EventTracker("JEWELS_COUNTERS", "EVENTS", foundJewelEventCategory, debug);
            jewelFoundEventTracker.setBufferSize(2);
            jewelFoundEventTracker.setBufferStepSize(2);
            jewelFoundEventTracker.addInput(jewelsCounterMO);
            jewelFoundEventTracker.addOutput(eventsMO);
            insertCodelet(jewelFoundEventTracker, "Perception");
        }

        //Collect Jewel Event
        for (Constants.JewelTypes type : Constants.JewelTypes.values()){
            Idea collectJewelEventCategory = constructEventCategory("Collected_" + type.typeName(), Arrays.asList("JewelsCounters." + type.typeName()), "StepDown");
            EventTracker jewelCollectedEventTracker = new EventTracker("JEWELS_COUNTERS", "EVENTS", collectJewelEventCategory, debug);
            jewelCollectedEventTracker.setBufferSize(2);
            jewelCollectedEventTracker.setBufferStepSize(2);
            jewelCollectedEventTracker.addInput(jewelsCounterMO);
            jewelCollectedEventTracker.addOutput(eventsMO);
            insertCodelet(jewelCollectedEventTracker, "Perception");
        }

        //Impulses
        //Go to jewel
        Codelet goToJewelImpulse = new GoToJewelImpulse();
        goToJewelImpulse.addInput(innerSenseMO);
        goToJewelImpulse.addInput(knownJewelsMO);
        goToJewelImpulse.addInput(leafletsMO);
        goToJewelImpulse.addOutput(impulsesMO);
        insertCodelet(goToJewelImpulse, "Behavioral");

        //Collect Jewel
        Codelet collectJewelImpulse = new CollectJewelImpulse();
        collectJewelImpulse.addInput(innerSenseMO);
        collectJewelImpulse.addInput(knownJewelsMO);
        collectJewelImpulse.addOutput(impulsesMO);
        insertCodelet(collectJewelImpulse, "Behavioral");


        //Move Action/Behaviour
        Codelet moveActionCodelet = new Move();
        moveActionCodelet.addInput(impulsesMO);
        moveActionCodelet.addOutput(legsMO);
        insertCodelet(moveActionCodelet, "Behavioral");

        //Collect Action/Behaviour
        Codelet collectActionCodelet = new Collect();
        collectActionCodelet.addInput(impulsesMO);
        collectActionCodelet.addInput(knownJewelsMO);
        collectActionCodelet.addOutput(handsMO);
        collectActionCodelet.addOutput(jewelsCounterMO);
        insertCodelet(collectActionCodelet, "Behavioral");

        //Hands Motor Codelet
        Codelet handsMotorCodelet = new HandsActuatorCodelet(env.creature);
        handsMotorCodelet.addInput(handsMO);
        insertCodelet(handsMotorCodelet, "Motor");

        //Legs Motor Codelet
        Codelet legsMotorCodelet = new LegsActuatorCodelet(env.creature);
        legsMotorCodelet.addInput(legsMO);
        insertCodelet(legsMotorCodelet, "Motor");

        TimelineBufferCodelet bufferCodelet = new TimelineBufferCodelet(Arrays.asList("INNER", "KNOWN_JEWELS", "IMPULSES"));
        bufferCodelet.maxSize = 1_000_000;
        bufferCodelet.addInput(innerSenseMO);
        bufferCodelet.addInput(knownJewelsMO);
        bufferCodelet.addInput(impulsesMO);
        bufferCodelet.addOutput(bufferMO);
        insertCodelet(bufferCodelet);

        bList.add(wallsDetectorCodelet);
        for (Codelet c : this.getCodeRack().getAllCodelets())
            c.setTimeStep(100);

        bufferCodelet.setTimeStep(200);

        start();
    }

    private Idea initializeInnerSenseIdea(){
        Idea innerSense = new Idea("Self", "AGENT", "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X",0, 3));
        posIdea.add(new Idea("Y",0, 3));
        innerSense.add(posIdea);
        innerSense.add(new Idea("Pitch", null, "Property", 1));
        innerSense.add(new Idea("Fuel", null, "Property", 1));
        innerSense.add(new Idea("Step", 0, "TimeStep", 1));
        innerSense.add(new Idea("TimeStamp", System.currentTimeMillis(), "Property", 1));

        return innerSense;
    }

    private Idea constructEventCategory(String name, List<String> properties, String type){
        Idea idea = new Idea(name, null, "Episode", 2);
        switch (type){
            case "Linear":
                idea.setValue(new LinearEventCategory(name, properties));
                break;
            case "StepUp":
                idea.setValue(new StepEventCategory(name, properties, "StepUp"));
                break;
            case "StepDown":
                idea.setValue(new StepEventCategory(name, properties, "StepDown"));
                break;
        }
        return idea;
    }

}
