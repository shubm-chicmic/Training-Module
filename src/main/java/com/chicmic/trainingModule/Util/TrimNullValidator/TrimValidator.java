package com.chicmic.trainingModule.Util.TrimNullValidator;


import com.fasterxml.jackson.databind.util.StdConverter;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;



public class TrimValidator  extends StdConverter<String, String> implements Converter<String,String> , ConstraintValidator<TrimAll, Object> {
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();
    private String[] fields;
    private String[] excludeFields;
    private String messages;
    public String convert(String value) {
        if (value == null){
            return null;
        }
        return value.trim();
    }

    @Override
    public void initialize(TrimAll constraintAnnotation) {
            fields = constraintAnnotation.value();
            messages = constraintAnnotation.message();
            excludeFields = constraintAnnotation.exclude();
            Arrays.sort(excludeFields);


        System.out.println("\u001B[31m" + "Fields length =  " + fields.length + "\u001B[0m");
        System.out.println("\u001B[31m" + "Exclude fieldslength = " + excludeFields.length + "\u001B[0m");

    }


    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        System.out.println("\u001B[31m" + "Stringhere = " + value + "\u001B[0m");
        messages = "";
        if(fields.length == 0) {
            Field[] tempFields = value.getClass().getDeclaredFields();
            fields= Stream.of(tempFields).filter(data->(data.getType()==String.class)).map(data-> data.getName()).toArray(String[] :: new);
            System.out.println("\u001B[31m" + "size = " + fields.length  + "\u001B[0m");
         }

        long notNull = Stream.of(fields)
                .map(field -> {
                    if(Arrays.binarySearch(excludeFields, field) < 0) {
//                        System.out.println("\u001B[36m" + "field value = " + (field) + "\u001B[0m");
                        try {
                            Field fields1 = value.getClass().getDeclaredField(field);
                            if((fields1.getType() == String.class)) {
                                fields1.setAccessible(true);
                                Object val = fields1.get(value);
//                                System.out.println("val = " + val);
                                if(val != null) {
                                    if(val.toString().trim() == ""){

                                        messages += (field + " is Empty,");
                                    }
                                    fields1.set(value, val.toString().trim());
                                } else {
                                    System.out.println("inside else");

                                   messages += (field + " is Null,");
                                }
                            }
                        } catch (NoSuchFieldException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
//                        System.out.println("\u001B[36m" + "field value = " + PARSER.parseExpression(field).getValue(value) + "\u001B[0m");
                    }
                    return PARSER.parseExpression(field).getValue(value);} )
                .filter((data)->{
                    return Objects.nonNull(data) && !(data.equals(""));
                })
                .count();


        System.out.println("\u001B[35m" + messages + "\u001B[0m");


        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(messages).addConstraintViolation();
        return notNull == 0 || notNull == fields.length;
    }


}
