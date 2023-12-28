package com.chicmic.trainingModule.Listener;


import com.chicmic.trainingModule.annotation.CascadeSave;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

@AllArgsConstructor
public class CascadeSaveMongoEventListener extends AbstractMongoEventListener<Object> {

    @Autowired
    private MongoOperations mongoOperations;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object source = event.getSource();
        saveReferencedObjects(source);
    }

    private void saveReferencedObjects(Object source) {
        Field[] fields = source.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(DBRef.class) && field.isAnnotationPresent(CascadeSave.class)) {
                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(source);
                    if (fieldValue != null) {
                        mongoOperations.save(fieldValue);
                        // If fieldValue is a collection, you might want to iterate through and save each element
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
