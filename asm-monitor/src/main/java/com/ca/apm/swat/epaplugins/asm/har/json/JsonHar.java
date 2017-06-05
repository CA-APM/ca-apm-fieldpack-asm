package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.har.Har;
import com.ca.apm.swat.epaplugins.asm.har.IterableClass;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import com.ca.apm.swat.epaplugins.asm.har.Log;
import com.ca.apm.swat.epaplugins.asm.har.OptionalItem;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import javax.xml.bind.DatatypeConverter;

public class JsonHar implements Har {
    private final JSONObject har;
    protected Log log;

    public JsonHar(JSONObject json) {
        har = json.getJSONObject("log");
        log = new JsonLog(har);
    }

    @Override
    public Log getLog() {
        return log;
    }

    // [RO] 2017/05/16
    // Unused at this point due to runtime problems where this method would not return 
//    @SuppressWarnings("unchecked")
//    private <T> T parse(final Class<T> type, final JSONObject o) {
//        return (T)Proxy.newProxyInstance(
//                type.getClassLoader(),
//                new java.lang.Class[] { type },
//                new InvocationHandler() {
//                    @Override
//          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                        
//                        Class<?> ret = method.getReturnType();
//                        String name = method.getName();
//                        // remove the get part of the name  getOnLoad = onLoad
//                        String key = name.substring(3, 4).toLowerCase()+name.substring(4);
//                        boolean isOptional = method.getAnnotation(OptionalItem.class) != null;
//
//
//       /* If the return type is of a "Har" package type", recurse with the new return type */
//                        if(ret.getPackage().equals(Har.class.getPackage())) {
//                   JSONObject value = isOptional ? o.optJSONObject(key) : o.getJSONObject(key);
//                            if(value == null) {
//                                return null;
//                            }
//                            return parse(ret, value);
//                        }
//         
//                        if(ret.isAssignableFrom(Iterable.class)) {
//                      JSONArray value = isOptional ? o.optJSONArray(key) : o.getJSONArray(key);
//                            if(value == null) {
//                                return null;
//                            }                            
//                            return new JSONObjectIterable(
//                                    method.getAnnotation(IterableClass.class).type(),
//                                    value
//                                );
//                        }
//                        if(ret.isAssignableFrom(Calendar.class)) {
//                            String value = isOptional ? o.optString(key) : o.getString(key);
//                            if(value == null) {
//                                return null;
//                            }
//                            return DatatypeConverter.parseDateTime(value);
//                        }
//                        return isOptional ? o.opt(key) : o.get(key);
//                    };
//                }
//            );
//    }
//
//    private class JSONObjectIterable<T> implements Iterable<T> {
//        private final Class<T> type;
//        private final JSONArray array;
//
//        public JSONObjectIterable(Class<T> type, JSONArray array) {
//            this.array = array;
//            this.type = type;
//        }
//
//        @Override
//        public Iterator<T> iterator() {
//            return new Iterator<T>() {
//                private int i = 0;
//
//                @Override
//                public boolean hasNext() {
//                    return i < array.length();
//                }
//
//                @Override
//                public T next() {
//                    if(!hasNext()) {
//                        throw new NoSuchElementException();
//                    }
//                    return parse(type, array.getJSONObject(i++));
//                }
//                @Override
//                public void remove() {
//                    throw new UnsupportedOperationException();
//                }
//            };
//        }
//    }
}
