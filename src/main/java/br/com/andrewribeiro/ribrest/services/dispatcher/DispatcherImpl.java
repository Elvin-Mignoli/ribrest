package br.com.andrewribeiro.ribrest.services.dispatcher;

import br.com.andrewribeiro.ribrest.core.model.Model;
import br.com.andrewribeiro.ribrest.services.dtos.FlowContainer;
import br.com.andrewribeiro.ribrest.services.dtos.Result;
import br.com.andrewribeiro.ribrest.services.miner.Miner;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 *
 * @author Andrew Ribeiro
 */
public class DispatcherImpl implements Dispatcher{
    
    @Inject
    FlowContainer flowContainer;
    
    GsonBuilder jsonBuilder = new GsonBuilder();
    
    @Override
    public Response send() {
        Miner currentMiner = flowContainer.getMiner();
        setModelsToResult();
        setupSerializationStrategy(currentMiner.extractIgnoredFields());
        return buildResultResponse();
    }
    
    private Response buildResultResponse(){
        return Response.status(flowContainer.getResult().getStatus()).entity(jsonBuilder.create().toJson(flowContainer.getResult())).build();
    }
    
    private void setupSerializationStrategy(List ignoreFields){
        BidirectionalModelsExclusionStrategy exclusionStrategy = new BidirectionalModelsExclusionStrategy(alwaysGetListOfModels(flowContainer.getResult()), ignoreFields);
        exclusionStrategy.removeCircularReferences();
        jsonBuilder.addDeserializationExclusionStrategy(exclusionStrategy);
    }
    
    private void setModelsToResult(){
        flowContainer.getResult().setHolder(flowContainer.getHolder());
    }
    
    private List<Model> alwaysGetListOfModels(Result result){
        return result.getHolder() == null ? new ArrayList() : result.getHolder().getModels();
    }
}
