/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DEpisodeDataset.perception;

import WS3DCoppelia.model.Thing;
import WS3DCoppelia.model.Agent;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;
import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author bruno
 */
public class JewelDetector extends Codelet {

    private Agent agent;
    private Memory visionMO;
    private Memory knownJewelsMO;
    private Memory jewelsCountersMO;
    private boolean debug = false;

    public JewelDetector(Agent agt) {
        this.name = "JewelDetector";
        this.agent = agt;
    }

    public JewelDetector(Agent agt, boolean debug) {
        this.name = "JewelDetector";
        this.debug = debug;
        this.agent = agt;
    }

    @Override
    public void accessMemoryObjects() {
        synchronized (this) {
            this.visionMO = (MemoryObject) this.getInput("VISION");
        }
        this.knownJewelsMO = (MemoryObject) this.getOutput("KNOWN_JEWELS");
        this.jewelsCountersMO = (MemoryObject) getOutput("JEWELS_COUNTERS");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        CopyOnWriteArrayList<Thing> vision;
        List<Idea> known;
        synchronized (visionMO) {
            vision = new CopyOnWriteArrayList((List<Thing>) visionMO.getI());
            synchronized (knownJewelsMO) {
                Idea jewelsIdea = (Idea) knownJewelsMO.getI();
                if (debug) {
                    System.out.println(jewelsIdea.toStringFull());
                }
                known = Collections.synchronizedList(jewelsIdea.getL());
                synchronized (vision) {
                    for (Thing t : vision) {
                        boolean found = false;
                        synchronized (known) {
                            CopyOnWriteArrayList<Idea> myknown = new CopyOnWriteArrayList<>(known);
                            for (Idea e : myknown)
                                if (t.getId() == ((int) e.get("ID").getValue())) {
                                    found = true;
                                    updateJewelIdea(t, e);
                                }
                            if (!found && t.isJewel()) {
                                known.add(constructJewelIdea(t));
                                synchronized (jewelsCountersMO) {
                                    Idea jewelsCountersIdea = (Idea) jewelsCountersMO.getI();
                                    List<Idea> counters = jewelsCountersIdea.getL();
                                    jewelsCountersIdea.get("Step").setValue((int) jewelsCountersIdea.get("Step").getValue() + 1);
                                    jewelsCountersIdea.get("TimeStamp").setValue(System.currentTimeMillis());
                                    for (Idea counter : counters) {
                                        if (counter.getName().equals(t.getTypeName())) {
                                            int count = (int) counter.getValue() + 1;
                                            counter.setValue(count);
                                        }
                                    }
                                    jewelsCountersMO.setI(jewelsCountersIdea);
                                }
                            }
                            synchronized (jewelsCountersMO) {
                                Idea jewelsCountersIdea = (Idea) jewelsCountersMO.getI();
                                List<Idea> counters = jewelsCountersIdea.getL();
                                jewelsCountersIdea.get("Step").setValue((int) jewelsCountersIdea.get("Step").getValue() + 1);
                                jewelsCountersIdea.get("TimeStamp").setValue(System.currentTimeMillis());
                                jewelsCountersMO.setI(jewelsCountersIdea);
                            }
                        }
                    }
                }
                knownJewelsMO.setI(jewelsIdea);
            }
        }
    }

    private void updateJewelIdea(Thing t, Idea e) {
        List<Float> relPos = agent.getRelativePosition(Arrays.asList(
                (float) e.get("Position.X").getValue(),
                (float) e.get("Position.Y").getValue(),
                (float) e.get("Position.Z").getValue()
        ));

        Vector3D jewelPos = new Vector3D(relPos.get(0), relPos.get(1), relPos.get(2));

        SphericalCoordinates shpericalPos = new SphericalCoordinates(jewelPos);
        e.get("Relative Position.r").setValue(shpericalPos.getR());
        e.get("Relative Position.theta").setValue(shpericalPos.getTheta());
        e.get("Relative Position.phi").setValue(shpericalPos.getPhi());
    }

    public Idea constructJewelIdea(Thing t) {

        Idea jewelIdea = new Idea("Jewel", t.getTypeName(), "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X", t.getPos().get(0), "QualityDimension", 1));
        posIdea.add(new Idea("Y", t.getPos().get(1), "QualityDimension", 1));
        posIdea.add(new Idea("Z", t.getPos().get(2), "QualityDimension", 1));
        jewelIdea.add(posIdea);
        List<Float> relPos = agent.getRelativePosition(t.getPos());
        Vector3D posVec = new Vector3D((double) relPos.get(0),
                (double) relPos.get(1),
                (double) relPos.get(2));
        SphericalCoordinates shpericalPos = new SphericalCoordinates(posVec);
        Idea relPosIdea = new Idea("Relative Position", null, "Property", 1);
        relPosIdea.add(new Idea("r", shpericalPos.getR(), "QualityDimension", 1));
        relPosIdea.add(new Idea("theta", shpericalPos.getTheta(), "QualityDimension", 1));
        relPosIdea.add(new Idea("phi", shpericalPos.getPhi(), "QualityDimension", 1));
        jewelIdea.add(relPosIdea);
        jewelIdea.add(new Idea("Color", t.getColor(), "Property", 1));
        jewelIdea.add(new Idea("ID", t.getId(), "Property", 1));
        return jewelIdea;
    }
}
