package WS3DEpisodeDataset.categories;

import br.unicamp.cst.representation.idea.*;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class EventCategory implements Category {

    private final List<String> vectorPropertiesList;
    private final List<String> contextPropertiesList;
    protected String name;

    public EventCategory(String name, List<String> properiesList) {
        this.name = name;
        this.vectorPropertiesList = properiesList;
        contextPropertiesList = new ArrayList<>();
    }

    public EventCategory(String name, List<String> vectorPropertiesList, List<String> contextPropertiesList) {
        this.name = name;
        this.vectorPropertiesList = vectorPropertiesList;
        this.contextPropertiesList = contextPropertiesList;
    }

    @Override
    public double membership(Idea idea) {
        List<ArrayRealVector> propertiesVector = new ArrayList<>();
        for (Idea step : idea.getL())
            propertiesVector.add(extractProperties(step));

        boolean check = checkVectorChange(propertiesVector);
        if (check)
            return 1.0;
        return 0;
    }

    protected abstract boolean checkVectorChange(List<ArrayRealVector> propertiesVector);


    private ArrayRealVector extractProperties(Idea idea) {
        ArrayRealVector propertyVector = new ArrayRealVector();
        for (String property : vectorPropertiesList){
            if (idea.get(property).getValue() instanceof Float)
                propertyVector = (ArrayRealVector) propertyVector.append((float) idea.get(property).getValue());
            if (idea.get(property).getValue() instanceof Integer)
                propertyVector = (ArrayRealVector) propertyVector.append((int) idea.get(property).getValue());
            if (idea.get(property).getValue() instanceof Double)
                propertyVector = (ArrayRealVector) propertyVector.append((double) idea.get(property).getValue());
        }
        return propertyVector;
    }

    @Override
    public Idea getInstance(List<Idea> constraints) {
        if (constraints != null){
            Idea eventIdea = new Idea("Event", this.name, "Episode", 1);
            int count = 1;
            for (Idea timeStep : constraints){
                Idea timeStepIdea = new Idea("", count++, "TimeStep", 1);
                timeStepIdea.add(timeStep);
                timeStepIdea.add(timeStep.get("TimeStamp"));
                eventIdea.add(timeStepIdea);
            }
            return eventIdea;
        }
        return null;
    }

    private List<Idea> extractRelevant(Idea idea) {
        Idea extracted = new Idea(idea.getName(), idea.getValue(), idea.getType());
        for (String property : vectorPropertiesList){
            extractMerge(extracted, idea, property);
        }
        for (String property : contextPropertiesList){
            extractMerge(extracted, idea, property);
        }
        return Arrays.asList(extracted, idea.get("TimeStamp"));
    }

    private Idea extractMerge(Idea copy, Idea original, String path){
        Idea i = copy;
        Idea j = original;
        String[] spath = path.split("\\.");
        for (int k=1; k<spath.length; k++){
            Idea i_ = i.get(spath[k]);
            j = j.get(spath[k]);
            if (i_ == null){
                i_ = new Idea(j.getName(), j.getValue(), j.getType());
                i.add(i_);
            }
            i = i_;
        }
        return copy;
    }
}
