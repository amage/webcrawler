package org.playstat.crawler.dop;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.playstat.crawler.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;

public class DOPWrapper {
    private final WebClient web;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public DOPWrapper(WebClient web) {
        this.web = web;
    }

    public <T> T get(Class<T> pageClass, String... params) {
        final String pageUrl = MessageFormat.format(pageClass.getAnnotation(Page.class).value(), (Object[]) params);
        try {
            return createDataObject(pageClass, web.go(pageUrl));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private <T> T createDataObject(Class<T> clazz, Element element) throws Exception {
        final Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        final T newInstance = constructor.newInstance();

        if (clazz.getAnnotation(Selector.class) == null && clazz.getAnnotation(Page.class) == null) {
            return newInstance;
        }

        final Field[] declaredFields = clazz.getDeclaredFields();

        for (final Field f : declaredFields) {
            final Class<?> fieldClass = f.getType();

            if (fieldClass.equals(java.util.List.class) && f.getAnnotation(Selector.class) == null) {
                solveListOfAnnotatedType(element, newInstance, f);
            }

            if (f.getAnnotation(Selector.class) != null) {
                solveAnnotatedField(element, newInstance, f, fieldClass);
            }

            // if (fieldClass.getAnnotation(Selector.class) != null) {
            // solveUnanotatedFieldOfAnnotatedType(element, newInstance, f,
            // fieldClass);
            // }

        }

        final Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method m : declaredMethods) {
            if (m.getAnnotation(Extractor.class) != null) {
                callExtractorMethod(element, newInstance, m);
            }
        }
        return newInstance;
    }

    private <T> void callExtractorMethod(Element element, T targetObject, Method m) {
        try {
            final String targetFieldName = m.getAnnotation(Extractor.class).value();
            final Field targetField = targetObject.getClass().getDeclaredField(targetFieldName);
            if (!m.getReturnType().equals(targetField.getType())) {
                throw new NoSuchFieldException(
                        targetFieldName + " " + targetField.getType() + " doesn't fit to " + m.getReturnType());
            }
            final Object result = m.invoke(targetObject, element);
            targetField.setAccessible(true);
            targetField.set(targetObject, result);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException
                | SecurityException e) {
            log.error(e.getMessage(), e);
        }
    }
//---
    private <T> void solveAnnotatedField(final Element node, final T targetObject, final Field field,
            final Class<?> fieldClass) throws Exception {
        if (fieldClass.equals(java.util.List.class)) {
            solveAnnotatedListField(node, targetObject, field);
        } else {
            solveAnnotatedFieldWithMappableType(node, targetObject, field, fieldClass);
        }
    }

    private <T> void solveListOfAnnotatedType(Element node, T newInstance, Field f) throws Exception {
        final Type genericType = f.getGenericType();
        final Class<?> listClass = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        final Selector selectorAnnotation = listClass.getAnnotation(Selector.class);
        if (selectorAnnotation == null) {
            return;
        }

        final String cssQuery = selectorAnnotation.value();
        final Elements nodes = node.select(cssQuery);
        f.setAccessible(true);
        f.set(newInstance, populateList(nodes, selectorAnnotation, listClass));
    }

    private <T> void solveAnnotatedListField(final Element node, final T newInstance, final Field f)
            throws Exception {
        final Type genericType = f.getGenericType();
        final Selector selector = f.getAnnotation(Selector.class);
        final String cssQuery = selector.value();
        final Elements nodes = node.select(cssQuery);
        final Type type = ((ParameterizedType) genericType).getActualTypeArguments()[0];
        if (type instanceof ParameterizedType) {
            f.setAccessible(true);
            f.set(newInstance, populateListOfLinks(nodes, (ParameterizedType) type));
            return;
        }
        final Class<?> listClass = (Class<?>) type;
        f.setAccessible(true);
        f.set(newInstance, populateList(nodes, selector, listClass));
    }

    private <T> List<T> populateList(Elements nodes, Selector selector, Class<T> clazz) throws Exception {
        final ArrayList<T> newInstanceList = new ArrayList<>();
        for (final Element node : nodes) {
            if (KnownTypes.isKnown(clazz)) {
                newInstanceList.add(KnownTypes.instanceForNode(node, selector, clazz));
            } else {
                newInstanceList.add(createDataObject(clazz, node));
            }
        }
        return newInstanceList;
    }

    private static <T> ArrayList<Link<T>> populateListOfLinks(final Elements nodes, final ParameterizedType paraType)
            throws InstantiationException, IllegalAccessException {
        final ArrayList<Link<T>> newInstanceList = new ArrayList<>();
        for (final Element node : nodes) {
            final Class<?> clazz = (Class<?>) paraType.getActualTypeArguments()[0];
            final Link<T> link = null; // Instantiator.visitableForNode(node,
                                       // clazz, "FIXME some link");
            newInstanceList.add(link);
        }
        return newInstanceList;
    }

    private static <T> void solveAnnotatedFieldWithMappableType(final Element node, final T newInstance, final Field f,
            final Class<?> fieldClass) throws Exception {
        final Selector selectorAnnotation = f.getAnnotation(Selector.class);
        final String cssQuery = selectorAnnotation.value();
        final Element selectedNode = getFirstOrNullOrCryIfMoreThanOne(node, cssQuery);
        if (selectedNode == null)
            return;

        if (Instantiator.typeIsVisitable(fieldClass)) {
            final Class<?> visitableGenericClass = TypeToken.of(f.getGenericType())
                    .resolveType(Link.class.getTypeParameters()[0]).getRawType();
            f.setAccessible(true);
            f.set(newInstance, Instantiator.visitableForNode(selectedNode, visitableGenericClass, "FIXME add url"));
            return;
        }

        if (KnownTypes.isKnown(fieldClass)) {
            f.setAccessible(true);
            f.set(newInstance, Instantiator.instanceForNode(selectedNode, selectorAnnotation, fieldClass));
            return;
        }

        throw new RuntimeException(
                "Can't convert html to class " + fieldClass.getName() + "\n" + "The field type must be a class with "
                        + Page.class.getSimpleName() + " annotation or one of these types:\n" +
                        String.join("\n",
                                List.class.getCanonicalName(),
                                String.class.getCanonicalName(),
                                Integer.class.getCanonicalName(),
                                Float.class.getCanonicalName(),
                                Boolean.class.getCanonicalName(),
                                Link.class.getCanonicalName(),
                                Element.class.getCanonicalName(),
                                Date.class.getCanonicalName()));
    }

    private static Element getFirstOrNullOrCryIfMoreThanOne(final Element node, final String cssQuery)
            throws Exception {
        final Elements elements = node.select(cssQuery);
        final int size = elements.size();
        if (size > 1) {
            throw new Exception(cssQuery);
        }
        if (size == 0) {
            return null;
        }
        return elements.first();
    }
}
