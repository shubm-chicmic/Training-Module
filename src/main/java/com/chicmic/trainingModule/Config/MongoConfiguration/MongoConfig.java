package com.chicmic.trainingModule.Config.MongoConfiguration;

import com.chicmic.trainingModule.Listener.CascadeSaveMongoEventListener;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Autowired
    private MongoOperations mongoOperations;

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create();
    }

    @Override
    protected String getDatabaseName() {
        return "yourDatabaseName";
    }

    @Bean
    public CascadeSaveMongoEventListener cascadeSaveMongoEventListener() {
        return new CascadeSaveMongoEventListener(mongoOperations);
    }
}

