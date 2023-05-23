package WS3DEpisodeDataset.impulses;

import WS3DEpisodeDataset.util.Vector2D;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static WS3DEpisodeDataset.util.IdeaHelper.csvPrint;

public class CollectJewelImpulse extends Codelet {

    private Memory innerSenseMO;
    private Memory jewelsMO;
    private MemoryContainer impulsesMO;
    private Idea inner;
    private Idea jewels;

    private double minDesire = 0.9, maxDesire = 1.0;
    private String impulseCat = "Collect";

    @Override
    public void accessMemoryObjects() {
        this.innerSenseMO = (MemoryObject) getInput("INNER");
        this.inner = (Idea) innerSenseMO.getI();
        this.jewelsMO = (MemoryObject) getInput("KNOWN_JEWELS");
        this.jewels = (Idea) jewelsMO.getI();
        this.impulsesMO = (MemoryContainer) getOutput("IMPULSES");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        removeSatisfiedImpulses();

        int numJewels = jewels.getL().size();
        if (numJewels > 0){
            for (Idea jewel : jewels.getL()){
                double desirability = calculateDesirability(jewel);
                if (desirability > -1.0){
                    desirability = desirability * (maxDesire - minDesire) + minDesire;
                    Idea impulse = createImpulse(jewel, desirability);
                    addIfNotPresent(impulse);
                } else {
                    removeIfPresent(jewel);
                }
            }
        }
    }

    private void removeSatisfiedImpulses() {
        List<Memory> toRemove = new ArrayList<>();
        List<Integer> jewelsID = jewels.getL().stream().map(e-> (int) e.get("ID").getValue()).collect(Collectors.toList());
        List<Memory> impulsesMemories = impulsesMO.getAllMemories();
        synchronized (impulsesMemories) {
            for (Memory impulseMem : impulsesMemories){
                Idea impulse = (Idea) impulseMem.getI();
                if (impulse.getValue().equals(this.impulseCat)){
                    if (!jewelsID.contains((int) impulse.get("State.Jewel.ID").getValue())){
                        toRemove.add(impulseMem);
                    }
                }
            }
            impulsesMemories.removeAll(toRemove);
        }
    }

    private double calculateDesirability(Idea jewel) {
        double maxDesire = -1.0;
        Vector2D selfPos = new Vector2D(
                (float) inner.get("Position.X").getValue(),
                (float) inner.get("Position.Y").getValue());
        Vector2D jewelPos = new Vector2D(
                (float) jewel.get("Position.X").getValue(),
                (float) jewel.get("Position.Y").getValue());
        if (selfPos.sub(jewelPos).magnitude() < 0.45)
            maxDesire = 1.0;
        return maxDesire;
    }

    private Idea createImpulse(Idea jewel, double desirability) {
        Idea impulse = new Idea("Impulse", this.impulseCat, "Episode", 0);
        Idea state = new Idea("State", null, "Timestep", 0);
        Idea stateJewel = new Idea("Jewel", jewel.getValue(), "AbstractObject", 1);
        stateJewel.add(jewel.get("ID").clone());
        stateJewel.add(new Idea("Condition", "In Bag", "Property", 1));
        state.add(stateJewel);
        state.add(jewel.get("ID").clone());
        state.add(new Idea("Desire", desirability, "Property", 1));
        impulse.add(state);
        return impulse;
    }

    public void addIfNotPresent(Idea idea){
        impulsesMO.setI(idea,
                (double) idea.get("State.Desire").getValue(),
                this.impulseCat + idea.get("State.ID").getValue());
    }

    public void removeIfPresent(Idea jewel){
        impulsesMO.setI(jewel,
                -1.0,
                this.impulseCat + jewel.get("ID").getValue());
    }
}
