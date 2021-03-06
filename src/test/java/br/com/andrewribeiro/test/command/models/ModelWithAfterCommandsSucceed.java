package br.com.andrewribeiro.test.command.models;

import br.com.andrewribeiro.ribrest.core.annotations.RibrestEndpointConfigurator;
import br.com.andrewribeiro.ribrest.core.annotations.RibrestModel;
import br.com.andrewribeiro.test.command.commands.AfterCommandSucceed;
import javax.persistence.Entity;

/**
 *
 * @author Andrew Ribeiro
 */
@Entity
@RibrestModel(defaultEndpointsConfigurators = {
    @RibrestEndpointConfigurator(afterCommands = AfterCommandSucceed.class)
})
public class ModelWithAfterCommandsSucceed extends br.com.andrewribeiro.ribrest.core.model.AbstractModel{
    
}
