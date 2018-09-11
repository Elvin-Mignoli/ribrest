/*
 * The MIT License
 *
 * Copyright 2018 ribeiro.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br.com.andrewribeiro.test.crud;

import br.com.andrewribeiro.test.RibrestTest;
import br.com.andrewribeiro.test.crud.models.ChildModel;
import br.com.andrewribeiro.test.crud.models.ModelCrud;
import br.com.andrewribeiro.test.crud.models.ModelWithManyToOneRelationship;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import junit.framework.Assert;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Andrew Ribeiro
 */
public class CrudWithManyToOneRelationshipTest extends RibrestTest{
    
    @Test
    public void postManyToOneModel(){
        MultivaluedMap mvm = new MultivaluedHashMap();
        mvm.add("model.name", "Child Of Many To One Model");
        Response response = post(ModelWithManyToOneRelationship.class, new Form(mvm));
        
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }
    
    @Test
//    @Ignore
    public void postManyToOneModelWithExistentChild(){
        Response response = post(ModelCrud.class, new Form());
        
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        String responseText = response.readEntity(String.class);
        JSONObject jsonObject = new JSONObject(responseText);
        Long childId = jsonObject.getJSONObject("holder").getJSONArray("models").getJSONObject(0).getLong("id");
        
        MultivaluedMap mvm = new MultivaluedHashMap();
        mvm.add("model.id", childId.toString());
        
        response = post(ModelWithManyToOneRelationship.class, new Form(mvm));
        
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        
        responseText = response.readEntity(String.class);
        
        jsonObject = new JSONObject(responseText);
        Assert.assertEquals(childId.longValue(), jsonObject.getJSONObject("holder").getJSONArray("models").getJSONObject(0).getJSONObject("model").getLong("id"));
    }
}