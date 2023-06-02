/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package WS3DEpisodeDataset;

import WS3DCoppelia.util.Constants;
import WS3DEpisodeDataset.util.visualization.IdeaVisualizer;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.io.rest.RESTServer;
import br.unicamp.cst.representation.idea.Idea;
import br.unicamp.cst.util.viewer.MindViewer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    public static void main(String[] args) {
        // Create Environment
        Environment env=new Environment(); //Creates only a creature and some apples
        AgentMind a = new AgentMind(env);  // Creates the Agent Mind and start it
        MindViewer view = new MindViewer(a, "MindView", new ArrayList<>());
        view.setVisible(true);
        RESTServer rs = new RESTServer(a,5000,true);

        IdeaVisualizer visu = new IdeaVisualizer(a);
        visu.addMemoryWatch("Impulses", 5);
        visu.addMemoryWatch("Events", 5);
        visu.addMemoryWatch("Known_jewels", 5);
        visu.addMemoryWatch("Buffer", 5);
        visu.setVisible(true);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                env.stopSimulation();
            }
        });
        runTestCommands(env, a);
    }

    public static void runTestCommands(Environment env, AgentMind a){
        env.world.createThing(Constants.JewelTypes.RED_JEWEL, 0.2f, 9.5f);
        env.world.createThing(Constants.JewelTypes.BLUE_JEWEL, 0.4f, 9.5f);
        env.world.createThing(Constants.JewelTypes.GREEN_JEWEL, 0.6f, 9.5f);
        env.world.createThing(Constants.JewelTypes.WHITE_JEWEL, 0.8f, 9.5f);
        env.world.createThing(Constants.JewelTypes.MAGENTA_JEWEL, 1.0f, 9.5f);
        env.world.createThing(Constants.JewelTypes.YELLOW_JEWEL, 1.2f, 9.5f);
        try {
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        //-------Explore Impulse (TEST ONLY) -----
        Idea impulse = new Idea("Impulse", "Explore", "Episode", 0);
        Idea state = new Idea("State", null, "Timestep", 0);
        Idea dest = new Idea("Self", null, "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X",0.5f, 3));
        posIdea.add(new Idea("Y",2f, 3));
        dest.add(posIdea);
        state.add(dest);
        state.add(new Idea("Desire", 0.1, "Property", 1));
        impulse.add(state);
        List<Memory> mems = a.getRawMemory().getAllMemoryObjects();
        for (Memory mo : mems) {
            if (mo.getName() != null && mo.getName().equalsIgnoreCase("IMPULSES")) {
                MemoryContainer moc = (MemoryContainer) mo;
                moc.setI(impulse, 0.1, "Explore");
                //System.out.println(fullPrint((Idea) mo.getI()));
            }
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        Idea impulse_ = new Idea("Impulse", "Explore", "Episode", 0);
        Idea state_ = new Idea("State", null, "Timestep", 0);
        Idea dest_ = new Idea("Self", null, "AbstractObject", 1);
        Idea posIdea_ = new Idea("Position", null, "Property", 1);
        posIdea_.add(new Idea("X",0.5f, 3));
        posIdea_.add(new Idea("Y",9f, 3));
        dest_.add(posIdea_);
        state_.add(dest_);
        state_.add(new Idea("Desire", 0.1, "Property", 1));
        impulse_.add(state_);
        for (Memory mo : mems) {
            if (mo.getName() != null && mo.getName().equalsIgnoreCase("IMPULSES")) {
                MemoryContainer moc = (MemoryContainer) mo;
                moc.setI(impulse_, 0.1, "Explore");
            }
        }
        //---------
    }
}
