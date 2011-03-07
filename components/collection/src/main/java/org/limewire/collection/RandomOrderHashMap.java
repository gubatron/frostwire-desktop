package org.limewire.collection;

import java.util.Iterator;
import java.util.Map;

/**
 * A variant of <tt>FixedSizeArrayHashMap</tt> that allows iterations over
 * its elements in random order. 
 */
public class RandomOrderHashMap<K, V> extends FixedSizeArrayHashMap<K, V> {
    
    /**
     * 
     */
    private static final long serialVersionUID = 7486635140443385580L;

    public RandomOrderHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }
    
    public RandomOrderHashMap(int maxCapacity, Map<? extends K, ? extends V> m) {
        super(maxCapacity, m);
    }

    public RandomOrderHashMap(int maxSize, int initialCapacity, float loadFactor) {
        super(maxSize, initialCapacity, loadFactor);
    }

    public RandomOrderHashMap(int maxCapacity) {
        super(maxCapacity);
    }

    @Override
    protected Iterator<Entry<K, V>> newEntryIterator2() {
        return new RandomIterator();
    }
    
    private class RandomIterator extends UnmodifiableIterator<Map.Entry<K, V>> {
        private final Iterator<Integer> sequence = new RandomSequence(size()).iterator();
        
        public boolean hasNext() {
            return sequence.hasNext();
        }
        
        public Map.Entry<K, V> next() {
            return getEntryAt(sequence.next());
        }
    }

}
