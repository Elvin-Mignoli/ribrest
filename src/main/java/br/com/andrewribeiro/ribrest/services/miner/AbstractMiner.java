package br.com.andrewribeiro.ribrest.services.miner;

import br.com.andrewribeiro.ribrest.exceptions.RibrestDefaultException;
import br.com.andrewribeiro.ribrest.model.IModel;
import br.com.andrewribeiro.ribrest.services.FlowContainer;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.glassfish.jersey.server.ContainerRequest;

/**
 *
 * @author Andrew Ribeiro
 */
public abstract class AbstractMiner implements IMiner {

    @Inject
    protected FlowContainer fc;

    protected MultivaluedMap<String, String> form;
    protected MultivaluedMap<String, String> query;
    protected MultivaluedMap<String, String> path;
    protected MultivaluedMap<String, String> header;

    protected List accepts;

    private List ignored;

    @Override
    public Response send(FlowContainer fc) {
        GsonBuilder gb = new GsonBuilder();
        if (fc.shouldGo()) {

            IModel m = (IModel) fc.getHolder().getModels().get(0);

            ignored = mineAttributes(m.getIgnoredAttributes(), accepts);

            gb.addDeserializationExclusionStrategy(new GenericExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fa) {
                    return ignored.contains(fa.getName());
                }
            });

        }

        Gson g = gb.create();

        return Response.status(fc.getResult().getStatus()).entity(g.toJson(fc.getResult()))
                .build();

    }

    @Override
    public void extract(ContainerRequest cr) throws RibrestDefaultException {
        setupData(cr);
    }

    protected void fill(IModel model) throws IllegalArgumentException, IllegalAccessException {
        List<Field> l = model.getAllAttributes();
        for (Field attribute : l) {
            attribute.setAccessible(true);

            /*Firstly, let's check for String types*/
            if (attribute.getClass().getSimpleName().equals("String")) {
                /*Fill the equivalent name to the model attribute*/
                String value = form.getFirst(attribute.getName());
                attribute.set(model, value);
            }
        }
    }

    private void setupData(ContainerRequest cr) {
        /*Populating form attribute with data coming from ContainerRequest*/
        Form f = cr.readEntity(Form.class);
        form = f.asMap();

        /*Populating query parameters with data coming from ContainerRequest*/
        UriInfo u = cr.getUriInfo();
        query = u.getQueryParameters();

        /*Populating path parameters with data coming from ContainerRequest*/
        path = u.getPathParameters();

        /*Populating header with data coming from ContainerRequest*/
        header = cr.getHeaders();

        /*GETTING THE ACCEPTS*/
        accepts = query != null ? query.get("accepts") : new ArrayList();
        accepts = accepts != null ? accepts : new ArrayList();
    }

    protected List mineAttributes(List ignored, List accepted) {
        ignored = ignored != null ? ignored : new ArrayList();
        accepted = accepted != null ? accepted : new ArrayList();

        ignored.removeAll(accepted);

        return new ArrayList(ignored);
    }

}