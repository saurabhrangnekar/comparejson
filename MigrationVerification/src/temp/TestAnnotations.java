package temp;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestAnnotations
{
	public static List<Class> findAnnotatedMethods(Class<? extends Annotation> annotationClass)
	{
		return null;
	}
	
	public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationClass)
	{
		Method[] methods = clazz.getMethods();
		System.out.println(methods.length);
		System.out.println(methods[0].getName());
		List<Method> annotatedMethods = new ArrayList<Method>(methods.length);
		
//		System.out.println("annotationClass="+ annotationClass);
		
		for (Method method : methods)
		{
			if (method.isAnnotationPresent(annotationClass))
			{
				MessageConsumer annotation = (MessageConsumer) method.getAnnotation(annotationClass);
				System.out.println(annotation.types()[0]); 
				annotatedMethods.add(method);
			}
		}
		return annotatedMethods;
	}

	public static void findClases() {
        System.out.println("Scanning using Reflections:");
 
        Reflections ref = new Reflections("com.farenda.java.lang");
        for (Class<?> cl : ref.getTypesAnnotatedWith(Findable.class)) {
            Findable findable = cl.getAnnotation(Findable.class);
            System.out.printf("Found class: %s, with meta name: %s%n",
                    cl.getSimpleName(), findable.name());
        }
    }
	
	public static void main(String[] args)
	{
//		System.out.println(findAnnotatedMethods(NotificationService.class, MessageConsumer.class));
		
//		List lst = new ArrayList();
//		System.out.println(lst.getClass());		
	}

}
