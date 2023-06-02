package WS3DEpisodeDataset.core.codelets;

import WS3DEpisodeDataset.util.IdeaHelper;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Idea;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static WS3DEpisodeDataset.util.IdeaHelper.csvPrint;

public class EventTracker extends Codelet {

    private String inputMemoryName = "PERCEPTION_MEMORY";
    private String outputMemoryName = "EVENTS_MEMORY";
    private Memory perceptionInputMO;
    private Memory eventsOutputMO;

    private int bufferSize = 1;
    private int bufferStepSize = 1;
    private List<Idea> inputIdeaBuffer = new LinkedList<Idea>();
    private Idea initialEventIdea;
    private List<Idea> trackedEventBuffer = new LinkedList<>();
    private Idea currentInputIdea;
    private static int count = 1;
    private double detectionTreashold = 0.5;
    private Idea trackedEventCategory;
    private boolean debug = false;

    public EventTracker(String inputMemoryName, String outputMemoryName, Idea trackedEventCategory) {
        this.inputMemoryName = inputMemoryName;
        this.outputMemoryName = outputMemoryName;
        if (trackedEventCategory.getValue() instanceof Category)
            this.trackedEventCategory = trackedEventCategory;
    }

    public EventTracker(String inputMemoryName, String outputMemoryName, Idea trackedEventCategory, boolean debug) {
        this.inputMemoryName = inputMemoryName;
        this.outputMemoryName = outputMemoryName;
        if (trackedEventCategory.getValue() instanceof Category)
            this.trackedEventCategory = trackedEventCategory;
        this.debug = debug;
    }

    public EventTracker(String inputMemoryName, String outputMemoryName, double detectionTreashold, Idea trackedEventCategory) {
        this.inputMemoryName = inputMemoryName;
        this.outputMemoryName = outputMemoryName;
        this.detectionTreashold = detectionTreashold;
        if (trackedEventCategory.getValue() instanceof Category)
            this.trackedEventCategory = trackedEventCategory;
    }

    public EventTracker(String inputMemoryName, String outputMemoryName, double detectionTreashold, Idea trackedEventCategory, boolean debug) {
        this.inputMemoryName = inputMemoryName;
        this.outputMemoryName = outputMemoryName;
        this.detectionTreashold = detectionTreashold;
        if (trackedEventCategory.getValue() instanceof Category)
            this.trackedEventCategory = trackedEventCategory;
        this.debug = debug;
    }

    @Override
    public void accessMemoryObjects() {
        this.perceptionInputMO=(MemoryObject)this.getInput(this.getInputMemoryName());
        this.currentInputIdea = (Idea) perceptionInputMO.getI();
        this.eventsOutputMO=(MemoryObject)this.getOutput(this.getOutputMemoryName());
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        //Initialize event track buffer
        if (inputIdeaBuffer.size() == 0) {
            inputIdeaBuffer.add(currentInputIdea.clone());
        } else {
            if (inputIdeaBuffer.size() < this.bufferSize) {
                if (checkElapsedTime()) {
                    inputIdeaBuffer.add(currentInputIdea.clone());
                }
            } else {
                if (checkElapsedTime()) {
                    //Check if current state is coherent with previous states and event category
                    Idea testEvent = constructTestEvent();
                    if (trackedEventCategory.membership(testEvent) >= detectionTreashold) {
                        Idea drop = inputIdeaBuffer.remove(0);
                        //Copies start of the event
                        //if (initialEventIdea == null) this.initialEventIdea = drop.clone();
                        trackedEventBuffer.add(drop.clone());

                        inputIdeaBuffer.add(currentInputIdea.clone());
                    } else {
                        if (trackedEventBuffer.size() > 0) {
                            trackedEventBuffer.addAll(inputIdeaBuffer);
                            Idea event = trackedEventCategory.instantiation(trackedEventBuffer);
                            event.setName("Event" + count++);
                            inputIdeaBuffer.clear();
                            inputIdeaBuffer.add(currentInputIdea.clone());
                            //initialEventIdea = null;
                            trackedEventBuffer.clear();
                            synchronized (eventsOutputMO) {
                                Idea eventsIdea = (Idea) eventsOutputMO.getI();
                                eventsIdea.add(event);
                                if (debug)
                                    System.out.println(csvPrint(eventsIdea));

                                try {
                                    PrintWriter out = new PrintWriter("../dataset/events");
                                    String csv = IdeaHelper.csvPrint(eventsIdea, 6);
                                    out.println(csv);
                                    out.close();
                                } catch (FileNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                                eventsOutputMO.setI(eventsIdea);
                            }

                        } else {
                            inputIdeaBuffer.remove(0);
                            inputIdeaBuffer.add(currentInputIdea.clone());
                        }
                    }
                }
            }
        }
    }

    private Idea constructTestEvent() {
        Idea testEvent = new Idea("Event", null, "Episode", 0);
        List<Idea> steps = new ArrayList<>();
        for (int i = 0; i<this.inputIdeaBuffer.size(); i++){
            Idea step = new Idea("Step", i, "Timestep", 0);
            step.add(inputIdeaBuffer.get(i));
            steps.add(step);
        }
        Idea step = new Idea("Step", this.inputIdeaBuffer.size(), "Timestep", 0);
        step.add(currentInputIdea);
        steps.add(step);
        testEvent.setL(steps);
        return testEvent;
    }

    private boolean checkElapsedTime() {
        return ((int) currentInputIdea.get("Step").getValue())
                - ((int) inputIdeaBuffer.get(inputIdeaBuffer.size() - 1).get("Step").getValue())
                >= bufferStepSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        if (bufferSize > 0)
            this.bufferSize = bufferSize;
    }

    public int getBufferStepSize() {
        return bufferStepSize;
    }

    public void setBufferStepSize(int bufferStepSize) {
        if (bufferStepSize > 0)
            this.bufferStepSize = bufferStepSize;
    }

    public List<Idea> getInputIdeaBuffer() {
        return inputIdeaBuffer;
    }

    public void setInputIdeaBuffer(List<Idea> inputIdeaBuffer) {
        this.inputIdeaBuffer = inputIdeaBuffer;
    }

    public Idea getInitialEventIdea() {
        return initialEventIdea;
    }

    public void setInitialEventIdea(Idea initialEventIdea) {
        this.initialEventIdea = initialEventIdea;
    }

    public Idea getCurrentInputIdea() {
        return currentInputIdea;
    }

    public void setCurrentInputIdea(Idea currentInputIdea) {
        this.currentInputIdea = currentInputIdea;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getInputMemoryName() {
        return inputMemoryName;
    }

    public void setInputMemoryName(String inputMemoryName) {
        this.inputMemoryName = inputMemoryName;
    }

    public String getOutputMemoryName() {
        return outputMemoryName;
    }

    public void setOutputMemoryName(String outputMemoryName) {
        this.outputMemoryName = outputMemoryName;
    }
}
