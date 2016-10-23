package org.playstat.crawler;

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
import org.playstat.crawler.dop.Instantiator;
import org.playstat.crawler.dop.KnownTypes;
import org.playstat.crawler.dop.Link;
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

    private static <T> T createDataObject(Class<T> clazz, Element element) throws Exception {
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
                callExtractorMathod(element, newInstance, m);
            }
        }
        return newInstance;
    }

    private static <T> void callExtractorMathod(Element element, T newInstance, Method m) {
        try {
            if (!m.isAccessible()) {
                m.setAccessible(true);
            }
            String targetFieldName = m.getAnnotation(Extractor.class).value();
            Field targetField = newInstance.getClass().getDeclaredField(targetFieldName);
            if (!m.getReturnType().equals(targetField.getType())) {
                throw new NoSuchFieldException(
                        targetFieldName + " " + targetField.getType() + " dosn't fit to " + m.getReturnType());
            }
            Object result = m.invoke(newInstance, element);
            targetField.setAccessible(true);
            targetField.set(newInstance, result);
            System.out.println(result);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException
                | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO Auto-generated method stub
    }

    private static <T> void solveAnnotatedField(final Element node, final T newInstance, final Field f,
            final Class<?> fieldClass) throws Exception {
        if (fieldClass.equals(java.util.List.class)) {
            solveAnnotatedListField(node, newInstance, f);
        } else {
            solveAnnotatedFieldWithMappableType(node, newInstance, f, fieldClass);
        }
    }

    private static <T> void solveListOfAnnotatedType(Element node, T newInstance, Field f) throws Exception {
        final Type genericType = f.getGenericType();
        final Class<?> listClass = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        final Selector selectorAnnotation = listClass.getAnnotation(Selector.class);
        if (selectorAnnotation == null)
            return;

        final String cssQuery = selectorAnnotation.value();
        final Elements nodes = node.select(cssQuery);
        f.setAccessible(true);
        f.set(newInstance, populateList(nodes, selectorAnnotation, listClass));
    }

    private static <T> void solveAnnotatedListField(final Element node, final T newInstance, final Field f)
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

    private static <T> List<T> populateList(Elements nodes, Selector selector, Class<T> clazz) throws Exception {
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
                        + Page.class.getSimpleName() + " annotation or one of these types:\n"
                        + List.class.getCanonicalName() + "\n" + String.class.getCanonicalName() + "\n"
                        + Integer.class.getCanonicalName() + "\n" + Float.class.getCanonicalName() + "\n"
                        + Boolean.class.getCanonicalName() + "\n" + Link.class.getCanonicalName() + "\n"
                        + Element.class.getCanonicalName() + "\n" + Date.class.getCanonicalName() + "\n");
    }

    private static Element getFirstOrNullOrCryIfMoreThanOne(final Element node, final String cssQuery)
            throws Exception {
        final Elements elements = node.select(cssQuery);
        final int size = elements.size();
        if (size > 1) {
            throw new Exception(cssQuery.toString());
        }
        if (size == 0) {
            return null;
        }
        return elements.first();
    }
}
