package WS3DEpisodeDataset.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;

public class RoomDetector extends Codelet {

    private Memory innerSenseMO;
    private Memory roomCategoriesMO;
    private Memory roomMO;
    private Idea innerSense;
    private List<Idea> roomCategories;
    private Idea detectedRoom;

    public RoomDetector() {
        this.name = "RoomDetector";
    }

    @Override
    public void accessMemoryObjects() {
        this.innerSenseMO = (MemoryObject) getInput("INNER");
        this.innerSense = (Idea) innerSenseMO.getI();
        this.roomCategoriesMO = (MemoryObject) getInput("ROOM_CATEGORIES");
        this.roomCategories = (List<Idea>) roomCategoriesMO.getI();
        this.roomMO = (MemoryObject) getOutput("ROOM");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        synchronized (roomMO) {
            this.detectedRoom = (Idea) roomMO.getI();
            detectedRoom.setL(new ArrayList<>());
            for (Idea category : roomCategories) {
                if (category.membership(innerSense) > 0) {
                    detectedRoom.add(new Idea("Location", category.getName(), "Property", 1));
                }
            }
            roomMO.setI(detectedRoom);
        }
        //System.out.println((float) innerSense.get("Position.Y").getValue());
        //System.out.printf(fullPrint(detectedRoom));
        //0,0 - 8,3
        //0,3 - 1,7
        //0,7 - 8,10
    }

}
