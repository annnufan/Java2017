package ru.ifmo.ctddev.kopeliovich.arrayset;

import java.util.*;

/**
 * Created by Kopeliovich Anna on 16.02.2017.
 */
public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final List<E> data;
    private final Comparator<? super E> comp;

    public ArraySet() {
        this(Collections.emptyList(), (Comparator<? super E>)Comparator.naturalOrder());
    }

    public ArraySet(Collection<? extends E> sourceData) {
        this(sourceData, (Comparator<? super E>)Comparator.naturalOrder());
        System.out.println("op");
    }

    public ArraySet(Collection<? extends E> sourceData, Comparator<? super E> sourceComp) {
        System.out.println("ArraySet.ArraySet");
        comp = sourceComp;
        TreeSet<E> container = new TreeSet<>(sourceComp);
        container.addAll(sourceData);
        data = new ArrayList<>(container);
    }

    public ArraySet(List<E> sourceData, Comparator<? super E> sourceComp) {
        System.out.println("=this=");
        data = sourceData;
        comp = sourceComp;
    }

    @Override
    public E lower(E e) {
        int ind = downInd(e, false);
        return ind < 0 ? null : data.get(ind);
    }

    private int downInd(E e, boolean inclusive) {
        int ind = Collections.binarySearch(data, e, comp);
        int diff = inclusive ? 0 : 1;
        if (ind >= 0) {
            return ind - diff;
        }
        return -(ind + 1) - 1;
    }

    @Override
    public E floor(E e) {
        int ind = downInd(e, true);
        return ind < 0 ? null : data.get(ind);
    }

    @Override
    public E ceiling(E e) {
        int ind = upperInd(e, true);
        return ind >= size() ? null : data.get(ind);
    }

    private int upperInd(E e, boolean inclusive) {
        int ind = Collections.binarySearch(data, e, comp);
        int diff = inclusive ? 0 : 1;
        if (ind >= 0) {
            return ind + diff;
        }
        return -(ind + 1);
    }

    @Override
    public E higher(E e) {
        int ind = upperInd(e, false);
        return ind >= size() ? null : data.get(ind);
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("pollFirst");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("pollLast");
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new ReversedArrayList<>(data), Collections.reverseOrder(comp));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new ReversedArrayList<>(data).iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int fromInd = upperInd(fromElement, fromInclusive);
        int toInd = downInd(toElement, toInclusive);
        if (fromInd > toInd) {
            if (comp.compare(fromElement, toElement) == 0) {
                return new ArraySet<>(Collections.emptyList(), comp);
            }
        }
        return new ArraySet<>(data.subList(fromInd, toInd + 1), comp);
    }

    @Override
    public Comparator<? super E> comparator() {
        if (comp == Comparator.naturalOrder())
            return null;
        return comp;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new ArraySet<>(data.subList(0, downInd(toElement, inclusive) + 1), comp);
    }


    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new ArraySet<>(data.subList(upperInd(fromElement, inclusive), size()), comp);
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(0);
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(size() - 1);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(data, o, (Comparator) comp) >= 0;
    }

    private class ReversedArrayList<E> extends AbstractList<E> implements RandomAccess  {
        private final List<E> data;
        private final boolean used;

        public ReversedArrayList(List<E> data) {
            if (data instanceof ReversedArrayList) {
                ReversedArrayList<E> ex = (ReversedArrayList<E>) data;
                this.data = ex.data;
                used = !ex.used;
                return;
            }
            if (!(data instanceof RandomAccess)) {
                throw new IllegalArgumentException("Substrate must be random access");
            }
            this.data = data;
            used = false;
        }

//        @Override
//        public NavigableSet descendingSet() {
//            return super.descendingSet();
//        }

        @Override
        public E get(int index) {
            return used ? data.get(index) : data.get(size() - 1 - index);
        }

        @Override
        public int size() {
            return data.size();
        }
    }
}