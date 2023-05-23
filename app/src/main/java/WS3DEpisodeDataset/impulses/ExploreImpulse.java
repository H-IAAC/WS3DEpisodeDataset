package WS3DEpisodeDataset.impulses;

import WS3DEpisodeDataset.util.IdeaHelper;
import WS3DEpisodeDataset.util.Vector2D;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.List;
import java.util.Random;

public class ExploreImpulse extends Codelet {

    private Memory jewelsMO;
    private Memory innerMO;
    private Memory roomMO;
    private MemoryContainer impulsesMO;
    private Memory locationsMO;

    private List<Idea> roomCategories;
    private String impulseCat = "Explore";

    public ExploreImpulse(List<Idea> roomCategories) {
        this.roomCategories = roomCategories;
    }

    @Override
    public void accessMemoryObjects() {
        this.jewelsMO = (MemoryObject) getInput("KNOWN_JEWELS");
        this.innerMO = (MemoryObject) getInput("INNER");
        this.roomMO = (MemoryObject) getInput("ROOM");
        this.impulsesMO = (MemoryContainer) getOutput("IMPULSES");
        this.locationsMO = (MemoryObject) getInput("LOCATION");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        Idea jewels = (Idea) jewelsMO.getI();
        Idea inner = (Idea) innerMO.getI();

        int numJewels = jewels.getL().size();
        if (numJewels == 0){
            Idea impulse = (Idea) impulsesMO.getI(this.impulseCat);
            if (impulse != null){
                Vector2D dest = new Vector2D(
                        (float) impulse.get("State.Self.Position.X").getValue(),
                        (float) impulse.get("State.Self.Position.Y").getValue());
                Vector2D curr = new Vector2D(
                        (float) inner.get("Position.X").getValue(),
                        (float) inner.get("Position.Y").getValue());
                System.out.println(dest.sub(curr).magnitude() );
                if (dest.sub(curr).magnitude() < 0.40) {
                    //removeSatisfiedImpulses();
                    Idea newDest = chooseLocation();
                    impulsesMO.setI(createImpulse(newDest, 0.1), 0.1, this.impulseCat);
                }
            } else {
                Idea dest = chooseLocation();
                impulsesMO.setI(createImpulse(dest, 0.1), 0.1, this.impulseCat);
            }
        } else {
            removeSatisfiedImpulses();
        }
    }

    private Idea chooseLocation() {
        Idea choosenLoc = null;
        //List known locations
        List<Idea> locations = (List<Idea>) locationsMO.getI();

        //Sample a location based on reward value
        if(locations.size() > 0) {
            Idea selected = null;
            synchronized (locations) {
                double total = 0d;
                for (Idea catLoc : locations){
                    double r = (double) catLoc.get("Reward").getValue();
                    total += r;
                }
                //5% chance of choosing a random, possibly unexplored, location
                double rnd = new Random().nextDouble() * total*1.05;
                total = 0;
                for (Idea catLoc : locations){
                    double r = (double) catLoc.get("Reward").getValue();
                    total += r;
                    if (rnd < total) {
                        selected = catLoc;
                        break;
                    }
                }
            }

            if (selected != null) {
                choosenLoc = selected.instantiation();
                return choosenLoc;
            }
        }

        boolean isInRoom = false;
        while (!isInRoom) {
            choosenLoc = new Idea("Position", null, "Property", 0);
            float x = 10 * new Random().nextFloat();
            float y = 8 * new Random().nextFloat();
            choosenLoc.add(new Idea("X", x, "QualityDimension", 0));
            choosenLoc.add(new Idea("Y", y, "QualityDimension", 0));

            for (Idea room : roomCategories) {
                if (room.membership(choosenLoc) > 0.8)
                    isInRoom = true;
            }
        }

        return choosenLoc;
    }

    private void removeSatisfiedImpulses() {
        List<Memory> impulsesMemories = impulsesMO.getAllMemories();
        Memory remove = null;
        synchronized (impulsesMemories) {
            for (Memory impulseMem : impulsesMemories) {
                Idea impulse = (Idea) impulseMem.getI();
                if (impulse.getValue().equals(this.impulseCat))
                    remove = impulseMem;
            }
            if (remove != null)
                impulsesMemories.remove(remove);
        }
    }

    private Idea createImpulse(Idea position, double desirability) {
        Idea impulse = new Idea("Impulse", this.impulseCat, "Episode", 0);
        Idea state = new Idea("State", null, "Timestamp", 0);
        Idea self = new Idea("Self", null, "AbstractObject", 1);
        self.add(position);
        state.add(self);
        state.add(new Idea("Desire", desirability, "Property", 1));
        impulse.add(state);
        return impulse;
    }
}
