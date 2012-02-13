package net.walnutvision.test;

import java.lang.reflect.Type;  
import java.lang.reflect.Field;  
import java.lang.reflect.ParameterizedType;  
import java.util.ArrayList;  
import java.util.LinkedList;
   
public class GenericTypeInfo {  
    java.util.List<? extends Foo> fooList = new ArrayList<Bar>();  
    java.util.List<String> strList = new LinkedList<String>();
    String str = "hello";
    int a = 0;
    float b = -1.0f;
    public static void main(String[] args) throws Exception {  
        Field field = GenericTypeInfo.class.getDeclaredField("strList");  
  
        Type type = field.getGenericType();  
        System.out.println("type: " + type);  
        if (type instanceof ParameterizedType) {  
            ParameterizedType pt = (ParameterizedType) type;  
            Type[] typeArguments = pt.getActualTypeArguments();
            Class cls = (Class)typeArguments[0];
           
            System.out.println(cls.getName());
            System.out.println("raw type: " + pt.getRawType());  
            System.out.println("owner type: " + pt.getOwnerType());  
            System.out.println("actual type args:");  
            for (Type t : pt.getActualTypeArguments()) {  
                System.out.println("    " + t);  
            }  
        }  
//   
//        System.out.println();  
//   
//        Object obj = field.get(new GenericTypeInfo());  
//        System.out.println("obj: " + obj);  
//        System.out.println("obj class: " + obj.getClass());  
    }  
   
    static class Foo {}  
   
    static class Bar extends Foo {}  
}  