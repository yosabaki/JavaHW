package ru.ifmo.rain.ustinov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentGroupQuery {

    private static final Comparator<Student> STUDENT_NAME_COMPARATOR =
            Comparator.comparing(Student::getLastName)
                    .thenComparing(Student::getFirstName).
                    thenComparing(Student::getId);

    private Stream<Map.Entry<String,List<Student>>> getGroupEntryStream(Collection<Student> collection) {
        return collection.stream()
                .collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList()))
                .entrySet()
                .stream();
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> collection) {
        return  getGroupEntryStream(collection)
                .map(s -> new Group(s.getKey(), sortStudentsByName(s.getValue())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> collection) {
        return getGroupEntryStream(collection)
                .map(s -> new Group(s.getKey(), sortStudentsById(s.getValue())))
                .collect(Collectors.toList());
    }

    @Override
    public String getLargestGroup(Collection<Student> collection) {
        return getGroupEntryStream(collection)
                .max(Comparator.comparingInt(a -> a.getValue().size()))
                .map(Map.Entry::getKey).orElse("");
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> collection) {
        return getGroupEntryStream(collection)
                .max(Comparator.comparingLong(a -> a.getValue().stream()
                        .map(Student::getFirstName)
                        .distinct()
                        .count()))
                .map(Map.Entry::getKey).orElse("");
    }

    @Override
    public List<String> getFirstNames(List<Student> list) {
        return list.stream().map(Student::getFirstName).collect(Collectors.toList());
    }

    @Override
    public List<String> getLastNames(List<Student> list) {
        return list.stream().map(Student::getLastName).collect(Collectors.toList());
    }

    @Override
    public List<String> getGroups(List<Student> list) {
        return list.stream().map(Student::getGroup).collect(Collectors.toList());
    }

    @Override
    public List<String> getFullNames(List<Student> list) {
        return list.stream().map(s -> s.getFirstName() + ' ' + s.getLastName()).collect(Collectors.toList());
    }


    @Override
    public Set<String> getDistinctFirstNames(List<Student> list) {
        return list.stream().map(Student::getFirstName).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> list) {
        return list.stream().min(Comparator.comparing(Student::getId)).map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> collection) {
        return collection.stream()
                .sorted(Comparator.comparing(Student::getId))
                .collect(Collectors.toList());
    }

    private Stream<Student> getStudentsSortedByName(Collection<Student> collection) {
        return collection.stream().sorted(STUDENT_NAME_COMPARATOR);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> collection) {
        return getStudentsSortedByName(collection)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> collection, String s) {
        return getStudentsSortedByName(collection)
                .filter(student -> student.getFirstName().equals(s))
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> collection, String s) {
        return getStudentsSortedByName(collection)
                .filter(student -> student.getLastName().equals(s))
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> collection, String s) {
        return getStudentsSortedByName(collection)
                .filter(student -> student.getGroup().equals(s))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> collection, final String group) {
        return collection.stream()
                .filter(student -> student.getGroup().equals(group))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, (a, b) -> (a.compareTo(b) < 0 ? a : b)));
    }
}

