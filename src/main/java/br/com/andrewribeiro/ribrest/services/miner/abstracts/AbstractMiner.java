package br.com.andrewribeiro.ribrest.services.miner.abstracts;

import br.com.andrewribeiro.ribrest.services.miner.interfaces.Miner;
import br.com.andrewribeiro.ribrest.exceptions.RibrestDefaultException;
import br.com.andrewribeiro.ribrest.services.FlowContainer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.glassfish.jersey.server.ContainerRequest;
import br.com.andrewribeiro.ribrest.model.interfaces.Model;
import javax.persistence.OneToOne;

/**
 *
 * @author Andrew Ribeiro
 */
public abstract class AbstractMiner implements Miner {

    @Inject
    protected FlowContainer fc;

    protected MultivaluedMap<String, String> form;
    protected MultivaluedMap<String, String> query;
    protected MultivaluedMap<String, String> path;
    protected MultivaluedMap<String, String> header;

    protected List accepts;

    private List ignored;

    @Override
    public void extractDataFromRequest(ContainerRequest cr) throws RibrestDefaultException {
        prepareDataObjects(cr);
    }

    @Override
    public List extractIgnoredFields() {
        ignored = ignored != null ? ignored : new ArrayList();
        accepts = accepts != null ? accepts : new ArrayList();

        ignored.removeAll(accepts);

        return new ArrayList(ignored);
    }

    private void prepareDataObjects(ContainerRequest cr) {
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

    protected void fillModel(Model model) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        try {
            model.setId(Long.parseLong(path.getFirst("id")));
        } catch (NumberFormatException nfe) {}

        for (Field attribute : model.getAllAttributesExceptId()) {
            fillAttribute(new FieldHelper(attribute, model, attribute.getName()));
        }
    }

    private void fillAttribute(FieldHelper fieldHelper) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        if (fieldHelper.attribute.getType() == String.class
                || fieldHelper.attribute.getType() == Long.class) {
            fieldHelper.fillNonEntityAttribute();
        } else if (fieldHelper.attribute.isAnnotationPresent(OneToOne.class)) {
            fieldHelper.fillEntityAttribute();
        }
    }

    private void fillChildModel(Model childModel, String parentAttributeName) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        for (Field attribute : childModel.getAllAttributes()) {
            fillAttribute(new FieldHelper(attribute, childModel, parentAttributeName + "." + attribute.getName()));
        }
    }

    class FieldHelper {

        public FieldHelper(Field attribute, Model model, String parameterName) {
            this.attribute = attribute;
            this.model = model;
            this.parameterName = parameterName;
        }

        Field attribute;
        Model model;
        String parameterName;
        Object parameterValue;

        void fillNonEntityAttribute() throws IllegalArgumentException, IllegalAccessException {
            parameterValue = form.getFirst(parameterName);
            fill();
        }

        void fillEntityAttribute() throws InstantiationException, IllegalAccessException {
            parameterValue = attribute.getType().newInstance();
            fillChildModel((Model) parameterValue, attribute.getName());
            fill();
        }

        void fill() throws IllegalArgumentException, IllegalAccessException {
            attribute.setAccessible(true);
            attribute.set(model, parseBeforeSetParameterValue(parameterValue));
        }

        Object parseBeforeSetParameterValue(Object parameterValue) {
            if (attribute.getType() == Long.class) {
                parameterValue = Long.parseLong((String) parameterValue);
            }

            return parameterValue;
        }
    }
}