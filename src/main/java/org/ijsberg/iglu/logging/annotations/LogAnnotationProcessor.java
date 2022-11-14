package org.ijsberg.iglu.logging.annotations;

import org.ijsberg.iglu.util.io.FileSupport;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

//@SupportedSourceVersion(SourceVersion.latestSupported())
/*@SupportedAnnotationTypes({
        // Set of full qullified annotation type names
        "org.ijsberg.iglu.logging.annotations.IgluLogging"
})*/
//org.ijsberg.iglu.logging.annotations.LogAnnotationProcessor


public class LogAnnotationProcessor extends AbstractProcessor {

    private static final String LOG_FILE = "/development/lab/LogAnnotationProcessor.log";

    public LogAnnotationProcessor() {
        //required default constructor
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        System.out.println("init LogAnnotationProcessor");
        FileSupport.appendToTextFile(new Date() + " init LogAnnotationProcessor", LOG_FILE);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        System.out.println("process LogAnnotationProcessor");
        FileSupport.appendToTextFile(new Date() + " process LogAnnotationProcessor", LOG_FILE);
        FileSupport.appendToTextFile(new Date() + " annotations: " + annotations, LOG_FILE);
        FileSupport.appendToTextFile(new Date() + " env -> elements annotated: " + env.getElementsAnnotatedWith(IgluLogging.class), LOG_FILE);
        
        for(Element element : env.getElementsAnnotatedWith(IgluLogging.class)) {
           //element.
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        System.out.println("getSupportedAnnotationTypes LogAnnotationProcessor");
        FileSupport.appendToTextFile(new Date() + " getSupportedAnnotationTypes LogAnnotationProcessor", LOG_FILE);
        Set<String> returnValue = new HashSet<>();
        returnValue.add(IgluLogging.class.getCanonicalName());
        return returnValue;
    }
    //org.ijsberg.iglu.logging.annotations.IgluLogging

    @Override
    public SourceVersion getSupportedSourceVersion() {
        System.out.println("getSupportedSourceVersion LogAnnotationProcessor");
        FileSupport.appendToTextFile(new Date() + " getSupportedSourceVersion LogAnnotationProcessor", LOG_FILE);
        return SourceVersion.RELEASE_11;
    }

    public static void main(String[] args) {
        System.out.println(IgluLogging.class.getCanonicalName());
    }

}