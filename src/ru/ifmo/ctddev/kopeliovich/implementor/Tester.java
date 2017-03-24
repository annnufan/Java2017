package ru.ifmo.ctddev.kopeliovich.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.nio.file.Paths;

/**
 * Created by Kopeliovicha Anna on 27.02.2017.
 */
public class Tester {
    public static void main(String[] args) throws ImplerException {
        Implementor imp = new Implementor();
        imp.implement(javax.xml.bind.Element.class, Paths.get("Test"));

        //imp.implementJar(javax.xml.bind.Element.class,);
    }
}
